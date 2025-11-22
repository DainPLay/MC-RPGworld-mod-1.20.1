package net.dainplay.rpgworldmod.entity.client.model;

import com.google.common.collect.ImmutableList;
import net.dainplay.rpgworldmod.entity.client.render.CurioLayers;
import net.dainplay.rpgworldmod.entity.client.render.CurioRenderers;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;

import java.util.function.Function;

public class TurretModel extends HumanoidModel<LivingEntity> {



    public TurretModel(ModelPart part, Function<ResourceLocation, RenderType> renderType) {
        super(part, renderType);
    }


    @Override
    protected Iterable<ModelPart> headParts() {
        return ImmutableList.of();
    }

    @Override
    protected Iterable<ModelPart> bodyParts() {
        return ImmutableList.of(body);
    }

    public static TurretModel createTurretModel() {
        return new TurretModel(CurioRenderers.bakeLayer(CurioLayers.TURRET), RenderType::entityTranslucent) {
            //private final ModelPart cloud = charm.getChild("cloud");

            @Override
            public void setupAnim(LivingEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
                super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
                //cloud.yRot = (ageInTicks) / 50;
                //cloud.y = Mth.cos((ageInTicks) / 30) / 2;
            }
        };
    }


    private static MeshDefinition createBelt(CubeListBuilder charm) {
        MeshDefinition mesh = createMesh(CubeDeformation.NONE, 0);

        mesh.getRoot().addOrReplaceChild(
                "body",
                charm,
                PartPose.ZERO
        );

        return mesh;
    }

    public static MeshDefinition createCloudInABottle() {
        CubeListBuilder turret = CubeListBuilder.create();
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();
        PartDefinition bb_main = partdefinition.addOrReplaceChild("bb_main", turret.texOffs(0, 30).addBox(-4.0F, -6.0F, 2.15F, 8.0F, 6.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-5.0F, -1.0F, -2.85F, 10.0F, 8.0F, 6.0F, new CubeDeformation(-0.5F))
                .texOffs(0, 14).addBox(-4.0F, 0.0F, 2.15F, 8.0F, 11.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(26, 14).addBox(-3.5F, -9.0F, 5.15F, 7.0F, 4.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(26, 26).addBox(-3.5F, -9.0F, 5.15F, 7.0F, 4.0F, 8.0F, new CubeDeformation(0.25F))
                .texOffs(26, 38).addBox(-3.5F, -9.0F, 16.15F, 7.0F, 4.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(40, 38).addBox(-2.5F, -8.0F, 13.15F, 2.0F, 2.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(0, 41).addBox(0.5F, -8.0F, 13.15F, 2.0F, 2.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(0, 46).addBox(3.5F, -8.0F, 6.15F, 1.0F, 9.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(12, 46).addBox(3.5F, -8.0F, 6.15F, 1.0F, 9.0F, 5.0F, new CubeDeformation(0.25F))
                .texOffs(0, 46).mirror().addBox(-4.5F, -8.0F, 6.15F, 1.0F, 9.0F, 5.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(12, 46).mirror().addBox(-4.5F, -8.0F, 6.15F, 1.0F, 9.0F, 5.0F, new CubeDeformation(0.25F)).mirror(false)
                .texOffs(10, 41).addBox(-0.5F, -1.5F, 4.65F, 1.0F, 2.0F, 2.0F, new CubeDeformation(0.25F))
                .texOffs(46, 39).addBox(0.0F, -5.0F, 2.15F, 0.0F, 16.0F, 9.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 24.0F, 0.0F));
        MeshDefinition mesh = createBelt(turret);
        return mesh;
    }
}
