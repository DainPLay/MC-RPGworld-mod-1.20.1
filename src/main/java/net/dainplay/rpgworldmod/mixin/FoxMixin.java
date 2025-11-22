package net.dainplay.rpgworldmod.mixin;

import net.dainplay.rpgworldmod.effect.ParalysisEffect;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Fox;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Witch;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Fox.class)
public abstract class FoxMixin {
    Fox entity = (Fox) (Object) this;
    @Inject(method = "pickUpItem", at = @At(value = "HEAD"), cancellable = true)
    private void pickUpItemParalysisCheck(ItemEntity pItemEntity, CallbackInfo ci) {
        if(entity.getAttribute(Attributes.MOVEMENT_SPEED).getModifier(ParalysisEffect.MODIFIER_UUID) != null
                && !entity.getItemBySlot(EquipmentSlot.MAINHAND).isEmpty())
            ci.cancel();
    }
}
