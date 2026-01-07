package net.dainplay.rpgworldmod.entity.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.dainplay.rpgworldmod.entity.custom.Razorleaf;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.Entity;

public abstract class ExtinguishedHeartLayer<T extends Entity, M extends EntityModel<T>> extends RenderLayer<T, M> {
        public ExtinguishedHeartLayer(RenderLayerParent<T, M> pRenderer) {
            super(pRenderer);
        }

        public void render(PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, T pLivingEntity, float pLimbSwing, float pLimbSwingAmount, float pPartialTicks, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch) {
            VertexConsumer vertexconsumer = pBuffer.getBuffer(this.renderType());
            if(pLivingEntity instanceof Razorleaf razorleaf && razorleaf.getState() == Razorleaf.State.EXTINGUISHED) this.getParentModel().renderToBuffer(pPoseStack, vertexconsumer, 15728640, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
        }

        public abstract RenderType renderType();
}
