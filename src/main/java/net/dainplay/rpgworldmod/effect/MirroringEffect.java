package net.dainplay.rpgworldmod.effect;


import net.dainplay.rpgworldmod.util.EffectSyncHandler;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

public class MirroringEffect extends MobEffect {

	public MirroringEffect(MobEffectCategory mobEffectCategory, int color) {
		super(mobEffectCategory, color);
	}

	@Override
	public void applyEffectTick(LivingEntity entity, int pAmplifier) {

		var effectInstance = entity.getEffect(this);
		if (effectInstance != null) {
			int duration = effectInstance.getDuration();
			if (duration == -1) duration = entity.tickCount;
			if (!entity.level().isClientSide && duration % 500 == 0) {
				EffectSyncHandler.generateAndSyncSeed(entity);
			}
		}
		super.applyEffectTick(entity, pAmplifier);
	}

	public boolean isDurationEffectTick(int i, int j) {
		return true;
	}
}