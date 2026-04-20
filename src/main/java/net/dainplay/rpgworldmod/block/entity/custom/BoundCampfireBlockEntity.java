package net.dainplay.rpgworldmod.block.entity.custom;

import net.dainplay.rpgworldmod.block.entity.ModBlockEntities;
import net.dainplay.rpgworldmod.network.PlayerManaProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.Containers;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.CampfireBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.UUID;

public class BoundCampfireBlockEntity extends CampfireBlockEntity {
	private boolean shouldRenderBeams = true;
	private int beamTick = 0;
	private boolean hasInitialBeamPlayed = false;


	@Nullable
	private UUID ownerUUID;
	private int playerDistanceCheckTimer = 0;
	private int noCookingTimer = 0;
	private boolean hasOwner = false;
	private final RecipeManager.CachedCheck<Container, SmeltingRecipe> quickCheck = RecipeManager.createCheck(RecipeType.SMELTING);

	public BoundCampfireBlockEntity(BlockPos pPos, BlockState pBlockState) {
		super(pPos, pBlockState);
	}

	@Override
	public BlockEntityType<?> getType() {
		return ModBlockEntities.BOUND_CAMPFIRE_BLOCK_ENTITY.get();
	}

	public Optional<SmeltingRecipe> getFurnaceRecipe(ItemStack stack) {
		return this.getItems().stream().noneMatch(ItemStack::isEmpty) ? Optional.empty() : this.quickCheck.getRecipeFor(new SimpleContainer(stack), this.level);
	}


	public static void SmeltTick(Level pLevel, BlockPos pPos, BlockState pState, BoundCampfireBlockEntity pBlockEntity) {
		boolean flag = false;

		for (int i = 0; i < pBlockEntity.getItems().size(); ++i) {
			ItemStack itemstack = pBlockEntity.getItems().get(i);
			if (!itemstack.isEmpty()) {
				flag = true;
				pBlockEntity.cookingProgress[i]++;
				if (pBlockEntity.cookingProgress[i] % 66 == 0 && pBlockEntity.hasOwner && pBlockEntity.ownerUUID != null) {
					Player owner = pBlockEntity.getOwner(pLevel);
					if (owner != null && owner instanceof ServerPlayer serverPlayer && !owner.getAbilities().instabuild) {
						serverPlayer.getCapability(PlayerManaProvider.PLAYER_MANA).ifPresent(mana -> {
							mana.reduceMana(serverPlayer, 1);
						});
					}
				}
				if (pBlockEntity.cookingProgress[i] >= pBlockEntity.cookingTime[i]) {
					Container container = new SimpleContainer(itemstack);
					ItemStack itemstack1 = pBlockEntity.quickCheck.getRecipeFor(container, pLevel).map((p_270054_) -> {
						return p_270054_.assemble(container, pLevel.registryAccess());
					}).orElse(itemstack);
					if (itemstack1.isItemEnabled(pLevel.enabledFeatures())) {
						Containers.dropItemStack(pLevel, (double) pPos.getX(), (double) pPos.getY(), (double) pPos.getZ(), itemstack1);
						pBlockEntity.getItems().set(i, ItemStack.EMPTY);
						pLevel.sendBlockUpdated(pPos, pState, pState, 3);
						pLevel.gameEvent(GameEvent.BLOCK_CHANGE, pPos, GameEvent.Context.of(pState));
					}
				}
			}
		}

		if (flag) {
			setChanged(pLevel, pPos, pState);
		}

	}


	public void setOwner(Player player) {
		this.ownerUUID = player.getUUID();
		this.hasOwner = true;
		setChanged();
	}

	@Nullable
	public UUID getOwnerUUID() {
		return ownerUUID;
	}

	@Nullable
	public Player getOwner(Level level) {
		if (ownerUUID == null || level.isClientSide) {
			return null;
		}
		return ((ServerLevel) level).getServer().getPlayerList().getPlayer(ownerUUID);
	}

	@Override
	public void load(CompoundTag tag) {
		super.load(tag);
		if (tag.contains("HasInitialBeamPlayed")) {
			this.hasInitialBeamPlayed = tag.getBoolean("HasInitialBeamPlayed");
		}
		if (tag.contains("OwnerUUID")) {
			this.ownerUUID = tag.getUUID("OwnerUUID");
			this.hasOwner = true;
		}
		if (tag.contains("PlayerDistanceCheckTimer")) {
			this.playerDistanceCheckTimer = tag.getInt("PlayerDistanceCheckTimer");
		}
		if (tag.contains("NoCookingTimer")) {
			this.noCookingTimer = tag.getInt("NoCookingTimer");
		}


		if (this.hasInitialBeamPlayed) {
			this.shouldRenderBeams = false;
			this.beamTick = 0;
		}
	}

	@Override
	protected void saveAdditional(CompoundTag tag) {
		super.saveAdditional(tag);
		tag.putBoolean("HasInitialBeamPlayed", this.hasInitialBeamPlayed);
		if (ownerUUID != null) {
			tag.putUUID("OwnerUUID", ownerUUID);
		}
		tag.putInt("PlayerDistanceCheckTimer", playerDistanceCheckTimer);
		tag.putInt("NoCookingTimer", noCookingTimer);
	}

	public static void tick(Level pLevel, BlockPos pPos, BlockState pState, BoundCampfireBlockEntity blockEntity) {
		if (pLevel.isClientSide) {
			if (blockEntity.shouldRenderBeams) {
				blockEntity.beamTick++;

				if (blockEntity.beamTick >= 40) {
					blockEntity.beamTick = 0;
					blockEntity.shouldRenderBeams = false;
					blockEntity.hasInitialBeamPlayed = true;


					blockEntity.setChanged();
				}
			}

			if (pState.getValue(CampfireBlock.LIT)) {
				particleTick(pLevel, pPos, pState, blockEntity);
			}
		} else {
			boolean shouldDestroy = false;

			if (blockEntity.hasOwner && blockEntity.ownerUUID != null) {
				Player owner = blockEntity.getOwner(pLevel);

				if (owner != null && !owner.getAbilities().instabuild) {
					owner.getCapability(PlayerManaProvider.PLAYER_MANA).ifPresent(mana -> {
						if (mana.getMana() <= 0) {
							pLevel.destroyBlock(pPos, false);
						}
					});
				}

				if (owner instanceof ServerPlayer serverPlayer) {
					double distanceSqr = serverPlayer.distanceToSqr(pPos.getX() + 0.5, pPos.getY() + 0.5, pPos.getZ() + 0.5);

					if (distanceSqr <= 64 * 64) {
						blockEntity.playerDistanceCheckTimer = 0;
					} else {
						blockEntity.playerDistanceCheckTimer++;

						if (blockEntity.playerDistanceCheckTimer >= 1200) {
							shouldDestroy = true;
						}
					}
				} else {
					blockEntity.playerDistanceCheckTimer++;
					if (blockEntity.playerDistanceCheckTimer >= 1200) {
						shouldDestroy = true;
					}
				}
			} else {
				shouldDestroy = true;
			}

			boolean hasItems = false;
			for (int i = 0; i < blockEntity.getItems().size(); i++) {
				if (!blockEntity.getItems().get(i).isEmpty()) {
					hasItems = true;
					blockEntity.noCookingTimer = 0;
					break;
				}
			}

			if (!hasItems) {
				blockEntity.noCookingTimer++;

				if (blockEntity.noCookingTimer >= 2400) {
					shouldDestroy = true;
				}
			}

			if (shouldDestroy) {
				pLevel.destroyBlock(pPos, false);
				return;
			}

			blockEntity.setChanged();

			if (pState.getValue(CampfireBlock.LIT)) {
				SmeltTick(pLevel, pPos, pState, blockEntity);
			} else {
				pLevel.destroyBlock(pPos, false);
			}
		}
	}

	public boolean shouldRenderBeams() {
		return shouldRenderBeams;
	}

	public int getBeamTick() {
		return beamTick;
	}

	public float getBeamProgress(float partialTick) {
		if (!shouldRenderBeams) return 0f;

		float totalTicks = 40f;
		float currentTick = beamTick + partialTick;
		float progress = currentTick / totalTicks;

		if (progress < 0.25f) {
			return progress * 4f;
		} else {
			return 1f - ((progress - 0.25f) / 0.75f);
		}
	}

	public float getRotationAngle(float partialTick) {
		if (!shouldRenderBeams) return 0f;

		float currentTick = beamTick + partialTick;
		return (currentTick / 40f) * 360f;
	}
}