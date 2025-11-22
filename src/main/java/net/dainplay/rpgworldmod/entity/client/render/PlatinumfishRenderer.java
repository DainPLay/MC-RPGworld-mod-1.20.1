package net.dainplay.rpgworldmod.entity.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.dainplay.rpgworldmod.RPGworldMod;
import net.dainplay.rpgworldmod.entity.client.model.PlatinumfishModel;
import net.dainplay.rpgworldmod.entity.custom.Platinumfish;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class PlatinumfishRenderer extends MobRenderer<Platinumfish, PlatinumfishModel<Platinumfish>> {
   public static final ResourceLocation TEXTURE = new ResourceLocation(RPGworldMod.MOD_ID, "textures/entity/fish/platinumfish.png");

   public PlatinumfishRenderer(EntityRendererProvider.Context p_173954_) {
      super(p_173954_, new PlatinumfishModel<>(p_173954_.bakeLayer(PlatinumfishModel.LAYER_LOCATION)), 0.3F);
   }

   /**
    * Returns the location of an entity's texture.
    */
   public ResourceLocation getTextureLocation(Platinumfish pEntity) {
      return TEXTURE;
   }

   protected void setupRotations(Platinumfish pEntityLiving, PoseStack pPoseStack, float pAgeInTicks, float pRotationYaw, float pPartialTicks) {
      super.setupRotations(pEntityLiving, pPoseStack, pAgeInTicks, pRotationYaw, pPartialTicks);
      float f = 4.3F * Mth.sin(0.6F * pAgeInTicks);
      pPoseStack.mulPose(Axis.YP.rotationDegrees(f));
      if (!pEntityLiving.isInWater()) {
         pPoseStack.translate(0.1F, 0.1F, -0.1F);
         pPoseStack.mulPose(Axis.ZP.rotationDegrees(90.0F));
      }

   }
}