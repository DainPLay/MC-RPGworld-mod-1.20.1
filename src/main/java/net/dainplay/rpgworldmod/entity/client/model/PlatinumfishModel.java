package net.dainplay.rpgworldmod.entity.client.model;


import net.dainplay.rpgworldmod.RPGworldMod;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;

public class PlatinumfishModel<T extends Entity> extends HierarchicalModel<T> {
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation(RPGworldMod.MOD_ID, "platinumfish"), "main");
	private final ModelPart root;
	private final ModelPart tail;

	public PlatinumfishModel(ModelPart pRoot) {
		this.root = pRoot;
		this.tail = pRoot.getChild("tail");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition body = partdefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(15, 1).addBox(-1.0F, -1.5F, -2.0F, 2.0F, 3.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 22.5F, 0.0F));

		body.addOrReplaceChild("right_fin", CubeListBuilder.create().texOffs(0, 15).addBox(-2.0F, -2.0F, 0.0F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.0F, 1.5F, 0.0F, 0.0F, 0.7854F, 0.0F));

		body.addOrReplaceChild("left_fin", CubeListBuilder.create().texOffs(4, 15).addBox(0.0F, -2.0F, 0.0F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.0F, 1.5F, 0.0F, 0.0F, -0.7854F, 0.0F));

		partdefinition.addOrReplaceChild("fin_top", CubeListBuilder.create().texOffs(14, 9).addBox(0.0F, -4.0F, 0.0F, 0.0F, 4.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 21.0F, -3.0F));

		partdefinition.addOrReplaceChild("tail", CubeListBuilder.create().texOffs(0, 0).addBox(0.0F, -4.5F, 0.0F, 0.0F, 8.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 22.5F, 3.0F));

		return LayerDefinition.create(meshdefinition, 32, 32);
	}


	public ModelPart root() {
		return this.root;
	}


	public void setupAnim(T pEntity, float pLimbSwing, float pLimbSwingAmount, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch) {
		float f = 1.0F;
		if (!pEntity.isInWater()) {
			f = 1.5F;
		}

		this.tail.yRot = -f * 0.45F * Mth.sin(0.6F * pAgeInTicks);
	}
}