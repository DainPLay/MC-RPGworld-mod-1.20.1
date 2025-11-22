package net.dainplay.rpgworldmod.mixin;

import net.dainplay.rpgworldmod.effect.ModEffects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.entity.EntitySelector;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractContainerScreen.class)
public abstract class AbstractContainerScreenMixin {


    @Inject(method = "keyPressed", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/inventory/AbstractContainerScreen;slotClicked(Lnet/minecraft/world/inventory/Slot;IILnet/minecraft/world/inventory/ClickType;)V", shift = At.Shift.BEFORE, opcode = Opcodes.PUTFIELD), cancellable = true)
    private void keyPressedParalysisCheck(int pKeyCode, int pScanCode, int pModifiers, CallbackInfoReturnable<Boolean> info) {
        if (Minecraft.getInstance().player != null && EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(Minecraft.getInstance().player) && Minecraft.getInstance().player.getEffect(ModEffects.PARALYSIS.get()) != null) {
            info.setReturnValue(true);
        }
    }


    @Inject(method = "checkHotbarKeyPressed", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/inventory/AbstractContainerScreen;slotClicked(Lnet/minecraft/world/inventory/Slot;IILnet/minecraft/world/inventory/ClickType;)V", shift = At.Shift.BEFORE, opcode = Opcodes.PUTFIELD), cancellable = true)
    private void checkHotbarKeyPressedParalysisCheck(int pKeyCode, int pScanCode, CallbackInfoReturnable<Boolean> info) {
        if (Minecraft.getInstance().player != null && EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(Minecraft.getInstance().player) && Minecraft.getInstance().player.getEffect(ModEffects.PARALYSIS.get()) != null) {
            info.setReturnValue(true);
        }
    }
}
