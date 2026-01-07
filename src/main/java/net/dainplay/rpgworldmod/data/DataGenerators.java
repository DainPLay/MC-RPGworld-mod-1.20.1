package net.dainplay.rpgworldmod.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.dainplay.rpgworldmod.RPGworldMod;
import net.dainplay.rpgworldmod.data.tags.DamageTypeTagGenerator;
import net.dainplay.rpgworldmod.loot.ModGlobalLootModifiersProvider;
import net.minecraft.DetectedVersion;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.data.metadata.PackMetadataGenerator;
import net.minecraft.data.models.ItemModelGenerators;
import net.minecraft.data.models.model.ModelLocationUtils;
import net.minecraft.data.models.model.ModelTemplates;
import net.minecraft.data.models.model.TextureMapping;
import net.minecraft.data.models.model.TextureSlot;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.data.DatapackBuiltinEntriesProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

@Mod.EventBusSubscriber(modid = RPGworldMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class DataGenerators {

	@SubscribeEvent
	public static void gatherData(GatherDataEvent event) {
		DataGenerator generator = event.getGenerator();
		PackOutput output = event.getGenerator().getPackOutput();
		CompletableFuture<HolderLookup.Provider> provider = event.getLookupProvider();
		ExistingFileHelper helper = event.getExistingFileHelper();
		addArmorTrims(helper);

		DatapackBuiltinEntriesProvider datapackProvider = new RegistryDataGenerator(output, provider);
		CompletableFuture<HolderLookup.Provider> lookupProvider = datapackProvider.getRegistryProvider();
		generator.addProvider(event.includeServer(), datapackProvider);
		generator.addProvider(event.includeClient(), new ItemModelGenerator(output, helper));
		generator.addProvider(event.includeServer(), new DamageTypeTagGenerator(output, lookupProvider, helper));
		generator.addProvider(true, new PackMetadataGenerator(output).add(PackMetadataSection.TYPE, new PackMetadataSection(
						Component.literal("Resources for RPGworld mod"),
						DetectedVersion.BUILT_IN.getPackVersion(PackType.CLIENT_RESOURCES),
						Arrays.stream(PackType.values()).collect(Collectors.toMap(Function.identity(), DetectedVersion.BUILT_IN::getPackVersion)))));
		generator.addProvider(event.includeServer(), new ModGlobalLootModifiersProvider(output));
	}


	private static void addArmorTrims(ExistingFileHelper existingFileHelper) {
		for (ItemModelGenerators.TrimModelData trim : ItemModelGenerators.GENERATED_TRIM_MODELS) {
			existingFileHelper.trackGenerated(new ResourceLocation("boots_trim_" + trim.name()), PackType.CLIENT_RESOURCES, ".png", "textures/trims/items");
			existingFileHelper.trackGenerated(new ResourceLocation("chestplate_trim_" + trim.name()), PackType.CLIENT_RESOURCES, ".png", "textures/trims/items");
			existingFileHelper.trackGenerated(new ResourceLocation("helmet_trim_" + trim.name()), PackType.CLIENT_RESOURCES, ".png", "textures/trims/items");
			existingFileHelper.trackGenerated(new ResourceLocation("leggings_trim_" + trim.name()), PackType.CLIENT_RESOURCES, ".png", "textures/trims/items");
		}
	}
}
