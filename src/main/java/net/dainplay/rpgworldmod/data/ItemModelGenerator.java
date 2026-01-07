package net.dainplay.rpgworldmod.data;

import net.dainplay.rpgworldmod.RPGworldMod;
import net.dainplay.rpgworldmod.item.ModItems;
import net.minecraft.data.PackOutput;
import net.minecraft.data.models.ItemModelGenerators;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ArmorItem;
import net.minecraftforge.client.model.generators.ItemModelBuilder;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.client.model.generators.loaders.ItemLayerModelBuilder;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.RegistryObject;

import java.util.Locale;

public class ItemModelGenerator extends ItemModelProvider {

	public ItemModelGenerator(PackOutput output, ExistingFileHelper existingFileHelper) {
		super(output, RPGworldMod.MOD_ID, existingFileHelper);
	}

	@Override
	protected void registerModels() {

		trimmedArmor(ModItems.LIVING_WOOD_HELMET);
		trimmedArmor(ModItems.LIVING_WOOD_CHESTPLATE);
		trimmedArmor(ModItems.LIVING_WOOD_LEGGINGS);
		trimmedArmor(ModItems.LIVING_WOOD_BOOTS);
	}

	public static ResourceLocation prefix(String name) {
		return new ResourceLocation(RPGworldMod.MOD_ID, name.toLowerCase(Locale.ROOT));
	}

	private ItemModelBuilder buildItem(String name, String parent, int emissivity, ResourceLocation... layers) {
		ItemModelBuilder builder = withExistingParent(name, parent);
		for (int i = 0; i < layers.length; i++) {
			builder = builder.texture("layer" + i, layers[i]);
		}
		if (emissivity > 0) builder = builder.customLoader(ItemLayerModelBuilder::begin).emissive(emissivity, emissivity, 0).renderType("minecraft:translucent", 0).end();
		return builder;
	}

	private ItemModelBuilder generated(String name, ResourceLocation... layers) {
		return buildItem(name, "item/generated", 0, layers);
	}

	private ItemModelBuilder singleTex(RegistryObject<?> item) {
		return generated(item.getId().getPath(), prefix("item/" + item.getId().getPath()));
	}

	private void trimmedArmor(RegistryObject<ArmorItem> armor) {
		ItemModelBuilder base = this.singleTex(armor);
		for (ItemModelGenerators.TrimModelData trim : ItemModelGenerators.GENERATED_TRIM_MODELS) {
			String material = trim.name();
			String name = armor.getId().getPath() + "_" + material + "_trim";
			ModelFile trimModel = this.withExistingParent(name, this.mcLoc("item/generated"))
					.texture("layer0", prefix("item/" + armor.getId().getPath()))
					.texture("layer1", this.mcLoc("trims/items/" + armor.get().getType().getName() + "_trim_" + material));
			base.override().predicate(new ResourceLocation("trim_type"), trim.itemModelIndex()).model(trimModel).end();
		}
	}
}