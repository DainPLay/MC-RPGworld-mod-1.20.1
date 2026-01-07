package net.dainplay.rpgworldmod.entity.client.render;

import net.dainplay.rpgworldmod.entity.client.model.BeltModel;
import net.dainplay.rpgworldmod.entity.client.model.CharmModel;
import net.dainplay.rpgworldmod.entity.client.model.ScarfModel;
import net.dainplay.rpgworldmod.entity.client.model.ThimbleModel;
import net.dainplay.rpgworldmod.entity.client.model.TurretModel;
import net.dainplay.rpgworldmod.item.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import top.theillusivec4.curios.api.client.CuriosRendererRegistry;

public class CurioRenderers {

    public static void register() {
        CuriosRendererRegistry.register(ModItems.BRAMBLEFOX_SCARF.get(), () -> new CurioRenderer("scarf/bramblefox_scarf", new ScarfModel(bakeLayer(CurioLayers.SCARF), RenderType::entityCutoutNoCull)));
        CuriosRendererRegistry.register(ModItems.LAPIS_CHARM.get(), () -> new CurioRenderer("charm/lapis_charm", new CharmModel(bakeLayer(CurioLayers.CHARM), RenderType::entityCutoutNoCull)));
        CuriosRendererRegistry.register(ModItems.FIG_LEAF.get(), () -> new CurioRenderer("belt/fig_leaf", new BeltModel(bakeLayer(CurioLayers.BELT), RenderType::entityCutoutNoCull)));
        CuriosRendererRegistry.register(ModItems.PORTABLE_TURRET.get(), () -> new CurioRenderer("turret/portable_turret", TurretModel.createTurretModel()));
        CuriosRendererRegistry.register(ModItems.CHITIN_THIMBLE.get(), () -> new CurioRenderer("ring/chitin_thimble", ThimbleModel.createThimbleModel()));
        }

    public static ModelPart bakeLayer(ModelLayerLocation layerLocation) {
        return Minecraft.getInstance().getEntityModels().bakeLayer(layerLocation);
    }
}
