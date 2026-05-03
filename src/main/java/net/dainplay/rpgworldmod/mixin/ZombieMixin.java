package net.dainplay.rpgworldmod.mixin;

import net.dainplay.rpgworldmod.item.ModItems;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Zombie.class)
public abstract class ZombieMixin {
	Zombie entity = (Zombie) (Object) this;

	@Inject(method = "populateDefaultEquipmentSlots", at = @At(value = "TAIL"), cancellable = true)
	private void daggerCheck(RandomSource pRandom, DifficultyInstance pDifficulty, CallbackInfo ci) {
		if (pRandom.nextFloat() < (entity.level().getDifficulty() == Difficulty.HARD ? 0.05F : 0.01F)) {
			entity.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(ModItems.IRON_DAGGER.get()));
		}
	}
}
