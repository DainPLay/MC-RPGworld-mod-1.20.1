package net.dainplay.rpgworldmod.entity.client.model;// Made with Blockbench 4.8.3
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports


import net.dainplay.rpgworldmod.RPGworldMod;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;

public class SheentroutModel<T extends Entity> extends HierarchicalModel<T> {
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation(RPGworldMod.MOD_ID, "sheentrout"), "main");
	private static final String BODY_FRONT = "body_front";
	private static final String BODY_BACK = "body_back";
	private final ModelPart root;
	private final ModelPart bodyBack;

	public SheentroutModel(ModelPart pRoot) {
		this.root = pRoot;
		this.bodyBack = pRoot.getChild("body_back");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition head = partdefinition.addOrReplaceChild("head", CubeListBuilder.create().texOffs(20, 10).addBox(-1.0F, -2.0F, -3.0F, 2.0F, 4.0F, 3.0F, new CubeDeformation(0.25F)), PartPose.offset(0.0F, 21.5F, -8.0F));

		PartDefinition body_front = partdefinition.addOrReplaceChild("body_front", CubeListBuilder.create().texOffs(0, 0).addBox(-1.5F, -2.5F, 0.0F, 3.0F, 5.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 21.5F, -8.0F));

		PartDefinition body_back = partdefinition.addOrReplaceChild("body_back", CubeListBuilder.create().texOffs(1, 13).addBox(-1.5F, -1.5F, -1.0F, 3.0F, 4.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 21.5F, 0.0F));

		PartDefinition fin_back_2 = body_back.addOrReplaceChild("fin_back_2", CubeListBuilder.create().texOffs(20, 17).addBox(0.0F, 0.0F, -1.0F, 0.0F, 3.0F, 4.0F, new CubeDeformation(0.0F))
				.texOffs(0, 23).addBox(0.0F, 7.0F, 1.0F, 0.0F, 2.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -4.5F, -1.0F));

		PartDefinition tail = body_back.addOrReplaceChild("tail", CubeListBuilder.create().texOffs(20, 0).addBox(0.0F, -2.5F, -3.0F, 0.0F, 5.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 8.0F));

		PartDefinition fin_left = partdefinition.addOrReplaceChild("fin_left", CubeListBuilder.create().texOffs(6, 23).addBox(0.0F, 0.0F, 0.0F, 2.0F, 0.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.5F, 23.0F, -8.0F, 0.0F, 0.0F, 0.7854F));

		PartDefinition fin_right = partdefinition.addOrReplaceChild("fin_right", CubeListBuilder.create().texOffs(14, 24).addBox(-2.0F, 0.0F, 0.0F, 2.0F, 0.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.5F, 23.0F, -8.0F, 0.0F, 0.0F, -0.7854F));

		PartDefinition fin_back_1 = partdefinition.addOrReplaceChild("fin_back_1", CubeListBuilder.create().texOffs(0, 23).addBox(0.0F, 0.0F, -1.0F, 0.0F, 2.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 17.0F, -3.0F));

		return LayerDefinition.create(meshdefinition, 32, 32);
	}

	public ModelPart root() {
		return this.root;
	}

	/**
	 * Sets this entity's model rotation angles
	 */
	public void setupAnim(T pEntity, float pLimbSwing, float pLimbSwingAmount, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch) {
		float f = 1.0F;
		float f1 = 1.0F;
		if (!pEntity.isInWater()) {
			f = 1.3F;
			f1 = 1.7F;
		}

		this.bodyBack.yRot = -f * 0.25F * Mth.sin(f1 * 0.6F * pAgeInTicks);
	}
}