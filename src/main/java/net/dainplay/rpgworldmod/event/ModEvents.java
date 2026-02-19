package net.dainplay.rpgworldmod.event;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.dainplay.rpgworldmod.RPGworldMod;
import net.dainplay.rpgworldmod.block.custom.EntFaceBlock;
import net.dainplay.rpgworldmod.block.custom.LivingWoodLogBlock;
import net.dainplay.rpgworldmod.block.custom.RieLeavesBlock;
import net.dainplay.rpgworldmod.block.entity.custom.EntFaceBlockEntity;
import net.dainplay.rpgworldmod.data.tags.DepressionDeathCheck;
import net.dainplay.rpgworldmod.data.tags.ModAdvancements;
import net.dainplay.rpgworldmod.effect.ModEffects;
import net.dainplay.rpgworldmod.item.ModItems;
import net.dainplay.rpgworldmod.item.custom.EmptyScrollItem;
import net.dainplay.rpgworldmod.item.custom.ScrollItem;
import net.dainplay.rpgworldmod.network.BoundEntitySyncPacket;
import net.dainplay.rpgworldmod.network.IllusionForceDataSyncS2CPacket;
import net.dainplay.rpgworldmod.network.IsManaRegenBlockedDataSyncS2CPacket;
import net.dainplay.rpgworldmod.network.ManaDataSyncS2CPacket;
import net.dainplay.rpgworldmod.network.MaxManaDataSyncS2CPacket;
import net.dainplay.rpgworldmod.network.ModMessages;
import net.dainplay.rpgworldmod.network.PlayerIllusionForce;
import net.dainplay.rpgworldmod.network.PlayerIllusionForceProvider;
import net.dainplay.rpgworldmod.network.PlayerMana;
import net.dainplay.rpgworldmod.network.PlayerManaProvider;
import net.dainplay.rpgworldmod.sounds.RPGSounds;
import net.dainplay.rpgworldmod.util.BoundEntityHelper;
import net.dainplay.rpgworldmod.util.ModTags;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.GrindstoneEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingBreatheEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerSleepInBedEvent;
import net.minecraftforge.event.entity.player.PlayerWakeUpEvent;
import net.minecraftforge.event.village.VillagerTradesEvent;
import net.minecraftforge.event.village.WandererTradesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

@Mod.EventBusSubscriber(modid = RPGworldMod.MOD_ID)
public class ModEvents {

	@SubscribeEvent
	public static void addCustomTrades(VillagerTradesEvent event) {
		if (event.getType() == VillagerProfession.FISHERMAN) {
			Int2ObjectMap<List<VillagerTrades.ItemListing>> trades = event.getTrades();

			trades.get(5).add((pTrader, pRandom) -> new MerchantOffer(
					new ItemStack(Items.EMERALD, 21),
					new ItemStack(ModItems.GASBASS.get(), 1),
					1, 50, 0.5f
			));
		}
	}

	@SubscribeEvent
	public static void addCustomWanderingTrades(WandererTradesEvent event) {

		List<VillagerTrades.ItemListing> genericTrades = event.getGenericTrades();
		List<VillagerTrades.ItemListing> rareTrades = event.getRareTrades();

		rareTrades.add((pTrader, pRandom) -> new MerchantOffer(
				new ItemStack(Items.EMERALD, 21),
				new ItemStack(ModItems.GASBASS.get(), 1),
				1, 50, 0.5f
		));
	}

	@SubscribeEvent
	public static void onAttachCapabilitiesPlayer(AttachCapabilitiesEvent<Entity> event) {
		if (event.getObject() instanceof Player) {
			if (!event.getObject().getCapability(PlayerManaProvider.PLAYER_MANA).isPresent()) {
				event.addCapability(new ResourceLocation(RPGworldMod.MOD_ID, "properties"), new PlayerManaProvider());
			}
			if (!event.getObject().getCapability(PlayerIllusionForceProvider.PLAYER_ILLUSION_FORCE).isPresent()) {
				event.addCapability(new ResourceLocation(RPGworldMod.MOD_ID, "illusion_force"), new PlayerIllusionForceProvider());
			}
		}
	}

	@SubscribeEvent
	public static void onPlayerCloned(PlayerEvent.Clone event) {
		if (event.isWasDeath()) {
			event.getOriginal().getCapability(PlayerManaProvider.PLAYER_MANA).ifPresent(oldStore -> {
				event.getOriginal().getCapability(PlayerManaProvider.PLAYER_MANA).ifPresent(newStore -> {
					newStore.copyFrom(oldStore);
				});
			});
			event.getOriginal().getCapability(PlayerIllusionForceProvider.PLAYER_ILLUSION_FORCE).ifPresent(oldStore -> {
				event.getOriginal().getCapability(PlayerIllusionForceProvider.PLAYER_ILLUSION_FORCE).ifPresent(newStore -> {
					newStore.copyFrom(oldStore);
				});
			});
		}
	}

	@SubscribeEvent
	public static void onRegisterCapabilities(RegisterCapabilitiesEvent event) {
		event.register(PlayerMana.class);
		event.register(PlayerIllusionForce.class);
	}

	@SubscribeEvent
	public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
		if (event.player instanceof ServerPlayer serverPlayer) {
			serverPlayer.getCapability(PlayerIllusionForceProvider.PLAYER_ILLUSION_FORCE).ifPresent(illusionForce -> {
				if (illusionForce.getIllusionForce() >= 0) {
					illusionForce.setIllusionForce(serverPlayer, Math.max(0, illusionForce.getIllusionForce() - 1));
					if (illusionForce.getIllusionForce() == 0)
						illusionForce.setEntPosition(serverPlayer, null);
				}
			});
			/*if (serverPlayer.getAdvancements().getOrStartProgress(serverPlayer.getServer().getAdvancements().getAdvancement(DepressionDeathCheck.ID)).isDone() && !serverPlayer.isDeadOrDying()) {
				Advancement advancement = serverPlayer.server.getAdvancements().getAdvancement(DepressionDeathCheck.ID);
				AdvancementProgress progress = serverPlayer.getAdvancements().getOrStartProgress(advancement);
				for (String criterion : progress.getCompletedCriteria()) {
					serverPlayer.getAdvancements().revoke(advancement, criterion);
				}
			}*/
			serverPlayer.getCapability(PlayerManaProvider.PLAYER_MANA).ifPresent(mana -> {

				if (serverPlayer.isSleeping() && mana.getManaRegenBlocked() > 0) {
					// Будим игрока
					serverPlayer.stopSleeping();
					serverPlayer.displayClientMessage(
							Component.translatable("mana.rpgworldmod.paranoia_wake_up"),
							true
					);
				}

				int regenSpeed = 50;
				if (ModItems.LAPIS_CHARM.get().isEquippedBy(serverPlayer) && serverPlayer.totalExperience >= 3)
					regenSpeed = (int) (regenSpeed * 0.6F);

				// Проверяем, не заблокировано ли восстановление маны
				if (event.phase == TickEvent.Phase.START && mana.getManaRegenBlocked() <= 0 &&
						mana.getMana() > 0 && mana.getMana() < mana.getMaxMana() &&
						serverPlayer.tickCount % regenSpeed == 0) {

					if (ModItems.LAPIS_CHARM.get().isEquippedBy(serverPlayer) && mana.getMana() < mana.getMaxMana()) {
						serverPlayer.giveExperiencePoints(-1);
						mana.addMana(serverPlayer, 1);
					} else {
						mana.addMana(serverPlayer, 1);
					}
				}
				if (event.phase == TickEvent.Phase.START && mana.getManaRegenBlocked() > 0)
					mana.setManaRegenBlocked(serverPlayer, mana.getManaRegenBlocked() - 1);

				mana.recalculateMaxMana(serverPlayer);
				//serverPlayer.sendSystemMessage(Component.literal("Мана: " + mana.getMana() + "/" + mana.getMaxMana()));
			});
		}
	}

	@SubscribeEvent
	public static void onPlayerJoinWorld(EntityJoinLevelEvent event) {
		if (event.getEntity() instanceof ServerPlayer player) {
			player.getCapability(PlayerManaProvider.PLAYER_MANA).ifPresent(mana -> {
				mana.recalculateMaxMana((ServerPlayer) player);
				ModMessages.sendToPlayer(new ManaDataSyncS2CPacket(mana.getMana()), player);
				ModMessages.sendToPlayer(new MaxManaDataSyncS2CPacket(mana.getMaxMana()), player);
				ModMessages.sendToPlayer(new IsManaRegenBlockedDataSyncS2CPacket(mana.getManaRegenBlocked()), player);
			});
			player.getCapability(PlayerIllusionForceProvider.PLAYER_ILLUSION_FORCE).ifPresent(illusionForce -> {
				ModMessages.sendToPlayer(new IllusionForceDataSyncS2CPacket(illusionForce.getIllusionForce(), illusionForce.getEntPosition()), player);
			});
		}
	}

	@SubscribeEvent
	public static void onPlayerJoinWorld(PlayerEvent.PlayerLoggedInEvent event) {
		if (event.getEntity() instanceof ServerPlayer player) {
			player.getCapability(PlayerManaProvider.PLAYER_MANA).ifPresent(mana -> {
				mana.recalculateMaxMana((ServerPlayer) player);
				ModMessages.sendToPlayer(new ManaDataSyncS2CPacket(mana.getMana()), player);
				ModMessages.sendToPlayer(new MaxManaDataSyncS2CPacket(mana.getMaxMana()), player);
				ModMessages.sendToPlayer(new IsManaRegenBlockedDataSyncS2CPacket(mana.getManaRegenBlocked()), player);
			});
			player.getCapability(PlayerIllusionForceProvider.PLAYER_ILLUSION_FORCE).ifPresent(illusionForce -> {
				ModMessages.sendToPlayer(new IllusionForceDataSyncS2CPacket(illusionForce.getIllusionForce(), illusionForce.getEntPosition()), player);
			});
		}
	}

	@SubscribeEvent
	public static void onPlayerDeath(LivingDeathEvent event) {
		if (event.getEntity() instanceof ServerPlayer player) {
			player.getCapability(PlayerManaProvider.PLAYER_MANA).ifPresent(mana -> {
				if (mana.getMana() == 0) {
					player.setRespawnPosition(player.serverLevel().dimension(), null, 0, false, false);
					ModAdvancements.DEPRESSION_DEATH_CHECK.trigger(player);
				}
			});
		}
	}

	@SubscribeEvent
	public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
		if (event.getEntity() instanceof ServerPlayer player) {
			player.getServer().execute(() -> {
				player.getCapability(PlayerManaProvider.PLAYER_MANA).ifPresent(mana -> {
					if (player.getAdvancements().getOrStartProgress(player.getServer().getAdvancements().getAdvancement(DepressionDeathCheck.ID)).isDone()) {
						player.displayClientMessage(Component.translatable("mana.rpgworldmod.depression_respawn"), false);
						Advancement advancement = player.server.getAdvancements().getAdvancement(DepressionDeathCheck.ID);
						AdvancementProgress progress = player.getAdvancements().getOrStartProgress(advancement);
						for (String criterion : progress.getCompletedCriteria()) {
							player.getAdvancements().revoke(advancement, criterion);
						}
					}
					mana.setManaRegenBlocked(player, 0);
					mana.recalculateMaxMana(player);
					mana.addMana(player, mana.getMaxMana());
					ModMessages.sendToPlayer(new MaxManaDataSyncS2CPacket(mana.getMaxMana()), player);
				});
				player.getCapability(PlayerIllusionForceProvider.PLAYER_ILLUSION_FORCE).ifPresent(illusionForce -> {
					illusionForce.setIllusionForce(player, 0);
					illusionForce.setEntPosition(player, null);
					ModMessages.sendToPlayer(new IllusionForceDataSyncS2CPacket(0, null), player);
				});
			});

		}
	}

	@SubscribeEvent
	public static void onPlayerWakeUp(PlayerWakeUpEvent event) {
		if (event.getEntity() instanceof ServerPlayer player) {
			player.getServer().execute(() -> {
				player.getCapability(PlayerManaProvider.PLAYER_MANA).ifPresent(mana -> {
					if (mana.getMana() > 0) mana.addMana(player, mana.getMaxMana() / 2);
				});
			});
		}
	}


	// Отменяем возможность лечь спать при отравлении
	@SubscribeEvent
	public static void onPlayerTrySleep(PlayerSleepInBedEvent event) {

		if (event.getEntity() instanceof ServerPlayer player) {
			player.getServer().execute(() -> {
				player.getCapability(PlayerManaProvider.PLAYER_MANA).ifPresent(mana -> {
					if (mana.getManaRegenBlocked() > 0) {
						event.setResult(Player.BedSleepingProblem.OTHER_PROBLEM);
						player.displayClientMessage(
								Component.translatable("mana.rpgworldmod.paranoia_wake_up"),
								true
						);
					}
				});
			});
		}
	}

	@SubscribeEvent
	public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
		// Проверяем, что это серверная сторона
		if (event.getLevel().isClientSide()) {
			return;
		}

		Level level = event.getLevel();
		BlockPos pos = event.getPos();
		Player player = event.getEntity();
		BlockState state = level.getBlockState(pos);
		Block block = state.getBlock();


		if (block instanceof EntFaceBlock entFaceBlock) {
			if (level.getBlockEntity(pos) instanceof EntFaceBlockEntity entEntity) {

				if (entFaceBlock.isAsleep(state))
					entEntity.onRelatedBlockAttacked(pos);

				if (!player.getAbilities().instabuild) {
					event.setCanceled(true);
				}

			}

			return;
		}

		boolean isEntBlock = (block instanceof LivingWoodLogBlock livingWoodLogBlock && livingWoodLogBlock.isRelatedToEnt(state) != 0) ||
				(block instanceof RieLeavesBlock rieLeavesBlock && rieLeavesBlock.isRelatedToEnt(state) != 0);

		if (!isEntBlock) {
			return;
		}

		for (int x = -16; x <= 16; x++) {
			for (int y = -16; y <= 16; y++) {
				for (int z = -16; z <= 16; z++) {
					BlockPos checkPos = pos.offset(x, y, z);
					BlockEntity blockEntity = level.getBlockEntity(checkPos);

					if (blockEntity instanceof EntFaceBlockEntity entEntity) {
						if (entEntity.getRelatedBlocks().contains(pos)) {
							entEntity.onRelatedBlockAttacked(pos);

							if (!player.getAbilities().instabuild) {
								event.setCanceled(true);
							}

							return;
						}
					}
				}
			}
		}
	}

	@SubscribeEvent
	public static void onLivingHurt(LivingHurtEvent event) {
		// Проверяем, что урон нанесён стрелой
		if (event.getSource().getDirectEntity() instanceof AbstractArrow arrow &&
				arrow.getOwner() instanceof Player shooter &&
				!arrow.level().isClientSide) {

			CompoundTag arrowTag = arrow.getPersistentData();
			// Проверяем, что это наша особенная стрела
			if (arrowTag.hasUUID("BoundPlayer") && arrowTag.getBoolean("LivingWoodArrow")) {
				LivingEntity target = event.getEntity();

				// Проверяем, что цель не стрелок и не эндермен
				if (target == shooter) {
					return;
				}

				// Проверяем, что урон был действительно нанесён (значение > 0)
				if (event.getAmount() <= 0) {
					return;
				}

				// Остальной код привязки...
				int knockback = arrow.getKnockback();
				if (knockback > 0) {
					CompoundTag mobTag = target.getPersistentData();
					mobTag.putInt("PunchLevel", knockback);
				}

				BoundEntityHelper.bindMobToPlayer(target, shooter, arrowTag.getDouble("BoundPullRange"));
				float pitch = 1.0F / (arrow.level().getRandom().nextFloat() * 0.4F + 1.2F);
				arrow.level().playSound(null, target.getX(), target.getY(), target.getZ(),
						RPGSounds.LIVING_WOOD_BOW_TIE.get(), SoundSource.PLAYERS,
						0.5F, pitch);
				arrow.level().playSound(null, shooter.getX(), shooter.getY(), shooter.getZ(),
						RPGSounds.LIVING_WOOD_BOW_TIE.get(), SoundSource.PLAYERS,
						0.5F, pitch);

				arrowTag.remove("BoundPlayer");
				arrowTag.remove("LivingWoodArrow");
				arrowTag.remove("ShotTime");
				arrowTag.remove("BoundPullRange");
				arrow.setNoGravity(false);
			}
		}
	}

	@SubscribeEvent
	public static void onLivingUpdate(LivingEvent.LivingTickEvent event) {
		LivingEntity entity = event.getEntity();

		// Проверяем привязанных мобов каждые 20 тиков (только на сервере)
		if (!entity.level().isClientSide && entity.tickCount % 20 == 0) {
			CompoundTag tag = entity.getPersistentData();
			if (tag.hasUUID("BoundPlayer") && tag.getBoolean("LivingWoodBound")) {
				// Получаем игрока по UUID
				Player player = entity.level().getPlayerByUUID(tag.getUUID("BoundPlayer"));
				if (player != null && !player.isRemoved()) {
					// Проверяем расстояние
					if (entity.distanceTo(player) > tag.getDouble("BoundPullRange")) {
						// Удаляем привязку, если слишком далеко
						tag.remove("BoundPlayer");
						tag.remove("BoundTime");
						tag.remove("LivingWoodBound");
						tag.remove("PunchLevel");
						tag.remove("BoundPullRange");

						float pitch = 1.0F / (entity.level().getRandom().nextFloat() * 0.4F + 1.2F);
						entity.level().playSound(null, entity.getX(), entity.getY(), entity.getZ(),
								RPGSounds.LIVING_WOOD_BOW_BREAK.get(), SoundSource.PLAYERS,
								0.5F, pitch);
						entity.level().playSound(null, player.getX(), player.getY(), player.getZ(),
								RPGSounds.LIVING_WOOD_BOW_BREAK.get(), SoundSource.PLAYERS,
								0.5F, pitch);
					}
				} else {
					// Если игрок не найден (вышел из игры), удаляем привязку
					tag.remove("BoundPlayer");
					tag.remove("BoundTime");
					tag.remove("LivingWoodBound");
					tag.remove("PunchLevel");
					tag.remove("BoundPullRange");

					float pitch = 1.0F / (entity.level().getRandom().nextFloat() * 0.4F + 1.2F);
					entity.level().playSound(null, entity.getX(), entity.getY(), entity.getZ(),
							RPGSounds.LIVING_WOOD_BOW_BREAK.get(), SoundSource.PLAYERS,
							0.5F, pitch);
				}
			}
		}

		// Проверяем привязанных мобов каждые 5 тиков (только на сервере)
		if (!entity.level().isClientSide && entity.tickCount % 5 == 0) {
			CompoundTag tag = entity.getPersistentData();
			if (tag.hasUUID("BoundPlayer") && tag.getBoolean("LivingWoodBound")) {
				Player player = entity.level().getPlayerByUUID(tag.getUUID("BoundPlayer"));

				if (player != null) {
					// Отправляем пакет с данными о привязанном существе
					ModMessages.sendToNearbyPlayers(
							new BoundEntitySyncPacket(
									entity.getId(),
									new BoundEntitySyncPacket.BoundEntityData(
											entity.getId(),
											player.getUUID(),
											player.getX(), player.getY(), player.getZ(),
											false
									)
							),
							entity.level(),
							entity.blockPosition(),
							300
					);
				} else {
					// Если игрок не найден, отправляем пакет удаления
					ModMessages.sendToNearbyPlayers(
							new BoundEntitySyncPacket(entity.getId()),
							entity.level(),
							entity.blockPosition(),
							300
					);
				}
			} else {
				// Если существо не привязано, отправляем пакет удаления
				ModMessages.sendToNearbyPlayers(
						new BoundEntitySyncPacket(entity.getId()),
						entity.level(),
						entity.blockPosition(),
						300
				);
			}
		}
	}

	@SubscribeEvent
	public static void onGrindstoneUse(GrindstoneEvent.OnPlaceItem event) {
		ItemStack topItem = event.getTopItem();
		ItemStack bottomItem = event.getBottomItem();

		if (topItem.getItem() instanceof EmptyScrollItem
				|| bottomItem.getItem() instanceof EmptyScrollItem
				|| topItem.getItem() instanceof ScrollItem
				|| bottomItem.getItem() instanceof ScrollItem) {
			event.setCanceled(true);
		}
	}

	@SubscribeEvent
	public static void onFoodEaten(LivingEntityUseItemEvent.Finish event) {
		if (!(event.getEntity() instanceof ServerPlayer serverPlayer)) {
			return;
		}

		ItemStack itemStack = event.getItem();

		if (itemStack.getItem().isEdible() && itemStack.is(ModTags.Items.SWEET_FOOD)) {
			serverPlayer.getCapability(PlayerManaProvider.PLAYER_MANA).ifPresent(mana -> {
				if (mana.getMana() > 0) {
					mana.addMana(serverPlayer, itemStack.getItem().getFoodProperties(itemStack, serverPlayer).getNutrition() * 2);
					if(serverPlayer.hasEffect(ModEffects.PARANOIA.get()))
						ModAdvancements.EAT_SWEETS_PARANOID_TRIGGER.trigger(serverPlayer);
				}
			});
		}
	}

	@SubscribeEvent
	public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
		Player player = event.getEntity();

		BlockState blockState = event.getLevel().getBlockState(event.getPos());

		if (blockState.is(ModTags.Blocks.SWEET_FOOD)) {
			if (player.canEat(false) && player instanceof ServerPlayer serverPlayer) {
				serverPlayer.getCapability(PlayerManaProvider.PLAYER_MANA).ifPresent(mana -> {
					if (mana.getMana() > 0) {
						mana.addMana(serverPlayer, 4);
						if(serverPlayer.hasEffect(ModEffects.PARANOIA.get()))
							ModAdvancements.EAT_SWEETS_PARANOID_TRIGGER.trigger(serverPlayer);
					}
				});
			}
		}
	}

	@SubscribeEvent
	public void onLivingBreathe(LivingBreatheEvent event) {
		LivingEntity entity = event.getEntity();

		if (entity.hasEffect(ModEffects.AMPHIBIOSIS.get())) {
			if (entity.isEyeInFluid(FluidTags.WATER)) {
				event.setCanBreathe(true);
				event.setCanRefillAir(true);
				event.setConsumeAirAmount(0);
				event.setRefillAirAmount(4);
			}
			else {
				event.setCanBreathe(false);
				event.setCanRefillAir(false);
				event.setConsumeAirAmount(1);
				event.setRefillAirAmount(0);
			}
		}
	}

	@SubscribeEvent
	public void onEnderManDrown(LivingDropsEvent event) {
		// 1. Проверяем, что умерший - эндермен
		if (!(event.getEntity() instanceof EnderMan enderMan)) return;

		// 2. Проверяем измерение (Энд)
		if (enderMan.level().dimension() != Level.END) return;

		// 3. Проверяем, идёт ли дождь на позиции эндермена
		if (!enderMan.level().isRainingAt(enderMan.blockPosition())) return;

		// 4. Проверяем тип урона (утопление)
		if (!event.getSource().is(DamageTypes.DROWN)) return;

		// Все условия выполнены – добавляем особый предмет
		ItemStack specialItem = new ItemStack(ModItems.MUSIC_DISC_RAIN_A_SIDE.get(), 1); // ваш предмет
		ItemEntity drop = new ItemEntity(
				enderMan.level(),
				enderMan.getX(), enderMan.getY(), enderMan.getZ(),
				specialItem
		);
		event.getDrops().add(drop);
	}
}
