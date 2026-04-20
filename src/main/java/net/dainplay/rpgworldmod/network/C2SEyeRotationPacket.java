package net.dainplay.rpgworldmod.network;

import net.dainplay.rpgworldmod.entity.custom.EnderEyeViewEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class C2SEyeRotationPacket {
	private final int entityId;
	private final float yaw;
	private final float pitch;

	public C2SEyeRotationPacket(int entityId, float yaw, float pitch) {
		this.entityId = entityId;
		this.yaw = yaw;
		this.pitch = pitch;
	}

	public static void encode(C2SEyeRotationPacket msg, FriendlyByteBuf buf) {
		buf.writeInt(msg.entityId);
		buf.writeFloat(msg.yaw);
		buf.writeFloat(msg.pitch);
	}

	public static C2SEyeRotationPacket decode(FriendlyByteBuf buf) {
		return new C2SEyeRotationPacket(buf.readInt(), buf.readFloat(), buf.readFloat());
	}

	public void handle(Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			ServerPlayer sender = ctx.get().getSender();
			if (sender != null) {
				Level level = sender.level();
				Entity entity = level.getEntity(entityId);
				if (entity instanceof EnderEyeViewEntity eye && eye.getOwner() == sender) {
					eye.setYaw(yaw);
					eye.setPitch(pitch);
				}
			}
		});
		ctx.get().setPacketHandled(true);
	}
}