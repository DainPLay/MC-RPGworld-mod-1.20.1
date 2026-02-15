package net.dainplay.rpgworldmod.entity.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.dainplay.rpgworldmod.RPGworldMod;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

public class PrismarineShardModel extends Model {
   public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation(RPGworldMod.MOD_ID, "prismarine_shard"), "main");
   private final ModelPart root;

   public PrismarineShardModel(ModelPart pRoot) {
      super(RenderType::entitySolid);
      this.root = pRoot;
   }

   public static LayerDefinition createBodyLayer() {
      MeshDefinition meshdefinition = new MeshDefinition();
      PartDefinition partdefinition = meshdefinition.getRoot();

      PartDefinition bb_main = partdefinition.addOrReplaceChild("bb_main", CubeListBuilder.create().texOffs(2, 0).addBox(0.0F, -28.0F, -0.5F, 5.0F, 7.0F, 0.0F, new CubeDeformation(0.0F))
              .texOffs(2, 0).addBox(0.0F, -28.0F, 0.5F, 5.0F, 7.0F, 0.0F, new CubeDeformation(0.0F))
              .texOffs(3, 6).addBox(2.0F, -21.0F, -0.5F, 3.0F, 0.0F, 1.0F, new CubeDeformation(0.0F))
              .texOffs(4, 0).addBox(3.0F, -28.0F, -0.5F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F))
              .texOffs(5, 4).addBox(4.0F, -24.0F, -0.5F, 1.0F, 0.0F, 1.0F, new CubeDeformation(0.0F))
              .texOffs(-1, 4).addBox(0.0F, -24.0F, -0.5F, 1.0F, 0.0F, 1.0F, new CubeDeformation(0.0F))
              .texOffs(-1, 2).addBox(1.0F, -26.0F, -0.5F, 1.0F, 0.0F, 1.0F, new CubeDeformation(0.0F))
              .texOffs(-1, 1).addBox(2.0F, -27.0F, -0.5F, 1.0F, 0.0F, 1.0F, new CubeDeformation(0.0F))
              .texOffs(-1, 5).addBox(0.0F, -22.0F, -0.5F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F))
              .texOffs(5, 2).addBox(4.0F, -25.0F, -0.5F, 1.0F, 0.0F, 1.0F, new CubeDeformation(0.0F))
              .texOffs(0, -1).addBox(3.0F, -28.0F, -0.5F, 0.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
              .texOffs(0, 0).addBox(2.0F, -27.0F, -0.5F, 0.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
              .texOffs(0, 1).addBox(1.0F, -26.0F, -0.5F, 0.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
              .texOffs(0, 3).addBox(0.0F, -24.0F, -0.5F, 0.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
              .texOffs(0, 5).addBox(2.0F, -22.0F, -0.5F, 0.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
              .texOffs(6, 3).addBox(5.0F, -24.0F, -0.5F, 0.0F, 3.0F, 1.0F, new CubeDeformation(0.0F))
              .texOffs(6, -1).addBox(5.0F, -28.0F, -0.5F, 0.0F, 3.0F, 1.0F, new CubeDeformation(0.0F))
              .texOffs(12, 2).addBox(4.0F, -25.0F, -0.5F, 0.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 24.0F, 0.0F));

      return LayerDefinition.create(meshdefinition, 16, 16);
   }

   public void renderToBuffer(PoseStack pPoseStack, VertexConsumer pBuffer, int pPackedLight, int pPackedOverlay, float pRed, float pGreen, float pBlue, float pAlpha) {
      this.root.render(pPoseStack, pBuffer, pPackedLight, pPackedOverlay, pRed, pGreen, pBlue, pAlpha);
   }
}