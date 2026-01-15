package net.dainplay.rpgworldmod.entity.custom;// SwingEntity.java

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
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;

public class TireSwingEntity extends Entity {
	private static final EntityDataAccessor<Integer> DATA_SWING_ANGLE = SynchedEntityData.defineId(TireSwingEntity.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Boolean> DATA_OCCUPIED = SynchedEntityData.defineId(TireSwingEntity.class, EntityDataSerializers.BOOLEAN);

	// Конструкторы
	public TireSwingEntity(EntityType<?> type, Level level) {
		super(type, level);
		this.noPhysics = false; // Должно быть false для взаимодействия
		this.setMaxUpStep(0.0F);
		this.blocksBuilding = true; // Важно для взаимодействия
	}

	@Override
	protected void defineSynchedData() {
		this.entityData.define(DATA_SWING_ANGLE, 0);
		this.entityData.define(DATA_OCCUPIED, false);
	}

	// Взаимодействие при клике
	@Override
	public InteractionResult interact(Player player, InteractionHand hand) {
		if ((!this.isOccupied() || !hasPassenger(this)) && !player.isSecondaryUseActive()) {
			if (!this.level().isClientSide) {
				// Проверяем, что пассажиром может стать только игрок
				if (player.startRiding(this)) {
					setOccupied(true);
				}
			}
			return InteractionResult.sidedSuccess(this.level().isClientSide);
		}
		return InteractionResult.PASS;
	}

	@Override
	public boolean canBeCollidedWith() {
		return true; // Очень важно! Позволяет сталкиваться с сущностью
	}

	@Override
	public boolean isPushable() {
		return false; // Можно толкать или нет
	}

	// Логика каждый тик
	@Override
	public void tick() {
		super.tick();

		// Проверяем, не сошел ли пассажир
		if (!this.level().isClientSide) {
			if (this.isOccupied() && !hasPassenger(this)) {
				setOccupied(false);
			}

			// Качание, если есть пассажир
			if (hasPassenger(this)) {
				int currentAngle = getSwingAngle();
				int newAngle = currentAngle + 4;
				if (newAngle >= 360) newAngle = newAngle - 360;
				setSwingAngle(newAngle);
			}

			// Предотвращаем падение
			if (!this.onGround() && this.getDeltaMovement().y < 0) {
				this.setDeltaMovement(this.getDeltaMovement().x, 0, this.getDeltaMovement().z);
			}
		}
	}

	// Физика пассажира
	@Override
	public void positionRider(Entity passenger, Entity.MoveFunction moveFunction) {
		if (!hasPassenger(this)) return;

		// Центрируем пассажира
		Vec3 seatPos = this.position().add(0, 0.5, 0);

		// Поворачиваем пассажира вместе с качелями
		float swingRotation = (float) Math.sin(Math.toRadians(getSwingAngle())) * 30.0F;
		passenger.setYRot(this.getYRot() + swingRotation);
		passenger.yRotO = this.getYRot() + swingRotation;

		// Двигаем пассажира
		moveFunction.accept(passenger, seatPos.x, seatPos.y, seatPos.z);
	}

	@Override
	public double getPassengersRidingOffset() {
		return 0.5; // Высота посадки
	}

	// Метод для правильной посадки пассажира
	@Override
	protected boolean canAddPassenger(Entity passenger) {
		return this.getPassengers().isEmpty(); // Только один пассажир
	}

	// Метод при сходе пассажира
	@Override
	protected void removePassenger(Entity passenger) {
		super.removePassenger(passenger);
		setOccupied(false);

		// Сбрасываем угол качания
		if (!hasPassenger(this)) {
			setSwingAngle(0);
		}
	}

	// Методы для возможности взаимодействия
	@Override
	public boolean isPickable() {
		return true; // Очень важно! Без этого клик не сработает
	}

	// Геттеры/сеттеры
	public int getSwingAngle() {
		return this.entityData.get(DATA_SWING_ANGLE);
	}

	public void setSwingAngle(int angle) {
		this.entityData.set(DATA_SWING_ANGLE, angle);
	}

	public boolean isOccupied() {
		return this.entityData.get(DATA_OCCUPIED);
	}

	public void setOccupied(boolean occupied) {
		this.entityData.set(DATA_OCCUPIED, occupied);
	}

	@Override
	protected void readAdditionalSaveData(CompoundTag compound) {
		if (compound.contains("SwingAngle")) {
			setSwingAngle(compound.getInt("SwingAngle"));
		}
		if (compound.contains("Occupied")) {
			setOccupied(compound.getBoolean("Occupied"));
		}
	}

	@Override
	protected void addAdditionalSaveData(CompoundTag compound) {
		compound.putInt("SwingAngle", getSwingAngle());
		compound.putBoolean("Occupied", isOccupied());
	}

	@Override
	public Packet<ClientGamePacketListener> getAddEntityPacket() {
		return NetworkHooks.getEntitySpawningPacket(this);
	}
}