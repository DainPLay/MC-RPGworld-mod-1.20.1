package net.dainplay.rpgworldmod.features;

import com.mojang.serialization.Codec;
import net.dainplay.rpgworldmod.block.ModBlocks;
import net.dainplay.rpgworldmod.block.custom.TreeHollowBlock;
import net.dainplay.rpgworldmod.block.entity.custom.TreeHollowBlockEntity;
import net.dainplay.rpgworldmod.world.RPGLootTables;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.BlockStateConfiguration;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

import java.util.ArrayList;
import java.util.List;

import static net.dainplay.rpgworldmod.block.custom.TreeHollowBlock.FACING;

public class TreeHollowFeatureSouth extends Feature<BlockStateConfiguration> {
    public TreeHollowFeatureSouth(Codec<BlockStateConfiguration> p_65248_) {
        super(p_65248_);
    }

    public boolean place(FeaturePlaceContext<BlockStateConfiguration> context) {
        BlockPos blockpos = context.origin();
        WorldGenLevel worldgenlevel = context.level();

        if (blockpos.getY() <= worldgenlevel.getMinBuildHeight() + 3) {
            return false;
        } else {
            int placechance = context.random().nextInt(21);
            if(placechance > 0 &&
                    worldgenlevel.getBlockState(blockpos).getBlock() == ModBlocks.RIE_LOG.get() &&
                    worldgenlevel.getBlockState(blockpos.above()).getBlock() == ModBlocks.RIE_LOG.get()) {

                worldgenlevel.setBlock(blockpos.above(),
                        ModBlocks.RIE_HOLLOW.get().defaultBlockState().setValue(FACING, Direction.SOUTH), 4);

                Block hollowblock = worldgenlevel.getBlockState(blockpos.above()).getBlock();
                ItemStack itemstack = ItemStack.EMPTY;

                LootTable lootTable = worldgenlevel.getServer().getLootData().getLootTable(RPGLootTables.RIE_HOLLOW);
                if(lootTable != null) {
                    // Используем RandomSource из контекста для потокобезопасности
                    long lootSeed = context.random().nextLong();

                    LootParams lootparams = (new LootParams.Builder(worldgenlevel.getLevel()))
                            .withParameter(LootContextParams.ORIGIN, blockpos.above().getCenter())
                            .create(LootContextParamSets.CHEST);

                    // Ключевое исправление: передаем seed в getRandomItems
                    List<ItemStack> loot = lootTable.getRandomItems(lootparams, lootSeed);

                    if (!loot.isEmpty()) {
                        itemstack = loot.get(0);
                    }
                }

                if (hollowblock instanceof TreeHollowBlock) {
                    ((TreeHollowBlock)hollowblock).setItem(worldgenlevel, blockpos.above(),
                            worldgenlevel.getBlockState(blockpos.above()), itemstack);
                }

                if (worldgenlevel.getBlockEntity(blockpos.above()) instanceof TreeHollowBlockEntity blockEntity) {
                    blockEntity.items.set(0, itemstack);
                }
            }
            return true;
        }
    }
}