package net.dainplay.rpgworldmod.effect;


import net.dainplay.rpgworldmod.data.tags.ModAdvancements;
import net.dainplay.rpgworldmod.entity.custom.TireSwingEntity;
import net.dainplay.rpgworldmod.network.PlayerManaProvider;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.util.UUID;

public class HappinessEffect extends MobEffect {
	protected final RandomSource random = RandomSource.create();

	public HappinessEffect(MobEffectCategory mobEffectCategory, int color) {
		super(mobEffectCategory, color);
	}

	@Override
	public void applyEffectTick(LivingEntity pLivingEntity, int pAmplifier) {
		if (pLivingEntity instanceof ServerPlayer serverPlayer) {
			if (!serverPlayer.getAbilities().instabuild) {
				serverPlayer.getCapability(PlayerManaProvider.PLAYER_MANA).ifPresent(mana -> {
					if (pLivingEntity.tickCount % Math.max(1, 20 - 5 * pAmplifier) == 0) {
						if (mana.getMana() == 0 && serverPlayer.getVehicle() instanceof TireSwingEntity)
							ModAdvancements.RIDE_TIRE_SWING_TRIGGER.trigger(serverPlayer);
						mana.addMana(serverPlayer, 1);
					}
				});
			}
		}
		super.applyEffectTick(pLivingEntity, pAmplifier);
	}

	public boolean isDurationEffectTick(int i, int j) {
		return true;
	}
}