package net.dainplay.rpgworldmod.network;

import net.dainplay.rpgworldmod.enchantment.ModEnchantments;
import net.dainplay.rpgworldmod.item.custom.EmberScrollItem;
import net.dainplay.rpgworldmod.item.custom.EnderEyeScrollItem;
import net.dainplay.rpgworldmod.item.custom.HeartOfTheSeaScrollItem;
import net.dainplay.rpgworldmod.item.custom.LivingWoodStaffItem;
import net.dainplay.rpgworldmod.item.custom.NetherStarScrollItem;
import net.dainplay.rpgworldmod.sounds.RPGSounds;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class UseBeaconSpellPacket {
    private final int playerId;
    private final int effectId;

    public UseBeaconSpellPacket(int playerId, int effectId) {
        this.playerId = playerId;
        this.effectId = effectId;
    }

    public UseBeaconSpellPacket(FriendlyByteBuf buf) {
        this.playerId = buf.readInt();
        this.effectId = buf.readInt();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(playerId);
        buf.writeInt(effectId);
    }

    public static void handle(UseBeaconSpellPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                // Получаем предмет в руке игрока
                var itemInHand = player.getItemInHand(player.getUsedItemHand());
                if (itemInHand.getItem() instanceof NetherStarScrollItem scroll) {
                    // Проверяем, есть ли зачарование ILLUSION
                    if (EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.RESTORATION.get(), itemInHand) > 0) {
                        // Останавливаем использование
                        player.stopUsingItem();
                        
                        // Удаляем данные об использовании
                        var levelUseData = EmberScrollItem.getPlayerUseData(player.level());
                        levelUseData.remove(player.getUUID());

                        player.level().playSound(null,
                                player.getX(), player.getY(), player.getZ(),
                                RPGSounds.SPELL_RESTORATION_STOP.get(),
                                SoundSource.PLAYERS, 1.0F, 1.0F
                        );
                        
                        // Отправляем пакет для остановки звука
                        ModMessages.sendToNearbyPlayers(
                            new LoopSoundPacket(player.getId(), false, itemInHand),
                            player.serverLevel(),
                            player.blockPosition(),
                            64.0
                        );

                        scroll.applyBeaconEffect(player, itemInHand, msg.effectId);
                    }
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}