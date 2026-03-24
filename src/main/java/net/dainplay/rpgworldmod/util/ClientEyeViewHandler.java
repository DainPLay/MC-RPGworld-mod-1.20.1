package net.dainplay.rpgworldmod.util;

import net.dainplay.rpgworldmod.entity.custom.EnderEyeViewEntity;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

public class ClientEyeViewHandler {
	private static EnderEyeViewEntity activeEye = null;
	private static CameraType previousCameraType = CameraType.FIRST_PERSON;

	public static void activate(EnderEyeViewEntity eye) {
		if (eye == null) return;
		activeEye = eye;
		Minecraft mc = Minecraft.getInstance();

		// Запоминаем текущий тип камеры
		previousCameraType = mc.options.getCameraType();

		mc.setCameraEntity(eye);
		// Принудительно устанавливаем вид от первого лица
		mc.options.setCameraType(CameraType.FIRST_PERSON);

		mc.player.displayClientMessage(
				Component.translatable("message.rpgworldmod.ender_eye_view",
						mc.options.keyShift.getKey().getDisplayName().getString()), true);
	}

	public static boolean isActive() {
		return activeEye != null && activeEye.isAlive();
	}

	public static EnderEyeViewEntity getActiveEye() {
		return activeEye;
	}

	public static void clear() {
		Minecraft mc = Minecraft.getInstance();
		mc.setCameraEntity(mc.player);
		mc.options.setCameraType(previousCameraType);
		activeEye = null;
	}

	public static void onClientTick(Minecraft mc) {
		if (!isActive()) {
			if (activeEye != null) {
				clear();
			}
			return;
		}

		if (!activeEye.isAlive()) {
			clear();
			return;
		}
	}
}