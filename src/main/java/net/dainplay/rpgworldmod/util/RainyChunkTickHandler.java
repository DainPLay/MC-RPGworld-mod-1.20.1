package net.dainplay.rpgworldmod.util;

import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class RainyChunkTickHandler {

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            event.getServer().getAllLevels().forEach(level -> {
                try {
                    RainyChunkManager.get(level).serverTick(level);
                } catch (Exception ignored) {
                    // Менеджер может быть недоступен, если мир не загружен
                }
            });
        }
    }
}