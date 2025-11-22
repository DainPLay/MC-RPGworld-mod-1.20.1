package net.dainplay.rpgworldmod.mixin;

import net.dainplay.rpgworldmod.effect.ModEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.control.LookControl;
import net.minecraft.world.entity.animal.horse.SkeletonHorse;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LookControl.class)
public abstract class LookControlMixin {
    @Shadow
    protected final Mob mob;

    protected LookControlMixin(Mob mob) {
        this.mob = mob;
    }

    @Inject(method = "setLookAt(DDDFF)V", at = @At(value = "HEAD"), cancellable = true)
    private void setLookAtParalysisCheck(CallbackInfo ci) {
        if (!(mob instanceof AbstractSkeleton) && !(mob instanceof SkeletonHorse)
                && mob.hasEffect(ModEffects.PARALYSIS.get()) && EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(mob) && mob.getEffect(ModEffects.PARALYSIS.get()).getAmplifier() >= 1)
            ci.cancel();
    }
}
