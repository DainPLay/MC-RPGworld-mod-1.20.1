package net.dainplay.rpgworldmod.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ParanoiaSoundPacket {
	private final int entityId;

	public ParanoiaSoundPacket(int entityId) {
		this.entityId = entityId;
	}

	public ParanoiaSoundPacket(FriendlyByteBuf buf) {
		this.entityId = buf.readInt();
	}

	public void toBytes(FriendlyByteBuf buf) {
		buf.writeInt(entityId);
	}

	public boolean handle(Supplier<NetworkEvent.Context> supplier) {
		NetworkEvent.Context context = supplier.get();
		context.enqueueWork(() ->
				DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
						ClientPacketHandlers.handleParanoiaSound(entityId))
		);
		context.setPacketHandled(true);
		return true;
	}
}