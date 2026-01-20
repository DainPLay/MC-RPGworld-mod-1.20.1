package net.dainplay.rpgworldmod.util;

import net.dainplay.rpgworldmod.network.BoundEntitySyncPacket;
import net.dainplay.rpgworldmod.network.ModMessages;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;

public class BoundEntityHelper {

    public static boolean hasBoundEntities(Player player, double pullRange) {

        Level level = player.level();
        boolean hasBound = false;

        // Проверяем стрелы
        for (AbstractArrow arrow : level.getEntitiesOfClass(AbstractArrow.class,
                player.getBoundingBox().inflate(pullRange))) {

            CompoundTag tag = arrow.getPersistentData();
            if (tag.hasUUID("BoundPlayer") &&
                    tag.getUUID("BoundPlayer").equals(player.getUUID()) &&
                    tag.getBoolean("LivingWoodArrow")) {
                return true;
            }
        }

        // Проверяем мобов
        for (LivingEntity mob : level.getEntitiesOfClass(LivingEntity.class,
                player.getBoundingBox().inflate(pullRange))) {

            if (mob == player) continue;

            CompoundTag tag = mob.getPersistentData();
            if (tag.hasUUID("BoundPlayer") &&
                    tag.getUUID("BoundPlayer").equals(player.getUUID()) &&
                    tag.getBoolean("LivingWoodBound")) {
                return true;
            }
        }

        return false;
    }

    // Вызывайте этот метод после изменения состояния привязанных сущностей
    public static void bindMobToPlayer(LivingEntity mob, Player player, Double pullRange) {
        CompoundTag tag = mob.getPersistentData();
        tag.putUUID("BoundPlayer", player.getUUID());
        tag.putLong("BoundTime", mob.level().getGameTime());
        tag.putBoolean("LivingWoodBound", true);
        tag.putDouble("BoundPullRange", pullRange);
        ModMessages.sendToNearbyPlayers(
                new BoundEntitySyncPacket(
                        mob.getId(),
                        new BoundEntitySyncPacket.BoundEntityData(
                                mob.getId(),
                                player.getUUID(),
                                player.getX(), player.getY(), player.getZ(),
                                false
                        )
                ),
                mob.level(),
                mob.blockPosition(),
                300
        );
    }

    // Добавьте вызов синхронизации при создании привязанной стрелы
    public static void bindArrowToPlayer(AbstractArrow arrow, Player player) {
        CompoundTag tag = arrow.getPersistentData();
        tag.putUUID("BoundPlayer", player.getUUID());
        tag.putLong("ShotTime", arrow.level().getGameTime());
        tag.putBoolean("LivingWoodArrow", true);


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

    public static int countBoundArrows(Player player, double pullRange) {
        int count = 0;
        Level level = player.level();

        for (AbstractArrow arrow : level.getEntitiesOfClass(AbstractArrow.class,
                player.getBoundingBox().inflate(pullRange))) {

            CompoundTag tag = arrow.getPersistentData();
            if (tag.hasUUID("BoundPlayer") &&
                    tag.getUUID("BoundPlayer").equals(player.getUUID()) &&
                    tag.getBoolean("LivingWoodArrow")) {
                count++;
            }
        }

        return count;
    }

    public static int countBoundMobs(Player player, double pullRange) {
        int count = 0;
        Level level = player.level();

        for (LivingEntity mob : level.getEntitiesOfClass(LivingEntity.class,
                player.getBoundingBox().inflate(pullRange))) {

            if (mob == player) continue;

            CompoundTag tag = mob.getPersistentData();
            if (tag.hasUUID("BoundPlayer") &&
                    tag.getUUID("BoundPlayer").equals(player.getUUID()) &&
                    tag.getBoolean("LivingWoodBound")) {
                count++;
            }
        }

        return count;
    }

    public static double calculatePullForce(LivingEntity target, ItemStack bowStack, boolean isPull) {
        // Базовая сила
        double baseForce = 0.4;

        // Получаем уровень зачарования "Отдача" на луке
        int punchLevel = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.PUNCH_ARROWS, bowStack);

        // Учитываем уровень зачарования
        if (punchLevel > 0) {
            baseForce += punchLevel * 0.6;
        }

        // Учитываем сопротивление отталкиванию у цели
        double knockbackResistance = target.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE);
        double resistanceFactor = Math.max(0.0D, 1.0D - knockbackResistance);

        // Итоговая сила
        double finalForce = baseForce * resistanceFactor;

        if (isPull) {
            finalForce += 0.15;
        }

        return Math.min(finalForce, 2.0);
    }
}