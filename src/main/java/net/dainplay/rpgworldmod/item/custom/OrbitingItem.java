package net.dainplay.rpgworldmod.item.custom;

import net.minecraft.resources.ResourceLocation;

public interface OrbitingItem {


	default int getColor() {
		return -65536;
	}

	default String getTexture() {
		return null;
	}

	default int getAnimationSpeed() {
		return 1;
	}

	default int getAnimationLength() {
		return 1;
	}

	default float getX() {
		return -0.05F;
	}

	default float getY() {
		return 0.6F;
	}

	default float getZ() {
		return -0.15F;
	}

	default float getZOffset() {
		return 0.05F;
	}
}