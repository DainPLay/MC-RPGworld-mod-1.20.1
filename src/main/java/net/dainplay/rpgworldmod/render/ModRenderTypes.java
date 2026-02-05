package net.dainplay.rpgworldmod.render;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

public class ModRenderTypes extends RenderType {
	private static final ResourceLocation ALTERATION_GLINT_TEXTURE =
			new ResourceLocation("rpgworldmod", "textures/misc/alteration_glint.png");
	private static final ResourceLocation DESTRUCTION_GLINT_TEXTURE =
			new ResourceLocation("rpgworldmod", "textures/misc/destruction_glint.png");
	private static final ResourceLocation RESTORATION_GLINT_TEXTURE =
			new ResourceLocation("rpgworldmod", "textures/misc/restoration_glint.png");
	private static final ResourceLocation CONJURATION_GLINT_TEXTURE =
			new ResourceLocation("rpgworldmod", "textures/misc/conjuration_glint.png");
	private static final ResourceLocation ILLUSION_GLINT_TEXTURE =
			new ResourceLocation("rpgworldmod", "textures/misc/illusion_glint.png");
	private static final ResourceLocation NECROMANCY_GLINT_TEXTURE =
			new ResourceLocation("rpgworldmod", "textures/misc/necromancy_glint.png");
	private static final ResourceLocation SUMMONED_GLINT_TEXTURE =
			new ResourceLocation("rpgworldmod", "textures/misc/summoned_glint.png");

	private ModRenderTypes(String name, VertexFormat format, VertexFormat.Mode mode,
						   int bufferSize, boolean affectsCrumbling, boolean sortOnUpload,
						   Runnable setupState, Runnable clearState) {
		super(name, format, mode, bufferSize, affectsCrumbling, sortOnUpload, setupState, clearState);
	}

	public static final RenderType ALTERATION_GLINT = create(
			"alteration_glint",
			DefaultVertexFormat.POSITION_TEX,
			VertexFormat.Mode.QUADS,
			256,
			false,
			false,
			CompositeState.builder()
					.setShaderState(RENDERTYPE_GLINT_SHADER)
					.setTextureState(new TextureStateShard(ALTERATION_GLINT_TEXTURE, true, false))
					.setWriteMaskState(COLOR_WRITE)
					.setCullState(NO_CULL)
					.setDepthTestState(EQUAL_DEPTH_TEST)
					.setTransparencyState(GLINT_TRANSPARENCY)
					.setTexturingState(GLINT_TEXTURING)
					.createCompositeState(false)
	);

	public static final RenderType DESTRUCTION_GLINT = create(
			"destruction_glint",
			DefaultVertexFormat.POSITION_TEX,
			VertexFormat.Mode.QUADS,
			256,
			false,
			false,
			CompositeState.builder()
					.setShaderState(RENDERTYPE_GLINT_SHADER)
					.setTextureState(new TextureStateShard(DESTRUCTION_GLINT_TEXTURE, true, false))
					.setWriteMaskState(COLOR_WRITE)
					.setCullState(NO_CULL)
					.setDepthTestState(EQUAL_DEPTH_TEST)
					.setTransparencyState(GLINT_TRANSPARENCY)
					.setTexturingState(GLINT_TEXTURING)
					.createCompositeState(false)
	);

	public static final RenderType RESTORATION_GLINT = create(
			"restoration_glint",
			DefaultVertexFormat.POSITION_TEX,
			VertexFormat.Mode.QUADS,
			256,
			false,
			false,
			CompositeState.builder()
					.setShaderState(RENDERTYPE_GLINT_SHADER)
					.setTextureState(new TextureStateShard(RESTORATION_GLINT_TEXTURE, true, false))
					.setWriteMaskState(COLOR_WRITE)
					.setCullState(NO_CULL)
					.setDepthTestState(EQUAL_DEPTH_TEST)
					.setTransparencyState(GLINT_TRANSPARENCY)
					.setTexturingState(GLINT_TEXTURING)
					.createCompositeState(false)
	);

	public static final RenderType CONJURATION_GLINT = create(
			"conjuration_glint",
			DefaultVertexFormat.POSITION_TEX,
			VertexFormat.Mode.QUADS,
			256,
			false,
			false,
			CompositeState.builder()
					.setShaderState(RENDERTYPE_GLINT_SHADER)
					.setTextureState(new TextureStateShard(CONJURATION_GLINT_TEXTURE, true, false))
					.setWriteMaskState(COLOR_WRITE)
					.setCullState(NO_CULL)
					.setDepthTestState(EQUAL_DEPTH_TEST)
					.setTransparencyState(GLINT_TRANSPARENCY)
					.setTexturingState(GLINT_TEXTURING)
					.createCompositeState(false)
	);

	public static final RenderType SUMMONED_GLINT = create(
			"summoned_glint",
			DefaultVertexFormat.POSITION_TEX,
			VertexFormat.Mode.QUADS,
			256,
			false,
			false,
			CompositeState.builder()
					.setShaderState(RENDERTYPE_GLINT_SHADER)
					.setTextureState(new TextureStateShard(SUMMONED_GLINT_TEXTURE, true, false))
					.setWriteMaskState(COLOR_WRITE)
					.setCullState(NO_CULL)
					.setDepthTestState(EQUAL_DEPTH_TEST)
					.setTransparencyState(GLINT_TRANSPARENCY)
					.setTexturingState(GLINT_TEXTURING)
					.createCompositeState(false)
	);

	public static final RenderType ILLUSION_GLINT = create(
			"illusion_glint",
			DefaultVertexFormat.POSITION_TEX,
			VertexFormat.Mode.QUADS,
			256,
			false,
			false,
			CompositeState.builder()
					.setShaderState(RENDERTYPE_GLINT_SHADER)
					.setTextureState(new TextureStateShard(ILLUSION_GLINT_TEXTURE, true, false))
					.setWriteMaskState(COLOR_WRITE)
					.setCullState(NO_CULL)
					.setDepthTestState(EQUAL_DEPTH_TEST)
					.setTransparencyState(GLINT_TRANSPARENCY)
					.setTexturingState(GLINT_TEXTURING)
					.createCompositeState(false)
	);

	public static final RenderType NECROMANCY_GLINT = create(
			"necromancy_glint",
			DefaultVertexFormat.POSITION_TEX,
			VertexFormat.Mode.QUADS,
			256,
			false,
			false,
			CompositeState.builder()
					.setShaderState(RENDERTYPE_GLINT_SHADER)
					.setTextureState(new TextureStateShard(NECROMANCY_GLINT_TEXTURE, true, false))
					.setWriteMaskState(COLOR_WRITE)
					.setCullState(NO_CULL)
					.setDepthTestState(EQUAL_DEPTH_TEST)
					.setTransparencyState(TRANSLUCENT_TRANSPARENCY)
					.setTexturingState(GLINT_TEXTURING)
					.createCompositeState(false)
	);


	public static RenderType getItemEntityTranslucentCull(ResourceLocation texture) {
		return RenderType.create("item_entity_translucent_cull_fabulous_" + texture.toString(),
				DefaultVertexFormat.NEW_ENTITY,
				VertexFormat.Mode.QUADS,
				256,
				true,
				true,
				RenderType.CompositeState.builder()
						.setShaderState(RenderStateShard.RENDERTYPE_ENTITY_TRANSLUCENT_CULL_SHADER)
						.setTextureState(new RenderStateShard.TextureStateShard(texture, false, false))
						.setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
						.setCullState(RenderStateShard.CULL)
						.setLightmapState(RenderStateShard.LIGHTMAP)
						.setOverlayState(RenderStateShard.OVERLAY)
						.setWriteMaskState(RenderStateShard.COLOR_DEPTH_WRITE)
						.createCompositeState(true));
	}
}