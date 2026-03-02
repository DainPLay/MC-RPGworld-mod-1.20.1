package net.dainplay.rpgworldmod.network;

import net.dainplay.rpgworldmod.item.custom.HornCoralStaffItem;
import net.dainplay.rpgworldmod.sounds.RPGSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class UseOnItemStorageBlockTargetPacket {
    private final int playerId;
    private final BlockPos targetPos;

    public UseOnItemStorageBlockTargetPacket(int playerId, BlockPos targetPos) {
        this.playerId = playerId;
        this.targetPos = targetPos;
    }

    public UseOnItemStorageBlockTargetPacket(FriendlyByteBuf buf) {
        this.playerId = buf.readInt();
        this.targetPos = buf.readBlockPos();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(playerId);
        buf.writeBlockPos(targetPos);
    }

    public static void handle(UseOnItemStorageBlockTargetPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                ItemStack itemInHand = player.getItemInHand(player.getUsedItemHand());
                if (itemInHand.getItem() instanceof HornCoralStaffItem staff) {
                    player.stopUsingItem();

                    player.level().playSound(null,
                            player.getX(), player.getY(), player.getZ(),
                            RPGSounds.STAFF_STOP.get(),
                            SoundSource.PLAYERS, 1.0F, 1.0F
                    );

                    ModMessages.sendToNearbyPlayers(
                            new LoopSoundPacket(player.getId(), false, itemInHand),
                            player.serverLevel(),
                            player.blockPosition(),
                            64.0
                    );

                    // Вызываем метод cast с BlockPos
                    staff.cast(player, msg.targetPos, itemInHand);
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}