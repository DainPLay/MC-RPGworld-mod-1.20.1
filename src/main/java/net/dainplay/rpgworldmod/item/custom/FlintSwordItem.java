package net.dainplay.rpgworldmod.item.custom;

import com.mojang.blaze3d.platform.InputConstants;
import net.dainplay.rpgworldmod.enchantment.ModEnchantments;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffects;
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

import javax.annotation.Nullable;
import java.util.List;

public class FlintSwordItem extends SwordItem implements RPGtooltip {
	public FlintSwordItem(Tier pTier, int pAttackDamageModifier, float pAttackSpeedModifier, Properties pProperties) {
		super(pTier, pAttackDamageModifier, pAttackSpeedModifier, pProperties);
	}

	@Override
	public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
		return enchantment != Enchantments.FIRE_ASPECT && (super.canApplyAtEnchantingTable(stack, enchantment) || enchantment == Enchantments.BLOCK_FORTUNE);
	}

	@Override
	public boolean isBookEnchantable(ItemStack stack, ItemStack book) {
		return !EnchantmentHelper.getEnchantments(book).containsKey(Enchantments.FIRE_ASPECT) && (super.isBookEnchantable(stack, book) ||
				EnchantmentHelper.getEnchantments(book).containsKey(Enchantments.BLOCK_EFFICIENCY));
	}

	@Override
	public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
		boolean result = super.hurtEnemy(stack, target, attacker);

		if (result && !target.level().isClientSide()) {
			int fortuneLevel = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.BLOCK_FORTUNE, stack);

			float baseChance = 0.10f;
			float chancePerLevel = 0.3f;

			float totalChance = baseChance + (fortuneLevel * chancePerLevel);

			if (attacker.isFallFlying()) totalChance *= 2;

			totalChance = Math.min(totalChance, 1f);

			if (attacker instanceof Player player) {
				boolean flag2 = player.fallDistance > 0.0F && !player.onGround() && !player.onClimbable() && !player.isInWater() && !player.hasEffect(MobEffects.BLINDNESS) && !player.isPassenger() && !player.isSprinting();
				if (flag2) totalChance *= 5;
			}

			float chance = target.level().getRandom().nextFloat();

			if (chance < totalChance && !target.fireImmune()) {
				int fireDuration = 5 + (fortuneLevel * 5);
				target.setSecondsOnFire(fireDuration);
			} else {
				((ServerLevel) target.level()).sendParticles(ParticleTypes.FLAME, target.getX(), target.getY()+target.getBbHeight()*0.5f, target.getZ(), 20, target.level().getRandom().nextFloat()/5, target.level().getRandom().nextFloat()/5, target.level().getRandom().nextFloat()/5, 0.01f);
			}
		}

		return result;
	}

	@Override
	public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltip, TooltipFlag pFlag) {
		RPGappendHoverText(pStack,pLevel,pTooltip,pFlag);
	}
}