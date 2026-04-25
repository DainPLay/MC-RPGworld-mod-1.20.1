package net.dainplay.rpgworldmod.util;

import net.dainplay.rpgworldmod.RPGworldMod;
import net.dainplay.rpgworldmod.effect.ModEffects;
import net.dainplay.rpgworldmod.network.MirroringEffectSyncPacket;
import net.dainplay.rpgworldmod.network.MirroringSeedSyncPacket;
import net.dainplay.rpgworldmod.network.ModMessages;
import net.dainplay.rpgworldmod.network.ParanoiaSoundPacket;
import net.dainplay.rpgworldmod.network.SyncEffectPacket;
import net.dainplay.rpgworldmod.sounds.RPGSounds;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = RPGworldMod.MOD_ID)
public class EffectSyncHandler {
	private static final String MIRRORING_SEED_KEY = "MirroringSeed";

	@SubscribeEvent
	public static void onEffectAdded(MobEffectEvent.Added event) {
		if (!event.getEntity().level().isClientSide()) {
			LivingEntity entity = event.getEntity();
			MobEffectInstance effect = event.getEffectInstance();

			// HAPPINESS — как раньше
			if (effect.getEffect() == ModEffects.HAPPINESS.get()) {
				ModMessages.sendToClients(new SyncEffectPacket(
						entity.getId(),
						true,
						effect.getAmplifier(),
						effect.getDuration()
				));
			}

			// MIRRORING — эффект и начальный seed
			if (effect.getEffect() == ModEffects.MIRRORING.get()) {
				ModMessages.sendToClients(new MirroringEffectSyncPacket(
						entity.getId(),
						true,
						effect.getAmplifier(),
						effect.getDuration()
				));
				generateAndSyncSeed(entity);
			}

			// PARANOIA — как раньше
			if (effect.getEffect() == ModEffects.PARANOIA.get()
					&& entity instanceof Player player) {
				ModMessages.sendToPlayer(
						new ParanoiaSoundPacket(player.getId()),
						(ServerPlayer) player
				);
			}
		}
	}

	@SubscribeEvent
	public static void onEffectRemoved(MobEffectEvent.Remove event) {
		if (!event.getEntity().level().isClientSide()) {
			LivingEntity entity = event.getEntity();

			if (event.getEffect() == ModEffects.HAPPINESS.get()) {
				ModMessages.sendToClients(new SyncEffectPacket(
						entity.getId(),
						false,
						0,
						0
				));
			}
			if (event.getEffect() == ModEffects.MIRRORING.get()) {
				ModMessages.sendToClients(new MirroringEffectSyncPacket(
						entity.getId(),
						false,
						0,
						0
				));
				entity.getPersistentData().remove(MIRRORING_SEED_KEY);
			}
		}
	}

	@SubscribeEvent
	public static void onEffectExpired(MobEffectEvent.Expired event) {
		if (!event.getEntity().level().isClientSide()) {
			LivingEntity entity = event.getEntity();
			MobEffectInstance effect = event.getEffectInstance();

			if (effect.getEffect() == ModEffects.HAPPINESS.get()) {
				ModMessages.sendToClients(new SyncEffectPacket(
						entity.getId(),
						false,
						0,
						0
				));
			}
			if (effect.getEffect() == ModEffects.MIRRORING.get()) {
				ModMessages.sendToClients(new MirroringEffectSyncPacket(
						entity.getId(),
						false,
						0,
						0
				));
				entity.getPersistentData().remove(MIRRORING_SEED_KEY);
			}
		}
	}

	@SubscribeEvent
	public static void onPlayerStartTracking(PlayerEvent.StartTracking event) {
		if (event.getTarget() instanceof LivingEntity target &&
				!event.getEntity().level().isClientSide() &&
				event.getEntity() instanceof ServerPlayer serverPlayer) {

			MobEffectInstance happiness = target.getEffect(ModEffects.HAPPINESS.get());
			if (happiness != null) {
				ModMessages.sendToPlayer(new SyncEffectPacket(
						target.getId(),
						true,
						happiness.getAmplifier(),
						happiness.getDuration()
				), serverPlayer);
			}

			MobEffectInstance mirroring = target.getEffect(ModEffects.MIRRORING.get());
			if (mirroring != null) {
				ModMessages.sendToPlayer(new MirroringEffectSyncPacket(
						target.getId(),
						true,
						mirroring.getAmplifier(),
						mirroring.getDuration()
				), serverPlayer);
				long seed = readSeed(target);
				if (seed != 0L) {
					ModMessages.sendToPlayer(new MirroringSeedSyncPacket(target.getId(), seed), serverPlayer);
				}
			}
		}
	}

	@SubscribeEvent
	public static void onEntityJoinLevel(net.minecraftforge.event.entity.EntityJoinLevelEvent event) {
		if (event.getEntity() instanceof LivingEntity entity &&
				!event.getLevel().isClientSide()) {
			event.getLevel().getServer().execute(() -> {
				MobEffectInstance happiness = entity.getEffect(ModEffects.HAPPINESS.get());
				if (happiness != null) {
					ModMessages.sendToClients(new SyncEffectPacket(
							entity.getId(),
							true,
							happiness.getAmplifier(),
							happiness.getDuration()
					));
				}

				MobEffectInstance mirroring = entity.getEffect(ModEffects.MIRRORING.get());
				if (mirroring != null) {
					ModMessages.sendToClients(new MirroringEffectSyncPacket(
							entity.getId(),
							true,
							mirroring.getAmplifier(),
							mirroring.getDuration()
					));
					long seed = readSeed(entity);
					if (seed != 0L) {
						ModMessages.sendToClients(new MirroringSeedSyncPacket(entity.getId(), seed));
					}
				}
			});
		}
	}

	public static void generateAndSyncSeed(LivingEntity entity) {
		entity.level().playSound(null, entity.getX(), entity.getY(), entity.getZ(),
				RPGSounds.SPELL_ILLUSION_PILLAGER_MOVE.get(), entity.getSoundSource(), 1.0F, 1.0F);
		if (entity.level().isClientSide) return;
		long seed = entity.getRandom().nextLong();
		if (seed == 0L) seed = 1L;
		entity.getPersistentData().putLong(MIRRORING_SEED_KEY, seed);
		ModMessages.sendToClients(new MirroringSeedSyncPacket(entity.getId(), seed));
	}

	private static long readSeed(LivingEntity entity) {
		if (entity.getPersistentData().contains(MIRRORING_SEED_KEY)) {
			return entity.getPersistentData().getLong(MIRRORING_SEED_KEY);
		}
		return 0L;
	}
}