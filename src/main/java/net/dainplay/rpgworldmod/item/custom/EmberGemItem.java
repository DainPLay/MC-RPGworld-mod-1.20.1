package net.dainplay.rpgworldmod.item.custom;

import com.mojang.authlib.GameProfile;
import net.dainplay.rpgworldmod.block.ModBlocks;
import net.dainplay.rpgworldmod.network.ClientManaData;
import net.dainplay.rpgworldmod.network.PlayerManaProvider;
import net.dainplay.rpgworldmod.sounds.RPGSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
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
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.TntBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.fml.DistExecutor;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

public class EmberGemItem extends Item implements RPGtooltip, ManaCostItem, OrbitingItem {

	private static final Map<Level, Map<UUID, EmberProjectileData>> activeProjectiles = new HashMap<>();
	private final int manacost;
	private final int color;
	private final String texture;
	private final int animationSpeed;
	private final int animationLength;

	public EmberGemItem(Properties pProperties, int cost, int color, String texture, int animationSpeed, int animationLength) {
		super(pProperties);
		this.manacost = cost;
		this.color = color;
		this.texture = texture;
		this.animationSpeed = animationSpeed;
		this.animationLength = animationLength;
	}

	@Override
	public String getTexture(ItemStack stack, Entity entity) {
		return texture;
	}

	@Override
	public int getAnimationSpeed(ItemStack stack, Entity entity) {
		return animationSpeed;
	}

	@Override
	public int getAnimationLength(ItemStack stack, Entity entity) {
		return animationLength;
	}

	@Override
	public int getColor(ItemStack stack, Entity entity) {
		return color;
	}

	@Override
	public int getManaCost(ItemStack item, Player player) {
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
		DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
				ClientRPGtooltipHandler.appendHoverText(pStack, pLevel, pTooltip, pFlag, this)
		);
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
			player.getCooldowns().addCooldown(this, 20);

			Vec3 lookAngle = player.getLookAngle();
			Vec3 startPos = player.getEyePosition().add(lookAngle.scale(0.5));

			EmberProjectileData projectile = new EmberProjectileData(
					player.getUUID(),
					startPos,
					lookAngle.scale(1.0),
					level.getGameTime()
			);

			getActiveProjectiles(level).put(UUID.randomUUID(), projectile);

			level.playSound(null, player.getX(), player.getY(), player.getZ(),
					RPGSounds.EMBER_GEM_SNAP.get(), SoundSource.PLAYERS,
					0.5F, 0.8F + level.random.nextFloat() * 0.4F);
		} else {
			if (!player.getAbilities().instabuild) {
				boolean hasEnoughMana = DistExecutor.unsafeCallWhenOn(Dist.CLIENT, () -> () -> ClientManaData.get() >= manacost);
				if (!hasEnoughMana) return InteractionResultHolder.fail(itemstack);
			}
		}

		player.gameEvent(GameEvent.PROJECTILE_SHOOT);
		return InteractionResultHolder.sidedSuccess(itemstack, level.isClientSide());
	}

	private static Map<UUID, EmberProjectileData> getActiveProjectiles(Level level) {
		return activeProjectiles.computeIfAbsent(level, k -> new HashMap<>());
	}

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

	public static void processProjectilesStatic(ServerLevel level) {
		Map<UUID, EmberProjectileData> levelActiveProjectiles = getActiveProjectiles(level);

		if (levelActiveProjectiles.isEmpty()) return;

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

			if (checkWaterContact(level, projectile.position)) {
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

			projectile.position = projectile.position.add(projectile.velocity);

			if (checkCollisions(level, projectile, projectileId)) {
				levelActiveProjectiles.remove(projectileId);
			}

			level.sendParticles(ParticleTypes.FLAME,
					projectile.position.x, projectile.position.y, projectile.position.z,
					1, 0.1, 0.1, 0.1, 0.01);
			level.sendParticles(ParticleTypes.SMOKE,
					projectile.position.x, projectile.position.y, projectile.position.z,
					1, 0.05, 0.05, 0.05, 0.005);
		}
	}

	private static boolean checkWaterContact(Level level, Vec3 position) {
		BlockPos pos = new BlockPos(
				(int) Math.floor(position.x),
				(int) Math.floor(position.y),
				(int) Math.floor(position.z)
		);

		FluidState fluidState = level.getFluidState(pos);
		if (fluidState.is(FluidTags.WATER)) {
			return true;
		}

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

	private static boolean checkCollisions(Level level, EmberProjectileData projectile, UUID projectileId) {
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

			if (level.getFluidState(hitPos).is(FluidTags.WATER)) {
				level.playSound(null, hitPos, RPGSounds.EMBER_GEM_EXTINGUISH.get(),
						SoundSource.BLOCKS, 0.5F, 1.0F);

				if (level instanceof ServerLevel serverLevel) {
					serverLevel.sendParticles(ParticleTypes.SMOKE,
							hitPos.getX() + 0.5, hitPos.getY() + 0.5, hitPos.getZ() + 0.5,
							10, 0.3, 0.3, 0.3, 0.05);
				}
				return true;
			}

			if (hitState.is(ModBlocks.ARBOR_FUEL_BLOCK.get())) {

				BlockState fireState = BaseFireBlock.getState(level, hitPos);
				if (BaseFireBlock.canBePlacedAt(level, hitPos, Direction.UP)) {
					level.setBlockAndUpdate(hitPos, fireState);
				} else level.setBlockAndUpdate(hitPos, Blocks.AIR.defaultBlockState());

				level.playSound(null, hitPos, RPGSounds.EMBER_GEM_IGNITE_BLOCK.get(),
						SoundSource.BLOCKS, 1.0F, level.random.nextFloat() * 0.4F + 0.8F);

				if (level instanceof ServerLevel serverLevel) {
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
				if (level instanceof ServerLevel serverLevel) {
					SmallFireball fireProjectile = new SmallFireball(
							serverLevel,
							projectile.position.x,
							projectile.position.y,
							projectile.position.z,
							projectile.velocity.x,
							projectile.velocity.y,
							projectile.velocity.z
					);

					if (projectile.ownerId != null) {
						Entity owner = serverLevel.getEntity(projectile.ownerId);
						if (owner != null) {
							fireProjectile.setOwner(owner);
						}
					}

					fireProjectile.setSecondsOnFire(100);

					tnt.onProjectileHit(level, hitState, blockHit, fireProjectile);

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

			ItemStack flintAndSteel = new ItemStack(Items.FLINT_AND_STEEL);

			if (level instanceof ServerLevel serverLevel) {
				FakePlayer fakePlayer = FakePlayerFactory.get(serverLevel, new GameProfile(UUID.randomUUID(), "FakePlayer"));

				fakePlayer.setPos(hitPos.getX(), hitPos.getY(), hitPos.getZ());

				fakePlayer.setYRot(blockHit.getDirection().toYRot());
				fakePlayer.setXRot((float) Math.toDegrees(blockHit.getDirection().toYRot()));

				fakePlayer.setItemInHand(InteractionHand.MAIN_HAND, flintAndSteel.copy());

				UseOnContext context = new UseOnContext(fakePlayer, InteractionHand.MAIN_HAND, blockHit);

				InteractionResult useResult = InteractionResult.PASS;
				if (flintAndSteel.getItem() instanceof FlintAndSteelItem flintAndSteelItem) {
					useResult = flintAndSteelItem.useOn(context);
				}

				fakePlayer.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);

				if (useResult.consumesAction()) {
					level.playSound(null, hitPos, RPGSounds.EMBER_GEM_IGNITE_BLOCK.get(),
							SoundSource.BLOCKS, 1.0F, level.random.nextFloat() * 0.4F + 0.8F);

					serverLevel.sendParticles(ParticleTypes.FLAME,
							hitPos.getX() + 0.5, hitPos.getY() + 0.5, hitPos.getZ() + 0.5,
							10, 0.5, 0.5, 0.5, 0.05);
					return true;
				}
			}

			BlockPos firePos = hitPos.relative(blockHit.getDirection());

			if (BaseFireBlock.canBePlacedAt(level, firePos, Direction.UP)) {
				level.setBlockAndUpdate(firePos, BaseFireBlock.getState(level, firePos));

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

					livingEntity.setSecondsOnFire(5);
					if (owner != null)
						entity.hurt(owner.damageSources().fireball(fakeFireball, owner), 1F);

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

	@Override
	public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
		return !ItemStack.isSameItem(oldStack, newStack);
	}
}