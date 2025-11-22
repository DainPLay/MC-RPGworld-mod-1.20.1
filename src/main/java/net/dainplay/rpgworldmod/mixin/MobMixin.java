package net.dainplay.rpgworldmod.mixin;

import net.dainplay.rpgworldmod.effect.ParalysisEffect;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Mob.class)
public abstract class MobMixin {
    Mob entity = (Mob) (Object) this;
    @Inject(method = "canReplaceCurrentItem", at = @At(value = "HEAD"), cancellable = true)
    private void canReplaceCurrentItemParalysisCheck(ItemStack pCandidate, ItemStack pExisting, CallbackInfoReturnable<Boolean> info) {
        if (!(entity instanceof AbstractSkeleton) && entity.getAttribute(Attributes.MOVEMENT_SPEED).getModifier(ParalysisEffect.MODIFIER_UUID) != null && !pExisting.isEmpty()) {
            info.setReturnValue(false);
        }
    }
}
