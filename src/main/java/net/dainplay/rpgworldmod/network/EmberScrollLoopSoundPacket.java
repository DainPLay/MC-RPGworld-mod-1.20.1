package net.dainplay.rpgworldmod.network;

import net.dainplay.rpgworldmod.enchantment.ModEnchantments;
import net.dainplay.rpgworldmod.sounds.EmberScrollSound;
import net.dainplay.rpgworldmod.sounds.RPGSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

public class EmberScrollLoopSoundPacket {
    private final int playerId;
    private final boolean start;
    private final ItemStack itemStack;

    public EmberScrollLoopSoundPacket(int playerId, boolean start, ItemStack itemStack) {
        this.playerId = playerId;
        this.start = start;
        this.itemStack = itemStack;
    }

    public static EmberScrollLoopSoundPacket decode(FriendlyByteBuf buf) {
        return new EmberScrollLoopSoundPacket(buf.readInt(), buf.readBoolean(), buf.readItem());
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(this.playerId);
        buf.writeBoolean(this.start);
        buf.writeItem(this.itemStack);
    }

    public void handle(Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            // Только на клиенте
            if (Minecraft.getInstance().level != null) {
                Player player = (Player) Minecraft.getInstance().level.getEntity(this.playerId);
                if (player != null) {
                    if (this.start) {
                        // Запускаем зацикленный звук на клиенте
                        SoundEvent soundEvent = RPGSounds.SPELL_DESTRUCTION_EMBER_LOOP.get();
                        if(this.itemStack.getEnchantmentLevel(ModEnchantments.RESTORATION.get()) > 0)
                            soundEvent = RPGSounds.SPELL_RESTORATION_LOOP.get();
                        if(this.itemStack.getEnchantmentLevel(ModEnchantments.ALTERATION.get()) > 0)
                            soundEvent = RPGSounds.SPELL_ALTERATION_LOOP.get();
                        if(this.itemStack.getEnchantmentLevel(ModEnchantments.ILLUSION.get()) > 0)
                            soundEvent = RPGSounds.SPELL_ILLUSION_LOOP.get();
                        if(this.itemStack.getEnchantmentLevel(ModEnchantments.NECROMANCY.get()) > 0)
                            soundEvent = RPGSounds.SPELL_NECROMANCY_LOOP.get();
                        EmberScrollSound sound = new EmberScrollSound(player, this.itemStack, soundEvent);
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