package net.dainplay.rpgworldmod.util;

import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class FireCatcherTickHandler {

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            event.getServer().getAllLevels().forEach(level -> {
                try {
                    FireCatcherManager manager = FireCatcherManager.get(level);
                    manager.serverTick(level);
                } catch (Exception e) {
                    // Игнорируем ошибки
                }
            });
        }
    }
}