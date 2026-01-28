package net.dainplay.rpgworldmod.enchantment;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.enchantment.MendingEnchantment;
import org.jetbrains.annotations.NotNull;

public class MagicScrollEnchantment extends Enchantment {
    public MagicScrollEnchantment(Rarity pRarity, EnchantmentCategory pCategory, EquipmentSlot... pApplicableSlots) {
        super(pRarity, pCategory, pApplicableSlots);
    }
    public int getMinCost(int pEnchantmentLevel) {
        return 1;
    }

    public int getMaxCost(int pEnchantmentLevel) {
        return this.getMinCost(pEnchantmentLevel) + 40;
    }

    public boolean checkCompatibility(Enchantment pEnch) {
        return !(pEnch instanceof MagicScrollEnchantment) && super.checkCompatibility(pEnch);
    }
    public boolean isAllowedOnBooks() {
        return false;
    }

    public boolean isTradeable() {
        return false;
    }

    @Override
    public @NotNull Component getFullname(int pLevel) {
        MutableComponent mutablecomponent = Component.translatable(this.getDescriptionId());
            mutablecomponent.withStyle(ChatFormatting.BLUE);
        return mutablecomponent;
    }
}