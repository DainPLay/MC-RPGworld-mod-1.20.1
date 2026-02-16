package net.dainplay.rpgworldmod.effect;

import net.dainplay.rpgworldmod.damage.ModDamageTypes;
import net.dainplay.rpgworldmod.sounds.RPGSounds;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.ArrayList;

public class BurnoutHandler {

	@SubscribeEvent
	public static void onLivingAttack(LivingAttackEvent event) {
		if (!event.getEntity().isSpectator() && (event.getEntity()).hasEffect(ModEffects.BURNOUT.get())) {
			if (event.getSource().getEntity() instanceof LivingEntity damageDealer) {
				int amp = event.getEntity().getEffect(ModEffects.BURNOUT.get()).getAmplifier();
				damageDealer.hurt(damageDealer.damageSources().magic(), amp + 1);
				event.getEntity().removeEffect(ModEffects.BURNOUT.get());
				event.getEntity().extinguishFire();
				event.getEntity().level().playSound(null, event.getEntity().blockPosition(), SoundEvents.GENERIC_EXTINGUISH_FIRE, SoundSource.PLAYERS, 0.7F, 1.6F + (event.getEntity().getRandom().nextFloat() - event.getEntity().getRandom().nextFloat()) * 0.4F);
				if (event.getEntity().getMaxHealth() <= amp + 1) {
					event.getEntity().hurt(ModDamageTypes.getDamageSource(event.getEntity().level(), ModDamageTypes.NECROSIS), Float.MAX_VALUE);
				} else {
					event.getEntity().hurt(ModDamageTypes.getDamageSource(event.getEntity().level(), ModDamageTypes.NECROSIS), amp + 1);
					if (event.getEntity().hasEffect(ModEffects.NECROSIS.get()))
						amp += 1 + event.getEntity().getEffect(ModEffects.NECROSIS.get()).getAmplifier();
					MobEffectInstance necrosis = new MobEffectInstance(ModEffects.NECROSIS.get(), 1200, amp);
					necrosis.setCurativeItems(new ArrayList<>());
					event.getEntity().addEffect(necrosis);
					event.getEntity().level().playSound(null,
							event.getEntity().getX(), event.getEntity().getY(), event.getEntity().getZ(),
							RPGSounds.SPELL_NECROMANCY_CAST.get(),
							SoundSource.PLAYERS, 1.0F, 1.0F
					);
				}
				event.setCanceled(true);
			}
		}
	}

	@SubscribeEvent
	public static void onLivingDamage(LivingDamageEvent event) {
		if (!event.getEntity().isSpectator() && (event.getEntity()).hasEffect(ModEffects.BURNOUT.get())) {
			if (!(event.getSource().getEntity() instanceof LivingEntity)) {
				int healthDifference = Mth.ceil(event.getEntity().getHealth()) - Mth.ceil(event.getEntity().getHealth() - event.getAmount());
				if (healthDifference > 0) {
					MobEffectInstance burnoutEffect = event.getEntity().getEffect(ModEffects.BURNOUT.get());
					int currentAmplifier = burnoutEffect.getAmplifier();
					int newAmplifier = Math.max(-1, currentAmplifier - healthDifference);
					if (newAmplifier != currentAmplifier) {
						int duration = burnoutEffect.getDuration();
						event.getEntity().removeEffect(ModEffects.BURNOUT.get());

						if (newAmplifier > -1) {
							MobEffectInstance burnout = new MobEffectInstance(
									ModEffects.BURNOUT.get(),
									duration,
									newAmplifier,
									burnoutEffect.isAmbient(),
									burnoutEffect.isVisible(),
									burnoutEffect.showIcon()
							);
							burnout.setCurativeItems(burnoutEffect.getCurativeItems());
							event.getEntity().addEffect(burnout);
						} else {
							event.getEntity().extinguishFire();
							event.getEntity().level().playSound(null, event.getEntity().blockPosition(), SoundEvents.GENERIC_EXTINGUISH_FIRE, SoundSource.PLAYERS, 0.7F, 1.6F + (event.getEntity().getRandom().nextFloat() - event.getEntity().getRandom().nextFloat()) * 0.4F);
						}
					}
				}
			}
		}
	}
}
