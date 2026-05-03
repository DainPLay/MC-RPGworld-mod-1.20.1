package net.dainplay.rpgworldmod.mixin;

import net.dainplay.rpgworldmod.enchantment.ModEnchantments;
import net.dainplay.rpgworldmod.gui.HealthOverlayEventHandler;
import net.dainplay.rpgworldmod.gui.ManaOverlayEventHandler;
import net.dainplay.rpgworldmod.item.ModItems;
import net.dainplay.rpgworldmod.item.custom.DaggerItem;
import net.dainplay.rpgworldmod.item.custom.PillagerScrollItem;
import net.dainplay.rpgworldmod.network.ClientMaxManaData;
import net.dainplay.rpgworldmod.util.BeaconSpellStarMenuHandler;
import net.dainplay.rpgworldmod.util.RecolorWoolMenuHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import static net.minecraft.world.item.alchemy.PotionUtils.getMobEffects;

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

		if (this.lastToolHighlight.getItem() != ModItems.BRAIN_CORAL_STAFF.get()
				&& this.lastToolHighlight.getItem() != ModItems.TUBE_CORAL_STAFF.get()
				&& this.lastToolHighlight.getItem() != ModItems.BUBBLE_CORAL_STAFF.get()
				&& this.lastToolHighlight.getItem() != ModItems.HORN_CORAL_STAFF.get()
				&& this.lastToolHighlight.getItem() != ModItems.FIRE_CORAL_STAFF.get()
				&& !(this.lastToolHighlight.getItem() instanceof DaggerItem
				&& PotionUtils.getPotion(this.lastToolHighlight) != Potions.EMPTY
				&& !getMobEffects(this.lastToolHighlight).isEmpty())
				&& !(this.lastToolHighlight.getItem() == ModItems.PILLAGER_SCROLL.get()
				&& this.lastToolHighlight.getEnchantmentLevel(ModEnchantments.ALTERATION.get()) > 0)) {
			return;
		}

		MutableComponent mutablecomponent = Component.empty()
				.append(this.lastToolHighlight.getHoverName())
				.withStyle(this.lastToolHighlight.getRarity().getStyleModifier());
		if (this.lastToolHighlight.hasCustomHoverName()) {
			mutablecomponent.withStyle(ChatFormatting.ITALIC);
		}
		Component highlightTip = this.lastToolHighlight.getHighlightTip(mutablecomponent);


		String firstLineText = highlightTip.getString();
		Style baseStyle = highlightTip.getStyle();
		int color = PillagerScrollItem.getSelectedColor(this.lastToolHighlight).getColor();
		if (this.lastToolHighlight.getItem() == ModItems.BRAIN_CORAL_STAFF.get()) color = 0xE47EB9;
		if (this.lastToolHighlight.getItem() == ModItems.TUBE_CORAL_STAFF.get()) color = 0x405CE2;
		if (this.lastToolHighlight.getItem() == ModItems.BUBBLE_CORAL_STAFF.get()) color = 0xC819BA;
		if (this.lastToolHighlight.getItem() == ModItems.HORN_CORAL_STAFF.get()) color = 0xEDEC4C;
		if (this.lastToolHighlight.getItem() == ModItems.FIRE_CORAL_STAFF.get()) color = 0xC62A37;

		String key = this.lastToolHighlight.getDescriptionId() + ".target";
		if (this.lastToolHighlight.getItem() == ModItems.PILLAGER_SCROLL.get())
			key = Component.translatable("tooltip.rpgworldmod.selected_color." + PillagerScrollItem.getSelectedColor(this.lastToolHighlight).getName()).getString();
		else if (this.lastToolHighlight.getItem() instanceof DaggerItem) key = "";

		int finalColor = color;
		Component secondLineComponent = Component.translatable("tooltip.rpgworldmod.target")
				.withStyle(ChatFormatting.WHITE, ChatFormatting.ITALIC)
				.append(Component.literal(" ").withStyle(ChatFormatting.ITALIC))
				.append(Component.translatable(key).withStyle(ChatFormatting.ITALIC).withStyle(style -> style.withColor(finalColor)));

		if (this.lastToolHighlight.getItem() == ModItems.PILLAGER_SCROLL.get()) {
			secondLineComponent = Component.translatable("tooltip.rpgworldmod.selected_color")
					.withStyle(ChatFormatting.WHITE, ChatFormatting.ITALIC)
					.append(Component.literal(" ").withStyle(ChatFormatting.ITALIC))
					.append(Component.translatable(key).withStyle(ChatFormatting.ITALIC).withStyle(style -> style.withColor(finalColor)));
		} else if (this.lastToolHighlight.getItem() instanceof DaggerItem) {

			for (MobEffectInstance mobeffectinstance : getMobEffects(this.lastToolHighlight)) {
				MobEffect mobeffect = mobeffectinstance.getEffect();
				secondLineComponent = Component.translatable(mobeffectinstance.getDescriptionId()).withStyle(mobeffect.getCategory().getTooltipFormatting());

				if (mobeffectinstance.getAmplifier() > 0) {
					secondLineComponent = Component.translatable("potion.withAmplifier", secondLineComponent, Component.translatable("potion.potency." + mobeffectinstance.getAmplifier()).withStyle(mobeffect.getCategory().getTooltipFormatting()));
				}

				if (!mobeffectinstance.endsWithin(20)) {
					secondLineComponent = Component.translatable("potion.withDuration", secondLineComponent, MobEffectUtil.formatDuration(mobeffectinstance, 0.125F)).withStyle(mobeffect.getCategory().getTooltipFormatting());
				}
			}
		}

		ci.cancel();


		Font font = IClientItemExtensions.of(this.lastToolHighlight)
				.getFont(this.lastToolHighlight, IClientItemExtensions.FontContext.SELECTED_ITEM_NAME);
		if (font == null) {
			font = this.getFont();
		}


		int firstWidth = font.width(firstLineText);
		int secondWidth = font.width(secondLineComponent);
		int maxWidth = Math.max(firstWidth, secondWidth);

		if (ManaOverlayEventHandler.shouldRenderMana()) {
			int currentMana = ClientMaxManaData.get();
			int manaRows = (currentMana + 49) / 50;
			if (ManaOverlayEventHandler.isAirRender() == 0) yShift -= 10;
			yShift += (manaRows * 10);
		}

		int baseY = this.screenHeight - Math.max(yShift, 59);
		if (!Minecraft.getInstance().gameMode.canHurtPlayer()) {
			baseY += 14;
		}

		int alpha = (int) ((float) this.toolHighlightTimer * 256.0F / 10.0F);
		if (alpha > 255) alpha = 255;
		int colorWithAlpha = color + (alpha << 24);

		int lineHeight = font.lineHeight;

		int startY = baseY - lineHeight;

		int bgColor = Minecraft.getInstance().options.getBackgroundColor(0);
		int bgX = (this.screenWidth - maxWidth) / 2;
		guiGraphics.fill(bgX - 2, startY - 2, bgX + maxWidth + 2, startY + 2 * lineHeight + 2, bgColor);


		int x1 = (this.screenWidth - firstWidth) / 2;
		Component firstComponent = Component.literal(firstLineText).withStyle(baseStyle);
		guiGraphics.drawString(font, firstComponent, x1, startY, colorWithAlpha);


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
					ordinal = 0,
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

	@Inject(method = "renderCrosshair", at = @At(value = "HEAD"), cancellable = true)
	private void onRenderCrosshair(GuiGraphics pGuiGraphics, CallbackInfo ci) {
		if (BeaconSpellStarMenuHandler.isActive() || RecolorWoolMenuHandler.isActive()) {
			ci.cancel();
		}
	}

	@Redirect(
			method = "renderHotbar",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/player/LocalPlayer;getAttackStrengthScale(F)F"
			)
	)
	private float renderHotbarDagger(LocalPlayer instance, float v) {
		Player player = Minecraft.getInstance().player;
		if (player.isUsingItem() && player.getUseItem().getItem() instanceof DaggerItem dagger) {
			return (float) (dagger.getUseDuration(player.getUseItem()) - player.getUseItemRemainingTicks()) / (float) dagger.getAttackCooldown();
		}
		return player.getAttackStrengthScale(0.0F);
	}

	@Redirect(
			method = "renderCrosshair",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/player/LocalPlayer;getAttackStrengthScale(F)F"
			)
	)
	private float renderCrosshairDagger(LocalPlayer instance, float v) {
		Player player = Minecraft.getInstance().player;
		if (player.isUsingItem() && player.getUseItem().getItem() instanceof DaggerItem dagger) {
			return (float) (dagger.getUseDuration(player.getUseItem()) - player.getUseItemRemainingTicks()) / (float) dagger.getAttackCooldown();
		}
		return player.getAttackStrengthScale(0.0F);
	}
}
