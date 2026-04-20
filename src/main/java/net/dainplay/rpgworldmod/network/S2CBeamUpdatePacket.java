package net.dainplay.rpgworldmod.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;


public class S2CBeamUpdatePacket {
	private final int ownerId;
	private final double endX, endY, endZ;
	private final boolean active;
	private final List<Vec3> hitEntityPositions;

	public S2CBeamUpdatePacket(int ownerId, Vec3 endPos, boolean active, List<Vec3> hitEntityPositions) {
		this.ownerId = ownerId;
		this.endX = endPos.x;
		this.endY = endPos.y;
		this.endZ = endPos.z;
		this.active = active;
		this.hitEntityPositions = hitEntityPositions;
	}


	public S2CBeamUpdatePacket(int ownerId, Vec3 endPos, boolean active) {
		this(ownerId, endPos, active, new ArrayList<>());
	}

	public static void encode(S2CBeamUpdatePacket packet, FriendlyByteBuf buf) {
		buf.writeInt(packet.ownerId);
		buf.writeDouble(packet.endX);
		buf.writeDouble(packet.endY);
		buf.writeDouble(packet.endZ);
		buf.writeBoolean(packet.active);
		buf.writeInt(packet.hitEntityPositions.size());
		for (Vec3 pos : packet.hitEntityPositions) {
			buf.writeDouble(pos.x);
			buf.writeDouble(pos.y);
			buf.writeDouble(pos.z);
		}
	}

	public static S2CBeamUpdatePacket decode(FriendlyByteBuf buf) {
		int ownerId = buf.readInt();
		double endX = buf.readDouble();
		double endY = buf.readDouble();
		double endZ = buf.readDouble();
		boolean active = buf.readBoolean();
		int size = buf.readInt();
		List<Vec3> positions = new ArrayList<>(size);
		for (int i = 0; i < size; i++) {
			positions.add(new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble()));
		}
		return new S2CBeamUpdatePacket(ownerId, new Vec3(endX, endY, endZ), active, positions);
	}

	public static void handle(S2CBeamUpdatePacket packet, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			ClientPacketHandlers.handleBeamUpdate(packet.ownerId, packet.endX, packet.endY, packet.endZ, packet.active, packet.hitEntityPositions);
		});
		ctx.get().setPacketHandled(true);
	}
}