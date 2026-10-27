package net.dainplay.rpgworldmod.mixin.compat.betteradvancements;

import betteradvancements.common.gui.BetterAdvancementTab;
import betteradvancements.common.gui.BetterAdvancementWidget;
import net.dainplay.rpgworldmod.util.IBetterAdvancementWidgetAccess;
import net.dainplay.rpgworldmod.util.IAdvancementTabAccess;
import net.dainplay.rpgworldmod.util.MultiParentHelper;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;
import java.util.*;

@Pseudo
@Mixin(value = BetterAdvancementWidget.class, remap = false)
public abstract class BetterAdvancementWidgetMixin implements IBetterAdvancementWidgetAccess {
    @Shadow @Final private BetterAdvancementTab betterAdvancementTabGui;
    @Shadow @Final private Advancement advancement;
    @Shadow @Mutable protected int x;
    @Shadow @Mutable protected int y;
    @Shadow @Nullable private BetterAdvancementWidget parent;
    @Shadow @Final private List<BetterAdvancementWidget> children;
    @Shadow @Final private DisplayInfo displayInfo;
    @Shadow private AdvancementProgress advancementProgress;

    @Override public BetterAdvancementWidget mp_getParent() { return this.parent; }
    @Override public List<BetterAdvancementWidget> mp_getChildren() { return this.children; }
    @Override public Advancement mp_getAdvancement() { return this.advancement; }
    @Override public void mp_setY(int y) { this.y = y; }
    @Override public void mp_setX(int x) { this.x = x; }
    @Override public boolean mp_isVisible() { return multiparents$isVisible(); }
    @Override public AdvancementProgress mp_getProgress() { return this.advancementProgress; }

    // ---- helpers ----

    @Unique
    private boolean multiparents$isDone(Advancement adv) {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.getConnection() == null) return false;
        AdvancementProgress prog = mc.getConnection().getAdvancements().progress.get(adv);
        return prog != null && prog.isDone();
    }

    @Unique
    private boolean multiparents$isVisible() {
        if (this.displayInfo.isHidden() && !multiparents$isDone(this.advancement)) return false;
        if (multiparents$isDone(this.advancement)) return true;

        List<Advancement> parents = MultiParentHelper.getAllParents(this.advancement);
        if (parents.isEmpty()) {
            return multiparents$anyDescendantDone(this.advancement);
        }
        int depth = MultiParentHelper.getEffectiveShowDepth(this.advancement);
        return multiparents$anyAncestorDone(this.advancement, depth);
    }

    @Unique
    private boolean multiparents$anyAncestorDone(Advancement adv, int levelsUp) {
        if (levelsUp <= 0) return false;
        for (Advancement p : MultiParentHelper.getAllParents(adv)) {
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
    private BetterAdvancementWidget multiparents$findVisibleAncestor(Advancement start) {
        Set<Advancement> seen = new HashSet<>();
        Deque<Advancement> q = new ArrayDeque<>();
        q.add(start);
        while (!q.isEmpty()) {
            Advancement a = q.poll();
            if (!seen.add(a)) continue;
            BetterAdvancementWidget w = this.betterAdvancementTabGui.getWidget(a);
            if (w != null && a.getDisplay() != null
                    && ((IBetterAdvancementWidgetAccess)(Object)w).mp_isVisible()) {
                return w;
            }
            for (Advancement p : MultiParentHelper.getAllParents(a)) q.add(p);
        }
        return null;
    }

    // ---- hooks ----

    @Inject(method = "getAdvancementProgress", at = @At("TAIL"))
    private void mp$onSetProgress(AdvancementProgress pProgress, CallbackInfo ci) {
        if (this.betterAdvancementTabGui instanceof IAdvancementTabAccess acc) {
            acc.mp_markDirty();
        }
    }

    @Inject(method = "attachToParent", at = @At("HEAD"), cancellable = true)
    private void multiparents$attachToParent(CallbackInfo ci) {
        List<Advancement> parents = MultiParentHelper.getAllParents(this.advancement);
        if (parents.isEmpty()) { ci.cancel(); return; }

        for (Advancement pa : parents) {
            BetterAdvancementWidget pw = multiparents$findVisibleAncestor(pa);
            if (pw != null && pw != (Object)this) {
                if (this.parent == null) this.parent = pw;
                List<BetterAdvancementWidget> pwChildren =
                        ((IBetterAdvancementWidgetAccess)(Object)pw).mp_getChildren();
                if (!pwChildren.contains((BetterAdvancementWidget)(Object)this)) {
                    pw.addGuiAdvancement((BetterAdvancementWidget)(Object)this);
                }
            }
        }
        ci.cancel();
    }

    @Inject(method = "isMouseOver", at = @At("HEAD"), cancellable = true)
    private void multiparents$isMouseOver(double pX, double pY, double pMouseX, double pMouseY,
                                          CallbackInfoReturnable<Boolean> cir) {
        if (!multiparents$isVisible()) cir.setReturnValue(false);
    }

    @Inject(method = "draw", at = @At("HEAD"), cancellable = true)
    private void multiparents$draw(GuiGraphics g, int pX, int pY, CallbackInfo ci) {
        if (!MultiParentHelper.DRAWN.get().add((Object)this)) {
            ci.cancel();
            return;
        }
        if (!multiparents$isVisible()) {
            for (BetterAdvancementWidget child : this.children) {
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

        // 1. Если текущий виджет невидим — не рисуем линии К ЕГО родителям.
        // Но обязательно рисуем связи для его детей!
        if (!multiparents$isVisible()) {
            for (BetterAdvancementWidget child : this.children) {
                child.drawConnectivity(g, pX, pY, shadow);
            }
            ci.cancel();
            return;
        }

        // 2. Ищем видимых родителей
        List<Advancement> parentsAdv = MultiParentHelper.getAllParents(this.advancement);
        List<BetterAdvancementWidget> pws = new ArrayList<>();
        for (Advancement pa : parentsAdv) {
            BetterAdvancementWidget pw = multiparents$findVisibleAncestor(pa);
            if (pw != null && pw != (Object)this && !pws.contains(pw)) {
                pws.add(pw);
            }
        }

        // 3. Если видимый родитель один (или их нет) — отдаём отрисовку ванильному коду
        if (pws.size() <= 1) {
            // Принудительно ставим правильного родителя, чтобы ванилла не нарисовала линию к скрытому
            this.parent = pws.isEmpty() ? null : pws.get(0);
            return;
        }

        // 4. Если родителей несколько — рисуем свои линии
        ci.cancel();

        int lineColor = MultiParentHelper.isGreyscale(this.advancement) ? 0xFF5555FF : -1;

        int trunkX = pX + this.x - 4;
        for (BetterAdvancementWidget pw : pws) {
            int minTrunk = pX + pw.getX() + 32;
            if (trunkX < minTrunk) trunkX = minTrunk;
        }

        for (BetterAdvancementWidget pw : pws) {
            multiparents$drawMultiLine(g, pw, pX, pY, shadow, trunkX, lineColor);
        }

        // Не забываем отрисовать связи для детей
        for (BetterAdvancementWidget child : this.children) {
            child.drawConnectivity(g, pX, pY, shadow);
        }
    }

    @Unique
    private void multiparents$drawMultiLine(GuiGraphics g, BetterAdvancementWidget parent,
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