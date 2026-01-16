// PacketTireSwingInteraction.java
package net.dainplay.rpgworldmod.network;

import net.dainplay.rpgworldmod.entity.custom.TireSwingEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketTireSwingInteraction {
    private final int entityId;
    
    public PacketTireSwingInteraction(int entityId) {
        this.entityId = entityId;
    }
    
    public PacketTireSwingInteraction(FriendlyByteBuf buffer) {
        this.entityId = buffer.readInt();
    }
    
    public void toBytes(FriendlyByteBuf buffer) {
        buffer.writeInt(this.entityId);
    }
    
    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            // Эта часть выполняется на сервере
            ServerPlayer player = context.getSender();
            if (player != null) {
                Entity entity = player.level().getEntity(this.entityId);
                if (entity instanceof TireSwingEntity tireSwing) {
                    // Попытка посадить игрока на качели
                    tireSwing.tryMountPlayer(player);
                }
            }
        });
        return true;
    }
}