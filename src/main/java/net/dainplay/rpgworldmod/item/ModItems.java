package net.dainplay.rpgworldmod.item;

import net.dainplay.rpgworldmod.RPGworldMod;
import net.dainplay.rpgworldmod.block.ModBlocks;
import net.dainplay.rpgworldmod.entity.ModEntities;
import net.dainplay.rpgworldmod.fluid.ModFluids;
import net.dainplay.rpgworldmod.item.custom.*;
import net.dainplay.rpgworldmod.entity.custom.ModBoat;
import net.dainplay.rpgworldmod.sounds.RPGSounds;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.*;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.List;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, RPGworldMod.MOD_ID);
    public static final RegistryObject<Item> WEALD_BLADE = ITEMS.register("weald_blade",
            () -> new WealdBladeItem(ModTiers.BURR, 3, -2.4f, new Item.Properties().rarity(Rarity.RARE)));
    public static final RegistryObject<Item> DRILL_SPEAR = ITEMS.register("drill_spear",
            () -> new DrillSpearItem((new Item.Properties()).durability(90).rarity(Rarity.UNCOMMON)));
    public static final RegistryObject<Item> MASKONITE_SWORD = ITEMS.register("maskonite_sword",
            () -> new SwordItem(ModTiers.MASKONITE, 3, -2.4f, new Item.Properties()));
    public static final RegistryObject<Item> MASKONITE_PICKAXE = ITEMS.register("maskonite_pickaxe",
            () -> new PickaxeItem(ModTiers.MASKONITE, 1, -2.8f, new Item.Properties()));
    public static final RegistryObject<Item> MASKONITE_AXE = ITEMS.register("maskonite_axe",
            () -> new AxeItem(ModTiers.MASKONITE, 7, -3.2f, new Item.Properties()));
    public static final RegistryObject<Item> GUITAR_AX = ITEMS.register("guitar_ax",
            () -> new GuitarAxItem(Tiers.NETHERITE, 3, -3f, new Item.Properties().rarity(Rarity.RARE).stacksTo(1)));
    public static final RegistryObject<Item> MASKONITE_SHOVEL = ITEMS.register("maskonite_shovel",
            () -> new ShovelItem(ModTiers.MASKONITE, 1.5f, -3f, new Item.Properties()));
    public static final RegistryObject<Item> MASKONITE_HOE = ITEMS.register("maskonite_hoe",
            () -> new HoeItem(ModTiers.MASKONITE, -1, -2f, new Item.Properties()));
    public static final RegistryObject<Item> FAIRAPIER_SWORD = ITEMS.register("fairapier_sword",
            () -> new FairapierSwordItem(ModTiers.FAIRAPIER, 3, -1.9f, new Item.Properties().rarity(Rarity.UNCOMMON)));
    public static final RegistryObject<Item> FAIRAPIER_SWORD_WILTED = ITEMS.register("fairapier_sword_wilted",
            () -> new FairapierSwordWiltedItem(ModBlocks.FAIRAPIER_WILTED_PLANT.get(), new Item.Properties().rarity(Rarity.UNCOMMON).stacksTo(1)));
    public static final RegistryObject<Item> FLINT_SWORD = ITEMS.register("flint_sword",
            () -> new FlintSwordItem(ModTiers.FLINT, 3, -2.4F, new Item.Properties()));
    public static final RegistryObject<Item> FLINT_PICKAXE = ITEMS.register("flint_pickaxe",
            () -> new FlintPickaxeItem(ModTiers.FLINT, 1, -2.8F, new Item.Properties()));
    public static final RegistryObject<Item> FLINT_AXE = ITEMS.register("flint_axe",
            () -> new FlintAxeItem(ModTiers.FLINT, 7.0F, -3.2F, new Item.Properties()));
    public static final RegistryObject<Item> FLINT_SHOVEL = ITEMS.register("flint_shovel",
            () -> new FlintShovelItem(ModTiers.FLINT, 1.5F, -3.0F, new Item.Properties()));
    public static final RegistryObject<Item> FLINT_HOE = ITEMS.register("flint_hoe",
            () -> new FlintHoeItem(ModTiers.FLINT, -1, -2.0F, new Item.Properties()));

    public static final RegistryObject<Item> SAMARAGUARD = ITEMS.register("samaraguard",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> MINTAL_INGOT = ITEMS.register("mintal_ingot",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> MINTAL_NUGGET = ITEMS.register("mintal_nugget",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> MINTAL_TRIANGLE = ITEMS.register("mintal_triangle",
            () -> new MintalTriangleItem(new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON).durability(121)));
    public static final RegistryObject<Item> STEELPORK = ITEMS.register("steelpork",
            () -> new LongFoodItem(new Item.Properties().food(ModFoods.STEELPORK)));
    public static final RegistryObject<Item> COOKED_STEELPORK = ITEMS.register("cooked_steelpork",
            () -> new LongFoodItem(new Item.Properties().food(ModFoods.COOKED_STEELPORK)));
    public static final RegistryObject<Item> RIE_FRUIT = ITEMS.register("rie_fruit",
            () -> new Item(new Item.Properties().food(ModFoods.RIE_FRUIT)));
    public static final RegistryObject<Item> MINT_RIE_FRUIT = ITEMS.register("mint_rie_fruit",
            () -> new ExtinguishingItem(new Item.Properties().food(ModFoods.MINT_RIE_FRUIT)));
    public static final RegistryObject<Item> BRAMBLEFOX_BERRIES = ITEMS.register("bramblefox_berries",
            () -> new Item(new Item.Properties().food(ModFoods.BRAMBLEFOX_BERRIES)));
    public static final RegistryObject<Item> FAIRAPIER_SEED = ITEMS.register("fairapier_seed",
            () -> new FairapierSeedItem(ModBlocks.FAIRAPIER_PLANT.get(), new Item.Properties().stacksTo(16)));
    public static final RegistryObject<Item> BURR_SPIKE = ITEMS.register("burr_spike",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> CHITIN_POWDER = ITEMS.register("chitin_powder",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> MOSQUITO_BOTTLE = ITEMS.register("mosquito_bottle",
            () -> new MosquitoBottleItem(new Item.Properties()));
    public static final RegistryObject<Item> SHIVERALIS_BERRIES = ITEMS.register("shiveralis_berries",
            () -> new ItemNameBlockItem(ModBlocks.SHIVERALIS.get(), new Item.Properties().food(ModFoods.SHIVERALIS_BERRIES)));
    public static final RegistryObject<Item> PARALILY = ITEMS.register("paralily",
            () -> new PlaceOnWaterBlockItem(ModBlocks.PARALILY.get(), new Item.Properties()));
    public static final RegistryObject<Item> PARALILY_BERRY = ITEMS.register("paralily_berry",
            () -> new Item(new Item.Properties().food(ModFoods.PARALILY_BERRY)));
    public static final RegistryObject<Item> RPGIROLLE_ITEM = ITEMS.register("rpgirolle_item",
            () -> new ItemNameBlockItem(ModBlocks.RPGIROLLE.get(), new Item.Properties().food(ModFoods.RPGIROLLE)));
    public static final RegistryObject<Item> CHEESE_CAP = ITEMS.register("cheese_cap",
            () -> new CheeseItem(ModBlocks.CHEESE_CAP.get(), new Item.Properties().food(ModFoods.CHEESE_CAP)));
    public static final RegistryObject<Item> MASKONITE_UPGRADE_SMITHING_TEMPLATE = ITEMS.register("maskonite_upgrade_smithing_template",
            () -> new SmithingTemplateItem(
                    Component.translatable(Util.makeDescriptionId("item", new ResourceLocation("smithing_template.maskonite_upgrade.applies_to"))).withStyle(ChatFormatting.BLUE),
                    Component.translatable(Util.makeDescriptionId("item", new ResourceLocation("smithing_template.maskonite_upgrade.ingredients"))).withStyle(ChatFormatting.BLUE),
                    Component.translatable(Util.makeDescriptionId("upgrade", new ResourceLocation("maskonite_upgrade"))).withStyle(ChatFormatting.GRAY),
                    Component.translatable(Util.makeDescriptionId("item", new ResourceLocation("smithing_template.maskonite_upgrade.base_slot_description"))),
                    Component.translatable(Util.makeDescriptionId("item", new ResourceLocation("smithing_template.maskonite_upgrade.additions_slot_description"))),
                    List.of(
                            new ResourceLocation("item/empty_slot_sword"),
                            new ResourceLocation("item/empty_slot_pickaxe"),
                            new ResourceLocation("item/empty_slot_axe"),
                            new ResourceLocation("item/empty_slot_hoe"),
                            new ResourceLocation("item/empty_slot_shovel")
                    ),
                    List.of(
                            new ResourceLocation("item/empty_slot_block")
                    )
            ));
    public static final RegistryObject<Item> LEAVES_ARMOR_TRIM_SMITHING_TEMPLATE = ITEMS.register("leaves_armor_trim_smithing_template",
            () -> SmithingTemplateItem.createArmorTrimTemplate(new ResourceLocation(RPGworldMod.MOD_ID, "leaves")));
    public static final RegistryObject<Item> PROJECTRUFFLE_ITEM = ITEMS.register("projectruffle_item",
            () -> new ProjectruffleArrowItem(new Item.Properties()));
    public static final RegistryObject<Item> TYPHON_DYE = ITEMS.register("typhon_dye",
            () -> new DyeItem(DyeColor.LIGHT_GRAY, new Item.Properties()));
    public static final RegistryObject<Item> RIE_SIGN = ITEMS.register("rie_sign",
            () -> new SignItem(new Item.Properties().stacksTo(16),
                    ModBlocks.RIE_SIGN.get(), ModBlocks.RIE_WALL_SIGN.get()));
    public static final RegistryObject<Item> RIE_HANGING_SIGN = ITEMS.register("rie_hanging_sign",
            () -> new HangingSignItem(ModBlocks.RIE_HANGING_SIGN.get(), ModBlocks.RIE_WALL_HANGING_SIGN.get(), new Item.Properties().stacksTo(16)));
    public static final RegistryObject<Item> RIE_BOAT = ITEMS.register("rie_boat",
            () -> new ModBoatItem(false, ModBoat.Type.RIE, (new Item.Properties()).stacksTo(1)));
    public static final RegistryObject<Item> RIE_CHEST_BOAT = ITEMS.register("rie_chest_boat",
            () -> new ModBoatItem(true, ModBoat.Type.RIE, (new Item.Properties()).stacksTo(1)));
    public static final RegistryObject<Item> BRAMBLEFOX_SPAWN_EGG = ITEMS.register("bramblefox_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.BRAMBLEFOX, 0x18693F, 0xE27C21, new Item.Properties()));
    public static final RegistryObject<Item> BIBBIT_SPAWN_EGG = ITEMS.register("bibbit_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.BIBBIT, 0x0D3821, 0xFFFF00, new Item.Properties()));
    public static final RegistryObject<Item> MINTOBAT_SPAWN_EGG = ITEMS.register("mintobat_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.MINTOBAT, 0x14B485, 0x263C47, new Item.Properties()));
    public static final RegistryObject<Item> FIREFLANTERN_SPAWN_EGG = ITEMS.register("fireflantern_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.FIREFLANTERN, 0x734029, 0xFFFF00, new Item.Properties()));
    public static final RegistryObject<Item> BURR_PURR_SPAWN_EGG = ITEMS.register("burr_purr_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.BURR_PURR, 0x176649, 0x5A9038, new Item.Properties()));
    public static final RegistryObject<Item> DRILLHOG_SPAWN_EGG = ITEMS.register("drillhog_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.DRILLHOG, 0x835853, 0xEBE1BE, new Item.Properties()));
    public static final RegistryObject<Item> MOSQUITO_SWARM_SPAWN_EGG = ITEMS.register("mosquito_swarm_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.MOSQUITO_SWARM, 0x4D7575, 0x648C8C, new Item.Properties()));
    public static final RegistryObject<CurioItem> BRAMBLEFOX_SCARF = ITEMS.register("bramblefox_scarf", () -> new CurioItem(new Item.Properties().rarity(Rarity.UNCOMMON)));
    public static final RegistryObject<CurioItem> FIG_LEAF = ITEMS.register("fig_leaf", () -> new CurioItem(new Item.Properties().rarity(Rarity.UNCOMMON)));
    public static final RegistryObject<CurioItem> PORTABLE_TURRET = ITEMS.register("portable_turret", () -> new CurioItem(new Item.Properties().rarity(Rarity.RARE)));
    public static final RegistryObject<ChitinThimbleItem> CHITIN_THIMBLE = ITEMS.register("chitin_thimble", () -> new ChitinThimbleItem(new Item.Properties().rarity(Rarity.UNCOMMON)));
    public static final RegistryObject<Item> MUSIC_DISC_HOWLING = ITEMS.register("music_disc_howling", () -> new RecordItem(12, RPGSounds.MUSIC_DISC_HOWLING.get(), new Item.Properties().stacksTo(1).rarity(Rarity.RARE), 184 * 20));

    public static final RegistryObject<Item> ARBOR_FUEL_BUCKET = ITEMS.register("arbor_fuel_bucket", () -> new FuelBucketItem(ModFluids.SOURCE_ARBOR_FUEL, new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1)));
    public static final RegistryObject<Item> PLATINUMFISH = ITEMS.register("platinumfish",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> PLATINUMFISH_BUCKET = ITEMS.register("platinumfish_bucket",
            () -> new MobBucketItem(ModEntities.PLATINUMFISH, () -> Fluids.WATER, () -> SoundEvents.BUCKET_EMPTY_FISH, (new Item.Properties()).stacksTo(1)));
    public static final RegistryObject<Item> PLATINUMFISH_SPAWN_EGG = ITEMS.register("platinumfish_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.PLATINUMFISH, 0x8097B8, 0xF6D8EB, new Item.Properties()));
    public static final RegistryObject<Item> BHLEE = ITEMS.register("bhlee",
            () -> new Item(new Item.Properties().food(ModFoods.BHLEE)));
    public static final RegistryObject<Item> COOKED_BHLEE = ITEMS.register("cooked_bhlee",
            () -> new Item(new Item.Properties().food(ModFoods.COOKED_BHLEE)));
    public static final RegistryObject<Item> BHLEE_BUCKET = ITEMS.register("bhlee_bucket",
            () -> new MobBucketItem(ModEntities.BHLEE, () -> Fluids.WATER, () -> SoundEvents.BUCKET_EMPTY_FISH, (new Item.Properties()).stacksTo(1)));
    public static final RegistryObject<Item> BHLEE_SPAWN_EGG = ITEMS.register("bhlee_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.BHLEE, 0xB0B0B0, 0x788DE1, new Item.Properties()));
    public static final RegistryObject<Item> MOSSFRONT = ITEMS.register("mossfront",
            () -> new Item(new Item.Properties().food(ModFoods.MOSSFRONT)));
    public static final RegistryObject<Item> MOSSFRONT_BUCKET = ITEMS.register("mossfront_bucket",
            () -> new MobBucketItem(ModEntities.MOSSFRONT, () -> Fluids.WATER, () -> SoundEvents.BUCKET_EMPTY_FISH, (new Item.Properties()).stacksTo(1)));
    public static final RegistryObject<Item> MOSSFRONT_SPAWN_EGG = ITEMS.register("mossfront_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.MOSSFRONT, 0x689043, 0x0F4434, new Item.Properties()));
    public static final RegistryObject<Item> SHEENTROUT = ITEMS.register("sheentrout",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> SHEENTROUT_BUCKET = ITEMS.register("sheentrout_bucket",
            () -> new MobBucketItem(ModEntities.SHEENTROUT, () -> Fluids.WATER, () -> SoundEvents.BUCKET_EMPTY_FISH, (new Item.Properties()).stacksTo(1)));
    public static final RegistryObject<Item> SHEENTROUT_SPAWN_EGG = ITEMS.register("sheentrout_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.SHEENTROUT, 0xE5D9BB, 0xF9F9F9, new Item.Properties()));
    public static final RegistryObject<Item> GASBASS = ITEMS.register("gasbass",
            () -> new GasbassItem(new Item.Properties().stacksTo(1).food(ModFoods.GASBASS)));
    public static final RegistryObject<Item> GASBASS_BUCKET = ITEMS.register("gasbass_bucket",
            () -> new MobBucketItem(ModEntities.GASBASS, () -> Fluids.WATER, () -> SoundEvents.BUCKET_EMPTY_FISH, (new Item.Properties()).stacksTo(1)));
    public static final RegistryObject<Item> GASBASS_SPAWN_EGG = ITEMS.register("gasbass_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.GASBASS, 0x516574, 0x2E404B, new Item.Properties()));

    public static void register(IEventBus eventbus) {
        ITEMS.register(eventbus);
    }
}
