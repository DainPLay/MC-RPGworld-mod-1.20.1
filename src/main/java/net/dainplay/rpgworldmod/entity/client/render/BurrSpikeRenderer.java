package net.dainplay.rpgworldmod.entity.client.render;

import net.dainplay.rpgworldmod.RPGworldMod;
import net.dainplay.rpgworldmod.entity.projectile.BurrSpikeEntity;
import net.dainplay.rpgworldmod.entity.projectile.FairapierSeedEntity;
import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class BurrSpikeRenderer extends ArrowRenderer<BurrSpikeEntity> {
    public static final ResourceLocation TEXTURE = new ResourceLocation(RPGworldMod.MOD_ID, "textures/entity/burr_spike.png");

    public BurrSpikeRenderer(EntityRendererProvider.Context manager) {
        super(manager);
    }

    public ResourceLocation getTextureLocation(BurrSpikeEntity arrow) {
        return TEXTURE;
    }
}