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
import net.minecraft.world.item.DyeColor;
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

    private static final int RADIUS = 60;
    private static final int SIZE = 32;
    private static final int MAX_RADIUS = 30;
    private static final int ARROW_SIZE = 15;
    private static final int MENU_TEXTURE_SIZE = 128;

    private static final ResourceLocation RECOLOR_MENU = new ResourceLocation(RPGworldMod.MOD_ID, "textures/gui/star_menu.png");

    private static final ResourceLocation[] DYE_TEXTURES = new ResourceLocation[16];

    static {
        for (DyeColor color : DyeColor.values()) {
            DYE_TEXTURES[color.getId()] = new ResourceLocation("textures/item/" + color.getName() + "_dye.png");
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
        double corrected = (angleDeg - 80 + 360) % 360;
        int seg = (int) (corrected / 22.5);
        selectedSegment = seg % 16;
    }

    private static boolean shouldBeActive(Player player) {
        return player.isUsingItem()
                && player.getUseItem().getItem() == ModItems.PILLAGER_SCROLL.get()
                && EnchantmentHelper.getEnchantments(player.getUseItem()).containsKey(ModEnchantments.ALTERATION.get());
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

        double cursorAngle;
        if (cursorX == 0 && cursorY == 0) {
            cursorAngle = -Math.PI / 2; // вверх при нулевом курсоре
        } else {
            cursorAngle = Math.atan2(cursorY, cursorX);
        }

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        final double MAX_DIST = RADIUS;      // 60
        final double MIN_DIST_SELECTED = 30; // макс. приближение выбранного
        final double MIN_DIST_OTHER = 45;    // макс. приближение остальных (в 2 раза слабее)
        final double ANGLE_THRESHOLD = 90.0; // градусов, при которых расстояние становится максимальным

        for (int i = 0; i < 16; i++) {
            // Угол текущего сегмента
            double segmentAngleRad = Math.toRadians(i * 22.5 - 90);

            // Минимальная угловая разница
            double diffRad = Math.abs(segmentAngleRad - cursorAngle);
            if (diffRad > Math.PI) {
                diffRad = 2 * Math.PI - diffRad;
            }
            double diffDeg = Math.toDegrees(diffRad);

            // Быстрый спад: квадратичная зависимость
            double t = Math.min(1.0, diffDeg / ANGLE_THRESHOLD);
            t = t * t;

            // Выбираем минимальное расстояние в зависимости от того, выбран ли сегмент
            double minDist = (selectedSegment == i) ? MIN_DIST_SELECTED : MIN_DIST_OTHER;
            double distance = minDist + (MAX_DIST - minDist) * t;

            int x = centerX + (int) (distance * Math.cos(segmentAngleRad));
            int y = centerY + (int) (distance * Math.sin(segmentAngleRad));

            // Коррекция для диагональных элементов (для сохранения визуальной симметрии)
            boolean isDiagonal = (i == 2 || i == 6 || i == 10 || i == 14);
            if (isDiagonal) {
                int dx = 0, dy = 0;
                if (i == 2) { dx = 1; dy = -1; }
                else if (i == 6) { dx = 1; dy = 1; }
                else if (i == 10) { dx = -1; dy = 1; }
                else if (i == 14) { dx = -1; dy = -1; }
                x += dx;
                y += dy;
            }

            int texX = x - SIZE / 2;
            int texY = y - SIZE / 2;

            // Фон слота
            guiGraphics.blit(RECOLOR_MENU, texX, texY, 0, 54, SIZE, SIZE, MENU_TEXTURE_SIZE, MENU_TEXTURE_SIZE);
            // Иконка красителя
            ResourceLocation dyeTex = DYE_TEXTURES[i];
            guiGraphics.blit(dyeTex, x - 8, y - 8, 0, 0, 16, 16, 16, 16);

            // Выделение выбранного сегмента
            if (selectedSegment == i) {
                guiGraphics.blit(RECOLOR_MENU, texX, texY, 32, 54, SIZE, SIZE, MENU_TEXTURE_SIZE, MENU_TEXTURE_SIZE);
            }
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
        guiGraphics.pose().mulPose(com.mojang.math.Axis.ZP.rotationDegrees((float) Math.toDegrees(angle)));
        guiGraphics.blit(RECOLOR_MENU, -ARROW_SIZE / 2, -ARROW_SIZE / 2, 92, 0, ARROW_SIZE, ARROW_SIZE, MENU_TEXTURE_SIZE, MENU_TEXTURE_SIZE);
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