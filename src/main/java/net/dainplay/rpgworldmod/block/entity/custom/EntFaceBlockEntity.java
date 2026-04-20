package net.dainplay.rpgworldmod.block.entity.custom;

import net.dainplay.rpgworldmod.RPGworldMod;
import net.dainplay.rpgworldmod.block.ModBlocks;
import net.dainplay.rpgworldmod.block.custom.EntFaceBlock;
import net.dainplay.rpgworldmod.block.custom.LivingWoodLogBlock;
import net.dainplay.rpgworldmod.block.custom.Looking;
import net.dainplay.rpgworldmod.block.custom.RieLeavesBlock;
import net.dainplay.rpgworldmod.block.entity.ModBlockEntities;
import net.dainplay.rpgworldmod.damage.ModDamageTypes;
import net.dainplay.rpgworldmod.data.tags.ModAdvancements;
import net.dainplay.rpgworldmod.entity.projectile.EntRieFruitProjectile;
import net.dainplay.rpgworldmod.entity.projectile.EntRootsEntity;
import net.dainplay.rpgworldmod.network.PlayerIllusionForceProvider;
import net.dainplay.rpgworldmod.particle.ModParticles;
import net.dainplay.rpgworldmod.sounds.RPGSounds;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

public class EntFaceBlockEntity extends BlockEntity {
	private int tickCounter = 0;
	private static final int UPDATE_INTERVAL = 2;
	private static final double MAX_DISTANCE = 40.0;
	private static final int SCAN_RADIUS = 10;
	private Direction lastFacing = Direction.NORTH;
	private Looking lastLooking = Looking.STRAIGHT;
	private final List<BlockPos> relatedBlocks = new ArrayList<>();
	private boolean hasScanned = false;


	private float destroyProgress = 0.0f;
	private static final float DESTROY_DECAY_RATE = 0.0005f;
	private static final float BASE_DESTROY_INCREMENT = 0.005f;
	private static final float TOOL_MULTIPLIER = 0.015f;
	private static final float ENCHANTMENT_MULTIPLIER = 0.01f;
	private static final int TRUNK_MAX_HEIGHT = 12;
	private static final int TRUNK_MAX_RADIUS = 6;
	private static final int FOLIAGE_MAX_RADIUS = 4;
	private static final int FOLIAGE_HEIGHT_VARIATION = 5;


	private int attackCooldown = 0;
	private static final int MIN_ATTACK_COOLDOWN = 80;
	private static final int MAX_ATTACK_COOLDOWN = 120;
	private static final int LEAF_ATTACK_RANGE = 12;
	private static final int FORCE_LOOK_RANGE = 40;
	private static final int FORCE_LOOK_DURATION = 400;
	private static final int FORCE_LOOK_WINDUP = 40;
	private static final int FORCE_LOOK_COOLDOWN = 300;
	private int forceLookCooldown = 0;
	private UUID forceLookTarget = null;


	private final List<BlockPos> availableLeafPositions = new ArrayList<>();
	private int leafAttackTimer = 0;
	private static final int LEAF_ATTACK_DURATION = 60;
	private int leafSpawnTimer = 0;
	private static final int LEAF_SPAWN_INTERVAL = 2;


	private int meleeAttackTimer = 0;
	private static final int MELEE_ATTACK_WINDUP = 60;
	private static final double MELEE_ATTACK_RADIUS = 2.0;


	private int forceLookTimer = 0;


	private int noPlayerTimer = 0;
	private static final int SLEEP_DELAY = 20 * 20;


	private int ignoreVisionTimer = 0;
	private static final int IGNORE_VISION_DURATION = 20 * 5;


	private int patrolTimer = 0;
	private int lookingCycleTimer = 0;
	private static final int PATROL_DIRECTION_CHANGE_INTERVAL = 20 * 5;
	private static final int LOOKING_CYCLE_INTERVAL = 10;
	private boolean isPatrolling = false;
	private Looking[] lookingCycle = {Looking.LEFT, Looking.STRAIGHT, Looking.RIGHT, Looking.STRAIGHT};
	private int currentLookingIndex = 0;

	public EntFaceBlockEntity(BlockPos pWorldPosition, BlockState pBlockState) {
		super(ModBlockEntities.ENT_FACE_BLOCK_ENTITY.get(), pWorldPosition, pBlockState);
		this.lastFacing = pBlockState.getValue(EntFaceBlock.FACING);
		this.lastLooking = pBlockState.getValue(EntFaceBlock.LOOKING);
	}

	public void tick() {
		if (level == null || level.isClientSide) return;

		tickCounter++;


		updateAttacks();


		if (ignoreVisionTimer > 0) {
			ignoreVisionTimer--;
		}


		updatePatrolConditions();


		if (isPatrolling) {
			updatePatrol();
		}

		if (tickCounter % UPDATE_INTERVAL == 0 && !isPatrolling) {
			updateFacing();
		}


		updateSleepConditions();
		updateDestroyProgress();
		updateDestroyStage();


		if (!hasScanned) {
			scanAndSaveRelatedBlocks();
			hasScanned = true;
		}


		BlockState state = getBlockState();
		boolean shouldBeAttacking = meleeAttackTimer > 9 || forceLookTimer > 0;
		if (state.getValue(EntFaceBlock.IS_ATTACKING) != shouldBeAttacking) {
			level.setBlockAndUpdate(worldPosition,
					state.setValue(EntFaceBlock.IS_ATTACKING, shouldBeAttacking));
		}
	}

	private void updatePatrolConditions() {
		if (level == null) return;

		BlockState state = getBlockState();


		if (!state.getValue(EntFaceBlock.ASLEEP) &&
				ignoreVisionTimer == 0 &&
				findNearestPlayer(MAX_DISTANCE) == null) {
			if (!isPatrolling) {
				startPatrolling();
			}
		} else {
			if (isPatrolling) {
				stopPatrolling();
			}
		}
	}

	private void startPatrolling() {
		isPatrolling = true;
		patrolTimer = 0;
		lookingCycleTimer = 0;
		currentLookingIndex = 0;


		updateLookingInCycle();
	}

	private void stopPatrolling() {
		isPatrolling = false;
		patrolTimer = 0;
		lookingCycleTimer = 0;


		BlockState state = getBlockState();
		if (state.getValue(EntFaceBlock.LOOKING) != Looking.STRAIGHT) {
			level.setBlockAndUpdate(worldPosition, state.setValue(EntFaceBlock.LOOKING, Looking.STRAIGHT));
			lastLooking = Looking.STRAIGHT;
		}
	}

	private void updatePatrol() {
		if (level == null) return;

		patrolTimer++;
		lookingCycleTimer++;


		if (patrolTimer >= PATROL_DIRECTION_CHANGE_INTERVAL) {
			patrolTimer = 0;
			setRandomFacing();
		}


		if (lookingCycleTimer >= LOOKING_CYCLE_INTERVAL) {
			lookingCycleTimer = 0;
			currentLookingIndex = (currentLookingIndex + 1) % lookingCycle.length;
			updateLookingInCycle();
		}
	}

	private void setRandomFacing() {
		if (level == null) return;


		Direction currentFacing = getBlockState().getValue(EntFaceBlock.FACING);


		Direction leftDirection;
		Direction rightDirection;

		switch (currentFacing) {
			case NORTH:
				leftDirection = Direction.WEST;
				rightDirection = Direction.EAST;
				break;
			case EAST:
				leftDirection = Direction.NORTH;
				rightDirection = Direction.SOUTH;
				break;
			case SOUTH:
				leftDirection = Direction.EAST;
				rightDirection = Direction.WEST;
				break;
			case WEST:
				leftDirection = Direction.SOUTH;
				rightDirection = Direction.NORTH;
				break;
			default:
				leftDirection = Direction.NORTH;
				rightDirection = Direction.SOUTH;
		}


		Direction newFacing = level.random.nextBoolean() ? leftDirection : rightDirection;


		BlockState state = getBlockState();
		if (state.getValue(EntFaceBlock.FACING) != newFacing) {
			level.setBlockAndUpdate(worldPosition, state.setValue(EntFaceBlock.FACING, newFacing));
			lastFacing = newFacing;
		}
	}

	private boolean isEntInPlayerViewCone(Player player) {
		if (level == null || player.isCreative() || player.isSpectator()) return false;


		Vec3 entPos = new Vec3(
				worldPosition.getX() + 0.5,
				worldPosition.getY() + 1.0,
				worldPosition.getZ() + 0.5
		);


		Vec3 playerEyePos = player.getEyePosition();


		Vec3 toEnt = entPos.subtract(playerEyePos).normalize();


		Vec3 lookVec = player.getViewVector(1.0F).normalize();


		double dot = lookVec.dot(toEnt);
		double angle = Math.acos(dot) * (180.0 / Math.PI);


		float playerViewCone = 120.0f;


		boolean inViewCone = angle <= playerViewCone / 2;

		return inViewCone;
	}

	private void updateLookingInCycle() {
		if (level == null) return;

		Looking newLooking = lookingCycle[currentLookingIndex];


		BlockState state = getBlockState();
		if (state.getValue(EntFaceBlock.LOOKING) != newLooking) {
			level.setBlockAndUpdate(worldPosition, state.setValue(EntFaceBlock.LOOKING, newLooking));
			lastLooking = newLooking;
		}
	}

	private void updateAttacks() {
		if (forceLookCooldown > 0) forceLookCooldown--;

		if (tickCounter % 20 == 0) {
			checkAndPerformAttacks();
		}


		if (attackCooldown > 0) attackCooldown--;


		updateLeafAttack();


		updateMeleeAttack();


		updateForceLookAttack();
	}

	private void updateLeafAttack() {
		if (leafAttackTimer > 0) {
			leafAttackTimer--;


			leafSpawnTimer++;
			if (leafSpawnTimer >= LEAF_SPAWN_INTERVAL && !availableLeafPositions.isEmpty()) {
				leafSpawnTimer = 0;


				int spawnCount = Math.max(1, availableLeafPositions.size() * 10 / 100);

				for (int i = 0; i < spawnCount; i++) {
					BlockPos leafPos = availableLeafPositions.get(
							level.random.nextInt(availableLeafPositions.size()));


					BlockState leafState = level.getBlockState(leafPos);
					int entId = computeEntId(worldPosition);

					if (leafState.getBlock() instanceof RieLeavesBlock &&
							leafState.getValue(RieLeavesBlock.RELATED_TO_ENT) == entId) {
						BlockPos belowPos = leafPos.below();
						if (level.getBlockState(belowPos).isAir()) {
							double spread = 0.2;
							double dx = (level.random.nextDouble() - 0.5) * spread;
							double dz = (level.random.nextDouble() - 0.5) * spread;

							EntRieFruitProjectile projectile = new EntRieFruitProjectile(
									level,
									leafPos.getX() + 0.5 + dx,
									leafPos.getY() - 0.5,
									leafPos.getZ() + 0.5 + dz,
									worldPosition
							);

							projectile.setDeltaMovement(dx * 0.1, 0, dz * 0.1);
							level.addFreshEntity(projectile);
							if (level.random.nextFloat() < 0.5F) {
								double d0 = (double) belowPos.getX() + level.random.nextDouble();
								double d1 = (double) belowPos.getY() - 0.05D;
								double d2 = (double) belowPos.getZ() + level.random.nextDouble();
								((ServerLevel) level).sendParticles(ModParticles.LEAVES.get(), d0, d1, d2,
										3, 0, 0, 0, 0.0);
							}
						}
					}
				}
			}


			if (leafAttackTimer <= 0) {
				leafAttackTimer = 0;
				leafSpawnTimer = 0;
			}
		}
	}

	private void updateMeleeAttack() {
		if (meleeAttackTimer > 0) {
			meleeAttackTimer--;
			BlockState state = getBlockState();
			int currentTimer = 0;
			if (meleeAttackTimer > 9) currentTimer = 1;
			if (meleeAttackTimer > 20) currentTimer = 2;
			if (meleeAttackTimer > 22) currentTimer = 3;
			if (meleeAttackTimer > 53) currentTimer = 4;
			if (meleeAttackTimer > 55) currentTimer = 5;
			if (state.getValue(EntFaceBlock.MELEE_PROGRESS) != currentTimer) {
				level.setBlockAndUpdate(worldPosition, state.setValue(EntFaceBlock.MELEE_PROGRESS, currentTimer));
			}

			if (meleeAttackTimer == 20) {
				performMeleeDamage();
			}
		}
	}

	private void updateForceLookAttack() {
		if (forceLookTimer > 0) {
			forceLookTimer--;


			if (forceLookTimer > FORCE_LOOK_DURATION - FORCE_LOOK_WINDUP) {
				return;
			}


			if (forceLookTarget != null) {
				Player targetPlayer = level.getPlayerByUUID(forceLookTarget);
				if (targetPlayer != null) {
					boolean canSee = canSeePlayer(targetPlayer);


					if (canSee) {
						performForceLookEffect(targetPlayer);
					}


					if (isAnyPlayerTooClose()) {
						interruptForceLookAttack();
						return;
					}
				} else {
					interruptForceLookAttack();
				}
			}

			if (forceLookTimer <= 0) {
				endForceLookAttack();
			}
		}
	}

	private void performMeleeDamage() {
		if (level == null) return;

		level.playSound(null, worldPosition, RPGSounds.ENT_CLOSE_HOLLOW.get(), SoundSource.HOSTILE, 1.0F, (level.random.nextFloat() - level.random.nextFloat()) * 0.2F + 1.0F);
		level.gameEvent(null, GameEvent.BLOCK_CLOSE, worldPosition);
		AABB attackArea = new AABB(worldPosition)
				.inflate(MELEE_ATTACK_RADIUS)
				.setMinY(worldPosition.getY() - 1)
				.setMaxY(worldPosition.getY() + 2);

		List<Player> players = level.getEntitiesOfClass(Player.class, attackArea);
		for (Player player : players) {
			if (player.isCreative() || player.isSpectator()) {
				continue;
			}

			if (player.hurt(ModDamageTypes.getDamageSource(level, ModDamageTypes.ENT_SNAP), 30.0f))
				dealtDamage(player);

			Vec3 away = player.position()
					.subtract(worldPosition.getX() + 0.5,
							worldPosition.getY() + 0.5,
							worldPosition.getZ() + 0.5)
					.normalize()
					.scale(1.5);

			player.setDeltaMovement(away.x, 0.5, away.z);
		}
	}

	private void performForceLookEffect(Player player) {
		if (player.isCreative() || player.isSpectator()) {
			return;
		}

		if (isPlayerUnderOtherEntIllusion(player)) {
			return;
		}

		if (!isEntInPlayerViewCone(player)) {
			return;
		}

		if (net.minecraftforge.common.ForgeHooks.shouldSuppressEnderManAnger(null, player, player.getInventory().armor.get(3))) {
			return;
		}


		player.getCapability(PlayerIllusionForceProvider.PLAYER_ILLUSION_FORCE).ifPresent(illusionForce -> {
			if (player instanceof ServerPlayer serverPlayer) {
				illusionForce.setIllusionForce(serverPlayer, 3, true, true);

				illusionForce.setEntPosition(serverPlayer, true,
						(float) worldPosition.getX() + 0.5f,
						(float) worldPosition.getY() + 0.5f,
						(float) worldPosition.getZ() + 0.5f, true);
			}
		});
	}

	private boolean isPlayerUnderOtherEntIllusion(Player player) {
		AtomicBoolean result = new AtomicBoolean(false);
		player.getCapability(PlayerIllusionForceProvider.PLAYER_ILLUSION_FORCE).ifPresent(illusionForce -> {
			if (illusionForce.getIllusionForce() > 0) {
				BlockPos otherEntPos = BlockPos.containing(
						illusionForce.getEntPosX(),
						illusionForce.getEntPosY(),
						illusionForce.getEntPosZ()
				);

				if (!otherEntPos.equals(worldPosition)) {
					result.set(true);
				}
			}
		});
		return result.get();
	}

	private float normalizeAngle(float angle) {
		angle = angle % 360;
		if (angle > 180) {
			angle -= 360;
		} else if (angle < -180) {
			angle += 360;
		}
		return angle;
	}

	private void checkAndPerformAttacks() {
		if (level == null || level.isClientSide) return;

		BlockState state = getBlockState();
		if (state.getValue(EntFaceBlock.ASLEEP)) return;


		if (meleeAttackTimer > 0 || forceLookTimer > 0 || leafAttackTimer > 0) {
			return;
		}


		if (attackCooldown > 0) {
			return;
		}

		if (level.getDifficulty() == Difficulty.PEACEFUL) {
			level.playSound(null, worldPosition, RPGSounds.ENT_ATTACK.get(), SoundSource.HOSTILE, 1.0F, (level.random.nextFloat() - level.random.nextFloat()) * 0.2F + 1.0F);
			level.gameEvent(null, GameEvent.ENTITY_ROAR, worldPosition);
			attackCooldown = getRandomAttackCooldown();
			return;
		}

		Player nearestPlayer = findNearestPlayer(FORCE_LOOK_RANGE);
		if (nearestPlayer == null) return;

		double distance = nearestPlayer.distanceToSqr(
				worldPosition.getX() + 0.5,
				worldPosition.getY() + 0.5,
				worldPosition.getZ() + 0.5
		);


		if (distance < 5) {
			level.playSound(null, worldPosition, RPGSounds.ENT_ATTACK.get(), SoundSource.HOSTILE, 1.0F, (level.random.nextFloat() - level.random.nextFloat()) * 0.2F + 1.0F);
			level.gameEvent(null, GameEvent.ENTITY_ROAR, worldPosition);
			startMeleeAttack();
			attackCooldown = getRandomAttackCooldown() / 2;
			return;
		}


		if (distance <= LEAF_ATTACK_RANGE * LEAF_ATTACK_RANGE) {
			level.playSound(null, worldPosition, RPGSounds.ENT_ATTACK.get(), SoundSource.HOSTILE, 1.0F, (level.random.nextFloat() - level.random.nextFloat()) * 0.2F + 1.0F);
			level.gameEvent(null, GameEvent.ENTITY_ROAR, worldPosition);

			if (isPlayerUnderLeaves(nearestPlayer)) {
				startLeafAttack();
			}
			performRootAttack(nearestPlayer);
			attackCooldown = getRandomAttackCooldown();
			return;
		}


		boolean canForceLook = ignoreVisionTimer > 0 || isInViewCone(nearestPlayer, 180);
		if (distance <= FORCE_LOOK_RANGE * FORCE_LOOK_RANGE && canForceLook && forceLookCooldown == 0) {
			level.playSound(null, worldPosition, RPGSounds.ENT_ATTACK.get(), SoundSource.HOSTILE, 1.0F, (level.random.nextFloat() - level.random.nextFloat()) * 0.2F + 1.0F);
			level.gameEvent(null, GameEvent.ENTITY_ROAR, worldPosition);
			startForceLookAttack(nearestPlayer);
			attackCooldown = getRandomAttackCooldown();
			return;
		}
	}

	private boolean isPlayerUnderLeaves(Player player) {
		if (player.isCreative() || player.isSpectator()) {
			return false;
		}

		int entId = computeEntId(worldPosition);
		BlockPos playerPos = player.blockPosition();


		for (int i = 1; i <= 10; i++) {
			BlockPos checkPos = playerPos.above(i);
			BlockState state = level.getBlockState(checkPos);

			if (state.getBlock() instanceof RieLeavesBlock &&
					state.getValue(RieLeavesBlock.RELATED_TO_ENT) == entId) {
				return true;
			}
		}
		return false;
	}

	private void startLeafAttack() {
		leafAttackTimer = LEAF_ATTACK_DURATION;
		leafSpawnTimer = 0;
		level.playSound(null, new BlockPos(worldPosition.getX(), worldPosition.getY() + 5, worldPosition.getZ()), RPGSounds.ENT_SHAKE.get(), SoundSource.HOSTILE, 1.0F, (level.random.nextFloat() - level.random.nextFloat()) * 0.2F + 1.0F);
		level.gameEvent(null, GameEvent.ENTITY_SHAKE, worldPosition);
	}

	private void startMeleeAttack() {
		meleeAttackTimer = MELEE_ATTACK_WINDUP;
		level.playSound(null, worldPosition, RPGSounds.ENT_OPEN_HOLLOW.get(), SoundSource.HOSTILE, 1.0F, (level.random.nextFloat() - level.random.nextFloat()) * 0.2F + 1.0F);
		level.gameEvent(null, GameEvent.BLOCK_OPEN, worldPosition);


		BlockState state = getBlockState();
		if (!state.getValue(EntFaceBlock.IS_ATTACKING)) {
			level.setBlockAndUpdate(worldPosition,
					state.setValue(EntFaceBlock.IS_ATTACKING, true));
		}
	}

	private void startForceLookAttack(Player player) {
		if (player.isCreative() || player.isSpectator()) {
			return;
		}


		if (isPlayerUnderOtherEntIllusion(player)) {
			return;
		}

		forceLookTimer = FORCE_LOOK_DURATION;
		forceLookTarget = player.getUUID();


		BlockState state = getBlockState();
		if (!state.getValue(EntFaceBlock.IS_ATTACKING)) {
			level.setBlockAndUpdate(worldPosition,
					state.setValue(EntFaceBlock.IS_ATTACKING, true));
		}
	}

	private void interruptForceLookAttack() {
		forceLookTimer = 0;
		forceLookTarget = null;
		forceLookCooldown = FORCE_LOOK_COOLDOWN;
	}

	private void endForceLookAttack() {
		forceLookTimer = 0;
		forceLookTarget = null;
		forceLookCooldown = FORCE_LOOK_COOLDOWN;
	}

	private boolean isAnyPlayerTooClose() {
		if (level == null) return false;

		AABB searchArea = new AABB(worldPosition)
				.inflate(10)
				.setMinY(worldPosition.getY() - 5)
				.setMaxY(worldPosition.getY() + 5);

		List<Player> players = level.getEntitiesOfClass(Player.class, searchArea);

		for (Player player : players) {
			if (!player.isCreative() && !player.isSpectator()) {
				double distance = player.distanceToSqr(
						worldPosition.getX() + 0.5,
						worldPosition.getY() + 0.5,
						worldPosition.getZ() + 0.5
				);

				if (distance <= 25) {
					return true;
				}
			}
		}
		return false;
	}

	private void performRootAttack(Player player) {
		if (player.isCreative() || player.isSpectator()) {
			return;
		}

		double minY = Math.min(player.getY(), worldPosition.getY());
		double maxY = Math.max(player.getY(), worldPosition.getY()) + 1.0;
		float angle = (float) Math.atan2(
				player.getZ() - (worldPosition.getZ() + 0.5),
				player.getX() - (worldPosition.getX() + 0.5)
		);

		double distanceSqr = player.distanceToSqr(
				worldPosition.getX() + 0.5,
				worldPosition.getY() + 0.5,
				worldPosition.getZ() + 0.5
		);

		if (distanceSqr < 9.0) {
			for (int i = 0; i < 5; i++) {
				float adjustedAngle = angle + (float) i * (float) Math.PI * 0.4F;
				createRootEntity(
						worldPosition.getX() + 0.5 + Math.cos(adjustedAngle) * 1.5,
						worldPosition.getZ() + 0.5 + Math.sin(adjustedAngle) * 1.5,
						minY, maxY, adjustedAngle, 0
				);
			}
		} else {
			for (int i = 0; i < 8; i++) {
				double dist = 1.25 * (i + 1);
				createRootEntity(
						worldPosition.getX() + 0.5 + Math.cos(angle) * dist,
						worldPosition.getZ() + 0.5 + Math.sin(angle) * dist,
						minY, maxY, angle, i
				);
			}
		}
	}

	private void createRootEntity(double x, double z, double minY, double maxY, float yRot, int warmupDelay) {
		BlockPos pos = BlockPos.containing(x, maxY, z);
		boolean foundGround = false;
		double groundY = 0.0;


		do {
			BlockPos below = pos.below();
			BlockState state = level.getBlockState(below);

			if (state.isFaceSturdy(level, below, Direction.UP)) {
				if (!level.isEmptyBlock(pos)) {
					BlockState aboveState = level.getBlockState(pos);
					var shape = aboveState.getCollisionShape(level, pos);
					if (!shape.isEmpty()) {
						groundY = shape.max(Direction.Axis.Y);
					}
				}
				foundGround = true;
				break;
			}

			pos = pos.below();
		} while (pos.getY() >= Mth.floor(minY) - 1);

		if (foundGround) {
			EntRootsEntity roots = new EntRootsEntity(
					level,
					x,
					pos.getY() + groundY,
					z,
					yRot,
					warmupDelay,
					worldPosition
			);
			level.addFreshEntity(roots);
		}
	}

	private boolean isInViewCone(Player player, float angleDegrees) {
		Direction facing = getBlockState().getValue(EntFaceBlock.FACING);
		Vec3 facingVec = new Vec3(facing.getStepX(), 0, facing.getStepZ()).normalize();
		Vec3 toPlayer = player.position()
				.subtract(worldPosition.getX() + 0.5, 0, worldPosition.getZ() + 0.5)
				.normalize();

		double dot = facingVec.dot(toPlayer);
		double angle = Math.acos(dot) * (180.0 / Math.PI);

		return angle <= angleDegrees / 2;
	}

	private Player findNearestPlayer(double maxDistance) {
		if (level == null) return null;

		AABB searchArea = new AABB(worldPosition)
				.inflate(maxDistance)
				.setMinY(worldPosition.getY() - 8)
				.setMaxY(worldPosition.getY() + 8);

		List<Player> players = level.getEntitiesOfClass(Player.class, searchArea);
		if (players.isEmpty()) return null;


		players.removeIf(player -> player.isCreative() || player.isSpectator());


		if (ignoreVisionTimer > 0) {
			return players.stream()
					.min(Comparator.comparingDouble(p ->
							p.distanceToSqr(worldPosition.getX() + 0.5,
									worldPosition.getY() + 0.5,
									worldPosition.getZ() + 0.5)))
					.orElse(null);
		} else {
			players.removeIf(player -> !canSeePlayer(player));

			if (players.isEmpty()) return null;

			return players.stream()
					.min(Comparator.comparingDouble(p ->
							p.distanceToSqr(worldPosition.getX() + 0.5,
									worldPosition.getY() + 0.5,
									worldPosition.getZ() + 0.5)))
					.orElse(null);
		}
	}

	private boolean canSeePlayer(Player player) {
		if (level == null || player.isCreative() || player.isSpectator()) return false;


		BlockState state = getBlockState();
		Direction facing = state.getValue(EntFaceBlock.FACING);


		Vec3 blockCenter = new Vec3(
				worldPosition.getX() + 0.5,
				worldPosition.getY() + 0.5,
				worldPosition.getZ() + 0.5
		);


		Vec3 eyePos = blockCenter.add(
				facing.getStepX() * 0.5,
				0,
				facing.getStepZ() * 0.5
		);


		Vec3 playerEyePos = player.getEyePosition();


		double distance = eyePos.distanceTo(playerEyePos);
		if (distance > FORCE_LOOK_RANGE) {
			return false;
		}


		if (!isInViewCone(player, 180)) {
			return false;
		}


		net.minecraft.world.level.ClipContext context = new net.minecraft.world.level.ClipContext(eyePos, playerEyePos,
				net.minecraft.world.level.ClipContext.Block.COLLIDER, net.minecraft.world.level.ClipContext.Fluid.NONE, null);

		net.minecraft.world.phys.BlockHitResult hitResult = level.clip(context);


		if (hitResult.getType() != net.minecraft.world.phys.HitResult.Type.MISS) {
			double distanceToHit = hitResult.getLocation().distanceTo(eyePos);
			double distanceToPlayer = playerEyePos.distanceTo(eyePos);


			if (distanceToHit < distanceToPlayer - 0.1) {
				return false;
			}
		}

		return true;
	}

	private Player findNearestPlayer() {
		return findNearestPlayer(MAX_DISTANCE);
	}

	@Override
	protected void saveAdditional(CompoundTag tag) {
		super.saveAdditional(tag);
		tag.putInt("ForceLookCooldown", forceLookCooldown);
		if (forceLookTarget != null) {
			tag.putUUID("ForceLookTarget", forceLookTarget);
		}


		ListTag blocksTag = new ListTag();
		for (BlockPos pos : relatedBlocks) {
			blocksTag.add(LongTag.valueOf(pos.asLong()));
		}
		tag.put("RelatedBlocks", blocksTag);
		tag.putBoolean("HasScanned", hasScanned);


		tag.putFloat("DestroyProgress", destroyProgress);


		tag.putInt("NoPlayerTimer", noPlayerTimer);
		tag.putInt("AttackCooldown", attackCooldown);


		tag.putInt("IgnoreVisionTimer", ignoreVisionTimer);


		tag.putBoolean("IsPatrolling", isPatrolling);
		tag.putInt("PatrolTimer", patrolTimer);
		tag.putInt("LookingCycleTimer", lookingCycleTimer);
		tag.putInt("CurrentLookingIndex", currentLookingIndex);


		tag.putInt("LeafAttackTimer", leafAttackTimer);
		tag.putInt("LeafSpawnTimer", leafSpawnTimer);
		tag.putInt("MeleeAttackTimer", meleeAttackTimer);
		tag.putInt("ForceLookTimer", forceLookTimer);


		ListTag leafPositionsTag = new ListTag();
		for (BlockPos pos : availableLeafPositions) {
			leafPositionsTag.add(LongTag.valueOf(pos.asLong()));
		}
		tag.put("AvailableLeafPositions", leafPositionsTag);
	}

	@Override
	public void load(CompoundTag tag) {
		super.load(tag);
		forceLookCooldown = tag.getInt("ForceLookCooldown");
		if (tag.hasUUID("ForceLookTarget")) {
			forceLookTarget = tag.getUUID("ForceLookTarget");
		}


		relatedBlocks.clear();
		ListTag blocksTag = tag.getList("RelatedBlocks", Tag.TAG_LONG);
		for (int i = 0; i < blocksTag.size(); i++) {
			Tag element = blocksTag.get(i);
			if (element.getId() == Tag.TAG_LONG) {
				relatedBlocks.add(BlockPos.of(((LongTag) element).getAsLong()));
			}
		}
		hasScanned = tag.getBoolean("HasScanned");


		destroyProgress = tag.getFloat("DestroyProgress");


		noPlayerTimer = tag.getInt("NoPlayerTimer");
		attackCooldown = tag.getInt("AttackCooldown");


		ignoreVisionTimer = tag.getInt("IgnoreVisionTimer");


		isPatrolling = tag.getBoolean("IsPatrolling");
		patrolTimer = tag.getInt("PatrolTimer");
		lookingCycleTimer = tag.getInt("LookingCycleTimer");
		currentLookingIndex = tag.getInt("CurrentLookingIndex");


		leafAttackTimer = tag.getInt("LeafAttackTimer");
		leafSpawnTimer = tag.getInt("LeafSpawnTimer");
		meleeAttackTimer = tag.getInt("MeleeAttackTimer");
		forceLookTimer = tag.getInt("ForceLookTimer");


		availableLeafPositions.clear();
		ListTag leafPositionsTag = tag.getList("AvailableLeafPositions", Tag.TAG_LONG);
		for (int i = 0; i < leafPositionsTag.size(); i++) {
			Tag element = leafPositionsTag.get(i);
			if (element.getId() == Tag.TAG_LONG) {
				availableLeafPositions.add(BlockPos.of(((LongTag) element).getAsLong()));
			}
		}
	}

	private void scanAndSaveRelatedBlocks() {
		if (level == null || level.isClientSide()) return;

		int entId = computeEntId(worldPosition);
		Set<BlockPos> scannedLogs = new HashSet<>();
		Set<BlockPos> scannedLeaves = new HashSet<>();


		scanTrunkBlocks(entId, scannedLogs);


		scanFoliageBlocks(scannedLogs, entId, scannedLeaves);


		relatedBlocks.clear();
		relatedBlocks.addAll(scannedLogs);
		relatedBlocks.addAll(scannedLeaves);


		availableLeafPositions.clear();
		availableLeafPositions.addAll(scannedLeaves);

		hasScanned = true;
		setChanged();
	}

	private void scanTrunkBlocks(int entId, Set<BlockPos> scannedLogs) {
		int minY = worldPosition.getY() - TRUNK_MAX_HEIGHT;
		int maxY = worldPosition.getY() + TRUNK_MAX_HEIGHT;
		int radius = TRUNK_MAX_RADIUS;

		for (int y = minY; y <= maxY; y++) {
			for (int dx = -radius; dx <= radius; dx++) {
				for (int dz = -radius; dz <= radius; dz++) {
					if (dx * dx + dz * dz > radius * radius) continue;

					BlockPos checkPos = new BlockPos(worldPosition.getX() + dx, y, worldPosition.getZ() + dz);
					BlockState state = level.getBlockState(checkPos);

					if (state.getBlock() instanceof LivingWoodLogBlock) {
						if (state.getValue(LivingWoodLogBlock.RELATED_TO_ENT) == 0) {
							level.setBlock(checkPos, state.setValue(LivingWoodLogBlock.RELATED_TO_ENT, entId), 3);
							scannedLogs.add(checkPos.immutable());
						}
					}
				}
			}
		}
	}

	private void scanFoliageBlocks(Set<BlockPos> logPositions, int entId, Set<BlockPos> scannedLeaves) {
		for (BlockPos logPos : logPositions) {
			int minY = logPos.getY() - FOLIAGE_HEIGHT_VARIATION;
			int maxY = logPos.getY() + FOLIAGE_HEIGHT_VARIATION;
			int radius = FOLIAGE_MAX_RADIUS;

			for (int y = minY; y <= maxY; y++) {
				for (int dx = -radius; dx <= radius; dx++) {
					for (int dz = -radius; dz <= radius; dz++) {
						if (dx * dx + dz * dz > radius * radius) continue;

						BlockPos checkPos = new BlockPos(logPos.getX() + dx, y, logPos.getZ() + dz);
						if (scannedLeaves.contains(checkPos)) continue;

						BlockState state = level.getBlockState(checkPos);

						if (state.getBlock() instanceof RieLeavesBlock) {
							if (state.getValue(RieLeavesBlock.SPAWNED_FOR_ENT) && state.getValue(RieLeavesBlock.RELATED_TO_ENT) == 0) {
								level.setBlock(checkPos,
										ModBlocks.RIE_LEAVES.get().defaultBlockState()
												.setValue(RieLeavesBlock.RELATED_TO_ENT, entId), 3);
								scannedLeaves.add(checkPos.immutable());
							}
						}
					}
				}
			}
		}
	}

	private void updateSleepConditions() {
		if (level == null) return;

		BlockState state = getBlockState();
		boolean isAsleep = state.getValue(EntFaceBlock.ASLEEP);

		if (isAsleep) {
			boolean isNight = !level.isDay();
			boolean isRaining = level.isRaining();

			if (isNight || isRaining) {
				wakeUp();
			}
		} else {
			Player nearestPlayer = findNearestPlayer();

			if (nearestPlayer == null) {
				noPlayerTimer++;


				boolean isDay = level.isDay();
				boolean isRaining = level.isRaining();

				if (isDay && !isRaining && noPlayerTimer >= SLEEP_DELAY) {
					fallAsleep();
				}
			} else {
				noPlayerTimer = 0;
			}
		}
	}

	public void wakeUp() {
		if (level == null) return;

		BlockState state = getBlockState();
		if (state.getValue(EntFaceBlock.ASLEEP)) {
			level.setBlockAndUpdate(worldPosition, state.setValue(EntFaceBlock.ASLEEP, false));
			level.playSound(null, worldPosition, RPGSounds.ENT_WAKE_UP.get(), SoundSource.HOSTILE, 2.0F, (level.random.nextFloat() - level.random.nextFloat()) * 0.2F + 1.0F);
			level.gameEvent(null, GameEvent.ENTITY_ROAR, worldPosition);
		}
		noPlayerTimer = 0;
		setChanged();
	}


	public boolean hasGoldenKill(ServerPlayer player) {
		PlayerAdvancements advancements = player.getAdvancements();

		Advancement silverAdvancement = player.server.getAdvancements().getAdvancement(RPGworldMod.prefix("kill_all_rie_weald_mobs"));
		AdvancementProgress silverProgress = advancements.getOrStartProgress(silverAdvancement);

		if (!silverProgress.isDone()) {
			return true;
		}

		Advancement advancement = player.server.getAdvancements().getAdvancement(RPGworldMod.prefix("golden_kill_all_rie_weald_mobs"));
		AdvancementProgress progress = advancements.getOrStartProgress(advancement);

		if (progress.isDone()) {
			return true;
		}

		Iterable<String> completedCriteria = progress.getCompletedCriteria();

		for (String criterionId : completedCriteria) {
			if (criterionId.equals("golden_kill_ent")) {
				return true;
			}
		}

		return false;
	}

	public void dealtDamage(Entity target) {
		if (level == null) return;

		BlockState state = getBlockState();
		if (!state.getValue(EntFaceBlock.DEALT_DAMAGE)) {
			if (target instanceof ServerPlayer player) {
				level.setBlockAndUpdate(worldPosition, state.setValue(EntFaceBlock.DEALT_DAMAGE, true));
				if (!hasGoldenKill(player))
					level.playSound(null, worldPosition, RPGSounds.GOLDEN_TOKEN_FAIL.get(), SoundSource.HOSTILE, 2.0F, 1.0F);
			}
		}

		setChanged();
	}

	private void fallAsleep() {
		if (level == null) return;

		BlockState state = getBlockState();
		if (!state.getValue(EntFaceBlock.ASLEEP)) {
			level.setBlockAndUpdate(worldPosition, state.setValue(EntFaceBlock.ASLEEP, true));
			level.playSound(null, worldPosition, RPGSounds.ENT_SLEEP.get(), SoundSource.HOSTILE, 1.0F, (level.random.nextFloat() - level.random.nextFloat()) * 0.2F + 1.0F);
			level.gameEvent(null, GameEvent.BLOCK_CLOSE, worldPosition);
		}


		destroyProgress = 0.0f;


		if (level.getBlockState(worldPosition).getValue(EntFaceBlock.DESTROY_STAGE) != 0) {
			level.setBlock(worldPosition, level.getBlockState(worldPosition)
					.setValue(EntFaceBlock.DESTROY_STAGE, 0), 3);
		}

		setChanged();
	}

	public void resetNoPlayerTimer() {
		noPlayerTimer = 0;
	}

	private void updateDestroyProgress() {
		if (level == null) return;

		BlockState state = getBlockState();
		if (state.getValue(EntFaceBlock.ASLEEP)) {
			return;
		}


		if (destroyProgress >= 2.0f && !level.isClientSide) {
			level.playSound(null, worldPosition, RPGSounds.ENT_DEATH.get(), SoundSource.HOSTILE, 1.0F, (level.random.nextFloat() - level.random.nextFloat()) * 0.2F + 1.0F);
			level.gameEvent(null, GameEvent.ENTITY_DIE, worldPosition);

			for (ServerPlayer serverplayer : level.getEntitiesOfClass(ServerPlayer.class, new AABB(worldPosition).inflate(10.0D, 5.0D, 10.0D))) {
				ModAdvancements.KILL_ENT_TRIGGER.trigger(serverplayer);
				if (!state.getValue(EntFaceBlock.DEALT_DAMAGE) && serverplayer.getLastDamageSource() == null) {
					ModAdvancements.GOLDEN_KILL_ENT.trigger(serverplayer);
				}
			}
			level.destroyBlock(worldPosition, true);
		}

		if (destroyProgress > 0) {
			destroyProgress = Math.max(0.0f, destroyProgress - DESTROY_DECAY_RATE);
		}

		setChanged();
	}

	private void updateDestroyStage() {
		if (level == null || level.isClientSide) return;

		BlockState state = getBlockState();
		int currentStage = state.getValue(EntFaceBlock.DESTROY_STAGE);
		int newStage = Mth.clamp((int) (destroyProgress * 5), 0, 10);

		if (currentStage != newStage) {
			level.setBlock(worldPosition, state.setValue(EntFaceBlock.DESTROY_STAGE, newStage), 3);
		}
	}

	public void activelyDestroy(Player player, ItemStack tool) {
		if (level == null || level.isClientSide) return;

		try {
			BlockState state = getBlockState();


			if (tool.isEmpty()) {
				tool = player.getMainHandItem();
			}

			float toolEfficiency = calculateToolEfficiency(tool, state);
			destroyProgress = Math.min(2.0f, destroyProgress + toolEfficiency);
			noPlayerTimer = 0;

			ignoreVisionTimer = IGNORE_VISION_DURATION;
			setChanged();

		} catch (Exception e) {
			RPGworldMod.LOGGER.error("Error in activelyDestroy: ", e);

			destroyProgress = Math.min(2.0f, destroyProgress + BASE_DESTROY_INCREMENT);
		}
	}


	private float calculateToolEfficiency(ItemStack tool, BlockState blockState) {
		float baseEfficiency = BASE_DESTROY_INCREMENT;


		if (isToolEffectiveAgainstWood(tool)) {
			baseEfficiency += TOOL_MULTIPLIER;


			if (tool.isEnchanted() && tool.getEnchantmentTags() != null) {
				int efficiencyLevel = getEfficiencyLevel(tool);
				if (efficiencyLevel > 0) {
					baseEfficiency += efficiencyLevel * ENCHANTMENT_MULTIPLIER;
				}
			}


			float digSpeed = tool.getDestroySpeed(blockState);
			if (digSpeed > 1.0f) {
				baseEfficiency *= Math.min(digSpeed / 2.0f, 2.5f);
			}
		}

		return baseEfficiency;
	}


	private boolean isToolEffectiveAgainstWood(ItemStack tool) {
		return tool.is(net.minecraft.tags.ItemTags.AXES);
	}


	private int getEfficiencyLevel(ItemStack tool) {
		if (tool.isEnchanted()) {
			var enchantments = tool.getAllEnchantments();
			for (var entry : enchantments.entrySet()) {
				if (entry.getKey() == net.minecraft.world.item.enchantment.Enchantments.BLOCK_EFFICIENCY) {
					return entry.getValue();
				}
			}
		}
		return 0;
	}

	public float getDestroyProgress() {
		return destroyProgress;
	}

	public void resetDestroyProgress() {
		destroyProgress = 0.0f;
		setChanged();
	}

	public static int computeEntId(BlockPos facePos) {
		int hash = Math.abs(facePos.getX() * 31 + facePos.getY() * 37 + facePos.getZ() * 41);
		return (hash % 128) + 1;
	}

	public static void scanAndSaveRelatedBlocks(Level level, BlockPos facePos) {
		if (level == null || level.isClientSide()) return;

		int entId = computeEntId(facePos);
		Set<BlockPos> scannedLogs = new HashSet<>();
		Set<BlockPos> scannedLeaves = new HashSet<>();


		scanTrunkBlocks(level, facePos, entId, scannedLogs);


		scanFoliageBlocks(level, scannedLogs, entId, scannedLeaves);


		BlockEntity blockEntity = level.getBlockEntity(facePos);
		if (blockEntity instanceof EntFaceBlockEntity entFace) {
			entFace.relatedBlocks.clear();
			entFace.relatedBlocks.addAll(scannedLogs);
			entFace.relatedBlocks.addAll(scannedLeaves);
			entFace.availableLeafPositions.clear();
			entFace.availableLeafPositions.addAll(scannedLeaves);
			entFace.hasScanned = true;
			entFace.setChanged();
		}
	}

	private static void scanTrunkBlocks(Level level, BlockPos facePos, int entId, Set<BlockPos> scannedLogs) {
		int minY = facePos.getY() - TRUNK_MAX_HEIGHT;
		int maxY = facePos.getY() + TRUNK_MAX_HEIGHT;
		int radius = TRUNK_MAX_RADIUS;


		for (int y = minY; y <= maxY; y++) {
			for (int dx = -radius; dx <= radius; dx++) {
				for (int dz = -radius; dz <= radius; dz++) {
					if (dx * dx + dz * dz > radius * radius) continue;

					BlockPos checkPos = new BlockPos(facePos.getX() + dx, y, facePos.getZ() + dz);
					BlockState state = level.getBlockState(checkPos);

					if (state.getBlock() instanceof LivingWoodLogBlock) {
						if (state.getValue(LivingWoodLogBlock.RELATED_TO_ENT) == 0) {
							level.setBlock(checkPos, state.setValue(LivingWoodLogBlock.RELATED_TO_ENT, entId), 3);
							scannedLogs.add(checkPos.immutable());
						}
					}
				}
			}
		}
	}

	private static void scanFoliageBlocks(Level level, Set<BlockPos> logPositions, int entId, Set<BlockPos> scannedLeaves) {
		for (BlockPos logPos : logPositions) {
			int minY = logPos.getY() - FOLIAGE_HEIGHT_VARIATION;
			int maxY = logPos.getY() + FOLIAGE_HEIGHT_VARIATION;
			int radius = FOLIAGE_MAX_RADIUS;

			for (int y = minY; y <= maxY; y++) {
				for (int dx = -radius; dx <= radius; dx++) {
					for (int dz = -radius; dz <= radius; dz++) {
						if (dx * dx + dz * dz > radius * radius) continue;

						BlockPos checkPos = new BlockPos(logPos.getX() + dx, y, logPos.getZ() + dz);


						if (scannedLeaves.contains(checkPos)) continue;

						BlockState state = level.getBlockState(checkPos);

						if (state.getBlock() instanceof RieLeavesBlock) {
							if (state.getValue(RieLeavesBlock.SPAWNED_FOR_ENT) && state.getValue(RieLeavesBlock.RELATED_TO_ENT) == 0) {
								level.setBlock(checkPos,
										ModBlocks.RIE_LEAVES.get().defaultBlockState()
												.setValue(RieLeavesBlock.RELATED_TO_ENT, entId), 3);
								scannedLeaves.add(checkPos.immutable());
							}
						}
					}
				}
			}
		}
	}

	public void onRelatedBlockAttacked(BlockPos attackedPos) {
		if (level == null || level.isClientSide) return;

		BlockState state = getBlockState();
		if (state.getValue(EntFaceBlock.ASLEEP)) {
			wakeUp();
		}

		ignoreVisionTimer = IGNORE_VISION_DURATION;
	}

	public static void destroyEntBlocks(Level level, BlockPos facePos) {
		if (level == null || level.isClientSide()) return;

		int entId = computeEntId(facePos);


		for (BlockPos pos : ((EntFaceBlockEntity) level.getBlockEntity(facePos)).relatedBlocks) {
			BlockState state = level.getBlockState(pos);

			if (state.getBlock() instanceof LivingWoodLogBlock) {
				if (state.getValue(LivingWoodLogBlock.RELATED_TO_ENT) == entId) {
					level.destroyBlock(pos, true);
				}
			} else if (state.getBlock() instanceof RieLeavesBlock) {
				if (state.getValue(RieLeavesBlock.RELATED_TO_ENT) == entId) {
					level.destroyBlock(pos, true);
				}
			}
		}


		level.destroyBlock(facePos, true);
	}

	private void updateFacing() {
		if (level == null) return;

		BlockState state = getBlockState();
		if (state.getValue(EntFaceBlock.ASLEEP)) {
			return;
		}

		Player nearestPlayer = findNearestPlayer();
		if (nearestPlayer == null) {
			return;
		}

		Vec3 blockCenter = new Vec3(
				worldPosition.getX() + 0.5,
				worldPosition.getY() + 0.5,
				worldPosition.getZ() + 0.5
		);

		Vec3 playerPos = nearestPlayer.position();
		double distance = blockCenter.distanceTo(playerPos);

		if (distance > MAX_DISTANCE) {
			return;
		}

		double dx = playerPos.x - blockCenter.x;
		double dz = playerPos.z - blockCenter.z;
		Direction newFacing = getHorizontalDirectionFromVector(dx, dz);
		Looking newLooking = calculateLooking(blockCenter, playerPos, newFacing);

		Direction currentFacing = state.getValue(EntFaceBlock.FACING);
		Looking currentLooking = state.getValue(EntFaceBlock.LOOKING);

		if (newFacing != currentFacing || newLooking != currentLooking) {
			BlockState newState = state
					.setValue(EntFaceBlock.FACING, newFacing)
					.setValue(EntFaceBlock.LOOKING, newLooking);
			level.setBlockAndUpdate(worldPosition, newState);
			this.lastFacing = newFacing;
			this.lastLooking = newLooking;
		}
	}

	private Direction getHorizontalDirectionFromVector(double dx, double dz) {
		double length = Math.sqrt(dx * dx + dz * dz);
		if (length < 0.0001) return lastFacing;

		dx /= length;
		dz /= length;
		double angle = Math.atan2(dz, dx) * (180.0 / Math.PI);
		angle = (angle + 360) % 360;

		if (angle >= 315 || angle < 45) {
			return Direction.EAST;
		} else if (angle >= 45 && angle < 135) {
			return Direction.SOUTH;
		} else if (angle >= 135 && angle < 225) {
			return Direction.WEST;
		} else {
			return Direction.NORTH;
		}
	}

	private Looking calculateLooking(Vec3 blockCenter, Vec3 playerPos, Direction facing) {
		Vec3 toPlayer = playerPos.subtract(blockCenter);
		Vec3 horizontal = new Vec3(toPlayer.x, 0, toPlayer.z).normalize();
		Vec3 facingVector = new Vec3(
				facing.getStepX(),
				0,
				facing.getStepZ()
		).normalize();

		double dot = horizontal.dot(facingVector);
		double det = horizontal.x * facingVector.z - horizontal.z * facingVector.x;
		double angle = Math.atan2(det, dot) * (180.0 / Math.PI);

		if (angle > 180) angle -= 360;
		if (angle < -180) angle += 360;

		if (angle >= -15 && angle <= 15) {
			return Looking.STRAIGHT;
		} else if (angle > 15 && angle <= 90) {
			return Looking.RIGHT;
		} else if (angle < -15 && angle >= -90) {
			return Looking.LEFT;
		} else {
			return angle > 0 ? Looking.RIGHT : Looking.LEFT;
		}
	}

	@Override
	public void onLoad() {
		super.onLoad();
		if (!level.isClientSide) {
			updateFacing();
		}
	}

	public List<BlockPos> getRelatedBlocks() {
		return relatedBlocks;
	}


	private int getRandomAttackCooldown() {
		if (level == null) {
			return MIN_ATTACK_COOLDOWN;
		}

		return MIN_ATTACK_COOLDOWN + level.random.nextInt(MAX_ATTACK_COOLDOWN - MIN_ATTACK_COOLDOWN + 1);
	}
}