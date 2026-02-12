package net.dainplay.rpgworldmod.network;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ParanoiaSoundPacket {
    private final int entityId;

    public ParanoiaSoundPacket(int entityId) {
        this.entityId = entityId;
    }

    public ParanoiaSoundPacket(FriendlyByteBuf buf) {
        this.entityId = buf.readInt();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(entityId);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            // Проверяем, что это локальный игрок
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.player != null && minecraft.player.getId() == this.entityId) {
                // Проигрываем звук только локальному игроку
                // Вы можете использовать свой звук вместо AMBIENT_CAVE
                minecraft.player.playNotifySound(
                    SoundEvents.AMBIENT_CAVE.value(), // Замените на ваш звук
                    SoundSource.AMBIENT,
                    1.0f,
                    1.0f
                );
            }
        });
        return true;
    }
}