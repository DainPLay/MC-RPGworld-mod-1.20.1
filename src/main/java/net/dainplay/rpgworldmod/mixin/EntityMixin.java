package net.dainplay.rpgworldmod.mixin;

import net.dainplay.rpgworldmod.effect.ModEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.horse.SkeletonHorse;
import net.minecraft.world.entity.monster.AbstractSkeleton;
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
	}


	@Unique
	private boolean wasCalledFrom(String method) {
		StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();

		// Traversing the call stack to find the calling method
		for (StackTraceElement element : stackTraceElements) {
			if (element.getMethodName().equals(method)) {
				return true; // The method was called from load method
			}
		}
		return false; // The method was not called from load method
	}

	@Inject(method = "setXRot", at = @At(value = "HEAD"), cancellable = true)
	private void setXRotParalysisCheck(CallbackInfo ci) {
		if (!(entity instanceof AbstractSkeleton) && !(entity instanceof SkeletonHorse) && entity instanceof LivingEntity
				&& ((LivingEntity) entity).hasEffect(ModEffects.PARALYSIS.get()) && EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(entity) && ((LivingEntity) entity).getEffect(ModEffects.PARALYSIS.get()).getAmplifier() >= 1)
			ci.cancel();
	}

	@Inject(method = "isOnFire", at = @At(value = "HEAD"), cancellable = true)
	private void isOnFireIllusion(CallbackInfoReturnable<Boolean> cir) {
		if (entity instanceof LivingEntity livingEntity && livingEntity.hasEffect(ModEffects.BURN_ILLUSION.get()) && EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(entity)
				/*&& livingEntity.getAttribute(Attributes.MOVEMENT_SPEED) != null
				&& livingEntity.getAttribute(Attributes.MOVEMENT_SPEED).getModifier(BurnIllusionEffect.MODIFIER_UUID) != null*/) {
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
