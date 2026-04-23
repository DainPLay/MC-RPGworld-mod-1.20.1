package net.dainplay.rpgworldmod.item.custom;

import com.mojang.blaze3d.platform.InputConstants;
import net.dainplay.rpgworldmod.enchantment.ModEnchantments;
import net.dainplay.rpgworldmod.item.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public class ClientRPGtooltipHandler {
	public static void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltip, TooltipFlag pFlag, RPGtooltip tooltipItem) {
		if (Minecraft.getInstance().player == null) return;

		if (tooltipItem.hasTarget(pStack)) {
			pTooltip.addAll(getDisplayTargetWithLineBreaks(pStack, tooltipItem));
		}

		if (tooltipItem.hasSelectedColor(pStack)) {
			pTooltip.addAll(getDisplaySelectedColorWithLineBreaks(pStack, tooltipItem));
		}

		if (pStack.getItem() instanceof ManaCostItem item) {
			MutableComponent costText = Component.translatable("tooltip.rpgworldmod.cost_text").withStyle(ChatFormatting.WHITE);
			if (item.usesHealthInsteadOfMana(pStack))
				costText.append(Component.translatable("tooltip.rpgworldmod.hp_cost_number", item.getDisplayManaCost(pStack, Minecraft.getInstance().player)).withStyle(ChatFormatting.RED));
			else
				costText.append(Component.translatable("tooltip.rpgworldmod.cost_number", item.getDisplayManaCost(pStack, Minecraft.getInstance().player)).withStyle(ChatFormatting.BLUE));
			costText.append(item.getManaCostAdditionalLine(pStack));
			pTooltip.add(costText);
		}

		if (pStack.getItem() instanceof DoubleSidedRecordItem item) {
			pTooltip.add(Component.translatable(pStack.getDescriptionId() + ".side").withStyle(ChatFormatting.WHITE));
			pTooltip.add(Component.translatable(pStack.getDescriptionId() + ".desc").withStyle(ChatFormatting.GRAY));
			pTooltip.add(Component.literal(item.getDisplayControls(pStack).getString()).withStyle(ChatFormatting.DARK_GRAY));
		}

		if (pStack.getItem() instanceof StaffItem item && item.hasCooldown(pStack)) {
			pTooltip.addAll(getDisplayCooldownWithLineBreaks(pStack, item));
		}

		if (InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), Minecraft.getInstance().options.keyShift.getKey().getValue())) {
			if (tooltipItem.hasFeatures(pStack)) {
				List<Component> featureLines = getDisplayFeaturesWithLineBreaks(pStack, tooltipItem);
				pTooltip.addAll(featureLines);
			}
			if (tooltipItem instanceof NetherStarScrollItem && pStack.getEnchantmentLevel(ModEnchantments.RESTORATION.get()) > 0) {
				MutableComponent effectText = Component.literal("- ").withStyle(ChatFormatting.GRAY)
						.append(Component.translatable("effect.minecraft.speed").withStyle(ChatFormatting.BLUE))
						.append(Component.literal(" "))
						.append(tooltipItem.getEffectDuration1(pStack).withStyle(ChatFormatting.BLUE));
				pTooltip.add(effectText);
				effectText = Component.literal("- ").withStyle(ChatFormatting.GRAY)
						.append(Component.translatable("effect.minecraft.haste").withStyle(ChatFormatting.BLUE))
						.append(Component.literal(" "))
						.append(tooltipItem.getEffectDuration1(pStack).withStyle(ChatFormatting.BLUE));
				pTooltip.add(effectText);
				effectText = Component.literal("- ").withStyle(ChatFormatting.GRAY)
						.append(Component.translatable("effect.minecraft.resistance").withStyle(ChatFormatting.BLUE))
						.append(Component.literal(" "))
						.append(tooltipItem.getEffectDuration2(pStack).withStyle(ChatFormatting.BLUE));
				pTooltip.add(effectText);
				effectText = Component.literal("- ").withStyle(ChatFormatting.GRAY)
						.append(Component.translatable("effect.minecraft.jump_boost").withStyle(ChatFormatting.BLUE))
						.append(Component.literal(" "))
						.append(tooltipItem.getEffectDuration2(pStack).withStyle(ChatFormatting.BLUE));
				pTooltip.add(effectText);
				effectText = Component.literal("- ").withStyle(ChatFormatting.GRAY)
						.append(Component.translatable("effect.minecraft.strength").withStyle(ChatFormatting.BLUE))
						.append(Component.literal(" "))
						.append(tooltipItem.getEffectDuration3(pStack).withStyle(ChatFormatting.BLUE));
				pTooltip.add(effectText);
			}
			if (tooltipItem.hasControls(pStack)) {
				List<Component> controlsLines = getDisplayControlsWithLineBreaks(pStack, tooltipItem);
				pTooltip.addAll(controlsLines);
			}
		} else {
			if (tooltipItem.hasFeatures(pStack)) {
				List<Component> combinedLines = getHoldShiftTooltipWithLineBreaks(pStack, tooltipItem);
				pTooltip.addAll(combinedLines);
			}
		}
		if (tooltipItem.hasComment(pStack)) {
			pTooltip.addAll(getDisplayCommentWithLineBreaks(pStack, tooltipItem));
		}
	}

	private static List<Component> getDisplayTargetWithLineBreaks(ItemStack item, RPGtooltip tooltipItem) {
		List<Component> result = new ArrayList<>();
		int maxLineLength = getMaxLineLength(item, tooltipItem, 0);

		String header = Component.translatable("tooltip.rpgworldmod.target").getString();
		String targetText = Component.translatable(item.getDescriptionId() + ".target").getString();
		String fullText = header + " " + targetText;
		int color;
		if (item.getItem() == ModItems.BRAIN_CORAL_STAFF.get()) {
			color = 0xE47EB9;
		} else if (item.getItem() == ModItems.TUBE_CORAL_STAFF.get()) {
			color = 0x405CE2;
		} else if (item.getItem() == ModItems.BUBBLE_CORAL_STAFF.get()) {
			color = 0xC819BA;
		} else if (item.getItem() == ModItems.HORN_CORAL_STAFF.get()) {
			color = 0xEDEC4C;
		} else {
			color = 0xC62A37;
		}

		List<String> wrappedLines = wrapText(fullText, maxLineLength);

		if (!wrappedLines.isEmpty()) {
			String firstLine = wrappedLines.get(0);
			int headerEndIndex = firstLine.indexOf(header) + header.length();
			if (headerEndIndex <= firstLine.length()) {
				String whitePart = firstLine.substring(0, Math.min(headerEndIndex, firstLine.length()));
				String coloredPart = firstLine.substring(Math.min(headerEndIndex, firstLine.length()));

				MutableComponent firstLineComponent = Component.literal(whitePart).withStyle(ChatFormatting.WHITE);
				if (!coloredPart.isEmpty()) {
					firstLineComponent.append(Component.literal(coloredPart)
							.withStyle(style -> style.withColor(color)));
				}
				result.add(firstLineComponent);
			} else {
				result.add(Component.literal(firstLine).withStyle(ChatFormatting.WHITE));
			}

			for (int i = 1; i < wrappedLines.size(); i++) {
				result.add(Component.literal(wrappedLines.get(i))
						.withStyle(style -> style.withColor(color)));
			}
		}

		return result;
	}

	private static List<Component> getDisplaySelectedColorWithLineBreaks(ItemStack item, RPGtooltip tooltipItem) {
		List<Component> result = new ArrayList<>();
		int maxLineLength = getMaxLineLength(item, tooltipItem, 0);

		String header = Component.translatable("tooltip.rpgworldmod.selected_color").getString();
		String targetText = Component.translatable("tooltip.rpgworldmod.selected_color." + PillagerScrollItem.getSelectedColor(item).getName()).getString();
		String fullText = header + " " + targetText;
		int color = PillagerScrollItem.getSelectedColor(item).getColor();

		List<String> wrappedLines = wrapText(fullText, maxLineLength);

		if (!wrappedLines.isEmpty()) {
			String firstLine = wrappedLines.get(0);
			int headerEndIndex = firstLine.indexOf(header) + header.length();
			if (headerEndIndex <= firstLine.length()) {
				String whitePart = firstLine.substring(0, Math.min(headerEndIndex, firstLine.length()));
				String coloredPart = firstLine.substring(Math.min(headerEndIndex, firstLine.length()));

				MutableComponent firstLineComponent = Component.literal(whitePart).withStyle(ChatFormatting.WHITE);
				if (!coloredPart.isEmpty()) {
					firstLineComponent.append(Component.literal(coloredPart)
							.withStyle(style -> style.withColor(color)));
				}
				result.add(firstLineComponent);
			} else {
				result.add(Component.literal(firstLine).withStyle(ChatFormatting.WHITE));
			}

			for (int i = 1; i < wrappedLines.size(); i++) {
				result.add(Component.literal(wrappedLines.get(i))
						.withStyle(style -> style.withColor(color)));
			}
		}

		return result;
	}

	private static List<Component> getDisplayCooldownWithLineBreaks(ItemStack item, StaffItem staffItem) {
		List<Component> result = new ArrayList<>();
		int maxLineLength = getMaxLineLength(item, staffItem, 0);

		String header = Component.translatable("tooltip.rpgworldmod.cooldown_text").getString();
		String cooldownText = staffItem.getDisplayCooldown(item).getString();
		String fullText = header + " " + cooldownText;

		List<String> wrappedLines = wrapText(fullText, maxLineLength);

		if (!wrappedLines.isEmpty()) {
			String firstLine = wrappedLines.get(0);
			int headerEndIndex = firstLine.indexOf(header) + header.length();
			if (headerEndIndex <= firstLine.length()) {
				String whitePart = firstLine.substring(0, Math.min(headerEndIndex, firstLine.length()));
				String purplePart = firstLine.substring(Math.min(headerEndIndex, firstLine.length()));

				MutableComponent firstLineComponent = Component.literal(whitePart).withStyle(ChatFormatting.WHITE);
				if (!purplePart.isEmpty()) {
					firstLineComponent.append(Component.literal(purplePart).withStyle(ChatFormatting.DARK_PURPLE));
				}
				result.add(firstLineComponent);
			} else {
				result.add(Component.literal(firstLine).withStyle(ChatFormatting.WHITE));
			}

			for (int i = 1; i < wrappedLines.size(); i++) {
				result.add(Component.literal(wrappedLines.get(i)).withStyle(ChatFormatting.DARK_PURPLE));
			}
		}

		return result;
	}

	private static List<Component> getDisplayFeaturesWithLineBreaks(ItemStack item, RPGtooltip tooltipItem) {
		List<Component> result = new ArrayList<>();
		int maxLineLength = getMaxLineLength(item, tooltipItem, 0);

		String header = Component.translatable("tooltip.rpgworldmod.features").getString();
		String featuresText = tooltipItem.getDisplayFeatures(item).getString();
		String fullText = header + " " + featuresText;

		List<String> wrappedLines = wrapText(fullText, maxLineLength);

		if (!wrappedLines.isEmpty()) {
			String firstLine = wrappedLines.get(0);
			int featuresEndIndex = firstLine.indexOf(header) + header.length();
			if (featuresEndIndex <= firstLine.length()) {
				String whitePart = firstLine.substring(0, Math.min(featuresEndIndex, firstLine.length()));
				String grayPart = firstLine.substring(Math.min(featuresEndIndex, firstLine.length()));

				MutableComponent firstLineComponent = Component.literal(whitePart).withStyle(ChatFormatting.WHITE);
				if (!grayPart.isEmpty()) {
					firstLineComponent.append(Component.literal(grayPart).withStyle(ChatFormatting.GRAY));
				}
				result.add(firstLineComponent);
			} else {
				result.add(Component.literal(firstLine).withStyle(ChatFormatting.WHITE));
			}

			for (int i = 1; i < wrappedLines.size(); i++) {
				result.add(Component.literal(wrappedLines.get(i)).withStyle(ChatFormatting.GRAY));
			}
		}

		return result;
	}

	private static List<Component> getDisplayControlsWithLineBreaks(ItemStack item, RPGtooltip tooltipItem) {
		List<Component> result = new ArrayList<>();
		int maxLineLength = getMaxLineLength(item, tooltipItem, 0);

		String header = Component.translatable("tooltip.rpgworldmod.controls").getString();
		String controlsText = tooltipItem.getDisplayControls(item).getString();
		String fullText = header + " " + controlsText;

		List<String> wrappedLines = wrapText(fullText, maxLineLength);

		if (!wrappedLines.isEmpty()) {
			String firstLine = wrappedLines.get(0);
			int controlsEndIndex = firstLine.indexOf(header) + header.length();
			if (controlsEndIndex <= firstLine.length()) {
				String whitePart = firstLine.substring(0, Math.min(controlsEndIndex, firstLine.length()));
				String grayPart = firstLine.substring(Math.min(controlsEndIndex, firstLine.length()));

				MutableComponent firstLineComponent = Component.literal(whitePart).withStyle(ChatFormatting.WHITE);
				if (!grayPart.isEmpty()) {
					firstLineComponent.append(Component.literal(grayPart).withStyle(ChatFormatting.DARK_GRAY));
				}
				result.add(firstLineComponent);
			} else {
				result.add(Component.literal(firstLine).withStyle(ChatFormatting.WHITE));
			}

			for (int i = 1; i < wrappedLines.size(); i++) {
				result.add(Component.literal(wrappedLines.get(i)).withStyle(ChatFormatting.DARK_GRAY));
			}
		}

		return result;
	}

	private static List<Component> getHoldShiftTooltipWithLineBreaks(ItemStack item, RPGtooltip tooltipItem) {
		List<Component> result = new ArrayList<>();

		String featuresText = Component.translatable("tooltip.rpgworldmod.features").getString();
		String holdShiftText = Component.translatable("tooltip.rpgworldmod.hold_shift_for_features",
				Minecraft.getInstance().options.keyShift.getKey().getDisplayName()).getString();

		String fullText = featuresText + " " + holdShiftText;
		int maxLineLength = getMaxLineLength(item, tooltipItem, 0);

		List<String> wrappedLines = wrapText(fullText, maxLineLength);

		if (!wrappedLines.isEmpty()) {
			String firstLine = wrappedLines.get(0);
			int featuresEndIndex = firstLine.indexOf(featuresText) + featuresText.length();
			if (featuresEndIndex <= firstLine.length()) {
				String whitePart = firstLine.substring(0, Math.min(featuresEndIndex, firstLine.length()));
				String grayPart = firstLine.substring(Math.min(featuresEndIndex, firstLine.length()));

				MutableComponent firstLineComponent = Component.literal(whitePart).withStyle(ChatFormatting.WHITE);
				if (!grayPart.isEmpty()) {
					firstLineComponent.append(Component.literal(grayPart).withStyle(ChatFormatting.GRAY));
				}
				result.add(firstLineComponent);
			} else {
				result.add(Component.literal(firstLine).withStyle(ChatFormatting.WHITE));
			}

			for (int i = 1; i < wrappedLines.size(); i++) {
				result.add(Component.literal(wrappedLines.get(i)).withStyle(ChatFormatting.GRAY));
			}
		}

		return result;
	}

	private static List<Component> getDisplayCommentWithLineBreaks(ItemStack item, RPGtooltip tooltipItem) {
		List<Component> result = new ArrayList<>();
		int maxLineLength = getMaxLineLength(item, tooltipItem, 5);
		String commentText = tooltipItem.getDisplayName(item).getString();
		List<String> wrappedLines = wrapText(commentText, maxLineLength);
		for (String line : wrappedLines) {
			result.add(Component.literal(line)
					.withStyle(ChatFormatting.ITALIC)
					.withStyle(ChatFormatting.RED));
		}
		return result;
	}

	private static int getMaxLineLength(ItemStack stack, RPGtooltip tooltipItem, int additional) {
		String displayName = stack.getHoverName().getString();
		int nameLength = displayName.length();
		return Math.max(tooltipItem.textLength(stack) + additional, nameLength);
	}

	private static List<String> wrapText(String text, int maxLineLength) {
		List<String> result = new ArrayList<>();

		if (text.length() <= maxLineLength) {
			result.add(text);
			return result;
		}

		int startIndex = 0;
		while (startIndex < text.length()) {
			int endIndex = Math.min(startIndex + maxLineLength, text.length());

			if (endIndex < text.length() && !Character.isWhitespace(text.charAt(endIndex))) {
				int lastSpace = text.lastIndexOf(' ', endIndex);
				if (lastSpace > startIndex) {
					endIndex = lastSpace;
				}
			}

			String line = text.substring(startIndex, endIndex).trim();
			if (!line.isEmpty()) {
				result.add(line);
			}

			startIndex = endIndex;
			while (startIndex < text.length() && Character.isWhitespace(text.charAt(startIndex))) {
				startIndex++;
			}
		}

		return result;
	}
}