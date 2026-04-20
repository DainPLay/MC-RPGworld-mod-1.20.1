package net.dainplay.rpgworldmod.mixin;

import net.dainplay.rpgworldmod.network.ClientRainyChunkData;
import net.dainplay.rpgworldmod.util.RainyChunkManager;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Level.class)
public class LevelMixin {
	Level self = (Level) (Object) this;

	@Inject(method = "isRainingAt", at = @At("HEAD"), cancellable = true)
	public void onIsRainingAt(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
		boolean hasSky = self.dimensionType().hasSkyLight();
		if (hasSky && !self.canSeeSky(pos)) return;

		if (self.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, pos).getY() > pos.getY())
			return;

		int chunkX = pos.getX() >> 4;
		int chunkZ = pos.getZ() >> 4;
		long gameTime = self.getGameTime();

		if (self.isClientSide) {
			if (ClientRainyChunkData.isRainyChunk(chunkX, chunkZ, gameTime)) {
				cir.setReturnValue(true);
			}
		} else {
			RainyChunkManager manager = RainyChunkManager.get(self);
			if (manager.isRainyChunk(self.dimension(), chunkX, chunkZ, gameTime)) {
				cir.setReturnValue(true);
			}
		}
	}
}