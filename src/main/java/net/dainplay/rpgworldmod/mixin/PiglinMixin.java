package net.dainplay.rpgworldmod.mixin;

import net.dainplay.rpgworldmod.item.ModItems;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Piglin.class)
public abstract class PiglinMixin {
	Piglin entity = (Piglin) (Object) this;

	@Inject(method = "createSpawnWeapon", at = @At(value = "HEAD"), cancellable = true)
	private void daggerCheck(CallbackInfoReturnable<ItemStack> cir) {
		if (entity.getRandom().nextFloat() < 0.05f || entity.isBaby()) {
			cir.setReturnValue(new ItemStack(ModItems.GOLDEN_DAGGER.get()));
		}
	}
}
