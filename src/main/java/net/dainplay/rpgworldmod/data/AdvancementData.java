package net.dainplay.rpgworldmod.data;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;

public class AdvancementData extends SavedData {
    private static final String DATA_NAME = "rpgworldmod_advancement_data";
    private boolean rieWealdFound = false;

    public boolean isRieWealdFound() {
        return rieWealdFound;
    }

    public void setRieWealdFound(boolean found) {
        this.rieWealdFound = found;
        this.setDirty();
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        tag.putBoolean("rie_weald_found", rieWealdFound);
        return tag;
    }

    public static AdvancementData load(CompoundTag tag) {
        AdvancementData data = new AdvancementData();
        data.rieWealdFound = tag.getBoolean("rie_weald_found");
        return data;
    }

    public static AdvancementData get(MinecraftServer server) {
        return server.overworld().getDataStorage()
                .computeIfAbsent(AdvancementData::load, AdvancementData::new, DATA_NAME);
    }
}