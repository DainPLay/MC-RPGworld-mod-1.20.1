package net.dainplay.rpgworldmod.network;

import net.dainplay.rpgworldmod.enchantment.ModEnchantments;
import net.dainplay.rpgworldmod.item.custom.EmberScrollItem;
import net.dainplay.rpgworldmod.sounds.RPGSounds;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class IgniteSelfPacket {
    private final int playerId;
    private final int healthToIgnite;

    public IgniteSelfPacket(int playerId, int healthToIgnite) {
        this.playerId = playerId;
        this.healthToIgnite = healthToIgnite;
    }

    public IgniteSelfPacket(FriendlyByteBuf buf) {
        this.playerId = buf.readInt();
        this.healthToIgnite = buf.readInt();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(playerId);
        buf.writeInt(healthToIgnite);
    }

    public static void handle(IgniteSelfPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                var itemInHand = player.getItemInHand(player.getUsedItemHand());
                if (itemInHand.getItem() instanceof EmberScrollItem scroll) {
                    if (EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.NECROMANCY.get(), itemInHand) > 0) {
                        player.stopUsingItem();

                        var levelUseData = EmberScrollItem.getPlayerUseData(player.level());
                        levelUseData.remove(player.getUUID());

                        player.level().playSound(null,
                                player.getX(), player.getY(), player.getZ(),
                                RPGSounds.SPELL_NECROMANCY_STOP.get(),
                                SoundSource.PLAYERS, 1.0F, 1.0F
                        );
                        
                        // Отправляем пакет для остановки звука
                        ModMessages.sendToNearbyPlayers(
                            new LoopSoundPacket(player.getId(), false, itemInHand),
                            player.serverLevel(),
                            player.blockPosition(),
                            64.0
                        );

                        scroll.igniteSelf(player, msg.healthToIgnite, itemInHand);
                    }
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}