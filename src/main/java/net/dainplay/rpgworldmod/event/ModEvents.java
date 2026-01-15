package net.dainplay.rpgworldmod.event;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.dainplay.rpgworldmod.RPGworldMod;
import net.dainplay.rpgworldmod.block.custom.EntFaceBlock;
import net.dainplay.rpgworldmod.block.custom.LivingWoodLogBlock;
import net.dainplay.rpgworldmod.block.custom.RieLeavesBlock;
import net.dainplay.rpgworldmod.block.entity.custom.EntFaceBlockEntity;
import net.dainplay.rpgworldmod.data.tags.DepressionDeathCheck;
import net.dainplay.rpgworldmod.data.tags.ModAdvancements;
import net.dainplay.rpgworldmod.item.ModItems;
import net.dainplay.rpgworldmod.network.IllusionForceDataSyncS2CPacket;
import net.dainplay.rpgworldmod.network.IsManaRegenBlockedDataSyncS2CPacket;
import net.dainplay.rpgworldmod.network.ManaDataSyncS2CPacket;
import net.dainplay.rpgworldmod.network.MaxManaDataSyncS2CPacket;
import net.dainplay.rpgworldmod.network.ModMessages;
import net.dainplay.rpgworldmod.network.PlayerIllusionForce;
import net.dainplay.rpgworldmod.network.PlayerIllusionForceProvider;
import net.dainplay.rpgworldmod.network.PlayerMana;
import net.dainplay.rpgworldmod.network.PlayerManaProvider;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
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

		// Проверяем, является ли блок связанным с энтом
		boolean isEntBlock = (block instanceof LivingWoodLogBlock livingWoodLogBlock && livingWoodLogBlock.isRelatedToEnt(state) != 0) ||
				(block instanceof RieLeavesBlock rieLeavesBlock && rieLeavesBlock.isRelatedToEnt(state) != 0);

		// Для блоков лица энта используем другой триггер (через use)
		if (!isEntBlock) {
			return;
		}

		// Поиск ближайшего блока лица энта в радиусе (например, 16 блоков)
		for (int x = -16; x <= 16; x++) {
			for (int y = -16; y <= 16; y++) {
				for (int z = -16; z <= 16; z++) {
					BlockPos checkPos = pos.offset(x, y, z);
					BlockEntity blockEntity = level.getBlockEntity(checkPos);

					if (blockEntity instanceof EntFaceBlockEntity entEntity) {
						// Проверяем, связан ли атакованный блок с этим энтом
						if (entEntity.getRelatedBlocks().contains(pos)) {
							// Будим энта!
							entEntity.onRelatedBlockAttacked(pos);

							// Отменяем стандартное разрушение, если нужно
							if (!player.getAbilities().instabuild) {
								event.setCanceled(true);
							}

							// Можно добавить эффекты или звук
							// level.playSound(null, pos, SoundEvents.ANVIL_LAND, SoundSource.BLOCKS, 0.5F, 1.0F);

							return;
						}
					}
				}
			}
		}
	}
}
