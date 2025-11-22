package net.dainplay.rpgworldmod.entity.client.model;


import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.dainplay.rpgworldmod.RPGworldMod;
import net.dainplay.rpgworldmod.entity.custom.Mintobat;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;

public class FireflanternModel<T extends Entity> extends EntityModel<T> {
   // This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
   public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation(RPGworldMod.MOD_ID, "fireflantern"), "main");
    private final ModelPart head;
    private final ModelPart propeller;

    public FireflanternModel(ModelPart root) {
        this.head = root.getChild("head");
        this.propeller = root.getChild("propeller");
    }


    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition head = partdefinition.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 16).addBox(-5.0F, 14.25F, -5.0F, 10.0F, 1.0F, 10.0F, new CubeDeformation(0.0F))
                .texOffs(0, 27).addBox(-5.0F, 2.25F, -5.0F, 10.0F, 1.0F, 10.0F, new CubeDeformation(0.0F))
                .texOffs(0, 38).addBox(-4.0F, 0.25F, -4.0F, 8.0F, 2.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(40, 16).addBox(3.0F, 3.25F, 3.0F, 2.0F, 11.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(8, 48).addBox(-5.0F, 3.25F, 3.0F, 2.0F, 11.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(16, 48).addBox(-5.0F, 3.25F, -5.0F, 2.0F, 11.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(0, 48).addBox(3.0F, 3.25F, -5.0F, 2.0F, 11.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(32, 44).addBox(-3.0F, 3.25F, -3.0F, 6.0F, 11.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 2.75F, 0.0F));

        PartDefinition cube_r1 = head.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(40, 25).addBox(0.0F, -7.0F, -4.0F, 0.0F, 11.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 10.25F, 0.0F, 0.0F, 2.3562F, 0.0F));

        PartDefinition cube_r2 = head.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(40, 25).addBox(0.0F, -7.0F, -4.0F, 0.0F, 11.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 10.25F, 0.0F, 0.0F, 0.7854F, 0.0F));

        PartDefinition propeller = partdefinition.addOrReplaceChild("propeller", CubeListBuilder.create().texOffs(0, 0).addBox(-8.0F, -0.5F, -8.0F, 16.0F, 0.0F, 16.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 3.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    @Override
    public void setupAnim(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        float propellerRotationSpeed = 0.0f; // Default to 0 when not airborne
        float maxBodySwingDegrees = 5.0f; // Maximum rotation in degrees
        float bodySwingSpeed = 0.0f; // Adjust for the speed of the sway

        if (!entity.onGround()) {
            // If the entity is in the air, set a high rotation speed.
            propellerRotationSpeed = 45.0f; // Adjust for desired speed
            bodySwingSpeed = 0.15f; // Adjust for the speed of the sway
        }
        // Apply rotation
        this.propeller.yRot = (float) Math.toRadians(ageInTicks * propellerRotationSpeed);
        float bodySwingAngle = maxBodySwingDegrees * Mth.sin(ageInTicks * bodySwingSpeed) ; // sin to create sway
        this.head.zRot = (float)Math.toRadians(bodySwingAngle); // Rotation around the z-axis

    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        head.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        propeller.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}