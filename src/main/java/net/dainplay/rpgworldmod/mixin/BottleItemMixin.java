package net.dainplay.rpgworldmod.mixin;

import net.dainplay.rpgworldmod.fluid.ModFluidTypes;
import net.dainplay.rpgworldmod.fluid.ModFluids;
import net.dainplay.rpgworldmod.potion.ModPotions;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BottleItem.class)
public abstract class BottleItemMixin {
    BottleItem bottleItem = (BottleItem) (Object) this;
    @Inject(method = "use", at = @At(value = "HEAD"), cancellable = true)
    private void useOnArborFuel(Level pLevel, Player pPlayer, InteractionHand pHand, CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir) {
        BlockHitResult blockhitresult = Item.getPlayerPOVHitResult(pLevel, pPlayer, ClipContext.Fluid.SOURCE_ONLY);
        ItemStack itemstack = pPlayer.getItemInHand(pHand);

        if (blockhitresult.getType() == HitResult.Type.MISS) {
            return;
        } else if (blockhitresult.getType() == HitResult.Type.BLOCK) {
            BlockPos blockpos = blockhitresult.getBlockPos();
            if (!pLevel.mayInteract(pPlayer, blockpos)) {
                return;
            }


            FluidState fluidState = pLevel.getFluidState(blockpos);
            if (fluidState.is(ModFluids.SOURCE_ARBOR_FUEL.get())) {
                pLevel.playSound(pPlayer, pPlayer.getX(), pPlayer.getY(), pPlayer.getZ(), SoundEvents.BOTTLE_FILL, SoundSource.NEUTRAL, 1.0F, 1.0F);
                pLevel.gameEvent(pPlayer, GameEvent.FLUID_PICKUP, blockpos);

                ItemStack filledBottle = PotionUtils.setPotion(new ItemStack(Items.POTION), ModPotions.ARBOR_FUEL_BOTTLE.get());
                pPlayer.awardStat(Stats.ITEM_USED.get(bottleItem));
                ItemStack resultStack = ItemUtils.createFilledResult(itemstack, pPlayer, filledBottle);
                cir.setReturnValue(InteractionResultHolder.sidedSuccess(resultStack,pLevel.isClientSide()));
            }
        }
    }

}
