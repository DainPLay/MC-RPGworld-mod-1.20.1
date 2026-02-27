package net.dainplay.rpgworldmod.item.custom;

import net.dainplay.rpgworldmod.data.tags.ModAdvancements;
import net.dainplay.rpgworldmod.enchantment.ModEnchantments;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

public class ScrollItem extends Item implements RPGtooltip, ManaCostItem, OrbitingItem, ChooseTargetItem {
	public ScrollItem(Properties pProperties) {
		super(pProperties);
	}

	@Override
	public boolean isFoil(ItemStack stack) {
		if (stack.getEnchantmentLevel(ModEnchantments.ALTERATION.get()) > 0
				|| stack.getEnchantmentLevel(ModEnchantments.RESTORATION.get()) > 0
				|| stack.getEnchantmentLevel(ModEnchantments.DESTRUCTION.get()) > 0
				|| stack.getEnchantmentLevel(ModEnchantments.ILLUSION.get()) > 0
				|| stack.getEnchantmentLevel(ModEnchantments.CONJURATION.get()) > 0
				|| stack.getEnchantmentLevel(ModEnchantments.NECROMANCY.get()) > 0)
			return false;
		else return stack.isEnchanted();
	}

	@Override
	public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltip, TooltipFlag pFlag) {
		if (pStack.getEnchantmentLevel(ModEnchantments.ALTERATION.get()) > 0
				|| pStack.getEnchantmentLevel(ModEnchantments.RESTORATION.get()) > 0
				|| pStack.getEnchantmentLevel(ModEnchantments.DESTRUCTION.get()) > 0
				|| pStack.getEnchantmentLevel(ModEnchantments.ILLUSION.get()) > 0
				|| pStack.getEnchantmentLevel(ModEnchantments.CONJURATION.get()) > 0
				|| pStack.getEnchantmentLevel(ModEnchantments.NECROMANCY.get()) > 0)
			DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
					ClientRPGtooltipHandler.appendHoverText(pStack, pLevel, pTooltip, pFlag, this)
			);
		else {
			pTooltip.add(this.getDisplayName(pStack).withStyle(ChatFormatting.WHITE));
		}
	}


	@Override
	public boolean shouldOrbit(ItemStack stack, Entity entity) {
		return (stack.getEnchantmentLevel(ModEnchantments.NECROMANCY.get()) > 0
				|| stack.getEnchantmentLevel(ModEnchantments.RESTORATION.get()) > 0
				|| stack.getEnchantmentLevel(ModEnchantments.ILLUSION.get()) > 0
				|| stack.getEnchantmentLevel(ModEnchantments.DESTRUCTION.get()) > 0
				|| stack.getEnchantmentLevel(ModEnchantments.CONJURATION.get()) > 0
				|| stack.getEnchantmentLevel(ModEnchantments.ALTERATION.get()) > 0);
	}

	public boolean isEnchantable(ItemStack pStack) {
		return pStack.getCount() == 1;
	}

	public int getEnchantmentValue() {
		return 1;
	}

	@Override
	public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
		super.inventoryTick(stack, level, entity, slotId, isSelected);

		if (!level.isClientSide && entity instanceof Player player) {
			if (isSelected || player.getOffhandItem() == stack) {
				updateManaTag(stack, player);
			}
			if ((entity instanceof ServerPlayer serverPlayer)
					&& ((EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.DESTRUCTION.get(), stack) > 0)
					|| (EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.RESTORATION.get(), stack) > 0)
					|| (EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.ALTERATION.get(), stack) > 0)
					|| (EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.ILLUSION.get(), stack) > 0)
					|| (EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.CONJURATION.get(), stack) > 0)
					|| (EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.NECROMANCY.get(), stack) > 0))) {
				ModAdvancements.OBTAIN_SPELL_TRIGGER.trigger(serverPlayer);
			}
		}
	}

	@Override
	public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
		if (ItemStack.isSameItem(oldStack, newStack)) {
			return !(EnchantmentHelper.getEnchantments(oldStack).equals(EnchantmentHelper.getEnchantments(newStack)));
		} else return true;
	}

	@Override
	public @NotNull String getDescriptionId(ItemStack pStack) {
		String addition = "";
		if (pStack.getEnchantmentLevel(ModEnchantments.ALTERATION.get()) > 0) addition = ".alteration";
		if (pStack.getEnchantmentLevel(ModEnchantments.RESTORATION.get()) > 0) addition = ".restoration";
		if (pStack.getEnchantmentLevel(ModEnchantments.DESTRUCTION.get()) > 0) addition = ".destruction";
		if (pStack.getEnchantmentLevel(ModEnchantments.ILLUSION.get()) > 0) addition = ".illusion";
		if (pStack.getEnchantmentLevel(ModEnchantments.CONJURATION.get()) > 0) addition = ".conjuration";
		if (pStack.getEnchantmentLevel(ModEnchantments.NECROMANCY.get()) > 0) addition = ".necromancy";
		return this.getDescriptionId() + addition;
	}
}
