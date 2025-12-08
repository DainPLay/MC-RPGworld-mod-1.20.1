package net.dainplay.rpgworldmod.enchantment;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.enchantment.KnockbackEnchantment;
import net.minecraft.world.item.enchantment.MendingEnchantment;

public class BlowingEnchantment extends Enchantment {
    public BlowingEnchantment(Rarity pRarity, EnchantmentCategory pCategory, EquipmentSlot... pApplicableSlots) {
        super(pRarity, pCategory, pApplicableSlots);
    }
    public int getMinCost(int pEnchantmentLevel) {
        return 1;
    }

    public int getMaxCost(int pEnchantmentLevel) {
        return this.getMinCost(pEnchantmentLevel) + 40;
    }

    public boolean checkCompatibility(Enchantment pEnch) {
        return !(pEnch instanceof KnockbackEnchantment) && super.checkCompatibility(pEnch);
    }
}