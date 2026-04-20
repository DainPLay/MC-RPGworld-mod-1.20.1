package net.dainplay.rpgworldmod.entity.custom;

import net.dainplay.rpgworldmod.enchantment.ModEnchantments;
import net.dainplay.rpgworldmod.item.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Vex;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class FriendlyVex extends Vex {
	private static final EntityDataAccessor<Optional<UUID>> DATA_OWNERUUID_ID =
			SynchedEntityData.defineId(FriendlyVex.class, EntityDataSerializers.OPTIONAL_UUID);
	private static final EntityDataAccessor<Integer> DATA_OWNER_TIMER =
			SynchedEntityData.defineId(FriendlyVex.class, EntityDataSerializers.INT);

	private static final int MAX_OWNER_TIMER = 200;
	private static final int TELEPORT_DISTANCE = 40;
	private static final int TELEPORT_DISTANCE_SQR = TELEPORT_DISTANCE * TELEPORT_DISTANCE;
	private static final int FAR_DISTANCE = 45;
	private static final int FAR_DISTANCE_SQR = FAR_DISTANCE * FAR_DISTANCE;
	private static final int OWNER_CHECK_INTERVAL = 20;

	private int ownerCheckTimer = 0;
	private int idleCombatTimer = 0;

	public FriendlyVex(EntityType<? extends Vex> entityType, Level level) {
		super(entityType, level);
		this.xpReward = 0;
	}

	@Override
	protected void defineSynchedData() {
		super.defineSynchedData();
		this.entityData.define(DATA_OWNERUUID_ID, Optional.empty());
		this.entityData.define(DATA_OWNER_TIMER, 0);
	}

	public void setOwnerUUID(@Nullable UUID uuid) {
		this.entityData.set(DATA_OWNERUUID_ID, Optional.ofNullable(uuid));
		resetOwnerTimer();
	}

	@Nullable
	public UUID getOwnerUUID() {
		return this.entityData.get(DATA_OWNERUUID_ID).orElse(null);
	}

	@Nullable
	public LivingEntity getLivingOwner() {
		UUID uuid = getOwnerUUID();
		if (uuid == null) return null;
		if (this.level() instanceof ServerLevel serverLevel) {
			Entity entity = serverLevel.getEntity(uuid);
			if (entity instanceof LivingEntity living) {
				return living;
			}
		}
		return null;
	}

	private void resetOwnerTimer() {
		this.entityData.set(DATA_OWNER_TIMER, 0);
	}

	private int getOwnerTimer() {
		return this.entityData.get(DATA_OWNER_TIMER);
	}

	private void incrementOwnerTimer() {
		int timer = getOwnerTimer();
		if (timer < MAX_OWNER_TIMER) {
			this.entityData.set(DATA_OWNER_TIMER, timer + 1);
		}
	}

	@Override
	public void tick() {
		super.tick();

		if (!this.level().isClientSide) {
			if (++ownerCheckTimer >= OWNER_CHECK_INTERVAL) {
				ownerCheckTimer = 0;
				checkOwnerAndTeleport();
			}

			if (this.getTarget() == null) {
				idleCombatTimer++;
			} else {
				idleCombatTimer = 0;
				resetOwnerTimer();
			}

			checkOwnerTimer();
		}
	}

	private void checkOwnerAndTeleport() {
		LivingEntity owner = getLivingOwner();
		if (owner == null) return;

		double distanceSq = this.distanceToSqr(owner);
		if (distanceSq > TELEPORT_DISTANCE_SQR) {
			teleportToOwner();
			return;
		}

		if (distanceSq > FAR_DISTANCE_SQR || !this.level().dimension().equals(owner.level().dimension())) {
			teleportToOwner();
		}
	}

	private void checkOwnerTimer() {
		LivingEntity owner = getLivingOwner();
		boolean shouldTickTimer = false;

		if (isFollowingOwner()) {
			shouldTickTimer = true;
		}

		if (owner != null && !this.level().dimension().equals(owner.level().dimension())) {
			shouldTickTimer = true;
		}

		if (owner != null && this.distanceToSqr(owner) > FAR_DISTANCE_SQR) {
			this.entityData.set(DATA_OWNER_TIMER, MAX_OWNER_TIMER);
			shouldTickTimer = true;
		}

		if (owner != null && !owner.isAlive()) {
			shouldTickTimer = true;
		}

		if (owner == null && getOwnerUUID() != null) {
			shouldTickTimer = true;
		}

		if (idleCombatTimer >= MAX_OWNER_TIMER) {
			shouldTickTimer = true;
		}

		if (shouldTickTimer) {
			int currentTimer = getOwnerTimer();
			if (currentTimer < MAX_OWNER_TIMER) {
				this.entityData.set(DATA_OWNER_TIMER, currentTimer + 1);
			} else {
				handleOwnerTimerMax();
			}
		} else {
			resetOwnerTimer();
		}
	}

	private void handleOwnerTimerMax() {
		LivingEntity owner = getLivingOwner();
		if (owner != null && owner.isAlive()) {
			if (owner.level().dimension() == Level.NETHER) {
				this.discard();
			} else if (this.level().dimension().equals(owner.level().dimension())) {
				teleportToOwner();
			} else {
				this.discard();
			}
		} else {
			this.discard();
		}
		resetOwnerTimer();
		idleCombatTimer = 0;
	}

	private void teleportToOwner() {
		LivingEntity owner = getLivingOwner();
		if (owner == null || !owner.isAlive()) return;

		if (owner.level().dimension() == Level.NETHER) {
			this.discard();
			return;
		}

		Vec3 ownerPos = owner.position();
		BlockPos targetPos = owner.blockPosition();

		for (int y = 0; y <= 3; y++) {
			for (int x = -1; x <= 1; x++) {
				for (int z = -1; z <= 1; z++) {
					BlockPos checkPos = targetPos.offset(x, y, z);
					if (this.level().isEmptyBlock(checkPos) && this.level().isEmptyBlock(checkPos.above())) {
						this.teleportTo(checkPos.getX() + 0.5, checkPos.getY(), checkPos.getZ() + 0.5);
						this.playSound(SoundEvents.VEX_AMBIENT, 1.0F, 1.0F);
						return;
					}
				}
			}
		}

		this.teleportTo(ownerPos.x(), ownerPos.y() + 1.0, ownerPos.z());
		this.playSound(SoundEvents.VEX_AMBIENT, 1.0F, 1.0F);
	}

	private boolean isFollowingOwner() {
		LivingEntity owner = getLivingOwner();
		if (owner == null) return false;
		return this.getNavigation().getTargetPos() != null &&
				owner.blockPosition().distSqr(this.getNavigation().getTargetPos()) < 9.0;
	}

	@Override
	public void addAdditionalSaveData(CompoundTag tag) {
		super.addAdditionalSaveData(tag);
		if (getOwnerUUID() != null) {
			tag.putUUID("OwnerUUID", getOwnerUUID());
		}
		tag.putInt("OwnerTimer", getOwnerTimer());
	}

	@Override
	public void readAdditionalSaveData(CompoundTag tag) {
		super.readAdditionalSaveData(tag);
		if (tag.hasUUID("OwnerUUID")) {
			setOwnerUUID(tag.getUUID("OwnerUUID"));
		} else {
			setOwnerUUID(null);
		}
		if (tag.contains("OwnerTimer")) {
			this.entityData.set(DATA_OWNER_TIMER, tag.getInt("OwnerTimer"));
		}
	}

	@Override
	public boolean isAlliedTo(Entity pEntity) {
		LivingEntity livingentity = this.getOwner();
		if (pEntity == livingentity) {
			return true;
		}
		if (livingentity != null) {
			return livingentity.isAlliedTo(pEntity);
		}
		return super.isAlliedTo(pEntity) || Objects.equals(this.getOwnerUUID(), pEntity.getUUID());
	}

	@Override
	public boolean isPreventingPlayerRest(Player pPlayer) {
		return false;
	}

	@Override
	protected boolean shouldDespawnInPeaceful() {
		return false;
	}

	@Override
	public void checkDespawn() {
	}

	@Override
	protected void registerGoals() {
		this.goalSelector.removeAllGoals(goal -> true);
		this.targetSelector.removeAllGoals(goal -> true);

		this.goalSelector.addGoal(0, new FloatGoal(this));
		this.goalSelector.addGoal(4, new VexChargeAttackGoal());
		this.goalSelector.addGoal(8, new VexRandomMoveGoal());
		this.goalSelector.addGoal(9, new LookAtPlayerGoal(this, Player.class, 3.0F, 1.0F));
		this.goalSelector.addGoal(10, new LookAtPlayerGoal(this, Mob.class, 8.0F));

		this.targetSelector.addGoal(1, new OwnerHurtTargetGoal(this));
		this.targetSelector.addGoal(2, new OwnerHurtByTargetGoal(this));
		this.targetSelector.addGoal(3, new AttackOwnerEnemiesGoal(this));
	}

	private static class OwnerHurtTargetGoal extends TargetGoal {
		private final FriendlyVex vex;
		private LivingEntity ownerLastHurt;
		private int timestamp;

		public OwnerHurtTargetGoal(FriendlyVex vex) {
			super(vex, false);
			this.vex = vex;
		}

		@Override
		public boolean canUse() {
			LivingEntity owner = vex.getLivingOwner();
			if (owner == null) return false;
			this.ownerLastHurt = owner.getLastHurtMob();
			int i = owner.getLastHurtMobTimestamp();
			if (this.ownerLastHurt == null || i == this.timestamp) return false;
			if (this.ownerLastHurt == owner) return false;
			return vex.canAttack(ownerLastHurt);
		}

		@Override
		public void start() {
			vex.setTarget(ownerLastHurt);
			LivingEntity owner = vex.getLivingOwner();
			if (owner != null) timestamp = owner.getLastHurtMobTimestamp();
			super.start();
		}
	}

	private static class OwnerHurtByTargetGoal extends TargetGoal {
		private final FriendlyVex vex;
		private LivingEntity ownerLastHurtBy;
		private int timestamp;

		public OwnerHurtByTargetGoal(FriendlyVex vex) {
			super(vex, false);
			this.vex = vex;
		}

		@Override
		public boolean canUse() {
			LivingEntity owner = vex.getLivingOwner();
			if (owner == null) return false;
			this.ownerLastHurtBy = owner.getLastHurtByMob();
			int i = owner.getLastHurtByMobTimestamp();
			if (this.ownerLastHurtBy == null || i == this.timestamp) return false;
			return vex.canAttack(ownerLastHurtBy);
		}

		@Override
		public void start() {
			vex.setTarget(ownerLastHurtBy);
			LivingEntity owner = vex.getLivingOwner();
			if (owner != null) timestamp = owner.getLastHurtByMobTimestamp();
			super.start();
		}
	}

	@Override
	@javax.annotation.Nullable
	public ItemStack getPickResult() {
		ItemStack scroll = ModItems.PILLAGER_SCROLL.get().getDefaultInstance();
		scroll.enchant(ModEnchantments.CONJURATION.get(), 1);
		return scroll;
	}

	private static class AttackOwnerEnemiesGoal extends TargetGoal {
		private final FriendlyVex vex;
		private LivingEntity owner;
		private LivingEntity toAttack;

		public AttackOwnerEnemiesGoal(FriendlyVex vex) {
			super(vex, false);
			this.vex = vex;
		}

		@Override
		public boolean canUse() {
			this.owner = vex.getLivingOwner();
			if (owner == null) return false;
			if (owner.distanceToSqr(vex) > 400) return false;

			List<Monster> monsters = vex.level().getEntitiesOfClass(
					Monster.class,
					vex.getBoundingBox().inflate(24.0),
					e -> e.isAlive()
							&& vex.canAttack(e)
							&& !e.isAlliedTo(owner)
							&& !e.is(vex)
							&& !(e instanceof MosquitoSwarm)
							&& !(e instanceof NeutralMob)
							&& !(e instanceof FriendlyVex)
							&& !(e instanceof FriendlyRavager)
			);
			if (!monsters.isEmpty()) {
				toAttack = monsters.get(vex.random.nextInt(monsters.size()));
				return true;
			}

			List<Mob> mobsTargetingOwner = vex.level().getEntitiesOfClass(
					Mob.class,
					vex.getBoundingBox().inflate(24.0),
					e -> e.isAlive() && e.getTarget() == owner && vex.canAttack(e)
			);
			if (!mobsTargetingOwner.isEmpty()) {
				toAttack = mobsTargetingOwner.get(vex.random.nextInt(mobsTargetingOwner.size()));
				return true;
			}

			return false;
		}

		@Override
		public void start() {
			vex.setTarget(toAttack);
			super.start();
		}
	}
}