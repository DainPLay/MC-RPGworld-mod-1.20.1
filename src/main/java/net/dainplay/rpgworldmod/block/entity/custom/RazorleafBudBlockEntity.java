package net.dainplay.rpgworldmod.block.entity.custom;

import net.dainplay.rpgworldmod.block.ModBlocks;
import net.dainplay.rpgworldmod.block.entity.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;

public class RazorleafBudBlockEntity extends TimedGrowthBlockEntity {

    public RazorleafBudBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.RAZORLEAF_BUD_BLOCK_ENTITY.get(), pos, state);
    }

    @Override
    protected boolean canGrow(ServerLevel level, BlockPos pos, BlockState state) {
        BlockPos abovePos = pos.above();
        BlockState above = level.getBlockState(abovePos);
        return above.isAir() || above.getExplosionResistance(level, abovePos, null) < 20F;
    }

    @Override
    protected void doGrow(ServerLevel level, BlockPos pos, BlockState state) {
        BlockPos abovePos = pos.above();
        level.destroyBlock(abovePos, true);
        level.setBlock(pos, ModBlocks.SPIKY_IVY.get().defaultBlockState(), 2);
        level.setBlock(abovePos, ModBlocks.YOUNG_RAZORLEAF.get().defaultBlockState(), 2);
    }
}