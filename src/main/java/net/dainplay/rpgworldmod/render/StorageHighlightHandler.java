package net.dainplay.rpgworldmod.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.dainplay.rpgworldmod.item.custom.ChooseTargetItem;
import net.dainplay.rpgworldmod.item.custom.HornCoralStaffItem;
import net.dainplay.rpgworldmod.network.ClientStorageTargetData;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.ContainerEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.EnderChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderHighlightEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class StorageHighlightHandler {

	private static final double SEARCH_RADIUS = 64.0;          // дальность поиска хранилищ
	private static final double ANGLE_THRESHOLD = 15.0;       // конус 15 градусов

	// Вспомогательный класс для кандидатов
	private static class Candidate {
		enum Type {BLOCK, ENTITY}

		Type type;
		Object target; // BlockEntity или ContainerEntity
		double angle;
		double distanceToPlayer;

		Candidate(Type type, Object target, double angle, double distanceToPlayer) {
			this.type = type;
			this.target = target;
			this.angle = angle;
			this.distanceToPlayer = distanceToPlayer;
		}
	}

	@SubscribeEvent
	public static void onHighlightBlock(RenderHighlightEvent.Block event) {
		Player player = Minecraft.getInstance().player;
		Level level = Minecraft.getInstance().level;
		if (player == null || level == null) {
			return;
		}

		if (player.isUsingItem() &&
				player.getUseItemRemainingTicks() > 0 &&
				player.getUseItem().getItem() instanceof ChooseTargetItem catItem
				&& catItem.highlightItemStorages(player.getUseItem(), player)) {
			BlockHitResult target = event.getTarget();
			BlockEntity be = level.getBlockEntity(target.getBlockPos());
			if (HornCoralStaffItem.isStorage(be)) {
				event.setCanceled(true); // отменяем стандартную обводку
			}
		}
	}

	@SubscribeEvent
	public static void onRenderLevelLast(RenderLevelStageEvent event) {
		if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_BLOCK_ENTITIES) return;
		Player player = Minecraft.getInstance().player;
		Level level = Minecraft.getInstance().level;
		if (player == null || level == null) return;

		if (player.isUsingItem() &&
				player.getUseItemRemainingTicks() > 0 &&
				player.getUseItem().getItem() instanceof ChooseTargetItem catItem
				&& catItem.highlightItemStorages(player.getUseItem(), player)) {

			Vec3 eyePos = player.getEyePosition(); // используем глаза игрока
			Vec3 lookVec = player.getLookAngle();

			AABB searchArea = player.getBoundingBox().inflate(SEARCH_RADIUS);

			// Блоки
			List<BlockEntity> storages = getStoragesInAABB(level, searchArea);

			// Сущности
			List<Entity> containerEntities = level.getEntitiesOfClass(Entity.class, searchArea,
					e -> e instanceof ContainerEntity && isContainerEntityVisible(player, e));

			List<Candidate> candidates = new ArrayList<>();

			// Блоки
			for (BlockEntity be : storages) {
				BlockPos pos = be.getBlockPos();
				AABB blockBox = getBlockAABB(level, pos);
				double angle = getMinAngleToAABB(eyePos, lookVec, blockBox, SEARCH_RADIUS);
				if (angle <= ANGLE_THRESHOLD && hasLineOfSightToAABB(player, level, eyePos, blockBox, SEARCH_RADIUS)) {
					double distance = eyePos.distanceTo(blockBox.getCenter());
					candidates.add(new Candidate(Candidate.Type.BLOCK, be, angle, distance));
				}
			}

			// Сущности
			for (Entity entity : containerEntities) {
				AABB entityBox = entity.getBoundingBox();
				double angle = getMinAngleToAABB(eyePos, lookVec, entityBox, SEARCH_RADIUS);
				if (angle <= ANGLE_THRESHOLD && hasLineOfSightToAABB(player, level, eyePos, entityBox, SEARCH_RADIUS)) {
					double distance = eyePos.distanceTo(entityBox.getCenter());
					candidates.add(new Candidate(Candidate.Type.ENTITY, entity, angle, distance));
				}
			}

			// Выбираем лучшего кандидата (минимальный угол, при равенстве — ближайший по расстоянию)
			Candidate best = null;
			for (Candidate c : candidates) {
				if (best == null) {
					best = c;
				} else {
					if (c.angle < best.angle - 1e-6) { // угол значительно меньше
						best = c;
					} else if (Math.abs(c.angle - best.angle) < 1e-6) { // углы примерно равны
						if (c.distanceToPlayer < best.distanceToPlayer) {
							best = c;
						}
					}
				}
			}

			// Сохраняем цель и рисуем обводку
			if (best != null) {
				if (best.type == Candidate.Type.BLOCK) {
					BlockEntity be = (BlockEntity) best.target;
					ClientStorageTargetData.set(null, be.getBlockPos());
					renderBlockOutline(event, level, be.getBlockPos(), event.getPoseStack(), Minecraft.getInstance().renderBuffers().bufferSource(), event.getCamera());
				} else {
					Entity entity = (Entity) best.target;
					ClientStorageTargetData.set(entity, null);
				}
			} else {
				ClientStorageTargetData.clear();
			}
		}
	}

	// === Поиск всех блоков-хранилищ внутри AABB ===
	private static List<BlockEntity> getStoragesInAABB(Level level, AABB area) {
		List<BlockEntity> storages = new ArrayList<>();
		int minX = (int) Math.floor(area.minX) >> 4;
		int maxX = (int) Math.floor(area.maxX) >> 4;
		int minZ = (int) Math.floor(area.minZ) >> 4;
		int maxZ = (int) Math.floor(area.maxZ) >> 4;

		for (int cx = minX; cx <= maxX; cx++) {
			for (int cz = minZ; cz <= maxZ; cz++) {
				LevelChunk chunk = level.getChunkSource().getChunk(cx, cz, false);
				if (chunk != null) {
					for (BlockEntity be : chunk.getBlockEntities().values()) {
						if (!HornCoralStaffItem.isStorage(be)) continue;
						BlockPos pos = be.getBlockPos();
						AABB blockAABB = getBlockAABB(level, pos);
						if (area.intersects(blockAABB)) {
							storages.add(be);
						}
					}
				}
			}
		}
		return storages;
	}

	// Вспомогательная проверка видимости сущности
	private static boolean isContainerEntityVisible(Player player, Entity entity) {
		if (entity.isInvisible() || entity.isSpectator()) return false;
		if (entity.level() != player.level()) return false;
		return true;
	}

	// === Отрисовка обводки блока ===
	private static void renderBlockOutline(RenderLevelStageEvent event, Level level, BlockPos pos,
										   PoseStack poseStack, MultiBufferSource bufferSource, Camera camera) {
		BlockState state = level.getBlockState(pos);
		VoxelShape shape = state.getShape(level, pos);
		if (shape.isEmpty()) {
			shape = Shapes.block();
		}
		VertexConsumer consumer = bufferSource.getBuffer(ModRenderTypes.GLOWING_OUTLINE);
		double camX = camera.getPosition().x;
		double camY = camera.getPosition().y;
		double camZ = camera.getPosition().z;
		LevelRenderer.renderShape(
				poseStack,
				consumer,
				shape,
				pos.getX() - camX,
				pos.getY() - camY,
				pos.getZ() - camZ,
				1.0f, 1.0f, 1.0f, 1.0f  // белый цвет (RGBA), полностью непрозрачный
		);
	}

	// === Вспомогательные методы для геометрии и видимости ===

	private static AABB getBlockAABB(Level level, BlockPos pos) {
		BlockState state = level.getBlockState(pos);
		VoxelShape shape = state.getShape(level, pos);
		if (shape.isEmpty()) {
			shape = Shapes.block();
		}
		return shape.bounds().move(pos.getX(), pos.getY(), pos.getZ());
	}

	private static double getMinAngleToAABB(Vec3 eyePos, Vec3 lookVec, AABB box, double maxDistance) {
		List<Vec3> points = generateTestPoints(box);
		double minAngle = 360.0;
		for (Vec3 point : points) {
			Vec3 toPoint = point.subtract(eyePos);
			double distance = toPoint.length();
			if (distance > maxDistance) continue;
			Vec3 normalized = toPoint.normalize();
			double dot = lookVec.dot(normalized);
			double angle = Math.acos(Math.max(-1.0, Math.min(1.0, dot))) * (180.0 / Math.PI);
			if (angle < minAngle) {
				minAngle = angle;
			}
		}
		return minAngle;
	}

	private static boolean hasLineOfSightToAABB(Player player, Level level, Vec3 startPos, AABB box, double maxDistance) {
		List<Vec3> points = generateTestPoints(box);
		// сначала центр
		if (hasLineOfSightToPoint(player, level, startPos, box.getCenter(), maxDistance)) {
			return true;
		}
		for (Vec3 point : points) {
			if (hasLineOfSightToPoint(player, level, startPos, point, maxDistance)) {
				return true;
			}
		}
		return false;
	}

	private static boolean hasLineOfSightToPoint(Player player, Level level, Vec3 start, Vec3 end, double maxDistance) {
		double distance = end.distanceTo(start);
		if (distance > maxDistance) return false;
		ClipContext context = new ClipContext(
				start,
				end,
				ClipContext.Block.VISUAL,
				ClipContext.Fluid.NONE,
				player
		);
		BlockHitResult blockHit = level.clip(context);
		if (blockHit.getType() != HitResult.Type.MISS) {
			double blockDist = blockHit.getLocation().distanceTo(start);
			double pointDist = distance;
			if (blockDist < pointDist - 0.3) { // небольшой допуск
				return false;
			}
		}
		return true;
	}

	private static List<Vec3> generateTestPoints(AABB box) {
		List<Vec3> points = new ArrayList<>();
		points.add(new Vec3(box.minX, box.minY, box.minZ));
		points.add(new Vec3(box.minX, box.minY, box.maxZ));
		points.add(new Vec3(box.minX, box.maxY, box.minZ));
		points.add(new Vec3(box.minX, box.maxY, box.maxZ));
		points.add(new Vec3(box.maxX, box.minY, box.minZ));
		points.add(new Vec3(box.maxX, box.minY, box.maxZ));
		points.add(new Vec3(box.maxX, box.maxY, box.minZ));
		points.add(new Vec3(box.maxX, box.maxY, box.maxZ));
		points.add(new Vec3((box.minX + box.maxX) / 2, box.minY, (box.minZ + box.maxZ) / 2));
		points.add(new Vec3((box.minX + box.maxX) / 2, box.maxY, (box.minZ + box.maxZ) / 2));
		points.add(new Vec3(box.minX, (box.minY + box.maxY) / 2, (box.minZ + box.maxZ) / 2));
		points.add(new Vec3(box.maxX, (box.minY + box.maxY) / 2, (box.minZ + box.maxZ) / 2));
		points.add(new Vec3((box.minX + box.maxX) / 2, (box.minY + box.maxY) / 2, box.minZ));
		points.add(new Vec3((box.minX + box.maxX) / 2, (box.minY + box.maxY) / 2, box.maxZ));
		points.add(new Vec3((box.minX + box.maxX) / 2, (box.minY + box.maxY) / 2, (box.minZ + box.maxZ) / 2));
		return points;
	}
}