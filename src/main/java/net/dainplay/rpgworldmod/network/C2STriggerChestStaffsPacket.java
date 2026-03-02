package net.dainplay.rpgworldmod.network;

import net.dainplay.rpgworldmod.data.tags.ModAdvancements;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class C2STriggerChestStaffsPacket {
    public C2STriggerChestStaffsPacket() {}

    public static void encode(C2STriggerChestStaffsPacket packet, FriendlyByteBuf buf) {}

    public static C2STriggerChestStaffsPacket decode(FriendlyByteBuf buf) {
        return new C2STriggerChestStaffsPacket();
    }

    public static void handle(C2STriggerChestStaffsPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                ModAdvancements.CHEST_STAFFS_TRIGGER.trigger(player);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}