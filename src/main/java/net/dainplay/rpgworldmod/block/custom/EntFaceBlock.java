package net.dainplay.rpgworldmod.block.custom;

import net.dainplay.rpgworldmod.block.entity.custom.EntFaceBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class EntFaceBlock extends BaseEntityBlock {
	public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
	public static final EnumProperty<Looking> LOOKING = EnumProperty.create("looking", Looking.class);
	public static final BooleanProperty ASLEEP = BooleanProperty.create("asleep");
	public static final BooleanProperty IS_ATTACKING = BooleanProperty.create("is_attacking");
	public static final BooleanProperty DEALT_DAMAGE = BooleanProperty.create("dealt_damage");
	public static final IntegerProperty DESTROY_STAGE = IntegerProperty.create("destroy_stage", 0, 10);
	public static final IntegerProperty MELEE_PROGRESS = IntegerProperty.create("melee_progress", 0, 5);

	public EntFaceBlock(Properties p_54257_) {
		super(p_54257_);
		this.registerDefaultState(this.stateDefinition.any()
				.setValue(FACING, Direction.NORTH)
				.setValue(LOOKING, Looking.STRAIGHT)
				.setValue(ASLEEP, true)
				.setValue(IS_ATTACKING, false)
				.setValue(DEALT_DAMAGE, false)
				.setValue(MELEE_PROGRESS, 0)
				.setValue(DESTROY_STAGE, 0));
	}

	@Override
	public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
		if (!state.is(newState.getBlock())) {
			BlockEntity blockEntity = level.getBlockEntity(pos);
			if (blockEntity instanceof EntFaceBlockEntity) {
				EntFaceBlockEntity.destroyEntBlocks(level,pos);
			}
		}
		super.onRemove(state, level, pos, newState, isMoving);
	}

	@Override
	public float getExplosionResistance(BlockState state, BlockGetter level, BlockPos pos, Explosion explosion) {
		return 3600000.0F;
	}

	@Override
	public PushReaction getPistonPushReaction(BlockState state) {
		return PushReaction.BLOCK;
	}

	@Override
	public boolean canBeReplaced(BlockState state, BlockPlaceContext pUseContext) {
		return false;
	}

	public boolean isAsleep(BlockState state) {
		return state.getValue(ASLEEP);
	}

	@Override
	public void setPlacedBy(Level pLevel, BlockPos pPos, BlockState pState, @Nullable LivingEntity pPlacer, ItemStack pStack) {
		super.setPlacedBy(pLevel, pPos, pState, pPlacer, pStack);
		CompoundTag compoundtag = BlockItem.getBlockEntityData(pStack);
		if (compoundtag != null) {
			pLevel.setBlock(pPos, pState, 2);
		}

		BlockEntity blockEntity = pLevel.getBlockEntity(pPos);
		if (blockEntity instanceof EntFaceBlockEntity) {
			EntFaceBlockEntity.scanAndSaveRelatedBlocks(pLevel,pPos);
		}
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext pContext) {
		return this.defaultBlockState()
				.setValue(FACING, pContext.getHorizontalDirection().getOpposite())
				.setValue(LOOKING, Looking.STRAIGHT)
				.setValue(ASLEEP, true)
				.setValue(IS_ATTACKING, false)
				.setValue(DEALT_DAMAGE, false)
				.setValue(MELEE_PROGRESS, 0)
				.setValue(DESTROY_STAGE, 0);
	}

	@Override
	public BlockState rotate(BlockState pState, Rotation pRotation) {
		return pState.setValue(FACING, pRotation.rotate(pState.getValue(FACING)));
	}

	@Override
	public BlockState mirror(BlockState pState, Mirror pMirror) {
		return pState.rotate(pMirror.getRotation(pState.getValue(FACING)));
	}

	@Override
	public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer,
								 InteractionHand pHand, BlockHitResult pHit) {
		if (pHit.getDirection() == pState.getValue(FACING) && pState.getValue(ASLEEP)) {
			if(!pLevel.isClientSide) {
				if(pLevel.getBlockEntity(pPos) instanceof EntFaceBlockEntity be) be.wakeUp();
			}
			return InteractionResult.SUCCESS;
		}
		return InteractionResult.PASS;
	}

	@Override
	public float getDestroyProgress(BlockState state, net.minecraft.world.entity.player.Player player, BlockGetter level, BlockPos pos) {
		if (!player.getAbilities().instabuild) {
			return 0.0F;
		}
		return super.getDestroyProgress(state, player, level, pos);
	}

	@Nullable
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
																  BlockEntityType<T> type) {
		return level.isClientSide ? null : (lvl, pos, blockState, t) -> {
			if (t instanceof EntFaceBlockEntity entity) {
				entity.tick();
			}
		};
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
		return new EntFaceBlockEntity(pPos, pState);
	}

	@Override
	public boolean hasAnalogOutputSignal(BlockState pState) {
		return true;
	}

	@Override
	public int getAnalogOutputSignal(BlockState pBlockState, Level pLevel, BlockPos pPos) {
		return 15;
	}

	@Override
	public RenderShape getRenderShape(BlockState pState) {
		return RenderShape.MODEL;
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
		pBuilder.add(FACING, LOOKING, ASLEEP, IS_ATTACKING, DESTROY_STAGE, MELEE_PROGRESS, DEALT_DAMAGE);
	}

	public static int getDestroyStage(BlockState state) {
		return state.getValue(DESTROY_STAGE);
	}
}