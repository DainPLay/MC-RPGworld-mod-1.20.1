// EntRootsEntity.java
package net.dainplay.rpgworldmod.entity.projectile;

import net.dainplay.rpgworldmod.block.entity.custom.EntFaceBlockEntity;
import net.dainplay.rpgworldmod.damage.ModDamageTypes;
import net.dainplay.rpgworldmod.entity.ModEntities;
import net.dainplay.rpgworldmod.sounds.RPGSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public class EntRootsEntity extends Entity {
    private static final EntityDataAccessor<Integer> WARMUP = SynchedEntityData.defineId(EntRootsEntity.class, EntityDataSerializers.INT);
    private int warmupDelay;
    private boolean startedAttack;
    private int ticksActive;
    protected BlockPos entPos;

    public EntRootsEntity(EntityType<? extends EntRootsEntity> type, Level level) {
        super(type, level);
    }

    public EntRootsEntity(Level level, double x, double y, double z, float yRot, int warmupDelay, BlockPos entPos) {
        this(ModEntities.ENT_ROOTS.get(), level); // Используйте ваш EntityType
        this.warmupDelay = warmupDelay;
        this.entPos = entPos;
        this.setYRot(yRot * (180F / (float)Math.PI));
        this.setPos(x, y, z);
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(WARMUP, 0);
    }
    
    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide) {
            return;
        }
        
        if (this.warmupDelay > 0) {
            --this.warmupDelay;
            this.entityData.set(WARMUP, this.warmupDelay);
            if (this.warmupDelay == 0) {
                this.startAttack();
            }
        } else if (!this.startedAttack) {
            this.startAttack();
        } else {
            ++this.ticksActive;
            if (this.ticksActive >= 20) { // Атака длится 1 секунду
                this.discard();
                return;
            }
            
            // Проверяем урон
            float damageRadius = 0.75f;
            AABB aabb = new AABB(
                this.getX() - damageRadius, 
                this.getY(), 
                this.getZ() - damageRadius,
                this.getX() + damageRadius, 
                this.getY() + 1.5, 
                this.getZ() + damageRadius
            );
            
            for (LivingEntity entity : this.level().getEntitiesOfClass(LivingEntity.class, aabb)) {
                if (entity instanceof net.minecraft.world.entity.player.Player) {
                    if(entity.hurt(ModDamageTypes.getDamageSource(this.level(), ModDamageTypes.ENT_ROOTS), 6.0f)) {
                        if (entPos != null) {
                            BlockEntity entEntity = level().getBlockEntity(entPos);
                            if (entEntity instanceof EntFaceBlockEntity entFaceBlockEntity) {
                                entFaceBlockEntity.dealtDamage(entity);
                            }
                        }
                    }
                }
            }
        }
    }
    
    private void startAttack() {
        if (!this.startedAttack) {
            this.startedAttack = true;
            this.playSound(RPGSounds.ENT_ROOTS.get(), 1.0f, (this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.0F);
        }
    }
    
    @Override
    protected void readAdditionalSaveData(CompoundTag compound) {
        this.warmupDelay = compound.getInt("Warmup");
    }
    
    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {
        compound.putInt("Warmup", this.warmupDelay);
    }
    
    public int getWarmupDelay() {
        return this.warmupDelay;
    }
}