package net.dainplay.rpgworldmod.item.custom;

import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.dainplay.rpgworldmod.block.ModBlocks;
import net.dainplay.rpgworldmod.block.entity.custom.BoundCampfireBlockEntity;
import net.dainplay.rpgworldmod.data.tags.ModAdvancements;
import net.dainplay.rpgworldmod.effect.ModEffects;
import net.dainplay.rpgworldmod.enchantment.ModEnchantments;
import net.dainplay.rpgworldmod.item.ModItems;
import net.dainplay.rpgworldmod.network.ClientManaData;
import net.dainplay.rpgworldmod.network.LoopSoundPacket;
import net.dainplay.rpgworldmod.network.ModMessages;
import net.dainplay.rpgworldmod.network.PlayerManaProvider;
import net.dainplay.rpgworldmod.network.UpdateItemTagMessage;
import net.dainplay.rpgworldmod.particle.ModParticles;
import net.dainplay.rpgworldmod.sounds.RPGSounds;
import net.minecraft.ChatFormatting;
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
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.world.item.FlintAndSteelItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.TntBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.LavaFluid;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class EmberScrollItem extends ScrollItem {
	private static final Map<Level, Map<UUID, EmberProjectileData>> activeProjectiles = new HashMap<>();


	private static final Map<Level, Map<UUID, PlayerUseData>> playerUseData = new HashMap<>();

	public EmberScrollItem(Properties pProperties) {
		super(pProperties);
	}

	@Override
	public String getTexture(ItemStack stack, Entity entity) {
		if (entity instanceof Player player && player.isUsingItem() && player.getUseItem() == stack) {
			return "textures/entity/spells/fire";
		}
		return "textures/entity/spells/spark";
	}

	@Override
	public float get1YOffset(ItemStack stack, Entity entity) {
		if (entity instanceof Player player && player.isUsingItem() && player.getUseItem() == stack) {
			return 0.25F;
		}
		return 0F;
	}

	@Override
	public float getZOffset(ItemStack stack, Entity entity) {
		if (entity instanceof Player player && player.isUsingItem() && player.getUseItem() == stack) {
			if (stack.getEnchantmentLevel(ModEnchantments.DESTRUCTION.get()) > 0) {
				return 0.1F;
			}
			if (stack.getEnchantmentLevel(ModEnchantments.RESTORATION.get()) > 0) {
				return 0.2F;
			}
			if (stack.getEnchantmentLevel(ModEnchantments.ALTERATION.get()) > 0) {
				return 0.2F;
			}
			if (stack.getEnchantmentLevel(ModEnchantments.ILLUSION.get()) > 0) {
				return 0.1F;
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
			if (stack.getEnchantmentLevel(ModEnchantments.RESTORATION.get()) > 0) {
				return 0F;
			}
			if (stack.getEnchantmentLevel(ModEnchantments.ALTERATION.get()) > 0) {
				return 0F;
			}
			if (stack.getEnchantmentLevel(ModEnchantments.NECROMANCY.get()) > 0) {
				return 0F;
			}
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
		if (stack.getEnchantmentLevel(ModEnchantments.RESTORATION.get()) > 0) {
			poseStack.mulPose(Axis.ZP.rotationDegrees(flip * 36.0F));
			poseStack.mulPose(Axis.XP.rotationDegrees(-7.0F));
			poseStack.mulPose(Axis.YP.rotationDegrees((-(float) Math.PI / 6F)));
			poseStack.translate(0F, 0.4F, 0F);
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
		if (stack.getEnchantmentLevel(ModEnchantments.NECROMANCY.get()) > 0) {
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
	public boolean highlightAnimateTarget(ItemStack stack, Player player) {
		if (stack.getEnchantmentLevel(ModEnchantments.ILLUSION.get()) > 0)
			return (player.isUsingItem() && player.getUseItemRemainingTicks() > 0 && player.getUseItem() == stack);
		return false;
	}

	@Override
	public PoseStack getEffectUsingPose(ItemStack stack, Player player, PoseStack poseStack, float flip) {
		if (stack.getEnchantmentLevel(ModEnchantments.DESTRUCTION.get()) > 0) {
			poseStack.translate(flip * 0.05F, 0.05F, -0.31F);
		}
		if (stack.getEnchantmentLevel(ModEnchantments.RESTORATION.get()) > 0) {
			poseStack.translate(flip * 0.35F, 0.5F, 0F);
		}
		if (stack.getEnchantmentLevel(ModEnchantments.ALTERATION.get()) > 0) {
			poseStack.translate(flip * 0.35F, 0.5F, 0F);
		}
		if (stack.getEnchantmentLevel(ModEnchantments.NECROMANCY.get()) > 0) {
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
			return 1;
		}
		return 2;
	}

	@Override
	public int getAnimationLength(ItemStack stack, Entity entity) {
		if (entity instanceof Player player && player.isUsingItem() && player.getUseItem() == stack) {
			return 32;
		}
		return 6;
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

			@Override
			public HumanoidModel.ArmPose getArmPose(LivingEntity entityLiving, InteractionHand hand, ItemStack itemStack) {
				if (!itemStack.isEmpty() && itemStack.getEnchantmentLevel(ModEnchantments.DESTRUCTION.get()) > 0) {
					if (entityLiving.isUsingItem() && entityLiving.getUseItemRemainingTicks() > 0 && entityLiving.getUsedItemHand() == hand) {
						return DESTRUCTION_POSE;
					}
				}
				if (!itemStack.isEmpty() && itemStack.getEnchantmentLevel(ModEnchantments.RESTORATION.get()) > 0) {
					if (entityLiving.isUsingItem() && entityLiving.getUseItemRemainingTicks() > 0 && entityLiving.getUsedItemHand() == hand) {
						return ACTIVE_USE_POSE;
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
				if (!itemStack.isEmpty() && itemStack.getEnchantmentLevel(ModEnchantments.NECROMANCY.get()) > 0) {
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
		if (EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.ALTERATION.get(), item) > 0) {
			return "4";
		}
		if (EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.ILLUSION.get(), item) > 0) {
			return "20";
		}
		if (EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.CONJURATION.get(), item) > 0) {
			return "10";
		}
		if (EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.NECROMANCY.get(), item) > 0) {
			return "2-20";
		}
		return "5";
	}

	@Override
	public int getManaCost(ItemStack item, Player player) {
		if (EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.DESTRUCTION.get(), item) > 0) {
			return 1;
		}
		if (EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.RESTORATION.get(), item) > 0) {
			if (player.isUsingItem() && player.getUseItemRemainingTicks() > 0 && player.getItemInHand(player.getUsedItemHand()) == item)
				return 1;
			else return 5;
		}
		if (EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.ALTERATION.get(), item) > 0) {
			return 1;
		}
		if (EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.ILLUSION.get(), item) > 0) {
			return 20;
		}
		if (EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.CONJURATION.get(), item) > 0) {
			return 10;
		}
		if (EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.NECROMANCY.get(), item) > 0) {
			if (player.isShiftKeyDown()) return 0;
			else return 2;
		}
		return 5;
	}

	public Component getManaCostAdditionalLine(ItemStack item) {
		if (EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.DESTRUCTION.get(), item) > 0) {
			return Component.translatable("tooltip.rpgworldmod.cost_per_second").withStyle(ChatFormatting.BLUE);
		}
		if (EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.RESTORATION.get(), item) > 0) {
			return Component.translatable("tooltip.rpgworldmod.plus_cost_per_second", 3).withStyle(ChatFormatting.BLUE);
		}
		if (EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.ALTERATION.get(), item) > 0) {
			return Component.translatable("tooltip.rpgworldmod.cost_per_second").withStyle(ChatFormatting.BLUE);
		}
		if (EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.CONJURATION.get(), item) > 0) {
			return Component.translatable("tooltip.rpgworldmod.plus_cost_per_second", "0-1.2").withStyle(ChatFormatting.BLUE);
		}
		return Component.literal("");
	}

	public static ItemStack createForEnchantment(EnchantmentInstance pInstance) {
		ItemStack itemstack = new ItemStack(ModItems.EMBER_SCROLL.get());
		itemstack.enchant(pInstance.enchantment, pInstance.level);
		return itemstack;
	}

	public static void setUseTime(ItemStack stack, int useTime) {
		CompoundTag tag = stack.getOrCreateTag();
		tag.putInt("PrevUseTime", getUseTime(stack));
		tag.putInt("UseTime", useTime);
	}

	public static int getUseTime(ItemStack stack) {
		CompoundTag compoundtag = stack.getTag();
		return compoundtag != null ? compoundtag.getInt("UseTime") : 0;
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		ItemStack itemstack = player.getItemInHand(hand);


		if (EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.DESTRUCTION.get(), itemstack) <= 0
				&& EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.RESTORATION.get(), itemstack) <= 0
				&& EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.ALTERATION.get(), itemstack) <= 0
				&& EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.ILLUSION.get(), itemstack) <= 0
				&& EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.CONJURATION.get(), itemstack) <= 0
				&& EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.NECROMANCY.get(), itemstack) <= 0) {
			return InteractionResultHolder.fail(itemstack);
		}

		if (!level.isClientSide) {
			if (EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.CONJURATION.get(), itemstack) > 0) {
				return InteractionResultHolder.pass(itemstack);
			}
			if (EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.NECROMANCY.get(), itemstack) > 0 && player.isShiftKeyDown()) {
				if (player.hasEffect(ModEffects.BURNOUT.get())) {
					player.removeEffect(ModEffects.BURNOUT.get());
					player.extinguishFire();
					player.level().playSound(null, player.blockPosition(), SoundEvents.GENERIC_EXTINGUISH_FIRE, SoundSource.PLAYERS, 0.7F, 1.6F + (player.getRandom().nextFloat() - player.getRandom().nextFloat()) * 0.4F);
				}
				player.getCooldowns().addCooldown(this, 15);
				return InteractionResultHolder.success(itemstack);
			}


			UUID playerId = player.getUUID();

			AtomicBoolean cir = new AtomicBoolean(false);

			if (!player.getAbilities().instabuild) {
				if (usesHealthInsteadOfMana(itemstack)) {
					if (Mth.ceil(player.getHealth()) < getManaCost(itemstack, player)) {
						cir.set(true);
					}
				} else {
					player.getCapability(PlayerManaProvider.PLAYER_MANA).ifPresent(mana -> {
						if (mana.getMana() < getManaCost(itemstack, player)) {
							cir.set(true);
							return;
						}
						if (EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.ILLUSION.get(), itemstack) <= 0)
							mana.reduceMana((ServerPlayer) player, getManaCost(itemstack, player));
					});

				}
			}

			if (cir.get()) {
				getPlayerUseData(level).remove(playerId);

				ModMessages.sendToNearbyPlayers(
						new LoopSoundPacket(player.getId(), false, itemstack),
						(ServerLevel) level,
						player.blockPosition(),
						64.0
				);
				return InteractionResultHolder.fail(itemstack);
			}

			getPlayerUseData(level).put(playerId, new PlayerUseData(playerId, level.getGameTime()));

			if (EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.DESTRUCTION.get(), itemstack) > 0) {
				level.playSound(null,
						player.getX(), player.getY(), player.getZ(),
						RPGSounds.SPELL_DESTRUCTION_EMBER_START.get(),
						SoundSource.PLAYERS, 1.0F, 1.0F
				);
				player.gameEvent(GameEvent.ENTITY_INTERACT, player);


				ModMessages.sendToNearbyPlayers(
						new LoopSoundPacket(player.getId(), true, itemstack),
						(ServerLevel) level,
						player.blockPosition(),
						64.0
				);
			}

			if (EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.RESTORATION.get(), itemstack) > 0) {
				level.playSound(null,
						player.getX(), player.getY(), player.getZ(),
						RPGSounds.SPELL_RESTORATION_START.get(),
						SoundSource.PLAYERS, 1.0F, 1.0F
				);
				player.gameEvent(GameEvent.ENTITY_INTERACT, player);


				ModMessages.sendToNearbyPlayers(
						new LoopSoundPacket(player.getId(), true, itemstack),
						(ServerLevel) level,
						player.blockPosition(),
						64.0
				);
			}

			if (EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.ALTERATION.get(), itemstack) > 0) {
				level.playSound(null,
						player.getX(), player.getY(), player.getZ(),
						RPGSounds.SPELL_ALTERATION_START.get(),
						SoundSource.PLAYERS, 1.0F, 1.0F
				);
				player.gameEvent(GameEvent.ENTITY_INTERACT, player);


				ModMessages.sendToNearbyPlayers(
						new LoopSoundPacket(player.getId(), true, itemstack),
						(ServerLevel) level,
						player.blockPosition(),
						64.0
				);
			}

			if (EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.ILLUSION.get(), itemstack) > 0) {
				level.playSound(null,
						player.getX(), player.getY(), player.getZ(),
						RPGSounds.SPELL_ILLUSION_START.get(),
						SoundSource.PLAYERS, 1.0F, 1.0F
				);


				ModMessages.sendToNearbyPlayers(
						new LoopSoundPacket(player.getId(), true, itemstack),
						(ServerLevel) level,
						player.blockPosition(),
						64.0
				);
			}

			if (EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.NECROMANCY.get(), itemstack) > 0) {
				level.playSound(null,
						player.getX(), player.getY(), player.getZ(),
						RPGSounds.SPELL_NECROMANCY_START.get(),
						SoundSource.PLAYERS, 1.0F, 1.0F
				);
				player.gameEvent(GameEvent.ENTITY_DAMAGE, player);


				ModMessages.sendToNearbyPlayers(
						new LoopSoundPacket(player.getId(), true, itemstack),
						(ServerLevel) level,
						player.blockPosition(),
						64.0
				);
			}

			player.startUsingItem(hand);
		} else {
			if (EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.CONJURATION.get(), itemstack) > 0) {
				return InteractionResultHolder.pass(itemstack);
			}
			if (EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.NECROMANCY.get(), itemstack) > 0 && player.isShiftKeyDown()) {
				return InteractionResultHolder.success(itemstack);
			}


			if (usesHealthInsteadOfMana(itemstack)) {
				if (!player.getAbilities().instabuild && Mth.ceil(player.getHealth()) < getManaCost(itemstack, player))
					return InteractionResultHolder.fail(itemstack);
			} else {
				if (!player.getAbilities().instabuild && ClientManaData.get() < getManaCost(itemstack, player))
					return InteractionResultHolder.fail(itemstack);
			}
			player.startUsingItem(hand);
		}

		return InteractionResultHolder.consume(itemstack);
	}

	@Override
	public InteractionResult useOn(UseOnContext context) {
		Level level = context.getLevel();
		Player player = context.getPlayer();
		ItemStack itemstack = context.getItemInHand();


		if (EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.CONJURATION.get(), itemstack) > 0) {
			if (level.isClientSide) {
				if (player != null && !player.getAbilities().instabuild &&
						ClientManaData.get() < getManaCost(itemstack, player)) {
					return InteractionResult.FAIL;
				}
			}


			InteractionResult interactionresult = this.placeCampfire(new BlockPlaceContext(context));


			if (!level.isClientSide && interactionresult.consumesAction() && player != null) {
				player.getCooldowns().addCooldown(this, 15);


				level.playSound(null, context.getClickedPos(),
						RPGSounds.SPELL_CONJURATION_START.get(),
						SoundSource.BLOCKS, 1.0F, (level.random.nextFloat() - level.random.nextFloat()) * 0.2F + 1.0F
				);
				player.gameEvent(GameEvent.ENTITY_INTERACT, player);
			}

			return interactionresult;
		}

		return InteractionResult.PASS;
	}

	private InteractionResult placeCampfire(BlockPlaceContext context) {
		if (context == null) {
			return InteractionResult.FAIL;
		} else {
			BlockPlaceContext blockplacecontext = this.updatePlacementContext(context);
			if (blockplacecontext == null) {
				return InteractionResult.FAIL;
			} else {
				BlockState blockstate = this.getPlacementState(blockplacecontext);
				if (blockstate == null) {
					return InteractionResult.FAIL;
				} else if (!this.placeBlock(blockplacecontext, blockstate)) {
					return InteractionResult.FAIL;
				} else {
					BlockPos blockpos = blockplacecontext.getClickedPos();
					Level level = blockplacecontext.getLevel();
					Player player = blockplacecontext.getPlayer();
					ItemStack itemstack = blockplacecontext.getItemInHand();
					BlockState blockstate1 = level.getBlockState(blockpos);


					if (blockstate1.is(ModBlocks.BOUND_CAMPFIRE.get())) {
						blockstate1 = this.updateBlockStateFromTag(blockpos, level, itemstack, blockstate1);


						blockstate1.getBlock().setPlacedBy(level, blockpos, blockstate1, player, itemstack);
						BlockEntity blockEntity = level.getBlockEntity(blockpos);
						if (blockEntity instanceof BoundCampfireBlockEntity boundCampfire) {
							if (player != null) {
								boundCampfire.setOwner(player);
								if (player instanceof ServerPlayer serverPlayer)
									ModAdvancements.SPELL_CONJURATION_EMBER_TRIGGER.trigger(serverPlayer);
							}
						}
					}

					SoundType soundtype = blockstate1.getSoundType(level, blockpos, context.getPlayer());
					level.playSound(player, blockpos, this.getPlaceSound(blockstate1, level, blockpos, context.getPlayer()),
							SoundSource.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);
					level.gameEvent(GameEvent.BLOCK_PLACE, blockpos, GameEvent.Context.of(player, blockstate1));

					return InteractionResult.sidedSuccess(level.isClientSide);
				}
			}
		}
	}


	protected net.minecraft.sounds.SoundEvent getPlaceSound(BlockState state, Level world, BlockPos pos, Player entity) {
		return state.getSoundType(world, pos, entity).getPlaceSound();
	}

	@Nullable
	public BlockPlaceContext updatePlacementContext(BlockPlaceContext context) {
		return context;
	}

	@Nullable
	protected BlockState getPlacementState(BlockPlaceContext context) {
		BlockState blockstate = ModBlocks.BOUND_CAMPFIRE.get().getStateForPlacement(context);
		return blockstate != null && this.canPlace(context, blockstate) ? blockstate : null;
	}

	private BlockState updateBlockStateFromTag(BlockPos pos, Level level, ItemStack stack, BlockState state) {
		return state;
	}

	protected boolean canPlace(BlockPlaceContext context, BlockState state) {
		Player player = context.getPlayer();
		CollisionContext collisioncontext = player == null ? CollisionContext.empty() : CollisionContext.of(player);

		return (state.canSurvive(context.getLevel(), context.getClickedPos())) &&
				context.getLevel().isUnobstructed(state, context.getClickedPos(), collisioncontext);
	}

	protected boolean placeBlock(BlockPlaceContext context, BlockState state) {
		Level level = context.getLevel();
		BlockPos blockpos = context.getClickedPos();
		Player player = context.getPlayer();


		if (!level.isClientSide && player != null && !player.getAbilities().instabuild) {
			AtomicBoolean hasEnoughMana = new AtomicBoolean(true);
			player.getCapability(PlayerManaProvider.PLAYER_MANA).ifPresent(mana -> {
				if (mana.getMana() < getManaCost(context.getItemInHand(), player)) {
					hasEnoughMana.set(false);
				} else {
					mana.reduceMana((ServerPlayer) player, getManaCost(context.getItemInHand(), player));
				}
			});

			if (!hasEnoughMana.get()) {
				return false;
			}
		}

		return level.setBlock(blockpos, state, 11);
	}

	public void igniteSelf(Player player, int healthToIgnite, ItemStack item) {
		player.getCooldowns().addCooldown(item.getItem(), 15);
		player.swing(player.getUsedItemHand());
		player.setSecondsOnFire(72000);
		MobEffectInstance burnout = new MobEffectInstance(ModEffects.BURNOUT.get(), -1, healthToIgnite - 1);
		burnout.setCurativeItems(new ArrayList<>());
		player.addEffect(burnout);
		if (player instanceof ServerPlayer serverPlayer)
			ModAdvancements.SPELL_NECROMANCY_EMBER_TRIGGER.trigger(serverPlayer);
		player.level().playSound(null,
				player.getX(), player.getY(), player.getZ(),
				RPGSounds.EMBER_GEM_IGNITE_ENTITY.get(),
				SoundSource.PLAYERS, 1.0F, 1.0F
		);
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
			MobEffectInstance illusion = new MobEffectInstance(ModEffects.BURN_ILLUSION.get(), 1200, 0);
			illusion.setCurativeItems(new ArrayList<>());
			target.addEffect(illusion);
			if (player instanceof ServerPlayer serverPlayer)
				ModAdvancements.SPELL_ILLUSION_EMBER_TRIGGER.trigger(serverPlayer);
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
				getPlayerUseData(level).remove(playerId);

				if (EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.DESTRUCTION.get(), stack) > 0) {
					level.playSound(null,
							player.getX(), player.getY(), player.getZ(),
							RPGSounds.SPELL_DESTRUCTION_EMBER_STOP.get(),
							SoundSource.PLAYERS, 1.0F, 1.0F
					);


					ModMessages.sendToNearbyPlayers(
							new LoopSoundPacket(player.getId(), false, stack),
							(ServerLevel) level,
							player.blockPosition(),
							64.0
					);
				}

				if (EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.ALTERATION.get(), stack) > 0) {
					level.playSound(null,
							player.getX(), player.getY(), player.getZ(),
							RPGSounds.SPELL_ALTERATION_STOP.get(),
							SoundSource.PLAYERS, 1.0F, 1.0F
					);


					ModMessages.sendToNearbyPlayers(
							new LoopSoundPacket(player.getId(), false, stack),
							(ServerLevel) level,
							player.blockPosition(),
							64.0
					);
				}

				if (EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.RESTORATION.get(), stack) > 0) {
					level.playSound(null,
							player.getX(), player.getY(), player.getZ(),
							RPGSounds.SPELL_RESTORATION_STOP.get(),
							SoundSource.PLAYERS, 1.0F, 1.0F
					);


					ModMessages.sendToNearbyPlayers(
							new LoopSoundPacket(player.getId(), false, stack),
							(ServerLevel) level,
							player.blockPosition(),
							64.0
					);

					player.extinguishFire();
				}

				if (EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.ILLUSION.get(), stack) > 0) {
					level.playSound(null,
							player.getX(), player.getY(), player.getZ(),
							RPGSounds.SPELL_ILLUSION_STOP.get(),
							SoundSource.PLAYERS, 1.0F, 1.0F
					);


					ModMessages.sendToNearbyPlayers(
							new LoopSoundPacket(player.getId(), false, stack),
							(ServerLevel) level,
							player.blockPosition(),
							64.0
					);
				}

				if (EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.NECROMANCY.get(), stack) > 0) {
					level.playSound(null,
							player.getX(), player.getY(), player.getZ(),
							RPGSounds.SPELL_NECROMANCY_STOP.get(),
							SoundSource.PLAYERS, 1.0F, 1.0F
					);


					ModMessages.sendToNearbyPlayers(
							new LoopSoundPacket(player.getId(), false, stack),
							(ServerLevel) level,
							player.blockPosition(),
							64.0
					);
				}
			} else {
				ModMessages.sendToServer(new UpdateItemTagMessage(player.getId(), stack));
			}
		}
		super.releaseUsing(stack, level, livingEntity, timeCharged);
	}

	@Override
	public int getUseDuration(ItemStack stack) {
		if (stack.getEnchantmentLevel(ModEnchantments.CONJURATION.get()) > 0) return 0;
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

	private static Map<UUID, EmberProjectileData> getActiveProjectiles(Level level) {
		return activeProjectiles.computeIfAbsent(level, k -> new HashMap<>());
	}


	private static class PlayerUseData {
		private final UUID playerId;
		private long startTime;
		private int useTicks;
		private int lastManaTick;
		private int lastProjectileTick;
		private int lastAlterationTick;
		private int lastAlterationRemovalTick;

		public PlayerUseData(UUID playerId, long startTime) {
			this.playerId = playerId;
			this.startTime = startTime;
			this.useTicks = 0;
			this.lastManaTick = 0;
			this.lastProjectileTick = -3;
			this.lastAlterationTick = -1;
			this.lastAlterationRemovalTick = -1;
		}

		public void tick() {
			useTicks++;
		}

		public boolean shouldConsumeMana(ItemStack item) {
			int tick = 4;
			if (item.getEnchantmentLevel(ModEnchantments.RESTORATION.get()) > 0) tick = 7;
			if (item.getEnchantmentLevel(ModEnchantments.ALTERATION.get()) > 0) tick = 5;
			if (item.getEnchantmentLevel(ModEnchantments.ILLUSION.get()) > 0 || item.getEnchantmentLevel(ModEnchantments.NECROMANCY.get()) > 0)
				return false;
			if (useTicks - lastManaTick >= tick) {
				lastManaTick = useTicks;
				return true;
			}
			return false;
		}

		public boolean shouldProcessAlteration(ItemStack item) {
			if (item.getEnchantmentLevel(ModEnchantments.ALTERATION.get()) > 0) {
				if (useTicks - lastAlterationTick >= 7) {
					lastAlterationTick = useTicks;
					return true;
				}
			}
			return false;
		}
	}


	private static class EmberProjectileData {
		private final UUID ownerId;
		private Vec3 position;
		private Vec3 velocity;
		private final long spawnTime;

		public EmberProjectileData(UUID ownerId, Vec3 position, Vec3 velocity, long spawnTime) {
			this.ownerId = ownerId;
			this.position = position;
			this.velocity = velocity;
			this.spawnTime = spawnTime;
		}
	}


	private static void stopPlayerUse(ServerLevel level, Player player, ItemStack usingItem) {
		if (player == null) return;
		player.stopUsingItem();


		ModMessages.sendToNearbyPlayers(
				new LoopSoundPacket(player.getId(), false, usingItem),
				level,
				player.blockPosition(),
				64.0
		);

		if (EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.DESTRUCTION.get(), usingItem) > 0) {
			level.playSound(null, player.getX(), player.getY(), player.getZ(),
					RPGSounds.SPELL_DESTRUCTION_EMBER_STOP.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
		}
		if (EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.ALTERATION.get(), usingItem) > 0) {
			level.playSound(null, player.getX(), player.getY(), player.getZ(),
					RPGSounds.SPELL_ALTERATION_STOP.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
		}
		if (EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.RESTORATION.get(), usingItem) > 0) {
			level.playSound(null, player.getX(), player.getY(), player.getZ(),
					RPGSounds.SPELL_RESTORATION_STOP.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
			player.extinguishFire();
		}
		if (EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.ILLUSION.get(), usingItem) > 0) {
			level.playSound(null, player.getX(), player.getY(), player.getZ(),
					RPGSounds.SPELL_ILLUSION_STOP.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
		}
		if (EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.NECROMANCY.get(), usingItem) > 0) {
			level.playSound(null, player.getX(), player.getY(), player.getZ(),
					RPGSounds.SPELL_NECROMANCY_STOP.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
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
				ItemStack usingItem = player.getUseItem();

				ModMessages.sendToNearbyPlayers(
						new LoopSoundPacket(player.getId(), false, usingItem),
						level,
						player.blockPosition(),
						64.0
				);
				if (usingItem.getItem() instanceof EmberScrollItem) {
					stopPlayerUse(level, player, usingItem);
				}
				continue;
			}

			ItemStack usingItem = player.getUseItem();
			if (!(usingItem.getItem() instanceof EmberScrollItem)) {
				levelPlayerUseData.remove(playerId);
				player.stopUsingItem();

				ModMessages.sendToNearbyPlayers(
						new LoopSoundPacket(player.getId(), false, usingItem),
						level,
						player.blockPosition(),
						64.0
				);
				continue;
			}


			if (EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.DESTRUCTION.get(), usingItem) <= 0
					&& EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.RESTORATION.get(), usingItem) <= 0
					&& EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.ALTERATION.get(), usingItem) <= 0
					&& EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.ILLUSION.get(), usingItem) <= 0
					&& EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.NECROMANCY.get(), usingItem) <= 0) {
				levelPlayerUseData.remove(playerId);
				stopPlayerUse(level, player, usingItem);
				continue;
			}


			if (EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.NECROMANCY.get(), usingItem) > 0 && player.isShiftKeyDown()) {
				levelPlayerUseData.remove(playerId);
				stopPlayerUse(level, player, usingItem);
				continue;
			}

			useData.tick();


			boolean continueUsing = true;

			if (useData.shouldConsumeMana(usingItem)) {
				if (!player.getAbilities().instabuild) {
					AtomicBoolean hasMana = new AtomicBoolean(true);
					player.getCapability(PlayerManaProvider.PLAYER_MANA).ifPresent(mana -> {
						if (usingItem.getItem() instanceof ManaCostItem manaCostItem) {
							if (mana.getMana() < manaCostItem.getManaCost(usingItem, player)) {
								hasMana.set(false);
							} else {
								mana.reduceMana((ServerPlayer) player, manaCostItem.getManaCost(usingItem, player));
							}
						}
					});
					if (!hasMana.get()) {
						continueUsing = false;
					}
				}
			}

			if (!continueUsing) {
				levelPlayerUseData.remove(playerId);
				stopPlayerUse(level, player, usingItem);
				continue;
			}


			if (!player.isShiftKeyDown() && useData.shouldProcessAlteration(usingItem)) {
				processLavaAlteration(level, player);
			}
			if (player.isShiftKeyDown() && EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.ALTERATION.get(), usingItem) > 0) {
				removeNearestLava(level, player);
			}

			if (EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.DESTRUCTION.get(), usingItem) > 0) {
				if (player instanceof ServerPlayer serverPlayer)
					ModAdvancements.SPELL_DESTRUCTION_EMBER_TRIGGER.trigger(serverPlayer);
				spawnProjectile(level, player);
			}
		}
	}

	private static void removeNearestLava(ServerLevel level, Player player) {
		BlockPos playerPos = player.blockPosition();
		int radius = 5;
		int radiusSquared = radius * radius;

		List<BlockPos> lavaSources = new ArrayList<>();
		List<BlockPos> flowingLavas = new ArrayList<>();


		for (int x = -radius; x <= radius; x++) {
			for (int y = -radius; y <= radius; y++) {
				for (int z = -radius; z <= radius; z++) {
					BlockPos checkPos = playerPos.offset(x, y, z);
					if (playerPos.distSqr(checkPos) <= radiusSquared) {
						FluidState fluidState = level.getFluidState(checkPos);
						if (fluidState.is(FluidTags.LAVA)) {
							if (fluidState.isSource()) {
								lavaSources.add(checkPos);
							} else {
								flowingLavas.add(checkPos);
							}
						}
					}
				}
			}
		}


		lavaSources.sort(Comparator.comparingDouble(playerPos::distSqr));
		flowingLavas.sort(Comparator.comparingDouble(playerPos::distSqr));

		boolean removedAny = false;


		if (!lavaSources.isEmpty()) {
			BlockPos sourcePos = lavaSources.get(0);
			removeLavaAt(level, sourcePos);
			removedAny = true;
		}


		if (!flowingLavas.isEmpty()) {
			BlockPos flowingPos = flowingLavas.get(0);
			removeLavaAt(level, flowingPos);
			removedAny = true;
		}


		if (removedAny && player instanceof ServerPlayer serverPlayer) {
			ModAdvancements.SPELL_ALTERATION_EMBER_TRIGGER.trigger(serverPlayer);
		}
	}

	private static void removeLavaAt(ServerLevel level, BlockPos pos) {
		level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
		level.sendParticles(ParticleTypes.SMOKE,
				pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
				5, 0.2, 0.2, 0.2, 0.05);
	}

	private static void processLavaAlteration(ServerLevel level, Player player) {
		BlockPos playerPos = player.blockPosition();
		int radius = 5;


		BlockPos nearestLavaPos = null;
		double nearestDistance = Double.MAX_VALUE;


		for (int x = -radius; x <= radius; x++) {
			for (int y = -radius; y <= radius; y++) {
				for (int z = -radius; z <= radius; z++) {
					BlockPos checkPos = playerPos.offset(x, y, z);
					double distance = playerPos.distSqr(checkPos);


					if (distance <= radius * radius) {
						BlockState state = level.getBlockState(checkPos);
						FluidState fluidState = level.getFluidState(checkPos);


						boolean isLavaSource = state.is(Blocks.LAVA) && fluidState.isSource();
						boolean isFlowingLava = fluidState.is(FluidTags.LAVA) && !fluidState.isSource();

						boolean proceedLavaSource = false;
						if (isLavaSource) {
							for (Direction direction : Direction.values()) {
								if (direction != Direction.UP) {
									BlockPos neighborPos = checkPos.relative(direction);


									if (canLavaSpreadTo(level, neighborPos)) {
										proceedLavaSource = true;
									}
								}
							}
						}

						if ((proceedLavaSource || isFlowingLava) && distance < nearestDistance) {
							nearestLavaPos = checkPos;
							nearestDistance = distance;
						}
					}
				}
			}
		}


		if (nearestLavaPos != null) {
			BlockState state = level.getBlockState(nearestLavaPos);
			FluidState fluidState = level.getFluidState(nearestLavaPos);


			if (fluidState.is(FluidTags.LAVA) && !fluidState.isSource()) {
				level.setBlockAndUpdate(nearestLavaPos, Fluids.LAVA.getSource().defaultFluidState().createLegacyBlock());
				if (player instanceof ServerPlayer serverPlayer)
					ModAdvancements.SPELL_ALTERATION_EMBER_TRIGGER.trigger(serverPlayer);


				level.sendParticles(ParticleTypes.LAVA,
						nearestLavaPos.getX() + 0.5, nearestLavaPos.getY() + 0.5, nearestLavaPos.getZ() + 0.5,
						5, 0.2, 0.2, 0.2, 0.01);
			} else if (state.is(Blocks.LAVA) && fluidState.isSource()) {
				spreadLavaInstantly(level, nearestLavaPos);
				if (player instanceof ServerPlayer serverPlayer)
					ModAdvancements.SPELL_ALTERATION_EMBER_TRIGGER.trigger(serverPlayer);
			}
		}
	}

	private static void spreadLavaInstantly(ServerLevel level, BlockPos sourcePos) {
		for (Direction direction : Direction.values()) {
			BlockPos neighborPos = sourcePos.relative(direction);

			if (direction == Direction.UP) {
				continue;
			}
			if (level.getBlockState(neighborPos).is(Blocks.LAVA)) {
				continue;
			}


			if (canLavaSpreadTo(level, neighborPos)) {
				if (level.getFluidState(sourcePos).getType() instanceof LavaFluid lava)
					lava.spreadTo(level, neighborPos, level.getBlockState(neighborPos), direction, lava.getFlowing(6, direction == Direction.DOWN));


				level.sendParticles(ParticleTypes.LAVA,
						neighborPos.getX() + 0.5, neighborPos.getY() + 0.5, neighborPos.getZ() + 0.5,
						3, 0.1, 0.1, 0.1, 0.005);
			}
		}
	}

	private static boolean canLavaSpreadTo(ServerLevel level, BlockPos pos) {
		BlockState state = level.getBlockState(pos);
		return state.isAir();
	}

	private static void spawnProjectile(ServerLevel level, Player player) {
		Vec3 lookAngle = player.getLookAngle();
		Vec3 startPos = player.getEyePosition().add(lookAngle.scale(0.5));

		EmberProjectileData projectile = new EmberProjectileData(
				player.getUUID(),
				startPos,
				lookAngle.scale(0.7),
				level.getGameTime()
		);

		getActiveProjectiles(level).put(UUID.randomUUID(), projectile);
	}

	public static void processProjectilesStatic(ServerLevel level) {
		Map<UUID, EmberProjectileData> levelActiveProjectiles = getActiveProjectiles(level);

		if (level == null || levelActiveProjectiles.isEmpty()) return;


		Map<UUID, EmberProjectileData> copy = new HashMap<>(levelActiveProjectiles);

		for (Map.Entry<UUID, EmberProjectileData> entry : copy.entrySet()) {
			UUID projectileId = entry.getKey();
			EmberProjectileData projectile = entry.getValue();

			if (level.getGameTime() - projectile.spawnTime >= 10) {
				levelActiveProjectiles.remove(projectileId);

				level.sendParticles(ParticleTypes.SMOKE,
						projectile.position.x, projectile.position.y, projectile.position.z,
						3, 0.2, 0.2, 0.2, 0.02);
				continue;
			}


			if (checkWaterContact(level, projectile.position)) {
				level.sendParticles(ParticleTypes.SMOKE,
						projectile.position.x, projectile.position.y, projectile.position.z,
						5, 0.2, 0.2, 0.2, 0.05);
				level.sendParticles(ParticleTypes.BUBBLE,
						projectile.position.x, projectile.position.y, projectile.position.z,
						3, 0.1, 0.1, 0.1, 0.1);

				level.playSound(null,
						projectile.position.x, projectile.position.y, projectile.position.z,
						RPGSounds.EMBER_GEM_EXTINGUISH.get(), SoundSource.NEUTRAL,
						0.3F, 1.0F);
				levelActiveProjectiles.remove(projectileId);
				continue;
			}


			projectile.position = projectile.position.add(projectile.velocity);


			if (checkCollisions(level, projectile, projectileId)) {
				levelActiveProjectiles.remove(projectileId);
				continue;
			}


			if (level.getGameTime() - projectile.spawnTime <= 1) level.sendParticles(ParticleTypes.FLAME,
					projectile.position.x, projectile.position.y, projectile.position.z,
					1, 0.1, 0.1, 0.1, 0.01);
			else level.sendParticles(ModParticles.FLAMES.get(),
					projectile.position.x, projectile.position.y, projectile.position.z,
					1, 0.1, 0.1, 0.1, 0.01);


			level.sendParticles(ParticleTypes.SMOKE,
					projectile.position.x, projectile.position.y, projectile.position.z,
					1, 0.05, 0.05, 0.05, 0.005);
		}
	}


	private static boolean checkWaterContact(Level level, Vec3 position) {
		BlockPos pos = new BlockPos(
				(int) Math.floor(position.x),
				(int) Math.floor(position.y),
				(int) Math.floor(position.z)
		);


		FluidState fluidState = level.getFluidState(pos);
		if (fluidState.is(FluidTags.WATER)) {
			return true;
		}


		for (int dx = -1; dx <= 1; dx++) {
			for (int dy = -1; dy <= 1; dy++) {
				for (int dz = -1; dz <= 1; dz++) {
					BlockPos checkPos = pos.offset(dx, dy, dz);
					FluidState nearbyFluid = level.getFluidState(checkPos);
					if (nearbyFluid.is(FluidTags.WATER)) {
						double distance = position.distanceTo(
								new Vec3(checkPos.getX() + 0.5, checkPos.getY() + 0.5, checkPos.getZ() + 0.5)
						);
						if (distance < 1.0) {
							return true;
						}
					}
				}
			}
		}

		return false;
	}


	private static boolean checkCollisions(Level level, EmberProjectileData projectile, UUID projectileId) {
		Vec3 startPos = projectile.position.subtract(projectile.velocity);
		Vec3 endPos = projectile.position.add(projectile.velocity);

		BlockHitResult blockHit = level.clip(new ClipContext(
				startPos, endPos,
				ClipContext.Block.COLLIDER,
				ClipContext.Fluid.ANY,
				null
		));

		if (blockHit.getType() != HitResult.Type.MISS) {
			BlockPos hitPos = blockHit.getBlockPos();
			BlockState hitState = level.getBlockState(hitPos);


			if (level.getFluidState(hitPos).is(FluidTags.WATER)) {
				level.playSound(null, hitPos, RPGSounds.EMBER_GEM_EXTINGUISH.get(),
						SoundSource.BLOCKS, 0.3F, 1.0F);

				if (level instanceof ServerLevel serverLevel) {
					serverLevel.sendParticles(ParticleTypes.SMOKE,
							hitPos.getX() + 0.5, hitPos.getY() + 0.5, hitPos.getZ() + 0.5,
							10, 0.3, 0.3, 0.3, 0.05);
				}
				return true;
			}


			if (hitState.is(ModBlocks.ARBOR_FUEL_BLOCK.get())) {
				BlockState fireState = BaseFireBlock.getState(level, hitPos);
				if (BaseFireBlock.canBePlacedAt(level, hitPos, Direction.UP)) {
					level.setBlockAndUpdate(hitPos, fireState);
				} else level.setBlockAndUpdate(hitPos, Blocks.AIR.defaultBlockState());
				return true;
			}

			if (hitState.getBlock() instanceof TntBlock tnt) {
				if (level instanceof ServerLevel serverLevel) {
					SmallFireball fireProjectile = new SmallFireball(
							serverLevel,
							projectile.position.x,
							projectile.position.y,
							projectile.position.z,
							projectile.velocity.x,
							projectile.velocity.y,
							projectile.velocity.z
					);


					if (projectile.ownerId != null) {
						Entity owner = serverLevel.getEntity(projectile.ownerId);
						if (owner != null) {
							fireProjectile.setOwner(owner);
						}
					}


					fireProjectile.setSecondsOnFire(100);


					tnt.onProjectileHit(level, hitState, blockHit, fireProjectile);
				}
				return true;
			}


			int flammability = Math.max(100 - hitState.getFlammability(level, hitPos, blockHit.getDirection()), 0);
			if (flammability < 100) {
				long timeAlive = 10 - ((level.getGameTime() - projectile.spawnTime));
				double requiredFlammability = timeAlive * 12;

				if (requiredFlammability >= flammability) {
					level.destroyBlock(hitPos, false);

					BlockState fireState = BaseFireBlock.getState(level, hitPos);
					if (BaseFireBlock.canBePlacedAt(level, hitPos, Direction.UP)) {
						level.setBlockAndUpdate(hitPos, fireState);
					} else level.setBlockAndUpdate(hitPos, Blocks.AIR.defaultBlockState());
					return false;
				}

			}


			ItemStack flintAndSteel = new ItemStack(Items.FLINT_AND_STEEL);


			if (level instanceof ServerLevel serverLevel) {
				FakePlayer fakePlayer = FakePlayerFactory.get(serverLevel, new GameProfile(UUID.randomUUID(), "FakePlayer"));


				fakePlayer.setPos(hitPos.getX(), hitPos.getY(), hitPos.getZ());


				fakePlayer.setYRot(blockHit.getDirection().toYRot());
				fakePlayer.setXRot((float) Math.toDegrees(blockHit.getDirection().toYRot()));


				fakePlayer.setItemInHand(InteractionHand.MAIN_HAND, flintAndSteel.copy());


				UseOnContext context = new UseOnContext(fakePlayer, InteractionHand.MAIN_HAND, blockHit);


				InteractionResult useResult = InteractionResult.PASS;
				if (flintAndSteel.getItem() instanceof FlintAndSteelItem flintAndSteelItem) {
					useResult = flintAndSteelItem.useOn(context);
				}


				fakePlayer.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);


				if (useResult.consumesAction()) {
					return true;
				}
			}


			BlockPos firePos = hitPos.relative(blockHit.getDirection());

			if (BaseFireBlock.canBePlacedAt(level, firePos, Direction.UP)) {
				level.setBlockAndUpdate(firePos, BaseFireBlock.getState(level, firePos));
			}
			return true;
		}


		List<Entity> entities = level.getEntities(null,
				new net.minecraft.world.phys.AABB(startPos, endPos).inflate(0.5));

		for (Entity entity : entities) {
			if (level instanceof ServerLevel serverLevel) {
				Entity owner = serverLevel.getEntity(projectile.ownerId);
				SmallFireball fakeFireball = new SmallFireball(
						level,
						projectile.position.x,
						projectile.position.y,
						projectile.position.z,
						projectile.velocity.x,
						projectile.velocity.y,
						projectile.velocity.z
				);
				fakeFireball.setOwner(owner);
				fakeFireball.setSecondsOnFire(1);
				if (entity instanceof LivingEntity livingEntity &&
						!entity.getUUID().equals(projectile.ownerId) && !entity.fireImmune()) {
					livingEntity.setSecondsOnFire(5);
					if (owner != null)
						entity.hurt(owner.damageSources().fireball(fakeFireball, owner), 2F);
					return false;
				}
			}
		}

		return false;
	}

	public String getFirstPredicate(ItemStack item) {
		return Minecraft.getInstance().options.keyShift.getKey().getDisplayName().getString();
	}

	public String getSecondPredicate(ItemStack item) {
		return Minecraft.getInstance().options.keyAttack.getKey().getDisplayName().getString();
	}

	public boolean hasControls(ItemStack item) {
		return item.getEnchantmentLevel(ModEnchantments.ILLUSION.get()) > 0
				|| item.getEnchantmentLevel(ModEnchantments.NECROMANCY.get()) > 0;
	}
}