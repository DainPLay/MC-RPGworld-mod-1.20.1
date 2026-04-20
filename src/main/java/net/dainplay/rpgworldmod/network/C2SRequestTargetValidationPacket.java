package net.dainplay.rpgworldmod.network;

import net.dainplay.rpgworldmod.util.ModTags;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.network.NetworkEvent;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

public class C2SRequestTargetValidationPacket {
	private final int targetId;

	public C2SRequestTargetValidationPacket(int targetId) {
		this.targetId = targetId;
	}

	public C2SRequestTargetValidationPacket(FriendlyByteBuf buf) {
		this.targetId = buf.readInt();
	}

	public void toBytes(FriendlyByteBuf buf) {
		buf.writeInt(targetId);
	}

	public void handle(Supplier<NetworkEvent.Context> supplier) {
		NetworkEvent.Context context = supplier.get();
		context.enqueueWork(() -> {
			ServerPlayer player = context.getSender();
			if (player == null || player.level().isClientSide) {
				return;
			}


			Entity target = player.level().getEntity(targetId);
			boolean isValid = true;


			if (target instanceof Player targetPlayer) {
				if (ForgeHooks.shouldSuppressEnderManAnger(
						null,
						targetPlayer,
						targetPlayer.getInventory().armor.get(3))) {
					isValid = false;
				}
				AtomicBoolean isDepressed = new AtomicBoolean(false);
				if (targetPlayer instanceof ServerPlayer serverPlayer) {
					serverPlayer.getCapability(PlayerManaProvider.PLAYER_MANA).ifPresent(mana -> {
						if (mana.getMana() <= 0) isDepressed.set(true);
					});
				}
				if (isDepressed.get()) isValid = false;
			}

			if (target.getType().is(ModTags.Entity.SOULLESS)) isValid = false;


			ModMessages.sendToPlayer(new S2CTargetValidationResultPacket(
					targetId, isValid), player);
		});
		context.setPacketHandled(true);
	}
}