package net.dainplay.rpgworldmod.network;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;

public class PlayerIllusionForce {
    private int illusionForce = 0;
    private BlockPos entPosition = null;

    public int getIllusionForce() {
        return illusionForce;
    }

    public void setIllusionForce(ServerPlayer player, int newForce) {
        if (this.illusionForce != newForce) {
            this.illusionForce = newForce;
            ModMessages.sendToPlayer(new IllusionForceDataSyncS2CPacket(illusionForce, entPosition), player);
        }
    }

    public BlockPos getEntPosition() {
        return entPosition;
    }

    public void setEntPosition(ServerPlayer player, BlockPos newPos) {
        if (this.entPosition != newPos) {
            this.entPosition = newPos;
            ModMessages.sendToPlayer(new IllusionForceDataSyncS2CPacket(illusionForce, entPosition), player);
        }
        this.entPosition = entPosition;
    }

    public void copyFrom(PlayerIllusionForce source) {
        this.illusionForce = source.illusionForce;
        this.entPosition = source.entPosition;
    }

    public void saveNBTData(CompoundTag nbt) {
        nbt.putInt("illusion_force", illusionForce);
        if (entPosition != null) {
            nbt.putLong("ent_position", entPosition.asLong());
        }
    }

    public void loadNBTData(CompoundTag nbt) {
        illusionForce = nbt.getInt("illusion_force");
        if (nbt.contains("ent_position")) {
            entPosition = BlockPos.of(nbt.getLong("ent_position"));
        }
    }
}