package net.dainplay.rpgworldmod.mixin;

import net.dainplay.rpgworldmod.effect.ModEffects;
import net.dainplay.rpgworldmod.entity.custom.MosquitoSwarm;
import net.dainplay.rpgworldmod.entity.custom.Razorleaf;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ThrownPotion.class)
public abstract class ThrownPotionMixin {

    @Inject(method = "applyWater", at = @At("TAIL"))
    private void isMosquitoSwarmOrHasMosquitoEffect(CallbackInfo ci) {
        ThrownPotion potion = (ThrownPotion) (Object) this;
        AABB aabb = potion.getBoundingBox().inflate(4.0D, 2.0D, 4.0D);

        for(LivingEntity livingentity : potion.level().getEntitiesOfClass(LivingEntity.class, aabb,
                (entity) -> entity.hasEffect(ModEffects.MOSQUITOING.get()))) {
            double d0 = potion.distanceToSqr(livingentity);
            if (d0 < 16.0D) {
                MosquitoSwarm.spawnBlock(livingentity,livingentity.getEffect(ModEffects.MOSQUITOING.get()).getAmplifier());
                livingentity.removeEffect(ModEffects.MOSQUITOING.get());
            }
        }

        for(MosquitoSwarm mosquitoSwarm : potion.level().getEntitiesOfClass(MosquitoSwarm.class, aabb)) {
            mosquitoSwarm.transformIntoBlock(mosquitoSwarm.getSize());
        }

        for(Razorleaf razorleaf : potion.level().getEntitiesOfClass(Razorleaf.class, aabb)) {
            razorleaf.turnToBlock();
        }
    }
}