package net.dainplay.rpgworldmod.mixin;

import net.dainplay.rpgworldmod.util.RemoteOpenContainerRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.ContainerOpenersCounter;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.level.entity.EntityTypeTest;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(ContainerOpenersCounter.class)
public abstract class MixinContainerOpenersCounter {

    /**
     * @author YourName
     * @reason Add support for remote container openers
     */
    @Overwrite
    private int getOpenCount(Level level, BlockPos pos) {
        // Сначала проверяем наш реестр удалённых открывателей
        int remoteCount = RemoteOpenContainerRegistry.getOpenerCount(level, pos);
        if (remoteCount > 0) {
            return remoteCount;
        }

        // Стандартная ванильная реализация (копия из исходного кода)
        int i = pos.getX();
        int j = pos.getY();
        int k = pos.getZ();
        float f = 5.0F;
        AABB aabb = new AABB(
                (double)((float)i - 5.0F), (double)((float)j - 5.0F), (double)((float)k - 5.0F),
                (double)((float)(i + 1) + 5.0F), (double)((float)(j + 1) + 5.0F), (double)((float)(k + 1) + 5.0F)
        );
        // isOwnContainer – абстрактный метод, реализованный в конкретном экземпляре
        return level.getEntities(EntityTypeTest.forClass(Player.class), aabb, this::isOwnContainer).size();
    }

    // Абстрактный метод, который нужно "прокинуть", чтобы компилятор не ругался
    protected abstract boolean isOwnContainer(Player player);
}