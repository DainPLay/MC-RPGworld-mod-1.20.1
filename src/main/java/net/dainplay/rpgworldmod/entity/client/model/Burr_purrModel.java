package net.dainplay.rpgworldmod.entity.client.model;


import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.dainplay.rpgworldmod.RPGworldMod;
import net.dainplay.rpgworldmod.entity.custom.Burr_purr;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;

import static net.dainplay.rpgworldmod.entity.custom.Burr_purr.*;

public class Burr_purrModel<T extends Entity> extends EntityModel<T> {
   // This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
   public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation(RPGworldMod.MOD_ID, "burr_purr"), "main");
    private final ModelPart normal;
    private final ModelPart body6;
    private final ModelPart body4;
    private final ModelPart body5;
    private final ModelPart body3;
    private final ModelPart body2;
    private final ModelPart ball;
    private final ModelPart[] bodyParts = new ModelPart[5];

    public Burr_purrModel(ModelPart root) {
        this.normal = root.getChild("normal");
        this.body6 = this.normal.getChild("body6");
        this.body4 = this.normal.getChild("body4");
        this.body5 = this.normal.getChild("body5");
        this.body3 = this.normal.getChild("body3");
        this.body2 = this.normal.getChild("body2");
        this.ball = root.getChild("ball");
        this.bodyParts[0] = this.body2;
        this.bodyParts[1] = this.body3;
        this.bodyParts[2] = this.body4;
        this.bodyParts[3] = this.body5;
        this.bodyParts[4] = this.body6;
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition normal = partdefinition.addOrReplaceChild("normal", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

        PartDefinition body6 = normal.addOrReplaceChild("body6", CubeListBuilder.create().texOffs(0, 32).addBox(-1.0F, 0.0F, -1.0F, 3.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -1.0F, 6.0F));

        PartDefinition body4 = normal.addOrReplaceChild("body4", CubeListBuilder.create().texOffs(24, 12).addBox(-3.5F, -3.0F, -1.5F, 8.0F, 6.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -3.0F, 0.5F));

        PartDefinition body5 = normal.addOrReplaceChild("body5", CubeListBuilder.create().texOffs(24, 21).addBox(-3.0F, -3.0F, -1.5F, 7.0F, 5.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(0, 22).addBox(-4.0F, -5.0F, -1.5F, 9.0F, 7.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -2.0F, 3.5F));

        PartDefinition body3 = normal.addOrReplaceChild("body3", CubeListBuilder.create().texOffs(0, 12).addBox(-4.0F, -3.0F, -1.5F, 9.0F, 7.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-5.0F, -5.0F, -1.5F, 11.0F, 9.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -4.0F, -2.5F));

        PartDefinition body2 = normal.addOrReplaceChild("body2", CubeListBuilder.create().texOffs(24, 29).addBox(-2.0F, -1.0F, -1.0F, 5.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(28, 0).addBox(-4.0F, -3.0F, -1.0F, 9.0F, 6.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(28, 7).addBox(-1.0F, 1.0F, -3.0F, 3.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -3.0F, -5.0F));

        PartDefinition ball = partdefinition.addOrReplaceChild("ball", CubeListBuilder.create().texOffs(36, 30).addBox(0.0F, -6.0F, -6.0F, 0.0F, 12.0F, 12.0F, new CubeDeformation(0.0F))
                .texOffs(36, 30).addBox(3.5F, -6.0F, -6.0F, 0.0F, 12.0F, 12.0F, new CubeDeformation(0.0F))
                .texOffs(36, 30).addBox(-3.5F, -6.0F, -6.0F, 0.0F, 12.0F, 12.0F, new CubeDeformation(0.0F))
                .texOffs(0, 36).addBox(-4.5F, -4.5F, -4.5F, 9.0F, 9.0F, 9.0F, new CubeDeformation(-0.25F)), PartPose.offset(0.5F, 18.5F, -0.5F));

        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    @Override
    public void setupAnim(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        if(entity.getEntityData().get(DATA_SPIN_TIMER) > entity.getEntityData().get(DATA_DASH_TIMER)) {
            this.ball.xRot = (float) Math.toRadians(ageInTicks * entity.getEntityData().get(DATA_SPIN_TIMER));
            spawnParticlesBehind(entity, 3, 1);
        }
        else
            this.ball.xRot = (float) Math.toRadians(ageInTicks * entity.getEntityData().get(DATA_DASH_TIMER));
        if(!entity.getEntityData().get(DATA_IS_MOVING)) ageInTicks = 9.3F;
        for (int i = 0; i < this.bodyParts.length; ++i) {
            this.bodyParts[i].yRot = Mth.cos(ageInTicks * 0.9F + (float) i * 0.15F * (float) Math.PI) * (float) Math.PI * 0.05F * (float) (1 + Math.abs(i - 2));
            this.bodyParts[i].x = Mth.sin(ageInTicks * 0.9F + (float) i * 0.15F * (float) Math.PI) * (float) Math.PI * 0.2F * (float) Math.abs(i - 2);
        }
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        normal.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        ball.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}