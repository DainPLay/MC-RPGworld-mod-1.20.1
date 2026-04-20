package net.dainplay.rpgworldmod.mixin;

import net.dainplay.rpgworldmod.item.custom.DoubleSidedRecordItem;
import net.dainplay.rpgworldmod.item.custom.MintalTriangleItem;
import net.dainplay.rpgworldmod.item.custom.NetherStarScrollItem;
import net.dainplay.rpgworldmod.item.custom.StaffItem;
import net.dainplay.rpgworldmod.network.C2SCheckItemForTempNBTPacket;
import net.dainplay.rpgworldmod.network.C2STriggerChestStaffsPacket;
import net.dainplay.rpgworldmod.network.ModMessages;
import net.dainplay.rpgworldmod.util.ITriggerChestStaffs;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@OnlyIn(Dist.CLIENT)
@Mixin(AbstractContainerMenu.class)
public abstract class MixinAbstractContainerMenuClient implements ITriggerChestStaffs {
	@Unique
	private boolean rpgworldmod$sentTrigger = false;

	@Override
	public boolean rpgworldmod$hasSentTrigger() {
		return this.rpgworldmod$sentTrigger;
	}

	@Override
	public void rpgworldmod$setSentTrigger(boolean sent) {
		this.rpgworldmod$sentTrigger = sent;
	}

	@Inject(method = "setItem(IILnet/minecraft/world/item/ItemStack;)V", at = @At("RETURN"))
	private void onSetItem(int slotId, int stateId, ItemStack stack, CallbackInfo ci) {
		if (stack.getItem() instanceof StaffItem) {
			this.checkAndSendTrigger();
		}
		if (stack.getItem() instanceof NetherStarScrollItem) {
			if (stack.getTag() != null && stack.getTag().contains("isPickaxe", Tag.TAG_INT)) {
				this.CheckItemForTempNBTPacket(slotId);
			}
		}
		if (stack.getItem() instanceof MintalTriangleItem) {
			this.CheckItemForTempNBTPacket(slotId);
		}
		if (stack.getItem() instanceof DoubleSidedRecordItem) {
			this.CheckItemForTempNBTPacket(slotId);
		}
	}

	@Inject(method = "initializeContents(ILjava/util/List;Lnet/minecraft/world/item/ItemStack;)V", at = @At("RETURN"))
	private void onInitializeContents(int stateId, List<ItemStack> items, ItemStack carried, CallbackInfo ci) {
		this.checkAndSendTrigger();
	}

	@Inject(method = "setRemoteSlot", at = @At("RETURN"))
	private void onSetRemoteSlot(int slot, ItemStack stack, CallbackInfo ci) {
		if (stack.getItem() instanceof StaffItem) {
			this.checkAndSendTrigger();
		}
		if (stack.getItem() instanceof NetherStarScrollItem) {
			if (stack.getTag() != null && stack.getTag().contains("isPickaxe", Tag.TAG_INT)) {
				this.CheckItemForTempNBTPacket(slot);
			}
		}
		if (stack.getItem() instanceof MintalTriangleItem) {
			this.CheckItemForTempNBTPacket(slot);
		}
		if (stack.getItem() instanceof DoubleSidedRecordItem) {
			this.CheckItemForTempNBTPacket(slot);
		}
	}

	@Inject(method = "setRemoteSlotNoCopy", at = @At("RETURN"))
	private void onSetRemoteSlotNoCopy(int slot, ItemStack stack, CallbackInfo ci) {
		if (stack.getItem() instanceof StaffItem) {
			this.checkAndSendTrigger();
		}
		if (stack.getItem() instanceof NetherStarScrollItem) {
			if (stack.getTag() != null && stack.getTag().contains("isPickaxe", Tag.TAG_INT)) {
				this.CheckItemForTempNBTPacket(slot);
			}
		}
		if (stack.getItem() instanceof MintalTriangleItem) {
			this.CheckItemForTempNBTPacket(slot);
		}
		if (stack.getItem() instanceof DoubleSidedRecordItem) {
			this.CheckItemForTempNBTPacket(slot);
		}
	}

	@Unique
	private void CheckItemForTempNBTPacket(int slot) {
		AbstractContainerMenu menu = (AbstractContainerMenu) (Object) this;

		if (menu.slots.get(slot).container instanceof Inventory) return;
		if (menu.slots.get(slot) instanceof CreativeModeInventoryScreen.CustomCreativeSlot) return;
		ModMessages.sendToServer(new C2SCheckItemForTempNBTPacket(slot));
	}

	@Unique
	private void checkAndSendTrigger() {
		AbstractContainerMenu menu = (AbstractContainerMenu) (Object) this;
		Minecraft mc = Minecraft.getInstance();
		if (mc.player == null) return;
		if (this.rpgworldmod$hasSentTrigger()) return;

		Set<String> uniqueKeys = new HashSet<>();
		for (Slot slot : menu.slots) {
			if (slot.container instanceof Inventory) continue;
			if (slot instanceof CreativeModeInventoryScreen.CustomCreativeSlot) continue;

			ItemStack stack = slot.getItem();
			if (stack.getItem() instanceof StaffItem staff) {
				StaffItem.GemType gem = StaffItem.getGemType(stack);
				String key = BuiltInRegistries.ITEM.getKey(staff).toString() + "_" + gem.getName();
				uniqueKeys.add(key);
				if (uniqueKeys.size() >= 27) {
					ModMessages.sendToServer(new C2STriggerChestStaffsPacket());
					this.rpgworldmod$setSentTrigger(true);
					break;
				}
			}
		}

		if (uniqueKeys.size() < 27 && this.rpgworldmod$hasSentTrigger()) {
			this.rpgworldmod$setSentTrigger(false);
		}
	}
}