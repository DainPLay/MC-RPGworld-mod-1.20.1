package net.dainplay.rpgworldmod.mixin;

import net.dainplay.rpgworldmod.RPGworldMod;
import net.dainplay.rpgworldmod.effect.ModEffects;
import net.dainplay.rpgworldmod.gui.ManaOverlayEventHandler;
import net.dainplay.rpgworldmod.network.ClientEntPositionData;
import net.dainplay.rpgworldmod.network.ClientMaxManaData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ForgeGui.class)
public class ForgeGuiMixin {
    ForgeGui thisGui = (ForgeGui) (Object) this;
    @Unique
    private float rpgworldmod$entAmount = 0.0F;
    @Unique
    private long rpgworldmod$lastRenderTime = 0L;
    @Unique
    private boolean rpgworldmod$wasEntDataNull = true;

    @Inject(method = "render", at = @At(value = "TAIL"))
    private void renderParalysisCheck(GuiGraphics guiGraphics, float partialTick, CallbackInfo ci) {
        Minecraft mc = Minecraft.getInstance();

        if (mc.player != null && mc.player.getEffect(ModEffects.PARALYSIS.get()) != null
                && mc.player.getEffect(ModEffects.PARALYSIS.get()).getAmplifier() >= 1) {
            float duration = mc.player.getEffect(ModEffects.PARALYSIS.get()).isInfiniteDuration()
                    ? 1F
                    : (float) mc.player.getEffect(ModEffects.PARALYSIS.get()).getDuration() / 200;
            thisGui.renderTextureOverlay(guiGraphics,
                    new ResourceLocation(RPGworldMod.MOD_ID, "textures/misc/paralysis_outline.png"),
                    duration);
        }

        // Плавная анимация оверлея EntPosition
        if (mc.player != null) {
            boolean hasEntData = ClientEntPositionData.get() != null;

            // Рассчитываем дельту времени для плавной анимации
            long currentTime = System.currentTimeMillis();
            float deltaTime = rpgworldmod$lastRenderTime == 0
                    ? 0.016F // Предполагаем 60 FPS при первом вызове
                    : (currentTime - rpgworldmod$lastRenderTime) / 1000.0F;
            rpgworldmod$lastRenderTime = currentTime;

            // Целевое значение прозрачности
            float targetAlpha = hasEntData ? 1.0F : 0.0F;

            // Скорость изменения (можно настроить под свои нужды)
            float fadeSpeed = 2.0F; // Полная анимация за 0.5 секунды

            // Плавное изменение значения
            if (rpgworldmod$entAmount < targetAlpha) {
                rpgworldmod$entAmount = Math.min(rpgworldmod$entAmount + fadeSpeed * deltaTime, targetAlpha);
            } else if (rpgworldmod$entAmount > targetAlpha) {
                rpgworldmod$entAmount = Math.max(rpgworldmod$entAmount - fadeSpeed * deltaTime, targetAlpha);
            }

            // Рендерим только если есть что рендерить
            if (rpgworldmod$entAmount > 0.001F) {
                thisGui.renderTextureOverlay(guiGraphics,
                        new ResourceLocation(RPGworldMod.MOD_ID, "textures/misc/ent_attract.png"),
                        rpgworldmod$entAmount);
            }

            rpgworldmod$wasEntDataNull = !hasEntData;
        } else {
            // Сброс значений при выходе из мира
            rpgworldmod$entAmount = 0.0F;
            rpgworldmod$lastRenderTime = 0L;
            rpgworldmod$wasEntDataNull = true;
        }
    }

    @ModifyVariable(
            method = "renderRecordOverlay(IIFLnet/minecraft/client/gui/GuiGraphics;)V",
            at = @At("HEAD"),
            argsOnly = true,
            remap = false, ordinal = 1
    )
    private int adjustYShiftForMana(int height) {
        if (ManaOverlayEventHandler.shouldRenderMana()) {
            int currentMana = ClientMaxManaData.get();
            int manaRows = (currentMana + 49) / 50;
            if (ManaOverlayEventHandler.isAirRender() == 0) height += 10;
            return height - (manaRows * 10);
        }
        return height;
    }
}