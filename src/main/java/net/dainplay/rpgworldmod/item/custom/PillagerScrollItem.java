package net.dainplay.rpgworldmod.item.custom;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.dainplay.rpgworldmod.damage.ModDamageTypes;
import net.dainplay.rpgworldmod.data.tags.ModAdvancements;
import net.dainplay.rpgworldmod.effect.ModEffects;
import net.dainplay.rpgworldmod.enchantment.ModEnchantments;
import net.dainplay.rpgworldmod.entity.ModEntities;
import net.dainplay.rpgworldmod.entity.custom.FriendlyRavager;
import net.dainplay.rpgworldmod.entity.custom.FriendlyVex;
import net.dainplay.rpgworldmod.item.ModItems;
import net.dainplay.rpgworldmod.network.ClientManaData;
import net.dainplay.rpgworldmod.network.LoopSoundPacket;
import net.dainplay.rpgworldmod.network.ModMessages;
import net.dainplay.rpgworldmod.network.PlayerManaProvider;
import net.dainplay.rpgworldmod.network.S2CGuardianAttackData;
import net.dainplay.rpgworldmod.network.UpdateItemTagMessage;
import net.dainplay.rpgworldmod.sounds.RPGSounds;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundGameEventPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.EvokerFangs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class PillagerScrollItem extends ScrollItem {
	private static final Map<Level, Map<UUID, PlayerUseData>> playerUseData = new HashMap<>();

	public PillagerScrollItem(Properties pProperties) {
		super(pProperties);
	}

	@Override
	public String getTexture(ItemStack stack, Entity entity) {
		return "textures/entity/spells/green_spark";
	}

	@Override
	public float get1YOffset(ItemStack stack, Entity entity) {
		return 0.175F;
	}

	@Override
	public float get1XOffset(ItemStack stack, Entity entity) {
		return 0.2F;
	}

	@Override
	public float getSize(ItemStack stack, Entity entity) {
		return 0.3F;
	}

	@Override
	public float get1Size(ItemStack stack, Entity entity) {
		return 0.3F;
	}

	@Override
	public PoseStack getUsingPose(ItemStack stack, Player player, PoseStack poseStack, float flip) {
		if (stack.getEnchantmentLevel(ModEnchantments.ALTERATION.get()) > 0) {
			poseStack.mulPose(Axis.ZP.rotationDegrees(flip * 36.0F));
			poseStack.mulPose(Axis.XP.rotationDegrees(-7.0F));
			poseStack.mulPose(Axis.YP.rotationDegrees((-(float) Math.PI / 6F)));
			poseStack.translate(0F, 0.4F, 0F);
			float shakeRotY = (float) (Math.cos(player.getTicksUsingItem() * 1.5) * 0.3F);
			poseStack.mulPose(Axis.YP.rotationDegrees(shakeRotY));
		}
		if (stack.getEnchantmentLevel(ModEnchantments.RESTORATION.get()) > 0) {
			poseStack.mulPose(Axis.ZP.rotationDegrees(flip * 36.0F));
			poseStack.mulPose(Axis.XP.rotationDegrees(-7.0F));
			poseStack.mulPose(Axis.YP.rotationDegrees((-(float) Math.PI / 6F)));
			poseStack.translate(0F, 0.4F, 0F);
			float shakeRotY = (float) (Math.cos(player.getTicksUsingItem() * 1.5) * 0.3F);
			poseStack.mulPose(Axis.YP.rotationDegrees(shakeRotY));
		}
		if (stack.getEnchantmentLevel(ModEnchantments.CONJURATION.get()) > 0) {
			poseStack.mulPose(Axis.ZP.rotationDegrees(flip * 36.0F));
			poseStack.mulPose(Axis.XP.rotationDegrees(-7.0F));
			poseStack.mulPose(Axis.YP.rotationDegrees((-(float) Math.PI / 6F)));
			poseStack.translate(0F, 0.4F, 0F);
			float shakeRotY = (float) (Math.cos(player.getTicksUsingItem() * 1.5) * 0.3F);
			poseStack.mulPose(Axis.YP.rotationDegrees(shakeRotY));
		}
		return poseStack;
	}

	@Override
	public int getAnimationSpeed(ItemStack stack, Entity entity) {
		return 3;
	}

	@Override
	public int getAnimationLength(ItemStack stack, Entity entity) {
		return 7;
	}

	@Override
	public boolean shouldRotate(ItemStack stack, Entity entity) {
		return true;
	}

	@Override
	public void initializeClient(Consumer<IClientItemExtensions> consumer) {
		consumer.accept(new IClientItemExtensions() {
			private static final HumanoidModel.ArmPose ACTIVE_USE_POSE = HumanoidModel.ArmPose.create("ACTIVE_USE", false, (model, entity, arm) -> {
				if (arm == HumanoidArm.RIGHT) {
					model.rightArm.xRot = model.rightArm.xRot * 0.5F - ((float) Math.PI * 0.8F);
					model.rightArm.zRot = model.rightArm.zRot * 0.5F - ((float) Math.PI * 0.1F);
					model.rightArm.yRot = 0F;
				} else {
					model.leftArm.xRot = model.leftArm.xRot * 0.5F - ((float) Math.PI * 0.8F);
					model.leftArm.zRot = model.leftArm.zRot * 0.5F + ((float) Math.PI * 0.1F);
					model.leftArm.yRot = 0F;
				}
			});

			@Override
			public HumanoidModel.ArmPose getArmPose(LivingEntity entityLiving, InteractionHand hand, ItemStack itemStack) {
				if (!itemStack.isEmpty() && itemStack.getEnchantmentLevel(ModEnchantments.CONJURATION.get()) > 0) {
					if (entityLiving.isUsingItem() && entityLiving.getUseItemRemainingTicks() > 0 && entityLiving.getUsedItemHand() == hand) {
						return ACTIVE_USE_POSE;
					}
				}
				if (!itemStack.isEmpty() && itemStack.getEnchantmentLevel(ModEnchantments.ALTERATION.get()) > 0) {
					if (entityLiving.isUsingItem() && entityLiving.getUseItemRemainingTicks() > 0 && entityLiving.getUsedItemHand() == hand) {
						return ACTIVE_USE_POSE;
					}
				}
				if (!itemStack.isEmpty() && itemStack.getEnchantmentLevel(ModEnchantments.RESTORATION.get()) > 0) {
					if (entityLiving.isUsingItem() && entityLiving.getUseItemRemainingTicks() > 0 && entityLiving.getUsedItemHand() == hand) {
						return ACTIVE_USE_POSE;
					}
				}
				return HumanoidModel.ArmPose.ITEM;
			}
		});
	}

	@Override
	public int getColor(ItemStack stack, Entity entity) {
		return -65536;
	}

	@Override
	public String getDisplayManaCost(ItemStack item, Player player) {
		if (EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.DESTRUCTION.get(), item) > 0) {
			return "7";
		}
		if (EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.ALTERATION.get(), item) > 0) {
			return "1";
		}
		if (EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.ILLUSION.get(), item) > 0) {
			return "20";
		}
		if (EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.RESTORATION.get(), item) > 0) {
			return "MAX";
		}
		if (EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.CONJURATION.get(), item) > 0) {
			return "35";
		}
		if (EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.NECROMANCY.get(), item) > 0) {
			return "16";
		}
		return "5";
	}

	@Override
	public int getManaCost(ItemStack item, Player player) {
		if (EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.DESTRUCTION.get(), item) > 0) {
			return 7;
		}
		if (EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.RESTORATION.get(), item) > 0) {
			if (player.level().isClientSide) {
				if (ClientManaData.get() <= 100) return 100;
				else return ClientManaData.get();
			} else {
				AtomicInteger result = new AtomicInteger(100);
				player.getCapability(PlayerManaProvider.PLAYER_MANA).ifPresent(mana -> {
					if (mana.getMana() > 100) {
						result.set(mana.getMana());
					}
				});
				return result.get();
			}
		}
		if (EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.ALTERATION.get(), item) > 0) {
			return 1;
		}
		if (EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.ILLUSION.get(), item) > 0) {
			return 20;
		}
		if (EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.CONJURATION.get(), item) > 0) {
			return 35;
		}
		if (EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.NECROMANCY.get(), item) > 0) {
			return 16;
		}
		return 5;
	}

	public Component getManaCostAdditionalLine(ItemStack item) {
		if (EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.RESTORATION.get(), item) > 0) {
			return Component.translatable("tooltip.rpgworldmod.cost_minimum", 100).withStyle(ChatFormatting.BLUE);
		}
		return Component.literal("");
	}

	public static ItemStack createForEnchantment(EnchantmentInstance pInstance) {
		ItemStack itemstack = new ItemStack(ModItems.PILLAGER_SCROLL.get());
		itemstack.enchant(pInstance.enchantment, pInstance.level);
		return itemstack;
	}

	public static void setUseTime(ItemStack stack, int useTime) {
		CompoundTag tag = stack.getOrCreateTag();
		tag.putInt("PrevUseTime", getUseTime(stack));
		tag.putInt("UseTime", useTime);
	}

	public static int getUseTime(ItemStack stack) {
		CompoundTag tag = stack.getTag();
		return tag != null ? tag.getInt("UseTime") : 0;
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		ItemStack itemstack = player.getItemInHand(hand);

		if (!hasAnyEnchant(itemstack)) {
			return InteractionResultHolder.fail(itemstack);
		}

		if (!level.isClientSide) {
			if (EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.NECROMANCY.get(), itemstack) > 0) {
				return InteractionResultHolder.pass(itemstack);
			}
			if (!player.isShiftKeyDown() && EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.ALTERATION.get(), itemstack) > 0) {
				return InteractionResultHolder.pass(itemstack);
			}
			if (EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.DESTRUCTION.get(), itemstack) > 0) {
				if (canUse(player, itemstack)) {
					if (player.isShiftKeyDown()) {
						performDestructionCircle(level, player, itemstack);
					} else {
						performDestructionLine(level, player, itemstack);
					}
				}
				return InteractionResultHolder.consume(itemstack);
			}
			if (EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.ILLUSION.get(), itemstack) > 0) {
				return handleIllusion(level, player, itemstack);
			}

			if (!canUse(player, itemstack)) {
				getPlayerUseData(level).remove(player.getUUID());
				ModMessages.sendToNearbyPlayers(new LoopSoundPacket(player.getId(), false, itemstack),
						level, player.blockPosition(), 64.0);
				return InteractionResultHolder.fail(itemstack);
			}

			getPlayerUseData(level).put(player.getUUID(), new PlayerUseData(player.getUUID(), level.getGameTime()));

			startEnchantmentSounds(level, player, itemstack);

			player.startUsingItem(hand);
		} else {
			if (!player.isShiftKeyDown() && EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.ALTERATION.get(), itemstack) > 0) {
				return InteractionResultHolder.pass(itemstack);
			}

			if (EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.NECROMANCY.get(), itemstack) > 0) {
				return InteractionResultHolder.pass(itemstack);
			}

			if (!canUseClient(player, itemstack)) {
				return InteractionResultHolder.fail(itemstack);
			}

			if (EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.ILLUSION.get(), itemstack) > 0) {
				return InteractionResultHolder.success(itemstack);
			}

			if (EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.DESTRUCTION.get(), itemstack) > 0) {
				return InteractionResultHolder.success(itemstack);
			}

			player.startUsingItem(hand);
		}

		return InteractionResultHolder.consume(itemstack);
	}

	private void performDestructionLine(Level level, Player player, ItemStack stack) {
		if (!(level instanceof ServerLevel serverLevel)) return;

		Vec3 eyePos = player.getEyePosition(1.0F);
		Vec3 lookVec = player.getLookAngle();
		Vec3 endPos = eyePos.add(lookVec.x * 16, lookVec.y * 16, lookVec.z * 16);
		HitResult hitResult = level.clip(new ClipContext(eyePos, endPos, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));

		Vec3 targetPos = hitResult.getType() == HitResult.Type.BLOCK ? hitResult.getLocation() : endPos;

		Vec3 startVec = new Vec3(player.getX(), player.getY() + 0.1, player.getZ());
		Vec3 endVec = new Vec3(targetPos.x, targetPos.y + 0.1, targetPos.z);
		double distance = startVec.distanceTo(endVec);
		int steps = Math.max(1, (int) Math.ceil(distance));

		for (int i = 0; i <= steps; i++) {
			double t = i / (double) steps;
			Vec3 point = startVec.lerp(endVec, t);
			int warmup = i;
			createSpellEntity(serverLevel, point.x, point.z, point.y, player, warmup);
		}

		player.getCooldowns().addCooldown(stack.getItem(), 15);
		level.playSound(null, player.getX(), player.getY(), player.getZ(),
				RPGSounds.SPELL_DESTRUCTION_PILLAGER.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
	}

	private void performDestructionCircle(Level level, Player player, ItemStack stack) {
		if (!(level instanceof ServerLevel serverLevel)) return;

		createCircleOfFangs(serverLevel, player, 1.5, 0, 5);
		createCircleOfFangs(serverLevel, player, 2.5, 3, 8);

		player.getCooldowns().addCooldown(stack.getItem(), 15);
		level.playSound(null, player.getX(), player.getY(), player.getZ(),
				RPGSounds.SPELL_DESTRUCTION_PILLAGER.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
	}

	private void createCircleOfFangs(ServerLevel level, Player player, double radius, int baseDelay, int count) {
		for (int i = 0; i < count; i++) {
			double angle = 2 * Math.PI * i / count;
			double x = player.getX() + radius * Math.cos(angle);
			double z = player.getZ() + radius * Math.sin(angle);
			double yHint = player.getY();
			createSpellEntity(level, x, z, yHint, player, baseDelay);
		}
	}

	private void createSpellEntity(ServerLevel level, double x, double z, double yHint, LivingEntity owner, int warmupDelay) {
		BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();
		mutablePos.set(x, yHint, z);

		int minY = Math.max(level.getMinBuildHeight(), Mth.floor(owner.getY()) - 4);

		boolean flag = false;
		double d0 = 0.0D;

		while (mutablePos.getY() >= minY) {
			BlockPos below = mutablePos.below();
			BlockState stateBelow = level.getBlockState(below);
			if (stateBelow.isFaceSturdy(level, below, Direction.UP)) {
				BlockState stateCurrent = level.getBlockState(mutablePos);
				if (!level.isEmptyBlock(mutablePos)) {
					VoxelShape voxelshape = stateCurrent.getCollisionShape(level, mutablePos);
					if (!voxelshape.isEmpty()) {
						d0 = voxelshape.max(Direction.Axis.Y);
					}
				}
				flag = true;
				break;
			}
			mutablePos.move(Direction.DOWN);
		}

		if (flag) {
			level.addFreshEntity(new EvokerFangs(level, x, mutablePos.getY() + d0, z, 0, warmupDelay, owner));
		}
	}

	public boolean hasAnyEnchant(ItemStack stack) {
		return EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.DESTRUCTION.get(), stack) > 0 ||
				EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.RESTORATION.get(), stack) > 0 ||
				EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.ALTERATION.get(), stack) > 0 ||
				EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.ILLUSION.get(), stack) > 0 ||
				EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.CONJURATION.get(), stack) > 0 ||
				EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.NECROMANCY.get(), stack) > 0;
	}

	private InteractionResultHolder<ItemStack> handleIllusion(Level level, Player player, ItemStack stack) {
		return InteractionResultHolder.success(stack);
	}

	private boolean canUse(Player player, ItemStack stack) {
		if (player.getAbilities().instabuild) return true;
		if (usesHealthInsteadOfMana(stack)) {
			return Mth.ceil(player.getHealth()) >= getManaCost(stack, player);
		} else {
			AtomicBoolean result = new AtomicBoolean(false);
			player.getCapability(PlayerManaProvider.PLAYER_MANA).ifPresent(mana -> {
				if (mana.getMana() >= getManaCost(stack, player)) {
					if (EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.RESTORATION.get(), stack) <= 0
							&& EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.CONJURATION.get(), stack) <= 0
							&& EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.ALTERATION.get(), stack) <= 0) {
						mana.reduceMana((ServerPlayer) player, getManaCost(stack, player));
					}
					result.set(true);
				}
			});
			return result.get();
		}
	}

	private boolean canUseClient(Player player, ItemStack stack) {
		if (player.getAbilities().instabuild) return true;
		if (usesHealthInsteadOfMana(stack)) {
			return Mth.ceil(player.getHealth()) >= getManaCost(stack, player);
		} else {
			return ClientManaData.get() >= getManaCost(stack, player);
		}
	}

	private void startEnchantmentSounds(Level level, Player player, ItemStack stack) {
		if (EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.ALTERATION.get(), stack) > 0) {
			level.playSound(null, player.getX(), player.getY(), player.getZ(),
					RPGSounds.SPELL_ALTERATION_START.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
			player.gameEvent(GameEvent.ENTITY_INTERACT, player);
			ModMessages.sendToNearbyPlayers(new LoopSoundPacket(player.getId(), true, stack),
					level, player.blockPosition(), 64.0);
		}
		if (EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.RESTORATION.get(), stack) > 0) {
			level.playSound(null, player.getX(), player.getY(), player.getZ(),
					RPGSounds.SPELL_RESTORATION_START.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
			ModMessages.sendToNearbyPlayers(new LoopSoundPacket(player.getId(), true, stack),
					level, player.blockPosition(), 64.0);
		}
		if (EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.CONJURATION.get(), stack) > 0) {
			ModMessages.sendToNearbyPlayers(new LoopSoundPacket(player.getId(), true, stack),
					level, player.blockPosition(), 64.0);
		}
	}

	@Override
	public InteractionResult useOn(UseOnContext context) {
		Level level = context.getLevel();
		Player player = context.getPlayer();
		ItemStack itemstack = context.getItemInHand();

		if (!player.isShiftKeyDown() && EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.ALTERATION.get(), itemstack) > 0) {
			return handleAlteration(context, level, player, itemstack);
		}
		if (EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.NECROMANCY.get(), itemstack) > 0) {
			return handleNecromancy(context, level, player, itemstack);
		}
		return InteractionResult.PASS;
	}

	private InteractionResult handleAlteration(UseOnContext context, Level level, Player player, ItemStack stack) {
		if (level.isClientSide) {
			if (player != null && !player.getAbilities().instabuild && ClientManaData.get() < getManaCost(stack, player)) {
				return InteractionResult.FAIL;
			}
			return InteractionResult.SUCCESS;
		}
		if (player == null) return InteractionResult.FAIL;

		if (!player.getAbilities().instabuild) {
			AtomicBoolean hasMana = new AtomicBoolean(true);
			player.getCapability(PlayerManaProvider.PLAYER_MANA).ifPresent(mana -> {
				if (mana.getMana() < getManaCost(stack, player)) {
					hasMana.set(false);
				} else {
					mana.reduceMana((ServerPlayer) player, getManaCost(stack, player));
				}
			});
			if (!hasMana.get()) return InteractionResult.FAIL;
		}
		return InteractionResult.CONSUME;
	}

	private InteractionResult handleNecromancy(UseOnContext context, Level level, Player player, ItemStack stack) {
		if (player == null) return InteractionResult.FAIL;

		BlockPos clickedPos = context.getClickedPos();
		BlockPos basePos = findTuffCubeStructure(level, clickedPos);

		if (basePos == null) {
			return InteractionResult.FAIL;
		}

		if (level.isClientSide) {
			if (!player.getAbilities().instabuild) {
				int amp = getManaCost(stack, player);
				if (Mth.ceil(player.getHealth()) < amp) {
					return InteractionResult.FAIL;
				}
			}
			return InteractionResult.SUCCESS;
		}

		if (!player.getAbilities().instabuild) {
			int amp = getManaCost(stack, player);
			if (Mth.ceil(player.getHealth()) < amp) {
				return InteractionResult.FAIL;
			}
			if (player.getMaxHealth() <= amp) {
				player.hurt(ModDamageTypes.getDamageSource(player.level(), ModDamageTypes.NECROSIS), Float.MAX_VALUE);
			} else {
				player.hurt(ModDamageTypes.getDamageSource(player.level(), ModDamageTypes.NECROSIS), amp);
				if (player.hasEffect(ModEffects.NECROSIS.get()))
					amp += 1 + player.getEffect(ModEffects.NECROSIS.get()).getAmplifier();
				MobEffectInstance necrosis = new MobEffectInstance(ModEffects.NECROSIS.get(), 1200, amp - 1);
				necrosis.setCurativeItems(new ArrayList<>());
				player.addEffect(necrosis);
			}
		}

		for (int x = 0; x < 2; x++) {
			for (int y = 0; y < 2; y++) {
				for (int z = 0; z < 2; z++) {
					BlockPos pos = basePos.offset(x, y, z);
					level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
					level.levelEvent(2001, pos, Block.getId(Blocks.TUFF.defaultBlockState()));
				}
			}
		}

		FriendlyRavager ravager = ModEntities.FRIENDLY_RAVAGER.get().create(level);
		if (ravager != null) {
			double centerX = basePos.getX() + 1.0;
			double centerY = basePos.getY() + 1.0;
			double centerZ = basePos.getZ() + 1.0;
			ravager.moveTo(centerX, centerY, centerZ, 0.0F, 0.0F);
			level.addFreshEntity(ravager);

			player.getCooldowns().addCooldown(this, 15);
			level.playSound(null, player.blockPosition(),
					RPGSounds.SPELL_NECROMANCY_CAST.get(),
					SoundSource.PLAYERS, 1.0F, (level.random.nextFloat() - level.random.nextFloat()) * 0.2F + 1.0F
			);
			player.gameEvent(GameEvent.ENTITY_DAMAGE, player);

			if (player instanceof ServerPlayer serverPlayer) {
			}
		}

		return InteractionResult.CONSUME;
	}

	@Nullable
	private BlockPos findTuffCubeStructure(Level level, BlockPos clickedPos) {
		for (int dx = 0; dx <= 1; dx++) {
			for (int dy = 0; dy <= 1; dy++) {
				for (int dz = 0; dz <= 1; dz++) {
					BlockPos basePos = clickedPos.offset(-dx, -dy, -dz);
					if (isTuffCubeAt(level, basePos)) {
						return basePos;
					}
				}
			}
		}
		return null;
	}

	private boolean isTuffCubeAt(Level level, BlockPos basePos) {
		for (int x = 0; x < 2; x++) {
			for (int y = 0; y < 2; y++) {
				for (int z = 0; z < 2; z++) {
					BlockPos pos = basePos.offset(x, y, z);
					if (!level.getBlockState(pos).is(Blocks.TUFF)) {
						return false;
					}
				}
			}
		}
		return true;
	}

	public void cast(Player player, LivingEntity target, ItemStack item) {
		AtomicBoolean hasEnoughMana = new AtomicBoolean(true);
		if (!player.getAbilities().instabuild) {
			player.getCapability(PlayerManaProvider.PLAYER_MANA).ifPresent(mana -> {
				if (mana.getMana() < getManaCost(item, player)) {
					hasEnoughMana.set(false);
				} else {
					mana.reduceMana((ServerPlayer) player, getManaCost(item, player));
				}
			});
		}
		player.getCooldowns().addCooldown(item.getItem(), 15);
		player.swing(player.getUsedItemHand());
		if (hasEnoughMana.get()) {
			MobEffectInstance mobeffectinstance = new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 1200, 2);
			if (target instanceof ServerPlayer serverPlayer)
				serverPlayer.connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.GUARDIAN_ELDER_EFFECT, 1.0F));
			target.addEffect(mobeffectinstance);
			if (player instanceof ServerPlayer serverPlayer)
				ModAdvancements.SPELL_ILLUSION_HEART_OF_THE_SEA_TRIGGER.trigger(serverPlayer);
			player.level().playSound(null,
					player.getX(), player.getY(), player.getZ(),
					RPGSounds.SPELL_ILLUSION_CAST.get(),
					SoundSource.PLAYERS, 1.0F, 1.0F
			);
		}
	}

	@Override
	public void releaseUsing(ItemStack stack, Level level, LivingEntity livingEntity, int timeCharged) {
		if (livingEntity instanceof Player player) {
			if (!level.isClientSide) {
				stopEnchantmentSounds(level, player, stack);
			} else {
				ModMessages.sendToServer(new UpdateItemTagMessage(player.getId(), stack));
			}
		}
		super.releaseUsing(stack, level, livingEntity, timeCharged);
	}

	private void stopEnchantmentSounds(Level level, Player player, ItemStack stack) {
		if (EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.RESTORATION.get(), stack) > 0) {
			level.playSound(null, player.getX(), player.getY(), player.getZ(),
					RPGSounds.SPELL_RESTORATION_STOP.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
		}
		if (EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.ALTERATION.get(), stack) > 0) {
			level.playSound(null, player.getX(), player.getY(), player.getZ(),
					RPGSounds.SPELL_ALTERATION_STOP.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
		}
		ModMessages.sendToNearbyPlayers(new LoopSoundPacket(player.getId(), false, stack),
				level, player.blockPosition(), 64.0);
	}

	@Override
	public int getUseDuration(ItemStack stack) {
		if (stack.getEnchantmentLevel(ModEnchantments.DESTRUCTION.get()) > 0) return 0;
		if (stack.getEnchantmentLevel(ModEnchantments.ILLUSION.get()) > 0) return 0;
		if (stack.getEnchantmentLevel(ModEnchantments.NECROMANCY.get()) > 0) return 0;
		return 72000;
	}

	@Override
	public void inventoryTick(ItemStack stack, Level level, Entity entity, int i, boolean held) {
		super.inventoryTick(stack, level, entity, i, held);
		if (level.isClientSide) {
			boolean using = entity instanceof LivingEntity living && living.getUseItem().equals(stack);
			int useTime = getUseTime(stack);
			CompoundTag tag = stack.getOrCreateTag();
			if (tag.getInt("PrevUseTime") != tag.getInt("UseTime")) {
				tag.putInt("PrevUseTime", getUseTime(stack));
			}
			if (using && useTime < 5.0F) {
				setUseTime(stack, useTime + 1);
			}
			if (!using && useTime > 0.0F) {
				setUseTime(stack, useTime - 1);
			}
		}
	}

	public static Map<UUID, PlayerUseData> getPlayerUseData(Level level) {
		return playerUseData.computeIfAbsent(level, k -> new HashMap<>());
	}

	private static void stopPlayerUse(ServerLevel level, Player player, ItemStack usingItem, boolean damageDealt) {
		if (player == null) return;
		player.stopUsingItem();
		getPlayerUseData(level).remove(player.getUUID());

		if (usingItem.getItem() instanceof PillagerScrollItem scroll) {
			scroll.stopEnchantmentSounds(level, player, usingItem);
		}

		ModMessages.sendToNearbyPlayers(
				new S2CGuardianAttackData(player.getId(), 0, 0, false, damageDealt),
				level, player.blockPosition(), 64.0
		);

		player.getCooldowns().addCooldown(usingItem.getItem(), 15);
	}

	@Override
	public void onUseTick(Level pLevel, LivingEntity pLivingEntity, ItemStack pStack, int pRemainingUseDuration) {
		super.onUseTick(pLevel, pLivingEntity, pStack, pRemainingUseDuration);
		if (pLevel instanceof ServerLevel serverLevel) {
			processPlayerUsageStatic(serverLevel);
		}
	}

	public static void processPlayerUsageStatic(ServerLevel level) {
		Map<UUID, PlayerUseData> levelPlayerUseData = getPlayerUseData(level);
		Map<UUID, PlayerUseData> copy = new HashMap<>(levelPlayerUseData);

		for (Map.Entry<UUID, PlayerUseData> entry : copy.entrySet()) {
			UUID playerId = entry.getKey();
			PlayerUseData useData = entry.getValue();

			Player player = level.getPlayerByUUID(playerId);
			if (player == null) {
				levelPlayerUseData.remove(playerId);
				continue;
			}

			if (!player.isUsingItem()) {
				levelPlayerUseData.remove(playerId);
				continue;
			}

			ItemStack usingItem = player.getUseItem();
			if (!(usingItem.getItem() instanceof PillagerScrollItem)) {
				levelPlayerUseData.remove(playerId);
				player.stopUsingItem();
				continue;
			}

			if (!hasAnyEnchantForContinuation(usingItem, player)) {
				levelPlayerUseData.remove(playerId);
				stopPlayerUse(level, player, usingItem, false);
				continue;
			}

			useData.tick();


			if (EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.CONJURATION.get(), usingItem) > 0 && useData.useTicks > 40) {
				if (usingItem.getItem() instanceof PillagerScrollItem pillagerScrollItem) {
					player.getCapability(PlayerManaProvider.PLAYER_MANA).ifPresent(mana -> {
						int cost = pillagerScrollItem.getManaCost(usingItem, player);
						if (mana.getMana() >= cost || player.getAbilities().instabuild) {
							if (!player.getAbilities().instabuild) mana.reduceMana((ServerPlayer) player, cost);
							useData.active = false;
							stopPlayerUse(level, player, usingItem, false);
							player.getCooldowns().addCooldown(ModItems.PILLAGER_SCROLL.get(), 15);
							level.playSound(null, player.getX(), player.getY(), player.getZ(),
									RPGSounds.SPELL_CONJURATION_PILLAGER.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
							for (int i = 0; i < 3; i++) {
								FriendlyVex vex = ModEntities.FRIENDLY_VEX.get().create(level);
								if (vex != null) {
									BlockPos spawnPos = player.blockPosition().offset(
											-1 + level.random.nextInt(3),
											1,
											-1 + level.random.nextInt(3)
									);
									vex.moveTo(spawnPos, 0.0F, 0.0F);
									vex.finalizeSpawn(level, level.getCurrentDifficultyAt(spawnPos),
											MobSpawnType.MOB_SUMMONED, null, null);
									vex.setOwnerUUID(player.getUUID());
									vex.setLimitedLife(20 * 60);
									level.addFreshEntityWithPassengers(vex);
								}
							}
							player.gameEvent(GameEvent.ENTITY_PLACE, player);
							if (player instanceof ServerPlayer serverPlayer)
								ModAdvancements.SPELL_CONJURATION_NETHER_STAR_TRIGGER.trigger(serverPlayer);
						}
					});
				}
			}
		}
	}

	private static boolean hasAnyEnchantForContinuation(ItemStack stack, Player player) {
		return EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.RESTORATION.get(), stack) > 0 ||
				(EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.ALTERATION.get(), stack) > 0 && player.isShiftKeyDown()) ||
				EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.CONJURATION.get(), stack) > 0;
	}

	public static class PlayerUseData {
		private int useTicks;
		private int lastManaTick;
		public boolean active;
		public UUID currentTargetUUID = null;
		public int attackTime = 0;

		public PlayerUseData(UUID playerId, long startTime) {
			this.useTicks = 0;
			this.lastManaTick = 0;
			this.active = true;
		}

		public void tick() {
			useTicks++;
		}
	}

	public String getFirstPredicate(ItemStack item) {
		return Minecraft.getInstance().options.keyShift.getKey().getDisplayName().getString();
	}

	public String getSecondPredicate(ItemStack item) {
		return Minecraft.getInstance().options.keyAttack.getKey().getDisplayName().getString();
	}

	public boolean hasControls(ItemStack item) {
		return item.getEnchantmentLevel(ModEnchantments.ALTERATION.get()) > 0;
	}
}