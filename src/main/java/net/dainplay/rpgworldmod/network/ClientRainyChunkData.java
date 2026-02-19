package net.dainplay.rpgworldmod.network;

import net.minecraft.client.Minecraft;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class ClientRainyChunkData {
    private static class ChunkRainData {
        long expiryTime;      // оригинальное время истечения (с сервера)
        float weight;         // 0..1, текущая интенсивность для этого чанка
        long fadeStartTime;   // -1, если не в режиме затухания

        ChunkRainData(long expiryTime) {
            this.expiryTime = expiryTime;
            this.weight = 1.0f;
            this.fadeStartTime = -1;
        }

        void startFade(long gameTime) {
            if (fadeStartTime == -1) {
                fadeStartTime = gameTime;
            }
        }

        void tick(long gameTime) {
            if (fadeStartTime != -1) {
                float progress = (gameTime - fadeStartTime) / (float) FADE_DURATION;
                weight = Math.max(0, 1.0f - progress);
            } else if (gameTime > expiryTime) {
                // Автоматическое затухание, если серверный пакет запоздал
                startFade(gameTime);
            }
        }
    }

    private static final Map<Long, ChunkRainData> RAINY_CHUNKS = new HashMap<>();
    private static float rainLevel = 0.0F;
    private static final int FADE_DURATION = 40; // тики (2 секунды при 20 tps)

    public static boolean isRainyChunk(int chunkX, int chunkZ, long gameTime) {
        long key = getChunkKey(chunkX, chunkZ);
        ChunkRainData data = RAINY_CHUNKS.get(key);
        return data != null && data.weight > 0;
    }

    public static float getRainLevel() {
        return rainLevel;
    }

    public static void tick() {
        Level level = Minecraft.getInstance().level;
        if (level == null) {
            // Если мир выгружен, очищаем данные
            if (!RAINY_CHUNKS.isEmpty()) {
                RAINY_CHUNKS.clear();
                rainLevel = 0;
            }
            return;
        }
        long gameTime = level.getGameTime();

        Iterator<Map.Entry<Long, ChunkRainData>> iter = RAINY_CHUNKS.entrySet().iterator();
        float totalWeight = 0;
        while (iter.hasNext()) {
            Map.Entry<Long, ChunkRainData> entry = iter.next();
            ChunkRainData data = entry.getValue();
            data.tick(gameTime);
            if (data.weight <= 0) {
                iter.remove();
            } else {
                totalWeight += data.weight;
            }
        }

        // Целевая интенсивность: полная при 25 активных чанках (5x5)
        float target = Math.min(1.0F, totalWeight / 25.0F);
        if (target > rainLevel) {
            rainLevel = Math.min(target, rainLevel + 0.01F);
        } else if (target < rainLevel) {
            rainLevel = Math.max(target, rainLevel - 0.1F);
        }
    }

    private static long getChunkKey(int chunkX, int chunkZ) {
        return ((long) chunkX << 32) | (chunkZ & 0xFFFFFFFFL);
    }

    public static void handlePacket(RainyChunkSyncPacket.Operation op, List<RainyChunkSyncPacket.Entry> entries) {
        Level level = Minecraft.getInstance().level;
        long gameTime = level != null ? level.getGameTime() : 0;

        switch (op) {
            case FULL_SYNC:
                RAINY_CHUNKS.clear();
                for (RainyChunkSyncPacket.Entry e : entries) {
                    RAINY_CHUNKS.put(getChunkKey(e.chunkX, e.chunkZ), new ChunkRainData(e.expiryTime));
                }
                break;
            case ADD:
                for (RainyChunkSyncPacket.Entry e : entries) {
                    long key = getChunkKey(e.chunkX, e.chunkZ);
                    ChunkRainData data = RAINY_CHUNKS.get(key);
                    if (data == null) {
                        data = new ChunkRainData(e.expiryTime);
                        RAINY_CHUNKS.put(key, data);
                    } else {
                        // Чанк уже есть – обновляем время и сбрасываем затухание
                        data.expiryTime = e.expiryTime;
                        data.weight = 1.0f;
                        data.fadeStartTime = -1;
                    }
                }
                break;
            case REMOVE:
                for (RainyChunkSyncPacket.Entry e : entries) {
                    long key = getChunkKey(e.chunkX, e.chunkZ);
                    ChunkRainData data = RAINY_CHUNKS.get(key);
                    if (data != null) {
                        if (level != null) {
                            data.startFade(gameTime);
                        } else {
                            // Если мир недоступен (например, при логине), удаляем сразу
                            RAINY_CHUNKS.remove(key);
                        }
                    }
                }
                break;
        }
    }

    public static void clear() {
        RAINY_CHUNKS.clear();
        rainLevel = 0;
    }
}