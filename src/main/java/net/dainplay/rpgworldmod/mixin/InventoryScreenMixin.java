package net.dainplay.rpgworldmod.mixin;

import net.dainplay.rpgworldmod.effect.ModEffects;
import net.dainplay.rpgworldmod.entity.custom.Mintobat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.Ocelot;
import net.minecraft.world.entity.animal.horse.SkeletonHorse;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InventoryScreen.class)
public abstract class InventoryScreenMixin {

    @Inject(method = "renderEntityInInventoryFollowsMouse", at = @At(value = "HEAD"), cancellable = true)
    private static void renderEntityInInventoryFollowsMouseParalysisCheck(GuiGraphics pGuiGraphics, int pX, int pY, int pScale, float angleXComponent, float angleYComponent, LivingEntity pEntity, CallbackInfo ci) {
        if (!(pEntity instanceof AbstractSkeleton) && !(pEntity instanceof SkeletonHorse) && EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(pEntity)
                && pEntity.hasEffect(ModEffects.PARALYSIS.get()) && pEntity.getEffect(ModEffects.PARALYSIS.get()).getAmplifier() >= 1) {
            Quaternionf quaternionf = (new Quaternionf()).rotateZ((float) Math.PI);
            Quaternionf quaternionf1 = (new Quaternionf()).rotateX(angleYComponent * 20.0F * ((float) Math.PI / 180F));
            InventoryScreen.renderEntityInInventory(pGuiGraphics, pX, pY, pScale, quaternionf, quaternionf1, pEntity);
            ci.cancel();
        }
    }
}
