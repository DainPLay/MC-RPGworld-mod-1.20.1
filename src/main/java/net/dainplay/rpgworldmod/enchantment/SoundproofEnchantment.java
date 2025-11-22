package net.dainplay.rpgworldmod.enchantment;

import net.dainplay.rpgworldmod.data.tags.DamageTypeTagGenerator;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.enchantment.ProtectionEnchantment;
import net.minecraft.world.item.enchantment.WaterWorkerEnchantment;

public class SoundproofEnchantment extends Enchantment {
    public SoundproofEnchantment(Rarity pRarity, EnchantmentCategory pCategory, EquipmentSlot... pApplicableSlots) {
        super(pRarity, pCategory, pApplicableSlots);
    }
    public int getMinCost(int pEnchantmentLevel) {
        return 1;
    }

    public int getMaxCost(int pEnchantmentLevel) {
        return this.getMinCost(pEnchantmentLevel) + 40;
    }

    public int getDamageProtection(int pLevel, DamageSource pSource) {
        if (pSource.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            return 0;
        } else {
            return pSource.is(DamageTypeTagGenerator.IS_SOUND) ? pLevel * 12 : 0;
        }
    }

    public boolean checkCompatibility(Enchantment pEnch) {
        return pEnch instanceof WaterWorkerEnchantment ? false : super.checkCompatibility(pEnch);
    }

}