package net.dainplay.rpgworldmod.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.dainplay.rpgworldmod.RPGworldMod;
import net.dainplay.rpgworldmod.enchantment.ModEnchantments;
import net.dainplay.rpgworldmod.item.custom.EmberScrollItem;
import net.dainplay.rpgworldmod.item.custom.ManaCostItem;
import net.dainplay.rpgworldmod.item.custom.OrbitingItem;
import net.dainplay.rpgworldmod.network.ClientManaData;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class OrbitingItemRenderer {

	@SubscribeEvent
	public static void onRenderLiving(RenderLivingEvent.Post<LivingEntity, ?> event) {
		// Рендеринг для всех существ, кроме игрока в первом лице
		if (event.getEntity() == Minecraft.getInstance().player &&
				Minecraft.getInstance().options.getCameraType().isFirstPerson()) {
			return;
		}

		LivingEntity entity = event.getEntity();
		List<ItemStack> orbitingItems = getOrbitingItems(entity);
		if (!orbitingItems.isEmpty()) {
			renderRotatingItems(event.getPoseStack(), entity, event.getPartialTick(),
					event.getMultiBufferSource(), orbitingItems);
		}
	}

	@SubscribeEvent
	public static void onRenderLevelStage(RenderLevelStageEvent event) {
		if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_ENTITIES) {
			return;
		}

		Minecraft minecraft = Minecraft.getInstance();
		Player player = minecraft.player;

		// Рендерим только для игрока в первом лице
		if (player == null || !minecraft.options.getCameraType().isFirstPerson()) {
			return;
		}

		List<ItemStack> orbitingItems = getOrbitingItems(player);
		if (orbitingItems.isEmpty()) {
			return;
		}

		// Рендерим предметы от первого лица
		renderRotatingItemsFirstPerson(event.getPoseStack(), player,
				event.getPartialTick(), Minecraft.getInstance().renderBuffers().bufferSource(), orbitingItems);
	}

	@SubscribeEvent
	public static void onRenderPlayerHand(RenderHandEvent event) {
		if (event.getItemStack().getItem() instanceof OrbitingItem item1 && item1.shouldOrbit(event.getItemStack(), Minecraft.getInstance().player)) {
			Minecraft mc = Minecraft.getInstance();
			AbstractClientPlayer player = mc.player;
			PlayerRenderer playerrenderer = (PlayerRenderer) mc.getEntityRenderDispatcher()
					.getRenderer(player);

			PoseStack ms = event.getPoseStack();
			MultiBufferSource buffer = event.getMultiBufferSource();
			int light = event.getPackedLight();

			boolean rightHand = event.getHand() == InteractionHand.MAIN_HAND ^ mc.player.getMainArm() == HumanoidArm.LEFT;
			float equipProgress = event.getEquipProgress();
			float swingProgress = event.getSwingProgress();

			// Копируем трансформации из ванильного метода
			float flip = rightHand ? 1.0F : -1.0F;
			float f1 = Mth.sqrt(swingProgress);
			float f2 = -0.3F * Mth.sin(f1 * (float) Math.PI);
			float f3 = 0.4F * Mth.sin(f1 * ((float) Math.PI * 2F));
			float f4 = -0.4F * Mth.sin(swingProgress * (float) Math.PI);

			// Первая группа трансформаций (как в ванильном методе)
			ms.pushPose();
			ms.translate(
					flip * (f2 + 0.64000005F),
					f3 + -0.6F + equipProgress * -0.6F,
					f4 + -0.71999997F
			);
			ms.mulPose(Axis.YP.rotationDegrees(flip * 45.0F));

			float f5 = Mth.sin(swingProgress * swingProgress * (float) Math.PI);
			float f6 = Mth.sin(f1 * (float) Math.PI);
			ms.mulPose(Axis.YP.rotationDegrees(flip * f6 * 70.0F));
			ms.mulPose(Axis.ZP.rotationDegrees(flip * f5 * -20.0F));

			// Вторая группа трансформаций (как в ванильном методе)
			ms.translate(flip * -1.0F, 3.6F, 3.5F);
			ms.mulPose(Axis.ZP.rotationDegrees(flip * 120.0F));
			ms.mulPose(Axis.XP.rotationDegrees(200.0F));
			ms.mulPose(Axis.YP.rotationDegrees(flip * -135.0F));
			ms.translate(flip * 5.6F, 0.0F, 0.0F);



			if (player.isUsingItem() && player.getUseItemRemainingTicks() > 0 && player.getUsedItemHand() == event.getHand()) {
				ms = item1.getUsingPose(event.getItemStack(), player, ms, flip);
			}
			// Рендерим руку
			if (rightHand) {
				playerrenderer.renderRightHand(ms, buffer, light, player);
			} else {
				playerrenderer.renderLeftHand(ms, buffer, light, player);
			}
			ms.popPose();

			// Получаем предмет и его текстуру
			OrbitingItem item = (OrbitingItem) event.getItemStack().getItem();
			String textureString = item.getTexture(event.getItemStack(), player);
			int animationSpeed = item.getAnimationSpeed(event.getItemStack(), player);
			int animationLength = item.getAnimationLength(event.getItemStack(), player);
			int manacost = 0;
			if (item instanceof ManaCostItem spell) manacost = spell.getManaCost(event.getItemStack(), player);

			// Рендерим текстуру или цветной квадрат позади руки
			ms.pushPose();
			// Применяем те же трансформации, что и для руки
			ms.translate(
					flip * (f2 + 0.64000005F),
					f3 + -0.6F + equipProgress * -0.6F,
					f4 + -0.71999997F
			);

			// Смещаем квадрат немного назад
			ms.translate(flip * item.get1XOffset(event.getItemStack(), player), item.get1YOffset(event.getItemStack(), player), item.get1ZOffset(event.getItemStack(), player));
			if (player.isUsingItem() && player.getUseItemRemainingTicks() > 0 && player.getUsedItemHand() == event.getHand()) {
				ms = item1.getEffectUsingPose(event.getItemStack(), player, ms, flip);
			}
			VertexConsumer vertexconsumer;
			float size = 0.5F;
			Matrix4f matrix4f = ms.last().pose();
			if (ClientManaData.get() >= manacost || player.getAbilities().instabuild) {
				if (textureString != null && !textureString.isEmpty()) {
					// Используем вертикальный спрайтшит (sprite sheet)
					int currentFrame = (player.tickCount / animationSpeed) % animationLength;
					float frameHeight = 1.0F / animationLength; // Высота одного кадра в UV координатах
					float vMin = currentFrame * frameHeight; // Начало текущего кадра по V
					float vMax = vMin + frameHeight; // Конец текущего кадра по V

					// Загружаем единую текстуру спрайтшита
					ResourceLocation texture = new ResourceLocation(RPGworldMod.MOD_ID, textureString + ".png");

					// ВТОРОЙ СЛОЙ: entityTranslucentEmissive (рендерится поверх)
					vertexconsumer = buffer.getBuffer(RenderType.entityTranslucentEmissive(texture));

					// Рендерим текстурированный квадрат с правильными UV координатами
					// Нижний левый
					vertexconsumer.vertex(matrix4f, -size, -size, 0.0F)
							.color(1.0F, 1.0F, 1.0F, 1F)  // Полупрозрачность 60%
							.uv(0.0F, vMax) // V теперь зависит от кадра
							.overlayCoords(OverlayTexture.NO_OVERLAY)
							.uv2(15728880) // Используем переданный свет вместо 15728880
							.normal(0.0F, 0.0F, 1.0F)
							.endVertex();

					// Нижний правый
					vertexconsumer.vertex(matrix4f, size, -size, 0.0F)
							.color(1.0F, 1.0F, 1.0F, 1F)
							.uv(1.0F, vMax) // V теперь зависит от кадра
							.overlayCoords(OverlayTexture.NO_OVERLAY)
							.uv2(15728880)
							.normal(0.0F, 0.0F, 1.0F)
							.endVertex();

					// Верхний правый
					vertexconsumer.vertex(matrix4f, size, size, 0.0F)
							.color(1.0F, 1.0F, 1.0F, 1F)
							.uv(1.0F, vMin) // V теперь зависит от кадра
							.overlayCoords(OverlayTexture.NO_OVERLAY)
							.uv2(15728880)
							.normal(0.0F, 0.0F, 1.0F)
							.endVertex();

					// Верхний левый
					vertexconsumer.vertex(matrix4f, -size, size, 0.0F)
							.color(1.0F, 1.0F, 1.0F, 1F)
							.uv(0.0F, vMin) // V теперь зависит от кадра
							.overlayCoords(OverlayTexture.NO_OVERLAY)
							.uv2(15728880)
							.normal(0.0F, 0.0F, 1.0F)
							.endVertex();

					// ПЕРВЫЙ СЛОЙ: eyes (рендерится первым)
					VertexConsumer eyesVertexConsumer = buffer.getBuffer(RenderType.eyes(texture));

					// Рендерим текстурированный квадрат с правильными UV координатами для eyes
					// Нижний левый
					eyesVertexConsumer.vertex(matrix4f, -size, -size, 0.0F)
							.color(1.0F, 1.0F, 1.0F, 1.0F)
							.uv(0.0F, vMax)
							.overlayCoords(OverlayTexture.NO_OVERLAY)
							.uv2(15728880)
							.normal(0.0F, 0.0F, 1.0F)
							.endVertex();

					// Нижний правый
					eyesVertexConsumer.vertex(matrix4f, size, -size, 0.0F)
							.color(1.0F, 1.0F, 1.0F, 1.0F)
							.uv(1.0F, vMax)
							.overlayCoords(OverlayTexture.NO_OVERLAY)
							.uv2(15728880)
							.normal(0.0F, 0.0F, 1.0F)
							.endVertex();

					// Верхний правый
					eyesVertexConsumer.vertex(matrix4f, size, size, 0.0F)
							.color(1.0F, 1.0F, 1.0F, 1.0F)
							.uv(1.0F, vMin)
							.overlayCoords(OverlayTexture.NO_OVERLAY)
							.uv2(15728880)
							.normal(0.0F, 0.0F, 1.0F)
							.endVertex();

					// Верхний левый
					eyesVertexConsumer.vertex(matrix4f, -size, size, 0.0F)
							.color(1.0F, 1.0F, 1.0F, 1.0F)
							.uv(0.0F, vMin)
							.overlayCoords(OverlayTexture.NO_OVERLAY)
							.uv2(15728880)
							.normal(0.0F, 0.0F, 1.0F)
							.endVertex();
				} else {
					// Рендерим цветной квадрат (старый вариант)
					int color = item.getColor(event.getItemStack(), player);
					int alpha = 150;
					int red = (color >> 16) & 0xFF;
					int green = (color >> 8) & 0xFF;
					int blue = color & 0xFF;

					vertexconsumer = buffer.getBuffer(RenderType.lightning());

					// Второй треугольник квадрата
					vertexconsumer.vertex(matrix4f, size, size, 0.0F)
							.color(red, green, blue, alpha)
							.endVertex();
					vertexconsumer.vertex(matrix4f, -size, size, 0.0F)
							.color(red, green, blue, alpha)
							.endVertex();
					vertexconsumer.vertex(matrix4f, -size, -size, 0.0F)
							.color(red, green, blue, alpha)
							.endVertex();
					vertexconsumer.vertex(matrix4f, size, -size, 0.0F)
							.color(red, green, blue, alpha)
							.endVertex();
				}
			}

			ms.popPose();

			event.setCanceled(true);
		}
	}

	private static List<ItemStack> getOrbitingItems(LivingEntity entity) {
		List<ItemStack> items = new ArrayList<>();

		ItemStack mainHand = entity.getMainHandItem();
		if (mainHand.getItem() instanceof OrbitingItem item && item.shouldOrbit(mainHand, entity)) {
			items.add(mainHand);
		}

		ItemStack offHand = entity.getOffhandItem();
		if (offHand.getItem() instanceof OrbitingItem item && item.shouldOrbit(offHand, entity)) {
			items.add(offHand);
		}

		return items;
	}

	private static void renderRotatingItemsFirstPerson(PoseStack poseStack, Player player,
													   float partialTick,
													   MultiBufferSource buffer, List<ItemStack> orbitingItems) {
		if (orbitingItems.isEmpty()) return;

		Minecraft minecraft = Minecraft.getInstance();
		ItemRenderer itemRenderer = minecraft.getItemRenderer();

		// Получаем интерполированную позицию камеры (глаз игрока)
		Camera camera = minecraft.gameRenderer.getMainCamera();
		Vec3 cameraPos = camera.getPosition();

		// Интерполируем позицию игрока для плавности
		double playerX = player.xOld + (player.getX() - player.xOld) * partialTick;
		double playerY = player.yOld + (player.getY() - player.yOld) * partialTick;
		double playerZ = player.zOld + (player.getZ() - player.zOld) * partialTick;

		// Параметры вращения
		float radius = 0.75F;
		float rotationSpeed = 0.05F;
		float baseAngle = (player.tickCount + partialTick) * rotationSpeed;

		for (int i = 0; i < orbitingItems.size(); i++) {
			ItemStack itemToRender = orbitingItems.get(i);

			// Рассчитываем угол для каждого предмета
			float angleOffset = (float) (Math.PI * 2 * i / orbitingItems.size());
			float angle = baseAngle + angleOffset;

			// Вычисляем позицию на окружности вокруг игрока
			float orbitX = (float) (playerX + radius * Math.cos(angle));
			float orbitY = (float) (playerY + player.getBbHeight() * 0.5f);
			float orbitZ = (float) (playerZ + radius * Math.sin(angle));

			poseStack.pushPose();

			// Перемещаем в мировые координаты относительно камеры
			poseStack.translate(orbitX - cameraPos.x,
					orbitY - cameraPos.y,
					orbitZ - cameraPos.z);

			poseStack.mulPose(Axis.YP.rotationDegrees(angle * 50));
			poseStack.scale(0.5F, 0.5F, 0.5F);

			itemRenderer.renderStatic(
					itemToRender,
					ItemDisplayContext.FIXED,
					15728880,
					OverlayTexture.NO_OVERLAY,
					poseStack,
					buffer,
					minecraft.level,
					0
			);

			poseStack.popPose();
		}
	}

	private static void renderRotatingItems(PoseStack poseStack, LivingEntity entity, float partialTick,
											MultiBufferSource buffer, List<ItemStack> orbitingItems) {
		if (orbitingItems.isEmpty()) return;

		Minecraft minecraft = Minecraft.getInstance();
		ItemRenderer itemRenderer = minecraft.getItemRenderer();

		// Получаем позицию существа (с учётом частичного тика для плавности)
		double x = entity.xOld + (entity.getX() - entity.xOld) * partialTick;
		double y = entity.yOld + (entity.getY() - entity.yOld) * partialTick; // Центр существа
		double z = entity.zOld + (entity.getZ() - entity.zOld) * partialTick;

		// Параметры вращения
		float radius = 0.75F; // Радиус орбиты
		float rotationSpeed = 0.05F; // Скорость вращения
		float baseAngle = (entity.tickCount + partialTick) * rotationSpeed; // Базовый угол на основе игрового времени

		for (int i = 0; i < orbitingItems.size(); i++) {
			ItemStack itemToRender = orbitingItems.get(i);

			// Рассчитываем угол для каждого предмета
			float angleOffset = (float) (Math.PI * 2 * i / orbitingItems.size());
			float angle = baseAngle + angleOffset;

			// Вычисляем позицию на окружности
			float orbitX = (float) (x + radius * Math.cos(angle));
			float orbitY = (float) y;
			float orbitZ = (float) (z + radius * Math.sin(angle));

			// Настраиваем матрицу для рендеринга
			poseStack.pushPose();
			poseStack.translate(orbitX - x, orbitY - y + entity.getBbHeight() / 2, orbitZ - z); // Перемещаем к точке на орбите

			// Вращение предмета вокруг своей оси
			poseStack.mulPose(Axis.YP.rotationDegrees(angle * 50)); // Вращение вокруг своей оси

			// Масштаб предмета (при необходимости)
			poseStack.scale(0.5F, 0.5F, 0.5F);

			// Рендеринг предмета
			itemRenderer.renderStatic(
					itemToRender,
					ItemDisplayContext.FIXED, // Контекст отображения
					15728880, // Свет
					OverlayTexture.NO_OVERLAY,
					poseStack,
					buffer,
					minecraft.level,
					0
			);

			poseStack.popPose();
		}
	}
}