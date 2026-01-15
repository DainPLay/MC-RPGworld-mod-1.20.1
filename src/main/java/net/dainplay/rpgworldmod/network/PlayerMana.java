package net.dainplay.rpgworldmod.network;

import net.dainplay.rpgworldmod.data.tags.ModAdvancements;
import net.dainplay.rpgworldmod.item.ModItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;

public class PlayerMana {
	private int mana;
	private int max_mana;
	private int isManaRegenBlocked; // Новое поле для блокировки восстановления маны

	public int getMana() {
		return mana;
	}

	public void setMana(ServerPlayer player, int amount) {
		int prev_mana = mana;
		mana = amount;
		if(amount != prev_mana) ModMessages.sendToPlayer(new ManaDataSyncS2CPacket(mana), player);
	}

	public void addMana(ServerPlayer player, int amount) {
		mana = Math.min(mana + amount, max_mana);
		ModMessages.sendToPlayer(new ManaDataSyncS2CPacket(mana), player);
	}

	public void reduceMana(ServerPlayer player, int amount) {
		mana = Math.max(mana - amount, 0);
		if (mana == 0) ModAdvancements.GET_DEPRESSED_TRIGGER.trigger(player);
		ModMessages.sendToPlayer(new ManaDataSyncS2CPacket(mana), player);
	}

	public void recalculateMaxMana(ServerPlayer player) {
		int prev_max_mana = max_mana;
		max_mana = 100;
		if(player.getItemBySlot(EquipmentSlot.HEAD).getItem() == ModItems.LIVING_WOOD_HELMET.get()) max_mana += 25;
		if(player.getItemBySlot(EquipmentSlot.CHEST).getItem() == ModItems.LIVING_WOOD_CHESTPLATE.get()) max_mana += 25;
		if(player.getItemBySlot(EquipmentSlot.LEGS).getItem() == ModItems.LIVING_WOOD_LEGGINGS.get()) max_mana += 25;
		if(player.getItemBySlot(EquipmentSlot.FEET).getItem() == ModItems.LIVING_WOOD_BOOTS.get()) max_mana += 25;
		this.setMana(player, Math.min(mana, max_mana));
		if(prev_max_mana == 0) addMana(player, max_mana);
		if(max_mana != prev_max_mana) ModMessages.sendToPlayer(new MaxManaDataSyncS2CPacket(max_mana), player);
	}

	public int getMaxMana() {
		return max_mana;
	}

	// Методы для управления блокировкой восстановления маны
	public void setManaRegenBlocked(ServerPlayer player, int ticks) {
		if (this.isManaRegenBlocked != ticks) {
			this.isManaRegenBlocked = ticks;
			ModMessages.sendToPlayer(new IsManaRegenBlockedDataSyncS2CPacket(isManaRegenBlocked), player);
		}
	}

	public int getManaRegenBlocked() {
		return isManaRegenBlocked;
	}

	public void copyFrom(PlayerMana source) {
		mana = source.mana;
		max_mana = source.max_mana;
		isManaRegenBlocked = source.isManaRegenBlocked;
	}

	public void saveNBTData (CompoundTag nbt) {
		nbt.putInt("rpgworld_mana", mana);
		nbt.putInt("rpgworld_max_mana", max_mana);
		nbt.putInt("rpgworld_mana_regen_blocked", isManaRegenBlocked);
	}

	public void loadNBTData (CompoundTag nbt) {
		mana = nbt.getInt("rpgworld_mana");
		max_mana = nbt.getInt("rpgworld_max_mana");
		isManaRegenBlocked = nbt.getInt("rpgworld_mana_regen_blocked");
	}

}
