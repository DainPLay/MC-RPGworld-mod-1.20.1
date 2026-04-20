package net.dainplay.rpgworldmod.network;

import net.minecraft.world.phys.Vec3;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ClientVelocityStorage {
	private static final Map<Integer, Vec3> PENDING_VELOCITIES = new ConcurrentHashMap<>();

	public static void storeVelocity(int playerId, Vec3 velocity) {
		PENDING_VELOCITIES.put(playerId, velocity);
	}

	public static Vec3 retrieveVelocity(int playerId) {
		return PENDING_VELOCITIES.remove(playerId);
	}

	public static boolean hasVelocity(int playerId) {
		return PENDING_VELOCITIES.containsKey(playerId);
	}
}