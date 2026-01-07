package net.dainplay.rpgworldmod.block.custom;

import net.dainplay.rpgworldmod.block.ModBlocks;
import net.dainplay.rpgworldmod.entity.ModEntities;
import net.dainplay.rpgworldmod.entity.custom.MosquitoSwarm;
import net.dainplay.rpgworldmod.entity.custom.Razorleaf;
import net.dainplay.rpgworldmod.particle.ModParticles;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.FlowerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.UUID;

public class YoungRazorleafBlock extends BushBlock {
	protected static final VoxelShape SHAPE = Shapes.or(
			Block.box(0.25D, 0.75D, 0.25D, 15.75D, 8.25D, 15.75D),
			Block.box(2.15D, 8.25D, 2.15D, 13.85D, 15.75D, 13.85D));

	public YoungRazorleafBlock(Properties pProperties) {
		super(pProperties);
	}

	@Override
	protected boolean mayPlaceOn(BlockState pState, BlockGetter pLevel, BlockPos pPos) {
		return pState.is(ModBlocks.SPIKY_IVY.get());
	}

	@Override
	public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
		return SHAPE;
	}
	@Override
	public void randomTick(BlockState pState, ServerLevel pLevel, BlockPos pPos, RandomSource pRandom) {
		float f = 0.25F;

		if (!pLevel.isDay()) return;

		if (!net.minecraftforge.common.ForgeHooks.onCropsGrowPre(
				pLevel, pPos, pState, pRandom.nextInt((int)(25.0F / f) + 1) == 0)) {
			return;
		}

		for (int dx = -1; dx <= 1; dx++) {
			for (int dy = 0; dy <= 2; dy++) {
				for (int dz = -1; dz <= 1; dz++) {
					if (dx == 0 && dz == 0 && dy == 0) continue;

					BlockPos checkPos = pPos.offset(dx, dy, dz);
					if (!canBeReplaced(pLevel.getBlockState(checkPos))) {
						return;
					}
				}
			}
		}

		pLevel.setBlock(pPos, ModBlocks.SPIKY_IVY.get().defaultBlockState().setValue(SpikyIvyBlock.AGE, 3), 2);

		for (int dx = -1; dx <= 1; dx++) {
			for (int dy = 0; dy <= 2; dy++) {
				for (int dz = -1; dz <= 1; dz++) {
					if (dx == 0 && dz == 0 && dy == 0) continue;

					BlockPos checkPos = pPos.offset(dx, dy, dz);
					pLevel.destroyBlock(checkPos, true);
				}
			}
		}

		// Создаем сущность
		Razorleaf razorleaf = new Razorleaf(ModEntities.RAZORLEAF.get(), pLevel);
		razorleaf.setPos(pPos.getX() + 0.5, pPos.getY(), pPos.getZ() + 0.5);
		pLevel.addFreshEntityWithPassengers(razorleaf);

		// Вызываем пост-обработку
		net.minecraftforge.common.ForgeHooks.onCropsGrowPost(pLevel, pPos, pState);
	}

	public boolean canBeReplaced(BlockState state) {
		return state.getBlock().getExplosionResistance() < 20F || state.isAir();
	}

	@Override
	public boolean isRandomlyTicking(BlockState pState) {
		return true;
	}

	@Override
	public void animateTick(BlockState pState, Level pLevel, BlockPos pPos, RandomSource pRandom) {
		if (pRandom.nextFloat() < 0.1f) {
			pLevel.addParticle(ParticleTypes.LARGE_SMOKE,
					pPos.getX() + 0.5F, pPos.getY() + 0.5F, pPos.getZ() + 0.5F,
					0, 0.05, 0);
		}
	}
}
