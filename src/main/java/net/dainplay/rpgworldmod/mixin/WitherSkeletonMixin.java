package net.dainplay.rpgworldmod.mixin;

import net.dainplay.rpgworldmod.item.ModItems;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.monster.WitherSkeleton;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WitherSkeleton.class)
public abstract class WitherSkeletonMixin {
	WitherSkeleton entity = (WitherSkeleton) (Object) this;

	@Inject(method = "populateDefaultEquipmentSlots", at = @At(value = "HEAD"), cancellable = true)
	private void daggerCheck(RandomSource pRandom, DifficultyInstance pDifficulty, CallbackInfo ci) {
		if (pRandom.nextFloat() < 0.05f) {
			entity.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(ModItems.STONE_DAGGER.get()));
			ci.cancel();
		}
		if (pRandom.nextFloat() < 0.05f) {
			entity.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(ModItems.FLINT_DAGGER.get()));
			ci.cancel();
		}
		if (pRandom.nextFloat() < 0.05f) {
			entity.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(ModItems.FLINT_SWORD.get()));
			ci.cancel();
		}
	}
}
