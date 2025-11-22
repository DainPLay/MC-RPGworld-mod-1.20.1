package net.dainplay.rpgworldmod.entity.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.dainplay.rpgworldmod.RPGworldMod;
import net.dainplay.rpgworldmod.entity.client.model.SheentroutModel;
import net.dainplay.rpgworldmod.entity.custom.Sheentrout;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class SheentroutRenderer extends MobRenderer<Sheentrout, SheentroutModel<Sheentrout>> {
   public static final ResourceLocation TEXTURE0 = new ResourceLocation(RPGworldMod.MOD_ID, "textures/entity/fish/sheentrout_0.png");
   public static final ResourceLocation TEXTURE1 = new ResourceLocation(RPGworldMod.MOD_ID, "textures/entity/fish/sheentrout_1.png");
   public static final ResourceLocation TEXTURE2 = new ResourceLocation(RPGworldMod.MOD_ID, "textures/entity/fish/sheentrout_2.png");
   public static final ResourceLocation TEXTURE3 = new ResourceLocation(RPGworldMod.MOD_ID, "textures/entity/fish/sheentrout_3.png");
   public static final ResourceLocation TEXTURE4 = new ResourceLocation(RPGworldMod.MOD_ID, "textures/entity/fish/sheentrout_4.png");
   public static final ResourceLocation TEXTURE5 = new ResourceLocation(RPGworldMod.MOD_ID, "textures/entity/fish/sheentrout_5.png");
    public static final ResourceLocation TEXTURE6 = new ResourceLocation(RPGworldMod.MOD_ID, "textures/entity/fish/sheentrout_6.png");
    public static final ResourceLocation TEXTURE7 = new ResourceLocation(RPGworldMod.MOD_ID, "textures/entity/fish/sheentrout_7.png");
    public static final ResourceLocation TEXTURE8 = new ResourceLocation(RPGworldMod.MOD_ID, "textures/entity/fish/sheentrout_8.png");
    public static final ResourceLocation TEXTURE9 = new ResourceLocation(RPGworldMod.MOD_ID, "textures/entity/fish/sheentrout_9.png");
    public static final ResourceLocation TEXTURE10 = new ResourceLocation(RPGworldMod.MOD_ID, "textures/entity/fish/sheentrout_10.png");
    public static final ResourceLocation TEXTURE11 = new ResourceLocation(RPGworldMod.MOD_ID, "textures/entity/fish/sheentrout_11.png");

   public SheentroutRenderer(EntityRendererProvider.Context p_173954_) {
      super(p_173954_, new SheentroutModel<>(p_173954_.bakeLayer(SheentroutModel.LAYER_LOCATION)), 0.3F);
   }
   /**
    * Returns the location of an entity's texture.
    */
   public ResourceLocation getTextureLocation(Sheentrout pEntity) {
       return switch (pEntity.tickCount % 28) {
           case 0 -> TEXTURE0;
           case 1 -> TEXTURE1;
           case 2 -> TEXTURE2;
           case 3 -> TEXTURE3;
           case 4 -> TEXTURE4;
           case 5 -> TEXTURE5;
           case 6 -> TEXTURE6;
           case 7 -> TEXTURE7;
           case 8 -> TEXTURE8;
           case 9 -> TEXTURE9;
           case 10 -> TEXTURE10;
           default -> TEXTURE11;
       };
   }

   protected void setupRotations(Sheentrout pEntityLiving, PoseStack pPoseStack, float pAgeInTicks, float pRotationYaw, float pPartialTicks) {
      super.setupRotations(pEntityLiving, pPoseStack, pAgeInTicks, pRotationYaw, pPartialTicks);
      float f = 4.3F * Mth.sin(0.6F * pAgeInTicks);
      pPoseStack.mulPose(Axis.YP.rotationDegrees(f));
      if (!pEntityLiving.isInWater()) {
         pPoseStack.translate(0.1F, 0.1F, -0.1F);
         pPoseStack.mulPose(Axis.ZP.rotationDegrees(90.0F));
      }

   }
}