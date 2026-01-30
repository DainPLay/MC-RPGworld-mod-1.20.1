package net.dainplay.rpgworldmod.item.custom;

import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.dainplay.rpgworldmod.block.ModBlocks;
import net.dainplay.rpgworldmod.enchantment.ModEnchantments;
import net.dainplay.rpgworldmod.item.ModItems;
import net.dainplay.rpgworldmod.network.ClientManaData;
import net.dainplay.rpgworldmod.network.EmberScrollLoopSoundPacket;
import net.dainplay.rpgworldmod.network.ModMessages;
import net.dainplay.rpgworldmod.network.PlayerManaProvider;
import net.dainplay.rpgworldmod.network.UpdateItemTagMessage;
import net.dainplay.rpgworldmod.particle.ModParticles;
import net.dainplay.rpgworldmod.sounds.RPGSounds;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.world.item.FlintAndSteelItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.TntBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.LavaFluid;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class EmberScrollItem extends ScrollItem implements IClientItemExtensions {

	// Хранилище активных снарядов с привязкой к уровню
	private static final Map<Level, Map<UUID, EmberProjectileData>> activeProjectiles = new HashMap<>();

	// Хранилище для отслеживания использования игроком с привязкой к уровню
	private static final Map<Level, Map<UUID, PlayerUseData>> playerUseData = new HashMap<>();

	public EmberScrollItem(Properties pProperties) {
		super(pProperties);
		MinecraftForge.EVENT_BUS.register(this);
	}

	@Override
	public String getTexture(ItemStack stack, Entity entity) {
		if (entity instanceof Player player && player.isUsingItem() && player.getUseItem() == stack) {
			return "textures/entity/spells/fire";
		}
		return "textures/entity/spells/spark";
	}

	@Override
	public float get1YOffset(ItemStack stack, Entity entity) {
		if (entity instanceof Player player && player.isUsingItem() && player.getUseItem() == stack) {
			return 0.25F;
		}
		return 0F;
	}

	@Override
	public float getZOffset(ItemStack stack, Entity entity) {
		if (entity instanceof Player player && player.isUsingItem() && player.getUseItem() == stack) {
			if(stack.getEnchantmentLevel(ModEnchantments.DESTRUCTION.get()) > 0) {
				return 0.1F;
			}
			if(stack.getEnchantmentLevel(ModEnchantments.RESTORATION.get()) > 0) {
				return 0.2F;
			}
			if(stack.getEnchantmentLevel(ModEnchantments.ALTERATION.get()) > 0) {
				return 0.2F;
			}
		}
		return 0.05F;
	}

	@Override
	public float getZ(ItemStack stack, Entity entity) {
		if (entity instanceof Player player && player.isUsingItem() && player.getUseItem() == stack) {
			if(stack.getEnchantmentLevel(ModEnchantments.RESTORATION.get()) > 0) {
				return 0F;
			}
			if(stack.getEnchantmentLevel(ModEnchantments.ALTERATION.get()) > 0) {
				return 0F;
			}
		}
		return -0.15F;
	}

	@Override
	public PoseStack getUsingPose(ItemStack stack, Player player, PoseStack poseStack, float flip) {
		if(stack.getEnchantmentLevel(ModEnchantments.DESTRUCTION.get()) > 0) {
			poseStack.mulPose(Axis.ZP.rotationDegrees(flip * -10.0F));
			poseStack.mulPose(Axis.YP.rotationDegrees((-(float)Math.PI / 6F)));
			poseStack.translate(0F, 0.25F, 0F);
			float shakeRotY = (float) (Math.cos(player.getTicksUsingItem() * 1.5) * 0.3F);
			poseStack.mulPose(Axis.YP.rotationDegrees(shakeRotY));
		}
		if(stack.getEnchantmentLevel(ModEnchantments.RESTORATION.get()) > 0) {
			poseStack.mulPose(Axis.ZP.rotationDegrees(flip * 36.0F));
			poseStack.mulPose(Axis.XP.rotationDegrees(-7.0F));
			poseStack.mulPose(Axis.YP.rotationDegrees((-(float)Math.PI / 6F)));
			poseStack.translate(0F, 0.4F, 0F);
			float shakeRotY = (float) (Math.cos(player.getTicksUsingItem() * 1.5) * 0.3F);
			poseStack.mulPose(Axis.YP.rotationDegrees(shakeRotY));
		}
		if(stack.getEnchantmentLevel(ModEnchantments.ALTERATION.get()) > 0) {
			poseStack.mulPose(Axis.ZP.rotationDegrees(flip * 36.0F));
			poseStack.mulPose(Axis.XP.rotationDegrees(-7.0F));
			poseStack.mulPose(Axis.YP.rotationDegrees((-(float)Math.PI / 6F)));
			poseStack.translate(0F, 0.4F, 0F);
			float shakeRotY = (float) (Math.cos(player.getTicksUsingItem() * 1.5) * 0.3F);
			poseStack.mulPose(Axis.YP.rotationDegrees(shakeRotY));
		}
		return poseStack;
	}

	@Override
	public PoseStack getEffectUsingPose(ItemStack stack, Player player, PoseStack poseStack, float flip) {
		if(stack.getEnchantmentLevel(ModEnchantments.DESTRUCTION.get()) > 0) {
			poseStack.translate(flip*0.05F, 0.05F, -0.31F);
		}
		if(stack.getEnchantmentLevel(ModEnchantments.RESTORATION.get()) > 0) {
			poseStack.translate(flip*0.35F, 0.5F, 0F);
		}
		if(stack.getEnchantmentLevel(ModEnchantments.ALTERATION.get()) > 0) {
			poseStack.translate(flip*0.35F, 0.5F, 0F);
		}

		return poseStack;
	}

	@Override
	public int getAnimationSpeed(ItemStack stack, Entity entity) {
		if (entity instanceof Player player && player.isUsingItem() && player.getUseItem() == stack) {
			return 1;
		}
		return 2;
	}

	@Override
	public int getAnimationLength(ItemStack stack, Entity entity) {
		if (entity instanceof Player player && player.isUsingItem() && player.getUseItem() == stack) {
			return 32;
		}
		return 6;
	}

	@Override
	public void initializeClient(Consumer<IClientItemExtensions> consumer)
	{
		consumer.accept(new IClientItemExtensions() {

			private static final HumanoidModel.ArmPose DESTRUCTION_POSE = HumanoidModel.ArmPose.create("DESTRUCTION", false, (model, entity, arm) -> {
				if (arm == HumanoidArm.RIGHT) {
					model.rightArm.xRot = (-(float)Math.PI / 2F) + model.head.xRot;
					model.rightArm.yRot = -0.1F + model.head.yRot;
				} else {
					model.leftArm.xRot = (-(float)Math.PI / 2F) + model.head.xRot;
					model.leftArm.yRot = 0.1F + model.head.yRot;
				}
			});

			private static final HumanoidModel.ArmPose ACTIVE_USE_POSE = HumanoidModel.ArmPose.create("ACTIVE_USE", false, (model, entity, arm) -> {
				if (arm == HumanoidArm.RIGHT) {
					model.rightArm.xRot = model.rightArm.xRot * 0.5F - ((float)Math.PI * 0.8F);
					model.rightArm.zRot = model.rightArm.zRot * 0.5F - ((float)Math.PI * 0.1F);
					model.rightArm.yRot = 0F;
				} else {
					model.leftArm.xRot = model.leftArm.xRot * 0.5F - ((float)Math.PI * 0.8F);
					model.leftArm.zRot = model.leftArm.zRot * 0.5F + ((float)Math.PI * 0.1F);
					model.leftArm.yRot = 0F;
				}
			});

			@Override
			public HumanoidModel.ArmPose getArmPose(LivingEntity entityLiving, InteractionHand hand, ItemStack itemStack) {
				if (!itemStack.isEmpty() && itemStack.getEnchantmentLevel(ModEnchantments.DESTRUCTION.get()) > 0) {
					if (entityLiving.isUsingItem() && entityLiving.getUseItemRemainingTicks() > 0 && entityLiving.getUsedItemHand() == hand) {
						return DESTRUCTION_POSE;
					}
				}
				if (!itemStack.isEmpty() && itemStack.getEnchantmentLevel(ModEnchantments.RESTORATION.get()) > 0) {
					if (entityLiving.isUsingItem() && entityLiving.getUseItemRemainingTicks() > 0 && entityLiving.getUsedItemHand() == hand) {
						return ACTIVE_USE_POSE;
					}
				}
				if (!itemStack.isEmpty() && itemStack.getEnchantmentLevel(ModEnchantments.ALTERATION.get()) > 0) {
					if (entityLiving.isUsingItem() && entityLiving.getUseItemRemainingTicks() > 0 && entityLiving.getUsedItemHand() == hand) {
						return ACTIVE_USE_POSE;
					}
				}
				return HumanoidModel.ArmPose.EMPTY;
			}
		});
	}

	@Override
	public int getColor(ItemStack stack, Entity entity) {
		return -65536;
	}

	@Override
	public int getDisplayManaCost(ItemStack item, Player player) {
		if (EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.ALTERATION.get(), item) > 0) {
			return 4;
		}
		return 5;
	}

	@Override
	public int getManaCost(ItemStack item, Player player) {
		// Возвращаем 1, если предмет зачарован на DESTRUCTION, иначе 5
		if (EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.DESTRUCTION.get(), item) > 0) {
			return 1;
		}
		if (EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.RESTORATION.get(), item) > 0) {
			if(player.isUsingItem() && player.getUseItemRemainingTicks() > 0 && player.getItemInHand(player.getUsedItemHand()) == item)
				return 1;
			else return 5;
		}
		if (EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.ALTERATION.get(), item) > 0) {
			return 1;
		}
		return 5;
	}

	public Component getManaCostAdditionalLine(ItemStack item) {
		if (EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.DESTRUCTION.get(), item) > 0) {
			return Component.translatable("tooltip.rpgworldmod.cost_per_second").withStyle(ChatFormatting.BLUE);
		}
		if (EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.RESTORATION.get(), item) > 0) {
			return Component.translatable("tooltip.rpgworldmod.plus_cost_per_second", 3).withStyle(ChatFormatting.BLUE);
		}
		if (EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.ALTERATION.get(), item) > 0) {
			return Component.translatable("tooltip.rpgworldmod.cost_per_second").withStyle(ChatFormatting.BLUE);
		}
		return Component.literal("");
	}

	public static ItemStack createForEnchantment(EnchantmentInstance pInstance) {
		ItemStack itemstack = new ItemStack(ModItems.EMBER_SCROLL.get());
		itemstack.enchant(pInstance.enchantment, pInstance.level);
		return itemstack;
	}

	public static void setUseTime(ItemStack stack, int useTime) {
		CompoundTag tag = stack.getOrCreateTag();
		tag.putInt("PrevUseTime", getUseTime(stack));
		tag.putInt("UseTime", useTime);
	}

	public static int getUseTime(ItemStack stack) {
		CompoundTag compoundtag = stack.getTag();
		return compoundtag != null ? compoundtag.getInt("UseTime") : 0;
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		ItemStack itemstack = player.getItemInHand(hand);

		// Проверка наличия нужного зачарования
		if (EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.DESTRUCTION.get(), itemstack) <= 0
				&& EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.RESTORATION.get(), itemstack) <= 0
				&& EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.ALTERATION.get(), itemstack) <= 0) {
			return InteractionResultHolder.fail(itemstack);
		}

		if (!level.isClientSide) {

			// На сервере
			UUID playerId = player.getUUID();

			AtomicBoolean cir = new AtomicBoolean(false);

			if (!player.getAbilities().instabuild) {
				player.getCapability(PlayerManaProvider.PLAYER_MANA).ifPresent(mana -> {
					if (mana.getMana() < getManaCost(itemstack, player)) {
						cir.set(true);
						return;
					}
					mana.reduceMana((ServerPlayer) player, getManaCost(itemstack, player));
				});
			}

			if (cir.get()) {
				getPlayerUseData(level).remove(playerId);
				// Отправляем пакет для остановки звуков на клиентах
				ModMessages.sendToNearbyPlayers(
						new EmberScrollLoopSoundPacket(player.getId(), false),
						(ServerLevel) level,
						player.blockPosition(),
						64.0
				);
				return InteractionResultHolder.fail(itemstack);
			}

			getPlayerUseData(level).put(playerId, new PlayerUseData(playerId, level.getGameTime()));

			if(EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.DESTRUCTION.get(), itemstack) > 0) {

				// Воспроизводим звук начала для всех игроков
				level.playSound(null,
						player.getX(), player.getY(), player.getZ(),
						RPGSounds.SPELL_DESTRUCTION_EMBER_START.get(),
						SoundSource.PLAYERS, 1.0F, 1.0F
				);

				// Отправляем пакет для запуска зацикленного звука на клиентах
				ModMessages.sendToNearbyPlayers(
						new EmberScrollLoopSoundPacket(player.getId(), true),
						(ServerLevel) level,
						player.blockPosition(),
						64.0
				);
			}

			if(EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.RESTORATION.get(), itemstack) > 0) {

				// Воспроизводим звук начала для всех игроков
				level.playSound(null,
						player.getX(), player.getY(), player.getZ(),
						RPGSounds.SPELL_RESTORATION_START.get(),
						SoundSource.PLAYERS, 1.0F, 1.0F
				);

				// Отправляем пакет для запуска зацикленного звука на клиентах
				ModMessages.sendToNearbyPlayers(
						new EmberScrollLoopSoundPacket(player.getId(), true),
						(ServerLevel) level,
						player.blockPosition(),
						64.0
				);
			}

			if(EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.ALTERATION.get(), itemstack) > 0) {

				// Воспроизводим звук начала для всех игроков
				level.playSound(null,
						player.getX(), player.getY(), player.getZ(),
						RPGSounds.SPELL_ALTERATION_START.get(),
						SoundSource.PLAYERS, 1.0F, 1.0F
				);

				// Отправляем пакет для запуска зацикленного звука на клиентах
				ModMessages.sendToNearbyPlayers(
						new EmberScrollLoopSoundPacket(player.getId(), true),
						(ServerLevel) level,
						player.blockPosition(),
						64.0
				);
			}

			player.startUsingItem(hand);
		} else {
			// Клиентская проверка маны
			if (!player.getAbilities().instabuild && ClientManaData.get() < getManaCost(itemstack,player))
				return InteractionResultHolder.fail(itemstack);

			player.startUsingItem(hand);
		}

		return InteractionResultHolder.consume(itemstack);
	}

	@Override
	public void releaseUsing(ItemStack stack, Level level, LivingEntity livingEntity, int timeCharged) {
		if (livingEntity instanceof Player player) {
			UUID playerId = player.getUUID();

			if (!level.isClientSide) {
				// На сервере
				getPlayerUseData(level).remove(playerId);

				if (EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.DESTRUCTION.get(), stack) > 0) {
					// Воспроизводим звук окончания для всех игроков
					level.playSound(null,
							player.getX(), player.getY(), player.getZ(),
							RPGSounds.SPELL_DESTRUCTION_EMBER_STOP.get(),
							SoundSource.PLAYERS, 1.0F, 1.0F
					);

					// Отправляем пакет для остановки зацикленного звука
					ModMessages.sendToNearbyPlayers(
							new EmberScrollLoopSoundPacket(player.getId(), false),
							(ServerLevel) level,
							player.blockPosition(),
							64.0
					);
				}

				if (EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.ALTERATION.get(), stack) > 0) {
					// Воспроизводим звук окончания для всех игроков
					level.playSound(null,
							player.getX(), player.getY(), player.getZ(),
							RPGSounds.SPELL_ALTERATION_STOP.get(),
							SoundSource.PLAYERS, 1.0F, 1.0F
					);

					// Отправляем пакет для остановки зацикленного звука
					ModMessages.sendToNearbyPlayers(
							new EmberScrollLoopSoundPacket(player.getId(), false),
							(ServerLevel) level,
							player.blockPosition(),
							64.0
					);
				}

				if (EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.RESTORATION.get(), stack) > 0) {
					// Воспроизводим звук окончания для всех игроков
					level.playSound(null,
							player.getX(), player.getY(), player.getZ(),
							RPGSounds.SPELL_RESTORATION_STOP.get(),
							SoundSource.PLAYERS, 1.0F, 1.0F
					);

					// Отправляем пакет для остановки зацикленного звука
					ModMessages.sendToNearbyPlayers(
							new EmberScrollLoopSoundPacket(player.getId(), false),
							(ServerLevel) level,
							player.blockPosition(),
							64.0
					);

					player.extinguishFire();
				}
			} else {
				ModMessages.sendToServer(new UpdateItemTagMessage(player.getId(), stack));
			}
		}
		super.releaseUsing(stack, level, livingEntity, timeCharged);
	}

	@Override
	public int getUseDuration(ItemStack stack) {
		return 72000;
	}

	@Override
	public void inventoryTick(ItemStack stack, Level level, Entity entity, int i, boolean held) {
		super.inventoryTick(stack, level, entity, i, held);

		// Обновляем тег UseTime на клиенте (аналогично RaygunItem)
		if (level.isClientSide) {
			boolean using = entity instanceof LivingEntity living && living.getUseItem().equals(stack);
			int useTime = getUseTime(stack);
			CompoundTag tag = stack.getOrCreateTag();

			if (tag.getInt("PrevUseTime") != tag.getInt("UseTime")) {
				tag.putInt("PrevUseTime", getUseTime(stack));
			}

			if (using && useTime < 5.0F) {
				setUseTime(stack, useTime + 1);
			}
			if (!using && useTime > 0.0F) {
				setUseTime(stack, useTime - 1);
			}
		}
	}

	// Вспомогательные методы для работы с данными
	private static Map<UUID, PlayerUseData> getPlayerUseData(Level level) {
		return playerUseData.computeIfAbsent(level, k -> new HashMap<>());
	}

	private static Map<UUID, EmberProjectileData> getActiveProjectiles(Level level) {
		return activeProjectiles.computeIfAbsent(level, k -> new HashMap<>());
	}

	// Класс для отслеживания использования игроком
	private static class PlayerUseData {
		private final UUID playerId;
		private long startTime;
		private int useTicks;
		private int lastManaTick;
		private int lastProjectileTick;
		private int lastAlterationTick;
		private int lastAlterationRemovalTick;

		public PlayerUseData(UUID playerId, long startTime) {
			this.playerId = playerId;
			this.startTime = startTime;
			this.useTicks = 0;
			this.lastManaTick = 0;
			this.lastProjectileTick = -3;
			this.lastAlterationTick = -1;
			this.lastAlterationRemovalTick = -1;
		}

		public void tick() {
			useTicks++;
		}

		public boolean shouldConsumeMana(ItemStack item) {
			int tick = 4;
			if (item.getEnchantmentLevel(ModEnchantments.RESTORATION.get())>0) tick = 7;
			if (item.getEnchantmentLevel(ModEnchantments.ALTERATION.get())>0) tick = 5;
			if (useTicks - lastManaTick >= tick) {
				lastManaTick = useTicks;
				return true;
			}
			return false;
		}

		public boolean shouldProcessAlteration(ItemStack item) {
			if (item.getEnchantmentLevel(ModEnchantments.ALTERATION.get()) > 0) {
				if (useTicks - lastAlterationTick >= 7) {
					lastAlterationTick = useTicks;
					return true;
				}
			}
			return false;
		}
	}

	// Класс для хранения данных снаряда
	private static class EmberProjectileData {
		private final UUID ownerId;
		private Vec3 position;
		private Vec3 velocity;
		private final long spawnTime;

		public EmberProjectileData(UUID ownerId, Vec3 position, Vec3 velocity, long spawnTime) {
			this.ownerId = ownerId;
			this.position = position;
			this.velocity = velocity;
			this.spawnTime = spawnTime;
		}
	}

	// Обработчик тиков для использования предмета и движения снарядов
	@SubscribeEvent
	public void onServerTick(TickEvent.ServerTickEvent event) {
		if (event.phase == TickEvent.Phase.START) {
			// Обрабатываем все уровни сервера
			event.getServer().getAllLevels().forEach(this::processPlayerUsage);
		} else if (event.phase == TickEvent.Phase.END) {
			// Обрабатываем все уровни сервера
			event.getServer().getAllLevels().forEach(this::processProjectiles);
		}
	}

	private void processPlayerUsage(ServerLevel level) {
		// Получаем данные для этого уровня
		Map<UUID, PlayerUseData> levelPlayerUseData = getPlayerUseData(level);

		// Копируем для безопасного удаления
		Map<UUID, PlayerUseData> copy = new HashMap<>(levelPlayerUseData);

		for (Map.Entry<UUID, PlayerUseData> entry : copy.entrySet()) {
			UUID playerId = entry.getKey();
			PlayerUseData useData = entry.getValue();

			Player player = level.getPlayerByUUID(playerId);
			if (player == null || !player.isUsingItem()) {
				levelPlayerUseData.remove(playerId);
				continue;
			}

			ItemStack usingItem = player.getUseItem();
			if (!(usingItem.getItem() instanceof EmberScrollItem)) {
				levelPlayerUseData.remove(playerId);
				continue;
			}

			// Проверяем наличие зачарования
			if (EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.DESTRUCTION.get(), usingItem) <= 0
					&& EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.RESTORATION.get(), usingItem) <= 0
					&& EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.ALTERATION.get(), usingItem) <= 0) {
				levelPlayerUseData.remove(playerId);
				player.stopUsingItem();
				continue;
			}

			useData.tick();

			if (useData.shouldConsumeMana(usingItem)) {
				if (!player.getAbilities().instabuild) {
					AtomicBoolean hasEnoughMana = new AtomicBoolean(true);
					player.getCapability(PlayerManaProvider.PLAYER_MANA).ifPresent(mana -> {
						if (mana.getMana() < getManaCost(usingItem, player)) {
							hasEnoughMana.set(false);
						} else {
							mana.reduceMana((ServerPlayer) player, getManaCost(usingItem, player));
						}
					});

					if (!hasEnoughMana.get()) {
						levelPlayerUseData.remove(playerId);
						player.stopUsingItem();

						// Отправляем пакет остановки звука
						ModMessages.sendToNearbyPlayers(
								new EmberScrollLoopSoundPacket(player.getId(), false),
								level,
								player.blockPosition(),
								64.0
						);

						if (EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.DESTRUCTION.get(), usingItem) > 0) {
							// Воспроизводим звук окончания для всех игроков
							level.playSound(null,
									player.getX(), player.getY(), player.getZ(),
									RPGSounds.SPELL_DESTRUCTION_EMBER_STOP.get(),
									SoundSource.PLAYERS, 1.0F, 1.0F
							);

							// Отправляем пакет для остановки зацикленного звука
							ModMessages.sendToNearbyPlayers(
									new EmberScrollLoopSoundPacket(player.getId(), false),
									(ServerLevel) level,
									player.blockPosition(),
									64.0
							);
						}

						if (EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.ALTERATION.get(), usingItem) > 0) {
							// Воспроизводим звук окончания для всех игроков
							level.playSound(null,
									player.getX(), player.getY(), player.getZ(),
									RPGSounds.SPELL_ALTERATION_STOP.get(),
									SoundSource.PLAYERS, 1.0F, 1.0F
							);

							// Отправляем пакет для остановки зацикленного звука
							ModMessages.sendToNearbyPlayers(
									new EmberScrollLoopSoundPacket(player.getId(), false),
									(ServerLevel) level,
									player.blockPosition(),
									64.0
							);
						}

						if (EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.RESTORATION.get(), usingItem) > 0) {
							// Воспроизводим звук окончания для всех игроков
							level.playSound(null,
									player.getX(), player.getY(), player.getZ(),
									RPGSounds.SPELL_RESTORATION_STOP.get(),
									SoundSource.PLAYERS, 1.0F, 1.0F
							);

							// Отправляем пакет для остановки зацикленного звука
							ModMessages.sendToNearbyPlayers(
									new EmberScrollLoopSoundPacket(player.getId(), false),
									(ServerLevel) level,
									player.blockPosition(),
									64.0
							);

							player.extinguishFire();
						}
						continue;
					}
				}
			}

			// Обработка ALTERATION - управление лавой
			if (!player.isShiftKeyDown() && useData.shouldProcessAlteration(usingItem)) {
				processLavaAlteration(level, player);
			}
			if (player.isShiftKeyDown() && EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.ALTERATION.get(), usingItem) > 0) {
				removeNearestLava(level, player);
			}

			if (EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.DESTRUCTION.get(), usingItem) > 0)
				spawnProjectile(level, player);
		}
	}

	private void removeNearestLava(ServerLevel level, Player player) {
		BlockPos playerPos = player.blockPosition();
		int radius = 5;
		int radiusSquared = radius * radius;

		BlockPos nearestLavaPos = null;
		double nearestDistance = Double.MAX_VALUE;

		// Ищем ближайший блок лавы в радиусе
		for (int x = -radius; x <= radius; x++) {
			for (int y = -radius; y <= radius; y++) {
				for (int z = -radius; z <= radius; z++) {
					BlockPos checkPos = playerPos.offset(x, y, z);
					double distance = playerPos.distSqr(checkPos);

					// Проверяем, что блок находится в сферическом радиусе
					if (distance <= radiusSquared) {
						BlockState state = level.getBlockState(checkPos);
						FluidState fluidState = level.getFluidState(checkPos);

						// Проверяем, является ли блок лавой (источником или текущей)
						boolean isLavaSource = state.is(Blocks.LAVA) && fluidState.isSource();
						boolean isFlowingLava = fluidState.is(FluidTags.LAVA) && !fluidState.isSource();

						if ((isLavaSource || isFlowingLava) && distance < nearestDistance) {
							nearestLavaPos = checkPos;
							nearestDistance = distance;
						}
					}
				}
			}
		}

		// Если нашли блок лавы, удаляем его
		if (nearestLavaPos != null) {
			// Удаляем блок лавы
			level.setBlockAndUpdate(nearestLavaPos, Blocks.AIR.defaultBlockState());

			// Спавним частицы дыма для визуального эффекта
			level.sendParticles(ParticleTypes.SMOKE,
					nearestLavaPos.getX() + 0.5, nearestLavaPos.getY() + 0.5, nearestLavaPos.getZ() + 0.5,
					5, 0.2, 0.2, 0.2, 0.05);
		}
	}

	private void processLavaAlteration(ServerLevel level, Player player) {
		BlockPos playerPos = player.blockPosition();
		int radius = 5;

		// Находим ближайший подходящий блок в радиусе
		BlockPos nearestLavaPos = null;
		double nearestDistance = Double.MAX_VALUE;

		// Проходим по всем блокам в кубе радиусом 5 блоков
		for (int x = -radius; x <= radius; x++) {
			for (int y = -radius; y <= radius; y++) {
				for (int z = -radius; z <= radius; z++) {
					BlockPos checkPos = playerPos.offset(x, y, z);
					double distance = playerPos.distSqr(checkPos);

					// Проверяем, что блок находится в сферическом радиусе
					if (distance <= radius * radius) {
						BlockState state = level.getBlockState(checkPos);
						FluidState fluidState = level.getFluidState(checkPos);

						// Проверяем, является ли блок лавой (источником или текущей)
						boolean isLavaSource = state.is(Blocks.LAVA) && fluidState.isSource();
						boolean isFlowingLava = fluidState.is(FluidTags.LAVA) && !fluidState.isSource();

						boolean proceedLavaSource = false;
						if(isLavaSource) {
							for (Direction direction : Direction.values()) {
								if (direction != Direction.UP) {
									BlockPos neighborPos = checkPos.relative(direction);

									// Проверяем, можно ли распространить лаву на этот блок
									if (canLavaSpreadTo(level, neighborPos)) {
										proceedLavaSource = true;
									}
								}
							}
						}

						if ((proceedLavaSource || isFlowingLava) && distance < nearestDistance) {
							nearestLavaPos = checkPos;
							nearestDistance = distance;
						}
					}
				}
			}
		}

		// Если нашли подходящий блок
		if (nearestLavaPos != null) {
			BlockState state = level.getBlockState(nearestLavaPos);
			FluidState fluidState = level.getFluidState(nearestLavaPos);

			// Если это льющаяся лава (не источник)
			if (fluidState.is(FluidTags.LAVA) && !fluidState.isSource()) {
				// Превращаем в источник лавы
				level.setBlockAndUpdate(nearestLavaPos, Fluids.LAVA.getSource().defaultFluidState().createLegacyBlock());

				// Спавним частицы для визуального эффекта
				level.sendParticles(ParticleTypes.LAVA,
						nearestLavaPos.getX() + 0.5, nearestLavaPos.getY() + 0.5, nearestLavaPos.getZ() + 0.5,
						5, 0.2, 0.2, 0.2, 0.01);
			}
			// Если это источник лавы
			else if (state.is(Blocks.LAVA) && fluidState.isSource()) {
				// Распространяем лаву на один соседний блок
				spreadLavaInstantly(level, nearestLavaPos);
			}
		}
	}

	private void spreadLavaInstantly(ServerLevel level, BlockPos sourcePos) {
		// Проверяем все соседние блоки
		for (Direction direction : Direction.values()) {
			BlockPos neighborPos = sourcePos.relative(direction);

			if (direction == Direction.UP) {
				continue;
			}
			if (level.getBlockState(neighborPos).is(Blocks.LAVA)) {
				continue;
			}

			// Проверяем, можно ли распространить лаву на этот блок
			if (canLavaSpreadTo(level, neighborPos)) {

				if(level.getFluidState(sourcePos).getType() instanceof LavaFluid lava) lava.spreadTo(level, neighborPos, level.getBlockState(neighborPos), direction, lava.getFlowing(6,direction==Direction.DOWN));

				// Спавним частицы
				level.sendParticles(ParticleTypes.LAVA,
						neighborPos.getX() + 0.5, neighborPos.getY() + 0.5, neighborPos.getZ() + 0.5,
						3, 0.1, 0.1, 0.1, 0.005);
			}
		}
	}

	private boolean canLavaSpreadTo(ServerLevel level, BlockPos pos) {
		BlockState state = level.getBlockState(pos);
		return state.isAir() ||
				state.getFluidState().is(FluidTags.WATER) ||
				(state.getBlock() != Blocks.LAVA && state.getFluidState().isEmpty() &&
						!state.isSolidRender(level, pos));
	}

	private void spawnProjectile(ServerLevel level, Player player) {
		// Создаем снаряд
		Vec3 lookAngle = player.getLookAngle();
		Vec3 startPos = player.getEyePosition().add(lookAngle.scale(0.5));

		EmberProjectileData projectile = new EmberProjectileData(
				player.getUUID(),
				startPos,
				lookAngle.scale(0.7),
				level.getGameTime()
		);

		getActiveProjectiles(level).put(UUID.randomUUID(), projectile);
	}

	private void processProjectiles(ServerLevel level) {
		// Получаем снаряды для этого уровня
		Map<UUID, EmberProjectileData> levelActiveProjectiles = getActiveProjectiles(level);

		if (level == null || levelActiveProjectiles.isEmpty()) return;

		// Копируем для безопасного удаления
		Map<UUID, EmberProjectileData> copy = new HashMap<>(levelActiveProjectiles);

		for (Map.Entry<UUID, EmberProjectileData> entry : copy.entrySet()) {
			UUID projectileId = entry.getKey();
			EmberProjectileData projectile = entry.getValue();

			if (level.getGameTime() - projectile.spawnTime >= 10) {
				levelActiveProjectiles.remove(projectileId);

				level.sendParticles(ParticleTypes.SMOKE,
						projectile.position.x, projectile.position.y, projectile.position.z,
						3, 0.2, 0.2, 0.2, 0.02);
				continue;
			}

			// Проверяем контакт с жидкостью перед обновлением позиции
			if (checkWaterContact(level, projectile.position)) {
				// Эффект шипения в воде
				level.sendParticles(ParticleTypes.SMOKE,
						projectile.position.x, projectile.position.y, projectile.position.z,
						5, 0.2, 0.2, 0.2, 0.05);
				level.sendParticles(ParticleTypes.BUBBLE,
						projectile.position.x, projectile.position.y, projectile.position.z,
						3, 0.1, 0.1, 0.1, 0.1);

				level.playSound(null,
						projectile.position.x, projectile.position.y, projectile.position.z,
						RPGSounds.EMBER_GEM_EXTINGUISH.get(), SoundSource.NEUTRAL,
						0.3F, 1.0F);
				levelActiveProjectiles.remove(projectileId);
				continue;
			}

			// Обновляем позицию (в 2 раза быстрее)
			projectile.position = projectile.position.add(projectile.velocity);

			// Проверяем столкновения
			if (checkCollisions(level, projectile, projectileId)) {
				levelActiveProjectiles.remove(projectileId);
				continue;
			}

			// Спавним частицы
			if(level.getGameTime() - projectile.spawnTime <= 1) level.sendParticles(ParticleTypes.FLAME,
					projectile.position.x, projectile.position.y, projectile.position.z,
					1, 0.1, 0.1, 0.1, 0.01);
			else  level.sendParticles(ModParticles.FLAMES.get(),
					projectile.position.x, projectile.position.y, projectile.position.z,
					1, 0.1, 0.1, 0.1, 0.01);

			// Добавляем больше частиц для эффекта скорости
			level.sendParticles(ParticleTypes.SMOKE,
					projectile.position.x, projectile.position.y, projectile.position.z,
					1, 0.05, 0.05, 0.05, 0.005);
		}
	}

	// Проверка контакта с водой
	private boolean checkWaterContact(Level level, Vec3 position) {
		BlockPos pos = new BlockPos(
				(int) Math.floor(position.x),
				(int) Math.floor(position.y),
				(int) Math.floor(position.z)
		);

		// Проверяем блок жидкости
		FluidState fluidState = level.getFluidState(pos);
		if (fluidState.is(FluidTags.WATER)) {
			return true;
		}

		// Проверяем соседние блоки для точности
		for (int dx = -1; dx <= 1; dx++) {
			for (int dy = -1; dy <= 1; dy++) {
				for (int dz = -1; dz <= 1; dz++) {
					BlockPos checkPos = pos.offset(dx, dy, dz);
					FluidState nearbyFluid = level.getFluidState(checkPos);
					if (nearbyFluid.is(FluidTags.WATER)) {
						double distance = position.distanceTo(
								new Vec3(checkPos.getX() + 0.5, checkPos.getY() + 0.5, checkPos.getZ() + 0.5)
						);
						if (distance < 1.0) {
							return true;
						}
					}
				}
			}
		}

		return false;
	}

	// Проверка столкновений
	private boolean checkCollisions(Level level, EmberProjectileData projectile, UUID projectileId) {
		// Проверка столкновения с блоками
		Vec3 startPos = projectile.position.subtract(projectile.velocity);
		Vec3 endPos = projectile.position.add(projectile.velocity);

		BlockHitResult blockHit = level.clip(new ClipContext(
				startPos, endPos,
				ClipContext.Block.COLLIDER,
				ClipContext.Fluid.ANY,
				null
		));

		if (blockHit.getType() != HitResult.Type.MISS) {
			BlockPos hitPos = blockHit.getBlockPos();
			BlockState hitState = level.getBlockState(hitPos);

			// Проверяем, не попали ли в воду (на всякий случай)
			if (level.getFluidState(hitPos).is(FluidTags.WATER)) {
				// Эффект шипения
				level.playSound(null, hitPos, RPGSounds.EMBER_GEM_EXTINGUISH.get(),
						SoundSource.BLOCKS, 0.3F, 1.0F);

				if (level instanceof ServerLevel serverLevel) {
					serverLevel.sendParticles(ParticleTypes.SMOKE,
							hitPos.getX() + 0.5, hitPos.getY() + 0.5, hitPos.getZ() + 0.5,
							10, 0.3, 0.3, 0.3, 0.05);
				}
				return true;
			}

			// Проверяем, является ли блок горючим
			if (hitState.is(ModBlocks.ARBOR_FUEL_BLOCK.get())) {
				// Заменяем блок земли на огонь
				level.setBlockAndUpdate(hitPos, BaseFireBlock.getState(level, hitPos));
				return true;
			}

			if (hitState.getBlock() instanceof TntBlock tnt) {
				// Создаём мнимый горящий снаряд для взаимодействия с TNT
				if (level instanceof ServerLevel serverLevel) {
					// Создаём фейковый SmallFireball
					SmallFireball fireProjectile = new SmallFireball(
							serverLevel,
							projectile.position.x,
							projectile.position.y,
							projectile.position.z,
							projectile.velocity.x,
							projectile.velocity.y,
							projectile.velocity.z
					);

					// Устанавливаем владельца снаряда, если есть
					if (projectile.ownerId != null) {
						Entity owner = serverLevel.getEntity(projectile.ownerId);
						if (owner != null) {
							fireProjectile.setOwner(owner);
						}
					}

					// Устанавливаем, что снаряд горит
					fireProjectile.setSecondsOnFire(100);

					// Вызываем метод взаимодействия TNT со снарядом
					tnt.onProjectileHit(level, hitState, blockHit, fireProjectile);
				}
				return true;
			}

			// НОВЫЙ КОД: Проверка воспламеняемости блоков
			int flammability = Math.max(100 - hitState.getFlammability(level, hitPos, blockHit.getDirection()), 0);
			if (flammability < 100) {
				long timeAlive = 10 - ((level.getGameTime() - projectile.spawnTime));
				double requiredFlammability = timeAlive * 12;

				if (requiredFlammability >= flammability) {
					// Уничтожаем блок без дропа
					level.destroyBlock(hitPos, false);
					// Ставим блок огня на его место
					BlockState fireState = BaseFireBlock.getState(level, hitPos);
					if (BaseFireBlock.canBePlacedAt(level, hitPos, Direction.UP)) {
						level.setBlockAndUpdate(hitPos, fireState);
					}
					else level.setBlockAndUpdate(hitPos, Blocks.AIR.defaultBlockState());
					return false;
				}
				// Если воспламеняемость недостаточна, продолжаем обычную обработку
			}

			// Для остальных блоков - проверяем, можно ли использовать зажигалку
			// Создаём мнимую зажигалку для проверки
			ItemStack flintAndSteel = new ItemStack(Items.FLINT_AND_STEEL);

			// Проверяем, может ли зажигалка быть использована на этом блоке
			if (level instanceof ServerLevel serverLevel) {
				// Создаём фейкового игрока
				FakePlayer fakePlayer = FakePlayerFactory.get(serverLevel, new GameProfile(UUID.randomUUID(), "FakePlayer"));

				// Устанавливаем позицию фейкового игрока в точку удара
				fakePlayer.setPos(hitPos.getX(), hitPos.getY(), hitPos.getZ());

				// Устанавливаем правильное вращение для контекста
				fakePlayer.setYRot(blockHit.getDirection().toYRot());
				fakePlayer.setXRot((float) Math.toDegrees(blockHit.getDirection().toYRot()));

				// Даём фейковому игроку зажигалку в руку
				fakePlayer.setItemInHand(InteractionHand.MAIN_HAND, flintAndSteel.copy());

				// Создаём UseOnContext для проверки использования зажигалки
				UseOnContext context = new UseOnContext(fakePlayer, InteractionHand.MAIN_HAND, blockHit);

				// Проверяем, можно ли использовать зажигалку на блоке
				InteractionResult useResult = InteractionResult.PASS;
				if (flintAndSteel.getItem() instanceof FlintAndSteelItem flintAndSteelItem) {
					useResult = flintAndSteelItem.useOn(context);
				}

				// Очищаем руку фейкового игрока
				fakePlayer.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);

				// Если зажигалка может быть использована (вернула SUCCESS или CONSUME),
				// то не спавним огонь на соседнем блоке
				if (useResult.consumesAction()) {
					return true;
				}
			}

			// Если зажигалка не может быть использована, пробуем поставить огонь на соседнем блоке
			BlockPos firePos = hitPos.relative(blockHit.getDirection());

			if (BaseFireBlock.canBePlacedAt(level, firePos, Direction.UP)) {
				level.setBlockAndUpdate(firePos, BaseFireBlock.getState(level, firePos));
			}
			return true;
		}

		// Проверка столкновения с существами
		List<Entity> entities = level.getEntities(null,
				new net.minecraft.world.phys.AABB(startPos, endPos).inflate(0.5));

		for (Entity entity : entities) {
			if (level instanceof ServerLevel serverLevel) {
				Entity owner = serverLevel.getEntity(projectile.ownerId);
				SmallFireball fakeFireball = new SmallFireball(
						level,
						projectile.position.x,
						projectile.position.y,
						projectile.position.z,
						projectile.velocity.x,
						projectile.velocity.y,
						projectile.velocity.z
				);
				fakeFireball.setOwner(owner);
				fakeFireball.setSecondsOnFire(1);
				if (entity instanceof LivingEntity livingEntity &&
						!entity.getUUID().equals(projectile.ownerId) && !entity.fireImmune()) {

					// Поджигаем существо на 5 секунд (100 тиков)
					livingEntity.setSecondsOnFire(5);
					if (owner != null)
						entity.hurt(owner.damageSources().fireball(fakeFireball, owner), 2F);
					return false;
				}
			}
		}

		return false;
	}

	public String getFirstPredicate() {
		return Minecraft.getInstance().options.keyShift.getKey().getDisplayName().getString();
	}
}