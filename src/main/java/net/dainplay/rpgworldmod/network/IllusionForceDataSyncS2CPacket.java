package net.dainplay.rpgworldmod.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class IllusionForceDataSyncS2CPacket {
	private final int illusionForce;
	private final float entPositionX;
	private final float entPositionY;
	private final float entPositionZ;
	private final boolean isSet;
	private final boolean isEnt;

	public IllusionForceDataSyncS2CPacket(int illusionForce, float entPositionX, float entPositionY, float entPositionZ, boolean isSet, boolean ent) {
		this.illusionForce = illusionForce;
		this.entPositionX = entPositionX;
		this.entPositionY = entPositionY;
		this.entPositionZ = entPositionZ;
		this.isSet = isSet;
		this.isEnt = ent;
	}

	public IllusionForceDataSyncS2CPacket(FriendlyByteBuf buf) {
		this.illusionForce = buf.readInt();
		this.isSet = buf.readBoolean();
		this.isEnt = buf.readBoolean();
		if (isSet) {
			this.entPositionX = buf.readFloat();
			this.entPositionY = buf.readFloat();
			this.entPositionZ = buf.readFloat();
		} else {
			this.entPositionX = 0.0f;
			this.entPositionY = 0.0f;
			this.entPositionZ = 0.0f;
		}
	}

	public void toBytes(FriendlyByteBuf buf) {
		buf.writeInt(illusionForce);
		buf.writeBoolean(isSet);
		buf.writeBoolean(isEnt);
		if (isSet) {
			buf.writeFloat(entPositionX);
			buf.writeFloat(entPositionY);
			buf.writeFloat(entPositionZ);
		}
	}

	public void handle(Supplier<NetworkEvent.Context> context) {
		context.get().enqueueWork(() ->
				DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
						ClientPacketHandlers.handleIllusionForceSync(illusionForce, entPositionX, entPositionY, entPositionZ, isSet, isEnt))
		);
		context.get().setPacketHandled(true);
	}
}