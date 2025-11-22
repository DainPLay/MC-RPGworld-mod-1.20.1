package net.dainplay.rpgworldmod.mixin;

import net.dainplay.rpgworldmod.block.ModBlocks;
import net.dainplay.rpgworldmod.sounds.RPGSounds;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class) // Target the LevelRenderer class
public class LevelRendererMixin {

    @Shadow
    private ClientLevel level;

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
        if(pType == 1501 &&  isAnyAdjacentWater(level, pPos) && isAnyAdjacentFuelBlock(level,pPos))
        {
            level.playLocalSound(pPos, RPGSounds.ARBOR_FUEL_EMULSION.get(), SoundSource.BLOCKS, 0.5F, 2.6F + (level.random.nextFloat() - level.random.nextFloat()) * 0.8F, false);
            ci.cancel();
            return;
        }
    }
}
