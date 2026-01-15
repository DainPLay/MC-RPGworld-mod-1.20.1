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

public class ParanoiaEffect extends MobEffect {
	public static final UUID MODIFIER_UUID = UUID.fromString("3affb255-7fb3-4086-8baa-35d7f21f5a49");
	protected final RandomSource random = RandomSource.create();

	public ParanoiaEffect(MobEffectCategory mobEffectCategory, int color) {
		super(mobEffectCategory, color);
		addAttributeModifier(Attributes.LUCK, ParanoiaEffect.MODIFIER_UUID.toString(), 0D, AttributeModifier.Operation.MULTIPLY_TOTAL);
	}

	@Override
	public void applyEffectTick(LivingEntity pLivingEntity, int pAmplifier) {
		if (pLivingEntity instanceof ServerPlayer serverPlayer) {
			if (!serverPlayer.getAbilities().instabuild) {
				serverPlayer.getCapability(PlayerManaProvider.PLAYER_MANA).ifPresent(mana -> {
					if (pLivingEntity.tickCount % Math.max(5, 20 - 10 * pAmplifier) == 0)
						mana.reduceMana(serverPlayer, 1);
					mana.setManaRegenBlocked(serverPlayer, 25);
				});
			}
		}
		super.applyEffectTick(pLivingEntity, pAmplifier);
	}

	public boolean isDurationEffectTick(int i, int j) {
		return true;
	}
}