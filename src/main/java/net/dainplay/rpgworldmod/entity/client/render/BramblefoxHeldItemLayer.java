package net.dainplay.rpgworldmod.entity.client.render;

        import com.mojang.blaze3d.vertex.PoseStack;
        import com.mojang.math.Axis;
import net.dainplay.rpgworldmod.entity.client.model.BramblefoxModel;
import net.dainplay.rpgworldmod.entity.custom.Bramblefox;
import net.minecraft.client.Minecraft;
        import net.minecraft.client.renderer.ItemInHandRenderer;
        import net.minecraft.client.renderer.MultiBufferSource;
        import net.minecraft.client.renderer.block.model.ItemTransforms;
        import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.entity.EquipmentSlot;
        import net.minecraft.world.item.ItemDisplayContext;
        import net.minecraft.world.item.ItemStack;
        import net.minecraftforge.api.distmarker.Dist;
        import net.minecraftforge.api.distmarker.OnlyIn;

public class BramblefoxHeldItemLayer extends RenderLayer<Bramblefox, BramblefoxModel<Bramblefox>> {
    private final ItemInHandRenderer itemInHandRenderer;
    public BramblefoxHeldItemLayer(RenderLayerParent<Bramblefox, BramblefoxModel<Bramblefox>> p_116994_, ItemInHandRenderer p_234839_) {
        super(p_116994_);
        this.itemInHandRenderer = p_234839_;
    }

    public void render(PoseStack pMatrixStack, MultiBufferSource pBuffer, int pPackedLight, Bramblefox pLivingEntity, float pLimbSwing, float pLimbSwingAmount, float pPartialTicks, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch) {
        boolean flag = pLivingEntity.isSleeping();
        boolean flag1 = pLivingEntity.isBaby();
        pMatrixStack.pushPose();
        if (flag1) {
            float f = 0.75F;
            pMatrixStack.scale(0.75F, 0.75F, 0.75F);
            pMatrixStack.translate(0.0D, 0.5D, (double)0.209375F);
        }

        pMatrixStack.translate((double)((this.getParentModel()).head.x / 16.0F), (double)((this.getParentModel()).head.y / 16.0F), (double)((this.getParentModel()).head.z / 16.0F));
        float f1 = pLivingEntity.getHeadRollAngle(pPartialTicks);
        pMatrixStack.mulPose(Axis.ZP.rotation(f1));
        pMatrixStack.mulPose(Axis.YP.rotationDegrees(pNetHeadYaw));
        pMatrixStack.mulPose(Axis.XP.rotationDegrees(pHeadPitch));
        if (pLivingEntity.isBaby()) {
            if (flag) {
                pMatrixStack.translate((double)0.4F, (double)0.26F, (double)0.15F);
            } else {
                pMatrixStack.translate((double)0.06F, (double)0.26F, -0.5D);
            }
        } else if (flag) {
            pMatrixStack.translate((double)0.46F, (double)0.26F, (double)0.22F);
        } else {
            pMatrixStack.translate((double)0.06F, (double)0.27F, -0.5D);
        }

        pMatrixStack.mulPose(Axis.XP.rotationDegrees(90.0F));
        if (flag) {
            pMatrixStack.mulPose(Axis.ZP.rotationDegrees(90.0F));
        }

        ItemStack itemstack = pLivingEntity.getItemBySlot(EquipmentSlot.MAINHAND);
        this.itemInHandRenderer.renderItem(pLivingEntity, itemstack, ItemDisplayContext.GROUND, false, pMatrixStack, pBuffer, pPackedLight);
        pMatrixStack.popPose();
    }
}