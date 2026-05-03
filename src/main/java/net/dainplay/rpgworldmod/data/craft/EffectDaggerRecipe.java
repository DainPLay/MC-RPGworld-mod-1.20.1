package net.dainplay.rpgworldmod.data.craft;

import net.dainplay.rpgworldmod.item.custom.DaggerItem;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.minecraft.world.level.Level;

public class EffectDaggerRecipe extends CustomRecipe {


	public static final RecipeSerializer<EffectDaggerRecipe> SERIALIZER =
			new SimpleCraftingRecipeSerializer<>(EffectDaggerRecipe::new);

	public EffectDaggerRecipe(ResourceLocation id, CraftingBookCategory category) {
		super(id, category);
	}


	@Override
	public boolean matches(CraftingContainer container, Level level) {
		boolean hasDagger = false;
		boolean hasPotion = false;

		for (int i = 0; i < container.getContainerSize(); i++) {
			ItemStack stack = container.getItem(i);
			if (stack.isEmpty()) continue;

			if (stack.getItem() instanceof DaggerItem) {
				if (hasDagger) return false;
				hasDagger = true;
			} else if (stack.is(Items.LINGERING_POTION)) {
				if (hasPotion) return false;
				hasPotion = true;
			} else {
				return false;
			}
		}
		return hasDagger && hasPotion;
	}

	@Override
	public ItemStack assemble(CraftingContainer container, RegistryAccess registryAccess) {
		ItemStack dagger = ItemStack.EMPTY;
		ItemStack potion = ItemStack.EMPTY;

		for (int i = 0; i < container.getContainerSize(); i++) {
			ItemStack stack = container.getItem(i);
			if (stack.getItem() instanceof DaggerItem) {
				dagger = stack;
			} else if (stack.is(Items.LINGERING_POTION)) {
				potion = stack;
			}
		}

		if (dagger.isEmpty() || potion.isEmpty()) {
			return ItemStack.EMPTY;
		}


		ItemStack result = dagger.copy();


		PotionUtils.setPotion(result, PotionUtils.getPotion(potion));
		PotionUtils.setCustomEffects(result, PotionUtils.getCustomEffects(potion));

		return result;
	}


	@Override
	public boolean canCraftInDimensions(int width, int height) {
		return width * height >= 2;
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return SERIALIZER;
	}
}