package net.dainplay.rpgworldmod.block.entity.custom;

import net.dainplay.rpgworldmod.block.custom.TreeHollowBlock;
import net.dainplay.rpgworldmod.block.entity.ModBlockEntities;
import net.dainplay.rpgworldmod.sounds.RPGSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.Clearable;
import net.minecraft.world.Container;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.JukeboxBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.ticks.ContainerSingleItem;
import org.antlr.runtime.tree.Tree;

import javax.annotation.Nullable;
import java.util.Objects;

public class TreeHollowBlockEntity extends BlockEntity implements Clearable, ContainerSingleItem {
        public final NonNullList<ItemStack> items = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);

        public TreeHollowBlockEntity(BlockPos pWorldPosition, BlockState pBlockState) {
            super(ModBlockEntities.TREE_HOLLOW_BLOCK_ENTITY.get(), pWorldPosition, pBlockState);
        }

        public void load(CompoundTag pTag) {
            super.load(pTag);
            if (pTag.contains("ContentItem", 10)) {
                this.items.set(0, ItemStack.of(pTag.getCompound("ContentItem")));
            }

        }

        protected void saveAdditional(CompoundTag pTag) {
            super.saveAdditional(pTag);
            if (!this.getFirstItem().isEmpty()) {
                pTag.put("ContentItem", this.getFirstItem().save(new CompoundTag()));
            }

        }

    private void setHasContentBlockState(@Nullable Entity pEntity, boolean pHasContents) {
        if (this.level.getBlockState(this.getBlockPos()) == this.getBlockState()) {
            this.level.setBlock(this.getBlockPos(), this.getBlockState().setValue(TreeHollowBlock.HAS_CONTENTS, Boolean.valueOf(pHasContents)), 2);
            this.level.gameEvent(GameEvent.BLOCK_CHANGE, this.getBlockPos(), GameEvent.Context.of(pEntity, this.getBlockState()));
        }

    }

    public ItemStack getItem(int pSlot) {
        return this.items.get(pSlot);
    }

    public ItemStack removeItem(int pSlot, int pAmount) {
        ItemStack itemstack = Objects.requireNonNullElse(this.items.get(pSlot), ItemStack.EMPTY);
        this.items.set(pSlot, ItemStack.EMPTY);
        if (!itemstack.isEmpty()) {
            this.setHasContentBlockState((Entity)null, false);
        }

        return itemstack;
    }

    public void setItem(int pSlot, ItemStack pStack) {
        if (this.level != null) {
            this.items.set(pSlot, pStack);
            this.level.playSound((Player)null, this.getBlockPos(), RPGSounds.TREE_HOLLOW_PUT.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
            this.setHasContentBlockState((Entity)null, true);
        }

    }

    @Override
    public boolean stillValid(Player pPlayer) {
        return Container.stillValidBlockEntity(this, pPlayer);
    }

    public boolean canPlaceItem(int pIndex, ItemStack pStack) {
        return this.getItem(pIndex).isEmpty();
    }
    public boolean canTakeItem(Container pTarget, int pIndex, ItemStack pStack) {
        return false;
    }
}
