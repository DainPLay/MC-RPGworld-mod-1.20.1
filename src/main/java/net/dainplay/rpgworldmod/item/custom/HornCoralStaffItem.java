package net.dainplay.rpgworldmod.item.custom;

import net.dainplay.rpgworldmod.enchantment.ModEnchantments;
import net.dainplay.rpgworldmod.network.LoopSoundPacket;
import net.dainplay.rpgworldmod.network.ModMessages;
import net.dainplay.rpgworldmod.sounds.RPGSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.ContainerEntity;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.EnderChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ForgeMod;

import java.util.Map;
import java.util.UUID;

public class HornCoralStaffItem extends StaffItem implements ChooseTargetItem {
	private static final UUID STAFF_REACH_MODIFIER_UUID = UUID.fromString("a1b2c3d4-e5f6-7890-1234-567890abcdef");

	public HornCoralStaffItem(Properties properties) {
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



	private static void addStaffReachModifier(Player player) {
		AttributeInstance blockReach = player.getAttribute(ForgeMod.BLOCK_REACH.get());
		AttributeInstance entityReach = player.getAttribute(ForgeMod.ENTITY_REACH.get());
		if (blockReach != null && entityReach != null) {
			removeStaffReachModifier(player);
			AttributeModifier modifier = new AttributeModifier(
					STAFF_REACH_MODIFIER_UUID,
					"Staff reach",
					1000.0, // достаточно большое число
					AttributeModifier.Operation.ADDITION
			);
			blockReach.addTransientModifier(modifier);
			entityReach.addTransientModifier(modifier);
		}
	}

	public static void removeStaffReachModifier(Player player) {
		AttributeInstance blockReach = player.getAttribute(ForgeMod.BLOCK_REACH.get());
		AttributeInstance entityReach = player.getAttribute(ForgeMod.ENTITY_REACH.get());
		if (blockReach != null) {
			blockReach.removeModifier(STAFF_REACH_MODIFIER_UUID);
		}
		if (entityReach != null) {
			entityReach.removeModifier(STAFF_REACH_MODIFIER_UUID);
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

	// Метод для сущностей (без изменений, оставлен для полноты)
	public void cast(Player player, Entity target, ItemStack item) {
		if (item.getEnchantmentLevel(ModEnchantments.DOUBLE_EXPOSURE.get()) > 0 &&
				player.getCooldowns().getCooldownPercent(item.getItem(), 0.0F) > 0.0F) {
			Map<Item, ItemCooldowns.CooldownInstance> cooldownsMap = player.getCooldowns().cooldowns;
			ItemCooldowns.CooldownInstance instance = cooldownsMap.get(item.getItem());
			if (instance != null) {
				int endTick = instance.endTime;
				int currentTick = player.getCooldowns().tickCount;
				player.getCooldowns().addCooldown(this, endTick - currentTick + getUseCooldown(item) * 2);
			}
		} else {
			player.getCooldowns().addCooldown(this, getUseCooldown(item));
		}

		player.swing(player.getUsedItemHand());

		if (target instanceof ContainerEntity containerEntity) {
			switch (getGemType(item)) {
				case EMBER_GEM:
					player.level().playSound(null,
							player.getX(), player.getY(), player.getZ(),
							RPGSounds.STAFF_EMBER_GEM_CAST.get(),
							SoundSource.PLAYERS, 1.0F, 1.0F);
					target.level().playSound(null,
							target.getX(), target.getY(), target.getZ(),
							RPGSounds.STAFF_EMBER_GEM_CAST.get(),
							SoundSource.PLAYERS, 1.0F, 1.0F);
					containerEntity.unpackChestVehicleLootTable(player);
					containerEntity.clearChestVehicleContent();
					break;

				case ENDER_EYE:
					addStaffReachModifier(player);
					player.level().playSound(null,
							player.getX(), player.getY(), player.getZ(),
							RPGSounds.STAFF_ENDER_EYE_CAST.get(),
							SoundSource.PLAYERS, 1.0F, 1.0F);
					target.level().playSound(null,
							target.getX(), target.getY(), target.getZ(),
							RPGSounds.STAFF_ENDER_EYE_CAST.get(),
							SoundSource.PLAYERS, 1.0F, 1.0F);
					containerEntity.unpackChestVehicleLootTable(player);
					containerEntity.interactWithContainerVehicle(player);
					break;

				case HEART_OF_THE_SEA:
					player.level().playSound(null,
							player.getX(), player.getY(), player.getZ(),
							RPGSounds.STAFF_HEART_OF_THE_SEA_ITEM.get(),
							SoundSource.PLAYERS, 1.0F, 1.0F);
					target.level().playSound(null,
							target.getX(), target.getY(), target.getZ(),
							RPGSounds.STAFF_HEART_OF_THE_SEA_ITEM.get(),
							SoundSource.PLAYERS, 1.0F, 1.0F);
					containerEntity.unpackChestVehicleLootTable(player);
					for (int i = 0; i < containerEntity.getContainerSize(); i++) {
						ItemStack stack = containerEntity.removeChestVehicleItem(i, Integer.MAX_VALUE);
						if (!stack.isEmpty()) {
							player.getInventory().add(stack);
							if (!stack.isEmpty()) {
								player.drop(stack, false);
							}
						}
					}
					break;

				case NETHER_STAR:
					player.level().playSound(null,
							player.getX(), player.getY(), player.getZ(),
							RPGSounds.STAFF_NETHER_STAR_ITEM.get(),
							SoundSource.PLAYERS, 0.5F, 1.0F);
					target.level().playSound(null,
							target.getX(), target.getY(), target.getZ(),
							RPGSounds.STAFF_NETHER_STAR_ITEM.get(),
							SoundSource.PLAYERS, 0.5F, 1.0F);
					containerEntity.unpackChestVehicleLootTable(player);
					int totalSlots = containerEntity.getContainerSize();
					int emptySlots = 0;
					for (int i = 0; i < totalSlots; i++) {
						if (containerEntity.getChestVehicleItem(i).isEmpty()) {
							emptySlots++;
						}
					}
					float power = ((float) (totalSlots - emptySlots) / totalSlots) * 2.0F;
					target.level().explode(
							null,
							target.getX(), target.getY(), target.getZ(),
							power,
							Level.ExplosionInteraction.MOB
					);
					break;
			}
		}

		item.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(player.getUsedItemHand()));
	}

	// === Проверка типа хранилища (блоки) ===
	public static boolean isStorage(BlockEntity be) {
		if (be == null) return false;
		return be instanceof BaseContainerBlockEntity || be instanceof EnderChestBlockEntity;
	}

	// Метод для блоков (исправленный)
	public void cast(Player player, BlockPos pos, ItemStack item) {
		if (item.getEnchantmentLevel(ModEnchantments.DOUBLE_EXPOSURE.get()) > 0 &&
				player.getCooldowns().getCooldownPercent(item.getItem(), 0.0F) > 0.0F) {
			Map<Item, ItemCooldowns.CooldownInstance> cooldownsMap = player.getCooldowns().cooldowns;
			ItemCooldowns.CooldownInstance instance = cooldownsMap.get(item.getItem());
			if (instance != null) {
				int endTick = instance.endTime;
				int currentTick = player.getCooldowns().tickCount;
				player.getCooldowns().addCooldown(this, endTick - currentTick + getUseCooldown(item) * 2);
			}
		} else {
			player.getCooldowns().addCooldown(this, getUseCooldown(item));
		}

		player.swing(player.getUsedItemHand());

		BlockEntity target = player.level().getBlockEntity(pos);

		if (isStorage(target)) {
			// Отдельно обрабатываем эндер-сундук
			if (target instanceof EnderChestBlockEntity enderChest) {
				switch (getGemType(item)) {
					case EMBER_GEM:
						player.level().playSound(null,
								player.getX(), player.getY(), player.getZ(),
								RPGSounds.STAFF_EMBER_GEM_CAST.get(),
								SoundSource.PLAYERS, 1.0F, 1.0F);
						player.level().playSound(null,
								pos.getX(), pos.getY(), pos.getZ(),
								RPGSounds.STAFF_EMBER_GEM_CAST.get(),
								SoundSource.PLAYERS, 1.0F, 1.0F);
						player.getEnderChestInventory().clearContent();
						break;

					case ENDER_EYE:
						player.level().playSound(null,
								player.getX(), player.getY(), player.getZ(),
								RPGSounds.STAFF_ENDER_EYE_CAST.get(),
								SoundSource.PLAYERS, 1.0F, 1.0F);
						player.level().playSound(null,
								pos.getX(), pos.getY(), pos.getZ(),
								RPGSounds.STAFF_ENDER_EYE_CAST.get(),
								SoundSource.PLAYERS, 1.0F, 1.0F);
						player.openMenu(new SimpleMenuProvider(
								(id, inv, p) -> ChestMenu.threeRows(id, inv, player.getEnderChestInventory()),
								Component.translatable("container.enderchest")
						));
						break;

					case HEART_OF_THE_SEA:
						player.level().playSound(null,
								player.getX(), player.getY(), player.getZ(),
								RPGSounds.STAFF_HEART_OF_THE_SEA_ITEM.get(),
								SoundSource.PLAYERS, 1.0F, 1.0F);
						player.level().playSound(null,
								pos.getX(), pos.getY(), pos.getZ(),
								RPGSounds.STAFF_HEART_OF_THE_SEA_ITEM.get(),
								SoundSource.PLAYERS, 1.0F, 1.0F);
						Container enderInv = player.getEnderChestInventory();
						for (int i = 0; i < enderInv.getContainerSize(); i++) {
							ItemStack stack = enderInv.getItem(i);
							if (!stack.isEmpty()) {
								enderInv.setItem(i, ItemStack.EMPTY);
								player.getInventory().add(stack);
								if (!stack.isEmpty()) {
									player.drop(stack, false);
								}
							}
						}
						break;

					case NETHER_STAR:
						player.level().playSound(null,
								player.getX(), player.getY(), player.getZ(),
								RPGSounds.STAFF_NETHER_STAR_ITEM.get(),
								SoundSource.PLAYERS, 0.5F, 1.0F);
						player.level().playSound(null,
								pos.getX(), pos.getY(), pos.getZ(),
								RPGSounds.STAFF_NETHER_STAR_ITEM.get(),
								SoundSource.PLAYERS, 0.5F, 1.0F);
						int totalSlots = player.getEnderChestInventory().getContainerSize();
						int filledSlots = 0;
						for (int i = 0; i < totalSlots; i++) {
							if (!player.getEnderChestInventory().getItem(i).isEmpty()) filledSlots++;
						}
						float power = 2.0F * (filledSlots / (float) totalSlots);
						player.level().explode(player, pos.getX()+0.5F, pos.getY()+0.5F, pos.getZ()+0.5F, power, Level.ExplosionInteraction.BLOCK);
						break;
				}
			}
			// Обработка всех остальных контейнеров (включая двойные сундуки)
			else {
				// Определяем контейнер и MenuProvider с учётом возможного двойного сундука
				Container container;
				MenuProvider menuProvider;

				if (target instanceof ChestBlockEntity) {
					BlockState state = player.level().getBlockState(pos);
					if (state.getBlock() instanceof ChestBlock chestBlock) {
						// Получаем объединённый контейнер для двойного сундука
						container = ChestBlock.getContainer(chestBlock, state, player.level(), pos, true);
						// Получаем правильный MenuProvider от блока (он создаст меню на 6 рядов для двойного сундука)
						menuProvider = chestBlock.getMenuProvider(state, player.level(), pos);
					} else {
						// На всякий случай, если блок не является ChestBlock (маловероятно)
						container = (Container) target;
						menuProvider = (MenuProvider) target;
					}
				} else {
					// Для остальных типов (ваши BlockEntity, не являющиеся сундуками)
					container = (Container) target;
					menuProvider = (MenuProvider) target;
				}

				// Действие в зависимости от самоцвета
				switch (getGemType(item)) {
					case EMBER_GEM:
						player.level().playSound(null,
								player.getX(), player.getY(), player.getZ(),
								RPGSounds.STAFF_EMBER_GEM_CAST.get(),
								SoundSource.PLAYERS, 1.0F, 1.0F);
						player.level().playSound(null,
								pos.getX(), pos.getY(), pos.getZ(),
								RPGSounds.STAFF_EMBER_GEM_CAST.get(),
								SoundSource.PLAYERS, 1.0F, 1.0F);
						container.clearContent();           // очищает все слоты (для двойного сундука очистит оба)
						container.setChanged();             // уведомляет об изменении
						break;

					case ENDER_EYE:
						player.level().playSound(null,
								player.getX(), player.getY(), player.getZ(),
								RPGSounds.STAFF_ENDER_EYE_CAST.get(),
								SoundSource.PLAYERS, 1.0F, 1.0F);
						player.level().playSound(null,
								pos.getX(), pos.getY(), pos.getZ(),
								RPGSounds.STAFF_ENDER_EYE_CAST.get(),
								SoundSource.PLAYERS, 1.0F, 1.0F);
						addStaffReachModifier(player);
						player.openMenu(menuProvider);      // открывает правильное меню (для двойного сундука – объединённое)
						break;

					case HEART_OF_THE_SEA:
						player.level().playSound(null,
								player.getX(), player.getY(), player.getZ(),
								RPGSounds.STAFF_HEART_OF_THE_SEA_ITEM.get(),
								SoundSource.PLAYERS, 1.0F, 1.0F);
						player.level().playSound(null,
								pos.getX(), pos.getY(), pos.getZ(),
								RPGSounds.STAFF_HEART_OF_THE_SEA_ITEM.get(),
								SoundSource.PLAYERS, 1.0F, 1.0F);
						for (int i = 0; i < container.getContainerSize(); i++) {
							ItemStack stack = container.getItem(i);
							if (!stack.isEmpty()) {
								container.setItem(i, ItemStack.EMPTY);
								player.getInventory().add(stack);
								if (!stack.isEmpty()) {
									player.drop(stack, false);
								}
							}
						}
						container.setChanged();
						break;

					case NETHER_STAR:
						player.level().playSound(null,
								player.getX(), player.getY(), player.getZ(),
								RPGSounds.STAFF_NETHER_STAR_ITEM.get(),
								SoundSource.PLAYERS, 0.5F, 1.0F);
						player.level().playSound(null,
								pos.getX(), pos.getY(), pos.getZ(),
								RPGSounds.STAFF_NETHER_STAR_ITEM.get(),
								SoundSource.PLAYERS, 0.5F, 1.0F);
						int totalSlots = container.getContainerSize();
						int filledSlots = 0;
						for (int i = 0; i < totalSlots; i++) {
							if (!container.getItem(i).isEmpty()) filledSlots++;
						}
						float power = 2.0F * (filledSlots / (float) totalSlots);
						player.level().explode(player, pos.getX()+0.5F, pos.getY()+0.5F, pos.getZ()+0.5F, power, Level.ExplosionInteraction.BLOCK);
						break;
				}
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
		return 300;
	}

	public boolean isValidRepairItem(ItemStack pToRepair, ItemStack pRepair) {
		return pRepair.is(Items.HORN_CORAL) || super.isValidRepairItem(pToRepair, pRepair);
	}

	@Override
	public boolean highlightItemStorages(ItemStack stack, Player player) {
		return isOffCooldown(stack, player) && player.isUsingItem() && player.getUseItemRemainingTicks() > 0 && player.getUseItem() == stack;
	}

	public float getY(ItemStack stack, Entity entity, boolean rightHand) {
		return 0.4F;
	}

	public float getZ(ItemStack stack, Entity entity, boolean rightHand) {
		if (getGemType(stack) == GemType.EMBER_GEM)
			return -1.05F;
		else if (getGemType(stack) == GemType.HEART_OF_THE_SEA)
			return -0.95F;
		else if (getGemType(stack) == GemType.NETHER_STAR)
			return -0.95F;
		else if (getGemType(stack) == GemType.ENDER_EYE)
			return -0.95F;
		else
			return -0.95F;
	}

	public float getX(ItemStack stack, Entity entity, boolean rightHand) {
		return rightHand ? 0.0F : -0.1F;
	}

}