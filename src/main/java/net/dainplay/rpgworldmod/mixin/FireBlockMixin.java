package net.dainplay.rpgworldmod.mixin;

import net.dainplay.rpgworldmod.util.FireCatcherManager;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.FireBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FireBlock.class)
public class FireBlockMixin {

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void onTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random, CallbackInfo ci) {
        int chunkX = pos.getX() >> 4;
        int chunkZ = pos.getZ() >> 4;

        FireCatcherManager manager = FireCatcherManager.get(level);
        if (manager.isChunkProtected(chunkX, chunkZ, level.dimension())) {
            // В обычном режиме отменяем распространение огня
            if (!manager.isFireAllowed(chunkX, chunkZ, level.dimension())) {
                ci.cancel();
            }
        }
    }

    // Используем onPlace вместо onBlockAdded
    @Inject(method = "onPlace", at = @At("HEAD"))
    private void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState,
                         boolean isMoving, CallbackInfo ci) {
        if (level.isClientSide) return;

        int chunkX = pos.getX() >> 4;
        int chunkZ = pos.getZ() >> 4;

        FireCatcherManager manager = FireCatcherManager.get(level);
        if (manager.isChunkProtected(chunkX, chunkZ, level.dimension())) {
            // Если чанк защищён в обычном режиме, предотвращаем появление огня
            if (!manager.isFireAllowed(chunkX, chunkZ, level.dimension())) {
                level.removeBlock(pos, false);
            }
        }
    }
}