package net.dainplay.rpgworldmod.enchantment;

import net.minecraft.world.entity.*;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;

public class PitchEnchantment extends Enchantment {
    public PitchEnchantment(Rarity pRarity, EnchantmentCategory pCategory, EquipmentSlot... pApplicableSlots) {
        super(pRarity, pCategory, pApplicableSlots);
    }
    public int getMinCost(int pEnchantmentLevel) {
        return 12 + (pEnchantmentLevel - 1) * 20;
    }

    public int getMaxCost(int pEnchantmentLevel) {
        return 50;
    }

    @Override
    public int getMaxLevel() {
        return 3;
    }

    public boolean checkCompatibility(Enchantment pEnch) {
        return pEnch instanceof CrescendoEnchantment ? false : super.checkCompatibility(pEnch);
    }
}