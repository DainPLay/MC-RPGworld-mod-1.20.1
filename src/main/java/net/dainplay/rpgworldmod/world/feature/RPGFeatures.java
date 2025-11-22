package net.dainplay.rpgworldmod.world.feature;

import net.dainplay.rpgworldmod.RPGworldMod;
import net.dainplay.rpgworldmod.features.*;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.BlockStateConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.HugeMushroomFeatureConfiguration;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class RPGFeatures {

    public static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(ForgeRegistries.FEATURES, RPGworldMod.MOD_ID);

    public static final RegistryObject<Feature<BlockStateConfiguration>> SPIKY_IVY = FEATURES.register("spiky_ivy_feature", () -> new SpikyIvyFeature(BlockStateConfiguration.CODEC));
    public static final RegistryObject<Feature<BlockStateConfiguration>> TREE_HOLLOW_WEST = FEATURES.register("tree_hollow_feature_west", () -> new TreeHollowFeatureWest(BlockStateConfiguration.CODEC));
    public static final RegistryObject<Feature<BlockStateConfiguration>> TREE_HOLLOW_EAST = FEATURES.register("tree_hollow_feature_east", () -> new TreeHollowFeatureEast(BlockStateConfiguration.CODEC));
    public static final RegistryObject<Feature<BlockStateConfiguration>> TREE_HOLLOW_NORTH = FEATURES.register("tree_hollow_feature_north", () -> new TreeHollowFeatureNorth(BlockStateConfiguration.CODEC));
    public static final RegistryObject<Feature<BlockStateConfiguration>> TREE_HOLLOW_SOUTH = FEATURES.register("tree_hollow_feature_south", () -> new TreeHollowFeatureSouth(BlockStateConfiguration.CODEC));


}
