package net.dainplay.rpgworldmod.entity.client.model;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

import java.util.function.Function;

public class CharmModel extends HumanoidModel<LivingEntity> {

    public CharmModel(ModelPart part, Function<ResourceLocation, RenderType> renderType) {
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

    public static MeshDefinition createCharm() {
        MeshDefinition mesh = createMesh(new CubeDeformation(1.1F), 0);

        mesh.getRoot().addOrReplaceChild("body",CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-4.0F, -0.5F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(1.1F)),
                        PartPose.ZERO
        );

        return mesh;
    }
}
