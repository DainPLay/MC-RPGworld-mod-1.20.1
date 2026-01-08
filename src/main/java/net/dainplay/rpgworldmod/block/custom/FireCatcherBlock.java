package net.dainplay.rpgworldmod.block.custom;

import net.dainplay.rpgworldmod.block.entity.custom.FireCatcherBlockEntity;
import net.dainplay.rpgworldmod.data.tags.ModAdvancements;
import net.dainplay.rpgworldmod.sounds.RPGSounds;
import net.dainplay.rpgworldmod.util.FireCatcherManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class FireCatcherBlock extends Block implements EntityBlock {
	public static final EnumProperty<Half> HALF = BlockStateProperties.HALF;
	public static final BooleanProperty HUNGRY = BooleanProperty.create("hungry");

	private static final VoxelShape TOP_SHAPE = Block.box(2.0, 0.0, 2.0, 14.0, 16.0, 14.0);
	private static final VoxelShape BOTTOM_SHAPE = Block.box(2.0, 6.0, 2.0, 14.0, 16.0, 14.0);

	public FireCatcherBlock(Properties properties) {
		super(properties);
		this.registerDefaultState(this.stateDefinition.any()
				.setValue(HALF, Half.TOP)
				.setValue(HUNGRY, false));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(HALF, HUNGRY);
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return state.getValue(HALF) == Half.TOP ? TOP_SHAPE : BOTTOM_SHAPE;
	}

	@Override
	public RenderShape getRenderShape(BlockState state) {
		return RenderShape.MODEL;
	}

	@Nullable
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		Level level = context.getLevel();
		BlockPos pos = context.getClickedPos();
		Player player = context.getPlayer();

		// Проверяем, что блок размещается под потолком
		BlockPos abovePos = pos.above();
		if (!level.getBlockState(abovePos).isSolidRender(level, abovePos)) {
			return null;
		}

		// Проверяем, что есть место для нижней части
		if (!level.getBlockState(pos.below()).canBeReplaced(context)) {
			return null;
		}

		// Размещаем нижнюю часть
		level.setBlock(pos.below(),
				this.defaultBlockState()
						.setValue(HALF, Half.BOTTOM)
						.setValue(HUNGRY, false),
				3);

		// Размещаем верхнюю часть
		level.setBlock(pos,
				this.defaultBlockState()
						.setValue(HALF, Half.TOP)
						.setValue(HUNGRY, false),
				3);

		// Принудительно обновляем клиент для нижнего блока
		level.sendBlockUpdated(pos.below(),
				level.getBlockState(pos.below()),
				level.getBlockState(pos.below()),
				2);

		return level.getBlockState(pos);
	}

	@Override
	public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
		super.setPlacedBy(level, pos, state, placer, stack);

		if (!level.isClientSide && state.getValue(HALF) == Half.TOP) {
			// Немедленно обновляем менеджер при размещении
			FireCatcherManager manager = FireCatcherManager.get(level);
			manager.addFireCatcher(pos, false, level.dimension());

			// Синхронизируем BlockEntity
			if (level.getBlockEntity(pos) instanceof FireCatcherBlockEntity catcher) {
				catcher.setInitialState(false);
			}
		}
	}

	@Override
	public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
		// При разрушении одной части, разрушаем и другую
		Half half = state.getValue(HALF);
		BlockPos otherPos = half == Half.TOP ? pos.below() : pos.above();
		BlockState otherState = level.getBlockState(otherPos);

		if (otherState.getBlock() == this) {
			// Не вызываем playerWillDestroy для другой части, чтобы избежать рекурсии
			level.setBlock(otherPos, net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(), 35);
			level.levelEvent(player, 2001, otherPos, Block.getId(otherState));
		}

		// Всегда удаляем из системы защиты при разрушении
		if (!level.isClientSide) {
			BlockPos mainPos = getMainPos(pos, state);
			FireCatcherManager manager = FireCatcherManager.get(level);
			manager.removeFireCatcher(mainPos, level.dimension(), (ServerLevel) level);
		}

		super.playerWillDestroy(level, pos, state, player);
	}

	@Override
	public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
								 InteractionHand hand, BlockHitResult hit) {
		if (!level.isClientSide && hand == InteractionHand.MAIN_HAND) {
			// Определяем основную позицию (верхнюю часть)
			BlockPos mainPos = getMainPos(pos, state);

			// Переключаем состояние
			BlockEntity be = level.getBlockEntity(mainPos);
			if (be instanceof FireCatcherBlockEntity catcher) {
				if (!state.getValue(HUNGRY) && player instanceof ServerPlayer serverPlayer) {
					ModAdvancements.SWITCH_FIRE_CATCHER_TO_HUNGRY_MODE_TRIGGER.trigger(serverPlayer);
				}
				catcher.toggleMode();

				// Звук переключения
				if (state.getValue(HUNGRY))
					level.playSound(null, pos, RPGSounds.FIRE_CATCHER_NOT_HUNGRY.get(), SoundSource.BLOCKS, 0.3f, 1f);
				else
					level.playSound(null, pos, RPGSounds.FIRE_CATCHER_HUNGRY.get(), SoundSource.BLOCKS, 0.3f, 1f);

				// Обновляем соседние блоки для синхронизации клиента
				level.sendBlockUpdated(mainPos, state, state, 3);

				return InteractionResult.SUCCESS;
			}
		}

		return InteractionResult.sidedSuccess(level.isClientSide);
	}

	@Override
	public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
		if (!level.isClientSide && state.getValue(HALF) == Half.TOP) {
			// Добавляем в систему защиты
			FireCatcherManager manager = FireCatcherManager.get(level);
			manager.addFireCatcher(pos, false, level.dimension());
		}
	}

	@Override
	public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
		// Визуальные эффекты
		if (state.getValue(HUNGRY) && state.getValue(HALF) == Half.TOP) {
			// Частицы для голодного состояния
			if (random.nextInt(2) == 0) {
				double x = pos.getX() + 0.5 + (random.nextDouble() - 0.5) * 0.5;
				double y = pos.getY() + 0.5625;
				double z = pos.getZ() + 0.5 + (random.nextDouble() - 0.5) * 0.5;
				level.addParticle(net.minecraft.core.particles.ParticleTypes.FLAME, x, y, z, 0, 0, 0);
			}
		}
	}

	@Override
	public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
		super.neighborChanged(state, level, pos, block, fromPos, isMoving);

		if (!level.isClientSide && !canSurvive(state, level, pos)) {
			// Разрушаем обе части, если блок не может выжить
			destroyBothParts(level, pos, state);
		}
	}

	@Override
	public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
		// Проверяем, может ли блок выжить после обновления соседа
		if (!canSurvive(state, level, pos)) {
			if (!level.isClientSide()) {
				level.scheduleTick(pos, this, 1);
			}
		}
		return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
	}

	public boolean canSurvive(BlockState state, Level level, BlockPos pos) {
		Half half = state.getValue(HALF);

		if (half == Half.TOP) {
			// Верхняя часть должна быть прикреплена к блоку сверху
			BlockPos supportPos = pos.above();
			return level.getBlockState(supportPos).isFaceSturdy(level, supportPos, Direction.DOWN);
		} else {
			// Нижняя часть должна иметь верхнюю часть над собой
			BlockPos topPos = pos.above();
			BlockState topState = level.getBlockState(topPos);
			return topState.getBlock() == this && topState.getValue(HALF) == Half.TOP;
		}
	}

	private void destroyBothParts(Level level, BlockPos pos, BlockState state) {
		Half half = state.getValue(HALF);
		BlockPos otherPos = half == Half.TOP ? pos.below() : pos.above();
		BlockPos mainPos = getMainPos(pos, state);

		// Удаляем из системы защиты
		if (!level.isClientSide) {
			FireCatcherManager manager = FireCatcherManager.get(level);
			manager.removeFireCatcher(mainPos, level.dimension(), (ServerLevel) level);
		}

		// Разрушаем эту часть
		level.destroyBlock(pos, true);

		// Разрушаем другую часть (без дропа, чтобы не дублировать)
		if (level.getBlockState(otherPos).getBlock() == this) {
			level.destroyBlock(otherPos, false);
		}
	}

	private BlockPos getMainPos(BlockPos pos, BlockState state) {
		return state.getValue(HALF) == Half.TOP ? pos : pos.above();
	}

	@Nullable
	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return state.getValue(HALF) == Half.TOP ? new FireCatcherBlockEntity(pos, state) : null;
	}

	@Nullable
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
		return level.isClientSide ? null : (level0, pos, state0, blockEntity) -> {
			if (blockEntity instanceof FireCatcherBlockEntity catcher) {
				catcher.tick();
			}
		};
	}
}