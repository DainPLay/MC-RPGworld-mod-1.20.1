package net.dainplay.rpgworldmod.effect;


import net.dainplay.rpgworldmod.biome.BiomeRegistry;
import net.dainplay.rpgworldmod.damage.ModDamageTypes;
import net.dainplay.rpgworldmod.entity.ModEntities;
import net.dainplay.rpgworldmod.entity.custom.MosquitoSwarm;
import net.dainplay.rpgworldmod.particle.ModParticles;
import net.dainplay.rpgworldmod.sounds.RPGSounds;
import net.dainplay.rpgworldmod.util.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public class MosquitoingEffect extends MobEffect {
    public static final UUID MODIFIER_UUID = UUID.fromString("9ff0666f-ceb3-4da5-b81f-0d83e1b24510");
    protected final RandomSource random = RandomSource.create();
    public MosquitoingEffect(MobEffectCategory mobEffectCategory, int color) {
        super(mobEffectCategory, color);
        addAttributeModifier(Attributes.SPAWN_REINFORCEMENTS_CHANCE, MosquitoingEffect.MODIFIER_UUID.toString(), 0D, AttributeModifier.Operation.MULTIPLY_TOTAL);
    }
    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        int HIT_TICK = 100-20*amplifier;
        if (HIT_TICK <= 0) HIT_TICK = 1;
        Level level = entity.level();
        BlockPos pos = entity.blockPosition();
        BlockPos newpos = new BlockPos(pos.getX(), (int) entity.getEyeY(), pos.getZ());

        Biome biome = level.getBiome(newpos).value();
        boolean canRain = biome.getPrecipitationAt(newpos) == Biome.Precipitation.RAIN;

        if ((level.isRaining() && canRain && level.canSeeSky(newpos)) || (level.getBiome(entity.getOnPos()).is(BiomeRegistry.RIE_WEALD) && level.isRaining())
                || !entity.getEyeInFluidType().isAir()
                || entity.isOnFire()
                || hasStickyBlockNearby(entity)
                || level.dimension() == Level.NETHER
        ) {
            entity.removeEffect(this);
            MosquitoSwarm.spawnBlock(entity,amplifier);
        }

        var effectInstance = entity.getEffect(this);
        if (effectInstance != null) {
            int duration = effectInstance.getDuration();

            if (duration % 60 == 0) {
                level.playSound(entity, entity.blockPosition(), RPGSounds.MOSQUITO_SWARM_AMBIENT.get(), SoundSource.PLAYERS, 1F, (random.nextFloat() - random.nextFloat()) * 0.05F + 1.0F);
            }
            if (duration % 5 == 0) {
                spawnParticles(entity);
            }
            if (duration % HIT_TICK == 1) {
                applyDamage(entity, level, amplifier);
                if(entity.isDeadOrDying()) {
                    spawnSwarmOnEffectEnd(entity, entity.level(), amplifier);
                    entity.removeEffect(this);
                }
            }
            if (duration == 1) {
                spawnSwarmOnEffectEnd(entity, entity.level(), amplifier);
                entity.removeEffect(this);
            }
        }
        super.applyEffectTick(entity, amplifier);
    }



    public boolean hasStickyBlockNearby(LivingEntity entity) {
        Level level = entity.level();
        BlockPos centerPos = entity.blockPosition();

        BlockPos[] directions = {
                centerPos.above(),    // +Y
                centerPos.below(),    // -Y
                centerPos.north(),    // -Z
                centerPos.south(),    // +Z
                centerPos.west(),     // -X
                centerPos.east()      // +X
        };

        for (BlockPos checkPos : directions) {
            BlockState state = level.getBlockState(checkPos);
            if (state.is(ModTags.Blocks.STICKY_FOR_MOSQUITOS)) {
                return true;
            }
        }

        return false;
    }

    private void spawnSwarmOnEffectEnd(LivingEntity entity, Level level, int amplifier) {
        if (!level.isClientSide) {
            ServerLevel serverLevel = (ServerLevel) level;

            // Создаем стаю москитов в позиции существа
            MosquitoSwarm swarm = new MosquitoSwarm(ModEntities.MOSQUITO_SWARM.get(), level);
            swarm.dealtDamage();
            swarm.setSize(amplifier + 1);
            swarm.setPos(entity.getX(), entity.getY(), entity.getZ());

            // Считываем сохраненный UUID владельца и присваиваем новой стае
            if (entity.getPersistentData().hasUUID("MosquitoSwarmOwner")) {
                UUID ownerUUID = entity.getPersistentData().getUUID("MosquitoSwarmOwner");
                swarm.setOwnerUUID(ownerUUID);
                swarm.setTame(true);

                // Очищаем тег после использования
                entity.getPersistentData().remove("MosquitoSwarmOwner");
            }

            // Добавляем стаю в мир
            serverLevel.addFreshEntityWithPassengers(swarm);

            // Выталкиваем стаю вверх от существа
            Vec3 pushDirection = new Vec3(0, 0.5, 0); // Вверх с силой
            swarm.push(pushDirection.x, pushDirection.y, pushDirection.z);

            swarm.setDeltaMovement(swarm.getDeltaMovement().add(0, 0.5, 0));

            // Эффекты при спавне
            serverLevel.sendParticles(ModParticles.MOSQUITOS.get(),
                    entity.getX(), entity.getY() + 0.5, entity.getZ(),
                    15, 0.35f + 0.05f * swarm.getSize(), 0.35f + 0.05f * swarm.getSize(), 0.35f + 0.05f * swarm.getSize(), 0.05);
        }
    }

    private void spawnParticles(LivingEntity entity) {
        Level level = entity.level();
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(
                    ModParticles.MOSQUITOS.get(),
                    entity.getX(),
                    entity.getY() + entity.getBbHeight() * 0.5f,
                    entity.getZ(),
                    10,
                    0.01F,
                    level.getRandom().nextFloat(),
                    0.01F,
                    0.0001F
            );
        }
    }

    private void applyDamage(LivingEntity entity, Level level, int amplifier) {
        if (!level.isClientSide) {
            entity.hurt(ModDamageTypes.getDamageSource(level, ModDamageTypes.MOSQUITOS), 2);
        }
    }

    public boolean isDurationEffectTick(int i, int j) {
        return true;
    }
}