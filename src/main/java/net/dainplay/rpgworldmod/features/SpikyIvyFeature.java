package net.dainplay.rpgworldmod.features;

import com.mojang.serialization.Codec;
import net.dainplay.rpgworldmod.block.ModBlocks;
import net.dainplay.rpgworldmod.block.custom.SpikyIvyBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.BlockStateConfiguration;

import java.util.Random;

import static net.dainplay.rpgworldmod.block.custom.SpikyIvyBlock.*;

public class SpikyIvyFeature extends Feature<BlockStateConfiguration> {
    public SpikyIvyFeature(Codec<BlockStateConfiguration> p_65248_) {
        super(p_65248_);
    }

    /**
     * Places the given feature at the given location.
     * During world generation, features are provided with a 3x3 region of chunks, centered on the chunk being generated,
     * that they can safely generate into.
     * @param pContext A context object with a reference to the level and the position the feature is being placed at
     */
    public boolean place(FeaturePlaceContext<BlockStateConfiguration> p_159471_) {
        BlockPos blockpos = p_159471_.origin();
        WorldGenLevel worldgenlevel = p_159471_.level();
        RandomSource random = p_159471_.random();

        BlockStateConfiguration blockstateconfiguration;
        for(blockstateconfiguration = p_159471_.config(); blockpos.getY() > worldgenlevel.getMinBuildHeight() + 3; blockpos = blockpos.below()) {
            if (!worldgenlevel.isEmptyBlock(blockpos.below())) {
                BlockState blockstate = worldgenlevel.getBlockState(blockpos.below());
                if (isDirt(blockstate)) {
                    break;
                }
            }
        }

        if (blockpos.getY() <= worldgenlevel.getMinBuildHeight() + 3) {
            return false;
        } else {
            int placechance_east = random.nextInt(2);
            int placechance_west = random.nextInt(2);
            int placechance_south = random.nextInt(2);
            int placechance_north = random.nextInt(2);
            if(worldgenlevel.getBlockState(blockpos).getBlock() == Blocks.AIR) {

                boolean place_east = (placechance_east == 1 || worldgenlevel.getBlockState(blockpos.east()).is(BlockTags.DIRT)
                        || worldgenlevel.getBlockState(blockpos.east()).is(Blocks.FARMLAND));

                boolean place_west = (placechance_west == 1 || worldgenlevel.getBlockState(blockpos.west()).is(BlockTags.DIRT)
                        || worldgenlevel.getBlockState(blockpos.west()).is(Blocks.FARMLAND));

                boolean place_south = (placechance_south == 1 || worldgenlevel.getBlockState(blockpos.south()).is(BlockTags.DIRT)
                        || worldgenlevel.getBlockState(blockpos.south()).is(Blocks.FARMLAND));

                boolean place_north = (placechance_north == 1 || worldgenlevel.getBlockState(blockpos.north()).is(BlockTags.DIRT)
                        || worldgenlevel.getBlockState(blockpos.north()).is(Blocks.FARMLAND));

                worldgenlevel.setBlock(blockpos, ModBlocks.SPIKY_IVY.get().defaultBlockState().setValue(BlockStateProperties.AGE_3, 3)
                                .setValue(EAST_IVY, place_east ? true : false)
                                .setValue(WEST_IVY, place_west ? true : false)
                                .setValue(SOUTH_IVY, place_south ? true : false)
                                .setValue(NORTH_IVY, place_north ? true : false),
                        4);

//east

                place_east = (worldgenlevel.getBlockState(blockpos.east().east()).is(BlockTags.DIRT)
                        || worldgenlevel.getBlockState(blockpos.east().east()).is(Blocks.FARMLAND));

                place_south = (worldgenlevel.getBlockState(blockpos.east().south()).is(BlockTags.DIRT)
                        || worldgenlevel.getBlockState(blockpos.east().south()).is(Blocks.FARMLAND));

                place_north = (worldgenlevel.getBlockState(blockpos.east().north()).is(BlockTags.DIRT)
                        || worldgenlevel.getBlockState(blockpos.east().north()).is(Blocks.FARMLAND));

                boolean place_bottom = (worldgenlevel.getBlockState(blockpos.east().below()).is(BlockTags.DIRT)
                        || worldgenlevel.getBlockState(blockpos.east().below()).is(Blocks.FARMLAND));

                if (placechance_east == 1 && worldgenlevel.getBlockState(blockpos.east()).getBlock() == Blocks.AIR)
                    worldgenlevel.setBlock(blockpos.east(), ModBlocks.SPIKY_IVY.get().defaultBlockState().setValue(BlockStateProperties.AGE_3, 3)
                                    .setValue(EAST_IVY, place_east ? true : false)
                                    .setValue(WEST_IVY, true)
                                    .setValue(SOUTH_IVY, place_south ? true : false)
                                    .setValue(NORTH_IVY, place_north ? true : false)
                                    .setValue(BlockStateProperties.BOTTOM, place_bottom),
                            4);
//west

                place_west = (worldgenlevel.getBlockState(blockpos.west().west()).is(BlockTags.DIRT)
                        || worldgenlevel.getBlockState(blockpos.west().west()).is(Blocks.FARMLAND));

                place_south = (worldgenlevel.getBlockState(blockpos.west().south()).is(BlockTags.DIRT)
                        || worldgenlevel.getBlockState(blockpos.west().south()).is(Blocks.FARMLAND));

                place_north = (worldgenlevel.getBlockState(blockpos.west().north()).is(BlockTags.DIRT)
                        || worldgenlevel.getBlockState(blockpos.west().north()).is(Blocks.FARMLAND));

                place_bottom = (worldgenlevel.getBlockState(blockpos.west().below()).is(BlockTags.DIRT)
                        || worldgenlevel.getBlockState(blockpos.west().below()).is(Blocks.FARMLAND));

                if (placechance_west == 1 && worldgenlevel.getBlockState(blockpos.west()).getBlock() == Blocks.AIR)
                    worldgenlevel.setBlock(blockpos.west(), ModBlocks.SPIKY_IVY.get().defaultBlockState().setValue(BlockStateProperties.AGE_3, 3)
                                    .setValue(WEST_IVY, place_west ? true : false)
                                    .setValue(EAST_IVY, true)
                                    .setValue(SOUTH_IVY, place_south ? true : false)
                                    .setValue(NORTH_IVY, place_north ? true : false)
                                    .setValue(BlockStateProperties.BOTTOM, place_bottom),
                            4);
//south

                place_east = (worldgenlevel.getBlockState(blockpos.south().east()).is(BlockTags.DIRT)
                        || worldgenlevel.getBlockState(blockpos.south().east()).is(Blocks.FARMLAND));

                place_west = (worldgenlevel.getBlockState(blockpos.south().west()).is(BlockTags.DIRT)
                        || worldgenlevel.getBlockState(blockpos.south().west()).is(Blocks.FARMLAND));

                place_south = (worldgenlevel.getBlockState(blockpos.south().south()).is(BlockTags.DIRT)
                        || worldgenlevel.getBlockState(blockpos.south().south()).is(Blocks.FARMLAND));

                place_bottom = (worldgenlevel.getBlockState(blockpos.south().below()).is(BlockTags.DIRT)
                        || worldgenlevel.getBlockState(blockpos.south().below()).is(Blocks.FARMLAND));

                if (placechance_south == 1 && worldgenlevel.getBlockState(blockpos.south()).getBlock() == Blocks.AIR)
                    worldgenlevel.setBlock(blockpos.south(), ModBlocks.SPIKY_IVY.get().defaultBlockState().setValue(BlockStateProperties.AGE_3, 3)
                                    .setValue(SOUTH_IVY, place_south ? true : false)
                                    .setValue(NORTH_IVY, true)
                                    .setValue(WEST_IVY, place_west ? true : false)
                                    .setValue(EAST_IVY, place_east ? true : false)
                                    .setValue(BlockStateProperties.BOTTOM, place_bottom),
                            4);
//north

                place_east = (worldgenlevel.getBlockState(blockpos.north().east()).is(BlockTags.DIRT)
                        || worldgenlevel.getBlockState(blockpos.north().east()).is(Blocks.FARMLAND));

                place_west = (worldgenlevel.getBlockState(blockpos.north().west()).is(BlockTags.DIRT)
                        || worldgenlevel.getBlockState(blockpos.north().west()).is(Blocks.FARMLAND));

                place_north = (worldgenlevel.getBlockState(blockpos.north().north()).is(BlockTags.DIRT)
                        || worldgenlevel.getBlockState(blockpos.north().north()).is(Blocks.FARMLAND));

                place_bottom = (worldgenlevel.getBlockState(blockpos.north().below()).is(BlockTags.DIRT)
                        || worldgenlevel.getBlockState(blockpos.north().below()).is(Blocks.FARMLAND));

                if (placechance_north == 1 && worldgenlevel.getBlockState(blockpos.north()).getBlock() == Blocks.AIR)
                    worldgenlevel.setBlock(blockpos.north(), ModBlocks.SPIKY_IVY.get().defaultBlockState().setValue(BlockStateProperties.AGE_3, 3)
                                    .setValue(NORTH_IVY, place_north ? true : false)
                                    .setValue(SOUTH_IVY, true)
                                    .setValue(WEST_IVY, place_west ? true : false)
                                    .setValue(EAST_IVY, place_east ? true : false)
                                    .setValue(BlockStateProperties.BOTTOM, place_bottom),
                            4);
            }
            return true;
        }
    }


}