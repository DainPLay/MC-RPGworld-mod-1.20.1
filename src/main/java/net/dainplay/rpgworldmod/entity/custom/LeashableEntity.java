package net.dainplay.rpgworldmod.entity.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.UUID;


public class LeashableEntity {
	private Entity entity;
	@Nullable
	private Entity leashHolder;
	private int delayedLeashHolderId;
	@Nullable
	private CompoundTag leashInfoTag;
	@Nullable
	private BlockPos leashFencePos;

	public LeashableEntity(Entity entity) {
		this.entity = entity;
	}

	public void tickLeash() {
		if (this.leashInfoTag != null) {
			this.restoreLeashFromSave();
		}

		if (this.leashHolder != null) {
			if (!this.entity.isAlive() || !this.leashHolder.isAlive()) {
				this.dropLeash(true, true);
			} else {
				double distance = this.entity.distanceToSqr(this.leashHolder);
				if (distance > 100.0D) {
					this.dropLeash(true, true);
				}
			}
		}


		if (this.leashFencePos != null) {
			BlockState fenceState = this.entity.level().getBlockState(this.leashFencePos);
			if (!fenceState.isSolid() || !this.isValidFenceAttachment(this.leashFencePos)) {
				this.dropLeash(true, false);
			}
		}
	}

	private void restoreLeashFromSave() {
		if (this.leashInfoTag != null && this.entity.level() instanceof net.minecraft.server.level.ServerLevel) {
			net.minecraft.server.level.ServerLevel serverLevel = (net.minecraft.server.level.ServerLevel) this.entity.level();

			if (this.leashInfoTag.hasUUID("UUID")) {
				UUID uuid = this.leashInfoTag.getUUID("UUID");
				Entity entity = serverLevel.getEntity(uuid);
				if (entity != null) {
					this.setLeashedTo(entity, true);
					return;
				}
			} else if (this.leashInfoTag.contains("X", 99) && this.leashInfoTag.contains("Y", 99) && this.leashInfoTag.contains("Z", 99)) {
				BlockPos pos = NbtUtils.readBlockPos(this.leashInfoTag);
				if (this.isValidFenceAttachment(pos)) {
					this.leashFencePos = pos;
				} else {
					this.leashFencePos = null;
				}
				return;
			}

			this.leashInfoTag = null;
		}
	}

	public void dropLeash(boolean broadcastPacket, boolean dropItem) {
		this.leashHolder = null;
		this.leashFencePos = null;
		this.leashInfoTag = null;

		if (dropItem && !this.entity.level().isClientSide) {
		}
	}

	public void setLeashedTo(Entity entity, boolean broadcast) {
		this.leashHolder = entity;
		this.leashFencePos = null;
	}

	public void setLeashedToFence(BlockPos fencePos, boolean broadcast) {
		this.leashHolder = null;
		this.leashFencePos = fencePos;
	}

	@Nullable
	public Entity getLeashHolder() {
		if (this.leashHolder == null && this.delayedLeashHolderId != 0 && this.entity.level().isClientSide) {
			this.leashHolder = this.entity.level().getEntity(this.delayedLeashHolderId);
		}
		return this.leashHolder;
	}

	@Nullable
	public BlockPos getLeashFencePos() {
		return leashFencePos;
	}

	public boolean isLeashed() {
		return this.leashHolder != null || this.leashFencePos != null;
	}

	public boolean isLeashedToFence() {
		return this.leashFencePos != null;
	}

	public boolean isLeashedToEntity() {
		return this.leashHolder != null;
	}

	public void readAdditionalSaveData(CompoundTag compound) {
		if (compound.contains("Leash", 10)) {
			this.leashInfoTag = compound.getCompound("Leash");
		}
	}

	public void addAdditionalSaveData(CompoundTag compound) {
		if (this.leashHolder != null) {
			CompoundTag leashTag = new CompoundTag();
			leashTag.putUUID("UUID", this.leashHolder.getUUID());
			compound.put("Leash", leashTag);
		} else if (this.leashFencePos != null) {
			CompoundTag fenceTag = NbtUtils.writeBlockPos(this.leashFencePos);
			compound.put("Leash", fenceTag);
		} else if (this.leashInfoTag != null) {
			compound.put("Leash", this.leashInfoTag.copy());
		}
	}

	public boolean isValidFenceAttachment(BlockPos fencePos) {
		if (fencePos == null) return false;

		BlockPos entityPos = this.entity.blockPosition();


		if (fencePos.getX() != entityPos.getX() || fencePos.getZ() != entityPos.getZ()) {
			return false;
		}


		if (fencePos.getY() - entityPos.getY() > 7) {
			return false;
		}


		if (fencePos.getY() <= entityPos.getY()) {
			return false;
		}


		Level level = this.entity.level();
		for (int y = entityPos.getY() + 1; y < fencePos.getY(); y++) {
			BlockPos checkPos = new BlockPos(entityPos.getX(), y, entityPos.getZ());
			if (!level.getBlockState(checkPos).isAir()) {
				return false;
			}
		}

		return true;
	}
}