package net.dainplay.rpgworldmod.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.dainplay.rpgworldmod.block.entity.custom.StareblossomBlockEntity;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

public class StareblossomBlockEntityRenderer implements BlockEntityRenderer<StareblossomBlockEntity> {
	private static final ResourceLocation BUD_TEXTURE =
			new ResourceLocation("rpgworldmod", "textures/block/stareblossom_bud.png");


	private static final float SIZE = 1.0f;
	private static final float OFFSET_Y = 0.625f;

	public StareblossomBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
	}

	@Override
	public void render(StareblossomBlockEntity blockEntity, float partialTick,
					   PoseStack poseStack, MultiBufferSource bufferSource,
					   int packedLight, int packedOverlay) {
		Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();

		poseStack.pushPose();


		poseStack.translate(0.5, 0.5, 0.5);


		Vec3 cameraPos = camera.getPosition();
		Vec3 blockPos = Vec3.atCenterOf(blockEntity.getBlockPos());
		Vec3 direction = cameraPos.subtract(blockPos);


		double angleY = Math.atan2(direction.x(), direction.z());


		poseStack.mulPose(Axis.YP.rotation((float) angleY));


		poseStack.mulPose(Axis.XP.rotationDegrees(-camera.getXRot()));


		poseStack.translate(0, OFFSET_Y - 0.5, 0.1);


		renderBillboard(poseStack, bufferSource, packedLight);

		poseStack.popPose();
	}

	private void renderBillboard(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
		VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.entityCutoutNoCull(BUD_TEXTURE));
		PoseStack.Pose pose = poseStack.last();


		float normalX = 0;
		float normalY = 0;
		float normalZ = -1;


		float u0 = 0.0f;
		float u1 = 1.0f;
		float v0 = 0.0f;
		float v1 = 1.0f;


		float size = SIZE / 2.0f;


		vertexConsumer.vertex(pose.pose(), -size, -size, 0.0F)
				.color(1.0F, 1.0F, 1.0F, 1F)
				.uv(0.0F, 1.0F)
				.overlayCoords(OverlayTexture.NO_OVERLAY)
				.uv2(packedLight)
				.normal(0.0F, 0.0F, 1.0F)
				.endVertex();


		vertexConsumer.vertex(pose.pose(), size, -size, 0.0F)
				.color(1.0F, 1.0F, 1.0F, 1F)
				.uv(1.0F, 1.0F)
				.overlayCoords(OverlayTexture.NO_OVERLAY)
				.uv2(packedLight)
				.normal(0.0F, 0.0F, 1.0F)
				.endVertex();


		vertexConsumer.vertex(pose.pose(), size, size, 0.0F)
				.color(1.0F, 1.0F, 1.0F, 1F)
				.uv(1.0F, 0.0F)
				.overlayCoords(OverlayTexture.NO_OVERLAY)
				.uv2(packedLight)
				.normal(0.0F, 0.0F, 1.0F)
				.endVertex();


		vertexConsumer.vertex(pose.pose(), -size, size, 0.0F)
				.color(1.0F, 1.0F, 1.0F, 1F)
				.uv(0.0F, 0.0F)
				.overlayCoords(OverlayTexture.NO_OVERLAY)
				.uv2(packedLight)
				.normal(0.0F, 0.0F, 1.0F)
				.endVertex();
	}

	@Override
	public boolean shouldRenderOffScreen(StareblossomBlockEntity blockEntity) {
		return true;
	}
}