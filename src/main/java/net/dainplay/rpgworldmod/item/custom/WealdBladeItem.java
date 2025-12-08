package net.dainplay.rpgworldmod.item.custom;

import com.google.common.collect.Sets;
import net.dainplay.rpgworldmod.data.tags.ModAdvancements;
import net.dainplay.rpgworldmod.enchantment.ModEnchantments;
import net.dainplay.rpgworldmod.entity.custom.Fireflantern;
import net.dainplay.rpgworldmod.particle.ModParticles;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.common.ToolActions;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class WealdBladeItem extends SwordItem implements RPGtooltip {
    // Константы для сдувания
    private static final double BLOW_SPEED = 0.2;
    private static final int BLOW_RANGE = 8;
    private static final float BLOW_WIDTH = 1f;
    private static final float BLOW_HEIGHT = 2.0f;
    private static final double PARTICLE_OFFSET = 0.8; // Расстояние перед игроком для спавна частиц
    private int useTicks = 0;

    // Режимы отрисовки частиц
    public enum ParticleMode {
        DESTROY_ON_COLLISION,    // Удалять частицы при достижении твёрдого блока
        SLOW_DOWN_NEAR_BLOCKS    // Замедлять частицы вблизи блоков
    }

    // Текущий режим (можно менять по необходимости)
    private static ParticleMode PARTICLE_MODE = ParticleMode.SLOW_DOWN_NEAR_BLOCKS;

    public WealdBladeItem(Tier pTier, int pAttackDamageModifier, float pAttackSpeedModifier, Properties pProperties) {
        super(pTier, pAttackDamageModifier, pAttackSpeedModifier, pProperties);
    }

    public int getUseDuration(ItemStack pStack) {
        return 72000;
    }

    public UseAnim getUseAnimation(ItemStack pStack) {
        return UseAnim.BLOCK;
    }

    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pHand) {
        ItemStack itemstack = pPlayer.getItemInHand(pHand);
        pPlayer.startUsingItem(pHand);
        return InteractionResultHolder.consume(itemstack);
    }

    @Override
    public void onUseTick(Level pLevel, LivingEntity pLivingEntity, ItemStack pStack, int pRemainingUseDuration) {
        super.onUseTick(pLevel, pLivingEntity, pStack, pRemainingUseDuration);

        if (pLivingEntity instanceof Player player && getEnchantmentLevel(pStack, ModEnchantments.BLOWING.get()) > 0) {
            // Постоянное сдувание каждый тик
            blowEntities(pLevel, player);
            useTicks++;

            if (useTicks % 60 == 0) {
                useTicks = 1;
                pStack.hurtAndBreak(1, player, (p_289501_) -> {
                    p_289501_.broadcastBreakEvent(player.getUsedItemHand());
                });
            }

            // Спавн частиц каждый тик на клиенте
            if (pLevel.isClientSide) {
                spawnBlowParticles(pLevel, player);
            }
        }
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
        super.releaseUsing(stack, level, entity, timeLeft);
    }

    private void blowEntities(Level level, Player player) {
        if (level.isClientSide) return;

        Vec3 blowDirection = getHorizontalBlowDirection(player);

        // Создаем AABB для сканирования сущностей ТОЛЬКО перед игроком
        AABB scanArea = createBlowAABB(player, blowDirection);

        // Получаем все сущности в области
        List<Entity> entities = level.getEntitiesOfClass(Entity.class, scanArea,
                entity -> entity != player && !entity.isSpectator() && isEntityInFront(player, entity));

        for (Entity entity : entities) {
            applyBlowMotion(entity, blowDirection, player);
        }
    }

    // Проверяем, находится ли сущность перед игроком
    private boolean isEntityInFront(Player player, Entity entity) {
        Vec3 playerLook = player.getLookAngle();
        Vec3 toEntity = entity.position().subtract(player.position());

        // Нормализуем векторы
        playerLook = playerLook.normalize();
        toEntity = toEntity.normalize();

        // Если скалярное произведение положительное - сущность впереди
        return playerLook.dot(toEntity) > 0;
    }

    private Vec3 getHorizontalBlowDirection(Player player) {
        // Получаем горизонтальное направление взгляда игрока
        float yaw = player.getYRot();
        double rad = Math.toRadians(yaw);

        // Рассчитываем вектор направления только по X и Z
        double x = -Math.sin(rad);
        double z = Math.cos(rad);

        // Нормализуем вектор
        double length = Math.sqrt(x * x + z * z);
        if (length > 0) {
            x /= length;
            z /= length;
        }

        return new Vec3(x, 0, z);
    }

    private AABB createBlowAABB(Player player, Vec3 direction) {
        Vec3 start = player.position().add(0, player.getBbHeight() * 0.5, 0);
        Vec3 end = start.add(direction.scale(BLOW_RANGE));

        // Убедимся, что AABB создается только впереди игрока
        // Для этого используем только позицию игрока как начальную точку
        // и создаем AABB вперед, а не симметрично

        double minX = Math.min(start.x, end.x);
        double minY = start.y - BLOW_HEIGHT * 0.5;
        double minZ = Math.min(start.z, end.z);
        double maxX = Math.max(start.x, end.x);
        double maxY = start.y + BLOW_HEIGHT * 0.5;
        double maxZ = Math.max(start.z, end.z);

        // Расширяем только в стороны, не назад
        if (direction.x > 0) {
            minX -= BLOW_WIDTH;
            maxX += BLOW_WIDTH;
        } else if (direction.x < 0) {
            minX -= BLOW_WIDTH;
            maxX += BLOW_WIDTH;
        }

        if (direction.z > 0) {
            minZ -= BLOW_WIDTH;
            maxZ += BLOW_WIDTH;
        } else if (direction.z < 0) {
            minZ -= BLOW_WIDTH;
            maxZ += BLOW_WIDTH;
        }

        return new AABB(minX, minY, minZ, maxX, maxY, maxZ);
    }

    private boolean canSeeEntity(Level level, Vec3 from, Vec3 to) {
        ClipContext context = new ClipContext(
                from,
                to,
                ClipContext.Block.COLLIDER, // Проверяем коллизии блоков
                ClipContext.Fluid.NONE,
                null
        );

        BlockHitResult result = level.clip(context);
        return result.getType() == HitResult.Type.MISS; // Если нет столкновения с блоками
    }

    private void applyBlowMotion(Entity entity, Vec3 direction, Player player) {
        if (entity instanceof Player targetPlayer && targetPlayer.isCreative()) {
            return; // Не двигаем игроков в креативе
        }

        // Проверяем наличие блоков между игроком и целью
        Level level = player.level();
        Vec3 playerEyePos = player.getEyePosition();
        Vec3 entityPos = entity.getBoundingBox().getCenter(); // Используем центр сущности

        // Проверяем, видна ли сущность (нет ли блоков между игроком и целью)
        if (!canSeeEntity(level, playerEyePos, entityPos)) {
            return; // Если есть блоки - не сдуваем
        }

        if(entity instanceof Fireflantern && player instanceof ServerPlayer serverPlayer) {
            ModAdvancements.BLOW_AWAY_A_FIREFLANTERN.trigger(serverPlayer);
        }

        Vec3 motion = entity.getDeltaMovement();

        // Уменьшаем скорость если сущность на земле
        double actualSpeed = entity.onGround() ? BLOW_SPEED / 2.5 : BLOW_SPEED;

        // Применяем движение только по горизонтали
        Vec3 blowMotion = direction.scale(actualSpeed);

        entity.setDeltaMovement(motion.add(blowMotion));
    }

    private void spawnBlowParticles(Level level, Player player) {
        SimpleParticleType particleType = ModParticles.LEAVES.get();

        // Получаем направление взгляда игрока (горизонтальное)
        Vec3 direction = getHorizontalBlowDirection(player);

        // Базовая позиция игрока (примерно на уровне груди)
        Vec3 basePos = player.position().add(0, player.getBbHeight() * 0.7, 0);

        // Позиция для спавна частиц - немного впереди игрока
        Vec3 spawnPos = basePos.add(direction.scale(PARTICLE_OFFSET));

        // Выбираем метод спавна частиц в зависимости от режима
        switch (PARTICLE_MODE) {
            case DESTROY_ON_COLLISION:
                spawnParticlesWithCollisionCheck(level, player, particleType, spawnPos, direction);
                break;
            case SLOW_DOWN_NEAR_BLOCKS:
                spawnParticlesWithSpeedAdjustment(level, player, particleType, spawnPos, direction);
                break;
        }
    }

    /**
     * Вариант 1: Удалять частицы при достижении твёрдого блока
     */
    private void spawnParticlesWithCollisionCheck(Level level, Player player, SimpleParticleType particleType,
                                                  Vec3 spawnPos, Vec3 direction) {
        // Проверяем, есть ли блок на пути частиц
        ClipContext context = new ClipContext(
                spawnPos,
                spawnPos.add(direction.scale(BLOW_RANGE)),
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE,
                null
        );

        BlockHitResult result = level.clip(context);

        // Если луч столкнулся с блоком, вычисляем расстояние до блока
        double maxDistance = BLOW_RANGE;
        if (result.getType() == HitResult.Type.BLOCK) {
            maxDistance = spawnPos.distanceTo(result.getLocation());
        }

        // Рассчитываем скорость частиц
        double newspeed = (double) BLOW_RANGE / 8.5 * 15;
        double xSpeed = direction.x * newspeed * 0.03;
        double ySpeed = 0;
        double zSpeed = direction.z * newspeed * 0.03;

        // Спавним частицы только до блока
        for (int i = 0; i < 3; i++) {
            // Случайное смещение в пределах допустимой дистанции
            double distanceMultiplier = level.random.nextDouble() * (maxDistance / BLOW_RANGE);
            Vec3 particlePos = spawnPos.add(
                    direction.scale(distanceMultiplier * 0.5) // Частицы летят не дальше половины расстояния до блока
            );

            level.addParticle(particleType,
                    particlePos.x + (level.random.nextDouble() - 0.5) * 0.5,
                    particlePos.y + (level.random.nextDouble() - 0.5) * 0.3,
                    particlePos.z + (level.random.nextDouble() - 0.5) * 0.5,
                    xSpeed, ySpeed, zSpeed
            );
        }
    }

    /**
     * Вариант 2: Замедлять частицы вблизи блоков
     */
    private void spawnParticlesWithSpeedAdjustment(Level level, Player player, SimpleParticleType particleType,
                                                   Vec3 spawnPos, Vec3 direction) {
        // Проверяем расстояние до ближайшего блока
        ClipContext context = new ClipContext(
                spawnPos,
                spawnPos.add(direction.scale(BLOW_RANGE)),
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE,
                null
        );

        BlockHitResult result = level.clip(context);

        double speedMultiplier = 1.0;

        if (result.getType() == HitResult.Type.BLOCK) {
            double distanceToBlock = spawnPos.distanceTo(result.getLocation());

            // Если блок вплотную к игроку (менее 1 блока) - не выпускаем частицы
            if (distanceToBlock < 1.0) {
                return;
            }

            // Если блок дальше 8 блоков - полная скорость
            if (distanceToBlock >= BLOW_RANGE) {
                speedMultiplier = 1.0;
            } else {
                // Линейное уменьшение скорости: чем ближе блок, тем медленнее частицы
                // При расстоянии 1 блок: скорость 0%, при расстоянии 8+ блоков: скорость 100%
                speedMultiplier = (distanceToBlock - 1.0) / (BLOW_RANGE - 1.0);
                // Ограничиваем от 0 до 1
                speedMultiplier = Math.max(0, Math.min(1, speedMultiplier));
            }
        }

        // Рассчитываем скорость частиц с учетом множителя
        double newspeed = (double) BLOW_RANGE / 8.5 * 15 * speedMultiplier;
        double xSpeed = direction.x * newspeed * 0.03;
        double ySpeed = 0;
        double zSpeed = direction.z * newspeed * 0.03;

        // Спавним частицы
        for (int i = 0; i < 3; i++) {
            level.addParticle(particleType,
                    spawnPos.x + (level.random.nextDouble() - 0.5) * 0.5,
                    spawnPos.y + (level.random.nextDouble() - 0.5) * 0.3,
                    spawnPos.z + (level.random.nextDouble() - 0.5) * 0.5,
                    xSpeed, ySpeed, zSpeed
            );
        }
    }

    @Override
    public boolean canPerformAction(ItemStack stack, ToolAction toolAction) {
        if (getEnchantmentLevel(stack, ModEnchantments.BLOWING.get()) > 0) {
            return (ToolActions.DEFAULT_SWORD_ACTIONS.contains(toolAction));
        }
        else return (ToolActions.DEFAULT_SHIELD_ACTIONS.contains(toolAction) ||
                ToolActions.DEFAULT_SWORD_ACTIONS.contains(toolAction));
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltip, TooltipFlag pFlag) {
        RPGappendHoverText(pStack, pLevel, pTooltip, pFlag);
    }

    @Override
    public MutableComponent getDisplayFeatures(ItemStack item) {
        if (getEnchantmentLevel(item, ModEnchantments.BLOWING.get()) > 0) {
            return Component.translatable(this.getDescriptionId() + ".features.blowing");
        } else {
            return Component.translatable(this.getDescriptionId() + ".features");
        }
    }

    // Методы для изменения режима работы частиц (можно вызывать извне)
    public static void setParticleMode(ParticleMode mode) {
        PARTICLE_MODE = mode;
    }

    public static ParticleMode getParticleMode() {
        return PARTICLE_MODE;
    }
}