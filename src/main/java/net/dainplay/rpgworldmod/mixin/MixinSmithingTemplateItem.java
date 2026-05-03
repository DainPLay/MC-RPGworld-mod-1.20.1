package net.dainplay.rpgworldmod.mixin;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.SmithingTemplateItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Mixin(SmithingTemplateItem.class)
public class MixinSmithingTemplateItem {

	@Inject(method = "createNetheriteUpgradeIconList", at = @At("RETURN"), cancellable = true)
	private static void addDaggerSlot(CallbackInfoReturnable<List<ResourceLocation>> cir) {
		List<ResourceLocation> original = cir.getReturnValue();
		List<ResourceLocation> modified = new ArrayList<>(original);
		modified.add(new ResourceLocation("item/empty_slot_dagger"));
		cir.setReturnValue(modified);
	}
}