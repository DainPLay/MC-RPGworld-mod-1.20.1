package net.dainplay.rpgworldmod.network;

import net.minecraft.network.FriendlyByteBuf;
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
		context.enqueueWork(() -> {
			// HERE WE ARE ON CLIENT
			ClientMaxManaData.set(max_mana);
		});
		return true;
	}
}
