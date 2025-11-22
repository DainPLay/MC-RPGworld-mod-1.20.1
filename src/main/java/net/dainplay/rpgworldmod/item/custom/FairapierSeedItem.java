package net.dainplay.rpgworldmod.item.custom;

import net.dainplay.rpgworldmod.entity.projectile.FairapierSeedEntity;
import net.dainplay.rpgworldmod.sounds.RPGSounds;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemNameBlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public class FairapierSeedItem extends ItemNameBlockItem {

    public FairapierSeedItem(Block p_41579_, Properties p_41580_) {
        super(p_41579_, p_41580_);
    }

    @Override
    public InteractionResult useOn(UseOnContext pContext) {
        InteractionResult interactionresult = this.place(new BlockPlaceContext(pContext));
        if(!pContext.getLevel().getBlockState(pContext.getClickedPos()).is(Blocks.FARMLAND)){
            InteractionResult interactionresult1 = this.use(pContext.getLevel(), pContext.getPlayer(), pContext.getHand()).getResult();
            return interactionresult1 == InteractionResult.CONSUME ? InteractionResult.CONSUME_PARTIAL : interactionresult1;
        }
        return interactionresult;
    }
    @Override
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pHand) {
        ItemStack itemstack = pPlayer.getItemInHand(pHand);
        pLevel.playSound((Player)null, pPlayer.getX(), pPlayer.getY(), pPlayer.getZ(), RPGSounds.FAIRAPIER_SEED_THROW.get(), SoundSource.NEUTRAL, 0.5F, 0.4F / (pLevel.getRandom().nextFloat() * 0.4F + 0.8F));
        pPlayer.getCooldowns().addCooldown(this, 20);
        if (!pLevel.isClientSide) {
            FairapierSeedEntity thrownseed = new FairapierSeedEntity(pLevel, pPlayer, itemstack);
            thrownseed.shootFromRotation(pPlayer, pPlayer.getXRot(), pPlayer.getYRot(), 0.0F, 2.5F, 1.0F);
            thrownseed.pickup = AbstractArrow.Pickup.DISALLOWED;
            pLevel.playSound((Player)null, thrownseed, RPGSounds.FAIRAPIER_SEED_THROW.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
            pLevel.addFreshEntity(thrownseed);
        }

        pPlayer.awardStat(Stats.ITEM_USED.get(this));
        if (!pPlayer.getAbilities().instabuild) {
            itemstack.shrink(1);
        }

        return InteractionResultHolder.sidedSuccess(itemstack, pLevel.isClientSide());
    }
}
