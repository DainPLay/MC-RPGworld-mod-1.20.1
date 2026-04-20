package net.dainplay.rpgworldmod.util;

import net.dainplay.rpgworldmod.network.ModMessages;
import net.dainplay.rpgworldmod.network.RainyChunkSyncPacket;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RainyChunkManager extends SavedData {
	private static final String DATA_NAME = "rainy_chunk_data";


	private final Map<ResourceKey<Level>, Map<Long, Long>> rainyChunks = new HashMap<>();

	public static RainyChunkManager get(Level level) {
		if (level.isClientSide) {
			throw new IllegalStateException("RainyChunkManager доступен только на сервере");
		}
		ServerLevel overworld = ((ServerLevel) level).getServer().getLevel(Level.OVERWORLD);
		if (overworld == null) {
			throw new IllegalStateException("Overworld не найден!");
		}
		DimensionDataStorage storage = overworld.getDataStorage();
		return storage.computeIfAbsent(RainyChunkManager::load, RainyChunkManager::new, DATA_NAME);
	}


	public void addRainyChunk(ResourceKey<Level> dimension, int chunkX, int chunkZ, long expiryTime) {
		long chunkKey = getChunkKey(chunkX, chunkZ);
		rainyChunks.computeIfAbsent(dimension, k -> new HashMap<>()).put(chunkKey, expiryTime);
		setDirty();
	}


	public boolean isRainyChunk(ResourceKey<Level> dimension, int chunkX, int chunkZ, long currentGameTime) {
		Map<Long, Long> dimMap = rainyChunks.get(dimension);
		if (dimMap == null) return false;
		Long expiry = dimMap.get(getChunkKey(chunkX, chunkZ));
		return expiry != null && expiry > currentGameTime;
	}

	private long getChunkKey(int chunkX, int chunkZ) {
		return ((long) chunkX << 32) | (chunkZ & 0xFFFFFFFFL);
	}


	public static RainyChunkManager load(CompoundTag tag) {
		RainyChunkManager manager = new RainyChunkManager();
		CompoundTag dimensionsTag = tag.getCompound("Dimensions");
		for (String dimKey : dimensionsTag.getAllKeys()) {
			ResourceKey<Level> dimension = ResourceKey.create(Registries.DIMENSION, new ResourceLocation(dimKey));
			CompoundTag dimTag = dimensionsTag.getCompound(dimKey);
			ListTag list = dimTag.getList("Chunks", Tag.TAG_COMPOUND);
			Map<Long, Long> map = new HashMap<>();
			for (int i = 0; i < list.size(); i++) {
				CompoundTag entryTag = list.getCompound(i);
				long chunkKey = entryTag.getLong("Chunk");
				long expiry = entryTag.getLong("Expiry");
				map.put(chunkKey, expiry);
			}
			manager.rainyChunks.put(dimension, map);
		}
		return manager;
	}

	@Override
	public @NotNull CompoundTag save(CompoundTag tag) {
		CompoundTag dimensionsTag = new CompoundTag();
		for (Map.Entry<ResourceKey<Level>, Map<Long, Long>> dimEntry : rainyChunks.entrySet()) {
			CompoundTag dimTag = new CompoundTag();
			ListTag list = new ListTag();
			for (Map.Entry<Long, Long> entry : dimEntry.getValue().entrySet()) {
				CompoundTag entryTag = new CompoundTag();
				entryTag.putLong("Chunk", entry.getKey());
				entryTag.putLong("Expiry", entry.getValue());
				list.add(entryTag);
			}
			dimTag.put("Chunks", list);
			dimensionsTag.put(dimEntry.getKey().location().toString(), dimTag);
		}
		tag.put("Dimensions", dimensionsTag);
		return tag;
	}

	public List<RainyChunkSyncPacket.Entry> getAllRainyChunks(ResourceKey<Level> dimension) {
		Map<Long, Long> dimMap = rainyChunks.get(dimension);
		if (dimMap == null) return List.of();
		List<RainyChunkSyncPacket.Entry> list = new ArrayList<>();
		for (Map.Entry<Long, Long> entry : dimMap.entrySet()) {
			long key = entry.getKey();
			int chunkX = (int) (key >> 32);
			int chunkZ = (int) key;
			list.add(new RainyChunkSyncPacket.Entry(chunkX, chunkZ, entry.getValue()));
		}
		return list;
	}


	private void sendPacketToDimension(ResourceKey<Level> dimension, RainyChunkSyncPacket packet) {
		ServerLevel level = getServerLevel(dimension);
		if (level != null) {
			level.players().forEach(player -> ModMessages.sendToPlayer(packet, player));
		}
	}

	private ServerLevel getServerLevel(ResourceKey<Level> dimension) {
		return net.minecraftforge.server.ServerLifecycleHooks.getCurrentServer().getLevel(dimension);
	}


	public void addRainyChunkAndSync(ResourceKey<Level> dimension, int chunkX, int chunkZ, long expiryTime) {
		addRainyChunk(dimension, chunkX, chunkZ, expiryTime);
		RainyChunkSyncPacket packet = new RainyChunkSyncPacket(
				RainyChunkSyncPacket.Operation.ADD,
				List.of(new RainyChunkSyncPacket.Entry(chunkX, chunkZ, expiryTime))
		);
		sendPacketToDimension(dimension, packet);
	}


	private void removeRainyChunkAndSync(ResourceKey<Level> dimension, long chunkKey) {
		Map<Long, Long> dimMap = rainyChunks.get(dimension);
		if (dimMap != null) {
			dimMap.remove(chunkKey);
			setDirty();
			int chunkX = (int) (chunkKey >> 32);
			int chunkZ = (int) chunkKey;
			RainyChunkSyncPacket packet = new RainyChunkSyncPacket(
					RainyChunkSyncPacket.Operation.REMOVE,
					List.of(new RainyChunkSyncPacket.Entry(chunkX, chunkZ, 0))
			);
			sendPacketToDimension(dimension, packet);
		}
	}


	public void serverTick(ServerLevel level) {
		ResourceKey<Level> dimension = level.dimension();
		Map<Long, Long> dimMap = rainyChunks.get(dimension);
		if (dimMap == null || dimMap.isEmpty()) return;

		long currentTime = level.getGameTime();
		List<Long> toRemove = new ArrayList<>();
		for (Map.Entry<Long, Long> entry : dimMap.entrySet()) {
			if (entry.getValue() <= currentTime) {
				toRemove.add(entry.getKey());
			}
		}
		for (long chunkKey : toRemove) {
			removeRainyChunkAndSync(dimension, chunkKey);
		}
	}
}