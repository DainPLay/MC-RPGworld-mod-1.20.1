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

		net.messageBuilder(PullDownPlayerPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
				.decoder(PullDownPlayerPacket::new)
				.encoder(PullDownPlayerPacket::toBytes)
				.consumerMainThread(PullDownPlayerPacket::handle)
				.add();

		net.messageBuilder(SwingPlayerPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
				.decoder(SwingPlayerPacket::new)
				.encoder(SwingPlayerPacket::toBytes)
				.consumerMainThread(SwingPlayerPacket::handle)
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

		net.messageBuilder(LoopSoundPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
				.decoder(LoopSoundPacket::decode)
				.encoder(LoopSoundPacket::encode)
				.consumerMainThread(LoopSoundPacket::handle)
				.add();

		net.messageBuilder(UseOnAnimateTargetPacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
				.decoder(UseOnAnimateTargetPacket::new)
				.encoder(UseOnAnimateTargetPacket::toBytes)
				.consumerMainThread(UseOnAnimateTargetPacket::handle)
				.add();

		net.messageBuilder(UseOnItemStorageEntityTargetPacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
				.decoder(UseOnItemStorageEntityTargetPacket::new)
				.encoder(UseOnItemStorageEntityTargetPacket::toBytes)
				.consumerMainThread(UseOnItemStorageEntityTargetPacket::handle)
				.add();

		net.messageBuilder(UseOnItemStorageBlockTargetPacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
				.decoder(UseOnItemStorageBlockTargetPacket::new)
				.encoder(UseOnItemStorageBlockTargetPacket::toBytes)
				.consumerMainThread(UseOnItemStorageBlockTargetPacket::handle)
				.add();

		net.messageBuilder(C2STriggerChestStaffsPacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
				.decoder(C2STriggerChestStaffsPacket::decode)
				.encoder(C2STriggerChestStaffsPacket::encode)
				.consumerMainThread(C2STriggerChestStaffsPacket::handle)
				.add();

		net.messageBuilder(C2SCheckItemForTempNBTPacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
				.decoder(C2SCheckItemForTempNBTPacket::decode)
				.encoder(C2SCheckItemForTempNBTPacket::encode)
				.consumerMainThread(C2SCheckItemForTempNBTPacket::handle)
				.add();

		net.messageBuilder(UseOnItemTargetPacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
				.decoder(UseOnItemTargetPacket::new)
				.encoder(UseOnItemTargetPacket::toBytes)
				.consumerMainThread(UseOnItemTargetPacket::handle)
				.add();

		net.messageBuilder(LeftClickWhileRightClickUsePacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
				.decoder(LeftClickWhileRightClickUsePacket::new)
				.encoder(LeftClickWhileRightClickUsePacket::toBytes)
				.consumerMainThread(LeftClickWhileRightClickUsePacket::handle)
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

		net.messageBuilder(SyncEffectPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
				.decoder(SyncEffectPacket::new)
				.encoder(SyncEffectPacket::toBytes)
				.consumerMainThread(SyncEffectPacket::handle)
				.add();

		net.messageBuilder(IgniteSelfPacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
				.decoder(IgniteSelfPacket::new)
				.encoder(IgniteSelfPacket::toBytes)
				.consumerMainThread(IgniteSelfPacket::handle)
				.add();

		net.messageBuilder(ParanoiaSoundPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
				.decoder(ParanoiaSoundPacket::new)
				.encoder(ParanoiaSoundPacket::toBytes)
				.consumerMainThread(ParanoiaSoundPacket::handle)
				.add();

		net.messageBuilder(S2CGuardianAttackData.class, id(), NetworkDirection.PLAY_TO_CLIENT)
				.decoder(S2CGuardianAttackData::new)
				.encoder(S2CGuardianAttackData::toBytes)
				.consumerMainThread(S2CGuardianAttackData::handle)
				.add();

		net.messageBuilder(RainyChunkSyncPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
				.decoder(RainyChunkSyncPacket::new)
				.encoder(RainyChunkSyncPacket::toBytes)
				.consumerMainThread(RainyChunkSyncPacket::handle)
				.add();

		net.messageBuilder(S2CViewEyePacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
				.encoder(S2CViewEyePacket::encode)
				.decoder(S2CViewEyePacket::decode)
				.consumerMainThread(S2CViewEyePacket::handle)
				.add();

		net.messageBuilder(C2SEyeDestroyPacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
				.encoder(C2SEyeDestroyPacket::encode)
				.decoder(C2SEyeDestroyPacket::decode)
				.consumerMainThread(C2SEyeDestroyPacket::handle)
				.add();

		net.messageBuilder(C2SEyeRotationPacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
				.encoder(C2SEyeRotationPacket::encode)
				.decoder(C2SEyeRotationPacket::decode)
				.consumerMainThread(C2SEyeRotationPacket::handle)
				.add();

		net.messageBuilder(UseBeaconSpellPacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
				.decoder(UseBeaconSpellPacket::new)
				.encoder(UseBeaconSpellPacket::toBytes)
				.consumerMainThread(UseBeaconSpellPacket::handle)
				.add();

		net.messageBuilder(UseRecolorWoolSpellPacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
				.decoder(UseRecolorWoolSpellPacket::new)
				.encoder(UseRecolorWoolSpellPacket::toBytes)
				.consumerMainThread(UseRecolorWoolSpellPacket::handle)
				.add();

		net.messageBuilder(S2CBeamUpdatePacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
				.encoder(S2CBeamUpdatePacket::encode)
				.decoder(S2CBeamUpdatePacket::decode)
				.consumerMainThread(S2CBeamUpdatePacket::handle)
				.add();

		net.messageBuilder(PortalEffectPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
				.decoder(PortalEffectPacket::new)
				.encoder(PortalEffectPacket::toBytes)
				.consumerMainThread(PortalEffectPacket::handle)
				.add();

		net.messageBuilder(SculkStaffCDDataSyncS2CPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
				.decoder(SculkStaffCDDataSyncS2CPacket::new)
				.encoder(SculkStaffCDDataSyncS2CPacket::toBytes)
				.consumerMainThread(SculkStaffCDDataSyncS2CPacket::handle)
				.add();

		net.messageBuilder(TotemEffectPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
				.decoder(TotemEffectPacket::new)
				.encoder(TotemEffectPacket::toBytes)
				.consumerMainThread(TotemEffectPacket::handle)
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


	public static <MSG> void sendToNearbyPlayers(MSG message, Level level, BlockPos pos, double radius) {
		INSTANCE.send(PacketDistributor.NEAR.with(
				() -> new PacketDistributor.TargetPoint(pos.getX(), pos.getY(), pos.getZ(), radius, level.dimension())
		), message);
	}
}