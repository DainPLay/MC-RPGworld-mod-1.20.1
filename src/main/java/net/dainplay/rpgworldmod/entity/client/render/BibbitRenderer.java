package net.dainplay.rpgworldmod.entity.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.dainplay.rpgworldmod.RPGworldMod;
import net.dainplay.rpgworldmod.entity.client.model.BibbitArmorModel;
import net.dainplay.rpgworldmod.entity.client.model.BibbitEyesLayer;
import net.dainplay.rpgworldmod.entity.client.model.BibbitModel;
import net.dainplay.rpgworldmod.entity.custom.Bibbit;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class BibbitRenderer extends MobRenderer<Bibbit, BibbitModel<Bibbit>> {
    public static final ResourceLocation TEXTURE = new ResourceLocation(
            RPGworldMod.MOD_ID, "textures/entity/bibbit/bibbit.png");
    public static final ResourceLocation TEXTURE_SITTING = new ResourceLocation(
            RPGworldMod.MOD_ID, "textures/entity/bibbit/bibbit_sitting.png");

    public BibbitRenderer(EntityRendererProvider.Context context) {
        super(context, new BibbitModel<>(context.bakeLayer(BibbitModel.LAYER_LOCATION)), 0.4f);

        this.addLayer(new BibbitEyesLayer<>(this));

        // Создаем модель брони и добавляем слой
        BibbitArmorModel armorModel = new BibbitArmorModel(
                context.bakeLayer(BibbitArmorModel.LAYER_LOCATION));

        this.addLayer(new BibbitArmorLayer(this, armorModel, context.getModelManager()));
    }

    @Override
    public void render(Bibbit entity, float entityYaw, float partialTicks,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        // Вычисляем анимацию для синхронизации с моделью брони
        float walkSpeed = 0.0F;
        float walkPosition = 0.0F;

        if (entity.isAlive()) {
            walkSpeed = entity.walkAnimation.speed(partialTicks);
            walkPosition = entity.walkAnimation.position(partialTicks);

            if (entity.isBaby()) {
                walkPosition *= 3.0F;
            }

            if (walkSpeed > 1.0F) {
                walkSpeed = 1.0F;
            }
        }

        // Применяем анимацию к модели (через super.render)
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    @Override
    protected void setupRotations(Bibbit entity, PoseStack poseStack,
                                  float ageInTicks, float rotationYaw, float partialTicks) {
        super.setupRotations(entity, poseStack, ageInTicks, rotationYaw, partialTicks);

        if (entity.isPouncing()) {
            float f = -Mth.lerp(partialTicks, entity.xRotO, entity.getXRot());
            poseStack.mulPose(Axis.XP.rotationDegrees(f));
        }
    }

    @Override
    public ResourceLocation getTextureLocation(Bibbit entity) {
        return entity.isCrouching() ? TEXTURE_SITTING : TEXTURE;
    }
}