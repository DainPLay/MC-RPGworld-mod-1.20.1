package net.dainplay.rpgworldmod.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.dainplay.rpgworldmod.block.entity.custom.BoundCampfireBlockEntity;
import net.dainplay.rpgworldmod.enchantment.ModEnchantments;
import net.dainplay.rpgworldmod.item.ModItems;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

public class BoundCampfireBlockRenderer implements BlockEntityRenderer<BoundCampfireBlockEntity> {
	private static final float HALF_SQRT_3 = (float)(Math.sqrt(3.0D) / 2.0D);
	private static final float ITEM_SIZE = 0.375F;
	static Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
	private final ItemRenderer itemRenderer;

	public static void setCamera(Camera value) {
		camera = value;
	}

	public BoundCampfireBlockRenderer(BlockEntityRendererProvider.Context pContext) {
		this.itemRenderer = pContext.getItemRenderer();
	}

	@Override
	public void render(BoundCampfireBlockEntity pBlockEntity, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, int pPackedOverlay) {
		Direction direction = pBlockEntity.getBlockState().getValue(CampfireBlock.FACING);
		NonNullList<ItemStack> nonnulllist = pBlockEntity.getItems();
		int i = (int)pBlockEntity.getBlockPos().asLong();

		for(int j = 0; j < nonnulllist.size(); ++j) {
			ItemStack itemstack = nonnulllist.get(j);
			if (!itemstack.isEmpty()) {
				pPoseStack.pushPose();
				pPoseStack.translate(0.5F, 0.44921875F, 0.5F);
				Direction direction1 = Direction.from2DDataValue((j + direction.get2DDataValue()) % 4);
				float f = -direction1.toYRot();
				pPoseStack.mulPose(Axis.YP.rotationDegrees(f));
				pPoseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
				pPoseStack.translate(-0.3125F, -0.3125F, 0.0F);
				pPoseStack.scale(ITEM_SIZE, ITEM_SIZE, ITEM_SIZE);
				this.itemRenderer.renderStatic(itemstack, ItemDisplayContext.FIXED, pPackedLight, pPackedOverlay, pPoseStack, pBuffer, pBlockEntity.getLevel(), i + j);
				pPoseStack.popPose();
			}
		}

		pPoseStack.pushPose();
		pPoseStack.translate(0.5, 0.5, 0.5);
		float rotationAngle = -direction.toYRot();
		pPoseStack.mulPose(Axis.YP.rotationDegrees(rotationAngle));
		ItemStack dummyStack = new ItemStack(ModItems.EMBER_SCROLL.get());
		CompoundTag nbtData = new CompoundTag();
		nbtData.putInt("SummonedObject", 1);
		dummyStack.setTag(nbtData);
		dummyStack.enchant(ModEnchantments.CONJURATION.get(), 1);
		pPoseStack.scale(1.0F, 1.0F, 1.0F);

		this.itemRenderer.renderStatic(
				dummyStack,
				ItemDisplayContext.NONE,
				pPackedLight,
				pPackedOverlay,
				pPoseStack,
				pBuffer,
				pBlockEntity.getLevel(),
				0
		);

		pPoseStack.popPose();

		if (pBlockEntity.shouldRenderBeams()) {
			renderRotatingBeams(pBlockEntity, pPartialTick, pPoseStack, pBuffer);
		}
	}

	private void renderRotatingBeams(BoundCampfireBlockEntity pBlockEntity, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBuffer) {
		VertexConsumer vertexconsumer = pBuffer.getBuffer(RenderType.lightning());
		pPoseStack.pushPose();

		pPoseStack.translate(0.5, 0.5, 0.5);

		Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
		Vec3 cameraPos = camera.getPosition();

		Vec3 beamPos = Vec3.atCenterOf(pBlockEntity.getBlockPos());

		Vec3 toCamera = cameraPos.subtract(beamPos).normalize();

		double horizontalAngleToCamera = Math.toDegrees(Math.atan2(toCamera.x, toCamera.z));
		double verticalAngleToCamera = Math.toDegrees(Math.asin(toCamera.y));

		float beamProgress = pBlockEntity.getBeamProgress(pPartialTick);
		float rotationAngle = pBlockEntity.getRotationAngle(pPartialTick);

		int beamCount = 12;
		for (int i = 0; i < beamCount; i++) {
			pPoseStack.pushPose();

			pPoseStack.mulPose(Axis.YP.rotationDegrees((float) horizontalAngleToCamera));
			pPoseStack.mulPose(Axis.XP.rotationDegrees((float) -verticalAngleToCamera));

			float angle = rotationAngle + (360f / beamCount) * i;
			pPoseStack.mulPose(Axis.ZP.rotationDegrees(angle));

			float beamLength = 2.0F * beamProgress;
			float beamWidth = 0.6F * beamProgress;

			Matrix4f matrix4f = pPoseStack.last().pose();
			int alpha = (int)(200 * beamProgress);

			vertex01(vertexconsumer, matrix4f, alpha, 0, 0, 255);
			vertex3(vertexconsumer, matrix4f, beamLength, beamWidth, 0, 0, 255);
			vertex4(vertexconsumer, matrix4f, beamLength, beamWidth, 0, 0, 255);
			vertex01(vertexconsumer, matrix4f, alpha, 0, 0, 255);
			vertex4(vertexconsumer, matrix4f, beamLength, beamWidth, 0, 0, 255);
			vertex2(vertexconsumer, matrix4f, beamLength, beamWidth, 0, 0, 255);

			pPoseStack.popPose();
		}
		pPoseStack.popPose();
	}

	// Обновленные методы с поддержкой синего цвета
	private static void vertex01(VertexConsumer pConsumer, Matrix4f pMatrix, int pAlpha, int r, int g, int b) {
		pConsumer.vertex(pMatrix, 0.0F, 0.0F, 0.0F).color(r, g, b, pAlpha).endVertex();
	}

	private static void vertex2(VertexConsumer pConsumer, Matrix4f pMatrix, float length, float width, int r, int g, int b) {
		pConsumer.vertex(pMatrix, -HALF_SQRT_3 * width, length, -0.5F * width).color(r, g, b, 0).endVertex();
	}

	private static void vertex3(VertexConsumer pConsumer, Matrix4f pMatrix, float length, float width, int r, int g, int b) {
		pConsumer.vertex(pMatrix, HALF_SQRT_3 * width, length, -0.5F * width).color(r, g, b, 0).endVertex();
	}

	private static void vertex4(VertexConsumer pConsumer, Matrix4f pMatrix, float length, float width, int r, int g, int b) {
		pConsumer.vertex(pMatrix, 0.0F, length, 1.0F * width).color(r, g, b, 0).endVertex();
	}
}