package net.dainplay.rpgworldmod.network;

import net.dainplay.rpgworldmod.RPGworldMod;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
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
        context.get().enqueueWork(() -> {
            ClientIllusionForceData.set(illusionForce);
            ClientEntPositionData.set(entPosition);
        });
        context.get().setPacketHandled(true);
    }
}