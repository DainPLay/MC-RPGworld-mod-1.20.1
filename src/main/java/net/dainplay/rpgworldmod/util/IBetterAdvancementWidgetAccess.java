package net.dainplay.rpgworldmod.util;

import betteradvancements.common.gui.BetterAdvancementWidget;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;

import java.util.List;

public interface IBetterAdvancementWidgetAccess {
    BetterAdvancementWidget mp_getParent();
    List<BetterAdvancementWidget> mp_getChildren();
    Advancement mp_getAdvancement();
    void mp_setY(int y);
    void mp_setX(int x);
    boolean mp_isVisible();
    AdvancementProgress mp_getProgress();
}