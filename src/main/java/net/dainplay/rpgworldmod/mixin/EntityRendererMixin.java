package net.dainplay.rpgworldmod.mixin;

import net.dainplay.rpgworldmod.effect.ModEffects;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin<T extends Entity> {
	@Inject(method = "shouldRender", at = @At(value = "HEAD"), cancellable = true)
	private void shouldRenderMirroringCheck(T entity, Frustum pCamera, double pCamX, double pCamY, double pCamZ, CallbackInfoReturnable<Boolean> cir) {
		if (entity instanceof LivingEntity livingEntity && livingEntity.hasEffect(ModEffects.MIRRORING.get())) {
			AABB expandedBox = entity.getBoundingBox().inflate(10.0);
			if (pCamera.isVisible(expandedBox)) {
				cir.setReturnValue(true);
			}
		}
	}
}
