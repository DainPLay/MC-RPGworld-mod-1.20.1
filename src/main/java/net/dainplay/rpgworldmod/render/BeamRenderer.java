package net.dainplay.rpgworldmod.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.dainplay.rpgworldmod.RPGworldMod;
import net.dainplay.rpgworldmod.enchantment.ModEnchantments;
import net.dainplay.rpgworldmod.item.ModItems;
import net.dainplay.rpgworldmod.item.custom.EnderEyeScrollItem;
import net.dainplay.rpgworldmod.item.custom.NetherStarScrollItem;
import net.dainplay.rpgworldmod.network.ClientGuardianAttackData;
import net.dainplay.rpgworldmod.particle.ModParticles;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EndCrystalRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

import java.util.List;

@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class BeamRenderer {
	private static final ResourceLocation BEAM_LOCATION = new ResourceLocation("textures/entity/guardian_beam.png");
	private static final RenderType BEAM_RENDER_TYPE = RenderType.entityCutoutNoCull(BEAM_LOCATION);
	private static final ResourceLocation CRYSTAL_BEAM_LOCATION = new ResourceLocation("textures/entity/end_crystal/end_crystal_beam.png");
	private static final RenderType CRYSTAL_BEAM_RENDER_TYPE = RenderType.entityTranslucent(CRYSTAL_BEAM_LOCATION);
	private static final ResourceLocation BEACON_BEAM_LOCATION = new ResourceLocation(RPGworldMod.MOD_ID, "textures/entity/spells/corrupted_beacon_beam.png");
	private static final ResourceLocation BEACON_GLOW_LOCATION = new ResourceLocation(RPGworldMod.MOD_ID, "textures/entity/spells/corrupted_beacon_glow.png");
	private static final ResourceLocation BEACON_BLACK_LOCATION = new ResourceLocation(RPGworldMod.MOD_ID, "textures/entity/spells/corrupted_beacon_black.png");

	@SubscribeEvent
	public static void onRenderLevelStage(RenderLevelStageEvent event) {
		if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_ENTITIES) return;

		Minecraft mc = Minecraft.getInstance();
		if (mc.level == null || mc.player == null) return;

		PoseStack poseStack = event.getPoseStack();
		MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();
		float partialTick = event.getPartialTick();
		Vec3 cameraPos = mc.gameRenderer.getMainCamera().getPosition();

		poseStack.pushPose();


		for (var entry : ClientGuardianAttackData.getAll().entrySet()) {
			ClientGuardianAttackData.AttackData data = entry.getValue();
			if (data.target == null || !data.target.isAlive() || data.attacker == null || !data.attacker.isAlive())
				continue;
			renderGuardianBeam(poseStack, bufferSource, partialTick, cameraPos, data.attacker, data.target, data.attackTime);
		}


		for (Player player : mc.level.players()) {
			if (player.isUsingItem()) {
				ItemStack usingItem = player.getUseItem();
				if (usingItem.getItem() instanceof EnderEyeScrollItem
						&& EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.RESTORATION.get(), usingItem) > 0) {
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


					if (nearest != null && nearestDistSq <= searchRadius * searchRadius) {
						Vec3 playerPos = player.getPosition(partialTick).add(0, player.getBbHeight() * 0.5, 0);
						int packedLight = mc.getEntityRenderDispatcher().getPackedLightCoords(nearest, partialTick);
						renderCrystalBeamToPlayer(poseStack, bufferSource, partialTick, cameraPos,
								playerPos, nearest, packedLight, player.tickCount);
					}
				}

				if (usingItem.getItem() instanceof NetherStarScrollItem &&
						EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.DESTRUCTION.get(), usingItem) > 0) {
					renderNetherStarBeam(poseStack, bufferSource, partialTick, cameraPos, player, player.getTicksUsingItem());
				}
			}
		}
		poseStack.popPose();
	}

	private static void renderNetherStarBeam(PoseStack poseStack, MultiBufferSource bufferSource,
											 float partialTick, Vec3 cameraPos, Player player, int charged) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.level == null) return;


		double maxDist = 128.0;
		Vec3 eyePos = player.getEyePosition(partialTick);
		Vec3 viewVec = player.getViewVector(partialTick);
		Vec3 rayEnd = eyePos.add(viewVec.x * maxDist, viewVec.y * maxDist, viewVec.z * maxDist);
		Vec3 targetPos = eyePos;
		double remaining = maxDist;
		Vec3 currentStart = eyePos;
		while (remaining > 0.01) {
			ClipContext ctx = new ClipContext(currentStart, currentStart.add(viewVec.scale(remaining)),
					ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player);
			BlockHitResult hit = mc.level.clip(ctx);
			if (hit.getType() == HitResult.Type.MISS) {
				targetPos = rayEnd;
				break;
			}
			var blockState = mc.level.getBlockState(hit.getBlockPos());
			if (blockState.canOcclude()) {
				targetPos = hit.getLocation();
				break;
			}
			double distToHit = eyePos.distanceTo(hit.getLocation());
			currentStart = hit.getLocation();
			remaining -= distToHit;
			targetPos = rayEnd;
		}


		boolean firstPerson = mc.options.getCameraType().isFirstPerson() && player == Minecraft.getInstance().player;
		Vec3 start = getMuzzlePosition(player, partialTick, firstPerson);


		Vec3 directionVec = targetPos.subtract(start);
		double length = directionVec.length();
		directionVec = directionVec.normalize();

		remaining = length;
		currentStart = start;
		double finalLength = length;
		while (remaining > 0.01) {
			ClipContext ctx = new ClipContext(currentStart, currentStart.add(directionVec.scale(remaining)),
					ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player);
			BlockHitResult hit = mc.level.clip(ctx);
			if (hit.getType() == HitResult.Type.MISS) break;

			var blockState = mc.level.getBlockState(hit.getBlockPos());
			if (blockState.canOcclude()) {
				finalLength = start.distanceTo(hit.getLocation());
				break;
			}
			double distToHit = start.distanceTo(hit.getLocation());
			currentStart = hit.getLocation();
			remaining -= distToHit;
		}
		if (charged > 40) generateBeamParticles(player, start, directionVec, finalLength);


		double dx = directionVec.z;
		double dy = -directionVec.y;
		double dz = -directionVec.x;
		float yaw = (float) Math.atan2(dz, dx);
		float pitch = (float) Math.asin(dy);

		poseStack.pushPose();
		poseStack.translate(start.x - cameraPos.x, start.y - cameraPos.y, start.z - cameraPos.z);
		poseStack.mulPose(Axis.YP.rotation(-yaw));
		poseStack.mulPose(Axis.XP.rotation(pitch));

		if (firstPerson && charged > 40) {
			renderBeamStartSprite(poseStack, bufferSource, player.tickCount, player.getTicksUsingItem(), partialTick);
		}

		if (length < 1) {
			poseStack.popPose();
			return;
		}

		float f = (float) (player.getTicksUsingItem() - 40) % 360 + partialTick;
		poseStack.mulPose(Axis.ZP.rotation((float) Math.toRadians(f) * 2.25F));

		float beamRadius = 0.1F;
		float glowRadius = 0.12F;
		float textureScale = 1.0F;

		if (charged <= 40) {
			glowRadius = 0.06F * charged / 40F;
		}

		float f1 = -f;
		float f2 = Mth.frac(f1 * 0.2F - (float) Mth.floor(f1 * 0.1F));
		float vStart = -1.0F + f2;
		float vEnd = (float) finalLength * textureScale + vStart;


		VertexConsumer glowConsumer = bufferSource.getBuffer(ModRenderTypes.SPELL_EFFECT.apply(BEACON_GLOW_LOCATION));
		renderGlowBeamSides(poseStack, glowConsumer, glowRadius, vStart, vEnd, 1.0F, 1.0F, 1.0F, 1F, (float) finalLength);

		if (charged > 40) {
			VertexConsumer beamConsumer = bufferSource.getBuffer(ModRenderTypes.SPELL_EFFECT.apply(BEACON_BEAM_LOCATION));
			renderBeamSides(poseStack, beamConsumer, beamRadius, vStart, vEnd, 1.0F, 1.0F, 1.0F, 1F, (float) finalLength);

			VertexConsumer blackConsumer = bufferSource.getBuffer(ModRenderTypes.SPELL_EFFECT.apply(BEACON_BLACK_LOCATION));
			renderGlowBeamSides(poseStack, blackConsumer, beamRadius, vStart, vEnd, 1.0F, 1.0F, 1.0F, 1F, (float) finalLength);
		}

		poseStack.popPose();

		if (charged > 40) {
			Vec3 end = start.add(directionVec.scale(finalLength));
			renderBillboardAtPosition(poseStack, bufferSource, end, cameraPos, (player.getTicksUsingItem() - 40), -f);
		}
	}

	private static void generateBeamParticles(Player player, Vec3 start, Vec3 direction, double length) {
		RandomSource random = player.level().getRandom();
		double step = 0.5;

		for (double d = 0; d < length; d += step) {
			Vec3 pos = start.add(direction.scale(d));

			if (random.nextFloat() < 0.01f) {
				float speedModifier = 0.0005F;
				player.level().addParticle(ModParticles.WHITE_NETHER_STAR_BEAM.get(), pos.x, pos.y, pos.z,
						random.nextFloat() * speedModifier, random.nextFloat() * speedModifier, random.nextFloat() * speedModifier);
			}

			if (random.nextFloat() < 0.01f) {
				float speedModifier = 0F;
				player.level().addParticle(ModParticles.BLACK_NETHER_STAR_BEAM.get(), pos.x, pos.y, pos.z,
						random.nextFloat() * speedModifier, random.nextFloat() * speedModifier, random.nextFloat() * speedModifier);
			}
		}
	}

	private static void renderBillboardAtPosition(PoseStack poseStack, MultiBufferSource bufferSource,
												  Vec3 worldPos, Vec3 cameraPos,
												  int tickCount, float f) {
		poseStack.pushPose();

		poseStack.translate(worldPos.x - cameraPos.x, worldPos.y - cameraPos.y, worldPos.z - cameraPos.z);


		Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
		Vec3 direction = cameraPos.subtract(worldPos);
		double angleY = Math.atan2(direction.x, direction.z);
		if (true) poseStack.mulPose(Axis.YP.rotation((float) angleY));
		poseStack.mulPose(Axis.XP.rotationDegrees(-camera.getXRot()));
		poseStack.mulPose(Axis.ZP.rotation((float) Math.toRadians(f) * 2.25F));
		if (true) poseStack.translate(0F, 0F, 0.5F);


		int currentFrame = (tickCount / 2) % 10;
		float frameHeight = 1.0F / 10;
		float vMin = currentFrame * frameHeight;
		float vMax = vMin + frameHeight;

		ResourceLocation texture = new ResourceLocation(RPGworldMod.MOD_ID, "textures/entity/spells/nether.png");
		VertexConsumer consumer = bufferSource.getBuffer(ModRenderTypes.SPELL_EFFECT.apply(texture));

		PoseStack.Pose pose = poseStack.last();
		Matrix4f matrix = pose.pose();
		Matrix3f normal = pose.normal();

		float halfSize = (float) 0.75 / 2.0f;


		consumer.vertex(matrix, -halfSize, -halfSize, 0.0F)
				.color(1.0F, 1.0F, 1.0F, 1.0F)
				.uv(0.0F, vMax)
				.overlayCoords(OverlayTexture.NO_OVERLAY)
				.uv2(15728880)
				.normal(normal, 0.0F, 0.0F, 1.0F)
				.endVertex();

		consumer.vertex(matrix, halfSize, -halfSize, 0.0F)
				.color(1.0F, 1.0F, 1.0F, 1.0F)
				.uv(1.0F, vMax)
				.overlayCoords(OverlayTexture.NO_OVERLAY)
				.uv2(15728880)
				.normal(normal, 0.0F, 0.0F, 1.0F)
				.endVertex();

		consumer.vertex(matrix, halfSize, halfSize, 0.0F)
				.color(1.0F, 1.0F, 1.0F, 1.0F)
				.uv(1.0F, vMin)
				.overlayCoords(OverlayTexture.NO_OVERLAY)
				.uv2(15728880)
				.normal(normal, 0.0F, 0.0F, 1.0F)
				.endVertex();

		consumer.vertex(matrix, -halfSize, halfSize, 0.0F)
				.color(1.0F, 1.0F, 1.0F, 1.0F)
				.uv(0.0F, vMin)
				.overlayCoords(OverlayTexture.NO_OVERLAY)
				.uv2(15728880)
				.normal(normal, 0.0F, 0.0F, 1.0F)
				.endVertex();

		poseStack.popPose();
	}

	private static void renderBeamSides(PoseStack poseStack, VertexConsumer consumer,
										float radius, float vStart, float vEnd,
										float r, float g, float b, float a, float length) {
		PoseStack.Pose pose = poseStack.last();
		Matrix4f matrix4f = pose.pose();
		Matrix3f matrix3f = pose.normal();


		renderQuadZ(matrix4f, matrix3f, consumer, r, g, b, a,
				0.0F, length, -radius, -radius, radius, -radius, 0.0F, 1.0F, vEnd, vStart);
		renderQuadZ(matrix4f, matrix3f, consumer, r, g, b, a,
				0.0F, length, radius, radius, -radius, radius, 0.0F, 1.0F, vEnd, vStart);
		renderQuadZ(matrix4f, matrix3f, consumer, r, g, b, a,
				0.0F, length, -radius, radius, -radius, -radius, 0.0F, 1.0F, vEnd, vStart);
		renderQuadZ(matrix4f, matrix3f, consumer, r, g, b, a,
				0.0F, length, radius, -radius, radius, radius, 0.0F, 1.0F, vEnd, vStart);
	}

	private static void renderGlowBeamSides(PoseStack poseStack, VertexConsumer consumer,
											float radius, float vStart, float vEnd,
											float r, float g, float b, float a, float length) {
		PoseStack.Pose pose = poseStack.last();
		Matrix4f matrix4f = pose.pose();
		Matrix3f matrix3f = pose.normal();


		renderQuadZ(matrix4f, matrix3f, consumer, r, g, b, a,
				0.0F, length, radius, -radius, -radius, -radius, 0.0F, 1.0F, vEnd, vStart);

		renderQuadZ(matrix4f, matrix3f, consumer, r, b, b, a,
				0.0F, length, -radius, radius, radius, radius, 0.0F, 1.0F, vEnd, vStart);

		renderQuadZ(matrix4f, matrix3f, consumer, r, g, b, a,
				0.0F, length, -radius, -radius, -radius, radius, 0.0F, 1.0F, vEnd, vStart);

		renderQuadZ(matrix4f, matrix3f, consumer, r, g, b, a,
				0.0F, length, radius, radius, radius, -radius, 0.0F, 1.0F, vEnd, vStart);
	}

	private static void renderBeamStartSprite(PoseStack poseStack, MultiBufferSource bufferSource,
											  int tickCount, int ticksUsingItem, float partialTick) {
		float textureSize = 0.3F;
		if (ticksUsingItem <= 50) {
			float t = (ticksUsingItem + partialTick - 40) / 10;

			float easeOut = 1 - (1 - t) * (1 - t);
			textureSize += 0.3F * (1 - easeOut);
		}
		int currentFrame = (tickCount / 2) % 10;
		float frameHeight = 1.0F / 10;
		float vMin = currentFrame * frameHeight;
		float vMax = vMin + frameHeight;

		ResourceLocation texture = new ResourceLocation(RPGworldMod.MOD_ID, "textures/entity/spells/nether.png");

		VertexConsumer texConsumer = bufferSource.getBuffer(ModRenderTypes.SPELL_EFFECT.apply(texture));

		PoseStack.Pose pose = poseStack.last();
		Matrix4f mat = pose.pose();
		Matrix3f norm = pose.normal();


		texConsumer.vertex(mat, -textureSize, -textureSize, 0.0F)
				.color(1.0F, 1.0F, 1.0F, 1.0F)
				.uv(0.0F, vMin)
				.overlayCoords(OverlayTexture.NO_OVERLAY)
				.uv2(15728880)
				.normal(norm, 0.0F, 0.0F, -1.0F)
				.endVertex();
		texConsumer.vertex(mat, -textureSize, textureSize, 0.0F)
				.color(1.0F, 1.0F, 1.0F, 1.0F)
				.uv(0.0F, vMax)
				.overlayCoords(OverlayTexture.NO_OVERLAY)
				.uv2(15728880)
				.normal(norm, 0.0F, 0.0F, -1.0F)
				.endVertex();
		texConsumer.vertex(mat, textureSize, textureSize, 0.0F)
				.color(1.0F, 1.0F, 1.0F, 1.0F)
				.uv(1.0F, vMax)
				.overlayCoords(OverlayTexture.NO_OVERLAY)
				.uv2(15728880)
				.normal(norm, 0.0F, 0.0F, -1.0F)
				.endVertex();
		texConsumer.vertex(mat, textureSize, -textureSize, 0.0F)
				.color(1.0F, 1.0F, 1.0F, 1.0F)
				.uv(1.0F, vMin)
				.overlayCoords(OverlayTexture.NO_OVERLAY)
				.uv2(15728880)
				.normal(norm, 0.0F, 0.0F, -1.0F)
				.endVertex();
	}

	private static void renderQuadZ(Matrix4f pose, Matrix3f normal, VertexConsumer consumer,
									float r, float g, float b, float a,
									float zStart, float zEnd,
									float x1, float y1, float x2, float y2,
									float uMin, float uMax, float vMin, float vMax) {
		addVertexZ(pose, normal, consumer, r, g, b, a, zEnd, x1, y1, uMax, vMin);
		addVertexZ(pose, normal, consumer, r, g, b, a, zStart, x1, y1, uMax, vMax);
		addVertexZ(pose, normal, consumer, r, g, b, a, zStart, x2, y2, uMin, vMax);
		addVertexZ(pose, normal, consumer, r, g, b, a, zEnd, x2, y2, uMin, vMin);
	}


	private static void addVertexZ(Matrix4f pose, Matrix3f normal, VertexConsumer consumer,
								   float r, float g, float b, float a,
								   float z, float x, float y,
								   float u, float v) {
		consumer.vertex(pose, x, y, z)
				.color(r, g, b, a)
				.uv(u, v)
				.overlayCoords(OverlayTexture.NO_OVERLAY)
				.uv2(15728880)
				.normal(normal, 0.0F, 1.0F, 0.0F)
				.endVertex();
	}


	private static Vec3 getMuzzlePosition(Player player, float partialTick, boolean firstPerson) {
		Minecraft mc = Minecraft.getInstance();
		HumanoidArm mainArm = player.getMainArm();
		boolean leftHand = mainArm == HumanoidArm.LEFT;
		int side = leftHand ? -1 : 1;
		if (player.getUsedItemHand() == InteractionHand.OFF_HAND) side *= -1;

		if (firstPerson) {
			Vec3 cameraPos = mc.gameRenderer.getMainCamera().getPosition();
			double scale = 1000.0 / mc.getEntityRenderDispatcher().options.fov().get().intValue();
			boolean isSlim = false;
			if (player instanceof AbstractClientPlayer localPlayer) {
				isSlim = "slim".equals(localPlayer.getModelName());
			}
			float pLeftScale = isSlim ? 0.414F : 0.438F;
			float pUpScale = isSlim ? -0.545F : -0.447F;
			Vec3 nearPoint = mc.getEntityRenderDispatcher().camera.getNearPlane().getPointOnPlane(side * pLeftScale, pUpScale);
			float attackAnim = player.getAttackAnim(partialTick);
			float f1 = Mth.sin(Mth.sqrt(attackAnim) * (float) Math.PI);
			nearPoint = nearPoint.scale(scale);
			nearPoint = nearPoint.yRot(f1 * 0.5F);
			nearPoint = nearPoint.xRot(-f1 * 0.7F);
			return cameraPos.add(nearPoint);
		} else {
			Vec3 playerPos = player.getPosition(partialTick);
			float yBodyRot = Mth.lerp(partialTick, player.yBodyRotO, player.yBodyRot);
			double armOffsetX = player.getBbWidth() * -0.5F * side;
			double armOffsetY = player.getBbHeight() * 0.71F;
			Vec3 offset = new Vec3(armOffsetX, armOffsetY, 0).yRot((float) Math.toRadians(-yBodyRot));
			Vec3 viewVec = player.getViewVector(partialTick).normalize().scale(0.574F);
			return playerPos.add(offset).add(viewVec);
		}
	}

	private static void renderGuardianBeam(PoseStack poseStack, MultiBufferSource bufferSource, float partialTick, Vec3 cameraPos,
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


	private static void renderCrystalBeamToPlayer(PoseStack poseStack, MultiBufferSource bufferSource, float partialTick, Vec3 cameraPos,
												  Vec3 playerPos, EndCrystal crystal, int packedLight, int tickCount) {
		Vec3 crystalPos = crystal.getPosition(partialTick).add(0, crystal.getBbHeight(), 0);
		float crystalYOffset = EndCrystalRenderer.getY(crystal, partialTick);
		double dx = crystalPos.x - playerPos.x;
		double dy = (crystalPos.y + crystalYOffset) - playerPos.y;
		double dz = crystalPos.z - playerPos.z;
		float length = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);

		poseStack.pushPose();

		poseStack.translate(playerPos.x - cameraPos.x, playerPos.y - cameraPos.y, playerPos.z - cameraPos.z);


		poseStack.mulPose(Axis.YP.rotation((float) (-Math.atan2(dz, dx)) - ((float) Math.PI / 2F)));
		poseStack.mulPose(Axis.XP.rotation((float) (-Math.atan2(Math.sqrt(dx * dx + dz * dz), dy)) - ((float) Math.PI / 2F)));

		float f2 = 0.0F - ((float) tickCount + partialTick) * 0.01F;
		float f3 = length / 32.0F - ((float) tickCount + partialTick) * 0.01F;

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


			vertexconsumer.vertex(matrix4f, f4 * 0.2F, f5 * 0.2F, 0.0F)
					.color(0, 0, 0, 0)
					.uv(f6, f2)
					.overlayCoords(OverlayTexture.NO_OVERLAY)
					.uv2(packedLight)
					.normal(matrix3f, 0.0F, -1.0F, 0.0F)
					.endVertex();


			vertexconsumer.vertex(matrix4f, f4, f5, length)
					.color(255, 255, 255, 255)
					.uv(f6, f3)
					.overlayCoords(OverlayTexture.NO_OVERLAY)
					.uv2(packedLight)
					.normal(matrix3f, 0.0F, -1.0F, 0.0F)
					.endVertex();


			vertexconsumer.vertex(matrix4f, f7, f8, length)
					.color(255, 255, 255, 255)
					.uv(f9, f3)
					.overlayCoords(OverlayTexture.NO_OVERLAY)
					.uv2(packedLight)
					.normal(matrix3f, 0.0F, -1.0F, 0.0F)
					.endVertex();


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