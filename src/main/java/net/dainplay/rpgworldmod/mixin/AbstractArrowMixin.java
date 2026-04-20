package net.dainplay.rpgworldmod.mixin;

import net.dainplay.rpgworldmod.network.BoundEntitySyncPacket;
import net.dainplay.rpgworldmod.network.ModMessages;
import net.dainplay.rpgworldmod.sounds.RPGSounds;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

@Mixin(AbstractArrow.class)
public abstract class AbstractArrowMixin {
	@Inject(method = "tick", at = @At("TAIL"))
	private void livingwood_onTick(CallbackInfo ci) {
		AbstractArrow arrow = (AbstractArrow) (Object) this;
		if (!arrow.level().isClientSide) {
			CompoundTag tag = arrow.getPersistentData();


			if (tag.hasUUID("BoundPlayer") && tag.getBoolean("LivingWoodArrow")) {
				UUID playerId = tag.getUUID("BoundPlayer");
				Player player = arrow.level().getPlayerByUUID(playerId);


				if (player == null || player.isRemoved()) {
					tag.remove("BoundPlayer");
					tag.remove("LivingWoodArrow");
					tag.remove("ShotTime");
					tag.remove("BoundPullRange");
					arrow.setNoGravity(false);
					float pitch = 1.0F / (arrow.level().getRandom().nextFloat() * 0.4F + 1.2F);
					arrow.level().playSound(null, arrow.getX(), arrow.getY(), arrow.getZ(),
							RPGSounds.LIVING_WOOD_BOW_BREAK.get(), SoundSource.PLAYERS,
							0.5F, pitch);
					return;
				}


				if (arrow.distanceTo(player) > tag.getDouble("BoundPullRange")) {
					tag.remove("BoundPlayer");
					tag.remove("LivingWoodArrow");
					tag.remove("ShotTime");
					tag.remove("BoundPullRange");
					arrow.setNoGravity(false);
					float pitch = 1.0F / (arrow.level().getRandom().nextFloat() * 0.4F + 1.2F);
					arrow.level().playSound(null, arrow.getX(), arrow.getY(), arrow.getZ(),
							RPGSounds.LIVING_WOOD_BOW_BREAK.get(), SoundSource.PLAYERS,
							0.5F, pitch);
					arrow.level().playSound(null, player.getX(), player.getY(), player.getZ(),
							RPGSounds.LIVING_WOOD_BOW_BREAK.get(), SoundSource.PLAYERS,
							0.5F, pitch);
					return;
				}


				long currentTime = arrow.level().getGameTime();
				if (tag.contains("ShotTime")) {
					long shotTime = tag.getLong("ShotTime");
					if (currentTime - shotTime > 12000) {
						tag.remove("BoundPlayer");
						tag.remove("LivingWoodArrow");
						tag.remove("ShotTime");
						tag.remove("BoundPullRange");
						arrow.setNoGravity(false);
						float pitch = 1.0F / (arrow.level().getRandom().nextFloat() * 0.4F + 1.2F);
						arrow.level().playSound(null, arrow.getX(), arrow.getY(), arrow.getZ(),
								RPGSounds.LIVING_WOOD_BOW_BREAK.get(), SoundSource.PLAYERS,
								0.5F, pitch);
					}
				} else {
					tag.putLong("ShotTime", currentTime);
				}
			}

			if (arrow.tickCount % 5 == 0) {
				if (tag.hasUUID("BoundPlayer") && tag.getBoolean("LivingWoodArrow")) {
					Player player = arrow.level().getPlayerByUUID(tag.getUUID("BoundPlayer"));

					if (player != null) {
						ModMessages.sendToNearbyPlayers(
								new BoundEntitySyncPacket(
										arrow.getId(),
										new BoundEntitySyncPacket.BoundEntityData(
												arrow.getId(),
												player.getUUID(),
												player.getX(), player.getY(), player.getZ(),
												false
										)
								),
								arrow.level(),
								arrow.blockPosition(),
								300
						);
					}
				} else {
					ModMessages.sendToNearbyPlayers(
							new BoundEntitySyncPacket(arrow.getId()),
							arrow.level(),
							arrow.blockPosition(),
							300
					);
				}
			}
		}
	}

	@Inject(method = "setKnockback", at = @At("TAIL"))
	private void livingwood_onSetKnockback(int knockback, CallbackInfo ci) {
		AbstractArrow arrow = (AbstractArrow) (Object) this;
		CompoundTag tag = arrow.getPersistentData();


		if (tag.getBoolean("LivingWoodArrow") && knockback > 0) {
			tag.putInt("StoredKnockback", knockback);
		}
	}


	@Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
	private void livingwood_onReadAdditionalSaveData(CompoundTag tag, CallbackInfo ci) {
		AbstractArrow arrow = (AbstractArrow) (Object) this;


		if (!arrow.level().isClientSide) {
			CompoundTag arrowTag = arrow.getPersistentData();
			if (arrowTag.hasUUID("BoundPlayer") && arrowTag.getBoolean("LivingWoodArrow")) {
				if (!arrowTag.contains("ShotTime")) {
					arrowTag.putLong("ShotTime", arrow.level().getGameTime());
				}
				if (!arrowTag.contains("BoundPullRange")) {
					arrowTag.putDouble("BoundPullRange", 50);
				}


				arrow.setNoGravity(true);
			}
		}
	}

	@Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
	private void livingwood_onAddAdditionalSaveData(CompoundTag tag, CallbackInfo ci) {
		AbstractArrow arrow = (AbstractArrow) (Object) this;
		CompoundTag arrowTag = arrow.getPersistentData();


		if (arrowTag.hasUUID("BoundPlayer")) {
			tag.putUUID("BoundPlayer", arrowTag.getUUID("BoundPlayer"));
		}
		if (arrowTag.contains("LivingWoodArrow")) {
			tag.putBoolean("LivingWoodArrow", arrowTag.getBoolean("LivingWoodArrow"));
		}
		if (arrowTag.contains("ShotTime")) {
			tag.putLong("ShotTime", arrowTag.getLong("ShotTime"));
		}
		if (arrowTag.contains("BoundPullRange")) {
			tag.putDouble("BoundPullRange", arrowTag.getDouble("BoundPullRange"));
		}
	}
}