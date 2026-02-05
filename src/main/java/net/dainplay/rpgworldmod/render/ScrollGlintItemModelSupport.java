package net.dainplay.rpgworldmod.render;

import net.dainplay.rpgworldmod.enchantment.ModEnchantments;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.nbt.Tag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;
import org.checkerframework.checker.optional.qual.OptionalBottom;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ScrollGlintItemModelSupport implements BakedModel {
	private final BakedModel originalModel;

	public ScrollGlintItemModelSupport(BakedModel originalModel) {

		this.originalModel = originalModel;
	}

	@Override
	@NotNull
	public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @NotNull RandomSource rand, @NotNull ModelData data, @Nullable RenderType renderType) {
		return originalModel.getQuads(state, side, rand, data, renderType);
	}

	@Override
	public List<BakedQuad> getQuads(@Nullable BlockState pState, @Nullable Direction pDirection, RandomSource pRandom) {
		return originalModel.getQuads(pState, pDirection, pRandom);
	}

	@Override
	public boolean useAmbientOcclusion() {
		return originalModel.useAmbientOcclusion();
	}

	@Override
	public boolean isGui3d() {
		return originalModel.isGui3d();
	}

	@Override
	public boolean usesBlockLight() {
		return originalModel.usesBlockLight();
	}

	@Override
	public boolean isCustomRenderer() {
		return originalModel.isCustomRenderer();
	}

	@Override
	public TextureAtlasSprite getParticleIcon() {
		return originalModel.getParticleIcon();
	}

	@Override
	public ItemOverrides getOverrides() {
		return originalModel.getOverrides();
	}

	@Override
	public ItemTransforms getTransforms() {
		return originalModel.getTransforms();
	}

	@Override
	public List<RenderType> getRenderTypes(ItemStack stack, boolean fabulous) {
		List<RenderType> renderTypes = new ArrayList<>();

		if (stack.getTag() != null && stack.getTag().contains("SummonedObject", Tag.TAG_INT)) {
			renderTypes.add(ModRenderTypes.getItemEntityTranslucentCull(originalModel.getParticleIcon().atlasLocation()));
			renderTypes.add(ModRenderTypes.SUMMONED_GLINT);
			renderTypes.add(ModRenderTypes.SUMMONED_GLINT);
		} else if (stack.getEnchantmentLevel(ModEnchantments.ALTERATION.get()) > 0) {
			renderTypes.add(ModRenderTypes.getItemEntityTranslucentCull(originalModel.getParticleIcon().atlasLocation()));
			renderTypes.add(ModRenderTypes.ALTERATION_GLINT);
			renderTypes.add(ModRenderTypes.ALTERATION_GLINT);
		} else if (stack.getEnchantmentLevel(ModEnchantments.DESTRUCTION.get()) > 0) {
			renderTypes.add(ModRenderTypes.getItemEntityTranslucentCull(originalModel.getParticleIcon().atlasLocation()));
			renderTypes.add(ModRenderTypes.DESTRUCTION_GLINT);
			renderTypes.add(ModRenderTypes.DESTRUCTION_GLINT);
		} else if (stack.getEnchantmentLevel(ModEnchantments.RESTORATION.get()) > 0) {
			renderTypes.add(ModRenderTypes.getItemEntityTranslucentCull(originalModel.getParticleIcon().atlasLocation()));
			renderTypes.add(ModRenderTypes.RESTORATION_GLINT);
			renderTypes.add(ModRenderTypes.RESTORATION_GLINT);
		} else if (stack.getEnchantmentLevel(ModEnchantments.ILLUSION.get()) > 0) {
			renderTypes.add(ModRenderTypes.getItemEntityTranslucentCull(originalModel.getParticleIcon().atlasLocation()));
			renderTypes.add(ModRenderTypes.ILLUSION_GLINT);
			renderTypes.add(ModRenderTypes.ILLUSION_GLINT);
		} else if (stack.getEnchantmentLevel(ModEnchantments.CONJURATION.get()) > 0) {
			renderTypes.add(ModRenderTypes.getItemEntityTranslucentCull(originalModel.getParticleIcon().atlasLocation()));
			renderTypes.add(ModRenderTypes.CONJURATION_GLINT);
			renderTypes.add(ModRenderTypes.CONJURATION_GLINT);
		} else if (stack.getEnchantmentLevel(ModEnchantments.NECROMANCY.get()) > 0) {
			renderTypes.add(ModRenderTypes.getItemEntityTranslucentCull(originalModel.getParticleIcon().atlasLocation()));
			renderTypes.add(ModRenderTypes.NECROMANCY_GLINT);
		} else
			return originalModel.getRenderTypes(stack, fabulous);

		return renderTypes;
	}
}