package net.dainplay.rpgworldmod.entity.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.dainplay.rpgworldmod.RPGworldMod;
import net.dainplay.rpgworldmod.entity.client.model.FireflanternModel;
import net.dainplay.rpgworldmod.entity.custom.Fireflantern;
import net.dainplay.rpgworldmod.entity.custom.Sheentrout;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.GlowSquid;
import net.minecraft.world.entity.animal.allay.Allay;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Quaternionf;


public class FireflanternRenderer extends MobRenderer<Fireflantern, FireflanternModel<Fireflantern>> {
    public static final ResourceLocation TEXTURE1 = new ResourceLocation(RPGworldMod.MOD_ID, "textures/entity/fireflantern/fireflantern_1.png");
    public static final ResourceLocation TEXTURE2 = new ResourceLocation(RPGworldMod.MOD_ID, "textures/entity/fireflantern/fireflantern_2.png");
    public static final ResourceLocation TEXTURE3 = new ResourceLocation(RPGworldMod.MOD_ID, "textures/entity/fireflantern/fireflantern_3.png");
    public static final ResourceLocation TEXTURE4 = new ResourceLocation(RPGworldMod.MOD_ID, "textures/entity/fireflantern/fireflantern_4.png");
    public static final ResourceLocation TEXTURE5 = new ResourceLocation(RPGworldMod.MOD_ID, "textures/entity/fireflantern/fireflantern_5.png");
    public static final ResourceLocation TEXTURE6 = new ResourceLocation(RPGworldMod.MOD_ID, "textures/entity/fireflantern/fireflantern_6.png");
    public static final ResourceLocation TEXTURE7 = new ResourceLocation(RPGworldMod.MOD_ID, "textures/entity/fireflantern/fireflantern_7.png");
    public static final ResourceLocation TEXTURE8 = new ResourceLocation(RPGworldMod.MOD_ID, "textures/entity/fireflantern/fireflantern_8.png");
    private static final float HALF_SQRT_3 = (float)(Math.sqrt(3.0D) / 2.0D);

    public FireflanternRenderer(EntityRendererProvider.Context context) {
        super(context, new FireflanternModel<>(context.bakeLayer(FireflanternModel.LAYER_LOCATION)), 0.25F);
        //this.addLayer(new FireflanternFireLayer<>(this));
    }
    protected int getBlockLightLevel(Fireflantern p_234560_, BlockPos p_234561_) {
        return 15;
    }

    public ResourceLocation getTextureLocation(Fireflantern pEntity) {
        return switch (pEntity.tickCount % 8) {
            case 1 -> TEXTURE1;
            case 2 -> TEXTURE2;
            case 3 -> TEXTURE3;
            case 4 -> TEXTURE4;
            case 5 -> TEXTURE5;
            case 6 -> TEXTURE6;
            case 7 -> TEXTURE7;
            default -> TEXTURE8;
        };
    }
    private static void vertex01(VertexConsumer pConsumer, Matrix4f pMatrix, int pAlpha) {
        pConsumer.vertex(pMatrix, 0.0F, 0.0F, 0.0F).color(255, 255, 255, pAlpha).endVertex();
    }

    private static void vertex2(VertexConsumer pConsumer, Matrix4f pMatrix, float p_253704_, float p_253701_) {
        pConsumer.vertex(pMatrix, -HALF_SQRT_3 * p_253701_, p_253704_, -0.5F * p_253701_).color(255, 216, 0, 0).endVertex();
    }

    private static void vertex3(VertexConsumer pConsumer, Matrix4f pMatrix, float p_253729_, float p_254030_) {
        pConsumer.vertex(pMatrix, HALF_SQRT_3 * p_254030_, p_253729_, -0.5F * p_254030_).color(255, 216, 0, 0).endVertex();
    }

    private static void vertex4(VertexConsumer pConsumer, Matrix4f pMatrix, float p_253649_, float p_253694_) {
        pConsumer.vertex(pMatrix, 0.0F, p_253649_, 1.0F * p_253694_).color(255, 216, 0, 0).endVertex();
    }


    @Override
    public void render(Fireflantern pEntity, float pEntityYaw, float pPartialTicks, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight) {
        super.render(pEntity, pEntityYaw, pPartialTicks, pPoseStack, pBuffer, pPackedLight);
        VertexConsumer vertexconsumer2 = pBuffer.getBuffer(RenderType.lightning());
        pPoseStack.pushPose();
        pPoseStack.translate(0.0F, 0.5F, 0.0F);

        Player player = Minecraft.getInstance().player;
        if(player == null) {
            pPoseStack.popPose();
            return;
        }
        Vec3 playerPos = player.getEyePosition(pPartialTicks);
        Vec3 entityPos = pEntity.position().add(0, 0.5F, 0);
        Vec3 cameraLookDir = player.getViewVector(pPartialTicks).normalize();

        // Calculate the backward offset
        float backwardOffset = 0.5f; // Adjust this value to control the distance
        Vec3 backwardOffsetVector = cameraLookDir.scale(backwardOffset);

        // Calculate the target position for the beams' center
        Vec3 targetPos = entityPos.add(backwardOffsetVector);

        float animationProgress = (pEntity.tickCount + pPartialTicks) / 200.0F;

        int beamCount = 10;
        for (int i = 0; i < beamCount; i++) {
            pPoseStack.pushPose();

            pPoseStack.translate(targetPos.x - entityPos.x, targetPos.y - entityPos.y, targetPos.z - entityPos.z);

            // Calculate the vector from the entity to the player
            Vec3 directionToPlayer = playerPos.subtract(targetPos).normalize();
            double horizontalAngleToPlayer = Math.toDegrees(Math.atan2(directionToPlayer.x, directionToPlayer.z));
            double verticalAngleToPlayer = Math.toDegrees(Math.asin(directionToPlayer.y));

            // Calculate the angle of the vector with the X-axis in degrees

            float angle = animationProgress * 360F + (360f / beamCount) * i;
            pPoseStack.mulPose(Axis.YP.rotationDegrees((float) horizontalAngleToPlayer));
            pPoseStack.mulPose(Axis.XP.rotationDegrees((float) -verticalAngleToPlayer));
            pPoseStack.mulPose(Axis.ZP.rotationDegrees(angle));
            float f3 = 2.1F;
            float f4 = 2.0F;
            Matrix4f matrix4f = pPoseStack.last().pose();
            int j = 128;
            vertex01(vertexconsumer2, matrix4f, j);
            vertex3(vertexconsumer2, matrix4f, f3, f4);
            vertex4(vertexconsumer2, matrix4f, f3, f4);
            vertex01(vertexconsumer2, matrix4f, j);
            vertex4(vertexconsumer2, matrix4f, f3, f4);
            vertex2(vertexconsumer2, matrix4f, f3, f4);
            pPoseStack.popPose();
        }
        pPoseStack.popPose();
    }

    protected void setupRotations(Fireflantern pEntityLiving, PoseStack pMatrixStack, float pAgeInTicks, float pRotationYaw, float pPartialTicks) {
        pMatrixStack.translate(0.0D, -0.4D, 0.0D);
        super.setupRotations(pEntityLiving, pMatrixStack, pAgeInTicks, pRotationYaw, pPartialTicks);
    }
}
