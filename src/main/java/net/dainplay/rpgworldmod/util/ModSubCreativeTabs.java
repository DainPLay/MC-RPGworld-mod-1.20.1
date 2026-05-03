package net.dainplay.rpgworldmod.util;

import net.dainplay.rpgworldmod.block.ModBlocks;
import net.dainplay.rpgworldmod.enchantment.ModEnchantments;
import net.dainplay.rpgworldmod.item.ModItems;
import net.dainplay.rpgworldmod.item.custom.EmberScrollItem;
import net.dainplay.rpgworldmod.item.custom.EnderEyeScrollItem;
import net.dainplay.rpgworldmod.item.custom.HeartOfTheSeaScrollItem;
import net.dainplay.rpgworldmod.item.custom.NetherStarScrollItem;
import net.dainplay.rpgworldmod.item.custom.PillagerScrollItem;
import net.dainplay.rpgworldmod.item.custom.StaffItem;
import net.dainplay.rpgworldmod.potion.ModPotions;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ModSubCreativeTabs {
	private static final Map<SubCreativeTabs, Boolean> FILTERS = new LinkedHashMap<>();

	public static final SubCreativeTabs MELEE = register(
			SubCreativeTabs.builder()
					.title(Component.translatable("subcreativetab.rpgworldmod.melee"))
					.displayItems((provider, output) -> {
						output.accept(ModItems.WOODEN_DAGGER.get());
						output.accept(ModItems.STONE_DAGGER.get());
						output.accept(ModItems.FLINT_DAGGER.get());
						output.accept(ModItems.MASKONITE_DAGGER.get());
						output.accept(ModItems.IRON_DAGGER.get());
						output.accept(ModItems.GOLDEN_DAGGER.get());
						output.accept(ModItems.DIAMOND_DAGGER.get());
						output.accept(ModItems.NETHERITE_DAGGER.get());
						output.accept(ItemStack.EMPTY);
						output.accept(ModItems.FLINT_SWORD.get());
						output.accept(ModItems.MASKONITE_SWORD.get());
						output.accept(ModItems.WEALD_BLADE.get());
						output.accept(ModItems.FAIRAPIER_SWORD.get());
						output.accept(ModItems.FLINT_AXE.get());
						output.accept(ModItems.MASKONITE_AXE.get());
						output.accept(ModItems.GUITAR_AX.get());
						output.accept(ItemStack.EMPTY);
						output.accept(ItemStack.EMPTY);

						List<Item> daggers = List.of(
								ModItems.WOODEN_DAGGER.get(),
								ModItems.STONE_DAGGER.get(),
								ModItems.FLINT_DAGGER.get(),
								ModItems.MASKONITE_DAGGER.get(),
								ModItems.IRON_DAGGER.get(),
								ModItems.GOLDEN_DAGGER.get(),
								ModItems.DIAMOND_DAGGER.get(),
								ModItems.NETHERITE_DAGGER.get()
						);

						List<Potion> potions = ForgeRegistries.POTIONS.getValues()
								.stream()
								.filter(p -> p != Potions.EMPTY)
								.collect(Collectors.toList());

						for (Item dagger : daggers) {
							for (Potion potion : potions) {
								ItemStack stack = new ItemStack(dagger);
								PotionUtils.setPotion(stack, potion);
								output.accept(stack);
							}
						}
					})
					.build()
	);

	public static final SubCreativeTabs RANGED = register(
			SubCreativeTabs.builder()
					.title(Component.translatable("subcreativetab.rpgworldmod.ranged"))
					.displayItems((provider, output) -> {
						output.accept(ModItems.FAIRAPIER_SEED.get());
						output.accept(ModItems.PROJECTRUFFLE_ITEM.get());
						output.accept(ModItems.LIVING_WOOD_BOW.get());
						output.accept(ModItems.MINTAL_TRIANGLE.get());
						output.accept(ModItems.DRILL_SPEAR.get());
						output.accept(PotionUtils.setPotion(new ItemStack(Items.TIPPED_ARROW), ModPotions.PARALYSIS_POTION.get()));
						output.accept(PotionUtils.setPotion(new ItemStack(Items.TIPPED_ARROW), ModPotions.LONG_PARALYSIS_POTION.get()));
						output.accept(PotionUtils.setPotion(new ItemStack(Items.TIPPED_ARROW), ModPotions.STRONG_PARALYSIS_POTION.get()));
						output.accept(PotionUtils.setPotion(new ItemStack(Items.TIPPED_ARROW), ModPotions.MOSSIOSIS_POTION.get()));
						output.accept(PotionUtils.setPotion(new ItemStack(Items.TIPPED_ARROW), ModPotions.LONG_MOSSIOSIS_POTION.get()));
						output.accept(PotionUtils.setPotion(new ItemStack(Items.TIPPED_ARROW), ModPotions.STRONG_MOSSIOSIS_POTION.get()));
						output.accept(PotionUtils.setPotion(new ItemStack(Items.TIPPED_ARROW), ModPotions.ARBOR_FUEL_BOTTLE.get()));
						output.accept(PotionUtils.setPotion(new ItemStack(Items.TIPPED_ARROW), ModPotions.LONG_ARBOR_FUEL_BOTTLE.get()));
						output.accept(PotionUtils.setPotion(new ItemStack(Items.TIPPED_ARROW), ModPotions.PARANOIA_POTION.get()));
						output.accept(PotionUtils.setPotion(new ItemStack(Items.TIPPED_ARROW), ModPotions.LONG_PARANOIA_POTION.get()));
						output.accept(PotionUtils.setPotion(new ItemStack(Items.TIPPED_ARROW), ModPotions.STRONG_PARANOIA_POTION.get()));
					})
					.build()
	);

	public static final SubCreativeTabs MAGIC = register(
			SubCreativeTabs.builder()
					.title(Component.translatable("subcreativetab.rpgworldmod.magic"))
					.displayItems((provider, output) -> {
						output.accept(ModItems.EMBER_GEM.get());
						output.accept(ItemStack.EMPTY);
						output.accept(EmberScrollItem.createForEnchantment(new EnchantmentInstance(ModEnchantments.RESTORATION.get(), ModEnchantments.RESTORATION.get().getMaxLevel())));
						output.accept(EmberScrollItem.createForEnchantment(new EnchantmentInstance(ModEnchantments.DESTRUCTION.get(), ModEnchantments.DESTRUCTION.get().getMaxLevel())));
						output.accept(EmberScrollItem.createForEnchantment(new EnchantmentInstance(ModEnchantments.ILLUSION.get(), ModEnchantments.ILLUSION.get().getMaxLevel())));
						output.accept(EmberScrollItem.createForEnchantment(new EnchantmentInstance(ModEnchantments.ALTERATION.get(), ModEnchantments.ALTERATION.get().getMaxLevel())));
						output.accept(EmberScrollItem.createForEnchantment(new EnchantmentInstance(ModEnchantments.CONJURATION.get(), ModEnchantments.CONJURATION.get().getMaxLevel())));
						output.accept(EmberScrollItem.createForEnchantment(new EnchantmentInstance(ModEnchantments.NECROMANCY.get(), ModEnchantments.NECROMANCY.get().getMaxLevel())));
						output.accept(ItemStack.EMPTY);
						output.accept(Items.HEART_OF_THE_SEA);
						output.accept(ItemStack.EMPTY);
						output.accept(HeartOfTheSeaScrollItem.createForEnchantment(new EnchantmentInstance(ModEnchantments.RESTORATION.get(), ModEnchantments.RESTORATION.get().getMaxLevel())));
						output.accept(HeartOfTheSeaScrollItem.createForEnchantment(new EnchantmentInstance(ModEnchantments.DESTRUCTION.get(), ModEnchantments.DESTRUCTION.get().getMaxLevel())));
						output.accept(HeartOfTheSeaScrollItem.createForEnchantment(new EnchantmentInstance(ModEnchantments.ILLUSION.get(), ModEnchantments.ILLUSION.get().getMaxLevel())));
						output.accept(HeartOfTheSeaScrollItem.createForEnchantment(new EnchantmentInstance(ModEnchantments.ALTERATION.get(), ModEnchantments.ALTERATION.get().getMaxLevel())));
						output.accept(HeartOfTheSeaScrollItem.createForEnchantment(new EnchantmentInstance(ModEnchantments.CONJURATION.get(), ModEnchantments.CONJURATION.get().getMaxLevel())));
						output.accept(HeartOfTheSeaScrollItem.createForEnchantment(new EnchantmentInstance(ModEnchantments.NECROMANCY.get(), ModEnchantments.NECROMANCY.get().getMaxLevel())));
						output.accept(ItemStack.EMPTY);
						output.accept(Items.ENDER_EYE);
						output.accept(ItemStack.EMPTY);
						output.accept(EnderEyeScrollItem.createForEnchantment(new EnchantmentInstance(ModEnchantments.RESTORATION.get(), ModEnchantments.RESTORATION.get().getMaxLevel())));
						output.accept(EnderEyeScrollItem.createForEnchantment(new EnchantmentInstance(ModEnchantments.DESTRUCTION.get(), ModEnchantments.DESTRUCTION.get().getMaxLevel())));
						output.accept(EnderEyeScrollItem.createForEnchantment(new EnchantmentInstance(ModEnchantments.ILLUSION.get(), ModEnchantments.ILLUSION.get().getMaxLevel())));
						output.accept(EnderEyeScrollItem.createForEnchantment(new EnchantmentInstance(ModEnchantments.ALTERATION.get(), ModEnchantments.ALTERATION.get().getMaxLevel())));
						output.accept(EnderEyeScrollItem.createForEnchantment(new EnchantmentInstance(ModEnchantments.CONJURATION.get(), ModEnchantments.CONJURATION.get().getMaxLevel())));
						output.accept(EnderEyeScrollItem.createForEnchantment(new EnchantmentInstance(ModEnchantments.NECROMANCY.get(), ModEnchantments.NECROMANCY.get().getMaxLevel())));
						output.accept(ItemStack.EMPTY);
						output.accept(ItemStack.EMPTY);
						output.accept(ItemStack.EMPTY);
						output.accept(PillagerScrollItem.createForEnchantment(new EnchantmentInstance(ModEnchantments.RESTORATION.get(), ModEnchantments.RESTORATION.get().getMaxLevel())));
						output.accept(PillagerScrollItem.createForEnchantment(new EnchantmentInstance(ModEnchantments.DESTRUCTION.get(), ModEnchantments.DESTRUCTION.get().getMaxLevel())));
						output.accept(PillagerScrollItem.createForEnchantment(new EnchantmentInstance(ModEnchantments.ILLUSION.get(), ModEnchantments.ILLUSION.get().getMaxLevel())));
						output.accept(PillagerScrollItem.createForEnchantment(new EnchantmentInstance(ModEnchantments.ALTERATION.get(), ModEnchantments.ALTERATION.get().getMaxLevel())));
						output.accept(PillagerScrollItem.createForEnchantment(new EnchantmentInstance(ModEnchantments.CONJURATION.get(), ModEnchantments.CONJURATION.get().getMaxLevel())));
						output.accept(PillagerScrollItem.createForEnchantment(new EnchantmentInstance(ModEnchantments.NECROMANCY.get(), ModEnchantments.NECROMANCY.get().getMaxLevel())));
						output.accept(ItemStack.EMPTY);
						output.accept(Items.NETHER_STAR);
						output.accept(ItemStack.EMPTY);
						output.accept(NetherStarScrollItem.createForEnchantment(new EnchantmentInstance(ModEnchantments.RESTORATION.get(), ModEnchantments.RESTORATION.get().getMaxLevel())));
						output.accept(NetherStarScrollItem.createForEnchantment(new EnchantmentInstance(ModEnchantments.DESTRUCTION.get(), ModEnchantments.DESTRUCTION.get().getMaxLevel())));
						output.accept(NetherStarScrollItem.createForEnchantment(new EnchantmentInstance(ModEnchantments.ILLUSION.get(), ModEnchantments.ILLUSION.get().getMaxLevel())));
						output.accept(NetherStarScrollItem.createForEnchantment(new EnchantmentInstance(ModEnchantments.ALTERATION.get(), ModEnchantments.ALTERATION.get().getMaxLevel())));
						output.accept(NetherStarScrollItem.createForEnchantment(new EnchantmentInstance(ModEnchantments.CONJURATION.get(), ModEnchantments.CONJURATION.get().getMaxLevel())));
						output.accept(NetherStarScrollItem.createForEnchantment(new EnchantmentInstance(ModEnchantments.NECROMANCY.get(), ModEnchantments.NECROMANCY.get().getMaxLevel())));
					})
					.build()
	);

	public static final SubCreativeTabs ARTIFACTS = register(
			SubCreativeTabs.builder()
					.title(Component.translatable("subcreativetab.rpgworldmod.artifacts"))
					.displayItems((provider, output) -> {
						output.accept(ModItems.LAPIS_CHARM.get());
						output.accept(ModItems.CHITIN_THIMBLE.get());
						output.accept(ModItems.CHITIN_POWDER.get());
					})
					.build()
	);

	public static final SubCreativeTabs ARMOR = register(
			SubCreativeTabs.builder()
					.title(Component.translatable("subcreativetab.rpgworldmod.armor"))
					.displayItems((provider, output) -> {
						output.accept(ModItems.FIREPROOF_SKIRT.get());
						output.accept(ModItems.LIVING_WOOD_HELMET.get());
						output.accept(ModItems.LIVING_WOOD_CHESTPLATE.get());
						output.accept(ModItems.LIVING_WOOD_LEGGINGS.get());
						output.accept(ModItems.LIVING_WOOD_BOOTS.get());
					})
					.build()
	);

	public static final SubCreativeTabs TOOLS = register(
			SubCreativeTabs.builder()
					.title(Component.translatable("subcreativetab.rpgworldmod.tools"))
					.displayItems((provider, output) -> {
						output.accept(ModItems.MASKONITE_SHOVEL.get());
						output.accept(ModItems.MASKONITE_PICKAXE.get());
						output.accept(ModItems.MASKONITE_AXE.get());
						output.accept(ModItems.MASKONITE_HOE.get());
						output.accept(ModItems.FLINT_SHOVEL.get());
						output.accept(ModItems.FLINT_PICKAXE.get());
						output.accept(ModItems.FLINT_AXE.get());
						output.accept(ModItems.FLINT_HOE.get());
						output.accept(ModItems.GUITAR_AX.get());
						output.accept(ModItems.DRILL_SPEAR.get());
						output.accept(ModBlocks.LIVING_WOOD_LOG.get());
						output.accept(Items.BLAZE_ROD);
						output.accept(Blocks.TUBE_CORAL_BLOCK);
						output.accept(Blocks.BRAIN_CORAL_BLOCK);
						output.accept(Blocks.BUBBLE_CORAL_BLOCK);
						output.accept(Blocks.FIRE_CORAL_BLOCK);
						output.accept(Blocks.HORN_CORAL_BLOCK);
						output.accept(Blocks.SCULK_SENSOR);
						output.accept(ModItems.EMBER_GEM.get());
						output.accept(StaffItem.createForGemType(ModItems.LIVING_WOOD_STAFF.get().getDefaultInstance(), StaffItem.GemType.EMBER_GEM));
						output.accept(StaffItem.createForGemType(ModItems.BLAZE_STAFF.get().getDefaultInstance(), StaffItem.GemType.EMBER_GEM));
						output.accept(StaffItem.createForGemType(ModItems.TUBE_CORAL_STAFF.get().getDefaultInstance(), StaffItem.GemType.EMBER_GEM));
						output.accept(StaffItem.createForGemType(ModItems.BRAIN_CORAL_STAFF.get().getDefaultInstance(), StaffItem.GemType.EMBER_GEM));
						output.accept(StaffItem.createForGemType(ModItems.BUBBLE_CORAL_STAFF.get().getDefaultInstance(), StaffItem.GemType.EMBER_GEM));
						output.accept(StaffItem.createForGemType(ModItems.FIRE_CORAL_STAFF.get().getDefaultInstance(), StaffItem.GemType.EMBER_GEM));
						output.accept(StaffItem.createForGemType(ModItems.HORN_CORAL_STAFF.get().getDefaultInstance(), StaffItem.GemType.EMBER_GEM));
						output.accept(StaffItem.createForGemType(ModItems.SCULK_STAFF.get().getDefaultInstance(), StaffItem.GemType.EMBER_GEM));
						output.accept(Items.HEART_OF_THE_SEA);
						output.accept(StaffItem.createForGemType(ModItems.LIVING_WOOD_STAFF.get().getDefaultInstance(), StaffItem.GemType.HEART_OF_THE_SEA));
						output.accept(StaffItem.createForGemType(ModItems.BLAZE_STAFF.get().getDefaultInstance(), StaffItem.GemType.HEART_OF_THE_SEA));
						output.accept(StaffItem.createForGemType(ModItems.TUBE_CORAL_STAFF.get().getDefaultInstance(), StaffItem.GemType.HEART_OF_THE_SEA));
						output.accept(StaffItem.createForGemType(ModItems.BRAIN_CORAL_STAFF.get().getDefaultInstance(), StaffItem.GemType.HEART_OF_THE_SEA));
						output.accept(StaffItem.createForGemType(ModItems.BUBBLE_CORAL_STAFF.get().getDefaultInstance(), StaffItem.GemType.HEART_OF_THE_SEA));
						output.accept(StaffItem.createForGemType(ModItems.FIRE_CORAL_STAFF.get().getDefaultInstance(), StaffItem.GemType.HEART_OF_THE_SEA));
						output.accept(StaffItem.createForGemType(ModItems.HORN_CORAL_STAFF.get().getDefaultInstance(), StaffItem.GemType.HEART_OF_THE_SEA));
						output.accept(StaffItem.createForGemType(ModItems.SCULK_STAFF.get().getDefaultInstance(), StaffItem.GemType.HEART_OF_THE_SEA));
						output.accept(Items.ENDER_EYE);
						output.accept(StaffItem.createForGemType(ModItems.LIVING_WOOD_STAFF.get().getDefaultInstance(), StaffItem.GemType.ENDER_EYE));
						output.accept(StaffItem.createForGemType(ModItems.BLAZE_STAFF.get().getDefaultInstance(), StaffItem.GemType.ENDER_EYE));
						output.accept(StaffItem.createForGemType(ModItems.TUBE_CORAL_STAFF.get().getDefaultInstance(), StaffItem.GemType.ENDER_EYE));
						output.accept(StaffItem.createForGemType(ModItems.BRAIN_CORAL_STAFF.get().getDefaultInstance(), StaffItem.GemType.ENDER_EYE));
						output.accept(StaffItem.createForGemType(ModItems.BUBBLE_CORAL_STAFF.get().getDefaultInstance(), StaffItem.GemType.ENDER_EYE));
						output.accept(StaffItem.createForGemType(ModItems.FIRE_CORAL_STAFF.get().getDefaultInstance(), StaffItem.GemType.ENDER_EYE));
						output.accept(StaffItem.createForGemType(ModItems.HORN_CORAL_STAFF.get().getDefaultInstance(), StaffItem.GemType.ENDER_EYE));
						output.accept(StaffItem.createForGemType(ModItems.SCULK_STAFF.get().getDefaultInstance(), StaffItem.GemType.ENDER_EYE));
						output.accept(Items.NETHER_STAR);
						output.accept(StaffItem.createForGemType(ModItems.LIVING_WOOD_STAFF.get().getDefaultInstance(), StaffItem.GemType.NETHER_STAR));
						output.accept(StaffItem.createForGemType(ModItems.BLAZE_STAFF.get().getDefaultInstance(), StaffItem.GemType.NETHER_STAR));
						output.accept(StaffItem.createForGemType(ModItems.TUBE_CORAL_STAFF.get().getDefaultInstance(), StaffItem.GemType.NETHER_STAR));
						output.accept(StaffItem.createForGemType(ModItems.BRAIN_CORAL_STAFF.get().getDefaultInstance(), StaffItem.GemType.NETHER_STAR));
						output.accept(StaffItem.createForGemType(ModItems.BUBBLE_CORAL_STAFF.get().getDefaultInstance(), StaffItem.GemType.NETHER_STAR));
						output.accept(StaffItem.createForGemType(ModItems.FIRE_CORAL_STAFF.get().getDefaultInstance(), StaffItem.GemType.NETHER_STAR));
						output.accept(StaffItem.createForGemType(ModItems.HORN_CORAL_STAFF.get().getDefaultInstance(), StaffItem.GemType.NETHER_STAR));
						output.accept(StaffItem.createForGemType(ModItems.SCULK_STAFF.get().getDefaultInstance(), StaffItem.GemType.NETHER_STAR));
					})
					.build()
	);

	public static final SubCreativeTabs SUMMONS = register(
			SubCreativeTabs.builder()
					.title(Component.translatable("subcreativetab.rpgworldmod.summons"))
					.displayItems((provider, output) -> {
						output.accept(HeartOfTheSeaScrollItem.createForEnchantment(new EnchantmentInstance(ModEnchantments.CONJURATION.get(), ModEnchantments.CONJURATION.get().getMaxLevel())));
						output.accept(PillagerScrollItem.createForEnchantment(new EnchantmentInstance(ModEnchantments.CONJURATION.get(), ModEnchantments.CONJURATION.get().getMaxLevel())));
						output.accept(PillagerScrollItem.createForEnchantment(new EnchantmentInstance(ModEnchantments.NECROMANCY.get(), ModEnchantments.NECROMANCY.get().getMaxLevel())));
						output.accept(ModItems.MOSQUITO_BOTTLE.get());
						output.accept(ModItems.CHITIN_THIMBLE.get());
						output.accept(ModItems.CHITIN_POWDER.get());
					})
					.build()
	);

	public static final SubCreativeTabs COSMETICS = register(
			SubCreativeTabs.builder()
					.title(Component.translatable("subcreativetab.rpgworldmod.cosmetics"))
					.displayItems((provider, output) -> {
						output.accept(ModItems.BRAMBLEFOX_SCARF.get());
						output.accept(ModItems.FIG_LEAF.get());
						output.accept(ModItems.PORTABLE_TURRET.get());
					})
					.build()
	);

	public static final SubCreativeTabs MISC = register(
			SubCreativeTabs.builder()
					.title(Component.translatable("subcreativetab.rpgworldmod.misc"))
					.displayItems((provider, output) -> {
						output.accept(ModItems.PLATINUMFISH_BUCKET.get());
						output.accept(ModItems.BHLEE_BUCKET.get());
						output.accept(ModItems.MOSSFRONT_BUCKET.get());
						output.accept(ModItems.SHEENTROUT_BUCKET.get());
						output.accept(ModItems.GASBASS_BUCKET.get());
						output.accept(ModItems.ARBOR_FUEL_BUCKET.get());
						output.accept(ModItems.RIE_BOAT.get());
						output.accept(ModItems.RIE_CHEST_BOAT.get());
						output.accept(ModItems.MUSIC_DISC_HOWLING.get());
						output.accept(ModItems.MUSIC_DISC_TIRE.get());
						output.accept(ModItems.MUSIC_DISC_RAIN_A_SIDE.get());
					})
					.build()
	);


	static {
		FILTERS.keySet().forEach(subtab -> subtab.populate(null));
	}

	public static SubCreativeTabs register(SubCreativeTabs builder) {
		FILTERS.put(builder, true);
		return builder;
	}

	public static List<SubCreativeTabs> getTabs() {
		return List.copyOf(FILTERS.keySet());
	}
}