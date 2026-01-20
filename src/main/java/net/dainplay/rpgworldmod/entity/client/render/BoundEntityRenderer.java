package net.dainplay.rpgworldmod.entity.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.dainplay.rpgworldmod.network.ClientBoundEntityData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.joml.Matrix4f;

public class BoundEntityRenderer {

    @SubscribeEvent
    public static void onRenderWorldLast(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRIPWIRE_BLOCKS) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) {
            return;
        }

        PoseStack poseStack = event.getPoseStack();
        float partialTicks = event.getPartialTick();
        MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();

        for (ClientBoundEntityData.BoundEntityClientData data : ClientBoundEntityData.getBoundEntities().values()) {
            Entity entity = data.getEntity();
            if (entity != null && entity.isAlive()) {
                renderLeashLine(poseStack, bufferSource, partialTicks, entity, mc.player);
            }
        }

        bufferSource.endBatch(RenderType.leash());
    }

    private static void renderLeashLine(PoseStack poseStack, MultiBufferSource bufferSource,
                                        float partialTicks, Entity entity, Entity holder) {
        if (holder == null) return;

        poseStack.pushPose();

        Vec3 cameraPos = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();

        // Получаем позиции с интерполяцией как в ванильном коде
        Vec3 holderPos = holder.getRopeHoldPosition(partialTicks);

        // Расчет точки крепления на сущности (аналогично getLeashOffset)
        double d0;
        if(entity instanceof LivingEntity livingEntity) d0 = (double)(Mth.lerp(partialTicks, livingEntity.yBodyRotO, livingEntity.yBodyRot) * ((float)Math.PI / 180F)) + (Math.PI / 2D);
        else d0 = (double)(Mth.lerp(partialTicks, 0, 0) * ((float)Math.PI / 180F)) + (Math.PI / 2D);
        Vec3 entityOffset = new Vec3(0.0D, entity.getEyeHeight(), 0.0D); // Простое смещение
        double d1 = Math.cos(d0) * entityOffset.z + Math.sin(d0) * entityOffset.x;
        double d2 = Math.sin(d0) * entityOffset.z - Math.cos(d0) * entityOffset.x;
        double d3 = Mth.lerp((double)partialTicks, entity.xo, entity.getX()) + d1;
        double d4 = Mth.lerp((double)partialTicks, entity.yo, entity.getY()) + entityOffset.y;
        double d5 = Mth.lerp((double)partialTicks, entity.zo, entity.getZ()) + d2;

        // Вектор от сущности к держателю
        float f = (float)(holderPos.x() - d3);
        float f1 = (float)(holderPos.y() - d4);
        float f2 = (float)(holderPos.z() - d5);

        // Переводим в пространство камеры
        poseStack.translate(d1, entityOffset.y, d2);
        poseStack.translate(
                Mth.lerp((double)partialTicks, entity.xo, entity.getX()) - cameraPos.x(),
                Mth.lerp((double)partialTicks, entity.yo, entity.getY()) - cameraPos.y(),
                Mth.lerp((double)partialTicks, entity.zo, entity.getZ()) - cameraPos.z()
        );

        // Рендерим поводок
        VertexConsumer vertexconsumer = bufferSource.getBuffer(RenderType.leash());
        Matrix4f matrix4f = poseStack.last().pose();

        // Расчет ширины ленты (как в ванильном коде)
        float horizontalDist = Mth.sqrt(f * f + f2 * f2);
        float segmentWidth = 0.025F;
        float perpendicularX = f2 * segmentWidth / horizontalDist / 2.0F;
        float perpendicularZ = f * segmentWidth / horizontalDist / 2.0F;

        // Получаем уровни освещения
        BlockPos entityPos = BlockPos.containing(entity.getEyePosition(partialTicks));
        BlockPos holderPosBlock = BlockPos.containing(holder.getEyePosition(partialTicks));

        int entityBlockLight = Minecraft.getInstance().level.getBrightness(LightLayer.BLOCK, entityPos);
        int holderBlockLight = Minecraft.getInstance().level.getBrightness(LightLayer.BLOCK, holderPosBlock);
        int entitySkyLight = Minecraft.getInstance().level.getBrightness(LightLayer.SKY, entityPos);
        int holderSkyLight = Minecraft.getInstance().level.getBrightness(LightLayer.SKY, holderPosBlock);

        // Рендерим 24 сегмента как в ванильном коде
        for (int i = 0; i <= 24; ++i) {
            addVertexPair(vertexconsumer, matrix4f, f, f1, f2,
                    entityBlockLight, holderBlockLight, entitySkyLight, holderSkyLight,
                    segmentWidth, 0.025F, perpendicularX, perpendicularZ, i, false);
        }

        for (int i = 24; i >= 0; --i) {
            addVertexPair(vertexconsumer, matrix4f, f, f1, f2,
                    entityBlockLight, holderBlockLight, entitySkyLight, holderSkyLight,
                    segmentWidth, 0.0F, perpendicularX, perpendicularZ, i, true);
        }

        poseStack.popPose();
    }

    private static void addVertexPair(VertexConsumer consumer, Matrix4f matrix,
                                      float dx, float dy, float dz,
                                      int entityBlockLight, int holderBlockLight,
                                      int entitySkyLight, int holderSkyLight,
                                      float width, float offset,
                                      float perpendicularX, float perpendicularZ,
                                      int segment, boolean reverse) {
        float progress = (float)segment / 24.0F;

        // Интерполяция освещения
        int blockLight = (int)Mth.lerp(progress, (float)entityBlockLight, (float)holderBlockLight);
        int skyLight = (int)Mth.lerp(progress, (float)entitySkyLight, (float)holderSkyLight);
        int packedLight = LightTexture.pack(blockLight, skyLight);

        // Цвет как у ванильного поводка (коричневый)
        float brightness = segment % 2 == (reverse ? 1 : 0) ? 0.7F : 1.0F;
        float red = 0.5F * brightness;
        float green = 0.4F * brightness;
        float blue = 0.3F * brightness;
        float alpha = 1.0F;

        // Позиция с волной (синусоидальная форма)
        float x = dx * progress;
        float y = dy > 0.0F ? dy * progress * progress : dy - dy * (1.0F - progress) * (1.0F - progress);
        float z = dz * progress;

        y += Mth.sin(progress * (float)Math.PI) * 0.1F;

        if (reverse) {
            consumer.vertex(matrix, x + perpendicularX, y + width - offset, z - perpendicularZ)
                    .color(red, green, blue, alpha)
                    .uv2(packedLight)
                    .endVertex();
            consumer.vertex(matrix, x - perpendicularX, y + offset, z + perpendicularZ)
                    .color(red, green, blue, alpha)
                    .uv2(packedLight)
                    .endVertex();
        } else {
            consumer.vertex(matrix, x - perpendicularX, y + offset, z + perpendicularZ)
                    .color(red, green, blue, alpha)
                    .uv2(packedLight)
                    .endVertex();
            consumer.vertex(matrix, x + perpendicularX, y + width - offset, z - perpendicularZ)
                    .color(red, green, blue, alpha)
                    .uv2(packedLight)
                    .endVertex();
        }
    }
}