package net.dainplay.rpgworldmod.entity.client.model;// Made with Blockbench 4.8.3
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports


import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.dainplay.rpgworldmod.RPGworldMod;
import net.dainplay.rpgworldmod.entity.custom.Bibbit;
import net.minecraft.client.model.AgeableListModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class BibbitModel<Type extends Bibbit> extends EntityModel<Bibbit> {
	// This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation(RPGworldMod.MOD_ID, "bibbit"), "main");
	ModelPart head;
	public final ModelPart left_leg;
	public final ModelPart right_leg;
	ModelPart sitting_head;

	public BibbitModel(ModelPart root) {
		this.head = root.getChild("head");
		this.left_leg = root.getChild("left_leg");
		this.right_leg = root.getChild("right_leg");
		this.sitting_head = root.getChild("sitting_head");
	}
	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition head = partdefinition.addOrReplaceChild("head", CubeListBuilder.create().texOffs(32, 0).addBox(-4.0F, -6.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.5F))
				.texOffs(0, 0).addBox(-4.0F, -6.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 16.0F, 0.0F));

		PartDefinition left_leg = partdefinition.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(0, 16).mirror().addBox(-0.9F, 1.0F, -1.5F, 2.0F, 8.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false)
				.texOffs(0, 27).mirror().addBox(-0.9F, 1.0F, -1.5F, 2.0F, 8.0F, 3.0F, new CubeDeformation(0.25F)).mirror(false), PartPose.offset(1.9F, 15.0F, 0.5F));

		PartDefinition right_leg = partdefinition.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(10, 16).addBox(-1.1F, 1.0F, -1.5F, 2.0F, 8.0F, 3.0F, new CubeDeformation(0.0F))
				.texOffs(10, 27).addBox(-1.1F, 1.0F, -1.5F, 2.0F, 8.0F, 3.0F, new CubeDeformation(0.25F)), PartPose.offset(-1.9F, 15.0F, 0.5F));

		PartDefinition sitting_head = partdefinition.addOrReplaceChild("sitting_head", CubeListBuilder.create().texOffs(32, 38).addBox(-4.0F, -6.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.5F))
				.texOffs(0, 38).addBox(-4.0F, -6.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 21.75F, 0.0F));

		return LayerDefinition.create(meshdefinition, 64, 64);
	}

	@Override
	public void setupAnim(Bibbit entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {

		limbSwingAmount = Math.min(0.25F, limbSwingAmount);
			this.head.xRot = headPitch * ((float)Math.PI / 180F);
			this.head.yRot = netHeadYaw * ((float)Math.PI / 180F);
		this.left_leg.xRot = Mth.sin(limbSwing * 1.5F * 0.5F) * 2.0F * limbSwingAmount;
		this.right_leg.xRot = Mth.sin(limbSwing * 1.5F * 0.5F + (float)Math.PI) * 2.0F * limbSwingAmount;
		this.left_leg.zRot = 0.17453292F * Mth.cos(limbSwing * 1.5F * 0.5F) * limbSwingAmount;
		this.right_leg.zRot = 0.17453292F * Mth.cos(limbSwing * 1.5F * 0.5F + (float)Math.PI) * limbSwingAmount;
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		head.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		sitting_head.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		left_leg.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		right_leg.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
	}

	/**
	 * Sets this entity's model rotation angles
	 */
}