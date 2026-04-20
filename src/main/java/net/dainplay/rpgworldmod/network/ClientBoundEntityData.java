package net.dainplay.rpgworldmod.network;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ClientBoundEntityData {
	private static final Map<Integer, BoundEntityClientData> boundEntities = new HashMap<>();

	public static void updateEntity(BoundEntitySyncPacket.BoundEntityData data) {
		boundEntities.put(data.entityId, new BoundEntityClientData(
				data.entityId,
				data.boundPlayerId,
				new Vec3(data.playerX, data.playerY, data.playerZ),
				data.isArrow,
				System.currentTimeMillis()
		));
	}

	public static void removeEntity(int entityId) {
		boundEntities.remove(entityId);
	}

	public static Map<Integer, BoundEntityClientData> getBoundEntities() {
		long currentTime = System.currentTimeMillis();
		boundEntities.entrySet().removeIf(entry ->
				currentTime - entry.getValue().receivedTime > 5000
		);
		return boundEntities;
	}

	public static class BoundEntityClientData {
		public final int entityId;
		public final UUID boundPlayerId;
		public Vec3 playerPos;
		public final boolean isArrow;
		public final long receivedTime;

		public BoundEntityClientData(int entityId, UUID boundPlayerId, Vec3 playerPos, boolean isArrow, long receivedTime) {
			this.entityId = entityId;
			this.boundPlayerId = boundPlayerId;
			this.playerPos = playerPos;
			this.isArrow = isArrow;
			this.receivedTime = receivedTime;
		}

		public Entity getEntity() {
			return Minecraft.getInstance().level.getEntity(entityId);
		}

		public Entity getHolder() {
			return Minecraft.getInstance().level.getPlayerByUUID(boundPlayerId);
		}

		public void updatePlayerPos() {
			if (Minecraft.getInstance().level != null) {
				Entity playerEntity = Minecraft.getInstance().level.getPlayerByUUID(boundPlayerId);
				if (playerEntity != null) {
					playerPos = playerEntity.position();
				}
			}
		}
	}
}