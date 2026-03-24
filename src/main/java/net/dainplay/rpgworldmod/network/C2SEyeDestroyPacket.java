package net.dainplay.rpgworldmod.network;

import net.dainplay.rpgworldmod.entity.custom.EnderEyeViewEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class C2SEyeDestroyPacket {
    private final int entityId;

    public C2SEyeDestroyPacket(int entityId) {
        this.entityId = entityId;
    }

    public static void encode(C2SEyeDestroyPacket msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.entityId);
    }

    public static C2SEyeDestroyPacket decode(FriendlyByteBuf buf) {
        return new C2SEyeDestroyPacket(buf.readInt());
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer sender = ctx.get().getSender();
            if (sender != null) {
                Level level = sender.level();
                Entity entity = level.getEntity(entityId);
                if (entity instanceof EnderEyeViewEntity eye && eye.getOwner() == sender) {
                    eye.explode();
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}