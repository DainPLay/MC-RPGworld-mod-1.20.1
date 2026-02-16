package net.dainplay.rpgworldmod.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class IsManaRegenBlockedDataSyncS2CPacket {
	private final int isManaRegenBlocked;

	public IsManaRegenBlockedDataSyncS2CPacket(int isManaRegenBlocked) {
		this.isManaRegenBlocked = isManaRegenBlocked;
	}

	public IsManaRegenBlockedDataSyncS2CPacket(FriendlyByteBuf buf) {
		this.isManaRegenBlocked = buf.readInt();
	}

	public void toBytes(FriendlyByteBuf buf) {
		buf.writeInt(isManaRegenBlocked);
	}

	public boolean handle(Supplier<NetworkEvent.Context> supplier) {
		NetworkEvent.Context context = supplier.get();
		context.enqueueWork(() ->
				DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientPacketHandlers.handleIsManaRegenBlockedSync(isManaRegenBlocked))
		);
		context.setPacketHandled(true);
		return true;
	}
}