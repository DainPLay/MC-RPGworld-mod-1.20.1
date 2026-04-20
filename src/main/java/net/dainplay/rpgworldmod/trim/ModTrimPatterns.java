package net.dainplay.rpgworldmod.trim;

import net.dainplay.rpgworldmod.RPGworldMod;
import net.dainplay.rpgworldmod.item.ModItems;
import net.minecraft.Util;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.armortrim.TrimPattern;
import net.minecraftforge.registries.ForgeRegistries;

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
