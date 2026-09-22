package net.dainplay.rpgworldmod.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SyncEntityMotionPacket {
	private final int entityId;
	private final int sourceEntityId;   // НОВОЕ: ID Razorleaf-источника
	private final boolean isPull;       // НОВОЕ: true только для притягивания языком
	private final double motionX;
	private final double motionY;
	private final double motionZ;

	public SyncEntityMotionPacket(int entityId, int sourceEntityId, boolean isPull,
								  double motionX, double motionY, double motionZ) {
		this.entityId = entityId;
		this.sourceEntityId = sourceEntityId;
		this.isPull = isPull;
		this.motionX = motionX;
		this.motionY = motionY;
		this.motionZ = motionZ;
	}

	public SyncEntityMotionPacket(FriendlyByteBuf buf) {
		this.entityId = buf.readInt();
		this.sourceEntityId = buf.readInt();
		this.isPull = buf.readBoolean();
		this.motionX = buf.readDouble();
		this.motionY = buf.readDouble();
		this.motionZ = buf.readDouble();
	}

	public void toBytes(FriendlyByteBuf buf) {
		buf.writeInt(entityId);
		buf.writeInt(sourceEntityId);
		buf.writeBoolean(isPull);
		buf.writeDouble(motionX);
		buf.writeDouble(motionY);
		buf.writeDouble(motionZ);
	}

	public boolean handle(Supplier<NetworkEvent.Context> supplier) {
		NetworkEvent.Context context = supplier.get();
		context.enqueueWork(() ->
				DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
						ClientPacketHandlers.handleSyncEntityMotion(
								entityId, sourceEntityId, isPull,
								motionX, motionY, motionZ))
		);
		context.setPacketHandled(true);
		return true;
	}
}