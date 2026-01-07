package net.dainplay.rpgworldmod.entity.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.dainplay.rpgworldmod.RPGworldMod;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;

import java.util.HashMap;
import java.util.Map;

public class SkirtModel extends HumanoidModel<LivingEntity> {
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation(RPGworldMod.MOD_ID, "skirt"), "main");

	// Константы для ограничения вращения лепестков
	private static final float MAX_PETAL_ANGLE_UP = 0.8f;   // Максимальный угол поднятия
	private static final float MAX_PETAL_ANGLE_DOWN = -0.3f; // Максимальный угол опускания
	private static final float FALL_RISE_FACTOR = 0.5f;     // Коэффициент реакции на падение/подъем
	private static final float CROUCH_ANGLE = 0.6f;         // Угол при приседании
	private static final float SIT_ANGLE = 0.8f;           // Угол при сидении
	private static final float ANIMATION_SMOOTHING = 0.1f;  // Сглаживание анимации
	private static final float CROUCH_Y_OFFSET = -1.5f;     // Смещение по Y при приседании (отрицательное = вверх)

	// Карта для хранения состояния анимации для каждой сущности
	private final Map<Integer, SkirtAnimationState> animationStates = new HashMap<>();

	public final ModelPart petal_1;
	public final ModelPart petal_2;
	public final ModelPart petal_3;
	public final ModelPart petal_4;
	public final ModelPart petal_5;
	public final ModelPart petal_6;
	public final ModelPart bb_main;

	// Переменные для хранения исходных позиций лепестков
	private final float petal1OriginalY;
	private final float petal2OriginalY;

	public SkirtModel(ModelPart root) {
		super(root);
		// Получаем части из тела
		this.petal_1 = this.body.getChild("petal_1");
		this.petal_2 = this.body.getChild("petal_2");
		this.petal_3 = this.body.getChild("petal_3");
		this.petal_4 = this.body.getChild("petal_4");
		this.petal_5 = this.body.getChild("petal_5");
		this.petal_6 = this.body.getChild("petal_6");
		this.bb_main = this.body.getChild("bb_main");

		// Сохраняем исходные позиции по Y
		this.petal1OriginalY = this.petal_1.y;
		this.petal2OriginalY = this.petal_2.y;
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		// Добавляем обязательные части для HumanoidModel (пустые или с базовой геометрией)
		PartDefinition head = partdefinition.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.ZERO);
		PartDefinition hat = partdefinition.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.ZERO);
		PartDefinition body = partdefinition.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.ZERO);
		PartDefinition right_arm = partdefinition.addOrReplaceChild("right_arm", CubeListBuilder.create(), PartPose.ZERO);
		PartDefinition left_arm = partdefinition.addOrReplaceChild("left_arm", CubeListBuilder.create(), PartPose.ZERO);
		PartDefinition right_leg = partdefinition.addOrReplaceChild("right_leg", CubeListBuilder.create(), PartPose.ZERO);
		PartDefinition left_leg = partdefinition.addOrReplaceChild("left_leg", CubeListBuilder.create(), PartPose.ZERO);

		// Добавляем части юбки как дочерние элементы для "body"
		PartDefinition petal_1 = body.addOrReplaceChild("petal_1", CubeListBuilder.create(), PartPose.offset(-2.5F, 10.0F, -2.0F));
		PartDefinition cube_r1 = petal_1.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(-7, 7).addBox(-2.5F, 0.0F, -6.0F, 5.0F, 0.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.7854F, 0.2618F, 0.0F));

		PartDefinition petal_2 = body.addOrReplaceChild("petal_2", CubeListBuilder.create(), PartPose.offset(2.5F, 10.0F, -2.0F));
		PartDefinition cube_r2 = petal_2.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(-7, 14).addBox(-2.5F, 0.0F, -6.0F, 5.0F, 0.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.7854F, -0.2618F, 0.0F));

		PartDefinition petal_3 = body.addOrReplaceChild("petal_3", CubeListBuilder.create(), PartPose.offset(4.5F, 10.0F, 0.0F));
		PartDefinition cube_r3 = petal_3.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(-6, 21).addBox(-2.5F, 0.0F, -6.0F, 5.0F, 0.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -1.5708F, 0.7854F));

		PartDefinition petal_4 = body.addOrReplaceChild("petal_4", CubeListBuilder.create(), PartPose.offset(2.5F, 10.0F, 2.0F));
		PartDefinition cube_r4 = petal_4.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(3, 7).addBox(-2.5F, 0.0F, -6.0F, 5.0F, 0.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -2.3562F, -0.2618F, 3.1416F));

		PartDefinition petal_5 = body.addOrReplaceChild("petal_5", CubeListBuilder.create(), PartPose.offset(-2.5F, 10.0F, 2.0F));
		PartDefinition cube_r5 = petal_5.addOrReplaceChild("cube_r5", CubeListBuilder.create().texOffs(3, 14).addBox(-2.5F, 0.0F, -6.0F, 5.0F, 0.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -2.3562F, 0.2618F, 3.1416F));

		PartDefinition petal_6 = body.addOrReplaceChild("petal_6", CubeListBuilder.create(), PartPose.offset(-4.5F, 10.0F, 0.0F));
		PartDefinition cube_r6 = petal_6.addOrReplaceChild("cube_r6", CubeListBuilder.create().texOffs(4, 21).addBox(-2.5F, 0.0F, -6.0F, 5.0F, 0.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 1.5708F, -0.7854F));

		PartDefinition bb_main = body.addOrReplaceChild("bb_main", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -16.0F, -2.0F, 8.0F, 3.0F, 4.0F, new CubeDeformation(0.5F)), PartPose.offset(0.0F, 24.0F, 0.0F));

		return LayerDefinition.create(meshdefinition, 32, 32);
	}

	@Override
	public void setupAnim(LivingEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);

		// Получаем уникальный ID сущности для хранения состояния
		int entityId = entity.getId();

		// Получаем или создаем состояние анимации для этой сущности
		SkirtAnimationState state = animationStates.computeIfAbsent(entityId, id -> new SkirtAnimationState());

		// Получаем вертикальную скорость движения (падение/подъем)
		float verticalMotion = (float) entity.getDeltaMovement().y;

		// Определяем состояние сущности
		boolean isCrouching = entity.isCrouching();
		boolean isSitting = entity.isPassenger() || entity.getPose() == Pose.SITTING;

		// Вычисляем анимацию для каждого лепестка
		float[] targetAngles = new float[6];

		// Базовая анимация от падения/подъема
		float fallRiseEffect = verticalMotion * FALL_RISE_FACTOR;

		// Целевые смещения по Y для лепестков 1 и 2
		float targetYOffset1 = 0;
		float targetYOffset2 = 0;

		// Применяем эффект падения/подъема ко всем лепесткам
		for (int i = 0; i < 6; i++) {
			targetAngles[i] = fallRiseEffect;
		}

		// Анимация при приседании (Shift)
		if (isCrouching && !isSitting) {
			// Лепестки 1 и 2 приподнимаются по вращению
			targetAngles[0] -= CROUCH_ANGLE;
			targetAngles[1] -= CROUCH_ANGLE;
			// Лепестки 4 и 5 опускаются
			targetAngles[3] += CROUCH_ANGLE;
			targetAngles[4] += CROUCH_ANGLE;

			// Лепестки 1 и 2 поднимаются по оси Y
			targetYOffset1 = CROUCH_Y_OFFSET;
			targetYOffset2 = CROUCH_Y_OFFSET;
		}

		// Анимация при сидении
		if (isSitting) {
			// Лепестки 1 и 2 приподнимаются
			targetAngles[0] -= SIT_ANGLE;
			targetAngles[1] -= SIT_ANGLE;
			targetYOffset1 = CROUCH_Y_OFFSET;
			targetYOffset2 = CROUCH_Y_OFFSET;
		}

		// Применяем сглаживание анимации для вращения
		for (int i = 0; i < 6; i++) {
			targetAngles[i] = state.prevPetalAngles[i] + (targetAngles[i] - state.prevPetalAngles[i]) * ANIMATION_SMOOTHING;

			// Ограничиваем углы вращения
			targetAngles[i] = clamp(targetAngles[i], MAX_PETAL_ANGLE_DOWN, MAX_PETAL_ANGLE_UP);

			// Сохраняем текущие углы для следующего кадра
			state.prevPetalAngles[i] = targetAngles[i];
		}

		// Применяем сглаживание для смещения по Y
		state.currentPetal1YOffset = state.currentPetal1YOffset + (targetYOffset1 - state.currentPetal1YOffset) * ANIMATION_SMOOTHING;
		state.currentPetal2YOffset = state.currentPetal2YOffset + (targetYOffset2 - state.currentPetal2YOffset) * ANIMATION_SMOOTHING;

		// Применяем вычисленные углы к лепесткам
		this.petal_1.xRot = targetAngles[0];
		this.petal_2.xRot = targetAngles[1];
		this.petal_3.zRot = targetAngles[2];
		this.petal_4.xRot = -targetAngles[3];
		this.petal_5.xRot = -targetAngles[4];
		this.petal_6.zRot = -targetAngles[5];

		// Применяем смещение по Y для лепестков 1 и 2
		this.petal_1.y = petal1OriginalY + state.currentPetal1YOffset;
		this.petal_2.y = petal2OriginalY + state.currentPetal2YOffset;

		// Сохраняем состояния для следующего кадра
		state.prevVerticalMotion = verticalMotion;
		state.prevCrouching = isCrouching;
		state.prevSitting = isSitting;
	}

	/**
	 * Ограничивает значение в заданных пределах
	 */
	private float clamp(float value, float min, float max) {
		return Math.max(min, Math.min(max, value));
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		// Рендерим только body с его дочерними элементами (части юбки)
		this.body.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
	}

	/**
	 * Внутренний класс для хранения состояния анимации каждой сущности
	 */
	private static class SkirtAnimationState {
		float prevVerticalMotion = 0;
		boolean prevCrouching = false;
		boolean prevSitting = false;
		float[] prevPetalAngles = new float[6];
		float currentPetal1YOffset = 0;
		float currentPetal2YOffset = 0;

		SkirtAnimationState() {
			// Инициализируем предыдущие углы
			for (int i = 0; i < 6; i++) {
				prevPetalAngles[i] = 0;
			}
		}
	}
}