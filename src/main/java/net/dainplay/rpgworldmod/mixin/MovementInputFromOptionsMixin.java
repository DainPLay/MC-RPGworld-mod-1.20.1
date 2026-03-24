package net.dainplay.rpgworldmod.mixin;

import net.dainplay.rpgworldmod.network.ClientIllusionForceData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.player.Input;
import net.minecraft.client.player.KeyboardInput;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardInput.class)
public abstract class MovementInputFromOptionsMixin extends Input {
    @Shadow
    private final Options options;

    protected MovementInputFromOptionsMixin(Options options) {
        this.options = options;
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void onTick(boolean pIsSneaking, float pSneakingSpeedMultiplier, CallbackInfo ci) {
        Player player = Minecraft.getInstance().player;
        if (player != null) {
                if (ClientIllusionForceData.getIllusionForce() > 0) {
                    if(ClientIllusionForceData.isEnt()) {
                        this.forwardImpulse = 1.0F;
                        if (pIsSneaking) {
                            this.forwardImpulse *= pSneakingSpeedMultiplier;
                        }
                    }
                    else {
                        this.forwardImpulse = options.keyUp.isDown() ? 1.0F : 0.0F;
                        if (pIsSneaking) {
                            this.forwardImpulse *= pSneakingSpeedMultiplier;
                        }
                    }
                }
        }
    }
}