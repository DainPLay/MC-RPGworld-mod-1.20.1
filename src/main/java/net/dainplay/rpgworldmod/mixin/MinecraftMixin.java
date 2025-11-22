package net.dainplay.rpgworldmod.mixin;

import net.dainplay.rpgworldmod.world.feature.ModConfiguredFeatures;
import net.minecraft.client.Minecraft;
import net.minecraft.sounds.Music;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static net.dainplay.rpgworldmod.util.FogEventHandler.isInRieWeald;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {

    @Inject(method = "getSituationalMusic", at = @At(value = "HEAD"), cancellable = true)
    private void setYRotParalysisCheck(CallbackInfoReturnable<Music> cir) {
        if (isInRieWeald()) cir.setReturnValue(ModConfiguredFeatures.RIE_WEALD_MUSIC_FOG);
    }


}
