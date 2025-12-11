package net.dainplay.rpgworldmod.item.custom;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

public abstract class ArtifactItem extends Item implements RPGtooltip {

    public ArtifactItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    public ArtifactItem() {
        this(new Properties());
    }

    @Override
    public int getMaxDamage(ItemStack stack) {
        return 0;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return Math.round(13 - stack.getDamageValue() * 13F / getMaxDamage(stack));
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return Mth.hsvToRgb(Math.max(0, (getMaxDamage(stack) - stack.getDamageValue()) / (float) getMaxDamage(stack)) / 3, 1, 1);
    }

    @Override
    public boolean canBeDepleted() {
        return getMaxDamage(ItemStack.EMPTY) > 0;
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return getMaxDamage(stack) > 0;
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
        return false;
    }
    @Override
    public boolean isBookEnchantable(ItemStack stack, ItemStack book) {
        return false;
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltip, TooltipFlag pFlag) {
        RPGappendHoverText(pStack,pLevel,pTooltip,pFlag);
    }
}
