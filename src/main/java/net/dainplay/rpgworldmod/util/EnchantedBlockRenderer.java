package net.dainplay.rpgworldmod.util;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.dainplay.rpgworldmod.block.custom.FairapierWiltedPlantBlock;
import net.dainplay.rpgworldmod.block.entity.custom.FairapierWiltedPlantBlockEntity;
import net.dainplay.rpgworldmod.item.ModItems;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;


public class EnchantedBlockRenderer implements BlockEntityRenderer<FairapierWiltedPlantBlockEntity> {
    static Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
    public static void setCamera(Camera value){
        camera = value;
    }

    public EnchantedBlockRenderer(BlockEntityRendererProvider.Context pContext) {
    }


    @Override
    public void render(FairapierWiltedPlantBlockEntity pBlockEntity, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, int pPackedOverlay) {
            pPoseStack.pushPose();

            // Маленькое смещение по Z для избежания z-fighting
            pPoseStack.translate(0.5, 0.34, 0.5);

            // Используем EntityItemRenderer для рендера эффекта
            ItemStack dummyStack = new ItemStack(ModItems.FAIRAPIER_SWORD.get());
            if(pBlockEntity.getBlockState().getValue(FairapierWiltedPlantBlock.getAgeProperty()) == 0) {
                CompoundTag nbtData = new CompoundTag();
                nbtData.putInt("Growing", 1);
                dummyStack.setTag(nbtData);
            }
            else if(pBlockEntity.getBlockState().getValue(FairapierWiltedPlantBlock.getAgeProperty()) == 1) {
                CompoundTag nbtData = new CompoundTag();
                nbtData.putInt("Growing", 2);
                dummyStack.setTag(nbtData);
            }
            if(pBlockEntity.getBlockState().getValue(FairapierWiltedPlantBlock.getIsEnchantedProperty())) {
                dummyStack.enchant(Enchantments.SHARPNESS, 1);
            }

            if ((pBlockEntity.getBlockPos().getX() + pBlockEntity.getBlockPos().getZ()) % 2 == 0) {
                pPoseStack.mulPose(Axis.YP.rotationDegrees(90));
            }

            // Настраиваем позицию
            pPoseStack.mulPose(Axis.ZP.rotationDegrees(-135));
            pPoseStack.scale(1.0F, 1.0F, 1.0F);

            // Рендерим эффект
            Minecraft.getInstance().getItemRenderer().renderStatic(
                    dummyStack,
                    ItemDisplayContext.NONE,
                    pPackedLight,
                    pPackedOverlay,
                    pPoseStack,
                    pBuffer,
                    pBlockEntity.getLevel(),
                    0
            );

            pPoseStack.popPose();
    }
}