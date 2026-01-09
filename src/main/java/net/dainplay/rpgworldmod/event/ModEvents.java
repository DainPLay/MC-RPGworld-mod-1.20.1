package net.dainplay.rpgworldmod.event;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.dainplay.rpgworldmod.RPGworldMod;
import net.dainplay.rpgworldmod.data.tags.DepressionDeathCheck;
import net.dainplay.rpgworldmod.data.tags.GetDepressedTrigger;
import net.dainplay.rpgworldmod.data.tags.ModAdvancements;
import net.dainplay.rpgworldmod.data.tags.WealdBladeGesturesTrigger;
import net.dainplay.rpgworldmod.effect.ModEffects;
import net.dainplay.rpgworldmod.item.ModItems;
import net.dainplay.rpgworldmod.mana.ClientManaData;
import net.dainplay.rpgworldmod.mana.ClientMaxManaData;
import net.dainplay.rpgworldmod.mana.ManaDataSyncS2CPacket;
import net.dainplay.rpgworldmod.mana.MaxManaDataSyncS2CPacket;
import net.dainplay.rpgworldmod.mana.ModMessages;
import net.dainplay.rpgworldmod.mana.PlayerMana;
import net.dainplay.rpgworldmod.mana.PlayerManaProvider;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.StatFormatter;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerSleepInBedEvent;
import net.minecraftforge.event.entity.player.PlayerWakeUpEvent;
import net.minecraftforge.event.level.SleepFinishedTimeEvent;
import net.minecraftforge.event.village.VillagerTradesEvent;
import net.minecraftforge.event.village.WandererTradesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
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
		}
	}

	@SubscribeEvent
	public static void onRegisterCapabilities(RegisterCapabilitiesEvent event) {
		event.register(PlayerMana.class);
	}

	@SubscribeEvent
	public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
		if (event.player instanceof ServerPlayer serverPlayer) {
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
		if (event.getLevel().isClientSide()) {
			if (event.getEntity() instanceof ServerPlayer player) {
				player.getCapability(PlayerManaProvider.PLAYER_MANA).ifPresent(mana -> {
					mana.recalculateMaxMana((ServerPlayer) player);
					ModMessages.sendToPlayer(new ManaDataSyncS2CPacket(mana.getMana()), player);
					ModMessages.sendToPlayer(new MaxManaDataSyncS2CPacket(mana.getMaxMana()), player);
				});
			}
		}
	}

	@SubscribeEvent
	public static void onPlayerJoinWorld(PlayerEvent.PlayerLoggedInEvent event) {
		if (event.getEntity() instanceof ServerPlayer player) {
			player.getCapability(PlayerManaProvider.PLAYER_MANA).ifPresent(mana -> {
				mana.recalculateMaxMana((ServerPlayer) player);
				ModMessages.sendToPlayer(new ManaDataSyncS2CPacket(mana.getMana()), player);
				ModMessages.sendToPlayer(new MaxManaDataSyncS2CPacket(mana.getMaxMana()), player);
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
					mana.recalculateMaxMana((ServerPlayer) player);
					mana.addMana(player, mana.getMaxMana());
					ModMessages.sendToPlayer(new MaxManaDataSyncS2CPacket(mana.getMaxMana()), player);
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
}
