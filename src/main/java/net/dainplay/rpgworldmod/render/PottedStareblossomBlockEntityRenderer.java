package net.dainplay.rpgworldmod.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.dainplay.rpgworldmod.RPGworldMod;
import net.dainplay.rpgworldmod.block.ModBlocks;
import net.dainplay.rpgworldmod.block.entity.custom.PottedStareblossomBlockEntity;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.FoliageColor;
import net.minecraft.world.level.GrassColor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

public class PottedStareblossomBlockEntityRenderer implements BlockEntityRenderer<PottedStareblossomBlockEntity> {

    // Путь к текстуре бутона
    private static final ResourceLocation BUD_TEXTURE =
        new ResourceLocation("rpgworldmod", "textures/block/stareblossom_bud.png");

    // Размеры billboard плоскости
    private static final float SIZE = 1.0f;
    private static final float OFFSET_Y = 0.6875f; // Смещение по Y относительно центра блока

    public PottedStareblossomBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
    }
    
    @Override
    public void render(PottedStareblossomBlockEntity blockEntity, float partialTick,
                      PoseStack poseStack, MultiBufferSource bufferSource, 
                      int packedLight, int packedOverlay) {
        
        // Получаем камеру игрока
        Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
        
        poseStack.pushPose();
        
        // Переходим в центр блока
        poseStack.translate(0.5, 0.5, 0.5);
        
        // Получаем направление от блока к камере
        Vec3 cameraPos = camera.getPosition();
        Vec3 blockPos = Vec3.atCenterOf(blockEntity.getBlockPos());
        Vec3 direction = cameraPos.subtract(blockPos);
        
        // Вычисляем угол поворота по оси Y
        double angleY = Math.atan2(direction.x(), direction.z());
        
        // Поворачиваем billboard так, чтобы он всегда смотрел на камеру
        poseStack.mulPose(Axis.YP.rotation((float) angleY));
        
        // Отменяем вращение камеры по X, чтобы billboard оставался вертикальным
        // (если нужно вращение по всем осям - уберите эту строку)
        poseStack.mulPose(Axis.XP.rotationDegrees(-camera.getXRot()));
        
        // Возвращаемся к позиции billboard
        poseStack.translate(0, OFFSET_Y - 0.5, 0.1);
        
        // Рендерим billboard плоскость
        renderBillboard(poseStack, bufferSource, packedLight);
        
        poseStack.popPose();
    }
    
    private void renderBillboard(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.entityCutoutNoCull(BUD_TEXTURE));
        PoseStack.Pose pose = poseStack.last();
        
        // Половина размеров
        float size = SIZE / 8.0f * 3.0F;
        
        // Вершины billboard (два треугольника для плоскости)

        // Нижний левый
        vertexConsumer.vertex(pose.pose(), -size, -size, 0.0F)
                .color(1.0F, 1.0F, 1.0F, 1F)  // Полупрозрачность 60%
                .uv(0.0F, 1.0F)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLight)
                .normal(0.0F, 0.0F, 1.0F)
                .endVertex();

        // Нижний правый
        vertexConsumer.vertex(pose.pose(), size, -size, 0.0F)
                .color(1.0F, 1.0F, 1.0F, 1F)
                .uv(1.0F, 1.0F)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLight)
                .normal(0.0F, 0.0F, 1.0F)
                .endVertex();

        // Верхний правый
        vertexConsumer.vertex(pose.pose(), size, size, 0.0F)
                .color(1.0F, 1.0F, 1.0F, 1F)
                .uv(1.0F, 0.0F)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLight)
                .normal(0.0F, 0.0F, 1.0F)
                .endVertex();

        // Верхний левый
        vertexConsumer.vertex(pose.pose(), -size, size, 0.0F)
                .color(1.0F, 1.0F, 1.0F, 1F)
                .uv(0.0F, 0.0F)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLight)
                .normal(0.0F, 0.0F, 1.0F)
                .endVertex();
    }
    
    @Override
    public boolean shouldRenderOffScreen(PottedStareblossomBlockEntity blockEntity) {
        return true; // Всегда рендерить, даже если блок вне видимой области
    }

    @Mod.EventBusSubscriber(modid = RPGworldMod.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ColorHandler
    {
        @SubscribeEvent
        public static void registerItemColors(RegisterColorHandlersEvent.Item event)
        {
            event.getItemColors().register((stack, tintIndex) -> {
                        BlockState state = ((BlockItem)stack.getItem()).getBlock().defaultBlockState();
                        return event.getBlockColors().getColor(state, null, null, tintIndex); },
                    ModBlocks.WIDOWEED.get(), ModBlocks.TRIPLOVER.get(), ModBlocks.RIE_LEAVES.get(), ModBlocks.SPIKY_IVY.get());
        }

        @SubscribeEvent
        public static void registerBlockColors(RegisterColorHandlersEvent.Block event)
        {
            //Grass Coloring
            event.getBlockColors().register((state, world, pos, tintIndex) ->
                            world != null && pos != null ? BiomeColors.getAverageGrassColor(world, pos) : GrassColor.get(0.5D, 1.0D),
                    ModBlocks.WIDOWEED.get(), ModBlocks.TRIPLOVER.get(), ModBlocks.HOLTS_REFLECTION.get(), ModBlocks.SPIKY_IVY.get(), ModBlocks.POTTED_SPIKY_IVY.get(), ModBlocks.POTTED_HOLTS_REFLECTION.get(), ModBlocks.PARALILY.get());

            //Foliage Coloring
            event.getBlockColors().register((state, world, pos, tintIndex) ->
                            world != null && pos != null ? BiomeColors.getAverageFoliageColor(world, pos) : FoliageColor.getDefaultColor(),
                    ModBlocks.RIE_LEAVES.get());
            //Arbor Fuel Coloring
            event.getBlockColors().register((state, world, pos, tintIndex) ->
                            11633736,
                    ModBlocks.ARBOR_FUEL_CAULDRON.get());


            event.getBlockColors().register((p_92596_, p_92597_, p_92598_, p_92599_) -> p_92597_ != null && p_92598_ != null ? 2129968 : 7455580, ModBlocks.PARALILY.get());
        }
    }

    public static class ColorConstants {

        public static final int RIE_WEALD_WATER = 0x3B8CAE;

        public static final int RIE_WEALD_WATERFOG = 0x1D4657;

        public static int RIE_WEALD_FOLIAGE_COLOR = 0x3EB261;

        public static int RIE_WEALD_GRASS_COLOR = 0x20AC6E;

        public static int STANDARD_FOG = 0x7AABFA;

        @Deprecated
        private ColorConstants() {}
    }
}