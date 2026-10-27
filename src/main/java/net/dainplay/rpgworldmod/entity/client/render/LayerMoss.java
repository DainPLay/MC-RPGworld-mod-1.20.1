package net.dainplay.rpgworldmod.entity.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.dainplay.rpgworldmod.RPGworldMod;
import net.dainplay.rpgworldmod.effect.MossiosisEffect;
import net.dainplay.rpgworldmod.render.ModRenderTypes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LayerMoss extends RenderLayer {

	private static final ResourceLocation MOSS_64x64 =
			new ResourceLocation(RPGworldMod.MOD_ID, "textures/entity/mossiosis_overlay.png");
	private static final ResourceLocation MOSS_64x32 =
			new ResourceLocation(RPGworldMod.MOD_ID, "textures/entity/mossiosis_overlay_half.png");

	/** Кэш размеров исходной текстуры существа, чтобы не читать PNG каждый кадр. */
	private static final Map<ResourceLocation, int[]> TEX_DIM_CACHE = new ConcurrentHashMap<>();

	private final RenderLayerParent parent;

	public LayerMoss(RenderLayerParent parent) {
		super(parent);
		this.parent = parent;
	}

	@Override
	public void render(PoseStack pose, MultiBufferSource bufferIn, int packedLight, Entity entity,
					   float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks,
					   float netHeadYaw, float headPitch) {
		if (!(entity instanceof LivingEntity living)) return;
		if (living.getAttribute(Attributes.MAX_HEALTH).getModifier(MossiosisEffect.MODIFIER_UUID) == null) return;

		ResourceLocation entityTex = getTextureLocation(entity);
		int[] dims = TEX_DIM_CACHE.computeIfAbsent(entityTex, LayerMoss::readDimensions);
		if (dims == null) return;

		int bodyW = dims[0];
		int bodyH = dims[1];
		if (bodyW <= 0 || bodyH <= 0) return;

		// Выбор moss-текстуры по аспекту (как в исходнике: 2:1 -> 64x32, иначе 64x64)
		boolean halfMoss = (bodyW / bodyH) != 1;
		ResourceLocation mossTex = halfMoss ? MOSS_64x32 : MOSS_64x64;
		int mossW = 64;
		int mossH = halfMoss ? 32 : 64;

		// Во сколько раз нужно "размножить" мох, чтобы его пиксели совпали
		// по масштабу с пикселями текстуры тела.
		float scaleU = bodyW / (float) mossW;
		float scaleV = bodyH / (float) mossH;

		// Включаем GL_REPEAT (нужно для тайлинга UV > 1.0)
		ModRenderTypes.ensureRepeatWrap(mossTex);

		VertexConsumer base = bufferIn.getBuffer(ModRenderTypes.getMossOverlay(mossTex));
		VertexConsumer tiled = new TilingVertexConsumer(base, scaleU, scaleV);

		pose.pushPose();
		this.getParentModel().renderToBuffer(
				pose,
				tiled,
				packedLight,
				LivingEntityRenderer.getOverlayCoords(living, 0),
				1.0F, 1.0F, 1.0F, 1.0F
		);
		pose.popPose();
	}

	private static int[] readDimensions(ResourceLocation tex) {
		try (var stream = Minecraft.getInstance().getResourceManager().getResource(tex).get().open()) {
			BufferedImage img = ImageIO.read(stream);
			return new int[]{img.getWidth(), img.getHeight()};
		} catch (IOException e) {
			return null;
		}
	}

	/**
	 * Обёртка над VertexConsumer, которая умножает UV на заданный масштаб.
	 * В сочетании с GL_REPEAT на текстуре это даёт тайлинг без растяжения.
	 */
	private static final class TilingVertexConsumer implements VertexConsumer {
		private final VertexConsumer delegate;
		private final float uScale;
		private final float vScale;

		TilingVertexConsumer(VertexConsumer delegate, float uScale, float vScale) {
			this.delegate = delegate;
			this.uScale = uScale;
			this.vScale = vScale;
		}

		@Override public VertexConsumer vertex(double x, double y, double z) { delegate.vertex(x, y, z); return this; }
		@Override public VertexConsumer color(int r, int g, int b, int a) { delegate.color(r, g, b, a); return this; }
		@Override public VertexConsumer uv(float u, float v) { delegate.uv(u * uScale, v * vScale); return this; }
		@Override public VertexConsumer overlayCoords(int u, int v) { delegate.overlayCoords(u, v); return this; }
		@Override public VertexConsumer uv2(int u, int v) { delegate.uv2(u, v); return this; }
		@Override public VertexConsumer normal(float x, float y, float z) { delegate.normal(x, y, z); return this; }
		@Override public void endVertex() { delegate.endVertex(); }
		@Override public void defaultColor(int r, int g, int b, int a) { delegate.defaultColor(r, g, b, a); }
		@Override public void unsetDefaultColor() { delegate.unsetDefaultColor(); }
	}
}