package net.dainplay.rpgworldmod.item.custom;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import net.dainplay.rpgworldmod.RPGworldMod;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.portal.PortalInfo;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.ITeleporter;

import java.util.function.Function;
import java.util.function.Predicate;

public class GasbassItem extends Item {
    // Константа для ключа биома Rie Weald (замените на актуальный путь к вашему биому)
    private static final ResourceKey<Biome> RIE_WEALD_KEY = ResourceKey.create(Registries.BIOME,
            new ResourceLocation(RPGworldMod.MOD_ID, "rie_weald"));

    // Или используйте тег, если у вас есть тег для этого биома:
    // private static final TagKey<Biome> RIE_WEALD_TAG = TagKey.create(Registries.BIOME,
    //     new ResourceLocation(RPGworldMod.MOD_ID, "rie_weald"));

    public GasbassItem(Item.Properties pProperties) {
        super(pProperties);
    }

    /**
     * Called when the player finishes using this Item (E.g. finishes eating.). Not called when the player stops using
     * the Item before the action is complete.
     */
    public ItemStack finishUsingItem(ItemStack pStack, Level pLevel, LivingEntity entity) {
        if (!pLevel.isClientSide()) {
            double teleportPosX;
            double teleportPosY;
            double teleportPosZ;
            ResourceKey<Level> teleportDimension;

            if (pStack.hasTag() && pStack.getTag().contains("rpgworldmod.return_pos_x")) {
                // Используем сохранённые координаты
                teleportPosX = pStack.getTag().getDouble("rpgworldmod.return_pos_x");
                teleportPosY = pStack.getTag().getDouble("rpgworldmod.return_pos_y");
                teleportPosZ = pStack.getTag().getDouble("rpgworldmod.return_pos_z");
                teleportDimension = DimensionType.parseLegacy(new Dynamic<>(NbtOps.INSTANCE,
                                pStack.getTag().get("rpgworldmod.return_pos_dimension")))
                        .resultOrPartial(RPGworldMod.LOGGER::error)
                        .orElse(entity.getCommandSenderWorld().dimension());
            } else {
                // Ищем ближайший биом Rie Weald
                BlockPos targetPos = findNearestRieWeald(entity, (ServerLevel) pLevel);

                if (targetPos != null) {
                    teleportPosX = targetPos.getX() + 0.5;
                    teleportPosY = targetPos.getY() + 1.0;
                    teleportPosZ = targetPos.getZ() + 0.5;
                    teleportDimension = entity.getCommandSenderWorld().dimension();
                } else {
                    // Если биом не найден, телепортируем на текущую позицию
                    teleportPosX = entity.getX();
                    teleportPosY = entity.getY();
                    teleportPosZ = entity.getZ();
                    teleportDimension = entity.getCommandSenderWorld().dimension();
                }
            }

            ITeleporter teleporter = new ITeleporter() {
                @Override
                public PortalInfo getPortalInfo(Entity entity, ServerLevel destWorld,
                                                Function<ServerLevel, PortalInfo> defaultPortalInfo) {
                    return new PortalInfo(new Vec3(teleportPosX, teleportPosY, teleportPosZ),
                            Vec3.ZERO, entity.getYRot(), entity.getXRot());
                }
                @Override
                public Entity placeEntity(Entity entity, ServerLevel currentWorld, ServerLevel destWorld, float yaw,
                                          Function<Boolean, Entity> repositionEntity) {
                    // Всегда отключаем создание портала/платформы
                    return repositionEntity.apply(false);
                }
                @Override
                public boolean playTeleportSound(ServerPlayer player, ServerLevel sourceWorld, ServerLevel destWorld)
                {
                    return false;
                }
            };

            if (entity instanceof ServerPlayer serverplayer) {
                if (serverplayer.connection.isAcceptingMessages() &&
                        serverplayer.level() == pLevel && !serverplayer.isSleeping()) {

                    if (serverplayer.isPassenger()) {
                        serverplayer.dismountTo(serverplayer.getX(), serverplayer.getY(), serverplayer.getZ());
                    }

                    if (!serverplayer.level().dimension().equals(teleportDimension)) {
                        ServerLevel targetLevel = serverplayer.getServer().getLevel(teleportDimension);
                        if (targetLevel != null) {
                            serverplayer.changeDimension(targetLevel, teleporter);
                        }
                    } else {
                        serverplayer.teleportTo(teleportPosX, teleportPosY, teleportPosZ);
                    }
                    serverplayer.resetFallDistance();
                }
            } else if (entity != null) {
                if (entity.canChangeDimensions()) {
                    if (!entity.level().dimension().equals(teleportDimension)) {
                        ServerLevel targetLevel = entity.getServer().getLevel(teleportDimension);
                        if (targetLevel != null) {
                            entity.changeDimension(targetLevel, teleporter);
                        }
                    } else {
                        entity.teleportTo(teleportPosX, teleportPosY, teleportPosZ);
                    }
                    entity.resetFallDistance();
                }
            }
        }

        ItemStack itemstack = super.finishUsingItem(pStack, pLevel, entity);
        emitSmokeParticles(entity);
        return itemstack;
    }

    /**
     * Ищет ближайший биом Rie Weald
     */
    private BlockPos findNearestRieWeald(LivingEntity entity, ServerLevel level) {
        BlockPos entityPos = entity.blockPosition();

        Predicate<Holder<Biome>> biomePredicate = holder -> holder.unwrapKey()
                .map(key -> key.equals(RIE_WEALD_KEY))
                .orElse(false);

        // Сначала пробуем поискать ближе к игроку
        int horizontalRadius = 1000; // Уменьшили радиус для первого поиска
        int verticalRadius = 100;
        int resolution = 4; // Увеличили разрешение для более точного поиска

        Pair<BlockPos, Holder<Biome>> result = level.findClosestBiome3d(
                biomePredicate,
                entityPos,
                horizontalRadius,
                verticalRadius,
                resolution
        );

        // Если не нашли в ближайшем радиусе, ищем дальше, но с другой стратегией
        if (result == null) {
            horizontalRadius = 10000;
            resolution = 16; // Уменьшаем разрешение для дальнего поиска
            result = level.findClosestBiome3d(
                    biomePredicate,
                    entityPos,
                    horizontalRadius,
                    verticalRadius,
                    resolution
            );
        }

        if (result != null) {
            BlockPos biomePos = result.getFirst();

            // Для дальних биомов делаем дополнительную проверку высоты
            if (Math.abs(entityPos.getX() - biomePos.getX()) > 500 ||
                    Math.abs(entityPos.getZ() - biomePos.getZ()) > 500) {

                // Для дальних биомов используем более надежный способ получения высоты
                return findSafeSurfacePosition(level, biomePos);
            } else {
                // Для близких биомов используем обычный способ
                int surfaceY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, biomePos.getX(), biomePos.getZ());

                if (surfaceY > level.getMinBuildHeight() && surfaceY < level.getMaxBuildHeight()) {
                    return new BlockPos(biomePos.getX(), surfaceY, biomePos.getZ());
                } else {
                    // Если не получили корректную высоту, ищем безопасную позицию
                    return findSafeSurfacePosition(level, biomePos);
                }
            }
        }

        return null;
    }

    /**
     * Находит безопасную поверхностную позицию для телепортации
     */
    private BlockPos findSafeSurfacePosition(ServerLevel level, BlockPos targetPos) {
        // Получаем высоту поверхности с использованием нескольких методов для надежности
        int surfaceY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, targetPos.getX(), targetPos.getZ());

        // Проверяем, является ли поверхность безопасной
        if (surfaceY <= level.getMinBuildHeight() + 1) {
            // Если поверхность слишком низкая, пробуем MOTION_BLOCKING
            surfaceY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, targetPos.getX(), targetPos.getZ());
        }

        // Проверяем, что позиция безопасна для появления
        if (surfaceY <= level.getMinBuildHeight() + 1) {
            // Если все еще низко, ищем безопасную позицию вручную
            return findManualSurfacePosition(level, targetPos);
        }

        // Убедимся, что игрок появится на безопасной высоте
        BlockPos spawnPos = new BlockPos(targetPos.getX(), surfaceY, targetPos.getZ());

        // Проверяем, что блок под ногами твердый
        if (!level.getBlockState(spawnPos.below()).isSolid()) {
            // Если нет, ищем ближайший твердый блок сверху
            for (int y = spawnPos.getY(); y > level.getMinBuildHeight(); y--) {
                BlockPos checkPos = new BlockPos(targetPos.getX(), y, targetPos.getZ());
                if (level.getBlockState(checkPos).isSolid() &&
                        level.isEmptyBlock(checkPos.above()) &&
                        level.isEmptyBlock(checkPos.above(2))) {
                    return checkPos.above();
                }
            }
        }

        return spawnPos;
    }

    /**
     * Ручной поиск безопасной поверхности
     */
    private BlockPos findManualSurfacePosition(ServerLevel level, BlockPos targetPos) {
        // Начинаем с максимальной высоты и идем вниз, пока не найдем безопасное место
        for (int y = level.getMaxBuildHeight() - 1; y > level.getMinBuildHeight(); y--) {
            BlockPos checkPos = new BlockPos(targetPos.getX(), y, targetPos.getZ());
            BlockPos belowPos = checkPos.below();

            // Проверяем условия для безопасного появления:
            // 1. Блок под ногами должен быть твердым
            // 2. Место появления должно быть пустым
            // 3. Над головой должно быть свободное пространство
            if (level.getBlockState(belowPos).isSolid() &&
                    level.isEmptyBlock(checkPos) &&
                    level.isEmptyBlock(checkPos.above())) {
                return checkPos;
            }
        }

        // Если ничего не нашли, возвращаем позицию на Y=64 (средняя безопасная высота)
        return new BlockPos(targetPos.getX(), Math.max(64, level.getMinBuildHeight() + 1), targetPos.getZ());
    }

    private static void emitSmokeParticles(LivingEntity entity) {
        double x = entity.getX();
        double y = entity.getY() + entity.getEyeHeight() / 2.0;
        double z = entity.getZ();

        for (int i = 0; i < 10; i++) {
            double offsetX = (entity.getRandom().nextDouble() - 0.5) * 0.8;
            double offsetY = (entity.getRandom().nextDouble() - 0.5) * 0.4;
            double offsetZ = (entity.getRandom().nextDouble() - 0.5) * 0.8;

            entity.level().addParticle(
                    ParticleTypes.LARGE_SMOKE,
                    x + offsetX,
                    y + offsetY,
                    z + offsetZ,
                    0.0, 0.02, 0.0
            );
        }
    }
}