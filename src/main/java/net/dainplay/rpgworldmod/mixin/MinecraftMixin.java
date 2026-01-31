package net.dainplay.rpgworldmod.mixin;

import net.dainplay.rpgworldmod.RPGworldMod;
import net.dainplay.rpgworldmod.item.custom.ChooseAnimateTargetItem;
import net.dainplay.rpgworldmod.network.C2SRequestTargetValidationPacket;
import net.dainplay.rpgworldmod.network.ClientAnimateTargetData;
import net.dainplay.rpgworldmod.network.ModMessages;
import net.dainplay.rpgworldmod.world.feature.ModConfiguredFeatures;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.Music;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;

import java.util.List;
import java.util.Optional;

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
		if (this.player == null || this.level == null || !(entity instanceof LivingEntity)) {
			return;
		}

		// Проверяем условия использования предмета
		if (this.player.isUsingItem() &&
				this.player.getUseItemRemainingTicks() > 0 &&
				this.player.getUseItem().getItem() instanceof ChooseAnimateTargetItem) {

			ChooseAnimateTargetItem catItem = (ChooseAnimateTargetItem) this.player.getUseItem().getItem();
			if (catItem.highlightTarget(this.player.getUseItem(), this.player)) {

				LivingEntity target = null;
				if (player.isShiftKeyDown())
					target = player;
				else
					target = findVisibleTargetInSight(this.player, 64.0, 15.0);

				if (target != null && target.getItemBySlot(EquipmentSlot.HEAD).isEnderMask(player, null))
					target = null;

				if (target instanceof Player) {
					ModMessages.sendToServer(new C2SRequestTargetValidationPacket(target.getId()));

					if (!ClientAnimateTargetData.isValidTarget(target)) {
						target = null;
					}
				}

				ClientAnimateTargetData.set(target);
            	/*if(target != null) player.sendSystemMessage(target.getName());
            	else player.sendSystemMessage(Component.literal("Без цели"));*/

				if (target != null && target.getId() == entity.getId()) {
					cir.setReturnValue(true);
					cir.cancel();
				}
			}
		}
	}

	private LivingEntity findVisibleTargetInSight(Player player, double maxDistance, double angleThreshold) {
		Vec3 eyePos = player.getEyePosition();
		Vec3 lookVec = player.getViewVector(1.0F);

		// Получаем все живые сущности в радиусе
		AABB searchBox = player.getBoundingBox().inflate(maxDistance);
		List<LivingEntity> entities = level.getEntitiesOfClass(
				LivingEntity.class,
				searchBox,
				e -> e != player && e.isAlive() && isEntityVisible(player, e)
		);

		LivingEntity closest = null;
		double closestAngle = angleThreshold;

		for (LivingEntity entity : entities) {
			// Проверяем, находится ли хоть какая-то точка хитбокса в поле зрения
			double angle = getMinAngleToBoundingBox(player, entity, maxDistance);

			if (angle < angleThreshold) {
				// Проверяем видимость до хитбокса
				if (hasLineOfSightToBoundingBox(player, entity, eyePos, maxDistance)) {
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
		// Проверяем базовую видимость сущности
		if (entity.isInvisible() || entity.isSpectator()) {
			return false;
		}

		// Для игроков дополнительная проверка
		if (entity instanceof net.minecraft.world.entity.player.Player) {
			net.minecraft.world.entity.player.Player targetPlayer = (net.minecraft.world.entity.player.Player) entity;

			// Проверяем, может ли игрок видеть этого игрока
			if (!targetPlayer.canBeSeenByAnyone()) {
				return false;
			}

			// Проверяем режим игры
			if (targetPlayer.isCreative() || targetPlayer.isSpectator()) {
				return false;
			}
		}

		// Проверяем, находится ли сущность в другом измерении или мире
		if (entity.level() != player.level()) {
			return false;
		}

		// Проверяем, находится ли сущность в поле зрения камеры (опционально)
		if (!isInCameraFrustum(entity, player)) {
			return false;
		}

		return true;
	}

	private boolean isInCameraFrustum(Entity entity, Player player) {
		// Получаем позицию камеры
		Vec3 cameraPos = player.getEyePosition();

		// Получаем направление взгляда
		Vec3 lookVec = player.getViewVector(1.0F);

		// Получаем bounding box сущности
		AABB entityBox = entity.getBoundingBox();

		// Упрощенная проверка: если сущность позади игрока, она не видима
		Vec3 toEntity = entityBox.getCenter().subtract(cameraPos);
		if (toEntity.length() > 0) {
			toEntity = toEntity.normalize();
			double dot = lookVec.dot(toEntity);

			// Если сущность позади игрока (угол > 90 градусов)
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

		// Генерируем точки для проверки на поверхности bounding box
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
		// Создаем точки для проверки на поверхности bounding box
		List<Vec3> points = new java.util.ArrayList<>();

		// Углы bounding box
		points.add(new Vec3(box.minX, box.minY, box.minZ));
		points.add(new Vec3(box.minX, box.minY, box.maxZ));
		points.add(new Vec3(box.minX, box.maxY, box.minZ));
		points.add(new Vec3(box.minX, box.maxY, box.maxZ));
		points.add(new Vec3(box.maxX, box.minY, box.minZ));
		points.add(new Vec3(box.maxX, box.minY, box.maxZ));
		points.add(new Vec3(box.maxX, box.maxY, box.minZ));
		points.add(new Vec3(box.maxX, box.maxY, box.maxZ));

		// Центры граней
		points.add(new Vec3((box.minX + box.maxX) / 2, box.minY, (box.minZ + box.maxZ) / 2));
		points.add(new Vec3((box.minX + box.maxX) / 2, box.maxY, (box.minZ + box.maxZ) / 2));
		points.add(new Vec3(box.minX, (box.minY + box.maxY) / 2, (box.minZ + box.maxZ) / 2));
		points.add(new Vec3(box.maxX, (box.minY + box.maxY) / 2, (box.minZ + box.maxZ) / 2));
		points.add(new Vec3((box.minX + box.maxX) / 2, (box.minY + box.maxY) / 2, box.minZ));
		points.add(new Vec3((box.minX + box.maxX) / 2, (box.minY + box.maxY) / 2, box.maxZ));

		// Центр bounding box
		points.add(new Vec3((box.minX + box.maxX) / 2, (box.minY + box.maxY) / 2, (box.minZ + box.maxZ) / 2));

		return points;
	}

	private boolean hasLineOfSightToBoundingBox(Player player, Entity entity, Vec3 startPos, double maxDistance) {
		AABB boundingBox = entity.getBoundingBox();
		List<Vec3> testPoints = generateTestPoints(boundingBox);

		// Сначала проверяем центр (самая вероятная видимая точка)
		Vec3 center = boundingBox.getCenter();
		if (hasLineOfSightToPoint(player, startPos, center, maxDistance)) {
			return true;
		}

		// Затем проверяем остальные точки
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

		// Проверяем коллизию с блоками
		ClipContext context = new ClipContext(
				start,
				end,
				ClipContext.Block.VISUAL, // Используем VISUAL для лучшей совместимости
				ClipContext.Fluid.NONE,
				player
		);

		BlockHitResult blockHit = level.clip(context);

		// Если луч столкнулся с блоком до достижения точки
		if (blockHit.getType() != HitResult.Type.MISS) {
			double blockDist = blockHit.getLocation().distanceTo(start);
			double pointDist = distance;

			// Если блок находится ближе, чем точка (с небольшим запасом)
			if (blockDist < pointDist - 0.3) { // Запас 0.3 блока для погрешности
				return false;
			}
		}

		return true;
	}


}
