package net.dainplay.rpgworldmod.network;

import net.minecraft.world.entity.player.Player;

public class ClientAdditionalHealthCostData {
	private static int healthCost;
	private static float lastYaw = 0;
	private static float lastPitch = 0;
	private static boolean hasStoredRotation = false;

	public static void set(int cost) {
		ClientAdditionalHealthCostData.healthCost = cost;
	}

	public static int get() {
		return ClientAdditionalHealthCostData.healthCost;
	}

	public static void storeRotation(Player player) {
		lastYaw = player.getYRot();
		lastPitch = player.getXRot();
		hasStoredRotation = true;
	}

	public static float calculateAngleDifference(Player player) {
		if (!hasStoredRotation) {
			return 0;
		}

		float currentYaw = player.getYRot();
		float currentPitch = player.getXRot();


		float yawDiff = Math.abs(normalizeAngle(currentYaw - lastYaw));
		float pitchDiff = Math.abs(normalizeAngle(currentPitch - lastPitch));


		float totalDiff = yawDiff + pitchDiff;


		totalDiff = Math.min(totalDiff, 90);

		return totalDiff;
	}

	public static void reset() {
		hasStoredRotation = false;
		ClientAdditionalHealthCostData.healthCost = 0;
	}

	public static boolean hasRotationStored() {
		return hasStoredRotation;
	}

	private static float normalizeAngle(float angle) {
		angle = angle % 360;
		if (angle > 180) {
			angle -= 360;
		} else if (angle < -180) {
			angle += 360;
		}
		return Math.abs(angle);
	}
}
