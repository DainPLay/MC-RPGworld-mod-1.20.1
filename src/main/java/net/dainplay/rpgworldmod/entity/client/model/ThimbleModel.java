package net.dainplay.rpgworldmod.entity.client.model;

import com.google.common.collect.ImmutableList;
import net.dainplay.rpgworldmod.entity.client.render.CurioLayers;
import net.dainplay.rpgworldmod.entity.client.render.CurioRenderers;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

import java.util.function.Function;

public class ThimbleModel extends HumanoidModel<LivingEntity> {



    public ThimbleModel(ModelPart part, Function<ResourceLocation, RenderType> renderType) {
        super(part, renderType);
    }


    @Override
    protected Iterable<ModelPart> headParts() {
        return ImmutableList.of();
    }

    @Override
    protected Iterable<ModelPart> bodyParts() {
        return ImmutableList.of(rightArm);
    }

    public static ThimbleModel createThimbleModel() {
        return new ThimbleModel(CurioRenderers.bakeLayer(CurioLayers.THIMBLE), RenderType::entityTranslucent) {
            //private final ModelPart cloud = charm.getChild("cloud");

            @Override
            public void setupAnim(LivingEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
                super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
                //cloud.yRot = (ageInTicks) / 50;
                //cloud.y = Mth.cos((ageInTicks) / 30) / 2;
            }
        };
    }


    private static MeshDefinition create(CubeListBuilder clb) {
        MeshDefinition mesh = createMesh(CubeDeformation.NONE, 0);

        mesh.getRoot().addOrReplaceChild("right_arm", CubeListBuilder.create(), PartPose.ZERO);
        mesh.getRoot().getChild("right_arm").addOrReplaceChild(
                "artifact",
                clb,
                PartPose.offset(5, 22, 0)
        );

        return mesh;
    }

    public static MeshDefinition createThimble() {
        CubeListBuilder thimble = CubeListBuilder.create();
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();
        PartDefinition bb_main = partdefinition.addOrReplaceChild("bb_main", thimble.texOffs(0, 0).addBox(-5.0F, -13.0F, 1.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.25F)), PartPose.offset(0.0F, 24.0F, 0.0F));
        MeshDefinition mesh = create(thimble);
        return mesh;
    }
}
