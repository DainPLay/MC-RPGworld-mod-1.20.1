package net.dainplay.rpgworldmod.block.custom;

import net.dainplay.rpgworldmod.block.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FlowerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class RazorleafBudBlock extends FlowerBlock {
	protected static final VoxelShape SHAPE = Shapes.or(
			Block.box(3.0D, 0.5D, 3.0D, 13.0D, 5.5D, 13.0D),
			Block.box(5.0D, 5.5D, 5.0D, 11.0D, 10.5D, 11.0D));

	public RazorleafBudBlock(MobEffect effectSupplier, int pEffectDuration, Properties pProperties) {
		super(effectSupplier, pEffectDuration, pProperties);
	}

	@Override
	public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
		return SHAPE;
	}
	@Override
	public void randomTick(BlockState pState, ServerLevel pLevel, BlockPos pPos, RandomSource pRandom) {
				float f = 0.25F;
				if (pLevel.isDay() && (pLevel.getBlockState(pPos.above()).getBlock().getExplosionResistance() < 20F || pLevel.getBlockState(pPos.above()).isAir()) && net.minecraftforge.common.ForgeHooks.onCropsGrowPre(pLevel, pPos, pState, pRandom.nextInt((int)(25.0F / f) + 1) == 0)) {
					pLevel.destroyBlock(pPos.above(), true);
					pLevel.setBlock(pPos, ModBlocks.SPIKY_IVY.get().defaultBlockState(), 2);
					pLevel.setBlock(pPos.above(), ModBlocks.YOUNG_RAZORLEAF.get().defaultBlockState(), 2);
					net.minecraftforge.common.ForgeHooks.onCropsGrowPost(pLevel, pPos, pState);
				}
	}

	@Override
	public boolean isRandomlyTicking(BlockState pState) {
		return true;
	}
}
