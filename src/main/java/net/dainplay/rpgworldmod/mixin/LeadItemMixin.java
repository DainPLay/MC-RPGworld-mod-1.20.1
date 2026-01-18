package net.dainplay.rpgworldmod.mixin;

import net.dainplay.rpgworldmod.entity.custom.TireSwingEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.LeadItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LeadItem.class)
public class LeadItemMixin {

    @Inject(
            method = "bindPlayerMobs",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void onBindPlayerMobs(Player player, Level level, BlockPos fencePos,
                                         CallbackInfoReturnable<InteractionResult> cir) {
        // Пытаемся привязать качели к забору
        InteractionResult result = bindPlayerTireSwings(player, level, fencePos);
        if (result.consumesAction()) {
            cir.setReturnValue(result);
        }
    }

    @Inject(
            method = "bindPlayerMobs",
            at = @At("TAIL")
    )
    private static void onAfterBindPlayerMobs(Player player, Level level, BlockPos fencePos,
                                              CallbackInfoReturnable<InteractionResult> cir) {
        // Если стандартная логика не сработала, пробуем привязать качели
        if (!cir.getReturnValue().consumesAction()) {
            InteractionResult result = bindPlayerTireSwings(player, level, fencePos);
            if (result.consumesAction()) {
                cir.setReturnValue(result);
            }
        }
    }

    private static InteractionResult bindPlayerTireSwings(Player player, Level level, BlockPos fencePos) {
        boolean flag = false;

        // Ищем качели, привязанные к игроку
        for (TireSwingEntity tireSwing : level.getEntitiesOfClass(
                TireSwingEntity.class,
                new AABB((double)fencePos.getX() - 7.0D, (double)fencePos.getY() - 7.0D,
                        (double)fencePos.getZ() - 7.0D, (double)fencePos.getX() + 7.0D,
                        (double)fencePos.getY() + 7.0D, (double)fencePos.getZ() + 7.0D)
        )) {
            if (tireSwing.getLeashHolder() == player) {
                // Пытаемся привязать к забору
                if (tireSwing.leashToFence(fencePos, player)) {
                    flag = true;
                }
            }
        }

        return flag ? InteractionResult.sidedSuccess(level.isClientSide) : InteractionResult.PASS;
    }
}