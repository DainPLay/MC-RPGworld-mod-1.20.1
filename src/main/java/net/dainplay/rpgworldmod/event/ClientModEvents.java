package net.dainplay.rpgworldmod.event; // или в пакет, где у вас клиентские события

import net.dainplay.rpgworldmod.RPGworldMod;
import net.dainplay.rpgworldmod.item.custom.ChooseTargetItem;
import net.dainplay.rpgworldmod.network.ClientAnimateTargetData;
import net.dainplay.rpgworldmod.network.C2SRequestTargetValidationPacket;
import net.dainplay.rpgworldmod.network.ClientItemTargetData;
import net.dainplay.rpgworldmod.network.ModMessages;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

@Mod.EventBusSubscriber(modid = RPGworldMod.MOD_ID, value = Dist.CLIENT)
public class ClientModEvents {

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.player instanceof LocalPlayer player) {
            if (player.isUsingItem() &&
                    player.getUseItemRemainingTicks() > 0 &&
                    player.getUseItem().getItem() instanceof ChooseTargetItem) {

                ChooseTargetItem catItem = (ChooseTargetItem) player.getUseItem().getItem();
                if (catItem.highlightAnimateTarget(player.getUseItem(), player) && catItem.canHighlightYourself(player.getUseItem(), player)) {

                    LivingEntity target = null;
                    if (player.isShiftKeyDown())
                        target = player;

                    if (target != null && target.getItemBySlot(EquipmentSlot.HEAD).isEnderMask(player, null))
                        target = null;

                    if (target != null) {
                        ModMessages.sendToServer(new C2SRequestTargetValidationPacket(target.getId()));

                        if (!ClientAnimateTargetData.isValidTarget(target)) {
                            target = null;
                        }
                    }

                    ClientAnimateTargetData.set(target);
                }
                if (catItem.highlightItemsInRadius(player.getUseItem(), player)) {
                    List<ItemEntity> itemsInRadius = getAllItemsInRadius(player, 16.0);
                    ClientItemTargetData.clear();
                    for (ItemEntity item : itemsInRadius) {
                        ClientItemTargetData.addTarget(item);
                    }
                }
                if (catItem.highlightRandomItemInRadius(player.getUseItem(), player)) {
                    ItemEntity randomItem = getRandomItemInRadius(player, 64.0);
                    ItemEntity anotherRandomItem = randomItem;
                    ClientItemTargetData.clear();
                    ClientItemTargetData.addTarget(randomItem);
                    if(player.tickCount % 20 == 0 || (anotherRandomItem != null && player.distanceToSqr(anotherRandomItem) > 64.0*64.0)) {
                        anotherRandomItem = getRandomItemInRadius(player, 64.0);
                        ClientItemTargetData.set(anotherRandomItem);
                    }
                }
            }
        }
    }

    private static List<ItemEntity> getAllItemsInRadius(Player player, double radius) {
        AABB searchBox = player.getBoundingBox().inflate(radius);
        return player.level().getEntitiesOfClass(ItemEntity.class, searchBox);
    }

    private static ItemEntity getRandomItemInRadius(Player player, double radius) {
        List<ItemEntity> items = getAllItemsInRadius(player, radius);
        if (items.isEmpty()) return null;
        return items.get(player.getRandom().nextInt(items.size()));
    }
}