package net.dainplay.rpgworldmod.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class BoundEntitySyncPacket {
    private final int entityId;
    private final boolean isRemoval;
    private final BoundEntityData data;

    public BoundEntitySyncPacket(int entityId, BoundEntityData data) {
        this.entityId = entityId;
        this.isRemoval = false;
        this.data = data;
    }

    public BoundEntitySyncPacket(int entityId) {
        this.entityId = entityId;
        this.isRemoval = true;
        this.data = null;
    }

    public BoundEntitySyncPacket(FriendlyByteBuf buf) {
        this.entityId = buf.readInt();
        this.isRemoval = buf.readBoolean();
        if (!isRemoval) {
            UUID boundPlayerId = buf.readUUID();
            double playerX = buf.readDouble();
            double playerY = buf.readDouble();
            double playerZ = buf.readDouble();
            boolean isArrow = buf.readBoolean();
            this.data = new BoundEntityData(entityId, boundPlayerId, playerX, playerY, playerZ, isArrow);
        } else {
            this.data = null;
        }
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(entityId);
        buf.writeBoolean(isRemoval);
        if (!isRemoval && data != null) {
            buf.writeUUID(data.boundPlayerId);
            buf.writeDouble(data.playerX);
            buf.writeDouble(data.playerY);
            buf.writeDouble(data.playerZ);
            buf.writeBoolean(data.isArrow);
        }
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() ->
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                        ClientPacketHandlers.handleBoundEntitySync(entityId, isRemoval, data))
        );
        context.setPacketHandled(true);
        return true;
    }

    public static class BoundEntityData {
        public final int entityId;
        public final UUID boundPlayerId;
        public final double playerX;
        public final double playerY;
        public final double playerZ;
        public final boolean isArrow;

        public BoundEntityData(int entityId, UUID boundPlayerId, double playerX, double playerY, double playerZ, boolean isArrow) {
            this.entityId = entityId;
            this.boundPlayerId = boundPlayerId;
            this.playerX = playerX;
            this.playerY = playerY;
            this.playerZ = playerZ;
            this.isArrow = isArrow;
        }
    }
}