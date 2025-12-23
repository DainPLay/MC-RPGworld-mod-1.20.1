package net.dainplay.rpgworldmod.data;

import net.dainplay.rpgworldmod.RPGworldMod;
import net.dainplay.rpgworldmod.biome.BiomeRegistry;
import net.dainplay.rpgworldmod.damage.ModDamageTypes;
import net.dainplay.rpgworldmod.item.ModBannerPatterns;
import net.dainplay.rpgworldmod.trim.ModTrimMaterials;
import net.dainplay.rpgworldmod.trim.ModTrimPatterns;
import net.dainplay.rpgworldmod.world.feature.ModConfiguredFeatures;
import net.dainplay.rpgworldmod.world.feature.ModPlacedFeatures;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.DatapackBuiltinEntriesProvider;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class RegistryDataGenerator extends DatapackBuiltinEntriesProvider {

	public static final RegistrySetBuilder BUILDER = new RegistrySetBuilder()
			.add(Registries.CONFIGURED_FEATURE, ModConfiguredFeatures::bootstrap)
			.add(Registries.PLACED_FEATURE, ModPlacedFeatures::bootstrap)
			.add(Registries.BIOME, BiomeRegistry::bootstrapBiomes)
			.add(Registries.DAMAGE_TYPE, ModDamageTypes::bootstrap)
			.add(Registries.TRIM_MATERIAL, ModTrimMaterials::bootstrap)
			.add(Registries.TRIM_PATTERN, ModTrimPatterns::bootstrap);

	public RegistryDataGenerator(PackOutput output, CompletableFuture<HolderLookup.Provider> provider) {
		super(output, provider, BUILDER, Set.of("minecraft", RPGworldMod.MOD_ID));
	}
}
