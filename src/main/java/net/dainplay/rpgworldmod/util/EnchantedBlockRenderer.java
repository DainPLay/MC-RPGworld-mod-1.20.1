package net.dainplay.rpgworldmod.util;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.SheetedDecalTextureGenerator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.dainplay.rpgworldmod.block.custom.FairapierWiltedPlantBlock;
import net.dainplay.rpgworldmod.block.entity.custom.FairapierWiltedPlantBlockEntity;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;


public class EnchantedBlockRenderer implements BlockEntityRenderer<FairapierWiltedPlantBlockEntity> {
    static Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
    public static void setCamera(Camera value){
        camera = value;
    }

    public EnchantedBlockRenderer(BlockEntityRendererProvider.Context pContext) {
    }


    @Override
    public void render(FairapierWiltedPlantBlockEntity pBlockEntity, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, int pPackedOverlay) {
        if(pBlockEntity.getBlockState().getValue(FairapierWiltedPlantBlock.getIsEnchantedProperty())) {
            pPoseStack.pushPose();
            PoseStack.Pose posestack$pose1 = pPoseStack.last();
            VertexConsumer vertexconsumer1 = new SheetedDecalTextureGenerator(pBuffer.getBuffer(RenderType.entityGlint()), posestack$pose1.pose(), posestack$pose1.normal(), 1F);
            net.minecraftforge.client.model.data.ModelData modelData = pBlockEntity.getLevel().getModelDataManager().getAt(pBlockEntity.getBlockPos());
            Minecraft.getInstance().getBlockRenderer().renderBreakingTexture(pBlockEntity.getBlockState(), pBlockEntity.getBlockPos(), pBlockEntity.getLevel(), pPoseStack, vertexconsumer1, modelData == null ? net.minecraftforge.client.model.data.ModelData.EMPTY : modelData);
            pPoseStack.popPose();
        }
    }
}