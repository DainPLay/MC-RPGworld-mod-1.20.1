package net.dainplay.rpgworldmod.network;

import net.dainplay.rpgworldmod.data.tags.ModAdvancements;
import net.dainplay.rpgworldmod.effect.ModEffects;
import net.dainplay.rpgworldmod.item.ModItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;

public class PlayerSculkStaffCD {
	private int cooldown;

	public int getCooldown() {
		return cooldown;
	}

	public void setCooldown(ServerPlayer player, int amount) {
		cooldown = amount;
	}

	public void copyFrom(PlayerSculkStaffCD source) {
		cooldown = source.cooldown;
	}

	public void saveNBTData (CompoundTag nbt) {
		nbt.putInt("rpgworld_sculk_staff_cooldown", cooldown);
	}

	public void loadNBTData (CompoundTag nbt) {
		cooldown = nbt.getInt("rpgworld_sculk_staff_cooldown");
	}

}
