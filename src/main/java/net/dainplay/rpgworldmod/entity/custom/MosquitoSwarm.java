package net.dainplay.rpgworldmod.entity.custom;

import net.dainplay.rpgworldmod.block.ModBlocks;
import net.dainplay.rpgworldmod.damage.ModDamageTypes;
import net.dainplay.rpgworldmod.data.tags.ModAdvancements;
import net.dainplay.rpgworldmod.effect.ModEffects;
import net.dainplay.rpgworldmod.sounds.RPGSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
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
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.vibrations.VibrationSystem;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fluids.FluidType;
import org.jetbrains.annotations.NotNull;

import java.util.EnumSet;

public class MosquitoSwarm extends Monster {
    private static final EntityDataAccessor<Byte> DATA_FLAGS_ID = SynchedEntityData.defineId(MosquitoSwarm.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Boolean> DATA_DEALT_DAMAGE = SynchedEntityData.defineId(MosquitoSwarm.class, EntityDataSerializers.BOOLEAN);

    public MosquitoSwarm(EntityType<? extends MosquitoSwarm> p_32219_, Level p_32220_) {
        super(p_32219_, p_32220_);
        this.moveControl = new FlyingMoveControl(this, 10, true);
        this.xpReward = 10;
    }

    public static boolean checkMosquitoSwarmSpawnRules(EntityType<MosquitoSwarm> mosquitoSwarm, LevelAccessor level, MobSpawnType type, BlockPos blockPos, RandomSource random) {
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
        this.goalSelector.addGoal(5, new MoveTowardsRestrictionGoal(this, 1.0D));
        this.goalSelector.addGoal(7, new WaterAvoidingRandomFlyingGoal(this, 1.0D));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, false));
        this.goalSelector.addGoal(2, new MoveTowardsTargetGoal(this, 0.9D, 16.0F));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 1D).add(Attributes.ATTACK_DAMAGE, 0.0D).add(Attributes.FLYING_SPEED, (double)1F).add(Attributes.MOVEMENT_SPEED, (double)1F).add(Attributes.FOLLOW_RANGE, 16.0D);
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

    /*@Override
    public boolean isAffectedByPotions() {
        return false;
    }*/

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

    @Override
    public boolean doHurtTarget(Entity pEntity) {
        if(pEntity instanceof Player player)
            if(!player.isDamageSourceBlocked(this.level().damageSources().mobAttack(this)) && !this.entityData.get(DATA_DEALT_DAMAGE)) {
                this.entityData.set(DATA_DEALT_DAMAGE, true);
                this.playSound(RPGSounds.GOLDEN_TOKEN_FAIL.get(), 1.0F, (this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.0F);
            }
        return super.doHurtTarget(pEntity);
    }


}


