package net.dainplay.rpgworldmod.gui;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.dainplay.rpgworldmod.RPGworldMod;
import net.dainplay.rpgworldmod.enchantment.ModEnchantments;
import net.dainplay.rpgworldmod.item.custom.StaffItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import org.lwjgl.opengl.GL11;

import java.util.Map;

public class ChargesOverlayEventHandler implements IGuiOverlay {
	public static final ResourceLocation ICONS = new ResourceLocation(RPGworldMod.MOD_ID, "textures/gui/icons.png");
	private static final Minecraft mc = Minecraft.getInstance();
	private static boolean active = false;

	public static boolean isActive() {
		return active;
	}

	@Override
	public void render(ForgeGui gui, GuiGraphics guiGraphics, float partialTick, int screenWidth, int screenHeight) {
		active = true;
		if (mc.options.hideGui || mc.player == null) {
			active = false;
			return;
		}

		// Определяем стек с посохом (сначала правая рука, потом левая)
		ItemStack stack = mc.player.getMainHandItem();
		if (!(stack.getItem() instanceof StaffItem)) {
			stack = mc.player.getOffhandItem();
			if (!(stack.getItem() instanceof StaffItem)) {
				active = false;
				return;
			}
		}

		StaffItem staff = (StaffItem) stack.getItem();

		// Максимальное количество зарядов
		int maxCooldown = staff.getMaxCooldown(stack);
		int useCooldown = staff.getUseCooldown(stack);
		int maxCharges = maxCooldown / useCooldown;

		// Не показываем, если максимум <= 1
		if (useCooldown >= maxCooldown && stack.getEnchantmentLevel(ModEnchantments.DOUBLE_EXPOSURE.get()) <= 0) {
			active = false;
			return;
		}

		// Текущее количество зарядов через кулдаун
		int currentCharges = 0;
		Map<Item, ItemCooldowns.CooldownInstance> cooldownsMap = Minecraft.getInstance().player.getCooldowns().cooldowns;
		int currentTick = Minecraft.getInstance().player.getCooldowns().tickCount;
		ItemCooldowns.CooldownInstance instance = cooldownsMap.get(stack.getItem());
		if (instance == null) {
			currentCharges = maxCooldown / useCooldown;
			if (stack.getEnchantmentLevel(ModEnchantments.DOUBLE_EXPOSURE.get()) > 0) {
				maxCharges += (maxCooldown - (maxCharges * useCooldown) + 2 * maxCooldown) / (useCooldown * 2);
				currentCharges = maxCharges;
			}
		} else {
			int endTick = instance.endTime;
			if (stack.getEnchantmentLevel(ModEnchantments.DOUBLE_EXPOSURE.get()) > 0) {
				maxCharges += (maxCooldown - (maxCharges * useCooldown) + 2 * maxCooldown) / (useCooldown * 2);
				int currentCooldown = endTick - currentTick;
				while (maxCooldown*3 >= currentCooldown) {
					if (currentCooldown+useCooldown <= maxCooldown) {
						currentCooldown += useCooldown;
						currentCharges += 1;
					}
					else {
						currentCooldown += useCooldown*2;
						if(currentCooldown <= maxCooldown*3) currentCharges += 1;
					}
				}
			}
			else {
				currentCharges = (maxCooldown - (endTick - currentTick)) / useCooldown;
			}
		}
		currentCharges = Math.min(currentCharges, maxCharges); // на всякий случай

		// Расчёт позиции по центру экрана
		int iconSize = 3;
		int spacing = 1;
		int totalWidth = maxCharges * iconSize + (maxCharges - 1) * spacing;
		int startX = (screenWidth - totalWidth) / 2;
		int y = screenHeight / 2 + 6; // чуть ниже прицела

		RenderSystem.enableBlend();
		RenderSystem.blendFuncSeparate(
				GlStateManager.SourceFactor.ONE_MINUS_DST_COLOR,
				GlStateManager.DestFactor.ONE_MINUS_SRC_COLOR,
				GlStateManager.SourceFactor.ONE,
				GlStateManager.DestFactor.ZERO
		);

		// Рендерим иконки слева направо: сначала полные, потом пустые
		for (int i = 0; i < maxCharges; i++) {
			boolean isFull = i < currentCharges;
			int textureX = isFull ? 57 : 54;
			int textureY = 27;
			int x = startX + i * (iconSize + spacing);
			guiGraphics.blit(ICONS, x, y, textureX, textureY, iconSize, iconSize);
		}

		RenderSystem.defaultBlendFunc();
		RenderSystem.disableBlend();
		// Сбрасываем цвет (на случай, если предыдущие оверлеи его меняли)
		RenderSystem.setShaderColor(1, 1, 1, 1);
	}
}