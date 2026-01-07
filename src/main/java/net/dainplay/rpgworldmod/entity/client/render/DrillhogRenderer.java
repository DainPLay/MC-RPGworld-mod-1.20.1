package net.dainplay.rpgworldmod.entity.client.render;

import net.dainplay.rpgworldmod.RPGworldMod;
import net.dainplay.rpgworldmod.entity.client.model.DrillhogModel;
import net.dainplay.rpgworldmod.entity.custom.Drillhog;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;


public class DrillhogRenderer extends MobRenderer<Drillhog, DrillhogModel<Drillhog>> {
    public static final ResourceLocation TEXTURE1 = new ResourceLocation(RPGworldMod.MOD_ID, "textures/entity/drillhog/drillhog_1.png");
    public static final ResourceLocation TEXTURE2 = new ResourceLocation(RPGworldMod.MOD_ID, "textures/entity/drillhog/drillhog_2.png");
    public static final ResourceLocation TEXTURE3 = new ResourceLocation(RPGworldMod.MOD_ID, "textures/entity/drillhog/drillhog_3.png");
    public static final ResourceLocation TEXTURE4 = new ResourceLocation(RPGworldMod.MOD_ID, "textures/entity/drillhog/drillhog_4.png");

    public DrillhogRenderer(EntityRendererProvider.Context context) {
        super(context, new DrillhogModel<>(context.bakeLayer(DrillhogModel.LAYER_LOCATION)), 0.7F);
    }


    public ResourceLocation getTextureLocation(Drillhog pEntity) {
        return switch (pEntity.tickCount % 4) {
            case 1 -> TEXTURE1;
            case 2 -> TEXTURE2;
            case 3 -> TEXTURE3;
            default -> TEXTURE4;
        };
    }
}
