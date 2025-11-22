package net.dainplay.rpgworldmod.particle.custom;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

import javax.annotation.Nullable;

public class QuartziteParticles extends TextureSheetParticle {
    protected final SpriteSet sprites;
    QuartziteParticles(ClientLevel pLevel, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed, SpriteSet pSprites) {
        super(pLevel, pX, pY, pZ);
        this.sprites = pSprites;
        this.alpha = 0;
        this.quadSize = 0;
        //this.quadSize *= 0.75F;
        this.hasPhysics = false;
        this.lifetime = 19 + this.random.nextInt(12);
        this.gravity = 0.0F;
        this.bbHeight = 0.2f;
        this.bbWidth = 0.2f;
        this.roll = (float) (Math.PI * this.random.nextFloat());
        this.setSpriteFromAge(pSprites);
    }


    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }



    public float getQuadSize(float pScaleFactor) {
        return this.quadSize * Mth.clamp(((float)this.age + pScaleFactor) / (float)this.lifetime * 32.0F, 0.0F, 1.0F);
    }

    @Override
    public void tick() {
        super.tick();
        float sin = Mth.sin((float) (Math.PI * this.age / (this.lifetime)));
        this.alpha = (float) (Math.pow(sin, 0.2));
        this.quadSize = (float) (0.1f * Math.pow(sin, 0.4));
        this.oRoll = this.roll;
    }

    public int getLightColor(float pPartialTick) {
        float f = ((float)this.age + pPartialTick) / (float)this.lifetime;
        f = Mth.clamp(f, 0.0F, 1.0F);
        int i = super.getLightColor(pPartialTick);
        int j = i & 255;
        int k = i >> 16 & 255;
        j += (int)(f * 15.0F * 16.0F);
        if (j > 240) {
            j = 240;
        }

        return j | k << 16;
    }

    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprite;

        public Provider(SpriteSet spriteSet) {
            this.sprite = spriteSet;
        }

        @Nullable
        @Override
        public Particle createParticle(SimpleParticleType pType, ClientLevel pLevel, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed) {
            QuartziteParticles glowparticle = new QuartziteParticles(pLevel, pX, pY, pZ, 0.0D, 0.0D, 0.0D, sprite);
            glowparticle.setColor(1.0F, 0.9F, 1.0F);
            glowparticle.setParticleSpeed(0D, 0D, 0D);
            int i = 2;
            int j = 4;
            return glowparticle;
        }
    }
}
