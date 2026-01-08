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

public class FireCatcherBlockEntity extends BlockEntity {
    private boolean isHungry = false;
    private boolean wasHungry = false; // Запоминаем предыдущее состояние
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
                level.setBlockAndUpdate(worldPosition, state.setValue(FireCatcherBlock.HUNGRY, isHungry));
            }

            // Обновляем систему защиты
            FireCatcherManager manager = FireCatcherManager.get(level);

            // Если состояние изменилось, используем специальный метод
            if (wasHungry != isHungry) {
                manager.updateFireCatcherState(worldPosition, wasHungry, isHungry, level.dimension(), (ServerLevel) level);
                wasHungry = isHungry;
            } else {
                manager.updateFireCatcher(worldPosition, isHungry, level.dimension());
            }

            // Если голодный, тушим огонь в чанках
            if (isHungry && tickCounter % 40 == 0) {
                manager.extinguishFireInProtectedChunks(worldPosition, level.dimension(), (ServerLevel) level);
            }
        }
    }

    public void toggleMode() {
        wasHungry = isHungry; // Сохраняем старое состояние перед изменением
        isHungry = !isHungry;
        setChanged();

        // Синхронизируем с клиентом
        if (level != null) {
            BlockState state = level.getBlockState(worldPosition);
            level.sendBlockUpdated(worldPosition, state, state, 3);

            // Обновляем нижнюю часть
            BlockPos bottomPos = worldPosition.below();
            BlockState bottomState = level.getBlockState(bottomPos);
            if (bottomState.getBlock() instanceof FireCatcherBlock) {
                level.sendBlockUpdated(bottomPos, bottomState, bottomState, 3);
            }
        }
    }

    public boolean isHungry() {
        return isHungry;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putBoolean("Hungry", isHungry);
        tag.putBoolean("WasHungry", wasHungry);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        isHungry = tag.getBoolean("Hungry");
        wasHungry = tag.getBoolean("WasHungry");
    }

    @Override
    public CompoundTag getUpdateTag() {
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
        load(pkt.getTag());

        // Обновляем рендер
        if (level != null && level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    public void setInitialState(boolean hungry) {
        this.isHungry = hungry;
        this.wasHungry = hungry;
        setChanged();
    }
}