package net.dainplay.rpgworldmod.entity.client.model;

import net.dainplay.rpgworldmod.RPGworldMod;
import net.dainplay.rpgworldmod.entity.custom.Bibbit;
import net.minecraft.client.model.EndermanModel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

public class BibbitEyesLayer<T extends LivingEntity> extends SittingEyesLayer<Bibbit, BibbitModel<Bibbit>> {
        private static final RenderType BIBBIT_EYES = RenderType.eyes(new ResourceLocation(RPGworldMod.MOD_ID, "textures/entity/bibbit/bibbit_eyes.png"));

        public BibbitEyesLayer(RenderLayerParent<Bibbit, BibbitModel<Bibbit>> p_116964_) {
            super(p_116964_);
        }

        public RenderType renderType() {
            return BIBBIT_EYES;
        }
}
