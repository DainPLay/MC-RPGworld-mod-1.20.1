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

	// Переменные для мигания
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

	// Новые переменные для подсветки требуемой маны
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
			// Вычисляем позицию единицы: двигаемся по массиву с каждым тиком
			int position = tickCount % maxMana;
			offset[position] = -2;
		}
		return offset;
	}

	public void render(ForgeGui gui, GuiGraphics guiGraphics, float partialTick, int screenWidth, int screenHeight) {
		if (!mc.options.hideGui && gui.shouldDrawSurvivalElements()) {
			gui.setupOverlayRenderState(true, false);

			int maxMana = ClientMaxManaData.get();

			// Проверяем, держит ли игрок предмет, требующий ману
			boolean hasManaCostItem = false;
			manaCostToShow = 0;

			if (mc.player != null) {
				ItemStack mainHandItem = mc.player.getMainHandItem();
				ItemStack offHandItem = mc.player.getOffhandItem();

				// Проверяем обе руки
				if (mainHandItem.getItem() instanceof ManaCostItem manaCostItem) {
					if(mainHandItem.getItem() instanceof OrbitingItem orbitingItem) hasManaCostItem = orbitingItem.shouldOrbit(mainHandItem, mc.player) && !manaCostItem.usesHealthInsteadOfMana(mainHandItem);
					else hasManaCostItem = !manaCostItem.usesHealthInsteadOfMana(mainHandItem);
					if (hasManaCostItem) manaCostToShow = ((ManaCostItem) mainHandItem.getItem()).getManaCost(mainHandItem, mc.player);
				} else if (offHandItem.getItem() instanceof ManaCostItem manaCostItem) {
					if(offHandItem.getItem() instanceof OrbitingItem orbitingItem) hasManaCostItem = orbitingItem.shouldOrbit(offHandItem, mc.player) && !manaCostItem.usesHealthInsteadOfMana(offHandItem);
					else hasManaCostItem = !manaCostItem.usesHealthInsteadOfMana(offHandItem);
					if (hasManaCostItem) manaCostToShow = ((ManaCostItem) offHandItem.getItem()).getManaCost(offHandItem, mc.player);
				}
			}

			if (shouldRenderMana() && mc.player != null) {
				int[] randomOffset;
				if(mc.player.hasEffect(ModEffects.HAPPINESS.get()))
					randomOffset = createHappinessOffset(maxMana/3, mc.player.tickCount);
				else randomOffset = new Random(mc.player.tickCount).ints(0, 2).limit(maxMana).toArray();

				// Обновляем состояние мигания
				updateManaBlink();
				// Обновляем состояние подсветки требуемой маны
				updateManaCostHighlight();

				// Рисуем фон (максимальную ману)
				renderManaBG(gui, guiGraphics, screenWidth, screenHeight, randomOffset);
				// Рисуем текущую ману поверх фона
				renderManaBar(gui, guiGraphics, screenWidth, screenHeight, randomOffset);
				// Рисуем подсветку требуемой маны
				if (hasManaCostItem && manaCostToShow > 0) {
					renderManaCostHighlight(guiGraphics, screenWidth, screenHeight, randomOffset);
				}
			}
		}
	}

	public static boolean shouldRenderMana()
	{
		if (mc.player == null) return false;
		boolean hasManaCostItem = false;
		ItemStack mainHandItem = mc.player.getMainHandItem();
		ItemStack offHandItem = mc.player.getOffhandItem();
		if (mainHandItem.getItem() instanceof ManaCostItem manaCostItem) {
			if(mainHandItem.getItem() instanceof OrbitingItem orbitingItem) hasManaCostItem = orbitingItem.shouldOrbit(mainHandItem, mc.player) && !manaCostItem.usesHealthInsteadOfMana(mainHandItem);
			else hasManaCostItem =  !manaCostItem.usesHealthInsteadOfMana(mainHandItem);
			if (hasManaCostItem) manaCostToShow = ((ManaCostItem) mainHandItem.getItem()).getManaCost(mainHandItem, mc.player);
		} else if (offHandItem.getItem() instanceof ManaCostItem manaCostItem) {
			if(offHandItem.getItem() instanceof OrbitingItem orbitingItem) hasManaCostItem = orbitingItem.shouldOrbit(offHandItem, mc.player) && !manaCostItem.usesHealthInsteadOfMana(offHandItem);
			else hasManaCostItem =  !manaCostItem.usesHealthInsteadOfMana(offHandItem);
			if (hasManaCostItem) manaCostToShow = ((ManaCostItem) offHandItem.getItem()).getManaCost(offHandItem, mc.player);
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

		// Проверяем изменение маны
		if (currentMana != lastMana) {
			if (currentMana > lastMana) {
				// Восстановление
				int restored = currentMana - lastMana;

				// Проверяем восстановление кратной 5 маны
				if (currentMana % 5 == 0 && restored > 0) {
					restoreBlink = true;
					// Индекс последней восстановленной звезды
					blinkStarIndex = (currentMana - 1) / 5;
					lastManaTime = currentTime;
					manaBlinkTime = currentTime + 200;
				}

				// Проверяем полное восстановление
				if (currentMana == maxMana && lastMana < maxMana) {
					fullManaBlink = true;
					fullManaDisplayTime = currentTime + 1000; // Показываем ману еще 1 секунду
					fullManaBlinkEndTime = currentTime + 200; // Мигание длится 200 мс
					manaBlinkTime = Math.max(manaBlinkTime, currentTime + 200);
					showManaBar = true;
				}
			} else if (currentMana < lastMana) {
				// Трата маны
				spentManaAmount = lastMana - currentMana;
				spendBlink = true;
				// Запоминаем ману до траты
				manaBeforeSpend = lastMana;
				lastManaTime = currentTime;
				manaBlinkTime = currentTime + 200;

				// Сбрасываем таймер полной маны при трате
				fullManaDisplayTime = 0;
				fullManaBlink = false;
				fullManaBlinkEndTime = 0;
				showManaBar = true; // Показываем ману при трате
			}

			lastMana = currentMana;
		}

		// Сбрасываем мигание по истечении времени
		if (currentTime > manaBlinkTime) {
			restoreBlink = false;
			spendBlink = false;
			blinkStarIndex = -1;
			spentManaAmount = 0;
			manaBeforeSpend = -1;
		}

		// Сбрасываем мигание полной маны после 200 мс
		if (currentTime > fullManaBlinkEndTime) {
			fullManaBlink = false;
		}

		// Скрываем ману через секунду после полного восстановления
		// Но не скрываем, если игрок держит предмет, требующий ману
		boolean hasManaCostItem = false;
		if (mc.player != null) {
			ItemStack mainHandItem = mc.player.getMainHandItem();
			ItemStack offHandItem = mc.player.getOffhandItem();
			if (mainHandItem.getItem() instanceof ManaCostItem manaCostItem) {
				if(mainHandItem.getItem() instanceof OrbitingItem orbitingItem) hasManaCostItem = orbitingItem.shouldOrbit(mainHandItem, mc.player) && !manaCostItem.usesHealthInsteadOfMana(mainHandItem);
				else hasManaCostItem = !manaCostItem.usesHealthInsteadOfMana(mainHandItem);
			} else if (offHandItem.getItem() instanceof ManaCostItem manaCostItem) {
				if(offHandItem.getItem() instanceof OrbitingItem orbitingItem) hasManaCostItem = orbitingItem.shouldOrbit(offHandItem, mc.player) && !manaCostItem.usesHealthInsteadOfMana(offHandItem);
				else hasManaCostItem = !manaCostItem.usesHealthInsteadOfMana(offHandItem);
			}
		}

		if (currentTime > fullManaDisplayTime && currentMana == maxMana && !hasManaCostItem) {
			showManaBar = false;
		}

		// Если displayMana еще не инициализирован, устанавливаем текущее значение
		if (displayMana < 0) {
			displayMana = currentMana;
		}
	}

	/**
	 * Обновление состояния подсветки требуемой маны
	 */
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

		// Обновляем иконки для текущей маны
		if (currentManaValue != previousManaValue) {
			manaIcons = StarsBar.calculateStarsIcons(currentManaValue);
			previousManaValue = currentManaValue;
		}

		if (manaIcons == null || manaIcons.length == 0) {
			return;
		}

		// Количество звезд для текущей маны
		int starsToDraw = (currentManaValue + 4) / 5; // Округляем вверх

		int yPosition = screenHeight - 50 - isAirRender();
		long currentTime = System.currentTimeMillis();
		boolean isBlinking = (currentTime / 100) % 2 == 0;

		for (int i = starsToDraw - 1; i >= 0; i--) {
			// ИЗМЕНЕНИЕ: Изменяем расчет X для отрисовки справа налево
			int xPosition = xStart + ((9 - (i % 10)) * 8); // 9 - обратный порядок
			int currentY = yPosition - max(3, (12 - ClientMaxManaData.get() / 50)) * (i / 10);

			if (mana <= 10 || ClientIsManaRegenBlockedData.get() > 0 || mc.player.hasEffect(ModEffects.HAPPINESS.get())) {
				currentY += randomOffset[i];
			}

			// Определяем смещение по Y для текстуры
			int textureYOffset = 0;

			// Если восстановление маны заблокировано, используем другой Y-сдвиг (20)
			if (ClientIsManaRegenBlockedData.get() > 0) {
				textureYOffset = 20; // Используем текстуры с Y=20
				if (restoreBlink && isBlinking) {
					textureYOffset = 60;
				} else if (fullManaBlink && currentTime < fullManaBlinkEndTime && isBlinking) {
					textureYOffset = 60;
				}
			} else {
				// Оригинальная логика для мигания
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

		// Обновляем иконки для максимальной маны
		if (maxManaValue != previousMaxManaValue) {
			maxManaIcons = StarsBar.calculateStarsIcons(maxManaValue);
			previousMaxManaValue = maxManaValue;
		}

		if (maxManaIcons == null || maxManaIcons.length == 0) {
			return;
		}

		// Количество звезд для максимальной маны
		int maxStarsToDraw = (maxManaValue + 4) / 5; // Округляем вверх

		// Вычисляем количество строк (по 10 звезд в строке)
		int totalRows = (maxStarsToDraw + 9) / 10; // Округляем вверх

		// Определяем, какие строки пустые (без текущей маны)
		boolean[] emptyRows = new boolean[totalRows];
		int currentStars = (currentMana + 4) / 5; // Звезд текущей маны

		for (int row = 0; row < totalRows; row++) {
			int starsInCurrentRow = Math.min(10, maxStarsToDraw - row * 10);
			if (starsInCurrentRow <= 0) {
				emptyRows[row] = true;
				continue;
			}

			// Проверяем, есть ли в этой строке текущая мана
			int firstStarInRow = row * 10;
			int lastStarInRow = Math.min(firstStarInRow + 9, maxStarsToDraw - 1);

			// Строка считается пустой, если в ней нет ни одной звезды текущей маны
			emptyRows[row] = true;
			for (int starIndex = firstStarInRow; starIndex <= lastStarInRow; starIndex++) {
				if (starIndex < currentStars) {
					// В этой строке есть хотя бы одна звезда текущей маны
					emptyRows[row] = false;
					break;
				}
			}
		}

		int yPosition = screenHeight - 50 - isAirRender();
		long currentTime = System.currentTimeMillis();
		boolean isBlinking = (currentTime / 100) % 2 == 0;

		// Рассчитываем отступы между строками с учетом пустых строк
		int rowHeight = max(3, (12 - maxManaValue / 50));
		int[] rowOffsets = new int[totalRows];

		for (int row = 0; row < totalRows; row++) {
			if (row == 0) {
				rowOffsets[row] = 0;
			} else {
				// Если текущая строка или предыдущая строка пустые, используем минимальный отступ (3)
				if (emptyRows[row] || emptyRows[row - 1]) {
					rowOffsets[row] = rowOffsets[row - 1] + Math.min(5, (12 - maxManaValue / 50));
				} else {
					rowOffsets[row] = rowOffsets[row - 1] + rowHeight;
				}
			}
		}

		for (int i = maxStarsToDraw - 1; i >= 0; i--) {
			// ИЗМЕНЕНИЕ: Изменяем расчет X для отрисовки справа налево
			int xPosition = xStart + ((9 - (i % 10)) * 8); // 9 - обратный порядок
			int rowIndex = i / 10; // Определяем строку (0 - нижняя)
			int currentY = yPosition - rowOffsets[rowIndex];

			if (mana <= 10 || ClientIsManaRegenBlockedData.get() > 0 || mc.player.hasEffect(ModEffects.HAPPINESS.get())) {
				// Убедимся, что индекс не выходит за пределы массива
				if (i < randomOffset.length) {
					currentY += randomOffset[i];
				}
			}

			// Определяем обводку в зависимости от типа мигания
			int bgTextureY = 0; // Стандартная обводка по умолчанию (Y=0)

			// Проверяем, является ли строка пустой
			boolean isEmptyRow = emptyRows[rowIndex];

			// Если строка пустая И это НЕ самая нижняя строка (rowIndex > 0), используем Y=60
			if (isEmptyRow && rowIndex > 0) {
				bgTextureY = 60;
			} else if (rowIndex == 0) {
				// Самая нижняя строка всегда использует стандартные спрайты (Y=0)
				bgTextureY = 0;
			}
			// Подсветка при трате маны (темно-синяя обводка нескольких иконок, которые были полными ДО траты)
			else if (spendBlink && isBlinking && spentManaAmount > 0 && manaBeforeSpend > 0) {
				// Вычисляем количество иконок, которые были полными ДО траты
				int starsBeforeSpend = (manaBeforeSpend + 4) / 5;

				// Вычисляем сколько иконок нужно подсветить (новый алгоритм)
				int iconsToHighlight = calculateAffectedIcons(manaBeforeSpend, spentManaAmount);

				// Проверяем, что текущая иконка была полной до траты
				if (i < starsBeforeSpend) {
					// Определяем индекс текущей иконки от конца ДО траты
					int indexFromEndBeforeSpend = starsBeforeSpend - 1 - i;
					// Подсвечиваем последние iconsToHighlight иконок, которые были полными ДО траты
					if (indexFromEndBeforeSpend < iconsToHighlight) {
						bgTextureY = 20; // Темно-синяя обводка (Y=20)
					}
				}
			}
			// Мигание при полном восстановлении (только 200 мс, белая обводка)
			else if (fullManaBlink && currentTime < fullManaBlinkEndTime && isBlinking) {
				bgTextureY = 10; // Белая обводка (Y=10)
			}
			// Подсветка фона только у последней восстановленной иконки
			else if (restoreBlink && isBlinking && i == blinkStarIndex) {
				bgTextureY = 10; // Белая обводка (Y=10)
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

	/**
	 * Рисует подсветку требуемой маны для предмета в руках
	 */
	private static void renderManaCostHighlight(GuiGraphics stack, int screenWidth, int screenHeight, int[] randomOffset) {
		int currentManaValue = calculateManaValue();
		int maxManaValue = calculateMaxManaValue();

		if (maxManaValue <= 0 || manaCostToShow <= 0) {
			return;
		}

		int xStart = screenWidth / 2 + 10;

		// Количество звезд для текущей маны
		int currentStarsToDraw = (currentManaValue + 4) / 5; // Округляем вверх

		int yPosition = screenHeight - 50 - isAirRender();

		// Устанавливаем альфа-канал для плавного мигания
		color4f(1, 1, 1, highlightAlpha);

		// ПРОВЕРКА: если стоимость маны >= текущей мане игрока
		boolean costExceedsMana = manaCostToShow >= currentManaValue;

		if (costExceedsMana) {
			// Отображаем полную стоимость сначала с тёмно-синей обводкой без фона (Y=50)
			renderFullCostWithDarkBlueOutline(stack, screenWidth, screenHeight, xStart, yPosition, maxManaValue, randomOffset);
		} else {
			// Оригинальная логика для случая, когда маны достаточно
			renderPartialCostHighlight(stack, xStart, yPosition, currentManaValue, maxManaValue, currentStarsToDraw, randomOffset);
		}

		// Сбрасываем цвет
		color4f(1, 1, 1, 1);
	}

	/**
	 * Рендерит полную стоимость маны с тёмно-синей обводкой без фона (Y=50)
	 * когда стоимость маны >= текущей мане игрока
	 */
	private static void renderFullCostWithDarkBlueOutline(GuiGraphics stack, int screenWidth, int screenHeight,
														  int xStart, int yPosition, int maxManaValue, int[] randomOffset) {
		// Количество иконок для отображения полной стоимости
		int costStars = (manaCostToShow + 4) / 5; // Округляем вверх
		int remainder = manaCostToShow % 5;
		if (remainder == 0) remainder = 5;

		// Отображаем все иконки стоимости с самого начала (справа налево)
		for (int i = 0; i < costStars; i++) {
			// Определяем координаты для текущей иконки (справа налево)
			int xPosition = xStart + ((9 - (i % 10)) * 8); // ИЗМЕНЕНИЕ: рисуем справа налево
			int currentY = yPosition - max(3, (12 - maxManaValue / 50)) * (i / 10);

			if (calculateManaValue() <= 10 || ClientIsManaRegenBlockedData.get() > 0 || mc.player.hasEffect(ModEffects.HAPPINESS.get())) {
				currentY += randomOffset[i];
			}
			// Определяем количество маны в текущей иконке
			int manaInThisIcon;
			if (i == costStars - 1) {
				// Последняя иконка (может быть частичной)
				manaInThisIcon = remainder;
			} else {
				// Все предыдущие иконки полные
				manaInThisIcon = 5;
			}

			// Используем Y=50 (тёмно-синяя обводка без фона)
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

	/**
	 * Оригинальная логика подсветки частичной стоимости (когда маны достаточно)
	 */
	private static void renderPartialCostHighlight(GuiGraphics stack, int xStart, int yPosition,
												   int currentManaValue, int maxManaValue,
												   int currentStarsToDraw, int[] randomOffset) {
		// Вычисляем остаток маны в последней иконке
		int lastIconRemainder = currentManaValue % 5;
		if (lastIconRemainder == 0) lastIconRemainder = 5;

		int remainingCost = manaCostToShow;
		int iconsToHighlightFromEnd = 0;

		// Сначала определяем, сколько иконок с конца нужно подсветить
		if (remainingCost <= lastIconRemainder) {
			// Вся стоимость помещается в последнюю частичную иконку
			iconsToHighlightFromEnd = 1;
		} else {
			// Вычитаем ману из последней иконки
			remainingCost -= lastIconRemainder;
			iconsToHighlightFromEnd = 1;

			// Добавляем полные иконки с конца
			while (remainingCost > 0 && iconsToHighlightFromEnd < currentStarsToDraw) {
				remainingCost -= 5;
				iconsToHighlightFromEnd++;
			}

			// Если после использования всех текущих иконок стоимость еще осталась
			if (remainingCost > 0) {
				// Добавляем дополнительные иконки слева
				iconsToHighlightFromEnd += (remainingCost + 4) / 5; // Округляем вверх
			}
		}

		// Пересчитываем оставшуюся стоимость для определения типа крайней левой иконки
		remainingCost = manaCostToShow;
		int[] manaInIcons = new int[iconsToHighlightFromEnd];

		// Заполняем массив количеством маны в каждой подсвечиваемой иконке
		for (int i = 0; i < iconsToHighlightFromEnd; i++) {
			// Начинаем с последней иконки
			int iconIndexFromEnd = iconsToHighlightFromEnd - 1 - i;

			if (i == 0) {
				// Последняя иконка (самая правая)
				if (remainingCost <= lastIconRemainder) {
					manaInIcons[iconIndexFromEnd] = remainingCost;
					remainingCost = 0;
				} else {
					manaInIcons[iconIndexFromEnd] = lastIconRemainder;
					remainingCost -= lastIconRemainder;
				}
			} else {
				// Проверяем, является ли эта иконка существующей или дополнительной
				boolean isExistingIcon = (iconIndexFromEnd >= currentStarsToDraw - iconsToHighlightFromEnd + i);

				if (isExistingIcon && i < currentStarsToDraw) {
					// Существующая промежуточная иконка (полная)
					int takeFromThisIcon = Math.min(5, remainingCost);
					manaInIcons[iconIndexFromEnd] = takeFromThisIcon;
					remainingCost -= takeFromThisIcon;
				} else {
					// Дополнительная иконка слева
					int takeFromThisIcon = Math.min(5, remainingCost);
					manaInIcons[iconIndexFromEnd] = takeFromThisIcon;
					remainingCost -= takeFromThisIcon;
				}
			}
		}

		// Теперь отрисовываем все подсвечиваемые иконки
		for (int i = 0; i < iconsToHighlightFromEnd; i++) {
			// Индекс иконки с конца текущей маны
			int iconIndex = currentStarsToDraw - 1 - i;
			boolean isAdditionalIcon = (i == iconsToHighlightFromEnd - 1) && i != 0;
			int manaInThisIcon = manaInIcons[iconsToHighlightFromEnd - i - 1];

			if (isAdditionalIcon) {
				// Это дополнительная иконка слева от текущей маны
				// Вычисляем абсолютный индекс для позиционирования
				int xPosition = xStart + ((9 - (iconIndex % 10)) * 8); // ИЗМЕНЕНИЕ: справа налево
				int currentY = yPosition - max(3, (12 - maxManaValue / 50)) * (iconIndex / 10);

				if (calculateManaValue() <= 10 || ClientIsManaRegenBlockedData.get() > 0 || mc.player.hasEffect(ModEffects.HAPPINESS.get())) {
					currentY += randomOffset[iconIndex];
				}

				// Используем Y=40 для дополнительных иконок
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
				// Это существующая иконка текущей маны
				int xPosition = xStart + ((9 - (iconIndex % 10)) * 8); // ИЗМЕНЕНИЕ: справа налево
				int currentY = yPosition - max(3, (12 - maxManaValue / 50)) * (iconIndex / 10);

				if (calculateManaValue() <= 10 || ClientIsManaRegenBlockedData.get() > 0 || mc.player.hasEffect(ModEffects.HAPPINESS.get())) {
					currentY += randomOffset[iconIndex];
				}

				// Проверяем, что текущая иконка существует в массиве manaIcons
				if (iconIndex < manaIcons.length && manaInThisIcon > 0) {
					// Используем текстуру с Y=30 (белая обводка без фона)
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

	static int calculateRigtestStar()
	{
		if (calculateManaValue()%5 == 0) return 5;
		return calculateManaValue()%5;
	}

	/**
	 * Вычисляет количество затронутых иконок при трате маны
	 *
	 * @param manaBefore  Маня до траты
	 * @param spentAmount Потраченное количество маны
	 * @return Количество иконок, которые нужно подсветить
	 */
	private static int calculateAffectedIcons(int manaBefore, int spentAmount) {
		// Количество полных иконок до траты (округляем вверх)
		int fullIconsBefore = (manaBefore + 4) / 5;

		// Остаток в последней иконке (от 1 до 5)
		// Если остаток 0, значит последняя иконка полная (5)
		int lastIconValue = manaBefore % 5;
		if (lastIconValue == 0) {
			lastIconValue = 5;
		}

		// Если тратим меньше, чем содержимое последней иконки
		if (spentAmount <= lastIconValue) {
			return 1; // Затронута только последняя иконка
		}

		// Вычисляем, сколько маны нужно вычесть из предыдущих иконок
		int remainingToSpend = spentAmount - lastIconValue;

		// Вычисляем, сколько предыдущих иконок нужно затронуть
		// (remainingToSpend - 1) / 5 + 1 дает количество иконок, которые нужно затронуть
		int previousIconsAffected = (remainingToSpend + 4) / 5; // Округляем вверх

		// Общее количество затронутых иконок = последняя + предыдущие
		return 1 + previousIconsAffected;
	}

	private static void color4f(float r, float g, float b, float a) {
		RenderSystem.setShaderColor(r, g, b, a);
	}
}