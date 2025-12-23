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

    /**
     * Проверяет, должен ли лук иметь тег UsingProjectruffle на основе инвентаря игрока
     */
    private static boolean shouldUseProjectruffle(Player living) {
        boolean result = false;
        if (living instanceof Player) {
            if (living.getItemInHand(InteractionHand.MAIN_HAND).getItem() == Items.BOW) {
                for (int i = 0; i < ((Player) living).getInventory().getContainerSize(); ++i) {
                    if (ModItems.PROJECTRUFFLE_ITEM.get() == (((Player) living).getInventory().getItem(i)).getItem()) {
                        result = true;
                        break;
                        //return result;
                    }
                    if (Items.ARROW == (((Player) living).getInventory().getItem(i)).getItem()) {
                        result = false;
                        break;
                        //return result;
                    }
                    if (Items.TIPPED_ARROW == (((Player) living).getInventory().getItem(i)).getItem()) {
                        result = false;
                        break;
                        //return result;
                    }
                    if (Items.SPECTRAL_ARROW == (((Player) living).getInventory().getItem(i)).getItem()) {
                        result = false;
                        break;
                        //return result;
                    }
                }
                if (living.getOffhandItem().getItem() == Items.ARROW) result = false;
                if (living.getOffhandItem().getItem() == Items.TIPPED_ARROW) result = false;
                if (living.getOffhandItem().getItem() == Items.SPECTRAL_ARROW) result = false;
                if (living.getOffhandItem().getItem() == ModItems.PROJECTRUFFLE_ITEM.get())
                    result = true;
            }
            if (living.getItemInHand(InteractionHand.OFF_HAND).getItem() == Items.BOW) {
                for (int i = 0; i < ((Player) living).getInventory().getContainerSize(); ++i) {
                    if (ModItems.PROJECTRUFFLE_ITEM.get() == (((Player) living).getInventory().getItem(i)).getItem()) {
                        result = true;
                        break;
                        //return result;
                    }
                    if (Items.ARROW == (((Player) living).getInventory().getItem(i)).getItem()) {
                        result = false;
                        break;
                        //return result;
                    }
                    if (Items.TIPPED_ARROW == (((Player) living).getInventory().getItem(i)).getItem()) {
                        result = false;
                        break;
                        //return result;
                    }
                    if (Items.SPECTRAL_ARROW == (((Player) living).getInventory().getItem(i)).getItem()) {
                        result = false;
                        break;
                        //return result;
                    }
                }
                if (living.getItemInHand(InteractionHand.MAIN_HAND).getItem() == Items.ARROW) result = false;
                if (living.getItemInHand(InteractionHand.MAIN_HAND).getItem() == Items.TIPPED_ARROW) result = false;
                if (living.getItemInHand(InteractionHand.MAIN_HAND).getItem() == Items.SPECTRAL_ARROW) result = false;
                if (living.getItemInHand(InteractionHand.MAIN_HAND).getItem() == ModItems.PROJECTRUFFLE_ITEM.get())
                    result = true;
            }
        }
        return result;
    }

    @SubscribeEvent
    public static void onBowUseStart(LivingEntityUseItemEvent.Start event) {
        if (event.getEntity() instanceof Player player && 
            event.getItem().getItem() == Items.BOW) {
            
            ItemStack bowStack = event.getItem();
            
            if (shouldUseProjectruffle(player)) {
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