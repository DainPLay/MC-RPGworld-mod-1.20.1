package net.dainplay.rpgworldmod.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class MaxManaDataSyncS2CPacket {
	private final int max_mana;

	public MaxManaDataSyncS2CPacket(int max_mana) {
		this.max_mana = max_mana;
	}

	public MaxManaDataSyncS2CPacket(FriendlyByteBuf buf) {
		this.max_mana = buf.readInt();
	}

	public void toBytes(FriendlyByteBuf buf) {
		buf.writeInt(max_mana);
	}

	public boolean handle(Supplier<NetworkEvent.Context> supplier) {
		NetworkEvent.Context context = supplier.get();
		context.enqueueWork(() ->
				DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientPacketHandlers.handleMaxManaSync(max_mana))
		);
		context.setPacketHandled(true);
		return true;
	}
}