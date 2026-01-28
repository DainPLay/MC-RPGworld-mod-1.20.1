package net.dainplay.rpgworldmod.enchantment;

import net.dainplay.rpgworldmod.RPGworldMod;
import net.dainplay.rpgworldmod.item.ModItems;
import net.dainplay.rpgworldmod.item.custom.DrillSpearItem;
import net.dainplay.rpgworldmod.item.custom.EmberScrollItem;
import net.dainplay.rpgworldmod.item.custom.EmptyScrollItem;
import net.dainplay.rpgworldmod.item.custom.GuitarAxItem;
import net.dainplay.rpgworldmod.item.custom.LivingWoodBowItem;
import net.dainplay.rpgworldmod.item.custom.MintalTriangleItem;
import net.dainplay.rpgworldmod.item.custom.ScrollItem;
import net.dainplay.rpgworldmod.item.custom.WealdBladeItem;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModEnchantments {
    public static final DeferredRegister<Enchantment> ENCHANTMENTS =
            DeferredRegister.create(ForgeRegistries.ENCHANTMENTS, RPGworldMod.MOD_ID);
    public static final EquipmentSlot[] ARMOR_SLOTS = new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
    public static final EnchantmentCategory DRILL_SPEAR = EnchantmentCategory.create("drill_spear", (item) -> (item instanceof DrillSpearItem));
    public static final EnchantmentCategory WEALD_BLADE = EnchantmentCategory.create("weald_blade", (item) -> (item instanceof WealdBladeItem));
    public static final EnchantmentCategory LIVING_WOOD_BOW = EnchantmentCategory.create("living_wood_bow", (item) -> (item instanceof LivingWoodBowItem));
    public static final EnchantmentCategory TRIANGLE = EnchantmentCategory.create("triangle", (item) -> (item instanceof MintalTriangleItem));
    public static final EnchantmentCategory GUITAR_AX = EnchantmentCategory.create("guitar_ax", (item) -> (item instanceof MintalTriangleItem || item instanceof GuitarAxItem));
    public static final EnchantmentCategory MAGIC_SCROLLS = EnchantmentCategory.create("magic_scrolls", (item) -> (item instanceof EmptyScrollItem || item instanceof ScrollItem));
    public static RegistryObject<Enchantment> PITCH =
            ENCHANTMENTS.register("pitch",
                    () -> new PitchEnchantment(Enchantment.Rarity.COMMON,
                            TRIANGLE, EquipmentSlot.MAINHAND));
    public static RegistryObject<Enchantment> CRESCENDO =
            ENCHANTMENTS.register("crescendo",
                    () -> new CrescendoEnchantment(Enchantment.Rarity.UNCOMMON,
                            GUITAR_AX, EquipmentSlot.MAINHAND));
    public static RegistryObject<Enchantment> STEREO =
            ENCHANTMENTS.register("stereo",
                    () -> new StereoEnchantment(Enchantment.Rarity.UNCOMMON,
                            GUITAR_AX, EquipmentSlot.MAINHAND));
    public static RegistryObject<Enchantment> TEMPO =
            ENCHANTMENTS.register("tempo",
                    () -> new TempoEnchantment(Enchantment.Rarity.RARE,
                            TRIANGLE, EquipmentSlot.MAINHAND));
    public static RegistryObject<Enchantment> SOUNDPROOF =
            ENCHANTMENTS.register("soundproof",
                    () -> new SoundproofEnchantment(Enchantment.Rarity.RARE,
                            EnchantmentCategory.ARMOR_HEAD, ARMOR_SLOTS));
    public static RegistryObject<Enchantment> COLLECTION =
            ENCHANTMENTS.register("collection",
                    () -> new CollectionEnchantment(Enchantment.Rarity.RARE,
                            DRILL_SPEAR, EquipmentSlot.MAINHAND));
    public static RegistryObject<Enchantment> BLOWING =
            ENCHANTMENTS.register("blowing",
                    () -> new BlowingEnchantment(Enchantment.Rarity.RARE,
                            WEALD_BLADE, EquipmentSlot.MAINHAND));
    public static RegistryObject<Enchantment> STRETCH =
            ENCHANTMENTS.register("stretch",
                    () -> new StretchEnchantment(Enchantment.Rarity.RARE,
                            LIVING_WOOD_BOW, EquipmentSlot.MAINHAND));
    public static RegistryObject<Enchantment> RESTORATION =
            ENCHANTMENTS.register("restoration",
                    () -> new RestorationEnchantment(Enchantment.Rarity.COMMON,
                            MAGIC_SCROLLS, EquipmentSlot.MAINHAND));
    public static RegistryObject<Enchantment> DESTRUCTION =
            ENCHANTMENTS.register("destruction",
                    () -> new DestructionEnchantment(Enchantment.Rarity.COMMON,
                            MAGIC_SCROLLS, EquipmentSlot.MAINHAND));
    public static RegistryObject<Enchantment> ILLUSION =
            ENCHANTMENTS.register("illusion",
                    () -> new IllusionEnchantment(Enchantment.Rarity.COMMON,
                            MAGIC_SCROLLS, EquipmentSlot.MAINHAND));
    public static RegistryObject<Enchantment> ALTERATION =
            ENCHANTMENTS.register("alteration",
                    () -> new AlterationEnchantment(Enchantment.Rarity.COMMON,
                            MAGIC_SCROLLS, EquipmentSlot.MAINHAND));
    public static RegistryObject<Enchantment> CREATION =
            ENCHANTMENTS.register("creation",
                    () -> new CreationEnchantment(Enchantment.Rarity.COMMON,
                            MAGIC_SCROLLS, EquipmentSlot.MAINHAND));
    public static RegistryObject<Enchantment> NECROMANCY =
            ENCHANTMENTS.register("necromancy",
                    () -> new NecromancyEnchantment(Enchantment.Rarity.RARE,
                            MAGIC_SCROLLS, EquipmentSlot.MAINHAND));


    public static void register(IEventBus eventBus) {
        ENCHANTMENTS.register(eventBus);
    }
}