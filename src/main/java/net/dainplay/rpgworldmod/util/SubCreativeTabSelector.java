package net.dainplay.rpgworldmod.util;

import net.dainplay.rpgworldmod.RPGworldMod;
import net.dainplay.rpgworldmod.item.ModCreativeModeTab;
import net.dainplay.rpgworldmod.mixin.CreativeModeInventoryScreenAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class SubCreativeTabSelector {
	private static final ResourceLocation SELECTOR_BAR = new ResourceLocation(RPGworldMod.MOD_ID, "textures/gui/creative_tabs.png");

	private static SubCreativeTabSelector instance;

	public static SubCreativeTabSelector bootstrap() {
		if (instance == null) {
			instance = new SubCreativeTabSelector();
		}
		return instance;
	}

	private int guiLeft;
	private int guiTop;

	private List<SubCreativeTabs> subtabs = null;
	private CreativeModeTab lastTab;
	private int itemCount;

	private final CreativeModeTab targetTab;

	private SubCreativeTabSelector() {
		this.targetTab = ModCreativeModeTab.RPGWORLD_EQUIPMENT_TAB.get();
	}

	public void initScreen(Screen screen, Consumer<AbstractWidget> addWidget) {
		if (screen instanceof CreativeModeInventoryScreen creativeScreen) {
			if (this.subtabs == null) this.subtabs = new ArrayList<>(ModSubCreativeTabs.getTabs());
			this.guiLeft = creativeScreen.getGuiLeft();
			this.guiTop = creativeScreen.getGuiTop();
			this.injectWidgets(creativeScreen, addWidget);
			this.itemCount = targetTab.getDisplayItems().size();
		}
	}

	public void renderBackground(AbstractContainerScreen<?> screen, GuiGraphics graphics) {
		if (screen instanceof CreativeModeInventoryScreen creativeScreen) {
			CreativeModeTab tab = ((CreativeModeInventoryScreenAccessor) creativeScreen).getSelectedTab();
			if (tab == targetTab) {
				graphics.blit(SELECTOR_BAR, this.guiLeft - 14, this.guiTop, 0, 0, 17, 136);
				if (hasSelectedBuSubTab() && creativeScreen.getMenu().items.size() == this.itemCount) {
					this.subtabs.forEach(SubCreativeTabs::deselect);
				}
			}
			if (this.lastTab != tab) {
				onSwitchCreativeTab(tab, creativeScreen);
				this.lastTab = tab;
			}
		}
	}

	public void onClose(Screen screen) {
		if (screen instanceof CreativeModeInventoryScreen) {
			this.subtabs.forEach(subtab -> {
				subtab.setContentTab(null);
				subtab.deselect();
			});
		}
	}

	private boolean hasSelectedBuSubTab() {
		return this.subtabs != null && this.subtabs.stream().anyMatch(SubCreativeTabs::isSelected);
	}

	private void injectWidgets(CreativeModeInventoryScreen screen, Consumer<AbstractWidget> addWidget) {
		this.subtabs.forEach(category -> {
			Tab tab = new Tab(this.guiLeft - 23, this.guiTop + 7, category, button -> {
				if (category.isSelected()) {
					category.deselect();
				} else {
					this.subtabs.forEach(SubCreativeTabs::deselect);
					category.select();
				}
				updateItems(screen);
			});
			tab.visible = false;
			addWidget.accept(tab);
		});

		updateWidgets();
		onSwitchCreativeTab(((CreativeModeInventoryScreenAccessor) screen).getSelectedTab(), screen);
	}

	private void updateItems(CreativeModeInventoryScreen screen) {
		NonNullList<ItemStack> items = screen.getMenu().items;
		items.clear();

		if (hasSelectedBuSubTab()) {
			this.subtabs.stream()
					.filter(SubCreativeTabs::isSelected)
					.findFirst()
					.ifPresent(subtab -> {
						for (ItemStack stack : subtab.getDisplayItems()) {
							items.add(stack.copy());
						}
					});
		} else {
			for (ItemStack stack : targetTab.getDisplayItems()) {
				items.add(stack.copy());
			}
		}

		screen.getMenu().scrollTo(0f);
	}

	private void updateWidgets() {
		boolean isOnTargetTab = this.lastTab == targetTab;
		this.subtabs.forEach(subtab -> subtab.setVisible(isOnTargetTab));

		if (isOnTargetTab) {
			int yOffset = 0;
			int index = 0;
			for (SubCreativeTabs subtab : this.subtabs) {
				subtab.setY(this.guiTop + yOffset);
				subtab.setIndex(index);
				yOffset += 15;
				index += 1;
			}
		}
	}

	private void onSwitchCreativeTab(CreativeModeTab tab, CreativeModeInventoryScreen screen) {
		this.lastTab = tab;
		updateWidgets();
		if (tab == targetTab) {
			updateItems(screen);
		}
	}

	public static class Tab extends Button {
		private final SubCreativeTabs subtab;

		protected Tab(int x, int y, SubCreativeTabs subtab, OnPress onPress) {
			super(x+9, y, 18, 16, Component.empty(), onPress, DEFAULT_NARRATION);
			this.subtab = subtab;
			subtab.setContentTab(this);
			this.setTooltip(Tooltip.create(subtab.getTooltip()));
		}

		@Override
		protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
			this.renderSelected(graphics);
		}

		private void renderSelected(GuiGraphics graphics) {
			if (this.subtab.isSelected()) {
				graphics.pose().pushPose();
				graphics.pose().translate(0, 0, 1);
				graphics.blit(SELECTOR_BAR, this.getX(), this.getY(), 17, this.subtab.getIndex()*15, 18, 16);
				graphics.pose().popPose();
			}
		}

		@Override
		protected ClientTooltipPositioner createTooltipPositioner() {
			return DefaultTooltipPositioner.INSTANCE;
		}
	}
}