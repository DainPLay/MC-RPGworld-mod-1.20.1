package net.dainplay.rpgworldmod.event; // или в пакет, где у вас клиентские события

import net.dainplay.rpgworldmod.RPGworldMod;
import net.dainplay.rpgworldmod.item.custom.ChooseAnimateTargetItem;
import net.dainplay.rpgworldmod.network.ClientAnimateTargetData;
import net.dainplay.rpgworldmod.network.C2SRequestTargetValidationPacket;
import net.dainplay.rpgworldmod.network.ModMessages;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = RPGworldMod.MOD_ID, value = Dist.CLIENT)
public class ClientModEvents {

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.player instanceof LocalPlayer player) {
            if (player.isUsingItem() &&
                    player.getUseItemRemainingTicks() > 0 &&
                    player.getUseItem().getItem() instanceof ChooseAnimateTargetItem) {

                ChooseAnimateTargetItem catItem = (ChooseAnimateTargetItem) player.getUseItem().getItem();
                if (catItem.highlightTarget(player.getUseItem(), player) && catItem.canHighlightYourself(player.getUseItem(), player)) {

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
            }
        }
    }
}