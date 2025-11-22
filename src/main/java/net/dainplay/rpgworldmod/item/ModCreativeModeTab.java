package net.dainplay.rpgworldmod.item;

import net.dainplay.rpgworldmod.RPGworldMod;
import net.dainplay.rpgworldmod.block.ModBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModCreativeModeTab {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB,
            RPGworldMod.MOD_ID);

    public static RegistryObject<CreativeModeTab> RPGWORLD_BLOCKS_TAB = CREATIVE_MODE_TABS.register("rpgworldtab1_blocks", () ->
            CreativeModeTab.builder().icon(() -> new ItemStack(ModBlocks.RIE_HOLLOW.get()))
                    .title(Component.translatable("creativemodetab.rpgworldtab_blocks")).build());
    public static RegistryObject<CreativeModeTab> RPGWORLD_FLORA_TAB = CREATIVE_MODE_TABS.register("rpgworldtab2_flora", () ->
            CreativeModeTab.builder().icon(() -> new ItemStack(ModBlocks.MIMOSSA.get()))
                    .title(Component.translatable("creativemodetab.rpgworldtab_flora")).build());
    public static RegistryObject<CreativeModeTab> RPGWORLD_FAUNA_TAB = CREATIVE_MODE_TABS.register("rpgworldtab3_fauna", () ->
            CreativeModeTab.builder().icon(() -> new ItemStack(ModItems.MOSSFRONT.get()))
                    .title(Component.translatable("creativemodetab.rpgworldtab_fauna")).build());
    public static RegistryObject<CreativeModeTab> RPGWORLD_EQUIPMENT_TAB = CREATIVE_MODE_TABS.register("rpgworldtab4_equipment", () ->
            CreativeModeTab.builder().icon(() -> new ItemStack(ModItems.MINTAL_TRIANGLE.get()))
                    .title(Component.translatable("creativemodetab.rpgworldtab_equipment")).build());
    public static RegistryObject<CreativeModeTab> RPGWORLD_FOODS_AND_DRINKS_TAB = CREATIVE_MODE_TABS.register("rpgworldtab5_foods_and_drinks", () ->
            CreativeModeTab.builder().icon(() -> new ItemStack(ModItems.MINT_RIE_FRUIT.get()))
                    .title(Component.translatable("creativemodetab.rpgworldtab_foods_and_drinks")).build());
    public static RegistryObject<CreativeModeTab> RPGWORLD_MATERIALS_TAB = CREATIVE_MODE_TABS.register("rpgworldtab6_materials", () ->
            CreativeModeTab.builder().icon(() -> new ItemStack(ModItems.MINTAL_INGOT.get()))
                    .title(Component.translatable("creativemodetab.rpgworldtab_materials")).build());
    public static RegistryObject<CreativeModeTab> RPGWORLD_SPAWN_EGGS_TAB = CREATIVE_MODE_TABS.register("rpgworldtab7_spawn_eggs", () ->
            CreativeModeTab.builder().icon(() -> new ItemStack(ModItems.MINTOBAT_SPAWN_EGG.get()))
                    .title(Component.translatable("creativemodetab.rpgworldtab_spawn_eggs")).build());

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}
