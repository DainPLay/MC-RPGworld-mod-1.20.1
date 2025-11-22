package net.dainplay.rpgworldmod.loot;

import net.dainplay.rpgworldmod.RPGworldMod;
import net.dainplay.rpgworldmod.biome.BiomeRegistry;
import net.dainplay.rpgworldmod.item.ModItems;
import net.dainplay.rpgworldmod.world.RPGLootTables;
import net.minecraft.advancements.critereon.LocationPredicate;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.*;
import net.minecraftforge.common.data.GlobalLootModifierProvider;
import net.minecraftforge.common.loot.LootTableIdCondition;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;

public class ModGlobalLootModifiersProvider extends GlobalLootModifierProvider {
    public ModGlobalLootModifiersProvider(PackOutput output) {
        super(output, RPGworldMod.MOD_ID);
    }

    @Override
    protected void start() {
        add("rie_weald_fog_fish", new ReplaceItemModifier(
                new LootItemCondition[] {
                        LocationCheck.checkLocation(LocationPredicate.Builder.location().setBiome(BiomeRegistry.RIE_WEALD)).build(),
                        WeatherCheck.weather().setRaining(true).build(),
                        new LootTableIdCondition.Builder(BuiltInLootTables.FISHING).build()}, RPGLootTables.RIE_WEALD_FOG_FISHING));
        add("rie_weald_fog_fish1", new ReplaceItemModifier(
                new LootItemCondition[] {
                        LocationCheck.checkLocation(LocationPredicate.Builder.location().setBiome(BiomeRegistry.RIE_WEALD)).build(),
                        WeatherCheck.weather().setThundering(true).build(),
                        new LootTableIdCondition.Builder(BuiltInLootTables.FISHING).build()}, RPGLootTables.RIE_WEALD_FOG_FISHING));
        add("rie_weald_fish", new ReplaceItemModifier(
                new LootItemCondition[] {
                        LocationCheck.checkLocation(LocationPredicate.Builder.location().setBiome(BiomeRegistry.RIE_WEALD)).build(),
                        WeatherCheck.weather().setRaining(false).setThundering(false).build(),
                        new LootTableIdCondition.Builder(BuiltInLootTables.FISHING).build()}, RPGLootTables.RIE_WEALD_FISHING));
    }

}