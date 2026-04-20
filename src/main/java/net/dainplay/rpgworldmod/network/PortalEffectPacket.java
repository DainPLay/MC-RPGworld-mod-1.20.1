package net.dainplay.rpgworldmod.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PortalEffectPacket {
	private final double x;
	private final double y;
	private final double z;

	public PortalEffectPacket(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}


	public PortalEffectPacket(FriendlyByteBuf buf) {
		this.x = buf.readDouble();
		this.y = buf.readDouble();
		this.z = buf.readDouble();
	}


	public void toBytes(FriendlyByteBuf buf) {
		buf.writeDouble(x);
		buf.writeDouble(y);
		buf.writeDouble(z);
	}


	public void handle(Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> ClientPacketHandlers.handlePortalEffect(x, y, z));
		ctx.get().setPacketHandled(true);
	}
}