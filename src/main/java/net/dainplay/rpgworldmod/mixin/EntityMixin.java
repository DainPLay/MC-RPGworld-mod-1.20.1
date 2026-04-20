package net.dainplay.rpgworldmod.mixin;

import net.dainplay.rpgworldmod.effect.ModEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.horse.SkeletonHorse;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntityMixin {
	Entity entity = (Entity) (Object) this;

	@Inject(method = "setYRot", at = @At(value = "HEAD"), cancellable = true)
	private void setYRotParalysisCheck(CallbackInfo ci) {
		if (!(entity instanceof AbstractSkeleton) && !(entity instanceof SkeletonHorse) && entity instanceof LivingEntity
				&& ((LivingEntity) entity).hasEffect(ModEffects.PARALYSIS.get()) && EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(entity) && ((LivingEntity) entity).getEffect(ModEffects.PARALYSIS.get()).getAmplifier() >= 1)
			ci.cancel();
		if (entity instanceof LivingEntity && !(entity instanceof Player) && ((LivingEntity) entity).hasEffect(ModEffects.MOB_BECKON.get()) && EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(entity))
			ci.cancel();
	}


	@Unique
	private boolean wasCalledFrom(String method) {
		StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();


		for (StackTraceElement element : stackTraceElements) {
			if (element.getMethodName().equals(method)) {
				return true;
			}
		}
		return false;
	}

	@Inject(method = "setXRot", at = @At(value = "HEAD"), cancellable = true)
	private void setXRotParalysisCheck(CallbackInfo ci) {
		if (!(entity instanceof AbstractSkeleton) && !(entity instanceof SkeletonHorse) && entity instanceof LivingEntity
				&& ((LivingEntity) entity).hasEffect(ModEffects.PARALYSIS.get()) && EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(entity) && ((LivingEntity) entity).getEffect(ModEffects.PARALYSIS.get()).getAmplifier() >= 1)
			ci.cancel();
		if (entity instanceof LivingEntity && !(entity instanceof Player) && ((LivingEntity) entity).hasEffect(ModEffects.MOB_BECKON.get()) && EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(entity))
			ci.cancel();
	}

	@Inject(method = "isOnFire", at = @At(value = "HEAD"), cancellable = true)
	private void isOnFireIllusion(CallbackInfoReturnable<Boolean> cir) {
		if (entity instanceof LivingEntity livingEntity && livingEntity.hasEffect(ModEffects.BURN_ILLUSION.get()) && EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(entity)
		) {
			cir.setReturnValue(true);
			cir.cancel();
		}
	}

	@Inject(method = "playEntityOnFireExtinguishedSound", at = @At(value = "HEAD"), cancellable = true)
	private void dontPlayEntityOnFireExtinguishedSound(CallbackInfo ci) {
		if (entity.getRemainingFireTicks() <= 0 && entity instanceof LivingEntity livingEntity && livingEntity.hasEffect(ModEffects.BURN_ILLUSION.get()) && EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(entity)) {
			ci.cancel();
		}
	}
}
