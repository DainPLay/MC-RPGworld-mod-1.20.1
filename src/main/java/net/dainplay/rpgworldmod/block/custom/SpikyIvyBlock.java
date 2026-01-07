package net.dainplay.rpgworldmod.block.custom;
import com.google.common.collect.ImmutableMap;
import net.dainplay.rpgworldmod.block.ModBlocks;
import net.dainplay.rpgworldmod.damage.ModDamageTypes;
import net.dainplay.rpgworldmod.entity.ModEntities;
import net.dainplay.rpgworldmod.entity.custom.Bramblefox;
import net.dainplay.rpgworldmod.item.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ShearsItem;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Random;

public class SpikyIvyBlock extends Block implements BonemealableBlock {
    private final Map<BlockState, VoxelShape> shapeByIndex;
    public static final BooleanProperty BOTTOM = BlockStateProperties.BOTTOM;
    public static final BooleanProperty UP = BlockStateProperties.UP;
    public static final BooleanProperty EAST_IVY = BooleanProperty.create("east");
    public static final BooleanProperty NORTH_IVY = BooleanProperty.create("north");
    public static final BooleanProperty SOUTH_IVY = BooleanProperty.create("south");
    public static final BooleanProperty WEST_IVY = BooleanProperty.create("west");
    public static final IntegerProperty AGE = BlockStateProperties.AGE_3;

    public SpikyIvyBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(AGE, 0).setValue(BOTTOM, Boolean.valueOf(true)).setValue(UP, Boolean.valueOf(false)).setValue(NORTH_IVY, false).setValue(EAST_IVY, false).setValue(SOUTH_IVY, false).setValue(WEST_IVY, false));
        this.shapeByIndex = this.makeShapes(3.0F, 3.0F, 12.0F, 4.0F, 12.0F, 12.0F);

    }
    @Override
    public boolean isRandomlyTicking(BlockState pState) {
        return pState.getValue(AGE) < 3;
    }
    @Override
    public void randomTick(BlockState pState, ServerLevel pLevel, BlockPos pPos, RandomSource pRandom) {
        Direction growthDirection = Direction.EAST;
        if (pState.getValue(AGE) < 3 && net.minecraftforge.common.ForgeHooks.onCropsGrowPre(pLevel, pPos.relative(growthDirection), pLevel.getBlockState(pPos.relative(growthDirection)),pRandom.nextDouble() < 0.05D)) {
            BlockPos blockpos = pPos.relative(growthDirection);
            if (pLevel.getBlockState(blockpos).getBlock() == Blocks.AIR) {
                pLevel.setBlockAndUpdate(blockpos, ModBlocks.SPIKY_IVY.get().defaultBlockState().setValue(AGE, pState.getValue(AGE)+1));
                net.minecraftforge.common.ForgeHooks.onCropsGrowPost(pLevel, blockpos, pLevel.getBlockState(blockpos));
            }
        }        
        growthDirection = Direction.WEST;
        if (pRandom.nextInt(2) == 1 && pState.getValue(AGE) < 3 && net.minecraftforge.common.ForgeHooks.onCropsGrowPre(pLevel, pPos.relative(growthDirection), pLevel.getBlockState(pPos.relative(growthDirection)),pRandom.nextDouble() <  0.05D)) {
            BlockPos blockpos = pPos.relative(growthDirection);
            if (pLevel.getBlockState(blockpos).getBlock() == Blocks.AIR) {
                pLevel.setBlockAndUpdate(blockpos, ModBlocks.SPIKY_IVY.get().defaultBlockState().setValue(AGE, pState.getValue(AGE)+1));
                net.minecraftforge.common.ForgeHooks.onCropsGrowPost(pLevel, blockpos, pLevel.getBlockState(blockpos));
            }
        }
        growthDirection = Direction.SOUTH;
        if (pRandom.nextInt(2) == 1 && pState.getValue(AGE) < 3 && net.minecraftforge.common.ForgeHooks.onCropsGrowPre(pLevel, pPos.relative(growthDirection), pLevel.getBlockState(pPos.relative(growthDirection)),pRandom.nextDouble() < 0.05D)) {
            BlockPos blockpos = pPos.relative(growthDirection);
            if (pLevel.getBlockState(blockpos).getBlock() == Blocks.AIR) {
                pLevel.setBlockAndUpdate(blockpos, ModBlocks.SPIKY_IVY.get().defaultBlockState().setValue(AGE, pState.getValue(AGE)+1));
                net.minecraftforge.common.ForgeHooks.onCropsGrowPost(pLevel, blockpos, pLevel.getBlockState(blockpos));
            }
        }
        growthDirection = Direction.NORTH;
        if (pRandom.nextInt(2) == 1 && pState.getValue(AGE) < 3 && net.minecraftforge.common.ForgeHooks.onCropsGrowPre(pLevel, pPos.relative(growthDirection), pLevel.getBlockState(pPos.relative(growthDirection)),pRandom.nextDouble() < 0.05D)) {
            BlockPos blockpos = pPos.relative(growthDirection);
            if (pLevel.getBlockState(blockpos).getBlock() == Blocks.AIR) {
                pLevel.setBlockAndUpdate(blockpos, ModBlocks.SPIKY_IVY.get().defaultBlockState().setValue(AGE, pState.getValue(AGE)+1));
                net.minecraftforge.common.ForgeHooks.onCropsGrowPost(pLevel, blockpos, pLevel.getBlockState(blockpos));
            }
        }

    }

    public boolean isValidBonemealTarget(LevelReader pLevel, BlockPos pPos, BlockState pState, boolean pIsClient) {
        return pLevel.getBlockState(pPos.east()).getBlock() == Blocks.AIR
                || pLevel.getBlockState(pPos.west()).getBlock() == Blocks.AIR
                || pLevel.getBlockState(pPos.north()).getBlock() == Blocks.AIR
                || pLevel.getBlockState(pPos.south()).getBlock() == Blocks.AIR;
    }

    public boolean isBonemealSuccess(Level pLevel, RandomSource pRand, BlockPos pPos, BlockState pState) {
        return true;
    }

    public void performBonemeal(ServerLevel pLevel, RandomSource pRandom, BlockPos pPos, BlockState pState) {
        pLevel.setBlockAndUpdate(pPos, ModBlocks.SPIKY_IVY.get().defaultBlockState()
                .setValue(AGE, pState.getValue(AGE) == 0 ? 0 : pState.getValue(AGE)-1)
                .setValue(EAST_IVY, pState.getValue(EAST_IVY))
                .setValue(WEST_IVY, pState.getValue(WEST_IVY))
                .setValue(NORTH_IVY, pState.getValue(NORTH_IVY))
                .setValue(SOUTH_IVY, pState.getValue(SOUTH_IVY))
                .setValue(BOTTOM, pState.getValue(BOTTOM))
                .setValue(UP, pState.getValue(UP))
        );
        net.minecraftforge.common.ForgeHooks.onCropsGrowPost(pLevel, pPos, pLevel.getBlockState(pPos));
        Direction growthDirection = Direction.EAST;
        if (pRandom.nextInt(2) == 1 && pState.getValue(AGE) < 3) {
            BlockPos blockpos = pPos.relative(growthDirection);
            if (pLevel.getBlockState(blockpos).getBlock() == Blocks.AIR) {
                pLevel.setBlockAndUpdate(blockpos, ModBlocks.SPIKY_IVY.get().defaultBlockState().setValue(AGE, pState.getValue(AGE)+1));
                net.minecraftforge.common.ForgeHooks.onCropsGrowPost(pLevel, blockpos, pLevel.getBlockState(blockpos));
            }
        }
        growthDirection = Direction.WEST;
        if (pRandom.nextInt(2) == 1 && pState.getValue(AGE) < 3) {
            BlockPos blockpos = pPos.relative(growthDirection);
            if (pLevel.getBlockState(blockpos).getBlock() == Blocks.AIR) {
                pLevel.setBlockAndUpdate(blockpos, ModBlocks.SPIKY_IVY.get().defaultBlockState().setValue(AGE, pState.getValue(AGE)+1));
                net.minecraftforge.common.ForgeHooks.onCropsGrowPost(pLevel, blockpos, pLevel.getBlockState(blockpos));
            }
        }
        growthDirection = Direction.SOUTH;
        if (pRandom.nextInt(2) == 1 && pState.getValue(AGE) < 3) {
            BlockPos blockpos = pPos.relative(growthDirection);
            if (pLevel.getBlockState(blockpos).getBlock() == Blocks.AIR) {
                pLevel.setBlockAndUpdate(blockpos, ModBlocks.SPIKY_IVY.get().defaultBlockState().setValue(AGE, pState.getValue(AGE)+1));
                net.minecraftforge.common.ForgeHooks.onCropsGrowPost(pLevel, blockpos, pLevel.getBlockState(blockpos));
            }
        }
        growthDirection = Direction.NORTH;
        if (pRandom.nextInt(2) == 1 && pState.getValue(AGE) < 3) {
            BlockPos blockpos = pPos.relative(growthDirection);
            if (pLevel.getBlockState(blockpos).getBlock() == Blocks.AIR) {
                pLevel.setBlockAndUpdate(blockpos, ModBlocks.SPIKY_IVY.get().defaultBlockState().setValue(AGE, pState.getValue(AGE)+1));
                net.minecraftforge.common.ForgeHooks.onCropsGrowPost(pLevel, blockpos, pLevel.getBlockState(blockpos));
            }
        }

    }

    @Override
    public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
        int i = pState.getValue(AGE);
        boolean flag = i == 3;
        if (!flag && pPlayer.getItemInHand(pHand).is(Items.BONE_MEAL)) {
            return InteractionResult.PASS;
        } else if (!flag && pPlayer.getItemInHand(pHand).getItem() instanceof ShearsItem) {
            pLevel.playSound((Player)null, pPos, SoundEvents.GROWING_PLANT_CROP, SoundSource.BLOCKS, 1.0F, 1.0F);
            pLevel.setBlockAndUpdate(pPos, ModBlocks.SPIKY_IVY.get().defaultBlockState().setValue(AGE, 3)
                    .setValue(EAST_IVY, pState.getValue(EAST_IVY))
                    .setValue(WEST_IVY, pState.getValue(WEST_IVY))
                    .setValue(NORTH_IVY, pState.getValue(NORTH_IVY))
                    .setValue(SOUTH_IVY, pState.getValue(SOUTH_IVY))
                    .setValue(BOTTOM, pState.getValue(BOTTOM))
                    .setValue(UP, pState.getValue(UP)));
            net.minecraftforge.common.ForgeHooks.onCropsGrowPost(pLevel, pPos, pLevel.getBlockState(pPos));
            if (pPlayer != null) {
                pPlayer.getItemInHand(pHand).hurtAndBreak(1, pPlayer, (p_186374_) -> {
                    p_186374_.broadcastBreakEvent(pHand);
                });
            }
            return InteractionResult.sidedSuccess(pLevel.isClientSide);
        } else {
            return super.use(pState, pLevel, pPos, pPlayer, pHand, pHit);
        }
    }
    private boolean connectsTo(BlockState pState) {
        return (pState.getBlock() == ModBlocks.SPIKY_IVY.get()
                || pState.is(BlockTags.DIRT)
                || pState.is(Blocks.FARMLAND)
                || pState.is(ModBlocks.YOUNG_RAZORLEAF.get()));
    }

    @Override
    public PushReaction getPistonPushReaction(BlockState pState) {
        return PushReaction.DESTROY;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        LevelReader levelreader = pContext.getLevel();
        BlockPos blockpos = pContext.getClickedPos();
        BlockPos blockpos1 = blockpos.north();
        BlockPos blockpos2 = blockpos.east();
        BlockPos blockpos3 = blockpos.south();
        BlockPos blockpos4 = blockpos.west();
        BlockPos blockpos5 = blockpos.below();
        BlockPos blockpos6 = blockpos.above();
        BlockState blockstate = levelreader.getBlockState(blockpos1);
        BlockState blockstate1 = levelreader.getBlockState(blockpos2);
        BlockState blockstate2 = levelreader.getBlockState(blockpos3);
        BlockState blockstate3 = levelreader.getBlockState(blockpos4);
        BlockState blockstate4 = levelreader.getBlockState(blockpos5);
        boolean flag = this.connectsTo(blockstate);
        boolean flag1 = this.connectsTo(blockstate1);
        boolean flag2 = this.connectsTo(blockstate2);
        boolean flag3 = this.connectsTo(blockstate3);
        BlockState blockstate5 = this.defaultBlockState();
        return this.updateShape(levelreader, blockstate5, blockpos5, blockpos6, blockstate4, flag, flag1, flag2, flag3);
    }

    @Override
    public BlockState updateShape(BlockState pState, Direction pFacing, BlockState pFacingState, LevelAccessor pLevel, BlockPos pCurrentPos, BlockPos pFacingPos) {

        return pFacing == Direction.DOWN ? this.topUpdate(pLevel, pState, pFacingPos, pCurrentPos.above(), pFacingState) : this.sideUpdate(pLevel, pCurrentPos, pState, pFacingPos, pFacingState, pFacing);

    }
    private static boolean isConnected(BlockState pState, Property<Boolean> pHeightProperty) {
        if(pState.getBlock()==ModBlocks.SPIKY_IVY.get())
            return pState.getValue(pHeightProperty) != false;
        else return false;
    }

    private BlockState topUpdate(LevelReader pLevel, BlockState p_57976_, BlockPos p_57977_, BlockPos above, BlockState p_57978_) {
        boolean flag = isConnected(p_57976_, NORTH_IVY);
        boolean flag1 = isConnected(p_57976_, EAST_IVY);
        boolean flag2 = isConnected(p_57976_, SOUTH_IVY);
        boolean flag3 = isConnected(p_57976_, WEST_IVY);
        return this.updateShape(pLevel, p_57976_, p_57977_, above, p_57978_, flag, flag1, flag2, flag3);
    }
    private Map<BlockState, VoxelShape> makeShapes(float pX1, float pX2, float pY1, float pY2, float pZ1, float pZ2) {
        float f = 8.0F - pX1;
        float f1 = 8.0F + pX1;
        float f2 = 8.0F - pX2;
        float f3 = 8.0F + pX2;
        VoxelShape voxelshape = Block.box(f, 4.0D, f, f1, 16.0D, f1);
        VoxelShape voxelshape0 = Block.box(f, 0.0D, f, f1, pZ2, f1);
        VoxelShape voxelshape1 = Block.box(f2, pY2, 0.0D, f3, pZ1, f3);
        VoxelShape voxelshape2 = Block.box(f2, pY2, f2, f3, pZ1, 16.0D);
        VoxelShape voxelshape3 = Block.box(0.0D, pY2, f2, f3, pZ1, f3);
        VoxelShape voxelshape4 = Block.box(f2, pY2, f2, 16.0D, pZ1, f3);
        VoxelShape voxelshape5 = Block.box(f2, pY2, 0.0D, f3, pZ2, f3);
        VoxelShape voxelshape6 = Block.box(f2, pY2, f2, f3, pZ2, 16.0D);
        VoxelShape voxelshape7 = Block.box(0.0D, pY2, f2, f3, pZ2, f3);
        VoxelShape voxelshape8 = Block.box(f2, pY2, f2, 16.0D, pZ2, f3);
        ImmutableMap.Builder<BlockState, VoxelShape> builder = ImmutableMap.builder();

        for (Integer age : AGE.getPossibleValues()) {
            for (Boolean bottom_connection : BOTTOM.getPossibleValues()) {
                for (Boolean up_connection : UP.getPossibleValues()) {
                for (Boolean east_connection : EAST_IVY.getPossibleValues()) {
                    for (Boolean north_connection : NORTH_IVY.getPossibleValues()) {
                        for (Boolean west_connection : WEST_IVY.getPossibleValues()) {
                            for (Boolean south_connection : SOUTH_IVY.getPossibleValues()) {
                                VoxelShape voxelshape9 = Shapes.empty();
                                voxelshape9 = applyWallShape(voxelshape9, east_connection, voxelshape4, voxelshape8);
                                voxelshape9 = applyWallShape(voxelshape9, west_connection, voxelshape3, voxelshape7);
                                voxelshape9 = applyWallShape(voxelshape9, north_connection, voxelshape1, voxelshape5);
                                voxelshape9 = applyWallShape(voxelshape9, south_connection, voxelshape2, voxelshape6);
                                if (bottom_connection) {
                                    voxelshape9 = Shapes.or(voxelshape9, voxelshape0);
                                }
                                if (up_connection) {
                                    voxelshape9 = Shapes.or(voxelshape9, voxelshape);
                                }
                                if (east_connection == false && north_connection == false
                                        && west_connection == false && south_connection == false
                                        && bottom_connection == false && up_connection == false) {
                                    voxelshape9 = Shapes.or(voxelshape9, voxelshape);
                                    voxelshape9 = Shapes.or(voxelshape9, voxelshape0);
                                    voxelshape9 = Shapes.or(voxelshape9, voxelshape1);
                                    voxelshape9 = Shapes.or(voxelshape9, voxelshape2);
                                    voxelshape9 = Shapes.or(voxelshape9, voxelshape3);
                                    voxelshape9 = Shapes.or(voxelshape9, voxelshape4);

                                }

                                BlockState blockstate = this.defaultBlockState().setValue(AGE, age).setValue(BOTTOM, bottom_connection).setValue(UP, up_connection).setValue(EAST_IVY, east_connection).setValue(WEST_IVY, west_connection).setValue(NORTH_IVY, north_connection).setValue(SOUTH_IVY, south_connection);
                                builder.put(blockstate, voxelshape9);
                            }
                        }
                    }
                }
            }
        }
    }

        return builder.build();
    }

    @Override
    @Deprecated
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        return this.shapeByIndex.get(pState);
    }

    @Override
    @Deprecated
    public VoxelShape getCollisionShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        return this.shapeByIndex.get(pState);
    }

    private static VoxelShape applyWallShape(VoxelShape pBaseShape, Boolean pHeight, VoxelShape pLowShape, VoxelShape pTallShape) {
        return pHeight == true ? Shapes.or(pBaseShape, pLowShape) : pBaseShape;
    }

    private BlockState sideUpdate(LevelReader pLevel, BlockPos p_57990_, BlockState p_57991_, BlockPos p_57992_, BlockState p_57993_, Direction p_57994_) {
        boolean flag = p_57994_ == Direction.NORTH ? this.connectsTo(p_57993_) : isConnected(p_57991_, NORTH_IVY);
        boolean flag1 = p_57994_ == Direction.EAST ? this.connectsTo(p_57993_) : isConnected(p_57991_, EAST_IVY);
        boolean flag2 = p_57994_ == Direction.SOUTH ? this.connectsTo(p_57993_) : isConnected(p_57991_, SOUTH_IVY);
        boolean flag3 = p_57994_ == Direction.WEST ? this.connectsTo(p_57993_) : isConnected(p_57991_, WEST_IVY);
        BlockPos blockpos = p_57990_.below();
        BlockPos blockpos_above = p_57990_.above();
        BlockState blockstate = pLevel.getBlockState(blockpos);
        return this.updateShape(pLevel, p_57991_, blockpos, blockpos_above, blockstate, flag, flag1, flag2, flag3);
    }

    @Override
    public float getDestroyProgress(BlockState state, Player player, BlockGetter getter, BlockPos pos) {
        return (player.getMainHandItem().getItem() instanceof ShearsItem || player.getMainHandItem().getItem() instanceof SwordItem) ? 0.9F : super.getDestroyProgress(state, player, getter, pos);
    }
    private BlockState updateShape(LevelReader pLevel, BlockState p_57981_, BlockPos p_57982_, BlockPos above, BlockState p_57983_, boolean p_57984_, boolean p_57985_, boolean p_57986_, boolean p_57987_) {
        VoxelShape voxelshape = p_57983_.getCollisionShape(pLevel, p_57982_).getFaceShape(Direction.UP);
        BlockState blockstate = this.updateSides(p_57981_, p_57984_, p_57985_, p_57986_, p_57987_, voxelshape);
        return blockstate.setValue(UP, Boolean.valueOf(this.connectsTo(pLevel.getBlockState(above)))).setValue(BOTTOM, Boolean.valueOf(this.connectsTo(pLevel.getBlockState(p_57982_))));
    }

    private BlockState updateSides(BlockState p_58025_, boolean p_58026_, boolean p_58027_, boolean p_58028_, boolean p_58029_, VoxelShape p_58030_) {
        return p_58025_.setValue(NORTH_IVY, this.makeWallState(p_58026_)).setValue(EAST_IVY, this.makeWallState(p_58027_)).setValue(SOUTH_IVY, this.makeWallState(p_58028_)).setValue(WEST_IVY, this.makeWallState(p_58029_));
    }

    private Boolean makeWallState(boolean p_58042_) {
        if (p_58042_) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean propagatesSkylightDown(BlockState pState, BlockGetter pReader, BlockPos pPos) {
        return true;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(AGE, BOTTOM, UP, NORTH_IVY, EAST_IVY, WEST_IVY, SOUTH_IVY);
    }

    @Override
    @Deprecated
    public void entityInside(BlockState pState, Level pLevel, BlockPos pPos, Entity pEntity) {
        if (!(pEntity instanceof ItemEntity)
                && !pEntity.isCrouching()
                && pEntity.getType() != ModEntities.BRAMBLEFOX.get()
                && pEntity.getType() != ModEntities.MINTOBAT.get()
                && pEntity.getType() != ModEntities.BIBBIT.get()
                && pEntity.getType() != ModEntities.BURR_PURR.get()
                && pEntity.getType() != ModEntities.DRILLHOG.get()
                && pEntity.getType() != ModEntities.MOSQUITO_SWARM.get()
                && pEntity.getType() != ModEntities.RAZORLEAF.get()) {
            if(pEntity.getBoundingBox().intersects(getAlignedBlockAABB(pLevel, pPos, pState).inflate(0.01D))) {
                pEntity.hurt(ModDamageTypes.getDamageSource(pLevel, ModDamageTypes.SPIKY_IVY), 2.5F);
                pEntity.makeStuckInBlock(pState, new Vec3(0.25D, 1.0F, 0.25D));
            }
        }
    }


    public AABB getAlignedBlockAABB(LevelAccessor world, BlockPos pos, BlockState blockState) {
        VoxelShape voxelShape = this.getCollisionShape(blockState, world, pos, CollisionContext.empty());
        AABB voxelShapeAABB = voxelShape.bounds();
        return new AABB(voxelShapeAABB.minX + pos.getX(), voxelShapeAABB.minY + pos.getY(), voxelShapeAABB.minZ + pos.getZ(),
                voxelShapeAABB.maxX + pos.getX(), voxelShapeAABB.maxY + pos.getY(), voxelShapeAABB.maxZ + pos.getZ());
    }

    @Nullable
    @Override
    public BlockPathTypes getBlockPathType(BlockState state, BlockGetter getter, BlockPos pos, @Nullable Mob entity) {
        return BlockPathTypes.DAMAGE_OTHER;
    }

    @Override
    public boolean isPathfindable(BlockState pState, BlockGetter pLevel, BlockPos pPos, PathComputationType pType) {
        return false;
    }

}
