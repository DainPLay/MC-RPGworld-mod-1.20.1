package net.dainplay.rpgworldmod.mixin;

import net.dainplay.rpgworldmod.item.custom.LivingWoodArmorItem;
import net.dainplay.rpgworldmod.item.custom.LivingWoodBowItem;
import net.dainplay.rpgworldmod.item.custom.LivingWoodStaffItem;
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
	private static List<EnchantmentInstance> addMendingForIronSwords(
			List<EnchantmentInstance> list,
			int pLevel,
			ItemStack pStack,
			boolean pAllowTreasure) {
		if (pStack.getItem() instanceof LivingWoodArmorItem || pStack.getItem() instanceof LivingWoodBowItem || pStack.getItem() instanceof LivingWoodStaffItem) {
			Enchantment mending = Enchantments.MENDING;


			if (isCompatibleWithList(mending, list)) {
				boolean alreadyAdded = list.stream()
						.anyMatch(instance -> instance.enchantment == mending);

				if (!alreadyAdded) {
					for (int level = mending.getMaxLevel(); level >= mending.getMinLevel(); level--) {
						if (pLevel >= mending.getMinCost(level) &&
								pLevel <= mending.getMaxCost(level)) {
							list.add(new EnchantmentInstance(mending, level));
							break;
						}
					}
				}
			}
		}

		return list;
	}

	private static boolean isCompatibleWithList(Enchantment enchantment, List<EnchantmentInstance> list) {
		for (EnchantmentInstance instance : list) {
			if (!enchantment.isCompatibleWith(instance.enchantment)) {
				return false;
			}
		}
		return true;
	}
}