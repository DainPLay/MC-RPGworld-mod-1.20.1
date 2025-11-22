package net.dainplay.rpgworldmod.trim;

import net.dainplay.rpgworldmod.RPGworldMod;
import net.dainplay.rpgworldmod.item.ModItems;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.armortrim.TrimPattern;
import net.minecraft.world.item.armortrim.TrimPattern;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ModTrimPatterns {

	public static final ResourceKey<TrimPattern> LEAVES = ResourceKey.create(Registries.TRIM_PATTERN,
			new ResourceLocation(RPGworldMod.MOD_ID, "leaves"));

	public static void bootstrap(BootstapContext<TrimPattern> context) {
		register(context, ModItems.LEAVES_ARMOR_TRIM_SMITHING_TEMPLATE.get(), LEAVES);
	}


	private static void register(BootstapContext<TrimPattern> context, Item item, ResourceKey<TrimPattern> key) {
		TrimPattern trimPattern = new TrimPattern(key.location(), ForgeRegistries.ITEMS.getHolder(item).get(),
				Component.translatable(Util.makeDescriptionId("trim_pattern", key.location())));
		context.register(key, trimPattern);
	}
}
