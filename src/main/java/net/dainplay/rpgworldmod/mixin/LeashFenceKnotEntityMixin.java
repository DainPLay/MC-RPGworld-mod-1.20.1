package net.dainplay.rpgworldmod.mixin;

import net.dainplay.rpgworldmod.entity.custom.TireSwingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.decoration.LeashFenceKnotEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(LeashFenceKnotEntity.class)
public class LeashFenceKnotEntityMixin {

    @Inject(
            method = "interact",
            at = @At("HEAD"),
            cancellable = true
    )
    private void onInteract(Player player, InteractionHand hand, 
                           CallbackInfoReturnable<InteractionResult> cir) {
        LeashFenceKnotEntity knot = (LeashFenceKnotEntity)(Object)this;
        
        if (knot.level().isClientSide) {
            return;
        }
        
        boolean flag = false;
        double range = 7.0D;
        
        // Ищем качели, привязанные к игроку, в радиусе узла
        List<TireSwingEntity> tireSwings = knot.level().getEntitiesOfClass(
                TireSwingEntity.class,
                new AABB(knot.getX() - range, knot.getY() - range, knot.getZ() - range,
                        knot.getX() + range, knot.getY() + range, knot.getZ() + range)
        );
        
        for (TireSwingEntity tireSwing : tireSwings) {
            if (tireSwing.getLeashHolder() == player) {
                // Привязываем качели к этому узлу
                if (tireSwing.leashToExistingKnot(knot, player, true)) {
                    flag = true;
                }
            }
        }

        boolean flag1 = false;

        if (!flag) {
                for(TireSwingEntity tireSwing : tireSwings) {
                    if (tireSwing.isLeashed() && tireSwing.getLeashHolder() == knot) {
                        tireSwing.dropLeash(true, false);
                        flag1 = true;
                    }
                }
        }

        if (flag || flag1) {
            knot.gameEvent(GameEvent.BLOCK_ATTACH, player);
        }
    }
}