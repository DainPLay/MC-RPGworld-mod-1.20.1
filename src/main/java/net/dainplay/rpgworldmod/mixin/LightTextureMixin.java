package net.dainplay.rpgworldmod.mixin;

import net.dainplay.rpgworldmod.util.ClientEyeViewHandler;
import net.dainplay.rpgworldmod.util.RemoteOpenContainerRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.ContainerOpenersCounter;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LightTexture.class)
public abstract class LightTextureMixin {
	@Inject(
			method = {"getBrightness"},
			cancellable = true,
			at = @At(value = "HEAD")
	)
	private static void ac_getBrightness(DimensionType pDimensionType, int pLightLevel, CallbackInfoReturnable<Float> cir) {
		if (ClientEyeViewHandler.isActive()) {
			cir.setReturnValue(Mth.lerp(pDimensionType.ambientLight(), 1F, 1.0F));
		}
	}
}