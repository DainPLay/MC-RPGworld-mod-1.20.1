package net.dainplay.rpgworldmod.particle.custom;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;

public class ParanoiaEyeParticle extends TextureSheetParticle {
	private final SpriteSet sprites;
	private final int totalLifetime;
	private final double blinkStartOffset;


	private static final int CLOSED_EYE_TICKS = 2;
	private static final int HALF_CLOSED_TICKS = 2;
	private static final int BLINK_SEQUENCE_TICKS = 6;
	private static final int CLOSING_SEQUENCE_TICKS = 4;


	private static final int MIN_LIFETIME = 120;
	private static final int MAX_LIFETIME = 240;


	private static final float MAX_FADE_DISTANCE = 5.0f;
	private static final float MIN_FADE_DISTANCE = 4.0f;


	private static final int SPRITE_CLOSED = 1;
	private static final int SPRITE_HALF_CLOSED = 2;
	private static final int SPRITE_OPEN = 3;

	protected ParanoiaEyeParticle(ClientLevel level, double x, double y, double z,
								  SpriteSet spriteSet, int lifetime) {
		super(level, x, y, z);
		this.sprites = spriteSet;
		this.totalLifetime = lifetime;
		this.lifetime = totalLifetime;
		this.gravity = 0.0F;
		this.hasPhysics = false;


		this.blinkStartOffset = totalLifetime / 2.0 - BLINK_SEQUENCE_TICKS / 2.0;


		this.quadSize = 0.25F;
		this.alpha = 1.0F;


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
			this.setSpriteForAge(this.age);
		}
	}

	private void setSpriteForAge(int currentAge) {
		int spriteIndex = getSpriteIndexForAge(currentAge);

		this.setSprite(sprites.get(spriteIndex, 3));
	}

	private int getSpriteIndexForAge(int currentAge) {
		int remainingTicks = totalLifetime - currentAge;


		if (currentAge < CLOSED_EYE_TICKS) {
			return SPRITE_CLOSED;
		}


		if (currentAge < CLOSED_EYE_TICKS + HALF_CLOSED_TICKS) {
			return SPRITE_HALF_CLOSED;
		}


		if (remainingTicks <= CLOSING_SEQUENCE_TICKS) {
			return getClosingSequenceSpriteIndex(remainingTicks);
		}


		int blinkStart = (int) Math.floor(blinkStartOffset);
		if (currentAge >= blinkStart && currentAge < blinkStart + BLINK_SEQUENCE_TICKS) {
			return getBlinkSequenceSpriteIndex(currentAge - blinkStart);
		}


		return SPRITE_OPEN;
	}

	private int getBlinkSequenceSpriteIndex(int blinkProgress) {
		if (blinkProgress < 2) {
			return SPRITE_HALF_CLOSED;
		} else if (blinkProgress < 4) {
			return SPRITE_CLOSED;
		} else {
			return SPRITE_HALF_CLOSED;
		}
	}

	private int getClosingSequenceSpriteIndex(int remainingTicks) {
		if (remainingTicks > 2) {
			return SPRITE_HALF_CLOSED;
		} else {
			return SPRITE_CLOSED;
		}
	}

	@Override
	public void setSpriteFromAge(SpriteSet spriteSet) {
		this.setSpriteForAge(this.age);
	}

	@Override
	public float getQuadSize(float scaleFactor) {
		return this.quadSize;
	}

	@Override
	public int getLightColor(float partialTick) {
		return 15728880;
	}

	@Override
	public void render(VertexConsumer vertexConsumer, Camera camera, float partialTick) {
		double cameraX = camera.getPosition().x();
		double cameraY = camera.getPosition().y();
		double cameraZ = camera.getPosition().z();


		double dx = this.x - cameraX;
		double dy = this.y - cameraY;
		double dz = this.z - cameraZ;
		double distanceSq = dx * dx + dy * dy + dz * dz;
		double distance = Math.sqrt(distanceSq);


		float targetAlpha = calculateAlphaBasedOnDistance((float) distance);


		float savedAlpha = this.alpha;


		this.alpha = targetAlpha;


		super.render(vertexConsumer, camera, partialTick);


		this.alpha = savedAlpha;
	}

	private float calculateAlphaBasedOnDistance(float distance) {
		if (distance >= MAX_FADE_DISTANCE) {
			return 1.0f;
		} else if (distance <= MIN_FADE_DISTANCE) {
			return 0.0f;
		} else {
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
			int lifetime = MIN_LIFETIME + level.random.nextInt(MAX_LIFETIME - MIN_LIFETIME + 1);

			ParanoiaEyeParticle particle = new ParanoiaEyeParticle(level, x, y, z, this.sprites, lifetime);


			particle.xd = (level.random.nextDouble() - 0.5) * 0.01;
			particle.yd = (level.random.nextDouble() - 0.5) * 0.01;
			particle.zd = (level.random.nextDouble() - 0.5) * 0.01;

			return particle;
		}
	}
}