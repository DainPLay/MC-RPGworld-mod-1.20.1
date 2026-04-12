package net.dainplay.rpgworldmod.item.custom;

import net.dainplay.rpgworldmod.enchantment.ModEnchantments;
import net.dainplay.rpgworldmod.network.ClientSculkStaffCDData;
import net.dainplay.rpgworldmod.network.LoopSoundPacket;
import net.dainplay.rpgworldmod.network.ModMessages;
import net.dainplay.rpgworldmod.network.PlayerSculkStaffCDProvider;
import net.dainplay.rpgworldmod.network.PullDownPlayerPacket;
import net.dainplay.rpgworldmod.sounds.RPGSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.gameevent.EntityPositionSource;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEventListener;
import net.minecraft.world.level.gameevent.GameEventListenerRegistry;
import net.minecraft.world.level.gameevent.PositionSource;
import net.minecraft.world.level.gameevent.vibrations.VibrationInfo;
import net.minecraft.world.level.gameevent.vibrations.VibrationSystem;
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.particles.VibrationParticleOption;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

public class SculkStaffItem extends StaffItem {

	// Используем обычную мапу, т.к. системы добавляются и удаляются синхронно
	private static final Map<Player, SculkStaffVibrationSystem> ACTIVE_SYSTEMS = new WeakHashMap<>();

	public SculkStaffItem(Properties properties) {
		super(properties);
	}

	@Override
	public String getFirstPredicate(ItemStack item) {
		return Minecraft.getInstance().options.keyAttack.getKey().getDisplayName().getString();
	}

	@Override
	public void onUseTick(Level pLevel, LivingEntity pLivingEntity, ItemStack pStack, int pRemainingUseDuration) {
		// ... Ваш существующий код обработки caughtVibration, кулдаунов и т.д. без изменений ...

		if (pStack.getTag() != null && pStack.getTag().contains("caughtVibration", Tag.TAG_INT) && pStack.getTag().getInt("caughtVibration") > 0) {
			CompoundTag nbtData = pStack.getTag();
			if (pStack.getTag().getInt("caughtVibration") == 0) {
				nbtData.remove("caughtVibration");
			} else {
				nbtData.putInt("caughtVibration", nbtData.getInt("caughtVibration") - 1);
			}
			pStack.setTag(nbtData);
		}
		int activeRechargeLevel = pStack.getEnchantmentLevel(ModEnchantments.ACTIVE_RECHARGE.get());
		if (activeRechargeLevel > 0 && pLivingEntity instanceof Player player && !isOffCooldown(pStack, player)) {
			Map<Item, ItemCooldowns.CooldownInstance> cooldownsMap = player.getCooldowns().cooldowns;
			ItemCooldowns.CooldownInstance instance = cooldownsMap.get(pStack.getItem());
			if (instance == null) return;
			int startTick = instance.startTime;
			int endTick = instance.endTime;
			int currentTick = player.getCooldowns().tickCount;
			int divider = 0;
			if (pStack.getEnchantmentLevel(ModEnchantments.DOUBLE_EXPOSURE.get()) > 0) {
				divider += getMaxCooldown(pStack);
			}
			if (endTick - currentTick <= activeRechargeLevel + 1 + divider) {
				if (!pLevel.isClientSide) {
					ModMessages.sendToNearbyPlayers(new LoopSoundPacket(player.getId(), false, pStack), pLevel, player.blockPosition(), 64.0);
					pLevel.playSound(null, player.getX(), player.getY(), player.getZ(), RPGSounds.STAFF_STOP.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
				}
				if (pLevel.isClientSide && pLivingEntity instanceof LocalPlayer)
					ClientSculkStaffCDData.set(endTick - currentTick);
				player.stopUsingItem();
			}
			if (endTick - currentTick <= activeRechargeLevel + divider) return;
			cooldownsMap.remove(pStack.getItem());
			cooldownsMap.put(pStack.getItem(), new ItemCooldowns.CooldownInstance(startTick, endTick - activeRechargeLevel));
		} else {
			// --- ИЗМЕНЕНИЕ: Тикаем нашу кастомную систему вместо VibrationSystem.Ticker.tick ---
			if (!pLevel.isClientSide && pLivingEntity instanceof Player player) {
				SculkStaffVibrationSystem system = ACTIVE_SYSTEMS.get(player);
				if (system != null && player.isUsingItem() && player.getUseItem() == pStack) {
					system.tick((ServerLevel) pLevel);
				}
			}
			// Конец изменения

			if (pLivingEntity instanceof Player player) {
				if (!pLevel.isClientSide) {
					player.getCapability(PlayerSculkStaffCDProvider.PLAYER_SCULK_STAFF_COOLDOWN).ifPresent(cooldown -> {
						int useTick = player.getTicksUsingItem();
						if (pStack.getTag() != null && pStack.getTag().contains("startingCooldown", Tag.TAG_INT) && pStack.getOrCreateTag().getInt("startingCooldown") > 0) {
							useTick += pStack.getTag().getInt("startingCooldown");
						}
						int resultTick;
						if (pStack.getEnchantmentLevel(ModEnchantments.DOUBLE_EXPOSURE.get()) > 0) {
							if (useTick <= getMaxCooldown(pStack))
								resultTick = useTick;
							else
								resultTick = getMaxCooldown(pStack) + (useTick - getMaxCooldown(pStack)) * 2;
						} else
							resultTick = useTick;
						cooldown.setCooldown((ServerPlayer) player, resultTick);
						if (pStack.getEnchantmentLevel(ModEnchantments.DOUBLE_EXPOSURE.get()) > 0) {
							if (useTick >= getMaxCooldown(pStack) * 2) {
								ModMessages.sendToNearbyPlayers(new LoopSoundPacket(player.getId(), false, pStack), pLevel, player.blockPosition(), 64.0);
								pLevel.playSound(null, player.getX(), player.getY(), player.getZ(), RPGSounds.STAFF_STOP.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
								player.stopUsingItem();
							}
						} else {
							if (useTick >= getMaxCooldown(pStack)) {
								ModMessages.sendToNearbyPlayers(new LoopSoundPacket(player.getId(), false, pStack), pLevel, player.blockPosition(), 64.0);
								pLevel.playSound(null, player.getX(), player.getY(), player.getZ(), RPGSounds.STAFF_STOP.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
								player.stopUsingItem();
							}
						}
					});
				} else if (pLivingEntity instanceof LocalPlayer) {
					int useTick = player.getTicksUsingItem();
					if (pStack.getTag() != null && pStack.getTag().contains("startingCooldown", Tag.TAG_INT) && pStack.getOrCreateTag().getInt("startingCooldown") > 0) {
						useTick += pStack.getTag().getInt("startingCooldown");
					}
					int resultTick;
					if (pStack.getEnchantmentLevel(ModEnchantments.DOUBLE_EXPOSURE.get()) > 0) {
						if (useTick <= getMaxCooldown(pStack))
							resultTick = useTick;
						else
							resultTick = getMaxCooldown(pStack) + (useTick - getMaxCooldown(pStack)) * 2;
					} else
						resultTick = useTick;
					ClientSculkStaffCDData.set(resultTick);
					if (pStack.getEnchantmentLevel(ModEnchantments.DOUBLE_EXPOSURE.get()) > 0) {
						if (useTick >= getMaxCooldown(pStack) * 2) player.stopUsingItem();
					} else {
						if (useTick >= getMaxCooldown(pStack)) player.stopUsingItem();
					}
				}
			}
		}
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		ItemStack itemstack = player.getItemInHand(hand);
		int activeRechargeLevel = itemstack.getEnchantmentLevel(ModEnchantments.ACTIVE_RECHARGE.get());
		int doubleExposureLevel = itemstack.getEnchantmentLevel(ModEnchantments.DOUBLE_EXPOSURE.get());

		if (activeRechargeLevel <= 0 && doubleExposureLevel <= 0 && player.getCooldowns().getCooldownPercent(itemstack.getItem(), 0.0F) > 0.0F)
			return InteractionResultHolder.pass(itemstack);
		if (player.getCooldowns().getCooldownPercent(itemstack.getItem(), 0.0F) > 0.0F) {
			Map<Item, ItemCooldowns.CooldownInstance> cooldownsMap = player.getCooldowns().cooldowns;
			int currentTick = player.getCooldowns().tickCount;
			ItemCooldowns.CooldownInstance instance = cooldownsMap.get(itemstack.getItem());
			if (instance != null) {
				int startTick = instance.startTime;
				if (currentTick - startTick <= 15 && !isOffCooldown(itemstack, player))
					return InteractionResultHolder.pass(itemstack);
			}
		}

		int startCooldown = 0;
		if (doubleExposureLevel > 0) {
			if (player.getCooldowns().getCooldownPercent(itemstack.getItem(), 0.0F) > 0.0F) {
				Map<Item, ItemCooldowns.CooldownInstance> cooldownsMap = player.getCooldowns().cooldowns;
				int currentTick = player.getCooldowns().tickCount;
				ItemCooldowns.CooldownInstance instance = cooldownsMap.get(itemstack.getItem());
				if (instance != null) {
					int endTick = instance.endTime;
					if (endTick - currentTick > getMaxCooldown(itemstack) && activeRechargeLevel <= 0)
						return InteractionResultHolder.pass(itemstack);
					if (isOffCooldown(itemstack, player)) {
						startCooldown = endTick - currentTick;
						player.getCooldowns().addCooldown(this, 0);
					}
				}
			}
		}

		CompoundTag nbtData = itemstack.getOrCreateTag();
		nbtData.putInt("startingCooldown", startCooldown);
		nbtData.putInt("caughtVibration", 0);
		itemstack.setTag(nbtData);

		if (isOffCooldown(itemstack, player)) {
			if (itemstack.getDamageValue() >= itemstack.getMaxDamage() - 1)
				return InteractionResultHolder.fail(itemstack);
			else
				itemstack.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(player.getUsedItemHand()));
		}

		if (!level.isClientSide) {
			level.playSound(null, player.getX(), player.getY(), player.getZ(), RPGSounds.STAFF_START.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
			ModMessages.sendToNearbyPlayers(new LoopSoundPacket(player.getId(), true, itemstack), (ServerLevel) level, player.blockPosition(), 64.0);

			// Создаём и регистрируем новую систему
			SculkStaffVibrationSystem system = new SculkStaffVibrationSystem(player, itemstack);
			ACTIVE_SYSTEMS.put(player, system);
			registerListener((ServerLevel) level, system);
		}

		player.startUsingItem(hand);
		return InteractionResultHolder.consume(itemstack);
	}

	@Override
	public void releaseUsing(ItemStack stack, Level level, LivingEntity livingEntity, int timeCharged) {
		if (livingEntity instanceof Player player && !level.isClientSide) {
			SculkStaffVibrationSystem system = ACTIVE_SYSTEMS.remove(player);
			if (system != null) {
				unregisterListener((ServerLevel) level, system);
			}
			ModMessages.sendToNearbyPlayers(new LoopSoundPacket(player.getId(), false, stack), level, player.blockPosition(), 64.0);
			level.playSound(null, player.getX(), player.getY(), player.getZ(), RPGSounds.STAFF_STOP.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
		}
		super.releaseUsing(stack, level, livingEntity, timeCharged);
	}

	@Override
	public int getMaxCooldown(ItemStack item) {
		return 300;
	}

	@Override
	public float getX(ItemStack stack, Entity entity, boolean righthand) {
		return -0.065F;
	}

	@Override
	public float get1XOffset(ItemStack stack, Entity entity, boolean righthand) {
		return 0.225F;
	}

	@Override
	public float get1YOffset(ItemStack stack, Entity entity, boolean righthand) {
		if (getGemType(stack) == GemType.EMBER_GEM)
			return 0.65F;
		else if (getGemType(stack) == GemType.HEART_OF_THE_SEA)
			return 0.55F;
		else if (getGemType(stack) == GemType.NETHER_STAR)
			return 0.55F;
		else if (getGemType(stack) == GemType.ENDER_EYE)
			return 0.55F;
		else
			return 0.55F;
	}

	public boolean isValidRepairItem(ItemStack pToRepair, ItemStack pRepair) {
		return pRepair.is(Items.SCULK) || super.isValidRepairItem(pToRepair, pRepair);
	}

	public float getY(ItemStack stack, Entity entity, boolean rightHand) {
		return 0.55F;
	}

	private void registerListener(ServerLevel level, SculkStaffVibrationSystem system) {
		BlockPos pos = system.getRegisteredPos();
		if (pos == null) {
			pos = system.getOwner().blockPosition();
			system.setRegisteredPos(pos);
		}
		LevelChunk chunk = (LevelChunk) level.getChunk(pos);
		int sectionY = SectionPos.blockToSectionCoord(pos.getY());
		GameEventListenerRegistry registry = chunk.getListenerRegistry(sectionY);
		registry.register(system.getListener());
	}

	private void unregisterListener(ServerLevel level, SculkStaffVibrationSystem system) {
		BlockPos pos = system.getRegisteredPos();
		if (pos == null) return;
		LevelChunk chunk = (LevelChunk) level.getChunk(pos);
		int sectionY = SectionPos.blockToSectionCoord(pos.getY());
		GameEventListenerRegistry registry = chunk.getListenerRegistry(sectionY);
		registry.unregister(system.getListener());
	}

	// -------------------------------------------------------------------------
	// НОВАЯ РЕАЛИЗАЦИЯ: система, поддерживающая множество параллельных вибраций
	// -------------------------------------------------------------------------
	private static class SculkStaffVibrationSystem {
		private final Player owner;
		private final ItemStack staffStack;
		private final List<ActiveVibration> activeVibrations = new ArrayList<>();
		private final VibrationUser vibrationUser;
		private final CustomListener listener;
		private BlockPos registeredPos;

		public SculkStaffVibrationSystem(Player owner, ItemStack staffStack) {
			this.owner = owner;
			this.staffStack = staffStack;
			this.vibrationUser = new VibrationUser();
			this.listener = new CustomListener(this);
		}

		public Player getOwner() {
			return owner;
		}

		public void setRegisteredPos(BlockPos pos) {
			this.registeredPos = pos;
		}

		public BlockPos getRegisteredPos() {
			return registeredPos;
		}

		public GameEventListener getListener() {
			return listener;
		}

		public void tick(ServerLevel level) {
			Iterator<ActiveVibration> it = activeVibrations.iterator();
			while (it.hasNext()) {
				ActiveVibration vib = it.next();
				// Уменьшаем время и при необходимости отправляем частицу движения
				vib.decrementTime();
				if (vib.getTicksLeft() > 0) {
					sendVibrationParticle(level, vib);
				}
				if (vib.isDone()) {
					// Применяем эффект
					BlockPos sourcePos = BlockPos.containing(vib.getInfo().pos());
					vibrationUser.onReceiveVibration(
							level,
							sourcePos,
							vib.getInfo().gameEvent(),
							vib.getInfo().getEntity(level).orElse(null),
							vib.getInfo().getProjectileOwner(level).orElse(null),
							VibrationSystem.Listener.distanceBetweenInBlocks(sourcePos,
									vibrationUser.getPositionSource().getPosition(level).map(BlockPos::containing).orElse(sourcePos))
					);
					it.remove();
				}
			}
		}

		public void addVibration(VibrationInfo info, ServerLevel level) {
			int travelTime = vibrationUser.calculateTravelTimeInTicks(info.distance());
			activeVibrations.add(new ActiveVibration(info, travelTime));
			// Стартовая частица (как в ваниле)
			level.sendParticles(
					new VibrationParticleOption(vibrationUser.getPositionSource(), travelTime),
					info.pos().x, info.pos().y, info.pos().z,
					1, 0.0, 0.0, 0.0, 0.0
			);
		}

		private void sendVibrationParticle(ServerLevel level, ActiveVibration vib) {
			VibrationInfo info = vib.getInfo();
			Vec3 start = info.pos();
			Vec3 end = vibrationUser.getPositionSource().getPosition(level).orElse(start);
			float progress = 1.0F - (float) vib.getTicksLeft() / vib.getTotalTicks();
			double x = Mth.lerp(progress, start.x, end.x);
			double y = Mth.lerp(progress, start.y, end.y);
			double z = Mth.lerp(progress, start.z, end.z);
			level.sendParticles(
					new VibrationParticleOption(vibrationUser.getPositionSource(), vib.getTicksLeft()),
					x, y, z,
					1, 0.0, 0.0, 0.0, 0.0
			);
		}

		// ---------------------------------------------------------------------
		// Вспомогательные классы
		// ---------------------------------------------------------------------
		private static class ActiveVibration {
			private final VibrationInfo info;
			private int ticksLeft;
			private final int totalTicks;

			public ActiveVibration(VibrationInfo info, int totalTicks) {
				this.info = info;
				this.ticksLeft = totalTicks;
				this.totalTicks = totalTicks;
			}

			public void decrementTime() {
				ticksLeft--;
			}

			public boolean isDone() {
				return ticksLeft <= 0;
			}

			public VibrationInfo getInfo() {
				return info;
			}

			public int getTicksLeft() {
				return ticksLeft;
			}

			public int getTotalTicks() {
				return totalTicks;
			}
		}

		private class VibrationUser implements VibrationSystem.User {
			private final PositionSource positionSource;

			public VibrationUser() {
				this.positionSource = new EntityPositionSource(owner, owner.getBbHeight() / 2);
			}

			@Override
			public int getListenerRadius() {
				return 24;
			}

			@Override
			public PositionSource getPositionSource() {
				return positionSource;
			}

			@Override
			public boolean canReceiveVibration(ServerLevel level, BlockPos pos, GameEvent event, @Nullable GameEvent.Context context) {
				if (!owner.isUsingItem() || owner.getUseItem() != staffStack || owner.isDeadOrDying()) {
					return false;
				}
				if (staffStack.getItem() instanceof SculkStaffItem staffItem && !staffItem.isOffCooldown(staffStack, owner))
					return false;
				if (event == GameEvent.ENTITY_DAMAGE) return false;
				if (context != null && context.sourceEntity() == owner) {
					return false;
				}
				if (context != null && context.sourceEntity() instanceof Player player && player.getAbilities().instabuild)
					return false;
				return true;
			}

			@Override
			public void onReceiveVibration(ServerLevel level, BlockPos pos, GameEvent event,
										   @Nullable Entity sourceEntity, @Nullable Entity projectileOwner, float distance) {
				LivingEntity target = null;
				if (sourceEntity instanceof LivingEntity living) {
					target = living;
				} else if (projectileOwner instanceof LivingEntity living) {
					target = living;
				}

				if (target != null && !target.isRemoved() && target.isAlive() && !(target instanceof Player player && player.getAbilities().instabuild)) {
					switch (getGemType(staffStack)) {
						case EMBER_GEM: {
							target.level().playSound(null,
									target.getX(), target.getY(), target.getZ(),
									RPGSounds.STAFF_EMBER_GEM_CAST.get(),
									SoundSource.PLAYERS, 1.0F, 1.0F
							);
							target.setSecondsOnFire(5);
						}
						break;
						case ENDER_EYE: {
							target.level().playSound(null,
									target.getX(), target.getY(), target.getZ(),
									RPGSounds.STAFF_ENDER_EYE_CAST.get(),
									SoundSource.PLAYERS, 1.0F, 1.0F
							);
							target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 200));
						}
						break;
						case HEART_OF_THE_SEA: {
							target.level().playSound(null,
									target.getX(), target.getY(), target.getZ(),
									RPGSounds.STAFF_HEART_OF_THE_SEA_SCULK.get(),
									SoundSource.PLAYERS, 1.0F, 1.0F
							);
							if (target instanceof ServerPlayer player)
								ModMessages.sendToPlayer(new PullDownPlayerPacket(), player);
							else {
								if (!target.isPassenger()) {
									Vec3 vec3 = target.getDeltaMovement();
									target.setDeltaMovement(vec3.x, vec3.y - 10D, vec3.z);
								}
							}
						}
						break;
						case NETHER_STAR: {
							target.level().playSound(null,
									target.getX(), target.getY(), target.getZ(),
									RPGSounds.STAFF_NETHER_STAR_SCULK.get(),
									SoundSource.PLAYERS, 1F, 1.0F
							);
							target.addEffect(new MobEffectInstance(MobEffects.WITHER, 200, 0));
						}
						break;
					}

					level.gameEvent(owner, GameEvent.SCULK_SENSOR_TENDRILS_CLICKING, owner.blockPosition());
					CompoundTag nbtData = staffStack.getOrCreateTag();
					nbtData.putInt("caughtVibration", 30);
					staffStack.setTag(nbtData);
					level.playSound(null, owner.getX(), owner.getY(), owner.getZ(),
							SoundEvents.SCULK_CLICKING, SoundSource.PLAYERS,
							1.0F, level.random.nextFloat() * 0.2F + 0.8F);
				}
			}

			@Override
			public boolean requiresAdjacentChunksToBeTicking() {
				return false;
			}
		}

		private static class CustomListener implements GameEventListener {
			private final SculkStaffVibrationSystem system;

			public CustomListener(SculkStaffVibrationSystem system) {
				this.system = system;
			}

			@Override
			public PositionSource getListenerSource() {
				return system.vibrationUser.getPositionSource();
			}

			@Override
			public int getListenerRadius() {
				return system.vibrationUser.getListenerRadius();
			}

			@Override
			public boolean handleGameEvent(ServerLevel level, GameEvent event, GameEvent.Context context, Vec3 pos) {
				VibrationSystem.User user = system.vibrationUser;
				if (!user.isValidVibration(event, context)) return false;

				Optional<Vec3> userPosOpt = user.getPositionSource().getPosition(level);
				if (userPosOpt.isEmpty()) return false;
				Vec3 userPos = userPosOpt.get();

				if (!user.canReceiveVibration(level, BlockPos.containing(pos), event, context))
					return false;

				// Проверка на преграду (как в ванильном VibrationSystem.Listener.isOccluded)
				if (VibrationSystem.Listener.isOccluded(level, pos, userPos))
					return false;

				float distance = (float) pos.distanceTo(userPos);
				VibrationInfo info = new VibrationInfo(event, distance, pos, context.sourceEntity());
				system.addVibration(info, level);
				return true;
			}
		}
	}

	@Override
	public float getZ(ItemStack stack, Entity entity, boolean rightHand) {
		if (getGemType(stack) == GemType.EMBER_GEM)
			return -1.025F;
		else if (getGemType(stack) == GemType.HEART_OF_THE_SEA)
			return -0.925F;
		else if (getGemType(stack) == GemType.NETHER_STAR)
			return -0.925F;
		else if (getGemType(stack) == GemType.ENDER_EYE)
			return -0.925F;
		else
			return -0.925F;
	}
}