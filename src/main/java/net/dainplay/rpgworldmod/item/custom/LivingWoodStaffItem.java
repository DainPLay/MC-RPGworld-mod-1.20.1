package net.dainplay.rpgworldmod.item.custom;

import net.dainplay.rpgworldmod.effect.ModEffects;
import net.dainplay.rpgworldmod.enchantment.ModEnchantments;
import net.dainplay.rpgworldmod.network.LoopSoundPacket;
import net.dainplay.rpgworldmod.network.ModMessages;
import net.dainplay.rpgworldmod.sounds.RPGSounds;
import net.dainplay.rpgworldmod.util.ModTags;
import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;

import javax.annotation.Nullable;
import java.util.Map;

public class LivingWoodStaffItem extends StaffItem implements ChooseTargetItem {
	public LivingWoodStaffItem(Properties properties) {
		super(properties);
	}

	@Override
	public String getFirstPredicate(ItemStack item) {
		return Minecraft.getInstance().options.keyShift.getKey().getDisplayName().getString();
	}

	public String getSecondPredicate(ItemStack item) {
		return Minecraft.getInstance().options.keyAttack.getKey().getDisplayName().getString();
	}

	@Override
	public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
		return enchantment == Enchantments.MENDING || super.canApplyAtEnchantingTable(stack, enchantment);
	}

	@Override
	public void onUseTick(Level pLevel, LivingEntity pLivingEntity, ItemStack pStack, int pRemainingUseDuration) {
		super.onUseTick(pLevel, pLivingEntity, pStack, pRemainingUseDuration);

		int activeRechargeLevel = pStack.getEnchantmentLevel(ModEnchantments.ACTIVE_RECHARGE.get());
		if (activeRechargeLevel > 0 && pLivingEntity instanceof Player player && !isOffCooldown(pStack, player)) {
			Map<Item, ItemCooldowns.CooldownInstance> cooldownsMap = player.getCooldowns().cooldowns;
			ItemCooldowns.CooldownInstance instance = cooldownsMap.get(pStack.getItem());
			if (instance == null) return;
			int startTick = instance.startTime;
			int endTick = instance.endTime;
			int currentTick = player.getCooldowns().tickCount;
			if (endTick - currentTick <= activeRechargeLevel) return;
			cooldownsMap.remove(pStack.getItem());
			cooldownsMap.put(pStack.getItem(), new ItemCooldowns.CooldownInstance(startTick, endTick - activeRechargeLevel));
		}
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		ItemStack itemstack = player.getItemInHand(hand);
		int activeRechargeLevel = itemstack.getEnchantmentLevel(ModEnchantments.ACTIVE_RECHARGE.get());
		int doubleExposureLevel = itemstack.getEnchantmentLevel(ModEnchantments.DOUBLE_EXPOSURE.get());
		if (activeRechargeLevel <= 0 && doubleExposureLevel <= 0 && player.getCooldowns().getCooldownPercent(itemstack.getItem(), 0.0F) > 0.0F) {
			return InteractionResultHolder.pass(itemstack);
		}


		if (player.getCooldowns().getCooldownPercent(itemstack.getItem(), 0.0F) > 0.0F) {
			Map<Item, ItemCooldowns.CooldownInstance> cooldownsMap = player.getCooldowns().cooldowns;
			int currentTick = player.getCooldowns().tickCount;
			ItemCooldowns.CooldownInstance instance = cooldownsMap.get(itemstack.getItem());
			if (instance != null) {
				int startTick = instance.startTime;
				if (currentTick - startTick <= 15)
					return InteractionResultHolder.pass(itemstack);
			}
		}

		if (doubleExposureLevel > 0) {
			if (player.getCooldowns().getCooldownPercent(itemstack.getItem(), 0.0F) > 0.0F) {
				Map<Item, ItemCooldowns.CooldownInstance> cooldownsMap = player.getCooldowns().cooldowns;
				int currentTick = player.getCooldowns().tickCount;
				ItemCooldowns.CooldownInstance instance = cooldownsMap.get(itemstack.getItem());
				if (instance != null) {
					int endTick = instance.endTime;
					if (endTick - currentTick > getMaxCooldown(itemstack) && activeRechargeLevel <= 0)
						return InteractionResultHolder.pass(itemstack);
				}
			}
		}

		if (!level.isClientSide) {
			level.playSound(null,
					player.getX(), player.getY(), player.getZ(),
					RPGSounds.STAFF_START.get(),
					SoundSource.PLAYERS, 1.0F, 1.0F
			);


			ModMessages.sendToNearbyPlayers(
					new LoopSoundPacket(player.getId(), true, itemstack),
					(ServerLevel) level,
					player.blockPosition(),
					64.0
			);
		}

		player.startUsingItem(hand);
		return InteractionResultHolder.consume(itemstack);
	}

	@Override
	public void releaseUsing(ItemStack stack, Level level, LivingEntity livingEntity, int timeCharged) {
		if (livingEntity instanceof Player player) {
			if (!level.isClientSide) {
				ModMessages.sendToNearbyPlayers(
						new LoopSoundPacket(player.getId(), false, stack),
						level,
						player.blockPosition(),
						64.0
				);

				level.playSound(null,
						player.getX(), player.getY(), player.getZ(),
						RPGSounds.STAFF_STOP.get(),
						SoundSource.PLAYERS, 1.0F, 1.0F
				);
			}
		}
		super.releaseUsing(stack, level, livingEntity, timeCharged);
	}

	public void cast(Player player, @Nullable LivingEntity target, ItemStack item) {
		if (item.getEnchantmentLevel(ModEnchantments.DOUBLE_EXPOSURE.get()) > 0 && player.getCooldowns().getCooldownPercent(item.getItem(), 0.0F) > 0.0F) {
			Map<Item, ItemCooldowns.CooldownInstance> cooldownsMap = player.getCooldowns().cooldowns;
			ItemCooldowns.CooldownInstance instance = cooldownsMap.get(item.getItem());
			if (instance != null) {
				int endTick = instance.endTime;
				int currentTick = player.getCooldowns().tickCount;
				player.getCooldowns().addCooldown(this, endTick - currentTick + getUseCooldown(item) * 2);
			}
		} else player.getCooldowns().addCooldown(this, getUseCooldown(item));
		player.swing(player.getUsedItemHand());

		switch (getGemType(item)) {
			case EMBER_GEM: {
				player.level().playSound(null,
						player.getX(), player.getY(), player.getZ(),
						RPGSounds.STAFF_EMBER_GEM_CAST.get(),
						SoundSource.PLAYERS, 1.0F, 1.0F
				);

				if (target != null) {
					target.level().playSound(null,
							target.getX(), target.getY(), target.getZ(),
							RPGSounds.STAFF_EMBER_GEM_CAST.get(),
							SoundSource.PLAYERS, 1.0F, 1.0F
					);
					target.setSecondsOnFire(10);
				}
			}
			break;
			case ENDER_EYE: {
				player.level().playSound(null,
						player.getX(), player.getY(), player.getZ(),
						RPGSounds.STAFF_ENDER_EYE_CAST.get(),
						SoundSource.PLAYERS, 1.0F, 1.0F
				);

				if (target != null) {
					target.level().playSound(null,
							target.getX(), target.getY(), target.getZ(),
							RPGSounds.STAFF_ENDER_EYE_CAST.get(),
							SoundSource.PLAYERS, 1.0F, 1.0F
					);
					target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 1200));
				}
			}
			break;
			case HEART_OF_THE_SEA: {
				player.level().playSound(null,
						player.getX(), player.getY(), player.getZ(),
						RPGSounds.STAFF_HEART_OF_THE_SEA_CAST.get(),
						SoundSource.PLAYERS, 1.0F, 1.0F
				);

				if (target != null) {
					target.level().playSound(null,
							target.getX(), target.getY(), target.getZ(),
							RPGSounds.STAFF_HEART_OF_THE_SEA_CAST.get(),
							SoundSource.PLAYERS, 1.0F, 1.0F
					);
					target.addEffect(new MobEffectInstance(ModEffects.AMPHIBIOSIS.get(), 400));
				}
			}
			break;
			case NETHER_STAR: {
				player.level().playSound(null,
						player.getX(), player.getY(), player.getZ(),
						RPGSounds.STAFF_NETHER_STAR_CAST.get(),
						SoundSource.PLAYERS, 0.5F, 1.0F
				);

				if (target != null) {
					target.level().playSound(null,
							target.getX(), target.getY(), target.getZ(),
							RPGSounds.STAFF_NETHER_STAR_CAST.get(),
							SoundSource.PLAYERS, 0.5F, 1.0F
					);
					target.addEffect(new MobEffectInstance(MobEffects.WITHER, 140, 1));
				}
			}
			break;
		}
		if (target != null) target.gameEvent(GameEvent.ENTITY_INTERACT, player);
		item.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(player.getUsedItemHand()));
	}

	@Override
	public boolean hasControls(ItemStack item) {
		return true;
	}

	@Override
	public int getMaxCooldown(ItemStack item) {
		return 300;
	}

	public boolean isValidRepairItem(ItemStack pToRepair, ItemStack pRepair) {
		return pRepair.is(ModTags.Items.LIVING_WOOD_LOGS) || super.isValidRepairItem(pToRepair, pRepair);
	}

	@Override
	public boolean highlightAnimateTarget(ItemStack stack, Player player) {
		return isOffCooldown(stack, player) && player.isUsingItem() && player.getUseItemRemainingTicks() > 0 && player.getUseItem() == stack;
	}

}