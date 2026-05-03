package net.dainplay.rpgworldmod.item.custom;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;

public class FlintDaggerItem extends DaggerItem implements RPGtooltip, IgniteOnCritItem {
	public FlintDaggerItem(Tier pTier, int pAttackDamageModifier, float pAttackSpeedModifier, Properties pProperties) {
		super(pTier, pAttackDamageModifier, pAttackSpeedModifier, pProperties);
	}

	@Override
	public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
		return enchantment != Enchantments.FIRE_ASPECT && (super.canApplyAtEnchantingTable(stack, enchantment) || enchantment == Enchantments.BLOCK_FORTUNE);
	}

	@Override
	public boolean isBookEnchantable(ItemStack stack, ItemStack book) {
		return !EnchantmentHelper.getEnchantments(book).containsKey(Enchantments.FIRE_ASPECT) && (super.isBookEnchantable(stack, book) ||
				EnchantmentHelper.getEnchantments(book).containsKey(Enchantments.BLOCK_FORTUNE));
	}

	@Override
	public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
		boolean result = super.hurtEnemy(stack, target, attacker);

		if (!target.level().isClientSide()) {
			int fortuneLevel = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.BLOCK_FORTUNE, stack);

			float baseChance = 0.10f;
			float chancePerLevel = 0.3f;

			float totalChance = baseChance + (fortuneLevel * chancePerLevel);

			if (attacker.isFallFlying()) totalChance *= 2;

			totalChance = Math.min(totalChance, 1f);

			float chance = target.level().getRandom().nextFloat();

			if (chance < totalChance && !target.fireImmune()) {
				int fireDuration = 5 + (fortuneLevel * 5);
				target.setSecondsOnFire(fireDuration);
			} else {
				((ServerLevel) target.level()).sendParticles(ParticleTypes.FLAME, target.getX(), target.getY() + target.getBbHeight() * 0.5f, target.getZ(), 20, target.level().getRandom().nextFloat() / 5, target.level().getRandom().nextFloat() / 5, target.level().getRandom().nextFloat() / 5, 0.01f);
			}
		}

		return result;
	}
}