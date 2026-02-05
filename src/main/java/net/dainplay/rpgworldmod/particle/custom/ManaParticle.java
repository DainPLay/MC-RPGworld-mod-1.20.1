package net.dainplay.rpgworldmod.particle.custom;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;

public class ManaParticle extends TextureSheetParticle {
    private final SpriteSet sprites;

    private final double xStart;
    private final double yStart;
    private final double zStart;
    private final int delayTicks;
    private boolean inDelayPhase = true;
    private int delayCounter = 0;

    ManaParticle(ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, SpriteSet spriteSet) {
        super(level, x, y, z, xSpeed, ySpeed, zSpeed);
        this.sprites = spriteSet;
        this.xStart = x;
        this.yStart = y;
        this.zStart = z;

        // Случайная задержка от 2 до 6 тиков
        this.delayTicks = random.nextInt(5) + 2;
        this.lifetime = 20 + random.nextInt(10); // Общее время жизни

        // Начальная позиция равна текущей
        this.xo = x;
        this.yo = y;
        this.zo = z;

        // Устанавливаем случайный спрайт и фиксируем его на всю жизнь
        this.setSprite(spriteSet.get(random.nextInt(4), 4));

        // Начальные скорости
        this.xd = xSpeed;
        this.yd = ySpeed;
        this.zd = zSpeed;

        // Частица не подвержена гравитации по умолчанию
        this.hasPhysics = false;

        // Размер частицы
        this.quadSize = 0.2F;

        // Начальная непрозрачность
        this.alpha = 1.0F;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    @Override
    public void move(double x, double y, double z) {
        this.setBoundingBox(this.getBoundingBox().move(x, y, z));
        this.setLocationFromBoundingbox();
    }

    @Override
    public float getQuadSize(float scaleFactor) {
        if (inDelayPhase) {
            return this.quadSize;
        }

        float f = ((float)this.age + scaleFactor) / (float)this.lifetime;
        f = Mth.clamp(f, 0.0F, 1.0F);
        return this.quadSize * (1.0F - f * f * 0.5F);
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;

        if (this.age++ >= this.lifetime) {
            this.remove();
            return;
        }

        // Не меняем спрайт с течением времени
        // this.setSpriteFromAge(this.sprites); // Убрано

        if (inDelayPhase) {
            delayCounter++;
            if (delayCounter >= delayTicks) {
                inDelayPhase = false;
                // После задержки включаем физику для падения
                this.hasPhysics = true;
            }
            // Во время задержки остаемся на месте
            this.setPos(this.x, this.y, this.z);
        } else {
            // После задержки используем движение как у EnchantmentTableParticle
            float f = (float)this.age / (float)this.lifetime;
            f = 1.0F - f;
            float f1 = 1.0F - f;
            f1 *= f1;
            f1 *= f1;

            // Параболическое движение вниз
            this.x = this.xStart + this.xd * (double)f;
            this.y = this.yStart + this.yd * (double)f - (double)(f1 * 1.2F);
            this.z = this.zStart + this.zd * (double)f;

            this.setPos(this.x, this.y, this.z);
        }

        // Плавное исчезновение в конце жизни
        // Начинаем уменьшать прозрачность во второй половине жизни
        float lifeProgress = (float)this.age / (float)this.lifetime;
        if (lifeProgress > 0.5F) {
            // От 0.5 до 1.0: альфа уменьшается от 1.0 до 0.0
            float fadeProgress = (lifeProgress - 0.5F) / 0.5F;
            fadeProgress = Mth.clamp(fadeProgress, 0.0F, 1.0F);
            // Используем квадратичную кривую для плавного затухания
            this.alpha = 1.0F - fadeProgress * fadeProgress;
        } else {
            this.alpha = 1.0F;
        }
    }

    // Убираем метод setSpriteFromAge, чтобы текстура не менялась

    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet spriteSet) {
            this.sprites = spriteSet;
        }

        @Override
        public Particle createParticle(SimpleParticleType particleType, ClientLevel level,
                                       double x, double y, double z,
                                       double xSpeed, double ySpeed, double zSpeed) {
            // Небольшие случайные скорости для эффекта "кружения" во время задержки
            double swayX = (level.random.nextDouble() - 0.5) * 0.02;
            double swayZ = (level.random.nextDouble() - 0.5) * 0.02;

            ManaParticle particle = new ManaParticle(level, x, y, z, swayX, 0.0, swayZ, this.sprites);
            particle.scale(0.5F);
            return particle;
        }
    }

    public static class SmallProvider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public SmallProvider(SpriteSet spriteSet) {
            this.sprites = spriteSet;
        }

        @Override
        public Particle createParticle(SimpleParticleType particleType, ClientLevel level,
                                       double x, double y, double z,
                                       double xSpeed, double ySpeed, double zSpeed) {
            // Небольшие случайные скорости для эффекта "кружения" во время задержки
            double swayX = (level.random.nextDouble() - 0.5) * 0.02;
            double swayZ = (level.random.nextDouble() - 0.5) * 0.02;

            ManaParticle particle = new ManaParticle(level, x, y, z, swayX, 0.0, swayZ, this.sprites);
            particle.scale(0.25F);
            return particle;
        }
    }
}