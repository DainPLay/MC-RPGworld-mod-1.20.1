package net.dainplay.rpgworldmod.util;

import net.dainplay.rpgworldmod.mana.FireExtinguishParticlesPacket;
import net.dainplay.rpgworldmod.mana.ModMessages;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
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
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class FireCatcherManager extends SavedData {
    private static final String DATA_NAME = "fire_catcher_data";
    private static final int CHUNK_RADIUS = 2;

    private final Map<ResourceKey<Level>, DimensionData> dimensionData = new HashMap<>();
    private final Map<ResourceKey<Level>, Set<Long>> pendingRestoration = new HashMap<>();

    private static class DimensionData {
        final Map<BlockPos, FireCatcherInfo> fireCatchers = new HashMap<>();
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

    public void serverTick(ServerLevel level) {
        ResourceKey<Level> dimension = level.dimension();

        if (pendingRestoration.containsKey(dimension)) {
            Set<Long> chunksToRestore = pendingRestoration.get(dimension);
            int processed = 0;
            Iterator<Long> iterator = chunksToRestore.iterator();

            while (iterator.hasNext() && processed < 2) {
                long chunkKey = iterator.next();
                restoreFireInChunk(level, chunkKey);
                iterator.remove();
                processed++;
            }

            if (chunksToRestore.isEmpty()) {
                pendingRestoration.remove(dimension);
            }
        }
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
            FireCatcherInfo oldInfo = data.fireCatchers.get(pos);

            // Восстанавливаем тики для старых чанков (для обоих режимов)
            if (oldInfo != null) {
                scheduleChunkRestoration(dimension, oldInfo.protectedChunks);
            }

            FireCatcherInfo newInfo = new FireCatcherInfo(isHungry);
            calculateProtectedChunks(pos, newInfo.protectedChunks);
            data.fireCatchers.put(pos, newInfo);
            updateChunkProtection(data);
            setDirty();
        }
    }

    public void removeFireCatcher(BlockPos pos, ResourceKey<Level> dimension, ServerLevel level) {
        DimensionData data = dimensionData.get(dimension);

        if (data != null) {
            FireCatcherInfo info = data.fireCatchers.get(pos);

            // Восстанавливаем тики в защищённых чанках (для обоих режимов)
            if (info != null) {
                scheduleChunkRestoration(dimension, info.protectedChunks);
            }

            data.fireCatchers.remove(pos);
            updateChunkProtection(data);
            setDirty();
        }
    }

    private void scheduleChunkRestoration(ResourceKey<Level> dimension, Set<Long> chunks) {
        pendingRestoration.computeIfAbsent(dimension, k -> new HashSet<>()).addAll(chunks);
    }

    private void restoreFireInChunk(ServerLevel level, long chunkKey) {
        int chunkX = (int)(chunkKey >> 32);
        int chunkZ = (int)(chunkKey & 0xFFFFFFFFL);

        if (!level.hasChunk(chunkX, chunkZ)) return;

        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                for (int y = level.getMinBuildHeight(); y < level.getMaxBuildHeight(); y++) {
                    pos.set(chunkX * 16 + x, y, chunkZ * 16 + z);
                    if (level.getBlockState(pos).getBlock() == Blocks.FIRE) {
                        level.scheduleTick(pos, Blocks.FIRE, level.getRandom().nextInt(10) + 10);
                    }
                }
            }
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

        // Возвращаем true только если чанк не защищён (ни в обычном, ни в голодном режиме)
        return protection == null;
    }

    private void extinguishFireInChunks(ServerLevel level, Set<Long> chunkKeys, BlockPos fireCatcherPos) {
        for (long chunkKey : chunkKeys) {
            extinguishFireInChunk(level, chunkKey, fireCatcherPos);
        }
    }

    public void extinguishFireInProtectedChunks(BlockPos fireCatcherPos, ResourceKey<Level> dimension, ServerLevel level) {
        DimensionData data = dimensionData.get(dimension);
        if (data == null) return;

        FireCatcherInfo info = data.fireCatchers.get(fireCatcherPos);
        if (info != null && info.isHungry) {
            extinguishFireInChunks(level, info.protectedChunks, fireCatcherPos);
        }
    }

    private void extinguishFireInChunk(ServerLevel level, long chunkKey, BlockPos fireCatcherPos) {
        int chunkX = (int)(chunkKey >> 32);
        int chunkZ = (int)(chunkKey & 0xFFFFFFFFL);

        if (!level.hasChunk(chunkX, chunkZ)) return;

        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                for (int y = level.getMinBuildHeight(); y < level.getMaxBuildHeight(); y++) {
                    pos.set(chunkX * 16 + x, y, chunkZ * 16 + z);
                    if (level.getBlockState(pos).getBlock() == Blocks.FIRE) {
                        level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());

                        // Отправляем пакет для создания частиц на клиенте
                        sendFireExtinguishParticles(level, fireCatcherPos, pos);
                    }
                }
            }
        }
    }

    private void sendFireExtinguishParticles(ServerLevel level, BlockPos fireCatcherPos, BlockPos firePos) {
        // Отправляем пакет всем игрокам в радиусе 64 блоков
        ModMessages.sendToNearbyPlayers(
                new FireExtinguishParticlesPacket(fireCatcherPos, firePos),
                level,
                firePos,
                64.0
        );
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