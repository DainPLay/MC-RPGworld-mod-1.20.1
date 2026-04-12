package net.dainplay.rpgworldmod.item.custom;

import net.dainplay.rpgworldmod.data.tags.ModAdvancements;
import net.dainplay.rpgworldmod.effect.ModEffects;
import net.dainplay.rpgworldmod.enchantment.ModEnchantments;
import net.dainplay.rpgworldmod.entity.custom.Fireflantern;
import net.dainplay.rpgworldmod.entity.custom.MosquitoSwarm;
import net.dainplay.rpgworldmod.particle.ModParticles;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.common.ToolActions;
import net.minecraftforge.fml.DistExecutor;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

public class WealdBladeItem extends SwordItem implements RPGtooltip {
	private static final double BLOW_SPEED = 0.2;
	private static final int BLOW_RANGE = 8;
	private static final float BLOW_WIDTH = 1f;
	private static final float BLOW_HEIGHT = 2.0f;
	private static final double PARTICLE_OFFSET = 0.8;
	private int useTicks = 0;

	public enum ParticleMode {
		DESTROY_ON_COLLISION,
		SLOW_DOWN_NEAR_BLOCKS
	}

	private static ParticleMode PARTICLE_MODE = ParticleMode.SLOW_DOWN_NEAR_BLOCKS;

	public WealdBladeItem(Tier pTier, int pAttackDamageModifier, float pAttackSpeedModifier, Properties pProperties) {
		super(pTier, pAttackDamageModifier, pAttackSpeedModifier, pProperties);
	}

	public int getUseDuration(ItemStack pStack) {
		return 72000;
	}

	public UseAnim getUseAnimation(ItemStack pStack) {
		return UseAnim.BLOCK;
	}

	public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pHand) {
		ItemStack itemstack = pPlayer.getItemInHand(pHand);
		pPlayer.startUsingItem(pHand);
		return InteractionResultHolder.consume(itemstack);
	}

	@Override
	public void onUseTick(Level pLevel, LivingEntity pLivingEntity, ItemStack pStack, int pRemainingUseDuration) {
		super.onUseTick(pLevel, pLivingEntity, pStack, pRemainingUseDuration);

		if (pLivingEntity.hasEffect(ModEffects.MOSQUITOING.get()) && getEnchantmentLevel(pStack, ModEnchantments.BLOWING.get()) > 0) {
			UUID ownerUUID = null;
			if (pLivingEntity.getPersistentData().hasUUID("MosquitoSwarmOwner")) {
				ownerUUID = pLivingEntity.getPersistentData().getUUID("MosquitoSwarmOwner");
				pLivingEntity.getPersistentData().remove("MosquitoSwarmOwner");
			}
			MosquitoSwarm.spawnBlock(pLivingEntity, pLivingEntity.getEffect(ModEffects.MOSQUITOING.get()).getAmplifier(), ownerUUID);
			pLivingEntity.removeEffect(ModEffects.MOSQUITOING.get());
		}
		if (pLivingEntity instanceof Player player && getEnchantmentLevel(pStack, ModEnchantments.BLOWING.get()) > 0) {
			blowEntities(pLevel, player);
			useTicks++;

			if (useTicks % 60 == 0) {
				useTicks = 1;
				pStack.hurtAndBreak(1, player, (p_289501_) -> {
					p_289501_.broadcastBreakEvent(player.getUsedItemHand());
				});
			}

			if (pLevel.isClientSide) {
				spawnBlowParticles(pLevel, player);
			}
		}
	}

	@Override
	public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
		super.releaseUsing(stack, level, entity, timeLeft);
	}

	private void blowEntities(Level level, Player player) {
		Vec3 blowDirection = getHorizontalBlowDirection(player);

		AABB scanArea = createBlowAABB(player, blowDirection);

		List<Entity> entities = level.getEntitiesOfClass(Entity.class, scanArea,
				entity -> entity != player && !entity.isSpectator() && isEntityInFront(player, entity));

		for (Entity entity : entities) {
			applyBlowMotion(entity, blowDirection, player);
		}
	}

	private boolean isEntityInFront(Player player, Entity entity) {
		Vec3 playerLook = player.getLookAngle();
		Vec3 toEntity = entity.position().subtract(player.position());

		playerLook = playerLook.normalize();
		toEntity = toEntity.normalize();

		return playerLook.dot(toEntity) > 0;
	}

	private Vec3 getHorizontalBlowDirection(Player player) {
		float yaw = player.getYRot();
		double rad = Math.toRadians(yaw);

		double x = -Math.sin(rad);
		double z = Math.cos(rad);

		double length = Math.sqrt(x * x + z * z);
		if (length > 0) {
			x /= length;
			z /= length;
		}

		return new Vec3(x, 0, z);
	}

	private AABB createBlowAABB(Player player, Vec3 direction) {
		Vec3 start = player.position().add(0, player.getBbHeight() * 0.5, 0);
		Vec3 end = start.add(direction.scale(BLOW_RANGE));

		double minX = Math.min(start.x, end.x);
		double minY = start.y - BLOW_HEIGHT * 0.5;
		double minZ = Math.min(start.z, end.z);
		double maxX = Math.max(start.x, end.x);
		double maxY = start.y + BLOW_HEIGHT * 0.5;
		double maxZ = Math.max(start.z, end.z);

		if (direction.x > 0) {
			minX -= BLOW_WIDTH;
			maxX += BLOW_WIDTH;
		} else if (direction.x < 0) {
			minX -= BLOW_WIDTH;
			maxX += BLOW_WIDTH;
		}

		if (direction.z > 0) {
			minZ -= BLOW_WIDTH;
			maxZ += BLOW_WIDTH;
		} else if (direction.z < 0) {
			minZ -= BLOW_WIDTH;
			maxZ += BLOW_WIDTH;
		}

		return new AABB(minX, minY, minZ, maxX, maxY, maxZ);
	}

	private boolean canSeeEntity(Level level, Vec3 from, Vec3 to) {
		ClipContext context = new ClipContext(
				from,
				to,
				ClipContext.Block.COLLIDER,
				ClipContext.Fluid.NONE,
				null
		);

		BlockHitResult result = level.clip(context);
		return result.getType() == HitResult.Type.MISS;
	}

	private void applyBlowMotion(Entity entity, Vec3 direction, Player player) {
		if (entity instanceof Player targetPlayer && targetPlayer.isCreative()) {
			return;
		}

		Level level = player.level();
		Vec3 playerEyePos = player.getEyePosition();
		Vec3 entityPos = entity.getBoundingBox().getCenter();

		if (!canSeeEntity(level, playerEyePos, entityPos)) {
			return;
		}

		if (entity instanceof Fireflantern && player instanceof ServerPlayer serverPlayer) {
			ModAdvancements.BLOW_AWAY_A_FIREFLANTERN.trigger(serverPlayer);
		}
		if (entity instanceof MosquitoSwarm mosquitoSwarm) {
			mosquitoSwarm.transformIntoBlock(mosquitoSwarm.getSize());
		}
		if (entity instanceof LivingEntity livingEntity && livingEntity.hasEffect(ModEffects.MOSQUITOING.get())) {
			UUID ownerUUID = null;
			if (entity.getPersistentData().hasUUID("MosquitoSwarmOwner")) {
				ownerUUID = entity.getPersistentData().getUUID("MosquitoSwarmOwner");
				entity.getPersistentData().remove("MosquitoSwarmOwner");
			}
			MosquitoSwarm.spawnBlock(livingEntity, livingEntity.getEffect(ModEffects.MOSQUITOING.get()).getAmplifier(), ownerUUID);
			livingEntity.removeEffect(ModEffects.MOSQUITOING.get());
		}

		Vec3 motion = entity.getDeltaMovement();

		double actualSpeed = entity.onGround() ? BLOW_SPEED / 2.5 : BLOW_SPEED;

		Vec3 blowMotion = direction.scale(actualSpeed);

		entity.setDeltaMovement(motion.add(blowMotion));
	}

	private void spawnBlowParticles(Level level, Player player) {
		SimpleParticleType particleType;

		if (level.random.nextFloat() < 0.1f) {
			particleType = ModParticles.LEAVES.get();
		} else {
			particleType = ModParticles.AIR.get();
		}

		Vec3 direction = getHorizontalBlowDirection(player);

		Vec3 basePos = player.position().add(0, player.getBbHeight() * 0.7, 0);

		Vec3 spawnPos = basePos.add(direction.scale(PARTICLE_OFFSET));

		switch (PARTICLE_MODE) {
			case DESTROY_ON_COLLISION:
				spawnParticlesWithCollisionCheck(level, player, particleType, spawnPos, direction);
				break;
			case SLOW_DOWN_NEAR_BLOCKS:
				spawnParticlesWithSpeedAdjustment(level, player, particleType, spawnPos, direction);
				break;
		}
	}

	private void spawnParticlesWithCollisionCheck(Level level, Player player, SimpleParticleType particleType,
												  Vec3 spawnPos, Vec3 direction) {
		ClipContext context = new ClipContext(
				spawnPos,
				spawnPos.add(direction.scale(BLOW_RANGE)),
				ClipContext.Block.COLLIDER,
				ClipContext.Fluid.NONE,
				null
		);

		BlockHitResult result = level.clip(context);

		double maxDistance = BLOW_RANGE;
		if (result.getType() == HitResult.Type.BLOCK) {
			maxDistance = spawnPos.distanceTo(result.getLocation());
		}

		double newspeed = (double) BLOW_RANGE / 8.5 * 15;
		double xSpeed = direction.x * newspeed * 0.03;
		double ySpeed = 0;
		double zSpeed = direction.z * newspeed * 0.03;

		for (int i = 0; i < 3; i++) {
			double distanceMultiplier = level.random.nextDouble() * (maxDistance / BLOW_RANGE);
			Vec3 particlePos = spawnPos.add(
					direction.scale(distanceMultiplier * 0.5)
			);

			level.addParticle(particleType,
					particlePos.x + (level.random.nextDouble() - 0.5) * 0.5,
					particlePos.y + (level.random.nextDouble() - 0.5) * 0.3,
					particlePos.z + (level.random.nextDouble() - 0.5) * 0.5,
					xSpeed, ySpeed, zSpeed
			);
		}
	}

	private void spawnParticlesWithSpeedAdjustment(Level level, Player player, SimpleParticleType particleType,
												   Vec3 spawnPos, Vec3 direction) {
		ClipContext context = new ClipContext(
				spawnPos,
				spawnPos.add(direction.scale(BLOW_RANGE)),
				ClipContext.Block.COLLIDER,
				ClipContext.Fluid.NONE,
				null
		);

		BlockHitResult result = level.clip(context);

		double speedMultiplier = 1.0;

		if (result.getType() == HitResult.Type.BLOCK) {
			double distanceToBlock = spawnPos.distanceTo(result.getLocation());

			if (distanceToBlock < 1.0) {
				return;
			}

			if (distanceToBlock >= BLOW_RANGE) {
				speedMultiplier = 1.0;
			} else {
				speedMultiplier = (distanceToBlock - 1.0) / (BLOW_RANGE - 1.0);
				speedMultiplier = Math.max(0, Math.min(1, speedMultiplier));
			}
		}

		double newspeed = (double) BLOW_RANGE / 8.5 * 15 * speedMultiplier;
		double xSpeed = direction.x * newspeed * 0.03;
		double ySpeed = 0;
		double zSpeed = direction.z * newspeed * 0.03;

		for (int i = 0; i < 3; i++) {
			level.addParticle(particleType,
					spawnPos.x + (level.random.nextDouble() - 0.5) * 0.5,
					spawnPos.y + (level.random.nextDouble() - 0.5) * 0.3,
					spawnPos.z + (level.random.nextDouble() - 0.5) * 0.5,
					xSpeed, ySpeed, zSpeed
			);
		}
	}

	@Override
	public boolean canPerformAction(ItemStack stack, ToolAction toolAction) {
		if (getEnchantmentLevel(stack, ModEnchantments.BLOWING.get()) > 0) {
			return (ToolActions.DEFAULT_SWORD_ACTIONS.contains(toolAction));
		} else return (ToolActions.DEFAULT_SHIELD_ACTIONS.contains(toolAction) ||
				ToolActions.DEFAULT_SWORD_ACTIONS.contains(toolAction));
	}

	@Override
	public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltip, TooltipFlag pFlag) {
		DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
				ClientRPGtooltipHandler.appendHoverText(pStack, pLevel, pTooltip, pFlag, this)
		);
	}

	@Override
	public MutableComponent getDisplayFeatures(ItemStack item) {
		if (getEnchantmentLevel(item, ModEnchantments.BLOWING.get()) > 0) {
			return Component.translatable(this.getDescriptionId() + ".features.blowing");
		} else {
			return Component.translatable(this.getDescriptionId() + ".features");
		}
	}

	public static void setParticleMode(ParticleMode mode) {
		PARTICLE_MODE = mode;
	}

	public static ParticleMode getParticleMode() {
		return PARTICLE_MODE;
	}

	@Override
	public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
		return !ItemStack.isSameItem(oldStack, newStack);
	}
}