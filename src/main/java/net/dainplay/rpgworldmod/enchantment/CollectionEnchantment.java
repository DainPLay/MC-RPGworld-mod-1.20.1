package net.dainplay.rpgworldmod.enchantment;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.enchantment.DiggingEnchantment;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;

public class CollectionEnchantment extends Enchantment {
    public CollectionEnchantment(Rarity pRarity, EnchantmentCategory pCategory, EquipmentSlot... pApplicableSlots) {
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
        return pEnch instanceof DiggingEnchantment ? false : super.checkCompatibility(pEnch);
    }
}