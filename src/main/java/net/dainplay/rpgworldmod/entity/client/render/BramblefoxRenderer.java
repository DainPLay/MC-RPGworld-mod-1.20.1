package net.dainplay.rpgworldmod.entity.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.dainplay.rpgworldmod.RPGworldMod;
import net.dainplay.rpgworldmod.entity.client.model.BramblefoxModel;
import net.dainplay.rpgworldmod.entity.custom.Bramblefox;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;


public class BramblefoxRenderer extends MobRenderer<Bramblefox, BramblefoxModel<Bramblefox>> {
    public static final ResourceLocation TEXTURE_STAGE0 = new ResourceLocation(RPGworldMod.MOD_ID, "textures/entity/bramblefox/bramblefox_stage0.png");
    public static final ResourceLocation TEXTURE_STAGE1 = new ResourceLocation(RPGworldMod.MOD_ID, "textures/entity/bramblefox/bramblefox_stage1.png");
    public static final ResourceLocation TEXTURE_STAGE2 = new ResourceLocation(RPGworldMod.MOD_ID, "textures/entity/bramblefox/bramblefox_stage2.png");
    public static final ResourceLocation TEXTURE_STAGE3 = new ResourceLocation(RPGworldMod.MOD_ID, "textures/entity/bramblefox/bramblefox_stage3.png");
    public static final ResourceLocation TEXTURE_STAGE0_SLEEPING = new ResourceLocation(RPGworldMod.MOD_ID, "textures/entity/bramblefox/bramblefox_stage0_sleeping.png");
    public static final ResourceLocation TEXTURE_STAGE1_SLEEPING = new ResourceLocation(RPGworldMod.MOD_ID, "textures/entity/bramblefox/bramblefox_stage1_sleeping.png");
    public static final ResourceLocation TEXTURE_STAGE2_SLEEPING = new ResourceLocation(RPGworldMod.MOD_ID, "textures/entity/bramblefox/bramblefox_stage2_sleeping.png");
    public static final ResourceLocation TEXTURE_STAGE3_SLEEPING = new ResourceLocation(RPGworldMod.MOD_ID, "textures/entity/bramblefox/bramblefox_stage3_sleeping.png");

    public BramblefoxRenderer(EntityRendererProvider.Context context) {
        super(context, new BramblefoxModel<>(context.bakeLayer(BramblefoxModel.LAYER_LOCATION)), 0.4f);
        this.addLayer(new BramblefoxHeldItemLayer(this, context.getItemInHandRenderer()));
    }
    protected void setupRotations(Bramblefox pEntityLiving, PoseStack pMatrixStack, float pAgeInTicks, float pRotationYaw, float pPartialTicks) {
        super.setupRotations(pEntityLiving, pMatrixStack, pAgeInTicks, pRotationYaw, pPartialTicks);
        if (pEntityLiving.isPouncing()) {
            float f = -Mth.lerp(pPartialTicks, pEntityLiving.xRotO, pEntityLiving.getXRot());
            pMatrixStack.mulPose(Axis.XP.rotationDegrees(f));
        }
    }
    public ResourceLocation getTextureLocation(Bramblefox pEntity) {
        if (pEntity.getBramblefoxType() == Bramblefox.Type.STAGE0) {
            return pEntity.isSleeping() ? TEXTURE_STAGE0_SLEEPING : TEXTURE_STAGE0;
        }
        else if (pEntity.getBramblefoxType() == Bramblefox.Type.STAGE1) {
            return pEntity.isSleeping() ? TEXTURE_STAGE1_SLEEPING : TEXTURE_STAGE1;
        }
        else if (pEntity.getBramblefoxType() == Bramblefox.Type.STAGE2) {
            return pEntity.isSleeping() ? TEXTURE_STAGE2_SLEEPING : TEXTURE_STAGE2;
        }
        else {
            return pEntity.isSleeping() ? TEXTURE_STAGE3_SLEEPING : TEXTURE_STAGE3;
        }
    }
}
