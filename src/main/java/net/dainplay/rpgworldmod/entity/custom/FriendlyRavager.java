package net.dainplay.rpgworldmod.entity.custom;

import net.dainplay.rpgworldmod.enchantment.ModEnchantments;
import net.dainplay.rpgworldmod.item.ModItems;
import net.dainplay.rpgworldmod.sounds.RPGSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.PlayerRideableJumping;
import net.minecraft.world.entity.Saddleable;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Ravager;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.ForgeEventFactory;

import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.List;

public class FriendlyRavager extends Ravager implements Saddleable, PlayerRideableJumping {
	private static final EntityDataAccessor<Boolean> DATA_DASHING = SynchedEntityData.defineId(FriendlyRavager.class, EntityDataSerializers.BOOLEAN);
	private static final EntityDataAccessor<Boolean> DATA_SADDLE_ID = SynchedEntityData.defineId(FriendlyRavager.class, EntityDataSerializers.BOOLEAN);
	private ItemStack saddleItem = ItemStack.EMPTY;

	private int dashCooldown = 0;
	private float playerJumpPendingScale;
	protected boolean isJumping;

	public FriendlyRavager(EntityType<? extends Ravager> entityType, Level level) {
		super(entityType, level);
		this.setMaxUpStep(1.0F);
		this.setPersistenceRequired();
		this.xpReward = 0;
	}

	@Override
	protected void defineSynchedData() {
		super.defineSynchedData();
		this.entityData.define(DATA_DASHING, false);
		this.entityData.define(DATA_SADDLE_ID, false);
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Monster.createMonsterAttributes()
				.add(Attributes.MAX_HEALTH, 100.0D)
				.add(Attributes.MOVEMENT_SPEED, 0.2D)
				.add(Attributes.KNOCKBACK_RESISTANCE, 0.75D)
				.add(Attributes.ATTACK_DAMAGE, 12.0D)
				.add(Attributes.ATTACK_KNOCKBACK, 1.5D)
				.add(Attributes.FOLLOW_RANGE, 32.0D)
				.add(Attributes.JUMP_STRENGTH, 0.5D);
	}

	@Override
	public boolean removeWhenFarAway(double distanceToClosestPlayer) {
		return false;
	}

	@Override
	public void checkDespawn() {
	}

	@Override
	public boolean shouldDespawnInPeaceful() {
		return false;
	}

	@Override
	public boolean isPersistenceRequired() {
		return true;
	}

	@Override
	public boolean canJoinRaid() {
		return false;
	}

	@Override
	public void applyRaidBuffs(int wave, boolean unused) {
	}

	@Override
	public boolean isPatrolLeader() {
		return false;
	}

	@Override
	public boolean canBeLeader() {
		return false;
	}

	@Override
	public SoundEvent getCelebrateSound() {
		return SoundEvents.RAVAGER_CELEBRATE;
	}

	@Override
	public boolean isPreventingPlayerRest(Player player) {
		return false;
	}

	@Override
	public boolean canBeLeashed(Player player) {
		return true;
	}

	@Override
	public boolean isSaddleable() {
		return this.isAlive() && !this.isBaby();
	}

	@Override
	public void equipSaddle(@Nullable SoundSource soundSource) {
		this.setSaddled(true);
		if (soundSource != null) {
			this.level().playSound(null, this, RPGSounds.FRIENDLY_RAVAGER_EQUIP_SADDLE.get(), soundSource, 0.5F, 1.0F);
		}
	}

	@Override
	public boolean isSaddled() {
		return this.entityData.get(DATA_SADDLE_ID);
	}

	public void setSaddled(boolean saddled) {
		this.entityData.set(DATA_SADDLE_ID, saddled);
	}

	public ItemStack getSaddleItem() {
		return saddleItem;
	}

	public void removeSaddle() {
		this.saddleItem = ItemStack.EMPTY;
	}

	@Nullable
	@Override
	public LivingEntity getControllingPassenger() {
		Entity entity = this.getFirstPassenger();
		if (entity instanceof LivingEntity living && this.isSaddled()) {
			return living;
		}
		return null;
	}

	@Override
	protected void tickRidden(Player player, Vec3 travelVector) {
		super.tickRidden(player, travelVector);
		Vec2 vec2 = this.getRiddenRotation(player);
		this.setRot(vec2.y, vec2.x);
		this.yRotO = this.yBodyRot = this.yHeadRot = this.getYRot();

		if (this.isControlledByLocalInstance()) {
			if (this.onGround()) {
				this.setIsJumping(false);
				if (this.playerJumpPendingScale > 0.0F && !this.isJumping()) {
					this.executeRidersJump(this.playerJumpPendingScale, travelVector);
				}
				this.playerJumpPendingScale = 0.0F;
			}
		}
	}

	@Override
	@javax.annotation.Nullable
	public ItemStack getPickResult() {
		ItemStack scroll = ModItems.PILLAGER_SCROLL.get().getDefaultInstance();
		scroll.enchant(ModEnchantments.NECROMANCY.get(), 1);
		return scroll;
	}

	private boolean isValidAttackTarget(LivingEntity target, @Nullable LivingEntity rider) {
		if (!target.isAlive()) return false;
		if (target instanceof FriendlyRavager) return false;
		if (!this.canAttack(target)) return false;

		if (rider != null && target.isAlliedTo(rider)) return false;

		if (target instanceof MosquitoSwarm) return false;

		if (target instanceof NeutralMob neutral) {
			LivingEntity neutralTarget = neutral.getTarget();
			if (neutralTarget == null) return false;
			if (neutralTarget != rider && neutralTarget != this) return false;
		}

		if (target instanceof FriendlyVex vex) {
			LivingEntity vexTarget = vex.getTarget();
			if (vexTarget == null) return false;
			if (vexTarget != rider && vexTarget != this) return false;
		}

		return target instanceof Monster ||
				target instanceof Raider ||
				(target instanceof IronGolem && target.getLastHurtByMob() != null) ||
				(target instanceof AbstractVillager && target.getLastHurtByMob() != null);
	}

	public boolean isJumping() {
		return this.isJumping;
	}

	public void setIsJumping(boolean pJumping) {
		this.isJumping = pJumping;
	}

	protected Vec2 getRiddenRotation(LivingEntity rider) {
		return new Vec2(rider.getXRot() * 0.5F, rider.getYRot());
	}

	@Override
	protected Vec3 getRiddenInput(Player player, Vec3 travelVector) {
		float forward = player.zza;
		float strafe = player.xxa * 0.5F;
		if (forward <= 0.0F) {
			forward *= 0.25F;
		}
		return new Vec3(strafe, 0.0D, forward);
	}

	@Override
	protected float getRiddenSpeed(Player player) {
		return (float) this.getAttributeValue(Attributes.MOVEMENT_SPEED) / 2F;
	}

	@Override
	public double getPassengersRidingOffset() {
		return 2.1D;
	}

	@Override
	protected void positionRider(Entity passenger, Entity.MoveFunction callback) {
		super.positionRider(passenger, callback);
		if (passenger instanceof LivingEntity) {
			((LivingEntity) passenger).yBodyRot = this.yBodyRot;
		}
	}

	@Override
	public void onPlayerJump(int jumpPower) {
		if (this.isSaddled() && this.dashCooldown <= 0 && this.onGround()) {
			if (jumpPower < 0) jumpPower = 0;
			if (jumpPower >= 90) {
				this.playerJumpPendingScale = 1.0F;
			} else {
				this.playerJumpPendingScale = 0.4F + 0.4F * (float) jumpPower / 90.0F;
			}
		}
	}

	@Override
	public boolean canJump() {
		return !this.isSaddled() || this.isVehicle();
	}

	@Override
	public void handleStartJump(int jumpPower) {
		this.playSound(RPGSounds.FRIENDLY_RAVAGER_DASH.get(), 1.0F, 1.0F);
		this.setDashing(true);
	}

	@Override
	public void handleStopJump() {
	}

	protected void executeRidersJump(float jumpScale, Vec3 travelVector) {
		double jumpStrength = this.getAttributeValue(Attributes.JUMP_STRENGTH) * (double) this.getBlockJumpFactor() + (double) this.getJumpBoostPower();
		Vec3 look = this.getLookAngle();
		Vec3 horizontal = new Vec3(look.x, 0.0D, look.z).normalize();
		double dashSpeed = 12F * jumpScale * this.getAttributeValue(Attributes.MOVEMENT_SPEED) * (double) this.getBlockSpeedFactor();
		this.setDeltaMovement(
				this.getDeltaMovement().add(
						horizontal.x * dashSpeed,
						1.4285F * jumpScale * jumpStrength,
						horizontal.z * dashSpeed
				)
		);
		this.dashCooldown = 55;
		this.setDashing(true);
		this.hasImpulse = true;
		net.minecraftforge.common.ForgeHooks.onLivingJump(this);
	}

	public boolean isDashing() {
		return this.entityData.get(DATA_DASHING);
	}

	public void setDashing(boolean dashing) {
		this.entityData.set(DATA_DASHING, dashing);
	}

	public int getDashCooldown() {
		return dashCooldown;
	}

	@Override
	public InteractionResult mobInteract(Player player, InteractionHand hand) {
		ItemStack itemstack = player.getItemInHand(hand);

		if (!this.isVehicle() && !this.isBaby()) {
			if (player.isSecondaryUseActive()) {
				if (this.isSaddled()) {
					if (!this.level().isClientSide) {
						this.setSaddled(false);
						if (!this.saddleItem.isEmpty()) {
							player.addItem(this.saddleItem.copy());
						} else {
							player.addItem(new ItemStack(Items.SADDLE));
						}
						this.saddleItem = ItemStack.EMPTY;
						this.level().playSound(null, this, RPGSounds.FRIENDLY_RAVAGER_UNEQUIP_SADDLE.get(), SoundSource.PLAYERS, 0.5F, 1.0F);
					}
					return InteractionResult.sidedSuccess(this.level().isClientSide);
				}
			} else {
				if (itemstack.is(Items.SADDLE) && this.isSaddleable() && !this.isSaddled()) {
					this.equipSaddle(SoundSource.NEUTRAL);
					if (!player.getAbilities().instabuild) {
						this.saddleItem = itemstack.copy();
						itemstack.shrink(1);
					}
					return InteractionResult.sidedSuccess(this.level().isClientSide);
				}
				if (this.isSaddled()) {
					this.doPlayerRide(player);
					return InteractionResult.sidedSuccess(this.level().isClientSide);
				}
			}
		}
		return super.mobInteract(player, hand);
	}

	protected void doPlayerRide(Player player) {
		if (!this.level().isClientSide) {
			player.setYRot(this.getYRot());
			player.setXRot(this.getXRot());
			player.startRiding(this);
		}
	}

	@Override
	protected void registerGoals() {
		this.goalSelector.removeAllGoals(goal -> true);
		this.targetSelector.removeAllGoals(goal -> true);

		this.goalSelector.addGoal(0, new FloatGoal(this));

		this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.7D) {
			@Override
			public boolean canUse() {
				return !FriendlyRavager.this.isVehicle() && super.canUse();
			}
		});
		this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 6.0F) {
			@Override
			public boolean canUse() {
				return !FriendlyRavager.this.isVehicle() && super.canUse();
			}
		});
		this.goalSelector.addGoal(7, new RandomLookAroundGoal(this) {
			@Override
			public boolean canUse() {
				return !FriendlyRavager.this.isVehicle() && super.canUse();
			}
		});

		this.goalSelector.addGoal(2, new FriendlyRavagerMeleeAttackGoal());

		this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
		this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, LivingEntity.class, 10, true, false, entity -> isValidAttackTarget(entity, null)));
	}

	class FriendlyRavagerMeleeAttackGoal extends MeleeAttackGoal {
		public FriendlyRavagerMeleeAttackGoal() {
			super(FriendlyRavager.this, 1.0D, true);
			this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
		}

		@Override
		public boolean canUse() {
			return !FriendlyRavager.this.isVehicle() && super.canUse();
		}

		@Override
		public boolean canContinueToUse() {
			return !FriendlyRavager.this.isVehicle() && super.canContinueToUse();
		}
	}

	@Override
	public void aiStep() {
		super.aiStep();

		if (!this.level().isClientSide && this.isAlive()) {
			if (ForgeEventFactory.getMobGriefingEvent(this.level(), this)) {
				AABB aabb = this.getBoundingBox().inflate(0.5);

				for (BlockPos blockpos : BlockPos.betweenClosed(Mth.floor(aabb.minX), Mth.floor(aabb.minY), Mth.floor(aabb.minZ), Mth.floor(aabb.maxX), Mth.floor(aabb.maxY), Mth.floor(aabb.maxZ))) {
					BlockState blockstate = this.level().getBlockState(blockpos);
					Block block = blockstate.getBlock();
					if (block instanceof LeavesBlock) {
						this.level().destroyBlock(blockpos, true, this);
					}
				}
			}
		}

		if (this.dashCooldown > 0) {
			this.dashCooldown--;
			if (this.dashCooldown == 0) {
				this.level().playSound(null, this, RPGSounds.FRIENDLY_RAVAGER_DASH_READY.get(), SoundSource.NEUTRAL, 1.0F, 1.0F);
			}
		}

		if (this.isDashing() && (this.onGround() || this.isInWater() || this.isPassenger())) {
			if (this.dashCooldown < 50) {
				this.setDashing(false);
			}
		}

		if (!this.level().isClientSide && this.isVehicle()) {
			LivingEntity controller = this.getControllingPassenger();
			if (controller instanceof Player player) {
				this.attackWhileRidden(player);
			}
		}
	}

	private boolean isInVisionCone(LivingEntity target, double coneAngleDegrees) {
		Vec3 lookVec = this.getViewVector(1.0F);
		Vec3 toTarget = target.position().subtract(this.position()).normalize();
		double dot = lookVec.dot(toTarget);
		double cosHalfAngle = Math.cos(Math.toRadians(coneAngleDegrees / 2.0));
		return dot >= cosHalfAngle;
	}

	private void attackWhileRidden(Player rider) {
		AABB attackBox = this.getBoundingBox().inflate(3.0D, 0.5D, 3.0D);
		List<LivingEntity> targets = this.level().getEntitiesOfClass(LivingEntity.class, attackBox,
				entity -> isValidAttackTarget(entity, rider));

		boolean isDashingNow = this.isDashing() && this.getDeltaMovement().horizontalDistanceSqr() > 0.2D;

		for (LivingEntity target : targets) {
			if (!isDashingNow && this.attackTick <= 0) {
				if (!isInVisionCone(target, 60.0)) {
					continue;
				}
				boolean flag = this.doHurtTarget(target);
				if (flag) {
					this.attackTick = 10;
					this.level().broadcastEntityEvent(this, (byte) 4);
					this.playSound(SoundEvents.RAVAGER_ATTACK, 1.0F, 1.0F);
				}
			}

			if (isDashingNow) {
				for (LivingEntity aoeTarget : targets) {
					this.strongKnockback(aoeTarget);
					aoeTarget.hurt(this.damageSources().mobAttack(this), (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE) * 0.5F);
				}
				if (this.level() instanceof ServerLevel serverLevel) {
					serverLevel.sendParticles(ParticleTypes.POOF, this.getX(), this.getY() + 1.0D, this.getZ(),
							5, 0.5, 0.2, 0.5, 0.0);
				}
				break;
			}
		}
	}

	private void strongKnockback(Entity entity) {
		double dx = entity.getX() - this.getX();
		double dz = entity.getZ() - this.getZ();
		double dist = Math.max(dx * dx + dz * dz, 0.001D);
		entity.push(dx / dist * 4.0D, 0.2D, dz / dist * 4.0D);
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return SoundEvents.RAVAGER_AMBIENT;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource source) {
		return SoundEvents.RAVAGER_HURT;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return SoundEvents.RAVAGER_DEATH;
	}

	@Override
	public void addAdditionalSaveData(CompoundTag tag) {
		super.addAdditionalSaveData(tag);
		tag.putBoolean("Saddled", this.isSaddled());
		if (!this.saddleItem.isEmpty()) {
			tag.put("SaddleItem", this.saddleItem.save(new CompoundTag()));
		}
		tag.putInt("DashCooldown", this.dashCooldown);
	}

	@Override
	public void readAdditionalSaveData(CompoundTag tag) {
		super.readAdditionalSaveData(tag);
		this.setSaddled(tag.getBoolean("Saddled"));
		if (tag.contains("SaddleItem", 10)) {
			this.saddleItem = ItemStack.of(tag.getCompound("SaddleItem"));
		}
		this.dashCooldown = tag.getInt("DashCooldown");
	}

	@Override
	public boolean hurt(DamageSource source, float amount) {
		return super.hurt(source, amount);
	}

	@Override
	public boolean isAttackable() {
		return true;
	}

	@Override
	public boolean canBeCollidedWith() {
		return this.isAlive();
	}

	@Override
	protected void updateControlFlags() {
	}

	@Override
	protected void dropCustomDeathLoot(DamageSource pSource, int pLooting, boolean pRecentlyHit) {
		super.dropCustomDeathLoot(pSource, pLooting, pRecentlyHit);
		if (!this.saddleItem.isEmpty()) {
			this.spawnAtLocation(this.saddleItem);
			this.saddleItem = ItemStack.EMPTY;
		}
	}

	public int getJumpCooldown() {
		return this.dashCooldown;
	}
}