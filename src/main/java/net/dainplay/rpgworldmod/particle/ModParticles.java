package net.dainplay.rpgworldmod.particle;

import net.dainplay.rpgworldmod.RPGworldMod;
import net.dainplay.rpgworldmod.particle.custom.AirParticle;
import net.dainplay.rpgworldmod.particle.custom.BlackNetherStarBeamParticles;
import net.dainplay.rpgworldmod.particle.custom.FlamesParticle;
import net.dainplay.rpgworldmod.particle.custom.LeavesParticle;
import net.dainplay.rpgworldmod.particle.custom.ManaParticle;
import net.dainplay.rpgworldmod.particle.custom.MosquitosParticles;
import net.dainplay.rpgworldmod.particle.custom.ParanoiaEyeParticle;
import net.dainplay.rpgworldmod.particle.custom.QuartziteParticles;
import net.dainplay.rpgworldmod.particle.custom.SummonRevokeParticle;
import net.dainplay.rpgworldmod.particle.custom.WhiteNetherStarBeamParticles;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
@Mod.EventBusSubscriber(modid = RPGworldMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ModParticles {
    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES =
            DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, RPGworldMod.MOD_ID);

    public static final RegistryObject<SimpleParticleType> QUARTZITE_SHINE_PARTICLES = PARTICLE_TYPES.register("quartzine_shine_particles",() -> new SimpleParticleType(true));
    public static final RegistryObject<SimpleParticleType> LEAVES = PARTICLE_TYPES.register("leaves",() -> new SimpleParticleType(true));
    public static final RegistryObject<SimpleParticleType> AIR = PARTICLE_TYPES.register("air",() -> new SimpleParticleType(true));
    public static final RegistryObject<SimpleParticleType> MOSQUITOS = PARTICLE_TYPES.register("mosquitos",() -> new SimpleParticleType(true));
    public static final RegistryObject<SimpleParticleType> FLAMES = PARTICLE_TYPES.register("flames",() -> new SimpleParticleType(true));
    public static final RegistryObject<SimpleParticleType> MANA = PARTICLE_TYPES.register("mana",() -> new SimpleParticleType(true));
    public static final RegistryObject<SimpleParticleType> SUMMON_REVOKE = PARTICLE_TYPES.register("summon_revoke",() -> new SimpleParticleType(true));
    public static final RegistryObject<SimpleParticleType> PARANOIA_EYE = PARTICLE_TYPES.register("paranoia_eye",() -> new SimpleParticleType(true));
    public static final RegistryObject<SimpleParticleType> WHITE_NETHER_STAR_BEAM = PARTICLE_TYPES.register("white_nether_star_beam",() -> new SimpleParticleType(true));
    public static final RegistryObject<SimpleParticleType> BLACK_NETHER_STAR_BEAM = PARTICLE_TYPES.register("black_nether_star_beam",() -> new SimpleParticleType(true));

    @SubscribeEvent
    public static void registerFactories(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(ModParticles.QUARTZITE_SHINE_PARTICLES.get(), QuartziteParticles.Provider::new);
        event.registerSpriteSet(ModParticles.LEAVES.get(), LeavesParticle.Provider::new);
        event.registerSpriteSet(ModParticles.AIR.get(), AirParticle.Provider::new);
        event.registerSpriteSet(ModParticles.MOSQUITOS.get(), MosquitosParticles.Provider::new);
        event.registerSpriteSet(ModParticles.FLAMES.get(), FlamesParticle.Provider::new);
        event.registerSpriteSet(ModParticles.MANA.get(), ManaParticle.Provider::new);
        event.registerSpriteSet(ModParticles.SUMMON_REVOKE.get(), SummonRevokeParticle.Provider::new);
        event.registerSpriteSet(ModParticles.PARANOIA_EYE.get(), ParanoiaEyeParticle.Provider::new);
        event.registerSpriteSet(ModParticles.WHITE_NETHER_STAR_BEAM.get(), WhiteNetherStarBeamParticles.Provider::new);
        event.registerSpriteSet(ModParticles.BLACK_NETHER_STAR_BEAM.get(), BlackNetherStarBeamParticles.Provider::new);
    }
}
