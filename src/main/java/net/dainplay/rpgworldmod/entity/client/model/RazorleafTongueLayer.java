package net.dainplay.rpgworldmod.entity.client.model;

import net.dainplay.rpgworldmod.RPGworldMod;
import net.dainplay.rpgworldmod.entity.custom.Razorleaf;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

public class RazorleafTongueLayer<T extends LivingEntity> extends TongueLayer<Razorleaf, RazorleafModel> {
	public static final ResourceLocation TEXTURE_EXTINGUISHED_OVERLAY = new ResourceLocation(RPGworldMod.MOD_ID, "textures/entity/razorleaf/razorleaf_extinguished.png");

	public RazorleafTongueLayer(RenderLayerParent<Razorleaf, RazorleafModel> p_116964_) {
		super(p_116964_);
	}

	@Override
	public ResourceLocation getTextureLocation(Razorleaf pEntity) {
		if (pEntity.getState() == Razorleaf.State.EXTINGUISHED) return TEXTURE_EXTINGUISHED_OVERLAY;
		return new ResourceLocation(RPGworldMod.MOD_ID, "textures/entity/razorleaf/tongue_" + (pEntity.tickCount % 16 + 1) + ".png");
	}
}
