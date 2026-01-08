package net.dainplay.rpgworldmod.item.custom;

import com.mojang.authlib.GameProfile;
import net.dainplay.rpgworldmod.block.ModBlocks;
import net.dainplay.rpgworldmod.mana.ClientManaData;
import net.dainplay.rpgworldmod.mana.PlayerManaProvider;
import net.dainplay.rpgworldmod.sounds.RPGSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.world.item.FlintAndSteelItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.TntBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

public class EmberGemItem extends Item implements RPGtooltip, ManaCostItem, OrbitingItem {

	// Хранилище активных снарядов
	private static final Map<UUID, EmberProjectileData> activeProjectiles = new HashMap<>();
	private static int manacost;
	private static int color;
	private static String texture;
	private static int animationSpeed;
	private static int animationLength;

	public EmberGemItem(Properties pProperties, int cost, int color, String texture, int animationSpeed, int animationLength) {
		super(pProperties);
		this.manacost = cost;
		this.color = color;
		this.texture = texture;
		this.animationSpeed = animationSpeed;
		this.animationLength = animationLength;
		MinecraftForge.EVENT_BUS.register(this);
	}


	@Override
	public String getTexture() {
		return texture;
	}


	@Override
	public int getAnimationSpeed() {
		return animationSpeed;
	}


	@Override
	public int getAnimationLength() {
		return animationLength;
	}

	@Override
	public int getColor() {
		return color;
	}

	@Override
	public int getManaCost() {
		return manacost;
	}

	@Override
	public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
		super.inventoryTick(stack, level, entity, slotId, isSelected);

		if (!level.isClientSide && entity instanceof Player player) {
			if (isSelected || player.getOffhandItem() == stack) {
				updateManaTag(stack, player);
			}
		}
	}

	public boolean isFoil(ItemStack pStack) {
		return true;
	}

	@Override
	public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltip, TooltipFlag pFlag) {
		RPGappendHoverText(pStack, pLevel, pTooltip, pFlag);
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		ItemStack itemstack = player.getItemInHand(hand);

		if (!level.isClientSide) {
			AtomicBoolean cir = new AtomicBoolean(false);

			if (!player.getAbilities().instabuild) {
				player.getCapability(PlayerManaProvider.PLAYER_MANA).ifPresent(mana -> {
					if (mana.getMana() < manacost) {
						cir.set(true);
						return;
					}
					mana.reduceMana((ServerPlayer) player, manacost);
				});
			}

			if (cir.get()) {
				return InteractionResultHolder.fail(itemstack);
			}
			// КД на предмет (20 тиков = 1 секунда)
			player.getCooldowns().addCooldown(this, 20);

			// Создаем снаряд
			Vec3 lookAngle = player.getLookAngle();
			Vec3 startPos = player.getEyePosition().add(lookAngle.scale(0.5));

			// Увеличиваем скорость в 2 раза (1.0 вместо 0.5)
			EmberProjectileData projectile = new EmberProjectileData(
					player.getUUID(),
					startPos,
					lookAngle.scale(1.0), // В 2 раза быстрее
					level.getGameTime()
			);

			activeProjectiles.put(UUID.randomUUID(), projectile);

			// Звук использования
			level.playSound(null, player.getX(), player.getY(), player.getZ(),
					RPGSounds.EMBER_GEM_SNAP.get(), SoundSource.PLAYERS,
					0.5F, 0.8F + level.random.nextFloat() * 0.4F);
		} else {
			if (!player.getAbilities().instabuild && ClientManaData.get() < manacost)
				return InteractionResultHolder.fail(itemstack);
		}

		return InteractionResultHolder.sidedSuccess(itemstack, level.isClientSide());
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

	// Обработчик тиков для движения снарядов
	@SubscribeEvent
	public void onServerTick(TickEvent.ServerTickEvent event) {
		if (event.phase == TickEvent.Phase.END) {
			// Копируем для безопасного удаления
			Map<UUID, EmberProjectileData> copy = new HashMap<>(activeProjectiles);

			for (Map.Entry<UUID, EmberProjectileData> entry : copy.entrySet()) {
				UUID projectileId = entry.getKey();
				EmberProjectileData projectile = entry.getValue();

				// Уменьшаем время жизни в 2 раза (10 тиков вместо 20)
				if (event.getServer().getLevel(Level.OVERWORLD).getGameTime() - projectile.spawnTime >= 10) {
					activeProjectiles.remove(projectileId);

					// Эффект исчезновения
					Level level = event.getServer().getLevel(Level.OVERWORLD);
					if (level instanceof ServerLevel serverLevel) {
						serverLevel.sendParticles(ParticleTypes.SMOKE,
								projectile.position.x, projectile.position.y, projectile.position.z,
								3, 0.2, 0.2, 0.2, 0.02);
					}
					continue;
				}

				// Проверяем контакт с жидкостью перед обновлением позиции
				Level level = event.getServer().getLevel(Level.OVERWORLD);
				if (checkWaterContact(level, projectile.position)) {
					// Эффект шипения в воде
					if (level instanceof ServerLevel serverLevel) {
						serverLevel.sendParticles(ParticleTypes.SMOKE,
								projectile.position.x, projectile.position.y, projectile.position.z,
								5, 0.2, 0.2, 0.2, 0.05);
						serverLevel.sendParticles(ParticleTypes.BUBBLE,
								projectile.position.x, projectile.position.y, projectile.position.z,
								3, 0.1, 0.1, 0.1, 0.1);
					}
					level.playSound(null,
							projectile.position.x, projectile.position.y, projectile.position.z,
							RPGSounds.EMBER_GEM_EXTINGUISH.get(), SoundSource.NEUTRAL,
							0.3F, 1.0F);
					activeProjectiles.remove(projectileId);
					continue;
				}

				// Обновляем позицию
				projectile.position = projectile.position.add(projectile.velocity);

				// Проверяем столкновения
				if (checkCollisions(level, projectile, projectileId)) {
					activeProjectiles.remove(projectileId);
				}

				// Спавним частицы
				if (level instanceof ServerLevel serverLevel) {
					serverLevel.sendParticles(ParticleTypes.FLAME,
							projectile.position.x, projectile.position.y, projectile.position.z,
							1, 0.1, 0.1, 0.1, 0.01);
					// Добавляем больше частиц для эффекта скорости
					serverLevel.sendParticles(ParticleTypes.SMOKE,
							projectile.position.x, projectile.position.y, projectile.position.z,
							1, 0.05, 0.05, 0.05, 0.005);
				}
			}
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
						SoundSource.BLOCKS, 0.5F, 1.0F);

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

				// Звук и частицы для земли
				level.playSound(null, hitPos, RPGSounds.EMBER_GEM_IGNITE_BLOCK.get(),
						SoundSource.BLOCKS, 1.0F, level.random.nextFloat() * 0.4F + 0.8F);

				if (level instanceof ServerLevel serverLevel) {
					// Частицы превращения
					serverLevel.sendParticles(ParticleTypes.FLAME,
							hitPos.getX() + 0.5, hitPos.getY() + 0.5, hitPos.getZ() + 0.5,
							15, 0.5, 0.5, 0.5, 0.05);
					serverLevel.sendParticles(ParticleTypes.SMOKE,
							hitPos.getX() + 0.5, hitPos.getY() + 0.5, hitPos.getZ() + 0.5,
							8, 0.3, 0.3, 0.3, 0.03);
				}
				return true;
			}

			if (hitState.getBlock() instanceof TntBlock tnt) {
				// Создаём мнимый горящий снаряд для взаимодействия с TNT
				if (level instanceof ServerLevel serverLevel) {
					// Создаём фейковый SmallFireball (или другой Projectile, который горит)
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

					// Звук и частицы для TNT
					level.playSound(null, hitPos, RPGSounds.EMBER_GEM_IGNITE_BLOCK.get(),
							SoundSource.BLOCKS, 1.0F, level.random.nextFloat() * 0.4F + 0.8F);

					serverLevel.sendParticles(ParticleTypes.FLAME,
							hitPos.getX() + 0.5, hitPos.getY() + 0.5, hitPos.getZ() + 0.5,
							20, 0.5, 0.5, 0.5, 0.05);
					serverLevel.sendParticles(ParticleTypes.SMOKE,
							hitPos.getX() + 0.5, hitPos.getY() + 0.5, hitPos.getZ() + 0.5,
							10, 0.3, 0.3, 0.3, 0.03);
				}
				return true;
			}

			// Для остальных блоков - проверяем, можно ли использовать зажигалку
			// Создаём мнимую зажигалку для проверки
			ItemStack flintAndSteel = new ItemStack(Items.FLINT_AND_STEEL);

			// Проверяем, может ли зажигалка быть использована на этом блоке
			// Создаём контекст для использования предмета
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
					// Звук и частицы для зажигания блока
					level.playSound(null, hitPos, RPGSounds.EMBER_GEM_IGNITE_BLOCK.get(),
							SoundSource.BLOCKS, 1.0F, level.random.nextFloat() * 0.4F + 0.8F);

					serverLevel.sendParticles(ParticleTypes.FLAME,
							hitPos.getX() + 0.5, hitPos.getY() + 0.5, hitPos.getZ() + 0.5,
							10, 0.5, 0.5, 0.5, 0.05);
					return true;
				}
			}

			// Если зажигалка не может быть использована, пробуем поставить огонь на соседнем блоке
			BlockPos firePos = hitPos.relative(blockHit.getDirection());

			if (BaseFireBlock.canBePlacedAt(level, firePos, Direction.UP)) {
				level.setBlockAndUpdate(firePos, BaseFireBlock.getState(level, firePos));

				// Звук и частицы
				level.playSound(null, hitPos, RPGSounds.EMBER_GEM_IGNITE_BLOCK.get(),
						SoundSource.BLOCKS, 1.0F, level.random.nextFloat() * 0.4F + 0.8F);

				if (level instanceof ServerLevel serverLevel) {
					serverLevel.sendParticles(ParticleTypes.FLAME,
							hitPos.getX() + 0.5, hitPos.getY() + 0.5, hitPos.getZ() + 0.5,
							10, 0.5, 0.5, 0.5, 0.05);
				}
			}
			return true;
		}

		// Проверка столкновения с существами
		List<Entity> entities = level.getEntities(null,
				new net.minecraft.world.phys.AABB(startPos, endPos).inflate(0.3));

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
					if(owner != null)
						entity.hurt(owner.damageSources().fireball(fakeFireball, owner), 1F);

					// Звук и частицы
					level.playSound(null, entity.getX(), entity.getY(), entity.getZ(),
							RPGSounds.EMBER_GEM_IGNITE_ENTITY.get(), SoundSource.NEUTRAL,
							1.0F, 1.0F);

					serverLevel.sendParticles(ParticleTypes.FLAME,
							entity.getX(), entity.getY() + entity.getBbHeight() / 2, entity.getZ(),
							15, 0.5, 0.5, 0.5, 0.05);
					return true;
				}
			}
		}

		return false;
	}
}