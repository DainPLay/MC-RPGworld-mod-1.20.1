package net.dainplay.rpgworldmod.mixin;

import net.dainplay.rpgworldmod.network.ClientIllusionForceData;
import net.dainplay.rpgworldmod.network.PlayerIllusionForceProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.player.Input;
import net.minecraft.client.player.KeyboardInput;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardInput.class)
public abstract class MovementInputFromOptionsMixin extends Input {

    @Inject(method = "tick", at = @At("TAIL"))
    private void onTick(boolean pIsSneaking, float pSneakingSpeedMultiplier, CallbackInfo ci) {
        Player player = Minecraft.getInstance().player;
        if (player != null) {
                if (ClientIllusionForceData.get() > 0) {
                    this.forwardImpulse = 1.0F;
                    if (pIsSneaking) {
                        this.forwardImpulse *= pSneakingSpeedMultiplier;
                    }
                }
        }
    }
}