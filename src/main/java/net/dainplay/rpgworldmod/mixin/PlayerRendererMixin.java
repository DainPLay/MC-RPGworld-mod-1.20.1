package net.dainplay.rpgworldmod.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.dainplay.rpgworldmod.RPGworldMod;
import net.dainplay.rpgworldmod.effect.ModEffects;
import net.dainplay.rpgworldmod.enchantment.ModEnchantments;
import net.dainplay.rpgworldmod.item.custom.MintalTriangleItem;
import net.dainplay.rpgworldmod.item.custom.NetherStarScrollItem;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerRenderer.class)
public abstract class PlayerRendererMixin {
	PlayerRenderer playerRenderer = (PlayerRenderer) (Object) this;

	@Inject(method = "renderHand", at = @At(value = "TAIL"), cancellable = true)
	private void renderHandMossiosisCheck(PoseStack pMatrixStack, MultiBufferSource pBuffer, int pCombinedLight, AbstractClientPlayer pPlayer, ModelPart pRendererArm, ModelPart pRendererArmwear, CallbackInfo ci) {
		if (!pPlayer.isInvisible()) {
			boolean isNecromancy = pPlayer.isUsingItem() && pPlayer.getUseItem().getItem() instanceof NetherStarScrollItem &&
					EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.NECROMANCY.get(), pPlayer.getUseItem()) > 0;
			float necroProgress;
			if (isNecromancy && pPlayer.getTicksUsingItem() > 0) {
				necroProgress = Math.min(30, pPlayer.getTicksUsingItem()) / 30.0F;

				boolean flash = (int) (necroProgress * 10) % 2 == 0;
				if (flash) {
					RenderSystem.setShaderColor(255F, 255F, 255F, 1.0F);
				}
			}
			VertexConsumer ivertexbuilder = pBuffer.getBuffer(RenderType.entityCutoutNoCull(new ResourceLocation(RPGworldMod.MOD_ID, "textures/entity/explosion_overlay.png")));
			pRendererArmwear.render(pMatrixStack, ivertexbuilder, pCombinedLight, OverlayTexture.NO_OVERLAY);
		}
		if (pPlayer.hasEffect(ModEffects.MOSSIOSIS.get())) {
			VertexConsumer ivertexbuilder = pBuffer.getBuffer(RenderType.entityCutoutNoCull(new ResourceLocation(RPGworldMod.MOD_ID, "textures/entity/mossiosis_overlay.png")));
			pRendererArmwear.render(pMatrixStack, ivertexbuilder, pCombinedLight, OverlayTexture.NO_OVERLAY);
		}
		RenderSystem.setShaderColor(1F, 1F, 1F, 1.0F);
	}

	@Inject(method = "renderHand", at = @At(value = "HEAD"), cancellable = true)
	private void renderHandInvisCheck(PoseStack pMatrixStack, MultiBufferSource pBuffer,
									  int pCombinedLight, AbstractClientPlayer pPlayer, ModelPart pRendererArm, ModelPart
											  pRendererArmwear, CallbackInfo ci) {
		if (pPlayer.isInvisible() && !pPlayer.hasEffect(ModEffects.MOSSIOSIS.get())) {
			ci.cancel();
		}
		boolean isNecromancy = pPlayer.isUsingItem() && pPlayer.getUseItem().getItem() instanceof NetherStarScrollItem &&
				EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.NECROMANCY.get(), pPlayer.getUseItem()) > 0;
		float necroProgress;
		if (isNecromancy && pPlayer.getTicksUsingItem() > 0) {
			necroProgress = Math.min(30, pPlayer.getTicksUsingItem()) / 30.0F;

			boolean flash = (int)(necroProgress * 10) % 2 == 0;
			if (flash) {
				RenderSystem.setShaderColor(255F, 255F, 255F, 1.0F);
			}
		}
	}


	@Inject(method = "renderHand", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/player/PlayerRenderer;setModelProperties(Lnet/minecraft/client/player/AbstractClientPlayer;)V", shift = At.Shift.AFTER, opcode = Opcodes.PUTFIELD), cancellable = true)
	private void renderHandMossiosisAndInvisCheck(PoseStack pMatrixStack, MultiBufferSource pBuffer,
												  int pCombinedLight, AbstractClientPlayer pPlayer, ModelPart pRendererArm, ModelPart
														  pRendererArmwear, CallbackInfo ci) {
		if (pPlayer.isInvisible() && pPlayer.hasEffect(ModEffects.MOSSIOSIS.get())) {
			PlayerModel<AbstractClientPlayer> playermodel = playerRenderer.getModel();
			playermodel.attackTime = 0.0F;
			playermodel.crouching = false;
			playermodel.swimAmount = 0.0F;
			pRendererArmwear.xRot = 0.0F;
			VertexConsumer ivertexbuilder = pBuffer.getBuffer(RenderType.entityCutoutNoCull(new ResourceLocation(RPGworldMod.MOD_ID, "textures/entity/mossiosis_overlay.png")));
			pRendererArmwear.render(pMatrixStack, ivertexbuilder, pCombinedLight, OverlayTexture.NO_OVERLAY);
			ci.cancel();
		}
	}

}
