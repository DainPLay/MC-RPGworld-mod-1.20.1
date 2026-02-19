// EmberScrollRecipe.java
package net.dainplay.rpgworldmod.data.craft;

import com.google.gson.JsonObject;
import net.dainplay.rpgworldmod.data.ModRecipeSerializers;
import net.dainplay.rpgworldmod.item.ModItems;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapelessRecipe;

public class SpellRecipe extends ShapelessRecipe {

    public SpellRecipe(ResourceLocation id, String group, CraftingBookCategory category,
                       ItemStack result, NonNullList<Ingredient> ingredients) {
        super(id, group, category, result, ingredients);
    }

    @Override
    public ItemStack assemble(CraftingContainer container, RegistryAccess registryAccess) {
        ItemStack result = super.assemble(container, registryAccess);

        // Ищем EMPTY_SCROLL в сетке крафта
        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack stack = container.getItem(i);
            if (stack.getItem() == ModItems.EMPTY_SCROLL.get() && stack.hasTag()) {
                // Копируем NBT на результат
                result.setTag(stack.getTag().copy());
                break;
            }
        }

        return result;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeSerializers.SPELL_RECIPE.get();
    }

    // Вложенный класс сериализатора
    public static class Serializer implements RecipeSerializer<SpellRecipe> {
        private final ShapelessRecipe.Serializer baseSerializer = new ShapelessRecipe.Serializer();

        @Override
        public SpellRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
            ShapelessRecipe baseRecipe = this.baseSerializer.fromJson(recipeId, json);
            return new SpellRecipe(
                    recipeId,
                    baseRecipe.getGroup(),
                    baseRecipe.category(),
                    baseRecipe.getResultItem(null),
                    baseRecipe.getIngredients()
            );
        }

        @Override
        public SpellRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
            ShapelessRecipe baseRecipe = this.baseSerializer.fromNetwork(recipeId, buffer);
            return new SpellRecipe(
                    recipeId,
                    baseRecipe.getGroup(),
                    baseRecipe.category(),
                    baseRecipe.getResultItem(null),
                    baseRecipe.getIngredients()
            );
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, SpellRecipe recipe) {
            this.baseSerializer.toNetwork(buffer, recipe);
        }
    }
}