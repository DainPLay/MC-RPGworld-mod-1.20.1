package net.dainplay.rpgworldmod.event;

import net.dainplay.rpgworldmod.data.tags.ModAdvancements;
import net.dainplay.rpgworldmod.data.tags.FindRieWealdFirstTrigger;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.biome.Biome;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;

@Mod.EventBusSubscriber
public class BiomeEvents {

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            // Проверяем при входе игрока на сервер
            checkBiomeAndTrigger(serverPlayer);
        }
    }

    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            // Проверяем при смене измерения
            checkBiomeAndTrigger(serverPlayer);
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        // Проверяем каждую секунду (20 тиков = 1 секунда)
        if (event.player instanceof ServerPlayer serverPlayer &&
            serverPlayer.tickCount % 20 == 0) {
            checkBiomeAndTrigger(serverPlayer);
        }
    }

    private static void checkBiomeAndTrigger(ServerPlayer player) {
        if (player.level().isClientSide) return;

        ResourceLocation currentBiomeId = player.level().getBiome(player.blockPosition()).unwrapKey()
                .map(key -> key.location())
                .orElse(null);

        if (currentBiomeId != null && currentBiomeId.toString().equals("rpgworldmod:rie_weald")) {
            // Вызываем триггер достижения
            ModAdvancements.FIND_RIE_WEALD_FIRST.trigger(player);
        }
    }
}