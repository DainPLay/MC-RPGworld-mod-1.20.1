package net.dainplay.rpgworldmod.util;

import net.dainplay.rpgworldmod.entity.custom.EnderEyeViewEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;

public class ClientEyeHelper {
    public static void handleClientTick(EnderEyeViewEntity eye) {
        if (eye == ClientEyeViewHandler.getActiveEye()) {
            Player owner = Minecraft.getInstance().player;
            if (owner != null) {
                owner.zza = 0;
                owner.yya = 0;
                owner.xxa = 0;
                owner.setJumping(false);
                owner.setSprinting(false);
            }
        }
    }
}