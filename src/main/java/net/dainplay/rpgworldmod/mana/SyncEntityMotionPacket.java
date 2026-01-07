package net.dainplay.rpgworldmod.mana;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SyncEntityMotionPacket {
	private final int entityId;
	private final double motionX;
	private final double motionY;
	private final double motionZ;

	public SyncEntityMotionPacket(int entityId, double motionX, double motionY, double motionZ) {
		this.entityId = entityId;
		this.motionX = motionX;
		this.motionY = motionY;
		this.motionZ = motionZ;
	}

	public SyncEntityMotionPacket(FriendlyByteBuf buf) {
		this.entityId = buf.readInt();
		this.motionX = buf.readDouble();
		this.motionY = buf.readDouble();
		this.motionZ = buf.readDouble();
	}

	public void toBytes(FriendlyByteBuf buf) {
		buf.writeInt(entityId);
		buf.writeDouble(motionX);
		buf.writeDouble(motionY);
		buf.writeDouble(motionZ);
	}

	public boolean handle(Supplier<NetworkEvent.Context> supplier) {
		NetworkEvent.Context context = supplier.get();
		context.enqueueWork(() -> {
			if (Minecraft.getInstance().level != null) {
				Entity entity = Minecraft.getInstance().level.getEntity(entityId);
				if (entity != null) {
					entity.setDeltaMovement(motionX, motionY, motionZ);
				}
			}
		});
		return true;
	}
}