package net.dainplay.rpgworldmod.network;

import net.dainplay.rpgworldmod.sounds.EmberScrollSound;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

public class EmberScrollLoopSoundPacket {
    private final int playerId;
    private final boolean start;

    public EmberScrollLoopSoundPacket(int playerId, boolean start) {
        this.playerId = playerId;
        this.start = start;
    }

    public static EmberScrollLoopSoundPacket decode(FriendlyByteBuf buf) {
        return new EmberScrollLoopSoundPacket(buf.readInt(), buf.readBoolean());
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(this.playerId);
        buf.writeBoolean(this.start);
    }

    public void handle(Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            // Только на клиенте
            if (Minecraft.getInstance().level != null) {
                Player player = (Player) Minecraft.getInstance().level.getEntity(this.playerId);
                if (player != null) {
                    if (this.start) {
                        // Запускаем зацикленный звук на клиенте
                        EmberScrollSound sound = new EmberScrollSound(player, player.getUseItem());
                        Minecraft.getInstance().getSoundManager().play(sound);
                        // Сохраняем звук для возможности остановки
                        EmberScrollSoundManager.addSound(player.getUUID(), sound);
                    } else {
                        // Останавливаем зацикленный звук на клиенте
                        EmberScrollSoundManager.stopSound(player.getUUID());
                    }
                }
            }
        });
        context.get().setPacketHandled(true);
    }

    // Менеджер для управления звуками на клиенте
    public static class EmberScrollSoundManager {
        private static final Map<UUID, EmberScrollSound> activeSounds = new HashMap<>();

        public static void addSound(UUID playerId, EmberScrollSound sound) {
            // Останавливаем предыдущий звук, если он есть
            stopSound(playerId);
            activeSounds.put(playerId, sound);
        }

        public static void stopSound(UUID playerId) {
            EmberScrollSound sound = activeSounds.get(playerId);
            if (sound != null) {
                sound.stop();
                activeSounds.remove(playerId);
            }
        }
    }
}