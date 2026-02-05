package net.dainplay.rpgworldmod.network;

import net.dainplay.rpgworldmod.RPGworldMod;
import net.dainplay.rpgworldmod.effect.ModEffects;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SyncEffectPacket {
    private final int entityId;
    private final boolean hasEffect;
    private final int amplifier;
    private final int duration;

    public SyncEffectPacket(int entityId, boolean hasEffect, int amplifier, int duration) {
        this.entityId = entityId;
        this.hasEffect = hasEffect;
        this.amplifier = amplifier;
        this.duration = duration;
    }

    public SyncEffectPacket(FriendlyByteBuf buf) {
        this.entityId = buf.readInt();
        this.hasEffect = buf.readBoolean();
        this.amplifier = buf.readInt();
        this.duration = buf.readInt();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(entityId);
        buf.writeBoolean(hasEffect);
        buf.writeInt(amplifier);
        buf.writeInt(duration);
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            if (Minecraft.getInstance().level != null) {
                Entity entity = Minecraft.getInstance().level.getEntity(entityId);
                if (entity instanceof LivingEntity livingEntity) {
                    if (hasEffect) {
                        livingEntity.addEffect(new MobEffectInstance(
                            ModEffects.HAPPINESS.get(),
                            duration,
                            amplifier,
                            false, // ambient
                            false, // visible
                            true   // showIcon - важно для клиента
                        ));
                    } else {
                        livingEntity.removeEffect(ModEffects.HAPPINESS.get());
                    }
                }
            }
        });
        context.setPacketHandled(true);
    }
}