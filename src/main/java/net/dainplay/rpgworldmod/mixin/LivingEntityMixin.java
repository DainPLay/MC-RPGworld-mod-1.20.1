package net.dainplay.rpgworldmod.mixin;

import net.dainplay.rpgworldmod.effect.ModEffects;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.horse.SkeletonHorse;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
	LivingEntity entity = (LivingEntity) (Object) this;


	private boolean wasCalledFromaiStepMethod() {
		StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();


		for (StackTraceElement element : stackTraceElements) {
			if (element.getMethodName().equals("aiStep")) {
				return true;
			}
		}
		return false;
	}

	@Inject(method = "tickHeadTurn", at = @At("HEAD"), cancellable = true)
	private void tickHeadTurnParalysisCheck(float p_21260_, float p_21261_, CallbackInfoReturnable<Float> cir) {
		if (!(entity instanceof AbstractSkeleton) && !(entity instanceof SkeletonHorse)
				&& entity.hasEffect(ModEffects.PARALYSIS.get()) && EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(entity) && entity.getEffect(ModEffects.PARALYSIS.get()).getAmplifier() >= 1)
			cir.setReturnValue(p_21261_);
		if (entity instanceof LivingEntity && !(entity instanceof Player) && ((LivingEntity) entity).hasEffect(ModEffects.MOB_BECKON.get()) && EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(entity))
			cir.setReturnValue(p_21261_);
	}
}
