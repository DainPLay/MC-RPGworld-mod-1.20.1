package net.dainplay.rpgworldmod.network;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PlayerSculkStaffCDProvider implements ICapabilityProvider, INBTSerializable<CompoundTag> {
	public static Capability<PlayerSculkStaffCD> PLAYER_SCULK_STAFF_COOLDOWN = CapabilityManager.get(new CapabilityToken<PlayerSculkStaffCD>() {});

	private PlayerSculkStaffCD cooldown = null;
	private final LazyOptional<PlayerSculkStaffCD> optional = LazyOptional.of(this::createPlayerSculkStaffCD);

	private PlayerSculkStaffCD createPlayerSculkStaffCD() {
		if (this.cooldown == null) {
			this.cooldown = new PlayerSculkStaffCD();
		}
		return this.cooldown;
	}

	@Override
	public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
		if (cap == PLAYER_SCULK_STAFF_COOLDOWN) {
			return optional.cast();
		}
		return LazyOptional.empty();
	}

	@Override
	public CompoundTag serializeNBT() {
		CompoundTag nbt = new CompoundTag();
		createPlayerSculkStaffCD().saveNBTData(nbt);
		return nbt;
	}

	@Override
	public void deserializeNBT(CompoundTag nbt) {
		createPlayerSculkStaffCD().loadNBTData(nbt);
	}
}
