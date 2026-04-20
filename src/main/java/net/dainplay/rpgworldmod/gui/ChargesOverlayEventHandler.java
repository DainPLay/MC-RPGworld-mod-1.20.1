package net.dainplay.rpgworldmod.gui;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.dainplay.rpgworldmod.RPGworldMod;
import net.dainplay.rpgworldmod.enchantment.ModEnchantments;
import net.dainplay.rpgworldmod.item.custom.SculkStaffItem;
import net.dainplay.rpgworldmod.item.custom.StaffItem;
import net.dainplay.rpgworldmod.network.ClientSculkStaffCDData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;

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
		if (!mc.options.getCameraType().isFirstPerson()) {
			active = false;
			return;
		}
		if (mc.player.isSpectator()) {
			active = false;
			return;
		}

		ItemStack stack = mc.player.getMainHandItem();
		if (!(stack.getItem() instanceof StaffItem)) {
			stack = mc.player.getOffhandItem();
			if (!(stack.getItem() instanceof StaffItem)) {
				active = false;
				return;
			}
		}

		StaffItem staff = (StaffItem) stack.getItem();

		if (staff instanceof SculkStaffItem) {
			renderTimeBar(guiGraphics, stack, (SculkStaffItem) staff, screenWidth, screenHeight);
			active = true;
			return;
		}

		int maxCooldown = staff.getMaxCooldown(stack);
		int useCooldown = staff.getUseCooldown(stack);
		int maxCharges = maxCooldown / useCooldown;

		if (useCooldown >= maxCooldown && stack.getEnchantmentLevel(ModEnchantments.DOUBLE_EXPOSURE.get()) <= 0) {
			active = false;
			return;
		}

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
				while (maxCooldown * 3 >= currentCooldown) {
					if (currentCooldown + useCooldown <= maxCooldown) {
						currentCooldown += useCooldown;
						currentCharges += 1;
					} else {
						currentCooldown += useCooldown * 2;
						if (currentCooldown <= maxCooldown * 3) currentCharges += 1;
					}
				}
			} else {
				currentCharges = (maxCooldown - (endTick - currentTick)) / useCooldown;
			}
		}
		currentCharges = Math.min(currentCharges, maxCharges);

		int iconSize = 3;
		int spacing = 1;
		int totalWidth = maxCharges * iconSize + (maxCharges - 1) * spacing;
		int startX = (screenWidth - totalWidth) / 2;
		int y = screenHeight / 2 + 6;

		RenderSystem.enableBlend();
		RenderSystem.blendFuncSeparate(
				GlStateManager.SourceFactor.ONE_MINUS_DST_COLOR,
				GlStateManager.DestFactor.ONE_MINUS_SRC_COLOR,
				GlStateManager.SourceFactor.ONE,
				GlStateManager.DestFactor.ZERO
		);

		for (int i = 0; i < maxCharges; i++) {
			boolean isFull = i < currentCharges;
			int textureX = isFull ? 57 : 54;
			int textureY = 27;
			int x = startX + i * (iconSize + spacing);
			guiGraphics.blit(ICONS, x, y, textureX, textureY, iconSize, iconSize);
		}

		RenderSystem.defaultBlendFunc();
		RenderSystem.disableBlend();
		RenderSystem.setShaderColor(1, 1, 1, 1);
	}

	private void renderTimeBar(GuiGraphics guiGraphics, ItemStack stack, SculkStaffItem staff, int screenWidth, int screenHeight) {
		int maxDuration = staff.getMaxCooldown(stack);
		int currentTicks = 0;
		boolean onCooldown = false;

		ItemStack temp1 = mc.player.getUseItem().copy();
		ItemStack temp2 = stack.copy();
		if (temp1.hasTag() && temp1.getTag().contains("caughtVibration") && temp1.getTag().getInt("caughtVibration") > 0) {
			temp1.getTag().remove("caughtVibration");
		}
		if (temp2.hasTag() && temp2.getTag().contains("caughtVibration") && temp2.getTag().getInt("caughtVibration") > 0) {
			temp2.getTag().remove("caughtVibration");
		}
		if (temp1.hasTag() && temp1.getTag().contains("startingCooldown") && temp1.getTag().getInt("startingCooldown") > 0) {
			temp1.getTag().remove("startingCooldown");
		}
		if (temp2.hasTag() && temp2.getTag().contains("startingCooldown") && temp2.getTag().getInt("startingCooldown") > 0) {
			temp2.getTag().remove("startingCooldown");
		}
		if (mc.player.isUsingItem() && ItemStack.isSameItemSameTags(temp1, temp2) && staff.isOffCooldown(stack, mc.player)) {
			currentTicks = mc.player.getTicksUsingItem();
			if (stack.getTag() != null && stack.getTag().contains("startingCooldown", Tag.TAG_INT) && stack.getOrCreateTag().getInt("startingCooldown") > 0) {
				currentTicks += stack.getTag().getInt("startingCooldown");
			}
		} else {
			Map<Item, ItemCooldowns.CooldownInstance> cooldownsMap = Minecraft.getInstance().player.getCooldowns().cooldowns;
			int currentTick = Minecraft.getInstance().player.getCooldowns().tickCount;
			ItemCooldowns.CooldownInstance instance = cooldownsMap.get(stack.getItem());
			if (instance != null) {
				int endTick = instance.endTime;
				int cooldownTicks = endTick - currentTick;
				if (cooldownTicks > maxDuration) {
					cooldownTicks = maxDuration + ((cooldownTicks - maxDuration) / 2);
				}
				currentTicks = cooldownTicks;
			} else {
				int cooldownTicks = ClientSculkStaffCDData.get();
				if (cooldownTicks > maxDuration) {
					cooldownTicks = maxDuration + ((cooldownTicks - maxDuration) / 2);
				}
				currentTicks = cooldownTicks;
			}
			onCooldown = true;
		}
		int additionalTicks = 0;
		if (currentTicks > maxDuration) {
			additionalTicks = currentTicks - maxDuration;
			currentTicks = maxDuration;
		}
		float progress = (float) currentTicks / (float) maxDuration;
		float additionalProgress = (float) additionalTicks / (float) maxDuration;
		progress = 1F - Math.min(1.0F, Math.max(0.0F, progress));
		additionalProgress = 1F - Math.min(1.0F, Math.max(0.0F, additionalProgress));
		boolean hasDoubleExposure = stack.getEnchantmentLevel(ModEnchantments.DOUBLE_EXPOSURE.get()) > 0;
		if (!hasDoubleExposure && progress == 1F) onCooldown = false;
		if (hasDoubleExposure && additionalProgress == 1F) onCooldown = false;

		int barWidth = 15;
		int barHeight = 2;
		int spacing = 1;

		int totalWidth;
		if (hasDoubleExposure) {
			totalWidth = barWidth * 2 + spacing;
		} else {
			totalWidth = barWidth;
		}
		int startX = (screenWidth - totalWidth) / 2;
		int y = screenHeight / 2 + 6;

		RenderSystem.enableBlend();
		RenderSystem.blendFuncSeparate(
				GlStateManager.SourceFactor.ONE_MINUS_DST_COLOR,
				GlStateManager.DestFactor.ONE_MINUS_SRC_COLOR,
				GlStateManager.SourceFactor.ONE,
				GlStateManager.DestFactor.ZERO
		);

		if (hasDoubleExposure) {
			drawSingleBar(guiGraphics, startX, y, barWidth, barHeight, additionalProgress, onCooldown);
			drawSingleBar(guiGraphics, startX + barWidth + spacing, y, barWidth, barHeight, progress, onCooldown);
		} else {
			drawSingleBar(guiGraphics, startX, y, barWidth, barHeight, progress, onCooldown);
		}

		RenderSystem.defaultBlendFunc();
		RenderSystem.disableBlend();
		RenderSystem.setShaderColor(1, 1, 1, 1);
	}

	private void drawSingleBar(GuiGraphics guiGraphics, int x, int y, int width, int height, float progress, boolean onCooldown) {
		guiGraphics.blit(ICONS, x, y, 54, onCooldown ? 34 : 30, width, height);
		int fillWidth = (int) (width * progress);
		if (!onCooldown) fillWidth = Math.min(width, fillWidth + 1);
		if (progress == 0F) fillWidth = 0;
		if (fillWidth > 0) {
			guiGraphics.blit(ICONS, x, y, 54, onCooldown ? 36 : 32, fillWidth, height);
		}
	}
}