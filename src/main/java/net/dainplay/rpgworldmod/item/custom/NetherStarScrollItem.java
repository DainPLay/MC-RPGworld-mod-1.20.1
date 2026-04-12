package net.dainplay.rpgworldmod.item.custom;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.dainplay.rpgworldmod.damage.ModDamageTypes;
import net.dainplay.rpgworldmod.data.tags.ModAdvancements;
import net.dainplay.rpgworldmod.effect.ModEffects;
import net.dainplay.rpgworldmod.enchantment.ModEnchantments;
import net.dainplay.rpgworldmod.item.ModItems;
import net.dainplay.rpgworldmod.item.ModTiers;
import net.dainplay.rpgworldmod.network.ClientManaData;
import net.dainplay.rpgworldmod.network.LoopSoundPacket;
import net.dainplay.rpgworldmod.network.ModMessages;
import net.dainplay.rpgworldmod.network.PlayerManaProvider;
import net.dainplay.rpgworldmod.network.PortalEffectPacket;
import net.dainplay.rpgworldmod.network.S2CBeamUpdatePacket;
import net.dainplay.rpgworldmod.network.UpdateItemTagMessage;
import net.dainplay.rpgworldmod.sounds.RPGSounds;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.DamageTypeTags;
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
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.portal.PortalInfo;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.common.util.ITeleporter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Function;

public class NetherStarScrollItem extends ScrollItem {

	private static final Map<Level, Map<UUID, PlayerUseData>> playerUseData = new HashMap<>();

	private static final double DESTRUCTION_BEAM_RANGE = 128.0;
	private static final double DESTRUCTION_BEAM_RADIUS = 1.2;
	private static final int DESTRUCTION_CHARGE_TICKS = 40;

	public NetherStarScrollItem(Properties pProperties) {
		super(pProperties);
	}

	@Override
	public String getTexture(ItemStack stack, Entity entity) {
		if (entity instanceof Player player && player.isUsingItem() && player.getUseItem() == stack) {
			return "textures/entity/spells/nether";
		}
		return "textures/entity/spells/wither_armor";
	}

	public boolean useCubeEffect(ItemStack stack, Entity entity) {
		if (entity instanceof Player player && player.isUsingItem() && player.getUseItem() == stack) {
			return false;
		}
		return true;
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
			return 0.3F;
		}
		return 0.15F;
	}

	@Override
	public float get1Size(ItemStack stack, Entity entity) {
		if (entity instanceof Player player && player.isUsingItem() && player.getUseItem() == stack) {
			if (stack.getEnchantmentLevel(ModEnchantments.DESTRUCTION.get()) > 0) {
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
			if (stack.getEnchantmentLevel(ModEnchantments.RESTORATION.get()) > 0) {
				return 0.2F;
			}
			if (stack.getEnchantmentLevel(ModEnchantments.CONJURATION.get()) > 0) {
				return 0.2F;
			}
			if (stack.getEnchantmentLevel(ModEnchantments.NECROMANCY.get()) > 0) {
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
		if (stack.getEnchantmentLevel(ModEnchantments.CONJURATION.get()) > 0) {
			poseStack.mulPose(Axis.ZP.rotationDegrees(flip * 36.0F));
			poseStack.mulPose(Axis.XP.rotationDegrees(-7.0F));
			poseStack.mulPose(Axis.YP.rotationDegrees((-(float) Math.PI / 6F)));
			poseStack.translate(0F, 0.4F, 0F);
			float shakeRotY = (float) (Math.cos(player.getTicksUsingItem() * 1.5) * 0.3F);
			poseStack.mulPose(Axis.YP.rotationDegrees(shakeRotY));
		}
		if (stack.getEnchantmentLevel(ModEnchantments.NECROMANCY.get()) > 0) {
			poseStack.mulPose(Axis.ZP.rotationDegrees(flip * 36.0F));
			poseStack.mulPose(Axis.XP.rotationDegrees(-7.0F));
			poseStack.mulPose(Axis.YP.rotationDegrees((-(float) Math.PI / 6F)));
			poseStack.translate(0F, 0.4F, 0F);
			float shakeRotY = (float) (Math.cos(player.getTicksUsingItem() * 1.5) * 0.3F);
			poseStack.mulPose(Axis.YP.rotationDegrees(shakeRotY));
		}
		if (stack.getEnchantmentLevel(ModEnchantments.ILLUSION.get()) > 0) {
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
		if (stack.getEnchantmentLevel(ModEnchantments.CONJURATION.get()) > 0) {
			poseStack.translate(flip * 0.35F, 0.5F, 0F);
		}
		if (stack.getEnchantmentLevel(ModEnchantments.ILLUSION.get()) > 0) {
			poseStack.translate(flip * 0.35F, 0.5F, 0F);
		}
		if (stack.getEnchantmentLevel(ModEnchantments.NECROMANCY.get()) > 0) {
			poseStack.translate(flip * 0.35F, 0.5F, 0F);
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
			return 10;
		}
		return 30;
	}

	@Override
	public void initializeClient(Consumer<IClientItemExtensions> consumer) {
		consumer.accept(new IClientItemExtensions() {

			private static final HumanoidModel.ArmPose DESTRUCTION_POSE = HumanoidModel.ArmPose.create("DESTRUCTION", false, (model, entity, arm) -> {
				float xOffset = 0F;
				if (model.crouching) xOffset = -0.6f;
				if (model.swimAmount > 0.0F) xOffset = -1.185f;
				if (arm == HumanoidArm.RIGHT) {
					model.rightArm.xRot = (-(float) Math.PI / 2F) + model.head.xRot + xOffset;
					model.rightArm.yRot = -0.1F + model.head.yRot;
				} else {
					model.leftArm.xRot = (-(float) Math.PI / 2F) + model.head.xRot + xOffset;
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

			private static final HumanoidModel.ArmPose SUMMON_POSE = HumanoidModel.ArmPose.create("SUMMON", false, (model, entity, arm) -> {
				// Определяем руку и получаем предмет
				InteractionHand hand = (arm == HumanoidArm.RIGHT) ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
				ItemStack stack = entity.getItemInHand(hand);
				float progress = 0f; // от 0 до 1
				if (stack.getItem() instanceof NetherStarScrollItem && stack.getTag() != null) {
					CompoundTag tag = stack.getTag();
					if (tag.contains("summonProgress", Tag.TAG_INT) && tag.getInt("summonProgress") > 15) {
						int val = tag.getInt("summonProgress") - 15;
						progress = Math.min(5, Math.max(0, val)) / 5f;
					}
				}

				if (arm == HumanoidArm.RIGHT) {
					// Целевые углы при progress=1 (summonProgress=20)
					float highXRot = -(float) Math.PI * 0.8f;
					float highZRot = -(float) Math.PI * 0.1f;
					// Целевые углы при progress=0
					float lowXRot = -(float) Math.PI / 10f;
					float lowZRot = 0f;
					// Интерполяция
					float targetXRot = lowXRot + (highXRot - lowXRot) * progress;
					float targetZRot = lowZRot + (highZRot - lowZRot) * progress;
					model.rightArm.xRot = model.rightArm.xRot * 0.5f + targetXRot;
					model.rightArm.zRot = model.rightArm.zRot * 0.5f + targetZRot;
					model.rightArm.yRot = 0f;
				} else { // левая рука
					float highXRot = -(float) Math.PI * 0.8f;
					float highZRot = (float) Math.PI * 0.1f; // положительный для левой
					float lowXRot = -(float) Math.PI / 10f;
					float lowZRot = 0f;
					float targetXRot = lowXRot + (highXRot - lowXRot) * progress;
					float targetZRot = lowZRot + (highZRot - lowZRot) * progress;
					model.leftArm.xRot = model.leftArm.xRot * 0.5f + targetXRot;
					model.leftArm.zRot = model.leftArm.zRot * 0.5f + targetZRot;
					model.leftArm.yRot = 0f;
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
				if (!itemStack.isEmpty() && itemStack.getEnchantmentLevel(ModEnchantments.NECROMANCY.get()) > 0) {
					if (entityLiving.isUsingItem() && entityLiving.getUseItemRemainingTicks() > 0 && entityLiving.getUsedItemHand() == hand) {
						return ACTIVE_USE_POSE;
					}
				}
				if (!itemStack.isEmpty() && itemStack.getEnchantmentLevel(ModEnchantments.CONJURATION.get()) > 0) {
					CompoundTag tag = itemStack.getTag();
					if (tag != null && tag.contains("isPickaxe", Tag.TAG_INT)) {
						if (tag.contains("summonProgress", Tag.TAG_INT) && tag.getInt("summonProgress") > 0) {
							return SUMMON_POSE;
						} else {
							return HumanoidModel.ArmPose.ITEM;
						}
					} else {
						// Обычное поведение: поза только во время использования
						if (entityLiving.isUsingItem() && entityLiving.getUseItemRemainingTicks() > 0 && entityLiving.getUsedItemHand() == hand) {
							return ACTIVE_USE_POSE;
						}
					}
				}
				if (!itemStack.isEmpty() && itemStack.getEnchantmentLevel(ModEnchantments.ILLUSION.get()) > 0) {
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
			return "40";
		}
		if (EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.ILLUSION.get(), item) > 0) {
			return "5";
		}
		if (EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.RESTORATION.get(), item) > 0) {
			return "25";
		}
		if (EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.CONJURATION.get(), item) > 0) {
			return "5";
		}
		if (EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.NECROMANCY.get(), item) > 0) {
			return "5";
		}
		return "5";
	}

	@Override
	public int getManaCost(ItemStack item, Player player) {
		if (EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.DESTRUCTION.get(), item) > 0) {
			return 1;
		}
		if (EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.RESTORATION.get(), item) > 0) {
			return 25;
		}
		if (EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.ALTERATION.get(), item) > 0) {
			return 40;
		}
		if (EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.ILLUSION.get(), item) > 0) {
			return 1;
		}
		if (EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.CONJURATION.get(), item) > 0) {
			CompoundTag tag = item.getTag();
			if (tag != null && tag.contains("isPickaxe", Tag.TAG_INT)) {
				return 1;
			}
			return 5;
		}
		if (EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.NECROMANCY.get(), item) > 0) {
			return 5;
		}
		return 5;
	}

	public Component getManaCostAdditionalLine(ItemStack item) {
		if (EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.DESTRUCTION.get(), item) > 0) {
			return Component.translatable("tooltip.rpgworldmod.cost_per_second").withStyle(ChatFormatting.BLUE);
		}
		if (EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.ILLUSION.get(), item) > 0) {
			return Component.translatable("tooltip.rpgworldmod.cost_per_second").withStyle(ChatFormatting.BLUE);
		}
		if (EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.CONJURATION.get(), item) > 0) {
			return Component.translatable("tooltip.rpgworldmod.plus_cost_per_block_destroyed", "1").withStyle(ChatFormatting.BLUE);
		}
		return Component.literal("");
	}

	public static ItemStack createForEnchantment(EnchantmentInstance pInstance) {
		ItemStack itemstack = new ItemStack(ModItems.NETHER_STAR_SCROLL.get());
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
				CompoundTag tag = itemstack.getTag();
				if (tag != null && tag.contains("isPickaxe", Tag.TAG_INT)) {
					return InteractionResultHolder.fail(itemstack);
				}
			}

			if (EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.ALTERATION.get(), itemstack) > 0) {
				boolean netherEnabled = level.getServer().isNetherEnabled();
				ResourceKey<Level> currentDim = player.level().dimension();
				ResourceKey<Level> targetDim = null;

				if (currentDim == Level.OVERWORLD) {
					targetDim = Level.NETHER;
				} else if (currentDim == Level.NETHER) {
					targetDim = Level.OVERWORLD;
				}
				if (targetDim == Level.NETHER && !netherEnabled) {
					player.displayClientMessage(
							Component.translatable("message.rpgworldmod.nether_disabled"), true);
					if (player instanceof ServerPlayer serverPlayer)
						ModAdvancements.SPELL_ALTERATION_NETHER_STAR_TRIGGER.trigger(serverPlayer);
					return InteractionResultHolder.fail(itemstack);
				}
				if (player.isPassenger() || player.isSleeping()) {
					player.displayClientMessage(
							Component.translatable("message.rpgworldmod.cannot_teleport_now"), true);
					return InteractionResultHolder.fail(itemstack);
				}
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
				data.active = true;
				data.lastSentBeamEndPoint = null;
				ModMessages.sendToNearbyPlayers(new S2CBeamUpdatePacket(player.getId(), Vec3.ZERO, false, new ArrayList<>()),
						level, player.blockPosition(), 144.0);
			}

			startEnchantmentSounds(level, player, itemstack);
			player.startUsingItem(hand);
		} else {
			// Клиентская сторона (без изменений)
			if (!canUseClient(player, itemstack)) {
				return InteractionResultHolder.fail(itemstack);
			}

			if (EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.ALTERATION.get(), itemstack) > 0) {
				if (player.isPassenger() || player.isSleeping()) {
					player.displayClientMessage(
							Component.translatable("message.rpgworldmod.cannot_teleport_now"), true);
					return InteractionResultHolder.fail(itemstack);
				}
			}
			if (EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.CONJURATION.get(), itemstack) > 0) {
				CompoundTag tag = itemstack.getTag();
				if (tag != null && tag.contains("isPickaxe", Tag.TAG_INT)) {
					return InteractionResultHolder.fail(itemstack);
				}
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

	private boolean canUse(Player player, ItemStack stack) {
		if (player.getAbilities().instabuild) return true;
		if (usesHealthInsteadOfMana(stack)) {
			return Mth.ceil(player.getHealth()) >= getManaCost(stack, player);
		} else {
			AtomicBoolean result = new AtomicBoolean(false);
			player.getCapability(PlayerManaProvider.PLAYER_MANA).ifPresent(mana -> {
				if (mana.getMana() >= getManaCost(stack, player)) {
					if (EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.RESTORATION.get(), stack) <= 0
							&& EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.DESTRUCTION.get(), stack) <= 0
							&& EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.ALTERATION.get(), stack) <= 0
							&& EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.CONJURATION.get(), stack) <= 0
							&& EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.NECROMANCY.get(), stack) <= 0) {
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
			player.gameEvent(GameEvent.ENTITY_INTERACT, player);
			ModMessages.sendToNearbyPlayers(new LoopSoundPacket(player.getId(), true, stack), level, player.blockPosition(), 64.0);
		}
		if (EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.ALTERATION.get(), stack) > 0) {
			level.playSound(null, player.getX(), player.getY(), player.getZ(),
					RPGSounds.SPELL_ALTERATION_START.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
			player.gameEvent(GameEvent.ENTITY_INTERACT, player);
			ModMessages.sendToNearbyPlayers(new LoopSoundPacket(player.getId(), true, stack), level, player.blockPosition(), 64.0);
		}
		if (EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.RESTORATION.get(), stack) > 0) {
			level.playSound(null, player.getX(), player.getY(), player.getZ(),
					RPGSounds.SPELL_RESTORATION_START.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
			player.gameEvent(GameEvent.ENTITY_INTERACT, player);
			ModMessages.sendToNearbyPlayers(new LoopSoundPacket(player.getId(), true, stack), level, player.blockPosition(), 64.0);
		}
		if (EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.CONJURATION.get(), stack) > 0) {
			ModMessages.sendToNearbyPlayers(new LoopSoundPacket(player.getId(), true, stack), level, player.blockPosition(), 64.0);
		}
		if (EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.NECROMANCY.get(), stack) > 0) {
			ModMessages.sendToNearbyPlayers(new LoopSoundPacket(player.getId(), true, stack), level, player.blockPosition(), 64.0);
			level.playSound(null, player.getX(), player.getY(), player.getZ(),
					RPGSounds.SPELL_NECROMANCY_START.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
			level.playSound(null, player.getX(), player.getY(), player.getZ(),
					RPGSounds.SPELL_NECROMANCY_NETHER_STAR_PRIMED.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
			player.gameEvent(GameEvent.PRIME_FUSE);
		}
		if (EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.ILLUSION.get(), stack) > 0) {
			level.playSound(null, player.getX(), player.getY(), player.getZ(),
					RPGSounds.SPELL_ILLUSION_START.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
			ModMessages.sendToNearbyPlayers(new LoopSoundPacket(player.getId(), true, stack), level, player.blockPosition(), 64.0);
		}
	}

	public void applyBeaconEffect(Player player, ItemStack item, int effectId) {
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
			switch (effectId) {
				case 0:
					player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 1200));
					break;
				case 1:
					player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 1200));
					break;
				case 2:
					player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 800));
					break;
				case 3:
					player.addEffect(new MobEffectInstance(MobEffects.JUMP, 800));
					break;
				case 4:
					player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 400));
					break;
				default:
					break;
			}
			if (player instanceof ServerPlayer serverPlayer)
				ModAdvancements.SPELL_RESTORATION_NETHER_STAR_TRIGGER.trigger(serverPlayer);
			player.level().playSound(null, player.blockPosition(),
					RPGSounds.SPELL_RESTORATION_CAST.get(),
					SoundSource.PLAYERS, 1.0F, (player.level().random.nextFloat() - player.level().random.nextFloat()) * 0.2F + 1.0F
			);
			player.gameEvent(GameEvent.ENTITY_INTERACT, player);
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
				stopEnchantmentSounds(level, player, stack, data.useTicks);
				ModMessages.sendToNearbyPlayers(new S2CBeamUpdatePacket(player.getId(), Vec3.ZERO, false, new ArrayList<>()),
						level, player.blockPosition(), 144.0);
			} else {
				ModMessages.sendToServer(new UpdateItemTagMessage(player.getId(), stack));
			}
		}
		super.releaseUsing(stack, level, livingEntity, timeCharged);
	}

	private void stopEnchantmentSounds(Level level, Player player, ItemStack stack, int useTicks) {
		if (EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.DESTRUCTION.get(), stack) > 0 && useTicks > 40 && level instanceof ServerLevel serverLevel) {
			Vec3 eyePos = player.getEyePosition();
			Vec3 direction = player.getLookAngle().normalize();
			handleBeamStop(player, eyePos, calculateBeamEndPoint(serverLevel, eyePos, direction, DESTRUCTION_BEAM_RANGE));
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
		if (EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.NECROMANCY.get(), stack) > 0) {
			level.playSound(null, player.getX(), player.getY(), player.getZ(),
					RPGSounds.SPELL_NECROMANCY_STOP.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
		}
		ModMessages.sendToNearbyPlayers(new LoopSoundPacket(player.getId(), false, stack),
				(ServerLevel) level, player.blockPosition(), 64.0);
	}

	@Override
	public int getUseDuration(ItemStack stack) {
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
		AtomicBoolean isDepressed = new AtomicBoolean(false);
		if (!level.isClientSide) {
			if (entity instanceof Player player && !player.getAbilities().instabuild) {
				player.getCapability(PlayerManaProvider.PLAYER_MANA).ifPresent(mana -> {
					if (mana.getMana() <= 0) {
						isDepressed.set(true);
					}
				});
			}
		}
		if ((!(held || entity instanceof Player player && player.getOffhandItem() == stack) || isDepressed.get())
				&& stack.getTag() != null
				&& stack.getTag().contains("isPickaxe", Tag.TAG_INT)) {
			CompoundTag nbtData = stack.getOrCreateTag();
			nbtData.remove("isPickaxe");
			stack.setTag(nbtData);
			level.playSound(null, entity.blockPosition(), RPGSounds.SPELL_CONJURATION_STOP.get(), SoundSource.PLAYERS, 1F, (level.random.nextFloat() - level.random.nextFloat()) * 0.2F + 1.0F);
			entity.gameEvent(GameEvent.ENTITY_DIE, entity);
		}
		if (stack.getTag() != null && stack.getTag().contains("summonProgress", Tag.TAG_INT) && stack.getOrCreateTag().getInt("summonProgress") > 0) {
			CompoundTag nbtData = stack.getOrCreateTag();
			if (stack.getOrCreateTag().getInt("summonProgress") == 0) {
				nbtData.remove("summonProgress");
			} else {
				nbtData.putInt("summonProgress", nbtData.getInt("summonProgress") - 1);
			}
			stack.setTag(nbtData);
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

		if (usingItem.getItem() instanceof NetherStarScrollItem scroll) {
			scroll.stopEnchantmentSounds(level, player, usingItem, data.useTicks);
		}

		ModMessages.sendToNearbyPlayers(new S2CBeamUpdatePacket(player.getId(), Vec3.ZERO, false, new ArrayList<>()),
				level, player.blockPosition(), 144.0);

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

			if (!player.isUsingItem()) {
				// Игрок перестал использовать предмет, но данные ещё активны — деактивируем
				data.active = false;
				continue;
			}

			ItemStack usingItem = player.getUseItem();
			if (!(usingItem.getItem() instanceof NetherStarScrollItem)) {
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

			if (data.shouldConsumeMana(usingItem, level, player.getTicksUsingItem())) {
				if (!player.getAbilities().instabuild) {
					AtomicBoolean hasMana = new AtomicBoolean(true);
					player.getCapability(PlayerManaProvider.PLAYER_MANA).ifPresent(mana -> {
						if (usingItem.getItem() instanceof ManaCostItem manaCostItem) {
							if (mana.getMana() < manaCostItem.getManaCost(usingItem, player)) {
								hasMana.set(false);
							} else {
								int cost = manaCostItem.getManaCost(usingItem, player);
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
			if (EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.ILLUSION.get(), usingItem) > 0) {
				if (player instanceof ServerPlayer serverPlayer)
					ModAdvancements.SPELL_ILLUSION_NETHER_STAR_TRIGGER.trigger(serverPlayer);
				double radius = 15.0;
				AABB aabb = player.getBoundingBox().inflate(radius);
				List<Player> nearbyPlayers = level.getEntitiesOfClass(Player.class, aabb,
						p -> p != player && p.isAlive() && !p.isSpectator() && !p.isCreative());

				for (Player target : nearbyPlayers) {
					if (canSeePlayer(target, player)) {
						MobEffectInstance illusion = new MobEffectInstance(ModEffects.NETHER_PORTAL_ILLUSION.get(), 30, 0);
						illusion.setCurativeItems(new ArrayList<>());
						target.addEffect(illusion);
					}
				}
			}
			if (EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.NECROMANCY.get(), usingItem) > 0 && data.useTicks > 30) {
				if (usingItem.getItem() instanceof NetherStarScrollItem netherStarScrollItem) {
					if (!player.getAbilities().instabuild) {
						int amp = netherStarScrollItem.getManaCost(usingItem, player);
						if (Mth.ceil(player.getHealth()) < amp) {
							return;
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
					player.getCooldowns().addCooldown(ModItems.NETHER_STAR_SCROLL.get(), 15);
					data.active = false;
					stopPlayerUse(level, player, usingItem, false);
					level.playSound(null, player.blockPosition(),
							RPGSounds.SPELL_NECROMANCY_CAST.get(),
							SoundSource.PLAYERS, 1.0F, (level.random.nextFloat() - level.random.nextFloat()) * 0.2F + 1.0F
					);
					player.gameEvent(GameEvent.ENTITY_DAMAGE, player);
					player.gameEvent(GameEvent.EXPLODE);
					if (player instanceof ServerPlayer serverPlayer)
						ModAdvancements.SPELL_NECROMANCY_NETHER_STAR_TRIGGER.trigger(serverPlayer);
					player.level().explode(player, player.getX(), player.getY(), player.getZ(), 4.5F, Level.ExplosionInteraction.MOB);
				}
			}
			if (EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.ALTERATION.get(), usingItem) > 0) {
				boolean netherEnabled = level.getServer().isNetherEnabled();
				ResourceKey<Level> currentDim = player.level().dimension();
				ResourceKey<Level> targetDim = null;

				if (currentDim == Level.OVERWORLD) {
					targetDim = Level.NETHER;
				} else if (currentDim == Level.NETHER) {
					targetDim = Level.OVERWORLD;
				}
				if (targetDim == Level.NETHER && !netherEnabled) {
					player.displayClientMessage(
							Component.translatable("message.rpgworldmod.nether_disabled"), true);
					if (player instanceof ServerPlayer serverPlayer)
						ModAdvancements.SPELL_ALTERATION_NETHER_STAR_TRIGGER.trigger(serverPlayer);
					data.active = false;
					stopPlayerUse(level, player, usingItem, false);
					player.getCooldowns().addCooldown(ModItems.NETHER_STAR_SCROLL.get(), 15);
					return;
				}
				if (player.isPassenger() || player.isSleeping()) {
					player.displayClientMessage(
							Component.translatable("message.rpgworldmod.cannot_teleport_now"), true);
					data.active = false;
					stopPlayerUse(level, player, usingItem, false);
					player.getCooldowns().addCooldown(ModItems.NETHER_STAR_SCROLL.get(), 15);
					return;
				}
			}
			if (EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.CONJURATION.get(), usingItem) > 0 && data.useTicks > 40) {
				if (usingItem.getItem() instanceof NetherStarScrollItem netherStarScrollItem) {
					player.getCapability(PlayerManaProvider.PLAYER_MANA).ifPresent(mana -> {
						int cost = netherStarScrollItem.getManaCost(usingItem, player);
						if (mana.getMana() >= cost || player.getAbilities().instabuild) {
							if (!player.getAbilities().instabuild) mana.reduceMana((ServerPlayer) player, cost);
							data.active = false;
							stopPlayerUse(level, player, usingItem, false);
							player.getCooldowns().addCooldown(ModItems.NETHER_STAR_SCROLL.get(), 15);
							level.playSound(null, player.blockPosition(),
									RPGSounds.SPELL_CONJURATION_START.get(),
									SoundSource.PLAYERS, 1.0F, (level.random.nextFloat() - level.random.nextFloat()) * 0.2F + 1.0F
							);
							player.gameEvent(GameEvent.ENTITY_PLACE, player);
							CompoundTag nbtData = usingItem.getOrCreateTag();
							nbtData.putInt("isPickaxe", 1);
							nbtData.putInt("summonProgress", 20);
							usingItem.setTag(nbtData);
							if (player instanceof ServerPlayer serverPlayer)
								ModAdvancements.SPELL_CONJURATION_NETHER_STAR_TRIGGER.trigger(serverPlayer);
						}
					});
				}
			}
			if (EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.ALTERATION.get(), usingItem) > 0 && data.useTicks > 80) {
				if (usingItem.getItem() instanceof NetherStarScrollItem netherStarScrollItem) {
					player.getCapability(PlayerManaProvider.PLAYER_MANA).ifPresent(mana -> {
						int cost = netherStarScrollItem.getManaCost(usingItem, player);
						if (mana.getMana() >= cost || player.getAbilities().instabuild) {

							if (!player.getAbilities().instabuild) mana.reduceMana((ServerPlayer) player, cost);
							data.active = false;
							stopPlayerUse(level, player, usingItem, false);
							player.getCooldowns().addCooldown(ModItems.NETHER_STAR_SCROLL.get(), 15);
							level.playSound(null, player.blockPosition(),
									RPGSounds.SPELL_ALTERATION_CAST.get(),
									SoundSource.PLAYERS, 1.0F,
									(level.random.nextFloat() - level.random.nextFloat()) * 0.2F + 1.0F);
							player.gameEvent(GameEvent.ENTITY_INTERACT, player);

							ResourceKey<Level> currentDim = player.level().dimension();
							ResourceKey<Level> targetDim = null;
							if (currentDim == Level.OVERWORLD) {
								targetDim = Level.NETHER;
							} else if (currentDim == Level.NETHER) {
								targetDim = Level.OVERWORLD;
							} else {
								level.explode((Entity) null, level.damageSources().badRespawnPointExplosion(player.getPosition(0)), (ExplosionDamageCalculator) null, player.getPosition(0), 5.0F, true, Level.ExplosionInteraction.BLOCK);
								return;
							}

							// Проверки
							if (targetDim == Level.NETHER && !level.getServer().isNetherEnabled()) {
								player.displayClientMessage(Component.translatable("message.rpgworldmod.nether_disabled"), true);
								if (player instanceof ServerPlayer serverPlayer)
									ModAdvancements.SPELL_ALTERATION_NETHER_STAR_TRIGGER.trigger(serverPlayer);
								return;
							}
							if (player.isPassenger() || player.isSleeping()) {
								player.displayClientMessage(Component.translatable("message.rpgworldmod.cannot_teleport_now"), true);
								return;
							}

							ServerLevel targetLevel = ((ServerPlayer) player).server.getLevel(targetDim);
							if (targetLevel == null) {
								if (player instanceof ServerPlayer serverPlayer)
									ModAdvancements.SPELL_ALTERATION_NETHER_STAR_TRIGGER.trigger(serverPlayer);
								return;
							}

							// Масштабирование координат
							BlockPos sourcePos = player.blockPosition();
							double x, z;
							if (currentDim == Level.OVERWORLD) {
								x = sourcePos.getX() / 8.0;
								z = sourcePos.getZ() / 8.0;
							} else {
								x = sourcePos.getX() * 8.0;
								z = sourcePos.getZ() * 8.0;
							}

							double x1 = player.getBlockX() + 0.5;
							double y1 = player.getBlockY() + 1;
							double z1 = player.getBlockZ() + 0.5;
							ModMessages.sendToNearbyPlayers(new PortalEffectPacket(x1, y1, z1), level, player.blockPosition(), 64.0);

							BlockPos safePos = findSafePortalPosition(targetLevel, new BlockPos((int) x, sourcePos.getY(), (int) z), Direction.Axis.X);
							safePos = new BlockPos(safePos.getX(), safePos.getY() + 1, safePos.getZ());

							// Выполняем телепортацию
							player.changeDimension(targetLevel, new CustomTeleporter(safePos));

							// После телепортации проверяем безопасность позиции и при необходимости создаём платформу
							BlockPos playerPos = player.blockPosition();
							if (!isSafePosition(targetLevel, playerPos)) {
								BlockPos safeBelow = findSafeGroundBelow(targetLevel, playerPos, 256);
								if (safeBelow != null) {
									player.teleportTo(player.getX(), safeBelow.getY() + 1, player.getZ());
								} else {
									BlockPos safeAbove = findSafeGroundAbove(targetLevel, playerPos, 256);
									if (safeAbove != null) {
										player.teleportTo(player.getX(), safeAbove.getY() + 1, player.getZ());
									} else {
										BlockPos belowPos = playerPos.below();
										createPlatform(targetLevel, belowPos);
										if (player.getY() < belowPos.getY() + 1) {
											player.teleportTo(player.getX(), belowPos.getY() + 1, player.getZ());
										}
									}
								}
							}
							ensureSafePosition(targetLevel, player);
							if (player instanceof ServerPlayer serverPlayer)
								ModAdvancements.SPELL_ALTERATION_NETHER_STAR_TRIGGER.trigger(serverPlayer);
							double x2 = player.getBlockX() + 0.5;
							double y2 = player.getBlockY() + 1;
							double z2 = player.getBlockZ() + 0.5;
							ModMessages.sendToNearbyPlayers(new PortalEffectPacket(x2, y2, z2), targetLevel, player.blockPosition(), 64.0);
						}
					});
				}
			}
			if (EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.DESTRUCTION.get(), usingItem) > 0) {
				if (data.useTicks > DESTRUCTION_CHARGE_TICKS) {
					List<Vec3> hitPositions = applyDestructionBeamDamage(level, player, usingItem);
					data.lastHitEntityPositions = hitPositions;

					Vec3 eyePos = player.getEyePosition();
					Vec3 direction = player.getLookAngle().normalize();
					Vec3 endPoint = calculateBeamEndPoint(level, eyePos, direction, DESTRUCTION_BEAM_RANGE);

					// Отправляем пакет каждый тик
					if (data.lastSentBeamEndPoint == null) {
						handleBeamStart(player, eyePos, endPoint, usingItem);
					}
					data.lastSentBeamEndPoint = endPoint;
					ModMessages.sendToNearbyPlayers(
							new S2CBeamUpdatePacket(player.getId(), endPoint, true, hitPositions),
							level, player.blockPosition(), 144.0
					);
				}
			}
		}
	}

	private static void ensureSafePosition(ServerLevel level, Player player) {
		BlockPos playerPos = player.blockPosition();
		BlockState blockAtFeet = level.getBlockState(playerPos);
		BlockState blockAtHead = level.getBlockState(playerPos.above());

		// Если игрок не внутри твёрдого блока (ноги и голова в воздухе или заменяемых блоках), выходим
		boolean isInsideSolid = (blockAtFeet.isSolid() && !blockAtFeet.canBeReplaced()) ||
				(blockAtHead.isSolid() && !blockAtHead.canBeReplaced());
		if (!isInsideSolid) {
			return;
		}

		// Разрушаем блоки в области 3x3x2 вокруг игрока (X: -1..1, Z: -1..1, Y: 0..1 относительно ног)
		float explosionResistanceThreshold = 20.0F; // как в примере со Spiky Ivy
		BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();
		for (int dx = -1; dx <= 1; dx++) {
			for (int dz = -1; dz <= 1; dz++) {
				for (int dy = 0; dy <= 1; dy++) {
					mutablePos.set(playerPos.getX() + dx, playerPos.getY() + dy, playerPos.getZ() + dz);
					BlockState state = level.getBlockState(mutablePos);
					// Проверяем, не взрывоустойчив ли блок (как в randomTick)
					if (state.getExplosionResistance(level, mutablePos, null) < explosionResistanceThreshold) {
						level.destroyBlock(mutablePos, true); // true - дропать предметы
					}
				}
			}
		}

		// Дополнительно: попробуем немного сдвинуть игрока вверх, если он всё ещё внутри блока
		// (на случай, если блоки были неразрушаемыми)
		if (!level.getBlockState(player.blockPosition()).isAir()) {
			player.teleportTo(player.getX(), player.getY() + 1, player.getZ());
		}
	}

	private static class CustomTeleporter implements ITeleporter {
		private final BlockPos safePos;

		public CustomTeleporter(BlockPos safePos) {
			this.safePos = safePos;
		}

		@Override
		public PortalInfo getPortalInfo(Entity entity, ServerLevel destWorld, Function<ServerLevel, PortalInfo> defaultPortalInfo) {
			if (safePos != null) {
				// Убедимся, что позиция внутри мира
				double x = safePos.getX() + 0.5;
				double y = safePos.getY() + 0.5;
				double z = safePos.getZ() + 0.5;
				y = Math.min(Math.max(y, destWorld.getMinBuildHeight() + 1), destWorld.dimension() == Level.OVERWORLD ? destWorld.getMaxBuildHeight() - 1 : 127);
				return new PortalInfo(new Vec3(x, y, z), Vec3.ZERO, entity.getYRot(), entity.getXRot());
			}
			// Если safePos null, используем стандартную логику
			return defaultPortalInfo.apply(destWorld);
		}

		@Override
		public Entity placeEntity(Entity entity, ServerLevel currentWorld, ServerLevel destWorld, float yaw, Function<Boolean, Entity> repositionEntity) {
			return repositionEntity.apply(false);
		}
	}

	private static BlockPos findSafePortalPosition(ServerLevel level, BlockPos pos, Direction.Axis axis) {
		int maxY = Math.min(level.dimension() == Level.OVERWORLD ? level.getMaxBuildHeight() : 128, level.getMinBuildHeight() + level.getLogicalHeight()) - 1;
		int minY = level.getMinBuildHeight();
		WorldBorder worldBorder = level.getWorldBorder();
		int searchRadius = 32;
		BlockPos bestPos = null;
		double bestDist = Double.MAX_VALUE;
		BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();

		// Поиск безопасной позиции
		for (BlockPos candidate : BlockPos.spiralAround(pos, searchRadius, Direction.EAST, Direction.SOUTH)) {
			int x = candidate.getX();
			int z = candidate.getZ();
			if (!worldBorder.isWithinBounds(x, z)) continue;

			int surfaceY = level.getHeight(Heightmap.Types.MOTION_BLOCKING, x, z);
			if (surfaceY > maxY - 2) continue;

			mutablePos.set(x, surfaceY, z);
			BlockState groundState = level.getBlockState(mutablePos);
			if (groundState.is(Blocks.LAVA) || !groundState.isSolid()) {
				mutablePos.set(x, surfaceY - 1, z);
				groundState = level.getBlockState(mutablePos);
				if (groundState.is(Blocks.LAVA) || !groundState.isSolid()) continue;
				surfaceY--;
			}

			boolean spaceAbove = true;
			for (int y = surfaceY + 1; y <= surfaceY + 2; y++) {
				if (y > maxY) {
					spaceAbove = false;
					break;
				}
				mutablePos.set(x, y, z);
				if (!level.getBlockState(mutablePos).isAir() && !level.getBlockState(mutablePos).canBeReplaced()) {
					spaceAbove = false;
					break;
				}
			}
			if (!spaceAbove) continue;

			boolean lavaNearby = false;
			for (int dx = -1; dx <= 1; dx++) {
				for (int dz = -1; dz <= 1; dz++) {
					mutablePos.set(x + dx, surfaceY, z + dz);
					if (level.getBlockState(mutablePos).is(Blocks.LAVA)) {
						lavaNearby = true;
						break;
					}
				}
			}
			if (lavaNearby) continue;

			mutablePos.set(x, surfaceY + 1, z);
			if (!level.getBlockState(mutablePos).isAir()) continue;

			double dist = candidate.distToCenterSqr(pos.getX(), 0, pos.getZ());
			if (bestPos == null || dist < bestDist) {
				bestDist = dist;
				bestPos = new BlockPos(x, surfaceY, z);
			}
		}

		// Fallback: если не нашли, используем позицию с коррекцией высоты
		if (bestPos == null) {
			int fallbackY = Math.max(minY + 1, Math.min(70, maxY - 3));
			bestPos = new BlockPos(pos.getX(), fallbackY, pos.getZ());
			// Попытаемся подобрать безопасную высоту в этой точке
			for (int y = fallbackY; y <= maxY - 2; y++) {
				mutablePos.set(bestPos.getX(), y, bestPos.getZ());
				BlockState below = level.getBlockState(mutablePos);
				if ((!below.isAir() && below.isSolid()) || y == fallbackY) {
					// Проверяем место над головой
					boolean ok = true;
					for (int h = 1; h <= 2; h++) {
						mutablePos.set(bestPos.getX(), y + h, bestPos.getZ());
						if (!level.getBlockState(mutablePos).isAir() && !level.getBlockState(mutablePos).canBeReplaced()) {
							ok = false;
							break;
						}
					}
					if (ok) {
						bestPos = new BlockPos(bestPos.getX(), y, bestPos.getZ());
						break;
					}
				}
			}
		}

		return bestPos;
	}

	private static BlockPos findSafeGroundBelow(ServerLevel level, BlockPos startPos, int maxFall) {
		BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
		for (int dy = 0; dy <= maxFall; dy++) {
			mutable.set(startPos.getX(), startPos.getY() - dy, startPos.getZ());
			BlockState state = level.getBlockState(mutable);
			// Ищем твёрдый блок, не лаву
			if (state.isSolid()) {
				// Проверяем, что над этим блоком есть 2 блока воздуха
				boolean spaceAbove = true;
				for (int h = 1; h <= 2; h++) {
					mutable.set(startPos.getX(), startPos.getY() - dy + h, startPos.getZ());
					if (!level.getBlockState(mutable).isAir() && !level.getBlockState(mutable).canBeReplaced()) {
						spaceAbove = false;
						break;
					}
				}
				if (spaceAbove) {
					return mutable.immutable();
				}
			}
			if (state.is(Blocks.LAVA)) {
				return null;
			}
		}
		return null;
	}

	private static BlockPos findSafeGroundAbove(ServerLevel level, BlockPos startPos, int maxRise) {
		BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
		for (int dy = 0; dy <= maxRise; dy++) {
			int limit = level.dimension() == Level.OVERWORLD ? level.getMaxBuildHeight() - 1 : 127;
			if (startPos.getY() + dy >= limit) return null;
			mutable.set(startPos.getX(), startPos.getY() + dy, startPos.getZ());
			BlockState state = level.getBlockState(mutable);
			if (state.isSolid() && !state.is(Blocks.LAVA)) {
				boolean spaceAbove = true;
				for (int h = 1; h <= 2; h++) {
					mutable.set(startPos.getX(), startPos.getY() + dy + h, startPos.getZ());
					if (!level.getBlockState(mutable).isAir() && !level.getBlockState(mutable).canBeReplaced()) {
						spaceAbove = false;
						break;
					}
				}
				if (spaceAbove) {
					return mutable.immutable();
				}
			}
		}
		return null;
	}

	private static boolean isSafePosition(ServerLevel level, BlockPos playerPos) {
		BlockPos below = playerPos.below();
		BlockState belowState = level.getBlockState(below);
		if (belowState.isAir() || belowState.is(Blocks.LAVA) || !belowState.isSolid()) {
			return false;
		}
		for (int y = 1; y <= 2; y++) {
			BlockPos checkPos = playerPos.above(y);
			BlockState state = level.getBlockState(checkPos);
			if (!state.isAir() && !state.canBeReplaced()) {
				return false;
			}
		}
		return true;
	}

	private static void createPlatform(ServerLevel level, BlockPos belowPos) {
		// Создаём платформу 3x3 из незерака
		for (int dx = -1; dx <= 1; dx++) {
			for (int dz = -1; dz <= 1; dz++) {
				BlockPos pos = belowPos.offset(dx, 0, dz);
				if (level.getBlockState(pos).isAir() || level.getBlockState(pos).is(Blocks.LAVA)) {
					level.setBlock(pos, Blocks.NETHERRACK.defaultBlockState(), 3);
				}
			}
		}
	}

	public static void handleBeamStart(Player player, Vec3 start, Vec3 end, ItemStack itemStack) {
		Vec3 direction = end.subtract(start).normalize();
		double length = start.distanceTo(end);
		double step = 1.0;
		for (double d = step; d <= length; d += step) {
			Vec3 pos = start.add(direction.scale(d));
			player.level().playSound(null, pos.x, pos.y, pos.z,
					RPGSounds.SPELL_DESTRUCTION_NETHER_STAR_START.get(), SoundSource.PLAYERS,
					0.1F, 1.0F);
			player.gameEvent(GameEvent.ENTITY_INTERACT, player);
		}
		if (player instanceof ServerPlayer serverPlayer)
			ModAdvancements.SPELL_DESTRUCTION_NETHER_STAR_TRIGGER.trigger(serverPlayer);
		ModMessages.sendToNearbyPlayers(new LoopSoundPacket(player.getId(), false, itemStack),
				(ServerLevel) player.level(), player.blockPosition(), 64.0);
	}

	public static void handleBeamStop(Player player, Vec3 start, Vec3 end) {
		Vec3 direction = end.subtract(start).normalize();
		double length = start.distanceTo(end);
		double step = 1.0;
		for (double d = step; d <= length; d += step) {
			Vec3 pos = start.add(direction.scale(d));
			player.level().playSound(null, pos.x, pos.y, pos.z,
					RPGSounds.SPELL_DESTRUCTION_NETHER_STAR_STOP.get(), SoundSource.PLAYERS,
					0.1F, 1.0F);
		}
	}

	private static List<Vec3> applyDestructionBeamDamage(ServerLevel level, Player player, ItemStack stack) {
		List<Vec3> hitPositions = new ArrayList<>();
		Vec3 eyePos = player.getEyePosition();
		Vec3 direction = player.getLookAngle().normalize();
		double maxDist = DESTRUCTION_BEAM_RANGE;
		double radius = DESTRUCTION_BEAM_RADIUS;

		AABB area = player.getBoundingBox().inflate(maxDist);
		List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, area,
				e -> e != player && e.isAlive() && !(e instanceof Player && ((Player) e).isCreative()));

		for (LivingEntity target : entities) {
			Vec3 targetCenter = target.getBoundingBox().getCenter();
			Vec3 toTarget = targetCenter.subtract(eyePos);
			double along = toTarget.dot(direction);
			if (along <= 0 || along > maxDist) continue;
			double distPerp = toTarget.cross(direction).length();
			if (distPerp > radius) continue;
			if (!isEntityVisibleThroughTransparentBlocks(level, eyePos, targetCenter)) continue;

			double t = 1.0 - along / maxDist;
			double damage = 7.0 * t * t;
			if (damage > 0) {
				DamageSource source = player.damageSources().magic();
				if (target instanceof Player targetPlayer) {
					if ((float) targetPlayer.invulnerableTime > 10.0F) {
						if (damage > targetPlayer.lastHurt) {
							targetPlayer.hurt(source, (float) damage);
						}
					} else {
						target.hurt(source, (float) damage);
					}
				} else {
					target.hurt(source, (float) damage);
				}
				if (!source.is(DamageTypeTags.NO_ANGER)) {
					target.setLastHurtByMob(player);
				}
				target.setLastHurtByPlayer(player);
				hitPositions.add(targetCenter);
			}
		}
		return hitPositions;
	}

	private static Vec3 calculateBeamEndPoint(ServerLevel level, Vec3 start, Vec3 direction, double maxDist) {
		double remaining = maxDist;
		Vec3 currentPos = start;
		double epsilon = 0.01; // точность

		while (remaining > epsilon) {
			Vec3 end = currentPos.add(direction.scale(remaining));
			ClipContext ctx = new ClipContext(currentPos, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, null);
			BlockHitResult hit = level.clip(ctx);

			if (hit.getType() == HitResult.Type.MISS) {
				// Нет столкновений – возвращаем конечную точку на максимальной дистанции
				return end;
			}

			BlockState state = level.getBlockState(hit.getBlockPos());
			if (state.canOcclude()) {
				// Непрозрачный блок – останавливаемся
				return hit.getLocation();
			}

			// Прозрачный блок – продолжаем луч
			double distToHit = currentPos.distanceTo(hit.getLocation());
			currentPos = hit.getLocation().add(direction.scale(0.001)); // небольшой сдвиг, чтобы пройти сквозь блок
			remaining -= distToHit;
		}

		return start;
	}

	private static boolean isEntityVisibleThroughTransparentBlocks(ServerLevel level, Vec3 origin, Vec3 targetPos) {
		Vec3 direction = targetPos.subtract(origin).normalize();
		double distance = origin.distanceTo(targetPos);
		double remaining = distance;
		Vec3 currentPos = origin;

		while (remaining > 0.01) {
			ClipContext ctx = new ClipContext(currentPos, currentPos.add(direction.scale(remaining)),
					ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, null);
			BlockHitResult hit = level.clip(ctx);
			if (hit.getType() == HitResult.Type.MISS) {
				// Нет столкновений — цель видима
				return true;
			}
			BlockState state = level.getBlockState(hit.getBlockPos());
			if (state.canOcclude()) {
				// Блок непрозрачный — цель не видна
				return false;
			}
			// Прозрачный блок — продолжаем луч
			double distToHit = origin.distanceTo(hit.getLocation());
			currentPos = hit.getLocation().add(direction.scale(0.001)); // сдвигаем, чтобы пройти сквозь блок
			remaining -= distToHit;
		}
		return true;
	}

	private static boolean canSeePlayer(Player player, Player owner) {
		if (player.isCreative() || player.isSpectator()) return false;
		Vec3 eyePos = owner.getEyePosition();

		Vec3 playerEyePos = player.getEyePosition();

		double distance = eyePos.distanceTo(playerEyePos);
		if (distance > 40D) {
			return false;
		}

		ClipContext context = new ClipContext(eyePos, playerEyePos,
				ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, null);

		BlockHitResult hitResult = player.level().clip(context);

		// Если луч попал в блок до игрока, значит игрок не виден
		if (hitResult.getType() != HitResult.Type.MISS) {
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
				EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.CONJURATION.get(), stack) > 0 ||
				EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.ALTERATION.get(), stack) > 0 ||
				EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.RESTORATION.get(), stack) > 0 ||
				EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.NECROMANCY.get(), stack) > 0 ||
				EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.ILLUSION.get(), stack) > 0;
	}

	public static class PlayerUseData {
		private final UUID playerId;
		private long startTime;
		private int useTicks;
		private int lastManaTick;
		public boolean active;
		public Vec3 lastSentBeamEndPoint;
		public List<Vec3> lastHitEntityPositions;
		private int lastSentTick;

		public PlayerUseData(UUID playerId, long startTime) {
			this.playerId = playerId;
			this.startTime = startTime;
			this.useTicks = 0;
			this.lastManaTick = 0;
			this.active = true;
			this.lastSentBeamEndPoint = null;
			this.lastHitEntityPositions = new ArrayList<>();
			this.lastSentTick = 0;
		}

		public void tick() {
			useTicks++;
		}

		public boolean shouldConsumeMana(ItemStack item, Level level, int useTicks) {
			int tick = 4;
			if (item.getEnchantmentLevel(ModEnchantments.ILLUSION.get()) > 0) tick = 4;
			if (item.getEnchantmentLevel(ModEnchantments.DESTRUCTION.get()) > 0) {
				if (useTicks < 41) return false;
				tick = 3;
			}
			if (item.getEnchantmentLevel(ModEnchantments.NECROMANCY.get()) > 0
					|| item.getEnchantmentLevel(ModEnchantments.RESTORATION.get()) > 0
					|| item.getEnchantmentLevel(ModEnchantments.ALTERATION.get()) > 0
					|| item.getEnchantmentLevel(ModEnchantments.CONJURATION.get()) > 0)
				return false;
			if (useTicks - lastManaTick >= tick) {
				lastManaTick = useTicks;
				return true;
			}
			return false;
		}
	}

	public String getFirstPredicate(ItemStack item) {
		return Minecraft.getInstance().options.keyShift.getKey().getDisplayName().getString();
	}

	public String getSecondPredicate(ItemStack item) {
		return Minecraft.getInstance().options.keyAttack.getKey().getDisplayName().getString();
	}

	public boolean hasControls(ItemStack item) {
		return item.getEnchantmentLevel(ModEnchantments.RESTORATION.get()) > 0;
	}

	@Override
	public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
		if (ItemStack.isSameItem(oldStack, newStack)) {
			boolean enchantmentsEqual = EnchantmentHelper.getEnchantments(oldStack)
					.equals(EnchantmentHelper.getEnchantments(newStack));
			boolean pickaxeTagsEqual = arePickaxeTagsEqual(oldStack, newStack);
			return !enchantmentsEqual || !pickaxeTagsEqual;
		} else {
			return true;
		}
	}

	private boolean arePickaxeTagsEqual(ItemStack a, ItemStack b) {
		int valA = getPickaxeTagValueOrNull(a);
		int valB = getPickaxeTagValueOrNull(b);
		return valA == valB;
	}

	private int getPickaxeTagValueOrNull(ItemStack stack) {
		CompoundTag tag = stack.getTag();
		if (tag != null && tag.contains("isPickaxe", Tag.TAG_INT)) {
			return tag.getInt("isPickaxe");
		}
		return -1;
	}

	public boolean isPickaxeMode(ItemStack stack) {
		if (stack.isEmpty()) return false;
		CompoundTag tag = stack.getTag();
		return tag != null && tag.contains("isPickaxe", Tag.TAG_INT) &&
				EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.CONJURATION.get(), stack) > 0;
	}

	@Override
	public boolean canPerformAction(ItemStack stack, net.minecraftforge.common.ToolAction toolAction) {
		if (isPickaxeMode(stack))
			return net.minecraftforge.common.ToolActions.DEFAULT_PICKAXE_ACTIONS.contains(toolAction);
		else return super.canPerformAction(stack, toolAction);
	}

	private static final Tier PICKAXE_TIER = ModTiers.CONJURED;

	@Override
	public float getDestroySpeed(ItemStack pStack, BlockState pState) {
		if (isPickaxeMode(pStack) && pState.is(BlockTags.MINEABLE_WITH_PICKAXE)) {
			return PICKAXE_TIER.getSpeed();
		}
		return super.getDestroySpeed(pStack, pState);
	}

	@Override
	public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
		if (isPickaxeMode(stack)) {
			if (attacker instanceof Player player) {
				if (!player.getAbilities().instabuild) {
					player.getCapability(PlayerManaProvider.PLAYER_MANA).ifPresent(mana -> {
						mana.reduceMana((ServerPlayer) player, getManaCost(stack, player) * 2);
					});
				}
			}
			return true;
		}
		return super.hurtEnemy(stack, target, attacker);
	}

	@Override
	public boolean mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity entity) {
		if (isPickaxeMode(stack)) {
			if (!level.isClientSide && state.getDestroySpeed(level, pos) != 0.0F) {
				if (entity instanceof Player player) {
					if (!player.getAbilities().instabuild) {
						player.getCapability(PlayerManaProvider.PLAYER_MANA).ifPresent(mana -> {
							mana.reduceMana((ServerPlayer) player, getManaCost(stack, player));
						});
					}
				}
			}
			return true;
		}
		return super.mineBlock(stack, level, state, pos, entity);
	}

	@Override
	public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot slot, ItemStack stack) {
		if (isPickaxeMode(stack) && slot == EquipmentSlot.MAINHAND) {
			ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
			float attackDamage = -2.0F + PICKAXE_TIER.getAttackDamageBonus();
			float attackSpeed = -2.8F;
			builder.put(Attributes.ATTACK_DAMAGE,
					new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, "Tool modifier", attackDamage, AttributeModifier.Operation.ADDITION));
			builder.put(Attributes.ATTACK_SPEED,
					new AttributeModifier(BASE_ATTACK_SPEED_UUID, "Tool modifier", attackSpeed, AttributeModifier.Operation.ADDITION));
			return builder.build();
		}
		return super.getAttributeModifiers(slot, stack);
	}

	@Override
	public boolean isCorrectToolForDrops(ItemStack stack, BlockState state) {
		if (isPickaxeMode(stack)) {
			return state.is(BlockTags.MINEABLE_WITH_PICKAXE) &&
					net.minecraftforge.common.TierSortingRegistry.isCorrectTierForDrops(PICKAXE_TIER, state);
		}
		return super.isCorrectToolForDrops(stack, state);
	}
}