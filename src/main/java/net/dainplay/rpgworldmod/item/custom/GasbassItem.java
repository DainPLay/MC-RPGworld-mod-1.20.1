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

        // Определяем предикат для поиска биома
        Predicate<Holder<Biome>> biomePredicate = holder -> {
            // Проверка по ключу биома
            return holder.unwrapKey()
                    .map(key -> key.equals(RIE_WEALD_KEY))
                    .orElse(false);

            // Альтернативно, если используете тег:
            // return holder.is(RIE_WEALD_TAG);
        };

        // Параметры поиска (можно настроить по необходимости)
        int horizontalRadius = 10000; // Радиус поиска по горизонтали
        int verticalRadius = 100;     // Радиус поиска по вертикали
        int resolution = 8;           // Разрешение поиска (чем меньше, тем точнее, но медленнее)

        // Ищем ближайший биом
        Pair<BlockPos, Holder<Biome>> result = level.findClosestBiome3d(
                biomePredicate,
                entityPos,
                horizontalRadius,
                verticalRadius,
                resolution
        );

        if (result != null) {
            BlockPos biomePos = result.getFirst();

            // Находим поверхность в этой позиции
            int surfaceY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                    biomePos.getX(), biomePos.getZ());

            // Проверяем, что позиция безопасна
            if (surfaceY > level.getMinBuildHeight() && surfaceY < level.getMaxBuildHeight()) {
                return new BlockPos(biomePos.getX(), surfaceY, biomePos.getZ());
            } else {
                return new BlockPos(biomePos.getX(), biomePos.getY(), biomePos.getZ());
            }
        }

        return null;
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