package net.dainplay.rpgworldmod.item.custom;

import net.dainplay.rpgworldmod.effect.ModEffects;
import net.dainplay.rpgworldmod.enchantment.ModEnchantments;
import net.dainplay.rpgworldmod.network.LoopSoundPacket;
import net.dainplay.rpgworldmod.network.ModMessages;
import net.dainplay.rpgworldmod.sounds.RPGSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

public class TubeCoralStaffItem extends StaffItem implements ChooseTargetItem {

	public TubeCoralStaffItem(Properties properties) {
		super(properties);
	}

	public String getFirstPredicate(ItemStack item) {
		return Minecraft.getInstance().options.keyAttack.getKey().getDisplayName().getString();
	}

	@Override
	public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
		return enchantment == Enchantments.MENDING || super.canApplyAtEnchantingTable(stack, enchantment);
	}

	@Override
	public boolean hasTarget(ItemStack item) {
		return true;
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
			// Воспроизводим звук начала использования для всех рядом
			level.playSound(null,
					player.getX(), player.getY(), player.getZ(),
					RPGSounds.STAFF_START.get(),
					SoundSource.PLAYERS, 1.0F, 1.0F
			);

			// Запускаем зацикленный звук на клиентах
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

	public void cast(Player player, List<ItemEntity> targets, ItemStack item) {
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

		for (ItemEntity target : targets) {
			switch (getGemType(item)) {
				case EMBER_GEM: {
					player.level().playSound(null,
							player.getX(), player.getY(), player.getZ(),
							RPGSounds.STAFF_EMBER_GEM_CAST.get(),
							SoundSource.PLAYERS, 1.0F, 1.0F
					);
					player.gameEvent(GameEvent.ENTITY_INTERACT, player);

					if (target != null) {
						target.level().playSound(null,
								target.getX(), target.getY(), target.getZ(),
								RPGSounds.STAFF_EMBER_GEM_CAST.get(),
								SoundSource.PLAYERS, 1.0F, 1.0F
						);
						if (!target.fireImmune()) {
							target.setSecondsOnFire(5);
						}
						target.hurt(player.level().damageSources().inFire(), 1F);
					}
				}
				break;
				case ENDER_EYE: {
					player.level().playSound(null,
							player.getX(), player.getY(), player.getZ(),
							RPGSounds.STAFF_ENDER_EYE_CAST.get(),
							SoundSource.PLAYERS, 1.0F, 1.0F
					);
					player.gameEvent(GameEvent.ENTITY_INTERACT, player);

					if (target != null) {
						target.level().playSound(null,
								target.getX(), target.getY(), target.getZ(),
								RPGSounds.STAFF_ENDER_EYE_CAST.get(),
								SoundSource.PLAYERS, 1.0F, 1.0F
						);
						target.setGlowingTag(true);
					}
				}
				break;
				case HEART_OF_THE_SEA: {
					player.level().playSound(null,
							player.getX(), player.getY(), player.getZ(),
							RPGSounds.STAFF_HEART_OF_THE_SEA_ITEM.get(),
							SoundSource.PLAYERS, 1.0F, 1.0F
					);
					player.gameEvent(GameEvent.SPLASH);

					if (target != null) {
						target.level().playSound(null,
								target.getX(), target.getY(), target.getZ(),
								RPGSounds.STAFF_HEART_OF_THE_SEA_ITEM.get(),
								SoundSource.PLAYERS, 1.0F, 1.0F
						);
						ItemStack itemStack = target.getItem();
						player.getInventory().add(itemStack);
						if (!itemStack.isEmpty()) {
							player.drop(itemStack, false);
						}
						target.discard();
					}
				}
				break;
				case NETHER_STAR: {
					player.level().playSound(null,
							player.getX(), player.getY(), player.getZ(),
							RPGSounds.STAFF_NETHER_STAR_ITEM.get(),
							SoundSource.PLAYERS, 0.5F, 1.0F
					);
					player.level().gameEvent(player, GameEvent.EXPLODE, new Vec3(player.getX(), player.getY(), player.getZ()));

					if (target != null) {
						target.level().playSound(null,
								target.getX(), target.getY(), target.getZ(),
								RPGSounds.STAFF_NETHER_STAR_ITEM.get(),
								SoundSource.PLAYERS, 0.5F, 1.0F
						);
						ItemStack targetStack = target.getItem();
						float power = ((float) targetStack.getCount() / targetStack.getMaxStackSize()) * 2.0F;
						target.level().explode(target, target.getX(), target.getY(), target.getZ(), power, Level.ExplosionInteraction.MOB);
						target.discard();
					}
				}
				break;
			}
		}
		item.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(player.getUsedItemHand()));
	}

	@Override
	public boolean hasControls(ItemStack item) {
		return true;
	}

	@Override
	public int getMaxCooldown(ItemStack item) {
		return 200;
	}

	public boolean isValidRepairItem(ItemStack pToRepair, ItemStack pRepair) {
		return pRepair.is(Items.TUBE_CORAL) || super.isValidRepairItem(pToRepair, pRepair);
	}

	@Override
	public boolean highlightItemsInSight(ItemStack stack, Player player) {
		return isOffCooldown(stack, player) && player.isUsingItem() && player.getUseItemRemainingTicks() > 0 && player.getUseItem() == stack;
	}

	@Override
	public float get1XOffset(ItemStack stack, Entity entity, boolean righthand) {
		return righthand ? 0.175F : 0.25F;
	}

	public float getY(ItemStack stack, Entity entity, boolean rightHand) {
		return 0.55F;
	}

	@Override
	public float getX(ItemStack stack, Entity entity, boolean righthand) {
		return -0.065F;
	}

}