package net.dainplay.rpgworldmod.util;

import net.dainplay.rpgworldmod.RPGworldMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.Tags;

public class ModTags {
    public static class Blocks {
        public static final TagKey<Block> STICKY_FOR_MOSQUITOS = forgeTag("sticky_for_mosquitos");
        private static TagKey<Block> forgeTag (String name) {
            return BlockTags.create(new ResourceLocation("forge", name));
        }
    }
    public static class Entity {
        /*public static final TagKey<EntityType<?>> IGNORE_TYPHON = forgeTag("ignore_typhon");
        private static TagKey<EntityType<?>> forgeTag (String name) {
            return EntityTypeTags.create(new ResourceLocation("forge", name));
        }*/
    }

    public static class Items {

        public static final TagKey<Item> WIDOWEED_CONSUMABLE = forgeTag("widoweed_consumable");
        public static final TagKey<Item> BRAMBLEFOX_FOOD = forgeTag("bramblefox_food");
        private static TagKey<Item> forgeTag (String name) {
            return ItemTags.create(new ResourceLocation("forge", name));
        }
    }

   //public static class Entities {

   //    public static final TagKey<EntityType<?>> WIDOWEED_CONSUMABLE = forgeTag("widoweed_consumable");
   //    private static TagKey<EntityType<?>> forgeTag (String name) {
   //        return EntityTypeTags.create(new ResourceLocation("forge", name));
   //    }
   //}
}
