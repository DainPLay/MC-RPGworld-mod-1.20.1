package net.dainplay.rpgworldmod.effect;


import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.util.UUID;

public class BurnoutEffect extends MobEffect {

    public BurnoutEffect(MobEffectCategory mobEffectCategory, int color) {
        super(mobEffectCategory, color);
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (!entity.level().isClientSide() && entity.hasEffect(ModEffects.BURNOUT.get())) {
            if (!entity.isOnFire() && entity.getRemainingFireTicks() <= 0) {
                entity.removeEffect(ModEffects.BURNOUT.get());
            }
        }
        super.applyEffectTick(entity, amplifier);
    }

    public boolean isDurationEffectTick(int i, int j) {
        return true;
    }
}