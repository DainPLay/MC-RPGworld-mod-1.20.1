package net.dainplay.rpgworldmod.entity.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.dainplay.rpgworldmod.RPGworldMod;
import net.dainplay.rpgworldmod.entity.client.model.BhleeModel;
import net.dainplay.rpgworldmod.entity.custom.Bhlee;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class BhleeRenderer extends MobRenderer<Bhlee, BhleeModel<Bhlee>> {
   public static final ResourceLocation TEXTURE = new ResourceLocation(RPGworldMod.MOD_ID, "textures/entity/fish/bhlee.png");

   public BhleeRenderer(EntityRendererProvider.Context p_173954_) {
      super(p_173954_, new BhleeModel<>(p_173954_.bakeLayer(BhleeModel.LAYER_LOCATION)), 0.3F);
   }

   /**
    * Returns the location of an entity's texture.
    */
   public ResourceLocation getTextureLocation(Bhlee pEntity) {
      return TEXTURE;
   }

   protected void setupRotations(Bhlee pEntityLiving, PoseStack pPoseStack, float pAgeInTicks, float pRotationYaw, float pPartialTicks) {
      super.setupRotations(pEntityLiving, pPoseStack, pAgeInTicks, pRotationYaw, pPartialTicks);
      float f = 4.3F * Mth.sin(0.6F * pAgeInTicks);
      pPoseStack.mulPose(Axis.YP.rotationDegrees(f));
      if (!pEntityLiving.isInWater()) {
         pPoseStack.translate(0.1F, 0.1F, -0.1F);
         pPoseStack.mulPose(Axis.ZP.rotationDegrees(90.0F));
      }

   }
}