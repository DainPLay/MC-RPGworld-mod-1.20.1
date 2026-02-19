package net.dainplay.rpgworldmod.item.custom;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public interface ChooseAnimateTargetItem {

	default boolean highlightTarget(ItemStack stack, Player player) {
		return false;
	}

	default boolean canHighlightYourself(ItemStack stack, Player player) {
		return true;
	}
}