package net.dainplay.rpgworldmod.effect;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.client.extensions.common.IClientMobEffectExtensions;

import java.util.UUID;
import java.util.function.Consumer;

public class BurnIllusionEffect extends MobEffect {
	public static final UUID MODIFIER_UUID = UUID.fromString("8a29b055-9257-4db7-8bce-d816a8f6594c");

	public BurnIllusionEffect(MobEffectCategory mobEffectCategory, int color) {
		super(mobEffectCategory, color);
		addAttributeModifier(Attributes.MOVEMENT_SPEED, MODIFIER_UUID.toString(), 0D, AttributeModifier.Operation.MULTIPLY_TOTAL);
	}

	@Override
	public void applyEffectTick(LivingEntity entity, int amplifier) {
		if (entity.getRemainingFireTicks() > 0) return;
		if (entity.tickCount % 20 == 0) {
			entity.hurt(entity.damageSources().onFire(), Float.MIN_VALUE);
		}
	}

	@Override
	public boolean isDurationEffectTick(int duration, int amplifier) {
		return true;
	}

	@Override
	public void initializeClient(Consumer<IClientMobEffectExtensions> consumer) {
		consumer.accept(new BurnIllusionEffectClient());
	}

	private static class BurnIllusionEffectClient implements IClientMobEffectExtensions {

		@Override
		public boolean isVisibleInInventory(MobEffectInstance instance) {
			return false;
		}

		@Override
		public boolean isVisibleInGui(MobEffectInstance instance) {
			return false;
		}

		// Остальные методы можно удалить или оставить пустыми
		@Override
		public boolean renderInventoryIcon(MobEffectInstance instance, EffectRenderingInventoryScreen<?> screen,
										   GuiGraphics guiGraphics, int x, int y, int blitOffset) {
			return true; // Возвращаем true, чтобы не рисовалась иконка
		}

		@Override
		public boolean renderGuiIcon(MobEffectInstance instance, Gui gui, GuiGraphics guiGraphics,
									 int x, int y, float z, float alpha) {
			return true; // Возвращаем true, чтобы не рисовалась иконка
		}
	}
}