package net.dainplay.rpgworldmod.entity.client.model;// Made with Blockbench 5.0.7


import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.dainplay.rpgworldmod.RPGworldMod;
import net.dainplay.rpgworldmod.entity.custom.TireSwingEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

public class TireSwingModel<T extends Entity> extends EntityModel<T> {
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation(RPGworldMod.MOD_ID, "textures/entity/tire_swing/tire_swing.png"), "main");
	public final ModelPart swing;

	public TireSwingModel(ModelPart root) {
		this.swing = root.getChild("swing");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition swing = partdefinition.addOrReplaceChild("swing", CubeListBuilder.create().texOffs(0, 0).addBox(-7.0F, 0.0F, -3.0F, 4.0F, 14.0F, 6.0F, new CubeDeformation(0.0F))
		.texOffs(0, 20).addBox(3.0F, 0.0F, -3.0F, 4.0F, 14.0F, 6.0F, new CubeDeformation(0.0F))
		.texOffs(20, 13).addBox(-3.0F, 10.0F, -3.0F, 6.0F, 4.0F, 6.0F, new CubeDeformation(0.0F))
		.texOffs(20, 0).addBox(-3.0F, -0.5F, -3.5F, 6.0F, 6.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 10.0F, 0.0F));

		return LayerDefinition.create(meshdefinition, 64, 64);
	}

	@Override
	public void setupAnim(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {

		if (entity instanceof TireSwingEntity swing) {
			float swingAngle = swing.getSwingAngle();
			float rotation = (float) Math.sin(Math.toRadians(swingAngle)) * 30.0F;
			this.swing.xRot = (float) Math.toRadians(rotation);
		}
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		swing.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
	}
}