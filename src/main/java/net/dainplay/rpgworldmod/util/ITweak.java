package net.dainplay.rpgworldmod.util;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

public interface ITweak {
	void adjust(Entity entity);


	String getName();


	ResourceLocation getEntityLocation();
}