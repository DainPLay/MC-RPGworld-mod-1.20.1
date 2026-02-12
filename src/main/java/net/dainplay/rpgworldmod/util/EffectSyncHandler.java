package net.dainplay.rpgworldmod.util;

import net.dainplay.rpgworldmod.damage.ModDamageTypes;
import net.dainplay.rpgworldmod.effect.ModEffects;
import net.dainplay.rpgworldmod.network.ModMessages;
import net.dainplay.rpgworldmod.network.ParanoiaSoundPacket;
import net.dainplay.rpgworldmod.network.SyncEffectPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.dainplay.rpgworldmod.RPGworldMod;

@Mod.EventBusSubscriber(modid = RPGworldMod.MOD_ID)
public class EffectSyncHandler {
    
    @SubscribeEvent
    public static void onEffectAdded(MobEffectEvent.Added event) {
        if (!event.getEntity().level().isClientSide()) {
            if(event.getEffectInstance().getEffect() == ModEffects.HAPPINESS.get()) {
                LivingEntity entity = event.getEntity();
                MobEffectInstance effect = event.getEffectInstance();

                ModMessages.sendToClients(new SyncEffectPacket(
                        entity.getId(),
                        true,
                        effect.getAmplifier(),
                        effect.getDuration()
                ));
            }
            if(event.getEffectInstance().getEffect() == ModEffects.PARANOIA.get()
                    && event.getEntity() instanceof Player player) {
                ModMessages.sendToPlayer(
                        new ParanoiaSoundPacket(player.getId()),
                        (ServerPlayer) player
                );
            }
        }
    }
    
    @SubscribeEvent
    public static void onEffectRemoved(MobEffectEvent.Remove event) {
        if (!event.getEntity().level().isClientSide() && 
            event.getEffect() == ModEffects.HAPPINESS.get()) {
            LivingEntity entity = event.getEntity();
            
            ModMessages.sendToClients(new SyncEffectPacket(
                entity.getId(),
                false,
                0,
                0
            ));
        }
    }
    
    @SubscribeEvent
    public static void onEffectExpired(MobEffectEvent.Expired event) {
        if (!event.getEntity().level().isClientSide() && 
            event.getEffectInstance().getEffect() == ModEffects.HAPPINESS.get()) {
            LivingEntity entity = event.getEntity();
            
            ModMessages.sendToClients(new SyncEffectPacket(
                entity.getId(),
                false,
                0,
                0
            ));
        }
    }
    
    @SubscribeEvent
    public static void onPlayerStartTracking(PlayerEvent.StartTracking event) {
        if (event.getTarget() instanceof LivingEntity targetEntity && 
            !event.getEntity().level().isClientSide() && event.getEntity() instanceof ServerPlayer serverPlayer) {
            
            MobEffectInstance effect = targetEntity.getEffect(ModEffects.HAPPINESS.get());
            if (effect != null) {
                ModMessages.sendToPlayer(new SyncEffectPacket(
                    targetEntity.getId(),
                    true,
                    effect.getAmplifier(),
                    effect.getDuration()
                ), serverPlayer);
            }
        }
    }
    
    @SubscribeEvent
    public static void onEntityJoinLevel(net.minecraftforge.event.entity.EntityJoinLevelEvent event) {
        if (event.getEntity() instanceof LivingEntity entity && 
            !event.getLevel().isClientSide()) {
            
            // Немного отложим отправку, чтобы все было инициализировано
            event.getLevel().getServer().execute(() -> {
                MobEffectInstance effect = entity.getEffect(ModEffects.HAPPINESS.get());
                if (effect != null) {
                    ModMessages.sendToClients(new SyncEffectPacket(
                        entity.getId(),
                        true,
                        effect.getAmplifier(),
                        effect.getDuration()
                    ));
                }
            });
        }
    }
}