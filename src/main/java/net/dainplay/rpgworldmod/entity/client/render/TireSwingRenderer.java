package net.dainplay.rpgworldmod.entity.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.dainplay.rpgworldmod.RPGworldMod;
import net.dainplay.rpgworldmod.entity.client.model.TireSwingModel;
import net.dainplay.rpgworldmod.entity.custom.TireSwingEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

public class TireSwingRenderer extends EntityRenderer<TireSwingEntity> {
    private final TireSwingModel<TireSwingEntity> model;
    private static final ResourceLocation TEXTURE = new ResourceLocation(RPGworldMod.MOD_ID, "textures/entity/tire_swing/tire_swing.png");

    public TireSwingRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new TireSwingModel<>(context.bakeLayer(TireSwingModel.LAYER_LOCATION));
        this.shadowRadius = 0.5F;
    }

    @Override
    public void render(TireSwingEntity entity, float entityYaw, float partialTicks,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {

        // Рендерим качели
        renderSwing(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);

        // Рендерим поводок, если есть привязка
        renderLeash(entity, partialTicks, poseStack, buffer);

        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    private void renderSwing(TireSwingEntity entity, float entityYaw, float partialTicks,
                             PoseStack poseStack, MultiBufferSource buffer, int packedLight) {

        poseStack.pushPose();

        // Получаем длину веревки из данных сущности
        float ropeLength = entity.getRopeLength() + 0.6F;

        // Получаем плавный угол качания с интерполяцией
        float swingAngle = entity.getRenderSwingAngle(partialTicks);

        // Используем yaw тела пассажира
        float yaw = entity.getPassengerBodyYaw(partialTicks);

        // Применяем вращение качелей
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - yaw));

        // Устанавливаем точку подвеса
        poseStack.translate(0.0D, ropeLength, 0.13);

        // Применяем качание
        poseStack.mulPose(Axis.XP.rotationDegrees(swingAngle));

        // Возвращаем модель в исходное положение
        poseStack.translate(0.0D, -ropeLength, -0.13);

        // Поворачиваем модель в зависимости от угла качания
        if (entity.isOccupied()) {
            float modelRotation = entity.getModelRotationAngle(swingAngle);

            // Временно смещаемся к центру модели для поворота
            poseStack.translate(0.0D, 0.5, 0.0D);
            poseStack.mulPose(Axis.XP.rotationDegrees(modelRotation));
            poseStack.translate(0.0D, -0.5, 0.0D);
        }

        // Центрируем модель
        poseStack.translate(0.0D, 1.5D, 0.0D);

        // Переворачиваем модель
        poseStack.scale(-1.0F, -1.0F, 1.0F);

        // Настраиваем анимацию модели
        this.model.setupAnim(entity, 0, 0, entity.tickCount + partialTicks, 0, 0);

        // Получаем VertexConsumer для отрисовки
        VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.entityCutoutNoCull(this.getTextureLocation(entity)));

        // Рендерим модель
        this.model.renderToBuffer(poseStack, vertexConsumer, packedLight,
                OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);

        poseStack.popPose();
    }

    private void renderLeash(TireSwingEntity entity, float partialTicks,
                             PoseStack poseStack, MultiBufferSource buffer) {

        byte leashType = entity.getLeashType();

        if (leashType == TireSwingEntity.LEASH_TYPE_NONE) {
            return;
        }

        Vec3 leashHolderPosition = null;

        // Получаем позицию, к которой привязаны качели
        if (leashType == TireSwingEntity.LEASH_TYPE_PLAYER) {
            Entity leashHolder = entity.getLeashHolder();
            if (leashHolder != null) {
                leashHolderPosition = leashHolder.getRopeHoldPosition(partialTicks);
            }
        } else if (leashType == TireSwingEntity.LEASH_TYPE_FENCE) {
            BlockPos fencePos = entity.getFencePos();
            if (fencePos != null) {
                // Позиция на заборе (середина верхней части)
                leashHolderPosition = new Vec3(
                        fencePos.getX() + 0.5,
                        fencePos.getY() + 1.0,
                        fencePos.getZ() + 0.5
                );
            }
        }

        if (leashHolderPosition == null) {
            return;
        }

        // Позиция качелей, откуда идет поводок
        Vec3 swingLeashPosition = entity.getRopeHoldPosition(partialTicks);

        // Рендерим поводок
        renderCustomLeash(entity, partialTicks, poseStack, buffer, leashHolderPosition);
    }

    private void renderCustomLeash(TireSwingEntity entity, float partialTicks, PoseStack poseStack,
                                   MultiBufferSource buffer, Vec3 leashHolderPosition) {

        poseStack.pushPose();

        double d0 = (double)(Mth.lerp(partialTicks, entity.yRotO, entity.getYRot()) * ((float)Math.PI / 180F)) + (Math.PI / 2D);
        Vec3 vec31 = entity.getLeashOffset(partialTicks);
        double d1 = Math.cos(d0) * vec31.z + Math.sin(d0) * vec31.x;
        double d2 = Math.sin(d0) * vec31.z - Math.cos(d0) * vec31.x;
        double d3 = Mth.lerp((double)partialTicks, entity.xo, entity.getX()) + d1;
        double d4 = Mth.lerp((double)partialTicks, entity.yo, entity.getY()) + vec31.y;
        double d5 = Mth.lerp((double)partialTicks, entity.zo, entity.getZ()) + d2;

        poseStack.translate(d1, vec31.y, d2);

        float f = (float)(leashHolderPosition.x - d3);
        float f1 = (float)(leashHolderPosition.y - d4);
        float f2 = (float)(leashHolderPosition.z - d5);
        float f3 = 0.025F;

        VertexConsumer vertexconsumer = buffer.getBuffer(RenderType.leash());
        Matrix4f matrix4f = poseStack.last().pose();
        float f4 = Mth.invSqrt(f * f + f2 * f2) * 0.025F / 2.0F;
        float f5 = f2 * f4;
        float f6 = f * f4;

        BlockPos blockpos = BlockPos.containing(entity.getEyePosition(partialTicks));
        BlockPos blockpos1 = BlockPos.containing(leashHolderPosition);
        int i = this.getBlockLightLevel(entity, blockpos);
        int j = entity.level().getBrightness(net.minecraft.world.level.LightLayer.BLOCK, blockpos1);
        int k = entity.level().getBrightness(net.minecraft.world.level.LightLayer.SKY, blockpos);
        int l = entity.level().getBrightness(net.minecraft.world.level.LightLayer.SKY, blockpos1);

        for(int i1 = 0; i1 <= 24; ++i1) {
            addVertexPair(vertexconsumer, matrix4f, f, f1, f2, i, j, k, l, 0.025F, 0.025F, f5, f6, i1, false);
        }

        for(int j1 = 24; j1 >= 0; --j1) {
            addVertexPair(vertexconsumer, matrix4f, f, f1, f2, i, j, k, l, 0.025F, 0.0F, f5, f6, j1, true);
        }

        poseStack.popPose();
    }

    private static void addVertexPair(VertexConsumer consumer, Matrix4f matrix, float dx, float dy, float dz,
                                      int entityLight, int holderLight, int entitySkyLight, int holderSkyLight,
                                      float thickness, float offset, float normalX, float normalZ,
                                      int segment, boolean reverse) {

        float f = (float)segment / 24.0F;
        int i = (int)Mth.lerp(f, (float)entityLight, (float)holderLight);
        int j = (int)Mth.lerp(f, (float)entitySkyLight, (float)holderSkyLight);
        int k = net.minecraft.client.renderer.LightTexture.pack(i, j);
        float f1 = segment % 2 == (reverse ? 1 : 0) ? 0.7F : 1.0F;
        float f2 = 0.5F * f1;
        float f3 = 0.4F * f1;
        float f4 = 0.3F * f1;
        float f5 = dx * f;
        float f6 = dy > 0.0F ? dy * f * f : dy - dy * (1.0F - f) * (1.0F - f);
        float f7 = dz * f;

        consumer.vertex(matrix, f5 - normalX, f6 + offset, f7 + normalZ)
                .color(f2, f3, f4, 1.0F)
                .uv2(k)
                .endVertex();
        consumer.vertex(matrix, f5 + normalX, f6 + thickness - offset, f7 - normalZ)
                .color(f2, f3, f4, 1.0F)
                .uv2(k)
                .endVertex();
    }

    @Override
    public ResourceLocation getTextureLocation(TireSwingEntity entity) {
        return TEXTURE;
    }
}