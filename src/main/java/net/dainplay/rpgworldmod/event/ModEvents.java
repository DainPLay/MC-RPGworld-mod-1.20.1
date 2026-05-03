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
import net.dainplay.rpgworldmod.enchantment.ModEnchantments;
import net.dainplay.rpgworldmod.item.ModItems;
import net.dainplay.rpgworldmod.item.custom.DaggerItem;
import net.dainplay.rpgworldmod.item.custom.EmptyScrollItem;
import net.dainplay.rpgworldmod.item.custom.EnderEyeScrollItem;
import net.dainplay.rpgworldmod.item.custom.GasbassItem;
import net.dainplay.rpgworldmod.item.custom.HornCoralStaffItem;
import net.dainplay.rpgworldmod.item.custom.IgniteOnCritItem;
import net.dainplay.rpgworldmod.item.custom.NetherStarScrollItem;
import net.dainplay.rpgworldmod.item.custom.PillagerScrollItem;
import net.dainplay.rpgworldmod.item.custom.ScrollItem;
import net.dainplay.rpgworldmod.item.custom.SculkStaffItem;
import net.dainplay.rpgworldmod.item.custom.StaffItem;
import net.dainplay.rpgworldmod.item.custom.WealdBladeItem;
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
import net.dainplay.rpgworldmod.network.PlayerSculkStaffCD;
import net.dainplay.rpgworldmod.network.PlayerSculkStaffCDProvider;
import net.dainplay.rpgworldmod.network.SculkStaffCDDataSyncS2CPacket;
import net.dainplay.rpgworldmod.network.TotemEffectPacket;
import net.dainplay.rpgworldmod.sounds.RPGSounds;
import net.dainplay.rpgworldmod.util.BoundEntityHelper;
import net.dainplay.rpgworldmod.util.EffectSyncHandler;
import net.dainplay.rpgworldmod.util.ModTags;
import net.dainplay.rpgworldmod.util.RemoteOpenContainerRegistry;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.CompoundContainer;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.PlayerEnderChestContainer;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.GrindstoneEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.VanillaGameEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingBreatheEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.CriticalHitEvent;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerSleepInBedEvent;
import net.minecraftforge.event.entity.player.PlayerWakeUpEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.event.village.VillagerTradesEvent;
import net.minecraftforge.event.village.WandererTradesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

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
			if (!event.getObject().getCapability(PlayerSculkStaffCDProvider.PLAYER_SCULK_STAFF_COOLDOWN).isPresent()) {
				event.addCapability(new ResourceLocation(RPGworldMod.MOD_ID, "sculk_staff_cooldown"), new PlayerSculkStaffCDProvider());
			}
		}
	}

	@SubscribeEvent
	public static void onPlayerClone(PlayerEvent.Clone event) {
		if (event.getEntity().level().isClientSide) return;

		event.getOriginal().reviveCaps();

		event.getOriginal().getCapability(PlayerManaProvider.PLAYER_MANA).ifPresent(oldMana -> {
			event.getEntity().getCapability(PlayerManaProvider.PLAYER_MANA).ifPresent(newMana -> {
				newMana.copyFrom(oldMana);
			});
		});

		event.getOriginal().getCapability(PlayerIllusionForceProvider.PLAYER_ILLUSION_FORCE).ifPresent(oldForce -> {
			event.getEntity().getCapability(PlayerIllusionForceProvider.PLAYER_ILLUSION_FORCE).ifPresent(newForce -> {
				newForce.copyFrom(oldForce);
			});
		});

		event.getOriginal().getCapability(PlayerSculkStaffCDProvider.PLAYER_SCULK_STAFF_COOLDOWN).ifPresent(oldCD -> {
			event.getEntity().getCapability(PlayerSculkStaffCDProvider.PLAYER_SCULK_STAFF_COOLDOWN).ifPresent(newCD -> {
				newCD.copyFrom(oldCD);
			});
		});

		event.getOriginal().invalidateCaps();
	}

	@SubscribeEvent
	public static void onRegisterCapabilities(RegisterCapabilitiesEvent event) {
		event.register(PlayerMana.class);
		event.register(PlayerIllusionForce.class);
		event.register(PlayerSculkStaffCD.class);
	}

	@SubscribeEvent
	public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
		if (event.player instanceof ServerPlayer serverPlayer) {
			serverPlayer.getCapability(PlayerIllusionForceProvider.PLAYER_ILLUSION_FORCE).ifPresent(illusionForce -> {
				if (illusionForce.getIllusionForce() >= 0) {
					illusionForce.setIllusionForce(serverPlayer, Math.max(0, illusionForce.getIllusionForce() - 1), illusionForce.getIllusionForce() > 0, illusionForce.getIsEnt());
					if (illusionForce.getIllusionForce() == 0)
						illusionForce.clearEntPosition(serverPlayer);
				}
			});
			serverPlayer.getCapability(PlayerSculkStaffCDProvider.PLAYER_SCULK_STAFF_COOLDOWN).ifPresent(sculkStaffCD -> {
				if (sculkStaffCD.getCooldown() > 0 && !(serverPlayer.isUsingItem() && serverPlayer.getUseItem().getItem() instanceof SculkStaffItem)) {
					serverPlayer.getCooldowns().addCooldown(ModItems.SCULK_STAFF.get(), sculkStaffCD.getCooldown());
					sculkStaffCD.setCooldown(serverPlayer, 0);
					ModMessages.sendToPlayer(new SculkStaffCDDataSyncS2CPacket(0), serverPlayer);
				}
			});

			serverPlayer.getCapability(PlayerManaProvider.PLAYER_MANA).ifPresent(mana -> {
				if (serverPlayer.isSleeping() && mana.getManaRegenBlocked() > 0) {
					serverPlayer.stopSleeping();
					serverPlayer.displayClientMessage(
							Component.translatable("mana.rpgworldmod.paranoia_wake_up"),
							true
					);
				}

				int regenSpeed = 50;
				if (ModItems.LAPIS_CHARM.get().isEquippedBy(serverPlayer) && serverPlayer.totalExperience >= 3)
					regenSpeed = (int) (regenSpeed * 0.6F);


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
				ModMessages.sendToPlayer(new IllusionForceDataSyncS2CPacket(
						illusionForce.getIllusionForce(),
						illusionForce.getEntPosX(),
						illusionForce.getEntPosY(),
						illusionForce.getEntPosZ(),
						false,
						illusionForce.getIsEnt()
				), player);
			});
			player.getCapability(PlayerSculkStaffCDProvider.PLAYER_SCULK_STAFF_COOLDOWN).ifPresent(cooldown -> {
				ModMessages.sendToPlayer(new SculkStaffCDDataSyncS2CPacket(cooldown.getCooldown()), player);
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
				ModMessages.sendToPlayer(new IllusionForceDataSyncS2CPacket(
						illusionForce.getIllusionForce(),
						illusionForce.getEntPosX(),
						illusionForce.getEntPosY(),
						illusionForce.getEntPosZ(),
						false,
						illusionForce.getIsEnt()
				), player);
			});
			player.getCapability(PlayerSculkStaffCDProvider.PLAYER_SCULK_STAFF_COOLDOWN).ifPresent(cooldown -> {
				ModMessages.sendToPlayer(new SculkStaffCDDataSyncS2CPacket(cooldown.getCooldown()), player);
			});
			if (player.getMainHandItem().getItem() instanceof DaggerItem) {
				DaggerItem.addDaggerReachModifier(player);
			}
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
		if (event.isEndConquered()) return;
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
					illusionForce.setIllusionForce(player, 0, false, false);
					illusionForce.clearEntPosition(player);
					ModMessages.sendToPlayer(new IllusionForceDataSyncS2CPacket(0, 0.0f, 0.0f, 0.0f, false, false), player);
				});
				player.getCapability(PlayerSculkStaffCDProvider.PLAYER_SCULK_STAFF_COOLDOWN).ifPresent(sculkStaffCD -> {
					sculkStaffCD.setCooldown(player, 0);
					ModMessages.sendToPlayer(new SculkStaffCDDataSyncS2CPacket(0), player);
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

		if (event.getSource().getDirectEntity() instanceof AbstractArrow arrow &&
				arrow.getOwner() instanceof Player shooter &&
				!arrow.level().isClientSide) {
			CompoundTag arrowTag = arrow.getPersistentData();

			if (arrowTag.hasUUID("BoundPlayer") && arrowTag.getBoolean("LivingWoodArrow")) {
				LivingEntity target = event.getEntity();


				if (target == shooter) {
					return;
				}


				if (event.getAmount() <= 0) {
					return;
				}


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

		LivingEntity entity = event.getEntity();
		if (!entity.level().isClientSide && entity instanceof Player player) {
			if (player.isUsingItem() && player.getUseItem().getItem() instanceof GasbassItem) {
				boolean pvpCooldownDisabled = player.level().getGameRules().getBoolean(RPGworldMod.DISABLE_GASBASS_PVP_COOLDOWN);
				if (!pvpCooldownDisabled) {
					player.stopUsingItem();
					player.getCooldowns().addCooldown(ModItems.GASBASS.get(), 15);
				}
			}
			if (player.isUsingItem() && player.getUseItem().getItem() instanceof NetherStarScrollItem
					&& player.getUseItem().getEnchantmentLevel(ModEnchantments.ALTERATION.get()) > 0) {
				player.stopUsingItem();
				player.getCooldowns().addCooldown(ModItems.NETHER_STAR_SCROLL.get(), 15);
			}
		}

		if (!entity.level().isClientSide && entity.hasEffect(ModEffects.MIRRORING.get())) {
			EffectSyncHandler.generateAndSyncSeed(entity);
		}
	}

	@SubscribeEvent
	public static void onLivingUpdate(LivingEvent.LivingTickEvent event) {
		LivingEntity entity = event.getEntity();

		if (!entity.level().isClientSide && entity.tickCount % 20 == 0) {
			CompoundTag tag = entity.getPersistentData();
			if (tag.hasUUID("BoundPlayer") && tag.getBoolean("LivingWoodBound")) {
				Player player = entity.level().getPlayerByUUID(tag.getUUID("BoundPlayer"));
				if (player != null && !player.isRemoved()) {
					if (entity.distanceTo(player) > tag.getDouble("BoundPullRange")) {
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


		if (!entity.level().isClientSide && entity.tickCount % 5 == 0) {
			CompoundTag tag = entity.getPersistentData();
			if (tag.hasUUID("BoundPlayer") && tag.getBoolean("LivingWoodBound")) {
				Player player = entity.level().getPlayerByUUID(tag.getUUID("BoundPlayer"));

				if (player != null) {
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
					ModMessages.sendToNearbyPlayers(
							new BoundEntitySyncPacket(entity.getId()),
							entity.level(),
							entity.blockPosition(),
							300
					);
				}
			} else {
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
					if (serverPlayer.hasEffect(ModEffects.PARANOIA.get()))
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
						if (serverPlayer.hasEffect(ModEffects.PARANOIA.get()))
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
			} else {
				event.setCanBreathe(false);
				event.setCanRefillAir(false);
				event.setConsumeAirAmount(1);
				event.setRefillAirAmount(0);
			}
		}
	}

	@SubscribeEvent
	public void onEnderManDrown(LivingDropsEvent event) {
		if (!(event.getEntity() instanceof EnderMan enderMan)) return;


		if (enderMan.level().dimension() != Level.END) return;


		if (!enderMan.level().isRainingAt(enderMan.blockPosition())) return;


		if (!event.getSource().is(DamageTypes.DROWN)) return;


		ItemStack specialItem = new ItemStack(ModItems.MUSIC_DISC_RAIN_A_SIDE.get(), 1);
		ItemEntity drop = new ItemEntity(
				enderMan.level(),
				enderMan.getX(), enderMan.getY(), enderMan.getZ(),
				specialItem
		);
		event.getDrops().add(drop);
	}

	@SubscribeEvent
	public void onServerTick(TickEvent.ServerTickEvent event) {
		if (event.phase == TickEvent.Phase.END) {
			int counter = event.getServer().getTickCount() % 20;
			if (counter == 0) {
				for (ServerPlayer player : event.getServer().getPlayerList().getPlayers()) {
					if (player.level().isClientSide) continue;

					boolean hasContainerOpen = player.containerMenu != player.inventoryMenu;

					if (!hasContainerOpen) {
						HornCoralStaffItem.removeStaffReachModifier(player);
					}
					if (!(player.getMainHandItem().getItem() instanceof DaggerItem) && !(player.isUsingItem() && player.getUseItem().getItem() instanceof DaggerItem)) {
						DaggerItem.removeDaggerReachModifier(player);
					}
				}
			}
		}
	}

	@SubscribeEvent
	public void onContainerClose(PlayerContainerEvent.Close event) {
		Player player = event.getEntity();
		AbstractContainerMenu menu = event.getContainer();
		Level level = player.level();

		if (!player.level().isClientSide) {
			HornCoralStaffItem.removeStaffReachModifier(player);

		}

		if (level.isClientSide) return;


		BlockPos pos = null;
		BlockPos pos2 = null;

		if (menu instanceof ChestMenu chestMenu) {
			Container container = chestMenu.getContainer();
			if (container instanceof BaseContainerBlockEntity blockEntity) {
				pos = blockEntity.getBlockPos();
			} else if (container instanceof PlayerEnderChestContainer) {
				long savedPos = player.getPersistentData().getLong("RPGLastEnderChestPos");
				if (savedPos != 0L) {
					pos = BlockPos.of(savedPos);
					player.getPersistentData().remove("RPGLastEnderChestPos");
				}
			} else if (container instanceof CompoundContainer compoundContainer) {
				Container container1 = compoundContainer.container1;
				if (container1 instanceof BaseContainerBlockEntity blockEntity) {
					pos = blockEntity.getBlockPos();
				}
				Container container2 = compoundContainer.container2;
				if (container2 instanceof BaseContainerBlockEntity blockEntity) {
					pos2 = blockEntity.getBlockPos();
				}
			}
		}

		if (pos != null) {
			RemoteOpenContainerRegistry.removeOpener(level, pos, player);
		}

		if (pos2 != null) {
			RemoteOpenContainerRegistry.removeOpener(level, pos2, player);
		}
	}

	@SubscribeEvent
	public void onLevelUnload(LevelEvent.Unload event) {
		Level level = (Level) event.getLevel();
		if (!level.isClientSide()) {
			RemoteOpenContainerRegistry.removeAllForLevel(level);
		}
	}

	@SubscribeEvent
	public static void onLivingDeath(LivingDeathEvent event) {
		if (event.isCanceled()) return;
		if (!(event.getEntity() instanceof Player player)) return;
		if (player.level().isClientSide) return;

		if (!player.isUsingItem()) return;
		if (event.getSource().is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
			return;
		}
		ItemStack usingItem = player.getUseItem();
		if (usingItem.getItem() instanceof EnderEyeScrollItem
				&& EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.RESTORATION.get(), usingItem) > 0) {
			double radius = 32.0D;
			AABB aabb = player.getBoundingBox().inflate(radius);
			List<EndCrystal> crystals = player.level().getEntitiesOfClass(EndCrystal.class, aabb);
			boolean hasCrystal = crystals.stream().anyMatch(EndCrystal::isAlive);
			if (!hasCrystal) {
				return;
			}

			event.setCanceled(true);

			player.setHealth(1.0F);
			player.removeAllEffects();
			player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 225, 1));
			player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 100, 1));
			player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 225, 0));
			if (player instanceof ServerPlayer serverPlayer) {
				ModAdvancements.SPELL_RESTORATION_ENDER_EYE_TRIGGER.trigger(serverPlayer);
			}

			for (EndCrystal crystal : crystals) {
				if (crystal.isAlive()) {
					crystal.hurt(player.level().damageSources().playerAttack(player), Float.MAX_VALUE);
				}
			}

			player.getCooldowns().addCooldown(usingItem.getItem(), 15);
		} else if (usingItem.getItem() instanceof PillagerScrollItem
				&& EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.RESTORATION.get(), usingItem) > 0) {
			AtomicBoolean hasEnoughMana = new AtomicBoolean(false);
			player.getCapability(PlayerManaProvider.PLAYER_MANA).ifPresent(mana -> {
				if (mana.getMana() >= 100) {
					mana.reduceMana((ServerPlayer) player, mana.getMana());
					hasEnoughMana.set(true);
				}
			});
			if (!hasEnoughMana.get()) return;

			event.setCanceled(true);

			player.setHealth(1.0F);
			player.removeAllEffects();
			player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 900, 1));
			player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 100, 1));
			player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 800, 0));
			if (player instanceof ServerPlayer serverPlayer) {
				ModAdvancements.SPELL_RESTORATION_PILLAGER_TRIGGER.trigger(serverPlayer);
			}
			ModMessages.sendToNearbyPlayers(
					new TotemEffectPacket(player.getId(), usingItem.copy()),
					player.level(),
					player.blockPosition(),
					64.0
			);
			player.getCooldowns().addCooldown(usingItem.getItem(), 15);
			if (player.level() instanceof ServerLevel serverLevel) {
				PillagerScrollItem.getPlayerUseData(serverLevel).remove(player.getUUID());
				PillagerScrollItem.stopPlayerUse(serverLevel, player, usingItem, false);
			}
		}
	}

	@SubscribeEvent
	public static void onBlockBreak(BlockEvent.BreakEvent event) {
		Player player = event.getPlayer();
		if (player == null) return;

		ItemStack mainHand = player.getMainHandItem();

		ItemStack usingStack;

		if (mainHand.getItem() instanceof NetherStarScrollItem scroll) {
			usingStack = mainHand;
		} else return;

		if (scroll.isPickaxeMode(usingStack) && !player.isShiftKeyDown()) {
			event.setExpToDrop(0);
		}
	}

	@SubscribeEvent
	public static void onVanillaGameEvent(VanillaGameEvent event) {
		if (event.getVanillaEvent() == GameEvent.ITEM_INTERACT_FINISH) {
			if (event.getCause() instanceof Player player) {
				ItemStack usedItem = player.getUseItem();
				if (usedItem.getItem() instanceof WealdBladeItem
						|| usedItem.getItem() instanceof StaffItem
						|| usedItem.getItem() instanceof ScrollItem) {
					event.setCanceled(true);
				}
			}
		}
	}

	@SubscribeEvent
	public static void onItemCrafted(PlayerEvent.ItemCraftedEvent event) {
		if (event.getCrafting().getItem() instanceof BlockItem blockItem && event.getInventory().countItem(ModItems.PILLAGER_SCROLL.get()) == 1) {
			if (blockItem.getDefaultInstance().is(ItemTags.WOOL) || blockItem.getDefaultInstance().is(ItemTags.WOOL_CARPETS)) {
				Player player = event.getEntity();
				if (!player.getAbilities().instabuild) {
					player.getCapability(PlayerManaProvider.PLAYER_MANA).ifPresent(mana -> {
						if (mana.getMana() >= 1) {
							mana.reduceMana((ServerPlayer) player, 1);
						}
					});
				}
			}
		}
	}

	@SubscribeEvent
	public static void onEquipmentChange(LivingEquipmentChangeEvent event) {
		if (!(event.getEntity() instanceof Player player) || player.level().isClientSide) {
			return;
		}

		if (event.getSlot() == EquipmentSlot.MAINHAND) {
			ItemStack newItem = event.getTo();
			ItemStack oldItem = event.getFrom();

			if (newItem.getItem() instanceof DaggerItem) {
				DaggerItem.addDaggerReachModifier(player);
			} else if (oldItem.getItem() instanceof DaggerItem && !(player.isUsingItem() && player.getUseItem().getItem() instanceof DaggerItem)) {
				DaggerItem.removeDaggerReachModifier(player);
			}
		}
	}

	@SubscribeEvent
	public static void onCriticalHit(CriticalHitEvent event) {
		Player player = event.getEntity();
		ItemStack stack = player.getItemInHand(InteractionHand.MAIN_HAND);
		Entity target = event.getTarget();
		if (stack.getItem() instanceof IgniteOnCritItem && event.isVanillaCritical()) {
			int fortuneLevel = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.BLOCK_FORTUNE, stack);

			float baseChance = 0.10f;
			float chancePerLevel = 0.3f;

			float totalChance = baseChance + (fortuneLevel * chancePerLevel);

			totalChance = Math.min(totalChance, 1f) * 5;

			float chance = target.level().getRandom().nextFloat();

			if (chance < totalChance && !target.fireImmune()) {
				int fireDuration = 5 + (fortuneLevel * 5);
				target.setSecondsOnFire(fireDuration);
			}
		}
	}

}
