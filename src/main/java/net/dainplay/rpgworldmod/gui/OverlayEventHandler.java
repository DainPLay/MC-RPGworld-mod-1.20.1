package net.dainplay.rpgworldmod.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.dainplay.rpgworldmod.RPGworldMod;
import net.dainplay.rpgworldmod.effect.ModEffects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;

import java.util.Objects;
import java.util.Random;

public class OverlayEventHandler implements IGuiOverlay {
    public static final ResourceLocation MOSSIOSIS_HEARTS = new ResourceLocation(RPGworldMod.MOD_ID, "textures/gui/mossiosis_hearts.png");
    static int renderHeartY = 0;
    static int regen = -1;

    public static void drawTexturedModalRect(GuiGraphics stack, int x, int y, int textureX, int textureY, int width, int height) {
        stack.blit(MOSSIOSIS_HEARTS,x, y, textureX, textureY, width, height);
    }
public static void setRenderHeartY(int value){
    renderHeartY = value;
}
    public static void setRegen(int value){
        regen = value;
    }
    private final static int UNKNOWN_ARMOR_VALUE = -1;
    private static int previousMossValue = UNKNOWN_ARMOR_VALUE;

    private static final Minecraft mc = Minecraft.getInstance();
    private static MossIcon[] mossIcons;


    public void render(ForgeGui gui, GuiGraphics guiGraphics, float partialTick, int screenWidth, int screenHeight) {
        if (!mc.options.hideGui && gui.shouldDrawSurvivalElements()) {
            gui.setupOverlayRenderState(true, false);
            renderMossBar(gui, guiGraphics, screenWidth, screenHeight);
        }
    }
    private static int calculateMossValue() {
        if (mc.player != null && mc.player.hasEffect(ModEffects.MOSSIOSIS.get()))
            return (Objects.requireNonNull(mc.player.getEffect(ModEffects.MOSSIOSIS.get())).getAmplifier()+1) * 6;
        else return -1;
    }

    public static void renderMossBar(ForgeGui gui, GuiGraphics stack, int screenWidth, int screenHeight) {
        int currentMossValue = calculateMossValue();
        int xStart = screenWidth / 2 - 91;
        int health = Mth.ceil(mc.player.getHealth());

        if (currentMossValue != previousMossValue) {
            mossIcons = MossBar.calculateMossIcons(currentMossValue);
            previousMossValue = currentMossValue;
        }
        if (health > 0)
        for (int mossIconCounter = 0; mossIconCounter < currentMossValue/2; mossIconCounter++) {
            int xPosition = xStart + mossIconCounter * 8;
            int yPosition = renderHeartY;
            if (health <= 4) {
                yPosition += new Random().nextInt(2);
            }
            if (mossIconCounter == regen) {
                yPosition -= 2;
            }
            switch (mossIcons[mossIconCounter].mossIconType) {
                case NONE:
                    if (currentMossValue > 20) {
                        drawTexturedModalRect(stack,xPosition, yPosition, 0, (mc.player.level().getLevelData().isHardcore() ? 9 : 0), 9, 9);
                    }
                    break;
                case HALF:
                    if (currentMossValue > 20) {
                        drawTexturedModalRect(stack,xPosition + 5, yPosition, 9, (mc.player.level().getLevelData().isHardcore() ? 9 : 0), 9, 9);
                    } else {
                        drawTexturedModalRect(stack,xPosition + 5, yPosition, 5, 18, 4, 9);
                    }
                    break;
                case FULL:
                    drawTexturedModalRect(stack,xPosition, yPosition, 0, (mc.player.level().getLevelData().isHardcore() ? 9 : 0), 9, 9);
                    break;
                default:
                    break;
            }
        }

        color4f(1, 1, 1, 1);
    }

    private static void color4f(float r, float g, float b, float a){
        RenderSystem.setShaderColor(r,g, b, a);
    }
}