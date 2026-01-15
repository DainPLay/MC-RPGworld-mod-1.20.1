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

public class PlayerIllusionForceProvider implements ICapabilityProvider, INBTSerializable<CompoundTag> {
    public static Capability<PlayerIllusionForce> PLAYER_ILLUSION_FORCE = 
        CapabilityManager.get(new CapabilityToken<PlayerIllusionForce>() {});

    private PlayerIllusionForce illusionForce = null;
    private final LazyOptional<PlayerIllusionForce> optional = LazyOptional.of(this::createPlayerIllusionForce);

    private PlayerIllusionForce createPlayerIllusionForce() {
        if (this.illusionForce == null) {
            this.illusionForce = new PlayerIllusionForce();
        }
        return this.illusionForce;
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == PLAYER_ILLUSION_FORCE) {
            return optional.cast();
        }
        return LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();
        createPlayerIllusionForce().saveNBTData(nbt);
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        createPlayerIllusionForce().loadNBTData(nbt);
    }
}