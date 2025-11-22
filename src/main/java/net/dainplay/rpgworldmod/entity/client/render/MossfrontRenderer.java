package net.dainplay.rpgworldmod.entity.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.dainplay.rpgworldmod.RPGworldMod;
import net.dainplay.rpgworldmod.entity.client.model.MossfrontModel;
import net.dainplay.rpgworldmod.entity.custom.Mossfront;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class MossfrontRenderer extends MobRenderer<Mossfront, MossfrontModel<Mossfront>> {
   public static final ResourceLocation TEXTURE = new ResourceLocation(RPGworldMod.MOD_ID, "textures/entity/fish/mossfront.png");

   public MossfrontRenderer(EntityRendererProvider.Context p_173954_) {
      super(p_173954_, new MossfrontModel<>(p_173954_.bakeLayer(MossfrontModel.LAYER_LOCATION)), 0.3F);
   }

   /**
    * Returns the location of an entity's texture.
    */
   public ResourceLocation getTextureLocation(Mossfront pEntity) {
      return TEXTURE;
   }

   protected void setupRotations(Mossfront pEntityLiving, PoseStack pPoseStack, float pAgeInTicks, float pRotationYaw, float pPartialTicks) {
      super.setupRotations(pEntityLiving, pPoseStack, pAgeInTicks, pRotationYaw, pPartialTicks);
      float f = 4.3F * Mth.sin(0.6F * pAgeInTicks);
      pPoseStack.mulPose(Axis.YP.rotationDegrees(f));
      if (!pEntityLiving.isInWater()) {
         pPoseStack.translate(0.1F, 0.1F, -0.1F);
         pPoseStack.mulPose(Axis.ZP.rotationDegrees(90.0F));
      }

   }
}