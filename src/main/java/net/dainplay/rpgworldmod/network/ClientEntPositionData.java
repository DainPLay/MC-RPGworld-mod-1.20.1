package net.dainplay.rpgworldmod.network;

import net.dainplay.rpgworldmod.sounds.RPGSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.BlockPos;

public class ClientEntPositionData {
	private static BlockPos entPosition;

	public static void set(BlockPos newPosition) {
		if(newPosition != null && entPosition == null)
			Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forLocalAmbience(RPGSounds.ENT_ATTRACT.get(), 1.0F, 0.1F));
		ClientEntPositionData.entPosition = newPosition;
	}

	public static BlockPos get() {
		return ClientEntPositionData.entPosition;
	}
}
