package net.dainplay.rpgworldmod.block;

import net.dainplay.rpgworldmod.item.ModItems;
import net.dainplay.rpgworldmod.potion.ModPotions;
import net.minecraft.core.BlockPos;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class ModCauldronInteraction implements CauldronInteraction {
    static Map<Item, CauldronInteraction> ARBOR_FUEL = CauldronInteraction.newInteractionMap();
    static {
        ARBOR_FUEL.put(Items.LAVA_BUCKET, CauldronInteraction.FILL_LAVA);
        ARBOR_FUEL.put(Items.WATER_BUCKET, CauldronInteraction.FILL_WATER);
        ARBOR_FUEL.put(Items.POWDER_SNOW_BUCKET, CauldronInteraction.FILL_POWDER_SNOW);
    }

    @Override
    public InteractionResult interact(BlockState pBlockState, Level pLevel, BlockPos pBlockPos, Player pPlayer, InteractionHand pHand, ItemStack pStack) {
        return null;
    }
}