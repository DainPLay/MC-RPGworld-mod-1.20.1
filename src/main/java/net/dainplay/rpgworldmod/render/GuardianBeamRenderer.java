package net.dainplay.rpgworldmod.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.dainplay.rpgworldmod.enchantment.ModEnchantments;
import net.dainplay.rpgworldmod.item.ModItems;
import net.dainplay.rpgworldmod.item.custom.EnderEyeScrollItem;
import net.dainplay.rpgworldmod.network.ClientGuardianAttackData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EndCrystalRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

import java.util.List;

@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class GuardianBeamRenderer {
	private static final ResourceLocation BEAM_LOCATION = new ResourceLocation("textures/entity/guardian_beam.png");
	private static final RenderType BEAM_RENDER_TYPE = RenderType.entityCutoutNoCull(BEAM_LOCATION);
	private static final ResourceLocation CRYSTAL_BEAM_LOCATION = new ResourceLocation("textures/entity/end_crystal/end_crystal_beam.png");
	private static final RenderType CRYSTAL_BEAM_RENDER_TYPE = RenderType.entityTranslucent(CRYSTAL_BEAM_LOCATION);

	@SubscribeEvent
	public static void onRenderLevelStage(RenderLevelStageEvent event) {
		if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_ENTITIES) return;

		Minecraft mc = Minecraft.getInstance();
		if (mc.level == null || mc.player == null) return;

		PoseStack poseStack = event.getPoseStack();
		MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();
		float partialTick = event.getPartialTick();
		Vec3 cameraPos = mc.gameRenderer.getMainCamera().getPosition();

		// Рендер существующих лучей (например, от атаки)
		for (var entry : ClientGuardianAttackData.getAll().entrySet()) {
			ClientGuardianAttackData.AttackData data = entry.getValue();
			if (data.target == null || !data.target.isAlive() || data.attacker == null || !data.attacker.isAlive())
				continue;
			renderBeam(poseStack, bufferSource, partialTick, cameraPos, data.attacker, data.target, data.attackTime);
		}

		// Рендер лучей от игроков, использующих свиток с чарами Restoration
		for (Player player : mc.level.players()) {
			if (player.isUsingItem()) {
				ItemStack usingItem = player.getUseItem();
				if (usingItem.getItem() instanceof EnderEyeScrollItem &&
						EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.RESTORATION.get(), usingItem) > 0) {

					double searchRadius = 32.0D;
					AABB aabb = player.getBoundingBox().inflate(searchRadius);
					List<EndCrystal> crystals = mc.level.getEntitiesOfClass(EndCrystal.class, aabb);
					EndCrystal nearest = null;
					double nearestDistSq = Double.MAX_VALUE;
					for (EndCrystal crystal : crystals) {
						double distSq = crystal.distanceToSqr(player);
						if (distSq < nearestDistSq) {
							nearestDistSq = distSq;
							nearest = crystal;
						}
					}

					if (nearest != null) {
						Vec3 playerPos = player.getPosition(partialTick).add(0, player.getBbHeight() * 0.5, 0);
						int packedLight = mc.getEntityRenderDispatcher().getPackedLightCoords(nearest, partialTick);
						renderCrystalBeamToPlayer(poseStack, bufferSource, partialTick, cameraPos,
								playerPos, nearest, packedLight, player.tickCount);
					}
				}
			}
		}
	}

	/**
	 * Вычисляет мировые координаты точки, из которой должен исходить луч (дуло оружия/рука).
	 * Учитывает режим камеры (первое/третье лицо), сторону руки и анимацию атаки.
	 */
	private static Vec3 getMuzzlePosition(Player player, float partialTick, boolean firstPerson) {
		Minecraft mc = Minecraft.getInstance();
		HumanoidArm mainArm = player.getMainArm();
		boolean leftHand = mainArm == HumanoidArm.LEFT;
		int side = leftHand ? -1 : 1;

		if (firstPerson) {
			Vec3 cameraPos = mc.gameRenderer.getMainCamera().getPosition();
			double scale = 1000.0 / mc.getEntityRenderDispatcher().options.fov().get().intValue();
			Vec3 nearPoint = mc.getEntityRenderDispatcher().camera.getNearPlane().getPointOnPlane(side * 0.35F, -0.25F);
			float attackAnim = player.getAttackAnim(partialTick);
			float f1 = Mth.sin(Mth.sqrt(attackAnim) * (float) Math.PI);
			nearPoint = nearPoint.scale(scale);
			nearPoint = nearPoint.yRot(f1 * 0.5F);
			nearPoint = nearPoint.xRot(-f1 * 0.7F);
			return cameraPos.add(nearPoint);
		} else {
			Vec3 playerPos = player.getPosition(partialTick);
			float yBodyRot = Mth.lerp(partialTick, player.yBodyRotO, player.yBodyRot);
			double armOffsetX = player.getBbWidth() * -0.5 * side;
			double armOffsetY = player.getBbHeight() * 0.8;
			Vec3 offset = new Vec3(armOffsetX, armOffsetY, 0).yRot((float) Math.toRadians(-yBodyRot));
			Vec3 viewVec = player.getViewVector(partialTick).normalize().scale(1.5);
			return playerPos.add(offset).add(viewVec);
		}
	}

	private static void renderBeam(PoseStack poseStack, MultiBufferSource bufferSource, float partialTick, Vec3 cameraPos,
								   Player attacker, LivingEntity target, int attackTime) {
		float attackProgress = (attackTime + partialTick) / 80.0F;
		if (!attacker.isUsingItem()
				|| attacker.getUseItem().getItem() != ModItems.HEART_OF_THE_SEA_SCROLL.get()
				|| attacker.getUseItem().getEnchantmentLevel(ModEnchantments.DESTRUCTION.get()) < 1) return;

		if (attacker.distanceTo(target) > 64.0F) {
			return;
		}
		boolean firstPerson = false;
		if (attacker == Minecraft.getInstance().player)
			firstPerson = Minecraft.getInstance().options.getCameraType().isFirstPerson();
		Vec3 start = getMuzzlePosition(attacker, partialTick, firstPerson);
		Vec3 end = getPosition(target, target.getBbHeight() * 0.5D, partialTick);
		Vec3 direction = end.subtract(start);
		float length = (float) (direction.length() + 1.0F);
		direction = direction.normalize();

		float yaw = (float) Math.atan2(direction.z, direction.x);
		float pitch = (float) Math.acos(direction.y);

		poseStack.pushPose();
		poseStack.translate(start.x - cameraPos.x, start.y - cameraPos.y, start.z - cameraPos.z);
		poseStack.mulPose(Axis.YP.rotationDegrees(((float) Math.PI / 2 - yaw) * (180F / (float) Math.PI)));
		poseStack.mulPose(Axis.XP.rotationDegrees(pitch * (180F / (float) Math.PI)));

		float f1 = attackTime + partialTick;
		float f2 = f1 * 0.05F * -1.5F;
		float f3 = attackProgress * attackProgress;

		int red = 64 + (int) (f3 * 191.0F);
		int green = 32 + (int) (f3 * 191.0F);
		int blue = 128 - (int) (f3 * 64.0F);

		float f11 = Mth.cos(f2 + 2.3561945F) * 0.282F;
		float f12 = Mth.sin(f2 + 2.3561945F) * 0.282F;
		float f13 = Mth.cos(f2 + ((float) Math.PI / 4F)) * 0.282F;
		float f14 = Mth.sin(f2 + ((float) Math.PI / 4F)) * 0.282F;
		float f15 = Mth.cos(f2 + 3.926991F) * 0.282F;
		float f16 = Mth.sin(f2 + 3.926991F) * 0.282F;
		float f17 = Mth.cos(f2 + 5.4977875F) * 0.282F;
		float f18 = Mth.sin(f2 + 5.4977875F) * 0.282F;
		float f19 = Mth.cos(f2 + (float) Math.PI) * 0.2F;
		float f20 = Mth.sin(f2 + (float) Math.PI) * 0.2F;
		float f21 = Mth.cos(f2 + 0.0F) * 0.2F;
		float f22 = Mth.sin(f2 + 0.0F) * 0.2F;
		float f23 = Mth.cos(f2 + ((float) Math.PI / 2F)) * 0.2F;
		float f24 = Mth.sin(f2 + ((float) Math.PI / 2F)) * 0.2F;
		float f25 = Mth.cos(f2 + ((float) Math.PI * 1.5F)) * 0.2F;
		float f26 = Mth.sin(f2 + ((float) Math.PI * 1.5F)) * 0.2F;

		float f29 = -1.0F + (f1 * 0.5F % 1.0F);
		float f30 = length * 2.5F + f29;

		VertexConsumer vertexconsumer = bufferSource.getBuffer(BEAM_RENDER_TYPE);
		Matrix4f matrix4f = poseStack.last().pose();
		Matrix3f matrix3f = poseStack.last().normal();

		vertex(vertexconsumer, matrix4f, matrix3f, f19, length, f20, red, green, blue, 0.4999F, f30);
		vertex(vertexconsumer, matrix4f, matrix3f, f19, 0.0F, f20, red, green, blue, 0.4999F, f29);
		vertex(vertexconsumer, matrix4f, matrix3f, f21, 0.0F, f22, red, green, blue, 0.0F, f29);
		vertex(vertexconsumer, matrix4f, matrix3f, f21, length, f22, red, green, blue, 0.0F, f30);
		vertex(vertexconsumer, matrix4f, matrix3f, f23, length, f24, red, green, blue, 0.4999F, f30);
		vertex(vertexconsumer, matrix4f, matrix3f, f23, 0.0F, f24, red, green, blue, 0.4999F, f29);
		vertex(vertexconsumer, matrix4f, matrix3f, f25, 0.0F, f26, red, green, blue, 0.0F, f29);
		vertex(vertexconsumer, matrix4f, matrix3f, f25, length, f26, red, green, blue, 0.0F, f30);

		float f31 = (attacker.tickCount % 2 == 0) ? 0.5F : 0.0F;
		vertex(vertexconsumer, matrix4f, matrix3f, f11, length, f12, red, green, blue, 0.5F, f31 + 0.5F);
		vertex(vertexconsumer, matrix4f, matrix3f, f13, length, f14, red, green, blue, 1.0F, f31 + 0.5F);
		vertex(vertexconsumer, matrix4f, matrix3f, f17, length, f18, red, green, blue, 1.0F, f31);
		vertex(vertexconsumer, matrix4f, matrix3f, f15, length, f16, red, green, blue, 0.5F, f31);

		poseStack.popPose();
	}

	private static Vec3 getPosition(LivingEntity entity, double yOffset, float partialTick) {
		double x = Mth.lerp(partialTick, entity.xOld, entity.getX());
		double y = Mth.lerp(partialTick, entity.yOld, entity.getY()) + yOffset;
		double z = Mth.lerp(partialTick, entity.zOld, entity.getZ());
		return new Vec3(x, y, z);
	}

	private static void vertex(VertexConsumer consumer, Matrix4f pose, Matrix3f normal,
							   float x, float y, float z, int red, int green, int blue, float u, float v) {
		consumer.vertex(pose, x, y, z)
				.color(red, green, blue, 255)
				.uv(u, v)
				.overlayCoords(OverlayTexture.NO_OVERLAY)
				.uv2(15728880)
				.normal(normal, 0.0F, 1.0F, 0.0F)
				.endVertex();
	}

	/**
	 * Рендерит луч от игрока к кристаллу, имитируя эффект эндер-дракона.
	 * Теперь направление изменено: луч исходит из позиции игрока и идёт к кристаллу.
	 */
	private static void renderCrystalBeamToPlayer(PoseStack poseStack, MultiBufferSource bufferSource, float partialTick, Vec3 cameraPos,
												  Vec3 playerPos, EndCrystal crystal, int packedLight, int tickCount) {
		Vec3 crystalPos = crystal.getPosition(partialTick).add(0, crystal.getBbHeight(), 0);
		float crystalYOffset = EndCrystalRenderer.getY(crystal, partialTick);
		double dx = crystalPos.x - playerPos.x;
		double dy = (crystalPos.y + crystalYOffset) - playerPos.y;  // добавляем смещение
		double dz = crystalPos.z - playerPos.z;
		float length = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);

		poseStack.pushPose();
		// Перемещаемся к позиции игрока (начало луча)
		poseStack.translate(playerPos.x - cameraPos.x, playerPos.y - cameraPos.y, playerPos.z - cameraPos.z);

		// Повороты, чтобы ось Z указывала от игрока к кристаллу (аналогично дракону)
		poseStack.mulPose(Axis.YP.rotation((float)(-Math.atan2(dz, dx)) - ((float)Math.PI / 2F)));
		poseStack.mulPose(Axis.XP.rotation((float)(-Math.atan2(Math.sqrt(dx * dx + dz * dz), dy)) - ((float)Math.PI / 2F)));

		float f2 = 0.0F - ((float)tickCount + partialTick) * 0.01F;
		float f3 = length / 32.0F - ((float)tickCount + partialTick) * 0.01F;

		VertexConsumer vertexconsumer = bufferSource.getBuffer(CRYSTAL_BEAM_RENDER_TYPE);
		Matrix4f matrix4f = poseStack.last().pose();
		Matrix3f matrix3f = poseStack.last().normal();

		float f4 = 0.0F;
		float f5 = 0.75F;
		float f6 = 0.0F;

		for (int j = 1; j <= 8; ++j) {
			float f7 = Mth.sin((float) j * ((float) Math.PI * 2F) / 8.0F) * 0.75F;
			float f8 = Mth.cos((float) j * ((float) Math.PI * 2F) / 8.0F) * 0.75F;
			float f9 = (float) j / 8.0F;

			// Вершина 1 (начало луча, внутренний радиус) – белая с alphaStart
			vertexconsumer.vertex(matrix4f, f4 * 0.2F, f5 * 0.2F, 0.0F)
					.color(0, 0, 0, 0)
					.uv(f6, f2)
					.overlayCoords(OverlayTexture.NO_OVERLAY)
					.uv2(packedLight)
					.normal(matrix3f, 0.0F, -1.0F, 0.0F)
					.endVertex();

			// Вершина 2 (конец луча, внешний радиус) – белая, непрозрачная
			vertexconsumer.vertex(matrix4f, f4, f5, length)
					.color(255, 255, 255, 255)
					.uv(f6, f3)
					.overlayCoords(OverlayTexture.NO_OVERLAY)
					.uv2(packedLight)
					.normal(matrix3f, 0.0F, -1.0F, 0.0F)
					.endVertex();

			// Вершина 3 (конец луча, следующий сегмент) – белая, непрозрачная
			vertexconsumer.vertex(matrix4f, f7, f8, length)
					.color(255, 255, 255, 255)
					.uv(f9, f3)
					.overlayCoords(OverlayTexture.NO_OVERLAY)
					.uv2(packedLight)
					.normal(matrix3f, 0.0F, -1.0F, 0.0F)
					.endVertex();

			// Вершина 4 (начало луча, внутренний радиус, следующий сегмент) – белая с alphaStart
			vertexconsumer.vertex(matrix4f, f7 * 0.2F, f8 * 0.2F, 0.0F)
					.color(0, 0, 0, 0)
					.uv(f9, f2)
					.overlayCoords(OverlayTexture.NO_OVERLAY)
					.uv2(packedLight)
					.normal(matrix3f, 0.0F, -1.0F, 0.0F)
					.endVertex();

			f4 = f7;
			f5 = f8;
			f6 = f9;
		}

		poseStack.popPose();
	}
}