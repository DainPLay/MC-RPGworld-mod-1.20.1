package net.dainplay.rpgworldmod.block.custom;

import net.dainplay.rpgworldmod.block.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.ToolAction;
import org.jetbrains.annotations.Nullable;

public class LivingWoodLogBlock extends RotatedPillarBlock {
	public static final IntegerProperty RELATED_TO_ENT = IntegerProperty.create("related_to_ent", 0, 128);

	public LivingWoodLogBlock(Properties pProperties) {
		super(pProperties);
		this.registerDefaultState(this.defaultBlockState()
				.setValue(RELATED_TO_ENT, 0)
				.setValue(AXIS, Direction.Axis.Y));
	}

	@Override
	public VoxelShape getOcclusionShape(BlockState state, BlockGetter level, BlockPos pos) {
		return Shapes.empty();
	}

	@Override
	public float getShadeBrightness(BlockState state, BlockGetter level, BlockPos pos) {
		return 1.0F;
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
		pBuilder.add(AXIS, RELATED_TO_ENT);
	}

	@Override
	public float getExplosionResistance(BlockState state, BlockGetter level, BlockPos pos, Explosion explosion) {
		if (state.getValue(RELATED_TO_ENT) != 0) {
			return 3600000.0F;
		}
		return super.getExplosionResistance(state, level, pos, explosion);
	}

	@Override
	public float getDestroyProgress(BlockState state, Player player, BlockGetter level, BlockPos pos) {
		if (state.getValue(RELATED_TO_ENT) != 0 && !player.getAbilities().instabuild) {
			return 0.0F;
		}
		return super.getDestroyProgress(state, player, level, pos);
	}

	@Override
	public PushReaction getPistonPushReaction(BlockState state) {
		return state.getValue(RELATED_TO_ENT) != 0 ? PushReaction.BLOCK : super.getPistonPushReaction(state);
	}

	@Override
	public boolean canBeReplaced(BlockState state, BlockPlaceContext pUseContext) {
		if (state.getValue(RELATED_TO_ENT) != 0) {
			return false;
		}
		return super.canBeReplaced(state, pUseContext);
	}

	@Override
	public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
								 InteractionHand hand, BlockHitResult hit) {
		if (state.getValue(RELATED_TO_ENT) != 0 && player.getItemInHand(hand).getItem() instanceof AxeItem) {
			return InteractionResult.FAIL;
		}
		return super.use(state, level, pos, player, hand, hit);
	}

	@Nullable
	@Override
	public BlockState getToolModifiedState(BlockState state, UseOnContext context, ToolAction toolAction, boolean simulate) {
		if (context.getItemInHand().getItem() instanceof AxeItem) {
			if (state.is(ModBlocks.LIVING_WOOD_LOG.get())) {
				return ModBlocks.STRIPPED_LIVING_WOOD_LOG.get().defaultBlockState().setValue(AXIS, state.getValue(AXIS));
			}
		}

		return super.getToolModifiedState(state, context, toolAction, simulate);
	}

	public int isRelatedToEnt(BlockState state) {
		return state.getValue(RELATED_TO_ENT);
	}
}