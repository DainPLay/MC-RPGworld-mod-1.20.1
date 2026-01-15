package net.dainplay.rpgworldmod.effect;


import net.dainplay.rpgworldmod.damage.ModDamageTypes;
import net.dainplay.rpgworldmod.data.tags.ModAdvancements;
import net.dainplay.rpgworldmod.entity.ModEntities;
import net.dainplay.rpgworldmod.entity.custom.Bhlee;
import net.dainplay.rpgworldmod.sounds.RPGSounds;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ambient.Bat;
import net.minecraft.world.entity.animal.AbstractFish;
import net.minecraft.world.entity.animal.Cod;
import net.minecraft.world.entity.animal.Fox;
import net.minecraft.world.entity.animal.Salmon;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;

import java.util.UUID;

public class MossiosisEffect extends MobEffect {
    public static final UUID MODIFIER_UUID = UUID.fromString("F6A6BC5A-2DC2-48C4-BFFE-0B4B3CB91361");
    protected final RandomSource random = RandomSource.create();
    public MossiosisEffect(MobEffectCategory mobEffectCategory, int color) {
        super(mobEffectCategory, color);
        addAttributeModifier(Attributes.MAX_HEALTH, MossiosisEffect.MODIFIER_UUID.toString(), 0D, AttributeModifier.Operation.MULTIPLY_TOTAL);
    }
    @Override
    public void applyEffectTick(LivingEntity pLivingEntity, int pAmplifier) {
        if(pLivingEntity instanceof Fox) {
            ((ServerLevel)pLivingEntity.level()).sendParticles(ParticleTypes.EXPLOSION, pLivingEntity.getX(), pLivingEntity.getY(0.5D), pLivingEntity.getZ(), 1, 0.0D, 0.0D, 0.0D, 0.0D);
            pLivingEntity.playSound(RPGSounds.MOSSIOSIS_CONVERTED.get(), 1.0F, 1F);
            for(ServerPlayer serverplayer : pLivingEntity.level().getEntitiesOfClass(ServerPlayer.class, new AABB(pLivingEntity.blockPosition()).inflate(10.0D, 5.0D, 10.0D))) {
                ModAdvancements.MOSSIOSIS_FOX_TRANSFORM_TRIGGER.trigger(serverplayer);
            }
            pLivingEntity = ((Mob) pLivingEntity).convertTo(ModEntities.BRAMBLEFOX.get(), true);
        }
        if(pLivingEntity instanceof Bat) {
            ((ServerLevel)pLivingEntity.level()).sendParticles(ParticleTypes.EXPLOSION, pLivingEntity.getX(), pLivingEntity.getY(0.5D), pLivingEntity.getZ(), 1, 0.0D, 0.0D, 0.0D, 0.0D);
            pLivingEntity.playSound(RPGSounds.MOSSIOSIS_CONVERTED.get(), 1.0F, 1F);
            for(ServerPlayer serverplayer : pLivingEntity.level().getEntitiesOfClass(ServerPlayer.class, new AABB(pLivingEntity.blockPosition()).inflate(10.0D, 5.0D, 10.0D))) {
                ModAdvancements.MOSSIOSIS_BAT_TRANSFORM_TRIGGER.trigger(serverplayer);
            }
            pLivingEntity = ((Mob) pLivingEntity).convertTo(ModEntities.MINTOBAT.get(), true);
        }
        if(pLivingEntity instanceof Cod || pLivingEntity instanceof Salmon|| pLivingEntity instanceof Bhlee) {
            ((ServerLevel)pLivingEntity.level()).sendParticles(ParticleTypes.EXPLOSION, pLivingEntity.getX(), pLivingEntity.getY(0.5D), pLivingEntity.getZ(), 1, 0.0D, 0.0D, 0.0D, 0.0D);
            pLivingEntity.playSound(RPGSounds.MOSSIOSIS_CONVERTED.get(), 1.0F, 1F);
            for(ServerPlayer serverplayer : pLivingEntity.level().getEntitiesOfClass(ServerPlayer.class, new AABB(pLivingEntity.blockPosition()).inflate(10.0D, 5.0D, 10.0D))) {
                ModAdvancements.MOSSIOSIS_FISH_TRANSFORM_TRIGGER.trigger(serverplayer);
            }
            pLivingEntity = ((Mob) pLivingEntity).convertTo(ModEntities.MOSSFRONT.get(), true);
        }

        if(pLivingEntity.canBeAffected(new MobEffectInstance(ModEffects.MOSSIOSIS.get())) && pLivingEntity.getHealth() - (pAmplifier+1)*6 <= 0) {
            if (pLivingEntity.getLastDamageSource() == null) {
                pLivingEntity.hurt(ModDamageTypes.getDamageSource(pLivingEntity.level(), ModDamageTypes.MOSSIOSIS), Float.MAX_VALUE);
            } else {
                pLivingEntity.hurt(pLivingEntity.getLastDamageSource(), Float.MAX_VALUE);
            }
        }
        super.applyEffectTick(pLivingEntity, pAmplifier);
    }

    public boolean isDurationEffectTick(int i, int j) {
        return true;
    }
}