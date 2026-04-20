package net.dainplay.rpgworldmod.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class FireExtinguishParticlesPacket {
	private final BlockPos fireCatcherPos;
	private final BlockPos firePos;

	public FireExtinguishParticlesPacket(BlockPos fireCatcherPos, BlockPos firePos) {
		this.fireCatcherPos = fireCatcherPos;
		this.firePos = firePos;
	}

	public FireExtinguishParticlesPacket(FriendlyByteBuf buf) {
		this.fireCatcherPos = buf.readBlockPos();
		this.firePos = buf.readBlockPos();
	}

	public void toBytes(FriendlyByteBuf buf) {
		buf.writeBlockPos(fireCatcherPos);
		buf.writeBlockPos(firePos);
	}

	public boolean handle(Supplier<NetworkEvent.Context> supplier) {
		NetworkEvent.Context context = supplier.get();
		context.enqueueWork(() ->
				DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
						ClientPacketHandlers.handleFireExtinguishParticles(fireCatcherPos, firePos))
		);
		context.setPacketHandled(true);
		return true;
	}
}