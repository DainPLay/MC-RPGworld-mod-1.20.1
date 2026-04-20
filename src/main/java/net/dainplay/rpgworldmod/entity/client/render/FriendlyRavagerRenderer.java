package net.dainplay.rpgworldmod.entity.client.render;

import net.dainplay.rpgworldmod.RPGworldMod;
import net.dainplay.rpgworldmod.entity.custom.FriendlyRavager;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.RavagerRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Ravager;

public class FriendlyRavagerRenderer extends RavagerRenderer {
	private static final ResourceLocation UNSADDLED_TEXTURE_LOCATION = new ResourceLocation(RPGworldMod.MOD_ID, "textures/entity/illager/unsaddled_ravager.png");

	public FriendlyRavagerRenderer(EntityRendererProvider.Context context) {
		super(context);
	}

	@Override
	public ResourceLocation getTextureLocation(Ravager entity) {
		if (entity instanceof FriendlyRavager friendly && friendly.isSaddled()) {
			return super.getTextureLocation(entity);
		}
		return UNSADDLED_TEXTURE_LOCATION;
	}
}