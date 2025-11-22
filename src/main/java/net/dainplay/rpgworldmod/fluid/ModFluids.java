package net.dainplay.rpgworldmod.fluid;

import net.dainplay.rpgworldmod.RPGworldMod;
import net.dainplay.rpgworldmod.block.ModBlocks;
import net.dainplay.rpgworldmod.item.ModItems;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModFluids {
    public static final DeferredRegister<Fluid> FLUIDS =
            DeferredRegister.create(ForgeRegistries.FLUIDS, RPGworldMod.MOD_ID);

    public static final RegistryObject<FlowingFluid> SOURCE_ARBOR_FUEL = FLUIDS.register("arbor_fuel_fluid",
            () -> new ForgeFlowingFluid.Source(ModFluids.ARBOR_FUEL_FLUID_PROPERTIES));
    public static final RegistryObject<FlowingFluid> FLOWING_ARBOR_FUEL = FLUIDS.register("flowing_arbor_fuel",
            () -> new ForgeFlowingFluid.Flowing(ModFluids.ARBOR_FUEL_FLUID_PROPERTIES));


    public static final ForgeFlowingFluid.Properties ARBOR_FUEL_FLUID_PROPERTIES = new ForgeFlowingFluid.Properties(
            ModFluidTypes.ARBOR_FUEL_FLUID_TYPE, SOURCE_ARBOR_FUEL, FLOWING_ARBOR_FUEL)
            .slopeFindDistance(1).levelDecreasePerBlock(3).block(ModBlocks.ARBOR_FUEL_BLOCK)
            .bucket(ModItems.ARBOR_FUEL_BUCKET);


    public static void register(IEventBus eventBus) {
        FLUIDS.register(eventBus);
    }
}
