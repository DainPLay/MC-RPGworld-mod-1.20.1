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
import net.minecraft.resources.ResourceLocation;

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

        poseStack.pushPose();

        // Получаем длину веревки
        float ropeLength = entity.getRopeLength() + 0.6F;

        // Получаем плавный угол качания с интерполяцией
        float swingAngle = entity.getRenderSwingAngle(partialTicks);

        // Используем yaw тела пассажира (точка 1)
        float yaw = entity.getPassengerBodyYaw(partialTicks);

        // Применяем вращение качелей
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - yaw));

        // Устанавливаем точку подвеса
        poseStack.translate(0.0D, ropeLength, 0.13);

        // Применяем качание
        poseStack.mulPose(Axis.XP.rotationDegrees(swingAngle));

        // Возвращаем модель в исходное положение
        poseStack.translate(0.0D, -ropeLength, -0.13);

        // Поворачиваем модель в зависимости от угла качания (точка 3)
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

        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(TireSwingEntity entity) {
        return TEXTURE;
    }
}