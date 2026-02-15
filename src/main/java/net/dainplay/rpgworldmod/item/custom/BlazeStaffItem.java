package net.dainplay.rpgworldmod.item.custom;

import net.dainplay.rpgworldmod.enchantment.ModEnchantments;
import net.dainplay.rpgworldmod.entity.client.render.PrismarineShardRenderer;
import net.dainplay.rpgworldmod.entity.projectile.PrismarineShardEntity;
import net.dainplay.rpgworldmod.network.LoopSoundPacket;
import net.dainplay.rpgworldmod.network.ModMessages;
import net.dainplay.rpgworldmod.sounds.RPGSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.DragonFireball;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.world.entity.projectile.WitherSkull;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.Map;

public class BlazeStaffItem extends StaffItem implements ChooseAnimateTargetItem {

	public BlazeStaffItem(Properties properties) {
		super(properties);
	}

	@Override
	public String getFirstPredicate(ItemStack item) {
		return Minecraft.getInstance().options.keyAttack.getKey().getDisplayName().getString();
	}

	@Override
	public Boolean hasControls(ItemStack item) {
		return true;
	}

	@Override
	public int getUseDuration(ItemStack pStack) {
		return 72000;
	}

	@Override
	public UseAnim getUseAnimation(ItemStack pStack) {
		return UseAnim.BOW;
	}

	@Override
	public void onUseTick(Level pLevel, LivingEntity pLivingEntity, ItemStack pStack, int pRemainingUseDuration) {
		super.onUseTick(pLevel, pLivingEntity, pStack, pRemainingUseDuration);

		int activeRechargeLevel = pStack.getEnchantmentLevel(ModEnchantments.ACTIVE_RECHARGE.get());
		if (activeRechargeLevel > 0 && pLivingEntity instanceof Player player && !isOffCooldown(pStack, player)) {
			Map<Item, ItemCooldowns.CooldownInstance> cooldownsMap = player.getCooldowns().cooldowns;
			ItemCooldowns.CooldownInstance instance = cooldownsMap.get(pStack.getItem());
			if (instance == null) return;
			int startTick = instance.startTime;
			int endTick = instance.endTime;
			int currentTick = Minecraft.getInstance().player.getCooldowns().tickCount;
			if (endTick - currentTick <= activeRechargeLevel) return;
			cooldownsMap.remove(pStack.getItem());
			cooldownsMap.put(pStack.getItem(), new ItemCooldowns.CooldownInstance(startTick, endTick - activeRechargeLevel));
		}
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {

		ItemStack itemstack = player.getItemInHand(hand);
		int activeRechargeLevel = itemstack.getEnchantmentLevel(ModEnchantments.ACTIVE_RECHARGE.get());
		int doubleExposureLevel = itemstack.getEnchantmentLevel(ModEnchantments.DOUBLE_EXPOSURE.get());
		if (activeRechargeLevel <= 0 && doubleExposureLevel <= 0 && !isOffCooldown(itemstack, player)) {
			return InteractionResultHolder.pass(itemstack);
		}


		if (player.getCooldowns().getCooldownPercent(itemstack.getItem(), 0.0F) > 0.0F) {
			Map<Item, ItemCooldowns.CooldownInstance> cooldownsMap = Minecraft.getInstance().player.getCooldowns().cooldowns;
			int currentTick = Minecraft.getInstance().player.getCooldowns().tickCount;
			ItemCooldowns.CooldownInstance instance = cooldownsMap.get(itemstack.getItem());
			if (instance != null) {
				int startTick = instance.startTime;
				if (currentTick - startTick <= 15 && !isOffCooldown(itemstack, player))
					return InteractionResultHolder.pass(itemstack);
			}
		}

		if (doubleExposureLevel > 0) {
			if (player.getCooldowns().getCooldownPercent(itemstack.getItem(), 0.0F) > 0.0F) {
				Map<Item, ItemCooldowns.CooldownInstance> cooldownsMap = Minecraft.getInstance().player.getCooldowns().cooldowns;
				int currentTick = Minecraft.getInstance().player.getCooldowns().tickCount;
				ItemCooldowns.CooldownInstance instance = cooldownsMap.get(itemstack.getItem());
				if (instance != null) {
					int endTick = instance.endTime;
					if (endTick - currentTick > getCooldown(itemstack)*2 - getProjectileCooldown(itemstack) && activeRechargeLevel <= 0)
						return InteractionResultHolder.pass(itemstack);
				}
			}
		}

		if (!level.isClientSide) {
			// Воспроизводим звук начала использования для всех рядом
			level.playSound(null,
					player.getX(), player.getY(), player.getZ(),
					RPGSounds.STAFF_START.get(),
					SoundSource.PLAYERS, 1.0F, 1.0F
			);

			// Запускаем зацикленный звук на клиентах
			ModMessages.sendToNearbyPlayers(
					new LoopSoundPacket(player.getId(), true, itemstack),
					(ServerLevel) level,
					player.blockPosition(),
					64.0
			);
		}

		player.startUsingItem(hand);
		return InteractionResultHolder.consume(itemstack);
	}

	@Override
	public void releaseUsing(ItemStack stack, Level level, LivingEntity livingEntity, int timeCharged) {
		if (livingEntity instanceof Player player) {
			if (!level.isClientSide) {
				ModMessages.sendToNearbyPlayers(
						new LoopSoundPacket(player.getId(), false, stack),
						level,
						player.blockPosition(),
						64.0
				);

				level.playSound(null,
						player.getX(), player.getY(), player.getZ(),
						RPGSounds.STAFF_STOP.get(),
						SoundSource.PLAYERS, 1.0F, 1.0F
				);
			}
		}
		super.releaseUsing(stack, level, livingEntity, timeCharged);
	}

	public void cast(Player player, ItemStack item) {
		if (player.level().isClientSide) return;

		if (player.getCooldowns().getCooldownPercent(item.getItem(), 0.0F) > 0.0F) {
			Map<Item, ItemCooldowns.CooldownInstance> cooldownsMap = Minecraft.getInstance().player.getCooldowns().cooldowns;
			ItemCooldowns.CooldownInstance instance = cooldownsMap.get(item.getItem());
			if (instance != null) {
				int endTick = instance.endTime;
				int currentTick = Minecraft.getInstance().player.getCooldowns().tickCount;
				player.getCooldowns().addCooldown(this, endTick - currentTick + getProjectileCooldown(item));
			}
		} else  {
			player.getCooldowns().addCooldown(this, getProjectileCooldown(item));
		}
		player.swing(player.getUsedItemHand());

		Level level = player.level();
		Vec3 look = player.getLookAngle();
		Vec3 startPos = player.getEyePosition().add(look.scale(0.8));
		double speed = 2.0;
		Vec3 velocity = look.scale(speed);

		switch (getGemType(item)) {
			case EMBER_GEM: {
				level.playSound(null,
						player.getX(), player.getY(), player.getZ(),
						RPGSounds.STAFF_EMBER_GEM_FIRE.get(),
						SoundSource.PLAYERS, 1.0F, 1.0F);

				SmallFireball fireball = new SmallFireball(level, player, velocity.x, velocity.y, velocity.z);
				fireball.setPos(startPos.x, startPos.y, startPos.z);
				level.addFreshEntity(fireball);
				break;
			}
			case ENDER_EYE: {
				level.playSound(null,
						player.getX(), player.getY(), player.getZ(),
						RPGSounds.STAFF_ENDER_EYE_FIRE.get(),
						SoundSource.PLAYERS, 1.0F, 1.0F);

				DragonFireball fireball = new DragonFireball(level, player, velocity.x, velocity.y, velocity.z);
				fireball.setPos(startPos.x, startPos.y, startPos.z);
				level.addFreshEntity(fireball);
				break;
			}
			case HEART_OF_THE_SEA: {
				level.playSound(null,
						player.getX(), player.getY(), player.getZ(),
						RPGSounds.STAFF_HEART_OF_THE_SEA_FIRE.get(),
						SoundSource.PLAYERS, 1.0F, 1.0F);

				RandomSource random = level.getRandom();
				float baseYaw = player.getYRot();
				float basePitch = player.getXRot();
				float baseSpeed = 3.0f;

				PrismarineShardEntity prismarineShardCenter = new PrismarineShardEntity(level, player);
				prismarineShardCenter.setPos(startPos.x, startPos.y, startPos.z);
				prismarineShardCenter.setOwner(player);
				prismarineShardCenter.pickup = AbstractArrow.Pickup.DISALLOWED;
				prismarineShardCenter.shootFromRotation(player, basePitch, baseYaw, 0.0F, baseSpeed, 0.0F);
				level.addFreshEntity(prismarineShardCenter);

				double maxSpread = 5.0;
				float deltaYaw = (random.nextFloat() * 2 - 1) * (float) maxSpread;
				float deltaPitch = (random.nextFloat() * 2 - 1) * (float) maxSpread;

				PrismarineShardEntity prismarineShardLeft = new PrismarineShardEntity(level, player);
				prismarineShardLeft.setPos(startPos.x, startPos.y, startPos.z);
				prismarineShardLeft.setOwner(player);
				prismarineShardLeft.pickup = AbstractArrow.Pickup.DISALLOWED;
				prismarineShardLeft.shootFromRotation(player, basePitch + deltaPitch, baseYaw + deltaYaw, 0.0F, baseSpeed, 0.0F);
				level.addFreshEntity(prismarineShardLeft);

				PrismarineShardEntity prismarineShardRight = new PrismarineShardEntity(level, player);
				prismarineShardRight.setPos(startPos.x, startPos.y, startPos.z);
				prismarineShardRight.setOwner(player);
				prismarineShardRight.pickup = AbstractArrow.Pickup.DISALLOWED;
				prismarineShardRight.shootFromRotation(player, basePitch - deltaPitch, baseYaw - deltaYaw, 0.0F, baseSpeed, 0.0F);
				level.addFreshEntity(prismarineShardRight);

				break;
			}
			case NETHER_STAR: {
				level.playSound(null,
						player.getX(), player.getY(), player.getZ(),
						RPGSounds.STAFF_NETHER_STAR_FIRE.get(),
						SoundSource.PLAYERS, 0.5F, 1.0F);

				WitherSkull skull = new WitherSkull(level, player, velocity.x, velocity.y, velocity.z);
				skull.setPos(startPos.x, startPos.y, startPos.z);
				level.addFreshEntity(skull);
				break;
			}
		}

		item.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(player.getUsedItemHand()));
	}

	@Override
	public int getCooldown(ItemStack item) {
		return 300;
	}

	public int getProjectileCooldown(ItemStack stack) {
		if (getGemType(stack) == GemType.EMBER_GEM)
			return 60;
		else if (getGemType(stack) == GemType.HEART_OF_THE_SEA)
			return 100;
		else if (getGemType(stack) == GemType.NETHER_STAR)
			return 200;
		else if (getGemType(stack) == GemType.ENDER_EYE)
			return 200;
		else
			return getCooldown(stack);
	}

	@Override
	public float getX(ItemStack stack, Entity entity) {
		return -0.065F;
	}
	@Override
	public float get1XOffset(ItemStack stack, Entity entity) {
		return 0.25F;
	}

	@Override
	public float get1YOffset(ItemStack stack, Entity entity) {
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

	@Override
	public boolean isOffCooldown(ItemStack item, Player player) {
		if(item.getEnchantmentLevel(ModEnchantments.DOUBLE_EXPOSURE.get())>0 && player.getCooldowns().getCooldownPercent(item.getItem(), 0.0F) > 0.0F) {
			Map<Item, ItemCooldowns.CooldownInstance> cooldownsMap = Minecraft.getInstance().player.getCooldowns().cooldowns;
			ItemCooldowns.CooldownInstance instance = cooldownsMap.get(item.getItem());
			if (instance == null) return true;
			int endTick = instance.endTime;
			int currentTick = Minecraft.getInstance().player.getCooldowns().tickCount;
			return endTick - currentTick <= getCooldown(item)*2 - getProjectileCooldown(item);
		}
		else if(player.getCooldowns().getCooldownPercent(item.getItem(), 0.0F) > 0.0F){
			Map<Item, ItemCooldowns.CooldownInstance> cooldownsMap = Minecraft.getInstance().player.getCooldowns().cooldowns;
			ItemCooldowns.CooldownInstance instance = cooldownsMap.get(item.getItem());
			if (instance == null) return true;
			int endTick = instance.endTime;
			int currentTick = Minecraft.getInstance().player.getCooldowns().tickCount;
			return endTick - currentTick <= getCooldown(item) - getProjectileCooldown(item);
		}
		return !(player.getCooldowns().getCooldownPercent(item.getItem(), 0.0F) > 0.0F);
	}
}