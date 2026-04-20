package net.dainplay.rpgworldmod.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SwingPlayerPacket {
	private final Vec3 motion;
	private final int playerId;

	public SwingPlayerPacket(Vec3 motion, int playerId) {
		this.motion = motion;
		this.playerId = playerId;
	}

	public SwingPlayerPacket(FriendlyByteBuf buf) {
		this.motion = new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble());
		this.playerId = buf.readInt();
	}

	public void toBytes(FriendlyByteBuf buf) {
		buf.writeDouble(motion.x);
		buf.writeDouble(motion.y);
		buf.writeDouble(motion.z);
		buf.writeInt(playerId);
	}

	public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
		NetworkEvent.Context context = contextSupplier.get();
		context.enqueueWork(() ->
				DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
						ClientPacketHandlers.handleSwingPlayer(motion, playerId))
		);
		context.setPacketHandled(true);
	}
}