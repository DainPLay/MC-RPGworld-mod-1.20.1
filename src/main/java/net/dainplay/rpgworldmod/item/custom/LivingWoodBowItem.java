package net.dainplay.rpgworldmod.item.custom;

import net.dainplay.rpgworldmod.data.tags.ModAdvancements;
import net.dainplay.rpgworldmod.enchantment.ModEnchantments;
import net.dainplay.rpgworldmod.entity.projectile.ProjectruffleArrowEntity;
import net.dainplay.rpgworldmod.item.ModItems;
import net.dainplay.rpgworldmod.network.BoundEntitySyncPacket;
import net.dainplay.rpgworldmod.network.ModMessages;
import net.dainplay.rpgworldmod.network.PullPlayerPacket;
import net.dainplay.rpgworldmod.sounds.RPGSounds;
import net.dainplay.rpgworldmod.util.BoundEntityHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Vanishable;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class LivingWoodBowItem extends BowItem implements RPGtooltip {
	private static final double BASE_PULL_FORCE = 1;
	private static final double PUNCH_FORCE_MULTIPLIER = 0.6;

	public LivingWoodBowItem(Properties properties) {
		super(properties);
	}

	@Override
	public void releaseUsing(ItemStack pStack, Level pLevel, LivingEntity pEntityLiving, int pTimeLeft) {
		if (pStack.hasTag() && pStack.getTag().contains("UsingProjectruffle")) {
			pStack.getTag().remove("UsingProjectruffle");
		}
		if (pEntityLiving instanceof Player player) {
			boolean flag = player.getAbilities().instabuild;
			ItemStack itemstack = player.getProjectile(pStack);

			int i = this.getUseDuration(pStack) - pTimeLeft;
			i = net.minecraftforge.event.ForgeEventFactory.onArrowLoose(pStack, pLevel, player, i, !itemstack.isEmpty() || flag);
			if (i < 0) return;

			if (!itemstack.isEmpty() || flag) {
				if (itemstack.isEmpty()) {
					itemstack = new ItemStack(Items.ARROW);
				}

				float f = getPowerForTime(i);
				if (!((double) f < 0.1D)) {
					boolean flag1 = player.getAbilities().instabuild || (itemstack.getItem() instanceof ArrowItem && ((ArrowItem) itemstack.getItem()).isInfinite(itemstack, pStack, player));
					if (!pLevel.isClientSide) {
						ArrowItem arrowitem = (ArrowItem) (itemstack.getItem() instanceof ArrowItem ? itemstack.getItem() : Items.ARROW);
						AbstractArrow abstractarrow = arrowitem.createArrow(pLevel, itemstack, player);
						abstractarrow = customArrow(abstractarrow);
						abstractarrow.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, f * 3.0F, 1.0F);
						if (f == 1.0F) {
							abstractarrow.setCritArrow(true);
						}

						int j = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.POWER_ARROWS, pStack);
						if (j > 0) {
							abstractarrow.setBaseDamage(abstractarrow.getBaseDamage() + (double) j * 0.5D + 0.5D);
						}

						int k = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.PUNCH_ARROWS, pStack);
						if (k > 0) {
							abstractarrow.setKnockback(k);
						}

						if (EnchantmentHelper.getItemEnchantmentLevel(Enchantments.FLAMING_ARROWS, pStack) > 0) {
							abstractarrow.setSecondsOnFire(100);
						}

						abstractarrow.getPersistentData().putDouble("BoundPullRange", 50 + EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.STRETCH.get(), pStack) * 50);

						pStack.hurtAndBreak(1, player, (p_289501_) -> {
							p_289501_.broadcastBreakEvent(player.getUsedItemHand());
						});
						if (flag1 || player.getAbilities().instabuild && (itemstack.is(Items.SPECTRAL_ARROW) || itemstack.is(Items.TIPPED_ARROW))) {
							abstractarrow.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
						}

						pLevel.addFreshEntity(abstractarrow);
					}

					pLevel.playSound((Player) null, player.getX(), player.getY(), player.getZ(), SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS, 1.0F, 1.0F / (pLevel.getRandom().nextFloat() * 0.4F + 1.2F) + f * 0.5F);
					if (!flag1 && !player.getAbilities().instabuild) {
						itemstack.shrink(1);
						if (itemstack.isEmpty()) {
							player.getInventory().removeItem(itemstack);
						}
					}

					player.awardStat(Stats.ITEM_USED.get(this));
				}
			}
		}
	}

	@Override
	public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
		if (stack.hasTag() && stack.getTag().contains("UsingProjectruffle")) {
			stack.getTag().remove("UsingProjectruffle");
		}
		return super.finishUsingItem(stack, level, entity);
	}

	@Override
	public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, Player player, @NotNull InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);

		if (player.isShiftKeyDown()) {
			if (!level.isClientSide) {
				if (BoundEntityHelper.hasBoundEntities(player,50 + EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.STRETCH.get(), stack) * 50)) {
					pullBoundEntities(player, (ServerLevel) level, stack, hand);
					level.playSound(null, player.getX(), player.getY(), player.getZ(),
							RPGSounds.LIVING_WOOD_BOW_PULL.get(), SoundSource.PLAYERS,
							1.0F, 1.0F / (level.getRandom().nextFloat() * 0.4F + 1.2F));
				}
				player.getCooldowns().addCooldown(this, 20);
			}

			return InteractionResultHolder.success(stack);
		}

		if (player.getProjectile(stack).getItem() == ModItems.PROJECTRUFFLE_ITEM.get()) {
			CompoundTag tag = stack.getOrCreateTag();
			tag.putBoolean("UsingProjectruffle", true);
		} else {
			if (stack.hasTag() && stack.getTag().contains("UsingProjectruffle")) {
				stack.getTag().remove("UsingProjectruffle");
			}
		}

		return super.use(level, player, hand);
	}

	@Override
	public AbstractArrow customArrow(AbstractArrow arrow) {
		// Устанавливаем привязку к стрелку для всех стрел, выпущенных из этого лука
		if (arrow.getOwner() != null && !arrow.level().isClientSide && arrow.getOwner() instanceof Player) {
			Player player = (Player) arrow.getOwner();
			BoundEntityHelper.bindArrowToPlayer(arrow, player);
			if (arrow instanceof ProjectruffleArrowEntity && player instanceof ServerPlayer serverplayer)
				ModAdvancements.SHOOT_PROJECTRUFFLE_TRIGGER.trigger(serverplayer);
		}
		return arrow;
	}

	private void pullBoundEntities(Player player, ServerLevel level, ItemStack bowStack, InteractionHand hand) {
		boolean pulledSomething = false;
		int arrowsCollected = 0;

		// Получаем уровень зачарования "Отдача" на луке
		int punchLevel = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.PUNCH_ARROWS, bowStack);

		// Сначала соберём все стрелы в список
		List<AbstractArrow> arrowsToCollect = new ArrayList<>();

		for (AbstractArrow arrow : level.getEntitiesOfClass(AbstractArrow.class,
				player.getBoundingBox().inflate(50 + EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.STRETCH.get(), bowStack) * 50))) {

			CompoundTag tag = arrow.getPersistentData();
			if (tag.hasUUID("BoundPlayer") &&
					tag.getUUID("BoundPlayer").equals(player.getUUID()) &&
					tag.getBoolean("LivingWoodArrow")) {

				if (arrow.distanceTo(player) > tag.getDouble("BoundPullRange")) {
					continue;
				}

				arrowsToCollect.add(arrow);
			}
		}

		// Обрабатываем собранные стрелы
		for (AbstractArrow arrow : arrowsToCollect) {
			pulledSomething = true;
			arrowsCollected++;

			ItemStack arrowItem = arrow.getPickupItem();
			if (!arrowItem.isEmpty()) {
				boolean addedToInventory = player.getInventory().add(arrowItem);

				if (!addedToInventory) {
					player.drop(arrowItem, false);
				} else {
					player.containerMenu.broadcastChanges();
				}
			}

			arrow.discard();
		}

		// Притягиваем мобов
		for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class,
				player.getBoundingBox().inflate(50 + EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.STRETCH.get(), bowStack) * 50))) {

			CompoundTag tag = entity.getPersistentData();
			if (tag.hasUUID("BoundPlayer") &&
					tag.getUUID("BoundPlayer").equals(player.getUUID()) &&
					tag.getBoolean("LivingWoodBound")) {

				if (entity.distanceTo(player) > tag.getDouble("BoundPullRange")) {
					continue;
				}

				pulledSomething = true;

				double pullForce = calculatePullForce(entity, punchLevel);
				Vec3 playerPos = player.position().add(0, player.getEyeHeight() * 0.8, 0);
				Vec3 entityPos = entity.position();
				Vec3 direction = playerPos.subtract(entityPos).normalize();
				Vec3 pullMotion = direction.scale(pullForce);

				// Если это другой игрок
				if (entity instanceof ServerPlayer targetPlayer && targetPlayer != player) {
					// Отправляем пакет целевому игроку
					ModMessages.sendToPlayer(new PullPlayerPacket(pullMotion, targetPlayer.getId()), targetPlayer);

					// Также обновляем движение на сервере для корректной работы
					targetPlayer.setDeltaMovement(targetPlayer.getDeltaMovement().add(pullMotion));
					targetPlayer.fallDistance = 0;

					// Убираем привязку после притягивания
					tag.remove("BoundPlayer");
					tag.remove("BoundTime");
					tag.remove("LivingWoodBound");

					// Синхронизируем с клиентами
					ModMessages.sendToNearbyPlayers(
							new BoundEntitySyncPacket(targetPlayer.getId()),
							targetPlayer.level(),
							targetPlayer.blockPosition(),
							300
					);
				}
				// Если это текущий игрок (себя самого)
				else if (entity == player) {
					// Просто применяем движение на сервере
					player.setDeltaMovement(player.getDeltaMovement().add(pullMotion));
					player.fallDistance = 0;
				}
				// Если это моб
				else {
					entity.setDeltaMovement(entity.getDeltaMovement().add(pullMotion));
					entity.fallDistance = 0;

					tag.remove("BoundPlayer");
					tag.remove("BoundTime");
					tag.remove("LivingWoodBound");

					ModMessages.sendToNearbyPlayers(
							new BoundEntitySyncPacket(entity.getId()),
							entity.level(),
							entity.blockPosition(),
							300
					);
				}
			}
		}

		// Наносим урон луку
		if (pulledSomething && !player.getAbilities().instabuild) {
			int durabilityDamage = Math.max(1, Math.min(arrowsCollected, 3));
			bowStack.hurtAndBreak(durabilityDamage, player, p -> p.broadcastBreakEvent(hand));
		}

		// Проигрываем звуки
		if (arrowsCollected > 0) {
			level.playSound(null, player.getX(), player.getY(), player.getZ(),
					SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS,
					0.5F, 0.8F + level.getRandom().nextFloat() * 0.4F);
		}
	}

	private double calculatePullForce(LivingEntity mob, int punchLevel) {
		double baseKnockback = BASE_PULL_FORCE;

		if (punchLevel > 0) {
			baseKnockback += punchLevel * PUNCH_FORCE_MULTIPLIER;
		}

		double knockbackResistance = mob.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE);
		double resistanceFactor = Math.max(0.0D, 1.0D - knockbackResistance);
		double finalForce = baseKnockback * resistanceFactor;
		finalForce += 0.15;

		return Math.min(finalForce, 2.0);
	}

	@Override
	public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
		return !ItemStack.isSameItem(oldStack, newStack);
	}

	@Override
	public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
		return enchantment != Enchantments.INFINITY_ARROWS && (enchantment == Enchantments.MENDING || super.canApplyAtEnchantingTable(stack, enchantment));
	}

	@Override
	public boolean isBookEnchantable(ItemStack stack, ItemStack book) {
		return !EnchantmentHelper.getEnchantments(book).containsKey(Enchantments.INFINITY_ARROWS) && super.isBookEnchantable(stack, book);
	}

	@Override
	public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltip, TooltipFlag pFlag) {
		DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
				ClientRPGtooltipHandler.appendHoverText(pStack, pLevel, pTooltip, pFlag, this)
		);
	}

	@Override
	public String getFirstPredicate(ItemStack item) {
		return Minecraft.getInstance().options.keyShift.getKey().getDisplayName().getString();
	}
}