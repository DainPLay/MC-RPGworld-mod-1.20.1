package net.dainplay.rpgworldmod.entity.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.dainplay.rpgworldmod.RPGworldMod;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.client.ICurioRenderer;

public class CurioRenderer implements ICurioRenderer {

    private final ResourceLocation texture;
    private final ResourceLocation crouching_texture;
    private final HumanoidModel<LivingEntity> model;

    public CurioRenderer(String texturePath, HumanoidModel<LivingEntity> model) {
        this(new ResourceLocation(RPGworldMod.MOD_ID, String.format("textures/entity/curio/%s.png", texturePath)), new ResourceLocation(RPGworldMod.MOD_ID, String.format("textures/entity/curio/%s_crouch.png", texturePath)), model);
    }

    public CurioRenderer(ResourceLocation texture, ResourceLocation crouching_texture, HumanoidModel<LivingEntity> model) {
        this.texture = texture;
        if(texture.getPath().equals("textures/entity/curio/turret/portable_turret.png")) this.crouching_texture = crouching_texture;
        else this.crouching_texture = texture;
        this.model = model;
    }

    protected ResourceLocation getTexture(boolean isCrouching) {
        if (isCrouching) return crouching_texture; else return texture;
    }

    protected HumanoidModel<LivingEntity> getModel() {
        return model;
    }

    @Override
    public <T extends LivingEntity, M extends EntityModel<T>> void render(
            ItemStack stack,
            SlotContext slotContext,
            PoseStack poseStack,
            RenderLayerParent<T, M> renderLayerParent,
            MultiBufferSource multiBufferSource,
            int light,
            float limbSwing,
            float limbSwingAmount,
            float partialTicks,
            float ageInTicks,
            float netHeadYaw,
            float headPitch
    ) {
        HumanoidModel<LivingEntity> model = getModel();

        model.setupAnim(slotContext.entity(), limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        model.prepareMobModel(slotContext.entity(), limbSwing, limbSwingAmount, partialTicks);
        ICurioRenderer.followBodyRotations(slotContext.entity(), model);
        render(poseStack, multiBufferSource, light, stack.hasFoil(), slotContext.entity().isCrouching());
    }

    protected void render(PoseStack matrixStack, MultiBufferSource buffer, int light, boolean hasFoil, boolean isCrouching) {
        RenderType renderType = model.renderType(getTexture(isCrouching));
        VertexConsumer vertexBuilder = ItemRenderer.getFoilBuffer(buffer, renderType, false, hasFoil);
        model.renderToBuffer(matrixStack, vertexBuilder, light, OverlayTexture.NO_OVERLAY, 1, 1, 1, 1);
    }
}
