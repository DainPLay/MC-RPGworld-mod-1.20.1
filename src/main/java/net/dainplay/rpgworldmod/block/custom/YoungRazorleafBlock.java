package net.dainplay.rpgworldmod.block.custom;

import net.dainplay.rpgworldmod.block.ModBlocks;
import net.dainplay.rpgworldmod.block.entity.custom.YoungRazorleafBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class YoungRazorleafBlock extends BushBlock implements EntityBlock {

	protected static final VoxelShape SHAPE = Shapes.or(
			Block.box(0.25D, 0.75D, 0.25D, 15.75D, 8.25D, 15.75D),
			Block.box(2.15D, 8.25D, 2.15D, 13.85D, 15.75D, 13.85D));

	public YoungRazorleafBlock(Properties props) {
		super(props);
	}

	@Override
	protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
		return state.is(ModBlocks.SPIKY_IVY.get());
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
		return SHAPE;
	}

	@Nullable
	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new YoungRazorleafBlockEntity(pos, state);
	}

	@Nullable
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
		if (level.isClientSide) return null;
		return (lvl, pos, st, be) -> {
			if (be instanceof YoungRazorleafBlockEntity young) {
				young.tick(lvl, pos, st);
			}
		};
	}

	@Override
	public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource rng) {
		if (rng.nextFloat() < 0.1f) {
			level.addParticle(ParticleTypes.LARGE_SMOKE,
					pos.getX() + 0.5F, pos.getY() + 0.5F, pos.getZ() + 0.5F,
					0, 0.05, 0);
		}
	}

}