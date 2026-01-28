package net.dainplay.rpgworldmod.network;

import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.function.Supplier;

public class MoveParticlesPacket {
    private final Vec3 startPos;
    private final Vec3 velocity;
    private final ParticleOptions particleType;
    private final boolean shouldCollide;
    private final double maxDistance;

    public MoveParticlesPacket(Vec3 startPos, Vec3 velocity, ParticleOptions particleType, boolean shouldCollide, double maxDistance) {
        this.startPos = startPos;
        this.velocity = velocity;
        this.particleType = particleType;
        this.shouldCollide = shouldCollide;
        this.maxDistance = maxDistance;
    }

    public MoveParticlesPacket(FriendlyByteBuf buf) {
        // Чтение Vec3
        this.startPos = new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble());
        this.velocity = new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble());

        // Чтение ParticleType из реестра
        ParticleType<?> type = ForgeRegistries.PARTICLE_TYPES.getValue(buf.readResourceLocation());
        if (type == null) {
            throw new IllegalArgumentException("Unknown particle type received");
        }
        this.particleType = (ParticleOptions) type;

        // Чтение boolean
        this.shouldCollide = buf.readBoolean();

        // Чтение boolean
        this.maxDistance = buf.readDouble();
    }

    public void toBytes(FriendlyByteBuf buf) {
        // Запись Vec3
        buf.writeDouble(startPos.x);
        buf.writeDouble(startPos.y);
        buf.writeDouble(startPos.z);

        buf.writeDouble(velocity.x);
        buf.writeDouble(velocity.y);
        buf.writeDouble(velocity.z);

        // Запись ParticleType
        buf.writeResourceLocation(ForgeRegistries.PARTICLE_TYPES.getKey(particleType.getType()));

        // Запись boolean
        buf.writeBoolean(shouldCollide);

        buf.writeDouble(maxDistance);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            // Клиентский код
            Level level = Minecraft.getInstance().level;
            if (level != null) {
                spawnParticle(level, startPos, velocity, particleType, shouldCollide, maxDistance);
            }
        });
        context.setPacketHandled(true);
        return true;
    }

    private void spawnParticle(Level level, Vec3 startPos, Vec3 velocity, ParticleOptions particleType, boolean shouldCollide, double maxDistance) {
        if (shouldCollide) {
            spawnParticleWithCollisionCheck(level, startPos, velocity, particleType, maxDistance);
        } else {
            spawnParticleWithoutCollision(level, startPos, velocity, particleType);
        }
    }

    private void spawnParticleWithCollisionCheck(Level level, Vec3 startPos, Vec3 velocity, ParticleOptions particleType, double maxDistance) {
        // Определяем конечную точку на основе скорости
        // Предполагаем, что частица будет лететь 1 секунду (20 тиков)
        double speedLength = velocity.length();
        double timeInSeconds = 1.0; // Время полета в секундах
        double ticksToFly = timeInSeconds * 20.0; // Конвертация в тики
        double distanceToFly = speedLength * ticksToFly;

        // Направление движения
        Vec3 direction = velocity.normalize();

        // Конечная точка без учета столкновений
        Vec3 endPoint = startPos.add(direction.scale(distanceToFly));

        // Создаем контекст для трассировки луча
        net.minecraft.world.phys.shapes.VoxelShape shape = null;
        BlockHitResult result = level.clip(
                new net.minecraft.world.level.ClipContext(
                        startPos,
                        endPoint,
                        net.minecraft.world.level.ClipContext.Block.COLLIDER,
                        net.minecraft.world.level.ClipContext.Fluid.NONE,
                        null
                )
        );

        // Если луч столкнулся с блоком, вычисляем расстояние до блока
        maxDistance = Math.min(maxDistance, distanceToFly);
        if (result.getType() == HitResult.Type.BLOCK) {
            maxDistance = startPos.distanceTo(result.getLocation());
        }

        // Если расстояние слишком мало, не создаём частицы
        if (maxDistance < 0.1) return;

        // Нормализуем скорость для движения частицы
        // Скорость частицы = (расстояние / время) * коэффициент
        double adjustedSpeed = (maxDistance / 8.5) * 15 * 0.03;
        Vec3 adjustedVelocity = direction.scale(adjustedSpeed);

        // Спавним несколько частиц
        for (int i = 0; i < 3; i++) {
            // Добавляем случайное смещение к начальной позиции
            double offsetX = (level.random.nextDouble() - 0.5) * 0.3;
            double offsetY = (level.random.nextDouble() - 0.5) * 0.3;
            double offsetZ = (level.random.nextDouble() - 0.5) * 0.3;

            Vec3 particleStartPos = startPos.add(offsetX, offsetY, offsetZ);

            level.addParticle(particleType,
                    particleStartPos.x, particleStartPos.y, particleStartPos.z,
                    adjustedVelocity.x, adjustedVelocity.y, adjustedVelocity.z);
        }
    }

    private void spawnParticleWithoutCollision(Level level, Vec3 startPos, Vec3 velocity, ParticleOptions particleType) {
        // Просто спавним частицу с заданной скоростью
        // Масштабируем скорость для правильного отображения частиц
        double speedMultiplier = 0.03; // Коэффициент для скорости частиц
        Vec3 particleVelocity = velocity.scale(speedMultiplier);

        // Спавним несколько частиц
        for (int i = 0; i < 3; i++) {
            // Добавляем случайное смещение к начальной позиции
            double offsetX = (level.random.nextDouble() - 0.5) * 0.3;
            double offsetY = (level.random.nextDouble() - 0.5) * 0.3;
            double offsetZ = (level.random.nextDouble() - 0.5) * 0.3;

            Vec3 particleStartPos = startPos.add(offsetX, offsetY, offsetZ);

            level.addParticle(particleType,
                    particleStartPos.x, particleStartPos.y, particleStartPos.z,
                    particleVelocity.x, particleVelocity.y, particleVelocity.z);
        }
    }
}