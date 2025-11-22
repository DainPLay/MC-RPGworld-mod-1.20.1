package net.dainplay.rpgworldmod.item.custom;

import net.dainplay.rpgworldmod.item.ModItems;
import net.dainplay.rpgworldmod.sounds.RPGSounds;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.List;

public class FairapierSwordItem extends SwordItem {
    public FairapierSwordItem(Tier pTier, int pAttackDamageModifier, float pAttackSpeedModifier, Properties pProperties) {
        super(pTier, pAttackDamageModifier, pAttackSpeedModifier, pProperties);
    }
    @Override
    public boolean hurtEnemy(ItemStack pStack, LivingEntity pTarget, LivingEntity pAttacker) {
        if(pStack.getDamageValue() < pStack.getMaxDamage() - 1) {
            pStack.hurtAndBreak(1, pAttacker, (p_43296_) -> {
                p_43296_.broadcastBreakEvent(EquipmentSlot.MAINHAND);
            });
        }
        else if (pAttacker instanceof Player && !((Player)pAttacker).getAbilities().instabuild){
            ItemStack wilted_item = ModItems.FAIRAPIER_SWORD_WILTED.get().getDefaultInstance();
            wilted_item.setTag(pStack.getTag());
            pAttacker.level().playSound((Player)null, pAttacker.getX(), pAttacker.getY(), pAttacker.getZ(), RPGSounds.FAIRAPIER_WILTS.get(), SoundSource.NEUTRAL, 0.5F, 0.4F / (pAttacker.getRandom().nextFloat() * 0.4F + 0.8F));
            pAttacker.setItemSlot(EquipmentSlot.MAINHAND, wilted_item);
        }
            return true;
    }
    @Override
    public boolean mineBlock(ItemStack pStack, Level pLevel, BlockState pState, BlockPos pPos, LivingEntity pEntityLiving) {
        if(pStack.getDamageValue() < pStack.getMaxDamage() - 2) {
        if (pState.getDestroySpeed(pLevel, pPos) != 0.0F) {
            pStack.hurtAndBreak(2, pEntityLiving, (p_43276_) -> {
                p_43276_.broadcastBreakEvent(EquipmentSlot.MAINHAND);
            });
        }
        }
        else  if (pEntityLiving instanceof Player && !((Player)pEntityLiving).getAbilities().instabuild){
            ItemStack wilted_item = ModItems.FAIRAPIER_SWORD_WILTED.get().getDefaultInstance();
            wilted_item.setTag(pStack.getTag());

            pLevel.playSound((Player)null, pEntityLiving.getX(), pEntityLiving.getY(), pEntityLiving.getZ(), RPGSounds.FAIRAPIER_WILTS.get(), SoundSource.NEUTRAL, 0.5F, 0.4F / (pEntityLiving.getRandom().nextFloat() * 0.4F + 0.8F));
            pEntityLiving.setItemSlot(EquipmentSlot.MAINHAND, wilted_item);

        }
        return true;
    }
    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltip, TooltipFlag pFlag) {
        pTooltip.add(this.getDisplayName().withStyle(ChatFormatting.ITALIC).withStyle(ChatFormatting.RED));
    }
    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
        return enchantment != Enchantments.MENDING && enchantment != Enchantments.SWEEPING_EDGE && super.canApplyAtEnchantingTable(stack, enchantment);
    }
    @Override
    public boolean isBookEnchantable(ItemStack stack, ItemStack book) {
        return !EnchantmentHelper.getEnchantments(book).containsKey(Enchantments.MENDING) &&
                !EnchantmentHelper.getEnchantments(book).containsKey(Enchantments.SWEEPING_EDGE) &&super.isBookEnchantable(stack, book);
    }

    @Override
    public boolean canPerformAction(ItemStack stack, net.minecraftforge.common.ToolAction toolAction) {
        return net.minecraftforge.common.ToolActions.SWORD_DIG == toolAction;
    }

    public MutableComponent getDisplayName() {
        return Component.translatable(this.getDescriptionId() + ".desc");
    }
}
