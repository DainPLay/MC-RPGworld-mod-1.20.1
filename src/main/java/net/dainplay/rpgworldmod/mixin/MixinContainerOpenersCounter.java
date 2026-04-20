package net.dainplay.rpgworldmod.mixin;

import net.dainplay.rpgworldmod.util.RemoteOpenContainerRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.ContainerOpenersCounter;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ContainerOpenersCounter.class)
public abstract class MixinContainerOpenersCounter {
	ContainerOpenersCounter coCounter = (ContainerOpenersCounter) (Object) this;

	@Shadow
	private int openCount;

	@Shadow
	protected abstract boolean isOwnContainer(Player player);

	@Overwrite
	private int getOpenCount(Level level, BlockPos pos) {
		int remoteCount = RemoteOpenContainerRegistry.getOpenerCount(level, pos);
		if (remoteCount > 0) {
			return remoteCount;
		}

		int i = pos.getX();
		int j = pos.getY();
		int k = pos.getZ();
		AABB aabb = new AABB(
				(double) ((float) i - 5.0F), (double) ((float) j - 5.0F), (double) ((float) k - 5.0F),
				(double) ((float) (i + 1) + 5.0F), (double) ((float) (j + 1) + 5.0F), (double) ((float) (k + 1) + 5.0F)
		);
		return level.getEntities(EntityTypeTest.forClass(Player.class), aabb, this::isOwnContainer).size();
	}

}