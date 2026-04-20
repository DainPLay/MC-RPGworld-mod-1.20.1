package net.dainplay.rpgworldmod.block.entity.custom;

import net.dainplay.rpgworldmod.block.entity.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class DirectionalFlowerBlockEntity extends BlockEntity {
	public DirectionalFlowerBlockEntity(BlockPos pWorldPosition, BlockState pBlockState) {
		super(ModBlockEntities.DIRECTIONAL_FLOWER_BLOCK_ENTITY.get(), pWorldPosition, pBlockState);
	}
}
