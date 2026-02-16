package net.dainplay.rpgworldmod.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ManaDataSyncS2CPacket {
	private final int mana;

	public ManaDataSyncS2CPacket(int mana) {
		this.mana = mana;
	}

	public ManaDataSyncS2CPacket(FriendlyByteBuf buf) {
		this.mana = buf.readInt();
	}

	public void toBytes(FriendlyByteBuf buf) {
		buf.writeInt(mana);
	}

	public boolean handle(Supplier<NetworkEvent.Context> supplier) {
		NetworkEvent.Context context = supplier.get();
		context.enqueueWork(() ->
				DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientPacketHandlers.handleManaSync(mana))
		);
		context.setPacketHandled(true);
		return true;
	}
}