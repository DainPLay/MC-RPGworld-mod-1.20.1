package net.dainplay.rpgworldmod.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.dainplay.rpgworldmod.RPGworldMod;
import net.dainplay.rpgworldmod.enchantment.ModEnchantments;
import net.dainplay.rpgworldmod.entity.custom.EnderEyeViewEntity;
import net.dainplay.rpgworldmod.item.custom.ManaCostItem;
import net.dainplay.rpgworldmod.item.custom.NetherStarScrollItem;
import net.dainplay.rpgworldmod.item.custom.OrbitingItem;
import net.dainplay.rpgworldmod.item.custom.StaffItem;
import net.dainplay.rpgworldmod.network.ClientManaData;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class OrbitingItemRenderer {

	@SubscribeEvent
	public static void onRenderLiving(RenderLivingEvent.Post<LivingEntity, ?> event) {
		if (event.getEntity() == Minecraft.getInstance().player &&
				Minecraft.getInstance().options.getCameraType().isFirstPerson()) {
			return;
		}

		LivingEntity entity = event.getEntity();
		List<ItemStack> orbitingItems = getOrbitingItems(entity);
		if (!orbitingItems.isEmpty()) {
			renderRotatingItems(event.getPoseStack(), entity, event.getPartialTick(),
					event.getMultiBufferSource(), orbitingItems);
		}
	}

	@SubscribeEvent
	public static void onRenderLevelStage(RenderLevelStageEvent event) {
		if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_ENTITIES) {
			return;
		}

		Minecraft minecraft = Minecraft.getInstance();
		Player player = minecraft.player;

		if (player == null || !minecraft.options.getCameraType().isFirstPerson()) {
			return;
		}

		List<ItemStack> orbitingItems = getOrbitingItems(player);
		if (orbitingItems.isEmpty()) {
			return;
		}

		renderRotatingItemsFirstPerson(event.getPoseStack(), player,
				event.getPartialTick(), Minecraft.getInstance().renderBuffers().bufferSource(), orbitingItems);
	}

	@SubscribeEvent
	public static void onRenderPlayerHand(RenderHandEvent event) {
		if (Minecraft.getInstance().getCameraEntity() instanceof EnderEyeViewEntity) {
			event.setCanceled(true);
			return;
		}
		if (event.getItemStack().getItem() instanceof OrbitingItem item1 && item1.shouldOrbit(event.getItemStack(), Minecraft.getInstance().player)) {
			Minecraft mc = Minecraft.getInstance();
			AbstractClientPlayer player = mc.player;
			PlayerRenderer playerrenderer = (PlayerRenderer) mc.getEntityRenderDispatcher()
					.getRenderer(player);

			PoseStack ms = event.getPoseStack();
			MultiBufferSource buffer = event.getMultiBufferSource();
			int light = event.getPackedLight();

			boolean rightHand = event.getHand() == InteractionHand.MAIN_HAND ^ mc.player.getMainArm() == HumanoidArm.LEFT;
			float equipProgress = event.getEquipProgress();
			float swingProgress = event.getSwingProgress();

			float flip = rightHand ? 1.0F : -1.0F;
			float f1 = Mth.sqrt(swingProgress);
			float f2 = -0.3F * Mth.sin(f1 * (float) Math.PI);
			float f3 = 0.4F * Mth.sin(f1 * ((float) Math.PI * 2F));
			float f4 = -0.4F * Mth.sin(swingProgress * (float) Math.PI);

			ms.pushPose();
			ms.translate(
					flip * (f2 + 0.64000005F),
					f3 + -0.6F + equipProgress * -0.6F,
					f4 + -0.71999997F
			);
			ms.mulPose(Axis.YP.rotationDegrees(flip * 45.0F));

			float f5 = Mth.sin(swingProgress * swingProgress * (float) Math.PI);
			float f6 = Mth.sin(f1 * (float) Math.PI);
			ms.mulPose(Axis.YP.rotationDegrees(flip * f6 * 70.0F));
			ms.mulPose(Axis.ZP.rotationDegrees(flip * f5 * -20.0F));

			ms.translate(flip * -1.0F, 3.6F, 3.5F);
			ms.mulPose(Axis.ZP.rotationDegrees(flip * 120.0F));
			ms.mulPose(Axis.XP.rotationDegrees(200.0F));
			ms.mulPose(Axis.YP.rotationDegrees(flip * -135.0F));
			ms.translate(flip * 5.6F, 0.0F, 0.0F);

			if (player.isUsingItem() && player.getUseItemRemainingTicks() > 0 && player.getUsedItemHand() == event.getHand()) {
				ms = item1.getUsingPose(event.getItemStack(), player, ms, flip);
			}
			if (rightHand) {
				playerrenderer.renderRightHand(ms, buffer, light, player);
			} else {
				playerrenderer.renderLeftHand(ms, buffer, light, player);
			}
			ms.popPose();

			OrbitingItem item = (OrbitingItem) event.getItemStack().getItem();
			String textureString = item.getTexture(event.getItemStack(), player);
			int animationSpeed = item.getAnimationSpeed(event.getItemStack(), player);
			int animationLength = item.getAnimationLength(event.getItemStack(), player);
			int manacost = 0;
			boolean hasEnough = false;
			if (item instanceof ManaCostItem spell) {
				manacost = spell.getManaCost(event.getItemStack(), player);
				hasEnough = ClientManaData.get() >= manacost;
				if (spell.usesHealthInsteadOfMana(event.getItemStack()))
					hasEnough = (int) Math.ceil(player.getHealth()) >= manacost;
			}

			ms.pushPose();
			ms.translate(
					flip * (f2 * 2F + 0.64000005F),
					f3 + -0.6F + equipProgress * -0.6F,
					f4 + -0.71999997F
			);
			boolean useCube = item.useCubeEffect(event.getItemStack(), player);
			boolean isSlim = "slim".equals(player.getModelName());
			if (useCube) {
				ms.mulPose(Axis.YP.rotationDegrees(flip * 45.0F));
				ms.mulPose(Axis.YP.rotationDegrees(flip * f6 * 70.0F));
				ms.mulPose(Axis.ZP.rotationDegrees(flip * f5 * -20.0F));

				ms.mulPose(Axis.ZP.rotationDegrees(flip * 120.0F));
				ms.mulPose(Axis.XP.rotationDegrees(200.0F));
				ms.mulPose(Axis.YP.rotationDegrees(flip * -135.0F));

				ms.mulPose(Axis.ZP.rotationDegrees(flip * 5F));
				if (isSlim) ms.translate(flip * -0.075F,0.1F,0.5F);
				else ms.translate(flip * -0.1F,0.1F,0.5F);

				if (player.isUsingItem() && player.getUseItemRemainingTicks() > 0 && player.getUsedItemHand() == event.getHand()) {
					ms = item1.getUsingPose(event.getItemStack(), player, ms, flip);
				}
			}
			ms.translate(flip * item.get1XOffset(event.getItemStack(), player), item.get1YOffset(event.getItemStack(), player), item.get1ZOffset(event.getItemStack(), player));
			if (player.isUsingItem() && player.getUseItemRemainingTicks() > 0 && player.getUsedItemHand() == event.getHand()) {
				ms = item1.getEffectUsingPose(event.getItemStack(), player, ms, flip);
			}

			float size = useCube ? 0.15F : item.get1Size(event.getItemStack(), player);
			if (item instanceof NetherStarScrollItem
					&& EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.DESTRUCTION.get(), event.getItemStack()) > 0
					&& player.getTicksUsingItem() <= 40) {
				size += (0.6F-size)*player.getTicksUsingItem()/40;
			}
			VertexConsumer vertexconsumer;
			Matrix4f matrix4f = ms.last().pose();

			if ((hasEnough || player.getAbilities().instabuild)
					&& !(item instanceof NetherStarScrollItem
					&& EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.DESTRUCTION.get(), event.getItemStack()) > 0
					&& player.getTicksUsingItem() > 40)) {
				if (textureString != null && !textureString.isEmpty()) {
					int currentFrame = (player.tickCount / animationSpeed) % animationLength;
					float frameHeight = 1.0F / animationLength;
					float vMin1 = currentFrame * frameHeight;
					float vMax1 = vMin1 + frameHeight;
					float vMin2 = (currentFrame+8) % animationLength * frameHeight;
					float vMax2 = vMin2 + frameHeight;
					float vMin3 = (currentFrame+16) % animationLength * frameHeight;
					float vMax3 = vMin3 + frameHeight;
					float vMin4 = (currentFrame+24) % animationLength * frameHeight;
					float vMax4 = vMin4 + frameHeight;
					if (useCube) {
						vertexconsumer = buffer.getBuffer(ModRenderTypes.GLOW_SPELL_EFFECT.apply(new ResourceLocation(RPGworldMod.MOD_ID, textureString + ".png")));
					}
					else {
						vertexconsumer = buffer.getBuffer(ModRenderTypes.SPELL_EFFECT.apply(new ResourceLocation(RPGworldMod.MOD_ID, textureString + ".png")));
					}

					if (useCube) {

						float hsX = size;

						if (isSlim) {
							hsX = 0.1125F;
						}
						addQuad(vertexconsumer, matrix4f,
								-hsX, -size, size,
								hsX, -size, size,
								hsX, size, size,
								-hsX, size, size,
								0, vMax1, 1, vMax1, 1, vMin1, 0, vMin1, 0, 0, 1);
						addQuad(vertexconsumer, matrix4f,
								hsX, -size, -size,
								-hsX, -size, -size,
								-hsX, size, -size,
								hsX, size, -size,
								0, vMax3, 1, vMax3, 1, vMin3, 0, vMin3, 0, 0, -1);
						addQuad(vertexconsumer, matrix4f,
								-hsX, -size, -size,
								-hsX, -size, size,
								-hsX, size, size,
								-hsX, size, -size,
								0, vMax4, 1, vMax4, 1, vMin4, 0, vMin4, -1, 0, 0);
						addQuad(vertexconsumer, matrix4f,
								hsX, -size, size,
								hsX, -size, -size,
								hsX, size, -size,
								hsX, size, size,
								0, vMax2, 1, vMax2, 1, vMin2, 0, vMin2, 1, 0, 0);
					} else {
						vertexconsumer.vertex(matrix4f, -size, -size, 0.0F)
								.color(1.0F, 1.0F, 1.0F, 1.0F)
								.uv(0.0F, vMax1)
								.overlayCoords(OverlayTexture.NO_OVERLAY)
								.uv2(15728880)
								.normal(0.0F, 0.0F, 1.0F)
								.endVertex();

						vertexconsumer.vertex(matrix4f, size, -size, 0.0F)
								.color(1.0F, 1.0F, 1.0F, 1.0F)
								.uv(1.0F, vMax1)
								.overlayCoords(OverlayTexture.NO_OVERLAY)
								.uv2(15728880)
								.normal(0.0F, 0.0F, 1.0F)
								.endVertex();

						vertexconsumer.vertex(matrix4f, size, size, 0.0F)
								.color(1.0F, 1.0F, 1.0F, 1.0F)
								.uv(1.0F, vMin1)
								.overlayCoords(OverlayTexture.NO_OVERLAY)
								.uv2(15728880)
								.normal(0.0F, 0.0F, 1.0F)
								.endVertex();

						vertexconsumer.vertex(matrix4f, -size, size, 0.0F)
								.color(1.0F, 1.0F, 1.0F, 1.0F)
								.uv(0.0F, vMin1)
								.overlayCoords(OverlayTexture.NO_OVERLAY)
								.uv2(15728880)
								.normal(0.0F, 0.0F, 1.0F)
								.endVertex();
					}
				} else {
					int color = item.getColor(event.getItemStack(), player);
					int alpha = 150;
					int red = (color >> 16) & 0xFF;
					int green = (color >> 8) & 0xFF;
					int blue = color & 0xFF;

					vertexconsumer = buffer.getBuffer(RenderType.lightning());

					if (useCube) {
						addColoredQuad(vertexconsumer, matrix4f,
								-size, -size, size, size, -size, size, size, size, size, -size, size, size,
								red, green, blue, alpha, 0, 0, 1);
						addColoredQuad(vertexconsumer, matrix4f,
								size, -size, -size, -size, -size, -size, -size, size, -size, size, size, -size,
								red, green, blue, alpha, 0, 0, -1);
						addColoredQuad(vertexconsumer, matrix4f,
								-size, -size, -size, -size, -size, size, -size, size, size, -size, size, -size,
								red, green, blue, alpha, -1, 0, 0);
						addColoredQuad(vertexconsumer, matrix4f,
								size, -size, size, size, -size, -size, size, size, -size, size, size, size,
								red, green, blue, alpha, 1, 0, 0);
					} else {
						vertexconsumer.vertex(matrix4f, size, size, 0.0F)
								.color(red, green, blue, alpha)
								.endVertex();
						vertexconsumer.vertex(matrix4f, -size, size, 0.0F)
								.color(red, green, blue, alpha)
								.endVertex();
						vertexconsumer.vertex(matrix4f, -size, -size, 0.0F)
								.color(red, green, blue, alpha)
								.endVertex();
						vertexconsumer.vertex(matrix4f, size, -size, 0.0F)
								.color(red, green, blue, alpha)
								.endVertex();
					}
				}
			}
			ms.popPose();

			event.setCanceled(true);
		}
		if (event.getItemStack().getItem() instanceof StaffItem item1 && Minecraft.getInstance().player != null && item1.isOffCooldown(event.getItemStack(), Minecraft.getInstance().player)) {
			Minecraft mc = Minecraft.getInstance();
			AbstractClientPlayer player = mc.player;

			PoseStack ms = event.getPoseStack();
			MultiBufferSource buffer = event.getMultiBufferSource();

			boolean rightHand = event.getHand() == InteractionHand.MAIN_HAND ^ mc.player.getMainArm() == HumanoidArm.LEFT;
			float equipProgress = event.getEquipProgress();
			float swingProgress = event.getSwingProgress();

			float flip = rightHand ? 1.0F : -1.0F;
			float f1 = Mth.sqrt(swingProgress);
			float f2 = -0.3F * Mth.sin(f1 * (float) Math.PI);
			float f3 = 0.4F * Mth.sin(f1 * ((float) Math.PI * 2F));
			float f4 = -0.4F * Mth.sin(swingProgress * (float) Math.PI);

			String textureString = item1.getTexture(event.getItemStack(), player);
			int animationSpeed = item1.getAnimationSpeed(event.getItemStack(), player);
			int animationLength = item1.getAnimationLength(event.getItemStack(), player);

			ms.pushPose();
			float swingFactor = 0F;
			ms.translate(
					flip * (0.64000005F),
					-0.6F + equipProgress * -0.6F,
					-0.71999997F
			);
			ms.translate(flip * item1.get1XOffset(event.getItemStack(), player, rightHand), item1.get1YOffset(event.getItemStack(), player, rightHand), item1.get1ZOffset(event.getItemStack(), player, rightHand));
			if (player.isUsingItem() && player.getUseItemRemainingTicks() > 0 && player.getUsedItemHand() == event.getHand()) {
				ms = item1.getEffectUsingPose(event.getItemStack(), player, ms, flip);
			}
			VertexConsumer vertexconsumer;
			float size = 0.35F;
			Matrix4f matrix4f = ms.last().pose();
			if (player.isUsingItem() && player.getUseItemRemainingTicks() > 0 && player.getUsedItemHand() == event.getHand()) {
				if (textureString != null && !textureString.isEmpty()) {
					int currentFrame = (player.tickCount / animationSpeed) % animationLength;
					float frameHeight = 1.0F / animationLength;
					float vMin = currentFrame * frameHeight;
					float vMax = vMin + frameHeight;

					vertexconsumer = buffer.getBuffer(ModRenderTypes.SPELL_EFFECT.apply(new ResourceLocation(RPGworldMod.MOD_ID, textureString + ".png")));

					vertexconsumer.vertex(matrix4f, -size, -size, 0.0F)
							.color(1.0F, 1.0F, 1.0F, 1.0F)
							.uv(0.0F, vMax)
							.overlayCoords(OverlayTexture.NO_OVERLAY)
							.uv2(15728880)
							.normal(0.0F, 0.0F, 1.0F)
							.endVertex();

					vertexconsumer.vertex(matrix4f, size, -size, 0.0F)
							.color(1.0F, 1.0F, 1.0F, 1.0F)
							.uv(1.0F, vMax)
							.overlayCoords(OverlayTexture.NO_OVERLAY)
							.uv2(15728880)
							.normal(0.0F, 0.0F, 1.0F)
							.endVertex();

					vertexconsumer.vertex(matrix4f, size, size, 0.0F)
							.color(1.0F, 1.0F, 1.0F, 1.0F)
							.uv(1.0F, vMin)
							.overlayCoords(OverlayTexture.NO_OVERLAY)
							.uv2(15728880)
							.normal(0.0F, 0.0F, 1.0F)
							.endVertex();

					vertexconsumer.vertex(matrix4f, -size, size, 0.0F)
							.color(1.0F, 1.0F, 1.0F, 1.0F)
							.uv(0.0F, vMin)
							.overlayCoords(OverlayTexture.NO_OVERLAY)
							.uv2(15728880)
							.normal(0.0F, 0.0F, 1.0F)
							.endVertex();
				} else {
					int color = item1.getColor(event.getItemStack(), player);
					int alpha = 150;
					int red = (color >> 16) & 0xFF;
					int green = (color >> 8) & 0xFF;
					int blue = color & 0xFF;

					vertexconsumer = buffer.getBuffer(RenderType.lightning());

					vertexconsumer.vertex(matrix4f, size, size, 0.0F)
							.color(red, green, blue, alpha)
							.endVertex();
					vertexconsumer.vertex(matrix4f, -size, size, 0.0F)
							.color(red, green, blue, alpha)
							.endVertex();
					vertexconsumer.vertex(matrix4f, -size, -size, 0.0F)
							.color(red, green, blue, alpha)
							.endVertex();
					vertexconsumer.vertex(matrix4f, size, -size, 0.0F)
							.color(red, green, blue, alpha)
							.endVertex();
				}
			}

			ms.popPose();
		}
	}

	private static List<ItemStack> getOrbitingItems(LivingEntity entity) {
		List<ItemStack> items = new ArrayList<>();

		ItemStack mainHand = entity.getMainHandItem();
		if (mainHand.getItem() instanceof OrbitingItem item && item.shouldOrbit(mainHand, entity)) {
			items.add(mainHand);
		}

		ItemStack offHand = entity.getOffhandItem();
		if (offHand.getItem() instanceof OrbitingItem item && item.shouldOrbit(offHand, entity)) {
			items.add(offHand);
		}

		return items;
	}

	private static void renderRotatingItemsFirstPerson(PoseStack poseStack, Player player,
													   float partialTick,
													   MultiBufferSource buffer, List<ItemStack> orbitingItems) {
		if (orbitingItems.isEmpty()) return;
		if (player.isSpectator()) return;

		Minecraft minecraft = Minecraft.getInstance();
		ItemRenderer itemRenderer = minecraft.getItemRenderer();

		Camera camera = minecraft.gameRenderer.getMainCamera();
		Vec3 cameraPos = camera.getPosition();

		double playerX = player.xOld + (player.getX() - player.xOld) * partialTick;
		double playerY = player.yOld + (player.getY() - player.yOld) * partialTick;
		double playerZ = player.zOld + (player.getZ() - player.zOld) * partialTick;

		float radius = 0.75F;
		float rotationSpeed = 0.05F;
		float baseAngle = (player.tickCount + partialTick) * rotationSpeed;

		for (int i = 0; i < orbitingItems.size(); i++) {
			ItemStack itemToRender = orbitingItems.get(i);

			float angleOffset = (float) (Math.PI * 2 * i / orbitingItems.size());
			float angle = baseAngle + angleOffset;

			float orbitX = (float) (playerX + radius * Math.cos(angle));
			float orbitY = (float) (playerY + player.getBbHeight() * 0.5f);
			float orbitZ = (float) (playerZ + radius * Math.sin(angle));

			poseStack.pushPose();

			poseStack.translate(orbitX - cameraPos.x,
					orbitY - cameraPos.y,
					orbitZ - cameraPos.z);

			poseStack.mulPose(Axis.YP.rotationDegrees(angle * 50));
			poseStack.scale(0.5F, 0.5F, 0.5F);

			itemRenderer.renderStatic(
					itemToRender,
					ItemDisplayContext.FIXED,
					15728880,
					OverlayTexture.NO_OVERLAY,
					poseStack,
					buffer,
					minecraft.level,
					0
			);

			poseStack.popPose();
		}
	}

	private static void renderRotatingItems(PoseStack poseStack, LivingEntity entity, float partialTick,
											MultiBufferSource buffer, List<ItemStack> orbitingItems) {
		if (orbitingItems.isEmpty()) return;
		if (entity instanceof Player player && player.isSpectator()) return;

		Minecraft minecraft = Minecraft.getInstance();
		ItemRenderer itemRenderer = minecraft.getItemRenderer();

		double x = entity.xOld + (entity.getX() - entity.xOld) * partialTick;
		double y = entity.yOld + (entity.getY() - entity.yOld) * partialTick;
		double z = entity.zOld + (entity.getZ() - entity.zOld) * partialTick;

		float radius = 0.75F;
		float rotationSpeed = 0.05F;
		float baseAngle = (entity.tickCount + partialTick) * rotationSpeed;

		for (int i = 0; i < orbitingItems.size(); i++) {
			ItemStack itemToRender = orbitingItems.get(i);

			float angleOffset = (float) (Math.PI * 2 * i / orbitingItems.size());
			float angle = baseAngle + angleOffset;

			float orbitX = (float) (x + radius * Math.cos(angle));
			float orbitY = (float) y;
			float orbitZ = (float) (z + radius * Math.sin(angle));

			poseStack.pushPose();
			poseStack.translate(orbitX - x, orbitY - y + entity.getBbHeight() / 2, orbitZ - z);

			poseStack.mulPose(Axis.YP.rotationDegrees(angle * 50));

			poseStack.scale(0.5F, 0.5F, 0.5F);

			itemRenderer.renderStatic(
					itemToRender,
					ItemDisplayContext.FIXED,
					15728880,
					OverlayTexture.NO_OVERLAY,
					poseStack,
					buffer,
					minecraft.level,
					0
			);

			poseStack.popPose();
		}
	}

	private static void addQuad(VertexConsumer consumer, Matrix4f matrix,
								float x1, float y1, float z1,
								float x2, float y2, float z2,
								float x3, float y3, float z3,
								float x4, float y4, float z4,
								float u1, float v1,
								float u2, float v2,
								float u3, float v3,
								float u4, float v4,
								float nx, float ny, float nz) {
		consumer.vertex(matrix, x1, y1, z1).color(1, 1, 1, 1).uv(u1, v1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(15728880).normal(nx, ny, nz).endVertex();
		consumer.vertex(matrix, x2, y2, z2).color(1, 1, 1, 1).uv(u2, v2).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(15728880).normal(nx, ny, nz).endVertex();
		consumer.vertex(matrix, x3, y3, z3).color(1, 1, 1, 1).uv(u3, v3).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(15728880).normal(nx, ny, nz).endVertex();
		consumer.vertex(matrix, x4, y4, z4).color(1, 1, 1, 1).uv(u4, v4).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(15728880).normal(nx, ny, nz).endVertex();
	}

	private static void addColoredQuad(VertexConsumer consumer, Matrix4f matrix,
									   float x1, float y1, float z1,
									   float x2, float y2, float z2,
									   float x3, float y3, float z3,
									   float x4, float y4, float z4,
									   int r, int g, int b, int a,
									   float nx, float ny, float nz) {
		consumer.vertex(matrix, x1, y1, z1).color(r, g, b, a).endVertex();
		consumer.vertex(matrix, x2, y2, z2).color(r, g, b, a).endVertex();
		consumer.vertex(matrix, x3, y3, z3).color(r, g, b, a).endVertex();
		consumer.vertex(matrix, x4, y4, z4).color(r, g, b, a).endVertex();
	}
}