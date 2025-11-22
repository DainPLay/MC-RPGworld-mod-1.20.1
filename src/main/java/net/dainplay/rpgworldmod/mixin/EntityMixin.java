package net.dainplay.rpgworldmod.mixin;

import net.dainplay.rpgworldmod.effect.ModEffects;
import net.dainplay.rpgworldmod.effect.ParalysisEffect;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.LookControl;
import net.minecraft.world.entity.animal.horse.SkeletonHorse;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(Entity.class)
public abstract class EntityMixin {
    Entity entity = (Entity) (Object) this;
    @Inject(method = "setYRot", at = @At(value = "HEAD"), cancellable = true)
    private void setYRotParalysisCheck(CallbackInfo ci) {
        if (!(entity instanceof AbstractSkeleton) && !(entity instanceof SkeletonHorse) && entity instanceof LivingEntity
                && ((LivingEntity) entity).hasEffect(ModEffects.PARALYSIS.get()) && EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(entity) && ((LivingEntity) entity).getEffect(ModEffects.PARALYSIS.get()).getAmplifier() >= 1)
            ci.cancel();
    }


    @Unique
    private boolean wasCalledFrom(String method) {
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();

        // Traversing the call stack to find the calling method
        for (StackTraceElement element : stackTraceElements) {
            if (element.getMethodName().equals(method)) {
                return true; // The method was called from load method
            }
        }
        return false; // The method was not called from load method
    }

    @Inject(method = "setXRot", at = @At(value = "HEAD"), cancellable = true)
    private void setXRotParalysisCheck(CallbackInfo ci) {
        if (!(entity instanceof AbstractSkeleton) && !(entity instanceof SkeletonHorse) && entity instanceof LivingEntity
                && ((LivingEntity) entity).hasEffect(ModEffects.PARALYSIS.get()) && EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(entity) && ((LivingEntity) entity).getEffect(ModEffects.PARALYSIS.get()).getAmplifier() >= 1)
            ci.cancel();
    }
}
