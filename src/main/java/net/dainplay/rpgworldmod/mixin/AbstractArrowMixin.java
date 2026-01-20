package net.dainplay.rpgworldmod.mixin;

import net.dainplay.rpgworldmod.sounds.RPGSounds;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

@Mixin(AbstractArrow.class)
public abstract class AbstractArrowMixin {

    @Inject(method = "tick", at = @At("TAIL"))
    private void livingwood_onTick(CallbackInfo ci) {
        AbstractArrow arrow = (AbstractArrow)(Object)this;
        if (!arrow.level().isClientSide) {
            CompoundTag tag = arrow.getPersistentData();

            // Проверяем, является ли стрела нашей привязанной стрелой
            if (tag.hasUUID("BoundPlayer") && tag.getBoolean("LivingWoodArrow")) {
                UUID playerId = tag.getUUID("BoundPlayer");
                Player player = arrow.level().getPlayerByUUID(playerId);

                // Если игрок не найден или удалён, очищаем привязку
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

                // Проверяем расстояние
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

                // Проверяем время жизни стрелы (максимум 10 минут)
                long currentTime = arrow.level().getGameTime();
                if (tag.contains("ShotTime")) {
                    long shotTime = tag.getLong("ShotTime");
                    if (currentTime - shotTime > 12000) { // 12000 тиков = 10 минут
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
                    // Если нет времени выстрела, добавляем его
                    tag.putLong("ShotTime", currentTime);
                }
            }
        }
    }

    @Inject(method = "setKnockback", at = @At("TAIL"))
    private void livingwood_onSetKnockback(int knockback, CallbackInfo ci) {
        AbstractArrow arrow = (AbstractArrow)(Object)this;
        CompoundTag tag = arrow.getPersistentData();

        // Сохраняем силу отталкивания в теге стрелы, если она выпущена из нашего лука
        if (tag.getBoolean("LivingWoodArrow") && knockback > 0) {
            tag.putInt("StoredKnockback", knockback);
        }
    }


    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void livingwood_onReadAdditionalSaveData(CompoundTag tag, CallbackInfo ci) {
        AbstractArrow arrow = (AbstractArrow)(Object)this;

        // При загрузке стрелы с диска, если у неё была привязка,
        // но нет метки времени выстрела, добавляем текущее время
        // чтобы избежать странного поведения
        if (!arrow.level().isClientSide) {
            CompoundTag arrowTag = arrow.getPersistentData();
            if (arrowTag.hasUUID("BoundPlayer") && arrowTag.getBoolean("LivingWoodArrow")) {
                if (!arrowTag.contains("ShotTime")) {
                    arrowTag.putLong("ShotTime", arrow.level().getGameTime());
                }
                if (!arrowTag.contains("BoundPullRange")) {
                    arrowTag.putDouble("BoundPullRange", 100);
                }

                // Принудительно сбрасываем гравитацию на сервере
                // чтобы клиент получил синхронизированное состояние
                arrow.setNoGravity(true);
            }
        }
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void livingwood_onAddAdditionalSaveData(CompoundTag tag, CallbackInfo ci) {
        AbstractArrow arrow = (AbstractArrow)(Object)this;
        CompoundTag arrowTag = arrow.getPersistentData();

        // Сохраняем нашу дополнительную информацию в основной тег
        // чтобы она сохранялась при сохранении мира
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