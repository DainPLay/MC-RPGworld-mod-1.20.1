package net.dainplay.rpgworldmod.mixin;

import net.dainplay.rpgworldmod.item.custom.StaffItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemCooldowns;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemCooldowns.class)
public abstract class ItemCooldownsMixin {
	@Inject(method = "isOnCooldown", at = @At(value = "HEAD"), cancellable = true)
	private void StaffCheck(Item pItem, CallbackInfoReturnable<Boolean> cir) {
		if (pItem instanceof StaffItem)
			cir.setReturnValue(false);
	}

}
