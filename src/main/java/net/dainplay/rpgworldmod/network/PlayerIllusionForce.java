package net.dainplay.rpgworldmod.network;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;

public class PlayerIllusionForce {
    private int illusionForce = 0;
    private float entPosX = 0.0f;
    private float entPosY = 0.0f;
    private float entPosZ = 0.0f;
    private boolean isEnt = false;

    public int getIllusionForce() {
        return illusionForce;
    }

    public void setIllusionForce(ServerPlayer player, int newForce, boolean isSet, boolean ent) {
        if (this.illusionForce != newForce) {
            this.illusionForce = newForce;
            ModMessages.sendToPlayer(new IllusionForceDataSyncS2CPacket(illusionForce, entPosX, entPosY, entPosZ, isSet, ent), player);
        }
    }

    public float getEntPosX() {
        return entPosX;
    }

    public float getEntPosY() {
        return entPosY;
    }

    public float getEntPosZ() {
        return entPosZ;
    }

    public boolean getIsEnt() {
        return isEnt;
    }

    public void setEntPosition(ServerPlayer player, boolean hasPosition, float x, float y, float z, boolean ent) {
        if (this.isEnt != ent || this.entPosX != x || this.entPosY != y || this.entPosZ != z) {
            this.isEnt = ent;
            this.entPosX = x;
            this.entPosY = y;
            this.entPosZ = z;
            ModMessages.sendToPlayer(new IllusionForceDataSyncS2CPacket(illusionForce, entPosX, entPosY, entPosZ, hasPosition, ent), player);
        }
    }

    public void clearEntPosition(ServerPlayer player) {
        setEntPosition(player, false, 0.0f, 0.0f, 0.0f, false);
    }

    public void copyFrom(PlayerIllusionForce source) {
        this.illusionForce = source.illusionForce;
        this.isEnt = source.isEnt;
        this.entPosX = source.entPosX;
        this.entPosY = source.entPosY;
        this.entPosZ = source.entPosZ;
    }

    public void saveNBTData(CompoundTag nbt) {
        nbt.putInt("illusion_force", illusionForce);
        nbt.putBoolean("is_ent", isEnt);
            nbt.putFloat("ent_pos_x", entPosX);
            nbt.putFloat("ent_pos_y", entPosY);
            nbt.putFloat("ent_pos_z", entPosZ);
    }

    public void loadNBTData(CompoundTag nbt) {
        illusionForce = nbt.getInt("illusion_force");
        isEnt = nbt.getBoolean("is_ent");
            entPosX = nbt.getFloat("ent_pos_x");
            entPosY = nbt.getFloat("ent_pos_y");
            entPosZ = nbt.getFloat("ent_pos_z");
    }
}