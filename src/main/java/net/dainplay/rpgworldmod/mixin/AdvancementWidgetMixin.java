package net.dainplay.rpgworldmod.mixin;

import net.dainplay.rpgworldmod.util.IAdvancementTabAccess;
import net.dainplay.rpgworldmod.util.IAdvancementWidgetAccess;
import net.dainplay.rpgworldmod.util.MultiParentHelper;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.advancements.AdvancementTab;
import net.minecraft.client.gui.screens.advancements.AdvancementWidget;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Mixin(AdvancementWidget.class)
public abstract class AdvancementWidgetMixin implements IAdvancementWidgetAccess {
	@Shadow @Final private AdvancementTab tab;
	@Shadow @Final private Advancement advancement;
	@Shadow @Final @Mutable private int x;
	@Shadow @Final @Mutable private int y;
	@Shadow @Nullable private AdvancementWidget parent;
	@Shadow @Final private List<AdvancementWidget> children;
	@Shadow @Final private DisplayInfo display;

	@Override public AdvancementWidget mp_getParent() { return this.parent; }
	@Override public List<AdvancementWidget> mp_getChildren() { return this.children; }
	@Override public Advancement mp_getAdvancement() { return this.advancement; }
	@Override public void mp_setY(int y) { this.y = y; }
	@Override public void mp_setX(int x) { this.x = x; }
	@Override public boolean mp_isVisible() { return multiparents$isVisible(); }

	// ---- helpers ----

	@Unique
	private boolean multiparents$isDone(Advancement adv) {
		Minecraft mc = Minecraft.getInstance();
		if (mc == null) return false;
		var conn = mc.getConnection();
		if (conn == null) return false;
		AdvancementProgress prog = conn.getAdvancements().progress.get(adv);
		return prog != null && prog.isDone();
	}

	@Unique
	private List<Advancement> multiparents$allParentsOf(Advancement adv) {
		List<Advancement> result = new ArrayList<>();
		if (adv.getParent() != null) result.add(adv.getParent());
		for (Advancement p : MultiParentHelper.getParents(adv)) {
			if (!result.contains(p)) result.add(p);
		}
		return result;
	}

	@Unique
	private boolean multiparents$isVisible() {
		if (this.display.isHidden() && !multiparents$isDone(this.advancement)) return false;
		if (multiparents$isDone(this.advancement)) return true;

		List<Advancement> parents = multiparents$allParentsOf(this.advancement);
		if (parents.isEmpty()) {
			return multiparents$anyDescendantDone(this.advancement);
		}
		int depth = MultiParentHelper.getEffectiveShowDepth(this.advancement);
		return multiparents$anyAncestorDone(this.advancement, depth);
	}

	@Unique
	private boolean multiparents$anyAncestorDone(Advancement adv, int levelsUp) {
		if (levelsUp <= 0) return false;
		for (Advancement p : multiparents$allParentsOf(adv)) {
			if (multiparents$isDone(p)) return true;
			if (multiparents$anyAncestorDone(p, levelsUp - 1)) return true;
		}
		return false;
	}

	@Unique
	private boolean multiparents$anyDescendantDone(Advancement root) {
		Set<Advancement> seen = new HashSet<>();
		Deque<Advancement> q = new ArrayDeque<>();
		q.add(root);
		while (!q.isEmpty()) {
			Advancement a = q.poll();
			if (!seen.add(a)) continue;
			if (a != root && multiparents$isDone(a)) return true;
			for (Advancement child : a.getChildren()) q.add(child);
		}
		return false;
	}

	@Unique
	@Nullable
	private AdvancementWidget multiparents$findVisibleAncestor(Advancement start) {
		Set<Advancement> seen = new HashSet<>();
		Deque<Advancement> q = new ArrayDeque<>();
		q.add(start);
		while (!q.isEmpty()) {
			Advancement a = q.poll();
			if (!seen.add(a)) continue;
			AdvancementWidget w = this.tab.getWidget(a);
			if (w != null && a.getDisplay() != null) {
				if (((IAdvancementWidgetAccess)(Object)w).mp_isVisible()) {
					return w;
				}
			}
			for (Advancement p : MultiParentHelper.getAllParents(a)) q.add(p);
		}
		return null;
	}

	// ---- hooks ----

	@Inject(method = "setProgress", at = @At("TAIL"))
	private void mp$onSetProgress(AdvancementProgress pProgress, CallbackInfo ci) {
		if (this.tab instanceof IAdvancementTabAccess) {
			((IAdvancementTabAccess) this.tab).mp_markDirty();
		}
	}

	@Inject(method = "attachToParent", at = @At("HEAD"), cancellable = true)
	private void multiparents$attachToParent(CallbackInfo ci) {
		List<Advancement> parents = MultiParentHelper.getAllParents(this.advancement);
		if (parents.isEmpty()) { ci.cancel(); return; }

		for (Advancement pa : parents) {
			AdvancementWidget pw = multiparents$findVisibleAncestor(pa);
			if (pw != null && pw != (Object)this) {
				if (this.parent == null) this.parent = pw;
				if (!pw.children.contains((AdvancementWidget)(Object)this)) {
					pw.addChild((AdvancementWidget)(Object)this);
				}
			}
		}
		ci.cancel();
	}

	@Inject(method = "isMouseOver", at = @At("HEAD"), cancellable = true)
	private void multiparents$isMouseOver(int pX, int pY, int pMouseX, int pMouseY,
										  CallbackInfoReturnable<Boolean> cir) {
		if (!multiparents$isVisible()) {
			cir.setReturnValue(false);
		}
	}

	@Inject(method = "draw", at = @At("HEAD"), cancellable = true)
	private void multiparents$draw(GuiGraphics g, int pX, int pY, CallbackInfo ci) {
		if (!MultiParentHelper.DRAWN.get().add((Object)this)) {
			ci.cancel();
			return;
		}
		if (!multiparents$isVisible()) {
			for (AdvancementWidget child : this.children) {
				child.draw(g, pX, pY);
			}
			ci.cancel();
		}
	}

	@Inject(method = "drawConnectivity", at = @At("HEAD"), cancellable = true)
	private void multiparents$drawConnectivity(GuiGraphics g, int pX, int pY, boolean shadow, CallbackInfo ci) {
		if (!MultiParentHelper.CONN.get().add((Object)this)) {
			ci.cancel();
			return;
		}

		if (!multiparents$isVisible()) {
			ci.cancel();
			return;
		}

		List<Advancement> parentsAdv = multiparents$allParentsOf(this.advancement);
		List<AdvancementWidget> pws = new ArrayList<>();
		for (Advancement pa : parentsAdv) {
			AdvancementWidget pw = multiparents$findVisibleAncestor(pa);
			if (pw != null && pw != (Object)this && !pws.contains(pw)) {
				pws.add(pw);
			}
		}

		if (pws.size() <= 1) {
			return;
		}

		ci.cancel();

		int lineColor = MultiParentHelper.isGreyscale(this.advancement) ? 0xFF5555FF : -1;

		int trunkX = pX + this.x - 4;
		for (AdvancementWidget pw : pws) {
			int minTrunk = pX + pw.getX() + 30;
			if (trunkX < minTrunk) trunkX = minTrunk;
		}

		for (AdvancementWidget pw : pws) {
			multiparents$drawMultiLine(g, pw, pX, pY, shadow, trunkX, lineColor);
		}
	}

	@Unique
	private void multiparents$drawMultiLine(GuiGraphics g, AdvancementWidget parent,
											int pX, int pY, boolean shadow,
											int trunkX, int lineColor) {
		int pCx = pX + parent.getX() + 13;
		int pCy = pY + parent.getY() + 13;
		int cCx = pX + this.x + 13;
		int cCy = pY + this.y + 13;

		int color = shadow ? -16777216 : lineColor;

		if (shadow) {
			g.hLine(trunkX, pCx, pCy - 1, color);
			g.hLine(trunkX + 1, pCx, pCy, color);
			g.hLine(trunkX, pCx, pCy + 1, color);

			g.hLine(cCx, trunkX - 1, cCy - 1, color);
			g.hLine(cCx, trunkX - 1, cCy, color);
			g.hLine(cCx, trunkX - 1, cCy + 1, color);

			g.vLine(trunkX - 1, cCy, pCy, color);
			g.vLine(trunkX + 1, cCy, pCy, color);
		} else {
			g.hLine(trunkX, pCx, pCy, color);
			g.hLine(cCx, trunkX, cCy, color);
			g.vLine(trunkX, cCy, pCy, color);
		}
	}
}