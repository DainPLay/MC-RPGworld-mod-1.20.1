package net.dainplay.rpgworldmod.mixin;

import net.dainplay.rpgworldmod.effect.ModEffects;
import net.dainplay.rpgworldmod.sounds.ModSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MouseHandler.class)
public class MouseHandlerMixin {

    @Shadow
    private double xpos;
    @Shadow
    private double ypos;
    @Shadow
    private double accumulatedDX;
    @Shadow
    private double accumulatedDY;
    @Unique
    private double frozenAccumulatedDX = xpos;;
    @Unique
    private double frozenAccumulatedDY = ypos;;

    @Inject(method = "turnPlayer", at = @At(value = "HEAD"), cancellable = true)
    private void MouseHandlerParalysisCheck(CallbackInfo ci) {
        if (Minecraft.getInstance().player != null && EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(Minecraft.getInstance().player) && Minecraft.getInstance().player.getEffect(ModEffects.PARALYSIS.get()) != null && Minecraft.getInstance().player.getEffect(ModEffects.PARALYSIS.get()).getAmplifier() >= 1) {
            this.accumulatedDX = this.frozenAccumulatedDX;
            this.accumulatedDY = this.frozenAccumulatedDY;
            ci.cancel();
        }
        else {
            this.frozenAccumulatedDX = this.accumulatedDX;
            this.frozenAccumulatedDY = this.accumulatedDY;
        }
    }
}
