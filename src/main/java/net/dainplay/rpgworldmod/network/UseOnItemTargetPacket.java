package net.dainplay.rpgworldmod.network;

import net.dainplay.rpgworldmod.item.custom.BrainCoralStaffItem;
import net.dainplay.rpgworldmod.item.custom.BubbleCoralStaffItem;
import net.dainplay.rpgworldmod.item.custom.FireCoralStaffItem;
import net.dainplay.rpgworldmod.item.custom.TubeCoralStaffItem;
import net.dainplay.rpgworldmod.sounds.RPGSounds;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class UseOnItemTargetPacket {
	private final int playerId;
	private final List<Integer> targetIds;

	public UseOnItemTargetPacket(int playerId, List<Integer> targetIds) {
		this.playerId = playerId;
		this.targetIds = targetIds;
	}

	public UseOnItemTargetPacket(FriendlyByteBuf buf) {
		this.playerId = buf.readInt();
		int size = buf.readInt(); // читаем размер списка
		this.targetIds = new ArrayList<>(size);
		for (int i = 0; i < size; i++) {
			targetIds.add(buf.readInt());
		}
	}

	public void toBytes(FriendlyByteBuf buf) {
		buf.writeInt(playerId);
		buf.writeInt(targetIds.size()); // пишем размер
		for (int id : targetIds) {
			buf.writeInt(id);
		}
	}

	public static void handle(UseOnItemTargetPacket msg, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			ServerPlayer player = ctx.get().getSender();
			if (player == null) return;

			// Получаем все сущности по ID
			List<ItemEntity> targets = new ArrayList<>();
			for (int id : msg.targetIds) {
				Entity entity = player.level().getEntity(id);
				if (entity instanceof ItemEntity itemEntity) {
					targets.add(itemEntity);
				}
			}

			ItemStack itemInHand = player.getItemInHand(player.getUsedItemHand());

			if (itemInHand.getItem() instanceof BrainCoralStaffItem
					|| itemInHand.getItem() instanceof TubeCoralStaffItem
					|| itemInHand.getItem() instanceof BubbleCoralStaffItem
					|| itemInHand.getItem() instanceof FireCoralStaffItem) {
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
				if (itemInHand.getItem() instanceof BrainCoralStaffItem staff) staff.cast(player, targets, itemInHand);
				if (itemInHand.getItem() instanceof TubeCoralStaffItem staff) staff.cast(player, targets, itemInHand);
				if (itemInHand.getItem() instanceof BubbleCoralStaffItem staff) staff.cast(player, targets, itemInHand);
				if (itemInHand.getItem() instanceof FireCoralStaffItem staff) staff.cast(player, targets, itemInHand);

			}
		});
		ctx.get().setPacketHandled(true);
	}
}