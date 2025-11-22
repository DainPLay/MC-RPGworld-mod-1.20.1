package net.dainplay.rpgworldmod.block.entity.custom;

import net.dainplay.rpgworldmod.block.entity.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import static net.dainplay.rpgworldmod.block.custom.GlossomBlock.GLOWING;

public class GlossomBlockEntity extends BlockEntity {
    public GlossomBlockEntity(BlockPos pWorldPosition, BlockState pBlockState) {
        super(ModBlockEntities.GLOSSOM_BLOCK_ENTITY.get(), pWorldPosition, pBlockState);
    }
    public static void tick(Level level, BlockPos blockPos, BlockState blockState, GlossomBlockEntity glossomBlockEntity) {
        if (level.dimension() == Level.OVERWORLD) {
            float time = level.dayTime() % 24000;
            if (time >= 13000 && time < 23000) {
                if (!blockState.getValue(GLOWING)) level.setBlock(blockPos, blockState.setValue(GLOWING, true),3);
            }
            else if (blockState.getValue(GLOWING)) level.setBlock(blockPos, blockState.setValue(GLOWING, false),3);

        }
        else if (!blockState.getValue(GLOWING)) level.setBlock(blockPos, blockState.setValue(GLOWING, true),3);
    }
}
