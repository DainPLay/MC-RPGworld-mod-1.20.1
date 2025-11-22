package net.dainplay.rpgworldmod.block.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

public class DrillTuskBlock extends FaceAttachedHorizontalDirectionalBlock {


    public DrillTuskBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(FACE, AttachFace.FLOOR));
    }
    @Override
    public boolean canSurvive(BlockState pState, LevelReader pLevel, BlockPos pPos) {
        return true;
    }
    public boolean isRandomlyTicking(BlockState pState) {
        return true;
    }

    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        for(Direction direction : pContext.getNearestLookingDirections()) {
            BlockState blockstate;
            if (direction.getAxis() == Direction.Axis.Y) {
                blockstate = this.defaultBlockState().setValue(FACE, direction == Direction.UP ? AttachFace.CEILING : AttachFace.FLOOR).setValue(FACING, pContext.getHorizontalDirection());
            } else {
                blockstate = this.defaultBlockState().setValue(FACE, AttachFace.WALL).setValue(FACING, direction.getOpposite());
            }

            if (canAttach(pContext.getLevel(), pContext.getClickedPos(), getConnectedDirection(blockstate).getOpposite())) {
                return blockstate;
            }
        }

        return null;
    }
    @Override
    public void randomTick(BlockState pState, ServerLevel pLevel, BlockPos pPos, RandomSource pRandom) {
            if(!canAttach(pLevel, pPos, getConnectedDirection(pState).getOpposite())){
                pLevel.destroyBlock(pPos, true);
            }
    }

    public boolean propagatesSkylightDown(BlockState pState, BlockGetter pReader, BlockPos pPos) {
        return true;
    }

    @Override
    public PushReaction getPistonPushReaction(BlockState pState) {
        return PushReaction.NORMAL;
    }


    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        return switch (getConnectedDirection(pState)) {
            case WEST -> Block.box(1.0D, 4.0D, 4.0D, 16.0D, 12.0D, 12.0D);
            case EAST -> Block.box(0.0D, 4.0D, 4.0D, 15.0D, 12.0D, 12.0D);
            case NORTH -> Block.box(4.0D, 4.0D, 1.0D, 12.0D, 12.0D, 16.0D);
            case SOUTH -> Block.box(4.0D, 4.0D, 0.0D, 12.0D, 12.0D, 15.0D);
            case DOWN -> Block.box(4.0D, 1.0D, 4.0D, 12.0D, 16.0D, 12.0D);
            case UP -> Block.box(4.0D, 0.0D, 4.0D, 12.0D, 15.0D, 12.0D);
        };
    }

    public static @NotNull Direction getConnectedDirection(BlockState pState) {
        return switch ((AttachFace) pState.getValue(FACE)) {
            case CEILING -> Direction.DOWN;
            case FLOOR -> Direction.UP;
            default -> pState.getValue(FACING);
        };
    }
    @Override
    public RenderShape getRenderShape (BlockState pstate)
    {
        return RenderShape.MODEL;
    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(FACING, FACE);
    }
}