package net.dainplay.rpgworldmod.entity.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.dainplay.rpgworldmod.RPGworldMod;
import net.dainplay.rpgworldmod.entity.client.model.MintobatModel;
import net.dainplay.rpgworldmod.entity.custom.Mintobat;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ambient.Bat;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;


public class MintobatRenderer extends MobRenderer<Mintobat, MintobatModel<Mintobat>> {
    public static final ResourceLocation TEXTURE = new ResourceLocation(RPGworldMod.MOD_ID, "textures/entity/mintobat/mintobat.png");
    public static final ResourceLocation TEXTURE_SCREAM = new ResourceLocation(RPGworldMod.MOD_ID, "textures/entity/mintobat/mintobat_scream.png");

    public MintobatRenderer(EntityRendererProvider.Context context) {
        super(context, new MintobatModel<>(context.bakeLayer(MintobatModel.LAYER_LOCATION)), 0.8F);
    }
    public ResourceLocation getTextureLocation(Mintobat pEntity) {
        return pEntity.isCharged() ? TEXTURE_SCREAM : TEXTURE;
    }
    protected void scale(Mintobat pLivingEntity, PoseStack pMatrixStack, float pPartialTickTime) {
        pMatrixStack.scale(0.8F, 0.8F, 0.8F);
    }
    protected void setupRotations(Mintobat pEntityLiving, PoseStack pMatrixStack, float pAgeInTicks, float pRotationYaw, float pPartialTicks) {
        pMatrixStack.translate(0.0D, -0.4D, 0.0D);
        super.setupRotations(pEntityLiving, pMatrixStack, pAgeInTicks, pRotationYaw, pPartialTicks);
    }
}
