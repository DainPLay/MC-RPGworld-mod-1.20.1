package net.dainplay.rpgworldmod.block.custom;

import net.dainplay.rpgworldmod.effect.ModEffects;
import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;

import java.util.Objects;
import java.util.function.Supplier;

public class FuelLiquidBlock extends LiquidBlock {
    public FuelLiquidBlock(Supplier<? extends FlowingFluid> pFluid, Properties pProperties) {
        super(pFluid, pProperties);
    }
    public void entityInside(BlockState blockState, Level level, BlockPos pos, Entity entity) {
        if(entity instanceof LivingEntity livingEntity && !level.isClientSide) {
            int duration = 0;
            MobEffectInstance effect = livingEntity.getEffect(ModEffects.FUELING.get());

            if(effect != null){
                duration = effect.getDuration();
            }

            if (duration >=280 && duration < 2400) {
                livingEntity.addEffect(new MobEffectInstance(ModEffects.FUELING.get(), duration + 3, 0));
            } else {
                livingEntity.addEffect(new MobEffectInstance(ModEffects.FUELING.get(), 280, 0));
            }
        }
    }
}
