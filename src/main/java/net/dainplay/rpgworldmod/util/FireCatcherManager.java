package net.dainplay.rpgworldmod.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class FireCatcherManager extends SavedData {
    private static final String DATA_NAME = "fire_catcher_data";
    private static final int CHUNK_RADIUS = 2; // 5x5 чанков (радиус 2 от центра)

    private final Map<ResourceKey<Level>, DimensionData> dimensionData = new HashMap<>();

    // Структура для хранения данных измерения
    private static class DimensionData {
        // Карта: позиция FireCatcher -> информация о нём
        final Map<BlockPos, FireCatcherInfo> fireCatchers = new HashMap<>();

        // Карта: ключ чанка -> тип защиты (0 - нет, 1 - обычный, 2 - голодный)
        final Map<Long, Byte> chunkProtection = new HashMap<>();
    }

    private static class FireCatcherInfo {
        final boolean isHungry;
        final Set<Long> protectedChunks = new HashSet<>();

        FireCatcherInfo(boolean isHungry) {
            this.isHungry = isHungry;
        }
    }

    public static FireCatcherManager get(Level level) {
        if (level.isClientSide) {
            throw new IllegalStateException("FireCatcherManager доступен только на сервере");
        }

        ServerLevel overworld = ((ServerLevel) level).getServer().getLevel(Level.OVERWORLD);
        if (overworld == null) {
            throw new IllegalStateException("Overworld не найден!");
        }

        DimensionDataStorage storage = overworld.getDataStorage();
        return storage.computeIfAbsent(FireCatcherManager::load, FireCatcherManager::new, DATA_NAME);
    }

    public void addFireCatcher(BlockPos pos, boolean isHungry, ResourceKey<Level> dimension) {
        DimensionData data = dimensionData.computeIfAbsent(dimension, k -> new DimensionData());

        FireCatcherInfo info = new FireCatcherInfo(isHungry);
        calculateProtectedChunks(pos, info.protectedChunks);

        data.fireCatchers.put(pos, info);
        updateChunkProtection(data);
        setDirty();
    }

    public void updateFireCatcher(BlockPos pos, boolean isHungry, ResourceKey<Level> dimension) {
        DimensionData data = dimensionData.get(dimension);

        if (data != null && data.fireCatchers.containsKey(pos)) {
            FireCatcherInfo info = data.fireCatchers.get(pos);
            if (info.isHungry != isHungry) {
                info = new FireCatcherInfo(isHungry);
                calculateProtectedChunks(pos, info.protectedChunks);
                data.fireCatchers.put(pos, info);
                updateChunkProtection(data);
                setDirty();
            }
        }
    }

    public void removeFireCatcher(BlockPos pos, ResourceKey<Level> dimension, ServerLevel level) {
        DimensionData data = dimensionData.get(dimension);

        if (data != null) {
            data.fireCatchers.remove(pos);
            updateChunkProtection(data);
            setDirty();
        }
    }

    public void updateFireCatcherState(BlockPos pos, boolean oldIsHungry, boolean newIsHungry, ResourceKey<Level> dimension, ServerLevel level) {
        DimensionData data = dimensionData.get(dimension);

        if (data != null && data.fireCatchers.containsKey(pos)) {
            FireCatcherInfo info = data.fireCatchers.get(pos);

            info = new FireCatcherInfo(newIsHungry);
            calculateProtectedChunks(pos, info.protectedChunks);
            data.fireCatchers.put(pos, info);
            updateChunkProtection(data);
            setDirty();
        }
    }

    public boolean isChunkProtected(int chunkX, int chunkZ, ResourceKey<Level> dimension) {
        DimensionData data = dimensionData.get(dimension);
        if (data == null) return false;

        long chunkKey = getChunkKey(chunkX, chunkZ);
        Byte protection = data.chunkProtection.get(chunkKey);
        return protection != null && protection > 0;
    }

    public boolean isFireAllowed(int chunkX, int chunkZ, ResourceKey<Level> dimension) {
        DimensionData data = dimensionData.get(dimension);
        if (data == null) return true;

        long chunkKey = getChunkKey(chunkX, chunkZ);
        Byte protection = data.chunkProtection.get(chunkKey);

        // Обычный режим: защита от распространения
        return protection == null || protection != 1;
    }

    public void extinguishFireInProtectedChunks(BlockPos fireCatcherPos, ResourceKey<Level> dimension, ServerLevel level) {
        DimensionData data = dimensionData.get(dimension);

        if (data != null) {
            FireCatcherInfo info = data.fireCatchers.get(fireCatcherPos);
            if (info != null && info.isHungry) {
                for (long chunkKey : info.protectedChunks) {
                    extinguishFireInChunk(level, chunkKey);
                }
            }
        }
    }

    private void extinguishFireInChunk(ServerLevel level, long chunkKey) {
        if (level == null) return;

        int chunkX = (int)(chunkKey >> 32);
        int chunkZ = (int)(chunkKey & 0xFFFFFFFFL);

        // Проверяем, загружен ли чанк
        if (!level.hasChunk(chunkX, chunkZ)) return;

        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                for (int y = level.getMinBuildHeight(); y < level.getMaxBuildHeight(); y++) {
                    pos.set(chunkX * 16 + x, y, chunkZ * 16 + z);
                    if (level.getBlockState(pos).getBlock() == Blocks.FIRE) {
                        level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
                    }
                }
            }
        }
    }

    private void calculateProtectedChunks(BlockPos pos, Set<Long> result) {
        int centerChunkX = pos.getX() >> 4;
        int centerChunkZ = pos.getZ() >> 4;

        for (int dx = -CHUNK_RADIUS; dx <= CHUNK_RADIUS; dx++) {
            for (int dz = -CHUNK_RADIUS; dz <= CHUNK_RADIUS; dz++) {
                result.add(getChunkKey(centerChunkX + dx, centerChunkZ + dz));
            }
        }
    }

    private void updateChunkProtection(DimensionData data) {
        data.chunkProtection.clear();

        for (FireCatcherInfo info : data.fireCatchers.values()) {
            for (long chunkKey : info.protectedChunks) {
                byte currentProtection = data.chunkProtection.getOrDefault(chunkKey, (byte)0);
                byte newProtection = info.isHungry ? (byte)2 : (byte)1;

                // Приоритет у голодного состояния
                if (newProtection == 2 || currentProtection != 2) {
                    data.chunkProtection.put(chunkKey, newProtection);
                }
            }
        }
    }

    private long getChunkKey(int chunkX, int chunkZ) {
        return ((long)chunkX << 32) | (chunkZ & 0xFFFFFFFFL);
    }

    public static FireCatcherManager load(CompoundTag tag) {
        FireCatcherManager manager = new FireCatcherManager();

        CompoundTag dimensionsTag = tag.getCompound("Dimensions");
        for (String dimKey : dimensionsTag.getAllKeys()) {
            ResourceKey<Level> dimension = ResourceKey.create(Registries.DIMENSION, new ResourceLocation(dimKey));
            DimensionData data = new DimensionData();

            CompoundTag dimTag = dimensionsTag.getCompound(dimKey);
            CompoundTag catchersTag = dimTag.getCompound("FireCatchers");

            // Загружаем Fire Catcher'ы
            for (String posKey : catchersTag.getAllKeys()) {
                CompoundTag catcherTag = catchersTag.getCompound(posKey);
                BlockPos pos = BlockPos.of(catcherTag.getLong("Pos"));
                boolean isHungry = catcherTag.getBoolean("Hungry");

                FireCatcherInfo info = new FireCatcherInfo(isHungry);
                ListTag chunksTag = catcherTag.getList("Chunks", Tag.TAG_LONG);

                for (int i = 0; i < chunksTag.size(); i++) {
                    Tag element = chunksTag.get(i);
                    if (element.getId() == Tag.TAG_LONG) {
                        info.protectedChunks.add(((LongTag) element).getAsLong());
                    }
                }

                data.fireCatchers.put(pos, info);
            }

            manager.dimensionData.put(dimension, data);
            manager.updateChunkProtection(data);
        }

        return manager;
    }

    @Override
    public @NotNull CompoundTag save(CompoundTag tag) {
        CompoundTag dimensionsTag = new CompoundTag();

        for (Map.Entry<ResourceKey<Level>, DimensionData> entry : dimensionData.entrySet()) {
            CompoundTag dimTag = new CompoundTag();
            CompoundTag catchersTag = new CompoundTag();

            for (Map.Entry<BlockPos, FireCatcherInfo> catcherEntry : entry.getValue().fireCatchers.entrySet()) {
                CompoundTag catcherTag = new CompoundTag();
                catcherTag.putLong("Pos", catcherEntry.getKey().asLong());
                catcherTag.putBoolean("Hungry", catcherEntry.getValue().isHungry);

                ListTag chunksTag = new ListTag();
                for (long chunkKey : catcherEntry.getValue().protectedChunks) {
                    chunksTag.add(LongTag.valueOf(chunkKey));
                }
                catcherTag.put("Chunks", chunksTag);

                catchersTag.put(catcherEntry.getKey().toString(), catcherTag);
            }

            dimTag.put("FireCatchers", catchersTag);
            dimensionsTag.put(entry.getKey().location().toString(), dimTag);
        }

        tag.put("Dimensions", dimensionsTag);
        return tag;
    }
}