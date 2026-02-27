package net.dainplay.rpgworldmod.entity.custom;

import net.dainplay.rpgworldmod.enchantment.ModEnchantments;
import net.dainplay.rpgworldmod.item.ModItems;
import net.dainplay.rpgworldmod.particle.ModParticles;
import net.dainplay.rpgworldmod.sounds.RPGSounds;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.Dolphin;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.Optional;
import java.util.UUID;

public class ConjuredDolphin extends Dolphin {
	private static final EntityDataAccessor<Optional<UUID>> DATA_OWNER = SynchedEntityData.defineId(ConjuredDolphin.class, EntityDataSerializers.OPTIONAL_UUID);

	private int livedTicks;

	public ConjuredDolphin(EntityType<? extends Dolphin> entityType, Level level) {
		super(entityType, level);
		this.livedTicks = 0;
	}

	@Override
	@Nullable
	public ItemStack getPickResult() {
		ItemStack scroll = ModItems.HEART_OF_THE_SEA_SCROLL.get().getDefaultInstance();
		scroll.enchant(ModEnchantments.CONJURATION.get(), 1);
		return scroll;
	}

	@Override
	protected void defineSynchedData() {
		super.defineSynchedData();
		this.entityData.define(DATA_OWNER, Optional.empty());
	}

	@Override
	public void addAdditionalSaveData(CompoundTag compound) {
		super.addAdditionalSaveData(compound);
		if (getOwnerUUID() != null) {
			compound.putUUID("Owner", getOwnerUUID());
		}
		compound.putInt("LivedTicks", livedTicks);
	}

	@Override
	public void readAdditionalSaveData(CompoundTag compound) {
		super.readAdditionalSaveData(compound);
		if (compound.hasUUID("Owner")) {
			setOwnerUUID(compound.getUUID("Owner"));
		}
		livedTicks = compound.getInt("LivedTicks");
	}

	public float getBeamProgress(float partialTick) {
		if (livedTicks > 40) return 0f;

		float totalTicks = 40f;
		float currentTick = livedTicks + partialTick;
		float progress = currentTick / totalTicks;

		// Анимация: быстро увеличиваем до 1, затем плавно уменьшаем до 0
		if (progress < 0.25f) {
			// Быстрое появление: 0 → 1 за 0.5 секунды (10 тиков)
			return progress * 4f;
		} else {
			// Плавное исчезновение: 1 → 0 за 1.5 секунды (30 тиков)
			return 1f - ((progress - 0.25f) / 0.75f);
		}
	}
	public float getBeamRotationAngle(float partialTick) {
		if (livedTicks > 40) return 0f;

		float currentTick = livedTicks + partialTick;
		// Один полный оборот за 2 секунды (40 тиков)
		return (currentTick / 40f) * 360f;
	}

	@Nullable
	public UUID getOwnerUUID() {
		return this.entityData.get(DATA_OWNER).orElse(null);
	}

	public void setOwnerUUID(@Nullable UUID uuid) {
		this.entityData.set(DATA_OWNER, Optional.ofNullable(uuid));
	}

	@Override
	protected void registerGoals() {
		super.registerGoals();
		this.goalSelector.getAvailableGoals().removeIf(
				prioritizedGoal -> prioritizedGoal.getGoal() instanceof DolphinSwimWithPlayerGoal
		);
		this.goalSelector.addGoal(2, new DolphinFollowOwnerGoal(this, 4.0D));
	}

	@Override
	public void tick() {
		super.tick();
		livedTicks++;
		if (!level().isClientSide) {
			if (livedTicks >= 1200) {
				vanishWithEffects();
			}
		}
	}

	@Override
	public boolean hurt(DamageSource source, float amount) {
		if (!level().isClientSide) {
			if (source.getEntity() instanceof Player player) {
				if (player.getUUID().equals(getOwnerUUID())) {
					// Проверяем, что в основной руке игрока каменный меч
					if (player.getMainHandItem().is(ModItems.HEART_OF_THE_SEA_SCROLL.get()) && player.getMainHandItem().getEnchantmentLevel(ModEnchantments.CONJURATION.get())>0) {
						vanishWithEffects();
						return true;
					}
				}
			}
		}
		return super.hurt(source, amount);
	}

	@Override
	public void die(DamageSource source) {
		// Отменяем стандартную смерть, чтобы не было дропа и звуков смерти
		if (!level().isClientSide && !isRemoved()) {
			vanishWithEffects();
		}
	}

	private void vanishWithEffects() {
		if (!level().isClientSide) {
			ServerLevel serverLevel = (ServerLevel) level();
			serverLevel.playSound(null, blockPosition(),
					RPGSounds.SPELL_CONJURATION_STOP.get(),
					SoundSource.NEUTRAL, 1.0F, 1.0F);
			serverLevel.sendParticles(ModParticles.SUMMON_REVOKE.get(),
					getX(), getY() + 0.5, getZ(), 1, 0, 0, 0, 0);
		}
		discard();
	}

	// Цель следования за владельцем и дарения эффекта грации дельфина
	static class DolphinFollowOwnerGoal extends Goal {
		private final ConjuredDolphin dolphin;
		private final double speedModifier;
		@Nullable
		private Player owner;

		public DolphinFollowOwnerGoal(ConjuredDolphin dolphin, double speedModifier) {
			this.dolphin = dolphin;
			this.speedModifier = speedModifier;
			this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
		}

		@Override
		public boolean canUse() {
			UUID ownerId = dolphin.getOwnerUUID();
			if (ownerId == null) return false;
			Player player = dolphin.level().getPlayerByUUID(ownerId);
			if (player == null) return false;
			if (!player.isSwimming()) return false;
			if (dolphin.getTarget() == player) return false;
			this.owner = player;
			return true;
		}

		@Override
		public boolean canContinueToUse() {
			return owner != null && owner.isSwimming() && dolphin.distanceToSqr(owner) < 256.0D;
		}

		@Override
		public void start() {
			if (owner != null) {
				owner.addEffect(new MobEffectInstance(MobEffects.DOLPHINS_GRACE, 100), dolphin);
			}
		}

		@Override
		public void stop() {
			owner = null;
			dolphin.getNavigation().stop();
		}

		@Override
		public void tick() {
			if (owner == null) return;
			dolphin.getLookControl().setLookAt(owner,
					(float) (dolphin.getMaxHeadYRot() + 20),
					(float) dolphin.getMaxHeadXRot());
			if (dolphin.distanceToSqr(owner) < 6.25D) {
				dolphin.getNavigation().stop();
			} else {
				dolphin.getNavigation().moveTo(owner, speedModifier);
			}
			if (owner.isSwimming() && owner.level().random.nextInt(6) == 0) {
				owner.addEffect(new MobEffectInstance(MobEffects.DOLPHINS_GRACE, 100), dolphin);
			}
		}
	}
}