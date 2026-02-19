package net.dainplay.rpgworldmod.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class RainyChunkSyncPacket {
    public enum Operation {
        FULL_SYNC,
        ADD,
        REMOVE
    }

    private final Operation operation;
    private final List<Entry> entries;

    public static class Entry {
        public final int chunkX;
        public final int chunkZ;
        public final long expiryTime; // для REMOVE не используется

        public Entry(int chunkX, int chunkZ, long expiryTime) {
            this.chunkX = chunkX;
            this.chunkZ = chunkZ;
            this.expiryTime = expiryTime;
        }
    }

    public RainyChunkSyncPacket(Operation operation, List<Entry> entries) {
        this.operation = operation;
        this.entries = entries;
    }

    public RainyChunkSyncPacket(FriendlyByteBuf buf) {
        this.operation = buf.readEnum(Operation.class);
        int size = buf.readVarInt();
        this.entries = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            int chunkX = buf.readInt();
            int chunkZ = buf.readInt();
            long expiry = buf.readLong();
            entries.add(new Entry(chunkX, chunkZ, expiry));
        }
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeEnum(operation);
        buf.writeVarInt(entries.size());
        for (Entry e : entries) {
            buf.writeInt(e.chunkX);
            buf.writeInt(e.chunkZ);
            buf.writeLong(e.expiryTime);
        }
    }

    public void handle(Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> ClientRainyChunkData.handlePacket(operation, entries));
        ctx.setPacketHandled(true);
    }
}