package net.dainplay.rpgworldmod.entity.client.render;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.dainplay.rpgworldmod.RPGworldMod;
import net.dainplay.rpgworldmod.entity.client.model.RazorleafHeartLayer;
import net.dainplay.rpgworldmod.entity.client.model.RazorleafModel;
import net.dainplay.rpgworldmod.entity.client.model.RazorleafTongueLayer;
import net.dainplay.rpgworldmod.entity.custom.Fireflantern;
import net.dainplay.rpgworldmod.entity.custom.Razorleaf;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public class RazorleafRenderer extends MobRenderer<Razorleaf, RazorleafModel> {
	private static final float HALF_SQRT_3 = (float)(Math.sqrt(3.0D) / 2.0D);
	public static final ResourceLocation TEXTURE = new ResourceLocation(RPGworldMod.MOD_ID, "textures/entity/razorleaf/razorleaf.png");
	public static final ResourceLocation TEXTURE_LOW_HEALTH_1 = new ResourceLocation(RPGworldMod.MOD_ID, "textures/entity/razorleaf/razorleaf_low_health_1.png");
	public static final ResourceLocation TEXTURE_LOW_HEALTH_2 = new ResourceLocation(RPGworldMod.MOD_ID, "textures/entity/razorleaf/razorleaf_low_health_2.png");

	public RazorleafRenderer(EntityRendererProvider.Context context) {
		super(context, new RazorleafModel(context.bakeLayer(RazorleafModel.LAYER_LOCATION)), 0.5f);
		this.addLayer(new RazorleafTongueLayer<>(this));
		this.addLayer(new RazorleafHeartLayer<>(this));
	}

	@Override
	public ResourceLocation getTextureLocation(Razorleaf entity) {
		if (entity.getHealth() < entity.getMaxHealth() / 3) return TEXTURE_LOW_HEALTH_2;
		if (entity.getHealth() < entity.getMaxHealth() / 3 * 2) return TEXTURE_LOW_HEALTH_1;
		return TEXTURE;
	}

	@Override
	public void render(Razorleaf pEntity, float pEntityYaw, float pPartialTicks, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight) {
		super.render(pEntity, pEntityYaw, pPartialTicks, pPoseStack, pBuffer, pPackedLight);
		if(pEntity.getState() != Razorleaf.State.EXTINGUISHED) return;
		VertexConsumer vertexconsumer2 = pBuffer.getBuffer(RenderType.lightning());
		pPoseStack.pushPose();
		pPoseStack.translate(0.0F, 1.2F, 0.0F);

		// Получаем камеру клиента
		Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
		Vec3 cameraPos = camera.getPosition();

		// Получаем вектор взгляда камеры как Vector3f и преобразуем в Vec3
		Vector3f lookVector3f = camera.getLookVector();
		Vec3 cameraLookDir = new Vec3(lookVector3f.x(), lookVector3f.y(), lookVector3f.z());

		Vec3 entityPos = pEntity.position().add(0, 1.2F, 0);

		// Вычисляем направление от моба к камере
		Vec3 toCamera = cameraPos.subtract(entityPos).normalize();

		// Для размещения лучей позади моба относительно камеры
		// Используем обратное направление (от камеры к мобу)
		float backwardOffset = -0.25f;
		Vec3 backwardOffsetVector = cameraLookDir.scale(-backwardOffset);

		// Вычисляем целевую позицию для лучей (позади моба относительно камеры)
		Vec3 targetPos = entityPos.add(backwardOffsetVector);

		float animationProgress = (pEntity.tickCount + pPartialTicks) * 0.005F;

		int beamCount = 10;
		for (int i = 0; i < beamCount; i++) {
			pPoseStack.pushPose();

			pPoseStack.translate(targetPos.x - entityPos.x, targetPos.y - entityPos.y, targetPos.z - entityPos.z);

			// Направляем лучи на камеру
			Vec3 directionToCamera = cameraPos.subtract(targetPos).normalize();
			double horizontalAngleToCamera = Math.toDegrees(Math.atan2(directionToCamera.x, directionToCamera.z));
			double verticalAngleToCamera = Math.toDegrees(Math.asin(directionToCamera.y));

			float angle = animationProgress * 360F + (360f / beamCount) * i;

			// Поворачиваем лучи чтобы смотреть на камеру
			pPoseStack.mulPose(Axis.YP.rotationDegrees((float) horizontalAngleToCamera));
			pPoseStack.mulPose(Axis.XP.rotationDegrees((float) -verticalAngleToCamera));
			pPoseStack.mulPose(Axis.ZP.rotationDegrees(angle));

			float f3 = 1F;
			float f4 = 0.5F;
			Matrix4f matrix4f = pPoseStack.last().pose();
			int j = 128;
			vertex01(vertexconsumer2, matrix4f, j);
			vertex3(vertexconsumer2, matrix4f, f3, f4);
			vertex4(vertexconsumer2, matrix4f, f3, f4);
			vertex01(vertexconsumer2, matrix4f, j);
			vertex4(vertexconsumer2, matrix4f, f3, f4);
			vertex2(vertexconsumer2, matrix4f, f3, f4);
			pPoseStack.popPose();
		}
		pPoseStack.popPose();
	}
	private static void vertex01(VertexConsumer pConsumer, Matrix4f pMatrix, int pAlpha) {
		pConsumer.vertex(pMatrix, 0.0F, 0.0F, 0.0F).color(255, 255, 255, pAlpha).endVertex();
	}

	private static void vertex2(VertexConsumer pConsumer, Matrix4f pMatrix, float p_253704_, float p_253701_) {
		pConsumer.vertex(pMatrix, -HALF_SQRT_3 * p_253701_, p_253704_, -0.5F * p_253701_).color(255, 216, 0, 0).endVertex();
	}

	private static void vertex3(VertexConsumer pConsumer, Matrix4f pMatrix, float p_253729_, float p_254030_) {
		pConsumer.vertex(pMatrix, HALF_SQRT_3 * p_254030_, p_253729_, -0.5F * p_254030_).color(255, 216, 0, 0).endVertex();
	}

	private static void vertex4(VertexConsumer pConsumer, Matrix4f pMatrix, float p_253649_, float p_253694_) {
		pConsumer.vertex(pMatrix, 0.0F, p_253649_, 1.0F * p_253694_).color(255, 216, 0, 0).endVertex();
	}

	protected void setupRotations(Razorleaf pEntityLiving, PoseStack pMatrixStack, float pAgeInTicks, float pRotationYaw, float pPartialTicks) {
		super.setupRotations(pEntityLiving, pMatrixStack, pAgeInTicks, pRotationYaw, pPartialTicks);
	}
}