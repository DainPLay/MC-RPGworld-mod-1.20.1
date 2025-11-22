package net.dainplay.rpgworldmod.util;
import net.dainplay.rpgworldmod.block.ModBlocks;
import net.dainplay.rpgworldmod.fluid.ModFluids;
import net.dainplay.rpgworldmod.item.ModItems;
import net.dainplay.rpgworldmod.item.custom.FuelBucketItem;
import net.dainplay.rpgworldmod.potion.ModPotions;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractCauldronBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CauldronBlock;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

public class CauldronInteractionHandler {
    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Level level = event.getLevel();
        BlockPos pos = event.getPos();
        Player player = event.getEntity();
        InteractionHand hand = event.getHand();
        ItemStack itemStack = player.getItemInHand(hand);
        BlockState state = level.getBlockState(pos);

        if (state.getBlock() instanceof AbstractCauldronBlock) {
            // Handle Arbor Fuel Bucket Interaction
            if (itemStack.is(ModItems.ARBOR_FUEL_BUCKET.get())) {
                if (!player.isCreative() && !level.isClientSide) {
                    player.setItemInHand(hand, new ItemStack(Items.BUCKET));
                }
                level.setBlock(pos, ModBlocks.ARBOR_FUEL_CAULDRON.get().defaultBlockState().setValue(BlockStateProperties.LEVEL_CAULDRON, 3), 3);
                level.playSound((Player) null, pos, SoundEvents.BUCKET_EMPTY_LAVA, SoundSource.BLOCKS, 1.0F, 1.0F);
                player.awardStat(Stats.FILL_CAULDRON);
                event.setCanceled(true); // cancel the original behavior of the cauldron.
                event.setCancellationResult(InteractionResult.SUCCESS); // cancel the original behavior of the cauldron and indicate the interaction was successful.
            }
        }
        if(state.is(ModBlocks.ARBOR_FUEL_CAULDRON.get()) )
        {
            if (itemStack.is(Items.BUCKET)) {
                if (state.getValue(BlockStateProperties.LEVEL_CAULDRON) == 3) {
                    if (!player.isCreative() && !level.isClientSide) {
                            itemStack.shrink(1);
                            if (itemStack.isEmpty())
                                player.setItemInHand(hand, new ItemStack(ModItems.ARBOR_FUEL_BUCKET.get()));
                            else if(player.getInventory().getFreeSlot() == -1) player.drop(new ItemStack(ModItems.ARBOR_FUEL_BUCKET.get()), false);
                            else player.getInventory().add(new ItemStack(ModItems.ARBOR_FUEL_BUCKET.get()));
                    }
                    level.setBlock(pos, Blocks.CAULDRON.defaultBlockState(), 3);
                    level.playSound((Player) null, pos, SoundEvents.BUCKET_FILL_LAVA, SoundSource.BLOCKS, 1.0F, 1.0F);
                    player.awardStat(Stats.USE_CAULDRON);
                    player.awardStat(Stats.ITEM_USED.get(itemStack.getItem()));
                    event.setCanceled(true);
                    event.setCancellationResult(InteractionResult.SUCCESS);
                }
            }

            else if (itemStack.is(Items.GLASS_BOTTLE)) {
                if (state.getValue(BlockStateProperties.LEVEL_CAULDRON) == 1) {
                    if (!player.isCreative() && !level.isClientSide) {
                            itemStack.shrink(1);
                            if (itemStack.isEmpty())
                                player.setItemInHand(hand, ItemUtils.createFilledResult(itemStack, player, PotionUtils.setPotion(new ItemStack(Items.POTION), ModPotions.ARBOR_FUEL_BOTTLE.get())));
                            else if(player.getInventory().getFreeSlot() == -1) player.drop(PotionUtils.setPotion(new ItemStack(Items.POTION), ModPotions.ARBOR_FUEL_BOTTLE.get()), false);
                            else player.getInventory().add(PotionUtils.setPotion(new ItemStack(Items.POTION), ModPotions.ARBOR_FUEL_BOTTLE.get()));

                    }
                    level.setBlock(pos, Blocks.CAULDRON.defaultBlockState(), 3);
                    level.playSound((Player) null, pos, SoundEvents.BOTTLE_FILL, SoundSource.BLOCKS, 1.0F, 1.0F);
                    player.awardStat(Stats.USE_CAULDRON);
                    player.awardStat(Stats.ITEM_USED.get(itemStack.getItem()));
                    event.setCanceled(true);
                    event.setCancellationResult(InteractionResult.SUCCESS);
                }
                else {
                    if (!player.isCreative() && !level.isClientSide) {
                            itemStack.shrink(1);
                            if (itemStack.isEmpty())
                                player.setItemInHand(hand, PotionUtils.setPotion(new ItemStack(Items.POTION), ModPotions.ARBOR_FUEL_BOTTLE.get()));
                            else if(player.getInventory().getFreeSlot() == -1) player.drop(PotionUtils.setPotion(new ItemStack(Items.POTION), ModPotions.ARBOR_FUEL_BOTTLE.get()), false);
                            else player.getInventory().add(PotionUtils.setPotion(new ItemStack(Items.POTION), ModPotions.ARBOR_FUEL_BOTTLE.get()));

                    }
                    level.setBlock(pos, ModBlocks.ARBOR_FUEL_CAULDRON.get().defaultBlockState().setValue(BlockStateProperties.LEVEL_CAULDRON, state.getValue(BlockStateProperties.LEVEL_CAULDRON) - 1), 3);
                    level.playSound((Player) null, pos, SoundEvents.BOTTLE_FILL, SoundSource.BLOCKS, 1.0F, 1.0F);
                    player.awardStat(Stats.USE_CAULDRON);
                    player.awardStat(Stats.ITEM_USED.get(itemStack.getItem()));
                    event.setCanceled(true);
                    event.setCancellationResult(InteractionResult.SUCCESS);
                }
            }
        }
        if (itemStack.is(Items.POTION) && PotionUtils.getPotion(itemStack) == ModPotions.ARBOR_FUEL_BOTTLE.get()) {
            if (state.is(Blocks.CAULDRON)) {
                if (!player.isCreative() && !level.isClientSide) {
                    player.setItemInHand(hand, new ItemStack(Items.GLASS_BOTTLE));
                }
                level.setBlock(pos, ModBlocks.ARBOR_FUEL_CAULDRON.get().defaultBlockState(), 3);
                level.playSound((Player) null, pos, SoundEvents.BOTTLE_EMPTY, SoundSource.BLOCKS, 1.0F, 1.0F);
                player.awardStat(Stats.FILL_CAULDRON);
                event.setCanceled(true);
                event.setCancellationResult(InteractionResult.SUCCESS);
            }
            else if(state.is(ModBlocks.ARBOR_FUEL_CAULDRON.get()) && state.getValue(BlockStateProperties.LEVEL_CAULDRON) != 3)
            {
                if (!player.isCreative() && !level.isClientSide) {
                    player.setItemInHand(hand, new ItemStack(Items.GLASS_BOTTLE));
                }
                level.setBlock(pos, ModBlocks.ARBOR_FUEL_CAULDRON.get().defaultBlockState().setValue(BlockStateProperties.LEVEL_CAULDRON, state.getValue(BlockStateProperties.LEVEL_CAULDRON) + 1), 3);
                level.playSound((Player) null, pos, SoundEvents.BOTTLE_EMPTY, SoundSource.BLOCKS, 1.0F, 1.0F);
                player.awardStat(Stats.FILL_CAULDRON);
                event.setCanceled(true);
                event.setCancellationResult(InteractionResult.SUCCESS);

            }
        }
    }
}
