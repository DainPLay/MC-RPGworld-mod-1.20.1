package net.dainplay.rpgworldmod.entity.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.dainplay.rpgworldmod.RPGworldMod;
import net.dainplay.rpgworldmod.entity.custom.Fireflantern;
import net.dainplay.rpgworldmod.entity.custom.MosquitoSwarm;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;


public class MosquitoSwarmRenderer extends EntityRenderer<MosquitoSwarm> {
    public static final ResourceLocation TEXTURE1 = new ResourceLocation(RPGworldMod.MOD_ID, "textures/entity/mosquito_swarm/mosquito_swarm_1.png");
    public static final ResourceLocation TEXTURE2 = new ResourceLocation(RPGworldMod.MOD_ID, "textures/entity/mosquito_swarm/mosquito_swarm_2.png");
    public static final ResourceLocation TEXTURE3 = new ResourceLocation(RPGworldMod.MOD_ID, "textures/entity/mosquito_swarm/mosquito_swarm_3.png");
    public static final ResourceLocation TEXTURE4 = new ResourceLocation(RPGworldMod.MOD_ID, "textures/entity/mosquito_swarm/mosquito_swarm_4.png");
    public static final ResourceLocation TEXTURE5 = new ResourceLocation(RPGworldMod.MOD_ID, "textures/entity/mosquito_swarm/mosquito_swarm_5.png");
    public static final ResourceLocation TEXTURE6 = new ResourceLocation(RPGworldMod.MOD_ID, "textures/entity/mosquito_swarm/mosquito_swarm_6.png");
    public static final ResourceLocation TEXTURE7 = new ResourceLocation(RPGworldMod.MOD_ID, "textures/entity/mosquito_swarm/mosquito_swarm_7.png");
    public static final ResourceLocation TEXTURE8 = new ResourceLocation(RPGworldMod.MOD_ID, "textures/entity/mosquito_swarm/mosquito_swarm_8.png");

    public MosquitoSwarmRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    public ResourceLocation getTextureLocation(MosquitoSwarm pEntity) {
        return switch (pEntity.tickCount % 8) {
            case 1 -> TEXTURE1;
            case 2 -> TEXTURE2;
            case 3 -> TEXTURE3;
            case 4 -> TEXTURE4;
            case 5 -> TEXTURE5;
            case 6 -> TEXTURE6;
            case 7 -> TEXTURE7;
            default -> TEXTURE8;
        };
    }


    @Override
    public void render(MosquitoSwarm entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();

        //Get the camera
        Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
        // Orient the entity to face the camera
        poseStack.mulPose(camera.rotation());
        //Optional scale modifier
        poseStack.scale(1.5f, 1.5f, 1.5f);

        // Render our flat quad
        VertexConsumer consumer = buffer.getBuffer(RenderType.entityTranslucent(getTextureLocation(entity)));
        renderQuad(poseStack, consumer, packedLight);

        poseStack.popPose();
    }

    private void renderQuad(PoseStack poseStack, VertexConsumer consumer, int packedLight) {
        float width = 0.5f; // half width
        float height = 0.5f; // half height

        //Define the vertices of the quad in local space
        //  (x, y, z, u, v, normal)
        //Note that in minecraft the X axis points toward the right, the Y to the sky, and the Z toward the player

        //Bottom Left Vertex
        vertex(consumer, poseStack, -width, -height+0.4f, 0, 0f, 1f, packedLight);
        //Bottom Right Vertex
        vertex(consumer, poseStack, width, -height+0.4f, 0, 1f, 1f, packedLight);
        //Top Right Vertex
        vertex(consumer, poseStack, width, height+0.4f, 0, 1f, 0f, packedLight);
        //Top Left Vertex
        vertex(consumer, poseStack, -width, height+0.4f, 0, 0f, 0f, packedLight);
    }

    private void vertex(VertexConsumer consumer, PoseStack poseStack, float x, float y, float z, float u, float v, int packedLight)
    {
        var entry = poseStack.last();
        consumer.vertex(entry.pose(), x, y, z).color(1.0f, 1.0f, 1.0f, 1.0f).uv(u, v)
                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(entry.normal(), 0f, 0f, 1.0f).endVertex();
    }

}
