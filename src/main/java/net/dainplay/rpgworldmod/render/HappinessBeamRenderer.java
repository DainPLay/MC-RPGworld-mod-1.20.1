package net.dainplay.rpgworldmod.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.dainplay.rpgworldmod.effect.ModEffects;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;
import org.joml.Vector3f;

@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class HappinessBeamRenderer {
	private static final float HALF_SQRT_3 = (float) (Math.sqrt(3.0D) / 2.0D);

	@SubscribeEvent
	public static void onRenderLivingPost(RenderLivingEvent.Post<?, ?> event) {
		LivingEntity entity = event.getEntity();

		if (entity.hasEffect(ModEffects.HAPPINESS.get()) && !entity.isSpectator() && !entity.isInvisible()) {

			var effect = entity.getEffect(ModEffects.HAPPINESS.get());
			if (effect != null) {
				renderRegenerationBeams(
						entity,
						effect.getAmplifier(),
						effect.getDuration(),
						event.getPartialTick(),
						event.getPoseStack(),
						event.getMultiBufferSource()
				);
			}
		}
	}

	private static void renderRegenerationBeams(
			LivingEntity entity,
			int amplifier,
			int duration,
			float partialTick,
			PoseStack poseStack,
			MultiBufferSource bufferSource
	) {
		if (duration == -1) duration = 2000;
		VertexConsumer vertexconsumer = bufferSource.getBuffer(RenderType.lightning());
		poseStack.pushPose();

		// Смещаем к центру сущности
		float yOffset = entity.getBbHeight() * 0.5F;
		poseStack.translate(0.0F, yOffset, 0.0F);

		// Получаем камеру
		Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
		Vec3 cameraPos = camera.getPosition();
		Vector3f lookVector3f = camera.getLookVector();
		Vec3 cameraLookDir = new Vec3(lookVector3f.x(), lookVector3f.y(), lookVector3f.z());

		Vec3 entityPos = entity.position().add(0, yOffset, 0);

		// Вычисляем радиус (от 1.5 до 0 в последние 10 секунд)
		float radius = 1.5F;
		int fadeTime = 10 * 20; // 10 секунд в тиках
		if (duration < fadeTime) {
			radius = 1.5F * (duration / (float) fadeTime);
		}

		float speedMultiplier = Math.min(2.5F, 1F + 0.375F * amplifier);

		// Размещение лучей позади моба относительно камеры
		float backwardOffset = -0.25f;
		Vec3 backwardOffsetVector = cameraLookDir.scale(-backwardOffset);
		Vec3 targetPos = entityPos.add(backwardOffsetVector);

		// Анимация с учетом уровня эффекта
		float animationProgress = (entity.tickCount + partialTick) * 0.005F * speedMultiplier;

		int beamCount = 10;
		for (int i = 0; i < beamCount; i++) {
			poseStack.pushPose();

			poseStack.translate(
					targetPos.x - entityPos.x,
					targetPos.y - entityPos.y,
					targetPos.z - entityPos.z
			);

			// Направляем лучи на камеру
			Vec3 directionToCamera = cameraPos.subtract(targetPos).normalize();
			double horizontalAngle = Math.toDegrees(Math.atan2(directionToCamera.x, directionToCamera.z));
			double verticalAngle = Math.toDegrees(Math.asin(directionToCamera.y));

			float angle = animationProgress * 360F + (360f / beamCount) * i;

			poseStack.mulPose(Axis.YP.rotationDegrees((float) horizontalAngle));
			poseStack.mulPose(Axis.XP.rotationDegrees((float) -verticalAngle));
			poseStack.mulPose(Axis.ZP.rotationDegrees(angle));

			Matrix4f matrix4f = poseStack.last().pose();

			// Белый цвет для эффекта регенерации
			int alpha = duration < fadeTime ? 128 * duration / fadeTime : 128;
			vertex01(vertexconsumer, matrix4f, alpha);
			vertex3(vertexconsumer, matrix4f, radius, 0.5F);
			vertex4(vertexconsumer, matrix4f, radius, 0.5F);
			vertex01(vertexconsumer, matrix4f, alpha);
			vertex4(vertexconsumer, matrix4f, radius, 0.5F);
			vertex2(vertexconsumer, matrix4f, radius, 0.5F);

			poseStack.popPose();
		}
		poseStack.popPose();
	}

	private static void vertex01(VertexConsumer pConsumer, Matrix4f pMatrix, int alpha) {
		pConsumer.vertex(pMatrix, 0.0F, 0.0F, 0.0F).color(255, 255, 255, alpha).endVertex();
	}

	private static void vertex2(VertexConsumer pConsumer, Matrix4f pMatrix, float p_253704_, float p_253701_) {
		pConsumer.vertex(pMatrix, -HALF_SQRT_3 * p_253701_, p_253704_, -0.5F * p_253701_)
				.color(0, 0, 0, 0).endVertex();
	}

	private static void vertex3(VertexConsumer pConsumer, Matrix4f pMatrix, float p_253729_, float p_254030_) {
		pConsumer.vertex(pMatrix, HALF_SQRT_3 * p_254030_, p_253729_, -0.5F * p_254030_)
				.color(0, 0, 0, 0).endVertex();
	}

	private static void vertex4(VertexConsumer pConsumer, Matrix4f pMatrix, float p_253649_, float p_253694_) {
		pConsumer.vertex(pMatrix, 0.0F, p_253649_, 1.0F * p_253694_)
				.color(0, 0, 0, 0).endVertex();
	}
}