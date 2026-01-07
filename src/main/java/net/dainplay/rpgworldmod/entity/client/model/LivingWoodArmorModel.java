package net.dainplay.rpgworldmod.entity.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.dainplay.rpgworldmod.RPGworldMod;
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

public class LivingWoodArmorModel<T extends Entity> extends EntityModel<T> {
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation(RPGworldMod.MOD_ID, "living_wood_armor"), "main");

	public final ModelPart head;
	public final ModelPart body;
	public final ModelPart left_shoe;
	public final ModelPart right_shoe;
	public final ModelPart left_arm;
	public final ModelPart right_arm;
	public final ModelPart left_leg;
	public final ModelPart right_leg;
	public final ModelPart waist;

	public LivingWoodArmorModel(ModelPart root) {
		this.head = root.getChild("head");
		this.body = root.getChild("body");
		this.left_shoe = root.getChild("left_shoe");
		this.right_shoe = root.getChild("right_shoe");
		this.left_arm = root.getChild("left_arm");
		this.right_arm = root.getChild("right_arm");
		this.left_leg = root.getChild("left_leg");
		this.right_leg = root.getChild("right_leg");
		this.waist = root.getChild("waist");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		// Голова поднята на 1 пиксель вверх (0.0625 блока)
		PartDefinition head = partdefinition.addOrReplaceChild("head", CubeListBuilder.create()
						.texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(1.0F))
						.texOffs(32, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(1.25F)),
				PartPose.offset(0.0F, 0.0F, 0.0F)); // Изменено с -1.0F на -1.0625F (поднято на 1 пиксель вверх)

		PartDefinition leaves_r1 = head.addOrReplaceChild("leaves_r1", CubeListBuilder.create()
						.texOffs(20, -4).addBox(0.0F, -4.0F, 0.0F, 0.0F, 5.0F, 4.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(4.75F, -4.75F, 4.75F, 0.0F, 0.7854F, 0.0F));

		// Тело остаётся с деформацией 1.0F
		PartDefinition body = partdefinition.addOrReplaceChild("body", CubeListBuilder.create()
						.texOffs(16, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(1.0F)),
				PartPose.offset(0.0F, 0.0F, 0.0F));

		// Ботинки остаются с деформацией 1.0F
		PartDefinition left_shoe = partdefinition.addOrReplaceChild("left_shoe", CubeListBuilder.create()
				.texOffs(0, 16).mirror().addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(1.0F))
				.mirror(false), PartPose.offset(1.9F, 12.0F, 0.0F));

		PartDefinition leaves_r2 = left_shoe.addOrReplaceChild("leaves_r2", CubeListBuilder.create()
						.texOffs(20, -4).addBox(0.0F, -4.0F, 0.0F, 0.0F, 5.0F, 4.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(2.75F, 9.0F, 2.75F, 0.0F, 0.7854F, 0.0F));

		PartDefinition right_shoe = partdefinition.addOrReplaceChild("right_shoe", CubeListBuilder.create()
						.texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(1.0F)),
				PartPose.offset(-1.9F, 12.0F, 0.0F));

		PartDefinition leaves_r3 = right_shoe.addOrReplaceChild("leaves_r3", CubeListBuilder.create()
						.texOffs(20, -4).addBox(0.0F, -4.0F, 0.0F, 0.0F, 5.0F, 4.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-2.75F, 9.0F, 2.75F, 0.0F, -0.7854F, 0.0F));

		// Руки остаются с деформацией 1.0F
		PartDefinition left_arm = partdefinition.addOrReplaceChild("left_arm", CubeListBuilder.create()
				.texOffs(40, 16).mirror().addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(1.0F))
				.mirror(false), PartPose.offset(5.0F, 2.0F, 0.0F));

		PartDefinition leaves_r4 = left_arm.addOrReplaceChild("leaves_r4", CubeListBuilder.create()
						.texOffs(20, -4).addBox(0.0F, -4.0F, 0.0F, 0.0F, 5.0F, 4.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(3.75F, 0.25F, 2.75F, 0.0F, 0.7854F, 0.0F));

		PartDefinition right_arm = partdefinition.addOrReplaceChild("right_arm", CubeListBuilder.create()
						.texOffs(40, 16).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(1.0F)),
				PartPose.offset(-5.0F, 2.0F, 0.0F));

		// Ноги изменены на 0.75F (по вашему запросу)
		PartDefinition left_leg = partdefinition.addOrReplaceChild("left_leg", CubeListBuilder.create()
				.texOffs(0, 16).mirror().addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.4F))
				.mirror(false), PartPose.offset(1.9F, 12.0F, 0.0F));

		PartDefinition right_leg = partdefinition.addOrReplaceChild("right_leg", CubeListBuilder.create()
						.texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.4F)),
				PartPose.offset(-1.9F, 12.0F, 0.0F));

		// Пояс изменён на 0.75F (по вашему запросу)
		PartDefinition waist = partdefinition.addOrReplaceChild("waist", CubeListBuilder.create()
						.texOffs(16, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.5F)),
				PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition leaves_r5 = waist.addOrReplaceChild("leaves_r5", CubeListBuilder.create()
						.texOffs(56, 23).addBox(-0.35F, -4.0F, -0.25F, 0.0F, 5.0F, 4.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(4.925F, 10.1F, 2.425F, 0.0F, 0.7854F, 0.0F));

		return LayerDefinition.create(meshdefinition, 64, 32);
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		head.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		body.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		left_shoe.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		right_shoe.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		left_arm.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		right_arm.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		left_leg.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		right_leg.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		waist.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
	}

	@Override
	public void setupAnim(T pEntity, float pLimbSwing, float pLimbSwingAmount, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch) {

	}
}