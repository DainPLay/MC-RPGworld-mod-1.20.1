package net.dainplay.rpgworldmod.network;

import net.minecraft.world.entity.LivingEntity;

import java.util.HashMap;
import java.util.Map;

public class ClientAnimateTargetData {
	private static LivingEntity currentTarget;
	private static final Map<Integer, Boolean> validationCache = new HashMap<>();

	public static void set(LivingEntity target) {
		currentTarget = target;
	}

	public static LivingEntity get() {
		return currentTarget;
	}

	public static void setValidationResult(int entityId, boolean isValid) {
		validationCache.put(entityId, isValid);
	}

	public static boolean isValidTarget(LivingEntity entity) {
		if (entity == null) return false;
		return validationCache.getOrDefault(entity.getId(), true);
	}

	public static void clearCache() {
		validationCache.clear();
	}
}