package net.dainplay.rpgworldmod.particle.custom;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

import javax.annotation.Nullable;

public class MosquitosParticles extends TextureSheetParticle {
   static final RandomSource RANDOM = RandomSource.create();
   protected final SpriteSet sprites;
   MosquitosParticles(ClientLevel pLevel, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed, SpriteSet pSprites) {
      super(pLevel, pX, pY, pZ, pXSpeed, pYSpeed, pZSpeed);
      this.friction = 0.96F;
      this.speedUpWhenYMotionIsBlocked = true;
      this.sprites = pSprites;
      this.quadSize *= 2F;
      this.hasPhysics = false;
      this.setSpriteFromAge(pSprites);
   }


   @Override
   public ParticleRenderType getRenderType() {
      return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
   }



   public float getQuadSize(float pScaleFactor) {
      return this.quadSize * Mth.clamp(((float)this.age + pScaleFactor) / (float)this.lifetime * 32.0F, 0.0F, 1.0F);
   }
   
   @Override
   public void tick() {
      super.tick();
   }

   public static class Provider implements ParticleProvider<SimpleParticleType> {
      private final SpriteSet sprite;

      public Provider(SpriteSet spriteSet) {
         this.sprite = spriteSet;
      }

      @Nullable
      @Override
      public Particle createParticle(SimpleParticleType pType, ClientLevel pLevel, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed) {
         MosquitosParticles mosquitosparticle = new MosquitosParticles(pLevel, pX, pY, pZ, 0.5D - MosquitosParticles.RANDOM.nextDouble(), pYSpeed, 0.5D - MosquitosParticles.RANDOM.nextDouble(), this.sprite);
         if (pLevel.random.nextBoolean()) {
            mosquitosparticle.setColor(77F/255F, 117F/255F, 117F/255F);
         } else {
            mosquitosparticle.setColor(100F/255F, 140F/255F, 140F/255F);
         }

         mosquitosparticle.yd *= (double)0.2F;
         if (pXSpeed == 0.0D && pZSpeed == 0.0D) {
            mosquitosparticle.xd *= (double)0.1F;
            mosquitosparticle.zd *= (double)0.1F;
         }

         mosquitosparticle.setLifetime((int)(8.0D / (pLevel.random.nextDouble() * 0.8D + 0.2D)));
         return mosquitosparticle;
      }
   }
}
