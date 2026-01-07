package net.dainplay.rpgworldmod.util;

import net.dainplay.rpgworldmod.RPGworldMod;
import net.dainplay.rpgworldmod.item.ModItems;
import net.dainplay.rpgworldmod.item.custom.FireproofSkirtItem;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = RPGworldMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class FireproofSkirtHandler {

    @SubscribeEvent
    public static void onEntityDamage(LivingAttackEvent event) {
        if (event.getSource().is(DamageTypes.ON_FIRE)
                || event.getSource().is(DamageTypes.IN_FIRE)
                || event.getSource().is(DamageTypes.LAVA)
                || event.getSource().is(DamageTypes.FIREBALL)
                || event.getSource().is(DamageTypes.HOT_FLOOR)
                || event.getSource().is(DamageTypeTags.IS_FIRE)) {
            LivingEntity entity = event.getEntity();

            ItemStack legsStack = entity.getItemBySlot(EquipmentSlot.LEGS);
            if (legsStack.getItem() == ModItems.FIREPROOF_SKIRT.get() && FireproofSkirtItem.isFireproof(legsStack)) {
                event.setCanceled(true);
                legsStack.hurtAndBreak(1, entity, 
                    (livingEntity) -> {
                        livingEntity.broadcastBreakEvent(EquipmentSlot.LEGS);
                    }
                );
            }
        }
    }
}