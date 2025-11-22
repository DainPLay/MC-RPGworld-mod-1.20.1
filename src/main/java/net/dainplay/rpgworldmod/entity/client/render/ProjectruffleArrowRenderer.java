package net.dainplay.rpgworldmod.entity.client.render;

import net.dainplay.rpgworldmod.RPGworldMod;
import net.dainplay.rpgworldmod.entity.projectile.ProjectruffleArrowEntity;
import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class ProjectruffleArrowRenderer extends ArrowRenderer<ProjectruffleArrowEntity> {
    public static final ResourceLocation TEXTURE = new ResourceLocation(RPGworldMod.MOD_ID, "textures/entity/projectruffle_arrow.png");

    public ProjectruffleArrowRenderer(EntityRendererProvider.Context manager) {
        super(manager);
    }

    public ResourceLocation getTextureLocation(ProjectruffleArrowEntity arrow) {
        return TEXTURE;
    }
}