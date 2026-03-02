package net.dainplay.rpgworldmod.item.custom;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public interface ChooseTargetItem {

	default boolean highlightAnimateTarget(ItemStack stack, Player player) {
		return false;
	}

	default boolean highlightSpecificItemTarget(ItemStack stack, Player player) {
		return false;
	}

	default boolean highlightItemsInRadius(ItemStack stack, Player player) {
		return false;
	}

	default boolean highlightItemsInSight(ItemStack stack, Player player) {
		return false;
	}

	default boolean highlightRandomItemInRadius(ItemStack stack, Player player) {
		return false;
	}

	default boolean highlightItemStorages(ItemStack stack, Player player) {
		return false;
	}

	default boolean canHighlightYourself(ItemStack stack, Player player) {
		return true;
	}
}