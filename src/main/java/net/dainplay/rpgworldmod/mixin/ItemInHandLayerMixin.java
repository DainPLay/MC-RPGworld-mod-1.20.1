package net.dainplay.rpgworldmod.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.dainplay.rpgworldmod.RPGworldMod;
import net.dainplay.rpgworldmod.item.custom.ManaCostItem;
import net.dainplay.rpgworldmod.item.custom.OrbitingItem;
import net.dainplay.rpgworldmod.render.ModRenderTypes;
import net.minecraft.client.model.ArmedModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
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
        if (itemStack.getItem() instanceof OrbitingItem orbitingItem && orbitingItem.shouldOrbit(itemStack, entity)) {
            poseStack.pushPose();

            ((ArmedModel)iihl.getParentModel()).translateToHand(arm, poseStack);
            boolean flag = arm == HumanoidArm.LEFT;
            poseStack.translate((float)(flag ? orbitingItem.getX(itemStack, entity)*-1 : orbitingItem.getX(itemStack, entity)), orbitingItem.getY(itemStack, entity), orbitingItem.getZ(itemStack, entity));

            Matrix4f originalMatrix = poseStack.last().pose();
            Vector3f handPosition = originalMatrix.getTranslation(new Vector3f());

            poseStack.setIdentity();

            poseStack.translate(handPosition.x(), handPosition.y(), handPosition.z()+orbitingItem.getZOffset(itemStack, entity));

            String textureString = orbitingItem.getTexture(itemStack, entity);
            int color = orbitingItem.getColor(itemStack, entity);
            float size = 0.25F;

            VertexConsumer vertexConsumer;
            Matrix4f matrix = poseStack.last().pose();

            boolean hasEnoughMana = true;
            if (orbitingItem instanceof ManaCostItem) {
                if (itemStack.hasTag() && itemStack.getTag().contains("notEnoughMana")) hasEnoughMana = false;
            }

            if (hasEnoughMana) {
                if (textureString != null && !textureString.isEmpty()) {
                    int animationSpeed = orbitingItem.getAnimationSpeed(itemStack, entity);
                    int animationLength = orbitingItem.getAnimationLength(itemStack, entity);

                    int currentFrame = (entity.tickCount / animationSpeed) % animationLength;

                    float frameHeight = 1.0F / animationLength;
                    float vMin = currentFrame * frameHeight;
                    float vMax = vMin + frameHeight;

                    vertexConsumer = bufferSource.getBuffer(ModRenderTypes.SPELL_EFFECT.apply(new ResourceLocation(RPGworldMod.MOD_ID,textureString + ".png")));

                    vertexConsumer.vertex(matrix, -size, -size, 0.0F)
                            .color(1.0F, 1.0F, 1.0F, 1F)
                            .uv(0.0F, vMax) // V теперь зависит от кадра
                            .overlayCoords(OverlayTexture.NO_OVERLAY)
                            .uv2(15728880)
                            .normal(0.0F, 0.0F, 1.0F)
                            .endVertex();

                    vertexConsumer.vertex(matrix, size, -size, 0.0F)
                            .color(1.0F, 1.0F, 1.0F, 1F)
                            .uv(1.0F, vMax) // V теперь зависит от кадра
                            .overlayCoords(OverlayTexture.NO_OVERLAY)
                            .uv2(15728880)
                            .normal(0.0F, 0.0F, 1.0F)
                            .endVertex();

                    vertexConsumer.vertex(matrix, size, size, 0.0F)
                            .color(1.0F, 1.0F, 1.0F, 1F)
                            .uv(1.0F, vMin) // V теперь зависит от кадра
                            .overlayCoords(OverlayTexture.NO_OVERLAY)
                            .uv2(15728880)
                            .normal(0.0F, 0.0F, 1.0F)
                            .endVertex();

                    vertexConsumer.vertex(matrix, -size, size, 0.0F)
                            .color(1.0F, 1.0F, 1.0F, 1F)
                            .uv(0.0F, vMin) // V теперь зависит от кадра
                            .overlayCoords(OverlayTexture.NO_OVERLAY)
                            .uv2(15728880)
                            .normal(0.0F, 0.0F, 1.0F)
                            .endVertex();
                } else {
                    vertexConsumer = bufferSource.getBuffer(RenderType.lightning());
                    int alpha = 150;
                    int red = (color >> 16) & 0xFF;
                    int green = (color >> 8) & 0xFF;
                    int blue = color & 0xFF;

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

            poseStack.popPose();
            ci.cancel();
        }
    }
}