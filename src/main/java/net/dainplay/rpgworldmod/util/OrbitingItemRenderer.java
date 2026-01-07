package net.dainplay.rpgworldmod.util;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.dainplay.rpgworldmod.RPGworldMod;
import net.dainplay.rpgworldmod.item.custom.ManaCostItem;
import net.dainplay.rpgworldmod.item.custom.OrbitingItem;
import net.dainplay.rpgworldmod.mana.ClientManaData;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

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
		if (entity.getMainHandItem().getItem() instanceof OrbitingItem ||
				entity.getOffhandItem().getItem() instanceof OrbitingItem) {
			renderRotatingItem(event.getPoseStack(), entity, event.getPartialTick(),
					event.getMultiBufferSource(), entity.getMainHandItem().getItem(), entity.getOffhandItem().getItem());
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

		if (!(player.getMainHandItem().getItem() instanceof OrbitingItem) &&
				!(player.getOffhandItem().getItem() instanceof OrbitingItem)) {
			return;
		}

		// Рендерим камень от первого лица
		renderRotatingItemFirstPerson(event.getPoseStack(), player,
				event.getPartialTick(), Minecraft.getInstance().renderBuffers().bufferSource(), player.getMainHandItem().getItem(), player.getOffhandItem().getItem());
	}

	@SubscribeEvent
	public static void onRenderPlayerHand(RenderHandEvent event) {
		if (event.getItemStack().getItem() instanceof OrbitingItem) {
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

			// Рендерим руку
			if (rightHand) {
				playerrenderer.renderRightHand(ms, buffer, light, player);
			} else {
				playerrenderer.renderLeftHand(ms, buffer, light, player);
			}
			ms.popPose();

			// Получаем предмет и его текстуру
			OrbitingItem item = (OrbitingItem) event.getItemStack().getItem();
			String textureString = item.getTexture();
			int animationSpeed = item.getAnimationSpeed();
			int animationLength = item.getAnimationLength();
			int manacost = 0;
			if (item instanceof ManaCostItem spell) manacost = spell.getManaCost();

			// Рендерим текстуру или цветной квадрат позади руки
			ms.pushPose();
			// Применяем те же трансформации, что и для руки
			ms.translate(
					flip * (f2 + 0.64000005F),
					f3 + -0.6F + equipProgress * -0.6F,
					f4 + -0.71999997F
			);

			// Смещаем квадрат немного назад
			ms.translate(rightHand ? 0.15F : -0.15F, 0.0F, -0.5F);

			VertexConsumer vertexconsumer;
			float size = 0.5F;
			Matrix4f matrix4f = ms.last().pose();
			if (ClientManaData.get() >= manacost || player.getAbilities().instabuild) {
				if (textureString != null) {
					ResourceLocation texture = new ResourceLocation(RPGworldMod.MOD_ID, textureString + "_" + player.tickCount / animationSpeed % animationLength + ".png");
					// Рендерим текстуру (как в MosquitoSwarmRenderer)
					vertexconsumer = buffer.getBuffer(RenderType.entityTranslucentEmissive(texture));

					// Нижний левый
					vertexconsumer.vertex(matrix4f, -size, -size, 0.0F)
							.color(1.0F, 1.0F, 1.0F, 1F)  // Полупрозрачность 60%
							.uv(0.0F, 1.0F)
							.overlayCoords(OverlayTexture.NO_OVERLAY)
							.uv2(15728880)
							.normal(0.0F, 0.0F, 1.0F)
							.endVertex();

					// Нижний правый
					vertexconsumer.vertex(matrix4f, size, -size, 0.0F)
							.color(1.0F, 1.0F, 1.0F, 1F)
							.uv(1.0F, 1.0F)
							.overlayCoords(OverlayTexture.NO_OVERLAY)
							.uv2(15728880)
							.normal(0.0F, 0.0F, 1.0F)
							.endVertex();

					// Верхний правый
					vertexconsumer.vertex(matrix4f, size, size, 0.0F)
							.color(1.0F, 1.0F, 1.0F, 1F)
							.uv(1.0F, 0.0F)
							.overlayCoords(OverlayTexture.NO_OVERLAY)
							.uv2(15728880)
							.normal(0.0F, 0.0F, 1.0F)
							.endVertex();

					// Верхний левый
					vertexconsumer.vertex(matrix4f, -size, size, 0.0F)
							.color(1.0F, 1.0F, 1.0F, 1F)
							.uv(0.0F, 0.0F)
							.overlayCoords(OverlayTexture.NO_OVERLAY)
							.uv2(15728880)
							.normal(0.0F, 0.0F, 1.0F)
							.endVertex();
				} else {
					// Рендерим цветной квадрат (старый вариант)
					int color = item.getColor();
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

	private static void renderRotatingItemFirstPerson(PoseStack poseStack, Player player,
													  float partialTick,
													  MultiBufferSource buffer, Item item1, Item item2) {
		Item itemToRender = item1;
		if (!(item1 instanceof OrbitingItem) && item2 instanceof OrbitingItem) itemToRender = item2;
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
		float angle = (player.tickCount + partialTick) * rotationSpeed;

		// Вычисляем позицию на окружности вокруг игрока
		float orbitX = (float) (playerX + radius * Math.cos(angle));
		float orbitY = (float) (playerY + player.getBbHeight() * 0.5f);
		float orbitZ = (float) (playerZ + radius * Math.sin(angle));

		poseStack.pushPose();

		// Перемещаем в мировые координаты относительно камеры
		poseStack.translate(orbitX - cameraPos.x,
				orbitY - cameraPos.y,
				orbitZ - cameraPos.z);

		poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(angle * 50));
		poseStack.scale(0.5F, 0.5F, 0.5F);

		itemRenderer.renderStatic(
				itemToRender.getDefaultInstance(),
				ItemDisplayContext.FIXED,
				15728880,
				0,
				poseStack,
				buffer,
				minecraft.level,
				0
		);

		poseStack.popPose();
	}

	private static void renderRotatingItem(PoseStack poseStack, LivingEntity entity, float partialTick, MultiBufferSource buffer, Item item1, Item item2) {
		Item itemToRender = item1;
		if (!(item1 instanceof OrbitingItem) && item2 instanceof OrbitingItem) itemToRender = item2;
		Minecraft minecraft = Minecraft.getInstance();
		ItemRenderer itemRenderer = minecraft.getItemRenderer();

		// Получаем позицию существа (с учётом частичного тика для плавности)
		double x = entity.xOld + (entity.getX() - entity.xOld) * partialTick;
		double y = entity.yOld + (entity.getY() - entity.yOld) * partialTick; // Центр существа
		double z = entity.zOld + (entity.getZ() - entity.zOld) * partialTick;

		// Параметры вращения
		float radius = 0.75F; // Радиус орбиты
		float rotationSpeed = 0.05F; // Скорость вращения
		float angle = (entity.tickCount + partialTick) * rotationSpeed; // Угол на основе игрового времени

		// Вычисляем позицию на окружности
		float orbitX = (float) (x + radius * Math.cos(angle));
		float orbitY = (float) y;
		float orbitZ = (float) (z + radius * Math.sin(angle));

		// Настраиваем матрицу для рендеринга
		poseStack.pushPose();
		poseStack.translate(orbitX - x, orbitY - y + entity.getBbHeight() / 2, orbitZ - z); // Перемещаем к точке на орбите

		// Вращение предмета вокруг своей оси
		poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(angle * 50)); // Вращение вокруг своей оси

		// Масштаб предмета (при необходимости)
		poseStack.scale(0.5F, 0.5F, 0.5F);

		// Рендеринг предмета
		itemRenderer.renderStatic(
				itemToRender.getDefaultInstance(),
				ItemDisplayContext.FIXED, // Контекст отображения
				15728880, // Свет
				0, // Наложение (overlay)
				poseStack,
				buffer,
				minecraft.level,
				0
		);

		poseStack.popPose();
	}
}