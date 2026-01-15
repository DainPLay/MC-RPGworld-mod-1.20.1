package net.dainplay.rpgworldmod.network;

public class ClientIsManaRegenBlockedData {
	private static int isManaRegenBlocked;

	public static void set(int isManaRegenBlocked) {
		ClientIsManaRegenBlockedData.isManaRegenBlocked = isManaRegenBlocked;
	}

	public static int get() {
		return ClientIsManaRegenBlockedData.isManaRegenBlocked;
	}
}
