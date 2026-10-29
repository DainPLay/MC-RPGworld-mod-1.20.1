package net.dainplay.rpgworldmod.block.entity.custom;

import net.dainplay.rpgworldmod.block.ModBlocks;
import net.dainplay.rpgworldmod.block.custom.SpikyIvyBlock;
import net.dainplay.rpgworldmod.block.entity.ModBlockEntities;
import net.dainplay.rpgworldmod.entity.ModEntities;
import net.dainplay.rpgworldmod.entity.custom.Razorleaf;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;

public class YoungRazorleafBlockEntity extends TimedGrowthBlockEntity {

    public YoungRazorleafBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.YOUNG_RAZORLEAF_BLOCK_ENTITY.get(), pos, state);
    }

    @Override
    protected boolean canGrow(ServerLevel level, BlockPos pos, BlockState state) {
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = 0; dy <= 2; dy++) {
                for (int dz = -1; dz <= 1; dz++) {
                    if (dx == 0 && dz == 0 && dy == 0) continue;
                    BlockPos check = pos.offset(dx, dy, dz);
                    BlockState s = level.getBlockState(check);
                    if (!(s.isAir() || s.getExplosionResistance(level, check, null) < 20F)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    @Override
    protected void doGrow(ServerLevel level, BlockPos pos, BlockState state) {
        level.setBlock(pos, ModBlocks.SPIKY_IVY.get().defaultBlockState()
                .setValue(SpikyIvyBlock.AGE, 3), 2);

        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = 0; dy <= 2; dy++) {
                for (int dz = -1; dz <= 1; dz++) {
                    if (dx == 0 && dz == 0 && dy == 0) continue;
                    level.destroyBlock(pos.offset(dx, dy, dz), true);
                }
            }
        }

        Razorleaf razorleaf = new Razorleaf(ModEntities.RAZORLEAF.get(), level);
        razorleaf.setPos(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
        level.addFreshEntityWithPassengers(razorleaf);
    }
}