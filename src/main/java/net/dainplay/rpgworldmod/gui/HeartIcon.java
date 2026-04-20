package net.dainplay.rpgworldmod.gui;


public class HeartIcon {
	public Type heartIconType;


	public HeartIcon() {
		heartIconType = Type.NONE;
	}


	public enum Type {
		NONE,
		HALF,
		FULL
	}
}
