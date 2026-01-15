package net.dainplay.rpgworldmod.mixin;

import net.dainplay.rpgworldmod.effect.ModEffects;
import net.dainplay.rpgworldmod.network.ClientEntPositionData;
import net.dainplay.rpgworldmod.sounds.RPGSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public class MouseHandlerMixin {

	@Shadow private double xpos;
	@Shadow private double ypos;
	@Shadow private double accumulatedDX;
	@Shadow private double accumulatedDY;

	@Unique private double frozenAccumulatedDX = 0;
	@Unique private double frozenAccumulatedDY = 0;

	@Unique private float targetYaw = 0;
	@Unique private float targetPitch = 0;
	@Unique private float currentYaw = 0;
	@Unique private float currentPitch = 0;
	@Unique private boolean isTracking = false;

	@Inject(method = "turnPlayer", at = @At(value = "HEAD"), cancellable = true)
	private void onTurnPlayer(CallbackInfo ci) {
		if (Minecraft.getInstance().player != null) {
			Player player = Minecraft.getInstance().player;

			// Проверяем эффект паралича
			if (player.hasEffect(ModEffects.PARALYSIS.get()) && !player.isCreative() && !player.isSpectator() &&
					player.getEffect(ModEffects.PARALYSIS.get()).getAmplifier() >= 1) {
				this.accumulatedDX = this.frozenAccumulatedDX;
				this.accumulatedDY = this.frozenAccumulatedDY;
				ci.cancel();
				return;
			}

			BlockPos targetPos = ClientEntPositionData.get();
			if (targetPos != null) {
				// Рассчитываем целевые углы
				Vec3 targetVec = new Vec3(targetPos.getX() + 0.5, targetPos.getY() + 0.5, targetPos.getZ() + 0.5);
				Vec3 playerVec = player.getEyePosition();

				double dx = targetVec.x - playerVec.x;
				double dy = targetVec.y - playerVec.y;
				double dz = targetVec.z - playerVec.z;

				double distance = Math.sqrt(dx * dx + dz * dz);
				targetYaw = (float) (Math.atan2(dz, dx) * (180.0 / Math.PI)) - 90.0f;
				targetPitch = (float) (-Math.atan2(dy, distance) * (180.0 / Math.PI));

				// Нормализуем углы
				targetYaw = normalizeAngle(targetYaw);
				targetPitch = normalizeAngle(targetPitch);

				// Получаем текущие углы игрока
				float playerYaw = player.getYRot();
				float playerPitch = player.getXRot();

				// Если только начали отслеживание, инициализируем
				if (!isTracking) {
					currentYaw = playerYaw;
					currentPitch = playerPitch;
					isTracking = true;

					// Включаем плавную камеру Minecraft
					Minecraft.getInstance().options.smoothCamera = true;
				}

				// Плавная интерполяция
				float rotationSpeed = 0.12f; // Регулируйте для нужной плавности
				currentYaw = lerpAngle(currentYaw, targetYaw, rotationSpeed);
				currentPitch = lerpAngle(currentPitch, targetPitch, rotationSpeed);

				// Устанавливаем новые углы игроку
				player.setYRot(currentYaw);
				player.setXRot(currentPitch);

				// Блокируем ввод мыши
				this.accumulatedDX = 0;
				this.accumulatedDY = 0;
				this.frozenAccumulatedDX = 0;
				this.frozenAccumulatedDY = 0;

				ci.cancel();
			} else {
				// Если слежение завершено
				if (isTracking) {
					isTracking = false;
					// Выключаем плавную камеру
					Minecraft.getInstance().options.smoothCamera = false;
				}
				this.frozenAccumulatedDX = this.accumulatedDX;
				this.frozenAccumulatedDY = this.accumulatedDY;
			}
		}
	}

	@Unique
	private float normalizeAngle(float angle) {
		angle = angle % 360;
		if (angle > 180) {
			angle -= 360;
		} else if (angle < -180) {
			angle += 360;
		}
		return angle;
	}

	@Unique
	private float lerpAngle(float current, float target, float speed) {
		// Вычисляем кратчайший путь для круговой интерполяции
		float diff = target - current;
		while (diff > 180) diff -= 360;
		while (diff < -180) diff += 360;

		// Применяем коэффициент плавности
		return current + diff * speed;
	}
}