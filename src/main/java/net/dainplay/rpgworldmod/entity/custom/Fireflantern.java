package net.dainplay.rpgworldmod.entity.custom;

import net.dainplay.rpgworldmod.RPGworldMod;
import net.dainplay.rpgworldmod.block.ModBlocks;
import net.dainplay.rpgworldmod.damage.ModDamageTypes;
import net.dainplay.rpgworldmod.data.tags.ModAdvancements;
import net.dainplay.rpgworldmod.effect.ModEffects;
import net.dainplay.rpgworldmod.sounds.RPGSounds;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
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
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.DataLayer;
import net.minecraft.world.level.gameevent.vibrations.VibrationSystem;
import net.minecraft.world.level.lighting.BlockLightEngine;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fluids.FluidType;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import java.util.List;

public class Fireflantern extends Monster {

    private static final EntityDataAccessor<Boolean> DATA_IS_FALLING = SynchedEntityData.defineId(Fireflantern.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> DATA_AIRBLOCKSBELOW = SynchedEntityData.defineId(Fireflantern.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_SHOULD_FALL = SynchedEntityData.defineId(Fireflantern.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_IS_LIT = SynchedEntityData.defineId(Fireflantern.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Float> DATA_FALLSPEED = SynchedEntityData.defineId(Fireflantern.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> DATA_DEALT_DAMAGE = SynchedEntityData.defineId(Fireflantern.class, EntityDataSerializers.BOOLEAN);

    public Fireflantern(EntityType<? extends Fireflantern> p_32219_, Level p_32220_) {
        super(p_32219_, p_32220_);
        this.moveControl = new FlyingMoveControl(this, 10, true);
        this.xpReward = 10;
    }

    public static boolean checkFireflanternSpawnRules(EntityType<Fireflantern> fireflantern, ServerLevelAccessor level, MobSpawnType type, BlockPos blockPos, RandomSource random) {


        for (int i = 1; i < 20; i++) {
            if (!level.isEmptyBlock(blockPos.above(i))) {
                return false;
            }
        }


        return level.canSeeSky(blockPos.above()) && checkMonsterSpawnRules(fireflantern,level,type,blockPos,random) && level.isEmptyBlock(blockPos) && (level.dayTime() % 24000) >= 13000 && (level.dayTime() % 24000) < 23000;
    }

    protected PathNavigation createNavigation(Level pLevel) {
        FlyingPathNavigation flyingpathnavigation = new FlyingPathNavigation(this, pLevel);
        flyingpathnavigation.setCanOpenDoors(false);
        flyingpathnavigation.setCanFloat(true);
        flyingpathnavigation.setCanPassDoors(true);
        return flyingpathnavigation;
    }

    protected void registerGoals() {
        this.targetSelector.addGoal(1, (new HurtByTargetGoal(this)).setAlertOthers());
        this.goalSelector.addGoal(7, new WaterAvoidingRandomFlyingGoal(this, 1.0D));
    }


    @Nullable
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor pLevel, DifficultyInstance pDifficulty, MobSpawnType pReason, @Nullable SpawnGroupData pSpawnData, @Nullable CompoundTag pDataTag) {

        this.setDeltaMovement(this.getDeltaMovement().add(0, 2, 0));
        return super.finalizeSpawn(pLevel, pDifficulty, pReason, pSpawnData, pDataTag);
    }
    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.ATTACK_DAMAGE, 6.0D).add(Attributes.FLYING_SPEED, (double)0.4F).add(Attributes.MOVEMENT_SPEED, (double)0.4F).add(Attributes.FOLLOW_RANGE, 16.0D);
    }

    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_IS_FALLING, false);
        this.entityData.define(DATA_AIRBLOCKSBELOW, 0);
        this.entityData.define(DATA_FALLSPEED, 0F);
        this.entityData.define(DATA_IS_LIT, false);
        this.entityData.define(DATA_SHOULD_FALL, false);
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

            if (entityData.get(DATA_AIRBLOCKSBELOW) > 12) {
                entityData.set(DATA_IS_FALLING, true);
            }
            else {

                Level level = this.level();
                BlockPos pos = this.blockPosition();
                AABB aabb = new AABB(pos).inflate(9);
                List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, aabb);

                for (LivingEntity entity : entities) {
                    if (!(entity instanceof Fireflantern) && !entity.isSpectator()) {
                        entity.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 140, 0));
                    }
                }
               this.setDeltaMovement(this.getDeltaMovement().add(0, 0.4, 0));
            }

            return super.hurt(pSource, pAmount);
        }
    }


    @Override
    public void die(net.minecraft.world.damagesource.@NotNull DamageSource damageSource) {
        if (damageSource.getEntity() instanceof ServerPlayer player && player.getLastDamageSource() == null && !this.entityData.get(DATA_DEALT_DAMAGE)) ModAdvancements.GOLDEN_KILL_FIREFLANTERN.trigger(player);
        if (!this.level().isClientSide()) {
            entityData.set(DATA_IS_FALLING, true);
            BlockPos deathPos = this.blockPosition();
            BlockPos lowestPos = findLowestPos(deathPos);
            spawnFuel(lowestPos);
        }
        super.die(damageSource);
    }

    private BlockPos findLowestPos(BlockPos startPos) {
        BlockPos currentPos = startPos;
        while (currentPos.getY() > level().getMinBuildHeight()) {
            BlockPos posBelow = currentPos.below();
            if (!(level().getBlockState(posBelow).isAir() || level().getBlockState(posBelow).canBeReplaced()) || level().getBlockState((posBelow)).is(Blocks.WATER))
            {
                return currentPos;
            }
            currentPos = posBelow;
        }
        return currentPos;
    }



    private void spawnFuel(BlockPos pos) {
        if(level().getBlockState(pos).getFluidState().isEmpty()){
            level().setBlock(pos, level().dimensionType().ultraWarm() ? Blocks.FIRE.defaultBlockState() : ModBlocks.ARBOR_FUEL_BLOCK.get().defaultBlockState(), 3);
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
    protected SoundEvent getAmbientSound() {
        return RPGSounds.FIREFLANTERN_AMBIENT.get();
    }

    protected SoundEvent getHurtSound(DamageSource pDamageSource) {
        return RPGSounds.FIREFLANTERN_HURT.get();
    }

    protected SoundEvent getDeathSound() {
        return RPGSounds.FIREFLANTERN_DEATH.get();
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

    @Override
    public void tick() {
        super.tick();
        updateAirBlocksBelow();
        //updateIsFirstBlockLit();
        updateFallingBehavior();
        handleFallDamage();
        moveEastward();
        setYRot(0.0F);
        setXRot(0.0F);
        spawnFallingParticles();
    }


    private void spawnFallingParticles() {
        if (entityData.get(DATA_IS_FALLING)) {
            Level level = this.level();
                double x = this.getX();
                double y = this.getY() + this.getBbHeight() / 2;
                double z = this.getZ();
                double speedX = this.getDeltaMovement().x * 0.1;
                double speedY = this.getDeltaMovement().y * 0.1;
                double speedZ = this.getDeltaMovement().z * 0.1;


                level.addParticle(ParticleTypes.FLAME, x, y, z, speedX, speedY, speedZ);
        }
    }
    private void moveEastward() {
        double speed = 0.01;
        if (entityData.get(DATA_AIRBLOCKSBELOW) > 12) {
            this.setDeltaMovement(this.getDeltaMovement().add(-speed, 0, 0));
        }
    }
    private void updateAirBlocksBelow() {
        entityData.set(DATA_AIRBLOCKSBELOW, 0);
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos(this.getX(), this.getY() - 1, this.getZ());
        while (this.level().isEmptyBlock(mutableBlockPos) && mutableBlockPos.getY() > this.level().getMinBuildHeight()) {
            entityData.set(DATA_AIRBLOCKSBELOW, entityData.get(DATA_AIRBLOCKSBELOW)+1);
            mutableBlockPos.move(Direction.DOWN);
            entityData.set(DATA_IS_LIT, this.level().getLightEmission(mutableBlockPos.above()) > 5);
        }
    }


    private void updateFallingBehavior() {
        if ((entityData.get(DATA_AIRBLOCKSBELOW) > 50 && !entityData.get(DATA_IS_LIT)) || entityData.get(DATA_IS_FALLING)) {
            if (!entityData.get(DATA_IS_FALLING)) this.playSound(RPGSounds.FIREFLANTERN_FALL.get(), 3.0F, 1.0F);
            entityData.set(DATA_SHOULD_FALL, true);
        } else {
            entityData.set(DATA_SHOULD_FALL, false);
        }


        if (entityData.get(DATA_SHOULD_FALL)) {
            entityData.set(DATA_FALLSPEED, entityData.get(DATA_FALLSPEED)+0.2F);
            if (entityData.get(DATA_FALLSPEED) > 2.0f) {
                entityData.set(DATA_FALLSPEED, 2.0F);
            }
            setDeltaMovement(getDeltaMovement().add(0, -entityData.get(DATA_FALLSPEED), 0));
            entityData.set(DATA_IS_FALLING, true);
        } else if(this.getLastDamageSource() == null) {
            entityData.set(DATA_FALLSPEED, 0F);
            if(entityData.get(DATA_AIRBLOCKSBELOW) < 50){
                setDeltaMovement(getDeltaMovement().add(0, 0.01, 0)); // Быстрый набор высоты
            }
            entityData.set(DATA_IS_FALLING, false);
        }
    }

    private void handleFallDamage() {
        if (this.onGround() && entityData.get(DATA_IS_FALLING)) {
            entityData.set(DATA_IS_FALLING, false);

            float damageAmount = (float) (entityData.get(DATA_FALLSPEED) * 5);
            if (damageAmount > 0) {
                Level level = this.level();
                if (!level.isClientSide) {
                    for (int i = 0; i < 50; i++) {
                        double x = this.getX() + (level.random.nextDouble() - 0.5) * 2.0;
                        double y = this.getY();
                        double z = this.getZ() + (level.random.nextDouble() - 0.5) * 2.0;

                        double speedX = (level.random.nextDouble() - 0.5) * 0.5;
                        double speedY = level.random.nextDouble() * 0.5;
                        double speedZ = (level.random.nextDouble() - 0.5) * 0.5;

                        ((ServerLevel) level).sendParticles(ParticleTypes.FLAME, x, y, z, 1, speedX, speedY, speedZ, 0.0);
                    }
                }
                List<LivingEntity> entities = this.level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(3.0));
                for (LivingEntity entity : entities) {
                    if (!entity.equals(this)) {
                        entity.hurt(damageSources().mobAttack(this), damageAmount);
                    }
                    if(entity instanceof ServerPlayer player)
                        if(!player.isDamageSourceBlocked(this.level().damageSources().mobAttack(this)) && !this.entityData.get(DATA_DEALT_DAMAGE)) {
                            this.entityData.set(DATA_DEALT_DAMAGE, true);
                             if (!hasGoldenKill(player)) this.playSound(RPGSounds.GOLDEN_TOKEN_FAIL.get(), 2.0F, 1.0F);
                        }
                }
                this.playSound(RPGSounds.FIREFLANTERN_IMPACT.get(), 1.0F, 1.0F);
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
            if (criterionId.equals("golden_kill_fireflantern")) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean isNoGravity() {
        return true;
    }

    @Override
    public void push(Entity entity) {}

    @Override
    protected boolean canRide(Entity entityIn) {
        return false;
    }

    @Override
    public boolean canBeCollidedWith() {
        return true;
    }
    public static boolean isEyeInAnyFluid(Entity entity) {
        FluidType fluidState = entity.getEyeInFluidType();
        return fluidState != net.minecraftforge.common.ForgeMod.EMPTY_TYPE.get();
    }

}


