package net.dainplay.rpgworldmod.network;

public class ClientIllusionForceData {
	private static int illusionForce;

	public static void set(int illusionForce) {
		ClientIllusionForceData.illusionForce = illusionForce;
	}

	public static int get() {
		return ClientIllusionForceData.illusionForce;
	}
}
