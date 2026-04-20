package net.dainplay.rpgworldmod.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.dainplay.rpgworldmod.RPGworldMod;
import net.dainplay.rpgworldmod.effect.ModEffects;
import net.dainplay.rpgworldmod.item.custom.ManaCostItem;
import net.dainplay.rpgworldmod.item.custom.OrbitingItem;
import net.dainplay.rpgworldmod.network.ClientAdditionalHealthCostData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;

import java.util.Objects;

import static java.lang.Math.max;

public class HealthOverlayEventHandler implements IGuiOverlay {
	public static final ResourceLocation ICONS = new ResourceLocation(RPGworldMod.MOD_ID, "textures/gui/icons.png");

	static int renderHeartY = 0;
	static int regen = -1;
	static int[] randomOffsets = new int[1024];


	private static long lastHighlightTime = 0;
	private static float highlightAlpha = 1.0f;
	private static boolean highlightIncreasing = false;

	public static void drawMossHeart(GuiGraphics stack, int x, int y, int textureX, int textureY, int width, int height) {
		stack.blit(ICONS, x, y, textureX, textureY, width, height);
	}

	public static void drawMosquitoHeart(GuiGraphics stack, int x, int y, int textureX, int textureY, int width, int height) {
		stack.blit(ICONS, x, y, textureX + ((mc.player.tickCount % 8 + 1) * 18), textureY, width, height);
	}

	public static void drawBurnoutHeart(GuiGraphics stack, int x, int y, int textureX, int textureY, int width, int height) {
		stack.blit(ICONS, x, y, textureX + ((mc.player.tickCount / 16) % 2) * 9, textureY + ((mc.player.tickCount % 16) * 12), width, height);
	}

	public static void setRenderHeartY(int value) {
		renderHeartY = value;
	}

	public static void setRegen(int value) {
		regen = value;
	}

	public static void setRandomOffset(int index, int value) {
		randomOffsets[index] = value;
	}

	private final static int UNKNOWN_VALUE = -1;
	private static int previousMossValue = UNKNOWN_VALUE;
	private static int previousMosquitoValue = UNKNOWN_VALUE;
	private static int previousBurnoutValue = UNKNOWN_VALUE;

	private static final Minecraft mc = Minecraft.getInstance();
	private static HeartIcon[] heartIcons = new HeartIcon[0];
	private static HeartIcon[] mosquitoIcons = new HeartIcon[0];
	private static HeartIcon[] burnoutIcons = new HeartIcon[0];

	public void render(ForgeGui gui, GuiGraphics guiGraphics, float partialTick, int screenWidth, int screenHeight) {
		if (!mc.options.hideGui && gui.shouldDrawSurvivalElements()) {
			gui.setupOverlayRenderState(true, false);


			boolean hasMossiosis = mc.player != null && mc.player.hasEffect(ModEffects.MOSSIOSIS.get());
			boolean hasMosquitoing = mc.player != null && mc.player.hasEffect(ModEffects.MOSQUITOING.get());
			boolean hasBurnout = mc.player != null && mc.player.hasEffect(ModEffects.BURNOUT.get());

			boolean hasHealthCostItem = false;
			int healthCost = 0;
			if (mc.player != null) {
				ItemStack mainHandItem = mc.player.getMainHandItem();
				ItemStack offHandItem = mc.player.getOffhandItem();
				if (mainHandItem.getItem() instanceof ManaCostItem manaCostItem) {
					if (mainHandItem.getItem() instanceof OrbitingItem orbitingItem) {
						hasHealthCostItem = orbitingItem.shouldOrbit(mainHandItem, mc.player) && manaCostItem.usesHealthInsteadOfMana(mainHandItem);
						healthCost = manaCostItem.getManaCost(mainHandItem, mc.player);
					} else {
						hasHealthCostItem = manaCostItem.usesHealthInsteadOfMana(mainHandItem);
						healthCost = manaCostItem.getManaCost(mainHandItem, mc.player);
					}
				} else if (offHandItem.getItem() instanceof ManaCostItem manaCostItem) {
					if (offHandItem.getItem() instanceof OrbitingItem orbitingItem) {
						hasHealthCostItem = orbitingItem.shouldOrbit(offHandItem, mc.player) && manaCostItem.usesHealthInsteadOfMana(offHandItem);
						healthCost = manaCostItem.getManaCost(offHandItem, mc.player);
					} else {
						hasHealthCostItem = manaCostItem.usesHealthInsteadOfMana(offHandItem);
						healthCost = manaCostItem.getManaCost(offHandItem, mc.player);
					}
				}
			}


			if (hasMossiosis) {
				renderMossBar(gui, guiGraphics, screenWidth, screenHeight);
			}
			if (hasMosquitoing) {
				renderMosquitoBar(gui, guiGraphics, screenWidth, screenHeight);
			}
			if (hasBurnout) {
				renderBurnoutBar(gui, guiGraphics, screenWidth, screenHeight);
			}


			if (hasHealthCostItem) {
				renderHealthCostHearts(gui, guiGraphics, screenWidth, screenHeight, healthCost);
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
			return Mth.ceil(mc.player.getMaxHealth()) + Mth.ceil(mc.player.getAbsorptionAmount());
		else return -1;
	}

	private static int calculateBurnoutValue() {
		if (mc.player != null && mc.player.hasEffect(ModEffects.BURNOUT.get()))
			return Math.min((Objects.requireNonNull(mc.player.getEffect(ModEffects.BURNOUT.get())).getAmplifier() + 1), Mth.ceil(mc.player.getHealth()));
		else return -1;
	}

	public static void renderMossBar(ForgeGui gui, GuiGraphics stack, int screenWidth, int screenHeight) {
		int currentMossValue = calculateMossValue();


		if (currentMossValue <= 0) {
			return;
		}

		int xStart = screenWidth / 2 - 91;
		int health = Mth.ceil(mc.player.getHealth());

		if (currentMossValue != previousMossValue) {
			heartIcons = HeartsBar.calculateHeartIcons(currentMossValue);
			previousMossValue = currentMossValue;
		}


		if (heartIcons == null || heartIcons.length == 0) {
			return;
		}


		int heartsToDraw = (currentMossValue + 1) / 2 + (currentMossValue + 1) % 2;


		int yPosition = renderHeartY;

		if (health > 0) {
			for (int i = heartsToDraw - 1; i >= 0; i--) {
				int xPosition = xStart + (i % 10) * 8;
				int currentY = yPosition - max(3, (11 - (Mth.ceil(mc.player.getMaxHealth()) + Mth.ceil(mc.player.getAbsorptionAmount()) - 2) / 20)) * (i / 10);

				if (health + mc.player.getAbsorptionAmount() <= 4) {
					currentY = randomOffsets[i];
				} else if (i == regen) {
					currentY -= 2;
				}


				if (i < heartIcons.length) {
					switch (heartIcons[i].heartIconType) {
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

	public static void renderBurnoutBar(ForgeGui gui, GuiGraphics stack, int screenWidth, int screenHeight) {
		if (mc.player == null) return;
		int currentBurnoutValue = calculateBurnoutValue();


		if (currentBurnoutValue <= 0) {
			return;
		}


		int currentHealth = Mth.ceil(mc.player.getHealth());
		int maxHealth = Mth.ceil(mc.player.getMaxHealth());
		int totalHearts = (maxHealth + Mth.ceil(mc.player.getAbsorptionAmount()) + 1) / 2;


		int xStart = screenWidth / 2 - 91;
		int yPosition = renderHeartY;


		color4f(1, 1, 1, 1);

		int heartsToDraw = currentBurnoutValue;
		int currentFullHearts = currentHealth / 2;
		boolean hasHalfHeart = (currentHealth % 2 == 1);


		int rightmostHeartIndex;
		if (hasHalfHeart) {
			rightmostHeartIndex = currentFullHearts;
		} else {
			rightmostHeartIndex = currentFullHearts - 1;
		}

		for (int offset = 0; offset < totalHearts; offset++) {
			if (heartsToDraw <= 0) break;


			int heartIndex = rightmostHeartIndex - offset;
			if (heartIndex < 0) {
				int rowsToAdd = (-heartIndex + 9) / 10;
				heartIndex = 9 - ((-heartIndex - 1) % 10);
			}


			int row = heartIndex / 10;
			int column = heartIndex % 10;
			int x = xStart + column * 8;
			int y = yPosition - Math.max(3, (11 - totalHearts / 10)) * row;


			if (currentHealth + mc.player.getAbsorptionAmount() <= 4) {
				y = randomOffsets[heartIndex];
			} else if (heartIndex == regen) {
				y -= 2;
			}
			y -= 4;


			int heartHealth;
			if (heartIndex == currentFullHearts && hasHalfHeart) {
				heartHealth = 1;
			} else if (heartIndex < currentFullHearts) {
				heartHealth = 2;
			} else {
				heartHealth = 0;
			}


			if (heartHealth == 0) {
				offset--;
				rightmostHeartIndex--;
				if (rightmostHeartIndex < 0) break;
				continue;
			}


			int healthToDrawInHeart = Math.min(heartsToDraw, heartHealth);


			boolean isRightHalf = false;
			if (healthToDrawInHeart == 1 && heartHealth == 2) {
				if (offset == 0 && heartsToDraw == 1) {
					isRightHalf = true;
				} else if (heartsToDraw == 1) {
					isRightHalf = true;
				}
			}


			if (healthToDrawInHeart == 2) {
				drawBurnoutHeart(stack, x, y, 0, 27, 9, 12);
			} else if (healthToDrawInHeart == 1) {
				if (heartHealth == 1) {
					drawBurnoutHeart(stack, x, y, 36, 27, 9, 12);
				} else if (isRightHalf) {
					drawBurnoutHeart(stack, x, y, 18, 27, 9, 12);
				} else {
					drawBurnoutHeart(stack, x, y, 36, 27, 9, 12);
				}
			}

			heartsToDraw -= healthToDrawInHeart;
		}
	}

	public static void renderMosquitoBar(ForgeGui gui, GuiGraphics stack, int screenWidth, int screenHeight) {
		int currentMosquitoValue = calculateMosquitoValue();


		if (currentMosquitoValue <= 0 || mc.player == null) {
			return;
		}

		int xStart = screenWidth / 2 - 91;


		int health = Mth.ceil(mc.player.getHealth());
		int maxHealth = Mth.ceil(mc.player.getMaxHealth());
		int absorption = Mth.ceil(mc.player.getAbsorptionAmount());


		int totalHealth = health + absorption;


		int totalRegularHearts = (maxHealth + 1) / 2;
		int absorptionHearts = (absorption + 1) / 2;


		int totalHeartsToDraw = totalRegularHearts + absorptionHearts;


		if (currentMosquitoValue != previousMosquitoValue) {
			mosquitoIcons = HeartsBar.calculateHeartIcons(currentMosquitoValue);
			previousMosquitoValue = currentMosquitoValue;
		}


		if (mosquitoIcons == null || mosquitoIcons.length == 0) {
			return;
		}


		int mosquitoHeartsToDraw = mosquitoIcons.length;


		int yPosition = renderHeartY;

		if (currentMosquitoValue > 0) {
			int drawnHearts = 0;
			int remainingHealth = currentMosquitoValue;


			for (int heartIndex = 0; heartIndex < totalHeartsToDraw && drawnHearts < mosquitoHeartsToDraw && remainingHealth > 0; heartIndex++) {
				int healthInThisHeart = 0;

				if (heartIndex < totalRegularHearts) {
					int heartStartHealth = heartIndex * 2;
					healthInThisHeart = Math.max(0, Math.min(2, health - heartStartHealth));
				} else {
					int absorptionHeartIndex = heartIndex - totalRegularHearts;
					int absorptionStart = absorptionHeartIndex * 2;
					healthInThisHeart = Math.max(0, Math.min(2, absorption - absorptionStart));
				}


				if (healthInThisHeart <= 0) {
					continue;
				}


				int mosquitoHealthInThisHeart = Math.min(healthInThisHeart, remainingHealth);


				if (mosquitoHealthInThisHeart <= 0) {
					continue;
				}


				int xPosition = xStart + (heartIndex % 10) * 8;


				int currentY = yPosition - max(3, (11 - (totalHeartsToDraw - 1) / 10)) * (heartIndex / 10);

				if (totalHealth <= 4) {
					currentY = randomOffsets[heartIndex];
				} else if (heartIndex == regen) {
					currentY -= 2;
				}


				if (mosquitoHealthInThisHeart == 2) {
					drawMosquitoHeart(stack, xPosition, currentY, 0, (mc.player.level().getLevelData().isHardcore() ? 9 : 0), 9, 9);
				} else if (mosquitoHealthInThisHeart == 1) {
					drawMosquitoHeart(stack, xPosition, currentY, 9, (mc.player.level().getLevelData().isHardcore() ? 9 : 0), 9, 9);
				}


				drawnHearts++;

				remainingHealth -= mosquitoHealthInThisHeart;
			}
		}

		color4f(1, 1, 1, 1);
	}


	private static void updateHealthCostHighlight() {
		if (mc.player == null) return;

		int tick = mc.player.tickCount;

		float progress = (tick % 20) / 20.0f;
		highlightAlpha = 0.3f + 0.7f * (progress < 0.5f ? progress * 2 : 2 - progress * 2);
	}

	public static void renderHealthCostHearts(ForgeGui gui, GuiGraphics stack, int screenWidth, int screenHeight, int healthCost) {
		if (mc.player == null) return;


		healthCost += ClientAdditionalHealthCostData.get();
		if (healthCost <= 0) return;


		int currentHealth = Mth.ceil(mc.player.getHealth());
		int maxHealth = Mth.ceil(mc.player.getMaxHealth());
		int totalHearts = (maxHealth + Mth.ceil(mc.player.getAbsorptionAmount()) + 1) / 2;


		boolean useRedOutline = healthCost >= currentHealth;


		int baseY = 18;


		int xStart = screenWidth / 2 - 91;
		int yPosition = renderHeartY;


		updateHealthCostHighlight();


		color4f(1, 1, 1, highlightAlpha);

		if (useRedOutline) {
			int heartsToOutline = Math.min(healthCost, maxHealth);

			for (int i = 0; i < totalHearts; i++) {
				if (heartsToOutline <= 0) break;


				int row = i / 10;
				int column = i % 10;
				int x = xStart + column * 8;
				int y = yPosition - Math.max(3, (11 - (totalHearts - 1) / 10)) * row;


				if (currentHealth + mc.player.getAbsorptionAmount() <= 4) {
					y = randomOffsets[i];
				} else if (i == regen) {
					y -= 2;
				}


				int heartHealth;
				if (i == totalHearts - 1 && maxHealth % 2 == 1) {
					heartHealth = 1;
				} else {
					heartHealth = 2;
				}


				int healthToOutlineInHeart = Math.min(heartsToOutline, heartHealth);


				if (healthToOutlineInHeart == 2) {
					stack.blit(ICONS, x, y, 45, baseY, 9, 9);
				} else if (healthToOutlineInHeart == 1) {
					if (heartHealth == 1) {
						stack.blit(ICONS, x, y, 54, baseY, 9, 9);
					} else {
						stack.blit(ICONS, x, y, 54, baseY, 9, 9);
					}
				}

				heartsToOutline -= healthToOutlineInHeart;
			}
		} else {
			int heartsToOutline = healthCost;
			int currentFullHearts = currentHealth / 2;
			boolean hasHalfHeart = (currentHealth % 2 == 1);


			int rightmostHeartIndex;
			if (hasHalfHeart) {
				rightmostHeartIndex = currentFullHearts;
			} else {
				rightmostHeartIndex = currentFullHearts - 1;
			}

			for (int offset = 0; offset < totalHearts; offset++) {
				if (heartsToOutline <= 0) break;


				int heartIndex = rightmostHeartIndex - offset;
				if (heartIndex < 0) {
					int rowsToAdd = (-heartIndex + 9) / 10;
					heartIndex = 9 - ((-heartIndex - 1) % 10);
				}


				int row = heartIndex / 10;
				int column = heartIndex % 10;
				int x = xStart + column * 8;
				int y = yPosition - Math.max(3, (11 - (totalHearts - 1) / 10)) * row;


				if (currentHealth + mc.player.getAbsorptionAmount() <= 4) {
					y = randomOffsets[heartIndex];
				} else if (heartIndex == regen) {
					y -= 2;
				}


				int heartHealth;
				if (heartIndex == currentFullHearts && hasHalfHeart) {
					heartHealth = 1;
				} else if (heartIndex < currentFullHearts) {
					heartHealth = 2;
				} else {
					heartHealth = 0;
				}


				if (heartHealth == 0) {
					offset--;
					rightmostHeartIndex--;
					if (rightmostHeartIndex < 0) break;
					continue;
				}


				int healthToOutlineInHeart = Math.min(heartsToOutline, heartHealth);


				boolean isRightHalf = false;
				if (healthToOutlineInHeart == 1 && heartHealth == 2) {
					if (offset == 0 && heartsToOutline == 1) {
						isRightHalf = true;
					} else if (heartsToOutline == 1) {
						isRightHalf = true;
					}
				}


				if (healthToOutlineInHeart == 2) {
					stack.blit(ICONS, x, y, 18, baseY, 9, 9);
				} else if (healthToOutlineInHeart == 1) {
					if (heartHealth == 1) {
						stack.blit(ICONS, x, y, 27, baseY, 9, 9);
					} else if (isRightHalf) {
						stack.blit(ICONS, x, y, 36, baseY, 9, 9);
					} else {
						stack.blit(ICONS, x, y, 27, baseY, 9, 9);
					}
				}

				heartsToOutline -= healthToOutlineInHeart;
			}
		}

		color4f(1, 1, 1, 1);
	}

	private static void color4f(float r, float g, float b, float a) {
		RenderSystem.setShaderColor(r, g, b, a);
	}
}