package net.dainplay.rpgworldmod.network;

import net.dainplay.rpgworldmod.item.custom.HornCoralStaffItem;
import net.dainplay.rpgworldmod.sounds.RPGSounds;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.vehicle.ContainerEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class UseOnItemStorageEntityTargetPacket {
	private final int playerId;
	private final int targetId;

	public UseOnItemStorageEntityTargetPacket(int playerId, int targetId) {
		this.playerId = playerId;
		this.targetId = targetId;
	}

	public UseOnItemStorageEntityTargetPacket(FriendlyByteBuf buf) {
		this.playerId = buf.readInt();
		this.targetId = buf.readInt();
	}

	public void toBytes(FriendlyByteBuf buf) {
		buf.writeInt(playerId);
		buf.writeInt(targetId);
	}

	public static void handle(UseOnItemStorageEntityTargetPacket msg, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			ServerPlayer player = ctx.get().getSender();
			if (player != null) {
				var itemInHand = player.getItemInHand(player.getUsedItemHand());
				if (itemInHand.getItem() instanceof HornCoralStaffItem staff) {
					player.stopUsingItem();

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

					Entity target = null;
					Entity entity = player.level().getEntity(msg.targetId);
					if (entity instanceof ContainerEntity) {
						target = entity;
					}

					staff.cast(player, target, itemInHand);
				}
			}
		});
		ctx.get().setPacketHandled(true);
	}
}