package net.dainplay.rpgworldmod.entity.custom;

import net.dainplay.rpgworldmod.biome.BiomeRegistry;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.data.ForgeItemTagsProvider;

import javax.annotation.Nullable;
import java.util.EnumSet;

public class BibbitTemptGoal extends Goal {
    private static final TargetingConditions TEMP_TARGETING = TargetingConditions.forNonCombat().range(10.0D).ignoreLineOfSight();
    private final TargetingConditions targetingConditions;
    protected final PathfinderMob mob;
    private final double speedModifier;
    private double px;
    private double py;
    private double pz;
    private double pRotX;
    private double pRotY;
    @Nullable
    protected LivingEntity player;
    private int calmDown;
    private boolean isRunning;
    private final boolean canScare;

    public BibbitTemptGoal(PathfinderMob pMob, double pSpeedModifier, boolean pCanScare) {
        this.mob = pMob;
        this.speedModifier = pSpeedModifier;
        this.canScare = pCanScare;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        this.targetingConditions = TEMP_TARGETING.copy().selector(this::shouldFollow);
    }

    /**
     * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
     * method as well.
     */
    public boolean canUse() {
        if (this.calmDown > 0) {
            --this.calmDown;
            return false;
        } else {
            this.player = this.mob.level().getNearestEntity(LivingEntity.class, this.targetingConditions, this.mob, this.mob.getX(), this.mob.getY(), this.mob.getZ(), this.mob.getBoundingBox().inflate(16.0D));
            return this.player != null;
        }
    }

    private boolean shouldFollow(LivingEntity p_148139_) {
        return (!(p_148139_ instanceof Bibbit) && mob.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_WANTED_ITEM).isEmpty() && mob.getItemBySlot(EquipmentSlot.FEET).isEmpty() && p_148139_.level().getBiome(p_148139_.getOnPos()).is(BiomeRegistry.RIE_WEALD) && (p_148139_.getMainHandItem().is(Tags.Items.ARMORS_BOOTS) || p_148139_.getOffhandItem().is(Tags.Items.ARMORS_BOOTS) || !p_148139_.getItemBySlot(EquipmentSlot.FEET).isEmpty()));
    }
    /**
     * Returns whether an in-progress EntityAIBase should continue executing
     */
    public boolean canContinueToUse() {
        if (this.canScare()) {
            if (this.mob.distanceToSqr(this.player) < 36.0D) {
                if (this.player.distanceToSqr(this.px, this.py, this.pz) > 0.010000000000000002D) {
                    return false;
                }

                if (Math.abs((double)this.player.getXRot() - this.pRotX) > 5.0D || Math.abs((double)this.player.getYRot() - this.pRotY) > 5.0D) {
                    return false;
                }
            } else {
                this.px = this.player.getX();
                this.py = this.player.getY();
                this.pz = this.player.getZ();
            }

            this.pRotX = (double)this.player.getXRot();
            this.pRotY = (double)this.player.getYRot();
        }

        return this.canUse();
    }

    protected boolean canScare() {
        return this.canScare;
    }

    /**
     * Execute a one shot task or start executing a continuous task
     */
    public void start() {
        this.px = this.player.getX();
        this.py = this.player.getY();
        this.pz = this.player.getZ();
        this.isRunning = true;
    }

    /**
     * Reset the task's internal state. Called when this task is interrupted by another one
     */
    public void stop() {
        this.player = null;
        this.mob.getNavigation().stop();
        this.calmDown = reducedTickDelay(100);
        this.isRunning = false;
    }

    /**
     * Keep ticking a continuous task that has already been started
     */
    public void tick() {
        this.mob.getLookControl().setLookAt(this.player, (float)(this.mob.getMaxHeadYRot() + 20), (float)this.mob.getMaxHeadXRot());
        if (this.mob.distanceToSqr(this.player) < 6.25D) {
            this.mob.getNavigation().stop();
        } else {
            this.mob.getNavigation().moveTo(this.player, this.speedModifier);
        }

    }

    /**
     * @see #isRunning
     */
    public boolean isRunning() {
        return this.isRunning;
    }
}
