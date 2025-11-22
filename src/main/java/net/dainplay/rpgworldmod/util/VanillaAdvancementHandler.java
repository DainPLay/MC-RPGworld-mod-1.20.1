package net.dainplay.rpgworldmod.util;

import net.dainplay.rpgworldmod.item.ModItems;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class VanillaAdvancementHandler {
    @SubscribeEvent
    public void onItemPickup(PlayerEvent.ItemPickupEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return; //Только для игроков
        }

        ItemStack itemStack = event.getStack();

        // Проверка, является ли это вашей модовой рыбой
        if (itemStack.getItem() == ModItems.MOSSFRONT.get()) {

            ResourceLocation vanillaAdvancementId = new ResourceLocation("husbandry/fishy_business");

            Advancement vanillaAdvancement = player.server.getAdvancements().getAdvancement(vanillaAdvancementId);

            /*if (vanillaAdvancement == null) {
                return;
            }*/

                AdvancementProgress advancementprogress = player.getAdvancements().getOrStartProgress(vanillaAdvancement);
                if (!advancementprogress.isDone()) {
                    for(String s : advancementprogress.getRemainingCriteria()) {
                        player.getAdvancements().award(vanillaAdvancement, s);
                    }

                }
        }
    }
}
