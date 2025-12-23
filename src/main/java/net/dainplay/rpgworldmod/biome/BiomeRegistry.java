package net.dainplay.rpgworldmod.biome;

import net.dainplay.rpgworldmod.RPGworldMod;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraftforge.fml.common.Mod;


@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class BiomeRegistry
{

    public static final ResourceKey<Biome> RIE_WEALD = register("rie_weald");

    private static ResourceKey<Biome> register(String name)
    {
        return ResourceKey.create(Registries.BIOME, new ResourceLocation(RPGworldMod.MOD_ID, name));
    }
    public static void bootstrapBiomes(BootstapContext<Biome> context)
    {
        HolderGetter<ConfiguredWorldCarver<?>> carverGetter = context.lookup(Registries.CONFIGURED_CARVER);
        HolderGetter<PlacedFeature> placedFeatureGetter = context.lookup(Registries.PLACED_FEATURE);
        context.register(RIE_WEALD, RPGworldBiomeDecorator.decorateRieWeald(placedFeatureGetter, carverGetter));
    }
}