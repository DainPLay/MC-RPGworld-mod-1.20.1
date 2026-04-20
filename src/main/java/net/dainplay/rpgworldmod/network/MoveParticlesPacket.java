package net.dainplay.rpgworldmod.network;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.function.Supplier;

public class MoveParticlesPacket {
	private final Vec3 startPos;
	private final Vec3 velocity;
	private final ParticleOptions particleType;
	private final boolean shouldCollide;
	private final double maxDistance;

	public MoveParticlesPacket(Vec3 startPos, Vec3 velocity, ParticleOptions particleType, boolean shouldCollide, double maxDistance) {
		this.startPos = startPos;
		this.velocity = velocity;
		this.particleType = particleType;
		this.shouldCollide = shouldCollide;
		this.maxDistance = maxDistance;
	}

	public MoveParticlesPacket(FriendlyByteBuf buf) {
		this.startPos = new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble());
		this.velocity = new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble());
		ParticleType<?> type = ForgeRegistries.PARTICLE_TYPES.getValue(buf.readResourceLocation());
		if (type == null) {
			throw new IllegalArgumentException("Unknown particle type received");
		}
		this.particleType = (ParticleOptions) type;
		this.shouldCollide = buf.readBoolean();
		this.maxDistance = buf.readDouble();
	}

	public void toBytes(FriendlyByteBuf buf) {
		buf.writeDouble(startPos.x);
		buf.writeDouble(startPos.y);
		buf.writeDouble(startPos.z);
		buf.writeDouble(velocity.x);
		buf.writeDouble(velocity.y);
		buf.writeDouble(velocity.z);
		buf.writeResourceLocation(ForgeRegistries.PARTICLE_TYPES.getKey(particleType.getType()));
		buf.writeBoolean(shouldCollide);
		buf.writeDouble(maxDistance);
	}

	public boolean handle(Supplier<NetworkEvent.Context> supplier) {
		NetworkEvent.Context context = supplier.get();
		context.enqueueWork(() ->
				DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
						ClientPacketHandlers.handleMoveParticles(startPos, velocity, particleType, shouldCollide, maxDistance))
		);
		context.setPacketHandled(true);
		return true;
	}
}