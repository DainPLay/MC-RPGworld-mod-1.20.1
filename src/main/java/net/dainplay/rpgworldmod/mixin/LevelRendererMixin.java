package net.dainplay.rpgworldmod.mixin;

import net.dainplay.rpgworldmod.block.ModBlocks;
import net.dainplay.rpgworldmod.network.ClientRainyChunkData;
import net.dainplay.rpgworldmod.sounds.RPGSounds;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class) // Target the LevelRenderer class
public class LevelRendererMixin {

	@Shadow
	private ClientLevel level;

	@Unique
	private BlockPos currentRainCheckPos; // Последняя позиция, для которой запрашивался биом

	@Redirect(
			method = {"renderSnowAndRain", "tickRain"},
			at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientLevel;getRainLevel(F)F")
	)
	private float redirectGetRainLevel(ClientLevel level, float partialTick) {
		float original = level.getRainLevel(partialTick);
		float ours = ClientRainyChunkData.getRainLevel();
		return Math.max(original, ours);
	}

	// Сохраняем позицию при вызове getBiome (это происходит перед hasPrecipitation)
	@Redirect(
			method = "renderSnowAndRain",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;getBiome(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/core/Holder;")
	)
	private Holder<Biome> onGetBiome(Level instance, BlockPos blockPos) {
		this.currentRainCheckPos = blockPos.immutable(); // сохраняем
		return instance.getBiome(blockPos); // возвращаем оригинальное значение
	}

	// Подменяем hasPrecipitation, используя сохранённую позицию
	@Redirect(
			method = "renderSnowAndRain",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/biome/Biome;hasPrecipitation()Z")
	)
	private boolean redirectHasPrecipitation(Biome biome) {
		if (level != null && currentRainCheckPos != null && isRainyChunk(level, currentRainCheckPos)) {
			return true;
		}
		if (level != null)
			return level.isRaining() && biome.hasPrecipitation();
		else return biome.hasPrecipitation();
	}

	/**
	 * Заставляет Biome.getPrecipitationAt() возвращать RAIN для блоков в дождевых чанках.
	 */
	@Redirect(
			method = "renderSnowAndRain",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/biome/Biome;getPrecipitationAt(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/biome/Biome$Precipitation;")
	)
	private Biome.Precipitation redirectGetPrecipitationAt(Biome biome, BlockPos pos) {
		if (level != null && isRainyChunk(level, pos)) {
			return Biome.Precipitation.RAIN;
		}
		if (level != null && !level.isRaining())
			return Biome.Precipitation.NONE;
		else return biome.getPrecipitationAt(pos);
	}

	/**
	 * То же самое для метода tickRain (звуки и частицы дождя).
	 */
	@Redirect(
			method = "tickRain",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/biome/Biome;getPrecipitationAt(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/biome/Biome$Precipitation;")
	)
	private Biome.Precipitation redirectTickRainGetPrecipitationAt(Biome biome,
																   BlockPos pos) {
		if (this.level != null && isRainyChunk(this.level, pos)) {
			return Biome.Precipitation.RAIN;
		}
		if (level != null && !level.isRaining())
			return Biome.Precipitation.NONE;
		else return biome.getPrecipitationAt(pos);
	}

	/**
	 * Проверка, находится ли блок в дождевом чанке.
	 */
	private boolean isRainyChunk(ClientLevel level, BlockPos pos) {
		int chunkX = pos.getX() >> 4;
		int chunkZ = pos.getZ() >> 4;
		return ClientRainyChunkData.isRainyChunk(chunkX, chunkZ, level.getGameTime());
	}

	boolean isAnyAdjacentBlockOfType(Level level, BlockPos pos, Block blockType) {
		return level.getBlockState(pos).is(blockType)
				|| level.getBlockState(pos.south()).is(blockType)
				|| level.getBlockState(pos.north()).is(blockType)
				|| level.getBlockState(pos.east()).is(blockType)
				|| level.getBlockState(pos.west()).is(blockType)
				|| level.getBlockState(pos.above()).is(blockType)
				|| level.getBlockState(pos.below()).is(blockType);
	}

	// Helper function to check if block is water
	boolean isAnyAdjacentWater(Level level, BlockPos pos) {
		return level.getBlockState(pos).getFluidState().is(FluidTags.WATER)
				|| level.getBlockState(pos.south()).getFluidState().is(FluidTags.WATER)
				|| level.getBlockState(pos.north()).getFluidState().is(FluidTags.WATER)
				|| level.getBlockState(pos.east()).getFluidState().is(FluidTags.WATER)
				|| level.getBlockState(pos.west()).getFluidState().is(FluidTags.WATER)
				|| level.getBlockState(pos.above()).getFluidState().is(FluidTags.WATER)
				|| level.getBlockState(pos.below()).getFluidState().is(FluidTags.WATER);
	}

	// Helper function to check if block is fuel block
	boolean isAnyAdjacentFuelBlock(Level level, BlockPos pos) {
		return isAnyAdjacentBlockOfType(level, pos, ModBlocks.ARBOR_FUEL_BLOCK.get());
	}

	@Inject(method = "levelEvent(ILnet/minecraft/core/BlockPos;I)V", at = @At(value = "HEAD"), cancellable = true)
	private void onLevelEvent(int pType, BlockPos pPos, int pData, CallbackInfo ci) {
		if (pType == 1501 && isAnyAdjacentWater(level, pPos) && isAnyAdjacentFuelBlock(level, pPos)) {
			level.playLocalSound(pPos, RPGSounds.ARBOR_FUEL_EMULSION.get(), SoundSource.BLOCKS, 0.5F, 2.6F + (level.random.nextFloat() - level.random.nextFloat()) * 0.8F, false);
			ci.cancel();
			return;
		}
	}


}
