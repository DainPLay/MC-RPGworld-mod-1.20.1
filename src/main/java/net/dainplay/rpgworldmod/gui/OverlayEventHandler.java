package net.dainplay.rpgworldmod.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.dainplay.rpgworldmod.RPGworldMod;
import net.dainplay.rpgworldmod.effect.ModEffects;
import net.dainplay.rpgworldmod.entity.custom.Drillhog;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;

import java.util.Objects;
import java.util.Random;

import static java.lang.Math.max;

public class OverlayEventHandler implements IGuiOverlay {
	public static final ResourceLocation ICONS = new ResourceLocation(RPGworldMod.MOD_ID, "textures/gui/icons.png");

	static int renderHeartY = 0;
	static int regen = -1;

	public static void drawMossHeart(GuiGraphics stack, int x, int y, int textureX, int textureY, int width, int height) {
		stack.blit(ICONS, x, y, textureX, textureY, width, height);
	}

	public static void drawMosquitoHeart(GuiGraphics stack, int x, int y, int textureX, int textureY, int width, int height) {
		stack.blit(ICONS, x, y, textureX+((mc.player.tickCount % 8 +1) * 18), textureY, width, height);
	}

	public static void setRenderHeartY(int value) {
		renderHeartY = value;
	}

	public static void setRegen(int value) {
		regen = value;
	}

	private final static int UNKNOWN_ARMOR_VALUE = -1;
	private static int previousMossValue = UNKNOWN_ARMOR_VALUE;
	private static int previousMosquitoValue = UNKNOWN_ARMOR_VALUE;

	private static final Minecraft mc = Minecraft.getInstance();
	private static MossIcon[] mossIcons = new MossIcon[0];
	private static MossIcon[] mosquitoIcons = new MossIcon[0];

	public void render(ForgeGui gui, GuiGraphics guiGraphics, float partialTick, int screenWidth, int screenHeight) {
		if (!mc.options.hideGui && gui.shouldDrawSurvivalElements()) {
			gui.setupOverlayRenderState(true, false);

			// Проверяем наличие эффектов
			boolean hasMossiosis = mc.player != null && mc.player.hasEffect(ModEffects.MOSSIOSIS.get());
			boolean hasMosquitoing = mc.player != null && mc.player.hasEffect(ModEffects.MOSQUITOING.get());

			// Рендерим бары в зависимости от наличия эффектов
			if (hasMossiosis) {
				renderMossBar(gui, guiGraphics, screenWidth, screenHeight);
			}
			if (hasMosquitoing) {
				renderMosquitoBar(gui, guiGraphics, screenWidth, screenHeight);
			}
		}
	}

	private static int calculateMossValue() {
		if (mc.player != null && mc.player.hasEffect(ModEffects.MOSSIOSIS.get()))
			return (Objects.requireNonNull(mc.player.getEffect(ModEffects.MOSSIOSIS.get())).getAmplifier() + 1) * 6;
		else return -1;
	}

	private static int calculateMosquitoValue() {
		if (mc.player != null && mc.player.hasEffect(ModEffects.MOSQUITOING.get()))
			return Mth.ceil(mc.player.getHealth());
		else return -1;
	}

	public static void renderMossBar(ForgeGui gui, GuiGraphics stack, int screenWidth, int screenHeight) {
		int currentMossValue = calculateMossValue();

		// Если эффекта нет, не рисуем ничего
		if (currentMossValue <= 0) {
			return;
		}

		int xStart = screenWidth / 2 - 91;
		int health = Mth.ceil(mc.player.getHealth());

		if (currentMossValue != previousMossValue) {
			mossIcons = MossBar.calculateMossIcons(currentMossValue);
			previousMossValue = currentMossValue;
		}

		// Проверяем, что массив проинициализирован
		if (mossIcons == null || mossIcons.length == 0) {
			return;
		}

		// Ограничиваем количество сердец 10
		int heartsToDraw = (currentMossValue + 1) / 2 + (currentMossValue + 1) % 2;

		// Определяем позицию Y в зависимости от наличия mossBar
		int yPosition = renderHeartY;

		if (health > 0) {
			for (int i = heartsToDraw - 1; i >= 0; i--) {
				int xPosition = xStart + (i % 10) * 8;
				int currentY = yPosition - max(3,(11 - Mth.ceil(mc.player.getMaxHealth()) / 20)) * (i / 10);

				if (health <= 4) {
					currentY += new Random().nextInt(2);
				}
				if (i == regen) {
					currentY -= 2;
				}

				// Безопасный доступ к массиву
				if (i < mossIcons.length) {
					switch (mossIcons[i].mossIconType) {
						case NONE:
							drawMossHeart(stack, xPosition, currentY, 0, (mc.player.level().getLevelData().isHardcore() ? 9 : 0), 9, 9);
							break;
						case HALF:
							drawMossHeart(stack, xPosition, currentY, 9, (mc.player.level().getLevelData().isHardcore() ? 9 : 0), 9, 9);
							break;
						case FULL:
							drawMossHeart(stack, xPosition, currentY, 0, (mc.player.level().getLevelData().isHardcore() ? 9 : 0), 9, 9);
							break;
						default:
							break;
					}
				}
			}
		}

		color4f(1, 1, 1, 1);
	}

	public static void renderMosquitoBar(ForgeGui gui, GuiGraphics stack, int screenWidth, int screenHeight) {
		int currentMosquitoValue = calculateMosquitoValue();

		// Если эффекта нет, не рисуем ничего
		if (currentMosquitoValue <= 0) {
			return;
		}

		int xStart = screenWidth / 2 - 91;
		int health = Mth.ceil(mc.player.getHealth());

		if (currentMosquitoValue != previousMosquitoValue) {
			mosquitoIcons = MossBar.calculateMossIcons(currentMosquitoValue);
			previousMosquitoValue = currentMosquitoValue;
		}

		// Проверяем, что массив проинициализирован
		if (mosquitoIcons == null || mosquitoIcons.length == 0) {
			return;
		}

		// Ограничиваем количество сердец 10
		int heartsToDraw = currentMosquitoValue / 2 + currentMosquitoValue % 2;

		// Определяем позицию Y в зависимости от наличия mossBar
		int yPosition = renderHeartY;

		if (health > 0) {
			for (int i = heartsToDraw - 1; i >= 0; i--) {
				int xPosition = xStart + (i % 10) * 8;
				int currentY = yPosition - max(3,(11 - Mth.ceil(mc.player.getMaxHealth()) / 20)) * (i / 10);

				if (health <= 4) {
					currentY += new Random().nextInt(2);
				}
				if (i == regen) {
					currentY -= 2;
				}

				// Безопасный доступ к массиву
				if (i < mosquitoIcons.length) {
					switch (mosquitoIcons[i].mossIconType) {
						case NONE:
							drawMosquitoHeart(stack, xPosition, currentY, 0, (mc.player.level().getLevelData().isHardcore() ? 9 : 0), 9, 9);
							break;
						case HALF:
							drawMosquitoHeart(stack, xPosition, currentY, 9, (mc.player.level().getLevelData().isHardcore() ? 9 : 0), 9, 9);
							break;
						case FULL:
							drawMosquitoHeart(stack, xPosition, currentY, 0, (mc.player.level().getLevelData().isHardcore() ? 9 : 0), 9, 9);
							break;
						default:
							break;
					}
				}
			}
		}

		color4f(1, 1, 1, 1);
	}

	private static void color4f(float r, float g, float b, float a) {
		RenderSystem.setShaderColor(r, g, b, a);
	}
}