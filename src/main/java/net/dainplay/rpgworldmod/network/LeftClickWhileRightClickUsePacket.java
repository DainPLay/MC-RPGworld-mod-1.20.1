package net.dainplay.rpgworldmod.network;

import net.dainplay.rpgworldmod.item.custom.BlazeStaffItem;
import net.dainplay.rpgworldmod.item.custom.EmberScrollItem;
import net.dainplay.rpgworldmod.sounds.RPGSounds;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class LeftClickWhileRightClickUsePacket {
	private final int playerId;

	public LeftClickWhileRightClickUsePacket(int playerId) {
		this.playerId = playerId;
	}

	public LeftClickWhileRightClickUsePacket(FriendlyByteBuf buf) {
		this.playerId = buf.readInt();
	}

	public void toBytes(FriendlyByteBuf buf) {
		buf.writeInt(playerId);
	}

	public static void handle(LeftClickWhileRightClickUsePacket msg, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			ServerPlayer player = ctx.get().getSender();
			if (player != null) {
				var itemInHand = player.getItemInHand(player.getUsedItemHand());
				if (itemInHand.getItem() instanceof BlazeStaffItem staff) {
					player.stopUsingItem();


					var levelUseData = EmberScrollItem.getPlayerUseData(player.level());
					levelUseData.remove(player.getUUID());

					player.level().playSound(null,
							player.getX(), player.getY(), player.getZ(),
							RPGSounds.STAFF_STOP.get(),
							SoundSource.PLAYERS, 1.0F, 1.0F
					);


					ModMessages.sendToNearbyPlayers(
							new LoopSoundPacket(player.getId(), false, itemInHand),
							player.serverLevel(),
							player.blockPosition(),
							64.0
					);
					staff.cast(player, itemInHand);
				}
			}
		});
		ctx.get().setPacketHandled(true);
	}
}