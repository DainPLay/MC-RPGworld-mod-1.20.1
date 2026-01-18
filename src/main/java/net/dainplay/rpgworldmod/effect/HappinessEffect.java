package net.dainplay.rpgworldmod.effect;


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
	public static final UUID MODIFIER_UUID = UUID.fromString("db65fd7e-1124-456a-9d0e-9124cbf1511f");
	protected final RandomSource random = RandomSource.create();

	public HappinessEffect(MobEffectCategory mobEffectCategory, int color) {
		super(mobEffectCategory, color);
		addAttributeModifier(Attributes.LUCK, HappinessEffect.MODIFIER_UUID.toString(), 0D, AttributeModifier.Operation.MULTIPLY_TOTAL);
	}

	@Override
	public void applyEffectTick(LivingEntity pLivingEntity, int pAmplifier) {
		if (pLivingEntity instanceof ServerPlayer serverPlayer) {
			if (!serverPlayer.getAbilities().instabuild) {
				serverPlayer.getCapability(PlayerManaProvider.PLAYER_MANA).ifPresent(mana -> {
					if (pLivingEntity.tickCount % Math.max(1, 20 - 5 * pAmplifier) == 0)
						mana.addMana(serverPlayer, 1);
				});
			}
		}
		super.applyEffectTick(pLivingEntity, pAmplifier);
	}

	public boolean isDurationEffectTick(int i, int j) {
		return true;
	}
}