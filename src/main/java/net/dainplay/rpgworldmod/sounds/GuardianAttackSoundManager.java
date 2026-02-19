package net.dainplay.rpgworldmod.sounds;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;

import java.util.HashMap;
import java.util.Map;

public class GuardianAttackSoundManager {
    private static final Map<Integer, GuardianAttackSoundInstance> activeSounds = new HashMap<>();

    public static void startOrUpdate(int playerId, int attackTime, boolean hasTarget) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        Player player = (Player) mc.level.getEntity(playerId);
        if (player == null) return;

        if (!hasTarget) {
            stop(playerId);
            return;
        }

        GuardianAttackSoundInstance sound = activeSounds.get(playerId);
        if (sound == null) {
            sound = new GuardianAttackSoundInstance(player, playerId);
            mc.getSoundManager().play(sound);
            activeSounds.put(playerId, sound);
        }
        // Звук сам обновит громкость/высоту в tick()
    }

    public static void stop(int playerId) {
        GuardianAttackSoundInstance sound = activeSounds.remove(playerId);
        if (sound != null) {
            sound.stop();
        }
    }
}