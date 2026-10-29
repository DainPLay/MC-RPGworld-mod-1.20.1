package net.dainplay.rpgworldmod.block.entity.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public abstract class TimedGrowthBlockEntity extends BlockEntity {

    private static final long NOT_SET = -1L;
    private static final long MIN_DAY_TICKS = 12000L;
    private static final int  EXTRA_DAY_TICKS = 6001;
    private static final long TIME_RESET_THRESHOLD = 60000L;

    private long growAtTick = NOT_SET;

    protected TimedGrowthBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public long getGrowAtTick() { return growAtTick; }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putLong("GrowAtTick", growAtTick);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        growAtTick = tag.contains("GrowAtTick") ? tag.getLong("GrowAtTick") : NOT_SET;
    }

    public void tick(Level level, BlockPos pos, BlockState state) {
        if (!(level instanceof ServerLevel serverLevel)) return;

        long now = serverLevel.getDayTime();

        if (growAtTick < 0L) {
            growAtTick = computeGrowTime(serverLevel, MIN_DAY_TICKS + serverLevel.random.nextInt(EXTRA_DAY_TICKS));
            setChanged();
            return;
        }

        if (growAtTick - now > TIME_RESET_THRESHOLD) {
            growAtTick = computeGrowTime(serverLevel, MIN_DAY_TICKS + serverLevel.random.nextInt(EXTRA_DAY_TICKS));
            setChanged();
            return;
        }

        if (serverLevel.isDay() && now >= growAtTick && canGrow(serverLevel, pos, state)) {
            doGrow(serverLevel, pos, state);
        }
    }

    protected abstract boolean canGrow(ServerLevel level, BlockPos pos, BlockState state);
    protected abstract void doGrow(ServerLevel level, BlockPos pos, BlockState state);

    public static long computeGrowTime(ServerLevel level, long dayTicksNeeded) {
        long t0 = level.getDayTime();
        long dayOffset = Math.floorMod(t0, 24000L);

        if (dayOffset < 12000L) {
            long dayLeft = 12000L - dayOffset;
            if (dayTicksNeeded < dayLeft) {
                return t0 + dayTicksNeeded;
            }
            long remaining  = dayTicksNeeded - dayLeft;
            long fullDays   = remaining / 12000L;
            long partial    = remaining % 12000L;
            return t0 + dayLeft + 12000L + fullDays * 24000L + partial;
        } else {
            long toNextDay = 24000L - dayOffset;
            long fullDays  = dayTicksNeeded / 12000L;
            long partial   = dayTicksNeeded % 12000L;
            return t0 + toNextDay + fullDays * 24000L + partial;
        }
    }
}