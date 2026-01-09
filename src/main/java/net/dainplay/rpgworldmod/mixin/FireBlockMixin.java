package net.dainplay.rpgworldmod.mixin;

import net.dainplay.rpgworldmod.util.FireCatcherManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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

        // В голодном режиме отменяем все тики огня
        if (manager.isChunkProtected(chunkX, chunkZ, level.dimension())) {
            ci.cancel();
        }
    }

    @Inject(method = "onPlace", at = @At("HEAD"))
    private void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState,
                         boolean isMoving, CallbackInfo ci) {
        if (level.isClientSide) return;

        int chunkX = pos.getX() >> 4;
        int chunkZ = pos.getZ() >> 4;

        FireCatcherManager manager = FireCatcherManager.get(level);

        // В голодном режиме не позволяем огню появляться
        if (!manager.isFireAllowedToExist(chunkX, chunkZ, level.dimension())) {
            BlockPos fireCatcherPos = manager.findNearestHungryFireCatcher(pos, level.dimension());
            level.removeBlock(pos, false);
            manager.sendFireExtinguishParticles((ServerLevel)level, fireCatcherPos, pos);
        }
    }

    @Inject(
            method = "tryCatchFire",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private void onTryCatchFire(Level level, BlockPos pos, int p_53434_, RandomSource randomSource,
                                int p_53436_, Direction face, CallbackInfo ci) {
        // Проверяем, защищён ли чанк, в который пытается распространиться огонь
        if (!level.isClientSide()) {
            int chunkX = pos.getX() >> 4;
            int chunkZ = pos.getZ() >> 4;

            FireCatcherManager manager = FireCatcherManager.get(level);

            // Отменяем распространение огня в обоих режимах
            if (!manager.isFireAllowedToSpread(chunkX, chunkZ, level.dimension())) {
                ci.cancel();
            }
        }
    }
}