package net.dainplay.rpgworldmod.mixin;

import net.dainplay.rpgworldmod.entity.custom.TireSwingEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.decoration.LeashFenceKnotEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.LeadItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(LeadItem.class)
public class LeadItemMixin {
    
    @Inject(method = "useOn", at = @At("HEAD"), cancellable = true)
    private void onUseOn(UseOnContext pContext, CallbackInfoReturnable<InteractionResult> cir) {
        Level level = pContext.getLevel();
        BlockPos blockpos = pContext.getClickedPos();
        BlockState blockstate = level.getBlockState(blockpos);
        Player player = pContext.getPlayer();
        
        if (blockstate.is(BlockTags.FENCES) && player != null) {
            // Проверяем, есть ли привязанные качели у игрока
            AABB aabb = new AABB(
                blockpos.getX() - 7.0D, blockpos.getY() - 7.0D, blockpos.getZ() - 7.0D,
                blockpos.getX() + 7.0D, blockpos.getY() + 7.0D, blockpos.getZ() + 7.0D
            );
            
            List<TireSwingEntity> tireSwings = level.getEntitiesOfClass(
                TireSwingEntity.class, 
                aabb,
                tireSwing -> tireSwing.getLeashHolder() == player
            );
            
            if (!tireSwings.isEmpty()) {
                LeashFenceKnotEntity leashfenceknotentity = LeashFenceKnotEntity.getOrCreateKnot(level, blockpos);
                leashfenceknotentity.playPlacementSound();
                
                for (TireSwingEntity tireSwing : tireSwings) {
                    // Проверяем, что забор вертикально над качелями и не выше 7 блоков
                    if (canAttachToFence(tireSwing, blockpos, level)) {
                        tireSwing.setLeashedToFence(leashfenceknotentity, true);
                    }
                }
                
                cir.setReturnValue(InteractionResult.sidedSuccess(level.isClientSide()));
            }
        }
    }
    
    private boolean canAttachToFence(TireSwingEntity tireSwing, BlockPos fencePos, Level level) {
        BlockPos swingPos = tireSwing.blockPosition();
        
        // Проверяем, что забор находится вертикально над качелями
        if (fencePos.getX() != swingPos.getX() || fencePos.getZ() != swingPos.getZ()) {
            return false;
        }
        
        // Проверяем высоту (не выше 7 блоков)
        int height = fencePos.getY() - swingPos.getY();
        if (height <= 0 || height > 7) {
            return false;
        }
        
        // Проверяем, что между качелями и забором нет блоков
        for (int y = swingPos.getY() + 1; y < fencePos.getY(); y++) {
            BlockPos checkPos = new BlockPos(swingPos.getX(), y, swingPos.getZ());
            if (!level.getBlockState(checkPos).isAir()) {
                return false;
            }
        }
        
        return true;
    }
}