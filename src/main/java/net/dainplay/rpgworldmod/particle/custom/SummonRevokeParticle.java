package net.dainplay.rpgworldmod.particle.custom;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.HugeExplosionParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.SimpleParticleType;

public class SummonRevokeParticle extends HugeExplosionParticle {
	protected SummonRevokeParticle(ClientLevel pLevel, double pX, double pY, double pZ, double pQuadSizeMultiplier, SpriteSet pSprites) {
		super(pLevel, pX, pY, pZ, pQuadSizeMultiplier, pSprites);
		this.lifetime = 10;
		this.quadSize = 1F;
		this.setSpriteFromAge(pSprites);
	}

	@Override
	public int getLightColor(float partialTick) {
		BlockPos blockpos = BlockPos.containing(this.x, this.y, this.z);
		return this.level.hasChunkAt(blockpos) ? LevelRenderer.getLightColor(this.level, blockpos) : 0;
	}

	public static class Provider implements ParticleProvider<SimpleParticleType> {
		private final SpriteSet sprites;

		public Provider(SpriteSet pSprites) {
			this.sprites = pSprites;
		}

		public Particle createParticle(SimpleParticleType pType, ClientLevel pLevel, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed) {
			return new SummonRevokeParticle(pLevel, pX, pY, pZ, pXSpeed, this.sprites);
		}
	}
}