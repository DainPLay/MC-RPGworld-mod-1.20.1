package net.dainplay.rpgworldmod.network;

import net.dainplay.rpgworldmod.enchantment.ModEnchantments;
import net.dainplay.rpgworldmod.item.custom.EmberScrollItem;
import net.dainplay.rpgworldmod.item.custom.HeartOfTheSeaScrollItem;
import net.dainplay.rpgworldmod.item.custom.LivingWoodStaffItem;
import net.dainplay.rpgworldmod.sounds.RPGSounds;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class UseOnAnimateTargetPacket {
    private final int playerId;
    private final int targetId;

    public UseOnAnimateTargetPacket(int playerId, int targetId) {
        this.playerId = playerId;
        this.targetId = targetId;
    }

    public UseOnAnimateTargetPacket(FriendlyByteBuf buf) {
        this.playerId = buf.readInt();
        this.targetId = buf.readInt();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(playerId);
        buf.writeInt(targetId);
    }

    public static void handle(UseOnAnimateTargetPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                // Получаем предмет в руке игрока
                var itemInHand = player.getItemInHand(player.getUsedItemHand());
                if (itemInHand.getItem() instanceof EmberScrollItem scroll) {
                    // Проверяем, есть ли зачарование ILLUSION
                    if (EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.ILLUSION.get(), itemInHand) > 0) {
                        // Останавливаем использование
                        player.stopUsingItem();
                        
                        // Удаляем данные об использовании
                        var levelUseData = EmberScrollItem.getPlayerUseData(player.level());
                        levelUseData.remove(player.getUUID());

                        player.level().playSound(null,
                                player.getX(), player.getY(), player.getZ(),
                                RPGSounds.SPELL_ILLUSION_STOP.get(),
                                SoundSource.PLAYERS, 1.0F, 1.0F
                        );
                        
                        // Отправляем пакет для остановки звука
                        ModMessages.sendToNearbyPlayers(
                            new LoopSoundPacket(player.getId(), false, itemInHand),
                            player.serverLevel(),
                            player.blockPosition(),
                            64.0
                        );

                        LivingEntity target = null;
                        Entity entity = player.level().getEntity(msg.targetId);
                        if (entity instanceof LivingEntity livingEntity) {
                            target = livingEntity;
                        }

                        scroll.cast(player, target, itemInHand);
                    }
                }
                if (itemInHand.getItem() instanceof HeartOfTheSeaScrollItem scroll) {
                    if (EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.DESTRUCTION.get(), itemInHand) > 0) {
                        // Начинаем атаку
                        LivingEntity target = (LivingEntity) player.level().getEntity(msg.targetId);
                        if (target == null) return;

                        var levelUseData = HeartOfTheSeaScrollItem.getPlayerUseData(player.level());
                        HeartOfTheSeaScrollItem.PlayerUseData useData = levelUseData.get(player.getUUID());
                        if (useData == null) return;

                        // Устанавливаем цель
                        useData.currentTargetUUID = target.getUUID();
                        useData.attackTime = 0;

                        // Отправляем клиенту старт атаки
                        ModMessages.sendToNearbyPlayers(
                                new S2CGuardianAttackData(player.getId(), target.getId(), 0, true, false),
                                player.serverLevel(), player.blockPosition(), 64.0
                        );

                        // Не останавливаем использование
                        return;
                    }
                    if (EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.ILLUSION.get(), itemInHand) > 0) {
                        // Останавливаем использование
                        player.stopUsingItem();

                        // Удаляем данные об использовании
                        var levelUseData = HeartOfTheSeaScrollItem.getPlayerUseData(player.level());
                        levelUseData.remove(player.getUUID());

                        player.level().playSound(null,
                                player.getX(), player.getY(), player.getZ(),
                                RPGSounds.SPELL_ILLUSION_STOP.get(),
                                SoundSource.PLAYERS, 1.0F, 1.0F
                        );

                        // Отправляем пакет для остановки звука
                        ModMessages.sendToNearbyPlayers(
                                new LoopSoundPacket(player.getId(), false, itemInHand),
                                player.serverLevel(),
                                player.blockPosition(),
                                64.0
                        );

                        LivingEntity target = null;
                        Entity entity = player.level().getEntity(msg.targetId);
                        if (entity instanceof LivingEntity livingEntity) {
                            target = livingEntity;
                        }

                        scroll.cast(player, target, itemInHand);
                    }
                }
                if (itemInHand.getItem() instanceof LivingWoodStaffItem staff) {
                        // Останавливаем использование
                        player.stopUsingItem();

                        player.level().playSound(null,
                                player.getX(), player.getY(), player.getZ(),
                                RPGSounds.STAFF_STOP.get(),
                                SoundSource.PLAYERS, 1.0F, 1.0F
                        );

                        // Отправляем пакет для остановки звука
                        ModMessages.sendToNearbyPlayers(
                                new LoopSoundPacket(player.getId(), false, itemInHand),
                                player.serverLevel(),
                                player.blockPosition(),
                                64.0
                        );

                        LivingEntity target = null;
                        Entity entity = player.level().getEntity(msg.targetId);
                        if (entity instanceof LivingEntity livingEntity) {
                            target = livingEntity;
                        }

                        staff.cast(player, target, itemInHand);
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}