package net.dainplay.rpgworldmod.mixin;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.dainplay.rpgworldmod.block.custom.DrillTuskBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.piston.MovingPistonBlock;
import net.minecraft.world.level.block.piston.PistonBaseBlock;
import net.minecraft.world.level.block.piston.PistonHeadBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.PistonType;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.PushReaction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import net.minecraft.core.Direction;

import net.minecraft.world.level.block.piston.PistonStructureResolver;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Map;

@Mixin(PistonStructureResolver.class)
public class PistonStructureResolverMixin {
    PistonStructureResolver that = (PistonStructureResolver) (Object) this;
    Map<BlockPos, BlockState> map = Maps.newHashMap();
    Map<BlockPos, BlockState> map1 = Maps.newHashMap();
    @Shadow
    private Level level;
    @Shadow
    private Direction pushDirection;
    @Shadow
    private BlockPos startPos;

    @Inject(method = "addBlockLine", at = @At("HEAD"), cancellable = true)
    private void onAddBlockLine(BlockPos pOriginPos, Direction pDirection, CallbackInfoReturnable<Boolean> cir) {
        BlockState blockstate;

        int j1 = 1;

        while(true) {
            BlockPos blockpos1 = pOriginPos.relative(this.pushDirection, j1);

            blockstate = this.level.getBlockState(blockpos1);

            if (blockstate.getBlock() instanceof DrillTuskBlock && DrillTuskBlock.getConnectedDirection(blockstate) == this.pushDirection && this.level.getBlockState(blockpos1.relative(this.pushDirection)).getBlock().getExplosionResistance() < 20F) {
                map.put(blockpos1.relative(this.pushDirection), this.level.getBlockState(blockpos1.relative(this.pushDirection)));
                this.level.setBlock(blockpos1.relative(this.pushDirection), Blocks.AIR.defaultBlockState(), 18);
            }
            if(j1 > 12) break;
            ++j1;
        }
    }
    @Inject(
            method = "addBlockLine",
            at = @At("RETURN")
    )
    private void onAddBlockLineReturn(BlockPos pOriginPos, Direction pDirection, CallbackInfoReturnable<Boolean> cir) {

        for(Map.Entry<BlockPos, BlockState> entry : map.entrySet()) {
            BlockPos blockpos = entry.getKey();
            BlockState blockstate = entry.getValue();
            this.level.setBlock(blockpos, blockstate, 18);
        }
    }


    @Inject(method = "resolve", at = @At("HEAD"), cancellable = true)
    private void onResolve(CallbackInfoReturnable<Boolean> cir) {
        BlockState blockstate = this.level.getBlockState(this.startPos);
        BlockPos blockpos1 = this.startPos.relative(this.pushDirection);

            if (blockstate.getBlock() instanceof DrillTuskBlock && DrillTuskBlock.getConnectedDirection(blockstate) == this.pushDirection && this.level.getBlockState(blockpos1).getBlock().getExplosionResistance() < 20F) {
                map1.put(blockpos1, this.level.getBlockState(blockpos1));
                this.level.setBlock(blockpos1, Blocks.AIR.defaultBlockState(), 18);
            }
    }

    @Inject(method = "resolve", at = @At("RETURN"))
    private void onResolveReturn(CallbackInfoReturnable<Boolean> cir) {

        for(Map.Entry<BlockPos, BlockState> entry : map1.entrySet()) {
            BlockPos blockpos = entry.getKey();
            BlockState blockstate = entry.getValue();
            this.level.setBlock(blockpos, blockstate, 18);
        }
    }


}