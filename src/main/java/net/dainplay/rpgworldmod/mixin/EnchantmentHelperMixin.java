package net.dainplay.rpgworldmod.mixin;

import net.dainplay.rpgworldmod.enchantment.ModEnchantments;
import net.dainplay.rpgworldmod.item.custom.EmptyScrollItem;
import net.dainplay.rpgworldmod.item.custom.LivingWoodArmorItem;
import net.dainplay.rpgworldmod.item.custom.LivingWoodBowItem;
import net.dainplay.rpgworldmod.item.custom.LivingWoodStaffItem;
import net.dainplay.rpgworldmod.item.custom.ScrollItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.item.enchantment.Enchantments;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.List;

@Mixin(EnchantmentHelper.class)
public abstract class EnchantmentHelperMixin {

	@ModifyVariable(
			method = "getAvailableEnchantmentResults",
			at = @At("RETURN"),
			ordinal = 0
	)
	private static List<EnchantmentInstance> addEnchants(
			List<EnchantmentInstance> list,
			int pLevel,
			ItemStack pStack,
			boolean pAllowTreasure) {

		if (pStack.getItem() instanceof LivingWoodArmorItem
				|| pStack.getItem() instanceof LivingWoodBowItem
				|| pStack.getItem() instanceof LivingWoodStaffItem) {
			tryAddEnchantment(list, Enchantments.MENDING, pLevel);
		}

		if (pStack.getItem() instanceof EmptyScrollItem
				|| pStack.getItem() instanceof ScrollItem) {
			tryAddEnchantment(list, ModEnchantments.RESTORATION.get(), pLevel);
			tryAddEnchantment(list, ModEnchantments.DESTRUCTION.get(), pLevel);
			tryAddEnchantment(list, ModEnchantments.ILLUSION.get(),    pLevel);
			tryAddEnchantment(list, ModEnchantments.ALTERATION.get(),  pLevel);
			tryAddEnchantment(list, ModEnchantments.CONJURATION.get(), pLevel);
			tryAddEnchantment(list, ModEnchantments.NECROMANCY.get(),  pLevel);
		}

		return list;
	}

	private static void tryAddEnchantment(List<EnchantmentInstance> list,
										  Enchantment enchantment,
										  int pLevel) {
		if (!isCompatibleWithList(enchantment, list)) return;

		boolean alreadyAdded = list.stream()
				.anyMatch(instance -> instance.enchantment == enchantment);
		if (alreadyAdded) return;

		for (int level = enchantment.getMaxLevel(); level >= enchantment.getMinLevel(); level--) {
			if (pLevel >= enchantment.getMinCost(level)
					&& pLevel <= enchantment.getMaxCost(level)) {
				list.add(new EnchantmentInstance(enchantment, level));
				break;
			}
		}
	}

	private static boolean isCompatibleWithList(Enchantment enchantment,
												List<EnchantmentInstance> list) {
		for (EnchantmentInstance instance : list) {
			if (isScrollEnchantment(enchantment) && isScrollEnchantment(instance.enchantment)) {
				continue;
			}
			if (!enchantment.isCompatibleWith(instance.enchantment)) {
				return false;
			}
		}
		return true;
	}

	private static boolean isScrollEnchantment(Enchantment enchantment) {
		return enchantment == ModEnchantments.RESTORATION.get()
				|| enchantment == ModEnchantments.DESTRUCTION.get()
				|| enchantment == ModEnchantments.ILLUSION.get()
				|| enchantment == ModEnchantments.ALTERATION.get()
				|| enchantment == ModEnchantments.CONJURATION.get()
				|| enchantment == ModEnchantments.NECROMANCY.get();
	}
}