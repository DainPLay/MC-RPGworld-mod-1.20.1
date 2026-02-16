package net.dainplay.rpgworldmod.item.custom;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public interface RPGtooltip {

    default MutableComponent getDisplayName(ItemStack item) {
        return Component.translatable(((Item)this).getDescriptionId(item) + ".desc");
    }

    default MutableComponent getDisplayFeatures(ItemStack item) {
        return Component.translatable(((Item)this).getDescriptionId(item) + ".features",
                getFirstPredicate(item),
                getSecondPredicate(item));
    }

    default MutableComponent getDisplayControls(ItemStack item) {
        return Component.translatable(((Item)this).getDescriptionId(item) + ".controls",
                getFirstPredicate(item),
                getSecondPredicate(item));
    }

    default String getFirstPredicate(ItemStack item) {
        return "";
    }

    default String getSecondPredicate(ItemStack item) {
        return "";
    }

    default boolean hasControls(ItemStack item) {
        return false;
    }
}