package net.dainplay.rpgworldmod.entity.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.dainplay.rpgworldmod.RPGworldMod;
import net.dainplay.rpgworldmod.effect.FuelingEffect;
import net.dainplay.rpgworldmod.effect.MossiosisEffect;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;

import javax.imageio.ImageIO;
import java.io.IOException;

public class LayerMoss extends RenderLayer {

    private RenderLayerParent parent;

    public LayerMoss(RenderLayerParent parent) {
        super(parent);
        this.parent = parent;
    }

    @Override
    public void render(PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn, Entity entity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        if(entity instanceof LivingEntity && ((LivingEntity) entity).getAttribute(Attributes.MAX_HEALTH).getModifier(MossiosisEffect.MODIFIER_UUID) != null) {
            int sootn = 1;
            try {
                if(!(entity instanceof Player))
                sootn = ImageIO.read(Minecraft.getInstance().getResourceManager().getResource(getTextureLocation(entity)).get().open()).getWidth()/
                        ImageIO.read(Minecraft.getInstance().getResourceManager().getResource(getTextureLocation(entity)).get().open()).getHeight();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            VertexConsumer ivertexbuilder = bufferIn.getBuffer(RenderType.entityCutout(new ResourceLocation(RPGworldMod.MOD_ID, "textures/entity/mossiosis_overlay.png")));
            if(sootn != 1)  ivertexbuilder = bufferIn.getBuffer(RenderType.entityCutout(new ResourceLocation(RPGworldMod.MOD_ID, "textures/entity/mossiosis_overlay_half.png")));
            float alpha = 1F;
            matrixStackIn.pushPose();
            this.getParentModel().renderToBuffer(matrixStackIn, ivertexbuilder, packedLightIn, LivingEntityRenderer.getOverlayCoords((LivingEntity)entity, 0), 1, 1, 1, alpha);
            matrixStackIn.popPose();
        }
    }
}
