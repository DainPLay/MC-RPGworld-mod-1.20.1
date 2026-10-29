package net.dainplay.rpgworldmod.block.custom;

import net.dainplay.rpgworldmod.block.entity.custom.RazorleafBudBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.FlowerBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class RazorleafBudBlock extends FlowerBlock implements EntityBlock {

	protected static final VoxelShape SHAPE = Shapes.or(
			Block.box(3.0D, 0.5D, 3.0D, 13.0D, 5.5D, 13.0D),
			Block.box(5.0D, 5.5D, 5.0D, 11.0D, 10.5D, 11.0D));

	public RazorleafBudBlock(MobEffect effect, int duration, Properties props) {
		super(effect, duration, props);
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
		return SHAPE;
	}

	@Nullable
	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new RazorleafBudBlockEntity(pos, state);
	}

	@Nullable
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
		if (level.isClientSide) return null;
		return (lvl, pos, st, be) -> {
			if (be instanceof RazorleafBudBlockEntity bud) {
				bud.tick(lvl, pos, st);
			}
		};
	}

}