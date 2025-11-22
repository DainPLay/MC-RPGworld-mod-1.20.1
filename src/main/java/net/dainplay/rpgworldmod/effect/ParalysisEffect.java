package net.dainplay.rpgworldmod.effect;


import net.dainplay.rpgworldmod.RPGworldMod;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.UUID;

public class ParalysisEffect extends MobEffect {

    protected static final ResourceLocation PARALYSIS_OUTLINE_LOCATION = new ResourceLocation(RPGworldMod.MOD_ID,"textures/misc/paralysis_outline.png");
    public static final UUID MODIFIER_UUID = UUID.fromString("55C22FFD-3972-4E3E-BB81-8C12E803E30F");

    public ParalysisEffect(MobEffectCategory mobEffectCategory, int color) {
        super(mobEffectCategory, color);
        addAttributeModifier(Attributes.MOVEMENT_SPEED, ParalysisEffect.MODIFIER_UUID.toString(), 0D, AttributeModifier.Operation.MULTIPLY_TOTAL);

    }
    @Override
    public void applyEffectTick(LivingEntity pLivingEntity, int pAmplifier) {
        if (!pLivingEntity.level().isClientSide() && pLivingEntity instanceof AbstractVillager) {
            ((AbstractVillager)pLivingEntity).setTradingPlayer((Player)null);
        }

        super.applyEffectTick(pLivingEntity, pAmplifier);
    }

}