package net.dainplay.rpgworldmod.data.tags;

import net.dainplay.rpgworldmod.RPGworldMod;
import net.dainplay.rpgworldmod.item.ModItems;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.*;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ActionRewardHandler {

    private static final int ACTION_COUNT = 3;
    private static final int TIME_LIMIT_TICKS = 20;
    private static final Map<UUID, PlayerActionData> playerData = new HashMap<>();

    private static class PlayerActionData {
        boolean iscrouching;
        int crouchCount;
        int wealdBladeUseCount;
        long lastActionTime;

        PlayerActionData() {
            this.iscrouching = false;
            this.crouchCount = 0;
            this.wealdBladeUseCount = 0;
            this.lastActionTime = 0;
        }
    }

    @SubscribeEvent
    public static void onUpdate(LivingEvent.LivingTickEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || !(player.getMainHandItem().is(ModItems.WEALD_BLADE.get()) || player.getOffhandItem().is(ModItems.WEALD_BLADE.get())) || player.getAdvancements().getOrStartProgress(player.getServer().getAdvancements().getAdvancement(WealdBladeGesturesTrigger.ID)).isDone()) return;
        UUID playerUUID = player.getUUID();
        PlayerActionData data = playerData.computeIfAbsent(playerUUID, k -> new PlayerActionData());

        if(isActionWithinTimeLimit(player, data))
        {
        if (player.isCrouching() && !data.iscrouching) {
            data.iscrouching = true;
            data.crouchCount++;
            data.lastActionTime = player.level().getGameTime();

            checkActionCompletion(player,data);

        }
        if (!player.isCrouching()) data.iscrouching = false;
      }
    else
        {
            resetPlayerData(data);
            if(player.isCrouching() && !data.iscrouching)
            {
                data.crouchCount++;
                data.lastActionTime = player.level().getGameTime();
            }
            if (!player.isCrouching()) data.iscrouching = false;
        }



    }
    @SubscribeEvent
    public static void onItemUse(LivingEntityUseItemEvent.Start event){
        if (!(event.getEntity() instanceof ServerPlayer player) || !event.getItem().is(ModItems.WEALD_BLADE.get()) || player.getAdvancements().getOrStartProgress(player.getServer().getAdvancements().getAdvancement(WealdBladeGesturesTrigger.ID)).isDone()) return;
        ItemStack usedItem = event.getItem();

        if(usedItem.is(ModItems.WEALD_BLADE.get())){

            UUID playerUUID = player.getUUID();
            PlayerActionData data = playerData.computeIfAbsent(playerUUID, k -> new PlayerActionData());
            if(isActionWithinTimeLimit(player,data))
            {
                data.wealdBladeUseCount++;
                data.lastActionTime = player.level().getGameTime();
                checkActionCompletion(player,data);
            }
            else {
                resetPlayerData(data);
                data.wealdBladeUseCount++;
                data.lastActionTime = player.level().getGameTime();
            }

        }
    }

    private static void checkActionCompletion(ServerPlayer player,PlayerActionData data){

        if((data.crouchCount >= ACTION_COUNT && data.wealdBladeUseCount >= ACTION_COUNT) || (data.crouchCount >= 1 && data.wealdBladeUseCount >= ACTION_COUNT*2)){
            ModAdvancements.WEALD_BLADE_GESTURES.trigger(player);
            resetPlayerData(data);
        }
    }


    private static boolean isActionWithinTimeLimit(ServerPlayer player, PlayerActionData data){
        return (player.level().getGameTime() - data.lastActionTime) <= TIME_LIMIT_TICKS;
    }
    private static void resetPlayerData(PlayerActionData data){
        data.crouchCount = 0;
        data.wealdBladeUseCount = 0;
        data.lastActionTime = 0;
    }


}
