package net.dainplay.rpgworldmod.item.custom;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class ExtinguishingItem extends Item {
	public ExtinguishingItem(Properties p_41580_) {
		super(p_41580_);
	}

	@Override
	public ItemStack finishUsingItem(ItemStack pStack, Level pLevel, LivingEntity pEntityLiving) {
		if (pEntityLiving.isOnFire()) {
			pEntityLiving.clearFire();
			pEntityLiving.level().playSound(null, pEntityLiving.blockPosition(), SoundEvents.GENERIC_EXTINGUISH_FIRE, SoundSource.PLAYERS, 0.7F, 1.6F + (pEntityLiving.getRandom().nextFloat() - pEntityLiving.getRandom().nextFloat()) * 0.4F);
		}
		if (pEntityLiving instanceof Player && !((Player) pEntityLiving).getAbilities().instabuild) {
			pEntityLiving.eat(pLevel, pStack);
		}

		return pStack;
	}
}
