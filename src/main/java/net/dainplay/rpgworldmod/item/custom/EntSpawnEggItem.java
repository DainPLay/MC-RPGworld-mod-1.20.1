package net.dainplay.rpgworldmod.item.custom;

import net.dainplay.rpgworldmod.entity.ModEntities;
import net.dainplay.rpgworldmod.world.feature.ModConfiguredFeatures;
import net.dainplay.rpgworldmod.world.feature.ModPlacedFeatures;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

import javax.annotation.Nullable;
import java.util.function.Supplier;

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

		// Проверяем, не спавнер ли это
		if (blockState.is(Blocks.SPAWNER)) {
			return InteractionResult.PASS; // Не изменяем спавнеры
		}

		// Определяем позицию для спавна дерева
		BlockPos spawnPos;
		if (blockState.getCollisionShape(level, pos).isEmpty()) {
			spawnPos = pos;
		} else {
			spawnPos = pos.relative(direction);
		}

		ConfiguredFeature<?, ?> treeFeature = level.registryAccess()
				.registryOrThrow(Registries.CONFIGURED_FEATURE)
				.get(ModConfiguredFeatures.ENT_FACE_EAST_KEY);

		// Получаем фичу дерева
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
			default: break;
		}

		if (treeFeature != null) {
			// Пытаемся сгенерировать дерево
			if (treeFeature.place(serverLevel,
					serverLevel.getChunkSource().getGenerator(),
					serverLevel.random,
					spawnPos)) {

				// Уменьшаем стак и проигрываем эффект
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

	private class TreeEggDispenseBehavior extends DefaultDispenseItemBehavior {
		@Override
		protected ItemStack execute(net.minecraft.core.BlockSource source, ItemStack stack) {
			ServerLevel level = source.getLevel();
			Direction direction = source.getBlockState().getValue(DispenserBlock.FACING);
			BlockPos pos = source.getPos().relative(direction);

			ConfiguredFeature<?, ?> treeFeature = level.registryAccess()
					.registryOrThrow(Registries.CONFIGURED_FEATURE)
					.get(ModConfiguredFeatures.ENT_FACE_EAST_KEY);

			// Получаем фичу дерева
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
				default: break;
			}

			if (treeFeature != null && treeFeature.feature() == Feature.TREE) {
				if (treeFeature.place(level,
						level.getChunkSource().getGenerator(),
						level.random,
						pos)) {

					stack.shrink(1);
					return stack;
				}
			}

			return stack;
		}
	}
}