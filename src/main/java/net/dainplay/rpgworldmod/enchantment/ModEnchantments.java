package net.dainplay.rpgworldmod.enchantment;

import net.dainplay.rpgworldmod.RPGworldMod;
import net.dainplay.rpgworldmod.item.custom.DrillSpearItem;
import net.dainplay.rpgworldmod.item.custom.GuitarAxItem;
import net.dainplay.rpgworldmod.item.custom.MintalTriangleItem;
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
    public static final EnchantmentCategory DRILL_SPEAR = EnchantmentCategory.create("drill_spear", (item) -> {return (item instanceof DrillSpearItem); });
    public static final EnchantmentCategory TRIANGLE = EnchantmentCategory.create("triangle", (item) -> {return (item instanceof MintalTriangleItem); });
    public static final EnchantmentCategory GUITAR_AX = EnchantmentCategory.create("guitar_ax", (item) -> {return (item instanceof MintalTriangleItem || item instanceof GuitarAxItem); });
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
                            TRIANGLE, EquipmentSlot.MAINHAND));


    public static void register(IEventBus eventBus) {
        ENCHANTMENTS.register(eventBus);
    }
}