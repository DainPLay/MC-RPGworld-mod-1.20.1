package net.dainplay.rpgworldmod.item.custom;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class LongFoodItem extends Item {
    public LongFoodItem(Properties pProperties) {
        super(pProperties);
    }
    public int getUseDuration(ItemStack pStack) {
        if (pStack.getItem().isEdible()) {
            return 48;
        } else {
            return 0;
        }
    }
}
