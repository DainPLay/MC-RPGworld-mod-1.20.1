package net.dainplay.rpgworldmod.item.custom;

import net.dainplay.rpgworldmod.entity.ModEntities;
import net.dainplay.rpgworldmod.world.feature.ModConfiguredFeatures;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockSource;
import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraftforge.common.ForgeSpawnEggItem;

import javax.annotation.Nullable;

public class EntSpawnEggItem extends ForgeSpawnEggItem {
	public EntSpawnEggItem(int backgroundColor, int highlightColor, Properties props) {
		super(ModEntities.BIBBIT, backgroundColor, highlightColor, props);
	}

	@Override
	public InteractionResult useOn(UseOnContext context) {
		Level level = context.getLevel();
		if (level.isClientSide) {
			return InteractionResult.SUCCESS;
		}

		ServerLevel serverLevel = (ServerLevel) level;
		BlockPos pos = context.getClickedPos();
		Direction direction = context.getClickedFace();
		BlockState blockState = level.getBlockState(pos);


		if (blockState.is(Blocks.SPAWNER)) {
			return InteractionResult.PASS;
		}


		BlockPos spawnPos;
		if (blockState.getCollisionShape(level, pos).isEmpty()) {
			spawnPos = pos;
		} else {
			spawnPos = pos.relative(direction);
		}

		ConfiguredFeature<?, ?> treeFeature = level.registryAccess()
				.registryOrThrow(Registries.CONFIGURED_FEATURE)
				.get(ModConfiguredFeatures.ENT_FACE_EAST_KEY);


		switch (level.random.nextInt(3)) {
			case 0:
				treeFeature = level.registryAccess()
						.registryOrThrow(Registries.CONFIGURED_FEATURE)
						.get(ModConfiguredFeatures.ENT_FACE_WEST_KEY);
				break;
			case 1:
				treeFeature = level.registryAccess()
						.registryOrThrow(Registries.CONFIGURED_FEATURE)
						.get(ModConfiguredFeatures.ENT_FACE_NORTH_KEY);
				break;
			case 2:
				treeFeature = level.registryAccess()
						.registryOrThrow(Registries.CONFIGURED_FEATURE)
						.get(ModConfiguredFeatures.ENT_FACE_SOUTH_KEY);
				break;
			default:
				break;
		}

		if (treeFeature != null) {
			if (treeFeature.place(serverLevel,
					serverLevel.getChunkSource().getGenerator(),
					serverLevel.random,
					spawnPos)) {
				ItemStack stack = context.getItemInHand();
				if (!context.getPlayer().getAbilities().instabuild) {
					stack.shrink(1);
				}

				return InteractionResult.CONSUME;
			}
		}

		return InteractionResult.FAIL;
	}

	@Nullable
	@Override
	protected DispenseItemBehavior createDispenseBehavior() {
		return new TreeEggDispenseBehavior();
	}

	private class TreeEggDispenseBehavior implements DispenseItemBehavior {
		@Override
		public ItemStack dispense(BlockSource source, ItemStack stack) {
			ServerLevel level = source.getLevel();
			BlockState state = source.getBlockState();


			if (!(state.getBlock() instanceof DispenserBlock)) {
				return stack;
			}

			Direction direction = state.getValue(DispenserBlock.FACING);
			BlockPos pos;
			if (direction == Direction.UP)
				pos = source.getPos().relative(direction).relative(direction);
			else
				pos = source.getPos().relative(direction);


			ResourceLocation[] faceKeys = {
					ModConfiguredFeatures.ENT_FACE_EAST_KEY.location(),
					ModConfiguredFeatures.ENT_FACE_WEST_KEY.location(),
					ModConfiguredFeatures.ENT_FACE_NORTH_KEY.location(),
					ModConfiguredFeatures.ENT_FACE_SOUTH_KEY.location()
			};
			ResourceLocation selectedKey = faceKeys[level.random.nextInt(faceKeys.length)];

			ConfiguredFeature<?, ?> treeFeature = level.registryAccess()
					.registryOrThrow(Registries.CONFIGURED_FEATURE)
					.get(selectedKey);

			if (treeFeature != null && treeFeature.place(level,
					level.getChunkSource().getGenerator(),
					level.random,
					pos)) {
				stack.shrink(1);
				playSound(source);
				playAnimation(source, direction);
			}
			return stack;
		}

		private void playSound(BlockSource source) {
			source.getLevel().levelEvent(1000, source.getPos(), 0);
		}

		private void playAnimation(BlockSource source, Direction direction) {
			source.getLevel().levelEvent(2000, source.getPos(), direction.get3DDataValue());
		}
	}
}