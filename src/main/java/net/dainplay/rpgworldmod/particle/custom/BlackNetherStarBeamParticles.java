package net.dainplay.rpgworldmod.particle.custom;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;

import javax.annotation.Nullable;

public class BlackNetherStarBeamParticles extends TextureSheetParticle {
	protected final SpriteSet sprites;

	BlackNetherStarBeamParticles(ClientLevel pLevel, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed, SpriteSet pSprites) {
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

	@Override
	public float getQuadSize(float pScaleFactor) {
		// Линейное уменьшение размера от полного в начале до 0 в конце жизни
		float progress = (this.age + pScaleFactor) / (float) this.lifetime;
		float scaleFactor = 1.0F - progress; // от 1 до 0
		return this.quadSize * scaleFactor;
	}

	// Добавляем свечение
	@Override
	public int getLightColor(float partialTick) {
		int i = super.getLightColor(partialTick);
		int j = i & 255;       // блочный свет
		int k = i >> 16 & 255; // небесный свет
		j = 240;               // устанавливаем максимальную яркость блочного света
		return j | (k << 16);
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
			BlackNetherStarBeamParticles netherStarBeamParticle = new BlackNetherStarBeamParticles(pLevel, pX, pY, pZ, 0.5D - pLevel.getRandom().nextDouble(), pYSpeed, 0.5D - pLevel.getRandom().nextDouble(), this.sprite);
			float[][] colors = {
					{0, 0, 0},
					{17F/255F, 11F/255F, 22F/255F}
			};
			float[] chosen = colors[pLevel.random.nextInt(colors.length)];
			netherStarBeamParticle.setColor(chosen[0], chosen[1], chosen[2]);

			netherStarBeamParticle.yd *= (double) 0.2F;
			if (pXSpeed == 0.0D && pZSpeed == 0.0D) {
				netherStarBeamParticle.xd *= (double) 0.1F;
				netherStarBeamParticle.zd *= (double) 0.1F;
			}

			netherStarBeamParticle.setLifetime((int) (8.0D / (pLevel.random.nextDouble() * 0.8D + 0.2D)));
			return netherStarBeamParticle;
		}
	}
}