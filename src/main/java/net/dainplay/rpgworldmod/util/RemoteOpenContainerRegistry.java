package net.dainplay.rpgworldmod.util;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class RemoteOpenContainerRegistry {
    private static final Map<Level, Map<BlockPos, Set<UUID>>> OPEN_CONTAINERS = new ConcurrentHashMap<>();

    public static void addOpener(Level level, BlockPos pos, Player player) {
        if (level.isClientSide) return;
        Map<BlockPos, Set<UUID>> levelMap = OPEN_CONTAINERS.computeIfAbsent(level, k -> new ConcurrentHashMap<>());
        Set<UUID> openers = levelMap.computeIfAbsent(pos, k -> ConcurrentHashMap.newKeySet());
        openers.add(player.getUUID());
    }

    public static void removeOpener(Level level, BlockPos pos, Player player) {
        if (level.isClientSide) return;
        Map<BlockPos, Set<UUID>> levelMap = OPEN_CONTAINERS.get(level);
        if (levelMap != null) {
            Set<UUID> openers = levelMap.get(pos);
            if (openers != null) {
                openers.remove(player.getUUID());
                if (openers.isEmpty()) {
                    levelMap.remove(pos);
                }
            }
        }
    }

    public static int getOpenerCount(Level level, BlockPos pos) {
        Map<BlockPos, Set<UUID>> levelMap = OPEN_CONTAINERS.get(level);
        if (levelMap != null) {
            Set<UUID> openers = levelMap.get(pos);
            if (openers != null) {
                return openers.size();
            }
        }
        return 0;
    }

    public static void removeAllForLevel(Level level) {
        OPEN_CONTAINERS.remove(level);
    }
}