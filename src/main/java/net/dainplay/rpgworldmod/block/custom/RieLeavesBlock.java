package net.dainplay.rpgworldmod.block.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.PushReaction;

public class RieLeavesBlock extends LeavesBlock {
	public static final BooleanProperty SPAWNED_FOR_ENT = BooleanProperty.create("spawned_for_ent");
	public static final IntegerProperty RELATED_TO_ENT = IntegerProperty.create("related_to_ent", 0, 128);

	public RieLeavesBlock(Properties pProperties) {
		super(pProperties);
		this.registerDefaultState(this.stateDefinition.any()
				.setValue(SPAWNED_FOR_ENT, false)
				.setValue(RELATED_TO_ENT, 0)
				.setValue(DISTANCE, 7)
				.setValue(PERSISTENT, false)
				.setValue(WATERLOGGED, false));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
		pBuilder.add(DISTANCE, PERSISTENT, WATERLOGGED, SPAWNED_FOR_ENT, RELATED_TO_ENT);
	}

	@Override
	public float getExplosionResistance(BlockState state, BlockGetter level, BlockPos pos, Explosion explosion) {
		if (state.getValue(RELATED_TO_ENT) != 0) {
			return 3600000.0F; // Неразрушим взрывом
		}
		return super.getExplosionResistance(state, level, pos, explosion);
	}

	@Override
	public float getDestroyProgress(BlockState state, net.minecraft.world.entity.player.Player player, BlockGetter level, BlockPos pos) {
		if (state.getValue(RELATED_TO_ENT) != 0 && !player.getAbilities().instabuild) {
			return 0.0F; // Нельзя разрушить в выживании
		}
		return super.getDestroyProgress(state, player, level, pos);
	}

	@Override
	public PushReaction getPistonPushReaction(BlockState state) {
		return state.getValue(RELATED_TO_ENT) != 0 ? PushReaction.BLOCK : super.getPistonPushReaction(state);
	}

	@Override
	public boolean isFlammable(BlockState state, BlockGetter world, BlockPos pos, Direction face) {
		return state.getValue(RELATED_TO_ENT) == 0;
	}

	@Override
	public int getFlammability(BlockState state, BlockGetter world, BlockPos pos, Direction face) {
		return state.getValue(RELATED_TO_ENT) != 0 ? 0 : 60;
	}

	@Override
	public int getFireSpreadSpeed(BlockState state, BlockGetter world, BlockPos pos, Direction face) {
		return state.getValue(RELATED_TO_ENT) != 0 ? 0 : 30;
	}

	public int isRelatedToEnt(BlockState state) {
		return state.getValue(RELATED_TO_ENT);
	}
}