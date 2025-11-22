package net.dainplay.rpgworldmod.block.entity.custom;

import net.dainplay.rpgworldmod.block.entity.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Clearable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class FairapierWiltedPlantBlockEntity extends BlockEntity implements Clearable {
        private ItemStack item = ItemStack.EMPTY;

        public FairapierWiltedPlantBlockEntity(BlockPos pWorldPosition, BlockState pBlockState) {
            super(ModBlockEntities.FAIRAPIER_WILTED_PLANT_BLOCK_ENTITY.get(), pWorldPosition, pBlockState);
        }

        public void load(CompoundTag pTag) {
            super.load(pTag);
            if (pTag.contains("ContentItem", 10)) {
                this.setItem(ItemStack.of(pTag.getCompound("ContentItem")));
            }

        }

        protected void saveAdditional(CompoundTag pTag) {
            super.saveAdditional(pTag);
            if (!this.getItem().isEmpty()) {
                pTag.put("ContentItem", this.getItem().save(new CompoundTag()));
            }

        }

        public ItemStack getItem() {
            return this.item;
        }

        public void setItem(ItemStack item) {
            this.item = item;
            this.setChanged();
        }

        public void clearContent() {
            this.setItem(ItemStack.EMPTY);
        }
    }
