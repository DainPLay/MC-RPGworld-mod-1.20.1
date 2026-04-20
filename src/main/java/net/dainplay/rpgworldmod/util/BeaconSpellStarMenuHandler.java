package net.dainplay.rpgworldmod.util;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.dainplay.rpgworldmod.RPGworldMod;
import net.dainplay.rpgworldmod.enchantment.ModEnchantments;
import net.dainplay.rpgworldmod.item.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid = RPGworldMod.MOD_ID, value = Dist.CLIENT)
public class BeaconSpellStarMenuHandler {
	private static boolean active = false;
	private static double cursorX = 0.0;
	private static double cursorY = 0.0;
	private static int selectedSegment = 0;


	private static final int RADIUS = 30;
	private static final int SIZE = 32;
	private static final int MAX_RADIUS = RADIUS;
	private static final int ARROW_SIZE = 15;
	private static final int STAR_MENU_SIZE = 128;

	private static final ResourceLocation STAR_MENU = new ResourceLocation(RPGworldMod.MOD_ID, "textures/gui/star_menu.png");
	private static final ResourceLocation EFFECT_SPEED = new ResourceLocation("textures/mob_effect/speed.png");
	private static final ResourceLocation EFFECT_HASTE = new ResourceLocation("textures/mob_effect/haste.png");
	private static final ResourceLocation EFFECT_RESISTANCE = new ResourceLocation("textures/mob_effect/resistance.png");
	private static final ResourceLocation EFFECT_JUMP_BOOST = new ResourceLocation("textures/mob_effect/jump_boost.png");
	private static final ResourceLocation EFFECT_STRENGTH = new ResourceLocation("textures/mob_effect/strength.png");

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
		cursorX = 0.0;
		cursorY = 0.0;
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
			selectedSegment = 0;
			return;
		}


		double angleRad = Math.atan2(-cursorY, -cursorX);
		double angleDeg = Math.toDegrees(angleRad);
		angleDeg = (angleDeg + 360) % 360;
		double corrected = (angleDeg - 55 + 360) % 360;
		int seg = (int) (corrected / 72.0);
		selectedSegment = seg % 5;
	}

	private static boolean shouldBeActive(Player player) {
		return player.isUsingItem()
				&& player.getUseItem().getItem() == ModItems.NETHER_STAR_SCROLL.get()
				&& EnchantmentHelper.getEnchantments(player.getUseItem()).containsKey(ModEnchantments.RESTORATION.get());
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

	private static void render(GuiGraphics guiGraphics, int width, int height) {
		int centerX = width / 2;
		int centerY = height / 2;

		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();


		for (int i = 0; i < 5; i++) {
			double angleRad = Math.toRadians(i * 72 - 90);
			int x = centerX + (int) (RADIUS * Math.cos(angleRad));
			int y = centerY + (int) (RADIUS * Math.sin(angleRad));
			int texX = x - SIZE / 2;
			int texY = y - SIZE / 2 + 3;

			ResourceLocation effect = switch (i) {
				case 0 -> EFFECT_SPEED;
				case 1 -> EFFECT_HASTE;
				case 2 -> EFFECT_RESISTANCE;
				case 3 -> EFFECT_JUMP_BOOST;
				case 4 -> EFFECT_STRENGTH;
				default -> EFFECT_SPEED;
			};

			guiGraphics.blit(STAR_MENU, texX, texY, 0, 22, SIZE, SIZE, STAR_MENU_SIZE, STAR_MENU_SIZE);
			guiGraphics.blit(effect, x - 9, y - 6, 0, 0, 18, 18, 18, 18);
			if (selectedSegment == i)
				guiGraphics.blit(STAR_MENU, texX, texY, 32, 22, SIZE, SIZE, STAR_MENU_SIZE, STAR_MENU_SIZE);
		}


		if (DEBUG_CURSOR) {
			int cursorXAbs = centerX + (int) cursorX;
			int cursorYAbs = centerY + (int) cursorY;
			guiGraphics.fill(cursorXAbs - 2, cursorYAbs - 2, cursorXAbs + 2, cursorYAbs + 2, 0xFFFF0000);
		}


		renderArrow(guiGraphics, centerX, centerY);

		RenderSystem.disableBlend();
	}

	private static void renderArrow(GuiGraphics guiGraphics, int centerX, int centerY) {
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


		float degrees = (float) Math.toDegrees(angle);
		guiGraphics.pose().mulPose(com.mojang.math.Axis.ZP.rotationDegrees(degrees));


		guiGraphics.blit(STAR_MENU, -ARROW_SIZE / 2, -ARROW_SIZE / 2, 92, 0, ARROW_SIZE, ARROW_SIZE, STAR_MENU_SIZE, STAR_MENU_SIZE);


		guiGraphics.pose().popPose();


		RenderSystem.defaultBlendFunc();
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
}