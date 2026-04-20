package net.dainplay.rpgworldmod.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class LoopSoundPacket {
	private final int playerId;
	private final boolean start;
	private final ItemStack itemStack;

	public LoopSoundPacket(int playerId, boolean start, ItemStack itemStack) {
		this.playerId = playerId;
		this.start = start;
		this.itemStack = itemStack;
	}

	public static LoopSoundPacket decode(FriendlyByteBuf buf) {
		return new LoopSoundPacket(buf.readInt(), buf.readBoolean(), buf.readItem());
	}

	public void encode(FriendlyByteBuf buf) {
		buf.writeInt(this.playerId);
		buf.writeBoolean(this.start);
		buf.writeItem(this.itemStack);
	}

	public void handle(Supplier<NetworkEvent.Context> context) {
		context.get().enqueueWork(() ->
				DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
						ClientPacketHandlers.handleLoopSound(playerId, start, itemStack))
		);
		context.get().setPacketHandled(true);
	}
}