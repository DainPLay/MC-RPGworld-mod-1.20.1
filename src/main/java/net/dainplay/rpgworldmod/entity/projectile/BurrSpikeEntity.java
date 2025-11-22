package net.dainplay.rpgworldmod.entity.projectile;

import net.dainplay.rpgworldmod.damage.ModDamageTypes;
import net.dainplay.rpgworldmod.entity.ModEntities;
import net.dainplay.rpgworldmod.item.ModItems;
import net.dainplay.rpgworldmod.sounds.RPGSounds;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

public class BurrSpikeEntity extends AbstractArrow {
    private ItemStack BurrSpikeItem = new ItemStack(ModItems.BURR_SPIKE.get());
    private boolean dealtDamage;
    public BurrSpikeEntity(EntityType<? extends BurrSpikeEntity> p_37561_, Level p_37562_) {
        super(p_37561_, p_37562_);
    }
    public BurrSpikeEntity(EntityType<BurrSpikeEntity> entityType, double x, double y, double z, Level world) {
        super(entityType, x, y, z, world);
    }

    public BurrSpikeEntity(Level p_37569_, LivingEntity p_37570_, ItemStack p_37571_) {
        super(ModEntities.BURR_SPIKE_PROJECTILE.get(), p_37570_, p_37569_);
        this.BurrSpikeItem = p_37571_.copy();
    }


    @Override
    protected ItemStack getPickupItem() {
        return this.BurrSpikeItem.copy();
    }
    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
    }
    @Nullable
    @Override
    protected EntityHitResult findHitEntity(Vec3 pStartVec, Vec3 pEndVec) {
        return this.dealtDamage ? null : super.findHitEntity(pStartVec, pEndVec);
    }

    /**
     * Called when the arrow hits an entity
     */
    @Override
    protected void onHitEntity(EntityHitResult pResult) {
        Entity entity = pResult.getEntity();
        float f = 5.0F;

        Entity entity1 = this.getOwner();

        DamageSource damagesource;
        if (entity1 == null) {
            damagesource = ModDamageTypes.getEntityDamageSource(this.level(), ModDamageTypes.BURR_SPIKE, this);
        } else {
            damagesource = ModDamageTypes.getEntityDamageSource(this.level(), ModDamageTypes.BURR_SPIKE, entity1);
            if (entity1 instanceof LivingEntity) {
                ((LivingEntity)entity1).setLastHurtMob(entity);
            }
        }
        this.dealtDamage = true;
        SoundEvent soundevent = RPGSounds.BURR_SPIKE_HIT.get();
        if (entity.hurt(damagesource, f)) {
            if (entity.getType() == EntityType.ENDERMAN) {
                return;
            }

            if (entity instanceof LivingEntity) {
                LivingEntity livingentity1 = (LivingEntity)entity;
                if (entity1 instanceof LivingEntity) {
                    EnchantmentHelper.doPostHurtEffects(livingentity1, entity1);
                    EnchantmentHelper.doPostDamageEffects((LivingEntity)entity1, livingentity1);
                }

                this.doPostHurtEffects(livingentity1);
            }
        }

        this.setDeltaMovement(this.getDeltaMovement().multiply(-0.01D, -0.1D, -0.01D));
        this.playSound(soundevent, 1.0F, 1.0F);
        this.tickDespawn();
    }



    @Override
    public void tickDespawn() {
        this.discard();
        for(int i = 0; i < 8; ++i) {
            this.spawnItemParticles(ModItems.BURR_SPIKE.get().getDefaultInstance(), 1);
        }
    }
    private void spawnItemParticles(ItemStack pStack, int pCount) {
        for(int i = 0; i < pCount; ++i) {
            Vec3 vec3 = new Vec3(((double)this.random.nextFloat() - 0.5D) * 0.1D, Math.random() * 0.1D + 0.1D, 0.0D);
            vec3 = vec3.xRot(-this.getXRot() * ((float)Math.PI / 180F));
            vec3 = vec3.yRot(-this.getYRot() * ((float)Math.PI / 180F));
            if (this.level() instanceof ServerLevel) //Forge: Fix MC-2518 spawnParticle is nooped on server, need to use server specific variant
                ((ServerLevel)this.level()).sendParticles(new ItemParticleOption(ParticleTypes.ITEM, pStack), this.getX(), this.getY(), this.getZ(), 1, vec3.x, vec3.y + 0.05D, vec3.z, 0.0D);
            else
                this.level().addParticle(new ItemParticleOption(ParticleTypes.ITEM, pStack), this.getX(), this.getY(), this.getZ(), vec3.x, vec3.y + 0.05D, vec3.z);
        }

    }
    @Override
    protected SoundEvent getDefaultHitGroundSoundEvent() {
        return RPGSounds.BURR_SPIKE_MISS.get();
    }
    public void readAdditionalSaveData(CompoundTag pCompound) {
        super.readAdditionalSaveData(pCompound);
        if (pCompound.contains("Trident", 10)) {
            this.BurrSpikeItem = ItemStack.of(pCompound.getCompound("Trident"));
        }

        this.dealtDamage = pCompound.getBoolean("DealtDamage");
    }

    @Override
    public void addAdditionalSaveData(CompoundTag pCompound) {
        super.addAdditionalSaveData(pCompound);
        pCompound.put("Trident", this.BurrSpikeItem.save(new CompoundTag()));
        pCompound.putBoolean("DealtDamage", this.dealtDamage);
    }
    @Override
    protected float getWaterInertia() {
        return 0.66F;
    }
@Override
    public boolean shouldRender(double pX, double pY, double pZ) {
        return true;
    }
    @Override
    public void tick() {
        if (this.inGroundTime > 0) {
            this.tickDespawn();
        }

        super.tick();
    }
}