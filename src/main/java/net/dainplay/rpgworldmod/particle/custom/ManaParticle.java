package net.dainplay.rpgworldmod.particle.custom;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
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


		this.delayTicks = random.nextInt(5) + 2;
		this.lifetime = 20 + random.nextInt(10);


		this.xo = x;
		this.yo = y;
		this.zo = z;


		this.setSprite(spriteSet.get(random.nextInt(4), 4));


		this.xd = xSpeed;
		this.yd = ySpeed;
		this.zd = zSpeed;


		this.hasPhysics = false;


		this.quadSize = 0.2F;


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

		float f = ((float) this.age + scaleFactor) / (float) this.lifetime;
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


		if (inDelayPhase) {
			delayCounter++;
			if (delayCounter >= delayTicks) {
				inDelayPhase = false;

				this.hasPhysics = true;
			}

			this.setPos(this.x, this.y, this.z);
		} else {
			float f = (float) this.age / (float) this.lifetime;
			f = 1.0F - f;
			float f1 = 1.0F - f;
			f1 *= f1;
			f1 *= f1;


			this.x = this.xStart + this.xd * (double) f;
			this.y = this.yStart + this.yd * (double) f - (double) (f1 * 1.2F);
			this.z = this.zStart + this.zd * (double) f;

			this.setPos(this.x, this.y, this.z);
		}


		float lifeProgress = (float) this.age / (float) this.lifetime;
		if (lifeProgress > 0.5F) {
			float fadeProgress = (lifeProgress - 0.5F) / 0.5F;
			fadeProgress = Mth.clamp(fadeProgress, 0.0F, 1.0F);

			this.alpha = 1.0F - fadeProgress * fadeProgress;
		} else {
			this.alpha = 1.0F;
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
			double swayX = (level.random.nextDouble() - 0.5) * 0.02;
			double swayZ = (level.random.nextDouble() - 0.5) * 0.02;

			ManaParticle particle = new ManaParticle(level, x, y, z, swayX, 0.0, swayZ, this.sprites);
			particle.scale(0.25F);
			return particle;
		}
	}
}