package net.dainplay.rpgworldmod.item.custom;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import net.dainplay.rpgworldmod.RPGworldMod;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.portal.PortalInfo;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.ITeleporter;

import java.util.function.Function;
import java.util.function.Predicate;

public class GasbassItem extends Item {
	private static final ResourceKey<Biome> RIE_WEALD_KEY = ResourceKey.create(Registries.BIOME,
			new ResourceLocation(RPGworldMod.MOD_ID, "rie_weald"));
	private static final String USE_DURATION_TAG = "gasbass_use_duration";

	public GasbassItem(Item.Properties pProperties) {
		super(pProperties);
	}

	@Override
	public int getUseDuration(ItemStack stack) {
		if (stack.hasTag() && stack.getTag().contains(USE_DURATION_TAG)) {
			return stack.getTag().getInt(USE_DURATION_TAG);
		}
		return 32;
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);


		int duration = calculateUseDuration(player);
		CompoundTag tag = stack.getOrCreateTag();
		tag.putInt(USE_DURATION_TAG, duration);
		stack.setTag(tag);

		return super.use(level, player, hand);
	}

	private int calculateUseDuration(Player player) {
		boolean pvpCooldownDisabled = player.level().getGameRules().getBoolean(RPGworldMod.DISABLE_GASBASS_PVP_COOLDOWN);
		if (pvpCooldownDisabled) {
			return 32;
		} else {
			boolean recentlyHurt = player.getLastDamageSource() != null;
			return recentlyHurt ? 120 : 32;
		}
	}


	public ItemStack finishUsingItem(ItemStack pStack, Level pLevel, LivingEntity entity) {
		if (!pLevel.isClientSide()) {
			double teleportPosX;
			double teleportPosY;
			double teleportPosZ;
			ResourceKey<Level> teleportDimension;
			boolean isUnbound = false;

			if (pStack.hasTag() && pStack.getTag().contains("rpgworldmod.return_pos_x")) {
				teleportPosX = pStack.getTag().getDouble("rpgworldmod.return_pos_x");
				teleportPosY = pStack.getTag().getDouble("rpgworldmod.return_pos_y");
				teleportPosZ = pStack.getTag().getDouble("rpgworldmod.return_pos_z");
				teleportDimension = DimensionType.parseLegacy(new Dynamic<>(NbtOps.INSTANCE,
								pStack.getTag().get("rpgworldmod.return_pos_dimension")))
						.resultOrPartial(RPGworldMod.LOGGER::error)
						.orElse(entity.getCommandSenderWorld().dimension());
			} else {
				isUnbound = true;
				BlockPos targetPos = findNearestRieWeald(entity, (ServerLevel) pLevel);

				if (targetPos != null) {
					teleportPosX = targetPos.getX() + 0.5;
					teleportPosY = targetPos.getY() + 1.0;
					teleportPosZ = targetPos.getZ() + 0.5;
					teleportDimension = entity.getCommandSenderWorld().dimension();
				} else {
					teleportPosX = entity.getX();
					teleportPosY = entity.getY();
					teleportPosZ = entity.getZ();
					teleportDimension = entity.getCommandSenderWorld().dimension();
				}
			}

			ITeleporter teleporter = new ITeleporter() {
				@Override
				public PortalInfo getPortalInfo(Entity entity, ServerLevel destWorld,
												Function<ServerLevel, PortalInfo> defaultPortalInfo) {
					return new PortalInfo(new Vec3(teleportPosX, teleportPosY, teleportPosZ),
							Vec3.ZERO, entity.getYRot(), entity.getXRot());
				}

				@Override
				public Entity placeEntity(Entity entity, ServerLevel currentWorld, ServerLevel destWorld, float yaw,
										  Function<Boolean, Entity> repositionEntity) {
					return repositionEntity.apply(false);
				}

				@Override
				public boolean playTeleportSound(ServerPlayer player, ServerLevel sourceWorld, ServerLevel destWorld) {
					return false;
				}
			};

			if (entity instanceof ServerPlayer serverplayer) {
				if (serverplayer.connection.isAcceptingMessages() &&
						serverplayer.level() == pLevel && !serverplayer.isSleeping()) {
					if (serverplayer.isPassenger()) {
						serverplayer.dismountTo(serverplayer.getX(), serverplayer.getY(), serverplayer.getZ());
					}

					if (!serverplayer.level().dimension().equals(teleportDimension)) {
						ServerLevel targetLevel = serverplayer.getServer().getLevel(teleportDimension);
						if (targetLevel != null) {
							serverplayer.changeDimension(targetLevel, teleporter);
						}
					} else {
						serverplayer.teleportTo(teleportPosX, teleportPosY, teleportPosZ);
					}
					serverplayer.resetFallDistance();
				}
			} else if (entity != null) {
				if (entity.canChangeDimensions()) {
					if (!entity.level().dimension().equals(teleportDimension)) {
						ServerLevel targetLevel = entity.getServer().getLevel(teleportDimension);
						if (targetLevel != null) {
							entity.changeDimension(targetLevel, teleporter);
						}
					} else {
						entity.teleportTo(teleportPosX, teleportPosY, teleportPosZ);
					}
					entity.resetFallDistance();
				}
			}
			if (isUnbound) {
				if (!pLevel.isClientSide() && entity != null) {
					Level currentLevel = entity.level();
					if (currentLevel instanceof ServerLevel serverLevel) {
						BlockPos currentPos = entity.blockPosition();
						int highestY = findHighestSolidBlockY(serverLevel, currentPos.getX(), currentPos.getZ());
						if (highestY > serverLevel.getMinBuildHeight()) {
							double newY = highestY + 1.0;
							entity.teleportTo(currentPos.getX() + 0.5, newY, currentPos.getZ() + 0.5);
							entity.resetFallDistance();
						}
					}
				}
			}

		}

		ItemStack itemstack = super.finishUsingItem(pStack, pLevel, entity);
		emitSmokeParticles(entity);
		return itemstack;
	}

	private int findHighestSolidBlockY(ServerLevel level, int x, int z) {
		for (int y = level.getMaxBuildHeight() - 1; y > level.getMinBuildHeight(); y--) {
			BlockPos pos = new BlockPos(x, y, z);
			if (level.getBlockState(pos).isSolid()) {
				return y;
			}
		}
		return level.getMinBuildHeight();
	}

	private BlockPos findNearestRieWeald(LivingEntity entity, ServerLevel level) {
		BlockPos entityPos = new BlockPos(entity.blockPosition().getX(), 64, entity.blockPosition().getZ());

		Predicate<Holder<Biome>> biomePredicate = holder -> holder.unwrapKey()
				.map(key -> key.equals(RIE_WEALD_KEY))
				.orElse(false);

		int horizontalRadius = 1000;
		int verticalRadius = 100;
		int resolution = 4;

		Pair<BlockPos, Holder<Biome>> result = level.findClosestBiome3d(
				biomePredicate,
				entityPos,
				horizontalRadius,
				verticalRadius,
				resolution
		);

		if (result == null) {
			horizontalRadius = 10000;
			resolution = 16;
			result = level.findClosestBiome3d(
					biomePredicate,
					entityPos,
					horizontalRadius,
					verticalRadius,
					resolution
			);
		}

		if (result != null) {
			BlockPos biomePos = result.getFirst();

			if (Math.abs(entityPos.getX() - biomePos.getX()) > 500 ||
					Math.abs(entityPos.getZ() - biomePos.getZ()) > 500) {
				return findSafeSurfacePosition(level, biomePos);
			} else {
				int surfaceY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, biomePos.getX(), biomePos.getZ());

				if (surfaceY > level.getMinBuildHeight() && surfaceY < level.getMaxBuildHeight()) {
					return new BlockPos(biomePos.getX(), surfaceY, biomePos.getZ());
				} else {
					return findSafeSurfacePosition(level, biomePos);
				}
			}
		}

		return null;
	}

	private BlockPos findSafeSurfacePosition(ServerLevel level, BlockPos targetPos) {
		int surfaceY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, targetPos.getX(), targetPos.getZ());

		if (surfaceY <= level.getMinBuildHeight() + 1) {
			surfaceY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, targetPos.getX(), targetPos.getZ());
		}

		if (surfaceY <= level.getMinBuildHeight() + 1) {
			return findManualSurfacePosition(level, targetPos);
		}

		BlockPos spawnPos = new BlockPos(targetPos.getX(), surfaceY, targetPos.getZ());

		if (!level.getBlockState(spawnPos.below()).isSolid()) {
			for (int y = spawnPos.getY(); y > level.getMinBuildHeight(); y--) {
				BlockPos checkPos = new BlockPos(targetPos.getX(), y, targetPos.getZ());
				if (level.getBlockState(checkPos).isSolid() &&
						level.isEmptyBlock(checkPos.above()) &&
						level.isEmptyBlock(checkPos.above(2))) {
					return checkPos.above();
				}
			}
		}

		return spawnPos;
	}


	private BlockPos findManualSurfacePosition(ServerLevel level, BlockPos targetPos) {
		for (int y = level.getMaxBuildHeight() - 1; y > level.getMinBuildHeight(); y--) {
			BlockPos checkPos = new BlockPos(targetPos.getX(), y, targetPos.getZ());
			BlockPos belowPos = checkPos.below();
			if (level.getBlockState(belowPos).isSolid() &&
					level.isEmptyBlock(checkPos) &&
					level.isEmptyBlock(checkPos.above())) {
				return checkPos;
			}
		}

		return new BlockPos(targetPos.getX(), Math.max(64, level.getMinBuildHeight() + 1), targetPos.getZ());
	}

	private static void emitSmokeParticles(LivingEntity entity) {
		double x = entity.getX();
		double y = entity.getY() + entity.getEyeHeight() / 2.0;
		double z = entity.getZ();

		for (int i = 0; i < 10; i++) {
			double offsetX = (entity.getRandom().nextDouble() - 0.5) * 0.8;
			double offsetY = (entity.getRandom().nextDouble() - 0.5) * 0.4;
			double offsetZ = (entity.getRandom().nextDouble() - 0.5) * 0.8;

			entity.level().addParticle(
					ParticleTypes.LARGE_SMOKE,
					x + offsetX,
					y + offsetY,
					z + offsetZ,
					0.0, 0.02, 0.0
			);
		}
	}

	@Override
	public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
		return !ItemStack.isSameItem(oldStack, newStack);
	}
}