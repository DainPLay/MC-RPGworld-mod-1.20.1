package net.dainplay.rpgworldmod.mixin;

import net.dainplay.rpgworldmod.effect.ModEffects;
import net.dainplay.rpgworldmod.enchantment.ModEnchantments;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.util.Unit;
import net.minecraft.world.damagesource.CombatRules;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.behavior.warden.SonicBoom;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.animal.horse.SkeletonHorse;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SonicBoom.class)
public abstract class SonicBoomMixin {

    @Inject(method = "tick(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/monster/warden/Warden;J)V", at = @At(value = "HEAD"), cancellable = true)
    private void setYRotParalysisCheck(ServerLevel pLevel, Warden pOwner, long pGameTime, CallbackInfo ci) {
        pOwner.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).ifPresent((p_289393_) -> {
            pOwner.getLookControl().setLookAt(p_289393_.position());
        });
        if (!pOwner.getBrain().hasMemoryValue(MemoryModuleType.SONIC_BOOM_SOUND_DELAY) && !pOwner.getBrain().hasMemoryValue(MemoryModuleType.SONIC_BOOM_SOUND_COOLDOWN)) {
            pOwner.getBrain().setMemoryWithExpiry(MemoryModuleType.SONIC_BOOM_SOUND_COOLDOWN, Unit.INSTANCE, (long)(Mth.ceil(60.0F) - Mth.ceil(34.0D)));
            pOwner.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).filter(pOwner::canTargetEntity).filter((p_217707_) -> {
                return pOwner.closerThan(p_217707_, 15.0D, 20.0D);
            }).ifPresent((p_217704_) -> {
                Vec3 vec3 = pOwner.position().add(0.0D, (double)1.6F, 0.0D);
                Vec3 vec31 = p_217704_.getEyePosition().subtract(vec3);
                Vec3 vec32 = vec31.normalize();

                for(int i = 1; i < Mth.floor(vec31.length()) + 7; ++i) {
                    Vec3 vec33 = vec3.add(vec32.scale((double)i));
                    pLevel.sendParticles(ParticleTypes.SONIC_BOOM, vec33.x, vec33.y, vec33.z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
                }

                pOwner.playSound(SoundEvents.WARDEN_SONIC_BOOM, 3.0F, 1.0F);
                float damage = 10F;
                if(EnchantmentHelper.getEnchantmentLevel(ModEnchantments.SOUNDPROOF.get(), p_217704_)>0) damage = CombatRules.getDamageAfterMagicAbsorb(10F, 12);
                p_217704_.hurt(pLevel.damageSources().sonicBoom(pOwner), damage);
                double d1 = 0.5D * (1.0D - p_217704_.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE));
                double d0 = 2.5D * (1.0D - p_217704_.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE));
                p_217704_.push(vec32.x() * d0, vec32.y() * d1, vec32.z() * d0);
            });
        }
    }
}
