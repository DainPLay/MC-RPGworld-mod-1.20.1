package net.dainplay.rpgworldmod.item.custom;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.dainplay.rpgworldmod.block.custom.EntFaceBlock;
import net.dainplay.rpgworldmod.damage.ModDamageTypes;
import net.dainplay.rpgworldmod.data.tags.ModAdvancements;
import net.dainplay.rpgworldmod.effect.ModEffects;
import net.dainplay.rpgworldmod.enchantment.ModEnchantments;
import net.dainplay.rpgworldmod.entity.ModEntities;
import net.dainplay.rpgworldmod.entity.custom.ConjuredDolphin;
import net.dainplay.rpgworldmod.entity.custom.EnderEyeViewEntity;
import net.dainplay.rpgworldmod.item.ModItems;
import net.dainplay.rpgworldmod.network.ClientManaData;
import net.dainplay.rpgworldmod.network.LoopSoundPacket;
import net.dainplay.rpgworldmod.network.ModMessages;
import net.dainplay.rpgworldmod.network.PlayerIllusionForceProvider;
import net.dainplay.rpgworldmod.network.PlayerManaProvider;
import net.dainplay.rpgworldmod.network.S2CGuardianAttackData;
import net.dainplay.rpgworldmod.network.S2CViewEyePacket;
import net.dainplay.rpgworldmod.network.UpdateItemTagMessage;
import net.dainplay.rpgworldmod.sounds.RPGSounds;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.StructureTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.control.LookControl;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlockContainer;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.WaterFluid;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.fml.DistExecutor;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class EnderEyeScrollItem extends ScrollItem {

	// Хранилище для отслеживания использования игроком с привязкой к уровню
	private static final Map<Level, Map<UUID, PlayerUseData>> playerUseData = new HashMap<>();

	private static final double MAX_BREATH_DISTANCE = 5.0D;
	private static final float CLOUD_MIN_RADIUS = 1.0F;
	private static final float CLOUD_MAX_RADIUS = 5.0F;
	private static final int CLOUD_DURATION = 200;

	public EnderEyeScrollItem(Properties pProperties) {
		super(pProperties);
	}

	@Override
	public String getTexture(ItemStack stack, Entity entity) {
		if (entity instanceof Player player && player.isUsingItem() && player.getUseItem() == stack) {
			return "textures/entity/spells/ender";
		}
		return "textures/entity/spells/ender_dust";
	}

	@Override
	public float get1YOffset(ItemStack stack, Entity entity) {
		if (entity instanceof Player player && player.isUsingItem() && player.getUseItem() == stack) {
			return 0.0F;
		}
		return 0.2F;
	}

	@Override
	public float get1XOffset(ItemStack stack, Entity entity) {
		if (entity instanceof Player player && player.isUsingItem() && player.getUseItem() == stack) {
			return 0.15F;
		}
		return 0.2F;
	}

	@Override
	public float getSize(ItemStack stack, Entity entity) {
		if (entity instanceof Player player && player.isUsingItem() && player.getUseItem() == stack) {
			return 0.25F;
		}
		return 0.15F;
	}

	@Override
	public float get1Size(ItemStack stack, Entity entity) {
		if (entity instanceof Player player && player.isUsingItem() && player.getUseItem() == stack) {
			if (stack.getEnchantmentLevel(ModEnchantments.DESTRUCTION.get()) > 0) {
				return 0.4F;
			}
			if (stack.getEnchantmentLevel(ModEnchantments.ILLUSION.get()) > 0) {
				return 0.4F;
			}
			return 0.5F;
		}
		return 0.3F;
	}

	@Override
	public float getZOffset(ItemStack stack, Entity entity) {
		if (entity instanceof Player player && player.isUsingItem() && player.getUseItem() == stack) {
			if (stack.getEnchantmentLevel(ModEnchantments.DESTRUCTION.get()) > 0) {
				return 0.1F;
			}
			if (stack.getEnchantmentLevel(ModEnchantments.ILLUSION.get()) > 0) {
				return 0.1F;
			}
			if (stack.getEnchantmentLevel(ModEnchantments.ALTERATION.get()) > 0) {
				return 0.2F;
			}
			if (stack.getEnchantmentLevel(ModEnchantments.RESTORATION.get()) > 0) {
				return 0.2F;
			}
		}
		return 0.05F;
	}

	@Override
	public float getZ(ItemStack stack, Entity entity) {
		if (entity instanceof Player player && player.isUsingItem() && player.getUseItem() == stack) {
			return 0F;
		}
		return -0.15F;
	}

	@Override
	public PoseStack getUsingPose(ItemStack stack, Player player, PoseStack poseStack, float flip) {
		if (stack.getEnchantmentLevel(ModEnchantments.DESTRUCTION.get()) > 0) {
			poseStack.mulPose(Axis.ZP.rotationDegrees(flip * -10.0F));
			poseStack.mulPose(Axis.YP.rotationDegrees((-(float) Math.PI / 6F)));
			poseStack.translate(0F, 0.25F, 0F);
			float shakeRotY = (float) (Math.cos(player.getTicksUsingItem() * 1.5) * 0.3F);
			poseStack.mulPose(Axis.YP.rotationDegrees(shakeRotY));
		}
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
		if (stack.getEnchantmentLevel(ModEnchantments.ILLUSION.get()) > 0) {
			poseStack.mulPose(Axis.ZP.rotationDegrees(flip * -10.0F));
			poseStack.mulPose(Axis.YP.rotationDegrees((-(float) Math.PI / 6F)));
			poseStack.translate(0F, 0.25F, 0F);
			float shakeRotY = (float) (Math.cos(player.getTicksUsingItem() * 1.5) * 0.3F);
			poseStack.mulPose(Axis.YP.rotationDegrees(shakeRotY));
		}
		return poseStack;
	}

	@Override
	public boolean highlightAnimateTarget(ItemStack stack, Player player) {
		if (stack.getEnchantmentLevel(ModEnchantments.ILLUSION.get()) > 0) {
			return player.isUsingItem() && player.getUseItemRemainingTicks() > 0 && player.getUseItem() == stack;
		}
		return false;
	}

	@Override
	public boolean canHighlightYourself(ItemStack stack, Player player) {
		return stack.getEnchantmentLevel(ModEnchantments.DESTRUCTION.get()) <= 0;
	}

	@Override
	public PoseStack getEffectUsingPose(ItemStack stack, Player player, PoseStack poseStack, float flip) {
		if (stack.getEnchantmentLevel(ModEnchantments.DESTRUCTION.get()) > 0) {
			poseStack.translate(flip * 0.05F, 0.05F, -0.31F);
		}
		if (stack.getEnchantmentLevel(ModEnchantments.ALTERATION.get()) > 0) {
			poseStack.translate(flip * 0.35F, 0.5F, 0F);
		}
		if (stack.getEnchantmentLevel(ModEnchantments.RESTORATION.get()) > 0) {
			poseStack.translate(flip * 0.35F, 0.5F, 0F);
		}
		if (stack.getEnchantmentLevel(ModEnchantments.ILLUSION.get()) > 0) {
			poseStack.translate(flip * 0.05F, 0.05F, -0.31F);
		}
		return poseStack;
	}

	@Override
	public int getAnimationSpeed(ItemStack stack, Entity entity) {
		return 2;
	}

	@Override
	public int getAnimationLength(ItemStack stack, Entity entity) {
		return 8;
	}

	@Override
	public void initializeClient(Consumer<IClientItemExtensions> consumer) {
		consumer.accept(new IClientItemExtensions() {

			private static final HumanoidModel.ArmPose DESTRUCTION_POSE = HumanoidModel.ArmPose.create("DESTRUCTION", false, (model, entity, arm) -> {
				if (arm == HumanoidArm.RIGHT) {
					model.rightArm.xRot = (-(float) Math.PI / 2F) + model.head.xRot;
					model.rightArm.yRot = -0.1F + model.head.yRot;
				} else {
					model.leftArm.xRot = (-(float) Math.PI / 2F) + model.head.xRot;
					model.leftArm.yRot = 0.1F + model.head.yRot;
				}
			});

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
				if (!itemStack.isEmpty() && itemStack.getEnchantmentLevel(ModEnchantments.DESTRUCTION.get()) > 0) {
					if (entityLiving.isUsingItem() && entityLiving.getUseItemRemainingTicks() > 0 && entityLiving.getUsedItemHand() == hand) {
						return DESTRUCTION_POSE;
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
				if (!itemStack.isEmpty() && itemStack.getEnchantmentLevel(ModEnchantments.ILLUSION.get()) > 0) {
					if (entityLiving.isUsingItem() && entityLiving.getUseItemRemainingTicks() > 0 && entityLiving.getUsedItemHand() == hand) {
						return DESTRUCTION_POSE;
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
			return "5";
		}
		if (EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.ALTERATION.get(), item) > 0) {
			return "5";
		}
		if (EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.ILLUSION.get(), item) > 0) {
			return "20";
		}
		if (EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.RESTORATION.get(), item) > 0) {
			return "5";
		}
		if (EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.CONJURATION.get(), item) > 0) {
			return "12";
		}
		if (EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.NECROMANCY.get(), item) > 0) {
			return "4";
		}
		return "5";
	}

	@Override
	public int getManaCost(ItemStack item, Player player) {
		if (EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.DESTRUCTION.get(), item) > 0) {
			return 1;
		}
		if (EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.RESTORATION.get(), item) > 0) {
			return 1;
		}
		if (EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.ALTERATION.get(), item) > 0) {
			return 1;
		}
		if (EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.ILLUSION.get(), item) > 0) {
			return 20;
		}
		if (EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.CONJURATION.get(), item) > 0) {
			return 12;
		}
		if (EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.NECROMANCY.get(), item) > 0) {
			return 4;
		}
		return 5;
	}

	public Component getManaCostAdditionalLine(ItemStack item) {
		if (EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.DESTRUCTION.get(), item) > 0) {
			return Component.translatable("tooltip.rpgworldmod.cost_per_second").withStyle(ChatFormatting.BLUE);
		}
		if (EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.ALTERATION.get(), item) > 0) {
			return Component.translatable("tooltip.rpgworldmod.cost_per_second").withStyle(ChatFormatting.BLUE);
		}
		if (EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.RESTORATION.get(), item) > 0) {
			return Component.translatable("tooltip.rpgworldmod.cost_per_second").withStyle(ChatFormatting.BLUE);
		}
		return Component.literal("");
	}

	public static ItemStack createForEnchantment(EnchantmentInstance pInstance) {
		ItemStack itemstack = new ItemStack(ModItems.ENDER_EYE_SCROLL.get());
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
			if (EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.CONJURATION.get(), itemstack) > 0) {
				if (!canUse(player, itemstack)) {
					return InteractionResultHolder.fail(itemstack);
				}

				ServerLevel serverLevel = (ServerLevel) level;
				Vec3 eyePos = player.getEyePosition();
				Vec3 lookVec = player.getLookAngle();
				double maxDistance = 30.0;
				Vec3 targetPos = eyePos.add(lookVec.scale(maxDistance));

				EnderEyeViewEntity eye = new EnderEyeViewEntity(serverLevel, player, eyePos.x, eyePos.y, eyePos.z);
				eye.setYaw(player.getYRot());
				eye.setPitch(player.getXRot());
				eye.signalTo(targetPos);
				serverLevel.addFreshEntity(eye);

				ModMessages.sendToPlayer(new S2CViewEyePacket(eye.getId()), (ServerPlayer) player);

				player.getCooldowns().addCooldown(this, 15);
				level.playSound(null, player.blockPosition(), SoundEvents.ENDER_EYE_LAUNCH,
						SoundSource.PLAYERS, 1.0F, 1.0F);
				if (player instanceof ServerPlayer serverPlayer) {
					ModAdvancements.SPELL_CONJURATION_ENDER_EYE_TRIGGER.trigger(serverPlayer);
				}
				return InteractionResultHolder.success(itemstack);
			}
			if (EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.NECROMANCY.get(), itemstack) > 0) {
				return handleNecromancy(level, player, itemstack);
			}

			// Общая проверка маны
			if (!canUse(player, itemstack)) {
				// Если не хватает маны, не начинаем использование
				ModMessages.sendToNearbyPlayers(new LoopSoundPacket(player.getId(), false, itemstack),
						(ServerLevel) level, player.blockPosition(), 64.0);
				return InteractionResultHolder.fail(itemstack);
			}

			// Получаем или создаём PlayerUseData
			Map<UUID, PlayerUseData> map = getPlayerUseData(level);
			PlayerUseData data = map.get(player.getUUID());
			if (data == null) {
				data = new PlayerUseData(player.getUUID(), level.getGameTime());
				map.put(player.getUUID(), data);
			} else {
				// Сбрасываем временные поля для нового использования
				data.startTime = level.getGameTime();
				data.useTicks = 0;
				data.lastManaTick = 0;
				data.breathProgress = 0;
				data.lastBreathTargetPos = null;
				data.active = true;
			}

			startEnchantmentSounds(level, player, itemstack);
			player.startUsingItem(hand);
		} else {
			// Клиентская сторона (без изменений)
			if (!canUseClient(player, itemstack)) {
				return InteractionResultHolder.fail(itemstack);
			}
			if (EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.CONJURATION.get(), itemstack) > 0) {
				if (!canUseClient(player, itemstack)) {
					return InteractionResultHolder.fail(itemstack);
				}
				return InteractionResultHolder.success(itemstack);
			}
			if (EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.NECROMANCY.get(), itemstack) > 0) {
				return InteractionResultHolder.success(itemstack);
			}
			player.startUsingItem(hand);
		}

		return InteractionResultHolder.consume(itemstack);
	}

	private boolean hasAnyEnchant(ItemStack stack) {
		return EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.DESTRUCTION.get(), stack) > 0 ||
				EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.RESTORATION.get(), stack) > 0 ||
				EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.ALTERATION.get(), stack) > 0 ||
				EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.ILLUSION.get(), stack) > 0 ||
				EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.CONJURATION.get(), stack) > 0 ||
				EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.NECROMANCY.get(), stack) > 0;
	}

	private InteractionResultHolder<ItemStack> handleNecromancy(Level level, Player player, ItemStack stack) {
		if (!player.getAbilities().instabuild) {
			int amp = getManaCost(stack, player);
			if (Mth.ceil(player.getHealth()) < amp) {
				return InteractionResultHolder.pass(stack);
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
		player.getCooldowns().addCooldown(this, 15);
		level.playSound(null, player.blockPosition(),
				SoundEvents.ENDER_CHEST_OPEN,
				SoundSource.PLAYERS, 1.0F, (level.random.nextFloat() - level.random.nextFloat()) * 0.2F + 1.0F
		);
		level.playSound(null, player.blockPosition(),
				RPGSounds.SPELL_NECROMANCY_CAST.get(),
				SoundSource.PLAYERS, 1.0F, (level.random.nextFloat() - level.random.nextFloat()) * 0.2F + 1.0F
		);
		if (player instanceof ServerPlayer serverPlayer) {
			ModAdvancements.SPELL_NECROMANCY_ENDER_EYE_TRIGGER.trigger(serverPlayer);
		}
		player.openMenu(new SimpleMenuProvider(
				(id, inv, p) -> ChestMenu.threeRows(id, inv, player.getEnderChestInventory()),
				Component.translatable("container.enderchest")
		));
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
					if (EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.DESTRUCTION.get(), stack) == 0) {
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
		if (EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.DESTRUCTION.get(), stack) > 0) {
			level.playSound(null, player.getX(), player.getY(), player.getZ(),
					RPGSounds.SPELL_DESTRUCTION_ENDER_EYE_START.get(), SoundSource.PLAYERS, 0.5F, 1.0F);
			ModMessages.sendToNearbyPlayers(new LoopSoundPacket(player.getId(), true, stack), level, player.blockPosition(), 64.0);
		}
		if (EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.ALTERATION.get(), stack) > 0) {
			level.playSound(null, player.getX(), player.getY(), player.getZ(),
					RPGSounds.SPELL_ALTERATION_START.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
			ModMessages.sendToNearbyPlayers(new LoopSoundPacket(player.getId(), true, stack), level, player.blockPosition(), 64.0);
		}
		if (EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.RESTORATION.get(), stack) > 0) {
			level.playSound(null, player.getX(), player.getY(), player.getZ(),
					RPGSounds.SPELL_RESTORATION_START.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
			ModMessages.sendToNearbyPlayers(new LoopSoundPacket(player.getId(), true, stack), level, player.blockPosition(), 64.0);
		}
		if (EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.ILLUSION.get(), stack) > 0) {
			level.playSound(null, player.getX(), player.getY(), player.getZ(),
					RPGSounds.SPELL_ILLUSION_START.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
			ModMessages.sendToNearbyPlayers(new LoopSoundPacket(player.getId(), true, stack), level, player.blockPosition(), 64.0);
		}
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
			MobEffectInstance mobeffectinstance = new MobEffectInstance(ModEffects.PARANOIA.get(), 400);
			target.addEffect(mobeffectinstance);
			if (player instanceof ServerPlayer serverPlayer)
				ModAdvancements.SPELL_ILLUSION_ENDER_EYE_TRIGGER.trigger(serverPlayer);
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
			UUID playerId = player.getUUID();

			if (!level.isClientSide) {
				// Не удаляем данные, а только деактивируем
				PlayerUseData data = getPlayerUseData(level).get(playerId);
				if (data != null) {
					data.active = false;
				}
				stopEnchantmentSounds(level, player, stack);
			} else {
				ModMessages.sendToServer(new UpdateItemTagMessage(player.getId(), stack));
			}
		}
		super.releaseUsing(stack, level, livingEntity, timeCharged);
	}

	private void stopEnchantmentSounds(Level level, Player player, ItemStack stack) {
		if (EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.DESTRUCTION.get(), stack) > 0) {
			level.playSound(null, player.getX(), player.getY(), player.getZ(),
					RPGSounds.SPELL_DESTRUCTION_ENDER_EYE_STOP.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
		}
		if (EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.ALTERATION.get(), stack) > 0) {
			level.playSound(null, player.getX(), player.getY(), player.getZ(),
					RPGSounds.SPELL_ALTERATION_STOP.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
		}
		if (EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.RESTORATION.get(), stack) > 0) {
			level.playSound(null, player.getX(), player.getY(), player.getZ(),
					RPGSounds.SPELL_RESTORATION_STOP.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
		}
		if (EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.ILLUSION.get(), stack) > 0) {
			level.playSound(null, player.getX(), player.getY(), player.getZ(),
					RPGSounds.SPELL_ILLUSION_STOP.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
		}
		ModMessages.sendToNearbyPlayers(new LoopSoundPacket(player.getId(), false, stack),
				(ServerLevel) level, player.blockPosition(), 64.0);
	}

	@Override
	public int getUseDuration(ItemStack stack) {
		if (stack.getEnchantmentLevel(ModEnchantments.CONJURATION.get()) > 0) return 0;
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

		// Не удаляем данные, только деактивируем
		PlayerUseData data = getPlayerUseData(level).get(player.getUUID());
		if (data != null) {
			data.active = false;
		}

		if (usingItem.getItem() instanceof EnderEyeScrollItem scroll) {
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
		if (pLivingEntity instanceof Player player && getEnchantmentLevel(pStack, ModEnchantments.DESTRUCTION.get()) > 0) {
			if (pLevel.isClientSide) {
				DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
						spawnDestructionParticles(pLevel, player)
				);
			}
		}
	}

	private Vec3 getMuzzlePositionForParticles(Player player) {
		Minecraft mc = Minecraft.getInstance();
		boolean firstPerson = mc.options.getCameraType().isFirstPerson();
		InteractionHand usedHand = player.getUsedItemHand();
		boolean mainHand = usedHand == InteractionHand.MAIN_HAND;
		HumanoidArm arm = mainHand ? player.getMainArm() : player.getMainArm().getOpposite();
		int side = arm == HumanoidArm.LEFT ? -1 : 1;
		Vec3 toReturn;

		if (player == mc.player && firstPerson) {
			Camera camera = mc.gameRenderer.getMainCamera();
			Vec3 cameraPos = camera.getPosition();
			double scale = 1000.0 / mc.getEntityRenderDispatcher().options.fov().get().intValue();
			Vec3 nearPoint = camera.getNearPlane().getPointOnPlane(side * 0.35F, -0.25F);
			nearPoint = nearPoint.scale(scale);
			return cameraPos.add(nearPoint);
		} else {
			float yBodyRot = Mth.lerp(1.0F, player.yBodyRotO, player.yBodyRot);
			double armOffsetX = player.getBbWidth() * -0.5 * side;
			double armOffsetY = player.getBbHeight() * 0.8;
			Vec3 offset = new Vec3(armOffsetX, armOffsetY, 0).yRot((float) Math.toRadians(-yBodyRot));
			Vec3 viewVec = player.getViewVector(1.0F).normalize().scale(1.5);
			return player.position().add(offset).add(viewVec);
		}
	}

	private void spawnDestructionParticles(Level level, Player player) {
		Vec3 lookVec = player.getLookAngle().normalize();
		Vec3 muzzlePos = getMuzzlePositionForParticles(player);
		RandomSource random = level.random;

		// Трассировка луча для определения ближайшего блока на пути
		ClipContext context = new ClipContext(
				muzzlePos,
				muzzlePos.add(lookVec.scale(MAX_BREATH_DISTANCE)),
				ClipContext.Block.COLLIDER,
				ClipContext.Fluid.NONE,
				player
		);
		BlockHitResult result = level.clip(context);

		double maxDist = MAX_BREATH_DISTANCE;
		if (result.getType() == HitResult.Type.BLOCK) {
			maxDist = muzzlePos.distanceTo(result.getLocation());
			// Если блок слишком близко (менее 0.5 блока), частицы не спавнятся
			if (maxDist < 0.5) return;
		}

		// Базовая скорость по аналогии с WealdBlade (подставлена наша дальность)
		double baseSpeed = MAX_BREATH_DISTANCE / 8.5 * 15 * 0.03; // ≈0.265 блока/тик
		double speedFactor = baseSpeed * (0.8 + random.nextDouble() * 0.4); // 0.8–1.2 от базовой

		int particleCount = 3 + random.nextInt(3); // 3–5 частиц за тик

		for (int i = 0; i < particleCount; i++) {
			// Случайная точка вдоль луча от дула до maxDist
			double distFactor = random.nextDouble();
			double distAlong = maxDist * distFactor;
			Vec3 pos = muzzlePos.add(lookVec.scale(distAlong));

			// Смещение перпендикулярно направлению взгляда
			Vec3 up = new Vec3(0, 1, 0);
			Vec3 right = lookVec.cross(up).normalize();
			if (right.lengthSqr() < 0.1) {
				// Если взгляд почти вертикальный, используем другую опорную ось
				right = new Vec3(1, 0, 0);
			}
			Vec3 upReal = right.cross(lookVec).normalize();

			double offsetX = (random.nextDouble() - 0.5) * 0.6;
			double offsetY = (random.nextDouble() - 0.5) * 0.6;
			pos = pos.add(right.scale(offsetX)).add(upReal.scale(offsetY));

			// Скорость с небольшим случайным шумом
			double velX = lookVec.x * speedFactor + (random.nextDouble() - 0.5) * 0.05;
			double velY = lookVec.y * speedFactor + (random.nextDouble() - 0.5) * 0.05;
			double velZ = lookVec.z * speedFactor + (random.nextDouble() - 0.5) * 0.05;

			level.addParticle(ParticleTypes.DRAGON_BREATH,
					pos.x, pos.y, pos.z,
					velX, velY, velZ);
		}
	}

	public static void processPlayerUsageStatic(ServerLevel level) {
		Map<UUID, PlayerUseData> levelPlayerUseData = getPlayerUseData(level);
		Map<UUID, PlayerUseData> copy = new HashMap<>(levelPlayerUseData);

		for (Map.Entry<UUID, PlayerUseData> entry : copy.entrySet()) {
			UUID playerId = entry.getKey();
			PlayerUseData data = entry.getValue();

			// Проверяем активность
			if (!data.active) {
				continue;
			}

			Player player = level.getPlayerByUUID(playerId);
			if (player == null) {
				// Игрок вышел — можно удалить данные (опционально)
				levelPlayerUseData.remove(playerId);
				continue;
			}

			// Очищаем список облаков от мёртвых
			data.ownedClouds.removeIf(cloudId -> {
				Entity e = level.getEntity(cloudId);
				return !(e instanceof AreaEffectCloud) || !e.isAlive();
			});

			if (!player.isUsingItem()) {
				// Игрок перестал использовать предмет, но данные ещё активны — деактивируем
				data.active = false;
				continue;
			}

			ItemStack usingItem = player.getUseItem();
			if (!(usingItem.getItem() instanceof EnderEyeScrollItem)) {
				data.active = false;
				player.stopUsingItem();
				continue;
			}

			if (!hasAnyEnchantForContinuation(usingItem)) {
				data.active = false;
				stopPlayerUse(level, player, usingItem, false);
				continue;
			}

			data.tick();

			if (data.shouldConsumeMana(usingItem, level)) {
				if (!player.getAbilities().instabuild) {
					AtomicBoolean hasMana = new AtomicBoolean(true);
					player.getCapability(PlayerManaProvider.PLAYER_MANA).ifPresent(mana -> {
						if (usingItem.getItem() instanceof ManaCostItem manaCostItem) {
							if (mana.getMana() < manaCostItem.getManaCost(usingItem, player)) {
								hasMana.set(false);
							} else {
								int cost = manaCostItem.getManaCost(usingItem, player);
								if (usingItem.getEnchantmentLevel(ModEnchantments.DESTRUCTION.get()) > 0) {
									cost = 1;
								}
								mana.reduceMana((ServerPlayer) player, cost);
							}
						}
					});
					if (!hasMana.get()) {
						data.active = false;
						stopPlayerUse(level, player, usingItem, false);
						continue;
					}
				}
			}

			// Обработка зачарований
			if (EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.ALTERATION.get(), usingItem) > 0) {
				processGazeControl(level, player, data, usingItem);
			}
			if (EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.RESTORATION.get(), usingItem) > 0) {
				// Лечение от ближайшего кристалла энда и урон при его разрушении
				int healInterval = 10; // тиков между лечением (как у дракона)
				double searchRadius = 32.0D; // радиус поиска кристаллов

				// Поиск ближайшего кристалла, если текущий отсутствует
				if (data.nearestCrystalId == null) {
					List<EndCrystal> crystals = level.getEntitiesOfClass(EndCrystal.class, player.getBoundingBox().inflate(searchRadius));
					EndCrystal nearest = null;
					double nearestDistSq = Double.MAX_VALUE;
					for (EndCrystal c : crystals) {
						double distSq = c.distanceToSqr(player);
						if (distSq < nearestDistSq) {
							nearestDistSq = distSq;
							nearest = c;
						}
					}
					if (nearest != null) {
						data.nearestCrystalId = nearest.getUUID();
						data.wasCrystalAliveLastTick = true; // кристалл найден и жив
					}
				}

				// Если есть связанный кристалл
				if (data.nearestCrystalId != null) {
					Entity crystalEntity = level.getEntity(data.nearestCrystalId);
					if (crystalEntity instanceof EndCrystal crystal) {
						boolean alive = crystal.isAlive();

						if (!alive && data.wasCrystalAliveLastTick) {
							float damageAmount = 10.0F;
							player.hurt(level.damageSources().explosion(crystal, null), damageAmount);
						}

						if (alive) {
							data.wasCrystalAliveLastTick = true;

							// Лечение с интервалом
							long gameTime = level.getGameTime();
							if (gameTime - data.lastHealGameTime >= healInterval && player.getHealth() < player.getMaxHealth()) {
								player.heal(1.0F); // восстанавливает 1 половинку сердца
								if (player instanceof ServerPlayer serverPlayer) {
									ModAdvancements.SPELL_RESTORATION_ENDER_EYE_TRIGGER.trigger(serverPlayer);
								}
								data.lastHealGameTime = gameTime;
							}
						} else {
							// Кристалл мёртв или исчез — сбрасываем данные
							data.nearestCrystalId = null;
							data.wasCrystalAliveLastTick = false;
						}
					} else {
						// Сущность не является кристаллом или полностью удалена
						if (data.wasCrystalAliveLastTick) {
							// Если в прошлом тике кристалл был жив, считаем что он разрушен
							player.hurt(level.damageSources().explosion(null, null), 10.0F);
						}
						data.nearestCrystalId = null;
						data.wasCrystalAliveLastTick = false;
					}
				}
			}
			if (EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.DESTRUCTION.get(), usingItem) > 0) {
				processDestructionBreath(level, player, data, usingItem);
			}
		}
	}

	private static void processGazeControl(ServerLevel level, Player player, PlayerUseData data, ItemStack stack) {
		double radius = 40D;
		AABB aabb = player.getBoundingBox().inflate(radius);
		Vec3 eyePos = player.getEyePosition(1.0F);

		Vec3 targetPos = player.getEyePosition();

		// Обработка мобов (существующий код)
		List<Mob> mobs = level.getEntitiesOfClass(Mob.class, aabb,
				mob -> mob.isAlive() && mob.distanceToSqr(player) <= radius * radius);
		for (Mob mob : mobs) {
			Vec3 mobEyePos = mob.getEyePosition(1.0F);
			ClipContext context = new ClipContext(mobEyePos, eyePos,
					ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, mob);
			BlockHitResult result = level.clip(context);
			boolean visible = result.getType() == HitResult.Type.MISS ||
					result.getLocation().distanceToSqr(eyePos) < 0.01;
			if (!visible) continue;

			mob.removeEffect(ModEffects.PARALYSIS.get());
			mob.getLookControl().setLookAt(player);
			mob.addEffect(new MobEffectInstance(ModEffects.PARALYSIS.get(), 5, 21, false, false));
			if (player instanceof ServerPlayer serverPlayer) {
				ModAdvancements.SPELL_ALTERATION_ENDER_EYE_TRIGGER.trigger(serverPlayer);
			}

			Vec3 toPlayer = eyePos.subtract(mobEyePos).normalize();
			float yaw = (float) Mth.atan2(toPlayer.z, toPlayer.x) * (180F / (float) Math.PI) - 90.0F;
			float pitch = (float) Mth.atan2(toPlayer.y, Math.sqrt(toPlayer.x * toPlayer.x + toPlayer.z * toPlayer.z))
					* (180F / (float) Math.PI);
			mob.yRot = yaw;
			mob.xRot = -pitch;
			mob.yHeadRot = yaw;
			mob.yBodyRot = yaw;
		}

		// Обработка игроков (новая функциональность)
		List<Player> nearbyPlayers = level.getEntitiesOfClass(Player.class, aabb,
				p -> p != player && p.isAlive() && !p.isSpectator() && !p.isCreative() &&
						p.distanceToSqr(player) <= radius * radius);

		for (Player target : nearbyPlayers) {
			// Проверка прямой видимости от глаз владельца до глаз цели
			if (!canSeePlayer(target, player)) continue;
			if (!isInPlayerViewCone(target, player)) {
				continue;
			}
			Vec3 targetEyePos = target.getEyePosition(1.0F);
			ClipContext visibilityContext = new ClipContext(eyePos, targetEyePos,
					ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player);
			BlockHitResult visibilityHit = level.clip(visibilityContext);
			boolean visible = visibilityHit.getType() == HitResult.Type.MISS ||
					visibilityHit.getLocation().distanceToSqr(targetEyePos) < 0.01;
			if (!visible) continue;

			// Проверяем, не находится ли цель уже под действием другой иллюзии
			AtomicBoolean alreadyHasIllusion = new AtomicBoolean(false);
			target.getCapability(PlayerIllusionForceProvider.PLAYER_ILLUSION_FORCE).ifPresent(illusionForce -> {
				if (illusionForce.getIllusionForce() > 0) {
					float deltaX = Math.abs(illusionForce.getEntPosX() - (float) targetPos.x);
					float deltaY = Math.abs(illusionForce.getEntPosY() - (float) targetPos.y);
					float deltaZ = Math.abs(illusionForce.getEntPosZ() - (float) targetPos.z);

					if (deltaX > 2.5f || deltaY > 2.5f || deltaZ > 2.5f) {
						alreadyHasIllusion.set(true);
					}
				}
			});
			if (alreadyHasIllusion.get()) continue;


			if (net.minecraftforge.common.ForgeHooks.shouldSuppressEnderManAnger(null, target, target.getInventory().armor.get(3))) {
				continue;
			}

			target.getCapability(PlayerIllusionForceProvider.PLAYER_ILLUSION_FORCE).ifPresent(illusionForce -> {
				if (target instanceof ServerPlayer serverTarget) {
					illusionForce.setIllusionForce(serverTarget, 3, true, false);
					illusionForce.setEntPosition(serverTarget, true,
							(float) targetPos.x,
							(float) targetPos.y,
							(float) targetPos.z, false);
					if (player instanceof ServerPlayer serverPlayer) {
						ModAdvancements.SPELL_ALTERATION_ENDER_EYE_TRIGGER.trigger(serverPlayer);
					}
				}
			});
		}
	}

	private static boolean isInPlayerViewCone(Player player, Player owner) {
		if (player.isCreative() || player.isSpectator()) return false;

		// Позиция энта
		Vec3 ownerPos = owner.getEyePosition();

		// Позиция глаз игрока
		Vec3 playerEyePos = player.getEyePosition();

		// Направление от игрока к энту
		Vec3 toEnt = ownerPos.subtract(playerEyePos).normalize();

		// Направление взгляда игрока
		Vec3 lookVec = player.getViewVector(1.0F).normalize();

		// Вычисляем угол между направлением взгляда игрока и направлением к энту
		double dot = lookVec.dot(toEnt);
		double angle = Math.acos(dot) * (180.0 / Math.PI);

		// Конус видимости игрока (например, 60 градусов)
		float playerViewCone = 120.0f;

		// Проверяем, находится ли энт в конусе зрения игрока
		boolean inViewCone = angle <= playerViewCone / 2;

		return inViewCone;
	}

	private static boolean canSeePlayer(Player player, Player owner) {
		if (player.isCreative() || player.isSpectator()) return false;
		Vec3 eyePos = owner.getEyePosition();

		Vec3 playerEyePos = player.getEyePosition();

		double distance = eyePos.distanceTo(playerEyePos);
		if (distance > 40D) {
			return false;
		}

		net.minecraft.world.level.ClipContext context = new net.minecraft.world.level.ClipContext(eyePos, playerEyePos,
				net.minecraft.world.level.ClipContext.Block.COLLIDER, net.minecraft.world.level.ClipContext.Fluid.NONE, null);

		net.minecraft.world.phys.BlockHitResult hitResult = player.level().clip(context);

		// Если луч попал в блок до игрока, значит игрок не виден
		if (hitResult.getType() != net.minecraft.world.phys.HitResult.Type.MISS) {
			double distanceToHit = hitResult.getLocation().distanceTo(eyePos);
			double distanceToPlayer = playerEyePos.distanceTo(eyePos);

			if (distanceToHit < distanceToPlayer - 0.1) {
				return false;
			}
		}

		return true;
	}

	private static boolean hasAnyEnchantForContinuation(ItemStack stack) {
		return EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.DESTRUCTION.get(), stack) > 0 ||
				EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.ALTERATION.get(), stack) > 0 ||
				EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.RESTORATION.get(), stack) > 0 ||
				EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.ILLUSION.get(), stack) > 0;
	}

// ==================== НОВАЯ ЛОГИКА ДЫХАНИЯ ДРАКОНА (DESTRUCTION) ====================

	public static class PlayerUseData {
		private final UUID playerId;
		private long startTime;
		private int useTicks;
		private int lastManaTick;
		public final List<UUID> ownedClouds = new ArrayList<>();
		public BlockPos lastBreathTargetPos;
		public int breathProgress;
		public boolean active;
		public UUID nearestCrystalId;
		public long lastHealGameTime;
		public boolean wasCrystalAliveLastTick;

		public PlayerUseData(UUID playerId, long startTime) {
			this.playerId = playerId;
			this.startTime = startTime;
			this.useTicks = 0;
			this.lastManaTick = 0;
			this.breathProgress = 0;
			this.lastBreathTargetPos = null;
			this.active = true;
			this.nearestCrystalId = null;
			this.lastHealGameTime = 0;
			this.wasCrystalAliveLastTick = false;
		}

		public void tick() {
			useTicks++;
		}

		public boolean shouldConsumeMana(ItemStack item, Level level) {
			int tick = 4;
			if (item.getEnchantmentLevel(ModEnchantments.ALTERATION.get()) > 0) tick = 4;
			if (item.getEnchantmentLevel(ModEnchantments.RESTORATION.get()) > 0) tick = 4;
			if (item.getEnchantmentLevel(ModEnchantments.DESTRUCTION.get()) > 0) tick = 4;
			if (item.getEnchantmentLevel(ModEnchantments.ILLUSION.get()) > 0 || item.getEnchantmentLevel(ModEnchantments.NECROMANCY.get()) > 0)
				return false;
			if (useTicks - lastManaTick >= tick) {
				lastManaTick = useTicks;
				return true;
			}
			return false;
		}
	}

	private static final float RADIUS_INCREMENT_PER_TICK = 0.05F;

	/**
	 * Обрабатывает дыхание дракона для зачарования DESTRUCTION.
	 */
	private static void processDestructionBreath(ServerLevel level, Player player, PlayerUseData useData, ItemStack stack) {
		Vec3 eyePos = player.getEyePosition(1.0F);
		Vec3 lookVec = player.getLookAngle().normalize();
		Vec3 maxEnd = eyePos.add(lookVec.scale(MAX_BREATH_DISTANCE));
		if (player instanceof ServerPlayer serverPlayer) {
			ModAdvancements.SPELL_DESTRUCTION_ENDER_EYE_TRIGGER.trigger(serverPlayer);
		}

		// Поиск ближайшего пересечения с облаками игрока
		double closestCloudDist = Double.MAX_VALUE;
		AreaEffectCloud closestCloud = null;
		for (UUID cloudId : useData.ownedClouds) {
			Entity entity = level.getEntity(cloudId);
			if (entity instanceof AreaEffectCloud cloud && cloud.isAlive()) {
				Optional<Vec3> hit = cloud.getBoundingBox().clip(eyePos, maxEnd);
				if (hit.isPresent()) {
					double dist = eyePos.distanceToSqr(hit.get());
					if (dist < closestCloudDist) {
						closestCloudDist = dist;
						closestCloud = cloud;
					}
				}
			}
		}

		// Трассировка блоков
		BlockHitResult blockHit = level.clip(new ClipContext(eyePos, maxEnd, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
		double blockDist = blockHit.getType() == HitResult.Type.BLOCK ? eyePos.distanceToSqr(blockHit.getLocation()) : Double.MAX_VALUE;

		Vec3 targetPos;
		BlockPos targetBlockPos;
		AreaEffectCloud targetCloud = null;

		// Выбираем ближайшее препятствие
		if (blockDist < closestCloudDist) {
			// Блок ближе
			targetPos = blockHit.getLocation();
			targetBlockPos = blockHit.getBlockPos();
			// Ищем облако рядом с точкой попадания (как раньше)
			for (UUID cloudId : useData.ownedClouds) {
				Entity entity = level.getEntity(cloudId);
				if (entity instanceof AreaEffectCloud cloud && cloud.isAlive()) {
					if (cloud.position().distanceToSqr(targetPos) <= 1.0) {
						targetCloud = cloud;
						break;
					}
				}
			}
		} else if (closestCloud != null) {
			targetPos = eyePos.add(lookVec.scale(Math.sqrt(closestCloudDist)));
			targetBlockPos = new BlockPos((int) targetPos.x(), (int) targetPos.y(), (int) targetPos.z());
			targetCloud = closestCloud;
		} else {
			// Ничего не найдено — конечная точка луча
			targetPos = maxEnd;
			targetBlockPos = new BlockPos((int) targetPos.x(), (int) targetPos.y(), (int) targetPos.z());
		}

		if (targetCloud == null) {
			if (useData.ownedClouds.size() < 64) {
				AreaEffectCloud cloud = new AreaEffectCloud(level, targetPos.x, targetPos.y, targetPos.z);
				cloud.setOwner(player);
				cloud.setRadius(CLOUD_MIN_RADIUS);
				cloud.setDuration(CLOUD_DURATION);
				cloud.setParticle(ParticleTypes.DRAGON_BREATH);
				cloud.addEffect(new MobEffectInstance(MobEffects.HARM, 1, 0));
				level.addFreshEntity(cloud);
				useData.ownedClouds.add(cloud.getUUID());
				useData.lastBreathTargetPos = targetBlockPos;
				useData.breathProgress = 0;

				level.sendParticles(ParticleTypes.DRAGON_BREATH, targetPos.x, targetPos.y, targetPos.z, 10, 0.5, 0.5, 0.5, 0.1);
			}
		} else {
			// Облако существует — проверяем, сменилась ли цель
			boolean sameTarget = useData.lastBreathTargetPos != null && useData.lastBreathTargetPos.equals(targetBlockPos);
			if (!sameTarget) {
				useData.lastBreathTargetPos = targetBlockPos;
				useData.breathProgress = 0;
			}

			useData.breathProgress++;

			// Плавное увеличение радиуса
			float currentRadius = targetCloud.getRadius();
			if (currentRadius < CLOUD_MAX_RADIUS) {
				float newRadius = Math.min(currentRadius + RADIUS_INCREMENT_PER_TICK, CLOUD_MAX_RADIUS);
				targetCloud.setRadius(newRadius);
			}

			// Продлеваем время жизни облака
			targetCloud.setDuration(targetCloud.tickCount + CLOUD_DURATION);

			// Визуальные частицы дыхания
			//level.sendParticles(ParticleTypes.DRAGON_BREATH,targetPos.x, targetPos.y, targetPos.z,5, 0.3, 0.3, 0.3, 0.05);
		}
	}

	// Методы для управления подсказками (без изменений)
	public String getFirstPredicate(ItemStack item) {
		return Minecraft.getInstance().options.keyShift.getKey().getDisplayName().getString();
	}

	public String getSecondPredicate(ItemStack item) {
		return Minecraft.getInstance().options.keyAttack.getKey().getDisplayName().getString();
	}

	public boolean hasControls(ItemStack item) {
		return item.getEnchantmentLevel(ModEnchantments.ILLUSION.get()) > 0;
	}
}