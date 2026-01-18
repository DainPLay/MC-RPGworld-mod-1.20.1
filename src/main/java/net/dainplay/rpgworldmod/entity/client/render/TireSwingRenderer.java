package net.dainplay.rpgworldmod.entity.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.dainplay.rpgworldmod.RPGworldMod;
import net.dainplay.rpgworldmod.entity.client.model.TireSwingModel;
import net.dainplay.rpgworldmod.entity.custom.TireSwingEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.LeashFenceKnotEntity;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

public class TireSwingRenderer extends EntityRenderer<TireSwingEntity> {
	private final TireSwingModel<TireSwingEntity> model;
	private static final ResourceLocation TEXTURE = new ResourceLocation(RPGworldMod.MOD_ID, "textures/entity/tire_swing/tire_swing.png");

	public TireSwingRenderer(EntityRendererProvider.Context context) {
		super(context);
		this.model = new TireSwingModel<>(context.bakeLayer(TireSwingModel.LAYER_LOCATION));
		this.shadowRadius = 0.5F;
	}

	@Override
	public void render(TireSwingEntity entity, float entityYaw, float partialTicks,
					   PoseStack poseStack, MultiBufferSource buffer, int packedLight) {

		// Рендерим качели
		renderSwing(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);

		// Рендерим поводок, если есть привязка
		renderLeash(entity, partialTicks, poseStack, buffer);

		super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
	}

	private void renderSwing(TireSwingEntity entity, float entityYaw, float partialTicks,
							 PoseStack poseStack, MultiBufferSource buffer, int packedLight) {

		poseStack.pushPose();

		// Получаем длину веревки из данных сущности
		float ropeLength = entity.getRopeLength() + 0.6F;

		// Получаем плавный угол качания с интерполяцией
		float swingAngle = entity.getRenderSwingAngle(partialTicks);

		// Используем yaw тела пассажира
		float yaw = entity.getPassengerBodyYaw(partialTicks);

		// Применяем вращение качелей
		poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - yaw));

		// Устанавливаем точку подвеса
		poseStack.translate(0.0D, ropeLength, 0.13);

		// Применяем качание
		poseStack.mulPose(Axis.XP.rotationDegrees(swingAngle));

		// Возвращаем модель в исходное положение
		poseStack.translate(0.0D, -ropeLength, -0.13);

		// Поворачиваем модель в зависимости от угла качания
		if (entity.isOccupied()) {
			float modelRotation = entity.getModelRotationAngle(swingAngle);

			// Временно смещаемся к центру модели для поворота
			poseStack.translate(0.0D, 0.5, 0.0D);
			poseStack.mulPose(Axis.XP.rotationDegrees(modelRotation));
			poseStack.translate(0.0D, -0.5, 0.0D);
		}

		// Центрируем модель
		poseStack.translate(0.0D, 1.5D, 0.0D);

		// Переворачиваем модель
		poseStack.scale(-1.0F, -1.0F, 1.0F);

		// Настраиваем анимацию модели
		this.model.setupAnim(entity, 0, 0, entity.tickCount + partialTicks, 0, 0);

		// Получаем VertexConsumer для отрисовки
		VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.entityCutoutNoCull(this.getTextureLocation(entity)));

		// Рендерим модель
		this.model.renderToBuffer(poseStack, vertexConsumer, packedLight,
				OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);

		poseStack.popPose();
	}

	private void renderLeash(TireSwingEntity entity, float partialTicks,
							 PoseStack poseStack, MultiBufferSource buffer) {

		byte leashType = entity.getLeashType();

		if (leashType == TireSwingEntity.LEASH_TYPE_NONE) {
			return;
		}

		Vec3 leashHolderPosition = null;
		Entity leashHolder = null;

		// Получаем позицию, к которой привязаны качели
		if (leashType == TireSwingEntity.LEASH_TYPE_PLAYER) {
			leashHolder = entity.getLeashHolder();
			if (leashHolder != null) {
				leashHolderPosition = leashHolder.getRopeHoldPosition(partialTicks);
			}
		} else if (leashType == TireSwingEntity.LEASH_TYPE_FENCE_KNOT) {
			// Для узла используем специальный метод
			leashHolder = entity.getLeashHolder();
			if (leashHolder instanceof LeashFenceKnotEntity) {
				leashHolderPosition = entity.getLeashRopePosition(partialTicks);
			}
		}

		if (leashHolderPosition == null) {
			return;
		}

		// Рендерим поводок до узла/игрока
		renderCustomLeash(entity, partialTicks, poseStack, buffer, leashHolderPosition);
	}

	// Обновить метод renderCustomLeash для правильной работы с узлом:
	private void renderCustomLeash(TireSwingEntity entity, float partialTicks,
								   PoseStack poseStack, MultiBufferSource buffer, Vec3 leashHolderPosition) {

		poseStack.pushPose();

		// Копируем ванильную логику вычислений
		double d0 = (double)(Mth.lerp(partialTicks, entity.yRotO, entity.getYRot()) * ((float)Math.PI / 180F)) + (Math.PI / 2D);
		Vec3 vec31 = entity.getLeashOffset(partialTicks);
		double d1 = Math.cos(d0) * vec31.z + Math.sin(d0) * vec31.x;
		double d2 = Math.sin(d0) * vec31.z - Math.cos(d0) * vec31.x;
		double d3 = Mth.lerp((double)partialTicks, entity.xo, entity.getX()) + d1;
		double d4 = Mth.lerp((double)partialTicks, entity.yo, entity.getY()) + vec31.y;
		double d5 = Mth.lerp((double)partialTicks, entity.zo, entity.getZ()) + d2;

		poseStack.translate(d1, vec31.y, d2);

		float f = (float)(leashHolderPosition.x - d3);
		float f1 = (float)(leashHolderPosition.y - d4);
		float f2 = (float)(leashHolderPosition.z - d5);

		VertexConsumer vertexconsumer = buffer.getBuffer(RenderType.leash());
		Matrix4f matrix4f = poseStack.last().pose();

		// Вычисляем освещение как в ванильном коде
		BlockPos blockpos = BlockPos.containing(entity.getEyePosition(partialTicks));
		BlockPos blockpos1 = BlockPos.containing(leashHolderPosition);
		int i = this.getBlockLightLevel(entity, blockpos);
		int j = entity.level().getBrightness(LightLayer.BLOCK, blockpos1);
		int k = entity.level().getBrightness(LightLayer.SKY, blockpos);
		int l = entity.level().getBrightness(LightLayer.SKY, blockpos1);

		// Вычисляем вектор направления поводка
		float dirX = f;
		float dirY = f1;
		float dirZ = f2;

		// Нормализуем вектор направления
		float length = Mth.sqrt(dirX * dirX + dirY * dirY + dirZ * dirZ);
		if (length > 0) {
			dirX /= length;
			dirY /= length;
			dirZ /= length;
		}

		// Находим перпендикулярный вектор #1
		float perpX1, perpY1, perpZ1;

		// Если поводок почти вертикальный, используем другое базовое направление
		if (Math.abs(dirY) > 0.99f) {
			// Почти вертикальный поводок - используем ось X как базовое направление
			perpX1 = 0f;
			perpY1 = dirZ;
			perpZ1 = -dirY;
		} else {
			// Ищем вектор, перпендикулярный направлению поводка
			// Используем вектор вверх (0,1,0) как начальное направление
			perpX1 = 1f;
			perpY1 = 0f;
			perpZ1 = 0f;

			// Убедимся, что он не коллинеарен
			float dot = dirX * perpX1 + dirY * perpY1 + dirZ * perpZ1;
			if (Math.abs(dot) > 0.9f) {
				// Если почти коллинеарен, используем другой вектор
				perpX1 = 0f;
				perpY1 = 0f;
				perpZ1 = 1f;
				dot = dirX * perpX1 + dirY * perpY1 + dirZ * perpZ1;
			}

			// Вычитаем проекцию на dir, чтобы сделать перпендикулярным
			perpX1 = perpX1 - dirX * dot;
			perpY1 = perpY1 - dirY * dot;
			perpZ1 = perpZ1 - dirZ * dot;
		}

		// Нормализуем перпендикулярный вектор #1
		float perpLength1 = Mth.sqrt(perpX1 * perpX1 + perpY1 * perpY1 + perpZ1 * perpZ1);
		if (perpLength1 > 0) {
			perpX1 /= perpLength1;
			perpY1 /= perpLength1;
			perpZ1 /= perpLength1;
		}

		// Находим перпендикулярный вектор #2 через векторное произведение
		float perpX2 = dirY * perpZ1 - dirZ * perpY1;
		float perpY2 = dirZ * perpX1 - dirX * perpZ1;
		float perpZ2 = dirX * perpY1 - dirY * perpX1;

		// Нормализуем перпендикулярный вектор #2
		float perpLength2 = Mth.sqrt(perpX2 * perpX2 + perpY2 * perpY2 + perpZ2 * perpZ2);
		if (perpLength2 > 0) {
			perpX2 /= perpLength2;
			perpY2 /= perpLength2;
			perpZ2 /= perpLength2;
		}

		// Масштабируем для толщины поводка
		float thickness = 0.025F;
		float halfThickness = thickness / 2.0F;

		perpX1 *= halfThickness;
		perpY1 *= halfThickness;
		perpZ1 *= halfThickness;

		perpX2 *= halfThickness;
		perpY2 *= halfThickness;
		perpZ2 *= halfThickness;

		// Рендерим первый проход (одна сторона перекрестия)
		for (int i1 = 0; i1 <= 24; ++i1) {
			addVertexPair(vertexconsumer, matrix4f, f, f1, f2, i, j, k, l,
					thickness, thickness, perpX1, perpY1, perpZ1, perpX2, perpY2, perpZ2, i1, false);
		}
		for (int j1 = 24; j1 >= 0; --j1) {
			addVertexPair(vertexconsumer, matrix4f, f, f1, f2, i, j, k, l,
					thickness, 0.0F, perpX1, perpY1, perpZ1, perpX2, perpY2, perpZ2, j1, true);
		}

		// Рендерим второй проход (другая сторона перекрестия)
		for (int i1 = 0; i1 <= 24; ++i1) {
			addVertexPair(vertexconsumer, matrix4f, f, f1, f2, i, j, k, l,
					thickness, thickness, perpX2, perpY2, perpZ2, perpX1, perpY1, perpZ1, i1, false);
		}
		for (int j1 = 24; j1 >= 0; --j1) {
			addVertexPair(vertexconsumer, matrix4f, f, f1, f2, i, j, k, l,
					thickness, 0.0F, perpX2, perpY2, perpZ2, perpX1, perpY1, perpZ1, j1, true);
		}

		poseStack.popPose();
	}

	// Модифицированный метод addVertexPair для работы с 3D перпендикулярами
	private static void addVertexPair(VertexConsumer pConsumer, Matrix4f pMatrix,
									  float dirX, float dirY, float dirZ,
									  int pEntityBlockLightLevel, int pLeashHolderBlockLightLevel,
									  int pEntitySkyLightLevel, int pLeashHolderSkyLightLevel,
									  float p_174317_, float p_174318_,
									  float perpAX, float perpAY, float perpAZ,  // Первый перпендикуляр (основное смещение)
									  float perpBX, float perpBY, float perpBZ,  // Второй перпендикуляр (дополнительное смещение для толщины)
									  int pIndex, boolean p_174322_) {

		float f = (float)pIndex / 24.0F;
		int i = (int)Mth.lerp(f, (float)pEntityBlockLightLevel, (float)pLeashHolderBlockLightLevel);
		int j = (int)Mth.lerp(f, (float)pEntitySkyLightLevel, (float)pLeashHolderSkyLightLevel);
		int k = net.minecraft.client.renderer.LightTexture.pack(i, j);

		float f1 = pIndex % 2 == (p_174322_ ? 1 : 0) ? 0.7F : 1.0F;
		float f2 = 0.5F * f1;
		float f3 = 0.4F * f1;
		float f4 = 0.3F * f1;

		float posX = dirX * f;
		float posY = dirY > 0.0F ? dirY * f * f : dirY - dirY * (1.0F - f) * (1.0F - f);
		float posZ = dirZ * f;

		// Первая вершина: смещение по основному перпендикуляру и небольшое смещение по второму для толщины
		pConsumer.vertex(pMatrix,
						posX - perpAX + perpBX * p_174318_,
						posY - perpAY + perpBY * p_174318_,
						posZ - perpAZ + perpBZ * p_174318_)
				.color(f2, f3, f4, 1.0F)
				.uv2(k)
				.endVertex();

		// Вторая вершина: смещение в противоположную сторону по основному перпендикуляру
		pConsumer.vertex(pMatrix,
						posX + perpAX + (perpBX * (p_174317_ - p_174318_)),
						posY + perpAY + (perpBY * (p_174317_ - p_174318_)),
						posZ + perpAZ + (perpBZ * (p_174317_ - p_174318_)))
				.color(f2, f3, f4, 1.0F)
				.uv2(k)
				.endVertex();
	}

	@Override
	public ResourceLocation getTextureLocation(TireSwingEntity entity) {
		return TEXTURE;
	}
}