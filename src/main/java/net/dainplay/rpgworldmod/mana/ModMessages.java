package net.dainplay.rpgworldmod.mana;

import net.dainplay.rpgworldmod.RPGworldMod;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public class ModMessages {
	private static SimpleChannel INSTANCE;

	private static int packetId = 0;

	private static int id() {
		return packetId++;
	}

	public static void register() {
		SimpleChannel net = NetworkRegistry.ChannelBuilder
				.named(new ResourceLocation(RPGworldMod.MOD_ID, "messages"))
				.networkProtocolVersion(() -> "1.0")
				.clientAcceptedVersions(s -> true)
				.serverAcceptedVersions(s -> true)
				.simpleChannel();

		INSTANCE = net;

		net.messageBuilder(ManaDataSyncS2CPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
				.decoder(ManaDataSyncS2CPacket::new)
				.encoder(ManaDataSyncS2CPacket::toBytes)
				.consumerMainThread(ManaDataSyncS2CPacket::handle)
				.add();

		net.messageBuilder(MaxManaDataSyncS2CPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
				.decoder(MaxManaDataSyncS2CPacket::new)
				.encoder(MaxManaDataSyncS2CPacket::toBytes)
				.consumerMainThread(MaxManaDataSyncS2CPacket::handle)
				.add();

		net.messageBuilder(IsManaRegenBlockedDataSyncS2CPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
				.decoder(IsManaRegenBlockedDataSyncS2CPacket::new)
				.encoder(IsManaRegenBlockedDataSyncS2CPacket::toBytes)
				.consumerMainThread(IsManaRegenBlockedDataSyncS2CPacket::handle)
				.add();

		net.messageBuilder(SyncRazorleafDataPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
				.decoder(SyncRazorleafDataPacket::new)
				.encoder(SyncRazorleafDataPacket::toBytes)
				.consumerMainThread(SyncRazorleafDataPacket::handle)
				.add();

		net.messageBuilder(SyncEntityMotionPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
				.decoder(SyncEntityMotionPacket::new)
				.encoder(SyncEntityMotionPacket::toBytes)
				.consumerMainThread(SyncEntityMotionPacket::handle)
				.add();

		// Регистрация нового пакета для частиц огня
		net.messageBuilder(FireExtinguishParticlesPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
				.decoder(FireExtinguishParticlesPacket::new)
				.encoder(FireExtinguishParticlesPacket::toBytes)
				.consumerMainThread(FireExtinguishParticlesPacket::handle)
				.add();
	}

	public static <MSG> void sendToServer(MSG message) {
		INSTANCE.sendToServer(message);
	}

	public static <MSG> void sendToPlayer(MSG message, ServerPlayer player) {
		INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), message);
	}

	public static <MSG> void sendToClients(MSG message) {
		INSTANCE.send(PacketDistributor.ALL.noArg(), message);
	}

	// Новый метод для отправки пакета конкретному игроку
	public static <MSG> void sendToNearbyPlayers(MSG message, Level level, BlockPos pos, double radius) {
		INSTANCE.send(PacketDistributor.NEAR.with(
				() -> new PacketDistributor.TargetPoint(pos.getX(), pos.getY(), pos.getZ(), radius, level.dimension())
		), message);
	}
}