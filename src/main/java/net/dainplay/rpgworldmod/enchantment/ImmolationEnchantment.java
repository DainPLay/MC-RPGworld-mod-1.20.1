package net.dainplay.rpgworldmod.enchantment;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;

public class ImmolationEnchantment extends Enchantment {
	public ImmolationEnchantment(Rarity pRarity, EnchantmentCategory pCategory, EquipmentSlot... pApplicableSlots) {
		super(pRarity, pCategory, pApplicableSlots);
	}

	public int getMinCost(int pEnchantmentLevel) {
		return 1;
	}

	public int getMaxCost(int pEnchantmentLevel) {
		return this.getMinCost(pEnchantmentLevel) + 40;
	}

	@Override
	public int getMaxLevel() {
		return 1;
	}

	public boolean checkCompatibility(Enchantment pEnch) {
		return super.checkCompatibility(pEnch);
	}
}