package net.dainplay.rpgworldmod.mixin;

import net.dainplay.rpgworldmod.effect.ModEffects;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.horse.SkeletonHorse;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

    LivingEntity entity = (LivingEntity) (Object) this;
    //@ModifyVariable(method = "aiStep()V", at = @At("STORE"), ordinal = 1)
    //private double injected(double d6) {
    //    if (wasCalledFromaiStepMethod() && !(entity instanceof AbstractSkeleton) && !(entity instanceof SkeletonHorse)
    //        && entity.hasEffect(ModEffects.PARALYSIS.get()) && entity.getEffect(ModEffects.PARALYSIS.get()).getAmplifier() >= 1)
    //        return Mth.wrapDegrees(entity.lerpYRot) + d6;
    //    else return d6;
    //}

    private boolean wasCalledFromaiStepMethod() {
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();

        // Traversing the call stack to find the calling method
        for (StackTraceElement element : stackTraceElements) {
            if (element.getMethodName().equals("aiStep")) {
                return true; // The method was called from load method
            }
        }
        return false; // The method was not called from load method
    }
    @Inject(method = "tickHeadTurn", at = @At("HEAD"), cancellable = true)
    private void tickHeadTurnParalysisCheck(float p_21260_, float p_21261_, CallbackInfoReturnable<Float> cir) {
        if (!(entity instanceof AbstractSkeleton) && !(entity instanceof SkeletonHorse)
                && entity.hasEffect(ModEffects.PARALYSIS.get()) && EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(entity) && entity.getEffect(ModEffects.PARALYSIS.get()).getAmplifier() >= 1)
            cir.setReturnValue(p_21261_);
    }
}
