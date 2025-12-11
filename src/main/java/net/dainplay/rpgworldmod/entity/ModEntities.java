package net.dainplay.rpgworldmod.entity;

import net.dainplay.rpgworldmod.RPGworldMod;
import net.dainplay.rpgworldmod.entity.custom.*;
import net.dainplay.rpgworldmod.entity.projectile.BurrSpikeEntity;
import net.dainplay.rpgworldmod.entity.projectile.FairapierSeedEntity;
import net.dainplay.rpgworldmod.entity.projectile.ProjectruffleArrowEntity;
import net.dainplay.rpgworldmod.entity.projectile.ThrownDrillSpear;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.animal.Cod;
import net.minecraft.world.entity.animal.WaterAnimal;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.event.entity.SpawnPlacementRegisterEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

@Mod.EventBusSubscriber(modid = RPGworldMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModEntities {
    public static DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, RPGworldMod.MOD_ID);
    public static final MobCategory RIE_MONSTER = MobCategory.create("rie_monster", "rpgworldmod:rie_monster", 12, false, false, 128);

    public static final RegistryObject<EntityType<ProjectruffleArrowEntity>> PROJECTRUFFLE_ARROW = ENTITY_TYPES.register("projectruffle_arrow",
            () -> EntityType.Builder.of((EntityType.EntityFactory<ProjectruffleArrowEntity>) ProjectruffleArrowEntity::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F).build("projectruffle_arrow"));
    public static final RegistryObject<EntityType<FairapierSeedEntity>> FAIRAPIER_SEED_PROJECTILE = ENTITY_TYPES.register("fairapier_seed_projectile",
            () -> EntityType.Builder.of((EntityType.EntityFactory<FairapierSeedEntity>) FairapierSeedEntity::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F).build("fairapier_seed_projectile"));
    public static final RegistryObject<EntityType<BurrSpikeEntity>> BURR_SPIKE_PROJECTILE = ENTITY_TYPES.register("burr_spike_projectile",
            () -> EntityType.Builder.of((EntityType.EntityFactory<BurrSpikeEntity>) BurrSpikeEntity::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F).build("burr_spike_projectile"));
    public static final RegistryObject<EntityType<ThrownDrillSpear>> DRILL_SPEAR_PROJECTILE = ENTITY_TYPES.register("drill_spear_projectile",
            () -> EntityType.Builder.of((EntityType.EntityFactory<ThrownDrillSpear>) ThrownDrillSpear::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F).clientTrackingRange(4).updateInterval(20).build("drill_spear_projectile"));

    public static final RegistryObject<EntityType<ModBoat>> MODBOAT = ENTITY_TYPES.register("modboat",
            () -> EntityType.Builder.of((EntityType.EntityFactory<ModBoat>) ModBoat::new, MobCategory.MISC).sized(1.375F, 0.5625F).clientTrackingRange(10).build("modboat"));

    public static final RegistryObject<EntityType<ModChestBoat>> MODCHESTBOAT = ENTITY_TYPES.register("modchestboat",
            () -> EntityType.Builder.of((EntityType.EntityFactory<ModChestBoat>) ModChestBoat::new, MobCategory.MISC).sized(1.375F, 0.5625F).clientTrackingRange(10).build("modboat"));
    public static final RegistryObject<EntityType<Bramblefox>> BRAMBLEFOX = ENTITY_TYPES.register("bramblefox",
            () -> EntityType.Builder.of(Bramblefox::new, MobCategory.CREATURE).sized(0.6F, 0.7F).clientTrackingRange(8).build("bramblefox"));
    public static final RegistryObject<EntityType<Mintobat>> MINTOBAT = ENTITY_TYPES.register("mintobat",
            () -> EntityType.Builder.of(Mintobat::new, RIE_MONSTER).sized(0.9F, 0.9F).clientTrackingRange(8).build("mintobat"));
    public static final RegistryObject<EntityType<Fireflantern>> FIREFLANTERN = ENTITY_TYPES.register("fireflantern",
            () -> EntityType.Builder.of(Fireflantern::new, RIE_MONSTER).sized(0.9F, 0.9F).clientTrackingRange(8).build("fireflantern"));
    public static final RegistryObject<EntityType<Burr_purr>> BURR_PURR = ENTITY_TYPES.register("burr_purr",
            () -> EntityType.Builder.of(Burr_purr::new, RIE_MONSTER).sized(0.6F, 0.6F).clientTrackingRange(8).build("burr_purr"));
    public static final RegistryObject<EntityType<Drillhog>> DRILLHOG = ENTITY_TYPES.register("drillhog",
            () -> EntityType.Builder.of(Drillhog::new, RIE_MONSTER).sized(0.9F, 0.9F).clientTrackingRange(8).build("drillhog"));
    public static final RegistryObject<EntityType<MosquitoSwarm>> MOSQUITO_SWARM = ENTITY_TYPES.register("mosquito_swarm",
            () -> EntityType.Builder.of(MosquitoSwarm::new, RIE_MONSTER).sized(0.95F, 0.95F).clientTrackingRange(8).build("mosquito_swarm"));
    public static final RegistryObject<EntityType<Bibbit>> BIBBIT = ENTITY_TYPES.register("bibbit",
            () -> EntityType.Builder.of(Bibbit::new, MobCategory.CREATURE).sized(0.6F, 0.9F).clientTrackingRange(8).build("bibbit"));
    public static final RegistryObject<EntityType<Platinumfish>> PLATINUMFISH = ENTITY_TYPES.register("platinumfish",
            () -> EntityType.Builder.of(Platinumfish::new, MobCategory.WATER_AMBIENT).sized(0.5F, 0.3F).clientTrackingRange(4).build("platinumfish"));
    public static final RegistryObject<EntityType<Mossfront>> MOSSFRONT = ENTITY_TYPES.register("mossfront",
            () -> EntityType.Builder.of(Mossfront::new, MobCategory.WATER_AMBIENT).sized(0.5F, 0.3F).clientTrackingRange(4).build("mossfront"));
    public static final RegistryObject<EntityType<Bhlee>> BHLEE = ENTITY_TYPES.register("bhlee",
            () -> EntityType.Builder.of(Bhlee::new, MobCategory.WATER_AMBIENT).sized(0.5F, 0.3F).clientTrackingRange(4).build("bhlee"));
    public static final RegistryObject<EntityType<Sheentrout>> SHEENTROUT = ENTITY_TYPES.register("sheentrout",
            () -> EntityType.Builder.of(Sheentrout::new, MobCategory.WATER_AMBIENT).sized(0.5F, 0.3F).clientTrackingRange(4).build("sheentrout"));
    public static final RegistryObject<EntityType<Gasbass>> GASBASS = ENTITY_TYPES.register("gasbass",
            () -> EntityType.Builder.of(Gasbass::new, MobCategory.WATER_AMBIENT).sized(0.5F, 0.3F).clientTrackingRange(4).build("gasbass"));
    private static <T extends Entity> Supplier<EntityType<T>> create(String key, EntityType.Builder<T> builder) {
        return ENTITY_TYPES.register(key, () -> builder.build(key));
    }
    @SubscribeEvent
    public static void registerEntities(SpawnPlacementRegisterEvent event) {
        event.register(BIBBIT.get(), SpawnPlacements.Type.NO_RESTRICTIONS, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Bibbit::checkBibbitSpawnRules, SpawnPlacementRegisterEvent.Operation.REPLACE);
        event.register(BRAMBLEFOX.get(), SpawnPlacements.Type.NO_RESTRICTIONS, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Bramblefox::checkBramblefoxSpawnRules, SpawnPlacementRegisterEvent.Operation.REPLACE);
        event.register(MINTOBAT.get(), SpawnPlacements.Type.NO_RESTRICTIONS, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Mintobat::checkMintobatSpawnRules, SpawnPlacementRegisterEvent.Operation.REPLACE);
        event.register(FIREFLANTERN.get(), SpawnPlacements.Type.NO_RESTRICTIONS, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Fireflantern::checkFireflanternSpawnRules, SpawnPlacementRegisterEvent.Operation.REPLACE);
        event.register(BURR_PURR.get(), SpawnPlacements.Type.NO_RESTRICTIONS, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Burr_purr::checkBurr_purrSpawnRules, SpawnPlacementRegisterEvent.Operation.REPLACE);
        event.register(DRILLHOG.get(), SpawnPlacements.Type.NO_RESTRICTIONS, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Drillhog::checkDrillhogSpawnRules, SpawnPlacementRegisterEvent.Operation.REPLACE);
        event.register(MOSQUITO_SWARM.get(), SpawnPlacements.Type.NO_RESTRICTIONS, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, MosquitoSwarm::checkMosquitoSwarmSpawnRules, SpawnPlacementRegisterEvent.Operation.REPLACE);
        event.register(PLATINUMFISH.get(), SpawnPlacements.Type.IN_WATER, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, WaterAnimal::checkSurfaceWaterAnimalSpawnRules, SpawnPlacementRegisterEvent.Operation.REPLACE);
        event.register(MOSSFRONT.get(), SpawnPlacements.Type.IN_WATER, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, WaterAnimal::checkSurfaceWaterAnimalSpawnRules, SpawnPlacementRegisterEvent.Operation.REPLACE);
        event.register(BHLEE.get(), SpawnPlacements.Type.IN_WATER, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, WaterAnimal::checkSurfaceWaterAnimalSpawnRules, SpawnPlacementRegisterEvent.Operation.REPLACE);
        event.register(SHEENTROUT.get(), SpawnPlacements.Type.IN_WATER, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, WaterAnimal::checkSurfaceWaterAnimalSpawnRules, SpawnPlacementRegisterEvent.Operation.REPLACE);
        event.register(GASBASS.get(), SpawnPlacements.Type.IN_WATER, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Gasbass::checkGasbassSpawnRules, SpawnPlacementRegisterEvent.Operation.REPLACE);
}

}
