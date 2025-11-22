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

public class BibbitArmorModel<Type extends Bibbit> extends EntityModel<Bibbit> {
	// This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation(RPGworldMod.MOD_ID, "bibbit_armor"), "main");
	public final ModelPart left_leg;
	public final ModelPart right_leg;
	private static final int LEG_SIZE = 6;
	private static final float HEAD_HEIGHT = 16.5F;
	private static final float LEG_POS = 17.5F;
	private float legMotionPos;

	public BibbitArmorModel(ModelPart root) {
		this.left_leg = root.getChild("left_leg");
		this.right_leg = root.getChild("right_leg");
	}
	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition left_leg = partdefinition.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(0, 12).mirror().addBox(-1.4F, -3.0F, -1.5F, 3.0F, 9.0F, 3.0F, new CubeDeformation(0.5F)).mirror(false), PartPose.offset(1.9F, 18.0F, 0.5F));

		PartDefinition right_leg = partdefinition.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(0, 12).addBox(-1.6F, -3.0F, -1.5F, 3.0F, 9.0F, 3.0F, new CubeDeformation(0.5F)), PartPose.offset(-1.9F, 18.0F, 0.5F));

		return LayerDefinition.create(meshdefinition, 48, 24);
	}

	public void setAllVisible(boolean pVisible) {
		this.left_leg.visible = pVisible;
		this.right_leg.visible = pVisible;
	}

	@Override
	public void setupAnim(Bibbit entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {

		limbSwingAmount = Math.min(0.25F, limbSwingAmount);
		this.left_leg.xRot = Mth.sin(limbSwing * 1.5F * 0.5F) * 2.0F * limbSwingAmount;
		this.right_leg.xRot = Mth.sin(limbSwing * 1.5F * 0.5F + (float)Math.PI) * 2.0F * limbSwingAmount;
		this.left_leg.zRot = 0.17453292F * Mth.cos(limbSwing * 1.5F * 0.5F) * limbSwingAmount;
		this.right_leg.zRot = 0.17453292F * Mth.cos(limbSwing * 1.5F * 0.5F + (float)Math.PI) * limbSwingAmount;
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		left_leg.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		right_leg.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
	}

	/**
	 * Sets this entity's model rotation angles
	 */
}