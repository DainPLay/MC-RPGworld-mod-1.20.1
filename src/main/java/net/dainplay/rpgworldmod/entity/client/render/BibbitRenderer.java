package net.dainplay.rpgworldmod.entity.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.dainplay.rpgworldmod.RPGworldMod;
import net.dainplay.rpgworldmod.entity.client.model.BibbitArmorModel;
import net.dainplay.rpgworldmod.entity.client.model.BibbitEyesLayer;
import net.dainplay.rpgworldmod.entity.client.model.BibbitModel;
import net.dainplay.rpgworldmod.entity.custom.Bibbit;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidArmorModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.EnderEyesLayer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.client.ICurioRenderer;


public class BibbitRenderer extends MobRenderer<Bibbit, BibbitModel<Bibbit>> {
    public static final ResourceLocation TEXTURE = new ResourceLocation(RPGworldMod.MOD_ID, "textures/entity/bibbit/bibbit.png");
    public static final ResourceLocation TEXTURE_SITTING = new ResourceLocation(RPGworldMod.MOD_ID, "textures/entity/bibbit/bibbit_sitting.png");
    public static BibbitArmorModel<Bibbit> model;

    public BibbitRenderer(EntityRendererProvider.Context context) {
        super(context, new BibbitModel<>(context.bakeLayer(BibbitModel.LAYER_LOCATION)), 0.4f);
        this.addLayer(new BibbitEyesLayer<>(this));
        model = new BibbitArmorModel(context.bakeLayer(BibbitArmorModel.LAYER_LOCATION));
        this.addLayer(new BibbitArmorLayer<>(this, model, context.getModelManager()) {
        });
    }
    @Override
    public void render(Bibbit pEntity, float pEntityYaw, float pPartialTicks, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight) {
        float f8 = 0.0F;
        float f5 = 0.0F;
        if (pEntity.isAlive()) {
            f8 = pEntity.walkAnimation.speed(pPartialTicks);
            f5 = pEntity.walkAnimation.position(pPartialTicks);
            if (pEntity.isBaby()) {
                f5 *= 3.0F;
            }

            if (f8 > 1.0F) {
                f8 = 1.0F;
            }
        }
        model.setupAnim(pEntity, f5, f8, this.getBob(pEntity, pPartialTicks), Mth.rotLerp(pPartialTicks, pEntity.yHeadRotO, pEntity.yHeadRot)-Mth.rotLerp(pPartialTicks, pEntity.yBodyRotO, pEntity.yBodyRot), Mth.lerp(pPartialTicks, pEntity.xRotO, pEntity.getXRot()));
        super.render(pEntity, pEntityYaw, pPartialTicks, pPoseStack, pBuffer, pPackedLight);
    }

    protected void setupRotations(Bibbit pEntityLiving, PoseStack pMatrixStack, float pAgeInTicks, float pRotationYaw, float pPartialTicks) {
        super.setupRotations(pEntityLiving, pMatrixStack, pAgeInTicks, pRotationYaw, pPartialTicks);
        if (pEntityLiving.isPouncing()) {
            float f = -Mth.lerp(pPartialTicks, pEntityLiving.xRotO, pEntityLiving.getXRot());
            pMatrixStack.mulPose(Axis.XP.rotationDegrees(f));
        }
    }
    public ResourceLocation getTextureLocation(Bibbit pEntity) {
            return pEntity.isCrouching() ? TEXTURE_SITTING : TEXTURE;
    }
}
