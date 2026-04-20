package net.dainplay.rpgworldmod.mixin;

import net.dainplay.rpgworldmod.effect.ParalysisEffect;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntityRenderer.class)
public class LivingEntityRendererMixin {
	@Inject(method = "isShaking", at = @At(value = "HEAD"), cancellable = true)
	private void renderParalysisCheck(LivingEntity livingEntity, CallbackInfoReturnable<Boolean> info) {
		if (livingEntity.getAttribute(Attributes.MOVEMENT_SPEED).getModifier(ParalysisEffect.MODIFIER_UUID) != null) {
			info.setReturnValue(true);
		}
	}
}
