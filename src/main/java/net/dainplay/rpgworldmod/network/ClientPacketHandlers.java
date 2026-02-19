package net.dainplay.rpgworldmod.network;

import net.dainplay.rpgworldmod.effect.ModEffects;
import net.dainplay.rpgworldmod.enchantment.ModEnchantments;
import net.dainplay.rpgworldmod.entity.custom.Razorleaf;
import net.dainplay.rpgworldmod.item.custom.EmberScrollItem;
import net.dainplay.rpgworldmod.item.custom.HeartOfTheSeaScrollItem;
import net.dainplay.rpgworldmod.item.custom.StaffItem;
import net.dainplay.rpgworldmod.sounds.LoopSound;
import net.dainplay.rpgworldmod.sounds.RPGSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
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

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@OnlyIn(Dist.CLIENT)
public class ClientPacketHandlers {

	// --- Mana ---
	public static void handleManaSync(int mana) {
		ClientManaData.set(mana);
	}

	public static void handleMaxManaSync(int maxMana) {
		ClientMaxManaData.set(maxMana);
	}

	public static void handleIsManaRegenBlockedSync(int blocked) {
		ClientIsManaRegenBlockedData.set(blocked);
	}

	// --- Razorleaf ---
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

	// --- Entity Motion ---
	public static void handleSyncEntityMotion(int entityId, double motionX, double motionY, double motionZ) {
		Level level = Minecraft.getInstance().level;
		if (level == null) return;
		Entity entity = level.getEntity(entityId);
		if (entity != null) {
			entity.setDeltaMovement(motionX, motionY, motionZ);
		}
	}

	// --- Illusion Force ---
	public static void handleIllusionForceSync(int illusionForce, BlockPos entPosition) {
		ClientIllusionForceData.set(illusionForce);
		ClientEntPositionData.set(entPosition);
	}

	// --- Bound Entity ---
	public static void handleBoundEntitySync(int entityId, boolean isRemoval, BoundEntitySyncPacket.BoundEntityData data) {
		if (isRemoval) {
			ClientBoundEntityData.removeEntity(entityId);
		} else {
			ClientBoundEntityData.updateEntity(data);
		}
	}

	// --- Pull Player ---
	public static void handlePullPlayer(Vec3 motion, int playerId) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.level != null && mc.player != null && mc.player.getId() == playerId) {
			mc.player.setDeltaMovement(mc.player.getDeltaMovement().add(motion));
			mc.player.fallDistance = 0;
		}
	}

	// --- Move Particles ---
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

	// --- Loop Sound ---
	public static void handleLoopSound(int playerId, boolean start, ItemStack itemStack) {
		//Minecraft.getInstance().player.sendSystemMessage(Component.literal("Пакет получен"));
		Level level = Minecraft.getInstance().level;
		if (level == null) return;
		Player player = (Player) level.getEntity(playerId);
		if (player == null) return;

		if (start) {
			SoundEvent soundEvent = RPGSounds.STAFF_LOOP.get();
			if (itemStack.getItem() instanceof EmberScrollItem) {
				if (itemStack.getEnchantmentLevel(ModEnchantments.DESTRUCTION.get()) > 0)
					soundEvent = RPGSounds.SPELL_DESTRUCTION_EMBER_LOOP.get();
				if (itemStack.getEnchantmentLevel(ModEnchantments.RESTORATION.get()) > 0)
					soundEvent = RPGSounds.SPELL_RESTORATION_LOOP.get();
				if (itemStack.getEnchantmentLevel(ModEnchantments.ALTERATION.get()) > 0)
					soundEvent = RPGSounds.SPELL_ALTERATION_LOOP.get();
				if (itemStack.getEnchantmentLevel(ModEnchantments.ILLUSION.get()) > 0)
					soundEvent = RPGSounds.SPELL_ILLUSION_LOOP.get();
				if (itemStack.getEnchantmentLevel(ModEnchantments.NECROMANCY.get()) > 0)
					soundEvent = RPGSounds.SPELL_NECROMANCY_LOOP.get();
			}
			if (itemStack.getItem() instanceof HeartOfTheSeaScrollItem) {
				if (itemStack.getEnchantmentLevel(ModEnchantments.DESTRUCTION.get()) > 0)
					soundEvent = RPGSounds.SPELL_DESTRUCTION_HEART_OF_THE_SEA_LOOP.get();
				if (itemStack.getEnchantmentLevel(ModEnchantments.RESTORATION.get()) > 0)
					soundEvent = RPGSounds.SPELL_RESTORATION_LOOP.get();
				if (itemStack.getEnchantmentLevel(ModEnchantments.ALTERATION.get()) > 0)
					soundEvent = RPGSounds.SPELL_ALTERATION_LOOP.get();
				if (itemStack.getEnchantmentLevel(ModEnchantments.ILLUSION.get()) > 0)
					soundEvent = RPGSounds.SPELL_ILLUSION_LOOP.get();
				if (itemStack.getEnchantmentLevel(ModEnchantments.NECROMANCY.get()) > 0)
					soundEvent = RPGSounds.SPELL_NECROMANCY_LOOP.get();
			}
			if (itemStack.getItem() instanceof StaffItem) {
				soundEvent = RPGSounds.STAFF_LOOP.get();
			}
			LoopSound sound = new LoopSound(player, itemStack, soundEvent);
				Minecraft.getInstance().getSoundManager().play(sound);
				EmberScrollSoundManager.addSound(player.getUUID(), sound);
			//Minecraft.getInstance().player.sendSystemMessage(Component.literal("Запуск звука "+soundEvent));
		} else {
			EmberScrollSoundManager.stopSound(player.getUUID());
			//Minecraft.getInstance().player.sendSystemMessage(Component.literal("Звук остановлен"));
		}
	}

	// Менеджер звуков внутри клиентского обработчика
	public static class EmberScrollSoundManager {
		private static final Map<UUID, LoopSound> activeSounds = new HashMap<>();

		public static void addSound(UUID playerId, LoopSound sound) {
			stopSound(playerId);
			activeSounds.put(playerId, sound);
		}

		public static void stopSound(UUID playerId) {
			LoopSound sound = activeSounds.remove(playerId);
			if (sound != null) {
				sound.stop();
			}
		}
	}

	// --- Target Validation Result ---
	public static void handleTargetValidationResult(int targetId, boolean isValid) {
		ClientAnimateTargetData.setValidationResult(targetId, isValid);
	}

	// --- Sync Effect (Happiness) ---
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

	// --- Paranoia Sound ---
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

	// --- Fire Extinguish Particles ---
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