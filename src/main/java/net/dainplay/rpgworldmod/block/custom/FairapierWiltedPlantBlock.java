package net.dainplay.rpgworldmod.block.custom;

import net.dainplay.rpgworldmod.RPGworldMod;
import net.dainplay.rpgworldmod.block.entity.ModBlockEntities;
import net.dainplay.rpgworldmod.block.entity.custom.FairapierWiltedPlantBlockEntity;
import net.dainplay.rpgworldmod.block.entity.custom.TreeHollowBlockEntity;
import net.dainplay.rpgworldmod.damage.ModDamageTypes;
import net.dainplay.rpgworldmod.data.tags.ModAdvancements;
import net.dainplay.rpgworldmod.item.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Ravager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nullable;
import java.util.Random;

public class FairapierWiltedPlantBlock extends BaseEntityBlock implements net.minecraftforge.common.IPlantable {
    public static final int MAX_AGE = 2;
    public static final IntegerProperty AGE = BlockStateProperties.AGE_2;
    public static final BooleanProperty IS_ENCHANTED = BooleanProperty.create("is_enchanted");
    private static final VoxelShape[] SHAPE_BY_AGE = new VoxelShape[]{
            Block.box(0.0D, 0.0D, 0.0D, 16.0D, 8.0D, 16.0D),
            Block.box(0.0D, 0.0D, 0.0D, 16.0D, 14.0D, 16.0D),
            Block.box(0.0D, 0.0D, 0.0D, 16.0D, 16.0D, 16.0D)};
    public FairapierWiltedPlantBlock(Properties properties) {
        super(properties);
    }
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(AGE, IS_ENCHANTED);
    }
    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        return SHAPE_BY_AGE[pState.getValue(this.getAgeProperty())];
    }
    protected boolean mayPlaceOn(BlockState pState, BlockGetter pLevel, BlockPos pPos) {
        return pState.is(Blocks.FARMLAND);
    }
    @Override
    public BlockState updateShape(BlockState pState, Direction pFacing, BlockState pFacingState, LevelAccessor pLevel, BlockPos pCurrentPos, BlockPos pFacingPos) {
        return !pState.canSurvive(pLevel, pCurrentPos) ? Blocks.AIR.defaultBlockState() : super.updateShape(pState, pFacing, pFacingState, pLevel, pCurrentPos, pFacingPos);
    }
    @Override
    public PushReaction getPistonPushReaction(BlockState pState) {
        return PushReaction.DESTROY;
    }


    @Override
    public void playerDestroy(Level world, Player player, BlockPos pos, BlockState state, @Nullable BlockEntity te, ItemStack stack) {
        super.playerDestroy(world, player, pos, state, te, stack);
        if(getAge(state) == MAX_AGE)
            ModAdvancements.FAIRAPIER_REPAIRED.trigger((ServerPlayer) player);
    }

    @Override
    public void onRemove(BlockState pState, Level pLevel, BlockPos pPos, BlockState pNewState, boolean pIsMoving) {
        if (!pState.is(pNewState.getBlock())) {
            this.dropItem(pLevel, pPos);
            super.onRemove(pState, pLevel, pPos, pNewState, pIsMoving);
        }
    }
    private void dropItem(Level pLevel, BlockPos pPos) {
        if (!pLevel.isClientSide) {
            BlockEntity blockentity = pLevel.getBlockEntity(pPos);
            if (blockentity instanceof FairapierWiltedPlantBlockEntity) {
                FairapierWiltedPlantBlockEntity FairapierWiltedPlantBlockEntity = (FairapierWiltedPlantBlockEntity)blockentity;
                ItemStack itemstack = FairapierWiltedPlantBlockEntity.getItem();
                if (!itemstack.isEmpty()) {
                    pLevel.levelEvent(1010, pPos, 0);
                    FairapierWiltedPlantBlockEntity.clearContent();
                    double d0 = (double) EntityType.ITEM.getWidth();
                    double d1 = 1.0D - d0;
                    double d2 = d0 / 2.0D;
                    double d3 = Math.floor(pPos.getX()) + RandomSource.create().nextDouble() * d1 + d2;
                    double d4 = Math.floor(pPos.getY()) + RandomSource.create().nextDouble() * d1;
                    double d5 = Math.floor(pPos.getZ()) + RandomSource.create().nextDouble() * d1 + d2;
                    ItemStack itemstack1 = itemstack.copy();
                    ItemEntity itementity = new ItemEntity(pLevel, d3, d4, d5, itemstack1);
                    itementity.setDefaultPickUpDelay();
                    pLevel.addFreshEntity(itementity);
                }
            }
        }
    }

    @Override
        public void setPlacedBy(Level pLevel, BlockPos pPos, BlockState pState, LivingEntity pPlacer, ItemStack pStack) {
        ItemStack item = ModItems.FAIRAPIER_SWORD_WILTED.get().getDefaultInstance();
        item.setTag(pStack.getTag());
        if(item.isEnchanted()) {
            pLevel.setBlock(pPos, pState.setValue(IS_ENCHANTED, true), 2);
        }
        else
        {
            pLevel.setBlock(pPos, pState.setValue(IS_ENCHANTED, false), 2);
        }
            BlockEntity blockentity = pLevel.getBlockEntity(pPos);
            if (blockentity instanceof FairapierWiltedPlantBlockEntity) {
                ((FairapierWiltedPlantBlockEntity)blockentity).setItem(item);
            }
    }
    @Override
    public boolean propagatesSkylightDown(BlockState pState, BlockGetter pReader, BlockPos pPos) {
        return pState.getFluidState().isEmpty();
    }

    @Override
    public boolean isPathfindable(BlockState pState, BlockGetter pLevel, BlockPos pPos, PathComputationType pType) {
        return pType == PathComputationType.AIR && !this.hasCollision || super.isPathfindable(pState, pLevel, pPos, pType);
    }

    @Override
    public RenderShape getRenderShape(BlockState pState) {
        return RenderShape.MODEL;
    }

    @Override
    public BlockState getPlant(BlockGetter world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        if (state.getBlock() != this) return defaultBlockState();
        return state;
    }
    public static IntegerProperty getAgeProperty() {
        return AGE;
    }

    public static BooleanProperty getIsEnchantedProperty() {
        return IS_ENCHANTED;
    }

    protected int getAge(BlockState pState) {
        return pState.getValue(this.getAgeProperty());
    }

    public BlockState getStateForAge(int pAge, boolean isEnchanted) {
        return this.defaultBlockState().setValue(this.getAgeProperty(), Integer.valueOf(pAge)).setValue(getIsEnchantedProperty(), isEnchanted);
    }
    @Override
    public boolean canSustainPlant(BlockState state, BlockGetter world, BlockPos pos, Direction facing, net.minecraftforge.common.IPlantable plantable) {
        return mayPlaceOn(state, world, pos);
    }

    protected static float getGrowthSpeed(Block pBlock, BlockGetter pLevel, BlockPos pPos) {
        float f = 1.0F;
        BlockPos blockpos = pPos.below();

        for(int i = -1; i <= 1; ++i) {
            for(int j = -1; j <= 1; ++j) {
                float f1 = 0.0F;
                BlockState blockstate = pLevel.getBlockState(blockpos.offset(i, 0, j));
                if (pLevel.getBlockState(blockpos).is(Blocks.FARMLAND)) {
                    f1 = 1.0F;
                    if (blockstate.isFertile(pLevel, pPos.offset(i, 0, j))) {
                        f1 = 3.0F;
                    }
                }

                if (i != 0 || j != 0) {
                    f1 /= 4.0F;
                }

                f += f1;
            }
        }

        BlockPos blockpos1 = pPos.north();
        BlockPos blockpos2 = pPos.south();
        BlockPos blockpos3 = pPos.west();
        BlockPos blockpos4 = pPos.east();
        boolean flag = pLevel.getBlockState(blockpos3).is(pBlock) || pLevel.getBlockState(blockpos4).is(pBlock);
        boolean flag1 = pLevel.getBlockState(blockpos1).is(pBlock) || pLevel.getBlockState(blockpos2).is(pBlock);
        if (flag && flag1) {
            f /= 2.0F;
        } else {
            boolean flag2 = pLevel.getBlockState(blockpos3.north()).is(pBlock) || pLevel.getBlockState(blockpos4.north()).is(pBlock) || pLevel.getBlockState(blockpos4.south()).is(pBlock) || pLevel.getBlockState(blockpos3.south()).is(pBlock);
            if (flag2) {
                f /= 2.0F;
            }
        }

        return f;
    }

    @Override
    public boolean canSurvive(BlockState pState, LevelReader pLevel, BlockPos pPos) {
        BlockPos blockpos = pPos.below();
        if ((pLevel.getRawBrightness(pPos, 0) >= 8 || pLevel.canSeeSky(pPos)) && super.canSurvive(pState, pLevel, pPos)) {
            return this.mayPlaceOn(pLevel.getBlockState(blockpos), pLevel, blockpos);
        }
        else return false;
    }

    @Override
    public void entityInside(BlockState pState, Level pLevel, BlockPos pPos, Entity pEntity) {
        if (pEntity instanceof Ravager && net.minecraftforge.event.ForgeEventFactory.getMobGriefingEvent(pLevel, pEntity)) {
            pLevel.destroyBlock(pPos, true, pEntity);
        }

        super.entityInside(pState, pLevel, pPos, pEntity);
    }

    public boolean isMaxAge(BlockState pState) {
        return pState.getValue(this.getAgeProperty()) >= this.getMaxAge();
    }
    @Override
    public boolean isRandomlyTicking(BlockState pState) {
        return !this.isMaxAge(pState);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new FairapierWiltedPlantBlockEntity(pPos, pState);
    }
    @Override
    public ItemStack getCloneItemStack(BlockGetter pLevel, BlockPos pPos, BlockState pState) {
        return ModItems.FAIRAPIER_SWORD_WILTED.get().getDefaultInstance();
    }
    public int getMaxAge() {
        return MAX_AGE;
    }
    @Override
    public void randomTick(BlockState pState, ServerLevel pLevel, BlockPos pPos, RandomSource pRandom) {
        if (!pLevel.isAreaLoaded(pPos, 1)) return; // Forge: prevent loading unloaded chunks when checking neighbor's light
        if (pLevel.getRawBrightness(pPos, 0) >= 9) {
            int i = this.getAge(pState);
            if (i < this.getMaxAge()) {
                float f = getGrowthSpeed(this, pLevel, pPos);
                if (net.minecraftforge.common.ForgeHooks.onCropsGrowPre(pLevel, pPos, pState, pRandom.nextInt((int)(50.0F / f) + 1) == 0)) {
                    BlockEntity blockentity = pLevel.getBlockEntity(pPos);
                    FairapierWiltedPlantBlockEntity blocken = (FairapierWiltedPlantBlockEntity)blockentity;
                    ItemStack item = blocken.getItem();
                    pLevel.setBlock(pPos, this.getStateForAge(i + 1, item.isEnchanted()), 2);
                    blockentity = pLevel.getBlockEntity(pPos);
                    blocken = (FairapierWiltedPlantBlockEntity)blockentity;
                    if (i == MAX_AGE-1) {
                        ItemStack temp_itemstack = ModItems.FAIRAPIER_SWORD.get().getDefaultInstance();
                        temp_itemstack.setTag(item.getTag());
                        item = temp_itemstack;
                        item.setDamageValue(0);
                    }
                    blocken.setItem(item);

                    net.minecraftforge.common.ForgeHooks.onCropsGrowPost(pLevel, pPos, pState);
                }
            }
        }

    }
}
