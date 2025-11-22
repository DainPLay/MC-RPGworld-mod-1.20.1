package net.dainplay.rpgworldmod.item.custom;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemNameBlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

import java.util.Properties;

public class ExtinguishingItem extends Item {
    protected final RandomSource random = RandomSource.create();
    public ExtinguishingItem(Properties p_41580_) {
        super(p_41580_);
    }

@Override
    public ItemStack finishUsingItem(ItemStack pStack, Level pLevel, LivingEntity pEntityLiving) {
    if (pEntityLiving.isOnFire()) {
        pEntityLiving.clearFire();
        pEntityLiving.playSound(SoundEvents.GENERIC_EXTINGUISH_FIRE, 0.7F, 1.6F + (this.random.nextFloat() - this.random.nextFloat()) * 0.4F);
    }
        if (pEntityLiving instanceof Player && !((Player)pEntityLiving).getAbilities().instabuild) {
            pEntityLiving.eat(pLevel, pStack);
        }

        return pStack;
    }
}
