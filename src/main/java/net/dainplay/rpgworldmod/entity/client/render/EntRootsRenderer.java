package net.dainplay.rpgworldmod.entity.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.dainplay.rpgworldmod.RPGworldMod;
import net.dainplay.rpgworldmod.entity.client.model.EntRootsModel;
import net.dainplay.rpgworldmod.entity.projectile.EntRootsEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class EntRootsRenderer extends EntityRenderer<EntRootsEntity> {

	private static final ResourceLocation TEXTURE = new ResourceLocation(RPGworldMod.MOD_ID, "textures/entity/ent/roots.png");
	private final EntRootsModel model;

	public EntRootsRenderer(EntityRendererProvider.Context context) {
		super(context);
		this.model = new EntRootsModel(context.bakeLayer(EntRootsModel.LAYER_LOCATION));
	}

	@Override
	public void render(EntRootsEntity entity, float entityYaw, float partialTicks, PoseStack poseStack,
					   MultiBufferSource buffer, int packedLight) {

		poseStack.pushPose();

		int maxLifetime = 20;
		int activeTime = entity.tickCount - entity.getWarmupDelay();

		if (activeTime < 0) {
			poseStack.popPose();
			return;
		}

		float progress = Mth.clamp((activeTime + partialTicks) / (float)maxLifetime, 0.0f, 1.0f);
		float maxHeight = 1f;

		// Разделяем анимацию на три фазы:
		// 1. Быстрый подъем (0-30% времени)
		// 2. Стояние на месте (30-70% времени)
		// 3. Медленное опускание (70-100% времени)

		float yOffset;
		if (progress < 0.3f) {
			// Фаза 1: Быстрый подъем
			float phaseProgress = progress / 0.3f;
			yOffset = Mth.sin(phaseProgress * Mth.HALF_PI) * maxHeight;
		} else if (progress < 0.7f) {
			// Фаза 2: Стояние на месте
			yOffset = maxHeight;
		} else {
			// Фаза 3: Медленное опускание
			float phaseProgress = (progress - 0.7f) / 0.3f;
			yOffset = Mth.cos(phaseProgress * Mth.HALF_PI) * maxHeight;
		}

		// Применяем смещение
		poseStack.translate(0.0D, yOffset, 0.0D);

		// Легкое покачивание
		if (progress < 0.7f) {
			float sway = Mth.sin(progress * Mth.PI * 4) * 0.01f;
			poseStack.translate(sway, 0.0D, sway);
		}

		this.model.renderToBuffer(poseStack, buffer.getBuffer(this.model.renderType(TEXTURE)),
				packedLight, OverlayTexture.NO_OVERLAY, 1.0f, 1.0f, 1.0f, 1.0f);

		poseStack.popPose();
		super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
	}

	@Override
	public ResourceLocation getTextureLocation(EntRootsEntity entity) {
		return TEXTURE;
	}
}