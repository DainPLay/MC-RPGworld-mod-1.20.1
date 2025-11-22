package net.dainplay.rpgworldmod.world;

import com.google.common.collect.Sets;
import net.dainplay.rpgworldmod.RPGworldMod;
import net.minecraft.resources.ResourceLocation;

import java.util.Collections;
import java.util.Set;

public class RPGLootTables {
    private static final Set<ResourceLocation> RPG_LOOT_TABLES = Sets.newHashSet();
    public static final ResourceLocation RIE_HOLLOW = register("rie_hollow");
    public static final ResourceLocation BIBBIT1 = register("bibbit_1");
    public static final ResourceLocation BIBBIT2 = register("bibbit_2");
    public static final ResourceLocation BIBBIT3 = register("bibbit_3");
    public static final ResourceLocation RIE_WEALD_FISHING = register("rie_weald_fishing");
    public static final ResourceLocation RIE_WEALD_FOG_FISHING = register("rie_weald_fog_fishing");
    public static final ResourceLocation RIE_WEALD_FISH = register("rie_weald_fish");
    public static final ResourceLocation RIE_WEALD_FOG_FISH = register("rie_weald_fog_fish");
    public static final ResourceLocation RIE_WEALD_TREASURE = register("rie_weald_treasure");
    public static final ResourceLocation RIE_WEALD_JUNK = register("rie_weald_junk");


    public final ResourceLocation lootTable;

    private RPGLootTables(String path) {
        this.lootTable = RPGworldMod.prefix(path);
    }

    private static ResourceLocation register(String id) {
        return register(RPGworldMod.prefix(id));
    }

    private static ResourceLocation register(ResourceLocation id) {
        if (RPG_LOOT_TABLES.add(id)) {
            return id;
        } else {
            throw new IllegalArgumentException(id + " is already a registered built-in loot table");
        }
    }

}
