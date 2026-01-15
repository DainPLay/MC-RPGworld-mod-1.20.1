package net.dainplay.rpgworldmod.network;

import net.dainplay.rpgworldmod.entity.custom.Razorleaf;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SyncRazorleafDataPacket {
    private final int entityId;
    private final int state;
    private final int attackType;
    private final int attackTimer;
    private final int tongueAnimationTime;
    private final Vec3 spitDirection;
    private final Vec3 pullDirection;
    private final boolean hasItemInMouth;

    public SyncRazorleafDataPacket(int entityId, int state, int attackType, int attackTimer,
                                   int tongueAnimationTime, Vec3 spitDirection, Vec3 pullDirection, boolean hasItemInMouth) {
        this.entityId = entityId;
        this.state = state;
        this.attackType = attackType;
        this.attackTimer = attackTimer;
        this.tongueAnimationTime = tongueAnimationTime;
        this.spitDirection = spitDirection;
        this.pullDirection = pullDirection;
        this.hasItemInMouth = hasItemInMouth;
    }

    public SyncRazorleafDataPacket(FriendlyByteBuf buf) {
        this.entityId = buf.readInt();
        this.state = buf.readInt();
        this.attackType = buf.readInt();
        this.attackTimer = buf.readInt();
        this.tongueAnimationTime = buf.readInt();
        this.spitDirection = new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble());
        this.pullDirection = new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble());
        this.hasItemInMouth = buf.readBoolean();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(entityId);
        buf.writeInt(state);
        buf.writeInt(attackType);
        buf.writeInt(attackTimer);
        buf.writeInt(tongueAnimationTime);
        buf.writeDouble(spitDirection.x);
        buf.writeDouble(spitDirection.y);
        buf.writeDouble(spitDirection.z);
        buf.writeDouble(pullDirection.x);
        buf.writeDouble(pullDirection.y);
        buf.writeDouble(pullDirection.z);
        buf.writeBoolean(hasItemInMouth);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            if (Minecraft.getInstance().level != null) {
                var entity = Minecraft.getInstance().level.getEntity(entityId);
                if (entity instanceof Razorleaf razorleaf) {
                    razorleaf.getEntityData().set(Razorleaf.DATA_STATE, state);
                    razorleaf.getEntityData().set(Razorleaf.DATA_ATTACK_TYPE, attackType);
                    razorleaf.getEntityData().set(Razorleaf.DATA_ATTACK_TIMER, attackTimer);
                    razorleaf.getEntityData().set(Razorleaf.DATA_TONGUE_ANIMATION_TIME, tongueAnimationTime);
                    razorleaf.getEntityData().set(Razorleaf.DATA_SPIT_DIRECTION_X, (float) spitDirection.x);
                    razorleaf.getEntityData().set(Razorleaf.DATA_SPIT_DIRECTION_Y, (float) spitDirection.y);
                    razorleaf.getEntityData().set(Razorleaf.DATA_SPIT_DIRECTION_Z, (float) spitDirection.z);
                    razorleaf.getEntityData().set(Razorleaf.DATA_PULL_DIRECTION_X, (float) pullDirection.x);
                    razorleaf.getEntityData().set(Razorleaf.DATA_PULL_DIRECTION_Y, (float) pullDirection.y);
                    razorleaf.getEntityData().set(Razorleaf.DATA_PULL_DIRECTION_Z, (float) pullDirection.z);
                    razorleaf.getEntityData().set(Razorleaf.DATA_HAS_ITEM_IN_MOUTH, hasItemInMouth);
                }
            }
        });
        return true;
    }
}