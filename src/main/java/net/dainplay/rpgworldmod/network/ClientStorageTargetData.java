package net.dainplay.rpgworldmod.network;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.vehicle.ContainerEntity;

import java.util.HashMap;
import java.util.Map;

public class ClientStorageTargetData {
	private static Entity currentEntityTarget;
	private static BlockPos currentBlockTarget = null;

	public static void set(Entity entityTarget, BlockPos blockTarget) {
		currentEntityTarget = entityTarget;
		currentBlockTarget = blockTarget;
	}

	public static Entity getEntityTarget() {
		return currentEntityTarget;
	}

	public static BlockPos getBlockTarget() {
		return currentBlockTarget;
	}

	public static void clear() {
		currentEntityTarget = null;
		currentBlockTarget = null;
	}
}