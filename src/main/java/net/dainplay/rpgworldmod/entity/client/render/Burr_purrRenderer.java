package net.dainplay.rpgworldmod.entity.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.dainplay.rpgworldmod.RPGworldMod;
import net.dainplay.rpgworldmod.entity.client.model.Burr_purrModel;
import net.dainplay.rpgworldmod.entity.client.model.MintobatModel;
import net.dainplay.rpgworldmod.entity.custom.Burr_purr;
import net.dainplay.rpgworldmod.entity.custom.Mintobat;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

import static net.dainplay.rpgworldmod.entity.custom.Burr_purr.DATA_IS_SPINNING;


public class Burr_purrRenderer extends MobRenderer<Burr_purr, Burr_purrModel<Burr_purr>> {
    public static final ResourceLocation TEXTURE = new ResourceLocation(RPGworldMod.MOD_ID, "textures/entity/burr_purr/burr_purr_normal.png");
    public static final ResourceLocation TEXTURE_BALL = new ResourceLocation(RPGworldMod.MOD_ID, "textures/entity/burr_purr/burr_purr_ball.png");

    public Burr_purrRenderer(EntityRendererProvider.Context context) {
        super(context, new Burr_purrModel<>(context.bakeLayer(Burr_purrModel.LAYER_LOCATION)), 0.25F);
    }
    public ResourceLocation getTextureLocation(Burr_purr pEntity) {
        return pEntity.getEntityData().get(DATA_IS_SPINNING) ? TEXTURE_BALL : TEXTURE;
    }
}
