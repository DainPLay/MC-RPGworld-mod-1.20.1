package net.dainplay.rpgworldmod.particle.custom;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.RisingParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;

public class FlamesParticle extends RisingParticle {
	private final SpriteSet sprites;
	private static final int TOTAL_SPRITES = 14;

	FlamesParticle(ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, SpriteSet spriteSet) {
		super(level, x, y, z, xSpeed, ySpeed, zSpeed);
		this.sprites = spriteSet;
		this.lifetime = 14;

		this.setSpriteFromAge(spriteSet);
	}

	@Override
	public ParticleRenderType getRenderType() {
		return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
	}

	@Override
	public void move(double x, double y, double z) {
		this.setBoundingBox(this.getBoundingBox().move(x, y, z));
		this.setLocationFromBoundingbox();
	}

	@Override
	public float getQuadSize(float scaleFactor) {
		float f = ((float) this.age + scaleFactor) / (float) this.lifetime;
		return this.quadSize * (1.0F - f * f * 0.5F);
	}

	@Override
	public int getLightColor(float partialTick) {
		float f = ((float) this.age + partialTick) / (float) this.lifetime;
		f = Mth.clamp(f, 0.0F, 1.0F);
		int i = super.getLightColor(partialTick);
		int j = i & 255;
		int k = i >> 16 & 255;
		j += (int) (f * 15.0F * 16.0F);
		if (j > 240) {
			j = 240;
		}
		return j | k << 16;
	}

	@Override
	public void tick() {
		super.tick();

		if (!this.removed) {
			this.setSpriteFromAge(this.sprites);
		}
	}

	@Override
	public void setSpriteFromAge(SpriteSet spriteSet) {
		float progress = (float) this.age / (float) this.lifetime;
		int spriteIndex = Mth.floor(progress * TOTAL_SPRITES);


		spriteIndex = Mth.clamp(spriteIndex, 0, TOTAL_SPRITES - 1);


		this.setSprite(spriteSet.get(spriteIndex, TOTAL_SPRITES));
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
			FlamesParticle particle = new FlamesParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, this.sprites);

			particle.scale(2F);
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
			FlamesParticle particle = new FlamesParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, this.sprites);
			particle.scale(1F);
			return particle;
		}
	}
}