package net.dainplay.rpgworldmod.entity.client.render;

import net.dainplay.rpgworldmod.RPGworldMod;
import net.dainplay.rpgworldmod.entity.client.model.BeltModel;
import net.dainplay.rpgworldmod.entity.client.model.ScarfModel;
import net.dainplay.rpgworldmod.entity.client.model.ThimbleModel;
import net.dainplay.rpgworldmod.entity.client.model.TurretModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.event.EntityRenderersEvent;

import java.util.function.Supplier;

public class CurioLayers {

    public static final ModelLayerLocation

            SCARF = createLayerLocation("scarf"),
            BELT = createLayerLocation("belt"),
            TURRET = createLayerLocation("turret"),
            THIMBLE = createLayerLocation("thimble");

    public static ModelLayerLocation createLayerLocation(String name) {
        return new ModelLayerLocation(new ResourceLocation(RPGworldMod.MOD_ID, name), name);
    }

    private static Supplier<LayerDefinition> layer(MeshDefinition mesh, int textureWidth, int textureHeight) {
        return () -> LayerDefinition.create(mesh, textureWidth, textureHeight);
    }

    private static void register(EntityRenderersEvent.RegisterLayerDefinitions event, ModelLayerLocation layerLocation, Supplier<LayerDefinition> layer) {
        event.registerLayerDefinition(layerLocation, layer);
    }

    public static void register(EntityRenderersEvent.RegisterLayerDefinitions event) {

        register(event, SCARF, layer(ScarfModel.createScarf(), 28, 16));
        register(event, BELT, layer(BeltModel.createBelt(), 28, 16));
        register(event, TURRET, layer(TurretModel.createTurret(), 64, 64));
        register(event, THIMBLE, layer(ThimbleModel.createThimble(), 16, 16));
    }
}
