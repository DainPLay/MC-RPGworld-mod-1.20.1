package net.dainplay.rpgworldmod.block.entity.custom;

import net.dainplay.rpgworldmod.block.entity.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.logging.log4j.LogManager;

import java.awt.*;
import org.apache.logging.log4j.Logger;

import static net.dainplay.rpgworldmod.block.custom.HoltsReflectionBlock.TIMESTATE;

public class HoltsReflectionBlockEntity extends BlockEntity {
    public HoltsReflectionBlockEntity(BlockPos pWorldPosition, BlockState pBlockState) {
        super(ModBlockEntities.HOLTS_REFLECTION_BLOCK_ENTITY.get(), pWorldPosition, pBlockState);
    }
    public static void tick(Level level, BlockPos blockPos, BlockState blockState, HoltsReflectionBlockEntity holtsReflectionBlockEntity) {
        if (level.dimension() == Level.OVERWORLD) {
            float time = level.dayTime() % 24000;
            if ((time >= 23000 || time < 1000) && blockState.getValue(TIMESTATE) != Integer.valueOf(0)) {
                level.setBlock(blockPos, blockState.setValue(TIMESTATE, Integer.valueOf(0)), 3);
            } else if (time >= 1000 && time < 12000 && blockState.getValue(TIMESTATE) != Integer.valueOf(1)) {
                level.setBlock(blockPos, blockState.setValue(TIMESTATE, Integer.valueOf(1)), 3);
            } else if (time >= 12000 && time < 14000 && blockState.getValue(TIMESTATE) != Integer.valueOf(2)) {
                level.setBlock(blockPos, blockState.setValue(TIMESTATE, Integer.valueOf(2)), 3);
            } else if (time >= 14000 && time < 23000 && blockState.getValue(TIMESTATE) != Integer.valueOf(3)) {
                level.setBlock(blockPos, blockState.setValue(TIMESTATE, Integer.valueOf(3)), 3);
            }
        }
        else if (level.dimension() == Level.NETHER && blockState.getValue(TIMESTATE) != Integer.valueOf(4)) {
            level.setBlock(blockPos, blockState.setValue(TIMESTATE, Integer.valueOf(4)), 3);
        }
        else if (level.dimension() != Level.NETHER && level.dimension() != Level.OVERWORLD && blockState.getValue(TIMESTATE) != Integer.valueOf(5)) {
            level.setBlock(blockPos, blockState.setValue(TIMESTATE, Integer.valueOf(5)), 3);
        }
    }
}
