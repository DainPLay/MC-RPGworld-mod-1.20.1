package net.dainplay.rpgworldmod.effect;


import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.client.extensions.common.IClientMobEffectExtensions;

import java.util.function.Consumer;

public class MobBeckonEffect extends MobEffect {
	public MobBeckonEffect(MobEffectCategory mobEffectCategory, int color) {
		super(mobEffectCategory, color);
	}

	@Override
	public void applyEffectTick(LivingEntity pLivingEntity, int pAmplifier) {
		if (!pLivingEntity.level().isClientSide() && pLivingEntity instanceof AbstractVillager) {
			((AbstractVillager) pLivingEntity).setTradingPlayer((Player) null);
		}

		super.applyEffectTick(pLivingEntity, pAmplifier);
	}

	@Override
	public void initializeClient(Consumer<IClientMobEffectExtensions> consumer) {
		consumer.accept(new MobBeckonEffect.MobBeckonEffectClient());
	}

	private static class MobBeckonEffectClient implements IClientMobEffectExtensions {
		@Override
		public boolean isVisibleInInventory(MobEffectInstance instance) {
			return false;
		}

		@Override
		public boolean isVisibleInGui(MobEffectInstance instance) {
			return false;
		}


		@Override
		public boolean renderInventoryIcon(MobEffectInstance instance, EffectRenderingInventoryScreen<?> screen,
										   GuiGraphics guiGraphics, int x, int y, int blitOffset) {
			return true;
		}

		@Override
		public boolean renderGuiIcon(MobEffectInstance instance, Gui gui, GuiGraphics guiGraphics,
									 int x, int y, float z, float alpha) {
			return true;
		}
	}

}