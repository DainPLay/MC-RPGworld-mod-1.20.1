package net.dainplay.rpgworldmod.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class UpdateItemTagMessage {
    private final int entityId;
    private final ItemStack itemStackFrom;

    public UpdateItemTagMessage(int entityId, ItemStack itemStackFrom) {
        this.entityId = entityId;
        this.itemStackFrom = itemStackFrom;
    }

    // Метод для чтения из буфера
    public static UpdateItemTagMessage decode(FriendlyByteBuf buf) {
        int entityId = buf.readInt();
        ItemStack stack = buf.readItem();
        return new UpdateItemTagMessage(entityId, stack);
    }

    // Метод для записи в буфер
    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(this.entityId);
        buf.writeItem(this.itemStackFrom);
    }

    // Метод для обработки пакета
    public void handle(Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            Player player = context.get().getSender(); // только на сервере
            if (player != null && player.level() != null) {
                Entity holder = player.level().getEntity(this.entityId);
                if (holder instanceof LivingEntity living) {
                    for (InteractionHand hand : InteractionHand.values()) {
                        ItemStack heldStack = living.getItemInHand(hand);
                        if (ItemStack.isSameItemSameTags(heldStack, this.itemStackFrom)) {
                            heldStack.setTag(this.itemStackFrom.getTag() != null ?
                                    this.itemStackFrom.getTag().copy() : null);
                            break;
                        }
                    }
                }
            }
        });
        context.get().setPacketHandled(true);
    }
}