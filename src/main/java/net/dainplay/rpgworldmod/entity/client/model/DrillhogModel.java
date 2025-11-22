package net.dainplay.rpgworldmod.entity.client.model;


import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.dainplay.rpgworldmod.RPGworldMod;
import net.dainplay.rpgworldmod.entity.custom.Drillhog;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;

import static net.dainplay.rpgworldmod.entity.custom.Burr_purr.DATA_SPIN_TIMER;

public class DrillhogModel<T extends Entity> extends EntityModel<T> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation(RPGworldMod.MOD_ID, "drillhog"), "main");
    private final ModelPart body;
    private final ModelPart head;
    private final ModelPart left_ear;
    private final ModelPart right_ear;
    private final ModelPart back_right_leg;
    private final ModelPart back_left_leg;
    private final ModelPart front_right_leg;
    private final ModelPart front_left_leg;

    public DrillhogModel(ModelPart root) {
        this.body = root.getChild("body");
        this.head = root.getChild("head");
        this.left_ear = this.head.getChild("left_ear");
        this.right_ear = this.head.getChild("right_ear");
        this.back_right_leg = root.getChild("back_right_leg");
        this.back_left_leg = root.getChild("back_left_leg");
        this.front_right_leg = root.getChild("front_right_leg");
        this.front_left_leg = root.getChild("front_left_leg");
    }


    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition body = partdefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-5.0F, -10.0F, -7.0F, 10.0F, 16.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 11.0F, 2.0F, 1.5708F, 0.0F, 0.0F));

        PartDefinition mane_r1 = body.addOrReplaceChild("mane_r1", CubeListBuilder.create().texOffs(0, 24).addBox(0.0F, -3.0F, -6.5F, 0.0F, 7.0F, 15.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -6.0F, 2.0F, -1.5708F, 0.0F, 0.0F));

        PartDefinition head = partdefinition.addOrReplaceChild("head", CubeListBuilder.create().texOffs(30, 24).addBox(-4.0F, -2.5F, -8.0F, 8.0F, 5.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(46, 37).addBox(3.0F, -3.5F, -6.5F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(46, 43).addBox(3.5F, -7.5F, -6.0F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(16, 46).addBox(-5.0F, -3.5F, -6.5F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(24, 46).addBox(-4.5F, -7.5F, -6.0F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 12.5F, -8.0F, 0.8727F, 0.0F, 0.0F));

        PartDefinition left_ear = head.addOrReplaceChild("left_ear", CubeListBuilder.create(), PartPose.offset(3.0F, -3.5F, -2.5F));

        PartDefinition left_ear_rotation2_r1 = left_ear.addOrReplaceChild("left_ear_rotation2_r1", CubeListBuilder.create().texOffs(36, 20).addBox(0.0F, -1.0F, -1.0F, 3.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.0F, 2.0F, 0.5F, 0.0F, 0.0F, 0.7854F));

        PartDefinition right_ear = head.addOrReplaceChild("right_ear", CubeListBuilder.create(), PartPose.offset(-4.0F, -2.5F, -2.5F));

        PartDefinition right_ear_rotation2_r1 = right_ear.addOrReplaceChild("right_ear_rotation2_r1", CubeListBuilder.create().texOffs(46, 20).addBox(-3.0F, -1.0F, -1.0F, 3.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 1.0F, 0.5F, 0.0F, 0.0F, -0.7854F));

        PartDefinition back_right_leg = partdefinition.addOrReplaceChild("back_right_leg", CubeListBuilder.create().texOffs(36, 10).addBox(-2.0F, 0.0F, -4.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(-3.0F, 18.0F, 7.0F));

        PartDefinition back_left_leg = partdefinition.addOrReplaceChild("back_left_leg", CubeListBuilder.create().texOffs(30, 37).addBox(-2.0F, 0.0F, -4.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(3.0F, 18.0F, 7.0F));

        PartDefinition front_right_leg = partdefinition.addOrReplaceChild("front_right_leg", CubeListBuilder.create().texOffs(0, 46).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(-3.0F, 18.0F, -5.0F));

        PartDefinition front_left_leg = partdefinition.addOrReplaceChild("front_left_leg", CubeListBuilder.create().texOffs(36, 0).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(3.0F, 18.0F, -5.0F));

        return LayerDefinition.create(meshdefinition, 64, 64);
    }


    /**
     * Sets this entity's model rotation angles
     */
    public void setupAnim(T pEntity, float pLimbSwing, float pLimbSwingAmount, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch) {
        this.right_ear.zRot = -0.6981317F - pLimbSwingAmount * Mth.sin(pLimbSwing);
        this.left_ear.zRot = 0.6981317F + pLimbSwingAmount * Mth.sin(pLimbSwing);
        this.head.yRot = pNetHeadYaw * ((float)Math.PI / 180F)/2;
        int i = ((Drillhog)pEntity).getAttackAnimationRemainingTicks();
        float f = 1.0F - (float)Mth.abs(10 - 2 * i) / 10.0F;
        this.head.xRot = Mth.lerp(f, 0.87266463F, -0.34906584F);

        float f1 = 1.2F;
        this.front_right_leg.xRot = Mth.cos(pLimbSwing) * 1.2F * pLimbSwingAmount;
        this.front_left_leg.xRot = Mth.cos(pLimbSwing + (float)Math.PI) * 1.2F * pLimbSwingAmount;
        this.back_right_leg.xRot = this.front_left_leg.xRot;
        this.back_left_leg.xRot = this.front_right_leg.xRot;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        body.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        head.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        back_right_leg.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        back_left_leg.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        front_right_leg.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        front_left_leg.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}