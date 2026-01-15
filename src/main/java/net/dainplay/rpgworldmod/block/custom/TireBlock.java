package net.dainplay.rpgworldmod.block.custom;

import net.dainplay.rpgworldmod.sounds.RPGSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;

public class TireBlock extends Block {
	public static final DirectionProperty FACING = net.minecraft.world.level.block.state.properties.BlockStateProperties.FACING;
	public static final EnumProperty<Form> FORM = EnumProperty.create("form", Form.class);

	// Voxel shapes (остаются без изменений)
	private static final VoxelShape SINGLE_NORTH = Shapes.or(
			Block.box(1, 1, 10, 5, 15, 16),   // Левая часть
			Block.box(11, 1, 10, 15, 15, 16), // Правая часть
			Block.box(5, 1, 10, 11, 5, 16),   // Нижняя часть
			Block.box(5, 11, 10, 11, 15, 16)  // Верхняя часть
	);

	private static final VoxelShape SINGLE_SOUTH = Shapes.or(
			Block.box(1, 1, 0, 5, 15, 6),     // Левая часть
			Block.box(11, 1, 0, 15, 15, 6),   // Правая часть
			Block.box(5, 1, 0, 11, 5, 6),     // Нижняя часть
			Block.box(5, 11, 0, 11, 15, 6)    // Верхняя часть
	);

	private static final VoxelShape SINGLE_EAST = Shapes.or(
			Block.box(0, 1, 1, 6, 5, 15),     // Нижняя часть
			Block.box(0, 11, 1, 6, 15, 15),   // Верхняя часть
			Block.box(0, 5, 1, 6, 11, 5),     // Северная часть
			Block.box(0, 5, 11, 6, 11, 15)    // Южная часть
	);

	private static final VoxelShape SINGLE_WEST = Shapes.or(
			Block.box(10, 1, 1, 16, 5, 15),   // Нижняя часть
			Block.box(10, 11, 1, 16, 15, 15), // Верхняя часть
			Block.box(10, 5, 1, 16, 11, 5),   // Северная часть
			Block.box(10, 5, 11, 16, 11, 15)  // Южная часть
	);

	private static final VoxelShape SINGLE_UP = Shapes.or(
			Block.box(1, 0, 1, 5, 6, 15),     // Западная часть
			Block.box(11, 0, 1, 15, 6, 15),   // Восточная часть
			Block.box(5, 0, 1, 11, 6, 5),     // Северная часть
			Block.box(5, 0, 11, 11, 6, 15)    // Южная часть
	);

	private static final VoxelShape SINGLE_DOWN = Shapes.or(
			Block.box(1, 10, 1, 5, 16, 15),   // Западная часть
			Block.box(11, 10, 1, 15, 16, 15), // Восточная часть
			Block.box(5, 10, 1, 11, 16, 5),   // Северная часть
			Block.box(5, 10, 11, 11, 16, 15)  // Южная часть
	);

	// DOUBLE формы: две шины с промежутком
	private static final VoxelShape DOUBLE_NORTH = Shapes.or(
			// Первая шина (северная)
			Block.box(1, 1, 10, 5, 15, 16),
			Block.box(11, 1, 10, 15, 15, 16),
			Block.box(5, 1, 10, 11, 5, 16),
			Block.box(5, 11, 10, 11, 15, 16),

			// Промежуточная часть
			Block.box(2, 2, 8, 4, 14, 10),
			Block.box(12, 2, 8, 14, 14, 10),
			Block.box(4, 2, 8, 12, 4, 10),
			Block.box(4, 12, 8, 12, 14, 10),

			// Вторая шина (южная)
			Block.box(1, 1, 2, 5, 15, 8),
			Block.box(11, 1, 2, 15, 15, 8),
			Block.box(5, 1, 2, 11, 5, 8),
			Block.box(5, 11, 2, 11, 15, 8)
	);

	private static final VoxelShape DOUBLE_SOUTH = Shapes.or(
			// Первая шина (южная)
			Block.box(1, 1, 0, 5, 15, 6),
			Block.box(11, 1, 0, 15, 15, 6),
			Block.box(5, 1, 0, 11, 5, 6),
			Block.box(5, 11, 0, 11, 15, 6),

			// Промежуточная часть
			Block.box(2, 2, 6, 4, 14, 8),
			Block.box(12, 2, 6, 14, 14, 8),
			Block.box(4, 2, 6, 12, 4, 8),
			Block.box(4, 12, 6, 12, 14, 8),

			// Вторая шина (северная)
			Block.box(1, 1, 8, 5, 15, 14),
			Block.box(11, 1, 8, 15, 15, 14),
			Block.box(5, 1, 8, 11, 5, 14),
			Block.box(5, 11, 8, 11, 15, 14)
	);

	private static final VoxelShape DOUBLE_EAST = Shapes.or(
			// Первая шина (восточная)
			Block.box(0, 1, 1, 6, 5, 15),
			Block.box(0, 11, 1, 6, 15, 15),
			Block.box(0, 5, 1, 6, 11, 5),
			Block.box(0, 5, 11, 6, 11, 15),

			// Промежуточная часть
			Block.box(6, 2, 2, 8, 4, 14),
			Block.box(6, 12, 2, 8, 14, 14),
			Block.box(6, 4, 2, 8, 12, 4),
			Block.box(6, 4, 12, 8, 12, 14),

			// Вторая шина (западная)
			Block.box(8, 1, 1, 14, 5, 15),
			Block.box(8, 11, 1, 14, 15, 15),
			Block.box(8, 5, 1, 14, 11, 5),
			Block.box(8, 5, 11, 14, 11, 15)
	);

	private static final VoxelShape DOUBLE_WEST = Shapes.or(
			// Первая шина (западная)
			Block.box(10, 1, 1, 16, 5, 15),
			Block.box(10, 11, 1, 16, 15, 15),
			Block.box(10, 5, 1, 16, 11, 5),
			Block.box(10, 5, 11, 16, 11, 15),

			// Промежуточная часть
			Block.box(8, 2, 2, 10, 4, 14),
			Block.box(8, 12, 2, 10, 14, 14),
			Block.box(8, 4, 2, 10, 12, 4),
			Block.box(8, 4, 12, 10, 12, 14),

			// Вторая шина (восточная)
			Block.box(2, 1, 1, 8, 5, 15),
			Block.box(2, 11, 1, 8, 15, 15),
			Block.box(2, 5, 1, 8, 11, 5),
			Block.box(2, 5, 11, 8, 11, 15)
	);

	private static final VoxelShape DOUBLE_UP = Shapes.or(
			// Первая шина (верхняя)
			Block.box(1, 0, 1, 5, 6, 15),
			Block.box(11, 0, 1, 15, 6, 15),
			Block.box(5, 0, 1, 11, 6, 5),
			Block.box(5, 0, 11, 11, 6, 15),

			// Промежуточная часть
			Block.box(2, 6, 2, 4, 8, 14),
			Block.box(12, 6, 2, 14, 8, 14),
			Block.box(4, 6, 2, 12, 8, 4),
			Block.box(4, 6, 12, 12, 8, 14),

			// Вторая шина (нижняя)
			Block.box(1, 8, 1, 5, 14, 15),
			Block.box(11, 8, 1, 15, 14, 15),
			Block.box(5, 8, 1, 11, 14, 5),
			Block.box(5, 8, 11, 11, 14, 15)
	);

	private static final VoxelShape DOUBLE_DOWN = Shapes.or(
			// Первая шина (нижняя)
			Block.box(1, 10, 1, 5, 16, 15),
			Block.box(11, 10, 1, 15, 16, 15),
			Block.box(5, 10, 1, 11, 16, 5),
			Block.box(5, 10, 11, 11, 16, 15),

			// Промежуточная часть
			Block.box(2, 8, 2, 4, 10, 14),
			Block.box(12, 8, 2, 14, 10, 14),
			Block.box(4, 8, 2, 12, 10, 4),
			Block.box(4, 8, 12, 12, 10, 14),

			// Вторая шина (верхняя)
			Block.box(1, 2, 1, 5, 8, 15),
			Block.box(11, 2, 1, 15, 8, 15),
			Block.box(5, 2, 1, 11, 8, 5),
			Block.box(5, 2, 11, 11, 8, 15)
	);

	// TRIPLE формы: три шины с двумя промежутками
	private static final VoxelShape TRIPLE_NORTH = Shapes.or(
			// Первая шина (северная)
			Block.box(1, 1, 10, 5, 15, 16),
			Block.box(11, 1, 10, 15, 15, 16),
			Block.box(5, 1, 10, 11, 5, 16),
			Block.box(5, 11, 10, 11, 15, 16),

			// Первый промежуток
			Block.box(2, 2, 8, 4, 14, 10),
			Block.box(12, 2, 8, 14, 14, 10),
			Block.box(4, 2, 8, 12, 4, 10),
			Block.box(4, 12, 8, 12, 14, 10),

			// Вторая шина (центральная)
			Block.box(1, 1, 4, 5, 15, 8),
			Block.box(11, 1, 4, 15, 15, 8),
			Block.box(5, 1, 4, 11, 5, 8),
			Block.box(5, 11, 4, 11, 15, 8),

			// Второй промежуток
			Block.box(2, 2, 2, 4, 14, 4),
			Block.box(12, 2, 2, 14, 14, 4),
			Block.box(4, 2, 2, 12, 4, 4),
			Block.box(4, 12, 2, 12, 14, 4),

			// Третья шина (южная)
			Block.box(1, 1, 0, 5, 15, 2),
			Block.box(11, 1, 0, 15, 15, 2),
			Block.box(5, 1, 0, 11, 5, 2),
			Block.box(5, 11, 0, 11, 15, 2)
	);

	private static final VoxelShape TRIPLE_SOUTH = Shapes.or(
			// Первая шина (южная)
			Block.box(1, 1, 0, 5, 15, 6),
			Block.box(11, 1, 0, 15, 15, 6),
			Block.box(5, 1, 0, 11, 5, 6),
			Block.box(5, 11, 0, 11, 15, 6),

			// Первый промежуток
			Block.box(2, 2, 6, 4, 14, 8),
			Block.box(12, 2, 6, 14, 14, 8),
			Block.box(4, 2, 6, 12, 4, 8),
			Block.box(4, 12, 6, 12, 14, 8),

			// Вторая шина (центральная)
			Block.box(1, 1, 8, 5, 15, 12),
			Block.box(11, 1, 8, 15, 15, 12),
			Block.box(5, 1, 8, 11, 5, 12),
			Block.box(5, 11, 8, 11, 15, 12),

			// Второй промежуток
			Block.box(2, 2, 12, 4, 14, 14),
			Block.box(12, 2, 12, 14, 14, 14),
			Block.box(4, 2, 12, 12, 4, 14),
			Block.box(4, 12, 12, 12, 14, 14),

			// Третья шина (северная)
			Block.box(1, 1, 14, 5, 15, 16),
			Block.box(11, 1, 14, 15, 15, 16),
			Block.box(5, 1, 14, 11, 5, 16),
			Block.box(5, 11, 14, 11, 15, 16)
	);

	private static final VoxelShape TRIPLE_EAST = Shapes.or(
			// Первая шина (восточная)
			Block.box(0, 1, 1, 6, 5, 15),
			Block.box(0, 11, 1, 6, 15, 15),
			Block.box(0, 5, 1, 6, 11, 5),
			Block.box(0, 5, 11, 6, 11, 15),

			// Первый промежуток
			Block.box(6, 2, 2, 8, 4, 14),
			Block.box(6, 12, 2, 8, 14, 14),
			Block.box(6, 4, 2, 8, 12, 4),
			Block.box(6, 4, 12, 8, 12, 14),

			// Вторая шина (центральная)
			Block.box(8, 1, 1, 12, 5, 15),
			Block.box(8, 11, 1, 12, 15, 15),
			Block.box(8, 5, 1, 12, 11, 5),
			Block.box(8, 5, 11, 12, 11, 15),

			// Второй промежуток
			Block.box(12, 2, 2, 14, 4, 14),
			Block.box(12, 12, 2, 14, 14, 14),
			Block.box(12, 4, 2, 14, 12, 4),
			Block.box(12, 4, 12, 14, 12, 14),

			// Третья шина (западная)
			Block.box(14, 1, 1, 16, 5, 15),
			Block.box(14, 11, 1, 16, 15, 15),
			Block.box(14, 5, 1, 16, 11, 5),
			Block.box(14, 5, 11, 16, 11, 15)
	);

	private static final VoxelShape TRIPLE_WEST = Shapes.or(
			// Первая шина (западная)
			Block.box(10, 1, 1, 16, 5, 15),
			Block.box(10, 11, 1, 16, 15, 15),
			Block.box(10, 5, 1, 16, 11, 5),
			Block.box(10, 5, 11, 16, 11, 15),

			// Первый промежуток
			Block.box(8, 2, 2, 10, 4, 14),
			Block.box(8, 12, 2, 10, 14, 14),
			Block.box(8, 4, 2, 10, 12, 4),
			Block.box(8, 4, 12, 10, 12, 14),

			// Вторая шина (центральная)
			Block.box(4, 1, 1, 8, 5, 15),
			Block.box(4, 11, 1, 8, 15, 15),
			Block.box(4, 5, 1, 8, 11, 5),
			Block.box(4, 5, 11, 8, 11, 15),

			// Второй промежуток
			Block.box(2, 2, 2, 4, 4, 14),
			Block.box(2, 12, 2, 4, 14, 14),
			Block.box(2, 4, 2, 4, 12, 4),
			Block.box(2, 4, 12, 4, 12, 14),

			// Третья шина (восточная)
			Block.box(0, 1, 1, 2, 5, 15),
			Block.box(0, 11, 1, 2, 15, 15),
			Block.box(0, 5, 1, 2, 11, 5),
			Block.box(0, 5, 11, 2, 11, 15)
	);

	private static final VoxelShape TRIPLE_UP = Shapes.or(
			// Первая шина (верхняя)
			Block.box(1, 0, 1, 5, 6, 15),
			Block.box(11, 0, 1, 15, 6, 15),
			Block.box(5, 0, 1, 11, 6, 5),
			Block.box(5, 0, 11, 11, 6, 15),

			// Первый промежуток
			Block.box(2, 6, 2, 4, 8, 14),
			Block.box(12, 6, 2, 14, 8, 14),
			Block.box(4, 6, 2, 12, 8, 4),
			Block.box(4, 6, 12, 12, 8, 14),

			// Вторая шина (центральная)
			Block.box(1, 8, 1, 5, 12, 15),
			Block.box(11, 8, 1, 15, 12, 15),
			Block.box(5, 8, 1, 11, 12, 5),
			Block.box(5, 8, 11, 11, 12, 15),

			// Второй промежуток
			Block.box(2, 12, 2, 4, 14, 14),
			Block.box(12, 12, 2, 14, 14, 14),
			Block.box(4, 12, 2, 12, 14, 4),
			Block.box(4, 12, 12, 12, 14, 14),

			// Третья шина (нижняя)
			Block.box(1, 14, 1, 5, 16, 15),
			Block.box(11, 14, 1, 15, 16, 15),
			Block.box(5, 14, 1, 11, 16, 5),
			Block.box(5, 14, 11, 11, 16, 15)
	);

	private static final VoxelShape TRIPLE_DOWN = Shapes.or(
			// Первая шина (нижняя)
			Block.box(1, 10, 1, 5, 16, 15),
			Block.box(11, 10, 1, 15, 16, 15),
			Block.box(5, 10, 1, 11, 16, 5),
			Block.box(5, 10, 11, 11, 16, 15),

			// Первый промежуток
			Block.box(2, 8, 2, 4, 10, 14),
			Block.box(12, 8, 2, 14, 10, 14),
			Block.box(4, 8, 2, 12, 10, 4),
			Block.box(4, 8, 12, 12, 10, 14),

			// Вторая шина (центральная)
			Block.box(1, 4, 1, 5, 8, 15),
			Block.box(11, 4, 1, 15, 8, 15),
			Block.box(5, 4, 1, 11, 8, 5),
			Block.box(5, 4, 11, 11, 8, 15),

			// Второй промежуток
			Block.box(2, 2, 2, 4, 4, 14),
			Block.box(12, 2, 2, 14, 4, 14),
			Block.box(4, 2, 2, 12, 4, 4),
			Block.box(4, 2, 12, 12, 4, 14),

			// Третья шина (верхняя)
			Block.box(1, 0, 1, 5, 2, 15),
			Block.box(11, 0, 1, 15, 2, 15),
			Block.box(5, 0, 1, 11, 2, 5),
			Block.box(5, 0, 11, 11, 2, 15)
	);

	public TireBlock(Properties properties) {
		super(properties);
		this.registerDefaultState(this.stateDefinition.any()
				.setValue(FACING, Direction.NORTH)
				.setValue(FORM, Form.SINGLE));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING, FORM);
	}

	@Nullable
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		Direction clickedFace = context.getClickedFace();
		BlockPos clickedPos = context.getClickedPos();
		Level level = context.getLevel();
		BlockState clickedState = level.getBlockState(clickedPos);

		// Если кликнули на существующую шину, пытаемся соединить их
		if (clickedState.is(this) && clickedState.getValue(FACING) == clickedFace) {
			Form currentForm = clickedState.getValue(FORM);
			if (currentForm == Form.SINGLE) {
				return clickedState.setValue(FORM, Form.DOUBLE);
			}
		}
		return this.defaultBlockState().setValue(FACING, clickedFace);
	}

	@Override
	public boolean canBeReplaced(BlockState state, BlockPlaceContext context) {
		ItemStack itemStack = context.getItemInHand();
		if (!itemStack.is(this.asItem())) {
			return false;
		}

		Direction clickedFace = context.getClickedFace();
		BlockPos clickedPos = context.getClickedPos();
		BlockState clickedState = context.getLevel().getBlockState(clickedPos);

		return clickedState.getBlock() == this &&
				clickedState.getValue(FACING) == clickedFace &&
				clickedState.getValue(FORM) == Form.SINGLE;
	}

	@Override
	public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
		// Проверяем только направление FACING - вперед
		if (direction == state.getValue(FACING)) {
			// Если текущий блок DOUBLE и впереди есть шина с тем же направлением
			if (state.getValue(FORM) == Form.DOUBLE) {
				if (neighborState.is(this) && neighborState.getValue(FACING) == direction) {
					return state.setValue(FORM, Form.TRIPLE);
				}
			}
			// Если текущий блок TRIPLE и шина впереди исчезла
			else if (state.getValue(FORM) == Form.TRIPLE) {
				if (!neighborState.is(this) || neighborState.getValue(FACING) != direction) {
					return state.setValue(FORM, Form.DOUBLE);
				}
			}
		}
		return state;
	}

	@Override
	public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
		super.neighborChanged(state, level, pos, block, fromPos, isMoving);

		// Проверяем блоки сзади (в противоположном направлении)
		Direction opposite = state.getValue(FACING).getOpposite();
		BlockPos behindPos = pos.relative(opposite);
		BlockState behindState = level.getBlockState(behindPos);

		// Если сзади стоит DOUBLE шина с тем же направлением
		if (behindState.is(this) && behindState.getValue(FACING) == state.getValue(FACING)) {
			if (behindState.getValue(FORM) == Form.DOUBLE) {
				// Проверяем, есть ли шина впереди у этой задней шины (то есть текущая шина)
				if (state.is(this) && state.getValue(FACING) == behindState.getValue(FACING)) {
					// Обновляем заднюю шину в TRIPLE
					level.setBlock(behindPos, behindState.setValue(FORM, Form.TRIPLE), 3);
				}
			}
		}
	}

	@Override
	public PushReaction getPistonPushReaction(BlockState state) {
		return PushReaction.DESTROY;
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		Direction facing = state.getValue(FACING);
		Form form = state.getValue(FORM);

		switch (form) {
			case SINGLE:
				switch (facing) {
					case NORTH:
						return SINGLE_NORTH;
					case SOUTH:
						return SINGLE_SOUTH;
					case EAST:
						return SINGLE_EAST;
					case WEST:
						return SINGLE_WEST;
					case UP:
						return SINGLE_UP;
					case DOWN:
						return SINGLE_DOWN;
				}
			case DOUBLE:
				switch (facing) {
					case NORTH:
						return DOUBLE_NORTH;
					case SOUTH:
						return DOUBLE_SOUTH;
					case EAST:
						return DOUBLE_EAST;
					case WEST:
						return DOUBLE_WEST;
					case UP:
						return DOUBLE_UP;
					case DOWN:
						return DOUBLE_DOWN;
				}
			case TRIPLE:
				switch (facing) {
					case NORTH:
						return TRIPLE_NORTH;
					case SOUTH:
						return TRIPLE_SOUTH;
					case EAST:
						return TRIPLE_EAST;
					case WEST:
						return TRIPLE_WEST;
					case UP:
						return TRIPLE_UP;
					case DOWN:
						return TRIPLE_DOWN;
				}
			default:
				return SINGLE_NORTH;
		}
	}


	public void fallOn(Level pLevel, BlockState pState, BlockPos pPos, Entity pEntity, float pFallDistance) {
		pEntity.causeFallDamage(pFallDistance, 0.0F, pLevel.damageSources().fall());
	}

	public void updateEntityAfterFallOn(BlockGetter pLevel, Entity pEntity) {
		if (pEntity.isSuppressingBounce()) {
			super.updateEntityAfterFallOn(pLevel, pEntity);
		}/* else {
			this.bounce(pEntity);
		}*/

	}

	private void bounce(Level pLevel, BlockPos pPos, Entity pEntity, BlockState pState) {
		Vec3 vec3 = pEntity.getDeltaMovement();
		if (vec3.y <= 0.0D && pState.getValue(FACING) != Direction.DOWN) {
			double d0 = pEntity instanceof LivingEntity ? 1.0D : 0.8D;
			double multiplier = pState.getValue(FORM) == Form.DOUBLE ? 1.8 : 1.2;
			double x_mov = vec3.x;
			double z_mov = vec3.z;
			if (pState.getValue(FACING) == Direction.NORTH) z_mov = -multiplier;
			if (pState.getValue(FACING) == Direction.SOUTH) z_mov = multiplier;
			if (pState.getValue(FACING) == Direction.WEST) x_mov = -multiplier;
			if (pState.getValue(FACING) == Direction.EAST) x_mov = multiplier;
			pEntity.setDeltaMovement(x_mov * d0, Math.max((-vec3.y), multiplier) * d0, z_mov * d0);
			pLevel.playSound(null, pPos, RPGSounds.TIRE_BOUNCE.get(), SoundSource.BLOCKS, 1.0F, (pLevel.random.nextFloat() - pLevel.random.nextFloat()) * 0.2F + 1.0F);
		}

	}

	public void stepOn(Level pLevel, BlockPos pPos, BlockState pState, Entity pEntity) {
		if (!pEntity.isSteppingCarefully()) {
			this.bounce(pLevel, pPos, pEntity, pState);
		}

		super.stepOn(pLevel, pPos, pState, pEntity);
	}

	@Override
	@Deprecated
	public void entityInside(BlockState pState, Level pLevel, BlockPos pPos, Entity pEntity) {
		if (!pEntity.isSteppingCarefully())
			if ((pState.getValue(FACING) == Direction.NORTH && pEntity.getBoundingBox().intersects(getAlignedBlockAABB(pLevel, pPos, pState).move(0, 0, -0.01D)))
					|| (pState.getValue(FACING) == Direction.SOUTH && pEntity.getBoundingBox().intersects(getAlignedBlockAABB(pLevel, pPos, pState).move(0, 0, 0.01D)))
					|| (pState.getValue(FACING) == Direction.WEST && pEntity.getBoundingBox().intersects(getAlignedBlockAABB(pLevel, pPos, pState).move(-0.01D, 0, 0)))
					|| (pState.getValue(FACING) == Direction.EAST && pEntity.getBoundingBox().intersects(getAlignedBlockAABB(pLevel, pPos, pState).move(0.01D, 0, 0)))) {
				this.bounce(pLevel, pPos, pEntity, pState);
			}
	}


	public AABB getAlignedBlockAABB(LevelAccessor world, BlockPos pos, BlockState blockState) {
		VoxelShape voxelShape = this.getCollisionShape(blockState, world, pos, CollisionContext.empty());
		AABB voxelShapeAABB = voxelShape.bounds();
		return new AABB(voxelShapeAABB.minX + pos.getX(), voxelShapeAABB.minY + pos.getY(), voxelShapeAABB.minZ + pos.getZ(),
				voxelShapeAABB.maxX + pos.getX(), voxelShapeAABB.maxY + pos.getY(), voxelShapeAABB.maxZ + pos.getZ());
	}
}