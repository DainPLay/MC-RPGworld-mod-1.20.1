package net.dainplay.rpgworldmod.item.custom;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.dainplay.rpgworldmod.damage.ModDamageTypes;
import net.dainplay.rpgworldmod.data.tags.ModAdvancements;
import net.dainplay.rpgworldmod.effect.ModEffects;
import net.dainplay.rpgworldmod.enchantment.ModEnchantments;
import net.dainplay.rpgworldmod.entity.ModEntities;
import net.dainplay.rpgworldmod.entity.custom.ConjuredDolphin;
import net.dainplay.rpgworldmod.item.ModItems;
import net.dainplay.rpgworldmod.network.ClientGuardianAttackData;
import net.dainplay.rpgworldmod.network.ClientManaData;
import net.dainplay.rpgworldmod.network.LoopSoundPacket;
import net.dainplay.rpgworldmod.network.ModMessages;
import net.dainplay.rpgworldmod.network.PlayerManaProvider;
import net.dainplay.rpgworldmod.network.S2CGuardianAttackData;
import net.dainplay.rpgworldmod.network.TargetHelper;
import net.dainplay.rpgworldmod.network.UpdateItemTagMessage;
import net.dainplay.rpgworldmod.sounds.RPGSounds;
import net.dainplay.rpgworldmod.util.RainyChunkManager;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundGameEventPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BubbleColumnBlock;
import net.minecraft.world.level.block.LiquidBlockContainer;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.WaterFluid;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class HeartOfTheSeaScrollItem extends ScrollItem {

	// Хранилище для отслеживания использования игроком с привязкой к уровню
	private static final Map<Level, Map<UUID, PlayerUseData>> playerUseData = new HashMap<>();

	public HeartOfTheSeaScrollItem(Properties pProperties) {
		super(pProperties);
	}

	@Override
	public String getTexture(ItemStack stack, Entity entity) {
		if (entity instanceof Player player && player.isUsingItem() && player.getUseItem() == stack) {
			return "textures/entity/spells/sea";
		}
		return "textures/entity/spells/bubble";
	}

	@Override
	public float get1YOffset(ItemStack stack, Entity entity) {
		if (entity instanceof Player player && player.isUsingItem() && player.getUseItem() == stack) {
			return 0.0F;
		}
		return 0.25F;
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
			if (stack.getEnchantmentLevel(ModEnchantments.ALTERATION.get()) > 0) {
				return 0.2F;
			}
			if (stack.getEnchantmentLevel(ModEnchantments.ILLUSION.get()) > 0) {
				return 0.1F;
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
		ClientGuardianAttackData.AttackData attackData = ClientGuardianAttackData.getForPlayer(player.getId());

		if ((stack.getEnchantmentLevel(ModEnchantments.DESTRUCTION.get()) > 0 && (attackData == null || attackData.target == null))
				|| stack.getEnchantmentLevel(ModEnchantments.ILLUSION.get()) > 0) {
			return (player.isUsingItem() && player.getUseItemRemainingTicks() > 0 && player.getUseItem() == stack);
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
		if (stack.getEnchantmentLevel(ModEnchantments.ILLUSION.get()) > 0) {
			poseStack.translate(flip * 0.05F, 0.05F, -0.31F);
		}

		return poseStack;
	}

	@Override
	public int getAnimationSpeed(ItemStack stack, Entity entity) {
		if (entity instanceof Player player && player.isUsingItem() && player.getUseItem() == stack) {
			return 2;
		}
		return 1;
	}

	@Override
	public int getAnimationLength(ItemStack stack, Entity entity) {
		if (entity instanceof Player player && player.isUsingItem() && player.getUseItem() == stack) {
			return 24;
		}
		return 18;
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
			return "1";
		}
		if (EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.ALTERATION.get(), item) > 0) {
			return "4";
		}
		if (EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.ILLUSION.get(), item) > 0) {
			return "25";
		}
		if (EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.RESTORATION.get(), item) > 0) {
			return "15";
		}
		if (EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.CONJURATION.get(), item) > 0) {
			return "30";
		}
		if (EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.NECROMANCY.get(), item) > 0) {
			return "3";
		}
		return "5";
	}

	@Override
	public int getManaCost(ItemStack item, Player player) {
		if (EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.DESTRUCTION.get(), item) > 0) {
			return 10;
		}
		if (EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.RESTORATION.get(), item) > 0) {
			return 15;
		}
		if (EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.ALTERATION.get(), item) > 0) {
			return 1;
		}
		if (EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.ILLUSION.get(), item) > 0) {
			return 25;
		}
		if (EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.CONJURATION.get(), item) > 0) {
			return 30;
		}
		if (EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.NECROMANCY.get(), item) > 0) {
			return 3;
		}
		return 5;
	}

	public Component getManaCostAdditionalLine(ItemStack item) {
		if (EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.DESTRUCTION.get(), item) > 0) {
			return Component.translatable("tooltip.rpgworldmod.cost_per_second_plus", "10").withStyle(ChatFormatting.BLUE);
		}
		if (EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.ALTERATION.get(), item) > 0) {
			return Component.translatable("tooltip.rpgworldmod.cost_per_second").withStyle(ChatFormatting.BLUE);
		}
		return Component.literal("");
	}

	public static ItemStack createForEnchantment(EnchantmentInstance pInstance) {
		ItemStack itemstack = new ItemStack(ModItems.HEART_OF_THE_SEA_SCROLL.get());
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
			// Специальные случаи, не требующие удержания
			if (EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.CONJURATION.get(), itemstack) > 0) {
				return InteractionResultHolder.pass(itemstack);
			}
			if (EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.RESTORATION.get(), itemstack) > 0) {
				return handleRestoration(level, player, itemstack);
			}
			if (EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.NECROMANCY.get(), itemstack) > 0) {
				return handleNecromancy(level, player, itemstack);
			}


			if (EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.DESTRUCTION.get(), itemstack) > 0) {
				ModMessages.sendToPlayer(
						new S2CGuardianAttackData(player.getId(), 0, 0, false, false),
						(ServerPlayer) player
				);
			}

			// Общая проверка маны
			if (!canUse(player, itemstack)) {
				getPlayerUseData(level).remove(player.getUUID());
				ModMessages.sendToNearbyPlayers(new LoopSoundPacket(player.getId(), false, itemstack),
						(ServerLevel) level, player.blockPosition(), 64.0);
				return InteractionResultHolder.fail(itemstack);
			}

			// Создаём данные использования
			getPlayerUseData(level).put(player.getUUID(), new PlayerUseData(player.getUUID(), level.getGameTime()));

			// Запускаем звуки начала и цикл
			startEnchantmentSounds(level, player, itemstack);

			player.startUsingItem(hand);
		} else {
			// Клиент
			if (EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.CONJURATION.get(), itemstack) > 0) {
				return InteractionResultHolder.pass(itemstack);
			}

			if (!canUseClient(player, itemstack)) {
				return InteractionResultHolder.fail(itemstack);
			}

			if (EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.RESTORATION.get(), itemstack) > 0) {
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

	private InteractionResultHolder<ItemStack> handleRestoration(Level level, Player player, ItemStack stack) {
		if (!player.getAbilities().instabuild) {
			AtomicBoolean fail = new AtomicBoolean(false);
			player.getCapability(PlayerManaProvider.PLAYER_MANA).ifPresent(mana -> {
				if (mana.getMana() < getManaCost(stack, player)) {
					fail.set(true);
				} else {
					mana.reduceMana((ServerPlayer) player, getManaCost(stack, player));
				}
			});
			if (fail.get()) return InteractionResultHolder.pass(stack);
		}
		player.getCooldowns().addCooldown(this, 15);
		level.playSound(null, player.blockPosition(),
				RPGSounds.SPELL_RESTORATION_CAST.get(),
				SoundSource.PLAYERS, 1.0F, (level.random.nextFloat() - level.random.nextFloat()) * 0.2F + 1.0F
		);
		player.addEffect(new MobEffectInstance(MobEffects.CONDUIT_POWER, 1200, 0));
		if (player instanceof ServerPlayer serverPlayer) {
			ModAdvancements.SPELL_RESTORATION_HEART_OF_THE_SEA_TRIGGER.trigger(serverPlayer);
		}
		return InteractionResultHolder.success(stack);
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
				MobEffectInstance necrosis = new MobEffectInstance(ModEffects.NECROSIS.get(), 1200, amp-1);
				necrosis.setCurativeItems(new ArrayList<>());
				player.addEffect(necrosis);
			}
		}
		player.getCooldowns().addCooldown(this, 15);
		level.playSound(null, player.blockPosition(),
				RPGSounds.SPELL_NECROMANCY_CAST.get(),
				SoundSource.PLAYERS, 1.0F, (level.random.nextFloat() - level.random.nextFloat()) * 0.2F + 1.0F
		);
		if (player instanceof ServerPlayer serverPlayer) {
			ModAdvancements.SPELL_NECROMANCY_HEART_OF_THE_SEA_TRIGGER.trigger(serverPlayer);
			if(serverPlayer.level().dimension() == Level.END)
				ModAdvancements.USE_RAIN_SPELL_IN_END_TRIGGER.trigger(serverPlayer);
		}
		if (!level.isClientSide) {
			ServerLevel serverLevel = (ServerLevel) level;
			int centerChunkX = player.chunkPosition().x;
			int centerChunkZ = player.chunkPosition().z;
			long expiryTime = serverLevel.getGameTime() + 300; // 15 секунд = 300 тиков

			RainyChunkManager manager = RainyChunkManager.get(serverLevel);
			for (int dx = -2; dx <= 2; dx++) {
				for (int dz = -2; dz <= 2; dz++) {
					manager.addRainyChunkAndSync(level.dimension(), centerChunkX + dx, centerChunkZ + dz, expiryTime);
				}
			}
		}
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
					if (EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.ILLUSION.get(), stack) <= 0
							&& EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.DESTRUCTION.get(), stack) <= 0) {
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
					RPGSounds.SPELL_DESTRUCTION_HEART_OF_THE_SEA_START.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
			ModMessages.sendToNearbyPlayers(new LoopSoundPacket(player.getId(), true, stack),
					(ServerLevel) level, player.blockPosition(), 64.0);
		}
		if (EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.ALTERATION.get(), stack) > 0) {
			level.playSound(null, player.getX(), player.getY(), player.getZ(),
					RPGSounds.SPELL_ALTERATION_START.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
			ModMessages.sendToNearbyPlayers(new LoopSoundPacket(player.getId(), true, stack),
					(ServerLevel) level, player.blockPosition(), 64.0);
		}
		if (EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.ILLUSION.get(), stack) > 0) {
			level.playSound(null, player.getX(), player.getY(), player.getZ(),
					RPGSounds.SPELL_ILLUSION_START.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
			ModMessages.sendToNearbyPlayers(new LoopSoundPacket(player.getId(), true, stack),
					(ServerLevel) level, player.blockPosition(), 64.0);
		}
	}

	@Override
	public InteractionResult useOn(UseOnContext context) {
		Level level = context.getLevel();
		Player player = context.getPlayer();
		ItemStack itemstack = context.getItemInHand();

		if (EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.CONJURATION.get(), itemstack) > 0) {
			// Логика призыва дельфина (без изменений)
			return handleConjuration(context, level, player, itemstack);
		}
		return InteractionResult.PASS;
	}

	private InteractionResult handleConjuration(UseOnContext context, Level level, Player player, ItemStack stack) {
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

		BlockPos clickedPos = context.getClickedPos();
		Direction face = context.getClickedFace();
		BlockState clickedState = level.getBlockState(clickedPos);
		BlockPos spawnPos = clickedState.getCollisionShape(level, clickedPos).isEmpty() ? clickedPos : clickedPos.relative(face);

		ServerLevel serverLevel = (ServerLevel) level;
		Entity dolphin = new ConjuredDolphin(ModEntities.CONJURED_DOLPHIN.get(), serverLevel);
		dolphin.setPos(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5);
		if (dolphin instanceof ConjuredDolphin conjured) {
			conjured.setOwnerUUID(player.getUUID());
		}
		serverLevel.addFreshEntity(dolphin);
		player.getCooldowns().addCooldown(this, 15);
		level.playSound(null, context.getClickedPos(), RPGSounds.SPELL_CONJURATION_START.get(),
				SoundSource.NEUTRAL, 1.0F, 1.0F);
		if (player instanceof ServerPlayer serverPlayer) {
			ModAdvancements.SPELL_CONJURATION_HEART_OF_THE_SEA_TRIGGER.trigger(serverPlayer);
		}
		return InteractionResult.CONSUME;
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
			UUID playerId = player.getUUID();
			int playerIdInt = player.getId();

			if (!level.isClientSide) {
				PlayerUseData useData = getPlayerUseData(level).remove(playerId);
				if (useData != null && useData.currentTargetUUID != null) {
					// Если была активна цель, отправляем клиенту сигнал остановки
					ModMessages.sendToNearbyPlayers(
							new S2CGuardianAttackData(playerIdInt, 0, 0, false, false),
							(ServerLevel) level, player.blockPosition(), 64.0
					);
				}

				// Останавливаем звуки
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
					RPGSounds.SPELL_DESTRUCTION_HEART_OF_THE_SEA_STOP.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
		}
		if (EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.ALTERATION.get(), stack) > 0) {
			level.playSound(null, player.getX(), player.getY(), player.getZ(),
					RPGSounds.SPELL_ALTERATION_STOP.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
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
		if (stack.getEnchantmentLevel(ModEnchantments.RESTORATION.get()) > 0) return 0;
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

		// Останавливаем звуки
		if (usingItem.getItem() instanceof HeartOfTheSeaScrollItem scroll) {
			scroll.stopEnchantmentSounds(level, player, usingItem);
		}

		// Отправляем сигнал остановки луча
		ModMessages.sendToNearbyPlayers(
				new S2CGuardianAttackData(player.getId(), 0, 0, false, damageDealt),
				level, player.blockPosition(), 64.0
		);

		// Ставим кулдаун
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
			if (!(usingItem.getItem() instanceof HeartOfTheSeaScrollItem)) {
				levelPlayerUseData.remove(playerId);
				player.stopUsingItem();
				continue;
			}

			// Проверка наличия зачарований
			if (!hasAnyEnchantForContinuation(usingItem)) {
				levelPlayerUseData.remove(playerId);
				stopPlayerUse(level, player, usingItem, false);
				continue;
			}

			useData.tick();

			// Потребление маны
			if (useData.shouldConsumeMana(usingItem, useData.currentTargetUUID)) {
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
						levelPlayerUseData.remove(playerId);
						stopPlayerUse(level, player, usingItem, false);
						continue;
					}
				}
			}

			// Обработка Alteration
			if (!player.isShiftKeyDown() && EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.ALTERATION.get(), usingItem) > 0) {
				processWaterAlteration(level, player);
			}
			if (player.isShiftKeyDown() && EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.ALTERATION.get(), usingItem) > 0) {
				removeNearestWater(level, player);
			}

			// Обработка Destruction
			if (EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.DESTRUCTION.get(), usingItem) > 0) {
				processDestructionAttack(level, player, useData, usingItem);
			}
		}
	}

	private static boolean hasAnyEnchantForContinuation(ItemStack stack) {
		return EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.DESTRUCTION.get(), stack) > 0 ||
				EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.ALTERATION.get(), stack) > 0 ||
				EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.ILLUSION.get(), stack) > 0;
	}

	// ==================== НОВАЯ ЛОГИКА АТАКИ DESTRUCTION ====================

	private static final double ATTACK_RANGE = 64.0D;
	private static final double CONE_ANGLE_DEGREES = 15.0D;
	private static final int ATTACK_DURATION = 80;          // тиков до выстрела (как у стража)
	private static final float ATTACK_DAMAGE = 10.0F;

	public static class PlayerUseData {
		private final UUID playerId;
		private long startTime;
		private int useTicks;
		private int lastManaTick;
		// Данные для Destruction
		public UUID currentTargetUUID = null;
		public int attackTime = 0;

		public PlayerUseData(UUID playerId, long startTime) {
			this.playerId = playerId;
			this.startTime = startTime;
			this.useTicks = 0;
			this.lastManaTick = 0;
		}

		public void tick() {
			useTicks++;
		}

		public boolean shouldConsumeMana(ItemStack item, UUID currentTargetUUID) {
			int tick = 4;
			if (item.getEnchantmentLevel(ModEnchantments.ALTERATION.get()) > 0) tick = 5;
			if (item.getEnchantmentLevel(ModEnchantments.DESTRUCTION.get()) > 0) {
				if (currentTargetUUID == null) return false;
				tick = 20;
			}
			if (item.getEnchantmentLevel(ModEnchantments.ILLUSION.get()) > 0 || item.getEnchantmentLevel(ModEnchantments.NECROMANCY.get()) > 0)
				return false;
			if (useTicks - lastManaTick >= tick) {
				lastManaTick = useTicks;
				return true;
			}
			return false;
		}

		public void resetAttack() {
			currentTargetUUID = null;
			attackTime = 0;
		}
	}

	/**
	 * Проверяет, находится ли фиксированная цель в конусе атаки, на дистанции и с прямой видимостью.
	 */
	private static boolean isTargetValid(ServerLevel level, Player player, LivingEntity target) {
		if (target == null || !target.isAlive()) return false;
		double distSq = player.distanceToSqr(target);
		if (distSq > ATTACK_RANGE * ATTACK_RANGE) return false;

		// Проверка угла с использованием bounding box цели (как в клиентском миксине)
		double minAngle = TargetHelper.getMinAngleToBoundingBox(player, target, ATTACK_RANGE);
		if (minAngle > CONE_ANGLE_DEGREES) return false;

		// Проверка прямой видимости
		return TargetHelper.hasLineOfSightToBoundingBox(player, target, player.getEyePosition(1.0F), ATTACK_RANGE);
	}

	/**
	 * Обрабатывает атаку Destruction: поиск цели, накопление времени, нанесение урона.
	 */
	private static void processDestructionAttack(ServerLevel level, Player player, PlayerUseData useData, ItemStack stack) {
		if (useData.currentTargetUUID == null) return;

		LivingEntity target = (LivingEntity) level.getEntity(useData.currentTargetUUID);
		if (!isTargetValid(level, player, target)) {
			useData.resetAttack();
			stopPlayerUse(level, player, stack, false);
			return;
		}

		useData.attackTime++;

		// Отправляем клиенту данные для рендера луча
		ModMessages.sendToNearbyPlayers(
				new S2CGuardianAttackData(player.getId(), target.getId(), useData.attackTime, true,false),
				level, player.blockPosition(), 64.0
		);

		if (useData.attackTime >= ATTACK_DURATION) {
			AtomicBoolean hasMana = new AtomicBoolean(true);
			if (!player.getAbilities().instabuild) {
				player.getCapability(PlayerManaProvider.PLAYER_MANA).ifPresent(mana -> {
					if (stack.getItem() instanceof ManaCostItem manaCostItem) {
						if (mana.getMana() < manaCostItem.getManaCost(stack, player)) {
							hasMana.set(false);
						} else {
							mana.reduceMana((ServerPlayer) player, manaCostItem.getManaCost(stack, player));
						}
					}
				});
			}
			if (hasMana.get()) {
				target.hurt(player.damageSources().indirectMagic(player, player), ATTACK_DAMAGE);
				if (player instanceof ServerPlayer serverPlayer) {
					ModAdvancements.SPELL_DESTRUCTION_HEART_OF_THE_SEA_TRIGGER.trigger(serverPlayer);
				}
				player.swing(player.getUsedItemHand());
				level.playSound(null, player.blockPosition(),
						RPGSounds.SPELL_DESTRUCTION_HEART_OF_THE_SEA_CAST.get(),
						SoundSource.PLAYERS, 1.0F, (level.random.nextFloat() - level.random.nextFloat()) * 0.2F + 1.0F
				);
				level.playSound(null, target.blockPosition(),
						RPGSounds.SPELL_DESTRUCTION_HEART_OF_THE_SEA_CAST.get(),
						SoundSource.PLAYERS, 1.0F, (level.random.nextFloat() - level.random.nextFloat()) * 0.2F + 1.0F
				);
				stopPlayerUse(level, player, stack, true);
			}
			else
				stopPlayerUse(level, player, stack, false);
		}
	}

	// ==================== ОСТАЛЬНЫЕ МЕТОДЫ (Alteration, Illusion и т.д.) ====================

	private static void removeNearestWater(ServerLevel level, Player player) {
		BlockPos playerPos = player.blockPosition();
		int radius = 8;
		int radiusSquared = radius * radius;

		List<BlockPos> waterSources = new ArrayList<>();
		List<BlockPos> flowingWaters = new ArrayList<>();

		for (int x = -radius; x <= radius; x++) {
			for (int y = -radius; y <= radius; y++) {
				for (int z = -radius; z <= radius; z++) {
					BlockPos checkPos = playerPos.offset(x, y, z);
					if (playerPos.distSqr(checkPos) <= radiusSquared) {
						FluidState fluidState = level.getFluidState(checkPos);
						if (fluidState.is(FluidTags.WATER)) {
							if (fluidState.isSource()) {
								waterSources.add(checkPos);
							} else {
								flowingWaters.add(checkPos);
							}
						}
					}
				}
			}
		}

		waterSources.sort(Comparator.comparingDouble(pos -> playerPos.distSqr(pos)));
		flowingWaters.sort(Comparator.comparingDouble(pos -> playerPos.distSqr(pos)));

		int sourcesToRemove = Math.min(waterSources.size(), 4);
		for (int i = 0; i < sourcesToRemove; i++) {
			removeWaterAt(level, waterSources.get(i));
		}

		int flowingToRemove = Math.min(flowingWaters.size(), 4);
		for (int i = 0; i < flowingToRemove; i++) {
			removeWaterAt(level, flowingWaters.get(i));
		}

		if (sourcesToRemove > 0 || flowingToRemove > 0) {
			if (player instanceof ServerPlayer serverPlayer)
				ModAdvancements.SPELL_ALTERATION_HEART_OF_THE_SEA_TRIGGER.trigger(serverPlayer);
		}
	}

	private static void removeWaterAt(ServerLevel level, BlockPos pos) {
		BlockState state = level.getBlockState(pos);

		if (state.is(Blocks.WATER)) {
			// Это непосредственно блок воды – убираем его полностью
			level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
		} else {
			// Это waterlogged блок – пробуем снять флаг WATERLOGGED

			if (state.getBlock() instanceof SimpleWaterloggedBlock) {
				if(state.hasProperty(BlockStateProperties.WATERLOGGED))
					level.setBlockAndUpdate(pos, state.setValue(BlockStateProperties.WATERLOGGED, false));
			}
			else if (state.getBlock() instanceof LiquidBlockContainer) {
				level.destroyBlock(pos,true);
				level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
			}
			else if (state.getBlock() instanceof BubbleColumnBlock) {
				level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
			}
		}

		// Частицы всё равно отправляем для визуального эффекта
		level.sendParticles(ParticleTypes.BUBBLE_POP,
				pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
				5, 0.2, 0.2, 0.2, 0.05);
	}

	private static void processWaterAlteration(ServerLevel level, Player player) {
		BlockPos playerPos = player.blockPosition();
		int radius = 8;

		BlockPos nearestWaterPos = null;
		double nearestDistance = Double.MAX_VALUE;

		for (int x = -radius; x <= radius; x++) {
			for (int y = -radius; y <= radius; y++) {
				for (int z = -radius; z <= radius; z++) {
					BlockPos checkPos = playerPos.offset(x, y, z);
					double distance = playerPos.distSqr(checkPos);

					if (distance <= radius * radius) {
						BlockState state = level.getBlockState(checkPos);
						FluidState fluidState = level.getFluidState(checkPos);

						boolean isWaterSource = state.is(Blocks.WATER) && fluidState.isSource();
						boolean isFlowingWater = fluidState.is(FluidTags.WATER) && !fluidState.isSource();

						boolean proceedWaterSource = false;
						if (isWaterSource) {
							for (Direction direction : Direction.values()) {
								if (direction == Direction.UP) {
									BlockPos neighborPos = checkPos.relative(direction);

									if (canWaterSpreadTo(level, neighborPos, playerPos)) {
										proceedWaterSource = true;
									}
								}
							}
						}

						if ((proceedWaterSource || isFlowingWater) && distance < nearestDistance) {
							nearestWaterPos = checkPos;
							nearestDistance = distance;
						}
					}
				}
			}
		}

		if (nearestWaterPos != null) {
			BlockState state = level.getBlockState(nearestWaterPos);
			FluidState fluidState = level.getFluidState(nearestWaterPos);

			if (fluidState.is(FluidTags.WATER) && !fluidState.isSource()) {
				level.setBlockAndUpdate(nearestWaterPos, Fluids.WATER.getSource().defaultFluidState().createLegacyBlock());
				if (player instanceof ServerPlayer serverPlayer)
					ModAdvancements.SPELL_ALTERATION_HEART_OF_THE_SEA_TRIGGER.trigger(serverPlayer);
			} else if (state.is(Blocks.WATER) && fluidState.isSource()) {
				spreadWaterUp(level, nearestWaterPos, playerPos);
				if (player instanceof ServerPlayer serverPlayer)
					ModAdvancements.SPELL_ALTERATION_HEART_OF_THE_SEA_TRIGGER.trigger(serverPlayer);
			}
		}
	}

	private static void spreadWaterUp(ServerLevel level, BlockPos sourcePos, BlockPos playerPos) {
		Direction direction = Direction.UP;
		BlockPos neighborPos = sourcePos.relative(direction);
		if (canWaterSpreadTo(level, neighborPos, playerPos)) {
			if (level.getFluidState(sourcePos).getType() instanceof WaterFluid water)
				water.spreadTo(level, neighborPos, level.getBlockState(neighborPos), direction, water.getFlowing(6, false));
		}
	}

	private static boolean canWaterSpreadTo(ServerLevel level, BlockPos pos, BlockPos playerPos) {
		BlockState state = level.getBlockState(pos);
		if (level.getBlockState(pos).is(Blocks.WATER)) {
			return false;
		}
		if (playerPos.getY() <= pos.getY()) return false;
		return state.isAir();
	}

	public String getFirstPredicate(ItemStack item) {
		return Minecraft.getInstance().options.keyShift.getKey().getDisplayName().getString();
	}

	public String getSecondPredicate(ItemStack item) {
		return Minecraft.getInstance().options.keyAttack.getKey().getDisplayName().getString();
	}

	public boolean hasControls(ItemStack item) {
		return item.getEnchantmentLevel(ModEnchantments.ILLUSION.get()) > 0
				|| item.getEnchantmentLevel(ModEnchantments.DESTRUCTION.get()) > 0;
	}
}