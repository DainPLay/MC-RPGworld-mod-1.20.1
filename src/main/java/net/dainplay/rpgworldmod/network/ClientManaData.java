package net.dainplay.rpgworldmod.network;

public class ClientManaData {
	private static int playerMana;

	public static void set(int mana) {
		ClientManaData.playerMana = mana;
	}

	public static int get() {
		return ClientManaData.playerMana;
	}
}
