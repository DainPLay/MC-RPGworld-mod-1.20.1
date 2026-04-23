package net.dainplay.rpgworldmod.util;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.math.Axis;
import net.dainplay.rpgworldmod.RPGworldMod;
import net.dainplay.rpgworldmod.enchantment.ModEnchantments;
import net.dainplay.rpgworldmod.item.ModItems;
import net.dainplay.rpgworldmod.item.custom.PillagerScrollItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid = RPGworldMod.MOD_ID, value = Dist.CLIENT)
public class RecolorWoolMenuHandler {
	private static boolean active = false;
	private static double cursorX = 0.0;
	private static double cursorY = 0.0;
	private static int selectedSegment = -1;
	private static final int MAX_RADIUS = 30;
	private static final ResourceLocation WHEEL_TEXTURE =
			new ResourceLocation(RPGworldMod.MOD_ID, "textures/gui/colorwheel_old.png");
	private static final int WHEEL_TEX_WIDTH = 256;
	private static final int WHEEL_TEX_HEIGHT = 256;
	private static final int BG_U = 0;
	private static final int BG_V = 0;
	private static final int BG_WIDTH = 112;
	private static final int BG_HEIGHT = 107;
	private static final int CURSOR_U = 112;
	private static final int CURSOR_V = 0;
	private static final int CURSOR_WIDTH = 16;
	private static final int CURSOR_HEIGHT = 16;
	private static final int[][] ICON_UV = new int[16][4];
	private static final int[][] ICON_OFFSETS = new int[16][2];
	private static final ResourceLocation[] DYE_TEXTURES = new ResourceLocation[16];
	private static final int DYE_SIZE = 16;
	private static final int RADIUS = 50;

	static {
		ICON_UV[0] = new int[]{73, 111, 18, 25};
		ICON_OFFSETS[0] = new int[]{47, 13};
		ICON_UV[1] = new int[]{91, 111, 22, 27};
		ICON_OFFSETS[1] = new int[]{58, 14};
		ICON_UV[2] = new int[]{113, 114, 25, 25};
		ICON_OFFSETS[2] = new int[]{64, 21};
		ICON_UV[3] = new int[]{139, 111, 27, 22};
		ICON_OFFSETS[3] = new int[]{69, 30};
		ICON_UV[4] = new int[]{139, 133, 27, 18};
		ICON_OFFSETS[4] = new int[]{70, 45};
		ICON_UV[5] = new int[]{138, 151, 27, 22};
		ICON_OFFSETS[5] = new int[]{69, 56};
		ICON_UV[6] = new int[]{112, 141, 24, 25};
		ICON_OFFSETS[6] = new int[]{65, 62};
		ICON_UV[7] = new int[]{91, 140, 21, 26};
		ICON_OFFSETS[7] = new int[]{58, 68};
		ICON_UV[8] = new int[]{73, 141, 18, 25};
		ICON_OFFSETS[8] = new int[]{47, 70};
		ICON_UV[9] = new int[]{52, 138, 21, 28};
		ICON_OFFSETS[9] = new int[]{33, 66};
		ICON_UV[10] = new int[]{28, 137, 24, 24};
		ICON_OFFSETS[10] = new int[]{23, 63};
		ICON_UV[11] = new int[]{0, 147, 27, 23};
		ICON_OFFSETS[11] = new int[]{16, 55};
		ICON_UV[12] = new int[]{0, 129, 25, 18};
		ICON_OFFSETS[12] = new int[]{15, 45};
		ICON_UV[13] = new int[]{0, 107, 27, 22};
		ICON_OFFSETS[13] = new int[]{16, 30};
		ICON_UV[14] = new int[]{27, 111, 25, 25};
		ICON_OFFSETS[14] = new int[]{23, 21};
		ICON_UV[15] = new int[]{52, 111, 21, 27};
		ICON_OFFSETS[15] = new int[]{33, 14};
		String[] dyeNames = {
				"white", "light_gray", "gray", "black",
				"brown", "red", "orange", "yellow",
				"lime", "green", "cyan", "light_blue",
				"blue", "purple", "magenta", "pink"
		};
		for (int i = 0; i < 16; i++) {
			DYE_TEXTURES[i] = new ResourceLocation("textures/item/" + dyeNames[i] + "_dye.png");
		}
	}

	private static final boolean DEBUG_CURSOR = false;

	public static void onMouseMove(double dx, double dy) {
		if (!active) return;
		cursorX += dx * Minecraft.getInstance().options.sensitivity().get();
		cursorY += dy * Minecraft.getInstance().options.sensitivity().get();
		double distance = Math.hypot(cursorX, cursorY);
		if (distance > MAX_RADIUS) {
			cursorX = cursorX / distance * MAX_RADIUS;
			cursorY = cursorY / distance * MAX_RADIUS;
		}
		updateSelectedSegment();
	}

	public static void activate() {
		active = true;
		int colorIndex = getCurrentSelectedColorIndex();
		if (colorIndex >= 0 && colorIndex < 16) {
			double angleRad = Math.toRadians(colorIndex * 22.5 - 90);
			cursorX = MAX_RADIUS * Math.cos(angleRad);
			cursorY = MAX_RADIUS * Math.sin(angleRad);
		} else {
			cursorX = 0.0;
			cursorY = 0.0;
		}
		updateSelectedSegment();
	}

	public static void deactivate() {
		active = false;
		selectedSegment = -1;
	}

	public static int getSelectedSegment() {
		return selectedSegment;
	}

	public static boolean isActive() {
		return active;
	}

	private static void updateSelectedSegment() {
		if (!active) return;
		if (cursorX == 0 && cursorY == 0) {
			selectedSegment = getCurrentSelectedColorIndex();
			return;
		}
		double angleRad = Math.atan2(-cursorY, -cursorX);
		double angleDeg = Math.toDegrees(angleRad);
		angleDeg = (angleDeg + 360) % 360;
		double corrected = (angleDeg - 80 + 360) % 360;
		int seg = (int) (corrected / 22.5);
		selectedSegment = seg % 16;
	}

	private static boolean shouldBeActive(Player player) {
		return player.isUsingItem()
				&& player.getUseItem().getItem() == ModItems.PILLAGER_SCROLL.get()
				&& EnchantmentHelper.getEnchantments(player.getUseItem()).containsKey(ModEnchantments.ALTERATION.get())
				&& player.getUseItem().getTag() != null
				&& player.getUseItem().getTag().contains("isSelectingColor", Tag.TAG_BYTE)
				&& player.getUseItem().getTag().getBoolean("isSelectingColor");
	}

	@SubscribeEvent
	public static void onRenderGui(RenderGuiEvent.Post event) {
		if (!active) return;
		Minecraft mc = Minecraft.getInstance();
		if (mc.screen != null) return;
		GuiGraphics guiGraphics = event.getGuiGraphics();
		int width = mc.getWindow().getGuiScaledWidth();
		int height = mc.getWindow().getGuiScaledHeight();
		render(guiGraphics, width, height);
	}

	@SubscribeEvent
	public static void onClientTick(TickEvent.ClientTickEvent event) {
		if (event.phase != TickEvent.Phase.END) return;
		Minecraft mc = Minecraft.getInstance();
		if (!active) {
			if (mc.player != null && shouldBeActive(mc.player)) activate();
			return;
		}
		if (mc.player == null || !shouldBeActive(mc.player)) {
			deactivate();
		}
	}

	private static void render(GuiGraphics guiGraphics, int width, int height) {
		int centerX = width / 2;
		int centerY = height / 2;
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		int bgScreenX = centerX - BG_WIDTH / 2;
		int bgScreenY = centerY - BG_HEIGHT / 2;
		guiGraphics.blit(WHEEL_TEXTURE,
				bgScreenX, bgScreenY,
				BG_U, BG_V,
				BG_WIDTH, BG_HEIGHT,
				WHEEL_TEX_WIDTH, WHEEL_TEX_HEIGHT);
		if (selectedSegment >= 0 && selectedSegment < 16) {
			int[] uv = ICON_UV[selectedSegment];
			int[] offset = ICON_OFFSETS[selectedSegment];
			int u = uv[0];
			int v = uv[1];
			int iconW = uv[2];
			int iconH = uv[3];
			int offsetX = offset[0];
			int offsetY = offset[1];
			int screenX = bgScreenX + offsetX;
			int screenY = bgScreenY + offsetY;
			guiGraphics.blit(WHEEL_TEXTURE,
					screenX, screenY,
					u, v,
					iconW, iconH,
					WHEEL_TEX_WIDTH, WHEEL_TEX_HEIGHT);
		}
		renderDyeIcons(guiGraphics, centerX, centerY);
		renderCursor(guiGraphics, centerX, centerY);
		if (DEBUG_CURSOR) {
			int cursorXAbs = centerX + (int) cursorX;
			int cursorYAbs = centerY + (int) cursorY;
			guiGraphics.fill(cursorXAbs - 2, cursorYAbs - 2, cursorXAbs + 2, cursorYAbs + 2, 0xFFFF0000);
		}
		RenderSystem.disableBlend();
	}

	private static void renderDyeIcons(GuiGraphics guiGraphics, int centerX, int centerY) {
		float swingDeg = 0f;
		if (selectedSegment != -1) {
			double time = System.currentTimeMillis() / 400.0;
			swingDeg = (float) (Math.sin(time * 2 * Math.PI) * 15.0);
		}
		for (int i = 0; i < 16; i++) {
			double angleRad = Math.toRadians(i * 22.5 - 90);
			int x = centerX + (int) (RADIUS * Math.cos(angleRad));
			int y = centerY + (int) (RADIUS * Math.sin(angleRad));
			int screenX = x - DYE_SIZE / 2;
			int screenY = y - DYE_SIZE / 2;
			if (i == selectedSegment && Math.abs(swingDeg) > 0.01f) {
				guiGraphics.pose().pushPose();
				guiGraphics.pose().translate(x, y, 0);
				guiGraphics.pose().mulPose(Axis.ZP.rotationDegrees(swingDeg));
				guiGraphics.blit(DYE_TEXTURES[i],
						-DYE_SIZE / 2, -DYE_SIZE / 2,
						0, 0,
						DYE_SIZE, DYE_SIZE,
						DYE_SIZE, DYE_SIZE);
				guiGraphics.pose().popPose();
			} else {
				guiGraphics.blit(DYE_TEXTURES[i],
						screenX, screenY,
						0, 0,
						DYE_SIZE, DYE_SIZE,
						DYE_SIZE, DYE_SIZE);
			}
		}
	}

	private static void renderCursor(GuiGraphics guiGraphics, int centerX, int centerY) {
		double angle = Math.atan2(cursorY, cursorX);
		if (cursorX == 0 && cursorY == 0) angle = -Math.PI / 2;
		RenderSystem.enableBlend();
		RenderSystem.blendFuncSeparate(
				GlStateManager.SourceFactor.ONE_MINUS_DST_COLOR,
				GlStateManager.DestFactor.ONE_MINUS_SRC_COLOR,
				GlStateManager.SourceFactor.ONE,
				GlStateManager.DestFactor.ZERO
		);
		guiGraphics.pose().pushPose();
		guiGraphics.pose().translate(centerX, centerY, 0);
		guiGraphics.pose().mulPose(Axis.ZP.rotationDegrees((float) Math.toDegrees(angle)));
		guiGraphics.blit(WHEEL_TEXTURE,
				-CURSOR_WIDTH / 2, -CURSOR_HEIGHT / 2,
				CURSOR_U, CURSOR_V,
				CURSOR_WIDTH, CURSOR_HEIGHT,
				WHEEL_TEX_WIDTH, WHEEL_TEX_HEIGHT);
		guiGraphics.pose().popPose();
		RenderSystem.defaultBlendFunc();
	}

	private static int getCurrentSelectedColorIndex() {
		Minecraft mc = Minecraft.getInstance();
		if (mc.player == null) return -1;
		if (!mc.player.isUsingItem()) return -1;
		ItemStack stack = mc.player.getUseItem();
		if (stack.getItem() != ModItems.PILLAGER_SCROLL.get()) return -1;
		return PillagerScrollItem.getSelectedColor(stack).getIndex();
	}
}
