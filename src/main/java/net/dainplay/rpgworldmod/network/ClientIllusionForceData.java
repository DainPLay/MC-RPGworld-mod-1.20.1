package net.dainplay.rpgworldmod.network;

public class ClientIllusionForceData {
	private static int illusionForce;
	private static boolean isEnt;

	public static void set(int illusionForce, boolean isEnt) {
		ClientIllusionForceData.illusionForce = illusionForce;
		ClientIllusionForceData.isEnt = isEnt;
	}

	public static int getIllusionForce() {
		return ClientIllusionForceData.illusionForce;
	}
	public static boolean isEnt() {
		return ClientIllusionForceData.isEnt;
	}
}
