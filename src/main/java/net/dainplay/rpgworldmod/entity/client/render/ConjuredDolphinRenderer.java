package net.dainplay.rpgworldmod.entity.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.dainplay.rpgworldmod.entity.custom.ConjuredDolphin;
import net.dainplay.rpgworldmod.render.ModRenderTypes;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.DolphinModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.DolphinRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.animal.Dolphin;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

public class ConjuredDolphinRenderer extends DolphinRenderer {
    private static final float HALF_SQRT_3 = (float) (Math.sqrt(3.0D) / 2.0D);

    public ConjuredDolphinRenderer(EntityRendererProvider.Context context) {
        super(context);

        this.addLayer(new RenderLayer<Dolphin, DolphinModel<Dolphin>>(this) {
            @Override
            public void render(PoseStack poseStack,
                               MultiBufferSource buffer,
                               int packedLight,
                               Dolphin entity,
                               float limbSwing,
                               float limbSwingAmount,
                               float partialTick,
                               float ageInTicks,
                               float netHeadYaw,
                               float headPitch) {
                if (entity instanceof ConjuredDolphin) {
                    VertexConsumer vertexConsumer = buffer.getBuffer(ModRenderTypes.SUMMONED_GLINT_ENTITY);
                    this.getParentModel().renderToBuffer(poseStack,
                            vertexConsumer,
                            packedLight,
                            OverlayTexture.NO_OVERLAY,
                            1.0F, 1.0F, 1.0F, 1.0F);
                }
            }
        });
        this.addLayer(new RenderLayer<Dolphin, DolphinModel<Dolphin>>(this) {
            @Override
            public void render(PoseStack poseStack,
                               MultiBufferSource buffer,
                               int packedLight,
                               Dolphin entity,
                               float limbSwing,
                               float limbSwingAmount,
                               float partialTick,
                               float ageInTicks,
                               float netHeadYaw,
                               float headPitch) {
                if (entity instanceof ConjuredDolphin) {
                    VertexConsumer vertexConsumer = buffer.getBuffer(ModRenderTypes.SUMMONED_GLINT_ENTITY);
                    this.getParentModel().renderToBuffer(poseStack,
                            vertexConsumer,
                            packedLight,
                            OverlayTexture.NO_OVERLAY,
                            1.0F, 1.0F, 1.0F, 1.0F);
                }
            }
        });
    }

    @Override
    public void render(Dolphin entity, float entityYaw, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight) {
        super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);

        if (entity instanceof ConjuredDolphin conjuredDolphin) {
            renderRotatingBeams(conjuredDolphin, partialTick, poseStack, buffer);
        }
    }

    private void renderRotatingBeams(ConjuredDolphin dolphin, float partialTick, PoseStack poseStack, MultiBufferSource buffer) {
        VertexConsumer vertexconsumer = buffer.getBuffer(RenderType.lightning());
        poseStack.pushPose();

        double heightOffset = dolphin.getBbHeight() * 0.5;
        poseStack.translate(0, heightOffset, 0);

        Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
        Vec3 cameraPos = camera.getPosition();
        Vec3 beamPos = dolphin.position().add(0, heightOffset, 0);
        Vec3 toCamera = cameraPos.subtract(beamPos).normalize();

        double horizontalAngleToCamera = Math.toDegrees(Math.atan2(toCamera.x, toCamera.z));
        double verticalAngleToCamera = Math.toDegrees(Math.asin(toCamera.y));

        float beamProgress = dolphin.getBeamProgress(partialTick);
        float rotationAngle = dolphin.getBeamRotationAngle(partialTick);

        int beamCount = 12;
        for (int i = 0; i < beamCount; i++) {
            poseStack.pushPose();

            poseStack.mulPose(Axis.YP.rotationDegrees((float) horizontalAngleToCamera));
            poseStack.mulPose(Axis.XP.rotationDegrees((float) -verticalAngleToCamera));

            float angle = rotationAngle + (360f / beamCount) * i;
            poseStack.mulPose(Axis.ZP.rotationDegrees(angle));

            float beamLength = 2.0F * beamProgress;
            float beamWidth = 0.6F * beamProgress;

            Matrix4f matrix4f = poseStack.last().pose();
            int alpha = (int) (200 * beamProgress);

            vertex01(vertexconsumer, matrix4f, alpha, 0, 0, 255);
            vertex3(vertexconsumer, matrix4f, beamLength, beamWidth, 0, 0, 255);
            vertex4(vertexconsumer, matrix4f, beamLength, beamWidth, 0, 0, 255);
            vertex01(vertexconsumer, matrix4f, alpha, 0, 0, 255);
            vertex4(vertexconsumer, matrix4f, beamLength, beamWidth, 0, 0, 255);
            vertex2(vertexconsumer, matrix4f, beamLength, beamWidth, 0, 0, 255);

            poseStack.popPose();
        }
        poseStack.popPose();
    }

    private static void vertex01(VertexConsumer pConsumer, Matrix4f pMatrix, int pAlpha, int r, int g, int b) {
        pConsumer.vertex(pMatrix, 0.0F, 0.0F, 0.0F).color(r, g, b, pAlpha).endVertex();
    }

    private static void vertex2(VertexConsumer pConsumer, Matrix4f pMatrix, float length, float width, int r, int g, int b) {
        pConsumer.vertex(pMatrix, -HALF_SQRT_3 * width, length, -0.5F * width).color(r, g, b, 0).endVertex();
    }

    private static void vertex3(VertexConsumer pConsumer, Matrix4f pMatrix, float length, float width, int r, int g, int b) {
        pConsumer.vertex(pMatrix, HALF_SQRT_3 * width, length, -0.5F * width).color(r, g, b, 0).endVertex();
    }

    private static void vertex4(VertexConsumer pConsumer, Matrix4f pMatrix, float length, float width, int r, int g, int b) {
        pConsumer.vertex(pMatrix, 0.0F, length, 1.0F * width).color(r, g, b, 0).endVertex();
    }
}