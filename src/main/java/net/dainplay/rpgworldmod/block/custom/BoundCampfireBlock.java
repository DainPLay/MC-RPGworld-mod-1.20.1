package net.dainplay.rpgworldmod.block.custom;

import net.dainplay.rpgworldmod.block.entity.ModBlockEntities;
import net.dainplay.rpgworldmod.block.entity.custom.BoundCampfireBlockEntity;
import net.dainplay.rpgworldmod.enchantment.ModEnchantments;
import net.dainplay.rpgworldmod.item.ModItems;
import net.dainplay.rpgworldmod.particle.ModParticles;
import net.dainplay.rpgworldmod.sounds.RPGSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.UUID;

public class BoundCampfireBlock extends CampfireBlock {
	public BoundCampfireBlock(boolean pSpawnParticles, int pFireDamage, Properties pProperties) {
		super(pSpawnParticles, pFireDamage, pProperties);
	}

	@Override
	public ItemStack getCloneItemStack(BlockGetter pLevel, BlockPos pPos, BlockState pState) {
		ItemStack scroll = ModItems.EMBER_SCROLL.get().getDefaultInstance();
		scroll.enchant(ModEnchantments.CONJURATION.get(), 1);
		return scroll;
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
		return new BoundCampfireBlockEntity(pPos, pState);
	}

	@Nullable
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
		return createTickerHelper(type, ModBlockEntities.BOUND_CAMPFIRE_BLOCK_ENTITY.get(), BoundCampfireBlockEntity::tick);
	}

	@Override
	public void attack(BlockState state, Level level, BlockPos pos, Player player) {
		if (!level.isClientSide) {
			BlockEntity blockEntity = level.getBlockEntity(pos);
			if (blockEntity instanceof BoundCampfireBlockEntity boundCampfire) {
				UUID ownerUUID = boundCampfire.getOwnerUUID();
				if (ownerUUID != null && ownerUUID.equals(player.getUUID())) {
					ItemStack mainHand = player.getMainHandItem();
					if (mainHand.getItem() == ModItems.EMBER_SCROLL.get() && mainHand.getEnchantmentLevel(ModEnchantments.CONJURATION.get()) > 0) {
						level.destroyBlock(pos, false);
					}
				}
			}
		}
		super.attack(state, level, pos, player);
	}

	@Override
	public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
		if (!state.is(newState.getBlock())) {
			if (!level.isClientSide) {
				level.playSound(null, pos, RPGSounds.SPELL_CONJURATION_STOP.get(), SoundSource.BLOCKS, 1F, (level.random.nextFloat() - level.random.nextFloat()) * 0.2F + 1.0F);
				((ServerLevel) level).sendParticles(ModParticles.SUMMON_REVOKE.get(), pos.getX() + 0.5F, pos.getY() + 0.5F, pos.getZ() + 0.5F, 1, 0.0D, 0.0D, 0.0D, 0.0D);
			}
		}
		super.onRemove(state, level, pos, newState, isMoving);
	}

	@Override
	public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
		BlockEntity blockentity = pLevel.getBlockEntity(pPos);
		if (blockentity instanceof BoundCampfireBlockEntity boundCampfireblockentity) {
			ItemStack itemstack = pPlayer.getItemInHand(pHand);
			Optional<SmeltingRecipe> optional = boundCampfireblockentity.getFurnaceRecipe(itemstack);
			if (optional.isPresent()) {
				if (!pLevel.isClientSide && boundCampfireblockentity.placeFood(pPlayer, pPlayer.getAbilities().instabuild ? itemstack.copy() : itemstack, optional.get().getCookingTime())) {
					pPlayer.awardStat(Stats.INTERACT_WITH_CAMPFIRE);
					return InteractionResult.SUCCESS;
				}

				return InteractionResult.CONSUME;
			}
		}

		return InteractionResult.PASS;
	}
}