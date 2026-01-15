package net.dainplay.rpgworldmod.entity.custom;

import net.dainplay.rpgworldmod.RPGworldMod;
import net.dainplay.rpgworldmod.block.ModBlocks;
import net.dainplay.rpgworldmod.data.tags.GoldenKillBurr_purrTrigger;
import net.dainplay.rpgworldmod.data.tags.ModAdvancements;
import net.dainplay.rpgworldmod.data.tags.WealdBladeGesturesTrigger;
import net.dainplay.rpgworldmod.effect.ModEffects;
import net.dainplay.rpgworldmod.entity.ModEntities;
import net.dainplay.rpgworldmod.entity.projectile.BurrSpikeEntity;
import net.dainplay.rpgworldmod.item.ModItems;
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
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.TimeUtil;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.animal.Ocelot;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Vex;
import net.minecraft.world.entity.monster.ZombifiedPiglin;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.function.Predicate;

public class Burr_purr extends Monster implements NeutralMob {

    public static final EntityDataAccessor<Boolean> DATA_IS_SPINNING = SynchedEntityData.defineId(Burr_purr.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<Boolean> DATA_IS_MOVING = SynchedEntityData.defineId(Burr_purr.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<Boolean> DATA_CAN_SEE_TARGET = SynchedEntityData.defineId(Burr_purr.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<Integer> DATA_SPIN_TIMER = SynchedEntityData.defineId(Burr_purr.class, EntityDataSerializers.INT);
    public static final EntityDataAccessor<Integer> DATA_DASH_TIMER = SynchedEntityData.defineId(Burr_purr.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_REMAINING_ANGER_TIME = SynchedEntityData.defineId(Burr_purr.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_REMAINING_SPIKES = SynchedEntityData.defineId(Burr_purr.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_DEALT_DAMAGE = SynchedEntityData.defineId(Burr_purr.class, EntityDataSerializers.BOOLEAN);
    private static final int SPIN_UP_TIME = 30;  // Время, в течение которого моб ускоряется, прежде чем сделать рывок.
    private Vec3 dashDirection = Vec3.ZERO; // Направление рывка.
    @Nullable
    private UUID persistentAngerTarget;
    private static final UniformInt PERSISTENT_ANGER_TIME = TimeUtil.rangeOfSeconds(20, 39);
    private int remainingPersistentAngerTime;
    public Burr_purr(EntityType<? extends Burr_purr> p_32219_, Level p_32220_) {
        super(p_32219_, p_32220_);
        this.getNavigation().setCanFloat(true);
        this.xpReward = 4;
    }

    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(0, new ClimbOnTopOfPowderSnowGoal(this, this.level()));
        this.goalSelector.addGoal(2, new MoveTowardsTargetGoal(this, 0.9D, 12.0F));
        this.goalSelector.addGoal(1, new LookAtPlayerGoal(this, Player.class, 6));
        this.goalSelector.addGoal(2, new RandomLookAroundGoal(this));
        this.goalSelector.addGoal(3, new RandomStrollGoal(this, 1.0));
        this.targetSelector.addGoal(1, (new HurtByTargetGoal(this)).setAlertOthers());
        this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(2, new AvoidEntityGoal<>(this, Mintobat.class, 16.0F, 1.0D, 1.2D));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 12.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.3D)
                .add(Attributes.FOLLOW_RANGE, 16.0D)
                .add(Attributes.ARMOR, 5) // Защита
                .add(Attributes.ATTACK_DAMAGE, 2.0);
    }

    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_IS_SPINNING, false);
        this.entityData.define(DATA_IS_MOVING, false);
        this.entityData.define(DATA_CAN_SEE_TARGET, false);
        this.entityData.define(DATA_SPIN_TIMER, 0);
        this.entityData.define(DATA_DASH_TIMER, 0);
        this.entityData.define(DATA_REMAINING_ANGER_TIME, 0);
        this.entityData.define(DATA_REMAINING_SPIKES, 3);
        this.entityData.define(DATA_DEALT_DAMAGE, false);
    }

    @Nullable
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor pLevel, DifficultyInstance pDifficulty, MobSpawnType pReason, @Nullable SpawnGroupData pSpawnData, @Nullable CompoundTag pDataTag) {
        this.entityData.set(DATA_REMAINING_SPIKES, 3+this.random.nextInt(6));
        return super.finalizeSpawn(pLevel, pDifficulty, pReason, pSpawnData, pDataTag);
    }

    public boolean causeFallDamage(float pFallDistance, float pMultiplier, DamageSource pSource) {
        return !this.getEntityData().get(DATA_IS_SPINNING);
    }

    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putBoolean("IsSpinning", this.entityData.get(DATA_IS_SPINNING));
        compound.putInt("SpinTimer", this.entityData.get(DATA_SPIN_TIMER));
        compound.putInt("DashTimer", this.entityData.get(DATA_DASH_TIMER));
        compound.putInt("Spikes", this.entityData.get(DATA_REMAINING_SPIKES));
        compound.putBoolean("Dealt damage", this.entityData.get(DATA_DEALT_DAMAGE));
    }

    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        this.entityData.set(DATA_IS_SPINNING, compound.getBoolean("IsSpinning"));
        this.entityData.set(DATA_SPIN_TIMER, compound.getInt("SpinTimer"));
        this.entityData.set(DATA_DASH_TIMER, compound.getInt("DashTimer"));
        this.entityData.set(DATA_REMAINING_SPIKES, compound.getInt("Spikes"));
        this.entityData.set(DATA_DEALT_DAMAGE, compound.getBoolean("Dealt damage"));
    }
    public static boolean checkBurr_purrSpawnRules(EntityType<Burr_purr> burr_purr, LevelAccessor level, MobSpawnType type, BlockPos blockPos, RandomSource random) {
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
            if (criterionId.equals("golden_kill_burr_purr")) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean doHurtTarget(Entity pEntity) {
        if (pEntity instanceof ServerPlayer player)
            if (!player.isDamageSourceBlocked(this.damageSources().thorns(this)) && !this.entityData.get(DATA_DEALT_DAMAGE)) {
                this.entityData.set(DATA_DEALT_DAMAGE, true);
                if (!hasGoldenKill(player)) this.playSound(RPGSounds.GOLDEN_TOKEN_FAIL.get(), 2.0F, 1.0F);
            }
        return super.doHurtTarget(pEntity);
    }
    public static void spawnParticlesBehind(Entity entity, int amount, double distance) {
        Vec3 lookVector = entity.getLookAngle(); // Get the entity's look vector
        Vec3 oppositeVector = lookVector.scale(-1); // Get the opposite direction of the look vector.

        for(int i = 0; i < amount; i++){
            // Use random offsets to create particle spread.
            double randomX = (Math.random() - 0.5) * 0.5;
            double randomZ = (Math.random() - 0.5) * 0.5;

            //Use the opposite vector to create an offset behind the entity.
            Vec3 particleOffset = oppositeVector.scale(distance).add(randomX, 0.5, randomZ);

            //Spawn our particle at the rotated offset of the entity.
            entity.level().addParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE,
                    entity.getX() + particleOffset.x,
                    entity.getY() + particleOffset.y,
                    entity.getZ() + particleOffset.z, 0, 0.01, 0);

        }


    }

    @Override
    public void tick() {
        super.tick();
        this.getEntityData().set(DATA_CAN_SEE_TARGET, canEntitySeeEntity(getTarget()));
        if (this.getDeltaMovement().horizontalDistanceSqr() > 0.0001 && this.onGround()) this.getEntityData().set(DATA_IS_MOVING, true);
        else this.getEntityData().set(DATA_IS_MOVING, false);
        if(this.getTarget() instanceof Player player && player.isCreative()) this.setTarget(null);
        if(!this.onGround() && this.entityData.get(DATA_DASH_TIMER) == 0) this.entityData.set(DATA_IS_SPINNING, false);
        if (this.entityData.get(DATA_DASH_TIMER) > 0) this.entityData.set(DATA_DASH_TIMER, this.entityData.get(DATA_DASH_TIMER)-1);

        if (this.entityData.get(DATA_DASH_TIMER) > 0 || this.entityData.get(DATA_SPIN_TIMER) > 0) this.entityData.set(DATA_IS_SPINNING, true);
        else this.entityData.set(DATA_IS_SPINNING, false);
        if ((this.entityData.get(DATA_IS_SPINNING) || this.getTarget() != null) && (this.onGround() || this.entityData.get(DATA_DASH_TIMER) > 0)) {
            this.spinTick();
        }
    }

    public boolean canEntitySeeEntity(Entity target) {
        if(target == null) return false;
        if (this.level() != target.level()) return false; // Different dimensions

        // Handle invisibilty effect
        if (target instanceof LivingEntity && ((LivingEntity) target).isInvisible()) return false;

        Level world = this.level();
        Vec3 start = this.getEyePosition();
        Vec3 end = target.getEyePosition();


        BlockHitResult blockHitResult = world.clip(
                new ClipContext(start, end,
                        ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this)
        );

        return blockHitResult.getType() == HitResult.Type.MISS; // If we hit nothing, the view is clear
    }
    private void spinTick() {
        if (this.getTarget() != null && this.entityData.get(DATA_CAN_SEE_TARGET)) {
            double d2 = this.getTarget().getX() - this.getX();
            double d1 = this.getTarget().getZ() - this.getZ();
            this.setYRot(-((float)Mth.atan2(d2, d1)) * (180F / (float)Math.PI));
            this.yBodyRot = this.getYRot();
            this.alertOthers();
        }
        List<LivingEntity> nearbyEntities = this.level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox(), (livingEntity) -> livingEntity != this);
        if (!nearbyEntities.isEmpty()) {
            for(LivingEntity livingentity : nearbyEntities){
                if(!(livingentity instanceof Burr_purr)) {
                    this.setDeltaMovement(this.getDeltaMovement().scale(-0.2D));
                    this.doHurtTarget(livingentity);
                    if(!livingentity.isDamageSourceBlocked(this.level().damageSources().mobAttack(this))) {
                        this.playSound(SoundEvents.THORNS_HIT, 1.0F, (this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.0F);
                    }
                    else  {
                        if(livingentity instanceof Player player) {
                            this.playSound(SoundEvents.SHIELD_BLOCK, 1.0F, (this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.0F);
                            this.DamageShield(player, player.isUsingItem() ? player.getUseItem() : ItemStack.EMPTY);
                        }
                        if (this.entityData.get(DATA_REMAINING_SPIKES) > 0) {
                            Random random = new Random();
                            if (random.nextInt(3) == 1) {
                                this.spawnAtLocation(ModItems.BURR_SPIKE.get());
                                this.entityData.set(DATA_REMAINING_SPIKES, this.entityData.get(DATA_REMAINING_SPIKES) - 1);
                                if(!this.level().isClientSide && livingentity instanceof ServerPlayer player){
                                    if(this.entityData.get(DATA_REMAINING_SPIKES) == 0)
                                        ModAdvancements.GET_ALL_BURR_PURR_SPIKES.trigger(player);
                                }
                            }
                        }
                    }
                }
            }
        }

        if (this.entityData.get(DATA_SPIN_TIMER) < SPIN_UP_TIME) {
            if (this.entityData.get(DATA_DASH_TIMER) == 0 && this.entityData.get(DATA_CAN_SEE_TARGET)) {
                if (this.entityData.get(DATA_SPIN_TIMER) == 0)
                    this.level().playSound(null, this, RPGSounds.BURR_PURR_SPIN.get(), SoundSource.NEUTRAL, 1.0F, (this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.0F);
                this.entityData.set(DATA_SPIN_TIMER, this.entityData.get(DATA_SPIN_TIMER) + 1);
                this.getNavigation().stop();
            }
            else if ( this.entityData.get(DATA_SPIN_TIMER) > 0)
                this.entityData.set(DATA_SPIN_TIMER, this.entityData.get(DATA_SPIN_TIMER) - 1);


        }
        else
            if (this.getTarget() == null || this.getTarget().isDeadOrDying() || !this.entityData.get(DATA_CAN_SEE_TARGET)){
                    this.entityData.set(DATA_IS_SPINNING, false);
                    this.entityData.set(DATA_SPIN_TIMER, 0);
            } else
                if (this.entityData.get(DATA_SPIN_TIMER) == SPIN_UP_TIME) {
                    // Начинаем рывок.
                    this.entityData.set(DATA_SPIN_TIMER, 0);
                    this.entityData.set(DATA_DASH_TIMER, 40);
                    this.dashDirection = this.getTarget().position().subtract(this.position()).normalize();
                    this.dash();
            }
    }

    private void dash() {
        this.playSound(RPGSounds.BURR_PURR_DASH.get(), 1.0F, (this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.0F);
            double speedMultiplier = 3.0;
            this.setDeltaMovement(this.dashDirection.scale(speedMultiplier)); // Рывок в направлении цели
            this.entityData.set(DATA_IS_SPINNING, false);
    }



    public void DamageShield(Player pPlayer, ItemStack pPlayerItemStack) {
        if (!pPlayerItemStack.isEmpty() && pPlayerItemStack.is(Items.SHIELD)) {
            pPlayerItemStack.hurtAndBreak(1, this, (p_43296_) -> {
                p_43296_.broadcastBreakEvent(EquipmentSlot.MAINHAND);
            });
        }

    }
    /**
     * Sets the active target the Goal system uses for tracking
     */
    public void setTarget(@Nullable LivingEntity pTarget) {
        if((pTarget instanceof Player player && player.isCreative()) || pTarget instanceof Mintobat) return;
        super.setTarget(pTarget);
    }

    private void alertOthers() {
        double d0 = this.getAttributeValue(Attributes.FOLLOW_RANGE);
        AABB aabb = AABB.unitCubeFromLowerCorner(this.position()).inflate(d0, 10.0D, d0);
        this.level().getEntitiesOfClass(Burr_purr.class, aabb, EntitySelector.NO_SPECTATORS).stream().filter((p_34463_) -> p_34463_ != this).filter((p_289465_) -> p_289465_.getTarget() == null).filter((p_289463_) -> !p_289463_.isAlliedTo(this.getTarget())).forEach((p_289464_) -> {
            p_289464_.setTarget(this.getTarget());
        });
    }
    @Override
    public boolean hurt(DamageSource pSource, float pAmount) {
        if (this.level().isClientSide) {
            return false;
        } else {
            if (!pSource.is(DamageTypeTags.AVOIDS_GUARDIAN_THORNS) && !pSource.is(DamageTypes.THORNS)) {
                Entity entity = pSource.getDirectEntity();
                if (entity instanceof LivingEntity livingentity) {
                    livingentity.hurt(this.damageSources().thorns(this), 2.0F);
                    this.setTarget(livingentity);
                    if (livingentity instanceof ServerPlayer player)
                        if (!player.isDamageSourceBlocked(this.damageSources().thorns(this)) && !this.entityData.get(DATA_DEALT_DAMAGE)) {
                            this.entityData.set(DATA_DEALT_DAMAGE, true);
                            if (!hasGoldenKill(player)) this.playSound(RPGSounds.GOLDEN_TOKEN_FAIL.get(), 2.0F, 1.0F);
                        }
                }
            }

            return super.hurt(pSource, pAmount);
        }
    }

    @Override
    public void die(@NotNull DamageSource damageSource) {
        super.die(damageSource);
        if (damageSource.getEntity() instanceof ServerPlayer player && player.getLastDamageSource() == null && !this.entityData.get(DATA_DEALT_DAMAGE)) ModAdvancements.GOLDEN_KILL_BURR_PURR.trigger(player);
        if (!this.level().isClientSide) {
            Random random = new Random();
            for (int i = 0; i < 8; i++) {
                double x = random.nextDouble() * 2 - 1;
                double y = random.nextDouble() * 2;
                double z = random.nextDouble() * 2 - 1;
                Vec3 dir = new Vec3(x,y,z).normalize();
                BurrSpikeEntity arrow = new BurrSpikeEntity(ModEntities.BURR_SPIKE_PROJECTILE.get(), this.getX(), this.getY() + 1 , this.getZ(), this.level());
                arrow.pickup = AbstractArrow.Pickup.DISALLOWED;
                arrow.shoot(dir.x, dir.y, dir.z, 0.3f, 1);
                this.level().addFreshEntity(arrow);

            }
        }
    }

    protected float getStandingEyeHeight(Pose pPose, EntityDimensions pSize) {
        return 0.1F;
    }

    protected SoundEvent getAmbientSound() {
        return RPGSounds.BURR_PURR_AMBIENT.get();
    }

    protected SoundEvent getHurtSound(DamageSource pDamageSource) {
        return RPGSounds.BURR_PURR_HURT.get();
    }

    protected SoundEvent getDeathSound() {
        return RPGSounds.BURR_PURR_DEATH.get();
    }

    /**
     * Called frequently so the entity can update its state every tick as required. For example, zombies and skeletons
     * use this to react to sunlight and start to burn.
     */


    public int getRemainingPersistentAngerTime() {
        return this.entityData.get(DATA_REMAINING_ANGER_TIME);
    }

    public void setRemainingPersistentAngerTime(int pTime) {
        this.entityData.set(DATA_REMAINING_ANGER_TIME, pTime);
    }

    public void startPersistentAngerTimer() {
        this.setRemainingPersistentAngerTime(PERSISTENT_ANGER_TIME.sample(this.random));
    }

    @Nullable
    public UUID getPersistentAngerTarget() {
        return this.persistentAngerTarget;
    }

    public void setPersistentAngerTarget(@Nullable UUID pTarget) {
        this.persistentAngerTarget = pTarget;
    }


}


