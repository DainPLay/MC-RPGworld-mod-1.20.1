package net.dainplay.rpgworldmod.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.dainplay.rpgworldmod.effect.ModEffects;
import net.dainplay.rpgworldmod.effect.ParalysisEffect;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin<T extends LivingEntity, M extends EntityModel<T>> {

	@Inject(method = "render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", at = @At(value = "HEAD"), cancellable = true)
	private void renderMirroringCheck(T pEntity, float pEntityYaw, float pPartialTicks, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, CallbackInfo ci) {
		if (pEntity.hasEffect(ModEffects.MIRRORING.get()) && pEntity.isInvisible() && !pEntity.isSpectator()) {
			LivingEntityRenderer renderer = (LivingEntityRenderer) (Object) this;
			net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.client.event.RenderLivingEvent.Post<T, M>(pEntity, renderer, pPartialTicks, pPoseStack, pBuffer, pPackedLight));
			ci.cancel();
		}
	}

	@Inject(method = "isShaking", at = @At(value = "HEAD"), cancellable = true)
	private void renderParalysisCheck(LivingEntity livingEntity, CallbackInfoReturnable<Boolean> info) {
		if (livingEntity.getAttribute(Attributes.MOVEMENT_SPEED).getModifier(ParalysisEffect.MODIFIER_UUID) != null) {
			info.setReturnValue(true);
		}
	}

}
