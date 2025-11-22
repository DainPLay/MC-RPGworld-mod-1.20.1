package net.dainplay.rpgworldmod.entity.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.dainplay.rpgworldmod.RPGworldMod;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

public class DrillSpearModel extends Model {
   public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation(RPGworldMod.MOD_ID, "drill_spear"), "main");
   private final ModelPart root;

   public DrillSpearModel(ModelPart pRoot) {
      super(RenderType::entitySolid);
      this.root = pRoot;
   }

   public static LayerDefinition createBodyLayer() {
      MeshDefinition meshdefinition = new MeshDefinition();
      PartDefinition partdefinition = meshdefinition.getRoot();

      PartDefinition bb_main = partdefinition.addOrReplaceChild("bb_main", CubeListBuilder.create().texOffs(4, 11).addBox(-1.0F, -23.0F, -1.0F, 2.0F, 3.0F, 2.0F, new CubeDeformation(0.0F))
              .texOffs(0, 0).addBox(-0.5F, -20.0F, -0.5F, 1.0F, 20.0F, 1.0F, new CubeDeformation(0.0F))
              .texOffs(12, 15).addBox(-0.5F, -31.0F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
              .texOffs(12, 11).addBox(-1.0F, -29.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
              .texOffs(4, 6).addBox(-1.5F, -27.0F, -1.5F, 3.0F, 2.0F, 3.0F, new CubeDeformation(0.0F))
              .texOffs(4, 0).addBox(-2.0F, -25.0F, -2.0F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 24.0F, 0.0F));

      return LayerDefinition.create(meshdefinition, 32, 32);
   }

   public void renderToBuffer(PoseStack pPoseStack, VertexConsumer pBuffer, int pPackedLight, int pPackedOverlay, float pRed, float pGreen, float pBlue, float pAlpha) {
      this.root.render(pPoseStack, pBuffer, pPackedLight, pPackedOverlay, pRed, pGreen, pBlue, pAlpha);
   }
}