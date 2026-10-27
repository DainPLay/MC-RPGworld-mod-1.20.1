package net.dainplay.rpgworldmod.util;

import net.minecraft.advancements.Advancement;
import net.minecraft.client.gui.screens.advancements.AdvancementWidget;

import java.util.List;

public interface IAdvancementWidgetAccess {
    AdvancementWidget mp_getParent();
    List<AdvancementWidget> mp_getChildren();
    Advancement mp_getAdvancement();
    void mp_setY(int y);
    void mp_setX(int x);
    boolean mp_isVisible();
}