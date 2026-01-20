package net.dainplay.rpgworldmod.network;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PullPlayerPacket {
    private final Vec3 motion;
    private final int playerId;

    public PullPlayerPacket(Vec3 motion, int playerId) {
        this.motion = motion;
        this.playerId = playerId;
    }

    public PullPlayerPacket(FriendlyByteBuf buf) {
        this.motion = new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble());
        this.playerId = buf.readInt();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeDouble(motion.x);
        buf.writeDouble(motion.y);
        buf.writeDouble(motion.z);
        buf.writeInt(playerId);
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            // Выполняем на клиенте
            if (context.getDirection().getReceptionSide().isClient()) {
                Minecraft minecraft = Minecraft.getInstance();
                if (minecraft.level != null) {
                    // Если это текущий игрок
                    if (minecraft.player != null && minecraft.player.getId() == playerId) {
                        // Применяем движение к локальному игроку
                        minecraft.player.setDeltaMovement(
                            minecraft.player.getDeltaMovement().add(motion)
                        );
                        // Сбрасываем высоту падения
                        minecraft.player.fallDistance = 0;
                    }
                }
            }
        });
        context.setPacketHandled(true);
    }
}