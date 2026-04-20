package net.dainplay.rpgworldmod.item.custom;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemNameBlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Vanishable;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

import javax.annotation.Nullable;
import java.util.List;

public class FairapierSwordWiltedItem extends ItemNameBlockItem implements Vanishable, RPGtooltip {
	public FairapierSwordWiltedItem(Block p_41579_, Properties p_41580_) {
		super(p_41579_, p_41580_);
	}

	@Override
	public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltip, TooltipFlag pFlag) {
		pTooltip.add(this.getDisplayName(pStack).withStyle(ChatFormatting.DARK_GRAY));
	}
}
