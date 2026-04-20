package net.dainplay.rpgworldmod.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.dainplay.rpgworldmod.RPGworldMod;
import net.dainplay.rpgworldmod.effect.ModEffects;
import net.dainplay.rpgworldmod.item.custom.ManaCostItem;
import net.dainplay.rpgworldmod.item.custom.OrbitingItem;
import net.dainplay.rpgworldmod.network.ClientIsManaRegenBlockedData;
import net.dainplay.rpgworldmod.network.ClientManaData;
import net.dainplay.rpgworldmod.network.ClientMaxManaData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;

import java.util.Random;

import static java.lang.Math.max;

public class ManaOverlayEventHandler implements IGuiOverlay {
	public static final ResourceLocation ICONS = new ResourceLocation(RPGworldMod.MOD_ID, "textures/gui/icons.png");

	private static long lastManaTime = 0;
	private static long manaBlinkTime = 0;
	private static int lastMana = -1;
	private static int displayMana = -1;
	private static boolean restoreBlink = false;
	private static boolean spendBlink = false;
	private static boolean fullManaBlink = false;
	private static int blinkStarIndex = -1;
	private static int spentManaAmount = 0;
	private static long fullManaDisplayTime = 0;
	private static boolean showManaBar = false;
	private static long fullManaBlinkEndTime = 0;
	private static int manaBeforeSpend = -1;

	private static int manaCostToShow = 0;
	private static long lastHighlightTime = 0;
	private static float highlightAlpha = 1.0f;
	private static boolean highlightIncreasing = false;

	public static void drawStar(GuiGraphics stack, int x, int y, int textureX, int textureY, int width, int height) {
		stack.blit(ICONS, x, y, textureX, textureY, width, height);
	}

	public static int isAirRender() {
		Player player = Minecraft.getInstance().player;
		int to_return = 0;
		if (player != null) {
			boolean isUnderwater = player.isEyeInFluid(net.minecraft.tags.FluidTags.WATER);
			boolean hasLowAir = player.getAirSupply() < player.getMaxAirSupply();

			if (isUnderwater || hasLowAir) {
				to_return += 10;
			}
			Entity vehicle = player.getVehicle();
			if (vehicle instanceof LivingEntity) {
				float health = ((LivingEntity) vehicle).getHealth();
				if (health > 40) {
					to_return += 20;
				} else if (health > 20) {
					to_return += 10;
				}
			}
		}
		return to_return;
	}

	private final static int UNKNOWN_VALUE = -1;
	private static int previousManaValue = UNKNOWN_VALUE;
	private static int previousMaxManaValue = UNKNOWN_VALUE;

	private static final Minecraft mc = Minecraft.getInstance();
	private static ManaIcon[] manaIcons = new ManaIcon[0];
	private static ManaIcon[] maxManaIcons = new ManaIcon[0];

	private static int[] createHappinessOffset(int maxMana, int tickCount) {
		int[] offset = new int[maxMana];
		if (maxMana > 0) {
			int position = tickCount % maxMana;
			offset[position] = -2;
		}
		return offset;
	}

	public void render(ForgeGui gui, GuiGraphics guiGraphics, float partialTick, int screenWidth, int screenHeight) {
		if (!mc.options.hideGui && gui.shouldDrawSurvivalElements()) {
			gui.setupOverlayRenderState(true, false);

			int maxMana = ClientMaxManaData.get();

			boolean hasManaCostItem = false;
			manaCostToShow = 0;

			if (mc.player != null) {
				ItemStack mainHandItem = mc.player.getMainHandItem();
				ItemStack offHandItem = mc.player.getOffhandItem();

				if (mainHandItem.getItem() instanceof ManaCostItem manaCostItem) {
					if (mainHandItem.getItem() instanceof OrbitingItem orbitingItem)
						hasManaCostItem = orbitingItem.shouldOrbit(mainHandItem, mc.player) && !manaCostItem.usesHealthInsteadOfMana(mainHandItem);
					else hasManaCostItem = !manaCostItem.usesHealthInsteadOfMana(mainHandItem);
					if (hasManaCostItem)
						manaCostToShow = ((ManaCostItem) mainHandItem.getItem()).getManaCost(mainHandItem, mc.player);
				} else if (offHandItem.getItem() instanceof ManaCostItem manaCostItem) {
					if (offHandItem.getItem() instanceof OrbitingItem orbitingItem)
						hasManaCostItem = orbitingItem.shouldOrbit(offHandItem, mc.player) && !manaCostItem.usesHealthInsteadOfMana(offHandItem);
					else hasManaCostItem = !manaCostItem.usesHealthInsteadOfMana(offHandItem);
					if (hasManaCostItem)
						manaCostToShow = ((ManaCostItem) offHandItem.getItem()).getManaCost(offHandItem, mc.player);
				}
			}

			if (shouldRenderMana() && mc.player != null) {
				int[] randomOffset;
				if (mc.player.hasEffect(ModEffects.HAPPINESS.get()))
					randomOffset = createHappinessOffset(maxMana / 3, mc.player.tickCount);
				else randomOffset = new Random(mc.player.tickCount).ints(0, 2).limit(maxMana).toArray();

				updateManaBlink();
				updateManaCostHighlight();

				renderManaBG(gui, guiGraphics, screenWidth, screenHeight, randomOffset);
				renderManaBar(gui, guiGraphics, screenWidth, screenHeight, randomOffset);
				if (hasManaCostItem && manaCostToShow > 0) {
					renderManaCostHighlight(guiGraphics, screenWidth, screenHeight, randomOffset);
				}
			}
		}
	}

	public static boolean shouldRenderMana() {
		if (mc.player == null) return false;
		boolean hasManaCostItem = false;
		ItemStack mainHandItem = mc.player.getMainHandItem();
		ItemStack offHandItem = mc.player.getOffhandItem();
		if (mainHandItem.getItem() instanceof ManaCostItem manaCostItem) {
			if (mainHandItem.getItem() instanceof OrbitingItem orbitingItem)
				hasManaCostItem = orbitingItem.shouldOrbit(mainHandItem, mc.player) && !manaCostItem.usesHealthInsteadOfMana(mainHandItem);
			else hasManaCostItem = !manaCostItem.usesHealthInsteadOfMana(mainHandItem);
			if (hasManaCostItem)
				manaCostToShow = ((ManaCostItem) mainHandItem.getItem()).getManaCost(mainHandItem, mc.player);
		} else if (offHandItem.getItem() instanceof ManaCostItem manaCostItem) {
			if (offHandItem.getItem() instanceof OrbitingItem orbitingItem)
				hasManaCostItem = orbitingItem.shouldOrbit(offHandItem, mc.player) && !manaCostItem.usesHealthInsteadOfMana(offHandItem);
			else hasManaCostItem = !manaCostItem.usesHealthInsteadOfMana(offHandItem);
			if (hasManaCostItem)
				manaCostToShow = ((ManaCostItem) offHandItem.getItem()).getManaCost(offHandItem, mc.player);
		}
		return ClientManaData.get() < ClientMaxManaData.get() ||
				System.currentTimeMillis() < fullManaDisplayTime ||
				showManaBar ||
				hasManaCostItem || ClientIsManaRegenBlockedData.get() > 0;
	}

	private static void updateManaBlink() {
		int currentMana = calculateManaValue();
		int maxMana = calculateMaxManaValue();
		if (currentMana < 0) return;

		long currentTime = System.currentTimeMillis();

		if (currentMana != lastMana) {
			if (currentMana > lastMana) {
				int restored = currentMana - lastMana;

				if (currentMana % 5 == 0 && restored > 0) {
					restoreBlink = true;
					blinkStarIndex = (currentMana - 1) / 5;
					lastManaTime = currentTime;
					manaBlinkTime = currentTime + 200;
				}

				if (currentMana == maxMana && lastMana < maxMana) {
					fullManaBlink = true;
					fullManaDisplayTime = currentTime + 1000;
					fullManaBlinkEndTime = currentTime + 200;
					manaBlinkTime = Math.max(manaBlinkTime, currentTime + 200);
					showManaBar = true;
				}
			} else if (currentMana < lastMana) {
				spentManaAmount = lastMana - currentMana;
				spendBlink = true;
				manaBeforeSpend = lastMana;
				lastManaTime = currentTime;
				manaBlinkTime = currentTime + 200;

				fullManaDisplayTime = 0;
				fullManaBlink = false;
				fullManaBlinkEndTime = 0;
				showManaBar = true;
			}

			lastMana = currentMana;
		}

		if (currentTime > manaBlinkTime) {
			restoreBlink = false;
			spendBlink = false;
			blinkStarIndex = -1;
			spentManaAmount = 0;
			manaBeforeSpend = -1;
		}

		if (currentTime > fullManaBlinkEndTime) {
			fullManaBlink = false;
		}

		boolean hasManaCostItem = false;
		if (mc.player != null) {
			ItemStack mainHandItem = mc.player.getMainHandItem();
			ItemStack offHandItem = mc.player.getOffhandItem();
			if (mainHandItem.getItem() instanceof ManaCostItem manaCostItem) {
				if (mainHandItem.getItem() instanceof OrbitingItem orbitingItem)
					hasManaCostItem = orbitingItem.shouldOrbit(mainHandItem, mc.player) && !manaCostItem.usesHealthInsteadOfMana(mainHandItem);
				else hasManaCostItem = !manaCostItem.usesHealthInsteadOfMana(mainHandItem);
			} else if (offHandItem.getItem() instanceof ManaCostItem manaCostItem) {
				if (offHandItem.getItem() instanceof OrbitingItem orbitingItem)
					hasManaCostItem = orbitingItem.shouldOrbit(offHandItem, mc.player) && !manaCostItem.usesHealthInsteadOfMana(offHandItem);
				else hasManaCostItem = !manaCostItem.usesHealthInsteadOfMana(offHandItem);
			}
		}

		if (currentTime > fullManaDisplayTime && currentMana == maxMana && !hasManaCostItem) {
			showManaBar = false;
		}

		if (displayMana < 0) {
			displayMana = currentMana;
		}
	}

	private static void updateManaCostHighlight() {
		if (mc.player == null) return;

		int tick = mc.player.tickCount;

		float progress = (tick % 20) / 20.0f;
		highlightAlpha = 0.3f + 0.7f * (progress < 0.5f ? progress * 2 : 2 - progress * 2);
	}

	private static int calculateManaValue() {
		if (mc.player != null)
			return ClientManaData.get();
		else return -1;
	}

	private static int calculateMaxManaValue() {
		if (mc.player != null)
			return ClientMaxManaData.get();
		else return -1;
	}

	public static void renderManaBar(ForgeGui gui, GuiGraphics stack, int screenWidth, int screenHeight, int[] randomOffset) {
		int currentManaValue = calculateManaValue();

		if (currentManaValue <= 0) {
			return;
		}

		int xStart = screenWidth / 2 + 10;
		int mana = ClientManaData.get();

		if (currentManaValue != previousManaValue) {
			manaIcons = StarsBar.calculateStarsIcons(currentManaValue);
			previousManaValue = currentManaValue;
		}

		if (manaIcons == null || manaIcons.length == 0) {
			return;
		}

		int starsToDraw = (currentManaValue + 4) / 5;

		int yPosition = screenHeight - 50 - isAirRender();
		long currentTime = System.currentTimeMillis();
		boolean isBlinking = (currentTime / 100) % 2 == 0;

		for (int i = starsToDraw - 1; i >= 0; i--) {
			int xPosition = xStart + ((9 - (i % 10)) * 8);
			int currentY = yPosition - max(3, (12 - ClientMaxManaData.get() / 50)) * (i / 10);

			if (mana <= 10 || ClientIsManaRegenBlockedData.get() > 0 || mc.player.hasEffect(ModEffects.HAPPINESS.get())) {
				currentY += randomOffset[i];
			}

			int textureYOffset = 0;

			if (ClientIsManaRegenBlockedData.get() > 0) {
				textureYOffset = 20;
				if (restoreBlink && isBlinking) {
					textureYOffset = 60;
				} else if (fullManaBlink && currentTime < fullManaBlinkEndTime && isBlinking) {
					textureYOffset = 60;
				}
			} else {
				if (restoreBlink && isBlinking) {
					textureYOffset = 10;
				} else if (fullManaBlink && currentTime < fullManaBlinkEndTime && isBlinking) {
					textureYOffset = 10;
				}
			}

			if (i < manaIcons.length) {
				switch (manaIcons[i].manaIconType) {
					case NONE:
						break;
					case ONE:
						drawStar(stack, xPosition, currentY, 243, textureYOffset, 9, 10);
						break;
					case TWO:
						drawStar(stack, xPosition, currentY, 234, textureYOffset, 9, 10);
						break;
					case THREE:
						drawStar(stack, xPosition, currentY, 225, textureYOffset, 9, 10);
						break;
					case FOUR:
						drawStar(stack, xPosition, currentY, 216, textureYOffset, 9, 10);
						break;
					case FULL:
						drawStar(stack, xPosition, currentY, 207, textureYOffset, 9, 10);
						break;
					default:
						break;
				}
			}
		}

		color4f(1, 1, 1, 1);
	}

	public static void renderManaBG(ForgeGui gui, GuiGraphics stack, int screenWidth, int screenHeight, int[] randomOffset) {
		int maxManaValue = calculateMaxManaValue();

		if (maxManaValue <= 0) {
			return;
		}

		int xStart = screenWidth / 2 + 10;
		int currentMana = ClientManaData.get();
		int mana = currentMana;

		if (maxManaValue != previousMaxManaValue) {
			maxManaIcons = StarsBar.calculateStarsIcons(maxManaValue);
			previousMaxManaValue = maxManaValue;
		}

		if (maxManaIcons == null || maxManaIcons.length == 0) {
			return;
		}

		int maxStarsToDraw = (maxManaValue + 4) / 5;

		int totalRows = (maxStarsToDraw + 9) / 10;

		boolean[] emptyRows = new boolean[totalRows];
		int currentStars = (currentMana + 4) / 5;

		for (int row = 0; row < totalRows; row++) {
			int starsInCurrentRow = Math.min(10, maxStarsToDraw - row * 10);
			if (starsInCurrentRow <= 0) {
				emptyRows[row] = true;
				continue;
			}

			int firstStarInRow = row * 10;
			int lastStarInRow = Math.min(firstStarInRow + 9, maxStarsToDraw - 1);

			emptyRows[row] = true;
			for (int starIndex = firstStarInRow; starIndex <= lastStarInRow; starIndex++) {
				if (starIndex < currentStars) {
					emptyRows[row] = false;
					break;
				}
			}
		}

		int yPosition = screenHeight - 50 - isAirRender();
		long currentTime = System.currentTimeMillis();
		boolean isBlinking = (currentTime / 100) % 2 == 0;

		int rowHeight = max(3, (12 - maxManaValue / 50));
		int[] rowOffsets = new int[totalRows];

		for (int row = 0; row < totalRows; row++) {
			if (row == 0) {
				rowOffsets[row] = 0;
			} else {
				if (emptyRows[row] || emptyRows[row - 1]) {
					rowOffsets[row] = rowOffsets[row - 1] + Math.min(5, (12 - maxManaValue / 50));
				} else {
					rowOffsets[row] = rowOffsets[row - 1] + rowHeight;
				}
			}
		}

		for (int i = maxStarsToDraw - 1; i >= 0; i--) {
			int xPosition = xStart + ((9 - (i % 10)) * 8);
			int rowIndex = i / 10;
			int currentY = yPosition - rowOffsets[rowIndex];

			if (mana <= 10 || ClientIsManaRegenBlockedData.get() > 0 || mc.player.hasEffect(ModEffects.HAPPINESS.get())) {
				if (i < randomOffset.length) {
					currentY += randomOffset[i];
				}
			}

			int bgTextureY = 0;

			boolean isEmptyRow = emptyRows[rowIndex];

			if (isEmptyRow && rowIndex > 0) {
				bgTextureY = 60;
			} else if (rowIndex == 0) {
				bgTextureY = 0;
			} else if (spendBlink && isBlinking && spentManaAmount > 0 && manaBeforeSpend > 0) {
				int starsBeforeSpend = (manaBeforeSpend + 4) / 5;

				int iconsToHighlight = calculateAffectedIcons(manaBeforeSpend, spentManaAmount);

				if (i < starsBeforeSpend) {
					int indexFromEndBeforeSpend = starsBeforeSpend - 1 - i;
					if (indexFromEndBeforeSpend < iconsToHighlight) {
						bgTextureY = 20;
					}
				}
			} else if (fullManaBlink && currentTime < fullManaBlinkEndTime && isBlinking) {
				bgTextureY = 10;
			} else if (restoreBlink && isBlinking && i == blinkStarIndex) {
				bgTextureY = 10;
			}

			if (i < maxManaIcons.length) {
				switch (maxManaIcons[i].manaIconType) {
					case NONE:
						break;
					case ONE:
						drawStar(stack, xPosition, currentY, 198, bgTextureY, 9, 10);
						break;
					case TWO:
						drawStar(stack, xPosition, currentY, 189, bgTextureY, 9, 10);
						break;
					case THREE:
						drawStar(stack, xPosition, currentY, 180, bgTextureY, 9, 10);
						break;
					case FOUR:
						drawStar(stack, xPosition, currentY, 171, bgTextureY, 9, 10);
						break;
					case FULL:
						drawStar(stack, xPosition, currentY, 162, bgTextureY, 9, 10);
						break;
					default:
						break;
				}
			}
		}

		color4f(1, 1, 1, 1);
	}

	private static void renderManaCostHighlight(GuiGraphics stack, int screenWidth, int screenHeight, int[] randomOffset) {
		int currentManaValue = calculateManaValue();
		int maxManaValue = calculateMaxManaValue();

		if (maxManaValue <= 0 || manaCostToShow <= 0) {
			return;
		}

		int xStart = screenWidth / 2 + 10;
		int currentStarsToDraw = (currentManaValue + 4) / 5;
		int maxStarsToDraw = (maxManaValue + 4) / 5;
		int totalRows = (maxStarsToDraw + 9) / 10;


		boolean[] emptyRows = new boolean[totalRows];
		for (int row = 0; row < totalRows; row++) {
			int starsInCurrentRow = Math.min(10, maxStarsToDraw - row * 10);
			if (starsInCurrentRow <= 0) {
				emptyRows[row] = true;
				continue;
			}
			int firstStarInRow = row * 10;
			int lastStarInRow = Math.min(firstStarInRow + 9, maxStarsToDraw - 1);
			emptyRows[row] = true;
			for (int starIndex = firstStarInRow; starIndex <= lastStarInRow; starIndex++) {
				if (starIndex < currentStarsToDraw) {
					emptyRows[row] = false;
					break;
				}
			}
		}

		int rowHeight = max(3, (12 - maxManaValue / 50));
		int[] rowOffsets = new int[totalRows];
		for (int row = 0; row < totalRows; row++) {
			if (row == 0) {
				rowOffsets[row] = 0;
			} else {
				if (emptyRows[row] || emptyRows[row - 1]) {
					rowOffsets[row] = rowOffsets[row - 1] + Math.min(5, (12 - maxManaValue / 50));
				} else {
					rowOffsets[row] = rowOffsets[row - 1] + rowHeight;
				}
			}
		}

		int yPosition = screenHeight - 50 - isAirRender();

		color4f(1, 1, 1, highlightAlpha);

		boolean costExceedsMana = manaCostToShow >= currentManaValue;

		if (costExceedsMana) {
			renderFullCostWithDarkBlueOutline(stack, xStart, yPosition, maxManaValue, randomOffset, rowOffsets);
		} else {
			renderPartialCostHighlight(stack, xStart, yPosition, currentManaValue, maxManaValue, currentStarsToDraw, randomOffset);
		}

		color4f(1, 1, 1, 1);
	}

	private static void renderFullCostWithDarkBlueOutline(GuiGraphics stack, int xStart, int yPosition,
														  int maxManaValue, int[] randomOffset, int[] rowOffsets) {
		int costStars = (manaCostToShow + 4) / 5;
		int remainder = manaCostToShow % 5;
		if (remainder == 0) remainder = 5;

		for (int i = 0; i < costStars; i++) {
			int rowIndex = i / 10;
			int xPosition = xStart + ((9 - (i % 10)) * 8);
			int currentY = yPosition - rowOffsets[rowIndex];

			if (calculateManaValue() <= 10 || ClientIsManaRegenBlockedData.get() > 0 || mc.player.hasEffect(ModEffects.HAPPINESS.get())) {
				currentY += randomOffset[i];
			}
			int manaInThisIcon = (i == costStars - 1) ? remainder : 5;

			switch (manaInThisIcon) {
				case 1:
					drawStar(stack, xPosition, currentY, 243, 30, 9, 10);
					break;
				case 2:
					drawStar(stack, xPosition, currentY, 234, 30, 9, 10);
					break;
				case 3:
					drawStar(stack, xPosition, currentY, 225, 30, 9, 10);
					break;
				case 4:
					drawStar(stack, xPosition, currentY, 216, 30, 9, 10);
					break;
				case 5:
					drawStar(stack, xPosition, currentY, 207, 30, 9, 10);
					break;
			}
		}
	}

	private static void renderPartialCostHighlight(GuiGraphics stack, int xStart, int yPosition,
												   int currentManaValue, int maxManaValue,
												   int currentStarsToDraw, int[] randomOffset) {
		int lastIconRemainder = currentManaValue % 5;
		if (lastIconRemainder == 0) lastIconRemainder = 5;

		int remainingCost = manaCostToShow;
		int iconsToHighlightFromEnd = 0;

		if (remainingCost <= lastIconRemainder) {
			iconsToHighlightFromEnd = 1;
		} else {
			remainingCost -= lastIconRemainder;
			iconsToHighlightFromEnd = 1;

			while (remainingCost > 0 && iconsToHighlightFromEnd < currentStarsToDraw) {
				remainingCost -= 5;
				iconsToHighlightFromEnd++;
			}

			if (remainingCost > 0) {
				iconsToHighlightFromEnd += (remainingCost + 4) / 5;
			}
		}

		remainingCost = manaCostToShow;
		int[] manaInIcons = new int[iconsToHighlightFromEnd];

		for (int i = 0; i < iconsToHighlightFromEnd; i++) {
			int iconIndexFromEnd = iconsToHighlightFromEnd - 1 - i;

			if (i == 0) {
				if (remainingCost <= lastIconRemainder) {
					manaInIcons[iconIndexFromEnd] = remainingCost;
					remainingCost = 0;
				} else {
					manaInIcons[iconIndexFromEnd] = lastIconRemainder;
					remainingCost -= lastIconRemainder;
				}
			} else {
				boolean isExistingIcon = (iconIndexFromEnd >= currentStarsToDraw - iconsToHighlightFromEnd + i);

				if (isExistingIcon && i < currentStarsToDraw) {
					int takeFromThisIcon = Math.min(5, remainingCost);
					manaInIcons[iconIndexFromEnd] = takeFromThisIcon;
					remainingCost -= takeFromThisIcon;
				} else {
					int takeFromThisIcon = Math.min(5, remainingCost);
					manaInIcons[iconIndexFromEnd] = takeFromThisIcon;
					remainingCost -= takeFromThisIcon;
				}
			}
		}

		for (int i = 0; i < iconsToHighlightFromEnd; i++) {
			int iconIndex = currentStarsToDraw - 1 - i;
			boolean isAdditionalIcon = (i == iconsToHighlightFromEnd - 1) && i != 0;
			int manaInThisIcon = manaInIcons[iconsToHighlightFromEnd - i - 1];

			if (isAdditionalIcon) {
				int xPosition = xStart + ((9 - (iconIndex % 10)) * 8);
				int currentY = yPosition - max(3, (12 - maxManaValue / 50)) * (iconIndex / 10);

				if (calculateManaValue() <= 10 || ClientIsManaRegenBlockedData.get() > 0 || mc.player.hasEffect(ModEffects.HAPPINESS.get())) {
					currentY += randomOffset[iconIndex];
				}

				switch (manaInThisIcon) {
					case 1:
						drawStar(stack, xPosition, currentY, 198, 40, 9, 10);
						break;
					case 2:
						drawStar(stack, xPosition, currentY, 180, 40, 9, 10);
						break;
					case 3:
						drawStar(stack, xPosition, currentY, 171, 40, 9, 10);
						break;
					case 4:
						drawStar(stack, xPosition, currentY, 162, 40, 9, 10);
						break;
					case 5:
						drawStar(stack, xPosition, currentY, 162, 30, 9, 10);
						break;
				}
			} else {
				int xPosition = xStart + ((9 - (iconIndex % 10)) * 8);
				int currentY = yPosition - max(3, (12 - maxManaValue / 50)) * (iconIndex / 10);

				if (calculateManaValue() <= 10 || ClientIsManaRegenBlockedData.get() > 0 || mc.player.hasEffect(ModEffects.HAPPINESS.get())) {
					currentY += randomOffset[iconIndex];
				}

				if (iconIndex < manaIcons.length && manaInThisIcon > 0) {
					switch (manaInThisIcon) {
						case 1:
							if (manaCostToShow < calculateRigtestStar())
								switch (calculateRigtestStar()) {
									case 5:
										drawStar(stack, xPosition, currentY, 198, 40, 9, 10);
										break;
									case 4:
										drawStar(stack, xPosition, currentY, 189, 40, 9, 10);
										break;
									case 3:
										drawStar(stack, xPosition, currentY, 171, 50, 9, 10);
										break;
									case 2:
										drawStar(stack, xPosition, currentY, 162, 50, 9, 10);
										break;
									default:
										drawStar(stack, xPosition, currentY, 198, 30, 9, 10);
										break;
								}
							else drawStar(stack, xPosition, currentY, 198, 30, 9, 10);
							break;
						case 2:
							if (manaCostToShow < calculateRigtestStar())
								switch (calculateRigtestStar()) {
									case 5:
										drawStar(stack, xPosition, currentY, 180, 40, 9, 10);
										break;
									case 4:
										drawStar(stack, xPosition, currentY, 189, 50, 9, 10);
										break;
									case 3:
										drawStar(stack, xPosition, currentY, 180, 50, 9, 10);
										break;
									default:
										drawStar(stack, xPosition, currentY, 189, 30, 9, 10);
										break;
								}
							else drawStar(stack, xPosition, currentY, 189, 30, 9, 10);
							break;
						case 3:
							if (manaCostToShow < calculateRigtestStar())
								switch (calculateRigtestStar()) {
									case 5:
										drawStar(stack, xPosition, currentY, 171, 40, 9, 10);
										break;
									case 4:
										drawStar(stack, xPosition, currentY, 198, 50, 9, 10);
										break;
									default:
										drawStar(stack, xPosition, currentY, 180, 30, 9, 10);
										break;
								}
							else drawStar(stack, xPosition, currentY, 180, 30, 9, 10);
							break;
						case 4:
							if (manaCostToShow < calculateRigtestStar())
								switch (calculateRigtestStar()) {
									case 5:
										drawStar(stack, xPosition, currentY, 162, 40, 9, 10);
										break;
									default:
										drawStar(stack, xPosition, currentY, 171, 30, 9, 10);
										break;
								}
							else drawStar(stack, xPosition, currentY, 171, 30, 9, 10);
							break;
						case 5:
							drawStar(stack, xPosition, currentY, 162, 30, 9, 10);
							break;
					}
				}
			}
		}
	}

	static int calculateRigtestStar() {
		if (calculateManaValue() % 5 == 0) return 5;
		return calculateManaValue() % 5;
	}

	private static int calculateAffectedIcons(int manaBefore, int spentAmount) {
		int fullIconsBefore = (manaBefore + 4) / 5;

		int lastIconValue = manaBefore % 5;
		if (lastIconValue == 0) {
			lastIconValue = 5;
		}

		if (spentAmount <= lastIconValue) {
			return 1;
		}

		int remainingToSpend = spentAmount - lastIconValue;

		int previousIconsAffected = (remainingToSpend + 4) / 5;

		return 1 + previousIconsAffected;
	}

	private static void color4f(float r, float g, float b, float a) {
		RenderSystem.setShaderColor(r, g, b, a);
	}
}