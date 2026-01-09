package net.dainplay.rpgworldmod.block.custom;

import com.google.common.collect.Maps;
import java.util.Map;

import net.dainplay.rpgworldmod.block.entity.custom.PottedStareblossomBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class StareblossomPotBlock extends FlowerPotBlock implements EntityBlock {
   public static final IntegerProperty SIGNAL = IntegerProperty.create("signal", 0, 15);

   public StareblossomPotBlock(Block pContent, Properties pProperties) {
      super(pContent, pProperties);
   }

   public RenderShape getRenderShape(BlockState pState) {
      return RenderShape.MODEL;
   }

   @Override
   public @Nullable BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
      return new PottedStareblossomBlockEntity(pPos, pState);
   }
   //Forge End

   @javax.annotation.Nullable
   @Override
   public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
      return level.isClientSide ? null : (level1, pos, state1, blockEntity) -> {
         if (blockEntity instanceof PottedStareblossomBlockEntity pottedStareblossom) {
            pottedStareblossom.serverTick();
         }
      };
   }

   @Override
   public boolean hasAnalogOutputSignal(BlockState state) {
      return true;
   }

   @Override
   public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
      return state.getValue(SIGNAL);
   }

   @Override
   protected void createBlockStateDefinition(net.minecraft.world.level.block.state.StateDefinition.Builder<Block, BlockState> builder) {
      super.createBlockStateDefinition(builder);
      builder.add(SIGNAL);
   }

   public boolean triggerEvent(BlockState pState, Level pLevel, BlockPos pPos, int pId, int pParam) {
      super.triggerEvent(pState, pLevel, pPos, pId, pParam);
      BlockEntity blockentity = pLevel.getBlockEntity(pPos);
      return blockentity == null ? false : blockentity.triggerEvent(pId, pParam);
   }

   @javax.annotation.Nullable
   public MenuProvider getMenuProvider(BlockState pState, Level pLevel, BlockPos pPos) {
      BlockEntity blockentity = pLevel.getBlockEntity(pPos);
      return blockentity instanceof MenuProvider ? (MenuProvider)blockentity : null;
   }

   @javax.annotation.Nullable
   protected static <E extends BlockEntity, A extends BlockEntity> BlockEntityTicker<A> createTickerHelper(BlockEntityType<A> pServerType, BlockEntityType<E> pClientType, BlockEntityTicker<? super E> pTicker) {
      return pClientType == pServerType ? (BlockEntityTicker<A>)pTicker : null;
   }
}
