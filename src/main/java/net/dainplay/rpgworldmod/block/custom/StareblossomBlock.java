package net.dainplay.rpgworldmod.block.custom;

import net.dainplay.rpgworldmod.block.entity.ModBlockEntities;
import net.dainplay.rpgworldmod.block.entity.custom.StareblossomBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SuspiciousEffectHolder;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;

public class StareblossomBlock extends BaseEntityBlock implements net.minecraftforge.common.IPlantable, SuspiciousEffectHolder {
	private final MobEffect suspiciousStewEffect;
	private final int effectDuration;
	public static final IntegerProperty SIGNAL = IntegerProperty.create("signal", 0, 15);

	private final java.util.function.Supplier<MobEffect> suspiciousStewEffectSupplier;

	public StareblossomBlock(java.util.function.Supplier<MobEffect> effectSupplier, int pEffectDuration, Properties properties) {
		super(properties);
		this.registerDefaultState(this.stateDefinition.any()
				.setValue(SIGNAL, 0));
		this.suspiciousStewEffect = null;
		this.suspiciousStewEffectSupplier = effectSupplier;
		this.effectDuration = pEffectDuration;

	}

	public StareblossomBlock(MobEffect pSuspiciousStewEffect, int pEffectDuration, Properties pProperties) {
		super(pProperties);
		this.suspiciousStewEffect = pSuspiciousStewEffect;
		if (pSuspiciousStewEffect.isInstantenous()) {
			this.effectDuration = pEffectDuration;
		} else {
			this.effectDuration = pEffectDuration * 20;
		}
		this.suspiciousStewEffectSupplier = net.minecraftforge.registries.ForgeRegistries.MOB_EFFECTS.getDelegateOrThrow(pSuspiciousStewEffect);

	}

	@Override
	protected void createBlockStateDefinition(net.minecraft.world.level.block.state.StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);
		builder.add(SIGNAL);
	}

	@Override
	public boolean canSurvive(BlockState pState, LevelReader pLevel, BlockPos pPos) {
		BlockPos blockpos = pPos.below();
		if (pState.getBlock() == this) //Forge: This function is called during world gen and placement, before this block is set, so if we are not 'here' then assume it's the pre-check.
			return pLevel.getBlockState(blockpos).canSustainPlant(pLevel, blockpos, Direction.UP, this);
		return this.mayPlaceOn(pLevel.getBlockState(blockpos), pLevel, blockpos);
	}

	protected boolean mayPlaceOn(BlockState pState, BlockGetter pLevel, BlockPos pPos) {
		return pState.is(BlockTags.DIRT) || pState.is(Blocks.FARMLAND);
	}

	@Override
	public boolean canSustainPlant(BlockState state, BlockGetter world, BlockPos pos, Direction facing, net.minecraftforge.common.IPlantable plantable) {
		if (plantable instanceof StareblossomBlock && ((StareblossomBlock) plantable).mayPlaceOn(state, world, pos))
			return true;

		return false;
	}

	@Nullable
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
		return level.isClientSide ? null : (level1, pos, state1, blockEntity) -> {
			if (blockEntity instanceof StareblossomBlockEntity stareblossom) {
				stareblossom.serverTick();
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
	public BlockState getPlant(BlockGetter world, BlockPos pos) {
		BlockState state = world.getBlockState(pos);
		if (state.getBlock() != this) return defaultBlockState();
		return state;
	}

	@Override
	public boolean isPathfindable(BlockState pState, BlockGetter pLevel, BlockPos pPos, PathComputationType pType) {
		return pType == PathComputationType.AIR && !this.hasCollision ? true : super.isPathfindable(pState, pLevel, pPos, pType);
	}

	@Override
	public VoxelShape getShape(BlockState pState, BlockGetter pLevel,
							   BlockPos pPos, CollisionContext pContext) {
		return Block.box(4.0D, 0.0D, 4.0D, 12.0D, 15.0D, 12.0D);
	}

	@Override
	public BlockState updateShape(BlockState pState, Direction pFacing, BlockState pFacingState, LevelAccessor pLevel, BlockPos pCurrentPos, BlockPos pFacingPos) {
		return !pState.canSurvive(pLevel, pCurrentPos) ? Blocks.AIR.defaultBlockState() : super.updateShape(pState, pFacing, pFacingState, pLevel, pCurrentPos, pFacingPos);
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
		return new StareblossomBlockEntity(pPos, pState);
	}

	@Override
	public RenderShape getRenderShape(BlockState pstate) {
		return RenderShape.MODEL;
	}

	public MobEffect getSuspiciousEffect() {
		if (true) return this.suspiciousStewEffectSupplier.get();
		return this.suspiciousStewEffect;
	}

	public int getEffectDuration() {
		if (this.suspiciousStewEffect == null && !this.suspiciousStewEffectSupplier.get().isInstantenous())
			return this.effectDuration * 20;
		return this.effectDuration;
	}
}
