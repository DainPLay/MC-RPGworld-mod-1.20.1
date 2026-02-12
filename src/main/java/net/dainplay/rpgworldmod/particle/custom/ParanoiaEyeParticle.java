package net.dainplay.rpgworldmod.particle.custom;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.world.entity.player.Player;
import java.util.Random;

public class ParanoiaEyeParticle extends TextureSheetParticle {
    private final SpriteSet sprites;
    private final int totalLifetime;
    private final double blinkStartOffset;

    // Константы для анимации
    private static final int CLOSED_EYE_TICKS = 2;
    private static final int HALF_CLOSED_TICKS = 2;
    private static final int BLINK_SEQUENCE_TICKS = 6; // 2+2+2 для мигания
    private static final int CLOSING_SEQUENCE_TICKS = 4; // 2+2 для закрытия в конце

    // Диапазон времени жизни: от 6 до 12 секунд (120-240 тиков)
    private static final int MIN_LIFETIME = 120; // 6 секунд * 20 тиков/секунда
    private static final int MAX_LIFETIME = 240; // 12 секунд * 20 тиков/секунда

    // Дистанция для прозрачности
    private static final float MAX_FADE_DISTANCE = 5.0f; // Полная прозрачность
    private static final float MIN_FADE_DISTANCE = 4.0f; // Нулевая прозрачность

    // Индексы спрайтов (0, 1, 2 соответствуют файлам)
    private static final int SPRITE_CLOSED = 1;    // paranoia_eye_0
    private static final int SPRITE_HALF_CLOSED = 2; // paranoia_eye_1
    private static final int SPRITE_OPEN = 3;      // paranoia_eye_2

    protected ParanoiaEyeParticle(ClientLevel level, double x, double y, double z,
                                  SpriteSet spriteSet, int lifetime) {
        super(level, x, y, z);
        this.sprites = spriteSet;
        this.totalLifetime = lifetime; // Случайное время жизни
        this.lifetime = totalLifetime;
        this.gravity = 0.0F;
        this.hasPhysics = false;

        // Вычисляем смещение для мигания (в середине жизни)
        this.blinkStartOffset = totalLifetime / 2.0 - BLINK_SEQUENCE_TICKS / 2.0;

        // Фиксированный размер
        this.quadSize = 0.25F;
        this.alpha = 1.0F; // Начальная прозрачность

        // Устанавливаем начальный спрайт
        this.setSpriteForAge(0);
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;

        if (this.age++ >= this.lifetime) {
            this.remove();
        } else {
            // Обновляем спрайт каждый тик
            this.setSpriteForAge(this.age);
        }
    }

    private void setSpriteForAge(int currentAge) {
        int spriteIndex = getSpriteIndexForAge(currentAge);
        // Получаем спрайт по индексу - всего у нас 3 спрайта
        this.setSprite(sprites.get(spriteIndex, 3));
    }

    private int getSpriteIndexForAge(int currentAge) {
        int remainingTicks = totalLifetime - currentAge;

        // 1. Начало: закрытый глаз (2 тика)
        if (currentAge < CLOSED_EYE_TICKS) {
            return SPRITE_CLOSED;
        }

        // 2. Полузакрытый глаз (2 тика)
        if (currentAge < CLOSED_EYE_TICKS + HALF_CLOSED_TICKS) {
            return SPRITE_HALF_CLOSED;
        }

        // 3. Проверяем, не началось ли закрытие в конце
        if (remainingTicks <= CLOSING_SEQUENCE_TICKS) {
            return getClosingSequenceSpriteIndex(remainingTicks);
        }

        // 4. Мигание в середине жизни
        int blinkStart = (int)Math.floor(blinkStartOffset);
        if (currentAge >= blinkStart && currentAge < blinkStart + BLINK_SEQUENCE_TICKS) {
            return getBlinkSequenceSpriteIndex(currentAge - blinkStart);
        }

        // 5. В остальное время - открытый глаз
        return SPRITE_OPEN;
    }

    private int getBlinkSequenceSpriteIndex(int blinkProgress) {
        // Последовательность мигания: paranoia_eye_1 (2 тика), paranoia_eye_0 (2 тика), paranoia_eye_1 (2 тика)
        if (blinkProgress < 2) {
            return SPRITE_HALF_CLOSED; // Первые 2 тика: полузакрытый
        } else if (blinkProgress < 4) {
            return SPRITE_CLOSED; // Следующие 2 тика: закрытый
        } else {
            return SPRITE_HALF_CLOSED; // Последние 2 тика: полузакрытый
        }
    }

    private int getClosingSequenceSpriteIndex(int remainingTicks) {
        // Закрытие в конце: paranoia_eye_1 (2 тика), paranoia_eye_0 (2 тика)
        if (remainingTicks > 2) {
            return SPRITE_HALF_CLOSED; // Первые 2 тика закрытия: полузакрытый
        } else {
            return SPRITE_CLOSED; // Последние 2 тика: закрытый
        }
    }

    @Override
    public void setSpriteFromAge(SpriteSet spriteSet) {
        // Используем наш метод для управления спрайтами
        this.setSpriteForAge(this.age);
    }

    @Override
    public float getQuadSize(float scaleFactor) {
        // Всегда возвращаем фиксированный размер
        return this.quadSize;
    }

    @Override
    public int getLightColor(float partialTick) {
        // Стандартное освещение для частиц
        return 15728880;
    }

    @Override
    public void render(VertexConsumer vertexConsumer, Camera camera, float partialTick) {
        // Получаем позицию камеры (игрока)
        double cameraX = camera.getPosition().x();
        double cameraY = camera.getPosition().y();
        double cameraZ = camera.getPosition().z();

        // Вычисляем расстояние от партикла до камеры
        double dx = this.x - cameraX;
        double dy = this.y - cameraY;
        double dz = this.z - cameraZ;
        double distanceSq = dx * dx + dy * dy + dz * dz;
        double distance = Math.sqrt(distanceSq);

        // Вычисляем прозрачность на основе расстояния
        float targetAlpha = calculateAlphaBasedOnDistance((float)distance);

        // Сохраняем текущую прозрачность
        float savedAlpha = this.alpha;

        // Устанавливаем новую прозрачность
        this.alpha = targetAlpha;

        // Рендерим с новой прозрачностью
        super.render(vertexConsumer, camera, partialTick);

        // Восстанавливаем прозрачность (опционально)
        this.alpha = savedAlpha;
    }

    private float calculateAlphaBasedOnDistance(float distance) {
        if (distance >= MAX_FADE_DISTANCE) {
            return 1.0f; // Полная непрозрачность
        } else if (distance <= MIN_FADE_DISTANCE) {
            return 0.0f; // Полная прозрачность
        } else {
            // Плавное изменение прозрачности от 0 до 1
            float normalizedDistance = (distance - MIN_FADE_DISTANCE) / (MAX_FADE_DISTANCE - MIN_FADE_DISTANCE);
            return Mth.clamp(normalizedDistance, 0.0f, 1.0f);
        }
    }

    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet spriteSet) {
            this.sprites = spriteSet;
        }

        @Override
        public Particle createParticle(SimpleParticleType particleType, ClientLevel level,
                                       double x, double y, double z,
                                       double xSpeed, double ySpeed, double zSpeed) {
            // Случайное время жизни от 6 до 12 секунд (120-240 тиков)
            int lifetime = MIN_LIFETIME + level.random.nextInt(MAX_LIFETIME - MIN_LIFETIME + 1);

            ParanoiaEyeParticle particle = new ParanoiaEyeParticle(level, x, y, z, this.sprites, lifetime);

            // Небольшое случайное смещение для естественности
            particle.xd = (level.random.nextDouble() - 0.5) * 0.01;
            particle.yd = (level.random.nextDouble() - 0.5) * 0.01;
            particle.zd = (level.random.nextDouble() - 0.5) * 0.01;

            return particle;
        }
    }
}