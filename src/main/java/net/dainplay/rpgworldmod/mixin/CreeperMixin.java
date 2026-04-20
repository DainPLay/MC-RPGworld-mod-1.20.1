package net.dainplay.rpgworldmod.mixin;

import net.dainplay.rpgworldmod.effect.ModEffects;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.monster.Creeper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Collection;
import java.util.stream.Collectors;

@Mixin(Creeper.class)
public abstract class CreeperMixin {
	@Redirect(
			method = "spawnLingeringCloud",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/monster/Creeper;getActiveEffects()Ljava/util/Collection;")
	)
	private Collection<MobEffectInstance> filterEffectsForCloud(Creeper creeper) {
		Collection<MobEffectInstance> original = creeper.getActiveEffects();

		return original.stream()
				.filter(effect -> {
					if (effect.getEffect().equals(ModEffects.MOSQUITOING.get())) {
						return false;
					}
					if (effect.getEffect().equals(ModEffects.MOB_BECKON.get())) {
						return false;
					}
					if (effect.getEffect().equals(ModEffects.BURN_ILLUSION.get())) {
						return false;
					}
					if (effect.getEffect().equals(ModEffects.NETHER_PORTAL_ILLUSION.get())) {
						return false;
					}
					return true;
				})
				.collect(Collectors.toList());
	}
}