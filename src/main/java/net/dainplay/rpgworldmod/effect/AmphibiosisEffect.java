package net.dainplay.rpgworldmod.effect;


import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

public class AmphibiosisEffect extends MobEffect {
	public AmphibiosisEffect(MobEffectCategory mobEffectCategory, int color) {
		super(mobEffectCategory, color);
	}

	@Override
	public void applyEffectTick(LivingEntity entity, int amplifier) {
		super.applyEffectTick(entity, amplifier);
	}

	public boolean isDurationEffectTick(int i, int j) {
		return true;
	}
}