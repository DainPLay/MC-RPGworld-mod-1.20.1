package net.dainplay.rpgworldmod.particle;

import net.dainplay.rpgworldmod.RPGworldMod;
import net.dainplay.rpgworldmod.particle.custom.LeavesParticle;
import net.dainplay.rpgworldmod.particle.custom.QuartziteParticles;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.world.item.Item;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.eventbus.api.IEventBus;
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

    @SubscribeEvent
    public static void registerFactories(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(ModParticles.QUARTZITE_SHINE_PARTICLES.get(), QuartziteParticles.Provider::new);
        event.registerSpriteSet(ModParticles.LEAVES.get(), LeavesParticle.Provider::new);
    }
}
