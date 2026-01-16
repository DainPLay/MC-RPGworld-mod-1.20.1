package net.dainplay.rpgworldmod.entity.custom;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;

public class TireSwingEntity extends Entity {
	private static final EntityDataAccessor<Float> DATA_SWING_ANGLE = SynchedEntityData.defineId(TireSwingEntity.class, EntityDataSerializers.FLOAT);
	private static final EntityDataAccessor<Float> DATA_SWING_VELOCITY = SynchedEntityData.defineId(TireSwingEntity.class, EntityDataSerializers.FLOAT);
	private static final EntityDataAccessor<Boolean> DATA_OCCUPIED = SynchedEntityData.defineId(TireSwingEntity.class, EntityDataSerializers.BOOLEAN);
	private static final EntityDataAccessor<Float> DATA_PASSENGER_YAW = SynchedEntityData.defineId(TireSwingEntity.class, EntityDataSerializers.FLOAT);

	// Физические константы
	private static final float MAX_SWING_ANGLE = 70.0F;
	private static final float SWING_DAMPING = 0.995F;
	private static final float SWING_GRAVITY = 0.3F;
	private static final float SWING_GRAVITY_BOOST = 0.1F; // Ускорение при больших углах
	private static final float PLAYER_PUSH_STRENGTH = 0.015F;
	private static final float STOP_THRESHOLD = 0.01F;
	private static final float ROPE_LENGTH = 5.0F;

	// Константы для зависимости толчка от скорости
	private static final float MIN_VELOCITY_FOR_MAX_PUSH = 5.0F;
	private static final float MAX_VELOCITY_MULTIPLIER = 2.0F;
	private static final float MIN_VELOCITY_MULTIPLIER = 0.3F;

	// Константы для поворота модели
	private static final float BASE_MODEL_ROTATION = 45.0F;
	private static final float MAX_MODEL_ROTATION_ADD = 15.0F;

	// Переменные для анимации и интерполяции
	private float swingProgress = 0.0F;
	private float lastSwingProgress = 0.0F;
	private float renderSwingAngle = 0.0F;
	private float lastRenderSwingAngle = 0.0F;
	private float passengerBodyYaw = 0.0F;
	private float lastPassengerBodyYaw = 0.0F;

	// Для интерполяции на клиенте
	private float clientSwingAngle = 0.0F;
	private float clientSwingVelocity = 0.0F;
	private float prevClientSwingAngle = 0.0F;
	private float prevClientSwingVelocity = 0.0F;

	// Таймеры для управления
	private int pushDirection = 0;
	private int swingUpdateTimer = 0;

	public TireSwingEntity(EntityType<?> type, Level level) {
		super(type, level);
		this.noPhysics = false;
		this.setMaxUpStep(0.0F);
		this.blocksBuilding = true;
	}

	@Override
	protected void defineSynchedData() {
		this.entityData.define(DATA_SWING_ANGLE, 0.0F);
		this.entityData.define(DATA_SWING_VELOCITY, 0.0F);
		this.entityData.define(DATA_OCCUPIED, false);
		this.entityData.define(DATA_PASSENGER_YAW, 0.0F);
	}

	@Override
	public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
		super.onSyncedDataUpdated(key);
		if (this.level().isClientSide) {
			if (key.equals(DATA_SWING_ANGLE)) {
				this.prevClientSwingAngle = this.clientSwingAngle;
				this.clientSwingAngle = getSwingAngle();
			}
			if (key.equals(DATA_SWING_VELOCITY)) {
				this.prevClientSwingVelocity = this.clientSwingVelocity;
				this.clientSwingVelocity = getSwingVelocity();
			}
		}
	}

	@Override
	public InteractionResult interact(Player player, InteractionHand hand) {
		if (this.getPassengers().isEmpty() && !player.isSecondaryUseActive()) {
			if (!this.level().isClientSide) {
				// Запоминаем yaw игрока как начальное значение тела
				this.passengerBodyYaw = player.yBodyRot;
				this.lastPassengerBodyYaw = player.yBodyRot;
				setPassengerYaw(player.yBodyRot);

				player.setPos(this.getX(), this.getY() + this.getPassengersRidingOffset(), this.getZ());

				if (player.startRiding(this)) {
					setOccupied(true);
				}
			}
			return InteractionResult.sidedSuccess(this.level().isClientSide);
		}
		return InteractionResult.PASS;
	}

	@Override
	public boolean isPushable() {
		return false;
	}

	@Override
	public void tick() {
		super.tick();

		// Обновляем интерполированные значения для рендеринга
		if (this.level().isClientSide) {
			this.lastRenderSwingAngle = this.renderSwingAngle;
			this.renderSwingAngle = getRenderSwingAngleInternal(1.0F);

			this.lastSwingProgress = this.swingProgress;
			float targetSwing = (float) Math.sin(Math.toRadians(this.renderSwingAngle * 10)) * 0.5F;
			this.swingProgress += (targetSwing - this.swingProgress) * 0.2F;

			// Интерполируем yaw тела пассажира
			this.lastPassengerBodyYaw = this.passengerBodyYaw;
			this.passengerBodyYaw = getPassengerYaw();
		} else {
			// Серверная логика
			boolean hasPassenger = !this.getPassengers().isEmpty();
			setOccupied(hasPassenger);

// Обновляем физику качания
			updateSwingPhysics(hasPassenger);

// Обновляем yaw тела пассажира и обработку ввода
			if (hasPassenger) {
				Entity passenger = this.getPassengers().get(0);

				// Плавное вращение тела к направлению качелей
				float swingYaw = this.getYRot();
				float bodyYawDiff = swingYaw - this.passengerBodyYaw;
				while (bodyYawDiff > 180) bodyYawDiff -= 360;
				while (bodyYawDiff < -180) bodyYawDiff += 360;

				this.passengerBodyYaw += bodyYawDiff * 0.3F;
				setPassengerYaw(this.passengerBodyYaw);

				// УПРОЩЕННОЕ УПРАВЛЕНИЕ КАЧАНИЕМ
				if (passenger instanceof Player player) {
					float moveInput = player.zza; // -1 (назад) до 1 (вперёд)

					if (moveInput != 0) {
						// Толкаем качели в направлении ввода игрока
						// НЕ проверяем направление скорости - игрок толкает всегда, когда хочет!
						this.pushDirection = (int) Math.signum(moveInput);

						// Дополнительный визуальный эффект: небольшой мгновенный импульс
						// для лучшей отзывчивости (опционально)
						float currentVel = getSwingVelocity();
						float impulse = moveInput * 0.008F; // Очень небольшой импульс
						setSwingVelocity(currentVel + impulse);
					} else {
						// Игрок не нажимает клавиши - не применяем силу
						this.pushDirection = 0;
					}
				}

				// Синхронизируем каждые 2 тика для плавности
				if (swingUpdateTimer++ % 2 == 0) {
					this.entityData.set(DATA_SWING_ANGLE, getSwingAngle());
					this.entityData.set(DATA_SWING_VELOCITY, getSwingVelocity());
				}
			}
		}
	}

	private void updateSwingPhysics(boolean hasPassenger) {
		float currentAngle = getSwingAngle();
		float currentVelocity = getSwingVelocity();

		// 1. Гравитационная сила с нелинейным усилением
		float angleRad = (float) Math.toRadians(currentAngle);
		float gravityForce = (float) -Math.sin(angleRad) * SWING_GRAVITY;

		// Нелинейное усиление: на больших углах сила растёт быстрее
		float angleRatio = Math.abs(currentAngle) / MAX_SWING_ANGLE;
		float nonLinearBoost = 1.0F + angleRatio * angleRatio * 2.0F;
		gravityForce *= nonLinearBoost;

		// 2. Сила от игрока (только если есть пассажир и активен ввод)
		float playerForce = 0.0f;
		if (hasPassenger && this.pushDirection != 0) {
			// Игрок толкает эффективнее всего в нижней точке (cos максимален при angle=0°)
			float efficiency = (float) Math.cos(angleRad);
			efficiency = Math.max(0.2F, Math.abs(efficiency)); // Минимум 20% эффективности

			playerForce = this.pushDirection * PLAYER_PUSH_STRENGTH * efficiency;

			// Автоматический сброс pushDirection после применения (игрок должен удерживать клавишу)
			this.pushDirection = 0;
		}

		// 3. Суммарное ускорение (гравитация доминирует)
		float acceleration = gravityForce + playerForce;

		// 4. Обновление скорости с демпфированием
		currentVelocity += acceleration;
		currentVelocity *= SWING_DAMPING;

		// 5. Обновление угла
		currentAngle += currentVelocity;

		// 6. Ограничение угла с потерей энергии при ударе о предельный угол
		if (Math.abs(currentAngle) > MAX_SWING_ANGLE) {
			currentAngle = MAX_SWING_ANGLE * Math.signum(currentAngle);
			currentVelocity *= -0.6F; // Потеря 40% энергии при отскоке
		}

		// 7. Гарантированный возврат в нейтраль при малых колебаниях
		if (Math.abs(currentVelocity) < STOP_THRESHOLD && Math.abs(currentAngle) < 2.0F) {
			// Плавное возвращение к нулю (критически важно!)
			currentAngle *= 0.9F;
			if (Math.abs(currentAngle) < 0.5F) {
				currentAngle = 0.0F;
				currentVelocity = 0.0F;
			}
		}

		setSwingAngle(currentAngle);
		setSwingVelocity(currentVelocity);
	}

	private float calculateSpeedMultiplier(float currentSpeed) {
		if (currentSpeed <= 0.0F) {
			return MIN_VELOCITY_MULTIPLIER;
		}

		float speedRatio = Math.min(currentSpeed / MIN_VELOCITY_FOR_MAX_PUSH, 1.0F);
		return MIN_VELOCITY_MULTIPLIER + (MAX_VELOCITY_MULTIPLIER - MIN_VELOCITY_MULTIPLIER) * speedRatio;
	}

	@Override
	public void positionRider(Entity passenger, Entity.MoveFunction moveFunction) {
		if (!this.hasPassenger(passenger)) return;

		float swingAngleRad = (float) Math.toRadians(getSwingAngle());

		double forwardOffset = ROPE_LENGTH * Math.sin(swingAngleRad);
		double verticalOffset = ROPE_LENGTH * (1.0 - Math.cos(swingAngleRad));
		double yOffset = this.getPassengersRidingOffset() + passenger.getMyRidingOffset();

		// Используем yaw тела (не головы) для позиционирования
		float yawRad = (float) Math.toRadians(this.passengerBodyYaw);

		double rotatedX = -forwardOffset * Math.sin(yawRad);
		double rotatedZ = forwardOffset * Math.cos(yawRad);

		Vec3 seatPos = this.position()
				.add(rotatedX, yOffset + verticalOffset, rotatedZ);

		passenger.setPos(seatPos.x, seatPos.y, seatPos.z);

		// Разрешаем независимое вращение головы (точка 1)
		passenger.setYBodyRot(this.passengerBodyYaw);
	}

	@Override
	public double getPassengersRidingOffset() {
		return 0.1;
	}

	@Override
	protected boolean canAddPassenger(Entity passenger) {
		return this.getPassengers().isEmpty();
	}

	@Override
	public void onPassengerTurned(Entity passenger) {
		// Разрешаем пассажиру свободно вращать голову
	}

	@Override
	protected void removePassenger(Entity passenger) {
		super.removePassenger(passenger);
		if (this.getPassengers().isEmpty()) {
			setOccupied(false);
			this.pushDirection = 0;
		}
	}

	@Override
	public boolean isPickable() {
		return true;
	}

	// Методы для рендерера
	public float getRenderSwingAngle(float partialTicks) {
		if (this.level().isClientSide) {
			// Плавная интерполяция между кадрами
			return this.lastRenderSwingAngle + (this.renderSwingAngle - this.lastRenderSwingAngle) * partialTicks;
		}
		return getSwingAngle();
	}

	private float getRenderSwingAngleInternal(float partialTicks) {
		if (this.level().isClientSide) {
			// Используем интерполированные клиентские значения
			return this.prevClientSwingAngle + (this.clientSwingAngle - this.prevClientSwingAngle) * partialTicks;
		}
		return getSwingAngle();
	}

	public float getSwingProgress(float partialTicks) {
		return this.lastSwingProgress + (this.swingProgress - this.lastSwingProgress) * partialTicks;
	}

	public float getPassengerBodyYaw(float partialTicks) {
		return this.lastPassengerBodyYaw + (this.passengerBodyYaw - this.lastPassengerBodyYaw) * partialTicks;
	}

	// Метод для вычисления поворота модели (точка 3)
	public float getModelRotationAngle(float swingAngle) {
		if (!isOccupied()) return 0.0F;

		float angleRatio = Math.abs(swingAngle) / MAX_SWING_ANGLE;
		float rotationAdjust = 45 * angleRatio * Math.signum(swingAngle);
		return BASE_MODEL_ROTATION - rotationAdjust;
	}

	// Геттеры/сеттеры
	public float getSwingAngle() {
		return this.entityData.get(DATA_SWING_ANGLE);
	}

	public void setSwingAngle(float angle) {
		this.entityData.set(DATA_SWING_ANGLE, angle);
	}

	public float getSwingVelocity() {
		return this.entityData.get(DATA_SWING_VELOCITY);
	}

	public void setSwingVelocity(float velocity) {
		this.entityData.set(DATA_SWING_VELOCITY, velocity);
	}

	public boolean isOccupied() {
		return this.entityData.get(DATA_OCCUPIED);
	}

	public void setOccupied(boolean occupied) {
		this.entityData.set(DATA_OCCUPIED, occupied);
	}

	public float getPassengerYaw() {
		return this.entityData.get(DATA_PASSENGER_YAW);
	}

	public void setPassengerYaw(float yaw) {
		this.entityData.set(DATA_PASSENGER_YAW, yaw);
	}

	public float getRopeLength() {
		return ROPE_LENGTH;
	}

	public float getMaxSwingAngle() {
		return MAX_SWING_ANGLE;
	}

	@Override
	protected void readAdditionalSaveData(CompoundTag compound) {
		if (compound.contains("SwingAngle")) {
			setSwingAngle(compound.getFloat("SwingAngle"));
		}
		if (compound.contains("SwingVelocity")) {
			setSwingVelocity(compound.getFloat("SwingVelocity"));
		}
		if (compound.contains("Occupied")) {
			setOccupied(compound.getBoolean("Occupied"));
		}
		if (compound.contains("PassengerBodyYaw")) {
			this.passengerBodyYaw = compound.getFloat("PassengerBodyYaw");
			this.lastPassengerBodyYaw = this.passengerBodyYaw;
			setPassengerYaw(this.passengerBodyYaw);
		}
	}

	@Override
	protected void addAdditionalSaveData(CompoundTag compound) {
		compound.putFloat("SwingAngle", getSwingAngle());
		compound.putFloat("SwingVelocity", getSwingVelocity());
		compound.putBoolean("Occupied", isOccupied());
		compound.putFloat("PassengerBodyYaw", this.passengerBodyYaw);
	}

	@Override
	public Packet<ClientGamePacketListener> getAddEntityPacket() {
		return NetworkHooks.getEntitySpawningPacket(this);
	}
}