package net.dainplay.rpgworldmod.network;

public class ClientSculkStaffCDData {
	private static int playerCooldown;

	public static void set(int cooldown) {
		ClientSculkStaffCDData.playerCooldown = cooldown;

	}

	public static int get() {
		return ClientSculkStaffCDData.playerCooldown;
	}
}
