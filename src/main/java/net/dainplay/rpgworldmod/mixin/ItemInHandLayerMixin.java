package net.dainplay.rpgworldmod.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.dainplay.rpgworldmod.item.custom.ManaCostItem;
import net.dainplay.rpgworldmod.item.custom.OrbitingItem;
import net.minecraft.client.model.ArmedModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandLayer.class)
public class ItemInHandLayerMixin {
    ItemInHandLayer iihl = (ItemInHandLayer) (Object) this;

    @Inject(method = "renderArmWithItem", at = @At(value = "HEAD"), cancellable = true)
    private void renderOrbitingItemTexture(
        LivingEntity entity,
        ItemStack itemStack,
        ItemDisplayContext displayContext,
        net.minecraft.world.entity.HumanoidArm arm,
        PoseStack poseStack,
        MultiBufferSource bufferSource,
        int packedLight,
        CallbackInfo ci
    ) {
        if (itemStack.getItem() instanceof OrbitingItem orbitingItem) {
            poseStack.pushPose();

            ((ArmedModel)iihl.getParentModel()).translateToHand(arm, poseStack);
            boolean flag = arm == HumanoidArm.LEFT;
            poseStack.translate((float)(flag ? orbitingItem.getX()*-1 : orbitingItem.getX()), orbitingItem.getY(), orbitingItem.getZ());

            // Получаем позицию руки из текущей матрицы
            Matrix4f originalMatrix = poseStack.last().pose();
            Vector3f handPosition = originalMatrix.getTranslation(new Vector3f());

            // Сбрасываем матрицу (убираем вращение руки)
            poseStack.setIdentity();

            // Восстанавливаем позицию руки
            poseStack.translate(handPosition.x(), handPosition.y(), handPosition.z()+orbitingItem.getZOffset());

            // Получаем вращение камеры

            // Получаем информацию о текстуре из OrbitingItem
            String textureString = orbitingItem.getTexture();
            int color = orbitingItem.getColor();
            float size = 0.25F; // Размер текстуры

            VertexConsumer vertexConsumer;
            Matrix4f matrix = poseStack.last().pose();

            int manacost;
            if (orbitingItem instanceof ManaCostItem spell) manacost = spell.getManaCost();
			else {
				manacost = 0;
			}
			boolean hasEnoughMana = true;
            if (orbitingItem instanceof ManaCostItem spell) {
                if (itemStack.hasTag() && itemStack.getTag().contains("notEnoughMana")) hasEnoughMana = false;
            }

            if (hasEnoughMana || (entity instanceof Player player && player.getAbilities().instabuild)) {
                if (textureString != null && !textureString.isEmpty()) {
                    // Используем анимированную текстуру
                    int animationSpeed = orbitingItem.getAnimationSpeed();
                    int animationLength = orbitingItem.getAnimationLength();
                    String frame = textureString + "_" + (entity.tickCount / animationSpeed % animationLength);
                    vertexConsumer = bufferSource.getBuffer(RenderType.itemEntityTranslucentCull(
                            new net.minecraft.resources.ResourceLocation(net.dainplay.rpgworldmod.RPGworldMod.MOD_ID, frame + ".png")
                    ));

                    // Рендерим текстурированный квадрат
                    // Нижний левый
                    vertexConsumer.vertex(matrix, -size, -size, 0.0F)
                            .color(1.0F, 1.0F, 1.0F, 1F)
                            .uv(0.0F, 1.0F)
                            .overlayCoords(OverlayTexture.NO_OVERLAY)
                            .uv2(15728880)
                            .normal(0.0F, 0.0F, 1.0F)
                            .endVertex();

                    // Нижний правый
                    vertexConsumer.vertex(matrix, size, -size, 0.0F)
                            .color(1.0F, 1.0F, 1.0F, 1F)
                            .uv(1.0F, 1.0F)
                            .overlayCoords(OverlayTexture.NO_OVERLAY)
                            .uv2(15728880)
                            .normal(0.0F, 0.0F, 1.0F)
                            .endVertex();

                    // Верхний правый
                    vertexConsumer.vertex(matrix, size, size, 0.0F)
                            .color(1.0F, 1.0F, 1.0F, 1F)
                            .uv(1.0F, 0.0F)
                            .overlayCoords(OverlayTexture.NO_OVERLAY)
                            .uv2(15728880)
                            .normal(0.0F, 0.0F, 1.0F)
                            .endVertex();

                    // Верхний левый
                    vertexConsumer.vertex(matrix, -size, size, 0.0F)
                            .color(1.0F, 1.0F, 1.0F, 1F)
                            .uv(0.0F, 0.0F)
                            .overlayCoords(OverlayTexture.NO_OVERLAY)
                            .uv2(15728880)
                            .normal(0.0F, 0.0F, 1.0F)
                            .endVertex();
                } else {
                    // Рендерим цветной квадрат
                    vertexConsumer = bufferSource.getBuffer(RenderType.lightning());
                    int alpha = 150;
                    int red = (color >> 16) & 0xFF;
                    int green = (color >> 8) & 0xFF;
                    int blue = color & 0xFF;

                    // Второй треугольник квадрата
                    vertexConsumer.vertex(matrix, size, size, 0.0F)
                            .color(red, green, blue, alpha)
                            .endVertex();
                    vertexConsumer.vertex(matrix, -size, size, 0.0F)
                            .color(red, green, blue, alpha)
                            .endVertex();
                    vertexConsumer.vertex(matrix, -size, -size, 0.0F)
                            .color(red, green, blue, alpha)
                            .endVertex();
                    vertexConsumer.vertex(matrix, size, -size, 0.0F)
                            .color(red, green, blue, alpha)
                            .endVertex();
                }
            }

            // Восстанавливаем матрицу
            poseStack.popPose();
            ci.cancel();
        }
    }
}