package net.dainplay.rpgworldmod.mixin;

import net.minecraft.client.particle.MobAppearanceParticle;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(MobAppearanceParticle.class)
public class MobAppearanceParticleMixin {

    @Unique
    public boolean shouldCull() {
        return false;
    }
}