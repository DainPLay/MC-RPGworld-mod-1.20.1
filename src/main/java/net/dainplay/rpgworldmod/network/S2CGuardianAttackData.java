package net.dainplay.rpgworldmod.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class S2CGuardianAttackData {
    private final int playerId;
    private final int targetId;
    private final int attackTime;
    private final boolean active;
    private final boolean damageDealt; // новое поле

    public S2CGuardianAttackData(int playerId, int targetId, int attackTime, boolean active, boolean damageDealt) {
        this.playerId = playerId;
        this.targetId = targetId;
        this.attackTime = attackTime;
        this.active = active;
        this.damageDealt = damageDealt;
    }

    public S2CGuardianAttackData(FriendlyByteBuf buf) {
        this.playerId = buf.readInt();
        this.targetId = buf.readInt();
        this.attackTime = buf.readInt();
        this.active = buf.readBoolean();
        this.damageDealt = buf.readBoolean();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(playerId);
        buf.writeInt(targetId);
        buf.writeInt(attackTime);
        buf.writeBoolean(active);
        buf.writeBoolean(damageDealt);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ClientGuardianAttackData.handle(playerId, targetId, attackTime, active, damageDealt);
        });
        ctx.get().setPacketHandled(true);
    }
}