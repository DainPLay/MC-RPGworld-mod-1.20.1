package net.dainplay.rpgworldmod.network;

public class ClientMaxManaData {
	private static int playerMaxMana;

	public static void set(int maxMana) {
		ClientMaxManaData.playerMaxMana = maxMana;
	}

	public static int get() {
		return ClientMaxManaData.playerMaxMana;
	}
}
