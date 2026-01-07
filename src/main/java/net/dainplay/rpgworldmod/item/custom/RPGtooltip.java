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
            costText.append(Component.translatable("tooltip.rpgworldmod.cost_number", item.getManaCost()).withStyle(ChatFormatting.BLUE));
            pTooltip.add(costText);
        }
        if (InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), Minecraft.getInstance().options.keyShift.getKey().getValue())) {
            // При зажатом Shift: заголовок белый, текст особенностей серый с переносами
            List<Component> featureLines = this.getDisplayFeaturesWithLineBreaks(pStack);
            pTooltip.addAll(featureLines);
        } else {
            // При отжатом Shift: объединяем с переносами при необходимости
            List<Component> combinedLines = this.getHoldShiftTooltipWithLineBreaks();
            pTooltip.addAll(combinedLines);
        }
        pTooltip.add(this.getDisplayName().withStyle(ChatFormatting.ITALIC).withStyle(ChatFormatting.RED));
    }

    default MutableComponent getDisplayName() {
        return Component.translatable(((Item)this).getDescriptionId() + ".desc");
    }

    default MutableComponent getDisplayFeatures(ItemStack item) {
        return Component.translatable(((Item)this).getDescriptionId() + ".features");
    }

    /**
     * Метод для получения списка компонентов с переносами строк,
     * где заголовок белый, а текст особенностей серый
     */
    private List<Component> getDisplayFeaturesWithLineBreaks(ItemStack item) {
        List<Component> result = new ArrayList<>();

        // Получаем текст заголовка и особенностей
        String header = Component.translatable("tooltip.rpgworldmod.features").getString();
        String featuresText = this.getDisplayFeatures(item).getString();
        String fullText = header + " " + featuresText;

        // Разбиваем текст на строки не более 25 символов
        List<String> wrappedLines = wrapText(fullText, 25);

        // Первая строка - заголовок белый
        if (!wrappedLines.isEmpty()) {
            String firstLine = wrappedLines.get(0);
            int featuresEndIndex = firstLine.indexOf(header) + header.length();
            if (featuresEndIndex <= firstLine.length()) {
                // Разделяем первую строку на две части по цветам
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

    /**
     * Метод для получения тултипа "hold shift" с переносами строк
     */
    private List<Component> getHoldShiftTooltipWithLineBreaks() {
        List<Component> result = new ArrayList<>();

        // Получаем текст для обеих частей
        String featuresText = Component.translatable("tooltip.rpgworldmod.features").getString();
        String holdShiftText = Component.translatable("tooltip.rpgworldmod.hold_shift_for_features",
                Minecraft.getInstance().options.keyShift.getKey().getDisplayName()).getString();

        String fullText = featuresText + " " + holdShiftText;

        // Разбиваем текст на строки не более 25 символов
        List<String> wrappedLines = wrapText(fullText, 25);

        if (!wrappedLines.isEmpty()) {
            String firstLine = wrappedLines.get(0);

            // Первая строка может содержать обе части или только первую
            int featuresEndIndex = firstLine.indexOf(featuresText) + featuresText.length();
            if (featuresEndIndex <= firstLine.length()) {
                // Разделяем первую строку на две части по цветам
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

            // Остальные строки - серые
            for (int i = 1; i < wrappedLines.size(); i++) {
                result.add(Component.literal(wrappedLines.get(i)).withStyle(ChatFormatting.GRAY));
            }
        }

        return result;
    }

    /**
     * Универсальный метод для переноса текста на строки заданной длины
     */
    private List<String> wrapText(String text, int maxLineLength) {
        List<String> result = new ArrayList<>();

        if (text.length() <= maxLineLength) {
            result.add(text);
            return result;
        }

        // Разбиваем текст на строки не более maxLineLength символов, не разрывая слова
        int startIndex = 0;
        while (startIndex < text.length()) {
            int endIndex = Math.min(startIndex + maxLineLength, text.length());

            // Если это не конец текста и мы не на границе слова, ищем место для переноса
            if (endIndex < text.length() && !Character.isWhitespace(text.charAt(endIndex))) {
                // Ищем последний пробел в пределах maxLineLength символов
                int lastSpace = text.lastIndexOf(' ', endIndex);
                if (lastSpace > startIndex) {
                    endIndex = lastSpace;
                }
            }

            // Извлекаем подстроку и обрезаем пробелы в начале/конце
            String line = text.substring(startIndex, endIndex).trim();
            if (!line.isEmpty()) {
                result.add(line);
            }

            // Пропускаем пробелы в начале следующей строки
            startIndex = endIndex;
            while (startIndex < text.length() && Character.isWhitespace(text.charAt(startIndex))) {
                startIndex++;
            }
        }

        return result;
    }
}