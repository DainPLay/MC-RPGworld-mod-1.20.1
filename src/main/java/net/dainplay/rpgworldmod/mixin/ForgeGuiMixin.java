package net.dainplay.rpgworldmod.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import net.dainplay.rpgworldmod.RPGworldMod;
import net.dainplay.rpgworldmod.effect.ModEffects;
import net.dainplay.rpgworldmod.gui.ManaOverlayEventHandler;
import net.dainplay.rpgworldmod.network.ClientEntPositionData;
import net.dainplay.rpgworldmod.network.ClientMaxManaData;
import net.dainplay.rpgworldmod.util.ClientEyeViewHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

@Mixin(ForgeGui.class)
public class ForgeGuiMixin {
    ForgeGui thisGui = (ForgeGui) (Object) this;
    @Unique
    private float rpgworldmod$entAmount = 0.0F;
    @Unique
    private long rpgworldmod$lastRenderTime = 0L;
    @Unique
    private boolean rpgworldmod$wasEntDataNull = true;

    @Inject(method = "render", at = @At(value = "HEAD"))
    private void renderParalysisCheck(GuiGraphics guiGraphics, float partialTick, CallbackInfo ci) {
        Minecraft mc = Minecraft.getInstance();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

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

        // Рендерим оверлей, если камера внутри твёрдого непрозрачного блока
        if (ClientEyeViewHandler.isActive()) {
            renderBlockOverlay(guiGraphics, mc);
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

    /**
     * Проверяет, находится ли камера внутри твёрдого непрозрачного блока.
     * Если да – рисует текстуру этого блока на весь экран.
     */
    @Unique
    private void renderBlockOverlay(GuiGraphics guiGraphics, Minecraft mc) {
        if (mc.player == null || mc.level == null) return;

        // Позиция камеры
        var camera = mc.gameRenderer.getMainCamera();
        BlockPos pos = camera.getBlockPosition();
        BlockState blockState = mc.level.getBlockState(pos);

        // Проверяем, что блок существует, твёрдый и непрозрачный
        if (!blockState.isAir() && blockState.isSolidRender(mc.level, pos) && blockState.isSolid()) {
            ResourceLocation texture = getBlockTexture(blockState.getBlock());
            int width = mc.getWindow().getGuiScaledWidth();
            int height = mc.getWindow().getGuiScaledHeight();

            RenderSystem.disableDepthTest();
            RenderSystem.depthMask(false);
            RenderSystem.defaultBlendFunc();

            if (texture != null) {
                // Рисуем текстуру блока полностью непрозрачной
                guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
                guiGraphics.blit(texture, 0, 0, 0, 0, 0, width, height, width, height);
            } else {
                // Если текстура не найдена, рисуем чёрный квадрат (полностью непрозрачный)
                guiGraphics.fill(0, 0, width, height, 0xFF000000);
            }

            // Затемнение на 95% – чёрный слой с альфой 0x95 (242/255 ≈ 0.95)
            guiGraphics.fill(0, 0, width, height, 0xF2000000);

            // Восстанавливаем цвет и настройки рендеринга
            guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.depthMask(true);
            RenderSystem.enableDepthTest();
        }
    }

    /**
     * Получает ResourceLocation текстуры блока, предполагая стандартный путь
     * textures/block/{registry_path}.png.
     */
    @Unique
    private ResourceLocation getBlockTexture(Block block) {
        ResourceLocation key = BuiltInRegistries.BLOCK.getKey(block);
        if (key == null) return null;
        ResourceLocation location = new ResourceLocation(key.getNamespace(), "textures/block/" + key.getPath() + ".png");
        // Проверяем существование ресурса
        Optional<Resource> resource = Minecraft.getInstance().getResourceManager().getResource(location);
        return resource.isPresent() ? location : null;
    }

}