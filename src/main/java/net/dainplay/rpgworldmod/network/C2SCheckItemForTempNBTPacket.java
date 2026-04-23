package net.dainplay.rpgworldmod.network;

import net.dainplay.rpgworldmod.item.ModItems;
import net.dainplay.rpgworldmod.item.custom.DoubleSidedRecordItem;
import net.dainplay.rpgworldmod.item.custom.MintalTriangleItem;
import net.dainplay.rpgworldmod.sounds.RPGSounds;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class C2SCheckItemForTempNBTPacket {
	private final int slotId;
	private final ClickType clickType;

	public C2SCheckItemForTempNBTPacket(int slotId, ClickType clickType) {
		this.slotId = slotId;
		this.clickType = clickType;
	}

	public C2SCheckItemForTempNBTPacket(int slotId) {
		this.slotId = slotId;
		this.clickType = null;
	}

	public static void encode(C2SCheckItemForTempNBTPacket msg, FriendlyByteBuf buf) {
		buf.writeInt(msg.slotId);
		boolean hasClickType = msg.clickType != null;
		buf.writeBoolean(hasClickType);
		if (hasClickType) {
			buf.writeEnum(msg.clickType);
		}
	}

	public static C2SCheckItemForTempNBTPacket decode(FriendlyByteBuf buf) {
		int slotId = buf.readInt();
		boolean hasClickType = buf.readBoolean();
		ClickType clickType = hasClickType ? buf.readEnum(ClickType.class) : null;
		return new C2SCheckItemForTempNBTPacket(slotId, clickType);
	}

	public void handle(Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			ServerPlayer sender = ctx.get().getSender();
			if (sender == null) return;

			AbstractContainerMenu menu = sender.containerMenu;

			if (clickType == ClickType.QUICK_MOVE) {
				for (int i = 0; i < menu.slots.size(); i++) {
					Slot slot = menu.getSlot(i);
					if (slot.container instanceof Inventory) continue;

					ItemStack stack = slot.getItem();
					if (stack.isEmpty()) continue;

					processItemStack(sender, menu, i, stack);
				}
			} else {
				if (slotId < 0 || slotId >= menu.slots.size()) return;
				Slot slot = menu.getSlot(slotId);
				if (slot.container instanceof Inventory) return;

				ItemStack stack = slot.getItem();
				if (stack.isEmpty()) return;
				processItemStack(sender, menu, slotId, stack);
			}
		});
		ctx.get().setPacketHandled(true);
	}

	private void processItemStack(ServerPlayer sender, AbstractContainerMenu menu, int slotIndex, ItemStack stack) {
		if (stack.getItem() == ModItems.MINTAL_TRIANGLE.get()) {
			MintalTriangleItem.setVibes(stack, 0);
			menu.setItem(slotIndex, menu.getStateId(), stack);
			menu.broadcastChanges();
			return;
		}

		if (stack.getItem() instanceof DoubleSidedRecordItem) {
			DoubleSidedRecordItem.removeFlip(stack);
			menu.setItem(slotIndex, menu.getStateId(), stack);
			menu.broadcastChanges();
			return;
		}

		if (stack.getItem() == ModItems.NETHER_STAR_SCROLL.get()
				&& stack.getTag() != null && stack.getTag().contains("isPickaxe", Tag.TAG_INT)) {
			CompoundTag nbtData = stack.getOrCreateTag();
			nbtData.remove("isPickaxe");
			stack.setTag(nbtData.isEmpty() ? null : nbtData);
			menu.setItem(slotIndex, menu.getStateId(), stack);
			menu.broadcastChanges();

			sender.level().playSound(null, sender.blockPosition(),
					RPGSounds.SPELL_CONJURATION_STOP.get(),
					SoundSource.PLAYERS, 1F,
					(sender.level().random.nextFloat() - sender.level().random.nextFloat()) * 0.2F + 1.0F);
			sender.gameEvent(GameEvent.ENTITY_DIE, sender);
		}
	}
}