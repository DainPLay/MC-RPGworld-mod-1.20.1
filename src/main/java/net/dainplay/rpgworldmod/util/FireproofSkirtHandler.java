package net.dainplay.rpgworldmod.util;

import net.dainplay.rpgworldmod.RPGworldMod;
import net.dainplay.rpgworldmod.data.tags.ModAdvancements;
import net.dainplay.rpgworldmod.enchantment.ModEnchantments;
import net.dainplay.rpgworldmod.item.ModItems;
import net.dainplay.rpgworldmod.item.custom.FireproofSkirtItem;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
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

			if (entity instanceof Player player
					&& player.isUsingItem()
					&& player.getUseItem().getItem() == ModItems.EMBER_SCROLL.get()
					&& player.getUseItem().getEnchantmentLevel(ModEnchantments.RESTORATION.get()) > 0) {
				if (!event.getSource().is(DamageTypes.LAVA) && !event.getSource().is(DamageTypes.IN_FIRE)) {
					if (player.getHealth() < player.getMaxHealth()) {
						ParticleOptions particleoptions = ParticleTypes.HEART;
						for (int i = 0; i < 4; ++i) {
							double d0 = player.getRandom().nextGaussian() * 0.02D;
							double d1 = player.getRandom().nextGaussian() * 0.02D;
							double d2 = player.getRandom().nextGaussian() * 0.02D;
							player.level().addParticle(particleoptions, player.getRandomX(1.0D), player.getRandomY() + 0.5D, player.getRandomZ(1.0D), d0, d1, d2);
						}
					}
					player.heal(event.getAmount());
					if (player instanceof ServerPlayer serverPlayer)
						ModAdvancements.SPELL_RESTORATION_EMBER_TRIGGER.trigger(serverPlayer);
				} else if (player.tickCount % 10 == 0) {
					if (player.getHealth() < player.getMaxHealth()) {
						ParticleOptions particleoptions = ParticleTypes.HEART;
						for (int i = 0; i < 4; ++i) {
							double d0 = player.getRandom().nextGaussian() * 0.02D;
							double d1 = player.getRandom().nextGaussian() * 0.02D;
							double d2 = player.getRandom().nextGaussian() * 0.02D;
							player.level().addParticle(particleoptions, player.getRandomX(1.0D), player.getRandomY() + 0.5D, player.getRandomZ(1.0D), d0, d1, d2);
						}
					}
					player.heal(event.getAmount());
					if (player instanceof ServerPlayer serverPlayer)
						ModAdvancements.SPELL_RESTORATION_EMBER_TRIGGER.trigger(serverPlayer);
				}
				event.setCanceled(true);
				return;
			}

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