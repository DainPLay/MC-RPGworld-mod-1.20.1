package net.dainplay.rpgworldmod.data.craft;

import com.google.gson.JsonObject;
import net.dainplay.rpgworldmod.data.ModRecipeSerializers;
import net.dainplay.rpgworldmod.enchantment.ModEnchantments;
import net.dainplay.rpgworldmod.item.ModItems;
import net.dainplay.rpgworldmod.item.custom.PillagerScrollItem;
import net.dainplay.rpgworldmod.network.PlayerManaProvider;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.TransientCraftingContainer;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.WoolCarpetBlock;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class WoolDyeingScrollRecipe extends ShapelessRecipe {

	public WoolDyeingScrollRecipe(ResourceLocation id, String group, CraftingBookCategory category,
								  ItemStack result, NonNullList<Ingredient> ingredients) {
		super(id, group, category, result, ingredients);
	}

	@Override
	public boolean matches(CraftingContainer container, Level level) {
		boolean hasCarpet = false;
		boolean hasWool = false;
		boolean hasScroll = false;
		ItemStack scrollStack = ItemStack.EMPTY;

		for (int i = 0; i < container.getContainerSize(); i++) {
			ItemStack stack = container.getItem(i);
			if (stack.isEmpty()) continue;

			if (stack.getItem() instanceof BlockItem blockItem &&
					blockItem.getDefaultInstance().is(ItemTags.WOOL)) {
				if (hasWool || hasCarpet) return false;
				hasWool = true;
			} else if (stack.getItem() instanceof BlockItem blockItem &&
					blockItem.getDefaultInstance().is(ItemTags.WOOL_CARPETS)) {
				if (hasWool || hasCarpet) return false;
				hasCarpet = true;
			} else if (stack.is(ModItems.PILLAGER_SCROLL.get())) {
				if (hasScroll) return false;
				hasScroll = true;
				scrollStack = stack;
			} else {
				return false;
			}
		}

		if (!(hasWool || hasCarpet) || !hasScroll) return false;

		Map<Enchantment, Integer> enchants = EnchantmentHelper.getEnchantments(scrollStack);
		return enchants.containsKey(ModEnchantments.ALTERATION.get());
	}

	@Override
	public ItemStack assemble(CraftingContainer container, RegistryAccess registryAccess) {
		ItemStack scrollStack = ItemStack.EMPTY;
		boolean isWool = false;
		DyeColor woolColor = DyeColor.WHITE;
		Player player = null;
		if (container instanceof TransientCraftingContainer transientCraftingContainer) {
			if(transientCraftingContainer.menu instanceof CraftingMenu menu)
				player = menu.player;
			if (transientCraftingContainer.menu instanceof InventoryMenu menu) {
				player = menu.owner;
			}
		}
		if (player == null) return ItemStack.EMPTY;

		for (int i = 0; i < container.getContainerSize(); i++) {
			ItemStack stack = container.getItem(i);
			if (stack.is(ModItems.PILLAGER_SCROLL.get())) {
				scrollStack = stack;
			}
			if (stack.is(ItemTags.WOOL)) {
				isWool = true;
			}
			if (stack.is(ItemTags.WOOL_CARPETS)) {
				isWool = false;
			}
		}

		if (!scrollStack.isEmpty()) {
			PillagerScrollItem.Color selected = PillagerScrollItem.getSelectedColor(scrollStack);
			woolColor = switch (selected) {
				case WHITE -> DyeColor.WHITE;
				case LIGHT_GRAY -> DyeColor.LIGHT_GRAY;
				case GRAY -> DyeColor.GRAY;
				case BLACK -> DyeColor.BLACK;
				case BROWN -> DyeColor.BROWN;
				case RED -> DyeColor.RED;
				case ORANGE -> DyeColor.ORANGE;
				case YELLOW -> DyeColor.YELLOW;
				case LIME -> DyeColor.LIME;
				case GREEN -> DyeColor.GREEN;
				case CYAN -> DyeColor.CYAN;
				case LIGHT_BLUE -> DyeColor.LIGHT_BLUE;
				case BLUE -> DyeColor.BLUE;
				case PURPLE -> DyeColor.PURPLE;
				case MAGENTA -> DyeColor.MAGENTA;
				case PINK -> DyeColor.PINK;
			};
		}

		Block woolBlock;

		if (isWool) {
			woolBlock = switch (woolColor) {
				case WHITE -> Blocks.WHITE_WOOL;
				case ORANGE -> Blocks.ORANGE_WOOL;
				case MAGENTA -> Blocks.MAGENTA_WOOL;
				case LIGHT_BLUE -> Blocks.LIGHT_BLUE_WOOL;
				case YELLOW -> Blocks.YELLOW_WOOL;
				case LIME -> Blocks.LIME_WOOL;
				case PINK -> Blocks.PINK_WOOL;
				case GRAY -> Blocks.GRAY_WOOL;
				case LIGHT_GRAY -> Blocks.LIGHT_GRAY_WOOL;
				case CYAN -> Blocks.CYAN_WOOL;
				case PURPLE -> Blocks.PURPLE_WOOL;
				case BLUE -> Blocks.BLUE_WOOL;
				case BROWN -> Blocks.BROWN_WOOL;
				case GREEN -> Blocks.GREEN_WOOL;
				case RED -> Blocks.RED_WOOL;
				case BLACK -> Blocks.BLACK_WOOL;
			};
		} else {
			woolBlock = switch (woolColor) {
				case WHITE -> Blocks.WHITE_CARPET;
				case ORANGE -> Blocks.ORANGE_CARPET;
				case MAGENTA -> Blocks.MAGENTA_CARPET;
				case LIGHT_BLUE -> Blocks.LIGHT_BLUE_CARPET;
				case YELLOW -> Blocks.YELLOW_CARPET;
				case LIME -> Blocks.LIME_CARPET;
				case PINK -> Blocks.PINK_CARPET;
				case GRAY -> Blocks.GRAY_CARPET;
				case LIGHT_GRAY -> Blocks.LIGHT_GRAY_CARPET;
				case CYAN -> Blocks.CYAN_CARPET;
				case PURPLE -> Blocks.PURPLE_CARPET;
				case BLUE -> Blocks.BLUE_CARPET;
				case BROWN -> Blocks.BROWN_CARPET;
				case GREEN -> Blocks.GREEN_CARPET;
				case RED -> Blocks.RED_CARPET;
				case BLACK -> Blocks.BLACK_CARPET;
			};
		}




		AtomicBoolean hasEnoughMana = new AtomicBoolean(true);
		if (!player.getAbilities().instabuild && scrollStack.getItem() instanceof PillagerScrollItem scroll) {
			ItemStack finalScrollStack = scrollStack;
			Player finalPlayer = player;
			player.getCapability(PlayerManaProvider.PLAYER_MANA).ifPresent(mana -> {
				if (mana.getMana() < scroll.getManaCost(finalScrollStack, finalPlayer)) {
					hasEnoughMana.set(false);
				}
			});
		}
		if (!hasEnoughMana.get()) return ItemStack.EMPTY;

		return new ItemStack(woolBlock);
	}

	@Override
	public NonNullList<ItemStack> getRemainingItems(CraftingContainer container) {
		NonNullList<ItemStack> remaining = NonNullList.withSize(container.getContainerSize(), ItemStack.EMPTY);

		for (int i = 0; i < container.getContainerSize(); i++) {
			ItemStack stack = container.getItem(i);
			if (stack.is(ModItems.PILLAGER_SCROLL.get())) {
				remaining.set(i, stack.copy());
			} else {
			}
		}
		return remaining;
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return ModRecipeSerializers.WOOL_DYEING_SCROLL_RECIPE.get();
	}

	public static class Serializer implements RecipeSerializer<WoolDyeingScrollRecipe> {
		private final ShapelessRecipe.Serializer baseSerializer = new ShapelessRecipe.Serializer();

		@Override
		public WoolDyeingScrollRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
			ShapelessRecipe baseRecipe = baseSerializer.fromJson(recipeId, json);
			return new WoolDyeingScrollRecipe(
					recipeId,
					baseRecipe.getGroup(),
					baseRecipe.category(),
					baseRecipe.getResultItem(null),
					replaceIngredients(baseRecipe.getIngredients())
			);
		}

		@Override
		public WoolDyeingScrollRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
			ShapelessRecipe baseRecipe = baseSerializer.fromNetwork(recipeId, buffer);
			return new WoolDyeingScrollRecipe(
					recipeId,
					baseRecipe.getGroup(),
					baseRecipe.category(),
					baseRecipe.getResultItem(null),
					replaceIngredients(baseRecipe.getIngredients())
			);
		}

		@Override
		public void toNetwork(FriendlyByteBuf buffer, WoolDyeingScrollRecipe recipe) {
			baseSerializer.toNetwork(buffer, recipe);
		}
	}

	public static NonNullList<Ingredient> replaceIngredients(NonNullList<Ingredient> ingredients) {
		NonNullList<Ingredient> modified = NonNullList.createWithCapacity(ingredients.size());
		for (Ingredient original : ingredients) {
			if (original.test(new ItemStack(ModItems.PILLAGER_SCROLL.get()))) {
				ItemStack stack = new ItemStack(ModItems.PILLAGER_SCROLL.get());
				stack.enchant(ModEnchantments.ALTERATION.get(), 1);
				modified.add(Ingredient.of(stack));
			} else {
				modified.add(original);
			}
		}
		return modified;
	}
}