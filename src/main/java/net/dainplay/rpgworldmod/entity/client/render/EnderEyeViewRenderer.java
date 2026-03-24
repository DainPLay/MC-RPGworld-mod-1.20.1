package net.dainplay.rpgworldmod.entity.client.render;

import net.dainplay.rpgworldmod.entity.custom.EnderEyeViewEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;

public class EnderEyeViewRenderer extends ThrownItemRenderer<EnderEyeViewEntity> {
	public EnderEyeViewRenderer(EntityRendererProvider.Context pContext) {
		super(pContext, 1.0f, true);
	}
}