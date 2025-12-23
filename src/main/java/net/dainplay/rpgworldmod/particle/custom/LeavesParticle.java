package net.dainplay.rpgworldmod.particle.custom;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;

import javax.annotation.Nullable;


public class LeavesParticle extends RisingParticle {
   protected final SpriteSet sprites;
   private float rotSpeed;
   private final float spinAcceleration;
   LeavesParticle(ClientLevel pLevel, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed, SpriteSet sprites) {
      super(pLevel, pX, pY, pZ, pXSpeed, pYSpeed, pZSpeed);
      this.sprites = sprites;
      this.rotSpeed = (float)Math.toRadians(this.random.nextBoolean() ? -30.0D : 30.0D);
      this.spinAcceleration = (float)Math.toRadians(this.random.nextBoolean() ? -5.0D : 5.0D);
      this.roll = (float) (Math.PI * this.random.nextFloat());
      this.setSpriteFromAge(sprites);
   }

   public ParticleRenderType getRenderType() {
      return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
   }

   public void move(double pX, double pY, double pZ) {
      this.setBoundingBox(this.getBoundingBox().move(pX, pY, pZ));
      this.setLocationFromBoundingbox();
   }

   public float getQuadSize(float pScaleFactor) {
      float f = ((float)this.age + pScaleFactor) / (float)this.lifetime;
      return this.quadSize * (1.25F - f * f * 0.5F);
   }

   public void tick() {
         super.tick();
         this.rotSpeed += this.spinAcceleration / 20.0F;
         this.oRoll = this.roll;
         this.roll += this.rotSpeed / 20.0F;
   }


   public static class Provider implements ParticleProvider<SimpleParticleType> {
      private final SpriteSet sprite;

      public Provider(SpriteSet pSprites) {
         this.sprite = pSprites;
      }

      @Nullable
      @Override
      public Particle createParticle(SimpleParticleType pType, ClientLevel pLevel, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed) {
          return new LeavesParticle(pLevel, pX, pY, pZ, pXSpeed, pYSpeed, pZSpeed, sprite);
      }
   }
}