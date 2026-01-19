package net.dainplay.rpgworldmod.entity.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.dainplay.rpgworldmod.RPGworldMod;
import net.dainplay.rpgworldmod.entity.client.model.TireSwingModel;
import net.dainplay.rpgworldmod.entity.custom.TireSwingEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.LeashFenceKnotEntity;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

public class TireSwingRenderer extends EntityRenderer<TireSwingEntity> {
	private final TireSwingModel<TireSwingEntity> model;
	private static final ResourceLocation TEXTURE = new ResourceLocation(RPGworldMod.MOD_ID, "textures/entity/tire_swing/tire_swing.png");

	public TireSwingRenderer(EntityRendererProvider.Context context) {
		super(context);
		this.model = new TireSwingModel<>(context.bakeLayer(TireSwingModel.LAYER_LOCATION));
		this.shadowRadius = 0F;
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

		// Получаем позицию сиденья (куда должен тянуться поводок)
		Vec3 seatPosition = getSeatPosition(entity, partialTicks);

		// Рендерим поводок от держателя до сиденья
		renderStraightLeash(entity, partialTicks, poseStack, buffer, leashHolderPosition, seatPosition);
	}

	// Новый метод для получения позиции сиденья
	private Vec3 getSeatPosition(TireSwingEntity entity, float partialTicks) {
		// Интерполируем позицию сущности
		double x = Mth.lerp(partialTicks, entity.xo, entity.getX());
		double y = Mth.lerp(partialTicks, entity.yo, entity.getY());
		double z = Mth.lerp(partialTicks, entity.zo, entity.getZ());

		// Получаем параметры для вычисления позиции сиденья
		float ropeLength = entity.getRopeLength();
		float swingAngle = entity.getRenderSwingAngle(partialTicks);
		float swingYaw = entity.getRenderSwingYaw(partialTicks);

		// Переводим углы в радианы
		float swingAngleRad = (float) Math.toRadians(swingAngle);
		float swingYawRad = (float) Math.toRadians(swingYaw);

		// Вычисляем смещение сиденья относительно точки подвеса (аналогично positionRider в сущности)
		double forwardOffset = ropeLength * Math.sin(swingAngleRad);
		double verticalOffset = ropeLength * (1.0 - Math.cos(swingAngleRad));
		double yOffset = 0.7;

		// Базовое смещение (центр сиденья)
		double rotatedX = -forwardOffset * Math.sin(swingYawRad);
		double rotatedZ = forwardOffset * Math.cos(swingYawRad);

		// Если есть пассажир, смещаем точку крепления поводка
		if (entity.isOccupied()) {
			// Смещение назад относительно направления взгляда пассажира
			// Направление назад: противоположное направлению forwardOffset
			double backDistance = 0.25; // Расстояние назад от центра сиденья

			// Вычисляем угол наклона шины для корректировки вертикального смещения
			float tireTilt = entity.getModelRotationAngle(swingAngle);
			float tireTiltRad = (float) Math.toRadians(tireTilt);

			// При наклоне шины точка крепления смещается по кругу
			// Смещение назад по горизонтали с учетом наклона
			double backX = backDistance * Math.sin(swingYawRad);
			double backZ = -backDistance * Math.cos(swingYawRad);

			// Вертикальное смещение в зависимости от наклона шины
			// Когда шина наклонена вперед (положительный угол), задняя точка поднимается
			double verticalBackOffset = backDistance * Math.sin(tireTiltRad) * 0.5;

			// Возвращаем мировую позицию с учетом смещения
			return new Vec3(
					x + rotatedX + backX,
					y + yOffset + verticalOffset + verticalBackOffset,
					z + rotatedZ + backZ
			);
		}

		// Возвращаем мировую позицию сиденья без смещения (для пустых качелей)
		return new Vec3(x + rotatedX, y + yOffset + verticalOffset, z + rotatedZ);
	}

	// Модифицированный метод для рендеринга прямого поводка
	private void renderStraightLeash(TireSwingEntity entity, float partialTicks,
									 PoseStack poseStack, MultiBufferSource buffer,
									 Vec3 leashHolderPosition, Vec3 seatPosition) {

		poseStack.pushPose();

		// Переходим к позиции сиденья
		double d3 = seatPosition.x;
		double d4 = seatPosition.y;
		double d5 = seatPosition.z;

		// Вместо использования entity.getLeashOffset, начинаем от сиденья
		poseStack.translate(d3 - entity.getX(), d4 - entity.getY(), d5 - entity.getZ());

		float f = (float)(leashHolderPosition.x - d3);
		float f1 = (float)(leashHolderPosition.y - d4);
		float f2 = (float)(leashHolderPosition.z - d5);

		VertexConsumer vertexconsumer = buffer.getBuffer(RenderType.leash());
		Matrix4f matrix4f = poseStack.last().pose();

		// Вычисляем освещение для сиденья и держателя
		BlockPos seatPos = BlockPos.containing(seatPosition);
		BlockPos holderPos = BlockPos.containing(leashHolderPosition);
		int seatBlockLight = this.getBlockLightLevel(entity, seatPos);
		int holderBlockLight = entity.level().getBrightness(LightLayer.BLOCK, holderPos);
		int seatSkyLight = entity.level().getBrightness(LightLayer.SKY, seatPos);
		int holderSkyLight = entity.level().getBrightness(LightLayer.SKY, holderPos);

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

		// Количество сегментов поводка (меньше сегментов = более прямая линия)
		int segments = 24;

		// Рендерим первый проход (одна сторона перекрестия)
		for (int i1 = 0; i1 <= segments; ++i1) {
			addStraightVertexPair(vertexconsumer, matrix4f, f, f1, f2, seatBlockLight, holderBlockLight, seatSkyLight, holderSkyLight,
					thickness, thickness, perpX1, perpY1, perpZ1, perpX2, perpY2, perpZ2, i1, segments, false);
		}
		for (int j1 = segments; j1 >= 0; --j1) {
			addStraightVertexPair(vertexconsumer, matrix4f, f, f1, f2, seatBlockLight, holderBlockLight, seatSkyLight, holderSkyLight,
					thickness, 0.0F, perpX1, perpY1, perpZ1, perpX2, perpY2, perpZ2, j1, segments, true);
		}

		// Рендерим второй проход (другая сторона перекрестия)
		for (int i1 = 0; i1 <= segments; ++i1) {
			addStraightVertexPair(vertexconsumer, matrix4f, f, f1, f2, seatBlockLight, holderBlockLight, seatSkyLight, holderSkyLight,
					thickness, thickness, perpX2, perpY2, perpZ2, perpX1, perpY1, perpZ1, i1, segments, false);
		}
		for (int j1 = segments; j1 >= 0; --j1) {
			addStraightVertexPair(vertexconsumer, matrix4f, f, f1, f2, seatBlockLight, holderBlockLight, seatSkyLight, holderSkyLight,
					thickness, 0.0F, perpX2, perpY2, perpZ2, perpX1, perpY1, perpZ1, j1, segments, true);
		}

		poseStack.popPose();
	}

	// Модифицированный метод для прямой линии
	private static void addStraightVertexPair(VertexConsumer pConsumer, Matrix4f pMatrix,
											  float dirX, float dirY, float dirZ,
											  int seatBlockLightLevel, int holderBlockLightLevel,
											  int seatSkyLightLevel, int holderSkyLightLevel,
											  float thickness, float offset,
											  float perpAX, float perpAY, float perpAZ,
											  float perpBX, float perpBY, float perpBZ,
											  int pIndex, int segments, boolean reverse) {

		// Линейная интерполяция вместо параболы
		float f = (float)pIndex / segments;

		// Интерполируем освещение
		int blockLight = (int)Mth.lerp(f, (float)seatBlockLightLevel, (float)holderBlockLightLevel);
		int skyLight = (int)Mth.lerp(f, (float)seatSkyLightLevel, (float)holderSkyLightLevel);
		int packedLight = net.minecraft.client.renderer.LightTexture.pack(blockLight, skyLight);

		// Цвет поводка
		float colorMultiplier = pIndex % 2 == (reverse ? 1 : 0) ? 0.7F : 1.0F;
		float r = 0.5F * colorMultiplier;
		float g = 0.4F * colorMultiplier;
		float b = 0.3F * colorMultiplier;

		// Прямая линейная интерполяция координат
		float posX = dirX * f;
		float posY = dirY * f;  // Прямая линия вместо параболы
		float posZ = dirZ * f;

		// Первая вершина
		pConsumer.vertex(pMatrix,
						posX - perpAX + perpBX * offset,
						posY - perpAY + perpBY * offset,
						posZ - perpAZ + perpBZ * offset)
				.color(r, g, b, 1.0F)
				.uv2(packedLight)
				.endVertex();

		// Вторая вершина
		pConsumer.vertex(pMatrix,
						posX + perpAX + (perpBX * (thickness - offset)),
						posY + perpAY + (perpBY * (thickness - offset)),
						posZ + perpAZ + (perpBZ * (thickness - offset)))
				.color(r, g, b, 1.0F)
				.uv2(packedLight)
				.endVertex();
	}

	@Override
	public ResourceLocation getTextureLocation(TireSwingEntity entity) {
		return TEXTURE;
	}

	@Override
	public boolean shouldRender(TireSwingEntity entity, Frustum frustum, double cameraX, double cameraY, double cameraZ) {
		// Всегда рендерить, если есть привязка (чтобы поводок был виден)
			// Расширяем проверяемую область для поводка
			AABB expandedBox = entity.getBoundingBox().inflate(10.0);
			if (frustum.isVisible(expandedBox)) {
				return true;
			}

		// Стандартная проверка для качелей
		return super.shouldRender(entity, frustum, cameraX, cameraY, cameraZ);
	}
}