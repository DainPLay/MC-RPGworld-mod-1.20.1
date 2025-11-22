package net.dainplay.rpgworldmod.block.custom;

import net.dainplay.rpgworldmod.particle.ModParticles;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.Vec3;

import java.util.EnumMap;
import java.util.Map;

public class QuartziteBlock extends Block {
    public QuartziteBlock(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public void animateTick(BlockState pState, Level pLevel, BlockPos pPos, RandomSource pRandom) {
        spawnParticlesOnBlockFaces(pLevel, pState, pPos, ModParticles.QUARTZITE_SHINE_PARTICLES.get(), pRandom);
    }


    public void spawnParticlesOnBlockFaces(Level level, BlockState state, BlockPos pos, ParticleOptions particleOptions,
                                           RandomSource randomSource) {
        Vec3 vec3 = Vec3.atCenterOf(pos);
        for (Direction direction : Direction.values()) {
            if (randomSource.nextFloat() < 0.15f) {
                int i = direction.getStepX();
                int j = direction.getStepY();
                int k = direction.getStepZ();
                double d0 = vec3.x + (i == 0 ? Mth.nextDouble(level.random, -0.5D, 0.5D) : i * 0.6D);
                double d1 = vec3.y + (j == 0 ? Mth.nextDouble(level.random, -0.5D, 0.5D) : j * 0.6D);
                double d2 = vec3.z + (k == 0 ? Mth.nextDouble(level.random, -0.5D, 0.5D) : k * 0.6D);
                //var c = ClientDynamicResourcesHandler.getGlowLightColor(DyeColor.WHITE, randomSource);
                level.addParticle(particleOptions, d0, d1, d2, 0, 0, 0);
            }
        }
    }
}
