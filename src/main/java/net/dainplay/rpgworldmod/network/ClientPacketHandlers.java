package net.dainplay.rpgworldmod.network;

import net.dainplay.rpgworldmod.effect.ModEffects;
import net.dainplay.rpgworldmod.enchantment.ModEnchantments;
import net.dainplay.rpgworldmod.entity.custom.Razorleaf;
import net.dainplay.rpgworldmod.item.custom.NetherStarScrollItem;
import net.dainplay.rpgworldmod.sounds.PositionedLoopSound;
import net.dainplay.rpgworldmod.sounds.RPGSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@OnlyIn(Dist.CLIENT)
public class ClientPacketHandlers {
	public static void handleManaSync(int mana) {
		ClientManaData.set(mana);
	}

	public static void handleSculkStaffCDSync(int cooldown) {
		ClientSculkStaffCDData.set(cooldown);
	}

	public static void handleMaxManaSync(int maxMana) {
		ClientMaxManaData.set(maxMana);
	}

	public static void handleIsManaRegenBlockedSync(int blocked) {
		ClientIsManaRegenBlockedData.set(blocked);
	}

	public static void handlePortalEffect(double x, double y, double z) {
		Level level = Minecraft.getInstance().level;
		if (level == null) return;

		double centerX = x;
		double centerY = y;
		double centerZ = z;

		for (int k3 = 0; k3 < 8; ++k3) {
			for (double d12 = 0.0; d12 < Math.PI * 2; d12 += 0.15707963267948966) {
				double cos = Math.cos(d12);
				double sin = Math.sin(d12);
				level.addParticle(ParticleTypes.PORTAL,
						centerX + cos * 5.0, centerY - 0.4, centerZ + sin * 5.0,
						cos * -5.0, 0.0, sin * -5.0);
				level.addParticle(ParticleTypes.PORTAL,
						centerX + cos * 5.0, centerY - 0.4, centerZ + sin * 5.0,
						cos * -7.0, 0.0, sin * -7.0);
			}
		}
	}

	public static void handleSyncRazorleafData(int entityId, int state, int attackType, int attackTimer,
											   int tongueAnimationTime, Vec3 spitDirection, Vec3 pullDirection,
											   boolean hasItemInMouth) {
		Level level = Minecraft.getInstance().level;
		if (level == null) return;
		Entity entity = level.getEntity(entityId);
		if (entity instanceof Razorleaf razorleaf) {
			razorleaf.getEntityData().set(Razorleaf.DATA_STATE, state);
			razorleaf.getEntityData().set(Razorleaf.DATA_ATTACK_TYPE, attackType);
			razorleaf.getEntityData().set(Razorleaf.DATA_ATTACK_TIMER, attackTimer);
			razorleaf.getEntityData().set(Razorleaf.DATA_TONGUE_ANIMATION_TIME, tongueAnimationTime);
			razorleaf.getEntityData().set(Razorleaf.DATA_SPIT_DIRECTION_X, (float) spitDirection.x);
			razorleaf.getEntityData().set(Razorleaf.DATA_SPIT_DIRECTION_Y, (float) spitDirection.y);
			razorleaf.getEntityData().set(Razorleaf.DATA_SPIT_DIRECTION_Z, (float) spitDirection.z);
			razorleaf.getEntityData().set(Razorleaf.DATA_PULL_DIRECTION_X, (float) pullDirection.x);
			razorleaf.getEntityData().set(Razorleaf.DATA_PULL_DIRECTION_Y, (float) pullDirection.y);
			razorleaf.getEntityData().set(Razorleaf.DATA_PULL_DIRECTION_Z, (float) pullDirection.z);
			razorleaf.getEntityData().set(Razorleaf.DATA_HAS_ITEM_IN_MOUTH, hasItemInMouth);
		}
	}

	private static final long MAX_PULL_DURATION_MS = 5000L;
	private static final long PULL_GAP_RESET_MS = 500L;
	private static final double MAX_PULL_DISTANCE = 5.0;

	private static final Map<Integer, PullData> pullDataMap = new HashMap<>();

	private static class PullData {
		int sourceId;
		long startTime;
		long lastPacketTime;
	}

	public static void handleSyncEntityMotion(int entityId, int sourceEntityId, boolean isPull,
											  double motionX, double motionY, double motionZ) {
		Level level = Minecraft.getInstance().level;
		if (level == null) return;
		Entity entity = level.getEntity(entityId);
		if (entity == null) return;

		if (isPull) {
			Entity source = level.getEntity(sourceEntityId);
			if (!(source instanceof Razorleaf)) {
				pullDataMap.remove(entityId);
				return;
			}
			if (entity.distanceTo(source) > MAX_PULL_DISTANCE) {
				pullDataMap.remove(entityId);
				return;
			}
			long now = System.currentTimeMillis();
			PullData data = pullDataMap.get(entityId);
			if (data == null
					|| data.sourceId != sourceEntityId
					|| (now - data.lastPacketTime) > PULL_GAP_RESET_MS) {
				data = new PullData();
				data.sourceId = sourceEntityId;
				data.startTime = now;
				data.lastPacketTime = now;
				pullDataMap.put(entityId, data);
			} else if (now - data.startTime > MAX_PULL_DURATION_MS) {
				pullDataMap.remove(entityId);
				return;
			} else {
				data.lastPacketTime = now;
			}
		}

		entity.setDeltaMovement(motionX, motionY, motionZ);
	}

	public static void clearPullData(int playerEntityId) {
		pullDataMap.remove(playerEntityId);
	}

	public static void handleIllusionForceSync(int illusionForce, float entPositionX, float entPositionY, float entPositionZ, boolean isSet, boolean isEnt) {
		ClientIllusionForceData.set(illusionForce, isEnt);
		ClientEntPositionData.set(entPositionX, entPositionY, entPositionZ, isSet);
	}

	public static void handleBoundEntitySync(int entityId, boolean isRemoval, BoundEntitySyncPacket.BoundEntityData data) {
		if (isRemoval) {
			ClientBoundEntityData.removeEntity(entityId);
		} else {
			ClientBoundEntityData.updateEntity(data);
		}
	}

	public static void handlePullPlayer(Vec3 motion, int playerId) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.level == null || mc.player == null || mc.player.getId() != playerId) return;

		if (!mc.player.isPassenger()) {
			mc.player.setDeltaMovement(mc.player.getDeltaMovement().add(motion));
			mc.player.fallDistance = 0;
		}
	}

	public static void handlePullDownPlayer() {
		Minecraft mc = Minecraft.getInstance();
		if (mc.level == null || mc.player == null) return;

		if (!mc.player.isPassenger()) {
			Vec3 vec3 = mc.player.getDeltaMovement();
			mc.player.setDeltaMovement(vec3.x, vec3.y - 10D, vec3.z);
		}
	}

	public static void handleSwingPlayer(Vec3 motion, int playerId) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.level == null || mc.player == null || mc.player.getId() != playerId) return;

		if (!mc.player.isPassenger()) {
			mc.player.setDeltaMovement(mc.player.getDeltaMovement().add(motion));
			mc.player.fallDistance = 0;
			return;
		}

		ClientVelocityStorage.storeVelocity(playerId, motion);
	}

	public static void handleMoveParticles(Vec3 startPos, Vec3 velocity, ParticleOptions particleType,
										   boolean shouldCollide, double maxDistance) {
		Level level = Minecraft.getInstance().level;
		if (level != null) {
			spawnParticle(level, startPos, velocity, particleType, shouldCollide, maxDistance);
		}
	}

	private static void spawnParticle(Level level, Vec3 startPos, Vec3 velocity, ParticleOptions particleType,
									  boolean shouldCollide, double maxDistance) {
		if (shouldCollide) {
			spawnParticleWithCollisionCheck(level, startPos, velocity, particleType, maxDistance);
		} else {
			spawnParticleWithoutCollision(level, startPos, velocity, particleType);
		}
	}

	private static void spawnParticleWithCollisionCheck(Level level, Vec3 startPos, Vec3 velocity, ParticleOptions particleType, double maxDistance) {
		double speedLength = velocity.length();
		double timeInSeconds = 1.0;
		double ticksToFly = timeInSeconds * 20.0;
		double distanceToFly = speedLength * ticksToFly;
		Vec3 direction = velocity.normalize();
		Vec3 endPoint = startPos.add(direction.scale(distanceToFly));

		BlockHitResult result = level.clip(
				new net.minecraft.world.level.ClipContext(
						startPos,
						endPoint,
						net.minecraft.world.level.ClipContext.Block.COLLIDER,
						net.minecraft.world.level.ClipContext.Fluid.NONE,
						null
				)
		);

		double actualMaxDistance = Math.min(maxDistance, distanceToFly);
		if (result.getType() == HitResult.Type.BLOCK) {
			actualMaxDistance = startPos.distanceTo(result.getLocation());
		}
		if (actualMaxDistance < 0.1) return;

		double adjustedSpeed = (actualMaxDistance / 8.5) * 15 * 0.03;
		Vec3 adjustedVelocity = direction.scale(adjustedSpeed);

		for (int i = 0; i < 3; i++) {
			double offsetX = (level.random.nextDouble() - 0.5) * 0.3;
			double offsetY = (level.random.nextDouble() - 0.5) * 0.3;
			double offsetZ = (level.random.nextDouble() - 0.5) * 0.3;
			Vec3 particleStartPos = startPos.add(offsetX, offsetY, offsetZ);
			level.addParticle(particleType,
					particleStartPos.x, particleStartPos.y, particleStartPos.z,
					adjustedVelocity.x, adjustedVelocity.y, adjustedVelocity.z);
		}
	}

	private static void spawnParticleWithoutCollision(Level level, Vec3 startPos, Vec3 velocity, ParticleOptions particleType) {
		double speedMultiplier = 0.03;
		Vec3 particleVelocity = velocity.scale(speedMultiplier);
		for (int i = 0; i < 3; i++) {
			double offsetX = (level.random.nextDouble() - 0.5) * 0.3;
			double offsetY = (level.random.nextDouble() - 0.5) * 0.3;
			double offsetZ = (level.random.nextDouble() - 0.5) * 0.3;
			Vec3 particleStartPos = startPos.add(offsetX, offsetY, offsetZ);
			level.addParticle(particleType,
					particleStartPos.x, particleStartPos.y, particleStartPos.z,
					particleVelocity.x, particleVelocity.y, particleVelocity.z);
		}
	}

	public static class BeamSoundManager {
		private static final Map<Integer, List<PositionedLoopSound>> activeBeamSounds = new HashMap<>();

		public static void updateBeamSounds(int ownerId, Vec3 start, Vec3 direction,
											double length, int useTicks, boolean isActive) {
			Level level = Minecraft.getInstance().level;
			if (level == null) return;
			Entity entity = level.getEntity(ownerId);
			if (!(entity instanceof Player owner)) return;

			ItemStack usingItem = owner.getUseItem();
			boolean conditionMet = usingItem.getItem() instanceof NetherStarScrollItem
					&& usingItem.getEnchantmentLevel(ModEnchantments.DESTRUCTION.get()) > 0
					&& useTicks > 40;

			boolean shouldBeActive = conditionMet && isActive && length > 0.1;

			List<PositionedLoopSound> oldSounds = activeBeamSounds.remove(ownerId);
			if (oldSounds == null) oldSounds = new ArrayList<>();

			List<Vec3> newPositions = new ArrayList<>();
			if (length > 0.1) {
				int count = (int) Math.ceil(length - 1e-3);
				for (int i = 1; i <= count; i++) {
					newPositions.add(start.add(direction.scale(i)));
				}
			}

			List<PositionedLoopSound> newSounds = new ArrayList<>(newPositions.size());

			int maxSize = Math.max(oldSounds.size(), newPositions.size());
			for (int i = 0; i < maxSize; i++) {
				if (i < newPositions.size()) {
					if (i < oldSounds.size()) {
						// переиспользуем — звук уже в движке
						PositionedLoopSound sound = oldSounds.get(i);
						sound.setPosition(newPositions.get(i));
						sound.setActive(shouldBeActive);
						newSounds.add(sound);
					} else if (shouldBeActive) {
						PositionedLoopSound newSound = new PositionedLoopSound(
								RPGSounds.SPELL_DESTRUCTION_NETHER_STAR_LOOP.get(),
								ownerId, newPositions.get(i));
						newSound.setActive(true);
						Minecraft.getInstance().getSoundManager().play(newSound);
						newSounds.add(newSound);
					}
				} else {
					oldSounds.get(i).markForStop();
				}
			}

			activeBeamSounds.put(ownerId, newSounds);
		}
	}

	public static class SpecialBeamSoundManager {
		private static final Map<Integer, List<PositionedLoopSound>> activeEntitySounds = new HashMap<>();
		private static final Map<Integer, PositionedLoopSound> activeEndSound = new HashMap<>();
		private static final Map<Integer, PositionedLoopSound> activeOwnerSound = new HashMap<>();

		public static void updateSpecialSounds(int ownerId, List<Vec3> hitEntityPositions,
											   Vec3 endPos, int useTicks) {
			Level level = Minecraft.getInstance().level;
			if (level == null) return;
			Entity entity = level.getEntity(ownerId);
			if (!(entity instanceof Player owner)) return;

			List<PositionedLoopSound> oldEntitySounds = activeEntitySounds.remove(ownerId);
			if (oldEntitySounds == null) oldEntitySounds = new ArrayList<>();

			List<PositionedLoopSound> newEntitySounds = new ArrayList<>(hitEntityPositions.size());
			int maxSize = Math.max(oldEntitySounds.size(), hitEntityPositions.size());
			for (int i = 0; i < maxSize; i++) {
				if (i < hitEntityPositions.size()) {
					Vec3 pos = hitEntityPositions.get(i);
					if (i < oldEntitySounds.size()) {
						PositionedLoopSound sound = oldEntitySounds.get(i);
						sound.setPosition(pos);
						sound.setActive(true);
						sound.setVolume(1.0F);
						newEntitySounds.add(sound);
					} else {
						PositionedLoopSound newSound = new PositionedLoopSound(
								RPGSounds.SPELL_DESTRUCTION_NETHER_STAR_HIT_ENTITY.get(),
								ownerId, pos);
						newSound.setActive(true);
						newSound.setVolume(1.0F);
						Minecraft.getInstance().getSoundManager().play(newSound);
						newEntitySounds.add(newSound);
					}
				} else {
					oldEntitySounds.get(i).markForStop();
				}
			}
			activeEntitySounds.put(ownerId, newEntitySounds);

			PositionedLoopSound oldOwnerSound = activeOwnerSound.get(ownerId);
			boolean shouldHaveOwnerSound = !hitEntityPositions.isEmpty();
			if (shouldHaveOwnerSound) {
				Vec3 ownerPos = owner.position();
				if (oldOwnerSound != null) {
					oldOwnerSound.setPosition(ownerPos);
					oldOwnerSound.setActive(true);
					oldOwnerSound.setVolume(1.0F);
					activeOwnerSound.put(ownerId, oldOwnerSound);
				} else {
					PositionedLoopSound newSound = new PositionedLoopSound(
							RPGSounds.SPELL_DESTRUCTION_NETHER_STAR_HIT_ENTITY.get(),
							ownerId, ownerPos);
					newSound.setActive(true);
					newSound.setVolume(1.0F);
					Minecraft.getInstance().getSoundManager().play(newSound);
					activeOwnerSound.put(ownerId, newSound);
				}
			} else {
				if (oldOwnerSound != null) {
					oldOwnerSound.markForStop();
					activeOwnerSound.remove(ownerId);
				}
			}

			PositionedLoopSound oldEndSound = activeEndSound.get(ownerId);
			ItemStack usingItem = owner.getUseItem();
			boolean isBeamActive = usingItem.getItem() instanceof NetherStarScrollItem
					&& usingItem.getEnchantmentLevel(ModEnchantments.DESTRUCTION.get()) > 0
					&& useTicks > 40
					&& owner.isUsingItem();
			boolean shouldHaveEndSound = isBeamActive && endPos.distanceTo(owner.getEyePosition()) > 0.1;

			if (oldEndSound != null && (!shouldHaveEndSound || oldEndSound.isStopped())) {
				oldEndSound.markForStop();
				activeEndSound.remove(ownerId);
				oldEndSound = null;
			}

			if (shouldHaveEndSound) {
				if (oldEndSound == null) {
					PositionedLoopSound newSound = new PositionedLoopSound(
							RPGSounds.SPELL_DESTRUCTION_NETHER_STAR_HIT_BLOCK.get(),
							ownerId, endPos);
					newSound.setActive(true);
					newSound.setVolume(0.3F);
					Minecraft.getInstance().getSoundManager().play(newSound);
					activeEndSound.put(ownerId, newSound);
				} else {
					oldEndSound.setPosition(endPos);
					oldEndSound.setActive(true);
					oldEndSound.setVolume(0.3F);
				}
			}
		}
	}

	public static void handleTargetValidationResult(int targetId, boolean isValid) {
		ClientAnimateTargetData.setValidationResult(targetId, isValid);
	}

	public static void handleSyncEffect(int entityId, boolean hasEffect, int amplifier, int duration) {
		Level level = Minecraft.getInstance().level;
		if (level == null) return;
		Entity entity = level.getEntity(entityId);
		if (entity instanceof LivingEntity livingEntity) {
			if (hasEffect) {
				livingEntity.addEffect(new MobEffectInstance(
						ModEffects.HAPPINESS.get(),
						duration,
						amplifier,
						false, false, true
				));
			} else {
				livingEntity.removeEffect(ModEffects.HAPPINESS.get());
			}
		}
	}

	public static void handleParanoiaSound(int entityId) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.player != null && mc.player.getId() == entityId) {
			mc.player.playNotifySound(
					SoundEvents.AMBIENT_CAVE.value(),
					SoundSource.AMBIENT,
					2.0f, 1.0f
			);
		}
	}

	public static void handleFireExtinguishParticles(BlockPos fireCatcherPos, BlockPos firePos) {
		Level level = Minecraft.getInstance().level;
		if (level == null) return;

		double catcherX = fireCatcherPos.getX() + 0.5;
		double catcherY = fireCatcherPos.getY() + 0.5;
		double catcherZ = fireCatcherPos.getZ() + 0.5;

		double fireX = firePos.getX() + 0.5;
		double fireY = firePos.getY() + 0.5;
		double fireZ = firePos.getZ() + 0.5;

		double dx = catcherX - fireX;
		double dy = catcherY - fireY;
		double dz = catcherZ - fireZ;

		double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
		if (distance < 0.1) return;

		dx /= distance;
		dy /= distance;
		dz /= distance;

		double speed = distance / 8.5 * 15 * 0.03;

		for (int i = 0; i < 3; i++) {
			double offsetX = (level.random.nextDouble() - 0.5) * 0.3;
			double offsetY = (level.random.nextDouble() - 0.5) * 0.3;
			double offsetZ = (level.random.nextDouble() - 0.5) * 0.3;

			double particleX = fireX + offsetX;
			double particleY = fireY + offsetY;
			double particleZ = fireZ + offsetZ;

			level.addParticle(ParticleTypes.FLAME,
					particleX, particleY, particleZ,
					dx * speed, dy * speed, dz * speed);
		}
	}
}