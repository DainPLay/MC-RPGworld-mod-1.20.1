package net.dainplay.rpgworldmod.entity.custom;

import net.dainplay.rpgworldmod.block.ModBlocks;
import net.dainplay.rpgworldmod.data.tags.ModAdvancements;
import net.dainplay.rpgworldmod.entity.ModEntities;
import net.dainplay.rpgworldmod.entity.projectile.BurrSpikeEntity;
import net.dainplay.rpgworldmod.sounds.RPGSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ToolActions;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Predicate;

public class Drillhog extends Monster {

    private static final EntityDataAccessor<Boolean> DATA_DEALT_DAMAGE = SynchedEntityData.defineId(Drillhog.class, EntityDataSerializers.BOOLEAN);
    private static final Predicate<Entity> AGGRO_PLAYERS = (player) ->
            (player.isDiscrete() && EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(player));

    private int attackAnimationRemainingTicks;
    public Drillhog(EntityType<? extends Drillhog> p_32219_, Level p_32220_) {
        super(p_32219_, p_32220_);
        this.xpReward = 10;
    }

    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.0D, false));
        this.goalSelector.addGoal(2, new MoveTowardsTargetGoal(this, 0.9D, 12.0F));
        this.goalSelector.addGoal(1, new LookAtPlayerGoal(this, Player.class, 6));
        this.goalSelector.addGoal(2, new RandomLookAroundGoal(this));
        this.goalSelector.addGoal(3, new RandomStrollGoal(this, 1.0));
        this.targetSelector.addGoal(1, (new HurtByTargetGoal(this)).setAlertOthers());
        this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(10, new Drillhog.DrillhogStripWoodGoal((double)1.2F, 12, 1));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true, false));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Drillhog.class, true, false));
        this.goalSelector.addGoal(2, new AvoidEntityGoal<>(this, Mintobat.class, 16.0F, 1.0D, 1.2D));
    }


    protected void defineSynchedData() {
        super.defineSynchedData();
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
    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 24.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.3D)
                .add(Attributes.MOVEMENT_SPEED, (double)0.3F)
                .add(Attributes.KNOCKBACK_RESISTANCE, (double)0.6F)
                .add(Attributes.ATTACK_KNOCKBACK, 1.0D)
                .add(Attributes.ATTACK_DAMAGE, 4.0D);
    }
    public void aiStep() {
        if (this.attackAnimationRemainingTicks > 0) {
            --this.attackAnimationRemainingTicks;
        }

        super.aiStep();
    }

    public void handleEntityEvent(byte pId) {
        if (pId == 4) {
            this.attackAnimationRemainingTicks = 10;
            this.playSound(RPGSounds.DRILLHOG_ATTACK.get(), 1.0F, this.getVoicePitch());
            this.playSound(RPGSounds.DRILLHOG_DRILL.get(), 1.0F, this.getVoicePitch());
        } else {
            super.handleEntityEvent(pId);
        }

    }

    public class DrillhogStripWoodGoal extends MoveToBlockGoal {
        protected int ticksWaited;

        public DrillhogStripWoodGoal(double p_28675_, int p_28676_, int p_28677_) {
            super(Drillhog.this, p_28675_, p_28676_, p_28677_);
        }

        public double acceptedDistance() {
            return 2.0D;
        }

        public boolean shouldRecalculatePath() {
            return this.tryTicks % 100 == 0;
        }

        /**
         * Return true to set given position as destination
         */
        protected boolean isValidTarget(LevelReader pLevel, BlockPos pPos) {
            BlockState blockstate = pLevel.getBlockState(pPos);
            return blockstate.is(BlockTags.LOGS) && blockstate.hasProperty(BlockStateProperties.AXIS);
        }

        /**
         * Keep ticking a continuous task that has already been started
         */
        public void tick() {
            if (this.isReachedTarget()) {
                if (this.ticksWaited >= 40) {
                    this.onReachedTarget();
                } else {
                    ++this.ticksWaited;
                }
            } else if (!this.isReachedTarget() && Drillhog.this.random.nextFloat() < 0.05F) {
                Drillhog.this.playSound(RPGSounds.DRILLHOG_DRILL.get(), 1.0F, 1.0F);
            }

            super.tick();
        }

        protected void onReachedTarget() {
            if (net.minecraftforge.event.ForgeEventFactory.getMobGriefingEvent(Drillhog.this.level(), Drillhog.this)) {
                this.stripWood();
            }
        }

        private void stripWood() {ItemStack axeStack = new ItemStack(Items.IRON_AXE);
            BlockState state = Drillhog.this.level().getBlockState(blockPos);
            BlockHitResult hitResult = new BlockHitResult(Vec3.atCenterOf(blockPos), Direction.UP, blockPos, false);
            UseOnContext useOnContext = new UseOnContext(Drillhog.this.level(), null,  InteractionHand.MAIN_HAND, axeStack, hitResult);


            Optional<BlockState> strippedState = Optional.ofNullable(state.getBlock().getToolModifiedState(state,  useOnContext, ToolActions.AXE_STRIP, false));

            if (strippedState.isPresent()) {
                Drillhog.this.level().setBlock(blockPos, strippedState.get(), 3);
                Drillhog.this.level().playSound(null, blockPos, SoundEvents.AXE_STRIP, SoundSource.BLOCKS, 1.0F, 1.0F);
            }
        }

        /**
         * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
         * method as well.
         */
        public boolean canUse() {
            return !Drillhog.this.isSleeping() && super.canUse();
        }

        /**
         * Execute a one shot task or start executing a continuous task
         */
        public void start() {
            this.ticksWaited = 0;
            super.start();
        }
    }
    public int getAttackAnimationRemainingTicks() {
        return this.attackAnimationRemainingTicks;
    }

    public static boolean checkDrillhogSpawnRules(EntityType<Drillhog> drillhog, LevelAccessor level, MobSpawnType type, BlockPos blockPos, RandomSource random) {
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

    protected float getStandingEyeHeight(Pose pPose, EntityDimensions pSize) {
        return 0.6F;
    }

    protected SoundEvent getAmbientSound() {
        return RPGSounds.DRILLHOG_AMBIENT.get();
    }

    protected SoundEvent getHurtSound(DamageSource pDamageSource) {
        return RPGSounds.DRILLHOG_HURT.get();
    }

    protected SoundEvent getDeathSound() {
        return RPGSounds.DRILLHOG_DEATH.get();
    }


    public boolean doHurtTarget(Entity pEntity) {
        if(pEntity instanceof Player player)
            if(!player.isDamageSourceBlocked(this.level().damageSources().mobAttack(this)) && !this.entityData.get(DATA_DEALT_DAMAGE)) {
                this.entityData.set(DATA_DEALT_DAMAGE, true);
                this.playSound(RPGSounds.GOLDEN_TOKEN_FAIL.get(), 1.0F, (this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.0F);
            }
        if (!(pEntity instanceof LivingEntity)) {
            return false;
        } else {
            this.attackAnimationRemainingTicks = 10;
            this.level().broadcastEntityEvent(this, (byte)4);
            this.playSound(RPGSounds.DRILLHOG_ATTACK.get(), 1.0F, this.getVoicePitch());
            this.playSound(RPGSounds.DRILLHOG_DRILL.get(), 1.0F, this.getVoicePitch());
            return hurtAndThrowTarget(this, (LivingEntity)pEntity);
        }
    }

    public void die(@NotNull DamageSource damageSource) {
        if (damageSource.getEntity() instanceof ServerPlayer player && player.getLastDamageSource() == null && !this.entityData.get(DATA_DEALT_DAMAGE)) ModAdvancements.GOLDEN_KILL_DRILLHOG.trigger(player);
        super.die(damageSource);
    }

    protected void blockedByShield(LivingEntity pEntity) {
            throwTarget(this, pEntity);
    }


    public boolean canDisableShield() {
        return true;
    }
    static boolean hurtAndThrowTarget(LivingEntity entity, LivingEntity pTarget) {
        float f1 = (float)entity.getAttributeValue(Attributes.ATTACK_DAMAGE);
        float f;
        if (!entity.isBaby() && (int)f1 > 0) {
            f = f1 / 2.0F + (float)entity.level().random.nextInt((int)f1);
        } else {
            f = f1;
        }

        boolean flag = pTarget.hurt(entity.damageSources().mobAttack(entity), f);
        if (flag) {
            entity.doEnchantDamageEffects(entity, pTarget);
            if (!entity.isBaby()) {
                throwTarget(entity, pTarget);
            }
        }

        return flag;
    }

    static void throwTarget(LivingEntity entity, LivingEntity pTarget) {
        if(pTarget instanceof Player player) player.disableShield(false);
        double d0 = entity.getAttributeValue(Attributes.ATTACK_KNOCKBACK);
        double d1 = pTarget.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE);
        double d2 = d0 - d1;
        if (!(d2 <= 0.0D)) {
            double d3 = pTarget.getX() - entity.getX();
            double d4 = pTarget.getZ() - entity.getZ();
            float f = (float)(entity.level().random.nextInt(21) - 10);
            double d5 = d2 * (double)(entity.level().random.nextFloat() * 0.5F + 0.2F);
            Vec3 vec3 = (new Vec3(d3, 0.0D, d4)).normalize().scale(d5).yRot(f);
            double d6 = d2 * (double)entity.level().random.nextFloat() * 0.5D;
            pTarget.push(vec3.x, d6, vec3.z);
            pTarget.hurtMarked = true;
        }
    }
}


