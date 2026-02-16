package net.dainplay.rpgworldmod.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class S2CTargetValidationResultPacket {
    private final int targetId;
    private final boolean isValid;

    public S2CTargetValidationResultPacket(int targetId, boolean isValid) {
        this.targetId = targetId;
        this.isValid = isValid;
    }

    public S2CTargetValidationResultPacket(FriendlyByteBuf buf) {
        this.targetId = buf.readInt();
        this.isValid = buf.readBoolean();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(targetId);
        buf.writeBoolean(isValid);
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() ->
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                        ClientPacketHandlers.handleTargetValidationResult(targetId, isValid))
        );
        context.setPacketHandled(true);
    }
}