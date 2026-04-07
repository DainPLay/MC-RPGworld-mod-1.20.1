package net.dainplay.rpgworldmod.network;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

public class ClientSculkStaffCDData {
	private static int playerCooldown;

	public static void set(int cooldown) {
		ClientSculkStaffCDData.playerCooldown = cooldown;
		//Minecraft.getInstance().player.sendSystemMessage(Component.literal("["+Minecraft.getInstance().player.tickCount+"] Cooldown set to "+cooldown));
	}

	public static int get() {
		return ClientSculkStaffCDData.playerCooldown;
	}
}
