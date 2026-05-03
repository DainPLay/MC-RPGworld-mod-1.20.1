package net.dainplay.rpgworldmod.util;

import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.BiConsumer;

public class SubCreativeTabs {
	private final Component tooltip;
	private final List<ItemStack> displayItems;
	private final @Nullable BiConsumer<HolderLookup.Provider, Output> populationLogic;
	private boolean populated;
	private @Nullable SubCreativeTabSelector.Tab tab;
	private boolean selected;
	private int index;

	private SubCreativeTabs(
			Component tooltip,
			List<ItemStack> staticItems,
			@Nullable BiConsumer<HolderLookup.Provider, Output> populationLogic
	) {
		this.tooltip = tooltip;
		this.displayItems = staticItems;
		this.populationLogic = populationLogic;
	}

	public static Builder builder() {
		return new Builder();
	}

	public Component getTooltip() {
		return this.tooltip;
	}

	public Collection<ItemStack> getDisplayItems() {
		return this.displayItems;
	}

	public boolean contains(ItemStack stack) {
		return this.displayItems.contains(stack);
	}

	public void select() {
		this.selected = true;
	}

	public void deselect() {
		this.selected = false;
	}

	public boolean isSelected() {
		return this.selected;
	}

	public void setContentTab(@Nullable SubCreativeTabSelector.Tab tab) {
		this.tab = tab;
	}

	public void setVisible(boolean visible) {
		if (this.tab != null) this.tab.visible = visible;
	}

	public void setY(int y) {
		if (this.tab != null) this.tab.setY(y);
	}

	public void setIndex(int index) {
		if (this.tab != null) this.index = index;
	}

	public int getIndex() {
		return this.index;
	}

	public void populate(@Nullable HolderLookup.Provider provider) {
		if (this.populated || this.populationLogic == null) return;
		this.populationLogic.accept(provider, new Output() {
			@Override
			public void accept(ItemLike item) {
				displayItems.add(new ItemStack(item));
			}

			@Override
			public void accept(ItemStack stack) {
				displayItems.add(stack);
			}
		});
		this.populated = true;
	}

	public static class Builder {
		private Component title;
		private BiConsumer<HolderLookup.Provider, Output> populationLogic;

		public Builder title(Component title) {
			this.title = title;
			return this;
		}

		public Builder displayItems(BiConsumer<HolderLookup.Provider, Output> logic) {
			this.populationLogic = logic;
			return this;
		}

		public SubCreativeTabs build() {
			if (this.title == null) this.title = Component.empty();
			return new SubCreativeTabs(this.title, new ArrayList<>(), this.populationLogic);
		}
	}

	public interface Output {
		void accept(ItemLike item);

		void accept(ItemStack stack);
	}
}