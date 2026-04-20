package net.dainplay.rpgworldmod.gui;


public class HeartsBar {
	public static HeartIcon[] calculateHeartIcons(int value) {
		HeartIcon[] icons = new HeartIcon[value / 2 + value % 2];

		int counter = value;
		for (int i = 0; i < value / 2 + value % 2; i++) {
			icons[i] = new HeartIcon();
			if (counter >= 2) {
				icons[i].heartIconType = HeartIcon.Type.FULL;
				counter -= 2;
			} else if (counter == 1) {
				icons[i].heartIconType = HeartIcon.Type.HALF;
				counter -= 1;
			} else {
				icons[i].heartIconType = HeartIcon.Type.NONE;
			}
		}

		return icons;
	}
}
