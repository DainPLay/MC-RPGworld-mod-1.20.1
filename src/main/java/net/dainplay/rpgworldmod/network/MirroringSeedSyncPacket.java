package net.dainplay.rpgworldmod.network;

import net.dainplay.rpgworldmod.render.MirroringEffectRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class MirroringSeedSyncPacket {
	private final int entityId;
	private final long seed;

	public MirroringSeedSyncPacket(int entityId, long seed) {
		this.entityId = entityId;
		this.seed = seed;
	}

	public MirroringSeedSyncPacket(FriendlyByteBuf buf) {
		this.entityId = buf.readVarInt();
		this.seed = buf.readLong();
	}

	public void toBytes(FriendlyByteBuf buf) {
		buf.writeVarInt(entityId);
		buf.writeLong(seed);
	}

	public boolean handle(Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			ClientLevel level = Minecraft.getInstance().level;
			if (level == null) return;
			MirroringEffectRenderer.updateSeed(entityId, seed, level.getGameTime());
		});
		return true;
	}
}