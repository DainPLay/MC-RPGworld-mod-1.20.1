package net.dainplay.rpgworldmod.util;

import net.dainplay.rpgworldmod.item.ModItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "rpgworldmod", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class BowNBTManager {

    @SubscribeEvent
    public static void onBowUseStart(LivingEntityUseItemEvent.Start event) {
        if (event.getEntity() instanceof Player player && 
            event.getItem().getItem() == Items.BOW) {
            
            ItemStack bowStack = event.getItem();
            
            if (player.getProjectile(bowStack).getItem() == ModItems.PROJECTRUFFLE_ITEM.get()) {
                // Устанавливаем тег
                CompoundTag tag = bowStack.getOrCreateTag();
                tag.putBoolean("UsingProjectruffle", true);
            } else {
                // Удаляем тег если он есть
                if (bowStack.hasTag() && bowStack.getTag().contains("UsingProjectruffle")) {
                    bowStack.getTag().remove("UsingProjectruffle");
                }
            }
        }
    }

    @SubscribeEvent
    public static void onBowUseStop(LivingEntityUseItemEvent.Stop event) {
        if (event.getItem().getItem() == Items.BOW) {
            ItemStack bowStack = event.getItem();
            if (bowStack.hasTag() && bowStack.getTag().contains("UsingProjectruffle")) {
                bowStack.getTag().remove("UsingProjectruffle");
            }
        }
    }

    @SubscribeEvent
    public static void onBowUseFinish(LivingEntityUseItemEvent.Finish event) {
        if (event.getItem().getItem() == Items.BOW) {
            ItemStack bowStack = event.getItem();
            if (bowStack.hasTag() && bowStack.getTag().contains("UsingProjectruffle")) {
                bowStack.getTag().remove("UsingProjectruffle");
            }
        }
    }
}