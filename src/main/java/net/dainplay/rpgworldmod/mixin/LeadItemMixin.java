package net.dainplay.rpgworldmod.mixin;

import net.dainplay.rpgworldmod.entity.custom.TireSwingEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.LeadItem;
import net.minecraft.world.level.Level;
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
        InteractionResult result = TireSwingEntity.bindPlayerMobs(player, level, fencePos);
        if (result.consumesAction()) {
            cir.setReturnValue(result);
        }
    }
}