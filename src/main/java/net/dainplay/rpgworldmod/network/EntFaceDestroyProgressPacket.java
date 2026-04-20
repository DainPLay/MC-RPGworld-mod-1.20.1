package net.dainplay.rpgworldmod.network;

import net.dainplay.rpgworldmod.block.entity.custom.EntFaceBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class EntFaceDestroyProgressPacket {
	private final BlockPos blockPos;
	private final boolean attacking;
	private final ItemStack heldItem;

	public EntFaceDestroyProgressPacket(BlockPos blockPos, boolean attacking, ItemStack heldItem) {
		this.blockPos = blockPos;
		this.attacking = attacking;
		this.heldItem = heldItem.copy();
	}

	public EntFaceDestroyProgressPacket(FriendlyByteBuf buf) {
		this.blockPos = buf.readBlockPos();
		this.attacking = buf.readBoolean();
		this.heldItem = buf.readItem();
	}

	public void toBytes(FriendlyByteBuf buf) {
		buf.writeBlockPos(blockPos);
		buf.writeBoolean(attacking);
		buf.writeItem(heldItem);
	}

	public boolean handle(Supplier<NetworkEvent.Context> supplier) {
		NetworkEvent.Context context = supplier.get();
		context.enqueueWork(() -> {
			ServerPlayer player = context.getSender();
			if (player != null && player.level().getBlockEntity(blockPos) instanceof EntFaceBlockEntity entFaceBlockEntity) {
				if (attacking) {
					entFaceBlockEntity.activelyDestroy(player, heldItem);
				}
			}
		});
		return true;
	}
}