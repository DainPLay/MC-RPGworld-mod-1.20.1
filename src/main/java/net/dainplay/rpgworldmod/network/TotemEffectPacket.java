package net.dainplay.rpgworldmod.network;

import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class TotemEffectPacket {
	private final int entityId;
	private final ItemStack itemStack;

	public TotemEffectPacket(int entityId, ItemStack itemStack) {
		this.entityId = entityId;
		this.itemStack = itemStack.copy();
	}

	public TotemEffectPacket(FriendlyByteBuf buf) {
		this.entityId = buf.readInt();
		this.itemStack = buf.readItem();
	}

	public void toBytes(FriendlyByteBuf buf) {
		buf.writeInt(entityId);
		buf.writeItem(itemStack);
	}

	public void handle(Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			Minecraft mc = Minecraft.getInstance();
			Entity entity = mc.level.getEntity(entityId);
			if (entity instanceof LivingEntity living) {
				mc.particleEngine.createTrackingEmitter(living, ParticleTypes.TOTEM_OF_UNDYING, 30);
				mc.level.playLocalSound(living.getX(), living.getY(), living.getZ(),
						SoundEvents.TOTEM_USE, living.getSoundSource(),
						1.0F, 1.0F, false);
				if (living == mc.player) {
					mc.gameRenderer.displayItemActivation(itemStack);
				}
			}
		});
		ctx.get().setPacketHandled(true);
	}
}