package net.dainplay.rpgworldmod.mixin;

import net.dainplay.rpgworldmod.item.custom.LivingWoodArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
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
        
        // Проверяем, что предмет - железный меч
        if (pStack.getItem() instanceof LivingWoodArmorItem) {
            Enchantment mending = Enchantments.MENDING;
            
            // Проверяем, совместимо ли Mending с другими зачарованиями в списке
            if (isCompatibleWithList(mending, list)) {
                // Проверяем, не добавлено ли уже Mending
                boolean alreadyAdded = list.stream()
                    .anyMatch(instance -> instance.enchantment == mending);
                
                if (!alreadyAdded) {
                    // Проверяем, доступен ли уровень для этого зачарования
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