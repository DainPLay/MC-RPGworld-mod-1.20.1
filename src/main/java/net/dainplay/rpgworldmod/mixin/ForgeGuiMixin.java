package net.dainplay.rpgworldmod.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.dainplay.rpgworldmod.RPGworldMod;
import net.dainplay.rpgworldmod.effect.ModEffects;
import net.dainplay.rpgworldmod.gui.ManaOverlayEventHandler;
import net.dainplay.rpgworldmod.mana.ClientMaxManaData;
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

    @Inject(method = "render", at = @At(value = "TAIL"))
    private void renderParalysisCheck(GuiGraphics guiGraphics, float partialTick, CallbackInfo ci) {
        if (Minecraft.getInstance().player != null && Minecraft.getInstance().player.getEffect(ModEffects.PARALYSIS.get()) != null && Minecraft.getInstance().player.getEffect(ModEffects.PARALYSIS.get()).getAmplifier() >= 1) {
            float duration = Minecraft.getInstance().player.getEffect(ModEffects.PARALYSIS.get()).isInfiniteDuration() ? 1F : (float) Minecraft.getInstance().player.getEffect(ModEffects.PARALYSIS.get()).getDuration() /200;
                Minecraft.getInstance().gui.renderTextureOverlay(guiGraphics, new ResourceLocation(RPGworldMod.MOD_ID,"textures/misc/paralysis_outline.png"), duration);

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
