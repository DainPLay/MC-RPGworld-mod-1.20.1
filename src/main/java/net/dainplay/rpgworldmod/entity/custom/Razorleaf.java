package net.dainplay.rpgworldmod.entity.custom;

import com.mojang.authlib.GameProfile;
import net.dainplay.rpgworldmod.RPGworldMod;
import net.dainplay.rpgworldmod.block.ModBlocks;
import net.dainplay.rpgworldmod.damage.ModDamageTypes;
import net.dainplay.rpgworldmod.data.tags.ModAdvancements;
import net.dainplay.rpgworldmod.item.ModItems;
import net.dainplay.rpgworldmod.item.custom.FireproofSkirtItem;
import net.dainplay.rpgworldmod.network.ModMessages;
import net.dainplay.rpgworldmod.network.SyncEntityMotionPacket;
import net.dainplay.rpgworldmod.network.SyncRazorleafDataPacket;
import net.dainplay.rpgworldmod.sounds.RPGSounds;
import net.dainplay.rpgworldmod.util.ModTags;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.LookControl;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.FlintAndSteelItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MobBucketItem;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.TntBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.OptionalInt;
import java.util.UUID;

public class Razorleaf extends Monster {

	public static boolean checkRazorleafSpawnRules(EntityType<Razorleaf> razorleafEntityType, ServerLevelAccessor serverLevelAccessor, MobSpawnType mobSpawnType, BlockPos blockPos, RandomSource randomSource) {
		return true;
	}

	public enum State {
		IDLE,
		ATTACKING,
		EXTINGUISHED
	}

	private static final int ATTACK_TONGUE = 1;
	private static final int ATTACK_FIRE = 2;
	private static final int ATTACK_SPIN = 3;

	public static final EntityDataAccessor<Integer> DATA_STATE = SynchedEntityData.defineId(Razorleaf.class, EntityDataSerializers.INT);
	public static final EntityDataAccessor<Integer> DATA_ATTACK_TYPE = SynchedEntityData.defineId(Razorleaf.class, EntityDataSerializers.INT);
	public static final EntityDataAccessor<Integer> DATA_EXTINGUISH_TIME = SynchedEntityData.defineId(Razorleaf.class, EntityDataSerializers.INT);
	public static final EntityDataAccessor<OptionalInt> DATA_TONGUE_TARGET_ID = SynchedEntityData.defineId(Razorleaf.class, EntityDataSerializers.OPTIONAL_UNSIGNED_INT);
	public static final EntityDataAccessor<Integer> DATA_ATTACK_TIMER = SynchedEntityData.defineId(Razorleaf.class, EntityDataSerializers.INT);
	public static final EntityDataAccessor<Integer> DATA_TONGUE_ANIMATION_TIME = SynchedEntityData.defineId(Razorleaf.class, EntityDataSerializers.INT);
	public static final EntityDataAccessor<Float> DATA_SPIT_DIRECTION_X = SynchedEntityData.defineId(Razorleaf.class, EntityDataSerializers.FLOAT);
	public static final EntityDataAccessor<Float> DATA_SPIT_DIRECTION_Y = SynchedEntityData.defineId(Razorleaf.class, EntityDataSerializers.FLOAT);
	public static final EntityDataAccessor<Float> DATA_SPIT_DIRECTION_Z = SynchedEntityData.defineId(Razorleaf.class, EntityDataSerializers.FLOAT);
	public static final EntityDataAccessor<Boolean> DATA_HAS_ITEM_IN_MOUTH = SynchedEntityData.defineId(Razorleaf.class, EntityDataSerializers.BOOLEAN);
	public static final EntityDataAccessor<Integer> DATA_RANDOM_SEED = SynchedEntityData.defineId(Razorleaf.class, EntityDataSerializers.INT);
	public static final EntityDataAccessor<Float> DATA_PULL_DIRECTION_X = SynchedEntityData.defineId(Razorleaf.class, EntityDataSerializers.FLOAT);
	public static final EntityDataAccessor<Float> DATA_PULL_DIRECTION_Y = SynchedEntityData.defineId(Razorleaf.class, EntityDataSerializers.FLOAT);
	public static final EntityDataAccessor<Float> DATA_PULL_DIRECTION_Z = SynchedEntityData.defineId(Razorleaf.class, EntityDataSerializers.FLOAT);
	private static final EntityDataAccessor<Boolean> DATA_DEALT_DAMAGE = SynchedEntityData.defineId(Razorleaf.class, EntityDataSerializers.BOOLEAN);
	private static final EntityDataAccessor<Boolean> DATA_CAN_SEE_TARGET = SynchedEntityData.defineId(Razorleaf.class, EntityDataSerializers.BOOLEAN);

	private int tongueCooldown = 0;
	private int fireCooldown = 0;
	private int randomSeed = 0;
	private int lastVisibilityCheckTick = 0;
	private static final int VISIBILITY_CHECK_INTERVAL = 5; // Проверка видимости каждые 5 тиков

	private ItemStack heldItem = ItemStack.EMPTY;
	private int tongueRetractTimer = 0;

	private float currentPetal1XRot = 0;
	private float currentPetal1ZRot = 0;
	private float currentPetal2XRot = 0;
	private float currentPetal2ZRot = 0;
	private float currentPetal3XRot = 0;
	private float currentPetal3ZRot = 0;
	private float currentPetal4XRot = 0;
	private float currentPetal4ZRot = 0;
	private float currentStemYRot = 0;
	private float currentLeaves1XRot = 0;
	private float currentLeaves2XRot = 0;
	private float currentLeaves3XRot = 0;
	private float currentLeaf1ZRot = 0;
	private float currentLeaf2ZRot = 0;
	private float currentLeaf3ZRot = 0;
	private float currentLeaf4ZRot = 0;
	private float currentLeaf5ZRot = 0;
	private float currentLeaf6ZRot = 0;
	private float currentTongueYRot = 0;
	private float currentTongueZRot = 0;
	private float currentTongueXRot = 0;
	private float currentTongueExtension = 0;
	private float currentHeadXRot = 0;

	private float targetPetal1XRot = 0;
	private float targetPetal1ZRot = 0;
	private float targetPetal2XRot = 0;
	private float targetPetal2ZRot = 0;
	private float targetPetal3XRot = 0;
	private float targetPetal3ZRot = 0;
	private float targetPetal4XRot = 0;
	private float targetPetal4ZRot = 0;
	private float targetStemYRot = 0;
	private float targetLeaves1XRot = 0;
	private float targetLeaves2XRot = 0;
	private float targetLeaves3XRot = 0;
	private float targetLeaf1ZRot = 0;
	private float targetLeaf2ZRot = 0;
	private float targetLeaf3ZRot = 0;
	private float targetLeaf4ZRot = 0;
	private float targetLeaf5ZRot = 0;
	private float targetLeaf6ZRot = 0;
	private float targetTongueYRot = 0;
	private float targetTongueZRot = 0;
	private float targetTongueXRot = 0;
	private float targetTongueExtension = 0;
	private float targetHeadXRot = 0;

	private State prevState = State.IDLE;
	private int prevAttackType = 0;

	private final List<FireProjectileData> fireProjectiles = new ArrayList<>();

	public Razorleaf(EntityType<? extends Monster> entityType, Level level) {
		super(entityType, level);
		this.lookControl = new RazorleafLookControl(this);
		this.xpReward = 40;

		if (!level.isClientSide) {
			this.randomSeed = this.random.nextInt();
			this.entityData.set(DATA_RANDOM_SEED, this.randomSeed);
		}
	}

	private static class RazorleafLookControl extends LookControl {
		private final Razorleaf razorleaf;

		public RazorleafLookControl(Razorleaf razorleaf) {
			super(razorleaf);
			this.razorleaf = razorleaf;
		}

		@Override
		public void tick() {
			if (razorleaf.shouldForceLook()) {
				Vec3 lookTarget = razorleaf.getForcedLookTarget();
				if (lookTarget != null) {
					this.mob.getLookControl().setLookAt(
							lookTarget.x,
							lookTarget.y,
							lookTarget.z,
							1800F,
							1800F
					);
				}
			}
			super.tick();
		}
	}

	@Override
	protected void defineSynchedData() {
		super.defineSynchedData();
		this.entityData.define(DATA_STATE, State.IDLE.ordinal());
		this.entityData.define(DATA_ATTACK_TYPE, 0);
		this.entityData.define(DATA_EXTINGUISH_TIME, 0);
		this.entityData.define(DATA_TONGUE_TARGET_ID, OptionalInt.empty());
		this.entityData.define(DATA_ATTACK_TIMER, 0);
		this.entityData.define(DATA_TONGUE_ANIMATION_TIME, 0);
		this.entityData.define(DATA_SPIT_DIRECTION_X, 0f);
		this.entityData.define(DATA_SPIT_DIRECTION_Y, 0f);
		this.entityData.define(DATA_SPIT_DIRECTION_Z, 0f);
		this.entityData.define(DATA_HAS_ITEM_IN_MOUTH, false);
		this.entityData.define(DATA_RANDOM_SEED, 0);
		this.entityData.define(DATA_PULL_DIRECTION_X, 0f);
		this.entityData.define(DATA_PULL_DIRECTION_Y, 0f);
		this.entityData.define(DATA_PULL_DIRECTION_Z, 0f);
		this.entityData.define(DATA_DEALT_DAMAGE, false);
		this.entityData.define(DATA_CAN_SEE_TARGET, false);
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Monster.createMonsterAttributes()
				.add(Attributes.MAX_HEALTH, 100.0D)
				.add(Attributes.ARMOR, 10.0D)
				.add(Attributes.KNOCKBACK_RESISTANCE, 1.0D)
				.add(Attributes.MOVEMENT_SPEED, 0.0D)
				.add(Attributes.FOLLOW_RANGE, 16.0D);
	}

	public boolean isDay(LevelAccessor level) {
		return (level.dayTime() % 24000) >= 0 && (level.dayTime() % 24000) < 12300;
	}

	@Override
	public void tick() {
		super.tick();

		if (this.level().isClientSide) {
			this.randomSeed = this.entityData.get(DATA_RANDOM_SEED);
			// Клиентская обработка притягивания
			handleClientPull();
		}

		if (this.isOnFire()) this.extinguishFire();

		if (!this.level().isClientSide) {
			serverTick();
		} else {
			clientTick();
		}
	}

	private void handleClientPull() {
		// Проверяем, находимся ли мы в состоянии притягивания
		Vec3 pullDirection = getPullDirection();
		if (pullDirection.lengthSqr() > 0.01) {
			// Находим цель притягивания
			Entity target = getTongueTargetEntity();
			if (target != null) {
				// Если цель - текущий игрок, применяем движение
				if (target instanceof Player player && player == Minecraft.getInstance().player) {
					Vec3 motion = pullDirection.scale(0.75);
					player.setDeltaMovement(motion);
				}
			}
		}
	}

	@Override
	protected boolean shouldDespawnInPeaceful() {
		return false;
	}

	@Override
	public void checkDespawn() {
	}

	public void turnToBlock() {
		if (!this.level().isClientSide) {
			BlockPos pos = this.blockPosition();

			if (ModBlocks.YOUNG_RAZORLEAF.get().canSurvive(ModBlocks.YOUNG_RAZORLEAF.get().defaultBlockState(), level(), pos))
				this.level().setBlockAndUpdate(pos, ModBlocks.YOUNG_RAZORLEAF.get().defaultBlockState());
			else if (level().getBlockState(pos.below()).canBeReplaced()) {
				this.level().setBlockAndUpdate(pos.below(), ModBlocks.SPIKY_IVY.get().defaultBlockState());
				this.level().setBlockAndUpdate(pos, ModBlocks.YOUNG_RAZORLEAF.get().defaultBlockState());
			} else {
				this.spawnAtLocation(ModBlocks.YOUNG_RAZORLEAF.get());
			}
			this.playSound(RPGSounds.RAZORLEAF_EXTINGUISH.get(), 1.0F, 0.8F + getRandom().nextFloat() * 0.4F);
			this.discard();
		}
	}

	private void serverTick() {
		if (!isDay(level()) || level().getDifficulty() == Difficulty.PEACEFUL) setState(State.IDLE);
		if (isDay(level()) && getState() == State.IDLE && this.tickCount % 100 == 0) {
			this.heal(1);
		}

		if (this.isInFluidType()) {
			AABB box = this.getBoundingBox();
			boolean inExtinguishingFluid = false;

			for (BlockPos pos : BlockPos.betweenClosed(
					Mth.floor(box.minX),
					Mth.floor(box.minY),
					Mth.floor(box.minZ),
					Mth.floor(box.maxX),
					Mth.floor(box.maxY),
					Mth.floor(box.maxZ))) {

				FluidState fluidState = this.level().getFluidState(pos);
				if (!fluidState.isEmpty() && fluidState.getFluidType() != null) {
					if (fluidState.getFluidType().canExtinguish(this)) {
						inExtinguishingFluid = true;
						break;
					}
				}
			}

			if (inExtinguishingFluid) {
				this.turnToBlock();
			}
		}

		updateFireProjectiles();

		if (getState() == State.EXTINGUISHED) {
			int time = getExtinguishTime() - 1;
			setExtinguishTime(Math.max(0, time));
			if (time <= 0) {
				setState(State.IDLE);
			}
		}

		if (tongueCooldown > 0) tongueCooldown--;
		if (fireCooldown > 0) fireCooldown--;

		if (isDay(level()) && level().getDifficulty() != Difficulty.PEACEFUL) {
			Zombie nearestZombie = this.level().getNearestEntity(Zombie.class,
					TargetingConditions.forCombat(),
					this,
					this.getX(), this.getY(), this.getZ(),
					this.getBoundingBox().inflate(4.0));

			if (getState() != State.ATTACKING && getState() != State.EXTINGUISHED) {
				if (nearestZombie != null) {
					if (tongueCooldown <= 0) {
						this.setTarget(nearestZombie);
						tongueAttack(nearestZombie);
					}
				} else {
					Player nearestPlayer = this.level().getNearestPlayer(this.getX(), this.getY(), this.getZ(), 16, true);
					if (nearestPlayer != null) {
						double distance = this.distanceTo(nearestPlayer);

						if (distance <= 4.0) {
							if (tongueCooldown <= 0) {
								startAttack(nearestPlayer);
							}
						} else if (distance <= 16.0) {
							if (fireCooldown <= 0) {
								startFireBreath(nearestPlayer);
							}
						} else {
							setState(State.IDLE);
						}
					} else {
						List<ItemEntity> items = this.level().getEntitiesOfClass(ItemEntity.class,
								this.getBoundingBox().inflate(4.0));

						ItemEntity nearestItem = null;
						double closestItemDistance = Double.MAX_VALUE;

						for (ItemEntity item : items) {
							double distance = this.distanceToSqr(item);
							if (distance < closestItemDistance) {
								closestItemDistance = distance;
								nearestItem = item;
							}
						}
						if (nearestItem != null) {
							if (tongueCooldown <= 0) {
								tongueAttack(nearestItem);
							}
						} else {
							setState(State.IDLE);
						}
					}
				}
			}
		}

		if (getState() == State.ATTACKING) {
			int attackTimer = getAttackTimer() + 1;
			setAttackTimer(attackTimer);

			switch (getAttackType()) {
				case ATTACK_TONGUE:
					handleTongueAttack(this.getTarget());
					setTongueAnimationTime(getTongueAnimationTime() + 1);
					break;
				case ATTACK_FIRE:
					handleFireBreath(this.getTarget());
					break;
				case ATTACK_SPIN:
					handlePetalSpin();
					break;
			}
		}

		if (this.tickCount % 5 == 0) {
			syncToClients();
		}
	}

	private void clientTick() {
		if (getState() == State.IDLE && this.random.nextFloat() < 0.1f) {
			Vec3 smokePos = this.position().add(0, 1.5, 0);
			this.level().addParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE,
					smokePos.x, smokePos.y, smokePos.z,
					0, 0.05, 0);
		}
	}

	// Новый метод для проверки видимости цели
	private boolean canSeeTarget(Entity target) {
		if (target == null) return false;

		// Проверяем только каждые N тиков для оптимизации
		if (this.tickCount - lastVisibilityCheckTick < VISIBILITY_CHECK_INTERVAL) {
			return this.entityData.get(DATA_CAN_SEE_TARGET);
		}

		lastVisibilityCheckTick = this.tickCount;
		boolean canSee = false;

		if (target instanceof LivingEntity) {
			// Для живых существ используем стандартную проверку видимости
			canSee = this.hasLineOfSight((LivingEntity) target);
		} else {
			// Для предметов и других сущностей используем проверку прямой видимости
			Vec3 eyePos = this.getEyePosition();
			Vec3 targetPos = target.getBoundingBox().getCenter();

			// Проверяем, нет ли препятствий между Razorleaf и целью
			BlockHitResult hitResult = this.level().clip(new ClipContext(
					eyePos,
					targetPos,
					ClipContext.Block.VISUAL,
					ClipContext.Fluid.NONE,
					this
			));

			canSee = hitResult.getType() == HitResult.Type.MISS;
		}

		this.entityData.set(DATA_CAN_SEE_TARGET, canSee);
		return canSee;
	}

	private void tongueAttack(Entity entity) {
		if (entity == null || !canSeeTarget(entity)) {
			setState(State.IDLE);
			return;
		}

		if (entity instanceof LivingEntity livingEntity) this.setTarget(livingEntity);
		else this.setTarget(null);
		setAttackType(ATTACK_TONGUE);
		tongueCooldown = 100;
		setTongueAnimationTime(0);
		setState(State.ATTACKING);
		setAttackTimer(0);
		tongueRetractTimer = 0;
		setHasItemInMouth(false);
		heldItem = ItemStack.EMPTY;
		// Сбрасываем направление притягивания
		setPullDirection(Vec3.ZERO);
	}

	private void startAttack(Player player) {
		if (player == null || !canSeeTarget(player)) {
			setState(State.IDLE);
			return;
		}

		this.setTarget(player);
		boolean useTongue = getRandom().nextFloat() < 0.5f && tongueCooldown <= 0;

		if (useTongue) {
			setAttackType(ATTACK_TONGUE);
			tongueCooldown = 100;
			setTongueAnimationTime(0);
			tongueRetractTimer = 0;
			setHasItemInMouth(false);
			heldItem = ItemStack.EMPTY;
		} else {
			setAttackType(ATTACK_SPIN);
			tongueCooldown = 80;
		}

		setState(State.ATTACKING);
		setAttackTimer(0);
	}

	private void startFireBreath(Player player) {
		if (player == null || !canSeeTarget(player)) {
			setState(State.IDLE);
			return;
		}

		this.setTarget(player);
		setAttackType(ATTACK_FIRE);
		setState(State.ATTACKING);
		setAttackTimer(0);
		fireCooldown = 150;
	}

	private void handleTongueAttack(Entity entity) {
		int attackTimer = getAttackTimer();

		// Проверяем видимость цели в начале атаки и периодически во время атаки
		if ((attackTimer == 1 || attackTimer % 10 == 0) && entity != null && !canSeeTarget(entity)) {
			// Если потеряли видимость цели, прерываем атаку
			clearTongueTarget();
			setState(State.IDLE);
			setAttackTimer(0);
			return;
		}

		if (attackTimer > 1 && attackTimer <= 30) {
			List<ItemEntity> items = this.level().getEntitiesOfClass(ItemEntity.class,
					this.getBoundingBox());

			for (ItemEntity item : items) {
				handleItemIngestion(item);
			}
		}
		if (attackTimer == 1) {
			this.playSound(RPGSounds.RAZORLEAF_ATTACK.get(), 1.0F, 0.8F + getRandom().nextFloat() * 0.4F);
			this.playSound(RPGSounds.RAZORLEAF_EAT.get(), 1.0F, 0.8F + getRandom().nextFloat() * 0.4F);
			if (entity != null && canSeeTarget(entity)) {
				setTongueTarget(entity);
			} else {
				List<ItemEntity> items = this.level().getEntitiesOfClass(ItemEntity.class,
						this.getBoundingBox().inflate(4.0));

				ItemEntity nearestItem = null;
				double closestItemDistance = Double.MAX_VALUE;

				for (ItemEntity item : items) {
					double distance = this.distanceToSqr(item);
					if (distance < closestItemDistance && canSeeTarget(item)) {
						closestItemDistance = distance;
						nearestItem = item;
					}
				}
				if (nearestItem != null) {
					setTongueTarget(nearestItem);
				} else setState(State.IDLE);
			}
		} else if (attackTimer < 22) {
			Entity target = getTongueTargetEntity();
			if (target != null && canSeeTarget(target)) {
				setForcedLookTarget(target.position());
			} else {
				// Если потеряли видимость, прерываем атаку
				clearTongueTarget();
				if(getState() == State.ATTACKING) setState(State.IDLE);
			}
		} else if (attackTimer <= 30) {
			Entity target = getTongueTargetEntity();
			if (target != null && canSeeTarget(target)) {
				Vec3 targetPos = target.position();
				Vec3 pullDirection = targetPos.vectorTo(this.position()).normalize();

				if (target instanceof ItemEntity item) {
					target.setDeltaMovement(pullDirection.scale(0.75D));

					if (attackTimer == 30 && targetPos.distanceTo(this.position()) < 1.0) {
						handleItemIngestion(item);
					}
				} else if (target instanceof LivingEntity living && living.position().distanceTo(this.position()) < 4) {
					// Синхронизируем направление притягивания
					setPullDirection(pullDirection);

					// Применяем движение и синхронизируем с клиентом
					Vec3 motion = pullDirection.scale(0.75D);
					target.setDeltaMovement(motion);

					// Синхронизируем движение с клиентом
					if (target instanceof ServerPlayer serverPlayer) {
						ModMessages.sendToPlayer(new SyncEntityMotionPacket(
								serverPlayer.getId(),
								motion.x,
								motion.y,
								motion.z
						), serverPlayer);
					}

					if (attackTimer == 30 && targetPos.distanceTo(this.position()) < 1.5) {
						if (!living.fireImmune()) {
							if (living instanceof Zombie) {
								for (ServerPlayer serverplayer : level().getEntitiesOfClass(ServerPlayer.class, new AABB(blockPosition()).inflate(10.0D, 5.0D, 10.0D))) {
									ModAdvancements.FEED_ZOMBIE_TO_RAZORLEAF_TRIGGER.trigger(serverplayer);
								}
							}
							if (living instanceof ServerPlayer player && player.getItemBySlot(EquipmentSlot.LEGS).getItem() == ModItems.FIREPROOF_SKIRT.get() && FireproofSkirtItem.isFireproof(player.getItemBySlot(EquipmentSlot.LEGS))) {
								ModAdvancements.GET_EATEN_BY_RAZORLEAF_WEARING_SKIRT_TRIGGER.trigger(player);
							}
							if (living.hurt(ModDamageTypes.getEntityDamageSource(this.level(), ModDamageTypes.SWALLOW, this), 30.0F))
								if (living instanceof ServerPlayer player)
									if (!player.fireImmune() && !this.entityData.get(DATA_DEALT_DAMAGE)) {
										this.entityData.set(DATA_DEALT_DAMAGE, true);
										if (!hasGoldenKill(player))this.playSound(RPGSounds.GOLDEN_TOKEN_FAIL.get(), 2.0F, 1.0F);
									}
							this.playSound(RPGSounds.RAZORLEAF_BITE.get(), 1.0F, 0.8F + getRandom().nextFloat() * 0.4F);
						}
					}
				}
			} else {
				// Если потеряли видимость цели, прерываем атаку
				setPullDirection(Vec3.ZERO);
				clearTongueTarget();
				setState(State.IDLE);
			}
		} else if (attackTimer == 31) {
			// Сбрасываем направление притягивания
			setPullDirection(Vec3.ZERO);

			if (hasItemInMouth()) {
				eatItem();
			} else {
				Entity target = getTongueTargetEntity();
				if (target instanceof LivingEntity living && living.isAlive() && living.position().distanceTo(this.position()) < 1.5) {
					spitOutEntity(living);
				}
				clearTongueTarget();
				setState(State.IDLE);
			}
		} else if (attackTimer >= 40) {
			setState(State.IDLE);
		}
	}


	public static boolean hasGoldenKill(ServerPlayer player) {
		PlayerAdvancements advancements = player.getAdvancements();

		Advancement silverAdvancement = player.server.getAdvancements().getAdvancement(RPGworldMod.prefix("kill_all_rie_weald_mobs"));
		AdvancementProgress silverProgress = advancements.getOrStartProgress(silverAdvancement);

		if (!silverProgress.isDone()) {
			return true;
		}

		Advancement advancement = player.server.getAdvancements().getAdvancement(RPGworldMod.prefix("golden_kill_all_rie_weald_mobs"));
		AdvancementProgress progress = advancements.getOrStartProgress(advancement);

		if (progress.isDone()) {
			return true;
		}

		Iterable<String> completedCriteria = progress.getCompletedCriteria();

		for (String criterionId : completedCriteria) {
			if (criterionId.equals("golden_kill_razorleaf")) {
				return true;
			}
		}

		return false;
	}

	private void handleItemIngestion(ItemEntity item) {
		if (this.isDeadOrDying()) return;

		ItemStack stack = item.getItem();

		if (stack.getItem().isFireResistant()) {
			spitOutItem(item);
			return;
		}

		if (stack.getItem() == ModBlocks.TIRE.get().asItem()) {
			item.discard();
			setState(State.EXTINGUISHED);
			die(this.damageSources().generic());
			this.level().explode(this, this.getX(), this.getY(0.0625D), this.getZ(), 2.0F, Level.ExplosionInteraction.MOB);
			spawnExtinguishSmokeCircleAnimated();
			this.playSound(RPGSounds.RAZORLEAF_EXTINGUISH.get(), 1.0F, 0.8F + getRandom().nextFloat() * 0.4F);
			this.spawnAtLocation(ModItems.MUSIC_DISC_TIRE.get());
			for(ServerPlayer serverplayer : level().getEntitiesOfClass(ServerPlayer.class, new AABB(this.blockPosition()).inflate(10.0D, 25.0D, 10.0D))) {
				ModAdvancements.FEED_TIRE_TO_RAZORLEAF.trigger(serverplayer);
			}
			this.discard();
			return;
		}

		if (stack.is(ModTags.Items.EXTINGUISH_RAZORLEAF)
				|| stack.getItem() instanceof MobBucketItem
				|| stack.getItem() instanceof PotionItem
				|| (stack.getItem() instanceof BucketItem bucket
				&& bucket.getFluid().getFluidType().canExtinguish(this))) {
			if (item.getItem().isEdible())
				item.getItem().finishUsingItem(level(), this);
			item.discard();
			setState(State.EXTINGUISHED);
			setExtinguishTime(200);
			setForcedLookTarget(null);
			spawnExtinguishSmokeCircleAnimated();
			this.playSound(RPGSounds.RAZORLEAF_EXTINGUISH.get(), 1.0F, 0.8F + getRandom().nextFloat() * 0.4F);
			return;
		}
		if (item.getItem().isEdible())
			item.getItem().finishUsingItem(level(), this);
		item.discard();
		clearTongueTarget();

		this.playSound(RPGSounds.RAZORLEAF_BITE.get(), 1.0F, 0.8F + getRandom().nextFloat() * 0.4F);
	}

	@Override
	public ItemStack eat(Level pLevel, ItemStack pFood) {
		this.addEatEffect(pFood, pLevel, this);
		this.gameEvent(GameEvent.EAT);

		return pFood;
	}

	private void eatItem() {
		if (heldItem.isEmpty()) return;

		float yaw = (getRandom().nextFloat() * 360.0F) * Mth.DEG_TO_RAD;
		float pitch = (getRandom().nextFloat() * 35.0F + 45.0F) * Mth.DEG_TO_RAD;

		Vec3 direction = new Vec3(
				Mth.sin(yaw) * Mth.cos(pitch),
				Mth.sin(pitch),
				Mth.cos(yaw) * Mth.cos(pitch)
		).normalize().scale(1);

		setSpitDirection(direction);
		setForcedLookTarget(this.position().add(direction));

		ItemEntity itemEntity = new ItemEntity(this.level(),
				this.getX(), this.getY() + 1.0, this.getZ(),
				heldItem.copy());
		itemEntity.setDeltaMovement(direction);
		itemEntity.setDefaultPickUpDelay();
		this.level().addFreshEntity(itemEntity);

		heldItem = ItemStack.EMPTY;
		setHasItemInMouth(false);
		clearTongueTarget();

		this.playSound(RPGSounds.RAZORLEAF_SPIT.get(), 1.0F, 0.8F + getRandom().nextFloat() * 0.4F);

		setState(State.IDLE);
		setAttackTimer(0);
	}

	private void spitOutItem(ItemEntity originalItem) {
		float yaw = (getRandom().nextFloat() * 360.0F) * Mth.DEG_TO_RAD;
		float pitch = (getRandom().nextFloat() * 35.0F + 45.0F) * Mth.DEG_TO_RAD;

		Vec3 direction = new Vec3(
				Mth.sin(yaw) * Mth.cos(pitch),
				Mth.sin(pitch),
				Mth.cos(yaw) * Mth.cos(pitch)
		).normalize().scale(1);

		setSpitDirection(direction);
		setForcedLookTarget(this.position().add(direction));

		originalItem.setPos(this.position());
		originalItem.setDeltaMovement(direction);
		originalItem.setDefaultPickUpDelay();

		this.playSound(RPGSounds.RAZORLEAF_SPIT.get(), 1.0F, 0.8F + getRandom().nextFloat() * 0.4F);

		setState(State.IDLE);
		clearTongueTarget();
	}

	private void spitOutEntity(LivingEntity entity) {
		float yaw = (getRandom().nextFloat() * 360.0F) * Mth.DEG_TO_RAD;
		float pitch = (getRandom().nextFloat() * 35.0F + 45.0F) * Mth.DEG_TO_RAD;

		Vec3 direction = new Vec3(
				Mth.sin(yaw) * Mth.cos(pitch),
				Mth.sin(pitch),
				Mth.cos(yaw) * Mth.cos(pitch)
		).normalize().scale(2);

		setSpitDirection(direction);
		setForcedLookTarget(this.position().add(direction));

		entity.setDeltaMovement(direction);
		entity.hurtMarked = true;

		// Синхронизируем движение с клиентом
		if (entity instanceof ServerPlayer serverPlayer) {
			ModMessages.sendToPlayer(new SyncEntityMotionPacket(
					serverPlayer.getId(),
					direction.x,
					direction.y,
					direction.z
			), serverPlayer);
		}

		this.playSound(RPGSounds.RAZORLEAF_SPIT.get(), 1.0F, 0.8F + getRandom().nextFloat() * 0.4F);

		setState(State.IDLE);
		clearTongueTarget();
	}

	private void handleFireBreath(LivingEntity entity) {
		int attackTimer = getAttackTimer();

		// Проверяем видимость цели в начале атаки и периодически во время атаки
		if ((attackTimer == 1 || attackTimer % 10 == 0) && (entity == null || !canSeeTarget(entity))) {
			// Если потеряли видимость цели, прерываем атаку
			setState(State.IDLE);
			setAttackTimer(0);
			return;
		}

		if (attackTimer < 20) {
			if (attackTimer == 1)
				this.playSound(RPGSounds.RAZORLEAF_ATTACK.get(), 1.0F, 0.8F + getRandom().nextFloat() * 0.4F);
			if (entity != null && canSeeTarget(entity)) {
				setForcedLookTarget(entity.position().add(0, entity.getEyeHeight() * 0.5, 0));
			}
		} else if (attackTimer < 60) {

			List<ItemEntity> items = this.level().getEntitiesOfClass(ItemEntity.class,
					this.getBoundingBox());

			for (ItemEntity item : items) {
				handleItemIngestion(item);
			}

			if (attackTimer == 21)
				this.playSound(RPGSounds.RAZORLEAF_FIRE.get(), 1.0F, 1F);
			if (entity != null && canSeeTarget(entity)) {
				setForcedLookTarget(entity.position().add(0, entity.getEyeHeight() * 0.5, 0));
			}
			if (attackTimer % 3 == 0) {
				if (entity != null && canSeeTarget(entity)) {
					createFireProjectile(entity);
				}
			}
		} else {
			setState(State.IDLE);
			setAttackTimer(0);
		}
	}

	private void handlePetalSpin() {
		int attackTimer = getAttackTimer();

		// Для спин-атаки проверяем видимость игрока в начале
		if (attackTimer == 1) {
			Player targetPlayer = null;
			for (Player player : this.level().getEntitiesOfClass(Player.class,
					new AABB(this.blockPosition()).inflate(4.0))) {
				if (this.distanceTo(player) <= 4.0 && canSeeTarget(player)) {
					targetPlayer = player;
					break;
				}
			}

			if (targetPlayer == null) {
				setState(State.IDLE);
				setAttackTimer(0);
				return;
			}

			this.playSound(RPGSounds.RAZORLEAF_ATTACK.get(), 1.0F, 0.8F + getRandom().nextFloat() * 0.4F);
			this.playSound(RPGSounds.RAZORLEAF_SPIN.get(), 1.0F, 0.8F + getRandom().nextFloat() * 0.4F);
		} else if (attackTimer < 20) {
			boolean hitAnyPlayer = false;
			for (Player player : this.level().getEntitiesOfClass(Player.class,
					new AABB(this.blockPosition()).inflate(4.0))) {
				if (this.distanceTo(player) <= 4.0 && canSeeTarget(player)) {
					hitAnyPlayer = true;
					if (player.hurt(this.damageSources().mobAttack(this), 5.0F))
						if (player instanceof ServerPlayer serverPlayer)
							if (!serverPlayer.fireImmune() && !this.entityData.get(DATA_DEALT_DAMAGE)) {
								this.entityData.set(DATA_DEALT_DAMAGE, true);
								if (!hasGoldenKill(serverPlayer)) this.playSound(RPGSounds.GOLDEN_TOKEN_FAIL.get(), 2.0F, 1.0F);
							}
					Vec3 knockback = player.position()
							.subtract(this.position())
							.normalize()
							.scale(2.0)
							.add(0, 0.5, 0);
					player.setDeltaMovement(knockback);

					// Синхронизируем откидывание с клиентом
					if (player instanceof ServerPlayer serverPlayer) {
						ModMessages.sendToPlayer(new SyncEntityMotionPacket(
								serverPlayer.getId(),
								knockback.x,
								knockback.y,
								knockback.z
						), serverPlayer);
					}
				}
			}

			// Если не попали ни в одного видимого игрока, прерываем атаку
			if (!hitAnyPlayer && attackTimer > 10) {
				setState(State.IDLE);
				setAttackTimer(0);
				return;
			}
		} else if (attackTimer >= 20) {
			setState(State.IDLE);
			setAttackTimer(0);
		}
	}

	private void createFireProjectile(LivingEntity target) {
		if (target == null || !canSeeTarget(target)) {
			return;
		}

		Vec3 direction = target.position()
				.add(0, target.getBbHeight() * 0.5, 0)
				.subtract(this.position().add(0, 0.5, 0))
				.normalize();

		FireProjectileData projectile = new FireProjectileData(
				this.position().add(direction.x(), 0.5, direction.z()),
				direction.scale(1.0),
				this.level().getGameTime()
		);

		fireProjectiles.add(projectile);

		if (this.level() instanceof ServerLevel serverLevel) {
			serverLevel.sendParticles(ParticleTypes.FLAME,
					projectile.position.x, projectile.position.y, projectile.position.z,
					3, 0.1, 0.1, 0.1, 0.01);
		}
	}

	public void dealtDamage(ServerPlayer player) {
			if (!this.entityData.get(DATA_DEALT_DAMAGE)) {
				if (!hasGoldenKill(player)) this.playSound(RPGSounds.GOLDEN_TOKEN_FAIL.get(), 2.0F, 1.0F);
			}
		this.entityData.set(DATA_DEALT_DAMAGE, true);
	}

	private void updateFireProjectiles() {
		if (this.level() instanceof ServerLevel level) {
			Iterator<FireProjectileData> iterator = fireProjectiles.iterator();
			while (iterator.hasNext()) {
				FireProjectileData projectile = iterator.next();

				if (this.level().getGameTime() - projectile.spawnTime > 20) {
					iterator.remove();
					if (this.level() instanceof ServerLevel serverLevel) {
						serverLevel.sendParticles(ParticleTypes.SMOKE,
								projectile.position.x, projectile.position.y, projectile.position.z,
								3, 0.2, 0.2, 0.2, 0.02);
					}
					continue;
				}

				if (checkWaterContact(level, projectile.position)) {
					// Эффект шипения в воде
					level.sendParticles(ParticleTypes.SMOKE,
							projectile.position.x, projectile.position.y, projectile.position.z,
							5, 0.2, 0.2, 0.2, 0.05);
					level.sendParticles(ParticleTypes.BUBBLE,
							projectile.position.x, projectile.position.y, projectile.position.z,
							3, 0.1, 0.1, 0.1, 0.1);
					level.playSound(null,
							projectile.position.x, projectile.position.y, projectile.position.z,
							RPGSounds.EMBER_GEM_EXTINGUISH.get(), SoundSource.NEUTRAL,
							0.3F, 1.0F);
					iterator.remove();
					continue;
				}

				projectile.position = projectile.position.add(projectile.velocity);

				if (checkCollisions(level, projectile)) {
					iterator.remove();
				}


				level.sendParticles(ParticleTypes.FLAME,
						projectile.position.x, projectile.position.y, projectile.position.z,
						1, 0.1, 0.1, 0.1, 0.01);
				level.sendParticles(ParticleTypes.SMOKE,
						projectile.position.x, projectile.position.y, projectile.position.z,
						1, 0.05, 0.05, 0.05, 0.005);
			}
		}
	}

	// Проверка контакта с водой
	private boolean checkWaterContact(Level level, Vec3 position) {
		BlockPos pos = new BlockPos(
				(int) Math.floor(position.x),
				(int) Math.floor(position.y),
				(int) Math.floor(position.z)
		);

		// Проверяем блок жидкости
		FluidState fluidState = level.getFluidState(pos);
		if (fluidState.is(FluidTags.WATER)) {
			return true;
		}

		// Проверяем соседние блоки для точности
		for (int dx = -1; dx <= 1; dx++) {
			for (int dy = -1; dy <= 1; dy++) {
				for (int dz = -1; dz <= 1; dz++) {
					BlockPos checkPos = pos.offset(dx, dy, dz);
					FluidState nearbyFluid = level.getFluidState(checkPos);
					if (nearbyFluid.is(FluidTags.WATER)) {
						double distance = position.distanceTo(
								new Vec3(checkPos.getX() + 0.5, checkPos.getY() + 0.5, checkPos.getZ() + 0.5)
						);
						if (distance < 1.0) {
							return true;
						}
					}
				}
			}
		}

		return false;
	}

	// Проверка столкновений
	private boolean checkCollisions(Level level, FireProjectileData projectile) {
		// Проверка столкновения с блоками
		Vec3 startPos = projectile.position.subtract(projectile.velocity);
		Vec3 endPos = projectile.position.add(projectile.velocity);

		BlockHitResult blockHit = level.clip(new ClipContext(
				startPos, endPos,
				ClipContext.Block.COLLIDER,
				ClipContext.Fluid.ANY, // Учитываем жидкости для лучшей детекции
				null
		));

		if (blockHit.getType() != HitResult.Type.MISS) {
			BlockPos hitPos = blockHit.getBlockPos();
			BlockState hitState = level.getBlockState(hitPos);

			// Проверяем, не попали ли в воду (на всякий случай)
			if (level.getFluidState(hitPos).is(FluidTags.WATER)) {
				// Эффект шипения
				level.playSound(null, hitPos, RPGSounds.EMBER_GEM_EXTINGUISH.get(),
						SoundSource.BLOCKS, 0.5F, 1.0F);

				if (level instanceof ServerLevel serverLevel) {
					serverLevel.sendParticles(ParticleTypes.SMOKE,
							hitPos.getX() + 0.5, hitPos.getY() + 0.5, hitPos.getZ() + 0.5,
							10, 0.3, 0.3, 0.3, 0.05);
				}
				return true;
			}

			// Проверяем, является ли блок горючим
			if (hitState.is(ModBlocks.ARBOR_FUEL_BLOCK.get())) {

				// Заменяем блок земли на огонь
				level.setBlockAndUpdate(hitPos, BaseFireBlock.getState(level, hitPos));

				if (level instanceof ServerLevel serverLevel) {
					// Частицы превращения
					serverLevel.sendParticles(ParticleTypes.FLAME,
							hitPos.getX() + 0.5, hitPos.getY() + 0.5, hitPos.getZ() + 0.5,
							15, 0.5, 0.5, 0.5, 0.05);
					serverLevel.sendParticles(ParticleTypes.SMOKE,
							hitPos.getX() + 0.5, hitPos.getY() + 0.5, hitPos.getZ() + 0.5,
							8, 0.3, 0.3, 0.3, 0.03);
				}
				return true;
			}

			if (hitState.getBlock() instanceof TntBlock tnt) {
				// Создаём мнимый горящий снаряд для взаимодействия с TNT
				if (level instanceof ServerLevel serverLevel) {
					// Создаём фейковый SmallFireball (или другой Projectile, который горит)
					SmallFireball fireProjectile = new SmallFireball(
							serverLevel,
							projectile.position.x,
							projectile.position.y,
							projectile.position.z,
							projectile.velocity.x,
							projectile.velocity.y,
							projectile.velocity.z
					);

					fireProjectile.setOwner(this);

					// Устанавливаем, что снаряд горит
					fireProjectile.setSecondsOnFire(100);

					// Вызываем метод взаимодействия TNT со снарядом
					tnt.onProjectileHit(level, hitState, blockHit, fireProjectile);

					serverLevel.sendParticles(ParticleTypes.FLAME,
							hitPos.getX() + 0.5, hitPos.getY() + 0.5, hitPos.getZ() + 0.5,
							20, 0.5, 0.5, 0.5, 0.05);
					serverLevel.sendParticles(ParticleTypes.SMOKE,
							hitPos.getX() + 0.5, hitPos.getY() + 0.5, hitPos.getZ() + 0.5,
							10, 0.3, 0.3, 0.3, 0.03);
				}
				return true;
			}

			// Для остальных блоков - проверяем, можно ли использовать зажигалку
			// Создаём мнимую зажигалку для проверки
			ItemStack flintAndSteel = new ItemStack(Items.FLINT_AND_STEEL);

			// Проверяем, может ли зажигалка быть использована на этом блоке
			// Создаём контекст для использования предмета
			if (level instanceof ServerLevel serverLevel) {
				// Создаём фейкового игрока
				FakePlayer fakePlayer = FakePlayerFactory.get(serverLevel, new GameProfile(UUID.randomUUID(), "FakePlayer"));

				// Устанавливаем позицию фейкового игрока в точку удара
				fakePlayer.setPos(hitPos.getX(), hitPos.getY(), hitPos.getZ());

				// Устанавливаем правильное вращение для контекста
				fakePlayer.setYRot(blockHit.getDirection().toYRot());
				fakePlayer.setXRot((float) Math.toDegrees(blockHit.getDirection().toYRot()));

				// Даём фейковому игроку зажигалку в руку
				fakePlayer.setItemInHand(InteractionHand.MAIN_HAND, flintAndSteel.copy());

				// Создаём UseOnContext для проверки использования зажигалки
				UseOnContext context = new UseOnContext(fakePlayer, InteractionHand.MAIN_HAND, blockHit);

				// Проверяем, можно ли использовать зажигалку на блоке
				InteractionResult useResult = InteractionResult.PASS;
				if (flintAndSteel.getItem() instanceof FlintAndSteelItem flintAndSteelItem) {
					useResult = flintAndSteelItem.useOn(context);
				}

				// Очищаем руку фейкового игрока
				fakePlayer.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);

				// Если зажигалка может быть использована (вернула SUCCESS или CONSUME),
				// то не спавним огонь на соседнем блоке
				if (useResult.consumesAction()) {

					serverLevel.sendParticles(ParticleTypes.FLAME,
							hitPos.getX() + 0.5, hitPos.getY() + 0.5, hitPos.getZ() + 0.5,
							10, 0.5, 0.5, 0.5, 0.05);
					return true;
				}
			}

			// Если зажигалка не может быть использована, пробуем поставить огонь на соседнем блоке
			BlockPos firePos = hitPos.relative(blockHit.getDirection());

			if (BaseFireBlock.canBePlacedAt(level, firePos, Direction.UP)) {
				level.setBlockAndUpdate(firePos, BaseFireBlock.getState(level, firePos));

				if (level instanceof ServerLevel serverLevel) {
					serverLevel.sendParticles(ParticleTypes.FLAME,
							hitPos.getX() + 0.5, hitPos.getY() + 0.5, hitPos.getZ() + 0.5,
							10, 0.5, 0.5, 0.5, 0.05);
				}
			}
			return true;
		}

		// Проверка столкновения с существами
		List<Entity> entities = level.getEntities(null,
				new net.minecraft.world.phys.AABB(startPos, endPos).inflate(0.3));

		for (Entity entity : entities) {
			SmallFireball fakeFireball = new SmallFireball(
					level,
					projectile.position.x,
					projectile.position.y,
					projectile.position.z,
					projectile.velocity.x,
					projectile.velocity.y,
					projectile.velocity.z
			);
			fakeFireball.setOwner(this);
			fakeFireball.setSecondsOnFire(1);
			if (entity instanceof LivingEntity livingEntity &&
					entity != this && !entity.fireImmune()) {

				// Поджигаем существо на 5 секунд (100 тиков)
				livingEntity.setSecondsOnFire(8);
				if (entity.hurt(this.damageSources().fireball(fakeFireball, this), 1.0F)) {
					if (entity instanceof ServerPlayer player)
						if (!player.fireImmune() && !this.entityData.get(DATA_DEALT_DAMAGE)) {
							this.entityData.set(DATA_DEALT_DAMAGE, true);
							if (!hasGoldenKill(player)) this.playSound(RPGSounds.GOLDEN_TOKEN_FAIL.get(), 2.0F, 1.0F);
						}
				}
				return true;
			}
		}

		return false;
	}

	private void spawnExtinguishSmokeCircleAnimated() {
		if (this.level().isClientSide) return;

		ServerLevel serverLevel = (ServerLevel) this.level();
		double centerX = this.getX();
		double centerY = this.getY() + 1.0;
		double centerZ = this.getZ();

		int particleCount = 24;
		double radius = 0.0;
		double maxRadius = 2.0;

		for (int wave = 0; wave < 3; wave++) {
			double currentRadius = radius + (maxRadius * wave / 3.0);

			for (int i = 0; i < particleCount; i++) {
				double angle = 2 * Math.PI * i / particleCount;
				double x = centerX + currentRadius * Math.cos(angle);
				double z = centerZ + currentRadius * Math.sin(angle);

				double dirX = Math.cos(angle) * 0.1;
				double dirZ = Math.sin(angle) * 0.1;

				serverLevel.sendParticles(
						ParticleTypes.CAMPFIRE_COSY_SMOKE,
						x,
						centerY + 0.3 + (wave * 0.2),
						z,
						2,
						0.05,
						0.15,
						0.05,
						0.01 + (wave * 0.01)
				);
			}
		}

		serverLevel.sendParticles(
				ParticleTypes.CAMPFIRE_COSY_SMOKE,
				centerX,
				centerY + 0.5,
				centerZ,
				15,
				0.4,
				0.6,
				0.4,
				0.03
		);
	}

	public State getState() {
		return State.values()[this.entityData.get(DATA_STATE)];
	}

	public void setState(State state) {
		if (!this.level().isClientSide) {
			this.entityData.set(DATA_STATE, state.ordinal());
		}
	}

	public int getAttackType() {
		return this.entityData.get(DATA_ATTACK_TYPE);
	}

	public void setAttackType(int type) {
		if (!this.level().isClientSide) {
			this.entityData.set(DATA_ATTACK_TYPE, type);
		}
	}

	public int getExtinguishTime() {
		return this.entityData.get(DATA_EXTINGUISH_TIME);
	}

	public void setExtinguishTime(int time) {
		if (!this.level().isClientSide) {
			this.entityData.set(DATA_EXTINGUISH_TIME, time);
		}
	}

	public int getAttackTimer() {
		return this.entityData.get(DATA_ATTACK_TIMER);
	}

	public void setAttackTimer(int timer) {
		if (!this.level().isClientSide) {
			this.entityData.set(DATA_ATTACK_TIMER, timer);
		}
	}

	public int getTongueAnimationTime() {
		return this.entityData.get(DATA_TONGUE_ANIMATION_TIME);
	}

	public void setTongueAnimationTime(int time) {
		if (!this.level().isClientSide) {
			this.entityData.set(DATA_TONGUE_ANIMATION_TIME, time);
		}
	}

	public OptionalInt getTongueTargetId() {
		return this.entityData.get(DATA_TONGUE_TARGET_ID);
	}

	public void setTongueTarget(Entity entity) {
		if (!this.level().isClientSide) {
			this.entityData.set(DATA_TONGUE_TARGET_ID, OptionalInt.of(entity.getId()));
			setForcedLookTarget(entity.position());
		}
	}

	public void clearTongueTarget() {
		if (!this.level().isClientSide) {
			this.entityData.set(DATA_TONGUE_TARGET_ID, OptionalInt.empty());
		}
	}

	public void setSpitDirection(Vec3 direction) {
		if (!this.level().isClientSide) {
			this.entityData.set(DATA_SPIT_DIRECTION_X, (float) direction.x);
			this.entityData.set(DATA_SPIT_DIRECTION_Y, (float) direction.y);
			this.entityData.set(DATA_SPIT_DIRECTION_Z, (float) direction.z);
		}
	}

	public Vec3 getSpitDirection() {
		return new Vec3(
				this.entityData.get(DATA_SPIT_DIRECTION_X),
				this.entityData.get(DATA_SPIT_DIRECTION_Y),
				this.entityData.get(DATA_SPIT_DIRECTION_Z)
		);
	}

	public void setPullDirection(Vec3 direction) {
		if (!this.level().isClientSide) {
			this.entityData.set(DATA_PULL_DIRECTION_X, (float) direction.x);
			this.entityData.set(DATA_PULL_DIRECTION_Y, (float) direction.y);
			this.entityData.set(DATA_PULL_DIRECTION_Z, (float) direction.z);
		}
	}

	public Vec3 getPullDirection() {
		return new Vec3(
				this.entityData.get(DATA_PULL_DIRECTION_X),
				this.entityData.get(DATA_PULL_DIRECTION_Y),
				this.entityData.get(DATA_PULL_DIRECTION_Z)
		);
	}

	public boolean hasItemInMouth() {
		return this.entityData.get(DATA_HAS_ITEM_IN_MOUTH);
	}

	public void setHasItemInMouth(boolean hasItem) {
		if (!this.level().isClientSide) {
			this.entityData.set(DATA_HAS_ITEM_IN_MOUTH, hasItem);
		}
	}

	public void setForcedLookTarget(Vec3 target) {
		if (target != null) {
			this.getLookControl().setLookAt(target.x, target.y, target.z, 1800F, 1800F);
		}
	}

	public Vec3 getForcedLookTarget() {
		return new Vec3(this.getLookControl().getWantedX(), this.getLookControl().getWantedY(), this.getLookControl().getWantedZ());
	}

	public boolean shouldForceLook() {
		return getState() == State.ATTACKING && getAttackType() == ATTACK_TONGUE && getTongueTargetId().isPresent();
	}

	@Nullable
	public Entity getTongueTargetEntity() {
		OptionalInt targetId = getTongueTargetId();
		return targetId.isPresent() ? this.level().getEntity(targetId.getAsInt()) : null;
	}

	private void syncToClients() {
		if (!this.level().isClientSide && this.level() instanceof ServerLevel serverLevel) {
			for (Player player : serverLevel.players()) {
				if (player.distanceToSqr(this) < 64 * 64) {
					ModMessages.sendToPlayer(new SyncRazorleafDataPacket(
							this.getId(),
							getState().ordinal(),
							getAttackType(),
							getAttackTimer(),
							getTongueAnimationTime(),
							getSpitDirection(),
							getPullDirection(),
							hasItemInMouth()
					), (ServerPlayer) player);
				}
			}
		}
	}

	@Override
	public boolean fireImmune() {
		return true;
	}

	@Override
	public void setTarget(@javax.annotation.Nullable LivingEntity pTarget) {
		if (pTarget != null)
			setForcedLookTarget(pTarget.position());
		super.setTarget(pTarget);
	}

	@Override
	public boolean isInvulnerableTo(DamageSource source) {
		if ((source.is(DamageTypes.HOT_FLOOR)
				|| source.is(DamageTypes.LAVA)
				|| source.is(DamageTypes.ON_FIRE)
				|| source.is(DamageTypes.IN_FIRE)
				|| source.is(DamageTypes.FIREBALL)
		)) {
			return true;
		}
		return super.isInvulnerableTo(source);
	}

	@Override
	public boolean hurt(DamageSource source, float amount) {
		if (source == this.damageSources().genericKill()
				|| getState() == State.EXTINGUISHED
				|| source.isCreativePlayer()
				|| this.level().getDifficulty() == Difficulty.PEACEFUL
		) {
			return super.hurt(source, amount);
		}
		return false;
	}

	@Override
	public boolean isPushable() {
		return false;
	}

	@Override
	public void knockback(double strength, double x, double z) {
	}

	@Override
	public void move(MoverType type, Vec3 pos) {
	}

	@Override
	protected void checkFallDamage(double y, boolean onGround, BlockState state, BlockPos pos) {
	}

	@Override
	public void setSecondsOnFire(int pSeconds) {
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource damageSource) {
		return RPGSounds.RAZORLEAF_HURT.get();
	}

	@Override
	protected SoundEvent getDeathSound() {
		return RPGSounds.RAZORLEAF_DEATH.get();
	}

	@Override
	public void addAdditionalSaveData(CompoundTag compound) {
		super.addAdditionalSaveData(compound);
		compound.putInt("State", getState().ordinal());
		compound.putInt("AttackType", getAttackType());
		compound.putInt("ExtinguishTime", getExtinguishTime());
		compound.putInt("AttackTimer", getAttackTimer());
		compound.putInt("TongueAnimationTime", getTongueAnimationTime());
		compound.putInt("RandomSeed", randomSeed);
		getTongueTargetId().ifPresent(id -> compound.putInt("TongueTarget", id));
		if (!heldItem.isEmpty()) {
			compound.put("HeldItem", heldItem.save(new CompoundTag()));
		}
		compound.putBoolean("HasItemInMouth", hasItemInMouth());
		compound.putBoolean("Dealt damage", this.entityData.get(DATA_DEALT_DAMAGE));
		compound.putBoolean("CanSeeTarget", this.entityData.get(DATA_CAN_SEE_TARGET));
	}

	@Override
	public void readAdditionalSaveData(CompoundTag compound) {
		super.readAdditionalSaveData(compound);
		setState(State.values()[compound.getInt("State")]);
		setAttackType(compound.getInt("AttackType"));
		setExtinguishTime(compound.getInt("ExtinguishTime"));
		setAttackTimer(compound.getInt("AttackTimer"));
		setTongueAnimationTime(compound.getInt("TongueAnimationTime"));
		this.randomSeed = compound.getInt("RandomSeed");
		this.entityData.set(DATA_RANDOM_SEED, randomSeed);
		if (compound.contains("TongueTarget")) {
			this.entityData.set(DATA_TONGUE_TARGET_ID, OptionalInt.of(compound.getInt("TongueTarget")));
		}
		if (compound.contains("HeldItem")) {
			heldItem = ItemStack.of(compound.getCompound("HeldItem"));
		}
		setHasItemInMouth(compound.getBoolean("HasItemInMouth"));
		this.entityData.set(DATA_DEALT_DAMAGE, compound.getBoolean("Dealt damage"));
		this.entityData.set(DATA_CAN_SEE_TARGET, compound.getBoolean("CanSeeTarget"));
	}

	@Override
	public void die(@NotNull DamageSource damageSource) {
		if (damageSource.getEntity() instanceof ServerPlayer player && player.getLastDamageSource() == null && !this.entityData.get(DATA_DEALT_DAMAGE))
			ModAdvancements.GOLDEN_KILL_RAZORLEAF.trigger(player);
		super.die(damageSource);
	}

	public float getCurrentPetal1XRot() {
		return currentPetal1XRot;
	}

	public void setCurrentPetal1XRot(float value) {
		this.currentPetal1XRot = value;
	}

	public float getCurrentPetal1ZRot() {
		return currentPetal1ZRot;
	}

	public void setCurrentPetal1ZRot(float value) {
		this.currentPetal1ZRot = value;
	}

	public float getCurrentPetal2XRot() {
		return currentPetal2XRot;
	}

	public void setCurrentPetal2XRot(float value) {
		this.currentPetal2XRot = value;
	}

	public float getCurrentPetal2ZRot() {
		return currentPetal2ZRot;
	}

	public void setCurrentPetal2ZRot(float value) {
		this.currentPetal2ZRot = value;
	}

	public float getCurrentPetal3XRot() {
		return currentPetal3XRot;
	}

	public void setCurrentPetal3XRot(float value) {
		this.currentPetal3XRot = value;
	}

	public float getCurrentPetal3ZRot() {
		return currentPetal3ZRot;
	}

	public void setCurrentPetal3ZRot(float value) {
		this.currentPetal3ZRot = value;
	}

	public float getCurrentPetal4XRot() {
		return currentPetal4XRot;
	}

	public void setCurrentPetal4XRot(float value) {
		this.currentPetal4XRot = value;
	}

	public float getCurrentPetal4ZRot() {
		return currentPetal4ZRot;
	}

	public void setCurrentPetal4ZRot(float value) {
		this.currentPetal4ZRot = value;
	}

	public float getCurrentStemYRot() {
		return currentStemYRot;
	}

	public void setCurrentStemYRot(float value) {
		this.currentStemYRot = value;
	}

	public float getCurrentLeaves1XRot() {
		return currentLeaves1XRot;
	}

	public void setCurrentLeaves1XRot(float value) {
		this.currentLeaves1XRot = value;
	}

	public float getCurrentLeaves2XRot() {
		return currentLeaves2XRot;
	}

	public void setCurrentLeaves2XRot(float value) {
		this.currentLeaves2XRot = value;
	}

	public float getCurrentLeaves3XRot() {
		return currentLeaves3XRot;
	}

	public void setCurrentLeaves3XRot(float value) {
		this.currentLeaves3XRot = value;
	}

	public float getCurrentLeaf1ZRot() {
		return currentLeaf1ZRot;
	}

	public void setCurrentLeaf1ZRot(float value) {
		this.currentLeaf1ZRot = value;
	}

	public float getCurrentLeaf2ZRot() {
		return currentLeaf2ZRot;
	}

	public void setCurrentLeaf2ZRot(float value) {
		this.currentLeaf2ZRot = value;
	}

	public float getCurrentLeaf3ZRot() {
		return currentLeaf3ZRot;
	}

	public void setCurrentLeaf3ZRot(float value) {
		this.currentLeaf3ZRot = value;
	}

	public float getCurrentLeaf4ZRot() {
		return currentLeaf4ZRot;
	}

	public void setCurrentLeaf4ZRot(float value) {
		this.currentLeaf4ZRot = value;
	}

	public float getCurrentLeaf5ZRot() {
		return currentLeaf5ZRot;
	}

	public void setCurrentLeaf5ZRot(float value) {
		this.currentLeaf5ZRot = value;
	}

	public float getCurrentLeaf6ZRot() {
		return currentLeaf6ZRot;
	}

	public void setCurrentLeaf6ZRot(float value) {
		this.currentLeaf6ZRot = value;
	}

	public float getCurrentTongueYRot() {
		return currentTongueYRot;
	}

	public void setCurrentTongueYRot(float value) {
		this.currentTongueYRot = value;
	}

	public float getCurrentTongueZRot() {
		return currentTongueZRot;
	}

	public void setCurrentTongueZRot(float value) {
		this.currentTongueZRot = value;
	}

	public float getCurrentTongueXRot() {
		return currentTongueXRot;
	}

	public void setCurrentTongueXRot(float value) {
		this.currentTongueXRot = value;
	}

	public float getCurrentTongueExtension() {
		return currentTongueExtension;
	}

	public void setCurrentTongueExtension(float value) {
		this.currentTongueExtension = value;
	}

	public float getCurrentHeadXRot() {
		return currentHeadXRot;
	}

	public void setCurrentHeadXRot(float value) {
		this.currentHeadXRot = value;
	}

	public float getTargetPetal1XRot() {
		return targetPetal1XRot;
	}

	public void setTargetPetal1XRot(float value) {
		this.targetPetal1XRot = value;
	}

	public float getTargetPetal1ZRot() {
		return targetPetal1ZRot;
	}

	public void setTargetPetal1ZRot(float value) {
		this.targetPetal1ZRot = value;
	}

	public float getTargetPetal2XRot() {
		return targetPetal2XRot;
	}

	public void setTargetPetal2XRot(float value) {
		this.targetPetal2XRot = value;
	}

	public float getTargetPetal2ZRot() {
		return targetPetal2ZRot;
	}

	public void setTargetPetal2ZRot(float value) {
		this.targetPetal2ZRot = value;
	}

	public float getTargetPetal3XRot() {
		return targetPetal3XRot;
	}

	public void setTargetPetal3XRot(float value) {
		this.targetPetal3XRot = value;
	}

	public float getTargetPetal3ZRot() {
		return targetPetal3ZRot;
	}

	public void setTargetPetal3ZRot(float value) {
		this.targetPetal3ZRot = value;
	}

	public float getTargetPetal4XRot() {
		return targetPetal4XRot;
	}

	public void setTargetPetal4XRot(float value) {
		this.targetPetal4XRot = value;
	}

	public float getTargetPetal4ZRot() {
		return targetPetal4ZRot;
	}

	public void setTargetPetal4ZRot(float value) {
		this.targetPetal4ZRot = value;
	}

	public float getTargetStemYRot() {
		return targetStemYRot;
	}

	public void setTargetStemYRot(float value) {
		this.targetStemYRot = value;
	}

	public float getTargetLeaves1XRot() {
		return targetLeaves1XRot;
	}

	public void setTargetLeaves1XRot(float value) {
		this.targetLeaves1XRot = value;
	}

	public float getTargetLeaves2XRot() {
		return targetLeaves2XRot;
	}

	public void setTargetLeaves2XRot(float value) {
		this.targetLeaves2XRot = value;
	}

	public float getTargetLeaves3XRot() {
		return targetLeaves3XRot;
	}

	public void setTargetLeaves3XRot(float value) {
		this.targetLeaves3XRot = value;
	}

	public float getTargetLeaf1ZRot() {
		return targetLeaf1ZRot;
	}

	public void setTargetLeaf1ZRot(float value) {
		this.targetLeaf1ZRot = value;
	}

	public float getTargetLeaf2ZRot() {
		return targetLeaf2ZRot;
	}

	public void setTargetLeaf2ZRot(float value) {
		this.targetLeaf2ZRot = value;
	}

	public float getTargetLeaf3ZRot() {
		return targetLeaf3ZRot;
	}

	public void setTargetLeaf3ZRot(float value) {
		this.targetLeaf3ZRot = value;
	}

	public float getTargetLeaf4ZRot() {
		return targetLeaf4ZRot;
	}

	public void setTargetLeaf4ZRot(float value) {
		this.targetLeaf4ZRot = value;
	}

	public float getTargetLeaf5ZRot() {
		return targetLeaf5ZRot;
	}

	public void setTargetLeaf5ZRot(float value) {
		this.targetLeaf5ZRot = value;
	}

	public float getTargetLeaf6ZRot() {
		return targetLeaf6ZRot;
	}

	public void setTargetLeaf6ZRot(float value) {
		this.targetLeaf6ZRot = value;
	}

	public float getTargetTongueYRot() {
		return targetTongueYRot;
	}

	public void setTargetTongueYRot(float value) {
		this.targetTongueYRot = value;
	}

	public float getTargetTongueZRot() {
		return targetTongueZRot;
	}

	public void setTargetTongueZRot(float value) {
		this.targetTongueZRot = value;
	}

	public float getTargetTongueXRot() {
		return targetTongueXRot;
	}

	public void setTargetTongueXRot(float value) {
		this.targetTongueXRot = value;
	}

	public float getTargetHeadXRot() {
		return targetHeadXRot;
	}

	public void setTargetHeadXRot(float value) {
		this.targetHeadXRot = value;
	}

	public State getPrevState() {
		return prevState;
	}

	public void setPrevState(State value) {
		this.prevState = value;
	}

	public int getPrevAttackType() {
		return prevAttackType;
	}

	public void setPrevAttackType(int value) {
		this.prevAttackType = value;
	}

	private static class FireProjectileData {
		Vec3 position;
		Vec3 velocity;
		long spawnTime;

		FireProjectileData(Vec3 position, Vec3 velocity, long spawnTime) {
			this.position = position;
			this.velocity = velocity;
			this.spawnTime = spawnTime;
		}
	}
}