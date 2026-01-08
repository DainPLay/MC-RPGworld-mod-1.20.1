package net.dainplay.rpgworldmod.mixin;

import net.dainplay.rpgworldmod.util.FireCatcherManager;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FireBlock;
import net.minecraft.world.ticks.TickPriority;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelAccessor.class)
public interface LevelAccessorMixin {

    @Inject(method = "scheduleTick(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/Block;ILnet/minecraft/world/ticks/TickPriority;)V",
            at = @At("HEAD"), cancellable = true)
    private void onScheduleTickWithPriority(BlockPos pos, Block block, int delay,
                                            TickPriority priority, CallbackInfo ci) {
        // Проверяем, что текущий объект является ServerLevel
        if (!(this instanceof ServerLevel serverLevel)) return;

        // Проверяем только блоки FireBlock
        if (block instanceof FireBlock) {
            int chunkX = pos.getX() >> 4;
            int chunkZ = pos.getZ() >> 4;

            FireCatcherManager manager = FireCatcherManager.get(serverLevel);
            if (manager.isChunkProtected(chunkX, chunkZ, serverLevel.dimension())) {
                // Отменяем запланированные тики огня в обоих режимах
                ci.cancel();
            }
        }
    }

    @Inject(method = "scheduleTick(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/Block;I)V",
            at = @At("HEAD"), cancellable = true)
    private void onScheduleTickWithoutPriority(BlockPos pos, Block block, int delay,
                                               CallbackInfo ci) {
        // Проверяем, что текущий объект является ServerLevel
        if (!(this instanceof ServerLevel serverLevel)) return;

        if (block instanceof FireBlock) {
            int chunkX = pos.getX() >> 4;
            int chunkZ = pos.getZ() >> 4;

            FireCatcherManager manager = FireCatcherManager.get(serverLevel);
            if (manager.isChunkProtected(chunkX, chunkZ, serverLevel.dimension())) {
                // Отменяем запланированные тики огня в обоих режимах
                ci.cancel();
            }
        }
    }
}