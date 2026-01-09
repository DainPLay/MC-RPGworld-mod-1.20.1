package net.dainplay.rpgworldmod.world.feature;

import com.google.common.collect.ImmutableList;
import net.dainplay.rpgworldmod.RPGworldMod;
import net.dainplay.rpgworldmod.block.ModBlocks;
import net.dainplay.rpgworldmod.block.custom.DirectionalFlowerBlock;
import net.dainplay.rpgworldmod.block.custom.HoltsReflectionBlock;
import net.dainplay.rpgworldmod.block.custom.TreeHollowBlock;
import net.dainplay.rpgworldmod.entity.custom.Razorleaf;
import net.dainplay.rpgworldmod.features.TrunkDecorator;
import net.dainplay.rpgworldmod.features.FancyOakTreeFeature;
import net.dainplay.rpgworldmod.sounds.RPGSounds;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.Music;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.PinkPetalsBlock;
import net.minecraft.world.level.block.SweetBerryBushBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.*;
import net.minecraft.world.level.levelgen.feature.featuresize.TwoLayersFeatureSize;
import net.minecraft.world.level.levelgen.feature.foliageplacers.BlobFoliagePlacer;
import net.minecraft.world.level.levelgen.feature.foliageplacers.BushFoliagePlacer;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.stateproviders.WeightedStateProvider;
import net.minecraft.world.level.levelgen.feature.treedecorators.LeaveVineDecorator;
import net.minecraft.world.level.levelgen.feature.treedecorators.TrunkVineDecorator;
import net.minecraft.world.level.levelgen.feature.trunkplacers.StraightTrunkPlacer;

import static net.dainplay.rpgworldmod.block.custom.ParalilyBlock.AGE;
import static net.dainplay.rpgworldmod.block.custom.TreeHollowBlock.FACING;

public class ModConfiguredFeatures {
    public static final ResourceKey<ConfiguredFeature<?, ?>> RIE_TREE_KEY = registerKey("rie_tree");
    public static final ResourceKey<ConfiguredFeature<?, ?>> HOLLOW_EAST_RIE_TREE_KEY = registerKey("hollow_east_rie_tree");
    public static final ResourceKey<ConfiguredFeature<?, ?>> HOLLOW_WEST_RIE_TREE_KEY = registerKey("hollow_west_rie_tree");
    public static final ResourceKey<ConfiguredFeature<?, ?>> HOLLOW_NORTH_RIE_TREE_KEY = registerKey("hollow_north_rie_tree");
    public static final ResourceKey<ConfiguredFeature<?, ?>> HOLLOW_SOUTH_RIE_TREE_KEY = registerKey("hollow_south_rie_tree");
    public static final ResourceKey<ConfiguredFeature<?, ?>> BIBBIT_HOLLOW_EAST_RIE_TREE_KEY = registerKey("bibbit_hollow_east_rie_tree");
    public static final ResourceKey<ConfiguredFeature<?, ?>> BIBBIT_HOLLOW_WEST_RIE_TREE_KEY = registerKey("bibbit_hollow_west_rie_tree");
    public static final ResourceKey<ConfiguredFeature<?, ?>> BIBBIT_HOLLOW_NORTH_RIE_TREE_KEY = registerKey("bibbit_hollow_north_rie_tree");
    public static final ResourceKey<ConfiguredFeature<?, ?>> BIBBIT_HOLLOW_SOUTH_RIE_TREE_KEY = registerKey("bibbit_hollow_south_rie_tree");
    public static final ResourceKey<ConfiguredFeature<?, ?>> FANCY_RIE_TREE_KEY = registerKey("fancy_rie_tree");
    public static final ResourceKey<ConfiguredFeature<?, ?>> HOLLOW_SOUTH_FANCY_RIE_TREE_KEY = registerKey("hollow_south_fancy_rie_tree");
    public static final ResourceKey<ConfiguredFeature<?, ?>> HOLLOW_NORTH_FANCY_RIE_TREE_KEY = registerKey("hollow_north_fancy_rie_tree");
    public static final ResourceKey<ConfiguredFeature<?, ?>> HOLLOW_WEST_FANCY_RIE_TREE_KEY = registerKey("hollow_west_fancy_rie_tree");
    public static final ResourceKey<ConfiguredFeature<?, ?>> HOLLOW_EAST_FANCY_RIE_TREE_KEY = registerKey("hollow_east_fancy_rie_tree");
    public static final ResourceKey<ConfiguredFeature<?, ?>> BIBBIT_HOLLOW_SOUTH_FANCY_RIE_TREE_KEY = registerKey("bibbit_hollow_south_fancy_rie_tree");
    public static final ResourceKey<ConfiguredFeature<?, ?>> BIBBIT_HOLLOW_NORTH_FANCY_RIE_TREE_KEY = registerKey("bibbit_hollow_north_fancy_rie_tree");
    public static final ResourceKey<ConfiguredFeature<?, ?>> BIBBIT_HOLLOW_WEST_FANCY_RIE_TREE_KEY = registerKey("bibbit_hollow_west_fancy_rie_tree");
    public static final ResourceKey<ConfiguredFeature<?, ?>> BIBBIT_HOLLOW_EAST_FANCY_RIE_TREE_KEY = registerKey("bibbit_hollow_east_fancy_rie_tree");
    public static final ResourceKey<ConfiguredFeature<?, ?>> RIE_FLOWER_KEY = registerKey("rie_flower");
    public static final ResourceKey<ConfiguredFeature<?, ?>> FAIRAPIER_KEY = registerKey("fairapier");
    public static final ResourceKey<ConfiguredFeature<?, ?>> SHIVERALIS_KEY = registerKey("shiveralis");
    public static final ResourceKey<ConfiguredFeature<?, ?>> TRIPLOVER_KEY = registerKey("triplover");
    public static final ResourceKey<ConfiguredFeature<?, ?>> GLOSSOM_KEY = registerKey("glossom");
    public static final ResourceKey<ConfiguredFeature<?, ?>> RPGIROLLE_KEY = registerKey("rpgirolle");
    public static final ResourceKey<ConfiguredFeature<?, ?>> MIMOSSA_KEY = registerKey("mimossa");
    public static final ResourceKey<ConfiguredFeature<?, ?>> TYPHON_KEY = registerKey("typhon");
    public static final ResourceKey<ConfiguredFeature<?, ?>> CHEESE_CAP_KEY = registerKey("cheese_cap");
    public static final ResourceKey<ConfiguredFeature<?, ?>> MOSSHROOM_KEY = registerKey("mosshroom");
    public static final ResourceKey<ConfiguredFeature<?, ?>> PROJECTRUFFLE_KEY = registerKey("projectruffle");
    public static final ResourceKey<ConfiguredFeature<?, ?>> SILICINA_KEY = registerKey("silicina");
    public static final ResourceKey<ConfiguredFeature<?, ?>> PATCH_PARALILY_KEY = registerKey("patch_paralily");
    public static final ResourceKey<ConfiguredFeature<?, ?>> HOLTS_REFLECTION_KEY = registerKey("holts_reflection");
    public static final ResourceKey<ConfiguredFeature<?, ?>> RAZORLEAF_BUD_KEY = registerKey("razorleaf_bud");
    public static final ResourceKey<ConfiguredFeature<?, ?>> STAREBLOSSOM_KEY = registerKey("stareblossom");
    public static final ResourceKey<ConfiguredFeature<?, ?>> MASKONITE_KEY = registerKey("maskonite");
    public static final ResourceKey<ConfiguredFeature<?, ?>> RIE_BUSH_KEY = registerKey("rie_bush");
    public static final ResourceKey<ConfiguredFeature<?, ?>> SPIKY_IVY_KEY = registerKey("spiky_ivy");
    public static final ResourceKey<ConfiguredFeature<?, ?>> TREE_HOLLOW_WEST_KEY = registerKey("tree_hollow_west");
    public static final ResourceKey<ConfiguredFeature<?, ?>> TREE_HOLLOW_EAST_KEY = registerKey("tree_hollow_east");
    public static final ResourceKey<ConfiguredFeature<?, ?>> TREE_HOLLOW_NORTH_KEY = registerKey("tree_hollow_north");
    public static final ResourceKey<ConfiguredFeature<?, ?>> TREE_HOLLOW_SOUTH_KEY = registerKey("tree_hollow_south");
    public static final ResourceKey<ConfiguredFeature<?, ?>> PATCH_MOSS_RIE_KEY = registerKey("patch_moss_rie");
    private static TreeConfiguration.TreeConfigurationBuilder createRieTree() {
        return createStraightBlobTree(ModBlocks.RIE_LOG.get(), ModBlocks.RIE_LEAVES.get(), 6, 3, 0, 2);
    }
    private static TreeConfiguration.TreeConfigurationBuilder createStraightBlobTree(Block p_195147_, Block p_195148_, int p_195149_, int p_195150_, int p_195151_, int p_195152_) {
        return new TreeConfiguration.TreeConfigurationBuilder(BlockStateProvider.simple(p_195147_), new StraightTrunkPlacer(p_195149_, p_195150_, p_195151_), BlockStateProvider.simple(p_195148_), new BlobFoliagePlacer(ConstantInt.of(p_195152_), ConstantInt.of(0), 3), new TwoLayersFeatureSize(1, 0, 1));
    }
    public static void bootstrap(BootstapContext<ConfiguredFeature<?, ?>> context) {

        register(context, RIE_TREE_KEY, Feature.TREE, createRieTree().decorators(ImmutableList.of(
                TrunkVineDecorator.INSTANCE,
                new LeaveVineDecorator(0.1F))).ignoreVines().build());

        register(context, HOLLOW_EAST_RIE_TREE_KEY, Feature.TREE, createRieTree().decorators(ImmutableList.of(
                TrunkVineDecorator.INSTANCE,
                new TrunkDecorator(6, 1.0f, BlockStateProvider.simple(ModBlocks.RIE_HOLLOW.get().defaultBlockState().setValue(FACING, Direction.EAST))),
                new LeaveVineDecorator(0.1F))).ignoreVines().build());

        register(context, HOLLOW_WEST_RIE_TREE_KEY, Feature.TREE, createRieTree().decorators(ImmutableList.of(
                TrunkVineDecorator.INSTANCE,
                new TrunkDecorator(6, 1.0f, BlockStateProvider.simple(ModBlocks.RIE_HOLLOW.get().defaultBlockState().setValue(FACING, Direction.WEST))),
                new LeaveVineDecorator(0.1F))).ignoreVines().build());

        register(context, HOLLOW_NORTH_RIE_TREE_KEY, Feature.TREE, createRieTree().decorators(ImmutableList.of(
                TrunkVineDecorator.INSTANCE,
                new TrunkDecorator(6, 1.0f, BlockStateProvider.simple(ModBlocks.RIE_HOLLOW.get().defaultBlockState().setValue(FACING, Direction.NORTH))),
                new LeaveVineDecorator(0.1F))).ignoreVines().build());

        register(context, HOLLOW_SOUTH_RIE_TREE_KEY, Feature.TREE, createRieTree().decorators(ImmutableList.of(
                TrunkVineDecorator.INSTANCE,
                new TrunkDecorator(6, 1.0f, BlockStateProvider.simple(ModBlocks.RIE_HOLLOW.get().defaultBlockState().setValue(FACING, Direction.SOUTH))),
                new LeaveVineDecorator(0.1F))).ignoreVines().build());

        register(context, BIBBIT_HOLLOW_EAST_RIE_TREE_KEY, Feature.TREE, createRieTree().decorators(ImmutableList.of(
                TrunkVineDecorator.INSTANCE,
                new TrunkDecorator(6, 1.0f, BlockStateProvider.simple(ModBlocks.RIE_HOLLOW.get().defaultBlockState().setValue(TreeHollowBlock.HAS_CONTENTS, true).setValue(FACING, Direction.EAST))),
                new LeaveVineDecorator(0.1F))).ignoreVines().build());

        register(context, BIBBIT_HOLLOW_WEST_RIE_TREE_KEY, Feature.TREE, createRieTree().decorators(ImmutableList.of(
                TrunkVineDecorator.INSTANCE,
                new TrunkDecorator(6, 1.0f, BlockStateProvider.simple(ModBlocks.RIE_HOLLOW.get().defaultBlockState().setValue(TreeHollowBlock.HAS_CONTENTS, true).setValue(FACING, Direction.WEST))),
                new LeaveVineDecorator(0.1F))).ignoreVines().build());

        register(context, BIBBIT_HOLLOW_NORTH_RIE_TREE_KEY, Feature.TREE, createRieTree().decorators(ImmutableList.of(
                TrunkVineDecorator.INSTANCE,
                new TrunkDecorator(6, 1.0f, BlockStateProvider.simple(ModBlocks.RIE_HOLLOW.get().defaultBlockState().setValue(TreeHollowBlock.HAS_CONTENTS, true).setValue(FACING, Direction.NORTH))),
                new LeaveVineDecorator(0.1F))).ignoreVines().build());

        register(context, BIBBIT_HOLLOW_SOUTH_RIE_TREE_KEY, Feature.TREE, createRieTree().decorators(ImmutableList.of(
                TrunkVineDecorator.INSTANCE,
                new TrunkDecorator(6, 1.0f, BlockStateProvider.simple(ModBlocks.RIE_HOLLOW.get().defaultBlockState().setValue(TreeHollowBlock.HAS_CONTENTS, true).setValue(FACING, Direction.SOUTH))),
                new LeaveVineDecorator(0.1F))).ignoreVines().build());

        register(context, FANCY_RIE_TREE_KEY, Feature.TREE, FancyOakTreeFeature.createFancyOak().build());

        register(context, HOLLOW_SOUTH_FANCY_RIE_TREE_KEY, Feature.TREE, FancyOakTreeFeature.createFancyOak().decorators(ImmutableList.of(
                TrunkVineDecorator.INSTANCE,
                new TrunkDecorator(6, 1.0f, BlockStateProvider.simple(ModBlocks.RIE_HOLLOW.get().defaultBlockState().setValue(FACING, Direction.SOUTH))))).build());

        register(context, HOLLOW_NORTH_FANCY_RIE_TREE_KEY, Feature.TREE, FancyOakTreeFeature.createFancyOak().decorators(ImmutableList.of(
                TrunkVineDecorator.INSTANCE,
                new TrunkDecorator(6, 1.0f, BlockStateProvider.simple(ModBlocks.RIE_HOLLOW.get().defaultBlockState().setValue(FACING, Direction.NORTH))))).build());

        register(context, HOLLOW_WEST_FANCY_RIE_TREE_KEY, Feature.TREE, FancyOakTreeFeature.createFancyOak().decorators(ImmutableList.of(
                TrunkVineDecorator.INSTANCE,
                new TrunkDecorator(6, 1.0f, BlockStateProvider.simple(ModBlocks.RIE_HOLLOW.get().defaultBlockState().setValue(FACING, Direction.WEST))))).build());

        register(context, HOLLOW_EAST_FANCY_RIE_TREE_KEY, Feature.TREE, FancyOakTreeFeature.createFancyOak().decorators(ImmutableList.of(
                TrunkVineDecorator.INSTANCE,
                new TrunkDecorator(6, 1.0f, BlockStateProvider.simple(ModBlocks.RIE_HOLLOW.get().defaultBlockState().setValue(FACING, Direction.EAST))))).build());

        register(context, BIBBIT_HOLLOW_SOUTH_FANCY_RIE_TREE_KEY, Feature.TREE, FancyOakTreeFeature.createFancyOak().decorators(ImmutableList.of(
                TrunkVineDecorator.INSTANCE,
                new TrunkDecorator(6, 1.0f, BlockStateProvider.simple(ModBlocks.RIE_HOLLOW.get().defaultBlockState().setValue(TreeHollowBlock.HAS_CONTENTS, true).setValue(FACING, Direction.SOUTH))))).build());

        register(context, BIBBIT_HOLLOW_NORTH_FANCY_RIE_TREE_KEY, Feature.TREE, FancyOakTreeFeature.createFancyOak().decorators(ImmutableList.of(
                TrunkVineDecorator.INSTANCE,
                new TrunkDecorator(6, 1.0f, BlockStateProvider.simple(ModBlocks.RIE_HOLLOW.get().defaultBlockState().setValue(TreeHollowBlock.HAS_CONTENTS, true).setValue(FACING, Direction.NORTH))))).build());

        register(context, BIBBIT_HOLLOW_WEST_FANCY_RIE_TREE_KEY, Feature.TREE, FancyOakTreeFeature.createFancyOak().decorators(ImmutableList.of(
                TrunkVineDecorator.INSTANCE,
                new TrunkDecorator(6, 1.0f, BlockStateProvider.simple(ModBlocks.RIE_HOLLOW.get().defaultBlockState().setValue(TreeHollowBlock.HAS_CONTENTS, true).setValue(FACING, Direction.WEST))))).build());

        register(context, BIBBIT_HOLLOW_EAST_FANCY_RIE_TREE_KEY, Feature.TREE, FancyOakTreeFeature.createFancyOak().decorators(ImmutableList.of(
                TrunkVineDecorator.INSTANCE,
                new TrunkDecorator(6, 1.0f, BlockStateProvider.simple(ModBlocks.RIE_HOLLOW.get().defaultBlockState().setValue(TreeHollowBlock.HAS_CONTENTS, true).setValue(FACING, Direction.EAST))))).build());

        register(context, RIE_FLOWER_KEY, Feature.FLOWER,
                new RandomPatchConfiguration(96, 6, 2, PlacementUtils.onlyWhenEmpty(Feature.SIMPLE_BLOCK, new SimpleBlockConfiguration(new WeightedStateProvider(SimpleWeightedRandomList.<BlockState>builder()
                        .add(ModBlocks.RPGIROLLE.get().defaultBlockState(), 8)
                        .add(ModBlocks.PROJECTRUFFLE.get().defaultBlockState(), 8)
                        .add(ModBlocks.SILICINA.get().defaultBlockState(), 8)
                        .add(ModBlocks.MIMOSSA.get().defaultBlockState(), 6)
                        .add(ModBlocks.TYPHON.get().defaultBlockState(), 6)
                        .add(ModBlocks.WILD_FAIRAPIER.get().defaultBlockState(), 8)
                        .add(ModBlocks.CHEESE_CAP.get().defaultBlockState(), 6)
                        .add(ModBlocks.MOSSHROOM.get().defaultBlockState(), 6)
                        .add(ModBlocks.RAZORLEAF_BUD.get().defaultBlockState(), 1)
                        .add(ModBlocks.STAREBLOSSOM.get().defaultBlockState(), 2)
                        .add(ModBlocks.HOLTS_REFLECTION.get().defaultBlockState().setValue(HoltsReflectionBlock.FACING, Direction.NORTH), 1)
                        .add(ModBlocks.HOLTS_REFLECTION.get().defaultBlockState().setValue(HoltsReflectionBlock.FACING, Direction.SOUTH), 1)
                        .add(ModBlocks.HOLTS_REFLECTION.get().defaultBlockState().setValue(HoltsReflectionBlock.FACING, Direction.EAST), 1)
                        .add(ModBlocks.HOLTS_REFLECTION.get().defaultBlockState().setValue(HoltsReflectionBlock.FACING, Direction.WEST), 1)
                        .add(ModBlocks.GLOSSOM.get().defaultBlockState().setValue(DirectionalFlowerBlock.FACING, Direction.NORTH), 1)
                        .add(ModBlocks.GLOSSOM.get().defaultBlockState().setValue(DirectionalFlowerBlock.FACING, Direction.SOUTH), 1)
                        .add(ModBlocks.GLOSSOM.get().defaultBlockState().setValue(DirectionalFlowerBlock.FACING, Direction.EAST), 1)
                        .add(ModBlocks.GLOSSOM.get().defaultBlockState().setValue(DirectionalFlowerBlock.FACING, Direction.WEST), 1))))));

        register(context, FAIRAPIER_KEY,
                Feature.NO_BONEMEAL_FLOWER,
                new RandomPatchConfiguration(48, 3, 3, PlacementUtils.onlyWhenEmpty(Feature.SIMPLE_BLOCK,
                        new SimpleBlockConfiguration(BlockStateProvider.simple(ModBlocks.WILD_FAIRAPIER.get().defaultBlockState())))));
        register(context, SHIVERALIS_KEY, Feature.NO_BONEMEAL_FLOWER,
                new RandomPatchConfiguration(48, 3, 3, PlacementUtils.onlyWhenEmpty(Feature.SIMPLE_BLOCK,
                        new SimpleBlockConfiguration(BlockStateProvider.simple(ModBlocks.SHIVERALIS.get().defaultBlockState().setValue(SweetBerryBushBlock.AGE,
                                3))))));
        register(context, TRIPLOVER_KEY, Feature.NO_BONEMEAL_FLOWER,
                new RandomPatchConfiguration(96, 7, 3, PlacementUtils.onlyWhenEmpty(Feature.SIMPLE_BLOCK, new SimpleBlockConfiguration(new WeightedStateProvider(SimpleWeightedRandomList.<BlockState>builder()

                        .add(ModBlocks.TRIPLOVER.get().defaultBlockState().setValue(PinkPetalsBlock.FACING, Direction.NORTH).setValue(PinkPetalsBlock.AMOUNT,4), 1)
                        .add(ModBlocks.TRIPLOVER.get().defaultBlockState().setValue(PinkPetalsBlock.FACING, Direction.SOUTH).setValue(PinkPetalsBlock.AMOUNT,4), 1)
                        .add(ModBlocks.TRIPLOVER.get().defaultBlockState().setValue(PinkPetalsBlock.FACING, Direction.EAST).setValue(PinkPetalsBlock.AMOUNT,4), 1)
                        .add(ModBlocks.TRIPLOVER.get().defaultBlockState().setValue(PinkPetalsBlock.FACING, Direction.WEST).setValue(PinkPetalsBlock.AMOUNT,4), 1)

                        .add(ModBlocks.TRIPLOVER.get().defaultBlockState().setValue(PinkPetalsBlock.FACING, Direction.NORTH).setValue(PinkPetalsBlock.AMOUNT,3), 1)
                        .add(ModBlocks.TRIPLOVER.get().defaultBlockState().setValue(PinkPetalsBlock.FACING, Direction.SOUTH).setValue(PinkPetalsBlock.AMOUNT,3), 1)
                        .add(ModBlocks.TRIPLOVER.get().defaultBlockState().setValue(PinkPetalsBlock.FACING, Direction.EAST).setValue(PinkPetalsBlock.AMOUNT,3), 1)
                        .add(ModBlocks.TRIPLOVER.get().defaultBlockState().setValue(PinkPetalsBlock.FACING, Direction.WEST).setValue(PinkPetalsBlock.AMOUNT,3), 1)

                        .add(ModBlocks.TRIPLOVER.get().defaultBlockState().setValue(PinkPetalsBlock.FACING, Direction.NORTH).setValue(PinkPetalsBlock.AMOUNT,2), 1)
                        .add(ModBlocks.TRIPLOVER.get().defaultBlockState().setValue(PinkPetalsBlock.FACING, Direction.SOUTH).setValue(PinkPetalsBlock.AMOUNT,2), 1)
                        .add(ModBlocks.TRIPLOVER.get().defaultBlockState().setValue(PinkPetalsBlock.FACING, Direction.EAST).setValue(PinkPetalsBlock.AMOUNT,2), 1)
                        .add(ModBlocks.TRIPLOVER.get().defaultBlockState().setValue(PinkPetalsBlock.FACING, Direction.WEST).setValue(PinkPetalsBlock.AMOUNT,2), 1)

                        .add(ModBlocks.TRIPLOVER.get().defaultBlockState().setValue(PinkPetalsBlock.FACING, Direction.NORTH).setValue(PinkPetalsBlock.AMOUNT,1), 1)
                        .add(ModBlocks.TRIPLOVER.get().defaultBlockState().setValue(PinkPetalsBlock.FACING, Direction.SOUTH).setValue(PinkPetalsBlock.AMOUNT,1), 1)
                        .add(ModBlocks.TRIPLOVER.get().defaultBlockState().setValue(PinkPetalsBlock.FACING, Direction.EAST).setValue(PinkPetalsBlock.AMOUNT,1), 1)
                        .add(ModBlocks.TRIPLOVER.get().defaultBlockState().setValue(PinkPetalsBlock.FACING, Direction.WEST).setValue(PinkPetalsBlock.AMOUNT,1), 1)
                )))));
        register(context, GLOSSOM_KEY, Feature.NO_BONEMEAL_FLOWER,
                new RandomPatchConfiguration(14, 2, 2, PlacementUtils.onlyWhenEmpty(Feature.SIMPLE_BLOCK, new SimpleBlockConfiguration(new WeightedStateProvider(SimpleWeightedRandomList.<BlockState>builder()

                        .add(ModBlocks.GLOSSOM.get().defaultBlockState().setValue(DirectionalFlowerBlock.FACING, Direction.NORTH), 1)
                        .add(ModBlocks.GLOSSOM.get().defaultBlockState().setValue(DirectionalFlowerBlock.FACING, Direction.SOUTH), 1)
                        .add(ModBlocks.GLOSSOM.get().defaultBlockState().setValue(DirectionalFlowerBlock.FACING, Direction.EAST), 1)
                        .add(ModBlocks.GLOSSOM.get().defaultBlockState().setValue(DirectionalFlowerBlock.FACING, Direction.WEST), 1)
                )))));
        register(context, RPGIROLLE_KEY, Feature.NO_BONEMEAL_FLOWER,
                new RandomPatchConfiguration(8, 2, 2, PlacementUtils.onlyWhenEmpty(Feature.SIMPLE_BLOCK,
                        new SimpleBlockConfiguration(BlockStateProvider.simple(ModBlocks.RPGIROLLE.get())))));
        register(context, MIMOSSA_KEY, Feature.NO_BONEMEAL_FLOWER,
                new RandomPatchConfiguration(14, 2, 2, PlacementUtils.onlyWhenEmpty(Feature.SIMPLE_BLOCK,
                        new SimpleBlockConfiguration(BlockStateProvider.simple(ModBlocks.MIMOSSA.get())))));
        register(context, TYPHON_KEY, Feature.NO_BONEMEAL_FLOWER,
                new RandomPatchConfiguration(14, 2, 2, PlacementUtils.onlyWhenEmpty(Feature.SIMPLE_BLOCK,
                        new SimpleBlockConfiguration(BlockStateProvider.simple(ModBlocks.TYPHON.get())))));
        register(context, CHEESE_CAP_KEY, Feature.NO_BONEMEAL_FLOWER,
                new RandomPatchConfiguration(14, 2, 2, PlacementUtils.onlyWhenEmpty(Feature.SIMPLE_BLOCK,
                        new SimpleBlockConfiguration(BlockStateProvider.simple(ModBlocks.CHEESE_CAP.get())))));
        register(context, MOSSHROOM_KEY, Feature.NO_BONEMEAL_FLOWER,
                new RandomPatchConfiguration(14, 2, 2, PlacementUtils.onlyWhenEmpty(Feature.SIMPLE_BLOCK,
                        new SimpleBlockConfiguration(BlockStateProvider.simple(ModBlocks.MOSSHROOM.get())))));
        register(context, PROJECTRUFFLE_KEY, Feature.NO_BONEMEAL_FLOWER,
                new RandomPatchConfiguration(14, 3, 2, PlacementUtils.onlyWhenEmpty(Feature.SIMPLE_BLOCK,
                        new SimpleBlockConfiguration(BlockStateProvider.simple(ModBlocks.PROJECTRUFFLE.get())))));
        register(context, SILICINA_KEY, Feature.NO_BONEMEAL_FLOWER,
                new RandomPatchConfiguration(14, 3, 2, PlacementUtils.onlyWhenEmpty(Feature.SIMPLE_BLOCK,
                        new SimpleBlockConfiguration(BlockStateProvider.simple(ModBlocks.SILICINA.get())))));
        register(context, PATCH_PARALILY_KEY, Feature.RANDOM_PATCH,
                new RandomPatchConfiguration(10, 7, 3, PlacementUtils.onlyWhenEmpty(Feature.SIMPLE_BLOCK,
                        new SimpleBlockConfiguration(new WeightedStateProvider(SimpleWeightedRandomList.<BlockState>builder()
                                .add(ModBlocks.PARALILY.get().defaultBlockState().setValue(AGE, 1), 1)
                                .add(Blocks.LILY_PAD.defaultBlockState(), 4))))));
        register(context, HOLTS_REFLECTION_KEY, Feature.NO_BONEMEAL_FLOWER,
                new RandomPatchConfiguration(10, 0, 2, PlacementUtils.onlyWhenEmpty(Feature.SIMPLE_BLOCK,
                        new SimpleBlockConfiguration(new WeightedStateProvider(SimpleWeightedRandomList.<BlockState>builder()
                                .add(ModBlocks.HOLTS_REFLECTION.get().defaultBlockState().setValue(HoltsReflectionBlock.FACING, Direction.NORTH), 1)
                                .add(ModBlocks.HOLTS_REFLECTION.get().defaultBlockState().setValue(HoltsReflectionBlock.FACING, Direction.SOUTH), 1)
                                .add(ModBlocks.HOLTS_REFLECTION.get().defaultBlockState().setValue(HoltsReflectionBlock.FACING, Direction.EAST), 1)
                                .add(ModBlocks.HOLTS_REFLECTION.get().defaultBlockState().setValue(HoltsReflectionBlock.FACING, Direction.WEST), 1))))));
        register(context, RAZORLEAF_BUD_KEY, Feature.NO_BONEMEAL_FLOWER,
                new RandomPatchConfiguration(10, 0, 2, PlacementUtils.onlyWhenEmpty(Feature.SIMPLE_BLOCK,
                        new SimpleBlockConfiguration(BlockStateProvider.simple((ModBlocks.RAZORLEAF_BUD.get()))))));
        register(context, STAREBLOSSOM_KEY, Feature.NO_BONEMEAL_FLOWER,
                new RandomPatchConfiguration(10, 0, 2, PlacementUtils.onlyWhenEmpty(Feature.SIMPLE_BLOCK,
                        new SimpleBlockConfiguration(BlockStateProvider.simple((ModBlocks.STAREBLOSSOM.get()))))));
        register(context, MASKONITE_KEY, Feature.FOREST_ROCK, new BlockStateConfiguration(ModBlocks.MASKONITE_BLOCK.get().defaultBlockState()));
        register(context, RIE_BUSH_KEY, Feature.TREE, (new TreeConfiguration.TreeConfigurationBuilder(BlockStateProvider.simple(ModBlocks.RIE_LOG.get()), new StraightTrunkPlacer(1, 0, 0), BlockStateProvider.simple(ModBlocks.RIE_LEAVES.get()), new BushFoliagePlacer(ConstantInt.of(2), ConstantInt.of(1), 2), new TwoLayersFeatureSize(0, 0, 0))).build());
        register(context, SPIKY_IVY_KEY, RPGFeatures.SPIKY_IVY.get(), new BlockStateConfiguration(ModBlocks.SPIKY_IVY.get().defaultBlockState()));
        register(context, TREE_HOLLOW_WEST_KEY, RPGFeatures.TREE_HOLLOW_WEST.get(), new BlockStateConfiguration(ModBlocks.RIE_HOLLOW.get().defaultBlockState()));
        register(context, TREE_HOLLOW_EAST_KEY, RPGFeatures.TREE_HOLLOW_EAST.get(), new BlockStateConfiguration(ModBlocks.RIE_HOLLOW.get().defaultBlockState()));
        register(context, TREE_HOLLOW_NORTH_KEY, RPGFeatures.TREE_HOLLOW_NORTH.get(), new BlockStateConfiguration(ModBlocks.RIE_HOLLOW.get().defaultBlockState()));
        register(context, TREE_HOLLOW_SOUTH_KEY, RPGFeatures.TREE_HOLLOW_SOUTH.get(), new BlockStateConfiguration(ModBlocks.RIE_HOLLOW.get().defaultBlockState()));
        register(context, PATCH_MOSS_RIE_KEY, Feature.RANDOM_PATCH, new RandomPatchConfiguration(32, 7, 3, PlacementUtils.filtered(Feature.SIMPLE_BLOCK, new SimpleBlockConfiguration(new WeightedStateProvider(SimpleWeightedRandomList.<BlockState>builder().add(ModBlocks.WIDOWEED.get().defaultBlockState(), 3).add(Blocks.MOSS_CARPET.defaultBlockState(), 1))), BlockPredicate.allOf(BlockPredicate.ONLY_IN_AIR_PREDICATE, (BlockPredicate.matchesBlocks(Direction.DOWN.getNormal(), Blocks.GRASS_BLOCK))))));
    }



    public static ResourceKey<ConfiguredFeature<?, ?>> registerKey(String name) {
        return ResourceKey.create(Registries.CONFIGURED_FEATURE, new ResourceLocation(RPGworldMod.MOD_ID, name));
    }

    private static <FC extends FeatureConfiguration, F extends Feature<FC>> void register(BootstapContext<ConfiguredFeature<?, ?>> context,
                                                                                          ResourceKey<ConfiguredFeature<?, ?>> key, F feature, FC configuration) {
        context.register(key, new ConfiguredFeature<>(feature, configuration));
    }
    public static final Music RIE_WEALD_MUSIC = new Music(RPGSounds.MUSIC_BIOME_RIE_WEALD.getHolder().orElseThrow(), 1200, 12000, true);
    public static final Music RIE_WEALD_MUSIC_FOG = new Music(RPGSounds.MUSIC_BIOME_RIE_WEALD_FOG.getHolder().orElseThrow(), 1200, 12000, true);

}