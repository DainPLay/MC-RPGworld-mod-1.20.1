package net.dainplay.rpgworldmod.effect;


import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.util.UUID;

public class NecrosisEffect extends MobEffect {
	public static final UUID MODIFIER_UUID = UUID.fromString("8fca842b-3951-4ee5-84b1-677c26eb5343");

	public NecrosisEffect(MobEffectCategory mobEffectCategory, int color) {
		super(mobEffectCategory, color);
		addAttributeModifier(Attributes.MAX_HEALTH, NecrosisEffect.MODIFIER_UUID.toString(), -1D, AttributeModifier.Operation.ADDITION);
	}

	@Override
	public void applyEffectTick(LivingEntity pLivingEntity, int pAmplifier) {
		if (pLivingEntity.getHealth() > pLivingEntity.getMaxHealth()) {
			pLivingEntity.setHealth(pLivingEntity.getMaxHealth());
		}
		super.applyEffectTick(pLivingEntity, pAmplifier);
	}

	public boolean isDurationEffectTick(int i, int j) {
		return true;
	}
}