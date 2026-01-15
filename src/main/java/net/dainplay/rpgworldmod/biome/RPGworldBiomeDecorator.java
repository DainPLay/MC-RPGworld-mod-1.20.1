package net.dainplay.rpgworldmod.biome;

import net.dainplay.rpgworldmod.entity.ModEntities;
import net.dainplay.rpgworldmod.util.ColorConstants;
import net.dainplay.rpgworldmod.world.feature.ModConfiguredFeatures;
import net.dainplay.rpgworldmod.world.feature.ModPlacedFeatures;
import net.minecraft.core.HolderGetter;
import net.minecraft.data.worldgen.BiomeDefaultFeatures;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.*;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public class RPGworldBiomeDecorator {

    private static int getSkyColorWithTemperatureModifier(float temperature) {
        float f = temperature / 3.0F;
        f = Mth.clamp(f, -1.0F, 1.0F);
        return Mth.hsvToRgb(0.6325F - f * 0.1F, 0.44F + f * 0.11F, 1F);
    }

    private static Biome biome(boolean precipitation, float temperature, float downfall, MobSpawnSettings.Builder spawnBuilder, BiomeGenerationSettings.Builder biomeBuilder)
    {
        return biome(precipitation, temperature, downfall, ColorConstants.RIE_WEALD_WATER, ColorConstants.RIE_WEALD_WATERFOG, ColorConstants.RIE_WEALD_FOLIAGE_COLOR, ColorConstants.RIE_WEALD_GRASS_COLOR, spawnBuilder, biomeBuilder);
    }

    private static Biome biome(boolean precipitation, float temperature, float downfall, int waterColor, int waterFogColor, int grassColor, int foliageColor, MobSpawnSettings.Builder spawnBuilder, BiomeGenerationSettings.Builder biomeBuilder)
    {
        return (new Biome.BiomeBuilder())
                .hasPrecipitation(precipitation)
                .temperature(temperature)
                .downfall(downfall)
                .specialEffects((new BiomeSpecialEffects.Builder())
                        .waterColor(waterColor)
                        .waterFogColor(waterFogColor)
                        .fogColor(12638463)
                        .skyColor(getSkyColorWithTemperatureModifier(temperature))
                        .ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS)
                        .foliageColorOverride(foliageColor)
                        .grassColorOverride(grassColor)
                        .backgroundMusic(ModConfiguredFeatures.RIE_WEALD_MUSIC)
                        .build())
                .mobSpawnSettings(spawnBuilder.build())
                .generationSettings(biomeBuilder.build())
                .build();
    }

    public static Biome decorateRieWeald(HolderGetter<PlacedFeature> placedFeatureGetter, HolderGetter<ConfiguredWorldCarver<?>> carverGetter) {
        MobSpawnSettings.Builder spawnSettings = new MobSpawnSettings.Builder();
        BiomeGenerationSettings.Builder biomeFeatures = new BiomeGenerationSettings.Builder(placedFeatureGetter, carverGetter);

        spawnSettings.addSpawn(MobCategory.AMBIENT, new MobSpawnSettings.SpawnerData(EntityType.BAT, 10, 8, 8));
        //spawnSettings.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.SPIDER, 80, 4, 4));
        //spawnSettings.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.ZOMBIE, 95, 4, 4));
        //spawnSettings.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.ZOMBIE_VILLAGER, 5, 1, 1));
        //spawnSettings.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.SKELETON, 90, 4, 4));
        //spawnSettings.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.CREEPER, 85, 4, 4));
        //spawnSettings.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.SLIME, 100, 4, 4));
        //spawnSettings.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.ENDERMAN, 7, 1, 4));
        //spawnSettings.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.WITCH, 5, 1, 1));
        //spawnSettings.addSpawn(MobCategory.AXOLOTLS, new MobSpawnSettings.SpawnerData(EntityType.AXOLOTL, 30, 1, 3));
        spawnSettings.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(ModEntities.BRAMBLEFOX.get(), 6, 1, 2));
        spawnSettings.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(ModEntities.BIBBIT.get(), 6, 1, 1));
        spawnSettings.addSpawn(MobCategory.WATER_AMBIENT, new MobSpawnSettings.SpawnerData(ModEntities.PLATINUMFISH.get(), 1, 1, 1));
        spawnSettings.addSpawn(MobCategory.WATER_AMBIENT, new MobSpawnSettings.SpawnerData(ModEntities.MOSSFRONT.get(), 5, 1, 2));
        spawnSettings.addSpawn(MobCategory.WATER_AMBIENT, new MobSpawnSettings.SpawnerData(ModEntities.BHLEE.get(), 5, 1, 3));
        spawnSettings.addSpawn(MobCategory.WATER_AMBIENT, new MobSpawnSettings.SpawnerData(ModEntities.SHEENTROUT.get(), 2, 1, 1));
        spawnSettings.addSpawn(MobCategory.WATER_AMBIENT, new MobSpawnSettings.SpawnerData(ModEntities.GASBASS.get(), 4, 1, 1));
        spawnSettings.addSpawn(ModEntities.RIE_MONSTER, new MobSpawnSettings.SpawnerData(ModEntities.MINTOBAT.get(), 3, 1, 3));
        spawnSettings.addSpawn(ModEntities.RIE_MONSTER, new MobSpawnSettings.SpawnerData(ModEntities.DRILLHOG.get(), 6, 1, 1));
        spawnSettings.addSpawn(ModEntities.RIE_MONSTER, new MobSpawnSettings.SpawnerData(ModEntities.MOSQUITO_SWARM.get(), 6, 1, 1));
        spawnSettings.addSpawn(ModEntities.RIE_MONSTER, new MobSpawnSettings.SpawnerData(ModEntities.FIREFLANTERN.get(), 10, 1, 1));
        spawnSettings.addSpawn(ModEntities.RIE_MONSTER, new MobSpawnSettings.SpawnerData(ModEntities.BURR_PURR.get(), 6, 1, 3));


        BiomeDefaultFeatures.addDefaultCarversAndLakes(biomeFeatures);
        BiomeDefaultFeatures.addDefaultCrystalFormations(biomeFeatures);
        BiomeDefaultFeatures.addDefaultMonsterRoom(biomeFeatures);
        BiomeDefaultFeatures.addDefaultUndergroundVariety(biomeFeatures);
        BiomeDefaultFeatures.addDefaultSprings(biomeFeatures);
        BiomeDefaultFeatures.addSurfaceFreezing(biomeFeatures);
        biomeFeatures.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, ModPlacedFeatures.PATCH_MOSS_RIE_PLACED_KEY);
        biomeFeatures.addFeature(GenerationStep.Decoration.LOCAL_MODIFICATIONS, ModPlacedFeatures.MASKONITE_PLACED_KEY);
        BiomeDefaultFeatures.addDefaultOres(biomeFeatures);
        BiomeDefaultFeatures.addDefaultSoftDisks(biomeFeatures);
        biomeFeatures.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, ModPlacedFeatures.ENT_FACE_SOUTH_CHECKED_KEY);
        biomeFeatures.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, ModPlacedFeatures.ENT_FACE_EAST_CHECKED_KEY);
        biomeFeatures.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, ModPlacedFeatures.ENT_FACE_NORTH_CHECKED_KEY);
        biomeFeatures.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, ModPlacedFeatures.ENT_FACE_WEST_CHECKED_KEY);
        biomeFeatures.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, ModPlacedFeatures.FANCY_RIE_TREE_CHECKED_KEY);
        biomeFeatures.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, ModPlacedFeatures.RIE_TREE_CHECKED_KEY);
        biomeFeatures.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, ModPlacedFeatures.RIE_BUSH_PLACED_KEY);
        biomeFeatures.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, ModPlacedFeatures.SHIVERALIS_PLACED_KEY);
        biomeFeatures.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, ModPlacedFeatures.TRIPLOVER_PLACED_KEY);
        biomeFeatures.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, ModPlacedFeatures.GLOSSOM_PLACED_KEY);
        biomeFeatures.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, ModPlacedFeatures.FAIRAPIER_PLACED_KEY);
        biomeFeatures.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, ModPlacedFeatures.RIE_FLOWER_PLACED_KEY);
        biomeFeatures.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, ModPlacedFeatures.PROJECTRUFFLE_PLACED_KEY);
        biomeFeatures.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, ModPlacedFeatures.SILICINA_PLACED_KEY);
        biomeFeatures.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, ModPlacedFeatures.HOLTS_REFLECTION_PLACED_KEY);
        biomeFeatures.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, ModPlacedFeatures.RAZORLEAF_BUD_PLACED_KEY);
        biomeFeatures.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, ModPlacedFeatures.STAREBLOSSOM_PLACED_KEY);
        biomeFeatures.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, ModPlacedFeatures.RPGIROLLE_PLACED_KEY);
        biomeFeatures.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, ModPlacedFeatures.MIMOSSA_PLACED_KEY);
        biomeFeatures.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, ModPlacedFeatures.TYPHON_PLACED_KEY);
        biomeFeatures.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, ModPlacedFeatures.CHEESE_CAP_PLACED_KEY);
        biomeFeatures.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, ModPlacedFeatures.MOSSHROOM_PLACED_KEY);
        biomeFeatures.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, ModPlacedFeatures.PATCH_PARALILY_PLACED_KEY);
        biomeFeatures.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, ModPlacedFeatures.SPIKY_IVY_PLACED_KEY);

        biomeFeatures.addFeature(GenerationStep.Decoration.TOP_LAYER_MODIFICATION , ModPlacedFeatures.TREE_HOLLOW_WEST_PLACED_KEY);
        biomeFeatures.addFeature(GenerationStep.Decoration.TOP_LAYER_MODIFICATION , ModPlacedFeatures.TREE_HOLLOW_EAST_PLACED_KEY);
        biomeFeatures.addFeature(GenerationStep.Decoration.TOP_LAYER_MODIFICATION , ModPlacedFeatures.TREE_HOLLOW_NORTH_PLACED_KEY);
        biomeFeatures.addFeature(GenerationStep.Decoration.TOP_LAYER_MODIFICATION , ModPlacedFeatures.TREE_HOLLOW_SOUTH_PLACED_KEY);

        return biome(false, 0.7F, 0.7F, ColorConstants.RIE_WEALD_WATER, ColorConstants.RIE_WEALD_WATERFOG, ColorConstants.RIE_WEALD_FOLIAGE_COLOR, ColorConstants.RIE_WEALD_GRASS_COLOR, spawnSettings, biomeFeatures);
    }
}
