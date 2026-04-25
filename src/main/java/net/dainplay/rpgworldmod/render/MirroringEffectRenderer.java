package net.dainplay.rpgworldmod.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.dainplay.rpgworldmod.effect.ModEffects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class MirroringEffectRenderer {

    private static final ThreadLocal<Boolean> RENDERING_MIRROR = ThreadLocal.withInitial(() -> false);
    private static final Map<Integer, MirrorData> MIRROR_DATA = new HashMap<>();

    public static void updateSeed(int entityId, long newSeed, long gameTime) {
        MirrorData data = MIRROR_DATA.computeIfAbsent(entityId, id -> new MirrorData());
        data.setSeed(newSeed, gameTime);
    }

    // ========= Рендеринг для всех, кроме клиентского игрока от первого лица =========
    @SubscribeEvent
    public static void onRenderLivingPost(RenderLivingEvent.Post<?, ?> event) {
        if (RENDERING_MIRROR.get()) return;
        LivingEntity entity = event.getEntity();
        if (entity == Minecraft.getInstance().player
                && Minecraft.getInstance().options.getCameraType().isFirstPerson()) return;
        if (!entity.hasEffect(ModEffects.MIRRORING.get())) return;

        MirrorData data = MIRROR_DATA.get(entity.getId());
        if (data == null || !data.isInitialized()) return;

        RENDERING_MIRROR.set(true);
        try {
            float partialTick = event.getPartialTick();
            @SuppressWarnings("unchecked")
            LivingEntityRenderer<LivingEntity, EntityModel<LivingEntity>> renderer =
                    (LivingEntityRenderer<LivingEntity, EntityModel<LivingEntity>>) event.getRenderer();
            PoseStack poseStack = event.getPoseStack();
            MultiBufferSource buffer = event.getMultiBufferSource();
            int packedLight = event.getPackedLight();

            float entityYaw = Mth.rotLerp(partialTick, entity.yBodyRotO, entity.yBodyRot);
            long gameTime = entity.level().getGameTime();
            boolean wasInvisible = entity.isInvisible();
            entity.setInvisible(false);

            for (int i = 0; i < entity.getEffect(ModEffects.MIRRORING.get()).getAmplifier()+1; i++) {
                Vec3 offset = data.getOffset(entity.getId(), i, gameTime, partialTick);
                poseStack.pushPose();
                poseStack.translate(offset.x, offset.y, offset.z);
                try {
                    renderer.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                poseStack.popPose();
            }
            entity.setInvisible(wasInvisible);
        } finally {
            RENDERING_MIRROR.set(false);
        }
    }

    // ========= Рендеринг клиентского игрока от первого лица =========
    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_ENTITIES) return;
        Minecraft mc = Minecraft.getInstance();
        LivingEntity player = mc.player;
        if (player == null || !mc.options.getCameraType().isFirstPerson()) return;
        if (!player.hasEffect(ModEffects.MIRRORING.get())) return;

        MirrorData data = MIRROR_DATA.get(player.getId());
        if (data == null || !data.isInitialized()) return;

        RENDERING_MIRROR.set(true);
        try {
            float partialTick = event.getPartialTick();
            LivingEntityRenderer<LivingEntity, EntityModel<LivingEntity>> renderer =
                    (LivingEntityRenderer<LivingEntity, EntityModel<LivingEntity>>)
                            mc.getEntityRenderDispatcher().getRenderer(player);
            if (renderer == null) return;

            MultiBufferSource originalBuffer = mc.renderBuffers().bufferSource();
            // Обёртка, подменяющая RenderType на полупрозрачный + заставляющая альфу быть 0.15
            MultiBufferSource translucentBuffer = renderType -> {
                VertexConsumer originalConsumer = originalBuffer.getBuffer(makeTranslucent(renderType));
                return new VertexConsumer() {
                    @Override public VertexConsumer vertex(double x, double y, double z) { return originalConsumer.vertex(x, y, z); }
                    @Override public VertexConsumer color(int red, int green, int blue, int alpha) { return originalConsumer.color(red, green, blue, (int)(alpha * 0.15F)); }
                    @Override public VertexConsumer uv(float u, float v) { return originalConsumer.uv(u, v); }
                    @Override public VertexConsumer overlayCoords(int u, int v) { return originalConsumer.overlayCoords(u, v); }
                    @Override public VertexConsumer uv2(int u, int v) { return originalConsumer.uv2(u, v); }
                    @Override public VertexConsumer normal(float x, float y, float z) { return originalConsumer.normal(x, y, z); }
                    @Override public void endVertex() { originalConsumer.endVertex(); }
                    @Override public void defaultColor(int defaultR, int defaultG, int defaultB, int defaultA) { originalConsumer.defaultColor(defaultR, defaultG, defaultB, (int)(defaultA * 0.15F)); }
                    @Override public void unsetDefaultColor() { originalConsumer.unsetDefaultColor(); }
                };
            };

            PoseStack poseStack = event.getPoseStack();
            double px = Mth.lerp(partialTick, player.xOld, player.getX());
            double py = Mth.lerp(partialTick, player.yOld, player.getY());
            double pz = Mth.lerp(partialTick, player.zOld, player.getZ());
            Vec3 camPos = mc.gameRenderer.getMainCamera().getPosition();
            float entityYaw = Mth.rotLerp(partialTick, player.yBodyRotO, player.yBodyRot);
            long gameTime = player.level().getGameTime();

            boolean wasInvisible = player.isInvisible();
            player.setInvisible(false);
            MobEffectInstance happiness = null;
            if(player.hasEffect(ModEffects.HAPPINESS.get())) {
                happiness = player.getEffect(ModEffects.HAPPINESS.get());
                player.removeEffect(ModEffects.HAPPINESS.get());
            }
            int hadArrows = player.getArrowCount();
            player.setArrowCount(0);

            for (int i = 0; i < player.getEffect(ModEffects.MIRRORING.get()).getAmplifier()+1; i++) {
                Vec3 offset = data.getOffset(player.getId(), i, gameTime, partialTick);
                double wx = px + offset.x, wy = py + offset.y, wz = pz + offset.z;
                int packedLight = getPackedLightAt(mc.level, px, py, pz);

                poseStack.pushPose();
                poseStack.translate(wx - camPos.x, wy - camPos.y, wz - camPos.z);
                // --- ИСПРАВЛЕНИЕ: защита от сбоя внутри render ---
                try {
                    renderer.render(player, entityYaw, partialTick, poseStack, translucentBuffer, packedLight);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                poseStack.popPose();
            }
            player.setInvisible(wasInvisible);
            if(happiness != null) player.addEffect(happiness);
            player.setArrowCount(hadArrows);
        } finally {
            RENDERING_MIRROR.set(false);
        }
    }

    // ==================== makeTranslucent ====================
    private static final Map<RenderType, RenderType> TRANSLUCENT_CACHE = new HashMap<>();

    private static RenderType makeTranslucent(RenderType original) {
        if (TRANSLUCENT_CACHE.containsKey(original)) return TRANSLUCENT_CACHE.get(original);
        RenderType result = original; // fallback

        try {
            ResourceLocation texture = extractTexture(original);
            // Не трогаем glint-слои – для них нет гарантированного буфера
            if (isGlintType(original)) {
                result = original;
            } else if (texture != null && original == RenderType.armorCutoutNoCull(texture)) {
                result = RenderType.entityTranslucentCull(texture);
            } else if (texture != null) {
                result = RenderType.entityTranslucentCull(texture);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        TRANSLUCENT_CACHE.put(original, result);
        return result;
    }

    private static boolean isGlintType(RenderType type) {
        return type == RenderType.glint()
                || type == RenderType.entityGlint()
                || type == RenderType.glintTranslucent()
                || type == RenderType.armorGlint()
                || type == RenderType.armorEntityGlint()
                || type == RenderType.entityGlintDirect()
                || type == ModRenderTypes.ALTERATION_GLINT
                || type == ModRenderTypes.RESTORATION_GLINT
                || type == ModRenderTypes.DESTRUCTION_GLINT
                || type == ModRenderTypes.ILLUSION_GLINT
                || type == ModRenderTypes.CONJURATION_GLINT
                || type == ModRenderTypes.SUMMONED_GLINT
                || type == ModRenderTypes.SUMMONED_GLINT_ENTITY
                || type == ModRenderTypes.SPELL_EFFECT
                || type == ModRenderTypes.GLOW_SPELL_EFFECT
                || type == ModRenderTypes.BEAMS_RENDER_TYPE
                || type == RenderType.glintDirect();
    }

    private static ResourceLocation extractTexture(RenderType type) {
        try {
            if (type instanceof RenderType.CompositeRenderType comp) {
                Field stateField = ObfuscationReflectionHelper.findField(RenderType.CompositeRenderType.class, "state");
                stateField.setAccessible(true);
                RenderType.CompositeState state = (RenderType.CompositeState) stateField.get(comp);

                Field textureStateField = ObfuscationReflectionHelper.findField(RenderType.CompositeState.class, "textureState");
                textureStateField.setAccessible(true);
                Object textureShard = textureStateField.get(state);
                if (textureShard instanceof RenderStateShard.EmptyTextureStateShard empty) {
                    Optional<ResourceLocation> texOpt = empty.cutoutTexture();
                    if (texOpt.isPresent()) return texOpt.get();
                }
            }
        } catch (Exception ignored) {}
        return null;
    }

    private static int getPackedLightAt(net.minecraft.world.level.Level level, double x, double y, double z) {
        if (level == null) return 15728880;
        net.minecraft.core.BlockPos pos = net.minecraft.core.BlockPos.containing(x, y, z);
        int blockLight = level.getBrightness(net.minecraft.world.level.LightLayer.BLOCK, pos);
        int skyLight = level.getBrightness(net.minecraft.world.level.LightLayer.SKY, pos);
        return net.minecraft.client.renderer.LightTexture.pack(blockLight, skyLight);
    }

    // ==================== MirrorData ====================
    private static class MirrorData {
        private long prevSeed, currentSeed, lastTransitionGameTime;
        private boolean initialized;

        boolean isInitialized() { return initialized; }

        void setSeed(long newSeed, long gameTime) {
            if (!initialized) {
                prevSeed = currentSeed = newSeed;
                initialized = true;
            } else {
                prevSeed = currentSeed;
                currentSeed = newSeed;
            }
            lastTransitionGameTime = gameTime;
        }

        Vec3 getOffset(int entityId, int index, long gameTime, float partialTick) {
            if (!initialized) return Vec3.ZERO;
            float progress = Mth.clamp((gameTime - lastTransitionGameTime + partialTick) / 3.0F, 0.0F, 1.0F);
            Vec3 prev = offsetFromSeed(prevSeed, entityId, index);
            Vec3 curr = offsetFromSeed(currentSeed, entityId, index);
            Vec3 base = prev.lerp(curr, progress);
            float bob = gameTime + partialTick;
            return base.add(
                    Mth.cos(index + bob * 0.5F) * 0.025,
                    Mth.cos(index + bob * 0.75F) * 0.0125,
                    Mth.cos(index + bob * 0.7F) * 0.025
            );
        }

        private static Vec3 offsetFromSeed(long seed, int entityId, int index) {
            RandomSource random = RandomSource.create(seed ^ ((long) entityId << 32) + index * 7162381L);
            double x = (-6.0F + random.nextInt(13)) * 0.5D;
            double y = Math.max(0, random.nextInt(6) - 4);
            double z = (-6.0F + random.nextInt(13)) * 0.5D;
            return new Vec3(x, y, z);
        }
    }
}