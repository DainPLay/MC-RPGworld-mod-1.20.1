package net.dainplay.rpgworldmod.block.custom;

import net.dainplay.rpgworldmod.block.entity.ModBlockEntities;
import net.dainplay.rpgworldmod.block.entity.custom.BlowerBlockEntity;
import net.dainplay.rpgworldmod.block.entity.custom.GlossomBlockEntity;
import net.dainplay.rpgworldmod.block.entity.custom.TreeHollowBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;

public class BlowerBlock extends BaseEntityBlock implements EntityBlock {

    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    public static final IntegerProperty POWER = BlockStateProperties.POWER;
    public static final IntegerProperty RANGE = IntegerProperty.create("range", 0, 15);

    private final BlockEntityType.BlockEntitySupplier<? extends BlowerBlockEntity> blockEntitySupplier;

    public BlowerBlock(Properties properties) {
        super(properties);
        this.blockEntitySupplier = BlowerBlockEntity::new;
        registerDefaultState(defaultBlockState().setValue(FACING, Direction.NORTH).setValue(POWERED, false).setValue(POWER, 0).setValue(RANGE, 0));
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new BlowerBlockEntity(pPos, pState);
    }

    public boolean useShapeForLightOcclusion(BlockState pState) {
        return true;
    }


    public AABB getAlignedBlockAABB(LevelAccessor world, BlockPos pos, BlockState blockState) {
        VoxelShape voxelShape = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 16.0D, 16.0D);
        AABB voxelShapeAABB = voxelShape.bounds();
        return new AABB(voxelShapeAABB.minX + pos.getX(), voxelShapeAABB.minY + pos.getY(), voxelShapeAABB.minZ + pos.getZ(),
                voxelShapeAABB.maxX + pos.getX(), voxelShapeAABB.maxY + pos.getY(), voxelShapeAABB.maxZ + pos.getZ());
    }

    public void neighborChanged(BlockState pState, Level pLevel, BlockPos pPos, Block pBlock, BlockPos pFromPos, boolean pIsMoving) {
        if (!pLevel.isClientSide) {
            boolean flag = pState.getValue(POWERED);
            if (flag != pLevel.hasNeighborSignal(pPos)) {
                if (flag) {
                    pLevel.scheduleTick(pPos, this, 4);
                } else {
                    pLevel.setBlock(pPos, pState.cycle(POWERED), 2);
                }
            }

        }
    }
    @Override
    public PushReaction getPistonPushReaction(BlockState pState) {
        return PushReaction.NORMAL;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getNearestLookingDirection().getOpposite()).setValue(POWERED, Boolean.valueOf(context.getLevel().hasNeighborSignal(context.getClickedPos())));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, POWERED, POWER, RANGE);
    }

    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        return switch (pState.getValue(FACING)) {
            case EAST -> Block.box(0.0D, 0.0D, 0.0D, 6.0D, 16.0D, 16.0D);
            case WEST -> Block.box(10.0D, 0.0D, 0.0D, 16.0D, 16.0D, 16.0D);
            case NORTH -> Block.box(0.0D, 0.0D, 10.0D, 16.0D, 16.0D, 16.0D);
            case SOUTH -> Block.box(0.0D, 0.0D, 0.0D, 16.0D, 16.0D, 6.0D);
            case DOWN -> Block.box(0.0D, 10.0D, 0.0D, 16.0D, 16.0D, 16.0D);
            case UP -> Block.box(0.0D, 0.0D, 0.0D, 16.0D, 6.0D, 16.0D);
        };
    }

    @Override
    public RenderShape getRenderShape (BlockState pstate)
    {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, ModBlockEntities.BLOWER_BLOCK_ENTITY.get(), BlowerBlockEntity::tick);
    }
}