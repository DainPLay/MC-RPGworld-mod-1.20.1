package net.dainplay.rpgworldmod.enchantment;

import net.dainplay.rpgworldmod.data.tags.DamageTypeTagGenerator;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.enchantment.MendingEnchantment;
import net.minecraft.world.item.enchantment.WaterWorkerEnchantment;

public class StereoEnchantment extends Enchantment {
    public StereoEnchantment(Rarity pRarity, EnchantmentCategory pCategory, EquipmentSlot... pApplicableSlots) {
        super(pRarity, pCategory, pApplicableSlots);
    }
    public int getMinCost(int pEnchantmentLevel) {
        return 1;
    }

    public int getMaxCost(int pEnchantmentLevel) {
        return this.getMinCost(pEnchantmentLevel) + 40;
    }

    public boolean checkCompatibility(Enchantment pEnch) {
        return pEnch instanceof MendingEnchantment ? false : super.checkCompatibility(pEnch);
    }
}