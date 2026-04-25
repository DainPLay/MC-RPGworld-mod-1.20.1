package net.dainplay.rpgworldmod.network;

import net.dainplay.rpgworldmod.effect.ModEffects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class MirroringEffectSyncPacket {
    private final int entityId;
    private final boolean hasEffect;
    private final int amplifier;
    private final int duration;

    public MirroringEffectSyncPacket(int entityId, boolean hasEffect, int amplifier, int duration) {
        this.entityId = entityId;
        this.hasEffect = hasEffect;
        this.amplifier = amplifier;
        this.duration = duration;
    }

    public MirroringEffectSyncPacket(FriendlyByteBuf buf) {
        this.entityId = buf.readVarInt();
        this.hasEffect = buf.readBoolean();
        this.amplifier = buf.readVarInt();
        this.duration = buf.readVarInt();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeVarInt(entityId);
        buf.writeBoolean(hasEffect);
        buf.writeVarInt(amplifier);
        buf.writeVarInt(duration);
    }

    public boolean handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ClientLevel level = Minecraft.getInstance().level;
            if (level == null) return;
            Entity entity = level.getEntity(entityId);
            if (entity instanceof LivingEntity living) {
                if (hasEffect) {
                    living.addEffect(new MobEffectInstance(ModEffects.MIRRORING.get(), duration, amplifier, false, false));
                } else {
                    living.removeEffect(ModEffects.MIRRORING.get());
                }
            }
        });
        return true;
    }
}