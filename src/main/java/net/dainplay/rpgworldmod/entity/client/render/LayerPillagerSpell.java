package net.dainplay.rpgworldmod.entity.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.dainplay.rpgworldmod.RPGworldMod;
import net.dainplay.rpgworldmod.item.custom.PillagerScrollItem;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;

public class LayerPillagerSpell extends RenderLayer {
	private RenderLayerParent parent;

	public LayerPillagerSpell(RenderLayerParent parent) {
		super(parent);
		this.parent = parent;
	}

	@Override
	public void render(PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn, Entity entity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
		if (entity instanceof Player player && !player.isInvisible()) {
			HumanoidArm mainArm = player.getMainArm();
			boolean mainLeftHand = mainArm == HumanoidArm.LEFT;
			boolean isSlim = false;
			if (player instanceof AbstractClientPlayer abstractClientPlayer)
				isSlim = "slim".equals(abstractClientPlayer.getModelName());

			boolean isPillagerScrollLeft = player.isUsingItem()
					&& player.getUseItem().getItem() instanceof PillagerScrollItem scroll
					&& scroll.hasAnyEnchant(player.getUseItem())
					&& player.getUsedItemHand() == (mainLeftHand ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND);

			boolean isPillagerScrollRight = player.isUsingItem()
					&& player.getUseItem().getItem() instanceof PillagerScrollItem scroll
					&& scroll.hasAnyEnchant(player.getUseItem())
					&& player.getUsedItemHand() == (mainLeftHand ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND);

			float time = (entity.tickCount + partialTicks) * 0.3f;
			float pulse = (float) ((Math.sin(time) + 1.0) / 2.0);
			float brightness = 0.6f + pulse * 0.4f;

			if (isPillagerScrollRight) {
				VertexConsumer ivertexbuilder = bufferIn.getBuffer(RenderType.energySwirl(
						new ResourceLocation(RPGworldMod.MOD_ID, "textures/entity/pillager_spell_usage_right" + (isSlim ? "_slim.png" : ".png")),
						0F, 0F));
				matrixStackIn.pushPose();
				this.getParentModel().renderToBuffer(matrixStackIn, ivertexbuilder, packedLightIn,
						LivingEntityRenderer.getOverlayCoords(player, 0),
						brightness, brightness, brightness, 1.0F);
				matrixStackIn.popPose();
			} else if (isPillagerScrollLeft) {
				VertexConsumer ivertexbuilder = bufferIn.getBuffer(RenderType.energySwirl(
						new ResourceLocation(RPGworldMod.MOD_ID, "textures/entity/pillager_spell_usage_left" + (isSlim ? "_slim.png" : ".png")),
						0F, 0F));
				matrixStackIn.pushPose();
				this.getParentModel().renderToBuffer(matrixStackIn, ivertexbuilder, packedLightIn,
						LivingEntityRenderer.getOverlayCoords(player, 0),
						brightness, brightness, brightness, 1.0F);
				matrixStackIn.popPose();
			}
		}
	}
}