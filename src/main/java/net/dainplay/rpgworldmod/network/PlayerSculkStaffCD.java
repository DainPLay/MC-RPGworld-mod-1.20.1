package net.dainplay.rpgworldmod.network;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;

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

	public void saveNBTData(CompoundTag nbt) {
		nbt.putInt("rpgworld_sculk_staff_cooldown", cooldown);
	}

	public void loadNBTData(CompoundTag nbt) {
		cooldown = nbt.getInt("rpgworld_sculk_staff_cooldown");
	}

}
