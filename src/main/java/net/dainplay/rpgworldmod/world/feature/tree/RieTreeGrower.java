package net.dainplay.rpgworldmod.world.feature.tree;

import net.dainplay.rpgworldmod.world.feature.ModConfiguredFeatures;
import net.minecraft.core.Holder;
import net.minecraft.data.worldgen.features.TreeFeatures;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.grower.AbstractTreeGrower;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import org.jetbrains.annotations.Nullable;

import java.util.Random;

public class RieTreeGrower extends AbstractTreeGrower {
    protected RandomSource random = RandomSource.create(21);
    @Nullable
    @Override
    protected ResourceKey<ConfiguredFeature<?, ?>> getConfiguredFeature(RandomSource pRandom, boolean p_204308_) {
        if (pRandom.nextInt(10) == 0) {
            int random1 = random.nextInt(1);
            return switch (random1) {
                case 0, 1 -> ModConfiguredFeatures.BIBBIT_HOLLOW_EAST_FANCY_RIE_TREE_KEY;
                case 2, 3 -> ModConfiguredFeatures.BIBBIT_HOLLOW_WEST_FANCY_RIE_TREE_KEY;
                case 4, 5 -> ModConfiguredFeatures.BIBBIT_HOLLOW_SOUTH_FANCY_RIE_TREE_KEY;
                case 6, 7 -> ModConfiguredFeatures.BIBBIT_HOLLOW_NORTH_FANCY_RIE_TREE_KEY;
                case 8 -> ModConfiguredFeatures.HOLLOW_EAST_FANCY_RIE_TREE_KEY;
                case 9 -> ModConfiguredFeatures.HOLLOW_WEST_FANCY_RIE_TREE_KEY;
                case 10 -> ModConfiguredFeatures.HOLLOW_SOUTH_FANCY_RIE_TREE_KEY;
                case 11 -> ModConfiguredFeatures.HOLLOW_NORTH_FANCY_RIE_TREE_KEY;
                default -> ModConfiguredFeatures.FANCY_RIE_TREE_KEY;
            };
        } else if (pRandom.nextInt(10) == 0) {
            int random1 = random.nextInt(4);
            return switch (random1) {
                case 0 -> ModConfiguredFeatures.ENT_FACE_EAST_KEY;
                case 1 -> ModConfiguredFeatures.ENT_FACE_NORTH_KEY;
                case 2 -> ModConfiguredFeatures.ENT_FACE_SOUTH_KEY;
                default -> ModConfiguredFeatures.ENT_FACE_WEST_KEY;
            };
        } else {
            int random1 = random.nextInt(63);
            return switch (random1) {
                case 0, 1 -> ModConfiguredFeatures.BIBBIT_HOLLOW_EAST_RIE_TREE_KEY;
                case 2, 3 -> ModConfiguredFeatures.BIBBIT_HOLLOW_WEST_RIE_TREE_KEY;
                case 4, 5 -> ModConfiguredFeatures.BIBBIT_HOLLOW_SOUTH_RIE_TREE_KEY;
                case 6, 7 -> ModConfiguredFeatures.BIBBIT_HOLLOW_NORTH_RIE_TREE_KEY;
                case 8 -> ModConfiguredFeatures.HOLLOW_EAST_RIE_TREE_KEY;
                case 9 -> ModConfiguredFeatures.HOLLOW_WEST_RIE_TREE_KEY;
                case 10 -> ModConfiguredFeatures.HOLLOW_SOUTH_RIE_TREE_KEY;
                case 11 -> ModConfiguredFeatures.HOLLOW_NORTH_RIE_TREE_KEY;
                default -> ModConfiguredFeatures.RIE_TREE_KEY;
            };
        }
    }
}
