package net.dainplay.rpgworldmod.loot;

import net.dainplay.rpgworldmod.RPGworldMod;
import net.dainplay.rpgworldmod.biome.BiomeRegistry;
import net.dainplay.rpgworldmod.enchantment.ModEnchantments;
import net.dainplay.rpgworldmod.item.ModItems;
import net.dainplay.rpgworldmod.world.RPGLootTables;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.LocationPredicate;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
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
		add("nether_star_scroll_pickaxe", new NetherStarScrollPickaxeModifier(
				new LootItemCondition[]{
						MatchTool.toolMatches(ItemPredicate.Builder.item().of(ModItems.NETHER_STAR_SCROLL.get())).build()
				}
		));
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

		add("pillager_scrolls_in_woodland_mansion", new AddEnchantedScrollModifier(
				new LootItemCondition[]{
						new LootTableIdCondition.Builder(new ResourceLocation("chests/woodland_mansion")).build(),
						LootItemRandomChanceCondition.randomChance(0.5F).build()
				},
				ModItems.PILLAGER_SCROLL.get(),
				1,
				1,
				0.1F,
				List.of(
						ModEnchantments.DESTRUCTION.getId(),
						ModEnchantments.RESTORATION.getId(),
						ModEnchantments.ALTERATION.getId(),
						ModEnchantments.ILLUSION.getId(),
						ModEnchantments.CONJURATION.getId(),
						ModEnchantments.NECROMANCY.getId()
				)
		));

		add("scroll_from_evoker", new ReplaceItemChanceModifier(
				new LootItemCondition[]{
						new LootTableIdCondition.Builder(new ResourceLocation("entities/evoker")).build()
				},
				Items.TOTEM_OF_UNDYING,
				ModItems.PILLAGER_SCROLL.get(),
				0.25F,
				0.05F
		));

		add("golden_dagger_in_bastion_bridge", new AddDaggerModifier(
				new LootItemCondition[]{
						new LootTableIdCondition.Builder(new ResourceLocation("chests/bastion_bridge")).build()
				},
				ModItems.GOLDEN_DAGGER.get(),
				0.112F,
				0.25F,
				0,
				0.0F,
				false
		));

		add("golden_dagger_in_bastion_other", new AddDaggerModifier(
				new LootItemCondition[]{
						new LootTableIdCondition.Builder(new ResourceLocation("chests/bastion_other")).build()
				},
				ModItems.GOLDEN_DAGGER.get(),
				0.098F,
				0.25F,
				0,
				0.0F,
				false
		));

		add("enchanted_iron_dagger_in_bastion_other", new AddDaggerModifier(
				new LootItemCondition[]{
						new LootTableIdCondition.Builder(new ResourceLocation("chests/bastion_other")).build()
				},
				ModItems.IRON_DAGGER.get(),
				0.152F,
				0.25F,
				15,
				1.0F,
				true
		));

		add("diamond_dagger_in_bastion_hoglin_stable", new AddDaggerModifier(
				new LootItemCondition[]{
						new LootTableIdCondition.Builder(new ResourceLocation("chests/bastion_hoglin_stable")).build()
				},
				ModItems.DIAMOND_DAGGER.get(),
				0.12F,
				0F,
				15,
				1.0F,
				false
		));

		add("diamond_dagger_in_bastion_treasure", new AddDaggerModifier(
				new LootItemCondition[]{
						new LootTableIdCondition.Builder(new ResourceLocation("chests/bastion_treasure")).build()
				},
				ModItems.DIAMOND_DAGGER.get(),
				0.152F,
				0.25F,
				0,
				0.0F,
				false
		));

		add("enchanted_diamond_dagger_in_bastion_treasure", new AddDaggerModifier(
				new LootItemCondition[]{
						new LootTableIdCondition.Builder(new ResourceLocation("chests/bastion_treasure")).build()
				},
				ModItems.DIAMOND_DAGGER.get(),
				0.152F,
				0.25F,
				15,
				1.0F,
				true
		));

		add("golden_dagger_in_ruined_portal", new AddDaggerModifier(
				new LootItemCondition[]{
						new LootTableIdCondition.Builder(new ResourceLocation("chests/ruined_portal")).build()
				},
				ModItems.GOLDEN_DAGGER.get(),
				0.205F,
				0F,
				30,
				1.0F,
				false
		));

		add("iron_dagger_in_buried_treasure", new AddDaggerModifier(
				new LootItemCondition[]{
						new LootTableIdCondition.Builder(new ResourceLocation("chests/buried_treasure")).build()
				},
				ModItems.IRON_DAGGER.get(),
				0.167F,
				0.25F,
				0,
				0F,
				false
		));

		add("iron_dagger_in_stronghold_corridor", new AddDaggerModifier(
				new LootItemCondition[]{
						new LootTableIdCondition.Builder(new ResourceLocation("chests/stronghold_corridor")).build()
				},
				ModItems.IRON_DAGGER.get(),
				0.119F,
				0.25F,
				0,
				0F,
				false
		));

		add("iron_dagger_in_village_weaponsmith", new AddDaggerModifier(
				new LootItemCondition[]{
						new LootTableIdCondition.Builder(new ResourceLocation("chests/village/village_weaponsmith")).build()
				},
				ModItems.IRON_DAGGER.get(),
				0.229F,
				0F,
				0,
				0F,
				false
		));

		add("iron_dagger_in_end_city_treasure", new AddDaggerModifier(
				new LootItemCondition[]{
						new LootTableIdCondition.Builder(new ResourceLocation("chests/end_city_treasure")).build()
				},
				ModItems.IRON_DAGGER.get(),
				0.127F,
				0.25F,
				30,
				1F,
				false
		));

		add("diamond_dagger_in_end_city_treasure", new AddDaggerModifier(
				new LootItemCondition[]{
						new LootTableIdCondition.Builder(new ResourceLocation("chests/end_city_treasure")).build()
				},
				ModItems.DIAMOND_DAGGER.get(),
				0.127F,
				0.25F,
				30,
				1F,
				false
		));
	}

}