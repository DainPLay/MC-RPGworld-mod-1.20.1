package net.dainplay.rpgworldmod.entity.client.model;

import net.dainplay.rpgworldmod.RPGworldMod;
import net.dainplay.rpgworldmod.entity.custom.Bibbit;
import net.dainplay.rpgworldmod.entity.custom.Razorleaf;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

public class RazorleafHeartLayer<T extends LivingEntity> extends ExtinguishedHeartLayer<Razorleaf, RazorleafModel> {
        private static final RenderType RAZORLEAF_HEART = RenderType.eyes(new ResourceLocation(RPGworldMod.MOD_ID, "textures/entity/razorleaf/heart.png"));

        public RazorleafHeartLayer(RenderLayerParent<Razorleaf, RazorleafModel> p_116964_) {
            super(p_116964_);
        }

        public RenderType renderType() {
            return RAZORLEAF_HEART;
        }
}
