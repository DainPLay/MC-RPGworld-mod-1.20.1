package net.dainplay.rpgworldmod.network;

import net.dainplay.rpgworldmod.item.ModItems;
import net.dainplay.rpgworldmod.item.custom.MintalTriangleItem;
import net.dainplay.rpgworldmod.sounds.RPGSounds;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class C2SCheckItemForTempNBTPacket {
    private final int slotId;

    public C2SCheckItemForTempNBTPacket(int slotId) {
        this.slotId = slotId;
    }

    public static void encode(C2SCheckItemForTempNBTPacket msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.slotId);
    }

    public static C2SCheckItemForTempNBTPacket decode(FriendlyByteBuf buf) {
        return new C2SCheckItemForTempNBTPacket(buf.readInt());
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer sender = ctx.get().getSender();
            if (sender == null) return;

            AbstractContainerMenu menu = sender.containerMenu;
            if (slotId < 0 || slotId >= menu.slots.size()) return;

            ItemStack stackInSlot = menu.slots.get(slotId).getItem();
            if (stackInSlot.isEmpty()) return;

            // Обработка MintalTriangleItem
            if (stackInSlot.getItem() == ModItems.MINTAL_TRIANGLE.get()) {
                MintalTriangleItem.setVibes(stackInSlot, 0);
                // Обновляем слот после изменения
                menu.setItem(slotId, menu.getStateId(), stackInSlot);
                menu.broadcastChanges();
                return;
            }

            // Обработка NetherStarScrollItem с тегом isPickaxe
            if (stackInSlot.getItem() == ModItems.NETHER_STAR_SCROLL.get()
                    && stackInSlot.getTag() != null && stackInSlot.getTag().contains("isPickaxe", Tag.TAG_INT)) {
                CompoundTag nbtData = stackInSlot.getOrCreateTag();
                nbtData.remove("isPickaxe");
                stackInSlot.setTag(nbtData.isEmpty() ? null : nbtData);
                menu.setItem(slotId, menu.getStateId(), stackInSlot);
                menu.broadcastChanges();

                // Звук
                sender.level().playSound(null, sender.blockPosition(),
                        RPGSounds.SPELL_CONJURATION_STOP.get(),
                        SoundSource.PLAYERS, 1F,
                        (sender.level().random.nextFloat() - sender.level().random.nextFloat()) * 0.2F + 1.0F);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}