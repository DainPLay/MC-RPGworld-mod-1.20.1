package net.dainplay.rpgworldmod.item.custom;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public interface RPGtooltip {

    default void RPGappendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltip, TooltipFlag pFlag) {

        if (Minecraft.getInstance().player == null)
            return;
        if(pStack.getItem() instanceof ManaCostItem item) {
            MutableComponent costText = Component.translatable("tooltip.rpgworldmod.cost_text").withStyle(ChatFormatting.WHITE);
            if (item.usesHealthInsteadOfMana(pStack))
                costText.append(Component.translatable("tooltip.rpgworldmod.hp_cost_number", item.getDisplayManaCost(pStack, Minecraft.getInstance().player)).withStyle(ChatFormatting.RED));
            else
                costText.append(Component.translatable("tooltip.rpgworldmod.cost_number", item.getDisplayManaCost(pStack, Minecraft.getInstance().player)).withStyle(ChatFormatting.BLUE));
            costText.append(item.getManaCostAdditionalLine(pStack));
            pTooltip.add(costText);
        }
        // Изменено: теперь кулдаун разбивается на строки
        if(pStack.getItem() instanceof StaffItem item && item.hasCooldown(pStack)) {
            pTooltip.addAll(getDisplayCooldownWithLineBreaks(pStack)); // Добавлен вызов нового метода
        }
        if (InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), Minecraft.getInstance().options.keyShift.getKey().getValue())) {
            List<Component> featureLines = this.getDisplayFeaturesWithLineBreaks(pStack);
            pTooltip.addAll(featureLines);
            if(hasControls(pStack)) {
                List<Component> controlsLines = this.getDisplayControlsWithLineBreaks(pStack);
                pTooltip.addAll(controlsLines);
            }
        } else {
            List<Component> combinedLines = this.getHoldShiftTooltipWithLineBreaks();
            pTooltip.addAll(combinedLines);
        }
        pTooltip.add(this.getDisplayName(pStack).withStyle(ChatFormatting.ITALIC).withStyle(ChatFormatting.RED));
    }

    default MutableComponent getDisplayName(ItemStack item) {
        return Component.translatable(((Item)this).getDescriptionId(item) + ".desc");
    }

    default MutableComponent getDisplayFeatures(ItemStack item) {
        return Component.translatable(((Item)this).getDescriptionId(item) + ".features",
                getFirstPredicate(item),
                getSecondPredicate(item));
    }

    default MutableComponent getDisplayControls(ItemStack item) {
        return Component.translatable(((Item)this).getDescriptionId(item) + ".controls",
                getFirstPredicate(item),
                getSecondPredicate(item));
    }

    default String getFirstPredicate(ItemStack item) {
        return "";
    }

    default String getSecondPredicate(ItemStack item) {
        return "";
    }

    default Boolean hasControls(ItemStack item) {
        return false;
    }

    // Добавлен метод для разбиения кулдауна
    private List<Component> getDisplayCooldownWithLineBreaks(ItemStack item) {
        List<Component> result = new ArrayList<>();

        String header = Component.translatable("tooltip.rpgworldmod.cooldown_text").getString();
        String cooldownText = ((StaffItem) item.getItem()).getDisplayCooldown(item).getString(); // получаем строку кулдауна
        String fullText = header + " " + cooldownText;

        List<String> wrappedLines = wrapText(fullText, 25);

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

    private List<Component> getDisplayFeaturesWithLineBreaks(ItemStack item) {
        List<Component> result = new ArrayList<>();

        String header = Component.translatable("tooltip.rpgworldmod.features").getString();
        String featuresText = this.getDisplayFeatures(item).getString();
        String fullText = header + " " + featuresText;

        List<String> wrappedLines = wrapText(fullText, 25);

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

    private List<Component> getDisplayControlsWithLineBreaks(ItemStack item) {
        List<Component> result = new ArrayList<>();

        String header = Component.translatable("tooltip.rpgworldmod.controls").getString();
        String controlsText = this.getDisplayControls(item).getString();
        String fullText = header + " " + controlsText;

        List<String> wrappedLines = wrapText(fullText, 25);

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

    private List<Component> getHoldShiftTooltipWithLineBreaks() {
        List<Component> result = new ArrayList<>();

        String featuresText = Component.translatable("tooltip.rpgworldmod.features").getString();
        String holdShiftText = Component.translatable("tooltip.rpgworldmod.hold_shift_for_features",
                Minecraft.getInstance().options.keyShift.getKey().getDisplayName()).getString();

        String fullText = featuresText + " " + holdShiftText;

        List<String> wrappedLines = wrapText(fullText, 25);

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

    private List<String> wrapText(String text, int maxLineLength) {
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