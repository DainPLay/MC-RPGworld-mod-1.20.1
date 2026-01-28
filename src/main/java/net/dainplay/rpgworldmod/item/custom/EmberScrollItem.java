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
import net.minecraft.world.level.block.TntBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
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

	// Хранилище активных снарядов
	private static final Map<UUID, EmberProjectileData> activeProjectiles = new HashMap<>();

	// Хранилище для отслеживания использования игроком
	private static final Map<UUID, PlayerUseData> playerUseData = new HashMap<>();

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
	public PoseStack getUsingPose(ItemStack stack, Player player, PoseStack poseStack, float flip) {
		if(stack.getEnchantmentLevel(ModEnchantments.DESTRUCTION.get()) > 0) {
			poseStack.mulPose(Axis.ZP.rotationDegrees(flip * -10.0F));
			poseStack.mulPose(Axis.YP.rotationDegrees((-(float)Math.PI / 6F)));
			poseStack.translate(0F, 0.25F, 0F);
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

	/*@Override
	public @NotNull UseAnim getUseAnimation(ItemStack item) {
		return item.getEnchantmentLevel(ModEnchantments.DESTRUCTION.get()) > 0 ? UseAnim.CUSTOM : UseAnim.NONE;
	}*/

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

			@Override
			public HumanoidModel.ArmPose getArmPose(LivingEntity entityLiving, InteractionHand hand, ItemStack itemStack) {
				if (!itemStack.isEmpty() && itemStack.getEnchantmentLevel(ModEnchantments.DESTRUCTION.get()) > 0) {
					if (entityLiving.isUsingItem() && entityLiving.getUseItemRemainingTicks() > 0 && entityLiving.getUsedItemHand() == hand) {
						return DESTRUCTION_POSE;
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
		return 5;
	}

	public Component getManaCostAdditionalLine(ItemStack item) {
		if (EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.DESTRUCTION.get(), item) > 0) {
			return Component.translatable("tooltip.rpgworldmod.cost_per_second").withStyle(ChatFormatting.BLUE);
		}
		if (EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.RESTORATION.get(), item) > 0) {
			return Component.translatable("tooltip.rpgworldmod.plus_cost_per_second", 5).withStyle(ChatFormatting.BLUE);
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
				&& EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.RESTORATION.get(), itemstack) <= 0) {
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
				playerUseData.remove(playerId);
				// Отправляем пакет для остановки звуков на клиентах
				ModMessages.sendToNearbyPlayers(
						new EmberScrollLoopSoundPacket(player.getId(), false),
						(ServerLevel) level,
						player.blockPosition(),
						64.0
				);
				return InteractionResultHolder.fail(itemstack);
			}
			if (!playerUseData.containsKey(playerId)) {
				playerUseData.put(playerId, new PlayerUseData(playerId, level.getGameTime()));
			}

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
					playerUseData.remove(playerId);

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

	// Класс для отслеживания использования игроком
	private static class PlayerUseData {
		private final UUID playerId;
		private long startTime;
		private int useTicks;
		private int lastManaTick;
		private int lastProjectileTick;

		public PlayerUseData(UUID playerId, long startTime) {
			this.playerId = playerId;
			this.startTime = startTime;
			this.useTicks = 0;
			this.lastManaTick = 0;
			this.lastProjectileTick = -3;
		}

		public void tick() {
			useTicks++;
		}

		public boolean shouldConsumeMana() {
			if (useTicks - lastManaTick >= 4) {
				lastManaTick = useTicks;
				return true;
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
			processPlayerUsage(event.getServer().overworld());
		} else if (event.phase == TickEvent.Phase.END) {
			processProjectiles(event.getServer().overworld());
		}
	}

	private void processPlayerUsage(ServerLevel level) {
		// Копируем для безопасного удаления
		Map<UUID, PlayerUseData> copy = new HashMap<>(playerUseData);

		for (Map.Entry<UUID, PlayerUseData> entry : copy.entrySet()) {
			UUID playerId = entry.getKey();
			PlayerUseData useData = entry.getValue();

			Player player = level.getPlayerByUUID(playerId);
			if (player == null || !player.isUsingItem()) {
				playerUseData.remove(playerId);
				continue;
			}

			ItemStack usingItem = player.getUseItem();
			if (!(usingItem.getItem() instanceof EmberScrollItem)) {
				playerUseData.remove(playerId);
				continue;
			}

			// Проверяем наличие зачарования
			if (EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.DESTRUCTION.get(), usingItem) <= 0 && EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.RESTORATION.get(), usingItem) <= 0) {
				playerUseData.remove(playerId);
				player.stopUsingItem();
				continue;
			}

			useData.tick();

			// Трата маны каждые 20 тиков
			if (useData.shouldConsumeMana()) {
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
						playerUseData.remove(playerId);
						player.stopUsingItem();

						// Отправляем пакет остановки звука
						ModMessages.sendToNearbyPlayers(
								new EmberScrollLoopSoundPacket(player.getId(), false),
								level,
								player.blockPosition(),
								64.0
						);
						continue;
					}
				}
			}
			spawnProjectile(level, player);
		}
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

		activeProjectiles.put(UUID.randomUUID(), projectile);

		/*ModMessages.sendToNearbyPlayers(
				new MoveParticlesPacket(startPos.add(lookAngle.scale(0.5)), lookAngle.scale(2.0), ModParticles.FLAMES.get(), true, 2D),
				level,
				player.getOnPos(),
				64.0
		);

		level.sendParticles(ModParticles.FLAMES.get(),
				projectile.position.x, projectile.position.y, projectile.position.z,
				1, 0.1, 0.1, 0.1, 0.01);*/
	}

	private void processProjectiles(ServerLevel level) {
		if (level == null) return;

		// Копируем для безопасного удаления
		Map<UUID, EmberProjectileData> copy = new HashMap<>(activeProjectiles);

		for (Map.Entry<UUID, EmberProjectileData> entry : copy.entrySet()) {
			UUID projectileId = entry.getKey();
			EmberProjectileData projectile = entry.getValue();

			if (level.getGameTime() - projectile.spawnTime >= 10) {
				activeProjectiles.remove(projectileId);

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
				activeProjectiles.remove(projectileId);
				continue;
			}

			// Обновляем позицию (в 2 раза быстрее)
			projectile.position = projectile.position.add(projectile.velocity);

			// Проверяем столкновения
			if (checkCollisions(level, projectile, projectileId)) {
				activeProjectiles.remove(projectileId);
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
				ClipContext.Fluid.ANY, // Учитываем жидкости для лучшей детекции
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
}