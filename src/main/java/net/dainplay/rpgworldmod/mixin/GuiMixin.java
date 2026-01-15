package net.dainplay.rpgworldmod.mixin;

import net.dainplay.rpgworldmod.gui.ManaOverlayEventHandler;
import net.dainplay.rpgworldmod.gui.OverlayEventHandler;
import net.dainplay.rpgworldmod.network.ClientMaxManaData;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public abstract class GuiMixin {

	@Inject(method = "renderHearts", at = @At(value = "HEAD"))
	private void Send(GuiGraphics pGuiGraphics, Player pPlayer, int pX, int pY, int pHeight, int pOffsetHeartIndex, float pMaxHealth, int pCurrentHealth, int pDisplayHealth, int pAbsorptionAmount, boolean pRenderHighlight, CallbackInfo ci) {
		OverlayEventHandler.setRenderHeartY(pY);
		OverlayEventHandler.setRegen(pOffsetHeartIndex);
	}

	@ModifyVariable(
			method = "renderSelectedItemName(Lnet/minecraft/client/gui/GuiGraphics;I)V",
			at = @At("HEAD"),
			argsOnly = true,
			remap = false
	)
	private int adjustYShiftForMana(int yShift) {
		if (ManaOverlayEventHandler.shouldRenderMana()) {
			int currentMana = ClientMaxManaData.get();
			int manaRows = (currentMana + 49) / 50;
			if (ManaOverlayEventHandler.isAirRender() == 0) yShift -= 10;
			return yShift + (manaRows * 10);
		}

		return yShift;
	}
}
