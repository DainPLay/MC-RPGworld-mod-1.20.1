package net.dainplay.rpgworldmod.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.dainplay.rpgworldmod.network.ClientGuardianAttackData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class GuardianBeamRenderer {
    private static final ResourceLocation BEAM_LOCATION = new ResourceLocation("textures/entity/guardian_beam.png");
    private static final RenderType BEAM_RENDER_TYPE = RenderType.entityCutoutNoCull(BEAM_LOCATION);

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_ENTITIES) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;

        PoseStack poseStack = event.getPoseStack();
        MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();
        float partialTick = event.getPartialTick();
        Vec3 cameraPos = mc.gameRenderer.getMainCamera().getPosition();

        for (var entry : ClientGuardianAttackData.getAll().entrySet()) {
            ClientGuardianAttackData.AttackData data = entry.getValue();
            if (data.target == null || !data.target.isAlive() || data.attacker == null || !data.attacker.isAlive())
                continue;
            renderBeam(poseStack, bufferSource, partialTick, cameraPos, data.attacker, data.target, data.attackTime);
        }
    }

    /**
     * Вычисляет мировые координаты точки, из которой должен исходить луч (дуло оружия/рука).
     * Учитывает режим камеры (первое/третье лицо), сторону руки и анимацию атаки.
     */
    private static Vec3 getMuzzlePosition(Player player, float partialTick, boolean firstPerson) {
        Minecraft mc = Minecraft.getInstance();
        HumanoidArm mainArm = player.getMainArm();
        // Определяем, левая ли рука используется (считаем, что атака всегда из главной руки)
        boolean leftHand = mainArm == HumanoidArm.LEFT;
        int side = leftHand ? -1 : 1;  // -1 для левой, 1 для правой

        if (firstPerson) {
            // Режим от первого лица: позиция руки относительно камеры через ближнюю плоскость
            Vec3 cameraPos = mc.gameRenderer.getMainCamera().getPosition();
            double scale = 1000.0 / mc.getEntityRenderDispatcher().options.fov().get().intValue();
            Vec3 nearPoint = mc.getEntityRenderDispatcher().camera.getNearPlane().getPointOnPlane(side * 0.35F, -0.25F);
            float attackAnim = player.getAttackAnim(partialTick);
            float f1 = Mth.sin(Mth.sqrt(attackAnim) * (float) Math.PI);
            nearPoint = nearPoint.scale(scale);
            nearPoint = nearPoint.yRot(f1 * 0.5F);
            nearPoint = nearPoint.xRot(-f1 * 0.7F);
            return cameraPos.add(nearPoint);
        } else {
            // Режим от третьего лица: смещение относительно позиции игрока с учётом поворота тела и взгляда
            Vec3 playerPos = player.getPosition(partialTick);
            float yBodyRot = Mth.lerp(partialTick, player.yBodyRotO, player.yBodyRot);
            double armOffsetX = player.getBbWidth() * -0.5 * side;
            double armOffsetY = player.getBbHeight() * 0.8;
            Vec3 offset = new Vec3(armOffsetX, armOffsetY, 0).yRot((float) Math.toRadians(-yBodyRot));
            Vec3 viewVec = player.getViewVector(partialTick).normalize().scale(1.5);
            return playerPos.add(offset).add(viewVec);
        }
    }

    private static void renderBeam(PoseStack poseStack, MultiBufferSource bufferSource, float partialTick, Vec3 cameraPos,
                                   Player attacker, LivingEntity target, int attackTime) {
        float attackProgress = (attackTime + partialTick) / 80.0F;

        // Определяем режим камеры и получаем точное начало луча от руки
        boolean firstPerson = false;
        if(attacker == Minecraft.getInstance().player) firstPerson = Minecraft.getInstance().options.getCameraType().isFirstPerson();
        Vec3 start = getMuzzlePosition(attacker, partialTick, firstPerson);
        Vec3 end = getPosition(target, target.getBbHeight() * 0.5D, partialTick);
        Vec3 direction = end.subtract(start);
        float length = (float) (direction.length() + 1.0F);
        direction = direction.normalize();

        float yaw = (float) Math.atan2(direction.z, direction.x);
        float pitch = (float) Math.acos(direction.y);

        poseStack.pushPose();
        // Смещаем относительно камеры
        poseStack.translate(start.x - cameraPos.x, start.y - cameraPos.y, start.z - cameraPos.z);
        poseStack.mulPose(Axis.YP.rotationDegrees(((float)Math.PI/2 - yaw) * (180F/(float)Math.PI)));
        poseStack.mulPose(Axis.XP.rotationDegrees(pitch * (180F/(float)Math.PI)));

        float f1 = attackTime + partialTick;
        float f2 = f1 * 0.05F * -1.5F;
        float f3 = attackProgress * attackProgress;

        int red = 64 + (int)(f3 * 191.0F);
        int green = 32 + (int)(f3 * 191.0F);
        int blue = 128 - (int)(f3 * 64.0F);

        // Предрассчитанные смещения для вершин (как в ванильном коде)
        float f11 = Mth.cos(f2 + 2.3561945F) * 0.282F;
        float f12 = Mth.sin(f2 + 2.3561945F) * 0.282F;
        float f13 = Mth.cos(f2 + ((float)Math.PI / 4F)) * 0.282F;
        float f14 = Mth.sin(f2 + ((float)Math.PI / 4F)) * 0.282F;
        float f15 = Mth.cos(f2 + 3.926991F) * 0.282F;
        float f16 = Mth.sin(f2 + 3.926991F) * 0.282F;
        float f17 = Mth.cos(f2 + 5.4977875F) * 0.282F;
        float f18 = Mth.sin(f2 + 5.4977875F) * 0.282F;
        float f19 = Mth.cos(f2 + (float)Math.PI) * 0.2F;
        float f20 = Mth.sin(f2 + (float)Math.PI) * 0.2F;
        float f21 = Mth.cos(f2 + 0.0F) * 0.2F;
        float f22 = Mth.sin(f2 + 0.0F) * 0.2F;
        float f23 = Mth.cos(f2 + ((float)Math.PI / 2F)) * 0.2F;
        float f24 = Mth.sin(f2 + ((float)Math.PI / 2F)) * 0.2F;
        float f25 = Mth.cos(f2 + ((float)Math.PI * 1.5F)) * 0.2F;
        float f26 = Mth.sin(f2 + ((float)Math.PI * 1.5F)) * 0.2F;

        float f29 = -1.0F + (f1 * 0.5F % 1.0F);
        float f30 = length * 2.5F + f29;

        VertexConsumer vertexconsumer = bufferSource.getBuffer(BEAM_RENDER_TYPE);
        Matrix4f matrix4f = poseStack.last().pose();
        Matrix3f matrix3f = poseStack.last().normal();

        // Рисуем основные полосы
        vertex(vertexconsumer, matrix4f, matrix3f, f19, length, f20, red, green, blue, 0.4999F, f30);
        vertex(vertexconsumer, matrix4f, matrix3f, f19, 0.0F, f20, red, green, blue, 0.4999F, f29);
        vertex(vertexconsumer, matrix4f, matrix3f, f21, 0.0F, f22, red, green, blue, 0.0F, f29);
        vertex(vertexconsumer, matrix4f, matrix3f, f21, length, f22, red, green, blue, 0.0F, f30);
        vertex(vertexconsumer, matrix4f, matrix3f, f23, length, f24, red, green, blue, 0.4999F, f30);
        vertex(vertexconsumer, matrix4f, matrix3f, f23, 0.0F, f24, red, green, blue, 0.4999F, f29);
        vertex(vertexconsumer, matrix4f, matrix3f, f25, 0.0F, f26, red, green, blue, 0.0F, f29);
        vertex(vertexconsumer, matrix4f, matrix3f, f25, length, f26, red, green, blue, 0.0F, f30);

        // Рисуем дополнительные сегменты (мигающие)
        float f31 = (attacker.tickCount % 2 == 0) ? 0.5F : 0.0F;
        vertex(vertexconsumer, matrix4f, matrix3f, f11, length, f12, red, green, blue, 0.5F, f31 + 0.5F);
        vertex(vertexconsumer, matrix4f, matrix3f, f13, length, f14, red, green, blue, 1.0F, f31 + 0.5F);
        vertex(vertexconsumer, matrix4f, matrix3f, f17, length, f18, red, green, blue, 1.0F, f31);
        vertex(vertexconsumer, matrix4f, matrix3f, f15, length, f16, red, green, blue, 0.5F, f31);

        poseStack.popPose();
    }

    private static Vec3 getPosition(LivingEntity entity, double yOffset, float partialTick) {
        double x = Mth.lerp(partialTick, entity.xOld, entity.getX());
        double y = Mth.lerp(partialTick, entity.yOld, entity.getY()) + yOffset;
        double z = Mth.lerp(partialTick, entity.zOld, entity.getZ());
        return new Vec3(x, y, z);
    }

    private static void vertex(VertexConsumer consumer, Matrix4f pose, Matrix3f normal,
                               float x, float y, float z, int red, int green, int blue, float u, float v) {
        consumer.vertex(pose, x, y, z)
                .color(red, green, blue, 255)
                .uv(u, v)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(15728880)
                .normal(normal, 0.0F, 1.0F, 0.0F)
                .endVertex();
    }
}