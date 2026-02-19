// ModRecipeSerializers.java
package net.dainplay.rpgworldmod.data;

import com.google.gson.JsonObject;
import net.dainplay.rpgworldmod.RPGworldMod;
import net.dainplay.rpgworldmod.data.craft.SpellRecipe;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModRecipeSerializers {
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
            DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, RPGworldMod.MOD_ID);

    public static final RegistryObject<RecipeSerializer<SpellRecipe>> SPELL_RECIPE =
            RECIPE_SERIALIZERS.register("spell", SpellRecipe.Serializer::new);

    // Вложенный статический класс для сериализатора
    public static class Serializer implements RecipeSerializer<SpellRecipe> {
        private final ShapelessRecipe.Serializer baseSerializer = new ShapelessRecipe.Serializer();

        @Override
        public SpellRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
            ShapelessRecipe baseRecipe = baseSerializer.fromJson(recipeId, json);
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
            ShapelessRecipe baseRecipe = baseSerializer.fromNetwork(recipeId, buffer);
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
            baseSerializer.toNetwork(buffer, recipe);
        }
    }
}