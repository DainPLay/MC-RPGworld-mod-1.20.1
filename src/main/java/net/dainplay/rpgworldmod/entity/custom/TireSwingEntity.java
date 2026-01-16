package net.dainplay.rpgworldmod.entity.custom;

import net.dainplay.rpgworldmod.network.ModMessages;
import net.dainplay.rpgworldmod.network.PacketTireSwingInteraction;
import net.dainplay.rpgworldmod.sounds.RPGSounds;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.entity.PartEntity;
import net.minecraftforge.network.NetworkHooks;

import java.util.Random;

public class TireSwingEntity extends Entity {
	private static final EntityDataAccessor<Float> DATA_SWING_ANGLE = SynchedEntityData.defineId(TireSwingEntity.class, EntityDataSerializers.FLOAT);
	private static final EntityDataAccessor<Float> DATA_SWING_VELOCITY = SynchedEntityData.defineId(TireSwingEntity.class, EntityDataSerializers.FLOAT);
	private static final EntityDataAccessor<Boolean> DATA_OCCUPIED = SynchedEntityData.defineId(TireSwingEntity.class, EntityDataSerializers.BOOLEAN);
	private static final EntityDataAccessor<Float> DATA_PASSENGER_YAW = SynchedEntityData.defineId(TireSwingEntity.class, EntityDataSerializers.FLOAT);
	private static final EntityDataAccessor<Float> DATA_SWING_YAW = SynchedEntityData.defineId(TireSwingEntity.class, EntityDataSerializers.FLOAT);
	private static final EntityDataAccessor<Float> DATA_TARGET_SWING_YAW = SynchedEntityData.defineId(TireSwingEntity.class, EntityDataSerializers.FLOAT);

	// Физические константы
	private static final float MAX_SWING_ANGLE = 70.0F;
	private static final float SWING_DAMPING = 0.995F;
	private static final float SWING_GRAVITY = 0.3F;
	private static final float PLAYER_PUSH_STRENGTH = 0.015F;
	private static final float STOP_THRESHOLD = 0.01F;
	private static final float ROPE_LENGTH = 5.0F;

	// Константы для поворота качелей
	private static final float BASE_ROTATION_SPEED = 1.5F;
	private static final float ROTATION_SMOOTHNESS = 0.1F;
	private static final float ROTATION_ANGLE_LIMIT = 15.0F;
	private static final float MAX_HEAD_YAW_OFFSET = 90.0F;

	// Константы для случайного поворота
	private static final float RANDOM_ROTATION_CHANCE = 0.02F;
	private static final float MAX_RANDOM_ROTATION = 5.0F;
	private static final float MIN_ANGLE_FOR_RANDOM_ROTATION = 10.0F;
	private static final float RANDOM_ROTATION_DECAY = 0.95F;

	// Константы для зависимости толчка от скорости
	private static final float MIN_VELOCITY_FOR_MAX_PUSH = 5.0F;
	private static final float MAX_VELOCITY_MULTIPLIER = 2.0F;
	private static final float MIN_VELOCITY_MULTIPLIER = 0.3F;

	private static final float COLLISION_BOUNCE_FACTOR = 0.7F; // Эластичность отскока (70%)
	private static final float MAX_BOUNCE_VELOCITY = 2.0F;     // Максимальная скорость после отскока
	private static final float COLLISION_ANGLE_REDUCTION = 0.8F; // Уменьшение угла при столкновении
	private static final float COLLISION_DAMPING_FACTOR = 0.9F;  // Дополнительное трение при столкновен

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
	private float swingYaw = 0.0F;
	private float lastSwingYaw = 0.0F;
	private float targetSwingYaw = 0.0F;

	// Переменные для плавного управления
	private float currentRotationSpeed = 0.0F;
	private float randomRotationOffset = 0.0F;
	private int randomRotationTimer = 0;

	// Для интерполяции на клиенте
	private float clientSwingAngle = 0.0F;
	private float clientSwingVelocity = 0.0F;
	private float prevClientSwingAngle = 0.0F;
	private float prevClientSwingVelocity = 0.0F;
	private float clientSwingYaw = 0.0F;
	private float prevClientSwingYaw = 0.0F;

	// Таймеры для управления
	private int pushDirection = 0;
	private int swingUpdateTimer = 0;
	private int rotationInput = 0;

	// Рандомизатор
	private final Random random = new Random();

	// Второй хитбокс для сиденья
	private final TireSwingSeatPart seatPart;

	// Флаг для отслеживания, было ли взаимодействие с сиденьем
	private boolean seatInteraction = false;

	public TireSwingEntity(EntityType<?> type, Level level) {
		super(type, level);
		this.noPhysics = false;
		this.setMaxUpStep(0.0F);
		this.blocksBuilding = true;
		this.seatPart = new TireSwingSeatPart(this);
	}

	@Override
	protected void defineSynchedData() {
		this.entityData.define(DATA_SWING_ANGLE, 0.0F);
		this.entityData.define(DATA_SWING_VELOCITY, 0.0F);
		this.entityData.define(DATA_OCCUPIED, false);
		this.entityData.define(DATA_PASSENGER_YAW, 0.0F);
		this.entityData.define(DATA_SWING_YAW, 0.0F);
		this.entityData.define(DATA_TARGET_SWING_YAW, 0.0F);
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
			if (key.equals(DATA_SWING_YAW)) {
				this.prevClientSwingYaw = this.clientSwingYaw;
				float newYaw = getSwingYaw();

				// Корректируем для плавной интерполяции
				float diff = newYaw - this.prevClientSwingYaw;
				if (diff > 180.0F) {
					newYaw -= 360.0F;
				} else if (diff < -180.0F) {
					newYaw += 360.0F;
				}

				this.clientSwingYaw = newYaw;
			}
		}
	}

	// Метод для посадки игрока (вызывается как из основного тела, так и из сиденья)
	public InteractionResult tryMountPlayer(Player player) {
		if (this.getPassengers().isEmpty() && !player.isSecondaryUseActive()) {
			if (!this.level().isClientSide) {
				// Запоминаем yaw игрока как начальное значение
				this.passengerBodyYaw = player.yBodyRot;
				this.lastPassengerBodyYaw = player.yBodyRot;
				this.swingYaw = player.yBodyRot;
				this.lastSwingYaw = player.yBodyRot;
				this.targetSwingYaw = player.yBodyRot;

				setPassengerYaw(player.yBodyRot);
				setSwingYaw(player.yBodyRot);
				setTargetSwingYaw(player.yBodyRot);

				// Позиция рассчитывается в positionRider, поэтому просто запускаем riding
				if (player.startRiding(this)) {
					setOccupied(true);
					return InteractionResult.CONSUME;
				}
			} else {
				// На клиенте просто возвращаем успех
				return InteractionResult.SUCCESS;
			}
		}
		return InteractionResult.PASS;
	}

	@Override
	public boolean isMultipartEntity() {
		return true;
	}

	@Override
	public PartEntity<?>[] getParts() {
		return new PartEntity<?>[]{seatPart};
	}

	@Override
	public boolean isPushable() {
		return false;
	}

	@Override
	public void tick() {
		super.tick();

		// Обновляем позицию хитбокса сиденья
		updateSeatPartPosition();

		// Обновляем интерполированные значения для рендеринга
		if (this.level().isClientSide) {
			this.lastRenderSwingAngle = this.renderSwingAngle;
			this.renderSwingAngle = getRenderSwingAngleInternal(1.0F);

			this.lastSwingProgress = this.swingProgress;
			float targetSwing = (float) Math.sin(Math.toRadians(this.renderSwingAngle * 10)) * 0.5F;
			this.swingProgress += (targetSwing - this.swingProgress) * 0.2F;

			// Интерполируем yaw тела пассажира и качелей
			this.lastPassengerBodyYaw = this.passengerBodyYaw;
			float newPassengerYaw = getPassengerYaw();

			float diffYaw = newPassengerYaw - this.lastPassengerBodyYaw;
			if (diffYaw > 180.0F) {
				this.passengerBodyYaw = newPassengerYaw - 360.0F;
			} else if (diffYaw < -180.0F) {
				this.passengerBodyYaw = newPassengerYaw + 360.0F;
			} else {
				this.passengerBodyYaw = newPassengerYaw;
			}

			this.lastSwingYaw = this.swingYaw;
			float newSwingYaw = getSwingYaw();
			float diff = newSwingYaw - this.lastSwingYaw;
			if (diff > 180.0F) {
				this.swingYaw = newSwingYaw - 360.0F;
			} else if (diff < -180.0F) {
				this.swingYaw = newSwingYaw + 360.0F;
			} else {
				this.swingYaw = newSwingYaw;
			}
		} else {
			// Серверная логика
			boolean hasPassenger = !this.getPassengers().isEmpty();
			setOccupied(hasPassenger);

			// Обновляем физику качания (включая проверку столкновений)
			updateSwingPhysics(hasPassenger);

			// Обновляем поворот качелей
			updateSwingRotation(hasPassenger);

			// Обновляем yaw тела пассажира и обработку ввода
			if (hasPassenger) {
				Entity passenger = this.getPassengers().get(0);

				if (passenger instanceof Player player) {
					// 1. Ограничиваем вращение головы игрока
					limitPlayerHeadRotation(player);

					// 2. Обрабатываем ввод для качания вперед/назад (только если нет столкновения)
					float moveInput = player.zza;
					if (moveInput != 0 && !checkSeatCollision(getSwingAngle())) {
						this.pushDirection = (int) Math.signum(moveInput);
						float currentVel = getSwingVelocity();
						float impulse = moveInput * 0.008F;
						setSwingVelocity(currentVel + impulse);
					} else {
						this.pushDirection = 0;
					}

					// 3. Обрабатываем ввод для поворота качелей
					float strafeInput = player.xxa;
					if (Math.abs(strafeInput) > 0.1f) {
						this.rotationInput = (int) Math.signum(strafeInput);
					} else {
						this.rotationInput = 0;
					}
				}

				// Синхронизируем каждые 2 тика для плавности
				if (swingUpdateTimer++ % 2 == 0) {
					this.entityData.set(DATA_SWING_ANGLE, getSwingAngle());
					this.entityData.set(DATA_SWING_VELOCITY, getSwingVelocity());
					this.entityData.set(DATA_SWING_YAW, getSwingYaw());
				}
			}
		}
	}

	private void updateSeatPartPosition() {
		if (seatPart == null) return;

		// Вычисляем позицию сиденья на основе текущего угла качания
		float swingAngleRad = (float) Math.toRadians(getSwingAngle());
		float swingYawRad = (float) Math.toRadians(getSwingYaw());

		double forwardOffset = ROPE_LENGTH * Math.sin(swingAngleRad);
		double verticalOffset = ROPE_LENGTH * (1.0 - Math.cos(swingAngleRad));
		double yOffset = this.getPassengersRidingOffset();

		// Поворачиваем смещение согласно swingYaw
		double rotatedX = -forwardOffset * Math.sin(swingYawRad);
		double rotatedZ = forwardOffset * Math.cos(swingYawRad);

		Vec3 seatPos = this.position()
				.add(rotatedX, yOffset + verticalOffset, rotatedZ);

		seatPart.setPos(seatPos.x, seatPos.y, seatPos.z);

		// Обновляем bounding box сиденья (слегка уменьшаем для лучшего геймплея)
		float width = 0.8f; // Было 0.875f
		float height = 0.8f; // Было 0.875f
		seatPart.setBoundingBox(new AABB(
				seatPos.x - width / 2, seatPos.y - 0.05, seatPos.z - width / 2,
				seatPos.x + width / 2, seatPos.y + height, seatPos.z + width / 2
		));
	}

	private void limitPlayerHeadRotation(Player player) {
		float swingYaw = getSwingYaw();
		float headYaw = player.getYHeadRot();

		// Нормализуем углы в диапазон -180..180
		float normalizedSwingYaw = swingYaw % 360;
		if (normalizedSwingYaw > 180) normalizedSwingYaw -= 360;
		if (normalizedSwingYaw < -180) normalizedSwingYaw += 360;

		float normalizedHeadYaw = headYaw % 360;
		if (normalizedHeadYaw > 180) normalizedHeadYaw -= 360;
		if (normalizedHeadYaw < -180) normalizedHeadYaw += 360;

		// Вычисляем разницу
		float diff = normalizedHeadYaw - normalizedSwingYaw;

		// Корректируем разницу в диапазон -180..180
		if (diff > 180) diff -= 360;
		if (diff < -180) diff += 360;

		// Ограничиваем отклонение головы
		if (Math.abs(diff) > MAX_HEAD_YAW_OFFSET) {
			float limitedHeadYaw = normalizedSwingYaw + (Math.signum(diff) * MAX_HEAD_YAW_OFFSET);

			// Возвращаем в исходный диапазон
			if (limitedHeadYaw > 180) limitedHeadYaw -= 360;
			if (limitedHeadYaw < -180) limitedHeadYaw += 360;

			player.setYHeadRot(limitedHeadYaw);
			player.yRotO = limitedHeadYaw;
			player.setYRot(limitedHeadYaw);
		}

		// Тело игрока следует за направлением качелей
		this.passengerBodyYaw = swingYaw;
		setPassengerYaw(this.passengerBodyYaw);
	}

	private float normalizeAngle(float angle) {
		angle %= 360.0F;
		if (angle > 180.0F) {
			angle -= 360.0F;
		} else if (angle < -180.0F) {
			angle += 360.0F;
		}
		return angle;
	}

	private float lerpAngle(float partialTicks, float start, float end) {
		// Нормализуем углы
		start = normalizeAngle(start);
		end = normalizeAngle(end);

		// Вычисляем кратчайший путь
		float diff = end - start;
		if (diff > 180.0F) {
			diff -= 360.0F;
		} else if (diff < -180.0F) {
			diff += 360.0F;
		}

		return start + diff * partialTicks;
	}

	private void updateSwingRotation(boolean hasPassenger) {
		if (!hasPassenger) {
			return;
		}

		float currentAngle = getSwingAngle();

		// Проверяем, что качели в диапазоне для поворота
		if (Math.abs(currentAngle) > ROTATION_ANGLE_LIMIT) {
			// Сбрасываем ввод, если вышли за пределы
			this.rotationInput = 0;
			this.currentRotationSpeed = 0.0F;
			return;
		}

		// Вычисляем множитель влияния в зависимости от угла качания
		float angleFactor = 1.0F - (Math.abs(currentAngle) / ROTATION_ANGLE_LIMIT);

		// Целевая скорость поворота на основе ввода игрока
		float targetRotationSpeed = 0.0F;

		if (this.rotationInput != 0) {
			// Игрок нажимает клавиши - учитываем его ввод
			targetRotationSpeed = -this.rotationInput * BASE_ROTATION_SPEED * angleFactor;

			// Плавное изменение текущей скорости
			this.currentRotationSpeed += (targetRotationSpeed - this.currentRotationSpeed) * ROTATION_SMOOTHNESS;

			// Сбрасываем случайное смещение при активном вводе
			this.randomRotationOffset *= 0.5F;
		} else {
			// Игрок не нажимает клавиши - плавно замедляемся
			this.currentRotationSpeed *= 0.8F;

			// Если скорость очень мала, обнуляем её
			if (Math.abs(this.currentRotationSpeed) < 0.01F) {
				this.currentRotationSpeed = 0.0F;
			}

			// Добавляем случайные повороты при малом угле качания
			if (Math.abs(currentAngle) < MIN_ANGLE_FOR_RANDOM_ROTATION) {
				// Увеличиваем таймер
				randomRotationTimer++;

				// Случайный поворот с определенной вероятностью
				if (randomRotationTimer > 10 && this.random.nextFloat() < RANDOM_ROTATION_CHANCE) {
					// Генерируем случайное смещение
					float randomRotation = (this.random.nextFloat() - 0.5F) * 2.0F * MAX_RANDOM_ROTATION;
					this.randomRotationOffset += randomRotation;
					randomRotationTimer = 0;
				}

				// Применяем случайное смещение
				if (Math.abs(this.randomRotationOffset) > 0.01F) {
					this.currentRotationSpeed += this.randomRotationOffset * 0.05F;
					this.randomRotationOffset *= RANDOM_ROTATION_DECAY;
				}
			}
		}

		// Ограничиваем максимальную скорость поворота
		float maxSpeed = BASE_ROTATION_SPEED * angleFactor;
		this.currentRotationSpeed = Mth.clamp(this.currentRotationSpeed, -maxSpeed, maxSpeed);

		// Обновляем целевой yaw
		this.targetSwingYaw += this.currentRotationSpeed;

		// Плавное приближение текущего yaw к целевому
		float currentSwingYaw = getSwingYaw();
		float targetDiff = this.targetSwingYaw - currentSwingYaw;

// Корректируем разницу для кратчайшего пути
		if (targetDiff > 180.0F) {
			targetDiff -= 360.0F;
		} else if (targetDiff < -180.0F) {
			targetDiff += 360.0F;
		}
		float newSwingYaw = currentSwingYaw + targetDiff * 0.2F;
		newSwingYaw = normalizeAngle(newSwingYaw);

		setSwingYaw(newSwingYaw);

		// Нормализуем целевой yaw
		while (this.targetSwingYaw > 180.0F) this.targetSwingYaw -= 360.0F;
		while (this.targetSwingYaw < -180.0F) this.targetSwingYaw += 360.0F;
		setTargetSwingYaw(this.targetSwingYaw);
	}

	private void updateSwingPhysics(boolean hasPassenger) {
		float currentAngle = getSwingAngle();
		float currentVelocity = getSwingVelocity();

		// 1. Проверяем столкновение ДО обновления физики
		boolean collisionDetected = checkSeatCollision(currentAngle);

		// 2. Гравитационная сила с нелинейным усилением
		float angleRad = (float) Math.toRadians(currentAngle);
		float gravityForce = (float) -Math.sin(angleRad) * SWING_GRAVITY;

		// Нелинейное усиление: на больших углах сила растёт быстрее
		float angleRatio = Math.abs(currentAngle) / MAX_SWING_ANGLE;
		float nonLinearBoost = 1.0F + angleRatio * angleRatio * 2.0F;
		gravityForce *= nonLinearBoost;

		// 3. Сила от игрока (только если есть пассажир и активен ввод)
		float playerForce = 0.0f;
		if (hasPassenger && this.pushDirection != 0 && !collisionDetected) {
			// Игрок толкает эффективнее всего в нижней точке (cos максимален при angle=0°)
			float efficiency = (float) Math.cos(angleRad);
			efficiency = Math.max(0.2F, Math.abs(efficiency)); // Минимум 20% эффективности

			playerForce = this.pushDirection * PLAYER_PUSH_STRENGTH * efficiency;

			// Автоматический сброс pushDirection после применения
			this.pushDirection = 0;
		}

		// 4. Если обнаружено столкновение - применяем отскок
		if (collisionDetected) {
			// Вычисляем вектор отскока на основе текущей скорости и угла
			float collisionVelocity = currentVelocity;

			// Эластичный отскок: сохраняем 70% энергии, меняем направление
			float bounceFactor = 1F; // 70% энергии сохраняется
			float newVelocity = -collisionVelocity * bounceFactor;

			// Ограничиваем максимальную скорость после отскока
			float maxBounceVelocity = 2.0F;
			if (Math.abs(newVelocity) > maxBounceVelocity) {
				newVelocity = Math.signum(newVelocity) * maxBounceVelocity;
			}

			// Применяем отскок
			currentVelocity = newVelocity;

			// Немного уменьшаем угол, чтобы качели не застревали в блоке
			float angleReduction = 0.8F; // Уменьшаем угол на 20%
			currentAngle *= angleReduction;

			// Сбрасываем силу игрока при столкновении
			playerForce = 0.0f;
			this.pushDirection = 0;

			level().playSound(null, this.seatPart.blockPosition(), RPGSounds.TIRE_BOUNCE.get(), SoundSource.BLOCKS, 1.0F, (level().random.nextFloat() - level().random.nextFloat()) * 0.2F + 1.0F);
		}

		// 5. Суммарное ускорение (гравитация доминирует)
		float acceleration = gravityForce + playerForce;

		// 6. Обновление скорости с демпфированием (увеличиваем трение при столкновении)
		float currentDamping = collisionDetected ? SWING_DAMPING * 0.9F : SWING_DAMPING;
		currentVelocity += acceleration;
		currentVelocity *= currentDamping;

		// 7. Обновление угла
		currentAngle += currentVelocity;

		// 8. Ограничение угла с потерей энергии при ударе о предельный угол
		if (Math.abs(currentAngle) > MAX_SWING_ANGLE) {
			currentAngle = MAX_SWING_ANGLE * Math.signum(currentAngle);
			currentVelocity *= -0.6F; // Потеря 40% энергии при отскоке
		}

		// 9. Гарантированный возврат в нейтраль при малых колебаниях
		if (Math.abs(currentVelocity) < STOP_THRESHOLD && Math.abs(currentAngle) < 2.0F) {
			// Плавное возвращение к нулю
			currentAngle *= 0.9F;
			if (Math.abs(currentAngle) < 0.5F) {
				currentAngle = 0.0F;
				currentVelocity = 0.0F;
			}
		}

		setSwingAngle(currentAngle);
		setSwingVelocity(currentVelocity);
	}

	private boolean checkSeatCollision(float swingAngle) {
		if (seatPart == null) return false;

		// Получаем текущую позицию сиденья (копируем логику из updateSeatPartPosition)
		float swingAngleRad = (float) Math.toRadians(swingAngle);
		float swingYawRad = (float) Math.toRadians(getSwingYaw());

		double forwardOffset = ROPE_LENGTH * Math.sin(swingAngleRad);
		double verticalOffset = ROPE_LENGTH * (1.0 - Math.cos(swingAngleRad));
		double yOffset = this.getPassengersRidingOffset();

		// Поворачиваем смещение согласно swingYaw
		double rotatedX = -forwardOffset * Math.sin(swingYawRad);
		double rotatedZ = forwardOffset * Math.cos(swingYawRad);

		Vec3 seatPos = this.position()
				.add(rotatedX, yOffset + verticalOffset, rotatedZ);

		// Создаём AABB для сиденья (немного меньше для предотвращения ложных срабатываний)
		float collisionWidth = 0.7f;
		float collisionHeight = 0.7f;
		AABB seatCollisionBox = new AABB(
				seatPos.x - collisionWidth / 2,
				seatPos.y - 0.05,
				seatPos.z - collisionWidth / 2,
				seatPos.x + collisionWidth / 2,
				seatPos.y + collisionHeight,
				seatPos.z + collisionWidth / 2
		);

		// Проверяем столкновение с твёрдыми блоками
		return checkAABBCollisionWithSolidBlocks(seatCollisionBox);
	}

	private boolean checkAABBCollisionWithSolidBlocks(AABB aabb) {
		// Получаем все блоки, которые пересекаются с AABB
		int minX = net.minecraft.util.Mth.floor(aabb.minX);
		int minY = net.minecraft.util.Mth.floor(aabb.minY);
		int minZ = net.minecraft.util.Mth.floor(aabb.minZ);
		int maxX = net.minecraft.util.Mth.floor(aabb.maxX);
		int maxY = net.minecraft.util.Mth.floor(aabb.maxY);
		int maxZ = net.minecraft.util.Mth.floor(aabb.maxZ);

		for (int x = minX; x <= maxX; x++) {
			for (int y = minY; y <= maxY; y++) {
				for (int z = minZ; z <= maxZ; z++) {
					net.minecraft.core.BlockPos pos = new net.minecraft.core.BlockPos(x, y, z);
					net.minecraft.world.level.block.state.BlockState state = this.level().getBlockState(pos);

					// Проверяем, является ли блок твёрдым (столкновение)
					if (!state.isAir() && state.isSolid()) {
						// Получаем VoxelShape блока и проверяем пересечение
						net.minecraft.world.phys.shapes.VoxelShape shape = state.getCollisionShape(this.level(), pos);
						if (!shape.isEmpty()) {
							net.minecraft.world.phys.shapes.VoxelShape offsetShape = shape.move(pos.getX(), pos.getY(), pos.getZ());
							if (offsetShape.toAabbs().stream().anyMatch(blockAABB -> blockAABB.intersects(aabb))) {
								return true;
							}
						}
					}
				}
			}
		}

		return false;
	}

	@Override
	public void positionRider(Entity passenger, Entity.MoveFunction moveFunction) {
		if (!this.hasPassenger(passenger)) return;

		float swingAngleRad = (float) Math.toRadians(getSwingAngle());
		float swingYawRad = (float) Math.toRadians(getSwingYaw());

		double forwardOffset = ROPE_LENGTH * Math.sin(swingAngleRad);
		double verticalOffset = ROPE_LENGTH * (1.0 - Math.cos(swingAngleRad));
		double yOffset = this.getPassengersRidingOffset() + passenger.getMyRidingOffset();

		// Используем swingYaw для позиционирования
		double rotatedX = -forwardOffset * Math.sin(swingYawRad);
		double rotatedZ = forwardOffset * Math.cos(swingYawRad);

		Vec3 seatPos = this.position()
				.add(rotatedX, yOffset + verticalOffset, rotatedZ);

		passenger.setPos(seatPos.x, seatPos.y, seatPos.z);

		// Устанавливаем тело пассажира в направлении качелей
		passenger.setYBodyRot(getSwingYaw());
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
		// Ограничиваем вращение пассажира в методе tick()
	}

	@Override
	protected void removePassenger(Entity passenger) {
		super.removePassenger(passenger);
		if (this.getPassengers().isEmpty()) {
			setOccupied(false);
			this.pushDirection = 0;
			this.rotationInput = 0;
			this.currentRotationSpeed = 0.0F;
			this.randomRotationOffset = 0.0F;
			this.randomRotationTimer = 0;
		}
	}

	// Методы для рендерера
	public float getRenderSwingAngle(float partialTicks) {
		if (this.level().isClientSide) {
			return this.lastRenderSwingAngle + (this.renderSwingAngle - this.lastRenderSwingAngle) * partialTicks;
		}
		return getSwingAngle();
	}

	private float getRenderSwingAngleInternal(float partialTicks) {
		if (this.level().isClientSide) {
			return this.prevClientSwingAngle + (this.clientSwingAngle - this.prevClientSwingAngle) * partialTicks;
		}
		return getSwingAngle();
	}

	public float getRenderSwingYaw(float partialTicks) {
		if (this.level().isClientSide) {
			// Используем lerpAngle для плавной интерполяции
			return lerpAngle(partialTicks, this.lastSwingYaw, this.swingYaw);
		}
		return getSwingYaw();
	}

	public float getSwingProgress(float partialTicks) {
		return this.lastSwingProgress + (this.swingProgress - this.lastSwingProgress) * partialTicks;
	}

	public float getPassengerBodyYaw(float partialTicks) {
		if (this.level().isClientSide) {
			// Используем lerpAngle для плавной интерполяции
			return lerpAngle(partialTicks, this.lastPassengerBodyYaw, this.passengerBodyYaw);
		}
		return getPassengerYaw();
	}

	// Метод для вычисления поворота модели
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
		yaw = normalizeAngle(yaw);
		this.entityData.set(DATA_PASSENGER_YAW, yaw);
	}

	public float getSwingYaw() {
		return this.entityData.get(DATA_SWING_YAW);
	}

	public void setSwingYaw(float yaw) {
		yaw = normalizeAngle(yaw);
		this.entityData.set(DATA_SWING_YAW, yaw);
	}

	public float getTargetSwingYaw() {
		return this.entityData.get(DATA_TARGET_SWING_YAW);
	}

	public void setTargetSwingYaw(float yaw) {
		yaw = normalizeAngle(yaw);
		this.entityData.set(DATA_TARGET_SWING_YAW, yaw);
	}

	public float getRopeLength() {
		return ROPE_LENGTH;
	}

	public float getMaxSwingAngle() {
		return MAX_SWING_ANGLE;
	}

	public float getRotationAngleLimit() {
		return ROTATION_ANGLE_LIMIT;
	}

	public float getMaxHeadYawOffset() {
		return MAX_HEAD_YAW_OFFSET;
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
		if (compound.contains("SwingYaw")) {
			this.swingYaw = compound.getFloat("SwingYaw");
			this.lastSwingYaw = this.swingYaw;
			setSwingYaw(this.swingYaw);
		}
		if (compound.contains("TargetSwingYaw")) {
			this.targetSwingYaw = compound.getFloat("TargetSwingYaw");
			setTargetSwingYaw(this.targetSwingYaw);
		}
	}

	@Override
	protected void addAdditionalSaveData(CompoundTag compound) {
		compound.putFloat("SwingAngle", getSwingAngle());
		compound.putFloat("SwingVelocity", getSwingVelocity());
		compound.putBoolean("Occupied", isOccupied());
		compound.putFloat("PassengerBodyYaw", this.passengerBodyYaw);
		compound.putFloat("SwingYaw", getSwingYaw());
		compound.putFloat("TargetSwingYaw", this.targetSwingYaw);
	}

	@Override
	public Packet<ClientGamePacketListener> getAddEntityPacket() {
		return NetworkHooks.getEntitySpawningPacket(this);
	}

	// Метод для обработки взаимодействия с сиденьем
	public void setSeatInteraction(boolean seatInteraction) {
		this.seatInteraction = seatInteraction;
	}

	// Внутренний класс для хитбокса сиденья
	public class TireSwingSeatPart extends PartEntity<TireSwingEntity> {
		public TireSwingSeatPart(TireSwingEntity parent) {
			super(parent);
		}

		@Override
		protected void defineSynchedData() {
			// Не требуется синхронизация данных для части
		}

		@Override
		protected void readAdditionalSaveData(CompoundTag pCompound) {
			// Части сущности не сохраняются отдельно
		}

		@Override
		protected void addAdditionalSaveData(CompoundTag pCompound) {
			// Части сущности не сохраняются отдельно
		}

		@Override
		public boolean isPickable() {
			return true;
		}

		@Override
		public boolean is(Entity entity) {
			return this == entity || this.getParent() == entity;
		}

		@Override
		public boolean hurt(net.minecraft.world.damagesource.DamageSource source, float amount) {
			// Можно добавить логику повреждения качелей
			return false;
		}

		@Override
		public void setPos(double x, double y, double z) {
			super.setPos(x, y, z);
			// Сбрасываем bounding box при изменении позиции
			this.setBoundingBox(this.getBoundingBox());
		}

		@Override
		public boolean isPushable() {
			return false;
		}

		@Override
		public InteractionResult interact(Player player, InteractionHand hand) {
			if (this.level().isClientSide) {
				// Отправляем пакет на сервер при клике на сиденье
				ModMessages.sendToServer(new PacketTireSwingInteraction(this.getParent().getId()));
				return InteractionResult.SUCCESS;
			}
			return InteractionResult.PASS;
		}

		@Override
		public boolean isNoGravity() {
			return true; // Часть не должна падать
		}

		@Override
		public boolean isAttackable() {
			return false; // Часть нельзя атаковать напрямую
		}

		@Override
		public boolean isAlive() {
			return this.getParent() != null && this.getParent().isAlive();
		}

		@Override
		public void remove(net.minecraft.world.entity.Entity.RemovalReason reason) {
			// Не позволяем удалять часть отдельно от родителя
			if (this.getParent() != null && !this.getParent().isRemoved()) {
				return;
			}
			super.remove(reason);
		}

		public boolean isVisible() {
			return true;
		}
	}
}