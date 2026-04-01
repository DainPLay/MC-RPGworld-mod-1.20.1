package net.dainplay.rpgworldmod.event; // или в пакет, где у вас клиентские события

import net.dainplay.rpgworldmod.RPGworldMod;
import net.dainplay.rpgworldmod.effect.ModEffects;
import net.dainplay.rpgworldmod.enchantment.ModEnchantments;
import net.dainplay.rpgworldmod.item.custom.ChooseTargetItem;
import net.dainplay.rpgworldmod.item.custom.NetherStarScrollItem;
import net.dainplay.rpgworldmod.network.ClientAnimateTargetData;
import net.dainplay.rpgworldmod.network.C2SRequestTargetValidationPacket;
import net.dainplay.rpgworldmod.network.ClientItemTargetData;
import net.dainplay.rpgworldmod.network.ModMessages;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
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
            boolean usingNetherWarpSpell = player.isUsingItem() && player.getUseItem().getItem() instanceof NetherStarScrollItem &&
                    player.getUseItem().getEnchantmentLevel(ModEnchantments.ALTERATION.get()) > 0;
            if (player.hasEffect(ModEffects.NETHER_PORTAL_ILLUSION.get()) || usingNetherWarpSpell) {
                float newIntensity = player.spinningEffectIntensity + 0.032F;
                player.spinningEffectIntensity = Math.min(1.0F, newIntensity);
            }
            if(usingNetherWarpSpell && player.getTicksUsingItem() == 1 && event.phase == TickEvent.Phase.END) {
                Minecraft.getInstance().getSoundManager().play(
                        SimpleSoundInstance.forLocalAmbience(
                                SoundEvents.PORTAL_TRIGGER,
                                player.getRandom().nextFloat() * 0.4F + 0.8F,
                                0.25F
                        )
                );
            }

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