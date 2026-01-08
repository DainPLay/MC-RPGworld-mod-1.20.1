package net.dainplay.rpgworldmod.block.custom;

import net.dainplay.rpgworldmod.block.entity.custom.FireCatcherBlockEntity;
import net.dainplay.rpgworldmod.util.FireCatcherManager;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
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

    private static final VoxelShape SHAPE = Block.box(2.0, 0.0, 2.0, 14.0, 16.0, 14.0);

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
        return SHAPE;
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

        // ВАЖНО: Сначала размещаем нижнюю часть, потом верхнюю
        // Это решает проблему с синхронизацией клиента

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
    public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        // При разрушении одной части, разрушаем и другую
        Half half = state.getValue(HALF);
        BlockPos otherPos = half == Half.TOP ? pos.below() : pos.above();
        BlockState otherState = level.getBlockState(otherPos);

        if (otherState.getBlock() == this) {
            level.setBlock(otherPos, net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(), 35);
            level.levelEvent(player, 2001, otherPos, Block.getId(otherState));

            // Удаляем из системы защиты и восстанавливаем тики огня
            if (!level.isClientSide) {
                FireCatcherManager manager = FireCatcherManager.get(level);
                manager.removeFireCatcher(getMainPos(pos, state), level.dimension(), (ServerLevel) level);
            }
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
                catcher.toggleMode();

                // Звук переключения
                level.playSound(null, pos, SoundEvents.LEVER_CLICK, SoundSource.BLOCKS, 0.3f, 0.6f);

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
        if (state.getValue(HUNGRY)) {
            // Частицы для голодного состояния
            if (random.nextInt(10) == 0) {
                double x = pos.getX() + 0.5 + (random.nextDouble() - 0.5) * 0.5;
                double y = pos.getY() + 0.2;
                double z = pos.getZ() + 0.5 + (random.nextDouble() - 0.5) * 0.5;
                level.addParticle(net.minecraft.core.particles.ParticleTypes.FLAME, x, y, z, 0, 0, 0);
            }
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