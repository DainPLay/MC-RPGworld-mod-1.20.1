package net.dainplay.rpgworldmod.entity.custom;

import net.dainplay.rpgworldmod.RPGworldMod;
import net.dainplay.rpgworldmod.block.ModBlocks;
import net.dainplay.rpgworldmod.damage.ModDamageTypes;
import net.dainplay.rpgworldmod.data.tags.ModAdvancements;
import net.dainplay.rpgworldmod.effect.ModEffects;
import net.dainplay.rpgworldmod.sounds.RPGSounds;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ambient.Bat;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.entity.animal.Ocelot;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.vibrations.VibrationSystem;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fluids.FluidType;
import org.jetbrains.annotations.NotNull;

import java.util.EnumSet;

public class Mintobat extends Monster {
    private static final EntityDataAccessor<Byte> DATA_FLAGS_ID = SynchedEntityData.defineId(Mintobat.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Boolean> DATA_DEALT_DAMAGE = SynchedEntityData.defineId(Mintobat.class, EntityDataSerializers.BOOLEAN);

    public Mintobat(EntityType<? extends net.dainplay.rpgworldmod.entity.custom.Mintobat> p_32219_, Level p_32220_) {
        super(p_32219_, p_32220_);
        this.moveControl = new FlyingMoveControl(this, 10, true);
        this.xpReward = 10;
    }

    public static boolean checkMintobatSpawnRules(EntityType<Mintobat> mintobat, LevelAccessor level, MobSpawnType type, BlockPos blockPos, RandomSource random) {
        return hasRieLeavesAboveBlock(level, blockPos) && (level.dayTime() % 24000) >= 0 && (level.dayTime() % 24000) < 12300 && !level.getLevelData().isRaining() && !level.getLevelData().isThundering();
    }
    public static boolean hasRieLeavesAboveBlock(LevelAccessor world, BlockPos pos) {
        int y = pos.getY() + 1;  // Start 1 block above the given pos

        while (y < world.getMaxBuildHeight()) {  // Iterate upwards till the world's build limit
            BlockPos blockAbovePos = new BlockPos(pos.getX(), y, pos.getZ());
            BlockState blockState = world.getBlockState(blockAbovePos);

            if (!blockState.isAir()) {  // Check if it's not air
                Block blockAbove = blockState.getBlock();
                return blockAbove == ModBlocks.RIE_LEAVES.get();  // Check if it's oak leaves
            }

            y++;
        }

        return false;  // If no non-air block was found above the initial BlockPos
    }

    protected PathNavigation createNavigation(Level pLevel) {
        FlyingPathNavigation flyingpathnavigation = new FlyingPathNavigation(this, pLevel);
        flyingpathnavigation.setCanOpenDoors(false);
        flyingpathnavigation.setCanFloat(true);
        flyingpathnavigation.setCanPassDoors(true);
        return flyingpathnavigation;
    }

    protected void registerGoals() {
        this.goalSelector.addGoal(4, new net.dainplay.rpgworldmod.entity.custom.Mintobat.MintobatAttackGoal(this));
        this.goalSelector.addGoal(5, new MoveTowardsRestrictionGoal(this, 1.0D));
        this.goalSelector.addGoal(7, new WaterAvoidingRandomFlyingGoal(this, 1.0D));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, (new HurtByTargetGoal(this)).setAlertOthers());
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.goalSelector.addGoal(9, new AvoidEntityGoal<>(this, Ocelot.class, 6.0F, 1.0D, 1.2D));
        this.goalSelector.addGoal(9, new AvoidEntityGoal<>(this, Cat.class, 6.0F, 1.0D, 1.2D));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.ATTACK_DAMAGE, 6.0D).add(Attributes.FLYING_SPEED, (double)0.4F).add(Attributes.MOVEMENT_SPEED, (double)0.4F).add(Attributes.FOLLOW_RANGE, 16.0D);
    }

    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_FLAGS_ID, (byte)0);
        this.entityData.define(DATA_DEALT_DAMAGE, false);
    }

    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putBoolean("Dealt damage", this.entityData.get(DATA_DEALT_DAMAGE));
    }

    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        this.entityData.set(DATA_DEALT_DAMAGE, compound.getBoolean("Dealt damage"));
    }

    public boolean hurt(DamageSource pSource, float pAmount) {
        if (this.isInvulnerableTo(pSource)) {
            return false;
        } else {
            if (!this.level().isClientSide) {
               this.setDeltaMovement(this.getDeltaMovement().add(0, 0.18, 0));
           }

            return super.hurt(pSource, pAmount);
        }
    }

    protected float getStandingEyeHeight(Pose pPose, EntityDimensions pSize) {
        return 0.5F;
    }
    public void travel(Vec3 pTravelVector) {
        if (this.isInWater()) {
            this.moveRelative(0.02F, pTravelVector);
            this.move(MoverType.SELF, this.getDeltaMovement());
            this.setDeltaMovement(this.getDeltaMovement().scale((double)0.8F));
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
            this.setDeltaMovement(this.getDeltaMovement().scale((double)f));
        }

        this.calculateEntityAnimation(false);
    }
    public boolean canBeAffected(MobEffectInstance pPotioneffect) {
        return pPotioneffect.getEffect() == ModEffects.MOSSIOSIS.get() ? false : super.canBeAffected(pPotioneffect);
    }
    protected SoundEvent getAmbientSound() {
        return RPGSounds.MINTOBAT_AMBIENT.get();
    }

    protected SoundEvent getHurtSound(DamageSource pDamageSource) {
        return RPGSounds.MINTOBAT_HURT.get();
    }

    protected SoundEvent getDeathSound() {
        return RPGSounds.MINTOBAT_DEATH.get();
    }

    /**
     * Called frequently so the entity can update its state every tick as required. For example, zombies and skeletons
     * use this to react to sunlight and start to burn.
     */
    public boolean causeFallDamage(float pFallDistance, float pMultiplier, DamageSource pSource) {
        return false;
    }

    protected void checkFallDamage(double pY, boolean pOnGround, BlockState pState, BlockPos pPos) {
    }


    public void die(@NotNull DamageSource damageSource) {
        if (damageSource.getEntity() instanceof ServerPlayer player && player.getLastDamageSource() == null && !this.entityData.get(DATA_DEALT_DAMAGE)) ModAdvancements.GOLDEN_KILL_MINTOBAT.trigger(player);
        super.die(damageSource);
    }
    public boolean isCharged() {
        return (this.entityData.get(DATA_FLAGS_ID) & 1) != 0;
    }

    void setCharged(boolean pOnFire) {
        byte b0 = this.entityData.get(DATA_FLAGS_ID);
        if (pOnFire) {
            b0 = (byte)(b0 | 1);
        } else {
            b0 = (byte)(b0 & -2);
        }

        this.entityData.set(DATA_FLAGS_ID, b0);
    }


    public static boolean hasGoldenKill(ServerPlayer player) {
        PlayerAdvancements advancements = player.getAdvancements();

        Advancement advancement = player.server.getAdvancements().getAdvancement(RPGworldMod.prefix("golden_kill_all_rie_weald_mobs"));
        AdvancementProgress progress = advancements.getOrStartProgress(advancement);

        if (progress.isDone()) {
            return true;
        }

        Iterable<String> completedCriteria = progress.getCompletedCriteria();

        for (String criterionId : completedCriteria) {
            if (criterionId.equals("golden_kill_mintobat")) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean doHurtTarget(Entity pEntity) {
        if(pEntity instanceof ServerPlayer player && !hasGoldenKill(player))
            if(!player.isDamageSourceBlocked(this.level().damageSources().mobAttack(this)) && !this.entityData.get(DATA_DEALT_DAMAGE)) {
                this.entityData.set(DATA_DEALT_DAMAGE, true);
                this.playSound(RPGSounds.GOLDEN_TOKEN_FAIL.get(), 2.0F, 1.0F);
            }
        return super.doHurtTarget(pEntity);
    }

    static class MintobatAttackGoal extends Goal {
        private final net.dainplay.rpgworldmod.entity.custom.Mintobat mintobat;
        private int attackStep;
        private int attackTime;
        private int lastSeen;
        protected final RandomSource random = RandomSource.create();

        public MintobatAttackGoal(net.dainplay.rpgworldmod.entity.custom.Mintobat p_32247_) {
            this.mintobat = p_32247_;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        /**
         * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
         * method as well.
         */
        public boolean canUse() {
            LivingEntity livingentity = this.mintobat.getTarget();
            return livingentity != null && livingentity.isAlive() && this.mintobat.canAttack(livingentity);
        }

        /**
         * Execute a one shot task or start executing a continuous task
         */
        public void start() {
            this.attackStep = 0;
        }

        /**
         * Reset the task's internal state. Called when this task is interrupted by another one
         */
        public void stop() {
            this.mintobat.setCharged(false);
            this.lastSeen = 0;
        }

        public boolean requiresUpdateEveryTick() {
            return true;
        }

        /**
         * Keep ticking a continuous task that has already been started
         */
        public void tick() {
            --this.attackTime;
            LivingEntity livingentity = this.mintobat.getTarget();
            if (livingentity != null) {
                boolean flag = this.mintobat.getSensing().hasLineOfSight(livingentity);
                if (flag) {
                    this.lastSeen = 0;
                } else {
                    ++this.lastSeen;
                }

                double d0 = this.mintobat.distanceToSqr(livingentity);
                if (d0 < 4.0D) {
                    if (!flag) {
                        return;
                    }

                    if (this.attackTime <= 0) {
                        this.attackTime = 20;
                        this.mintobat.doHurtTarget(livingentity);
                    }

                    this.mintobat.getMoveControl().setWantedPosition(livingentity.getX(), livingentity.getY(), livingentity.getZ(), 1.0D);
                } else if (d0 < this.getFollowDistance() * this.getFollowDistance() && flag) {
                    if (this.attackTime <= 0) {
                        ++this.attackStep;
                        if (this.attackStep == 1) {
                            this.attackTime = 20;
                        } else if (this.attackStep <= 2) {
                            this.attackTime = 20;
                        } else {
                            this.attackTime = 120;
                            this.attackStep = 0;
                            this.mintobat.setCharged(false);
                        }

                        if (this.attackStep > 1) {
                            if (!this.mintobat.isSilent()) {
                                this.mintobat.playSound(RPGSounds.MINTOBAT_ATTACK.get(), 1.0F, (this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.0F);
                            }


                            this.mintobat.setCharged(true);
                            ((ServerLevel)this.mintobat.level()).sendParticles(ParticleTypes.SONIC_BOOM, this.mintobat.getX(), this.mintobat.getY(0.5D), this.mintobat.getZ(), 1, 0.0D, 0.0D, 0.0D, 0.0D);
                            this.mintobat.level().getEntities(this.mintobat, this.mintobat.getBoundingBox().inflate(this.getFollowDistance()),
                                            target ->
                                            target instanceof LivingEntity && !(target instanceof Mintobat) && !(target instanceof Bat))
                                    .forEach(target -> {
                                        BlockPos entityPos = new BlockPos(this.mintobat.getBlockX(), this.mintobat.getBlockY(), this.mintobat.getBlockZ());
                                        Vec3 targetPos = new Vec3(target.getX(), target.getEyeY(), target.getZ());
                                        if (!VibrationSystem.Listener.isOccluded(this.mintobat.level(), entityPos.getCenter(), targetPos)) {
                                            double scaledDamage = this.mintobat.getAttributeValue(Attributes.ATTACK_DAMAGE) * (1 - Math.min(1, this.mintobat.distanceTo(target) / this.getFollowDistance()));
                                            if (isEyeInAnyFluid(this.mintobat) && !isEyeInAnyFluid(target)) scaledDamage /= 4;
                                            if (isEyeInAnyFluid(target) && !isEyeInAnyFluid(this.mintobat)) scaledDamage /= 4;
                                            (target).hurt(ModDamageTypes.getEntityDamageSource(this.mintobat.level(), ModDamageTypes.SCREAM, this.mintobat), (float) scaledDamage);

                                            if(target instanceof ServerPlayer player && !hasGoldenKill(player))
                                                if(!player.isDamageSourceBlocked(this.mintobat.level().damageSources().mobAttack(this.mintobat)) && !this.mintobat.entityData.get(DATA_DEALT_DAMAGE)) {
                                                    this.mintobat.entityData.set(DATA_DEALT_DAMAGE, true);
                                                    this.mintobat.playSound(RPGSounds.GOLDEN_TOKEN_FAIL.get(), 2.0F, 1.0F);
                                                }
                                        }
                                    });
                        }
                    }

                    this.mintobat.getLookControl().setLookAt(livingentity, 10.0F, 10.0F);
                } else if (this.lastSeen < 5) {
                    this.mintobat.getMoveControl().setWantedPosition(livingentity.getX(), livingentity.getY(), livingentity.getZ(), 1.0D);
                }

                super.tick();
            }
        }

        private double getFollowDistance() {
            return this.mintobat.getAttributeValue(Attributes.FOLLOW_RANGE);
        }
    }

    public static boolean isEyeInAnyFluid(Entity entity) {
        FluidType fluidState = entity.getEyeInFluidType();
        return fluidState != net.minecraftforge.common.ForgeMod.EMPTY_TYPE.get();
    }

}


