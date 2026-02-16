package net.dainplay.rpgworldmod.event;

import net.dainplay.rpgworldmod.RPGworldMod;
import net.dainplay.rpgworldmod.item.custom.EmberGemItem;
import net.dainplay.rpgworldmod.item.custom.EmberScrollItem;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = RPGworldMod.MOD_ID)
public class EmberGemEvents {

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            event.getServer().getAllLevels().forEach(EmberScrollItem::processPlayerUsageStatic);
        } else if (event.phase == TickEvent.Phase.END) {
            event.getServer().getAllLevels().forEach(EmberGemItem::processProjectilesStatic);
            event.getServer().getAllLevels().forEach(EmberScrollItem::processProjectilesStatic);
        }
    }
}