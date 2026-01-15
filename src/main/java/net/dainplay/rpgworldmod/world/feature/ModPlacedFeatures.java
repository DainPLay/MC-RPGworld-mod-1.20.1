package net.dainplay.rpgworldmod.world.feature;

import com.google.common.collect.ImmutableList;
import net.dainplay.rpgworldmod.RPGworldMod;
import net.dainplay.rpgworldmod.features.EntFeature;
import net.dainplay.rpgworldmod.features.FancyTreeFeature;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.*;

import java.util.List;

public class ModPlacedFeatures {
    public static final ResourceKey<PlacedFeature> RIE_TREE_CHECKED_KEY = createKey("rie_tree_checked");
    public static final ResourceKey<PlacedFeature> HOLLOW_EAST_RIE_TREE_CHECKED_KEY = createKey("hollow_east_rie_tree_checked");
    public static final ResourceKey<PlacedFeature> HOLLOW_WEST_RIE_TREE_CHECKED_KEY = createKey("hollow_west_rie_tree_checked");
    public static final ResourceKey<PlacedFeature> HOLLOW_SOUTH_RIE_TREE_CHECKED_KEY = createKey("hollow_south_rie_tree_checked");
    public static final ResourceKey<PlacedFeature> HOLLOW_NORTH_RIE_TREE_CHECKED_KEY = createKey("hollow_north_rie_tree_checked");
    public static final ResourceKey<PlacedFeature> BIBBIT_HOLLOW_EAST_RIE_TREE_CHECKED_KEY = createKey("bibbit_hollow_east_rie_tree_checked");
    public static final ResourceKey<PlacedFeature> BIBBIT_HOLLOW_WEST_RIE_TREE_CHECKED_KEY = createKey("bibbit_hollow_west_rie_tree_checked");
    public static final ResourceKey<PlacedFeature> BIBBIT_HOLLOW_SOUTH_RIE_TREE_CHECKED_KEY = createKey("bibbit_hollow_south_rie_tree_checked");
    public static final ResourceKey<PlacedFeature> BIBBIT_HOLLOW_NORTH_RIE_TREE_CHECKED_KEY = createKey("bibbit_hollow_north_rie_tree_checked");
    public static final ResourceKey<PlacedFeature> FANCY_RIE_TREE_CHECKED_KEY = createKey("fancy_rie_tree_checked");
    public static final ResourceKey<PlacedFeature> HOLLOW_SOUTH_FANCY_RIE_TREE_CHECKED_KEY = createKey("hollow_south_fancy_rie_tree_checked");
    public static final ResourceKey<PlacedFeature> HOLLOW_NORTH_FANCY_RIE_TREE_CHECKED_KEY = createKey("hollow_north_fancy_rie_tree_checked");
    public static final ResourceKey<PlacedFeature> HOLLOW_WEST_FANCY_RIE_TREE_CHECKED_KEY = createKey("hollow_west_fancy_rie_tree_checked");
    public static final ResourceKey<PlacedFeature> HOLLOW_EAST_FANCY_RIE_TREE_CHECKED_KEY = createKey("hollow_east_fancy_rie_tree_checked");
    public static final ResourceKey<PlacedFeature> BIBBIT_HOLLOW_SOUTH_FANCY_RIE_TREE_CHECKED_KEY = createKey("bibbit_hollow_south_fancy_rie_tree_checked");
    public static final ResourceKey<PlacedFeature> BIBBIT_HOLLOW_NORTH_FANCY_RIE_TREE_CHECKED_KEY = createKey("bibbit_hollow_north_fancy_rie_tree_checked");
    public static final ResourceKey<PlacedFeature> BIBBIT_HOLLOW_WEST_FANCY_RIE_TREE_CHECKED_KEY = createKey("bibbit_hollow_west_fancy_rie_tree_checked");
    public static final ResourceKey<PlacedFeature> BIBBIT_HOLLOW_EAST_FANCY_RIE_TREE_CHECKED_KEY = createKey("bibbit_hollow_east_fancy_rie_tree_checked");
    public static final ResourceKey<PlacedFeature> ENT_FACE_SOUTH_CHECKED_KEY = createKey("ent_face_south_checked");
    public static final ResourceKey<PlacedFeature> ENT_FACE_NORTH_CHECKED_KEY = createKey("ent_face_north_checked");
    public static final ResourceKey<PlacedFeature> ENT_FACE_WEST_CHECKED_KEY = createKey("ent_face_west_checked");
    public static final ResourceKey<PlacedFeature> ENT_FACE_EAST_CHECKED_KEY = createKey("ent_face_east_checked");
    public static final ResourceKey<PlacedFeature> RIE_FLOWER_PLACED_KEY = createKey("rie_flower_placed");
    public static final ResourceKey<PlacedFeature> FAIRAPIER_PLACED_KEY = createKey("fairapier_placed");
    public static final ResourceKey<PlacedFeature> SHIVERALIS_PLACED_KEY = createKey("shiveralis_placed");
    public static final ResourceKey<PlacedFeature> TRIPLOVER_PLACED_KEY = createKey("triplover_placed");
    public static final ResourceKey<PlacedFeature> GLOSSOM_PLACED_KEY = createKey("glossom_placed");
    public static final ResourceKey<PlacedFeature> RPGIROLLE_PLACED_KEY = createKey("rpgirolle_placed");
    public static final ResourceKey<PlacedFeature> MIMOSSA_PLACED_KEY = createKey("mimossa_placed");
    public static final ResourceKey<PlacedFeature> TYPHON_PLACED_KEY = createKey("typhon_placed");
    public static final ResourceKey<PlacedFeature> CHEESE_CAP_PLACED_KEY = createKey("cheese_cap_placed");
    public static final ResourceKey<PlacedFeature> MOSSHROOM_PLACED_KEY = createKey("mosshroom_placed");
    public static final ResourceKey<PlacedFeature> PROJECTRUFFLE_PLACED_KEY = createKey("projectruffle_placed");
    public static final ResourceKey<PlacedFeature> SILICINA_PLACED_KEY = createKey("silicina_placed");
    public static final ResourceKey<PlacedFeature> PATCH_PARALILY_PLACED_KEY = createKey("patch_paralily_placed");
    public static final ResourceKey<PlacedFeature> HOLTS_REFLECTION_PLACED_KEY = createKey("holts_reflection_placed");
    public static final ResourceKey<PlacedFeature> RAZORLEAF_BUD_PLACED_KEY = createKey("razorleaf_bud_placed");
    public static final ResourceKey<PlacedFeature> STAREBLOSSOM_PLACED_KEY = createKey("stareblossom_placed");
    public static final ResourceKey<PlacedFeature> MASKONITE_PLACED_KEY = createKey("maskonite_placed");
    public static final ResourceKey<PlacedFeature> RIE_BUSH_PLACED_KEY = createKey("rie_bush_placed");
    public static final ResourceKey<PlacedFeature> SPIKY_IVY_PLACED_KEY = createKey("spiky_ivy_placed");
    public static final ResourceKey<PlacedFeature> TREE_HOLLOW_WEST_PLACED_KEY = createKey("tree_hollow_west_placed");
    public static final ResourceKey<PlacedFeature> TREE_HOLLOW_EAST_PLACED_KEY = createKey("tree_hollow_east_placed");
    public static final ResourceKey<PlacedFeature> TREE_HOLLOW_NORTH_PLACED_KEY = createKey("tree_hollow_north_placed");
    public static final ResourceKey<PlacedFeature> TREE_HOLLOW_SOUTH_PLACED_KEY = createKey("tree_hollow_south_placed");
    public static final ResourceKey<PlacedFeature> PATCH_MOSS_RIE_PLACED_KEY = createKey("patch_moss_rie_placed");


    public static void bootstrap(BootstapContext<PlacedFeature> context) {
        HolderGetter<ConfiguredFeature<?, ?>> configuredFeatures = context.lookup(Registries.CONFIGURED_FEATURE);

        register(context, RIE_TREE_CHECKED_KEY, configuredFeatures.getOrThrow(ModConfiguredFeatures.RIE_TREE_KEY),
                FancyTreeFeature.treePlacement(PlacementUtils.countExtra(2, 0.1f, 1)));
        register(context, HOLLOW_EAST_RIE_TREE_CHECKED_KEY, configuredFeatures.getOrThrow(ModConfiguredFeatures.HOLLOW_EAST_RIE_TREE_KEY),
                FancyTreeFeature.treePlacement(PlacementUtils.countExtra(2, 0.1f, 1)));
        register(context, HOLLOW_WEST_RIE_TREE_CHECKED_KEY, configuredFeatures.getOrThrow(ModConfiguredFeatures.HOLLOW_WEST_RIE_TREE_KEY),
                FancyTreeFeature.treePlacement(PlacementUtils.countExtra(2, 0.1f, 1)));
        register(context, HOLLOW_NORTH_RIE_TREE_CHECKED_KEY, configuredFeatures.getOrThrow(ModConfiguredFeatures.HOLLOW_NORTH_RIE_TREE_KEY),
                FancyTreeFeature.treePlacement(PlacementUtils.countExtra(2, 0.1f, 1)));
        register(context, HOLLOW_SOUTH_RIE_TREE_CHECKED_KEY, configuredFeatures.getOrThrow(ModConfiguredFeatures.HOLLOW_SOUTH_RIE_TREE_KEY),
                FancyTreeFeature.treePlacement(PlacementUtils.countExtra(2, 0.1f, 1)));
        register(context, BIBBIT_HOLLOW_EAST_RIE_TREE_CHECKED_KEY, configuredFeatures.getOrThrow(ModConfiguredFeatures.BIBBIT_HOLLOW_EAST_RIE_TREE_KEY),
                FancyTreeFeature.treePlacement(PlacementUtils.countExtra(2, 0.1f, 1)));
        register(context, BIBBIT_HOLLOW_WEST_RIE_TREE_CHECKED_KEY, configuredFeatures.getOrThrow(ModConfiguredFeatures.BIBBIT_HOLLOW_WEST_RIE_TREE_KEY),
                FancyTreeFeature.treePlacement(PlacementUtils.countExtra(2, 0.1f, 1)));
        register(context, BIBBIT_HOLLOW_NORTH_RIE_TREE_CHECKED_KEY, configuredFeatures.getOrThrow(ModConfiguredFeatures.BIBBIT_HOLLOW_NORTH_RIE_TREE_KEY),
                FancyTreeFeature.treePlacement(PlacementUtils.countExtra(2, 0.1f, 1)));
        register(context, BIBBIT_HOLLOW_SOUTH_RIE_TREE_CHECKED_KEY, configuredFeatures.getOrThrow(ModConfiguredFeatures.BIBBIT_HOLLOW_SOUTH_RIE_TREE_KEY),
                FancyTreeFeature.treePlacement(PlacementUtils.countExtra(2, 0.1f, 1)));

        register(context, FANCY_RIE_TREE_CHECKED_KEY, configuredFeatures.getOrThrow(ModConfiguredFeatures.FANCY_RIE_TREE_KEY),
                FancyTreeFeature.treePlacement(PlacementUtils.countExtra(3, 0.1f, 1)));
        register(context, HOLLOW_SOUTH_FANCY_RIE_TREE_CHECKED_KEY, configuredFeatures.getOrThrow(ModConfiguredFeatures.HOLLOW_SOUTH_FANCY_RIE_TREE_KEY),
                FancyTreeFeature.treePlacement(PlacementUtils.countExtra(3, 0.1f, 1)));
        register(context, HOLLOW_NORTH_FANCY_RIE_TREE_CHECKED_KEY, configuredFeatures.getOrThrow(ModConfiguredFeatures.HOLLOW_NORTH_FANCY_RIE_TREE_KEY),
                FancyTreeFeature.treePlacement(PlacementUtils.countExtra(3, 0.1f, 1)));
        register(context, HOLLOW_WEST_FANCY_RIE_TREE_CHECKED_KEY, configuredFeatures.getOrThrow(ModConfiguredFeatures.HOLLOW_WEST_FANCY_RIE_TREE_KEY),
                FancyTreeFeature.treePlacement(PlacementUtils.countExtra(3, 0.1f, 1)));
        register(context, HOLLOW_EAST_FANCY_RIE_TREE_CHECKED_KEY, configuredFeatures.getOrThrow(ModConfiguredFeatures.HOLLOW_EAST_FANCY_RIE_TREE_KEY),
                FancyTreeFeature.treePlacement(PlacementUtils.countExtra(3, 0.1f, 1)));
        register(context, BIBBIT_HOLLOW_SOUTH_FANCY_RIE_TREE_CHECKED_KEY, configuredFeatures.getOrThrow(ModConfiguredFeatures.BIBBIT_HOLLOW_SOUTH_FANCY_RIE_TREE_KEY),
                FancyTreeFeature.treePlacement(PlacementUtils.countExtra(3, 0.1f, 1)));
        register(context, BIBBIT_HOLLOW_NORTH_FANCY_RIE_TREE_CHECKED_KEY, configuredFeatures.getOrThrow(ModConfiguredFeatures.BIBBIT_HOLLOW_NORTH_FANCY_RIE_TREE_KEY),
                FancyTreeFeature.treePlacement(PlacementUtils.countExtra(3, 0.1f, 1)));
        register(context, BIBBIT_HOLLOW_WEST_FANCY_RIE_TREE_CHECKED_KEY, configuredFeatures.getOrThrow(ModConfiguredFeatures.BIBBIT_HOLLOW_WEST_FANCY_RIE_TREE_KEY),
                FancyTreeFeature.treePlacement(PlacementUtils.countExtra(3, 0.1f, 1)));
        register(context, BIBBIT_HOLLOW_EAST_FANCY_RIE_TREE_CHECKED_KEY, configuredFeatures.getOrThrow(ModConfiguredFeatures.BIBBIT_HOLLOW_EAST_FANCY_RIE_TREE_KEY),
                FancyTreeFeature.treePlacement(PlacementUtils.countExtra(3, 0.1f, 1)));
        register(context, ENT_FACE_SOUTH_CHECKED_KEY,
                configuredFeatures.getOrThrow(ModConfiguredFeatures.ENT_FACE_SOUTH_KEY),
                EntFeature.treePlacement(RarityFilter.onAverageOnceEvery(39)));
        register(context, ENT_FACE_NORTH_CHECKED_KEY, configuredFeatures.getOrThrow(ModConfiguredFeatures.ENT_FACE_NORTH_KEY),
                EntFeature.treePlacement(RarityFilter.onAverageOnceEvery(39)));
        register(context, ENT_FACE_WEST_CHECKED_KEY, configuredFeatures.getOrThrow(ModConfiguredFeatures.ENT_FACE_WEST_KEY),
                EntFeature.treePlacement(RarityFilter.onAverageOnceEvery(39)));
        register(context, ENT_FACE_EAST_CHECKED_KEY, configuredFeatures.getOrThrow(ModConfiguredFeatures.ENT_FACE_EAST_KEY),
                EntFeature.treePlacement(RarityFilter.onAverageOnceEvery(39)));

        register(context, RIE_FLOWER_PLACED_KEY, configuredFeatures.getOrThrow(ModConfiguredFeatures.RIE_FLOWER_KEY), RarityFilter.onAverageOnceEvery(2147483647),
                InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP, BiomeFilter.biome());

        register(context, SHIVERALIS_PLACED_KEY, configuredFeatures.getOrThrow(ModConfiguredFeatures.SHIVERALIS_KEY), RarityFilter.onAverageOnceEvery(6),
                InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP, BiomeFilter.biome());
        register(context, TRIPLOVER_PLACED_KEY, configuredFeatures.getOrThrow(ModConfiguredFeatures.TRIPLOVER_KEY), RarityFilter.onAverageOnceEvery(2),
                InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP, BiomeFilter.biome());
        register(context, GLOSSOM_PLACED_KEY, configuredFeatures.getOrThrow(ModConfiguredFeatures.GLOSSOM_KEY), RarityFilter.onAverageOnceEvery(6),
                InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP, BiomeFilter.biome());
        register(context, FAIRAPIER_PLACED_KEY, configuredFeatures.getOrThrow(ModConfiguredFeatures.FAIRAPIER_KEY), RarityFilter.onAverageOnceEvery(6),
                InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP, BiomeFilter.biome());
        register(context, PROJECTRUFFLE_PLACED_KEY, configuredFeatures.getOrThrow(ModConfiguredFeatures.PROJECTRUFFLE_KEY), RarityFilter.onAverageOnceEvery(6),
                InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP, BiomeFilter.biome());
        register(context, SILICINA_PLACED_KEY, configuredFeatures.getOrThrow(ModConfiguredFeatures.SILICINA_KEY), RarityFilter.onAverageOnceEvery(6),
                InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP, BiomeFilter.biome());
        register(context, HOLTS_REFLECTION_PLACED_KEY, configuredFeatures.getOrThrow(ModConfiguredFeatures.HOLTS_REFLECTION_KEY), RarityFilter.onAverageOnceEvery(6),
                InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP, BiomeFilter.biome());
        register(context, RAZORLEAF_BUD_PLACED_KEY, configuredFeatures.getOrThrow(ModConfiguredFeatures.RAZORLEAF_BUD_KEY), RarityFilter.onAverageOnceEvery(6),
                InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP, BiomeFilter.biome());
        register(context, STAREBLOSSOM_PLACED_KEY, configuredFeatures.getOrThrow(ModConfiguredFeatures.STAREBLOSSOM_KEY), RarityFilter.onAverageOnceEvery(6),
                InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP, BiomeFilter.biome());
        register(context, RPGIROLLE_PLACED_KEY, configuredFeatures.getOrThrow(ModConfiguredFeatures.RPGIROLLE_KEY), RarityFilter.onAverageOnceEvery(8),
                InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP, BiomeFilter.biome());
        register(context, MIMOSSA_PLACED_KEY, configuredFeatures.getOrThrow(ModConfiguredFeatures.MIMOSSA_KEY), RarityFilter.onAverageOnceEvery(5),
                InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP, BiomeFilter.biome());
        register(context, TYPHON_PLACED_KEY, configuredFeatures.getOrThrow(ModConfiguredFeatures.TYPHON_KEY), RarityFilter.onAverageOnceEvery(5),
                InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP, BiomeFilter.biome());
        register(context, CHEESE_CAP_PLACED_KEY, configuredFeatures.getOrThrow(ModConfiguredFeatures.CHEESE_CAP_KEY), RarityFilter.onAverageOnceEvery(5),
                InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP, BiomeFilter.biome());
        register(context, MOSSHROOM_PLACED_KEY, configuredFeatures.getOrThrow(ModConfiguredFeatures.MOSSHROOM_KEY), RarityFilter.onAverageOnceEvery(5),
                InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP, BiomeFilter.biome());
        register(context, PATCH_PARALILY_PLACED_KEY, configuredFeatures.getOrThrow(ModConfiguredFeatures.PATCH_PARALILY_KEY), worldSurfaceSquaredWithCount(4));
        register(context, SPIKY_IVY_PLACED_KEY, configuredFeatures.getOrThrow(ModConfiguredFeatures.SPIKY_IVY_KEY), CountPlacement.of(6), InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP_OCEAN_FLOOR, PlacementUtils.HEIGHTMAP_WORLD_SURFACE, SurfaceWaterDepthFilter.forMaxDepth(0), BiomeFilter.biome(), RarityFilter.onAverageOnceEvery(4));
        register(context, TREE_HOLLOW_WEST_PLACED_KEY, configuredFeatures.getOrThrow(ModConfiguredFeatures.TREE_HOLLOW_WEST_KEY), CountPlacement.of(6), InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP_OCEAN_FLOOR, PlacementUtils.HEIGHTMAP_WORLD_SURFACE, SurfaceWaterDepthFilter.forMaxDepth(0), BiomeFilter.biome());
        register(context, TREE_HOLLOW_EAST_PLACED_KEY, configuredFeatures.getOrThrow(ModConfiguredFeatures.TREE_HOLLOW_EAST_KEY), CountPlacement.of(6), InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP_OCEAN_FLOOR, PlacementUtils.HEIGHTMAP_WORLD_SURFACE, SurfaceWaterDepthFilter.forMaxDepth(0), BiomeFilter.biome());
        register(context, TREE_HOLLOW_SOUTH_PLACED_KEY, configuredFeatures.getOrThrow(ModConfiguredFeatures.TREE_HOLLOW_SOUTH_KEY), CountPlacement.of(6), InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP_OCEAN_FLOOR, PlacementUtils.HEIGHTMAP_WORLD_SURFACE, SurfaceWaterDepthFilter.forMaxDepth(0), BiomeFilter.biome());
        register(context, TREE_HOLLOW_NORTH_PLACED_KEY, configuredFeatures.getOrThrow(ModConfiguredFeatures.TREE_HOLLOW_NORTH_KEY), CountPlacement.of(6), InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP_OCEAN_FLOOR, PlacementUtils.HEIGHTMAP_WORLD_SURFACE, SurfaceWaterDepthFilter.forMaxDepth(0), BiomeFilter.biome());
        register(context, MASKONITE_PLACED_KEY, configuredFeatures.getOrThrow(ModConfiguredFeatures.MASKONITE_KEY), CountPlacement.of(2), InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP_OCEAN_FLOOR, PlacementUtils.HEIGHTMAP_WORLD_SURFACE, SurfaceWaterDepthFilter.forMaxDepth(0), BiomeFilter.biome());
        register(context, RIE_BUSH_PLACED_KEY, configuredFeatures.getOrThrow(ModConfiguredFeatures.RIE_BUSH_KEY), onLandPlacement(PlacementUtils.countExtra(3, 0.1f, 1)));
        register(context, PATCH_MOSS_RIE_PLACED_KEY, configuredFeatures.getOrThrow(ModConfiguredFeatures.PATCH_MOSS_RIE_KEY), worldSurfaceSquaredWithCount(25));
    }

    public static List<PlacementModifier> worldSurfaceSquaredWithCount(int p_195475_) {
        return List.of(CountPlacement.of(p_195475_), InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP_WORLD_SURFACE, BiomeFilter.biome());
    }
    private static ImmutableList.Builder<PlacementModifier> onLandPlacementBase(PlacementModifier placement) {
        return ImmutableList.<PlacementModifier>builder()
                .add(placement)
                .add(InSquarePlacement.spread())
                .add(SurfaceWaterDepthFilter.forMaxDepth(0))
                .add(PlacementUtils.HEIGHTMAP_OCEAN_FLOOR)
                .add(PlacementUtils.HEIGHTMAP_WORLD_SURFACE)
                .add(BlockPredicateFilter.forPredicate(BlockPredicate.matchesBlocks(new BlockPos(0, -1, 0), List.of(Blocks.GRASS_BLOCK))))
                .add(BiomeFilter.biome());
    }

    private static List<PlacementModifier> onLandPlacement(PlacementModifier placement) {
        return onLandPlacementBase(placement).build();
    }

    private static ResourceKey<PlacedFeature> createKey(String name) {
        return ResourceKey.create(Registries.PLACED_FEATURE, new ResourceLocation(RPGworldMod.MOD_ID, name));
    }

    private static void register(BootstapContext<PlacedFeature> context, ResourceKey<PlacedFeature> key, Holder<ConfiguredFeature<?, ?>> configuration,
                                 List<PlacementModifier> modifiers) {
        context.register(key, new PlacedFeature(configuration, List.copyOf(modifiers)));
    }

    private static void register(BootstapContext<PlacedFeature> context, ResourceKey<PlacedFeature> key, Holder<ConfiguredFeature<?, ?>> configuration,
                                 PlacementModifier... modifiers) {
        register(context, key, configuration, List.of(modifiers));
    }
}