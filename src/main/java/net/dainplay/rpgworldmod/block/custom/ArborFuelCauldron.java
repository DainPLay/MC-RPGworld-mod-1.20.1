package net.dainplay.rpgworldmod.block.custom;

import net.dainplay.rpgworldmod.block.ModBlocks;
import net.dainplay.rpgworldmod.effect.ModEffects;
import net.dainplay.rpgworldmod.item.ModItems;
import net.dainplay.rpgworldmod.potion.ModPotions;
import net.minecraft.core.BlockPos;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.AbstractCauldronBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.function.Predicate;

public class ArborFuelCauldron extends LayeredCauldronBlock {

    public static final Predicate<Biome.Precipitation> NONE = (p_153526_) -> false;
    public ArborFuelCauldron(Properties pProperties, Map<Item, CauldronInteraction> pInteractions) {
        super(pProperties, NONE, pInteractions);
    }
    @Override
    public ItemStack getCloneItemStack(BlockGetter pLevel, BlockPos pPos, BlockState pState) {
        return new ItemStack(Items.CAULDRON);
    }

    protected double getContentHeight(BlockState p_153500_) {
        return 0.9375D;
    }

    @Override
    protected void handleEntityOnFireInside(BlockState p_154294_, Level p_154295_, BlockPos p_154296_) {
        lowerFillLevel(Blocks.WATER_CAULDRON.defaultBlockState().setValue(LEVEL, p_154294_.getValue(LEVEL)), p_154295_, p_154296_);
    }

    public void entityInside(BlockState p_153506_, Level level, BlockPos p_153508_, Entity entity) {
        if (this.isEntityInsideContent(p_153506_, p_153508_, entity)) {
            if(entity instanceof LivingEntity livingEntity && !level.isClientSide) {
                int duration = 0;
                MobEffectInstance effect = livingEntity.getEffect(ModEffects.FUELING.get());

                if(effect != null){
                    duration = effect.getDuration();
                }

                if (duration >=280 && duration < 2400) {
                    livingEntity.addEffect(new MobEffectInstance(ModEffects.FUELING.get(), duration + 3, 0));
                } else {
                    livingEntity.addEffect(new MobEffectInstance(ModEffects.FUELING.get(), 280, 0));
                }
            }
        }

    }

}