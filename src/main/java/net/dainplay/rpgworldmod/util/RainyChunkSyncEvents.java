package net.dainplay.rpgworldmod.util;

import net.dainplay.rpgworldmod.network.ModMessages;
import net.dainplay.rpgworldmod.network.RainyChunkSyncPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class RainyChunkSyncEvents {

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            syncPlayer(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            syncPlayer(player);
        }
    }

    private static void syncPlayer(ServerPlayer player) {
        RainyChunkManager manager = RainyChunkManager.get(player.level());
        var entries = manager.getAllRainyChunks(player.level().dimension());
        RainyChunkSyncPacket packet = new RainyChunkSyncPacket(RainyChunkSyncPacket.Operation.FULL_SYNC, entries);
        ModMessages.sendToPlayer(packet, player);
    }
}