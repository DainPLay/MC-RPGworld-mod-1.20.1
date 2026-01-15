package net.dainplay.rpgworldmod.entity.custom;

import net.dainplay.rpgworldmod.RPGworldMod;
import net.dainplay.rpgworldmod.biome.BiomeRegistry;
import net.dainplay.rpgworldmod.block.ModBlocks;
import net.dainplay.rpgworldmod.data.tags.ModAdvancements;
import net.dainplay.rpgworldmod.effect.ModEffects;
import net.dainplay.rpgworldmod.entity.ModEntities;
import net.dainplay.rpgworldmod.item.ModItems;
import net.dainplay.rpgworldmod.particle.ModParticles;
import net.dainplay.rpgworldmod.sounds.RPGSounds;
import net.dainplay.rpgworldmod.util.ModTags;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.OldUsersConverter;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.MoveTowardsRestrictionGoal;
import net.minecraft.world.entity.ai.goal.MoveTowardsTargetGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomFlyingGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.MultifaceBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.Team;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class MosquitoSwarm extends Monster implements OwnableEntity {
	private static final EntityDataAccessor<Byte> DATA_FLAGS_ID = SynchedEntityData.defineId(MosquitoSwarm.class, EntityDataSerializers.BYTE);
	private static final EntityDataAccessor<Boolean> DATA_DEALT_DAMAGE = SynchedEntityData.defineId(MosquitoSwarm.class, EntityDataSerializers.BOOLEAN);
	private static final EntityDataAccessor<Integer> DATA_SWARM_SIZE = SynchedEntityData.defineId(MosquitoSwarm.class, EntityDataSerializers.INT);
	protected static final EntityDataAccessor<Optional<UUID>> DATA_OWNERUUID_ID = SynchedEntityData.defineId(MosquitoSwarm.class, EntityDataSerializers.OPTIONAL_UUID);
	private static final EntityDataAccessor<Integer> DATA_OWNER_TIMER = SynchedEntityData.defineId(MosquitoSwarm.class, EntityDataSerializers.INT);
	private static final UUID INVULNERABILITY_MODIFIER_UUID = UUID.fromString("d3064e68-58ff-4826-a2f9-cb8880749c6a");
	private static final String INVULNERABILITY_MODIFIER_NAME = "Mosquitos";
	private int transformationCheckTimer = 0;
	private int attackCooldown = 0;
	private static final int MERGE_CHECK_INTERVAL = 10;
	private int mergeCheckTimer = 0;
	private LivingEntity cachedOwner = null;
	private static final int MAX_OWNER_TIMER = 200; // 30 секунд (600 тиков)
	private static final int TELEPORT_DISTANCE_THRESHOLD = 20; // 20 блоков
	private static final int FAR_DISTANCE_THRESHOLD = 45; // 100 блоков
	private int pathCheckTimer = 0;
	private static final int PATH_CHECK_INTERVAL = 5;
	private static final float SPECIAL_EFFECT_CHANCE = 0.1F;

	public MosquitoSwarm(EntityType<? extends MosquitoSwarm> p_32219_, Level p_32220_) {
		super(p_32219_, p_32220_);
		this.moveControl = new FlyingMoveControl(this, 10, true);
		this.applyInvulnerability();
	}

	@Nullable
	public SpawnGroupData finalizeSpawn(ServerLevelAccessor pLevel, DifficultyInstance pDifficulty, MobSpawnType pReason, @Nullable SpawnGroupData pSpawnData, @Nullable CompoundTag pDataTag) {

		RandomSource randomsource = pLevel.getRandom();

		if (pLevel.getDifficulty() == Difficulty.HARD && randomsource.nextFloat() < 0.1F * pDifficulty.getSpecialMultiplier()) {
			this.addEffect(new MobEffectInstance(ModEffects.MOSSIOSIS.get(), 2400, 0));
		}
		if (pLevel.getDifficulty() == Difficulty.HARD && randomsource.nextFloat() < 0.1F * pDifficulty.getSpecialMultiplier()) {
			this.addEffect(new MobEffectInstance(ModEffects.PARALYSIS.get(), 1200, 0));
		}
		return super.finalizeSpawn(pLevel, pDifficulty, pReason, pSpawnData, pDataTag);
	}

	@Override
	public void tick() {
		super.tick();

		if (!this.level().isClientSide) {
			// Проверка пути к цели каждые PATH_CHECK_INTERVAL тиков
			if (++pathCheckTimer >= PATH_CHECK_INTERVAL) {
				pathCheckTimer = 0;
				checkPathToTarget();
			}

			// Проверка таймера владельца
			checkOwnerTimer();

			// Проверка слияния каждые MERGE_CHECK_INTERVAL тиков
			if (++mergeCheckTimer >= MERGE_CHECK_INTERVAL) {
				mergeCheckTimer = 0;
				checkMerge();
			}

			/*if (this.getOwner() != null) {
				this.level().getServer().getPlayerList().broadcastSystemMessage(Component.literal(String.format("Distance to owner: %.1f", this.getOwner().distanceToSqr(this))), false);
				this.level().getServer().getPlayerList().broadcastSystemMessage(Component.literal(String.format("Timer: " + this.entityData.get(DATA_OWNER_TIMER))), false);
			}*/

			// Проверка трансформации каждые 5 тиков
			if (++transformationCheckTimer >= 5) {
				transformationCheckTimer = 0;
				if (this.level() instanceof ServerLevel serverLevel) {
					serverLevel.sendParticles(ModParticles.MOSQUITOS.get(),
							this.getX(), this.getY() + 0.5, this.getZ(),
							5, 0.35f + 0.05f * this.getSize(), 0.35f + 0.05f * this.getSize(), 0.35f + 0.05f * this.getSize(), 0.05);
				}
				checkTransformationConditions();
			}

			if (this.level().dimension() == Level.NETHER) {
				this.playSound(SoundEvents.GENERIC_EXTINGUISH_FIRE, 1.0F, 1.0F);
				if (this.getOwner() == null) this.spawnAtLocation(ModItems.CHITIN_THIMBLE.get());
				this.proceedKill();
			}

			// Проверка атаки на игрока
			if (attackCooldown > 0) {
				attackCooldown--;
			}

			// Проверяем, есть ли цель для атаки и достаточно ли близко
			LivingEntity target = this.getTarget();
			if (target != null && attackCooldown == 0) {
				double distance = this.distanceTo(target);

				// Если очень близко к цели (вплотную), накладываем эффект отравления
				if (distance < 1.5) {
					applyEffect(target);

					if (target instanceof ServerPlayer player)
						if (!this.entityData.get(DATA_DEALT_DAMAGE)) {
							this.entityData.set(DATA_DEALT_DAMAGE, true);
							if (!hasGoldenKill(player)) this.playSound(RPGSounds.GOLDEN_TOKEN_FAIL.get(), 2.0F, 1.0F);
						}
					this.proceedKill();
				}
			}
		}
	}


	public boolean hasGoldenKill(ServerPlayer player) {
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
			if (criterionId.equals("golden_kill_mosquito_swarm")) {
				return true;
			}
		}

		return false;
	}

	// Новый метод для проверки пути к цели
	private void checkPathToTarget() {
		LivingEntity target = this.getTarget();
		LivingEntity owner = this.getOwner();

		// Проверяем путь к текущей цели
		if (target != null) {
			if (isPathBlocked(target.position())) {
				this.setTarget(null); // Сбрасываем цель, если путь заблокирован
			}
		}

		// Проверяем путь к владельцу, если он есть
		if (owner != null && this.getNavigation().getTargetPos() != null) {
			BlockPos targetPos = this.getNavigation().getTargetPos();
			Vec3 targetVec = new Vec3(targetPos.getX() + 0.5, targetPos.getY(), targetPos.getZ() + 0.5);
			if (isPathBlocked(targetVec)) {
				this.getNavigation().stop(); // Останавливаем навигацию к владельцу
			}
		}
	}

	// Проверяет, заблокирован ли путь к указанной позиции
	private boolean isPathBlocked(Vec3 targetPos) {
		Vec3 currentPos = this.position();
		Vec3 direction = targetPos.subtract(currentPos);
		double distance = direction.length();

		if (distance < 1.0) {
			return false; // Цель уже близко
		}

		// Нормализуем направление
		direction = direction.normalize();

		// Проверяем точки вдоль пути
		int steps = (int) Math.ceil(distance);
		for (int i = 1; i <= steps; i++) {
			Vec3 checkPos = currentPos.add(direction.scale(i));
			BlockPos blockPos = BlockPos.containing(checkPos);

			// Проверяем блок в этой позиции
			BlockState state = this.level().getBlockState(blockPos);

			// Проверяем жидкости
			if (!state.getFluidState().isEmpty()) {
				return true;
			}

			// Проверяем огонь
			if (state.is(BlockTags.FIRE)) {
				return true;
			}

			// Проверяем липкие блоки
			if (state.is(ModTags.Blocks.STICKY_FOR_MOSQUITOS)) {
				return true;
			}
		}

		return false;
	}

	// Новый метод для проверки таймера владельца
	private void checkOwnerTimer() {
		LivingEntity owner = this.getOwner();
		boolean shouldTickTimer = false;

		// Условия для тикания таймера:
		// 1. Москиты преследуют владельца (следуют к нему)
		if (this.isFollowingOwner()) {
			shouldTickTimer = true;
		}

		// 2. Владелец в другом измерении
		if (owner != null && !this.level().dimension().equals(owner.level().dimension())) {
			shouldTickTimer = true;
		}

		// 3. Владелец дальше 100 блоков
		if (owner != null && this.distanceToSqr(owner) > FAR_DISTANCE_THRESHOLD * FAR_DISTANCE_THRESHOLD) {
			this.entityData.set(DATA_OWNER_TIMER, MAX_OWNER_TIMER);
			shouldTickTimer = true;
		}

		// 4. Владелец мёртв
		if (owner != null && !owner.isAlive()) {
			shouldTickTimer = true;
		}

		// 5. Владельца нет на сервере
		if (owner == null && this.getOwnerUUID() != null) {
			shouldTickTimer = true;
		}

		// Если условия выполнены, увеличиваем таймер
		if (shouldTickTimer) {
			int currentTimer = this.entityData.get(DATA_OWNER_TIMER);
			if (currentTimer < MAX_OWNER_TIMER) {
				this.entityData.set(DATA_OWNER_TIMER, currentTimer + 1);
			} else {
				// Таймер достиг максимума, обрабатываем
				handleOwnerTimerMax();
			}
		} else {
			// Сбрасываем таймер если условия не выполнены
			this.resetOwnerTimer();
		}
	}

	@Override
	protected boolean shouldDespawnInPeaceful() {
		return this.getOwner() == null;
	}

	/**
	 * Makes the entity despawn if requirements are reached
	 */

	@Override
	public void checkDespawn() {
		if (this.getOwner() == null) {
			super.checkDespawn();
			return;
		}
		if (this.level().getDifficulty() == Difficulty.PEACEFUL && this.shouldDespawnInPeaceful()) {
			this.discard();
		} else {
			this.noActionTime = 0;
		}
	}

	// Проверяет, преследует ли моб владельца
	private boolean isFollowingOwner() {
		LivingEntity owner = this.getOwner();
		if (owner == null) return false;

		// Проверяем, является ли владелец текущей целью навигации
		return this.getNavigation().getTargetPos() != null &&
				owner.blockPosition().distSqr(this.getNavigation().getTargetPos()) < 9.0;
	}

	// Обработка достижения максимума таймера
	private void handleOwnerTimerMax() {
		LivingEntity owner = this.getOwner();

		if (owner != null && owner.isAlive()) {
			// Проверяем, в каком измерении владелец
			if (owner.level().dimension() == Level.NETHER) {
				// Владелец в Незере, убиваем москитов
				this.proceedKill();
			} else if (this.level().dimension().equals(owner.level().dimension())) {
				// Владелец в том же измерении, телепортируемся
				this.teleportToOwner();
			} else {
				// Владелец в другом измерении (но не в Незере), можем попытаться телепортировать
				// Для этого нужно перенести моба в измерение владельца
				this.proceedKill();
			}
		} else {
			// Владельца нет или он мёртв, убиваем москитов
			this.proceedKill();
		}

		// Сбрасываем таймер после обработки
		this.resetOwnerTimer();
	}

	// Сброс таймера владельца
	private void resetOwnerTimer() {
		this.entityData.set(DATA_OWNER_TIMER, 0);
	}

	// Метод телепортации к владельцу
	private void teleportToOwner() {
		LivingEntity owner = this.getOwner();
		if (owner != null && owner.isAlive()) {
			// Проверяем, не в Незере ли владелец
			if (owner.level().dimension() == Level.NETHER) {
				this.proceedKill();
				return;
			}

			// Ищем безопасное место рядом с владельцем
			Vec3 ownerPos = owner.position();
			BlockPos targetPos = owner.blockPosition();

			// Пытаемся найти место для телепортации
			for (int y = 0; y <= 3; y++) {
				for (int x = -1; x <= 1; x++) {
					for (int z = -1; z <= 1; z++) {
						BlockPos checkPos = targetPos.offset(x, y, z);
						if (this.level().isEmptyBlock(checkPos) &&
								this.level().isEmptyBlock(checkPos.above())) {
							// Нашли безопасное место
							this.teleportTo(checkPos.getX() + 0.5, checkPos.getY(), checkPos.getZ() + 0.5);

							// Эффекты телепортации
							if (this.level() instanceof ServerLevel serverLevel) {
								serverLevel.sendParticles(ModParticles.MOSQUITOS.get(),
										this.getX(), this.getY() + 0.5, this.getZ(),
										20, 0.5, 0.5, 0.5, 0.1);
							}
							this.playSound(RPGSounds.MOSQUITO_SWARM_ATTACK.get(), 0.5F, 1.0F);
							return;
						}
					}
				}
			}

			// Если не нашли безопасное место, телепортируемся прямо к владельцу
			this.teleportTo(ownerPos.x(), ownerPos.y() + 1.0, ownerPos.z());
		}
	}

	// Новый метод для проверки слияния с другим мобом
	private void checkMerge() {
		UUID ownerUUID = this.getOwnerUUID();
		if (ownerUUID == null) return;

		// Ищем рядом мобов того же класса с тем же владельцем
		List<MosquitoSwarm> nearbySwarms = this.level().getEntitiesOfClass(
				MosquitoSwarm.class,
				this.getBoundingBox().inflate(6.0),
				swarm -> swarm != this &&
						swarm.getOwnerUUID() != null &&
						swarm.getOwnerUUID().equals(ownerUUID) &&
						swarm.isAlive()
		);

		if (!nearbySwarms.isEmpty()) {
			MosquitoSwarm other = nearbySwarms.get(0); // Берем первого найденного

			// Проверяем, не заблокирован ли путь к другому мобу
			if (isPathBlocked(other.position())) {
				return; // Не сливаемся, если путь заблокирован
			}

			// Летим к другому мобу
			this.getNavigation().moveTo(other, 1.5);

			// Проверяем расстояние для слияния
			if (this.distanceToSqr(other) < 1.0) {
				int totalSize = this.getSize() + other.getSize();
				if (totalSize > 3) totalSize = 3;

				// Создаем новую стаю
				MosquitoSwarm newSwarm = new MosquitoSwarm(ModEntities.MOSQUITO_SWARM.get(), this.level());
				newSwarm.setOwnerUUID(ownerUUID);
				newSwarm.setSize(totalSize);
				newSwarm.moveTo(this.position());
				newSwarm.setTame(this.isTame());

				// Добавляем в мир
				this.level().addFreshEntity(newSwarm);

				// Удаляем старых мобов
				this.proceedKill();
				other.proceedKill();

				// Эффекты слияния
				if (this.level() instanceof ServerLevel serverLevel) {
					serverLevel.sendParticles(ModParticles.MOSQUITOS.get(),
							this.getX(), this.getY() + 0.5, this.getZ(),
							30, 0.5, 0.5, 0.5, 0.1);
				}
				this.playSound(RPGSounds.MOSQUITO_SWARM_ATTACK.get(), 1.0F, 1.0F);
			}
		}
	}

	private void applyEffect(LivingEntity target) {
		if (target.hasEffect(ModEffects.MOSQUITOING.get())) {
			int amp = this.getSize(); //1
			amp += target.getEffect(ModEffects.MOSQUITOING.get()).getAmplifier() + 1; //1+0+1
			amp -= 1; //2-1
			if (amp > 2) amp = 2;
			target.addEffect(new MobEffectInstance(ModEffects.MOSQUITOING.get(), 2401, amp));
		} else {
			target.addEffect(new MobEffectInstance(ModEffects.MOSQUITOING.get(), 2401, this.getSize() - 1));
		}

		for (MobEffectInstance effect : this.getActiveEffects()) {
			target.addEffect(effect);
		}

		// Сохраняем UUID владельца москитов в тег сущности
		UUID ownerUUID = this.getOwnerUUID();
		if (ownerUUID != null) {
			target.getPersistentData().putUUID("MosquitoSwarmOwner", ownerUUID);
		} else {
			// Если москиты дикие, удаляем тег, если он был
			target.getPersistentData().remove("MosquitoSwarmOwner");
		}

		// Визуальные эффекты
		if (this.level() instanceof ServerLevel serverLevel) {
			serverLevel.sendParticles(ModParticles.MOSQUITOS.get(),
					this.getX(), this.getY() + 0.5, this.getZ(),
					20, 0.35f + 0.05f * this.getSize(), 0.35f + 0.05f * this.getSize(), 0.35f + 0.05f * this.getSize(), 0.05);
		}

		// Звук атаки
		this.playSound(RPGSounds.MOSQUITO_SWARM_ATTACK.get(), 1.0F, (this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.0F);
	}

	private void applyInvulnerability() {
		this.setInvulnerable(true);
		AttributeInstance attribute = getAttribute(Attributes.KNOCKBACK_RESISTANCE);
		if (attribute != null) {
			AttributeModifier modifier = new AttributeModifier(INVULNERABILITY_MODIFIER_UUID, INVULNERABILITY_MODIFIER_NAME, 1.0, AttributeModifier.Operation.ADDITION);
			attribute.addTransientModifier(modifier);
		}
		AttributeInstance damageAttribute = getAttribute(Attributes.ARMOR);
		if (damageAttribute != null) {
			AttributeModifier modifier = new AttributeModifier(INVULNERABILITY_MODIFIER_UUID, INVULNERABILITY_MODIFIER_NAME, 20, AttributeModifier.Operation.ADDITION); // a lot of armor
			damageAttribute.addTransientModifier(modifier);
		}
	}

	public int getSize() {
		return this.entityData.get(DATA_SWARM_SIZE);
	}

	@Override
	public EntityDimensions getDimensions(Pose pPose) {
		return super.getDimensions(pPose).scale(0.255F * (float) this.getSize());
	}

	@Override
	public boolean isSensitiveToWater() {
		return true;
	}

	// Класс цели для преследования владельца когда он далеко (высокий приоритет)
	private class FollowOwnerFarGoal extends Goal {
		private final MosquitoSwarm mosquito;
		private LivingEntity owner;
		private final Level level;
		private final double speedModifier;
		private int timeToRecalcPath;

		public FollowOwnerFarGoal(MosquitoSwarm mosquito, double speedModifier) {
			this.mosquito = mosquito;
			this.level = mosquito.level();
			this.speedModifier = speedModifier;
		}

		@Override
		public boolean canUse() {
			LivingEntity owner = mosquito.getOwner();
			if (owner == null) {
				return false;
			}

			// Не преследуем если владелец в другом измерении
			if (!mosquito.level().dimension().equals(owner.level().dimension())) {
				return false;
			}

			// Проверяем, не заблокирован ли путь к владельцу
			if (mosquito.isPathBlocked(owner.position())) {
				return false;
			}

			// Если владелец дальше 20 блоков, это высший приоритет
			if (owner.distanceToSqr(mosquito) < TELEPORT_DISTANCE_THRESHOLD * TELEPORT_DISTANCE_THRESHOLD) {
				return false;
			}

			this.owner = owner;
			return true;
		}

		@Override
		public boolean canContinueToUse() {
			if (owner == null || !owner.isAlive()) {
				return false;
			}

			// Прекращаем если владелец в другом измерении
			if (!mosquito.level().dimension().equals(owner.level().dimension())) {
				return false;
			}

			// Проверяем, не заблокирован ли путь к владельцу
			if (mosquito.isPathBlocked(owner.position())) {
				return false;
			}

			// Продолжаем пока владелец далеко
			return owner.distanceToSqr(mosquito) >= 9.0; // Не останавливаемся пока не подойдем близко
		}

		@Override
		public void start() {
			this.timeToRecalcPath = 0;
		}

		@Override
		public void stop() {
			this.owner = null;
			this.mosquito.getNavigation().stop();
		}

		@Override
		public void tick() {
			this.mosquito.getLookControl().setLookAt(this.owner, 10.0F, (float) this.mosquito.getMaxHeadXRot());
			if (--this.timeToRecalcPath <= 0) {
				this.timeToRecalcPath = 10;
				this.mosquito.getNavigation().moveTo(this.owner, this.speedModifier);
			}
		}

		@Override
		public boolean isInterruptable() {
			return false; // Не прерывается другими целями
		}
	}

	// Класс цели для преследования владельца когда нет цели (низкий приоритет)
	private class FollowOwnerWhenIdleGoal extends Goal {
		private final MosquitoSwarm mosquito;
		private LivingEntity owner;
		private final Level level;
		private final double speedModifier;
		private int timeToRecalcPath;

		public FollowOwnerWhenIdleGoal(MosquitoSwarm mosquito, double speedModifier) {
			this.mosquito = mosquito;
			this.level = mosquito.level();
			this.speedModifier = speedModifier;
		}

		@Override
		public boolean canUse() {
			LivingEntity owner = mosquito.getOwner();
			if (owner == null) {
				return false;
			}

			// Не преследуем если владелец в другом измерении
			if (!mosquito.level().dimension().equals(owner.level().dimension())) {
				return false;
			}

			// Проверяем, не заблокирован ли путь к владельцу
			if (mosquito.isPathBlocked(owner.position())) {
				return false;
			}

			if (mosquito.getTarget() != null) {
				return false;
			}
			if (owner.distanceToSqr(mosquito) < 9.0) {
				return false;
			}
			this.owner = owner;
			return true;
		}

		@Override
		public boolean canContinueToUse() {
			if (mosquito.getTarget() != null) {
				return false;
			}
			if (owner == null || !owner.isAlive()) {
				return false;
			}

			// Прекращаем если владелец в другом измерении
			if (!mosquito.level().dimension().equals(owner.level().dimension())) {
				return false;
			}

			// Проверяем, не заблокирован ли путь к владельцу
			if (mosquito.isPathBlocked(owner.position())) {
				return false;
			}

			return !this.mosquito.getNavigation().isDone() && owner.distanceToSqr(mosquito) > 9.0;
		}

		@Override
		public void start() {
			this.timeToRecalcPath = 0;
		}

		@Override
		public void stop() {
			this.owner = null;
		}

		@Override
		public void tick() {
			this.mosquito.getLookControl().setLookAt(this.owner, 10.0F, (float) this.mosquito.getMaxHeadXRot());
			if (--this.timeToRecalcPath <= 0) {
				this.timeToRecalcPath = 10;
				this.mosquito.getNavigation().moveTo(this.owner, this.speedModifier);
			}
		}
	}

	private boolean canSeeEntity(Level level, Vec3 from, Vec3 to) {
		ClipContext context = new ClipContext(
				from,
				to,
				ClipContext.Block.COLLIDER, // Проверяем коллизии блоков
				ClipContext.Fluid.NONE,
				null
		);

		BlockHitResult result = level.clip(context);
		return result.getType() == HitResult.Type.MISS; // Если нет столкновения с блоками
	}

	// Класс цели для атаки того, кого атаковал владелец
	private class OwnerHurtTargetGoal extends TargetGoal {
		private final MosquitoSwarm mosquito;
		private LivingEntity ownerLastHurt;
		private int timestamp;

		public OwnerHurtTargetGoal(MosquitoSwarm mosquito) {
			super(mosquito, false);
			this.mosquito = mosquito;
		}

		@Override
		public boolean canUse() {
			if (mosquito.getOwner() == null) {
				return false;
			}

			LivingEntity owner = mosquito.getOwner();
			if (owner == null) {
				return false;
			}

			this.ownerLastHurt = owner.getLastHurtMob();
			int i = owner.getLastHurtMobTimestamp();

			if (this.ownerLastHurt == null) {
				return false;
			}

			if (i == this.timestamp) {
				return false;
			}

			// Не атакуем если владелец в другом измерении
			if (!mosquito.level().dimension().equals(owner.level().dimension())) {
				return false;
			}

			// Проверяем, не заблокирован ли путь к цели
			if (mosquito.isPathBlocked(this.ownerLastHurt.position())) {
				return false;
			}

			// Проверяем, можно ли атаковать эту цель
			return mosquito.canAttack(this.ownerLastHurt) &&
					!mosquito.isAlliedTo(this.ownerLastHurt) &&
					!this.ownerLastHurt.equals(owner); // Не атакуем владельца самого себя
		}

		@Override
		public void start() {
			// Используем специальный метод для установки цели
			this.mosquito.setTargetIgnoringOwnerCheck(this.ownerLastHurt);
			LivingEntity owner = this.mosquito.getOwner();
			if (owner != null) {
				this.timestamp = owner.getLastHurtMobTimestamp();
			}
			super.start();
		}

		@Override
		public boolean canContinueToUse() {
			if (this.ownerLastHurt == null || !this.ownerLastHurt.isAlive()) {
				return false;
			}

			LivingEntity owner = mosquito.getOwner();
			if (owner == null) {
				return false;
			}

			// Проверяем, не заблокирован ли путь к цели
			if (mosquito.isPathBlocked(this.ownerLastHurt.position())) {
				return false;
			}

			return mosquito.canAttack(this.ownerLastHurt) &&
					!mosquito.isAlliedTo(this.ownerLastHurt) &&
					!this.ownerLastHurt.equals(owner);
		}
	}

	private class OwnerHurtByTargetGoal extends TargetGoal {
		private final MosquitoSwarm mosquito;
		private LivingEntity ownerLastHurtBy;
		private int timestamp;

		public OwnerHurtByTargetGoal(MosquitoSwarm mosquito) {
			super(mosquito, false);
			this.mosquito = mosquito;
		}

		@Override
		public boolean canUse() {
			if (mosquito.getOwner() == null) {
				return false;
			}

			LivingEntity owner = mosquito.getOwner();
			if (owner == null) {
				return false;
			}

			this.ownerLastHurtBy = owner.getLastHurtByMob();
			int i = owner.getLastHurtByMobTimestamp();

			if (this.ownerLastHurtBy == null) {
				return false;
			}

			if (i == this.timestamp) {
				return false;
			}

			// Не атакуем если владелец в другом измерении
			if (!mosquito.level().dimension().equals(owner.level().dimension())) {
				return false;
			}

			// Проверяем, не заблокирован ли путь к цели
			if (mosquito.isPathBlocked(this.ownerLastHurtBy.position())) {
				return false;
			}

			// Проверяем, можно ли атаковать эту цель
			return mosquito.canAttack(this.ownerLastHurtBy) &&
					!mosquito.isAlliedTo(this.ownerLastHurtBy);
		}

		@Override
		public void start() {
			// Используем специальный метод для установки цели, который игнорирует проверку на игрока
			// для того, кто атаковал владельца
			this.mosquito.setTargetIgnoringOwnerCheck(this.ownerLastHurtBy);
			LivingEntity owner = this.mosquito.getOwner();
			if (owner != null) {
				this.timestamp = owner.getLastHurtByMobTimestamp();
			}
			super.start();
		}

		@Override
		public boolean canContinueToUse() {
			if (this.ownerLastHurtBy == null || !this.ownerLastHurtBy.isAlive()) {
				return false;
			}

			LivingEntity owner = mosquito.getOwner();
			if (owner == null) {
				return false;
			}

			// Проверяем, не заблокирован ли путь к цели
			if (mosquito.isPathBlocked(this.ownerLastHurtBy.position())) {
				return false;
			}

			return mosquito.canAttack(this.ownerLastHurtBy) &&
					!mosquito.isAlliedTo(this.ownerLastHurtBy);
		}
	}

	// Метод для установки цели без проверки на владельца (используется в OwnerHurtByTargetGoal)
	public void setTargetIgnoringOwnerCheck(@Nullable LivingEntity target) {
		super.setTarget(target);
		if (target != null) {
			this.getNavigation().moveTo(target, 1.2D);
		}
	}

	// Класс цели для атаки врагов владельца
	private class AttackOwnerEnemiesGoal extends TargetGoal {
		private final MosquitoSwarm mosquito;
		private LivingEntity owner;
		private LivingEntity toAttack;
		private int timestamp;

		public AttackOwnerEnemiesGoal(MosquitoSwarm mosquito) {
			super(mosquito, false);
			this.mosquito = mosquito;
		}

		@Override
		public boolean canUse() {
			this.owner = mosquito.getOwner();
			if (owner == null) {
				return false;
			}

			// Не атакуем если владелец в другом измерении
			if (!mosquito.level().dimension().equals(owner.level().dimension())) {
				return false;
			}

			// Не атакуем врагов если владелец слишком далеко
			if (owner.distanceToSqr(mosquito) > TELEPORT_DISTANCE_THRESHOLD * TELEPORT_DISTANCE_THRESHOLD) {
				return false;
			}

			// Ищем цели в следующем порядке приоритета:

			// 1. Враждебные мобы (Monster)
			List<Monster> monsters = mosquito.level().getEntitiesOfClass(
					Monster.class,
					mosquito.getBoundingBox().inflate(24.0),
					entity -> entity.isAlive() &&
							!entity.isAlliedTo(owner) &&
							!entity.is(mosquito) &&
							!(entity instanceof MosquitoSwarm) &&
							!(entity instanceof NeutralMob) &&
							(canSeeEntity(mosquito.level(), mosquito.getEyePosition(), entity.getBoundingBox().getCenter())) &&
							entity.distanceToSqr(mosquito) <= 400 &&
							mosquito.canAttack(entity)
			);

			if (!monsters.isEmpty()) {
				this.toAttack = monsters.get(mosquito.random.nextInt(monsters.size()));
				// Проверяем, не заблокирован ли путь к цели
				if (mosquito.isPathBlocked(this.toAttack.position())) {
					return false;
				}
				return true;
			}

			// 2. Мобы, чья цель - владелец
			List<Mob> mobsTargetingOwner = mosquito.level().getEntitiesOfClass(
					Mob.class,
					mosquito.getBoundingBox().inflate(24.0),
					entity -> entity.isAlive() &&
							entity.getTarget() == owner &&
							!(entity instanceof MosquitoSwarm) &&
							(canSeeEntity(mosquito.level(), mosquito.getEyePosition(), entity.getBoundingBox().getCenter())) &&
							entity.distanceToSqr(mosquito) <= 400 &&
							!entity.is(mosquito)
			);

			if (!mobsTargetingOwner.isEmpty()) {
				this.toAttack = mobsTargetingOwner.get(mosquito.random.nextInt(mobsTargetingOwner.size()));
				// Проверяем, не заблокирован ли путь к цели
				if (mosquito.isPathBlocked(this.toAttack.position())) {
					return false;
				}
				return true;
			}

			List<Cow> cows = mosquito.level().getEntitiesOfClass(
					Cow.class,
					mosquito.getBoundingBox().inflate(24.0),
					entity -> entity.isAlive() &&
							!entity.isAlliedTo(owner) &&
							!entity.is(mosquito) &&
							(canSeeEntity(mosquito.level(), mosquito.getEyePosition(), entity.getBoundingBox().getCenter())) &&
							entity.distanceToSqr(mosquito) <= 400 &&
							mosquito.canAttack(entity)
			);

			if (!cows.isEmpty()) {
				this.toAttack = cows.get(mosquito.random.nextInt(cows.size()));
				// Проверяем, не заблокирован ли путь к цели
				if (mosquito.isPathBlocked(this.toAttack.position())) {
					return false;
				}
				return true;
			}

			// 3. Игроки, которых атаковал владелец
//			List<Player> players = mosquito.level().getEntitiesOfClass(
//					Player.class,
//					mosquito.getBoundingBox().inflate(24.0),
//					entity -> entity.isAlive() &&
//							!entity.is(owner) &&
//							!entity.isAlliedTo(owner) &&
//							!entity.is(mosquito) &&
//							(canSeeEntity(mosquito.level(), mosquito.getEyePosition(), entity.getBoundingBox().getCenter())) &&
//							entity.distanceToSqr(mosquito) <= 400 &&
//							mosquito.canAttack(entity)
//			);
//
//			if (!players.isEmpty()) {
//				this.toAttack = players.get(mosquito.random.nextInt(players.size()));
//				// Проверяем, не заблокирован ли путь к цели
//				if (mosquito.isPathBlocked(this.toAttack.position())) {
//					return false;
//				}
//				return true;
//			}

			return false;
		}

		@Override
		public void start() {
			this.mosquito.setTarget(this.toAttack);
			this.timestamp = this.toAttack.tickCount;
			super.start();
		}

		@Override
		public boolean canContinueToUse() {
			if (this.toAttack == null || !this.toAttack.isAlive()) {
				return false;
			}
			if (this.owner == null || !this.owner.isAlive()) {
				return false;
			}

			// Прекращаем если владелец в другом измерении
			if (!mosquito.level().dimension().equals(this.owner.level().dimension())) {
				return false;
			}

			// Также прекращаем если владелец стал слишком далеко
			if (this.owner.distanceToSqr(mosquito) > TELEPORT_DISTANCE_THRESHOLD * TELEPORT_DISTANCE_THRESHOLD) {
				return false;
			}

			// Проверяем, не заблокирован ли путь к цели
			if (mosquito.isPathBlocked(this.toAttack.position())) {
				return false;
			}

			return true;
		}
	}

	// Модифицированный класс цели для атаки с проверкой пути
	private class SafeMeleeAttackGoal extends MeleeAttackGoal {
		public SafeMeleeAttackGoal() {
			super(MosquitoSwarm.this, 1.0D, false);
		}

		@Override
		public boolean canUse() {
			if (MosquitoSwarm.this.getTarget() != null) {
				// Проверяем, не заблокирован ли путь к цели
				if (MosquitoSwarm.this.isPathBlocked(MosquitoSwarm.this.getTarget().position())) {
					return false;
				}
			}
			return super.canUse();
		}

		@Override
		public boolean canContinueToUse() {
			if (MosquitoSwarm.this.getTarget() != null) {
				// Проверяем, не заблокирован ли путь к цели
				if (MosquitoSwarm.this.isPathBlocked(MosquitoSwarm.this.getTarget().position())) {
					return false;
				}
			}
			return super.canContinueToUse();
		}

		@Override
		protected double getAttackReachSqr(LivingEntity pAttackTarget) {
			return 2.0D; // Увеличиваем радиус атаки
		}
	}

	@Nullable
	@Override
	public LivingEntity getTarget() {
		LivingEntity target = super.getTarget();
		if (target != null && target.hasEffect(ModEffects.MOSQUITOING.get()) && target.getEffect(ModEffects.MOSQUITOING.get()).getAmplifier() >= 2) {
			this.setTarget(null);
			return null;
		}
		return target;
	}

	@Override
	protected void registerGoals() {
		this.goalSelector.addGoal(0, new FollowOwnerFarGoal(this, 40D)); // Высший приоритет - владелец далеко
		this.goalSelector.addGoal(1, new SafeMeleeAttackGoal()); // Используем безопасную цель атаки
		this.goalSelector.addGoal(5, new MoveTowardsRestrictionGoal(this, 1.0D));
		this.goalSelector.addGoal(7, new WaterAvoidingRandomFlyingGoal(this, 1.0D));
		this.goalSelector.addGoal(8, new FollowOwnerWhenIdleGoal(this, 1.0D)); // Низкий приоритет - владелец близко, нет цели

		// Добавляем цель для атаки врагов владельца
		this.targetSelector.addGoal(2, new OwnerHurtByTargetGoal(this));
		this.targetSelector.addGoal(1, new OwnerHurtTargetGoal(this));
		this.targetSelector.addGoal(2, new AttackOwnerEnemiesGoal(this));

		// Обычная цель (только если нет владельца)
		this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Player.class, true) {
			@Override
			public boolean canUse() {
				return MosquitoSwarm.this.getOwnerUUID() == null && super.canUse();
			}

			@Override
			public boolean canContinueToUse() {
				if (MosquitoSwarm.this.getTarget() != null) {
					// Проверяем, не заблокирован ли путь к цели
					if (MosquitoSwarm.this.isPathBlocked(MosquitoSwarm.this.getTarget().position())) {
						return false;
					}
				}
				return super.canContinueToUse();
			}
		});

		// Обычная цель (только если нет владельца)
		this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, Cow.class, true) {
			@Override
			public boolean canUse() {
				return MosquitoSwarm.this.getOwnerUUID() == null && super.canUse();
			}

			@Override
			public boolean canContinueToUse() {
				if (MosquitoSwarm.this.getTarget() != null) {
					// Проверяем, не заблокирован ли путь к цели
					if (MosquitoSwarm.this.isPathBlocked(MosquitoSwarm.this.getTarget().position())) {
						return false;
					}
				}
				return super.canContinueToUse();
			}
		});

		this.goalSelector.addGoal(2, new MoveTowardsTargetGoal(this, 0.9D, 16.0F) {
			@Override
			public boolean canUse() {
				if (MosquitoSwarm.this.getTarget() != null) {
					// Проверяем, не заблокирован ли путь к цели
					if (MosquitoSwarm.this.isPathBlocked(MosquitoSwarm.this.getTarget().position())) {
						return false;
					}
				}
				return super.canUse();
			}

			@Override
			public boolean canContinueToUse() {
				if (MosquitoSwarm.this.getTarget() != null) {
					// Проверяем, не заблокирован ли путь к цели
					if (MosquitoSwarm.this.isPathBlocked(MosquitoSwarm.this.getTarget().position())) {
						return false;
					}
				}
				return super.canContinueToUse();
			}
		});
	}

	private void checkTransformationConditions() {
		boolean shouldTransform = false;

		// Условие 1: Касается любой жидкости
		if (this.isInWater() || this.isInLava() || this.isInFluidType((fluidType, height) -> height > 0)) {
			shouldTransform = true;
		}

		// Условие 2: Горит
		if (this.isOnFire()) {
			shouldTransform = true;
		}

		// Условие 3: Рядом есть блок с тегом STICKY_FOR_MOSQUITOS в радиусе 1 блока
		if (hasStickyBlockNearby()) {
			shouldTransform = true;
		}

		Level level = this.level();
		BlockPos pos = this.blockPosition();
		BlockPos newpos = new BlockPos(pos.getX(), (int) this.getEyeY(), pos.getZ());

		Biome biome = level.getBiome(newpos).value();
		boolean canRain = biome.getPrecipitationAt(newpos) == Biome.Precipitation.RAIN;

		if ((level.isRaining() && canRain && level.canSeeSky(newpos)) || (level.getBiome(this.getOnPos()).is(BiomeRegistry.RIE_WEALD) && level.isRaining())) {
			shouldTransform = true;
		}

		if (shouldTransform) {
			transformIntoBlock(this.getSize());
		}
	}

	public boolean hasStickyBlockNearby() {
		Level level = this.level();
		BlockPos centerPos = this.blockPosition();

		BlockPos[] directions = {
				centerPos.above(),    // +Y
				centerPos.below(),    // -Y
				centerPos.north(),    // -Z
				centerPos.south(),    // +Z
				centerPos.west(),     // -X
				centerPos.east()      // +X
		};

		for (BlockPos checkPos : directions) {
			BlockState state = level.getBlockState(checkPos);
			if (state.is(ModTags.Blocks.STICKY_FOR_MOSQUITOS)) {
				return true;
			}
		}

		return false;
	}

	public static void spawnBlock(LivingEntity entity, int amp) {
		if (!entity.level().isClientSide) {
			Level level = entity.level();
			BlockPos pos = entity.blockPosition();

			boolean placed = placeMosquitoBlockAtPosition(entity, pos);

			if (placed) {
				if (level instanceof ServerLevel serverLevel) {
					serverLevel.sendParticles(ModParticles.MOSQUITOS.get(),
							entity.getX(), entity.getY() + 0.5, entity.getZ(),
							20, 0.5, 0.5, 0.5, 0.05);
				}
				switch (amp) {
					case 1 -> entity.spawnAtLocation(ModItems.CHITIN_POWDER.get());
					case 2 -> {
						entity.spawnAtLocation(ModItems.CHITIN_POWDER.get());
						entity.spawnAtLocation(ModItems.CHITIN_POWDER.get());
					}
					default -> {
					}
				}
				;
			} else {
				switch (amp) {
					case 1 -> {
						entity.spawnAtLocation(ModItems.CHITIN_POWDER.get());
						entity.spawnAtLocation(ModItems.CHITIN_POWDER.get());
					}
					case 2 -> {
						entity.spawnAtLocation(ModItems.CHITIN_POWDER.get());
						entity.spawnAtLocation(ModItems.CHITIN_POWDER.get());
						entity.spawnAtLocation(ModItems.CHITIN_POWDER.get());
					}
					default -> entity.spawnAtLocation(ModItems.CHITIN_POWDER.get());
				}
				;
			}
			entity.playSound(RPGSounds.MOSQUITO_SWARM_DEATH.get(), 1.0F, 1.0F);
		}
	}

	public void transformIntoBlock(int size) {
		if (!this.level().isClientSide) {
			Level level = this.level();
			BlockPos pos = this.blockPosition();

			boolean placed = placeMosquitoBlockAtPosition(pos);

			if (placed) {
				if (level instanceof ServerLevel serverLevel) {
					serverLevel.sendParticles(ModParticles.MOSQUITOS.get(),
							this.getX(), this.getY() + 0.5, this.getZ(),
							20, 0.35f + 0.05f * this.getSize(), 0.35f + 0.05f * this.getSize(), 0.35f + 0.05f * this.getSize(), 0.05);
				}
				switch (size) {
					case 2 -> this.spawnAtLocation(ModItems.CHITIN_POWDER.get());
					case 3 -> {
						this.spawnAtLocation(ModItems.CHITIN_POWDER.get());
						this.spawnAtLocation(ModItems.CHITIN_POWDER.get());
					}
					default -> {
					}
				}
				;
			} else {
				switch (size) {
					case 2 -> {
						this.spawnAtLocation(ModItems.CHITIN_POWDER.get());
						this.spawnAtLocation(ModItems.CHITIN_POWDER.get());
					}
					case 3 -> {
						this.spawnAtLocation(ModItems.CHITIN_POWDER.get());
						this.spawnAtLocation(ModItems.CHITIN_POWDER.get());
						this.spawnAtLocation(ModItems.CHITIN_POWDER.get());
					}
					default -> this.spawnAtLocation(ModItems.CHITIN_POWDER.get());
				}
				;

			}

			if (this.getOwner() == null) {
				for (ServerPlayer serverplayer : this.level().getEntitiesOfClass(ServerPlayer.class, new AABB(this.getOnPos()).inflate(10.0D, 15.0D, 10.0D))) {
					if (!this.entityData.get(DATA_DEALT_DAMAGE) && serverplayer.getLastDamageSource() == null) {
						ModAdvancements.GOLDEN_KILL_MOSQUITO_SWARM.trigger(serverplayer);
					}
					ModAdvancements.KILL_MOSQUITO_SWARM.trigger(serverplayer);
				}
			}
			this.playSound(RPGSounds.MOSQUITO_SWARM_DEATH.get(), 1.0F, 1.0F);
			this.proceedKill();
		}
	}

	public final void proceedKill() {
		if (this.level() instanceof ServerLevel serverLevel) {
			serverLevel.sendParticles(ModParticles.MOSQUITOS.get(),
					this.getX(), this.getY() + 0.5, this.getZ(),
					20, 0.35f + 0.05f * this.getSize(), 0.35f + 0.05f * this.getSize(), 0.35f + 0.05f * this.getSize(), 0.05);
		}
		this.discard();
	}

	private static boolean placeMosquitoBlockAtPosition(LivingEntity entity, BlockPos centerPos) {
		List<Direction> stickyDirections = findStickyBlocks(entity, centerPos);

		if (!stickyDirections.isEmpty()) {
			Direction stickyDir = stickyDirections.get(0);
			return placeMosquitoBlockOnFace(entity, centerPos, stickyDir);
		}

		Direction solidDirection = findNearestSolidBlock(entity, centerPos);

		if (solidDirection != null) {
			return placeMosquitoBlockOnFace(entity, centerPos, solidDirection.getOpposite());
		}

		return false;
	}

	private boolean placeMosquitoBlockAtPosition(BlockPos centerPos) {
		List<Direction> stickyDirections = findStickyBlocks(centerPos);

		if (!stickyDirections.isEmpty()) {
			Direction stickyDir = stickyDirections.get(0);
			return placeMosquitoBlockOnFace(centerPos, stickyDir);
		}

		Direction solidDirection = findNearestSolidBlock(centerPos);

		if (solidDirection != null) {
			return placeMosquitoBlockOnFace(centerPos, solidDirection.getOpposite());
		}

		return false;
	}

	static private List<Direction> findStickyBlocks(LivingEntity entity, BlockPos centerPos) {
		List<Direction> stickyDirections = new ArrayList<>();
		Level level = entity.level();

		for (int yOffset = 0; yOffset >= -3; yOffset--) {
			BlockPos checkCenterPos = centerPos.offset(0, yOffset, 0);

			for (Direction direction : Direction.values()) {
				BlockPos checkPos = checkCenterPos.relative(direction);
				BlockState state = level.getBlockState(checkPos);

				if (canAttachTo(level, direction, checkPos, state)) {
					if (state.is(ModTags.Blocks.STICKY_FOR_MOSQUITOS)) {
						stickyDirections.add(direction);
						return stickyDirections;
					}
				}
			}
		}

		return stickyDirections;
	}

	private List<Direction> findStickyBlocks(BlockPos centerPos) {
		List<Direction> stickyDirections = new ArrayList<>();
		Level level = this.level();

		for (int yOffset = 0; yOffset >= -3; yOffset--) {
			BlockPos checkCenterPos = centerPos.offset(0, yOffset, 0);

			for (Direction direction : Direction.values()) {
				BlockPos checkPos = checkCenterPos.relative(direction);
				BlockState state = level.getBlockState(checkPos);

				if (canAttachTo(level, direction, checkPos, state)) {
					if (state.is(ModTags.Blocks.STICKY_FOR_MOSQUITOS)) {
						stickyDirections.add(direction);
						return stickyDirections;
					}
				}
			}
		}

		return stickyDirections;
	}

	private static Direction findNearestSolidBlock(LivingEntity entity, BlockPos centerPos) {
		Level level = entity.level();
		Map<Direction, Double> solidBlocks = new HashMap<>();

		for (int yOffset = 0; yOffset >= -3; yOffset--) {
			BlockPos checkCenterPos = centerPos.offset(0, yOffset, 0);

			for (Direction direction : Direction.values()) {
				BlockPos checkPos = checkCenterPos.relative(direction);
				BlockState state = level.getBlockState(checkPos);

				if (canAttachTo(level, direction, checkPos, state)) {
					double distance = centerPos.distSqr(checkPos);
					solidBlocks.put(direction, distance);
				}
			}
		}

		if (!solidBlocks.isEmpty()) {
			return Collections.min(solidBlocks.entrySet(),
					Map.Entry.comparingByValue()).getKey();
		}

		return null;
	}

	private Direction findNearestSolidBlock(BlockPos centerPos) {
		Level level = this.level();
		Map<Direction, Double> solidBlocks = new HashMap<>();

		for (int yOffset = 0; yOffset >= -3; yOffset--) {
			BlockPos checkCenterPos = centerPos.offset(0, yOffset, 0);

			for (Direction direction : Direction.values()) {
				BlockPos checkPos = checkCenterPos.relative(direction);
				BlockState state = level.getBlockState(checkPos);

				if (canAttachTo(level, direction, checkPos, state)) {
					double distance = centerPos.distSqr(checkPos);
					solidBlocks.put(direction, distance);
				}
			}
		}

		if (!solidBlocks.isEmpty()) {
			return Collections.min(solidBlocks.entrySet(),
					Map.Entry.comparingByValue()).getKey();
		}

		return null;
	}

	private static boolean canAttachTo(BlockGetter level, Direction direction, BlockPos pos, BlockState state) {
		return Block.isFaceFull(state.getBlockSupportShape(level, pos), direction.getOpposite())
				|| Block.isFaceFull(state.getCollisionShape(level, pos), direction.getOpposite()) || state.is(ModTags.Blocks.STICKY_FOR_MOSQUITOS);
	}

	private static boolean placeMosquitoBlockOnFace(LivingEntity entity, BlockPos centerPos, Direction face) {
		Level level = entity.level();

		BlockPos mosquitoBlockPos = centerPos;
		BlockState currentState = level.getBlockState(mosquitoBlockPos);

		if (!currentState.isAir() && !currentState.canBeReplaced()) {
			return false;
		}

		BlockState mosquitoBlockState = ModBlocks.MOSQUITOS.get().defaultBlockState();

		mosquitoBlockState = mosquitoBlockState.setValue(MultifaceBlock.getFaceProperty(face), true);

		if (canAttachTo(level, face, mosquitoBlockPos.relative(face),
				level.getBlockState(mosquitoBlockPos.relative(face)))) {

			level.setBlock(mosquitoBlockPos, mosquitoBlockState, 3);
			return true;
		}

		return false;
	}

	private boolean placeMosquitoBlockOnFace(BlockPos centerPos, Direction face) {
		Level level = this.level();

		BlockPos mosquitoBlockPos = centerPos;
		BlockState currentState = level.getBlockState(mosquitoBlockPos);

		if (!currentState.isAir() && !currentState.canBeReplaced()) {
			return false;
		}

		BlockState mosquitoBlockState = ModBlocks.MOSQUITOS.get().defaultBlockState();

		mosquitoBlockState = mosquitoBlockState.setValue(MultifaceBlock.getFaceProperty(face), true);

		if (canAttachTo(level, face, mosquitoBlockPos.relative(face),
				level.getBlockState(mosquitoBlockPos.relative(face)))) {

			level.setBlock(mosquitoBlockPos, mosquitoBlockState, 3);
			return true;
		}

		return false;
	}

	public static boolean checkMosquitoSwarmSpawnRules(EntityType<MosquitoSwarm> mosquitoSwarm, LevelAccessor level, MobSpawnType type, BlockPos blockPos, RandomSource random) {
		return hasRieLeavesAboveBlock(level, blockPos) && (level.dayTime() % 24000) >= 0 && (level.dayTime() % 24000) < 12300 && !level.getLevelData().isRaining() && !level.getLevelData().isThundering();
	}

	public static boolean hasRieLeavesAboveBlock(LevelAccessor world, BlockPos pos) {
		int y = pos.getY() + 1;
		while (y < world.getMaxBuildHeight()) {
			BlockPos blockAbovePos = new BlockPos(pos.getX(), y, pos.getZ());
			BlockState blockState = world.getBlockState(blockAbovePos);

			if (!blockState.isAir()) {
				Block blockAbove = blockState.getBlock();
				return blockAbove == ModBlocks.RIE_LEAVES.get();
			}
			y++;
		}
		return false;
	}

	protected PathNavigation createNavigation(Level pLevel) {
		FlyingPathNavigation flyingpathnavigation = new FlyingPathNavigation(this, pLevel);
		flyingpathnavigation.setCanOpenDoors(false);
		flyingpathnavigation.setCanFloat(true);
		flyingpathnavigation.setCanPassDoors(true);
		return flyingpathnavigation;
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Monster.createMonsterAttributes()
				.add(Attributes.MAX_HEALTH, 1D)
				.add(Attributes.ATTACK_DAMAGE, 0.0D)
				.add(Attributes.FLYING_SPEED, 1.2F)
				.add(Attributes.MOVEMENT_SPEED, 1.2F)
				.add(Attributes.FOLLOW_RANGE, 24.0D)
				.add(Attributes.ATTACK_KNOCKBACK, 0.0D);
	}

	@Override
	public boolean doHurtTarget(Entity pEntity) {
		if (pEntity instanceof LivingEntity livingEntity && attackCooldown == 0) {
			applyEffect(livingEntity);
			if (pEntity instanceof ServerPlayer player)
				if (!this.entityData.get(DATA_DEALT_DAMAGE)) {
					this.entityData.set(DATA_DEALT_DAMAGE, true);
					if (!hasGoldenKill(player)) this.playSound(RPGSounds.GOLDEN_TOKEN_FAIL.get(), 2.0F, 1.0F);
				}
			this.proceedKill();
			return true;
		}
		return false;
	}

	@Override
	public void setTarget(@Nullable LivingEntity target) {
		// Предотвращаем установку цели-игрока, если есть владелец
		if (this.getOwnerUUID() != null && target instanceof Player) {
			return;
		}
		super.setTarget(target);
		if (target != null) {
			this.getNavigation().moveTo(target, 1.2D);
		}
	}

	protected void defineSynchedData() {
		super.defineSynchedData();
		this.entityData.define(DATA_FLAGS_ID, (byte) 0);
		this.entityData.define(DATA_DEALT_DAMAGE, false);
		this.entityData.define(DATA_SWARM_SIZE, 3);
		this.entityData.define(DATA_OWNERUUID_ID, Optional.empty());
		this.entityData.define(DATA_OWNER_TIMER, 0);
	}

	public void dealtDamage() {
		this.entityData.set(DATA_DEALT_DAMAGE, true);

	}

	public void setSize(int size) {
		this.entityData.set(DATA_SWARM_SIZE, size);
	}

	@Nullable
	public UUID getOwnerUUID() {
		return this.entityData.get(DATA_OWNERUUID_ID).orElse((UUID) null);
	}

	public void setOwnerUUID(@Nullable UUID pUuid) {
		this.entityData.set(DATA_OWNERUUID_ID, Optional.ofNullable(pUuid));
		this.cachedOwner = null; // Сбрасываем кэш при изменении владельца
		this.resetOwnerTimer(); // Сбрасываем таймер при изменении владельца
	}

	@Nullable
	@Override
	public LivingEntity getOwner() {
		if (this.cachedOwner != null && this.cachedOwner.isAlive()) {
			return this.cachedOwner;
		}

		UUID uuid = this.getOwnerUUID();
		if (uuid == null) {
			return null;
		}

		if (this.level() instanceof ServerLevel) {
			Entity entity = ((ServerLevel) this.level()).getEntity(uuid);
			if (entity instanceof LivingEntity) {
				this.cachedOwner = (LivingEntity) entity;
				return this.cachedOwner;
			}
		}
		return null;
	}

	public boolean isOwnedBy(LivingEntity pEntity) {
		return pEntity == this.getOwner();
	}

	public boolean isAlliedTo(Entity pEntity) {
		if (this.isTame()) {
			LivingEntity livingentity = this.getOwner();
			if (pEntity == livingentity) {
				return true;
			}

			if (livingentity != null) {
				return livingentity.isAlliedTo(pEntity);
			}
		}

		return super.isAlliedTo(pEntity);
	}

	public boolean isTame() {
		return (this.entityData.get(DATA_FLAGS_ID) & 4) != 0;
	}

	public void setTame(boolean pTamed) {
		byte b0 = this.entityData.get(DATA_FLAGS_ID);
		if (pTamed) {
			this.entityData.set(DATA_FLAGS_ID, (byte) (b0 | 4));
		} else {
			this.entityData.set(DATA_FLAGS_ID, (byte) (b0 & -5));
		}
	}

	public void tame(Player pPlayer) {
		this.setTame(true);
		this.setOwnerUUID(pPlayer.getUUID());
	}

	@Override
	public Team getTeam() {
		if (this.isTame()) {
			LivingEntity livingentity = this.getOwner();
			if (livingentity != null) {
				return livingentity.getTeam();
			}
		}

		return super.getTeam();
	}

	public void addAdditionalSaveData(CompoundTag compound) {
		super.addAdditionalSaveData(compound);
		compound.putBoolean("Dealt damage", this.entityData.get(DATA_DEALT_DAMAGE));
		compound.putInt("Swarm size", this.entityData.get(DATA_SWARM_SIZE));
		compound.putInt("Owner timer", this.entityData.get(DATA_OWNER_TIMER));
		UUID uuid = this.getOwnerUUID();
		if (uuid != null) {
			compound.putUUID("Owner", uuid);
		}
	}

	public void readAdditionalSaveData(CompoundTag compound) {
		super.readAdditionalSaveData(compound);
		this.entityData.set(DATA_DEALT_DAMAGE, compound.getBoolean("Dealt damage"));
		this.entityData.set(DATA_SWARM_SIZE, compound.getInt("Swarm size"));
		this.entityData.set(DATA_OWNER_TIMER, compound.getInt("Owner timer"));
		UUID uuid;
		if (compound.hasUUID("Owner")) {
			uuid = compound.getUUID("Owner");
		} else {
			String s = compound.getString("Owner");
			uuid = OldUsersConverter.convertMobOwnerIfNecessary(this.getServer(), s);
		}

		if (uuid != null) {
			this.setOwnerUUID(uuid);
		}
	}

	public boolean hurt(DamageSource pSource, float pAmount) {
		if (pSource == this.damageSources().genericKill() || pSource.isCreativePlayer()) {
			transformIntoBlock(this.getSize());
			return super.hurt(pSource, pAmount);
		}
		return false;
	}

	@Override
	public boolean isPushable() {
		return false;
	}

	@Override
	protected void playStepSound(BlockPos pPos, BlockState pBlock) {
	}

	@Override
	public boolean attackable() {
		return false;
	}

	@Override
	public boolean isAttackable() {
		return false;
	}

	@Override
	protected void pushEntities() {
	}

	protected float getStandingEyeHeight(Pose pPose, EntityDimensions pSize) {
		return 0.5F;
	}

	public void travel(Vec3 pTravelVector) {
		if (this.isInWater()) {
			this.moveRelative(0.02F, pTravelVector);
			this.move(MoverType.SELF, this.getDeltaMovement());
			this.setDeltaMovement(this.getDeltaMovement().scale(0.8F));
		} else if (this.isInLava()) {
			this.moveRelative(0.02F, pTravelVector);
			this.move(MoverType.SELF, this.getDeltaMovement());
			this.setDeltaMovement(this.getDeltaMovement().scale(0.5D));
		} else {
			BlockPos ground = new BlockPos(this.getBlockX(), this.getBlockY() - 1, this.getBlockZ());
			float f = 0.91F;
			if (this.onGround()) {
				f = this.level().getBlockState(ground).getFriction(this.level(), ground, this) * 0.91F;
			}

			float f1 = 0.16277137F / (f * f * f);
			f = 0.91F;
			if (this.onGround()) {
				f = this.level().getBlockState(ground).getFriction(this.level(), ground, this) * 0.91F;
			}

			this.moveRelative(this.onGround() ? 0.1F * f1 : 0.02F, pTravelVector);
			this.move(MoverType.SELF, this.getDeltaMovement());
			this.setDeltaMovement(this.getDeltaMovement().scale(f));
		}
		this.calculateEntityAnimation(false);
	}

	protected SoundEvent getAmbientSound() {
		return RPGSounds.MOSQUITO_SWARM_AMBIENT.get();
	}

	protected SoundEvent getHurtSound(DamageSource pDamageSource) {
		return RPGSounds.MOSQUITO_SWARM_DEATH.get();
	}

	protected SoundEvent getDeathSound() {
		return RPGSounds.MOSQUITO_SWARM_DEATH.get();
	}

	public boolean causeFallDamage(float pFallDistance, float pMultiplier, DamageSource pSource) {
		return false;
	}

	protected void checkFallDamage(double pY, boolean pOnGround, BlockState pState, BlockPos pPos) {
	}

	public InteractionResult mobInteract(Player pPlayer, InteractionHand pHand) {
		ItemStack itemstack = pPlayer.getItemInHand(pHand);
		if (itemstack.is(Items.GLASS_BOTTLE)) {
			pPlayer.playSound(SoundEvents.BOTTLE_FILL_DRAGONBREATH, 1.0F, 1.0F);
			if (this.getOwner() != null) {
				if (!pPlayer.isCreative() && !pPlayer.level().isClientSide) {
					itemstack.shrink(1);
					ItemStack bottle = ModItems.MOSQUITO_BOTTLE.get().getDefaultInstance();
					CompoundTag nbtData = new CompoundTag();
					nbtData.putUUID("rpgworldmod.mosquitos_owner", this.getOwnerUUID());
					bottle.setTag(nbtData);
					if (itemstack.isEmpty())
						pPlayer.setItemInHand(pHand, bottle);
					else if (pPlayer.getInventory().getFreeSlot() == -1)
						pPlayer.drop(bottle, false);
					else pPlayer.getInventory().add(bottle);
				}
				this.entityData.set(DATA_SWARM_SIZE, this.entityData.get(DATA_SWARM_SIZE) - 1);
				if (this.entityData.get(DATA_SWARM_SIZE) == 0) this.proceedKill();
			} else {
				if (!pPlayer.isCreative() && !pPlayer.level().isClientSide) {
					itemstack.shrink(1);
					ItemStack bottle = ModItems.MOSQUITO_BOTTLE.get().getDefaultInstance();
					if (itemstack.isEmpty())
						pPlayer.setItemInHand(pHand, bottle);
					else if (pPlayer.getInventory().getFreeSlot() == -1)
						pPlayer.drop(bottle, false);
					else pPlayer.getInventory().add(bottle);
				}
				this.entityData.set(DATA_SWARM_SIZE, this.entityData.get(DATA_SWARM_SIZE) - 1);
				if (this.entityData.get(DATA_SWARM_SIZE) == 0) {
					this.proceedKill();
					if (pPlayer instanceof ServerPlayer serverPlayer) {
						if (!this.entityData.get(DATA_DEALT_DAMAGE) && serverPlayer.getLastDamageSource() == null) {
							ModAdvancements.GOLDEN_KILL_MOSQUITO_SWARM.trigger(serverPlayer);
						}
						ModAdvancements.KILL_MOSQUITO_SWARM.trigger(serverPlayer);
					}
				}
			}
			return InteractionResult.sidedSuccess(this.level().isClientSide);
		} else {
			return super.mobInteract(pPlayer, pHand);
		}
	}
}