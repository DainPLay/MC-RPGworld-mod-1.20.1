package net.dainplay.rpgworldmod.features;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.dainplay.rpgworldmod.RPGworldMod;
import net.dainplay.rpgworldmod.block.custom.TreeHollowBlock;
import net.dainplay.rpgworldmod.block.entity.ModBlockEntities;
import net.dainplay.rpgworldmod.block.entity.custom.TreeHollowBlockEntity;
import net.dainplay.rpgworldmod.item.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecorator;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecoratorType;

public class BibbitHollowTrunkDecorator extends TreeDecorator {
	public static final Codec<BibbitHollowTrunkDecorator> CODEC = RecordCodecBuilder.create(
			instance -> instance.group(
					Codec.intRange(0, 64).fieldOf("placement_count").forGetter(o -> o.count),
					Codec.floatRange(0f, 1f).fieldOf("probability_of_placement").forGetter(o -> o.probability),
					BlockStateProvider.CODEC.fieldOf("deco_provider").forGetter(o -> o.decoration)
			).apply(instance, BibbitHollowTrunkDecorator::new)
	);

	private final int count;
	private final float probability;
	private final BlockStateProvider decoration;

	public BibbitHollowTrunkDecorator(int count, float probability, BlockStateProvider decoration) {
		this.count = count;
		this.probability = probability;
		this.decoration = decoration;
	}

	@Override
	protected TreeDecoratorType<?> type() {
		return RPGFeatureModifiers.BIBBIT_HOLLOW_DECORATOR.get();
	}

	@Override
	public void place(Context context) {
		int blockCount = context.logs().size();

		if (blockCount < 3) {
			return;
		}

		if (context.random().nextFloat() >= this.probability) return;

		BlockPos logPos = context.logs().get(2);

		BlockState stateToPlace = this.decoration.getState(context.random(), logPos);
		context.setBlock(logPos, stateToPlace);

		if (stateToPlace.getBlock() instanceof TreeHollowBlock) {
			BlockEntity blockEntity = context.level().getBlockEntity(logPos, ModBlockEntities.TREE_HOLLOW_BLOCK_ENTITY.get()).get();
			if (blockEntity instanceof TreeHollowBlockEntity hollowBE) {
				hollowBE.items.set(0, new ItemStack(ModItems.BIBBIT_SPAWN_EGG.get()));
				hollowBE.setChanged();
			} else {
				RPGworldMod.LOGGER.warn("[BibbitHollowTrunkDecorator] BlockEntity at {} is not TreeHollowBlockEntity", logPos);
			}
		}
	}
}