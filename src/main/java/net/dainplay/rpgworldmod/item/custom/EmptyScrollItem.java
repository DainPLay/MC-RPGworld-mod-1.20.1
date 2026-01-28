package net.dainplay.rpgworldmod.item.custom;

import net.dainplay.rpgworldmod.RPGworldClient;
import net.dainplay.rpgworldmod.RPGworldMod;
import net.dainplay.rpgworldmod.enchantment.ModEnchantments;
import net.dainplay.rpgworldmod.item.ModItems;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

public class EmptyScrollItem extends Item {
	public EmptyScrollItem(Item.Properties pProperties) {
		super(pProperties);
	}

	public boolean isEnchantable(ItemStack pStack) {
		return pStack.getCount() == 1;
	}

	public int getEnchantmentValue() {
		return 1;
	}

	public static ItemStack createForEnchantment(EnchantmentInstance pInstance) {
		ItemStack itemstack = new ItemStack(ModItems.EMPTY_SCROLL.get());
		itemstack.enchant(pInstance.enchantment, pInstance.level);
		return itemstack;
	}

	@Override
	public boolean isFoil(ItemStack stack) {
		if (stack.getEnchantmentLevel(ModEnchantments.ALTERATION.get()) > 0
				|| stack.getEnchantmentLevel(ModEnchantments.RESTORATION.get()) > 0
				|| stack.getEnchantmentLevel(ModEnchantments.DESTRUCTION.get()) > 0
				|| stack.getEnchantmentLevel(ModEnchantments.ILLUSION.get()) > 0
				|| stack.getEnchantmentLevel(ModEnchantments.CREATION.get()) > 0
				|| stack.getEnchantmentLevel(ModEnchantments.NECROMANCY.get()) > 0)
			return false;
		else return stack.isEnchanted();
	}

	@Override
	public boolean canGrindstoneRepair(ItemStack stack)
	{
		return false;
	}

}
