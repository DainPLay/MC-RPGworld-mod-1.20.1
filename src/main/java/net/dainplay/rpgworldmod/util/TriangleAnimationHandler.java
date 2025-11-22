package net.dainplay.rpgworldmod.util;

import net.dainplay.rpgworldmod.item.ModItems;
import net.dainplay.rpgworldmod.item.custom.MintalTriangleItem;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class TriangleAnimationHandler {
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END && !event.player.level().isClientSide) {
            Player player = event.player;
            for (ItemStack stack : player.getInventory().items) {
                if (stack.getItem() == ModItems.MINTAL_TRIANGLE.get()) {
                    int vibes = MintalTriangleItem.getVibes(stack);
                    if(vibes-1 >= 0) MintalTriangleItem.setVibes(stack, vibes-1); // Reduce durability by 1
                }
            }
            if (player.getOffhandItem().getItem() == ModItems.MINTAL_TRIANGLE.get()) {
                int vibes = MintalTriangleItem.getVibes(player.getOffhandItem());
                if(vibes-1 >= 0) MintalTriangleItem.setVibes(player.getOffhandItem(), vibes-1); // Reduce durability by 1
            }
        }
    }
    @SubscribeEvent
    public static void onEntityJoinWorld(EntityJoinLevelEvent event) {
        if (event.getEntity() instanceof ItemEntity) {
            ItemEntity itemEntity = (ItemEntity) event.getEntity();
            ItemStack itemStack = itemEntity.getItem();

            // Check if the spawned item is damageable
            if (itemStack.getItem() == ModItems.MINTAL_TRIANGLE.get()) {
                MintalTriangleItem.setVibes(itemStack, 0); // Reduce durability by 1
            }
        }
    }
}