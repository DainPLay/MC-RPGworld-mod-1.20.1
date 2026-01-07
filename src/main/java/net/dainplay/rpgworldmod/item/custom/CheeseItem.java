package net.dainplay.rpgworldmod.item.custom;

import net.dainplay.rpgworldmod.data.tags.ModAdvancements;
import net.dainplay.rpgworldmod.effect.ModEffects;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

public class CheeseItem extends ItemNameBlockItem {
	public CheeseItem(Block p_41579_, Properties p_41580_) {
		super(p_41579_, p_41580_);
	}


	@Override
	public ItemStack finishUsingItem(ItemStack pStack, Level pLevel, LivingEntity pEntityLiving) {
		if (!pLevel.isClientSide && pEntityLiving instanceof ServerPlayer player) {
			if (player.hasEffect(MobEffects.POISON) && player.hasEffect(ModEffects.PARALYSIS.get()) && player.hasEffect(ModEffects.MOSSIOSIS.get()) && player.hasEffect(ModEffects.FUELING.get()))
				ModAdvancements.CLEAR_EFFECTS_WITH_CHEESE.trigger(player);
		}
		pEntityLiving.curePotionEffects(Items.MILK_BUCKET.getDefaultInstance());

		if (pEntityLiving instanceof Player && !((Player) pEntityLiving).getAbilities().instabuild) {
			pEntityLiving.eat(pLevel, pStack);
		}

		return pStack;
	}
}
