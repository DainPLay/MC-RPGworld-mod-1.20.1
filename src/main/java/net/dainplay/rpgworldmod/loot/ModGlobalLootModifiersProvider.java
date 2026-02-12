package net.dainplay.rpgworldmod.loot;

import net.dainplay.rpgworldmod.RPGworldMod;
import net.dainplay.rpgworldmod.biome.BiomeRegistry;
import net.dainplay.rpgworldmod.enchantment.ModEnchantments;
import net.dainplay.rpgworldmod.item.ModItems;
import net.dainplay.rpgworldmod.world.RPGLootTables;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.LocationPredicate;
import net.minecraft.advancements.critereon.LootTableTrigger;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.predicates.LocationCheck;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.predicates.MatchTool;
import net.minecraft.world.level.storage.loot.predicates.WeatherCheck;
import net.minecraftforge.common.data.GlobalLootModifierProvider;
import net.minecraftforge.common.loot.LootTableIdCondition;

import java.util.List;

public class ModGlobalLootModifiersProvider extends GlobalLootModifierProvider {
	public ModGlobalLootModifiersProvider(PackOutput output) {
		super(output, RPGworldMod.MOD_ID);
	}

	@Override
	protected void start() {
		add("rie_weald_fog_fish", new ReplaceItemModifier(
				new LootItemCondition[]{
						LocationCheck.checkLocation(LocationPredicate.Builder.location().setBiome(BiomeRegistry.RIE_WEALD)).build(),
						WeatherCheck.weather().setRaining(true).build(),
						new LootTableIdCondition.Builder(BuiltInLootTables.FISHING).build()}, RPGLootTables.RIE_WEALD_FOG_FISHING));
		add("rie_weald_fog_fish1", new ReplaceItemModifier(
				new LootItemCondition[]{
						LocationCheck.checkLocation(LocationPredicate.Builder.location().setBiome(BiomeRegistry.RIE_WEALD)).build(),
						WeatherCheck.weather().setThundering(true).build(),
						new LootTableIdCondition.Builder(BuiltInLootTables.FISHING).build()}, RPGLootTables.RIE_WEALD_FOG_FISHING));
		add("rie_weald_fish", new ReplaceItemModifier(
				new LootItemCondition[]{
						LocationCheck.checkLocation(LocationPredicate.Builder.location().setBiome(BiomeRegistry.RIE_WEALD)).build(),
						WeatherCheck.weather().setRaining(false).setThundering(false).build(),
						new LootTableIdCondition.Builder(BuiltInLootTables.FISHING).build()}, RPGLootTables.RIE_WEALD_FISHING));
		add("tool_smelting", new SmeltingModifier(new LootItemCondition[]{MatchTool.toolMatches(ItemPredicate.Builder.item().of(ModItems.FLINT_PICKAXE.get(), ModItems.FLINT_SHOVEL.get(), ModItems.FLINT_SWORD.get(), ModItems.FLINT_AXE.get(), ModItems.FLINT_HOE.get())).build()}));
		add("scrolls_in_library", new AddEnchantedScrollModifier(
				new LootItemCondition[]{
						new LootTableIdCondition.Builder(new ResourceLocation("chests/stronghold_library")).build()
				},
				ModItems.EMPTY_SCROLL.get(),
				1,
				2,
				0.5F,
				List.of(
						ModEnchantments.DESTRUCTION.getId(),
						ModEnchantments.RESTORATION.getId(),
						ModEnchantments.ALTERATION.getId(),
						ModEnchantments.ILLUSION.getId(),
						ModEnchantments.CONJURATION.getId()
				)
		));

		add("necromancy_scrolls_in_ancient_city", new AddEnchantedScrollModifier(
				new LootItemCondition[]{
						new LootTableIdCondition.Builder(new ResourceLocation("chests/ancient_city")).build(),
						LootItemRandomChanceCondition.randomChance(0.5F).build()
				},
				ModItems.EMPTY_SCROLL.get(),
				1,
				1,
				1.0F,
				List.of(ModEnchantments.NECROMANCY.getId())
		));
	}

}