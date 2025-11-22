package net.dainplay.rpgworldmod.mixin;

import net.dainplay.rpgworldmod.effect.ParalysisEffect;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Witch;
import net.minecraft.world.item.alchemy.Potion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(Witch.class)
public abstract class WitchMixin {
    Witch entity = (Witch) (Object) this;
    @Inject(method = "performRangedAttack", at = @At(value = "HEAD"), cancellable = true)
    private void performRangedAttackParalysisCheck(LivingEntity pTarget, float pDistanceFactor, CallbackInfo ci) {
        if (entity.getAttribute(Attributes.MOVEMENT_SPEED).getModifier(ParalysisEffect.MODIFIER_UUID) != null) {
            ci.cancel();
        }
    }

    @Redirect(method = "aiStep()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/monster/Witch;isDrinkingPotion()Z"))
    private boolean injected(Witch instance) {
        if (entity.getAttribute(Attributes.MOVEMENT_SPEED).getModifier(ParalysisEffect.MODIFIER_UUID) != null) {
            return true;
        }
        return entity.isDrinkingPotion();
    }
}
