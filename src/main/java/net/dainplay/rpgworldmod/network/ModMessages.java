package net.dainplay.rpgworldmod.network;

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

		net.messageBuilder(FireExtinguishParticlesPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
				.decoder(FireExtinguishParticlesPacket::new)
				.encoder(FireExtinguishParticlesPacket::toBytes)
				.consumerMainThread(FireExtinguishParticlesPacket::handle)
				.add();

		net.messageBuilder(EntFaceDestroyProgressPacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
				.decoder(EntFaceDestroyProgressPacket::new)
				.encoder(EntFaceDestroyProgressPacket::toBytes)
				.consumerMainThread(EntFaceDestroyProgressPacket::handle)
				.add();

		net.messageBuilder(IllusionForceDataSyncS2CPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
				.decoder(IllusionForceDataSyncS2CPacket::new)
				.encoder(IllusionForceDataSyncS2CPacket::toBytes)
				.consumerMainThread(IllusionForceDataSyncS2CPacket::handle)
				.add();

		net.messageBuilder(PacketTireSwingInteraction.class, id(), NetworkDirection.PLAY_TO_SERVER)
				.decoder(PacketTireSwingInteraction::new)
				.encoder(PacketTireSwingInteraction::toBytes)
				.consumerMainThread(PacketTireSwingInteraction::handle)
				.add();

		net.messageBuilder(BoundEntitySyncPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
				.decoder(BoundEntitySyncPacket::new)
				.encoder(BoundEntitySyncPacket::toBytes)
				.consumerMainThread(BoundEntitySyncPacket::handle)
				.add();

		net.messageBuilder(PullPlayerPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
				.decoder(PullPlayerPacket::new)
				.encoder(PullPlayerPacket::toBytes)
				.consumerMainThread(PullPlayerPacket::handle)
				.add();

		net.messageBuilder(UpdateItemTagMessage.class, id(), NetworkDirection.PLAY_TO_SERVER)
				.decoder(UpdateItemTagMessage::decode)
				.encoder(UpdateItemTagMessage::encode)
				.consumerMainThread(UpdateItemTagMessage::handle)
				.add();

		net.messageBuilder(MoveParticlesPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
				.decoder(MoveParticlesPacket::new)
				.encoder(MoveParticlesPacket::toBytes)
				.consumerMainThread(MoveParticlesPacket::handle)
				.add();

		net.messageBuilder(EmberScrollLoopSoundPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
				.decoder(EmberScrollLoopSoundPacket::decode)
				.encoder(EmberScrollLoopSoundPacket::encode)
				.consumerMainThread(EmberScrollLoopSoundPacket::handle)
				.add();

		net.messageBuilder(CancelScrollUsagePacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
				.decoder(CancelScrollUsagePacket::new)
				.encoder(CancelScrollUsagePacket::toBytes)
				.consumerMainThread(CancelScrollUsagePacket::handle)
				.add();

		net.messageBuilder(C2SRequestTargetValidationPacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
				.decoder(C2SRequestTargetValidationPacket::new)
				.encoder(C2SRequestTargetValidationPacket::toBytes)
				.consumerMainThread(C2SRequestTargetValidationPacket::handle)
				.add();

		net.messageBuilder(S2CTargetValidationResultPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
				.decoder(S2CTargetValidationResultPacket::new)
				.encoder(S2CTargetValidationResultPacket::toBytes)
				.consumerMainThread(S2CTargetValidationResultPacket::handle)
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