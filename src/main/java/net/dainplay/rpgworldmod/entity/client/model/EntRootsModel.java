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

public class EntRootsModel extends Model {
        public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(new ResourceLocation(RPGworldMod.MOD_ID, "ent_roots"), "main");
        
        private final ModelPart roots;
        
        public EntRootsModel(ModelPart root) {
            super(RenderType::entityCutoutNoCull);
            this.roots = root.getChild("roots");
        }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition bb_main = partdefinition.addOrReplaceChild("roots", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -16.0F, 0.0F, 4.0F, 16.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(0, 20).addBox(-6.0F, -16.0F, 2.0F, 8.0F, 12.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(16, 0).addBox(-2.0F, -16.0F, -2.0F, 0.0F, 12.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(2.0F, -0.5F, -2.0F));

        return LayerDefinition.create(meshdefinition, 32, 32);
    }
        
        @Override
        public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer,
                                   int packedLight, int packedOverlay,
                                   float red, float green, float blue, float alpha) {
            this.roots.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        }
    }