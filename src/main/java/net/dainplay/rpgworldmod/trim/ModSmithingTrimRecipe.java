package net.dainplay.rpgworldmod.trim;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.SmithingTrimRecipe;

public class ModSmithingTrimRecipe extends SmithingTrimRecipe {
    public ModSmithingTrimRecipe(ResourceLocation pId, Ingredient pTemplate, Ingredient pBase, Ingredient pAddition) {
        super(pId, pTemplate, pBase, pAddition);
    }
}
