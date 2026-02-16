package net.dainplay.rpgworldmod.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class IllusionForceDataSyncS2CPacket {
    private final int illusionForce;
    private final BlockPos entPosition;

    public IllusionForceDataSyncS2CPacket(int illusionForce, BlockPos entPosition) {
        this.illusionForce = illusionForce;
        this.entPosition = entPosition;
    }

    public IllusionForceDataSyncS2CPacket(FriendlyByteBuf buf) {
        this.illusionForce = buf.readInt();
        boolean hasPosition = buf.readBoolean();
        if (hasPosition) {
            this.entPosition = buf.readBlockPos();
        } else {
            this.entPosition = null;
        }
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(illusionForce);
        buf.writeBoolean(entPosition != null);
        if (entPosition != null) {
            buf.writeBlockPos(entPosition);
        }
    }

    public void handle(Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() ->
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                        ClientPacketHandlers.handleIllusionForceSync(illusionForce, entPosition))
        );
        context.get().setPacketHandled(true);
    }
}