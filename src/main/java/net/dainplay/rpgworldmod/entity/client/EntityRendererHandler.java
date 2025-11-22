package net.dainplay.rpgworldmod.entity.client;

import net.dainplay.rpgworldmod.RPGworldMod;
import net.dainplay.rpgworldmod.entity.client.model.*;
import net.dainplay.rpgworldmod.entity.client.render.*;
import net.dainplay.rpgworldmod.entity.custom.ModBoat;
import net.dainplay.rpgworldmod.entity.ModEntities;
import net.minecraft.client.model.BoatModel;
import net.minecraft.client.model.ChestBoatModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.entity.BoatRenderer;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = RPGworldMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class EntityRendererHandler {

    @SubscribeEvent
    public static void doSetup(FMLClientSetupEvent event) {
        EntityRenderers.register(ModEntities.PROJECTRUFFLE_ARROW.get(), ProjectruffleArrowRenderer::new);
        EntityRenderers.register(ModEntities.FAIRAPIER_SEED_PROJECTILE.get(), FairapierSeedRenderer::new);
        EntityRenderers.register(ModEntities.BURR_SPIKE_PROJECTILE.get(), BurrSpikeRenderer::new);
        EntityRenderers.register(ModEntities.DRILL_SPEAR_PROJECTILE.get(), ThrownDrillSpearRenderer::new);
    }
    @SubscribeEvent
    public static void registerLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(BibbitModel.LAYER_LOCATION, BibbitModel::createBodyLayer);
        event.registerLayerDefinition(BibbitArmorModel.LAYER_LOCATION, BibbitArmorModel::createBodyLayer);
        event.registerLayerDefinition(BramblefoxModel.LAYER_LOCATION, BramblefoxModel::createBodyLayer);
        event.registerLayerDefinition(MintobatModel.LAYER_LOCATION, MintobatModel::createBodyLayer);
        event.registerLayerDefinition(FireflanternModel.LAYER_LOCATION, FireflanternModel::createBodyLayer);
        event.registerLayerDefinition(Burr_purrModel.LAYER_LOCATION, Burr_purrModel::createBodyLayer);
        event.registerLayerDefinition(DrillhogModel.LAYER_LOCATION, DrillhogModel::createBodyLayer);
        event.registerLayerDefinition(PlatinumfishModel.LAYER_LOCATION, PlatinumfishModel::createBodyLayer);
        event.registerLayerDefinition(MossfrontModel.LAYER_LOCATION, MossfrontModel::createBodyLayer);
        event.registerLayerDefinition(SheentroutModel.LAYER_LOCATION, SheentroutModel::createBodyLayer);
        event.registerLayerDefinition(BhleeModel.LAYER_LOCATION, BhleeModel::createBodyLayer);
        event.registerLayerDefinition(GasbassModel.LAYER_LOCATION, GasbassModel::createBodyLayer);
        event.registerLayerDefinition(DrillSpearModel.LAYER_LOCATION, DrillSpearModel::createBodyLayer);

        for(ModBoat.Type boatType : ModBoat.Type.values()) {
            event.registerLayerDefinition(ModBoatRenderer.createBoatModelName(boatType), BoatModel::createBodyModel);
            event.registerLayerDefinition(ModBoatRenderer.createChestBoatModelName(boatType), ChestBoatModel::createBodyModel);
        }
    }
    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event)
    {
        event.registerEntityRenderer(ModEntities.MODBOAT.get(), m -> new ModBoatRenderer(m, false));
        event.registerEntityRenderer(ModEntities.MODCHESTBOAT.get(), m -> new ModBoatRenderer(m, true));
        event.registerEntityRenderer(ModEntities.BIBBIT.get(), BibbitRenderer::new);
        event.registerEntityRenderer(ModEntities.PLATINUMFISH.get(), PlatinumfishRenderer::new);
        event.registerEntityRenderer(ModEntities.MOSSFRONT.get(), MossfrontRenderer::new);
        event.registerEntityRenderer(ModEntities.SHEENTROUT.get(), SheentroutRenderer::new);
        event.registerEntityRenderer(ModEntities.BHLEE.get(), BhleeRenderer::new);
        event.registerEntityRenderer(ModEntities.GASBASS.get(), GasbassRenderer::new);
        event.registerEntityRenderer(ModEntities.BRAMBLEFOX.get(), BramblefoxRenderer::new);
        event.registerEntityRenderer(ModEntities.MINTOBAT.get(), MintobatRenderer::new);
        event.registerEntityRenderer(ModEntities.FIREFLANTERN.get(), FireflanternRenderer::new);
        event.registerEntityRenderer(ModEntities.BURR_PURR.get(), Burr_purrRenderer::new);
        event.registerEntityRenderer(ModEntities.DRILLHOG.get(), DrillhogRenderer::new);
        event.registerEntityRenderer(ModEntities.MOSQUITO_SWARM.get(), MosquitoSwarmRenderer::new);
        event.registerEntityRenderer(ModEntities.MODBOAT.get(), (p_174094_) -> new ModBoatRenderer(p_174094_, false));
        event.registerEntityRenderer(ModEntities.MODCHESTBOAT.get(), (p_174092_) -> new ModBoatRenderer(p_174092_, true));
    }
}