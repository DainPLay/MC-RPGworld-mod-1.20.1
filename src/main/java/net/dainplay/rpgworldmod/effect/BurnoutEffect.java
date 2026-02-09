package net.dainplay.rpgworldmod.effect;


import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.util.UUID;

public class BurnoutEffect extends MobEffect {
    public static final UUID MODIFIER_UUID = UUID.fromString("63597591-b08d-473a-a4fd-0a3ab6a1e714");

    public BurnoutEffect(MobEffectCategory mobEffectCategory, int color) {
        super(mobEffectCategory, color);
        addAttributeModifier(Attributes.MAX_HEALTH, BurnoutEffect.MODIFIER_UUID.toString(), 0D, AttributeModifier.Operation.MULTIPLY_TOTAL);
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