package net.dainplay.rpgworldmod.mixin;

import net.dainplay.rpgworldmod.gui.HealthOverlayEventHandler;
import net.dainplay.rpgworldmod.gui.ManaOverlayEventHandler;
import net.dainplay.rpgworldmod.item.ModItems;
import net.dainplay.rpgworldmod.network.ClientMaxManaData;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(Gui.class)
public abstract class GuiMixin {

	@Shadow
	public abstract Font getFont();

	@Shadow
	protected ItemStack lastToolHighlight;
	@Shadow
	protected int toolHighlightTimer;
	@Shadow
	protected int screenWidth;
	@Shadow
	protected int screenHeight;

	@Inject(method = "renderSelectedItemName(Lnet/minecraft/client/gui/GuiGraphics;I)V",
			at = @At("HEAD"),
			cancellable = true,
			remap = false)
	private void onRenderSelectedItemName(GuiGraphics guiGraphics, int yShift, CallbackInfo ci) {
		if (this.toolHighlightTimer <= 0 || this.lastToolHighlight.isEmpty()) {
			return;
		}

		// Вмешиваемся только для нужного предмета
		if (this.lastToolHighlight.getItem() != ModItems.BRAIN_CORAL_STAFF.get()
				&& this.lastToolHighlight.getItem() != ModItems.TUBE_CORAL_STAFF.get()
				&& this.lastToolHighlight.getItem() != ModItems.BUBBLE_CORAL_STAFF.get()
				&& this.lastToolHighlight.getItem() != ModItems.HORN_CORAL_STAFF.get()
				&& this.lastToolHighlight.getItem() != ModItems.FIRE_CORAL_STAFF.get()) {
			return; // для остальных оставляем оригинальное поведение
		}

		// Формируем компонент имени точно как в оригинале
		MutableComponent mutablecomponent = Component.empty()
				.append(this.lastToolHighlight.getHoverName())
				.withStyle(this.lastToolHighlight.getRarity().getStyleModifier());
		if (this.lastToolHighlight.hasCustomHoverName()) {
			mutablecomponent.withStyle(ChatFormatting.ITALIC);
		}
		Component highlightTip = this.lastToolHighlight.getHighlightTip(mutablecomponent);

		// Первая строка: текст оригинального названия с его стилем
		String firstLineText = highlightTip.getString();
		Style baseStyle = highlightTip.getStyle();
		int color = 16777215;
		if (this.lastToolHighlight.getItem() == ModItems.BRAIN_CORAL_STAFF.get()) color = 0xE47EB9;
		if (this.lastToolHighlight.getItem() == ModItems.TUBE_CORAL_STAFF.get()) color = 0x405CE2;
		if (this.lastToolHighlight.getItem() == ModItems.BUBBLE_CORAL_STAFF.get()) color = 0xC819BA;
		if (this.lastToolHighlight.getItem() == ModItems.HORN_CORAL_STAFF.get()) color = 0xEDEC4C;
		if (this.lastToolHighlight.getItem() == ModItems.FIRE_CORAL_STAFF.get()) color = 0xC62A37;

		String key = this.lastToolHighlight.getDescriptionId() + ".target";
		int finalColor = color;
		Component secondLineComponent = Component.translatable("tooltip.rpgworldmod.target")
				.withStyle(ChatFormatting.WHITE, ChatFormatting.ITALIC)
				.append(Component.literal(" ").withStyle(ChatFormatting.ITALIC))
				.append(Component.translatable(key).withStyle(ChatFormatting.ITALIC).withStyle(style -> style.withColor(finalColor)));

		// Отменяем стандартную отрисовку
		ci.cancel();

		// Определяем шрифт (кастомный, если есть)
		Font font = IClientItemExtensions.of(this.lastToolHighlight)
				.getFont(this.lastToolHighlight, IClientItemExtensions.FontContext.SELECTED_ITEM_NAME);
		if (font == null) {
			font = this.getFont();
		}

		// Ширина каждой строки и максимальная ширина
		int firstWidth = font.width(firstLineText);
		int secondWidth = font.width(secondLineComponent);
		int maxWidth = Math.max(firstWidth, secondWidth);

		// Базовый Y (низ последней строки, как в оригинале)
		int baseY = this.screenHeight - Math.max(yShift, 59);
		if (!Minecraft.getInstance().gameMode.canHurtPlayer()) {
			baseY += 14;
		}

		// Альфа-канал для эффекта появления/исчезновения
		int alpha = (int) ((float) this.toolHighlightTimer * 256.0F / 10.0F);
		if (alpha > 255) alpha = 255;
		int colorWithAlpha = color + (alpha << 24);

		int lineHeight = font.lineHeight;
		// Верх первой строки (так как строк две)
		int startY = baseY - lineHeight;

		// Фон под всем текстом
		int bgColor = Minecraft.getInstance().options.getBackgroundColor(0);
		int bgX = (this.screenWidth - maxWidth) / 2;
		guiGraphics.fill(bgX - 2, startY - 2, bgX + maxWidth + 2, startY + 2 * lineHeight + 2, bgColor);

		// Рисуем первую строку (центрированную)
		int x1 = (this.screenWidth - firstWidth) / 2;
		Component firstComponent = Component.literal(firstLineText).withStyle(baseStyle);
		guiGraphics.drawString(font, firstComponent, x1, startY, colorWithAlpha);

		// Рисуем вторую строку (центрированную)
		int x2 = (this.screenWidth - secondWidth) / 2;
		guiGraphics.drawString(font, secondLineComponent, x2, startY + lineHeight, colorWithAlpha);
	}

	@Inject(method = "renderHearts", at = @At(value = "HEAD"))
	private void Send(GuiGraphics pGuiGraphics, Player pPlayer, int pX, int pY, int pHeight, int pOffsetHeartIndex, float pMaxHealth, int pCurrentHealth, int pDisplayHealth, int pAbsorptionAmount, boolean pRenderHighlight, CallbackInfo ci) {
		HealthOverlayEventHandler.setRenderHeartY(pY);
		HealthOverlayEventHandler.setRegen(pOffsetHeartIndex);
	}

	@Inject(
			method = "renderHearts",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/gui/Gui;renderHeart(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/gui/Gui$HeartType;IIIZZ)V",
					ordinal = 0, // первый вызов renderHeart (контейнер)
					shift = At.Shift.AFTER
			),
			locals = LocalCapture.CAPTURE_FAILHARD
	)
	private void onRenderEachHeart(
			GuiGraphics pGuiGraphics, Player pPlayer, int pX, int pY, int pHeight,
			int pOffsetHeartIndex, float pMaxHealth, int pCurrentHealth,
			int pDisplayHealth, int pAbsorptionAmount, boolean pRenderHighlight,
			CallbackInfo ci,
			Gui.HeartType heartType, int i, int j, int k, int l,
			int i1, int j1, int k1, int l1, int i2
	) {
		// Этот метод будет вызываться для каждого сердца
		HealthOverlayEventHandler.setRandomOffset(i1, i2);
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
