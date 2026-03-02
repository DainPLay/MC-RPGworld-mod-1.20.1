package net.dainplay.rpgworldmod.mixin;

import net.dainplay.rpgworldmod.item.custom.ChooseTargetItem;
import net.dainplay.rpgworldmod.network.C2SRequestTargetValidationPacket;
import net.dainplay.rpgworldmod.network.ClientAnimateTargetData;
import net.dainplay.rpgworldmod.network.ClientItemTargetData;
import net.dainplay.rpgworldmod.network.ClientStorageTargetData;
import net.dainplay.rpgworldmod.network.ModMessages;
import net.dainplay.rpgworldmod.util.ModTags;
import net.dainplay.rpgworldmod.world.feature.ModConfiguredFeatures;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.sounds.Music;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.ContainerEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

import static net.dainplay.rpgworldmod.util.FogEventHandler.isInRieWeald;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {

	@Shadow
	public LocalPlayer player;

	@Shadow
	public ClientLevel level;

	@Shadow
	public HitResult hitResult;

	Minecraft mc = (Minecraft) (Object) this;

	@Inject(method = "getSituationalMusic", at = @At(value = "HEAD"), cancellable = true)
	private void getSituationalRieWealdMusic(CallbackInfoReturnable<Music> cir) {
		if (isInRieWeald()) cir.setReturnValue(ModConfiguredFeatures.RIE_WEALD_MUSIC_FOG);
	}

	@Inject(method = "shouldEntityAppearGlowing", at = @At("HEAD"), cancellable = true)
	private void onShouldEntityAppearGlowing(Entity entity, CallbackInfoReturnable<Boolean> cir) {
		if (this.player == null || this.level == null) {
			return;
		}

		if (this.player.isUsingItem() &&
				this.player.getUseItemRemainingTicks() > 0 &&
				this.player.getUseItem().getItem() instanceof ChooseTargetItem) {

			ChooseTargetItem catItem = (ChooseTargetItem) this.player.getUseItem().getItem();

			// Подсветка живых целей (анимация)
			if (catItem.highlightAnimateTarget(this.player.getUseItem(), this.player)) {
				if (!(entity instanceof LivingEntity)) {
					return;
				}
				LivingEntity target = null;
				if (player.isShiftKeyDown() && catItem.canHighlightYourself(this.player.getUseItem(), this.player))
					target = player;
				else
					target = findVisibleAnimateTargetInSight(this.player, 64.0, 15.0);

				if (target != null && target.getItemBySlot(EquipmentSlot.HEAD).isEnderMask(player, null))
					target = null;

				if (target instanceof Player) {
					ModMessages.sendToServer(new C2SRequestTargetValidationPacket(target.getId()));
					if (!ClientAnimateTargetData.isValidTarget(target)) {
						target = null;
					}
				}

				ClientAnimateTargetData.set(target);

				if (target != null && target.getId() == entity.getId()) {
					cir.setReturnValue(true);
					cir.cancel();
				}
			}

			if (catItem.highlightSpecificItemTarget(this.player.getUseItem(), this.player)) {
				if (!(entity instanceof ItemEntity)) {
					return;
				}
				ItemEntity target = findVisibleItemTargetInSight(this.player, 128.0, 15.0);
				ClientItemTargetData.clear();
				ClientItemTargetData.addTarget(target);

				if (target != null && target.getId() == entity.getId()) {
					cir.setReturnValue(true);
					cir.cancel();
				}
			}

			// Подсветка всех предметов в поле зрения (32 блока, угол 15°)
			if (catItem.highlightItemsInSight(this.player.getUseItem(), this.player)) {
				if (!(entity instanceof ItemEntity itemEntity)) {
					return;
				}
				List<ItemEntity> targetsInSight = getAllVisibleItemsInSight(this.player, 32.0, 60.0);
				ClientItemTargetData.clear();
				for (ItemEntity item : targetsInSight) {
					ClientItemTargetData.addTarget(item);
				}
				if (ClientItemTargetData.contains(itemEntity)) {
					cir.setReturnValue(true);
					cir.cancel();
					return;
				}
			}

			// Подсветка всех предметов в радиусе 16 блоков
			if (catItem.highlightItemsInRadius(this.player.getUseItem(), this.player)) {
				if (!(entity instanceof ItemEntity itemEntity)) {
					return;
				}
				if (ClientItemTargetData.contains(itemEntity)) {
					cir.setReturnValue(true);
					cir.cancel();
					return;
				}
			}

			// Подсветка случайного предмета в радиусе 64 блоков
			if (catItem.highlightRandomItemInRadius(this.player.getUseItem(), this.player)) {
				if (!(entity instanceof ItemEntity)) {
					return;
				}
				if (ClientItemTargetData.get() != null && ClientItemTargetData.get().getId() == entity.getId()) {
					cir.setReturnValue(true);
					cir.cancel();
					return;
				}
			}

			if (catItem.highlightItemStorages(this.player.getUseItem(), this.player)) {
				if (!(entity instanceof ContainerEntity)) {
					return;
				}
				if (ClientStorageTargetData.getEntityTarget() != null && ClientStorageTargetData.getEntityTarget().getId() == entity.getId()) {
					cir.setReturnValue(true);
					cir.cancel();
					return;
				}
			}
		}
	}

	// Вспомогательные методы для поиска предметов

	private List<ItemEntity> getAllVisibleItemsInSight(Player player, double maxDistance, double angleThreshold) {
		Vec3 eyePos = player.getEyePosition();
		AABB searchBox = player.getBoundingBox().inflate(maxDistance);
		List<ItemEntity> entities = level.getEntitiesOfClass(
				ItemEntity.class,
				searchBox,
				e -> isEntityVisible(player, e)
		);

		List<ItemEntity> visibleInSight = new ArrayList<>();
		for (ItemEntity entity : entities) {
			double angle = getAngleToCenter(player, entity);               // ← замена
			if (angle < angleThreshold && hasLineOfSightToCenter(player, entity, eyePos, maxDistance)) { // ← замена
				visibleInSight.add(entity);
			}
		}
		return visibleInSight;
	}

	// ---- существующие методы (без изменений) ----
	private ItemEntity findVisibleItemTargetInSight(Player player, double maxDistance, double angleThreshold) {
		Vec3 eyePos = player.getEyePosition();
		AABB searchBox = player.getBoundingBox().inflate(maxDistance);
		List<ItemEntity> entities = level.getEntitiesOfClass(
				ItemEntity.class,
				searchBox,
				e -> isEntityVisible(player, e)
		);

		ItemEntity closest = null;
		double closestAngle = angleThreshold;

		for (ItemEntity entity : entities) {
			double angle = getAngleToCenter(player, entity);               // ← замена
			if (angle < angleThreshold) {
				if (hasLineOfSightToCenter(player, entity, eyePos, maxDistance)) { // ← замена
					if (angle < closestAngle) {
						closestAngle = angle;
						closest = entity;
					}
				}
			}
		}
		return closest;
	}

	private LivingEntity findVisibleAnimateTargetInSight(Player player, double maxDistance, double angleThreshold) {
		Vec3 eyePos = player.getEyePosition();
		Vec3 lookVec = player.getViewVector(1.0F);

		AABB searchBox = player.getBoundingBox().inflate(maxDistance);
		List<LivingEntity> entities = level.getEntitiesOfClass(
				LivingEntity.class,
				searchBox,
				e -> e != player && e.isAlive() && isEntityVisible(player, e)
		);

		LivingEntity closest = null;
		double closestAngle = angleThreshold;

		for (LivingEntity entity : entities) {
			double angle = getMinAngleToBoundingBox(player, entity, maxDistance);
			if (angle < angleThreshold) {
				if (hasLineOfSightToBoundingBox(player, entity, eyePos, maxDistance)
						&& !entity.getType().is(ModTags.Entity.SOULLESS)) {
					if (angle < closestAngle) {
						closestAngle = angle;
						closest = entity;
					}
				}
			}
		}
		return closest;
	}

	private boolean isEntityVisible(Player player, Entity entity) {
		if (entity.isInvisible() || entity.isSpectator()) {
			return false;
		}
		if (entity instanceof net.minecraft.world.entity.player.Player) {
			net.minecraft.world.entity.player.Player targetPlayer = (net.minecraft.world.entity.player.Player) entity;
			if (!targetPlayer.canBeSeenByAnyone()) {
				return false;
			}
			if (targetPlayer.isCreative() || targetPlayer.isSpectator()) {
				return false;
			}
		}
		if (entity.level() != player.level()) {
			return false;
		}
		if (!isInCameraFrustum(entity, player)) {
			return false;
		}
		return true;
	}

	private boolean isInCameraFrustum(Entity entity, Player player) {
		Vec3 cameraPos = player.getEyePosition();
		Vec3 lookVec = player.getViewVector(1.0F);
		AABB entityBox = entity.getBoundingBox();
		Vec3 toEntity = entityBox.getCenter().subtract(cameraPos);
		if (toEntity.length() > 0) {
			toEntity = toEntity.normalize();
			double dot = lookVec.dot(toEntity);
			if (dot < 0) {
				return false;
			}
		}
		return true;
	}

	private double getMinAngleToBoundingBox(Player player, Entity entity, double maxDistance) {
		Vec3 eyePos = player.getEyePosition();
		Vec3 lookVec = player.getViewVector(1.0F).normalize();
		AABB boundingBox = entity.getBoundingBox();
		List<Vec3> testPoints = generateTestPoints(boundingBox);
		double minAngle = 360.0;
		for (Vec3 point : testPoints) {
			Vec3 toPoint = point.subtract(eyePos);
			double distance = toPoint.length();
			if (distance > maxDistance) continue;
			Vec3 normalizedToPoint = toPoint.normalize();
			double dot = lookVec.dot(normalizedToPoint);
			double angle = Math.acos(Math.max(-1.0, Math.min(1.0, dot))) * (180.0 / Math.PI);
			minAngle = Math.min(minAngle, angle);
		}
		return minAngle;
	}

	private List<Vec3> generateTestPoints(AABB box) {
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

	private boolean hasLineOfSightToBoundingBox(Player player, Entity entity, Vec3 startPos, double maxDistance) {
		AABB boundingBox = entity.getBoundingBox();
		List<Vec3> testPoints = generateTestPoints(boundingBox);
		Vec3 center = boundingBox.getCenter();
		if (hasLineOfSightToPoint(player, startPos, center, maxDistance)) {
			return true;
		}
		for (Vec3 point : testPoints) {
			if (hasLineOfSightToPoint(player, startPos, point, maxDistance)) {
				return true;
			}
		}
		return false;
	}

	private boolean hasLineOfSightToPoint(Player player, Vec3 start, Vec3 end, double maxDistance) {
		double distance = end.distanceTo(start);
		if (distance > maxDistance) {
			return false;
		}
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
			if (blockDist < pointDist - 0.3) {
				return false;
			}
		}
		return true;
	}

	private double getAngleToCenter(Player player, Entity entity) {
		Vec3 eyePos = player.getEyePosition();
		Vec3 lookVec = player.getViewVector(1.0F).normalize();
		Vec3 center = entity.getBoundingBox().getCenter();
		Vec3 toCenter = center.subtract(eyePos);
		double distance = toCenter.length();
		if (distance <= 0) return 360.0;
		Vec3 normalizedToCenter = toCenter.normalize();
		double dot = lookVec.dot(normalizedToCenter);
		double angle = Math.acos(Math.max(-1.0, Math.min(1.0, dot))) * (180.0 / Math.PI);
		return angle;
	}

	private boolean hasLineOfSightToCenter(Player player, Entity entity, Vec3 startPos, double maxDistance) {
		Vec3 center = entity.getBoundingBox().getCenter();
		return hasLineOfSightToPoint(player, startPos, center, maxDistance);
	}
}