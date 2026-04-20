package net.dainplay.rpgworldmod.mixin;

import net.dainplay.rpgworldmod.effect.ModEffects;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.alchemy.PotionUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(PotionUtils.class)
public abstract class PotionUtilsMixin {
	@Inject(at = @At("TAIL"), method = "addPotionTooltip(Ljava/util/List;Ljava/util/List;F)V")
	private static void addPotionTooltipRPGCheck(List<MobEffectInstance> pEffects, List<Component> pTooltips, float pDurationFactor, CallbackInfo ci) {
		for (MobEffectInstance mobeffectinstance : pEffects) {
			MobEffect mobeffect = mobeffectinstance.getEffect();
			if (mobeffect == ModEffects.MOSSIOSIS.get() || mobeffect == ModEffects.PARALYSIS.get()) {
				pTooltips.remove(pTooltips.size() - 1);
				pTooltips.remove(pTooltips.size() - 1);
			}
		}
	}
}
