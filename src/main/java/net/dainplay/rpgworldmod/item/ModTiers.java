package net.dainplay.rpgworldmod.item;

import net.dainplay.rpgworldmod.block.ModBlocks;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.ForgeTier;

public class ModTiers {
    public static final ForgeTier MASKONITE = new ForgeTier(2, 400, 4.0F, 1.0F, 8, BlockTags.NEEDS_IRON_TOOL,
            () -> Ingredient.of(ModBlocks.MASKONITE_BLOCK.get()));
    public static final ForgeTier FLINT = new ForgeTier(1, 111, 4.0F, 1.0F, 7, BlockTags.NEEDS_STONE_TOOL,
            () -> Ingredient.of(Items.FLINT));
    public static final ForgeTier FAIRAPIER = new ForgeTier(2, 250, 6.0F, 2.0F, 22, BlockTags.NEEDS_IRON_TOOL,
            () -> Ingredient.EMPTY);
    public static final ForgeTier BURR = new ForgeTier(2, 440, 6.0F, 2.0F, 12, BlockTags.NEEDS_IRON_TOOL,
            () -> Ingredient.of(ModItems.BURR_SPIKE.get()));
}
