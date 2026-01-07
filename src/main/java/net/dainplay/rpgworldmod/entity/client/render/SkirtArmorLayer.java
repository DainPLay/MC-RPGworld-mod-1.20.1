package net.dainplay.rpgworldmod.entity.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.SheetedDecalTextureGenerator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.dainplay.rpgworldmod.entity.client.model.SkirtModel;
import net.dainplay.rpgworldmod.item.custom.FireproofSkirtItem;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.function.Function;

public class SkirtArmorLayer<T extends LivingEntity, M extends EntityModel<T>> extends RenderLayer<T, M> {

	private final SkirtModel skirtModel;
	private final Function<LivingEntity, ItemStack> skirtItemGetter;

	public SkirtArmorLayer(RenderLayerParent<T, M> renderer, SkirtModel skirtModel) {
		super(renderer);
		this.skirtModel = skirtModel;
		this.skirtItemGetter = this::getSkirtItemStack;
	}

	public SkirtArmorLayer(RenderLayerParent<T, M> renderer, SkirtModel skirtModel,
						   Function<LivingEntity, ItemStack> skirtItemGetter) {
		super(renderer);
		this.skirtModel = skirtModel;
		this.skirtItemGetter = skirtItemGetter;
	}

	@Override
	public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight,
					   LivingEntity entity, float limbSwing, float limbSwingAmount,
					   float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {

		ItemStack skirtStack = skirtItemGetter.apply(entity);
		if (skirtStack.isEmpty() || !(skirtStack.getItem() instanceof FireproofSkirtItem))
			return;

		// НЕ копируем свойства в родительскую модель!
		// Вместо этого настраиваем анимацию только для модели юбки
		skirtModel.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);

		// Синхронизируем только необходимые части
		M parentModel = getParentModel();
		if (parentModel instanceof HumanoidModel) {
			// Копируем только вращение тела, если это необходимо для привязки
			skirtModel.body.copyFrom(((HumanoidModel<?>) parentModel).body);

			// Устанавливаем юбку в правильную позицию относительно тела
			skirtModel.body.xRot = ((HumanoidModel<?>) parentModel).body.xRot;
			skirtModel.body.yRot = ((HumanoidModel<?>) parentModel).body.yRot;
			skirtModel.body.zRot = ((HumanoidModel<?>) parentModel).body.zRot;

			// Копируем положение ног для правильной анимации (если нужно)
			skirtModel.leftLeg.copyFrom(((HumanoidModel<?>) parentModel).leftLeg);
			skirtModel.rightLeg.copyFrom(((HumanoidModel<?>) parentModel).rightLeg);
		}

		VertexConsumer vertexConsumer = buffer.getBuffer(
				skirtModel.renderType(FireproofSkirtItem.getTextureLocation())
		);

		poseStack.pushPose();

		// Применяем трансформации родительской модели для правильного позиционирования
		// Только трансформации, без копирования свойств
		if (parentModel instanceof HumanoidModel) {
			// Сохраняем текущую позу
			PoseStack.Pose last = poseStack.last();

			// Применяем трансформации тела
			poseStack.translate(0.0D, 0.0D, 0.0D);
		}

		// Корректируем позицию юбки
		poseStack.translate(0.0F, 0.0F, 0.0F);

		// Рендерим модель юбки
		skirtModel.renderToBuffer(poseStack, vertexConsumer, packedLight,
				OverlayTexture.NO_OVERLAY,
				1.0F, 1.0F, 1.0F, 1.0F);

		// Рендерим enchantment glint, если юбка зачарована
		if (skirtStack.hasFoil()) {
			VertexConsumer glintConsumer = ItemRenderer.getArmorFoilBuffer(buffer, RenderType.armorCutoutNoCull(FireproofSkirtItem.getTextureLocation()), false, skirtStack.hasFoil());
			skirtModel.renderToBuffer(poseStack, glintConsumer, packedLight, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
		}

		poseStack.popPose();
	}

	// Метод для получения ItemStack юбки
	private ItemStack getSkirtItemStack(LivingEntity entity) {
		// Проверяем слот ног (leggings) так как юбка обычно экипируется туда
		return entity.getItemBySlot(EquipmentSlot.LEGS);
	}

	// Методы для регистрации слоя на рендерах сущностей
	public static void registerOnAll(EntityRenderDispatcher renderManager, SkirtModel skirtModel) {
		for (EntityRenderer<? extends Player> renderer : renderManager.getSkinMap().values())
			registerOn(renderer, skirtModel);
		for (EntityRenderer<?> renderer : renderManager.renderers.values())
			registerOn(renderer, skirtModel);
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	public static void registerOn(EntityRenderer<?> entityRenderer, SkirtModel skirtModel) {
		if (!(entityRenderer instanceof LivingEntityRenderer<?, ?> livingRenderer))
			return;
		if (!(livingRenderer.getModel() instanceof HumanoidModel))
			return;
		SkirtArmorLayer<?, ?> layer = new SkirtArmorLayer<>(livingRenderer, skirtModel);
		livingRenderer.addLayer((SkirtArmorLayer) layer);
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	public static void registerOn(EntityRenderer<?> entityRenderer, SkirtModel skirtModel,
								  Function<LivingEntity, ItemStack> skirtItemGetter) {
		if (!(entityRenderer instanceof LivingEntityRenderer<?, ?> livingRenderer))
			return;
		if (!(livingRenderer.getModel() instanceof HumanoidModel))
			return;
		SkirtArmorLayer<?, ?> layer = new SkirtArmorLayer<>(livingRenderer, skirtModel, skirtItemGetter);
		livingRenderer.addLayer((SkirtArmorLayer) layer);
	}
}