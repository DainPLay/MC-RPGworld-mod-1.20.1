package net.dainplay.rpgworldmod.effect;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.client.extensions.common.IClientMobEffectExtensions;

import java.util.function.Consumer;

public class NetherPortalIllusionEffect extends MobEffect {

	public NetherPortalIllusionEffect(MobEffectCategory mobEffectCategory, int color) {
		super(mobEffectCategory, color);
	}

	@Override
	public boolean isDurationEffectTick(int duration, int amplifier) {
		return true;
	}

	@Override
	public void initializeClient(Consumer<IClientMobEffectExtensions> consumer) {
		consumer.accept(new NetherPortalIllusionEffectClient());
	}

	private static class NetherPortalIllusionEffectClient implements IClientMobEffectExtensions {

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