package net.dainplay.rpgworldmod.mixin;

import net.dainplay.rpgworldmod.effect.ModEffects;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.animal.horse.SkeletonHorse;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RandomLookAroundGoal.class)
public abstract class RandomLookAroundGoalMixin {
    @Shadow
	private final Mob mob;

    protected RandomLookAroundGoalMixin(Mob mob) {
        this.mob = mob;
    }

    @Inject(method = "canUse", at = @At("HEAD"), cancellable = true)
    public void canUseWithParalysis(CallbackInfoReturnable<Boolean> cir) {
        if (shouldParalyzeLook()) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "canContinueToUse", at = @At("HEAD"), cancellable = true)
    public void canContinueToUseWithParalysis(CallbackInfoReturnable<Boolean> cir) {
        if (shouldParalyzeLook()) {
            cir.setReturnValue(false);
        }
    }

    private boolean shouldParalyzeLook() {
        // Ваша оригинальная логика проверки (можно вынести, чтобы не дублировать)
        return !(mob instanceof AbstractSkeleton) && !(mob instanceof SkeletonHorse)
                && mob.hasEffect(ModEffects.PARALYSIS.get())
                && EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(mob)
                && mob.getEffect(ModEffects.PARALYSIS.get()).getAmplifier() >= 1;
    }
}