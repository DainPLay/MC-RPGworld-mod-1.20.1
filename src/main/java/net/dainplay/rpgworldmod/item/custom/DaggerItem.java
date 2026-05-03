package net.dainplay.rpgworldmod.item.custom;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.dainplay.rpgworldmod.damage.ModDamageTypes;
import net.dainplay.rpgworldmod.data.tags.ModAdvancements;
import net.dainplay.rpgworldmod.enchantment.ModEnchantments;
import net.dainplay.rpgworldmod.network.PlayerManaProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.Vanishable;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.common.ToolActions;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.entity.player.CriticalHitEvent;
import net.minecraftforge.fml.DistExecutor;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class DaggerItem extends TieredItem implements Vanishable, RPGtooltip {
	private static final UUID DAGGER_REACH_MODIFIER_UUID = UUID.fromString("982345c1-f965-4227-8e00-cfccf483fa0a");
	private final int attackCooldown;
	private final float attackDamage;
	private final Multimap<Attribute, AttributeModifier> defaultModifiers;

	public DaggerItem(Tier pTier, int pAttackDamageModifier, float pAttackSpeedModifier, Properties pProperties) {
		super(pTier, pProperties);
		this.attackDamage = (float) pAttackDamageModifier + pTier.getAttackDamageBonus();
		ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
		builder.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, "Weapon modifier", (double) this.attackDamage, AttributeModifier.Operation.ADDITION));
		builder.put(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_UUID, "Weapon modifier", (double) pAttackSpeedModifier, AttributeModifier.Operation.ADDITION));
		this.defaultModifiers = builder.build();
		this.attackCooldown = Math.max(1, (int) ((6F / pTier.getSpeed()) * 20));
	}

	public float getDamage() {
		return this.attackDamage;
	}

	public boolean canAttackBlock(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer) {
		return !pPlayer.isCreative();
	}

	public float getDestroySpeed(ItemStack pStack, BlockState pState) {
		if (pState.is(Blocks.COBWEB)) {
			return 15.0F;
		} else {
			return pState.is(BlockTags.SWORD_EFFICIENT) ? 1.5F : 1.0F;
		}
	}

	public boolean mineBlock(ItemStack pStack, Level pLevel, BlockState pState, BlockPos pPos, LivingEntity pEntityLiving) {
		if (pState.getDestroySpeed(pLevel, pPos) != 0.0F) {
			pStack.hurtAndBreak(2, pEntityLiving, (p_43276_) -> {
				p_43276_.broadcastBreakEvent(EquipmentSlot.MAINHAND);
			});
		}

		return true;
	}

	public boolean isCorrectToolForDrops(BlockState pBlock) {
		return pBlock.is(Blocks.COBWEB);
	}

	public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot pEquipmentSlot) {
		return pEquipmentSlot == EquipmentSlot.MAINHAND ? this.defaultModifiers : super.getDefaultAttributeModifiers(pEquipmentSlot);
	}


	@Override
	public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
		return (enchantment == Enchantments.SHARPNESS
				|| enchantment == Enchantments.BANE_OF_ARTHROPODS
				|| enchantment == Enchantments.SMITE
				|| enchantment == Enchantments.MOB_LOOTING
				|| enchantment == ModEnchantments.IMMOLATION.get()
				|| super.canApplyAtEnchantingTable(stack, enchantment));
	}

	@Override
	public boolean isBookEnchantable(ItemStack stack, ItemStack book) {
		return (EnchantmentHelper.getEnchantments(book).containsKey(Enchantments.SHARPNESS)
				|| EnchantmentHelper.getEnchantments(book).containsKey(Enchantments.BANE_OF_ARTHROPODS)
				|| EnchantmentHelper.getEnchantments(book).containsKey(Enchantments.SMITE)
				|| EnchantmentHelper.getEnchantments(book).containsKey(Enchantments.MOB_LOOTING)
				|| EnchantmentHelper.getEnchantments(book).containsKey(ModEnchantments.IMMOLATION.get())
				|| super.isBookEnchantable(stack, book));
	}

	@Override
	public boolean canPerformAction(ItemStack stack, ToolAction toolAction) {
		return ToolActions.SWORD_DIG == toolAction;
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		ItemStack itemstack = player.getItemInHand(hand);
		player.startUsingItem(hand);
		addDaggerReachModifier(player);
		return InteractionResultHolder.consume(itemstack);
	}

	@Override
	public int getUseDuration(ItemStack stack) {
		return 72000;
	}

	@Override
	public void onUseTick(Level level, LivingEntity livingEntity, ItemStack stack, int remainingUseDuration) {
		if (livingEntity instanceof Player player) {
			if (!level.isClientSide && this.getUseDuration(stack) - remainingUseDuration >= this.attackCooldown) {
				InteractionHand hand = player.getUsedItemHand();

				if (getEnchantmentLevel(stack, ModEnchantments.IMMOLATION.get()) > 0)
					performSelfStab(player, stack, hand);
				else performStab(player, stack, hand);
			}
		} else if (livingEntity instanceof Mob mob) {
			if (!level.isClientSide) {
				int usedTicks = this.getUseDuration(stack) - remainingUseDuration;
				if (usedTicks >= this.attackCooldown) {
					LivingEntity target = mob.getTarget();
					if (target != null && target.isAlive() && mob.distanceTo(target) < 2.0) {
						mobStab(mob, target, stack);
					}
					mob.stopUsingItem();
				}
			}
		}
	}

	public int getAttackCooldown() {
		return attackCooldown;
	}

	private void performSelfStab(Player player, ItemStack daggerStack, InteractionHand hand) {
		Level level = player.level();

		if (!player.isAttackable()) {
			player.resetAttackStrengthTicker();
			player.swing(hand, true);
			player.getCooldowns().addCooldown(this, 15);
			player.stopUsingItem();
			return;
		}

		float baseDamage = getDamageForDaggerStab(player, daggerStack, player, hand);
		float enchantBonus = EnchantmentHelper.getDamageBonus(daggerStack, player.getMobType());
		float totalDamage = baseDamage + enchantBonus;

		if (totalDamage <= 0.0F) {
			player.resetAttackStrengthTicker();
			player.swing(hand, true);
			player.getCooldowns().addCooldown(this, 15);
			player.stopUsingItem();
			return;
		}

		int fireAspect = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.FIRE_ASPECT, daggerStack);
		if (fireAspect > 0 && !player.isOnFire()) {
			player.setSecondsOnFire(1);
		}


		float healthBefore = player.getHealth();


		DamageSource source = ModDamageTypes.getEntityDamageSource(level, ModDamageTypes.STAB, player);
		boolean hurtSucceeded = player.hurt(source, totalDamage);

		if (hurtSucceeded) {
			Potion potion = PotionUtils.getPotion(daggerStack);
			Set<MobEffectInstance> effects = Sets.newHashSet();
			Collection<MobEffectInstance> collection = PotionUtils.getCustomEffects(daggerStack);
			if (!collection.isEmpty()) {
				for (MobEffectInstance mobeffectinstance : collection) {
					effects.add(new MobEffectInstance(mobeffectinstance));
				}
			}
			for (MobEffectInstance mobeffectinstance : potion.getEffects()) {
				player.addEffect(new MobEffectInstance(mobeffectinstance.getEffect(), Math.max(mobeffectinstance.mapDuration((p_268168_) -> {
					return p_268168_ / 8;
				}), 1), mobeffectinstance.getAmplifier(), mobeffectinstance.isAmbient(), mobeffectinstance.isVisible()));
			}

			if (!effects.isEmpty()) {
				for (MobEffectInstance mobeffectinstance1 : effects) {
					player.addEffect(mobeffectinstance1);
				}
			}

			player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
					SoundEvents.PLAYER_ATTACK_STRONG, SoundSource.PLAYERS, 1.0F, 1.0F);

			if (enchantBonus > 0.0F) {
				player.magicCrit(player);
			}

			EnchantmentHelper.doPostHurtEffects(player, player);
			EnchantmentHelper.doPostDamageEffects(player, player);

			if (!player.level().isClientSide && !daggerStack.isEmpty()) {
				ItemStack copy = daggerStack.copy();
				daggerStack.hurtEnemy(player, player);
				if (daggerStack.isEmpty()) {
					ForgeEventFactory.onPlayerDestroyItem(player, copy, hand);
					player.setItemInHand(hand, ItemStack.EMPTY);
				}
			}


			float damageDealt = healthBefore - player.getHealth();
			if (player instanceof ServerPlayer serverPlayer) {
				if (!player.getAbilities().instabuild) {
					serverPlayer.getCapability(PlayerManaProvider.PLAYER_MANA).ifPresent(mana -> {
						if (mana.getMana() > 0)
							mana.addMana(serverPlayer, (int) (damageDealt * 2));
					});
				}
			}
			player.awardStat(Stats.DAMAGE_DEALT, Math.round(damageDealt * 10.0F));

			if (fireAspect > 0) {
				player.setSecondsOnFire(fireAspect * 4);
			}

			if (player.level() instanceof ServerLevel && damageDealt > 2.0F) {
				int particleCount = (int) (damageDealt * 0.5D);
				((ServerLevel) player.level()).sendParticles(ParticleTypes.DAMAGE_INDICATOR,
						player.getX(), player.getY(0.5D), player.getZ(), particleCount,
						0.1D, 0.0D, 0.1D, 0.2D);
			}

			player.causeFoodExhaustion(0.1F);
		} else {

			player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_ATTACK_NODAMAGE, SoundSource.PLAYERS, 1.0F, 1.0F);
			if (fireAspect > 0 && player.isOnFire()) {
				player.clearFire();
			}
		}
		removeDaggerReachModifier(player);
		daggerStack.hurtAndBreak(1, player, (p) -> {
			p.broadcastBreakEvent(hand);
		});
		player.resetAttackStrengthTicker();
		player.swing(hand, true);
		player.getCooldowns().addCooldown(this, 15);
		player.stopUsingItem();
	}

	private void performStab(Player player, ItemStack daggerStack, InteractionHand hand) {
		Level level = player.level();

		Vec3 start = player.getEyePosition(1.0F);
		Vec3 look = player.getLookAngle();
		Vec3 end = start.add(look.scale(player.getEntityReach()));

		AABB searchArea = player.getBoundingBox()
				.expandTowards(look.scale(player.getEntityReach()))
				.inflate(1.0D);

		Predicate<Entity> filter = entity ->
				entity instanceof LivingEntity &&
						entity != player &&
						entity.isAlive();

		EntityHitResult entityHit = ProjectileUtil.getEntityHitResult(
				level, player, start, end, searchArea, filter, 0.0F
		);

		if (entityHit != null) {
			BlockHitResult blockHit = level.clip(new ClipContext(start, end,
					ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
			if (blockHit.getType() == HitResult.Type.BLOCK) {
				double blockDistSqr = blockHit.getLocation().distanceToSqr(start);
				double entityDistSqr = entityHit.getLocation().distanceToSqr(start);
				if (blockDistSqr < entityDistSqr) {
					entityHit = null;
				}
			}
		}

		if (entityHit != null && entityHit.getEntity() instanceof LivingEntity target) {
			if (!ForgeHooks.onPlayerAttackTarget(player, target) || !target.isAttackable() || target.skipAttackInteraction(player)) {
				player.resetAttackStrengthTicker();
				player.swing(hand, true);
				player.getCooldowns().addCooldown(this, 15);
				player.stopUsingItem();
				return;
			}

			float baseDamage = getDamageForDaggerStab(player, daggerStack, target, hand);
			float enchantBonus = EnchantmentHelper.getDamageBonus(daggerStack, target.getMobType());
			float totalDamage = baseDamage + enchantBonus;

			if (totalDamage <= 0.0F) {
				player.resetAttackStrengthTicker();
				player.swing(hand, true);
				player.getCooldowns().addCooldown(this, 15);
				player.stopUsingItem();
				return;
			}

			Vec3 vec32 = player.position();
			Vec3 vec3 = target.getViewVector(1.0F);
			Vec3 vec31 = vec32.vectorTo(target.position()).normalize();
			vec31 = new Vec3(vec31.x, 0.0D, vec31.z);
			boolean backstab = vec31.dot(vec3) >= 0.0D;
			if (backstab) {
				totalDamage *= 10.0F;
			}

			boolean isCrit = backstab;


			CriticalHitEvent critEvent = ForgeHooks.getCriticalHit(player, target, isCrit, 1.0F);
			if (critEvent != null) {
				totalDamage *= critEvent.getDamageModifier();
				isCrit = true;
			}


			int fireAspect = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.FIRE_ASPECT, daggerStack);
			if (fireAspect > 0 && !target.isOnFire()) {
				target.setSecondsOnFire(1);
			}


			float healthBefore = target.getHealth();


			DamageSource source = ModDamageTypes.getEntityDamageSource(level, ModDamageTypes.STAB, player);
			boolean hurtSucceeded = target.hurt(source, totalDamage);

			if (hurtSucceeded) {
				boolean hasInvis = false;
				Potion potion = PotionUtils.getPotion(daggerStack);
				Set<MobEffectInstance> effects = Sets.newHashSet();
				Collection<MobEffectInstance> collection = PotionUtils.getCustomEffects(daggerStack);
				if (!collection.isEmpty()) {
					for (MobEffectInstance mobeffectinstance : collection) {
						effects.add(new MobEffectInstance(mobeffectinstance));
					}
				}
				for (MobEffectInstance mobeffectinstance : potion.getEffects()) {
					if (mobeffectinstance.getEffect() == MobEffects.INVISIBILITY) hasInvis = true;
					target.addEffect(new MobEffectInstance(mobeffectinstance.getEffect(), Math.max(mobeffectinstance.mapDuration((p_268168_) -> {
						return p_268168_ / 8;
					}), 1), mobeffectinstance.getAmplifier(), mobeffectinstance.isAmbient(), mobeffectinstance.isVisible()));
				}

				if (!effects.isEmpty()) {
					for (MobEffectInstance mobeffectinstance1 : effects) {
						target.addEffect(mobeffectinstance1);
					}
				}
				if (isCrit) {
					player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
							SoundEvents.PLAYER_ATTACK_CRIT, SoundSource.PLAYERS, 1.0F, 1.0F);
					player.crit(target);
					if (player instanceof ServerPlayer serverPlayer) {
						ModAdvancements.BACKSTAB_TRIGGER.trigger(serverPlayer);
					}
					if (hasInvis && target.isDeadOrDying() && player instanceof ServerPlayer serverPlayer) {
						ModAdvancements.INVIS_BACKSTAB_KILL_TRIGGER.trigger(serverPlayer);
					}
				} else {
					player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
							SoundEvents.PLAYER_ATTACK_STRONG, SoundSource.PLAYERS, 1.0F, 1.0F);
				}

				if (enchantBonus > 0.0F) {
					player.magicCrit(target);
				}

				player.setLastHurtMob(target);

				if (target instanceof LivingEntity) {
					EnchantmentHelper.doPostHurtEffects((LivingEntity) target, player);
				}
				EnchantmentHelper.doPostDamageEffects(player, target);

				if (!player.level().isClientSide && !daggerStack.isEmpty() && target instanceof LivingEntity) {
					ItemStack copy = daggerStack.copy();
					daggerStack.hurtEnemy((LivingEntity) target, player);
					if (daggerStack.isEmpty()) {
						ForgeEventFactory.onPlayerDestroyItem(player, copy, hand);
						player.setItemInHand(hand, ItemStack.EMPTY);
					}
				}


				float damageDealt = healthBefore - target.getHealth();
				player.awardStat(Stats.DAMAGE_DEALT, Math.round(damageDealt * 10.0F));

				if (fireAspect > 0) {
					target.setSecondsOnFire(fireAspect * 4);
				}

				if (player.level() instanceof ServerLevel && damageDealt > 2.0F) {
					int particleCount = (int) (damageDealt * 0.5D);
					((ServerLevel) player.level()).sendParticles(ParticleTypes.DAMAGE_INDICATOR,
							target.getX(), target.getY(0.5D), target.getZ(), particleCount,
							0.1D, 0.0D, 0.1D, 0.2D);
				}

				player.causeFoodExhaustion(0.1F);
			} else {

				player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
						SoundEvents.PLAYER_ATTACK_NODAMAGE, SoundSource.PLAYERS, 1.0F, 1.0F);
				if (fireAspect > 0 && target.isOnFire()) {
					target.clearFire();
				}
			}
			removeDaggerReachModifier(player);
			daggerStack.hurtAndBreak(1, player, (p) -> {
				p.broadcastBreakEvent(hand);
			});
			player.resetAttackStrengthTicker();
			player.swing(hand, true);
			player.getCooldowns().addCooldown(this, 15);
			player.stopUsingItem();

		} else {

			player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
					SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.0F, 1.0F);
			player.resetAttackStrengthTicker();
			player.swing(hand, true);
			player.getCooldowns().addCooldown(this, 15);
			player.stopUsingItem();
		}
	}

	private void mobStab(Mob attacker, LivingEntity target, ItemStack daggerStack) {
		Level level = attacker.level();


		if (!target.isAttackable() || target.skipAttackInteraction(attacker)) {
			attacker.swing(InteractionHand.MAIN_HAND, true);
			attacker.stopUsingItem();
			return;
		}

		float baseDamage = getDamageForDaggerStab(attacker, daggerStack, target, InteractionHand.MAIN_HAND);
		float enchantBonus = EnchantmentHelper.getDamageBonus(daggerStack, target.getMobType());
		float totalDamage = baseDamage + enchantBonus;

		if (totalDamage <= 0.0F) {
			attacker.swing(InteractionHand.MAIN_HAND, true);
			attacker.stopUsingItem();
			return;
		}

		Vec3 vec32 = attacker.position();
		Vec3 vec3 = target.getViewVector(1.0F);
		Vec3 vec31 = vec32.vectorTo(target.position()).normalize();
		vec31 = new Vec3(vec31.x, 0.0D, vec31.z);
		boolean backstab = vec31.dot(vec3) >= 0.0D;
		if (backstab) {
			totalDamage *= 10.0F;
		}

		boolean isCrit = backstab;


		int fireAspect = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.FIRE_ASPECT, daggerStack);
		if (fireAspect > 0 && !target.isOnFire()) {
			target.setSecondsOnFire(1);
		}


		DamageSource source = ModDamageTypes.getEntityDamageSource(level, ModDamageTypes.STAB, attacker);
		boolean hurtSucceeded = target.hurt(source, totalDamage);

		if (hurtSucceeded) {
			Potion potion = PotionUtils.getPotion(daggerStack);
			Set<MobEffectInstance> effects = Sets.newHashSet();
			Collection<MobEffectInstance> collection = PotionUtils.getCustomEffects(daggerStack);
			if (!collection.isEmpty()) {
				for (MobEffectInstance mobeffectinstance : collection) {
					effects.add(new MobEffectInstance(mobeffectinstance));
				}
			}
			for (MobEffectInstance mobeffectinstance : potion.getEffects()) {
				target.addEffect(new MobEffectInstance(mobeffectinstance.getEffect(), Math.max(mobeffectinstance.mapDuration((p_268168_) -> {
					return p_268168_ / 8;
				}), 1), mobeffectinstance.getAmplifier(), mobeffectinstance.isAmbient(), mobeffectinstance.isVisible()));
			}

			if (!effects.isEmpty()) {
				for (MobEffectInstance mobeffectinstance1 : effects) {
					target.addEffect(mobeffectinstance1);
				}
			}
			if (isCrit) {
				attacker.level().playSound(null, attacker.getX(), attacker.getY(), attacker.getZ(),
						SoundEvents.PLAYER_ATTACK_CRIT, SoundSource.PLAYERS, 1.0F, 1.0F);
				if (daggerStack.getItem() instanceof IgniteOnCritItem) {
					int fortuneLevel = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.BLOCK_FORTUNE, daggerStack);

					float baseChance = 0.10f;
					float chancePerLevel = 0.3f;

					float totalChance = baseChance + (fortuneLevel * chancePerLevel);

					totalChance = Math.min(totalChance, 1f) * 5;

					float chance = target.level().getRandom().nextFloat();

					if (chance < totalChance && !target.fireImmune()) {
						int fireDuration = 5 + (fortuneLevel * 5);
						target.setSecondsOnFire(fireDuration);
					}
				}
			} else {
				attacker.level().playSound(null, attacker.getX(), attacker.getY(), attacker.getZ(),
						SoundEvents.PLAYER_ATTACK_STRONG, SoundSource.PLAYERS, 1.0F, 1.0F);
			}

			attacker.setLastHurtMob(target);

			if (target instanceof LivingEntity) {
				EnchantmentHelper.doPostHurtEffects(target, attacker);
			}
			EnchantmentHelper.doPostDamageEffects(attacker, target);

			if (fireAspect > 0) {
				target.setSecondsOnFire(fireAspect * 4);
			}
		} else {

			attacker.level().playSound(null, attacker.getX(), attacker.getY(), attacker.getZ(),
					SoundEvents.PLAYER_ATTACK_NODAMAGE, SoundSource.PLAYERS, 1.0F, 1.0F);
			if (fireAspect > 0 && target.isOnFire()) {
				target.clearFire();
			}
		}
		attacker.swing(InteractionHand.MAIN_HAND, true);
		attacker.stopUsingItem();

	}

	@Override
	public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
		super.inventoryTick(stack, level, entity, slotId, isSelected);
		if (!level.isClientSide && entity instanceof ServerPlayer player && PotionUtils.getPotion(stack) != Potions.EMPTY) {
			ModAdvancements.EFFECT_DAGGER_TRIGGER.trigger(player);
		}
	}


	private float getDamageForDaggerStab(LivingEntity entity, ItemStack daggerStack, LivingEntity target, InteractionHand hand) {
		AttributeInstance attackAttribute = entity.getAttribute(Attributes.ATTACK_DAMAGE);
		double current = attackAttribute != null ? attackAttribute.getValue() : 1.0;
		ItemStack stack = entity.getMainHandItem();
		current -= getItemDamageBonus(stack, EquipmentSlot.MAINHAND);
		if (entity instanceof Mob) current = 1.0;
		float daggerDamage = ((DaggerItem) daggerStack.getItem()).getDamage();
		return (float) current + daggerDamage;
	}


	private double getItemDamageBonus(ItemStack stack, EquipmentSlot slot) {
		double bonus = 0;
		if (!stack.isEmpty()) {
			Multimap<Attribute, AttributeModifier> modifiers = stack.getAttributeModifiers(slot);
			for (AttributeModifier modifier : modifiers.get(Attributes.ATTACK_DAMAGE)) {
				if (modifier.getOperation() == AttributeModifier.Operation.ADDITION) {
					bonus += modifier.getAmount();
				}
			}
		}
		return bonus;
	}

	public static void addDaggerReachModifier(Player player) {
		AttributeInstance entityReach = player.getAttribute(ForgeMod.ENTITY_REACH.get());
		if (entityReach != null) {
			removeDaggerReachModifier(player);

			AttributeModifier modifier = new AttributeModifier(
					DAGGER_REACH_MODIFIER_UUID,
					"Dagger reach",
					-0.75,
					AttributeModifier.Operation.ADDITION
			);
			entityReach.addTransientModifier(modifier);
		}
	}

	public static void removeDaggerReachModifier(Player player) {
		AttributeInstance entityReach = player.getAttribute(ForgeMod.ENTITY_REACH.get());
		if (entityReach != null) {
			entityReach.removeModifier(DAGGER_REACH_MODIFIER_UUID);
		}
	}

	public UseAnim getUseAnimation(ItemStack pStack) {
		if (getEnchantmentLevel(pStack, ModEnchantments.IMMOLATION.get()) > 0)
			return UseAnim.NONE;
		return UseAnim.SPEAR;
	}

	@Override
	public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltip, TooltipFlag pFlag) {
		DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
				ClientRPGtooltipHandler.appendHoverText(pStack, pLevel, pTooltip, pFlag, this)
		);
	}


	public ItemStack getDefaultInstance() {
		return PotionUtils.setPotion(super.getDefaultInstance(), Potions.POISON);
	}

	@Override
	public MutableComponent getDisplayFeatures(ItemStack item) {
		if (getEnchantmentLevel(item, ModEnchantments.IMMOLATION.get()) > 0) {
			return Component.translatable(this.getDescriptionId() + ".features.immolation");
		} else {
			return Component.translatable(this.getDescriptionId() + ".features");
		}
	}

	@Override
	public void initializeClient(Consumer<IClientItemExtensions> consumer) {
		consumer.accept(new IClientItemExtensions() {

			private static final HumanoidModel.ArmPose DAGGER_POSE = HumanoidModel.ArmPose.create("DAGGER", false, (model, entity, arm) -> {
				if (arm == HumanoidArm.RIGHT) {
					model.rightArm.xRot = Mth.clamp(model.head.xRot, -1.2F, 1.2F) - 1.4835298F;
					model.rightArm.yRot = 0F;
				} else {
					model.leftArm.xRot = Mth.clamp(model.head.xRot, -1.2F, 1.2F) - 1.4835298F;
					model.leftArm.yRot = 0F;
				}
			});


			@Override
			public HumanoidModel.ArmPose getArmPose(LivingEntity entityLiving, InteractionHand hand, ItemStack itemStack) {
				if (!itemStack.isEmpty() && itemStack.getEnchantmentLevel(ModEnchantments.IMMOLATION.get()) > 0) {
					if (entityLiving.isUsingItem() && entityLiving.getUseItemRemainingTicks() > 0 && entityLiving.getUsedItemHand() == hand) {
						return DAGGER_POSE;
					}
				}
				return HumanoidModel.ArmPose.ITEM;
			}

			@Override
			public boolean applyForgeHandTransform(PoseStack poseStack, LocalPlayer player,
												   HumanoidArm arm, ItemStack itemInHand, float partialTick,
												   float equipProcess, float swingProcess) {

				if (player.isUsingItem()
						&& player.getUseItem().getItem() instanceof DaggerItem dagger
						&& getEnchantmentLevel(player.getUseItem(), ModEnchantments.IMMOLATION.get()) > 0
						&& player.getUsedItemHand() == (arm == HumanoidArm.RIGHT ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND)) {
					int direction = arm == HumanoidArm.RIGHT ? 1 : -1;
					float progress = Math.min(1.0F, (float) (dagger.getUseDuration(player.getUseItem()) - player.getUseItemRemainingTicks()) / (float) dagger.getAttackCooldown());

					float halfProgress = Math.min(0.7F, progress);
					poseStack.rotateAround(Axis.XP.rotationDegrees(((0.7F - halfProgress) / 0.7F) * -105F), 0.4F * direction, -0.2F, -1F);
					poseStack.rotateAround(Axis.ZP.rotationDegrees(((0.7F - halfProgress) / 0.7F) * 30F * direction), 0.4F * direction, -0.2F, -1F);
					poseStack.translate(((0.7F - halfProgress) / 0.7F) * 0.1F * direction, ((0.7F - halfProgress) / 0.7F) * -0.3F, ((0.7F - halfProgress) / 0.7F) * -0.3F);
					poseStack.mulPose(Axis.ZP.rotationDegrees((halfProgress / 0.7F) * 35F * direction));
					poseStack.translate(-0.225F * direction, 0F, 0F);

					poseStack.mulPose(Axis.XN.rotationDegrees(45F));
					poseStack.mulPose(Axis.YN.rotationDegrees(0F));
					poseStack.mulPose(Axis.ZN.rotationDegrees(-70F * direction));
					if (progress >= 0.35F)
						poseStack.rotateAround(Axis.XP.rotationDegrees(((Math.min(0.85F, progress) - 0.35F) / 0.5F) * 270F * direction), 0.7F * direction, -0.4F, -0.9F);
					float useTime = (float) itemInHand.getUseDuration() - ((float) player.getUseItemRemainingTicks() - partialTick + 1.0F);
					float rotateProgress = useTime / 20.0F;
					rotateProgress = (rotateProgress * rotateProgress + rotateProgress * 2.0F) / 3.0F;
					if (rotateProgress > 1.0F) rotateProgress = 1.0F;

					if (rotateProgress > 0.1F) {
						float shake = Mth.sin((useTime - 0.1F) * 1.3F);
						float shakeAmount = shake * (rotateProgress - 0.1F);
						poseStack.rotateAround(Axis.YP.rotationDegrees(shakeAmount), 0.7F * direction, -0.4F, -0.9F);
					}
					poseStack.rotateAround(Axis.YP.rotationDegrees(-20F * direction), 0.7F * direction, -0.4F, -0.9F);
					poseStack.rotateAround(Axis.ZP.rotationDegrees(-20F * direction), 0.7F * direction, -0.4F, -0.9F);
					poseStack.mulPose(Axis.ZP.rotationDegrees(-70F * direction));
					poseStack.mulPose(Axis.XP.rotationDegrees(45F));

					return true;
				}
				return false;
			}


			private void drawPivotAxes(PoseStack poseStack, float cx, float cy, float cz) {
				Minecraft mc = Minecraft.getInstance();
				MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();
				VertexConsumer vc = bufferSource.getBuffer(RenderType.LINES);
				Matrix4f matrix = poseStack.last().pose();
				Matrix3f normal = poseStack.last().normal();

				float size = 0.2F;


				vc.vertex(matrix, cx - size, cy, cz)
						.color(255, 0, 0, 255)
						.uv(0, 0).overlayCoords(OverlayTexture.NO_OVERLAY)
						.uv2(0x00F000F0).normal(normal, 1, 0, 0).endVertex();
				vc.vertex(matrix, cx + size, cy, cz)
						.color(255, 0, 0, 255)
						.uv(0, 0).overlayCoords(OverlayTexture.NO_OVERLAY)
						.uv2(0x00F000F0).normal(normal, 1, 0, 0).endVertex();


				vc.vertex(matrix, cx, cy - size, cz)
						.color(0, 255, 0, 255)
						.uv(0, 0).overlayCoords(OverlayTexture.NO_OVERLAY)
						.uv2(0x00F000F0).normal(normal, 0, 1, 0).endVertex();
				vc.vertex(matrix, cx, cy + size, cz)
						.color(0, 255, 0, 255)
						.uv(0, 0).overlayCoords(OverlayTexture.NO_OVERLAY)
						.uv2(0x00F000F0).normal(normal, 0, 1, 0).endVertex();


				vc.vertex(matrix, cx, cy, cz - size)
						.color(0, 0, 255, 255)
						.uv(0, 0).overlayCoords(OverlayTexture.NO_OVERLAY)
						.uv2(0x00F000F0).normal(normal, 0, 0, 1).endVertex();
				vc.vertex(matrix, cx, cy, cz + size)
						.color(0, 0, 255, 255)
						.uv(0, 0).overlayCoords(OverlayTexture.NO_OVERLAY)
						.uv2(0x00F000F0).normal(normal, 0, 0, 1).endVertex();

				bufferSource.endBatch();
			}
		});
	}
}