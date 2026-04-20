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
		InteractionResult result = handleTireSwingLeashing(player, level, fencePos);
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
		if (!cir.getReturnValue().consumesAction()) {
			InteractionResult result = handleTireSwingLeashing(player, level, fencePos);
			if (result.consumesAction()) {
				cir.setReturnValue(result);
			}
		}
	}

	private static InteractionResult handleTireSwingLeashing(Player player, Level level, BlockPos fencePos) {
		boolean success = false;


		for (TireSwingEntity tireSwing : level.getEntitiesOfClass(
				TireSwingEntity.class,
				new AABB((double) fencePos.getX() - 7.0D, (double) fencePos.getY() - 7.0D,
						(double) fencePos.getZ() - 7.0D, (double) fencePos.getX() + 7.0D,
						(double) fencePos.getY() + 7.0D, (double) fencePos.getZ() + 7.0D)
		)) {
			if (tireSwing.getLeashHolder() == player) {
				if (tireSwing.leashToFence(fencePos, player, true)) {
					success = true;
				}
			}
		}

		return success ? InteractionResult.sidedSuccess(level.isClientSide()) : InteractionResult.PASS;
	}
}