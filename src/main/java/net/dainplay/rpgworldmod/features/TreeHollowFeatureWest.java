package net.dainplay.rpgworldmod.features;

import com.mojang.serialization.Codec;
import net.dainplay.rpgworldmod.block.ModBlocks;
import net.dainplay.rpgworldmod.block.custom.TreeHollowBlock;
import net.dainplay.rpgworldmod.block.entity.custom.TreeHollowBlockEntity;
import net.dainplay.rpgworldmod.item.ModItems;
import net.dainplay.rpgworldmod.world.RPGLootTables;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.BlockStateConfiguration;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

import java.util.List;
import java.util.Random;

import static net.dainplay.rpgworldmod.block.custom.TreeHollowBlock.FACING;

public class TreeHollowFeatureWest extends Feature<BlockStateConfiguration> {

    public TreeHollowFeatureWest(Codec<BlockStateConfiguration> p_65248_) {
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

        if (blockpos.getY() <= worldgenlevel.getMinBuildHeight() + 3) {
            return false;
        } else {
            int placechance = p_159471_.random().nextInt(21);
            if(placechance > 0 && worldgenlevel.getBlockState(blockpos).getBlock() == ModBlocks.RIE_LOG.get() && worldgenlevel.getBlockState(blockpos.above()).getBlock() == ModBlocks.RIE_LOG.get()) {
                worldgenlevel.setBlock(blockpos.above(), ModBlocks.RIE_HOLLOW.get().defaultBlockState().setValue(FACING, Direction.WEST),4);
                Block hollowblock = worldgenlevel.getBlockState(blockpos.above()).getBlock();
                ItemStack itemstack = ItemStack.EMPTY;

                LootTable lootTable = worldgenlevel.getServer().getLootData().getLootTable(RPGLootTables.RIE_HOLLOW);
                if(lootTable != null) {
                    LootParams lootparams = (new LootParams.Builder(worldgenlevel.getLevel())).withParameter(LootContextParams.ORIGIN, blockpos.above().getCenter()).create(LootContextParamSets.CHEST);
                    List<ItemStack> loot = lootTable.getRandomItems(lootparams);
                    if (!loot.isEmpty()) {
                        itemstack = loot.get(0);
                    }
                }
                    ((TreeHollowBlock) hollowblock).setItem(worldgenlevel, blockpos.above(), worldgenlevel.getBlockState(blockpos.above()), itemstack);
                    ((TreeHollowBlockEntity) worldgenlevel.getBlockEntity(blockpos.above())).items.set(0, itemstack);
            }
            return true;
        }
    }

}