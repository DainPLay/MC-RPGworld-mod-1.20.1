package net.dainplay.rpgworldmod.item.custom;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
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

	default float get1XOffset(ItemStack stack, Entity entity) {
		return 0.15F;
	}

	default float get1YOffset(ItemStack stack, Entity entity) {
		return 0F;
	}

	default float get1ZOffset(ItemStack stack, Entity entity) {
		return -0.5F;
	}

	default float get1Size(ItemStack stack, Entity entity) {
		return 0.5F;
	}

	default float getSize(ItemStack stack, Entity entity) {
		return 0.25F;
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

	default PoseStack getUsingPose(ItemStack stack, Player player, PoseStack poseStack, float flip) {
		return poseStack;
	}

	default PoseStack getEffectUsingPose(ItemStack stack, Player player, PoseStack poseStack, float flip) {
		return poseStack;
	}
}