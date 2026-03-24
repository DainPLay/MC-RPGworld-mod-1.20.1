package net.dainplay.rpgworldmod.network;

import net.dainplay.rpgworldmod.sounds.RPGSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.BlockPos;

public class ClientEntPositionData {
	private static float entPositionX;
	private static float entPositionY;
	private static float entPositionZ;
	private static boolean entPositionSet;

	public static void set(float x, float y, float z, boolean isSet) {
		if (!entPositionSet && isSet)
			Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forLocalAmbience(RPGSounds.ENT_ATTRACT.get(), 1.0F, 0.1F));
		ClientEntPositionData.entPositionX = x;
		ClientEntPositionData.entPositionY = y;
		ClientEntPositionData.entPositionZ = z;
		ClientEntPositionData.entPositionSet = isSet;
	}

	public static BlockPos get() {
		if (!entPositionSet) return null;
		return new BlockPos((int) entPositionX, (int) entPositionY, (int) entPositionZ);
	}

	public static float getX() {
		return ClientEntPositionData.entPositionX;
	}

	public static float getY() {
		return ClientEntPositionData.entPositionY;
	}

	public static float getZ() {
		return ClientEntPositionData.entPositionZ;
	}

	public static boolean entPositionSet() {
		return ClientEntPositionData.entPositionSet;
	}
}
