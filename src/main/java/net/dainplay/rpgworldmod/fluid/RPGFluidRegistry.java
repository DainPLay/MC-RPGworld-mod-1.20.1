package net.dainplay.rpgworldmod.fluid;

import net.dainplay.rpgworldmod.block.ModBlocks;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.fluids.FluidInteractionRegistry;

public class RPGFluidRegistry {
    public static void postInit() {
        FluidInteractionRegistry.addInteraction(ModFluidTypes.ARBOR_FUEL_FLUID_TYPE.get(), new FluidInteractionRegistry.InteractionInformation(
                (level, currentPos, relativePos, currentState) -> level.getBlockState(relativePos).is(Blocks.FIRE) || level.getBlockState(relativePos).is(Blocks.MAGMA_BLOCK),
                Blocks.FIRE.defaultBlockState()
        ));
        FluidInteractionRegistry.addInteraction(ModFluidTypes.ARBOR_FUEL_FLUID_TYPE.get(), new FluidInteractionRegistry.InteractionInformation(
                ForgeMod.WATER_TYPE.get(),
                fluidState -> ModBlocks.EMULSION_BLOCK.get().defaultBlockState()
        ));
        FluidInteractionRegistry.addInteraction(ModFluidTypes.ARBOR_FUEL_FLUID_TYPE.get(), new FluidInteractionRegistry.InteractionInformation(
                ForgeMod.LAVA_TYPE.get(),
                fluidState -> Blocks.FIRE.defaultBlockState()
        ));
    }
}
