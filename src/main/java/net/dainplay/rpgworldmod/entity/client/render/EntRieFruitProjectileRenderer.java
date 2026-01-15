package net.dainplay.rpgworldmod.entity.client.render;

import net.dainplay.rpgworldmod.entity.projectile.EntRieFruitProjectile;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;

public class EntRieFruitProjectileRenderer extends ThrownItemRenderer<EntRieFruitProjectile> {

	public EntRieFruitProjectileRenderer(EntityRendererProvider.Context context) {
		super(context, 1.0f, true);
	}
}