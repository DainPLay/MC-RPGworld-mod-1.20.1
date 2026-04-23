package net.dainplay.rpgworldmod.network;

import net.dainplay.rpgworldmod.enchantment.ModEnchantments;
import net.dainplay.rpgworldmod.item.custom.PillagerScrollItem;
import net.dainplay.rpgworldmod.sounds.RPGSounds;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class UseRecolorWoolSpellPacket {
	private final int playerId;
	private final int colorId;

	public UseRecolorWoolSpellPacket(int playerId, int colorId) {
		this.playerId = playerId;
		this.colorId = colorId;
	}

	public UseRecolorWoolSpellPacket(FriendlyByteBuf buf) {
		this.playerId = buf.readInt();
		this.colorId = buf.readInt();
	}

	public void toBytes(FriendlyByteBuf buf) {
		buf.writeInt(playerId);
		buf.writeInt(colorId);
	}

	public static void handle(UseRecolorWoolSpellPacket msg, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			ServerPlayer player = ctx.get().getSender();
			if (player != null) {
				var itemInHand = player.getItemInHand(player.getUsedItemHand());
				if (itemInHand.getItem() instanceof PillagerScrollItem scroll) {
					if (EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.ALTERATION.get(), itemInHand) > 0) {
						player.stopUsingItem();


						var levelUseData = PillagerScrollItem.getPlayerUseData(player.level());
						levelUseData.remove(player.getUUID());

						player.level().playSound(null, player.blockPosition(),
								RPGSounds.SPELL_ALTERATION_PILLAGER.get(),
								SoundSource.PLAYERS, 1.0F, 1.0F);

						ModMessages.sendToNearbyPlayers(
								new LoopSoundPacket(player.getId(), false, itemInHand),
								player.serverLevel(),
								player.blockPosition(),
								64.0
						);

						scroll.selectColor(player, itemInHand, msg.colorId);
					}
				}
			}
		});
		ctx.get().setPacketHandled(true);
	}
}