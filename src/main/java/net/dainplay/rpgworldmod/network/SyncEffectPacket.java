package net.dainplay.rpgworldmod.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SyncEffectPacket {
    private final int entityId;
    private final boolean hasEffect;
    private final int amplifier;
    private final int duration;

    public SyncEffectPacket(int entityId, boolean hasEffect, int amplifier, int duration) {
        this.entityId = entityId;
        this.hasEffect = hasEffect;
        this.amplifier = amplifier;
        this.duration = duration;
    }

    public SyncEffectPacket(FriendlyByteBuf buf) {
        this.entityId = buf.readInt();
        this.hasEffect = buf.readBoolean();
        this.amplifier = buf.readInt();
        this.duration = buf.readInt();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(entityId);
        buf.writeBoolean(hasEffect);
        buf.writeInt(amplifier);
        buf.writeInt(duration);
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() ->
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                        ClientPacketHandlers.handleSyncEffect(entityId, hasEffect, amplifier, duration))
        );
        context.setPacketHandled(true);
    }
}