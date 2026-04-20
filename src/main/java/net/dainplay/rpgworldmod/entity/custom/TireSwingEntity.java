package net.dainplay.rpgworldmod.entity.custom;

import net.dainplay.rpgworldmod.block.ModBlocks;
import net.dainplay.rpgworldmod.effect.ModEffects;
import net.dainplay.rpgworldmod.network.ModMessages;
import net.dainplay.rpgworldmod.network.PacketTireSwingInteraction;
import net.dainplay.rpgworldmod.network.SwingPlayerPacket;
import net.dainplay.rpgworldmod.sounds.RPGSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.decoration.LeashFenceKnotEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.entity.PartEntity;
import net.minecraftforge.network.NetworkHooks;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

public class TireSwingEntity extends Entity {
	private static final EntityDataAccessor<Float> DATA_SWING_ANGLE = SynchedEntityData.defineId(TireSwingEntity.class, EntityDataSerializers.FLOAT);
	private static final EntityDataAccessor<Float> DATA_SWING_VELOCITY = SynchedEntityData.defineId(TireSwingEntity.class, EntityDataSerializers.FLOAT);
	private static final EntityDataAccessor<Boolean> DATA_OCCUPIED = SynchedEntityData.defineId(TireSwingEntity.class, EntityDataSerializers.BOOLEAN);
	private static final EntityDataAccessor<Float> DATA_PASSENGER_YAW = SynchedEntityData.defineId(TireSwingEntity.class, EntityDataSerializers.FLOAT);
	private static final EntityDataAccessor<Float> DATA_SWING_YAW = SynchedEntityData.defineId(TireSwingEntity.class, EntityDataSerializers.FLOAT);
	private static final EntityDataAccessor<Float> DATA_TARGET_SWING_YAW = SynchedEntityData.defineId(TireSwingEntity.class, EntityDataSerializers.FLOAT);
	private static final EntityDataAccessor<Integer> DATA_LEASH_HOLDER_ID = SynchedEntityData.defineId(TireSwingEntity.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Byte> DATA_LEASH_TYPE = SynchedEntityData.defineId(TireSwingEntity.class, EntityDataSerializers.BYTE);
	private static final EntityDataAccessor<Optional<BlockPos>> DATA_FENCE_POS = SynchedEntityData.defineId(TireSwingEntity.class, EntityDataSerializers.OPTIONAL_BLOCK_POS);
	private static final EntityDataAccessor<Float> DATA_ROPE_LENGTH = SynchedEntityData.defineId(TireSwingEntity.class, EntityDataSerializers.FLOAT);


	private static final float MAX_SWING_ANGLE = 90.0F;
	private static final float SWING_DAMPING = 0.995F;
	private static final float SWING_GRAVITY = 0.3F;
	private static final float PLAYER_PUSH_STRENGTH = 0.015F;
	private static final float STOP_THRESHOLD = 0.01F;
	private static final float COLLISION_LOOKAHEAD_FACTOR = 1.2F;
	private static final float MIN_VELOCITY_FOR_SOUND = 0.1F;


	public static final byte LEASH_TYPE_NONE = 0;
	public static final byte LEASH_TYPE_PLAYER = 1;
	public static final byte LEASH_TYPE_FENCE_KNOT = 2;


	private static final float MAX_LEASH_DISTANCE = 10.0F;
	private static final int MAX_FENCE_HEIGHT = 7;


	private static final float BASE_ROTATION_SPEED = 1.5F;
	private static final float ROTATION_SMOOTHNESS = 0.1F;
	private static final float ROTATION_ANGLE_LIMIT = 15.0F;
	private static final float MAX_HEAD_YAW_OFFSET = 90.0F;


	private static final float RANDOM_ROTATION_CHANCE = 0.02F;
	private static final float MAX_RANDOM_ROTATION = 5.0F;
	private static final float MIN_ANGLE_FOR_RANDOM_ROTATION = 10.0F;
	private static final float RANDOM_ROTATION_DECAY = 0.95F;


	private static final float BASE_MODEL_ROTATION = 45.0F;
	private static final float MAX_MODEL_ROTATION_ADD = 15.0F;


	private static final float SWOOSH_ANGLE_THRESHOLD = 45.0F;
	private static final float CRACK_VELOCITY_THRESHOLD = 1.5F;
	private static final float MAX_SWOOSH_SPEED = 5.0F;
	private static final float CRACK_ANGLE_DEADZONE = 2.0F;


	private static final int HAPPINESS_DURATION = 200;
	private static final int MAX_HAPPINESS_DURATION = 3600;
	private static final float HAPPINESS_VELOCITY_THRESHOLD = 1.0F;
	private static final int HAPPINESS_COOLDOWN = 20;
	private int happinessCooldown = 0;


	private boolean hasCrossedZeroRecently = false;
	private boolean wasAboveSwooshAngle = false;
	private int zeroCrossCooldown = 0;
	private int swooshCooldown = 0;


	private float swingProgress = 0.0F;
	private float lastSwingProgress = 0.0F;
	private float renderSwingAngle = 0.0F;
	private float lastRenderSwingAngle = 0.0F;
	private float passengerBodyYaw = 0.0F;
	private float lastPassengerBodyYaw = 0.0F;
	private float swingYaw = 0.0F;
	private float lastSwingYaw = 0.0F;
	private float targetSwingYaw = 0.0F;


	private float currentRotationSpeed = 0.0F;
	private float randomRotationOffset = 0.0F;
	private int randomRotationTimer = 0;


	private float clientSwingAngle = 0.0F;
	private float clientSwingVelocity = 0.0F;
	private float prevClientSwingAngle = 0.0F;
	private float prevClientSwingVelocity = 0.0F;
	private float clientSwingYaw = 0.0F;
	private float prevClientSwingYaw = 0.0F;


	private int pushDirection = 0;
	private int swingUpdateTimer = 0;
	private int rotationInput = 0;
	private int ticksSeatInsideBlock = 0;


	private final Random random = new Random();


	private final TireSwingSeatPart seatPart;


	private boolean seatInteraction = false;


	private float currentRopeLength = 5.0F;
	private BlockPos fencePos;
	private boolean isLeashed = false;

	public TireSwingEntity(EntityType<?> type, Level level) {
		super(type, level);
		this.noPhysics = false;
		this.setMaxUpStep(0.0F);
		this.blocksBuilding = false;
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


		this.entityData.define(DATA_LEASH_HOLDER_ID, -1);
		this.entityData.define(DATA_LEASH_TYPE, (byte) 0);
		this.entityData.define(DATA_FENCE_POS, Optional.empty());
		this.entityData.define(DATA_ROPE_LENGTH, 5.0F);
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


	public void setLeashedTo(@Nullable Entity entity, boolean broadcast) {
		if (entity != null) {
			this.isLeashed = true;
			this.entityData.set(DATA_LEASH_HOLDER_ID, entity.getId());
			if (entity instanceof LeashFenceKnotEntity knot) {
				this.entityData.set(DATA_LEASH_TYPE, LEASH_TYPE_FENCE_KNOT);
				this.entityData.set(DATA_FENCE_POS, Optional.of(knot.getPos()));
			} else {
				this.entityData.set(DATA_LEASH_TYPE, LEASH_TYPE_PLAYER);
				this.entityData.set(DATA_FENCE_POS, Optional.empty());
			}

			if (broadcast && !this.level().isClientSide) {
				this.level().broadcastEntityEvent(this, (byte) 7);
			}
		} else {
			this.entityData.set(DATA_LEASH_HOLDER_ID, null);
		}
	}

	public boolean canBeLeashed(Player player) {
		return !this.isLeashed();
	}

	public boolean isLeashed() {
		return this.isLeashed;
	}

	@Nullable
	public Entity getLeashHolder() {
		Entity leashholder = null;

		int holderId = this.entityData.get(DATA_LEASH_HOLDER_ID);
		if (holderId != -1) {
			leashholder = this.level().getEntity(holderId);
		}
		return leashholder;
	}

	public byte getLeashType() {
		return this.entityData.get(DATA_LEASH_TYPE);
	}

	@Nullable
	public BlockPos getFencePos() {
		return this.entityData.get(DATA_FENCE_POS).orElse(null);
	}

	public ItemStack getPickResult() {
		return new ItemStack(ModBlocks.TIRE.get());
	}


	public boolean leashToFence(BlockPos fencePos, Player player, boolean newSwing) {
		if (canLeashToFence(fencePos)) {
			LeashFenceKnotEntity knot = LeashFenceKnotEntity.getOrCreateKnot(this.level(), fencePos);
			knot.playPlacementSound();
			return leashToExistingKnot(knot, player, newSwing);
		}
		return false;
	}


	private boolean canLeashToFence(BlockPos fencePos) {
		BlockPos currentPos = this.blockPosition();
		if (fencePos.getX() != currentPos.getX() || fencePos.getZ() != currentPos.getZ()) {
			return false;
		}


		int heightDiff = fencePos.getY() - currentPos.getY();
		if (heightDiff <= 1 || heightDiff > MAX_FENCE_HEIGHT) {
			return false;
		}


		for (int y = currentPos.getY() + 1; y < fencePos.getY(); y++) {
			BlockPos checkPos = new BlockPos(currentPos.getX(), y, currentPos.getZ());
			BlockState state = this.level().getBlockState(checkPos);
			if (!state.isAir() && !state.getCollisionShape(this.level(), checkPos).isEmpty()) {
				return false;
			}
		}


		BlockState fenceState = this.level().getBlockState(fencePos);
		return fenceState.getBlock() instanceof FenceBlock;
	}


	public void dropLeash(boolean broadcast, boolean dropItem) {
		Entity oldHolder = getLeashHolder();
		byte leashType = getLeashType();

		this.isLeashed = false;
		this.entityData.set(DATA_LEASH_HOLDER_ID, -1);
		this.entityData.set(DATA_LEASH_TYPE, LEASH_TYPE_NONE);
		this.entityData.set(DATA_FENCE_POS, Optional.empty());
		this.fencePos = null;


		if (oldHolder instanceof LeashFenceKnotEntity knot) {
			boolean hasOtherEntities = false;


			List<Mob> mobs = this.level().getEntitiesOfClass(Mob.class,
					new AABB(knot.getX() - 7, knot.getY() - 7, knot.getZ() - 7,
							knot.getX() + 7, knot.getY() + 7, knot.getZ() + 7));
			for (Mob mob : mobs) {
				if (mob.isLeashed() && mob.getLeashHolder() == knot) {
					hasOtherEntities = true;
					break;
				}
			}


			if (!hasOtherEntities) {
				List<TireSwingEntity> swings = this.level().getEntitiesOfClass(TireSwingEntity.class,
						new AABB(knot.getX() - 7, knot.getY() - 7, knot.getZ() - 7,
								knot.getX() + 7, knot.getY() + 7, knot.getZ() + 7));
				for (TireSwingEntity swing : swings) {
					if (swing.isLeashed() && swing.getLeashHolder() == knot && swing != this) {
						hasOtherEntities = true;
						break;
					}
				}
			}


			if (!hasOtherEntities && !this.level().isClientSide) {
				knot.discard();
			}
		}


		if (dropItem && !this.level().isClientSide) {
			this.destroyAndDropTire();
		}

		if (broadcast && !this.level().isClientSide) {
			this.level().broadcastEntityEvent(this, (byte) 6);
		}
	}

	public boolean canShareLeashKnot() {
		if (!this.isLeashed || getLeashType() != LEASH_TYPE_FENCE_KNOT) {
			return false;
		}

		Entity holder = getLeashHolder();
		if (!(holder instanceof LeashFenceKnotEntity)) {
			return false;
		}


		return true;
	}

	public Vec3 getLeashRopePosition(float partialTicks) {
		if (getLeashType() == LEASH_TYPE_FENCE_KNOT && getLeashHolder() instanceof LeashFenceKnotEntity) {
			return new Vec3(getLeashHolder().getX(), getLeashHolder().getY() + 0.5, getLeashHolder().getZ());
		} else if (getLeashHolder() != null) {
			return getLeashHolder().getRopeHoldPosition(partialTicks);
		}
		return this.position();
	}


	private void destroyAndDropTire() {
		if (!this.level().isClientSide) {
			this.spawnAtLocation(ModBlocks.TIRE.get());
			this.spawnAtLocation(Items.LEAD);
			this.discard();
		}
	}


	public InteractionResult tryMountPlayer(Player player) {
		if (this.getPassengers().isEmpty() && !player.isSecondaryUseActive() && getLeashType() == LEASH_TYPE_FENCE_KNOT) {
			if (!this.level().isClientSide) {
				float currentSwingYaw = getSwingYaw();
				this.passengerBodyYaw = currentSwingYaw;
				this.lastPassengerBodyYaw = currentSwingYaw;

				setPassengerYaw(currentSwingYaw);


				if (player.startRiding(this)) {
					setOccupied(true);
					return InteractionResult.CONSUME;
				}
			} else {
				return InteractionResult.SUCCESS;
			}
		} else if (getLeashType() == LEASH_TYPE_PLAYER) {
			dropLeash(true, true);
			return InteractionResult.CONSUME;
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
		if (this.tickCount == 1 && !this.level().isClientSide && this.isLeashed) {
			restoreLeash();
		}

		updateSeatPartPosition();
		if (seatPart != null) {
			boolean seatCollision = checkAABBCollisionWithSolidBlocks(seatPart.getBoundingBox());
			if (seatCollision) {
				ticksSeatInsideBlock++;
				if (ticksSeatInsideBlock >= 60) {
					this.destroyAndDropTire();
					ticksSeatInsideBlock = 0;
				}
			} else {
				ticksSeatInsideBlock = 0;
			}
		}


		if (this.level().isClientSide) {
			this.lastRenderSwingAngle = this.renderSwingAngle;
			this.renderSwingAngle = getRenderSwingAngleInternal(1.0F);

			this.lastSwingProgress = this.swingProgress;
			float targetSwing = (float) Math.sin(Math.toRadians(this.renderSwingAngle * 10)) * 0.5F;
			this.swingProgress += (targetSwing - this.swingProgress) * 0.2F;


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
			if (zeroCrossCooldown > 0) zeroCrossCooldown--;
			if (swooshCooldown > 0) swooshCooldown--;
			if (happinessCooldown > 0) happinessCooldown--;
		} else {
			if (this.tickCount % 100 == 0 && this.isLeashed) {
				validateLeashConnection();
			}
			updateLeashState();

			boolean hasPassenger = !this.getPassengers().isEmpty();
			setOccupied(hasPassenger);


			updateSwingPhysics(hasPassenger);


			updateSwingRotation(hasPassenger);


			if (hasPassenger) {
				Entity passenger = this.getPassengers().get(0);

				if (passenger instanceof Player player) {
					limitPlayerHeadRotation(player);


					float moveInput = player.zza;
					if (moveInput != 0 && !checkSeatCollision(getSwingAngle())) {
						this.pushDirection = (int) Math.signum(moveInput);
						float currentVel = getSwingVelocity();
						float impulse = moveInput * 0.008F;
						setSwingVelocity(currentVel + impulse);
					} else {
						this.pushDirection = 0;
					}


					float strafeInput = player.xxa;
					if (Math.abs(strafeInput) > 0.1f) {
						this.rotationInput = (int) Math.signum(strafeInput);
					} else {
						this.rotationInput = 0;
					}
				}


				if (swingUpdateTimer++ % 2 == 0) {
					this.entityData.set(DATA_SWING_ANGLE, getSwingAngle());
					this.entityData.set(DATA_SWING_VELOCITY, getSwingVelocity());
					this.entityData.set(DATA_SWING_YAW, getSwingYaw());
				}
			}
		}
	}

	private void validateLeashConnection() {
		if (!this.isLeashed) return;

		Entity holder = getLeashHolder();
		if (holder instanceof LeashFenceKnotEntity knot) {
			if (!knot.isAlive()) {
				this.dropLeash(true, true);
			} else if (this.fencePos != null && !knot.getPos().equals(this.fencePos)) {
				this.dropLeash(true, true);
			}
		}
	}

	public boolean leashToExistingKnot(LeashFenceKnotEntity knot, Player player, boolean newSwing) {
		BlockPos fencePos = knot.getPos();


		if (canLeashToFence(fencePos)) {
			this.isLeashed = true;
			this.fencePos = fencePos;


			this.entityData.set(DATA_LEASH_HOLDER_ID, knot.getId());
			this.entityData.set(DATA_LEASH_TYPE, LEASH_TYPE_FENCE_KNOT);
			this.entityData.set(DATA_FENCE_POS, Optional.of(fencePos));


			float distance = (float) Math.sqrt(
					Math.pow(knot.getX() - this.getX(), 2) +
							Math.pow(knot.getY() + 0.5 - this.getY(), 2) +
							Math.pow(knot.getZ() - this.getZ(), 2)
			);
			this.currentRopeLength = distance;
			this.entityData.set(DATA_ROPE_LENGTH, distance);


			if (!this.level().isClientSide) {
				this.level().broadcastEntityEvent(this, (byte) 7);
			}
			if (newSwing) this.setPos(this.getX(), this.getY() + 0.5, this.getZ());

			return true;
		}
		return false;
	}

	private void updateLeashState() {
		if (this.isLeashed) {
			switch (getLeashType()) {
				case LEASH_TYPE_PLAYER:
					updatePlayerLeash();
					break;
				case LEASH_TYPE_FENCE_KNOT:
					updateFenceLeash();
					break;
			}
		} else {
			this.destroyAndDropTire();
		}
	}


	private void updatePlayerLeash() {
		Entity holder = this.getLeashHolder();
		if (holder != null && holder.isAlive()) {
			double distance = this.distanceTo(holder);
			if (distance > MAX_LEASH_DISTANCE) {
				this.dropLeash(true, true);
			}
		} else {
			this.dropLeash(false, false);
		}
	}

	@Nullable
	public LeashFenceKnotEntity getLeashKnot() {
		Entity holder = this.getLeashHolder();
		if (holder instanceof LeashFenceKnotEntity) {
			return (LeashFenceKnotEntity) holder;
		}
		return null;
	}


	private void updateFenceLeash() {
		BlockPos fence = this.getFencePos();
		if (fence != null) {
			LeashFenceKnotEntity knot = this.getLeashKnot();
			if (knot != null && knot.isAlive()) {
				BlockState fenceState = this.level().getBlockState(fence);
				if (!(fenceState.getBlock() instanceof FenceBlock)) {
					this.dropLeash(true, true);
					return;
				}


				float distance = (float) Math.sqrt(
						Math.pow(knot.getX() - this.getX(), 2) +
								Math.pow(knot.getY() + 0.5 - this.getY(), 2) +
								Math.pow(knot.getZ() - this.getZ(), 2)
				);
				this.currentRopeLength = distance;
				this.entityData.set(DATA_ROPE_LENGTH, distance);
			} else {
				this.dropLeash(true, true);
			}
		} else {
			this.dropLeash(false, false);
		}
	}


	public float getRopeLength() {
		return this.entityData.get(DATA_ROPE_LENGTH);
	}

	private void updateSeatPartPosition() {
		if (seatPart == null) return;


		float swingAngleRad = (float) Math.toRadians(getSwingAngle());
		float swingYawRad = (float) Math.toRadians(getSwingYaw());

		double forwardOffset = getRopeLength() * Math.sin(swingAngleRad);
		double verticalOffset = getRopeLength() * (1.0 - Math.cos(swingAngleRad));
		double yOffset = this.getPassengersRidingOffset();


		double rotatedX = -forwardOffset * Math.sin(swingYawRad);
		double rotatedZ = forwardOffset * Math.cos(swingYawRad);

		Vec3 seatPos = this.position()
				.add(rotatedX, yOffset + verticalOffset, rotatedZ);

		seatPart.setPos(seatPos.x, seatPos.y, seatPos.z);


		float width = 0.8f;
		float height = 0.8f;
		seatPart.setBoundingBox(new AABB(
				seatPos.x - width / 2, seatPos.y - 0.05, seatPos.z - width / 2,
				seatPos.x + width / 2, seatPos.y + height, seatPos.z + width / 2
		));
	}

	private void limitPlayerHeadRotation(Player player) {
		float swingYaw = getSwingYaw();
		float headYaw = player.getYHeadRot();


		float normalizedSwingYaw = swingYaw % 360;
		if (normalizedSwingYaw > 180) normalizedSwingYaw -= 360;
		if (normalizedSwingYaw < -180) normalizedSwingYaw += 360;

		float normalizedHeadYaw = headYaw % 360;
		if (normalizedHeadYaw > 180) normalizedHeadYaw -= 360;
		if (normalizedHeadYaw < -180) normalizedHeadYaw += 360;


		float diff = normalizedHeadYaw - normalizedSwingYaw;


		if (diff > 180) diff -= 360;
		if (diff < -180) diff += 360;


		if (Math.abs(diff) > MAX_HEAD_YAW_OFFSET) {
			float limitedHeadYaw = normalizedSwingYaw + (Math.signum(diff) * MAX_HEAD_YAW_OFFSET);


			if (limitedHeadYaw > 180) limitedHeadYaw -= 360;
			if (limitedHeadYaw < -180) limitedHeadYaw += 360;

			player.setYHeadRot(limitedHeadYaw);
			player.yRotO = limitedHeadYaw;
			player.setYRot(limitedHeadYaw);
		}


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
		start = normalizeAngle(start);
		end = normalizeAngle(end);


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


		if (Math.abs(currentAngle) > ROTATION_ANGLE_LIMIT) {
			this.rotationInput = 0;
			this.currentRotationSpeed = 0.0F;
			return;
		}


		float angleFactor = 1.0F - (Math.abs(currentAngle) / ROTATION_ANGLE_LIMIT);


		float targetRotationSpeed = 0.0F;

		if (this.rotationInput != 0) {
			targetRotationSpeed = -this.rotationInput * BASE_ROTATION_SPEED * angleFactor;


			this.currentRotationSpeed += (targetRotationSpeed - this.currentRotationSpeed) * ROTATION_SMOOTHNESS;


			this.randomRotationOffset *= 0.5F;
		} else {
			this.currentRotationSpeed *= 0.8F;


			if (Math.abs(this.currentRotationSpeed) < 0.01F) {
				this.currentRotationSpeed = 0.0F;
			}


			if (Math.abs(currentAngle) < MIN_ANGLE_FOR_RANDOM_ROTATION) {
				randomRotationTimer++;


				if (randomRotationTimer > 10 && this.random.nextFloat() < RANDOM_ROTATION_CHANCE) {
					float randomRotation = (this.random.nextFloat() - 0.5F) * 2.0F * MAX_RANDOM_ROTATION;
					this.randomRotationOffset += randomRotation;
					randomRotationTimer = 0;
				}


				if (Math.abs(this.randomRotationOffset) > 0.01F) {
					this.currentRotationSpeed += this.randomRotationOffset * 0.05F;
					this.randomRotationOffset *= RANDOM_ROTATION_DECAY;
				}
			}
		}


		float maxSpeed = BASE_ROTATION_SPEED * angleFactor;
		this.currentRotationSpeed = Mth.clamp(this.currentRotationSpeed, -maxSpeed, maxSpeed);


		this.targetSwingYaw += this.currentRotationSpeed;


		float currentSwingYaw = getSwingYaw();
		float targetDiff = this.targetSwingYaw - currentSwingYaw;


		if (targetDiff > 180.0F) {
			targetDiff -= 360.0F;
		} else if (targetDiff < -180.0F) {
			targetDiff += 360.0F;
		}
		float newSwingYaw = currentSwingYaw + targetDiff * 0.2F;
		newSwingYaw = normalizeAngle(newSwingYaw);

		setSwingYaw(newSwingYaw);


		while (this.targetSwingYaw > 180.0F) this.targetSwingYaw -= 360.0F;
		while (this.targetSwingYaw < -180.0F) this.targetSwingYaw += 360.0F;
		setTargetSwingYaw(this.targetSwingYaw);
	}

	private boolean checkSeatCollisionWithVelocity(float swingAngle, float velocity) {
		if (seatPart == null) return false;


		if (Math.abs(velocity) < STOP_THRESHOLD) {
			return false;
		}


		float lookAheadAngle = swingAngle + velocity * COLLISION_LOOKAHEAD_FACTOR;


		float swingAngleRad = (float) Math.toRadians(lookAheadAngle);
		float swingYawRad = (float) Math.toRadians(getSwingYaw());

		double forwardOffset = getRopeLength() * Math.sin(swingAngleRad);
		double verticalOffset = getRopeLength() * (1.0 - Math.cos(swingAngleRad));
		double yOffset = this.getPassengersRidingOffset();


		double rotatedX = -forwardOffset * Math.sin(swingYawRad);
		double rotatedZ = forwardOffset * Math.cos(swingYawRad);

		Vec3 futureSeatPos = this.position()
				.add(rotatedX, yOffset + verticalOffset, rotatedZ);


		float collisionWidth = 0.7f;
		float collisionHeight = 0.7f;
		AABB futureSeatCollisionBox = new AABB(
				futureSeatPos.x - collisionWidth / 2,
				futureSeatPos.y - 0.05,
				futureSeatPos.z - collisionWidth / 2,
				futureSeatPos.x + collisionWidth / 2,
				futureSeatPos.y + collisionHeight,
				futureSeatPos.z + collisionWidth / 2
		);


		float currentSwingAngleRad = (float) Math.toRadians(swingAngle);
		double currentForwardOffset = getRopeLength() * Math.sin(currentSwingAngleRad);
		double currentVerticalOffset = getRopeLength() * (1.0 - Math.cos(currentSwingAngleRad));

		double currentRotatedX = -currentForwardOffset * Math.sin(swingYawRad);
		double currentRotatedZ = currentForwardOffset * Math.cos(swingYawRad);

		Vec3 currentSeatPos = this.position()
				.add(currentRotatedX, yOffset + currentVerticalOffset, currentRotatedZ);

		AABB currentSeatCollisionBox = new AABB(
				currentSeatPos.x - collisionWidth / 2,
				currentSeatPos.y - 0.05,
				currentSeatPos.z - collisionWidth / 2,
				currentSeatPos.x + collisionWidth / 2,
				currentSeatPos.y + collisionHeight,
				currentSeatPos.z + collisionWidth / 2
		);


		boolean futureCollision = checkAABBCollisionWithSolidBlocks(futureSeatCollisionBox);


		boolean currentCollision = checkAABBCollisionWithSolidBlocks(currentSeatCollisionBox);

		return futureCollision || currentCollision;
	}

	private void updateSwingPhysics(boolean hasPassenger) {
		float currentAngle = getSwingAngle();
		float currentVelocity = getSwingVelocity();


		float previousAngle = getSwingAngle();


		boolean collisionDetected = checkSeatCollisionWithVelocity(currentAngle, currentVelocity);


		float preCollisionVelocity = currentVelocity;


		float angleRad = (float) Math.toRadians(currentAngle);
		float gravityForce = (float) -Math.sin(angleRad) * SWING_GRAVITY;


		float angleRatio = Math.abs(currentAngle) / MAX_SWING_ANGLE;
		float nonLinearBoost = 1.0F + angleRatio * angleRatio * 2.0F;
		gravityForce *= nonLinearBoost;


		float playerForce = 0.0f;
		if (hasPassenger && this.pushDirection != 0) {
			float efficiency = (float) Math.cos(angleRad);
			efficiency = Math.max(0.2F, Math.abs(efficiency));

			playerForce = this.pushDirection * PLAYER_PUSH_STRENGTH * efficiency;


			this.pushDirection = 0;
		}


		if (collisionDetected) {
			float bounceFactor = 0.7F;
			float newVelocity = -currentVelocity * bounceFactor;


			float maxBounceVelocity = 2.0F;
			if (Math.abs(newVelocity) > maxBounceVelocity) {
				newVelocity = Math.signum(newVelocity) * maxBounceVelocity;
			}


			currentVelocity = newVelocity;


			float angleReduction = 0.9F;
			currentAngle *= angleReduction;


			playerForce = 0.0f;
			this.pushDirection = 0;


			if (Math.abs(preCollisionVelocity) >= MIN_VELOCITY_FOR_SOUND) {
				level().playSound(null, this.seatPart.blockPosition(), RPGSounds.TIRE_BOUNCE.get(),
						SoundSource.BLOCKS, 1.0F, (level().random.nextFloat() - level().random.nextFloat()) * 0.2F + 1.0F);
			}
		}


		float acceleration = gravityForce + playerForce;


		float currentDamping = collisionDetected ? SWING_DAMPING * 0.9F : SWING_DAMPING;
		currentVelocity += acceleration;
		currentVelocity *= currentDamping;


		currentAngle += currentVelocity;


		if (Math.abs(currentAngle) > MAX_SWING_ANGLE) {
			currentAngle = MAX_SWING_ANGLE * Math.signum(currentAngle);
			currentVelocity *= -0.6F;


			if (Math.abs(preCollisionVelocity) >= MIN_VELOCITY_FOR_SOUND) {
				level().playSound(null, this.seatPart.blockPosition(), RPGSounds.TIRE_BOUNCE.get(),
						SoundSource.BLOCKS, 0.8F, (level().random.nextFloat() - level().random.nextFloat()) * 0.2F + 0.9F);
			}
		}


		if (Math.abs(currentVelocity) < STOP_THRESHOLD && Math.abs(currentAngle) < 2.0F) {
			currentAngle *= 0.9F;
			if (Math.abs(currentAngle) < 0.5F) {
				currentAngle = 0.0F;
				currentVelocity = 0.0F;
			}
		}


		updateSwooshSound(currentAngle, currentVelocity);


		updateCrackSound(previousAngle, currentAngle, currentVelocity);


		setSwingAngle(currentAngle);
		setSwingVelocity(currentVelocity);
	}


	private void updateSwooshSound(float currentAngle, float currentVelocity) {
		if (swooshCooldown > 0) {
			swooshCooldown--;
			return;
		}

		float absAngle = Math.abs(currentAngle);
		float absVelocity = Math.abs(currentVelocity);


		boolean isAboveThreshold = absAngle >= SWOOSH_ANGLE_THRESHOLD;


		if (isAboveThreshold && !wasAboveSwooshAngle && absVelocity > 0.5F) {
			float normalizedSpeed = Math.min(absVelocity / MAX_SWOOSH_SPEED, 1.0F);
			float volume = 0.5F + normalizedSpeed * 0.5F;


			float pitch = 0.8F + (normalizedSpeed * 0.4F);

			level().playSound(null, this.blockPosition(), RPGSounds.TIRE_SWING_SWOOSH.get(),
					SoundSource.BLOCKS, volume, pitch);


			swooshCooldown = 5;
			wasAboveSwooshAngle = true;
		}


		if (!isAboveThreshold) {
			wasAboveSwooshAngle = false;
		}
	}


	private void updateCrackSound(float previousAngle, float currentAngle, float currentVelocity) {
		if (zeroCrossCooldown > 0) {
			zeroCrossCooldown--;
			return;
		}


		if (happinessCooldown > 0) {
			happinessCooldown--;
		}


		boolean crossedZero = (previousAngle > 0 && currentAngle <= 0) ||
				(previousAngle < 0 && currentAngle >= 0);


		boolean wasFarEnoughFromZero = Math.abs(previousAngle) > CRACK_ANGLE_DEADZONE;


		boolean hasEnoughVelocityForSound = Math.abs(currentVelocity) >= CRACK_VELOCITY_THRESHOLD;


		boolean hasEnoughVelocityForHappiness = Math.abs(currentVelocity) >= HAPPINESS_VELOCITY_THRESHOLD;


		if (crossedZero && wasFarEnoughFromZero && hasEnoughVelocityForSound && !hasCrossedZeroRecently) {
			float normalizedVelocity = Math.min(Math.abs(currentVelocity) / (CRACK_VELOCITY_THRESHOLD * 2), 1.0F);
			float volume = 0.1F + normalizedVelocity * 0.1F;


			float pitch = 0.3F + (normalizedVelocity * 0.2F);

			level().playSound(null, this.blockPosition(), RPGSounds.TIRE_SWING_CRACK.get(),
					SoundSource.BLOCKS, volume, pitch);


			zeroCrossCooldown = 3;
			hasCrossedZeroRecently = true;
		}


		if (hasEnoughVelocityForHappiness && happinessCooldown == 0 && !this.getPassengers().isEmpty()) {
			applyHappinessEffect();
			happinessCooldown = HAPPINESS_COOLDOWN;
		}


		if (Math.abs(currentAngle) > CRACK_ANGLE_DEADZONE) {
			hasCrossedZeroRecently = false;
		}
	}

	private void applyHappinessEffect() {
		if (this.level().isClientSide || this.getPassengers().isEmpty()) {
			return;
		}

		Entity passenger = this.getPassengers().get(0);
		if (passenger instanceof LivingEntity livingPassenger) {
			MobEffectInstance currentHappiness = livingPassenger.getEffect(ModEffects.HAPPINESS.get());

			int newDuration = HAPPINESS_DURATION;


			if (currentHappiness != null) {
				newDuration = currentHappiness.getDuration() + HAPPINESS_DURATION;


				if (newDuration > MAX_HAPPINESS_DURATION) {
					newDuration = MAX_HAPPINESS_DURATION;
				}


				int amplifier = currentHappiness.getAmplifier();


				MobEffectInstance newEffect = new MobEffectInstance(
						ModEffects.HAPPINESS.get(),
						newDuration,
						amplifier,
						false,
						true,
						true
				);
				newEffect.setCurativeItems(new ArrayList<>());

				livingPassenger.removeEffect(ModEffects.HAPPINESS.get());
				livingPassenger.addEffect(newEffect);
			} else {
				MobEffectInstance newEffect = new MobEffectInstance(
						ModEffects.HAPPINESS.get(),
						newDuration,
						0,
						false,
						true,
						true
				);
				newEffect.setCurativeItems(new ArrayList<>());

				livingPassenger.addEffect(newEffect);
			}
		}
	}


	private boolean checkSeatCollision(float swingAngle) {
		return checkSeatCollisionWithVelocity(swingAngle, getSwingVelocity());
	}

	private boolean checkAABBCollisionWithSolidBlocks(AABB aabb) {
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


					if (!state.isAir() && state.isSolid()) {
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

	private void clampPassengerRotation(Entity passenger) {
		passenger.setYBodyRot(getSwingYaw());
		float f = Mth.wrapDegrees(passenger.getYRot() - getSwingYaw());
		float f1 = Mth.clamp(f, -105.0F, 105.0F);
		passenger.yRotO += f1 - f;
		passenger.setYRot(passenger.getYRot() + f1 - f);
		passenger.setYHeadRot(passenger.getYRot());
	}


	@Override
	public void positionRider(Entity passenger, Entity.MoveFunction moveFunction) {
		if (!this.hasPassenger(passenger)) return;

		float swingAngleRad = (float) Math.toRadians(getSwingAngle());
		float swingYawRad = (float) Math.toRadians(getSwingYaw());

		double forwardOffset = getRopeLength() * Math.sin(swingAngleRad);
		double verticalOffset = getRopeLength() * (1.0 - Math.cos(swingAngleRad));
		double yOffset = this.getPassengersRidingOffset() + passenger.getMyRidingOffset();


		double rotatedX = -forwardOffset * Math.sin(swingYawRad);
		double rotatedZ = forwardOffset * Math.cos(swingYawRad);

		Vec3 seatPos = this.position()
				.add(rotatedX, yOffset + verticalOffset, rotatedZ);


		moveFunction.accept(passenger, seatPos.x, seatPos.y, seatPos.z);


		passenger.setYBodyRot(getSwingYaw());
	}

	@Override
	public double getPassengersRidingOffset() {
		return 0.1;
	}

	@Override
	public Vec3 getDismountLocationForPassenger(LivingEntity passenger) {
		float swingAngleRad = (float) Math.toRadians(getSwingAngle());
		float swingYawRad = (float) Math.toRadians(getSwingYaw());

		double forwardOffset = getRopeLength() * Math.sin(swingAngleRad);
		double verticalOffset = getRopeLength() * (1.0 - Math.cos(swingAngleRad));
		double yOffset = this.getPassengersRidingOffset() + passenger.getMyRidingOffset();

		double rotatedX = -forwardOffset * Math.sin(swingYawRad);
		double rotatedZ = forwardOffset * Math.cos(swingYawRad);

		Vec3 seatPos = this.position()
				.add(rotatedX, yOffset + verticalOffset, rotatedZ);

		return seatPos;
	}

	@Override
	protected boolean canAddPassenger(Entity passenger) {
		return this.getPassengers().isEmpty();
	}

	@Override
	public void onPassengerTurned(Entity passenger) {
		this.clampPassengerRotation(passenger);
	}

	private Vec3 calculateSeatVelocity() {
		float swingAngleRad = (float) Math.toRadians(getSwingAngle());
		float swingVelocityRad = (float) Math.toRadians(getSwingVelocity());
		float swingYawRad = (float) Math.toRadians(getSwingYaw());


		double tangentialSpeed = getRopeLength() * swingVelocityRad;


		double velocityX = -tangentialSpeed * Math.cos(swingAngleRad) * Math.sin(swingYawRad);
		double velocityZ = tangentialSpeed * Math.cos(swingAngleRad) * Math.cos(swingYawRad);
		double velocityY = tangentialSpeed * Math.sin(swingAngleRad);


		if (Math.abs(getSwingAngle()) > 30.0F) {
			velocityY += Math.signum(getSwingVelocity()) * Math.abs(Math.sin(swingAngleRad)) * 0.2;
		}

		return new Vec3(velocityX, velocityY, velocityZ);
	}

	@Override
	protected void removePassenger(Entity passenger) {
		Vec3 seatVelocity = Vec3.ZERO;
		if (!this.level().isClientSide && passenger != null) {
			seatVelocity = calculateSeatVelocity().scale(2);
		}


		super.removePassenger(passenger);


		if (!this.level().isClientSide && passenger != null && seatVelocity.lengthSqr() > 0) {
			if (passenger instanceof ServerPlayer serverPlayer) {
				ModMessages.sendToPlayer(new SwingPlayerPacket(seatVelocity, serverPlayer.getId()), serverPlayer);
			}
		}


		if (this.getPassengers().isEmpty()) {
			setOccupied(false);
			this.pushDirection = 0;
			this.rotationInput = 0;
			this.currentRotationSpeed = 0.0F;
			this.randomRotationOffset = 0.0F;
			this.randomRotationTimer = 0;
			this.hasCrossedZeroRecently = false;
			this.wasAboveSwooshAngle = false;
			this.zeroCrossCooldown = 0;
			this.swooshCooldown = 0;
			this.happinessCooldown = 0;
		}
	}


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
			return lerpAngle(partialTicks, this.lastSwingYaw, this.swingYaw);
		}
		return getSwingYaw();
	}

	public float getSwingProgress(float partialTicks) {
		return this.lastSwingProgress + (this.swingProgress - this.lastSwingProgress) * partialTicks;
	}

	public float getPassengerBodyYaw(float partialTicks) {
		if (this.level().isClientSide) {
			return lerpAngle(partialTicks, this.lastPassengerBodyYaw, this.passengerBodyYaw);
		}
		return getPassengerYaw();
	}


	public float getModelRotationAngle(float swingAngle) {
		if (!isOccupied()) return 0.0F;

		float angleRatio = Math.abs(swingAngle) / MAX_SWING_ANGLE;
		float rotationAdjust = 45 * angleRatio * Math.signum(swingAngle);
		return BASE_MODEL_ROTATION - rotationAdjust;
	}


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

		if (compound.contains("HasCrossedZeroRecently")) {
			this.hasCrossedZeroRecently = compound.getBoolean("HasCrossedZeroRecently");
		}
		if (compound.contains("WasAboveSwooshAngle")) {
			this.wasAboveSwooshAngle = compound.getBoolean("WasAboveSwooshAngle");
		}

		if (compound.contains("LeashHolderId")) {
			int holderId = compound.getInt("LeashHolderId");
			if (holderId != -1) {
				this.entityData.set(DATA_LEASH_HOLDER_ID, holderId);
			}
		}

		if (compound.contains("LeashType")) {
			this.entityData.set(DATA_LEASH_TYPE, compound.getByte("LeashType"));
		}

		if (compound.contains("FencePos")) {
			BlockPos fencePos = NbtUtils.readBlockPos(compound.getCompound("FencePos"));
			this.fencePos = fencePos;
			this.entityData.set(DATA_FENCE_POS, Optional.of(fencePos));
		}

		if (compound.contains("RopeLength")) {
			this.currentRopeLength = compound.getFloat("RopeLength");
			this.entityData.set(DATA_ROPE_LENGTH, this.currentRopeLength);
		}

		if (compound.contains("IsLeashed")) {
			this.isLeashed = compound.getBoolean("IsLeashed");
		}


		if (this.level() != null && !this.level().isClientSide && this.isLeashed) {
			this.restoreLeashConnection();
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

		compound.putBoolean("HasCrossedZeroRecently", this.hasCrossedZeroRecently);
		compound.putBoolean("WasAboveSwooshAngle", this.wasAboveSwooshAngle);
		compound.putInt("LeashHolderId", this.entityData.get(DATA_LEASH_HOLDER_ID));
		compound.putByte("LeashType", this.entityData.get(DATA_LEASH_TYPE));

		if (this.fencePos != null) {
			compound.put("FencePos", NbtUtils.writeBlockPos(this.fencePos));
		}

		compound.putFloat("RopeLength", this.currentRopeLength);
		compound.putBoolean("IsLeashed", this.isLeashed);
	}

	private void restoreLeashConnection() {
		if (!this.isLeashed) return;

		byte leashType = getLeashType();
		int holderId = this.entityData.get(DATA_LEASH_HOLDER_ID);

		if (leashType == LEASH_TYPE_FENCE_KNOT && holderId != -1) {
			Entity entity = this.level().getEntity(holderId);
			if (entity instanceof LeashFenceKnotEntity knot) {
				BlockPos savedPos = this.fencePos;
				if (savedPos != null && knot.getPos().equals(savedPos)) {
					float distance = (float) Math.sqrt(
							Math.pow(knot.getX() - this.getX(), 2) +
									Math.pow(knot.getY() + 0.5 - this.getY(), 2) +
									Math.pow(knot.getZ() - this.getZ(), 2)
					);
					this.currentRopeLength = distance;
					this.entityData.set(DATA_ROPE_LENGTH, distance);
				} else {
					this.dropLeash(false, false);
				}
			} else {
				BlockPos fencePos = getFencePos();
				if (fencePos != null) {
					LeashFenceKnotEntity newKnot = LeashFenceKnotEntity.getOrCreateKnot(
							this.level(), fencePos);
					if (newKnot != null) {
						this.leashToExistingKnot(newKnot, null, false);
					} else {
						this.dropLeash(false, false);
					}
				}
			}
		}
	}

	public Vec3 getLeashOffset(float partialTicks) {
		return new Vec3(0.0D, 0.5D, 0.0D);
	}

	private void restoreLeash() {
		if (!this.level().isClientSide && this.isLeashed) {
			byte leashType = getLeashType();

			if (leashType == LEASH_TYPE_FENCE_KNOT) {
				BlockPos fence = getFencePos();
				if (fence != null) {
					LeashFenceKnotEntity knot = LeashFenceKnotEntity.getOrCreateKnot(this.level(), fence);
					if (knot != null) {
						this.setLeashedTo(knot, false);
						this.entityData.set(DATA_LEASH_HOLDER_ID, knot.getId());


						float distance = (float) Math.sqrt(
								Math.pow(knot.getX() - this.getX(), 2) +
										Math.pow(knot.getY() + 0.5 - this.getY(), 2) +
										Math.pow(knot.getZ() - this.getZ(), 2)
						);
						this.currentRopeLength = distance;
						this.entityData.set(DATA_ROPE_LENGTH, distance);
					} else {
						this.dropLeash(false, false);
					}
				}
			} else if (leashType == LEASH_TYPE_PLAYER) {
				int holderId = this.entityData.get(DATA_LEASH_HOLDER_ID);
				if (holderId != -1) {
					Entity holder = this.level().getEntity(holderId);
					if (holder instanceof Player) {
						this.setLeashedTo(holder, false);
					} else {
						this.dropLeash(false, false);
					}
				}
			}
		}
	}

	@Override
	public Packet<ClientGamePacketListener> getAddEntityPacket() {
		return NetworkHooks.getEntitySpawningPacket(this);
	}


	public void setSeatInteraction(boolean seatInteraction) {
		this.seatInteraction = seatInteraction;
	}


	public class TireSwingSeatPart extends PartEntity<TireSwingEntity> {
		public TireSwingSeatPart(TireSwingEntity parent) {
			super(parent);
			this.blocksBuilding = true;
		}

		public ItemStack getPickResult() {
			return new ItemStack(ModBlocks.TIRE.get());
		}

		@Override
		protected void defineSynchedData() {
		}

		@Override
		protected void readAdditionalSaveData(CompoundTag pCompound) {
		}

		@Override
		protected void addAdditionalSaveData(CompoundTag pCompound) {
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
			return false;
		}

		@Override
		public void setPos(double x, double y, double z) {
			super.setPos(x, y, z);

			this.setBoundingBox(this.getBoundingBox());
		}

		@Override
		public boolean isPushable() {
			return false;
		}

		@Override
		public InteractionResult interact(Player player, InteractionHand hand) {
			if (this.level().isClientSide && this.getParent().getPassengers().isEmpty()) {
				ModMessages.sendToServer(new PacketTireSwingInteraction(this.getParent().getId()));
				return InteractionResult.SUCCESS;
			}
			return InteractionResult.PASS;
		}

		@Override
		public boolean isNoGravity() {
			return true;
		}

		@Override
		public boolean isAttackable() {
			return false;
		}

		@Override
		public boolean isAlive() {
			return this.getParent() != null && this.getParent().isAlive();
		}

		@Override
		public void remove(net.minecraft.world.entity.Entity.RemovalReason reason) {
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
