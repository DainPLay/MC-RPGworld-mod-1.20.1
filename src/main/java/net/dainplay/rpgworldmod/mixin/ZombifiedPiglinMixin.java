package net.dainplay.rpgworldmod.mixin;

import net.dainplay.rpgworldmod.item.ModItems;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.monster.ZombifiedPiglin;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ZombifiedPiglin.class)
public abstract class ZombifiedPiglinMixin {
	ZombifiedPiglin entity = (ZombifiedPiglin) (Object) this;

	@Inject(method = "populateDefaultEquipmentSlots", at = @At(value = "HEAD"), cancellable = true)
	private void daggerCheck(RandomSource pRandom, DifficultyInstance pDifficulty, CallbackInfo ci) {
		if (pRandom.nextFloat() < 0.05f || entity.isBaby()) {
			entity.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(ModItems.GOLDEN_DAGGER.get()));
			ci.cancel();
		}
	}
}
