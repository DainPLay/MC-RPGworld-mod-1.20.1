package net.dainplay.rpgworldmod.mixin;

import net.dainplay.rpgworldmod.util.MultiParentHelper;
import net.minecraft.advancements.Advancement;
import net.minecraft.client.gui.screens.advancements.AdvancementTab;
import net.minecraft.client.gui.screens.advancements.AdvancementsScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Mixin(AdvancementsScreen.class)
public abstract class AdvancementsScreenMixin {
	@Shadow private Map<Advancement, AdvancementTab> tabs;

	@Inject(method = "getTab", at = @At("HEAD"), cancellable = true)
	private void mp$getTab(Advancement adv, CallbackInfoReturnable<AdvancementTab> cir) {
		Set<Advancement> seen = new HashSet<>();
		Deque<Advancement> q = new ArrayDeque<>();
		q.add(adv);
		while (!q.isEmpty()) {
			Advancement a = q.poll();
			if (!seen.add(a)) continue;
			AdvancementTab tab = this.tabs.get(a);
			if (tab != null) { cir.setReturnValue(tab); return; }
			for (Advancement p : MultiParentHelper.getAllParents(a)) q.add(p);
		}
		cir.setReturnValue(null);
	}
}