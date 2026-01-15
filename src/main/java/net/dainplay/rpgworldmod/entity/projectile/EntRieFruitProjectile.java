package net.dainplay.rpgworldmod.entity.projectile;

import net.dainplay.rpgworldmod.block.custom.EntFaceBlock;
import net.dainplay.rpgworldmod.block.entity.custom.EntFaceBlockEntity;
import net.dainplay.rpgworldmod.damage.ModDamageTypes;
import net.dainplay.rpgworldmod.entity.ModEntities;
import net.dainplay.rpgworldmod.item.ModItems;
import net.dainplay.rpgworldmod.sounds.RPGSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

public class EntRieFruitProjectile extends ThrowableProjectile implements ItemSupplier {
    protected final BlockPos entPos;
    
    public EntRieFruitProjectile(EntityType<? extends EntRieFruitProjectile> type, Level level) {
        super(type, level);
        this.entPos = null;
    }

    public EntRieFruitProjectile(Level level, LivingEntity shooter) {
        super(ModEntities.ENT_RIE_FRUIT_PROJECTILE.get(), shooter, level); // Используйте ваш EntityType
        this.entPos = null;
    }
    public EntRieFruitProjectile(Level level, double x, double y, double z, BlockPos entPos) {
        super(ModEntities.ENT_RIE_FRUIT_PROJECTILE.get(), x, y, z, level);
        this.entPos = entPos;
    }

    @Override
    protected void defineSynchedData() {
        // Не требуется специальных данных
    }

    @Override
    protected void onHitEntity(EntityHitResult pResult) {
        Entity entity = pResult.getEntity();
        float f = 5.0F;

        Entity entity1 = this.getOwner();

        DamageSource damagesource;
        if (entity1 == null) {
            damagesource = ModDamageTypes.getEntityDamageSource(this.level(), ModDamageTypes.ENT_RIE_FRUIT, this);
        } else {
            damagesource = ModDamageTypes.getEntityDamageSource(this.level(), ModDamageTypes.ENT_RIE_FRUIT, entity1);
            if (entity1 instanceof LivingEntity) {
                ((LivingEntity)entity1).setLastHurtMob(entity);
            }
        }
        SoundEvent soundevent = RPGSounds.RIE_FRUIT_SPLAT.get();
        if (entity.hurt(damagesource, f)) {
            if (entity.getType() == EntityType.ENDERMAN) {
                return;
            }
            if(entPos != null) {
                BlockEntity entEntity = level().getBlockEntity(entPos);
                if (entEntity instanceof EntFaceBlockEntity entFaceBlockEntity) {
                    entFaceBlockEntity.dealtDamage(entity);
                }
            }

            if (entity instanceof LivingEntity) {
                LivingEntity livingentity1 = (LivingEntity)entity;
                if (entity1 instanceof LivingEntity) {
                    EnchantmentHelper.doPostHurtEffects(livingentity1, entity1);
                    EnchantmentHelper.doPostDamageEffects((LivingEntity)entity1, livingentity1);
                }
            }
        }

        this.setDeltaMovement(this.getDeltaMovement().multiply(-0.01D, -0.1D, -0.01D));
        this.playSound(soundevent, 1.0F, 1.0F);
        this.tickDespawn();
    }



    public void tickDespawn() {
        this.discard();
        for(int i = 0; i < 4; ++i) {
            this.spawnItemParticles(ModItems.RIE_FRUIT.get().getDefaultInstance(), 1);
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
    public void tick() {
        super.tick();
        // Добавляем гравитацию
        if (!this.isNoGravity()) {
            this.setDeltaMovement(this.getDeltaMovement().add(0.0, -0.05, 0.0));
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult pResult) {
        if (this.random.nextFloat() < 0.5F) {
            this.playSound(RPGSounds.RIE_FRUIT_SPLAT.get(), 1.0F, 0.8F+this.random.nextFloat()*0.4F);
        }
        tickDespawn();
        super.onHitBlock(pResult);
    }

    @Override
    public ItemStack getItem() {
        return ModItems.RIE_FRUIT.get().getDefaultInstance();
    }
}