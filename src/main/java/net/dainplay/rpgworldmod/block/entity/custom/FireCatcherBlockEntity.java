package net.dainplay.rpgworldmod.block.entity.custom;

import net.dainplay.rpgworldmod.block.custom.FireCatcherBlock;
import net.dainplay.rpgworldmod.block.entity.ModBlockEntities;
import net.dainplay.rpgworldmod.util.FireCatcherManager;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class FireCatcherBlockEntity extends BlockEntity {
    private boolean isHungry = false;
    private int tickCounter = 0;

    public FireCatcherBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.FIRE_CATCHER.get(), pos, state);
    }

    public void tick() {
        if (level == null || level.isClientSide) return;

        tickCounter++;

        // Каждую секунду (20 тиков) обновляем состояние
        if (tickCounter % 20 == 0) {
            // Обновляем состояние блока
            BlockState state = level.getBlockState(worldPosition);
            if (state.getValue(FireCatcherBlock.HUNGRY) != isHungry) {
                // Обновляем верхнюю часть
                level.setBlockAndUpdate(worldPosition, state.setValue(FireCatcherBlock.HUNGRY, isHungry));

                // Обновляем нижнюю часть
                updateBottomPart();
            }

            // Обновляем систему защиты
            FireCatcherManager manager = FireCatcherManager.get(level);
            manager.updateFireCatcher(worldPosition, isHungry, level.dimension());

            // Если голодный, тушим огонь в чанках
            if (isHungry && tickCounter % 40 == 0) {
                manager.extinguishFireInProtectedChunks(worldPosition, level.dimension(), (ServerLevel) level);
            }
        }
    }

    public void toggleMode() {
        isHungry = !isHungry;
        setChanged();

        // Синхронизируем с клиентом
        if (level != null) {
            // Обновляем верхнюю часть
            BlockState state = level.getBlockState(worldPosition);
            level.setBlockAndUpdate(worldPosition, state.setValue(FireCatcherBlock.HUNGRY, isHungry));
            level.sendBlockUpdated(worldPosition, state, state, 3);

            // Обновляем нижнюю часть
            updateBottomPart();
        }
    }

    private void updateBottomPart() {
        if (level == null) return;

        BlockPos bottomPos = worldPosition.below();
        BlockState bottomState = level.getBlockState(bottomPos);
        if (bottomState.getBlock() instanceof FireCatcherBlock) {
            level.setBlockAndUpdate(bottomPos, bottomState.setValue(FireCatcherBlock.HUNGRY, isHungry));
            level.sendBlockUpdated(bottomPos, bottomState, bottomState, 3);
        }
    }

    public void setInitialState(boolean hungry) {
        this.isHungry = hungry;
        setChanged();

        // Немедленно обновляем обе части
        if (level != null) {
            BlockState state = level.getBlockState(worldPosition);
            level.setBlockAndUpdate(worldPosition, state.setValue(FireCatcherBlock.HUNGRY, isHungry));
            updateBottomPart();
        }
    }

    public boolean isHungry() {
        return isHungry;
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putBoolean("Hungry", isHungry);
    }

    @Override
    public void load(@NotNull CompoundTag tag) {
        super.load(tag);
        isHungry = tag.getBoolean("Hungry");

        // При загрузке синхронизируем состояние
        if (level != null && !level.isClientSide) {
            BlockState state = level.getBlockState(worldPosition);
            if (state.getValue(FireCatcherBlock.HUNGRY) != isHungry) {
                level.setBlockAndUpdate(worldPosition, state.setValue(FireCatcherBlock.HUNGRY, isHungry));
                updateBottomPart();
            }
        }
    }

    @Override
    public @NotNull CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        tag.putBoolean("Hungry", isHungry);
        return tag;
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        CompoundTag tag = pkt.getTag() != null ? pkt.getTag() : new CompoundTag();
        load(tag);

        // Обновляем рендер
        if (level != null && level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);

            // Также обновляем нижнюю часть на клиенте
            BlockPos bottomPos = worldPosition.below();
            BlockState bottomState = level.getBlockState(bottomPos);
            if (bottomState.getBlock() instanceof FireCatcherBlock) {
                level.sendBlockUpdated(bottomPos, bottomState, bottomState, 3);
            }
        }
    }
}