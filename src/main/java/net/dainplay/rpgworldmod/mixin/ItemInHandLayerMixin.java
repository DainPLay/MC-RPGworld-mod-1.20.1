package net.dainplay.rpgworldmod.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.dainplay.rpgworldmod.RPGworldMod;
import net.dainplay.rpgworldmod.enchantment.ModEnchantments;
import net.dainplay.rpgworldmod.item.ModItems;
import net.dainplay.rpgworldmod.item.custom.ManaCostItem;
import net.dainplay.rpgworldmod.item.custom.NetherStarScrollItem;
import net.dainplay.rpgworldmod.item.custom.OrbitingItem;
import net.dainplay.rpgworldmod.item.custom.PillagerScrollItem;
import net.dainplay.rpgworldmod.item.custom.StaffItem;
import net.dainplay.rpgworldmod.render.ModRenderTypes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ArmedModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandLayer.class)
public class ItemInHandLayerMixin {
	ItemInHandLayer iihl = (ItemInHandLayer) (Object) this;

	@Final
	@Shadow
	private ItemInHandRenderer itemInHandRenderer;
	private static final float HALF_SQRT_3 = (float) (Math.sqrt(3.0D) / 2.0D);

	@Inject(method = "renderArmWithItem", at = @At(value = "HEAD"), cancellable = true)
	private void renderOrbitingItemTexture(
			LivingEntity entity,
			ItemStack itemStack,
			ItemDisplayContext displayContext,
			HumanoidArm arm,
			PoseStack poseStack,
			MultiBufferSource bufferSource,
			int packedLight,
			CallbackInfo ci
	) {
		if (itemStack.getItem() instanceof NetherStarScrollItem &&
				itemStack.getEnchantmentLevel(ModEnchantments.CONJURATION.get()) > 0 &&
				itemStack.getTag() != null &&
				itemStack.getTag().contains("isPickaxe", Tag.TAG_INT)) {
			ItemStack dummyStack = new ItemStack(ModItems.NETHER_STAR_SCROLL.get());
			CompoundTag nbtData = new CompoundTag();
			nbtData.putInt("SummonedObject", 1);
			if (itemStack.getTag().contains("summonProgress", Tag.TAG_INT))
				nbtData.putInt("summonProgress", itemStack.getTag().getInt("summonProgress"));
			dummyStack.setTag(nbtData);
			dummyStack.enchant(ModEnchantments.CONJURATION.get(), 1);

			poseStack.pushPose();
			((ArmedModel) iihl.getParentModel()).translateToHand(arm, poseStack);
			poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));
			poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
			boolean flag = arm == HumanoidArm.LEFT;
			poseStack.translate((float) (flag ? -1 : 1) / 16.0F, 0.125F, -0.625F);
			itemInHandRenderer.renderItem(entity, dummyStack, displayContext, flag, poseStack, bufferSource, packedLight);

			if (itemStack.getTag() != null && itemStack.getTag().contains("summonProgress", Tag.TAG_INT)) {
				int summonProgress = itemStack.getTag().getInt("summonProgress");
				if (summonProgress > 0) {
					poseStack.pushPose();
					poseStack.translate(0F, 0.5F, 0F);
					float progress = summonProgress / 20.0f;
					float partialTick = Minecraft.getInstance().getFrameTime();
					float age = entity.tickCount + partialTick;
					renderSummonBeams(poseStack, bufferSource, packedLight, progress, age);
					poseStack.popPose();
				}
			}

			poseStack.popPose();
			ci.cancel();
			return;
		}

		if (itemStack.getItem() instanceof OrbitingItem orbitingItem && orbitingItem.shouldOrbit(itemStack, entity)) {
			poseStack.pushPose();

			((ArmedModel) iihl.getParentModel()).translateToHand(arm, poseStack);
			boolean flag = arm == HumanoidArm.LEFT;
			boolean useCube = orbitingItem.useCubeEffect(itemStack, entity);
			float size = orbitingItem.getSize(itemStack, entity);
			float hsX = size;
			boolean isSlim = false;
			if (entity instanceof AbstractClientPlayer player) {
				isSlim = "slim".equals(player.getModelName());
			}
			if (useCube) {
				size = 0.15f;
				poseStack.translate((float) (flag ? -1 : 1) * -0.065F, 0.525F, 0.0F);

				if (isSlim) {
					hsX = 0.1125F;
				}
			} else {
				poseStack.translate((float) (flag ? orbitingItem.getX(itemStack, entity) * -1 : orbitingItem.getX(itemStack, entity)), orbitingItem.getY(itemStack, entity), orbitingItem.getZ(itemStack, entity));

				Matrix4f originalMatrix = poseStack.last().pose();
				Vector3f handPosition = originalMatrix.getTranslation(new Vector3f());

				poseStack.setIdentity();
				poseStack.translate(handPosition.x(), handPosition.y(), handPosition.z() + orbitingItem.getZOffset(itemStack, entity));

			}
			if (orbitingItem.shouldRotate(itemStack, entity)) {
				float f = (float) entity.tickCount % 360F + Minecraft.getInstance().getFrameTime();
				poseStack.mulPose(Axis.ZP.rotation((float) Math.toRadians(f) * -2.25F));
			}

			String textureString = orbitingItem.getTexture(itemStack, entity);
			int color = orbitingItem.getColor(itemStack, entity);
			VertexConsumer vertexConsumer;
			Matrix4f matrix = poseStack.last().pose();

			boolean hasEnoughMana = true;
			if (orbitingItem instanceof ManaCostItem) {
				if (itemStack.hasTag() && itemStack.getTag().contains("notEnoughMana")) hasEnoughMana = false;
			}

			if (hasEnoughMana && !(orbitingItem instanceof PillagerScrollItem
					&& entity.isUsingItem() && entity.getUseItem() == itemStack)) {
				if (useCube) {
					int animationSpeed = orbitingItem.getAnimationSpeed(itemStack, entity);
					int animationLength = orbitingItem.getAnimationLength(itemStack, entity);

					if (textureString != null && !textureString.isEmpty()) {
						int currentFrame = (entity.tickCount / animationSpeed) % animationLength;
						float frameHeight = 1.0F / animationLength;

						float vMin1 = currentFrame * frameHeight;
						float vMax1 = vMin1 + frameHeight;
						float vMin2 = ((currentFrame + 8) % animationLength) * frameHeight;
						float vMax2 = vMin2 + frameHeight;
						float vMin3 = ((currentFrame + 16) % animationLength) * frameHeight;
						float vMax3 = vMin3 + frameHeight;
						float vMin4 = ((currentFrame + 24) % animationLength) * frameHeight;
						float vMax4 = vMin4 + frameHeight;

						vertexConsumer = bufferSource.getBuffer(ModRenderTypes.GLOW_SPELL_EFFECT.apply(new ResourceLocation(RPGworldMod.MOD_ID, textureString + ".png")));


						drawCubeFace(vertexConsumer, matrix,
								-hsX, -size, size,
								hsX, -size, size,
								hsX, size, size,
								-hsX, size, size,
								0, vMax1, 1, vMax1, 1, vMin1, 0, vMin1,
								0, 0, 1,
								packedLight);

						drawCubeFace(vertexConsumer, matrix,
								hsX, -size, -size,
								-hsX, -size, -size,
								-hsX, size, -size,
								hsX, size, -size,
								0, vMax3, 1, vMax3, 1, vMin3, 0, vMin3,
								0, 0, -1,
								packedLight);

						drawCubeFace(vertexConsumer, matrix,
								-hsX, -size, -size,
								-hsX, -size, size,
								-hsX, size, size,
								-hsX, size, -size,
								0, vMax4, 1, vMax4, 1, vMin4, 0, vMin4,
								-1, 0, 0,
								packedLight);

						drawCubeFace(vertexConsumer, matrix,
								hsX, -size, size,
								hsX, -size, -size,
								hsX, size, -size,
								hsX, size, size,
								0, vMax2, 1, vMax2, 1, vMin2, 0, vMin2,
								1, 0, 0,
								packedLight);
					} else {
						vertexConsumer = bufferSource.getBuffer(RenderType.lightning());
						int alpha = 150;
						int red = (color >> 16) & 0xFF;
						int green = (color >> 8) & 0xFF;
						int blue = color & 0xFF;


						drawColoredCubeFace(vertexConsumer, matrix,
								-size, -size, size,
								size, -size, size,
								size, size, size,
								-size, size, size,
								red, green, blue, alpha,
								0, 0, 1,
								packedLight);

						drawColoredCubeFace(vertexConsumer, matrix,
								size, -size, -size,
								-size, -size, -size,
								-size, size, -size,
								size, size, -size,
								red, green, blue, alpha,
								0, 0, -1,
								packedLight);

						drawColoredCubeFace(vertexConsumer, matrix,
								-size, -size, -size,
								-size, -size, size,
								-size, size, size,
								-size, size, -size,
								red, green, blue, alpha,
								-1, 0, 0,
								packedLight);

						drawColoredCubeFace(vertexConsumer, matrix,
								size, -size, size,
								size, -size, -size,
								size, size, -size,
								size, size, size,
								red, green, blue, alpha,
								1, 0, 0,
								packedLight);
					}
				} else {
					if (textureString != null && !textureString.isEmpty()) {
						int animationSpeed = orbitingItem.getAnimationSpeed(itemStack, entity);
						int animationLength = orbitingItem.getAnimationLength(itemStack, entity);

						int currentFrame = (entity.tickCount / animationSpeed) % animationLength;
						float frameHeight = 1.0F / animationLength;
						float vMin = currentFrame * frameHeight;
						float vMax = vMin + frameHeight;

						vertexConsumer = bufferSource.getBuffer(ModRenderTypes.SPELL_EFFECT.apply(new ResourceLocation(RPGworldMod.MOD_ID, textureString + ".png")));

						vertexConsumer.vertex(matrix, -size, -size, 0.0F)
								.color(1.0F, 1.0F, 1.0F, 1F)
								.uv(0.0F, vMax)
								.overlayCoords(OverlayTexture.NO_OVERLAY)
								.uv2(packedLight)
								.normal(0.0F, 0.0F, 1.0F)
								.endVertex();

						vertexConsumer.vertex(matrix, size, -size, 0.0F)
								.color(1.0F, 1.0F, 1.0F, 1F)
								.uv(1.0F, vMax)
								.overlayCoords(OverlayTexture.NO_OVERLAY)
								.uv2(packedLight)
								.normal(0.0F, 0.0F, 1.0F)
								.endVertex();

						vertexConsumer.vertex(matrix, size, size, 0.0F)
								.color(1.0F, 1.0F, 1.0F, 1F)
								.uv(1.0F, vMin)
								.overlayCoords(OverlayTexture.NO_OVERLAY)
								.uv2(packedLight)
								.normal(0.0F, 0.0F, 1.0F)
								.endVertex();

						vertexConsumer.vertex(matrix, -size, size, 0.0F)
								.color(1.0F, 1.0F, 1.0F, 1F)
								.uv(0.0F, vMin)
								.overlayCoords(OverlayTexture.NO_OVERLAY)
								.uv2(packedLight)
								.normal(0.0F, 0.0F, 1.0F)
								.endVertex();
					} else {
						vertexConsumer = bufferSource.getBuffer(RenderType.lightning());
						int alpha = 150;
						int red = (color >> 16) & 0xFF;
						int green = (color >> 8) & 0xFF;
						int blue = color & 0xFF;

						vertexConsumer.vertex(matrix, size, size, 0.0F)
								.color(red, green, blue, alpha)
								.overlayCoords(OverlayTexture.NO_OVERLAY)
								.uv2(packedLight)
								.normal(0.0F, 0.0F, 1.0F)
								.endVertex();
						vertexConsumer.vertex(matrix, -size, size, 0.0F)
								.color(red, green, blue, alpha)
								.overlayCoords(OverlayTexture.NO_OVERLAY)
								.uv2(packedLight)
								.normal(0.0F, 0.0F, 1.0F)
								.endVertex();
						vertexConsumer.vertex(matrix, -size, -size, 0.0F)
								.color(red, green, blue, alpha)
								.overlayCoords(OverlayTexture.NO_OVERLAY)
								.uv2(packedLight)
								.normal(0.0F, 0.0F, 1.0F)
								.endVertex();
						vertexConsumer.vertex(matrix, size, -size, 0.0F)
								.color(red, green, blue, alpha)
								.overlayCoords(OverlayTexture.NO_OVERLAY)
								.uv2(packedLight)
								.normal(0.0F, 0.0F, 1.0F)
								.endVertex();
					}
				}
			}
			poseStack.popPose();
			ci.cancel();
		}


		if (itemStack.getItem() instanceof StaffItem staffItem && entity instanceof Player player) {
			if (itemStack.hasTag() && itemStack.getTag().contains("onCooldown")) return;
			poseStack.pushPose();

			((ArmedModel) iihl.getParentModel()).translateToHand(arm, poseStack);
			boolean flag = arm == HumanoidArm.LEFT;
			poseStack.translate((float) (flag ? staffItem.getX(itemStack, entity, false) * -1 : staffItem.getX(itemStack, entity, true)), staffItem.getY(itemStack, entity, !flag), staffItem.getZ(itemStack, entity, !flag));

			Matrix4f originalMatrix = poseStack.last().pose();
			Vector3f handPosition = originalMatrix.getTranslation(new Vector3f());

			poseStack.setIdentity();
			poseStack.translate(handPosition.x(), handPosition.y(), handPosition.z() + staffItem.getZOffset(itemStack, entity));

			String textureString = staffItem.getTexture(itemStack, entity);
			int color = staffItem.getColor(itemStack, entity);
			float size = 0.25F;

			VertexConsumer vertexConsumer;
			Matrix4f matrix = poseStack.last().pose();
			boolean rightHand = entity.getUsedItemHand() == (arm == HumanoidArm.RIGHT ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND);
			if (Minecraft.getInstance().options.mainHand().get() == HumanoidArm.LEFT) rightHand = !rightHand;
			if (entity.isUsingItem() && entity.getUseItemRemainingTicks() > 0 && rightHand) {
				if (textureString != null && !textureString.isEmpty()) {
					int animationSpeed = staffItem.getAnimationSpeed(itemStack, entity);
					int animationLength = staffItem.getAnimationLength(itemStack, entity);

					int currentFrame = (entity.tickCount / animationSpeed) % animationLength;
					float frameHeight = 1.0F / animationLength;
					float vMin = currentFrame * frameHeight;
					float vMax = vMin + frameHeight;

					vertexConsumer = bufferSource.getBuffer(ModRenderTypes.SPELL_EFFECT.apply(new ResourceLocation(RPGworldMod.MOD_ID, textureString + ".png")));

					vertexConsumer.vertex(matrix, -size, -size, 0.0F)
							.color(1.0F, 1.0F, 1.0F, 1F)
							.uv(0.0F, vMax)
							.overlayCoords(OverlayTexture.NO_OVERLAY)
							.uv2(packedLight)
							.normal(0.0F, 0.0F, 1.0F)
							.endVertex();

					vertexConsumer.vertex(matrix, size, -size, 0.0F)
							.color(1.0F, 1.0F, 1.0F, 1F)
							.uv(1.0F, vMax)
							.overlayCoords(OverlayTexture.NO_OVERLAY)
							.uv2(packedLight)
							.normal(0.0F, 0.0F, 1.0F)
							.endVertex();

					vertexConsumer.vertex(matrix, size, size, 0.0F)
							.color(1.0F, 1.0F, 1.0F, 1F)
							.uv(1.0F, vMin)
							.overlayCoords(OverlayTexture.NO_OVERLAY)
							.uv2(packedLight)
							.normal(0.0F, 0.0F, 1.0F)
							.endVertex();

					vertexConsumer.vertex(matrix, -size, size, 0.0F)
							.color(1.0F, 1.0F, 1.0F, 1F)
							.uv(0.0F, vMin)
							.overlayCoords(OverlayTexture.NO_OVERLAY)
							.uv2(packedLight)
							.normal(0.0F, 0.0F, 1.0F)
							.endVertex();
				} else {
					vertexConsumer = bufferSource.getBuffer(RenderType.lightning());
					int alpha = 150;
					int red = (color >> 16) & 0xFF;
					int green = (color >> 8) & 0xFF;
					int blue = color & 0xFF;

					vertexConsumer.vertex(matrix, size, size, 0.0F)
							.color(red, green, blue, alpha)
							.overlayCoords(OverlayTexture.NO_OVERLAY)
							.uv2(packedLight)
							.normal(0.0F, 0.0F, 1.0F)
							.endVertex();
					vertexConsumer.vertex(matrix, -size, size, 0.0F)
							.color(red, green, blue, alpha)
							.overlayCoords(OverlayTexture.NO_OVERLAY)
							.uv2(packedLight)
							.normal(0.0F, 0.0F, 1.0F)
							.endVertex();
					vertexConsumer.vertex(matrix, -size, -size, 0.0F)
							.color(red, green, blue, alpha)
							.overlayCoords(OverlayTexture.NO_OVERLAY)
							.uv2(packedLight)
							.normal(0.0F, 0.0F, 1.0F)
							.endVertex();
					vertexConsumer.vertex(matrix, size, -size, 0.0F)
							.color(red, green, blue, alpha)
							.overlayCoords(OverlayTexture.NO_OVERLAY)
							.uv2(packedLight)
							.normal(0.0F, 0.0F, 1.0F)
							.endVertex();
				}
			}

			poseStack.popPose();
		}
	}


	private static void drawCubeFace(VertexConsumer consumer, Matrix4f matrix,
									 float x1, float y1, float z1,
									 float x2, float y2, float z2,
									 float x3, float y3, float z3,
									 float x4, float y4, float z4,
									 float u1, float v1, float u2, float v2, float u3, float v3, float u4, float v4,
									 float nx, float ny, float nz,
									 int light) {
		consumer.vertex(matrix, x1, y1, z1).color(1.0F, 1.0F, 1.0F, 1.0F).uv(u1, v1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(nx, ny, nz).endVertex();
		consumer.vertex(matrix, x2, y2, z2).color(1.0F, 1.0F, 1.0F, 1.0F).uv(u2, v2).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(nx, ny, nz).endVertex();
		consumer.vertex(matrix, x3, y3, z3).color(1.0F, 1.0F, 1.0F, 1.0F).uv(u3, v3).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(nx, ny, nz).endVertex();
		consumer.vertex(matrix, x4, y4, z4).color(1.0F, 1.0F, 1.0F, 1.0F).uv(u4, v4).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(nx, ny, nz).endVertex();
	}

	private static void drawColoredCubeFace(VertexConsumer consumer, Matrix4f matrix,
											float x1, float y1, float z1,
											float x2, float y2, float z2,
											float x3, float y3, float z3,
											float x4, float y4, float z4,
											int r, int g, int b, int a,
											float nx, float ny, float nz,
											int light) {
		consumer.vertex(matrix, x1, y1, z1).color(r, g, b, a).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(nx, ny, nz).endVertex();
		consumer.vertex(matrix, x2, y2, z2).color(r, g, b, a).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(nx, ny, nz).endVertex();
		consumer.vertex(matrix, x3, y3, z3).color(r, g, b, a).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(nx, ny, nz).endVertex();
		consumer.vertex(matrix, x4, y4, z4).color(r, g, b, a).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(nx, ny, nz).endVertex();
	}


	private void renderSummonBeams(PoseStack poseStack, MultiBufferSource buffer, int packedLight, float progress, float ageTicks) {
		VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.lightning());
		poseStack.pushPose();


		net.minecraft.client.Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
		float yaw = camera.getYRot();
		float pitch = camera.getXRot();
		poseStack.mulPose(Axis.YP.rotationDegrees(-yaw));
		poseStack.mulPose(Axis.XP.rotationDegrees(-pitch));

		float rotationAngle = ageTicks * 5.0f;
		int beamCount = 12;
		float maxLength = 0.8f;
		float maxWidth = 0.3f;
		float beamLength = maxLength * progress;
		float beamWidth = maxWidth * progress;
		int alpha = (int) (200 * progress);

		for (int i = 0; i < beamCount; i++) {
			poseStack.pushPose();
			float angle = rotationAngle + (360f / beamCount) * i;
			poseStack.mulPose(Axis.ZP.rotationDegrees(angle));
			Matrix4f matrix = poseStack.last().pose();


			vertex01(vertexConsumer, matrix, alpha, 0, 0, 255);
			vertex3(vertexConsumer, matrix, beamLength, beamWidth, 0, 0, 255);
			vertex4(vertexConsumer, matrix, beamLength, beamWidth, 0, 0, 255);
			vertex01(vertexConsumer, matrix, alpha, 0, 0, 255);
			vertex4(vertexConsumer, matrix, beamLength, beamWidth, 0, 0, 255);
			vertex2(vertexConsumer, matrix, beamLength, beamWidth, 0, 0, 255);

			poseStack.popPose();
		}
		poseStack.popPose();
	}


	private static void vertex01(VertexConsumer consumer, Matrix4f matrix, int alpha, int r, int g, int b) {
		consumer.vertex(matrix, 0.0F, 0.0F, 0.0F)
				.color(r, g, b, alpha)
				.endVertex();
	}

	private static void vertex2(VertexConsumer consumer, Matrix4f matrix, float length, float width, int r, int g, int b) {
		consumer.vertex(matrix, -HALF_SQRT_3 * width, length, -0.5F * width)
				.color(r, g, b, 0)
				.endVertex();
	}

	private static void vertex3(VertexConsumer consumer, Matrix4f matrix, float length, float width, int r, int g, int b) {
		consumer.vertex(matrix, HALF_SQRT_3 * width, length, -0.5F * width)
				.color(r, g, b, 0)
				.endVertex();
	}

	private static void vertex4(VertexConsumer consumer, Matrix4f matrix, float length, float width, int r, int g, int b) {
		consumer.vertex(matrix, 0.0F, length, 1.0F * width)
				.color(r, g, b, 0)
				.endVertex();
	}

}