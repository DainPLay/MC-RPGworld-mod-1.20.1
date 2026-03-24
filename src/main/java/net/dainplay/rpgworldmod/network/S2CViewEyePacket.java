package net.dainplay.rpgworldmod.network;

import net.dainplay.rpgworldmod.entity.custom.EnderEyeViewEntity;
import net.dainplay.rpgworldmod.util.ClientEyeViewHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class S2CViewEyePacket {
    private final int entityId;

    public S2CViewEyePacket(int entityId) {
        this.entityId = entityId;
    }

    public static void encode(S2CViewEyePacket msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.entityId);
    }

    public static S2CViewEyePacket decode(FriendlyByteBuf buf) {
        return new S2CViewEyePacket(buf.readInt());
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.level != null) {
                Entity entity = mc.level.getEntity(entityId);
                if (entity instanceof EnderEyeViewEntity eye) {
                    ClientEyeViewHandler.activate(eye);
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}