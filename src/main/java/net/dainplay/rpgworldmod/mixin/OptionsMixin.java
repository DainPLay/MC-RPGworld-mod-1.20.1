package net.dainplay.rpgworldmod.mixin;

import net.dainplay.rpgworldmod.util.ClientEyeViewHandler;
import net.minecraft.client.CameraType;
import net.minecraft.client.Options;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Options.class)
public class OptionsMixin {

    @Shadow
    private CameraType cameraType;

    @Inject(method = "setCameraType", at = @At("TAIL"))
    private void onSetCameraType(CallbackInfo ci) {
        if (ClientEyeViewHandler.isActive() && this.cameraType != CameraType.FIRST_PERSON) {
            this.cameraType = CameraType.FIRST_PERSON;
        }
    }
}