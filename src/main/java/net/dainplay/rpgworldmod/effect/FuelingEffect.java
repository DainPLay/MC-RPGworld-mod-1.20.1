package net.dainplay.rpgworldmod.effect;


import net.dainplay.rpgworldmod.damage.ModDamageTypes;
import net.dainplay.rpgworldmod.data.tags.ModAdvancements;
import net.dainplay.rpgworldmod.entity.ModEntities;
import net.dainplay.rpgworldmod.entity.custom.Bhlee;
import net.dainplay.rpgworldmod.sounds.RPGSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ambient.Bat;
import net.minecraft.world.entity.animal.Cod;
import net.minecraft.world.entity.animal.Fox;
import net.minecraft.world.entity.animal.Salmon;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public class FuelingEffect extends MobEffect {
    public static final UUID MODIFIER_UUID = UUID.fromString("267495bd-9e39-4ee5-807e-4bea7e558883");
    protected final RandomSource random = RandomSource.create();
    public FuelingEffect(MobEffectCategory mobEffectCategory, int color) {
        super(mobEffectCategory, color);
        addAttributeModifier(Attributes.FLYING_SPEED, FuelingEffect.MODIFIER_UUID.toString(), 0D, AttributeModifier.Operation.MULTIPLY_TOTAL);
    }
    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        Level level = entity.level();
        BlockPos pos = entity.blockPosition();
        BlockPos newpos = new BlockPos(pos.getX(), (int) entity.getEyeY(), pos.getZ());
        FluidState fluid = level.getFluidState(pos);

        Biome biome = level.getBiome(newpos).value();
        boolean canRain = biome.getPrecipitationAt(newpos) == Biome.Precipitation.RAIN;

        if ((level.isRaining() && canRain && level.canSeeSky(newpos)) || level.getFluidState(pos).getType() == Fluids.WATER  || level.getFluidState(newpos).getType() == Fluids.WATER) {
            if(!EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(entity) && !entity.isPushedByFluid()) return;

            float liftAmount; // Adjust the lifting speed
            if (entity.onGround()) liftAmount = 0.16f; else liftAmount = 0.08f;
            Vec3 motion = entity.getDeltaMovement();
            entity.setDeltaMovement(motion.x, motion.y + liftAmount, motion.z);
            if((level.isRaining() && canRain && level.canSeeSky(newpos)) && !level.isClientSide && entity instanceof ServerPlayer serverPlayer) ModAdvancements.FLY_IN_OIL.trigger(serverPlayer);
            entity.hasImpulse = true;
            entity.resetFallDistance();
        }
    }


    public boolean isDurationEffectTick(int i, int j) {
        return true;
    }
}