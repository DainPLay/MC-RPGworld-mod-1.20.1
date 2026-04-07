package net.dainplay.rpgworldmod.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SculkStaffCDDataSyncS2CPacket {
	private final int cooldown;

	public SculkStaffCDDataSyncS2CPacket(int cooldown) {
		this.cooldown = cooldown;
	}

	public SculkStaffCDDataSyncS2CPacket(FriendlyByteBuf buf) {
		this.cooldown = buf.readInt();
	}

	public void toBytes(FriendlyByteBuf buf) {
		buf.writeInt(cooldown);
	}

	public boolean handle(Supplier<NetworkEvent.Context> supplier) {
		NetworkEvent.Context context = supplier.get();
		context.enqueueWork(() ->
				DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientPacketHandlers.handleSculkStaffCDSync(cooldown))
		);
		context.setPacketHandled(true);
		return true;
	}
}