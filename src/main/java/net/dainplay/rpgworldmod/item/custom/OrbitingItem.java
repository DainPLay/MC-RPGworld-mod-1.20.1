package net.dainplay.rpgworldmod.item.custom;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;

public interface OrbitingItem {

	default int getColor(ItemStack stack, Entity entity) {
		return -65536;
	}

	default String getTexture(ItemStack stack, Entity entity) {
		return null;
	}

	default int getAnimationSpeed(ItemStack stack, Entity entity) {
		return 1;
	}

	default int getAnimationLength(ItemStack stack, Entity entity) {
		return 1;
	}

	default float getYOffset(ItemStack stack, Entity entity) {
		return 0F;
	}

	default float getX(ItemStack stack, Entity entity) {
		return -0.05F;
	}

	default float getY(ItemStack stack, Entity entity) {
		return 0.6F;
	}

	default float getZ(ItemStack stack, Entity entity) {
		return -0.15F;
	}

	default float getZOffset(ItemStack stack, Entity entity) {
		return 0.05F;
	}

	default boolean shouldOrbit(ItemStack stack, Entity entity) {
		return true;
	}
}