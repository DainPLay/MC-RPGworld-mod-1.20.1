package net.dainplay.rpgworldmod.entity.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.dainplay.rpgworldmod.RPGworldMod;
import net.dainplay.rpgworldmod.entity.custom.Razorleaf;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class RazorleafModel extends EntityModel<Razorleaf> {
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation(RPGworldMod.MOD_ID, "razorleaf"), "main");
	public final ModelPart body;
	public final ModelPart tongue;
	public final ModelPart head;
	public final ModelPart petal1;
	public final ModelPart petal2;
	public final ModelPart petal3;
	public final ModelPart petal4;
	public final ModelPart stem;
	public final ModelPart leaves1;
	public final ModelPart leaf1;
	public final ModelPart leaf2;
	public final ModelPart leaves2;
	public final ModelPart leaf4;
	public final ModelPart leaf3;
	public final ModelPart leaves3;
	public final ModelPart leaf5;
	public final ModelPart leaf6;

	// Константы для плавности анимаций
	private static final float ANIMATION_SMOOTHING = 0.05f;  // Коэффициент сглаживания
	private static final float MAX_PETAL_ROTATION = 0.5f;    // Максимальный угол поворота лепестков
	private static final float MAX_LEAF_ROTATION = 0.2f;     // Максимальный угол поворота листьев
	private static final float TONGUE_SPEED = 0.5f;          // Скорость движения языка

	public RazorleafModel(ModelPart root) {
		this.body = root.getChild("body");
		this.head = this.body.getChild("head");
		this.petal1 = this.head.getChild("petal1");
		this.petal2 = this.head.getChild("petal2");
		this.petal3 = this.head.getChild("petal3");
		this.petal4 = this.head.getChild("petal4");
		this.tongue = this.head.getChild("tongue");
		this.stem = this.body.getChild("stem");
		this.leaves1 = this.stem.getChild("leaves1");
		this.leaf1 = this.leaves1.getChild("leaf1");
		this.leaf2 = this.leaves1.getChild("leaf2");
		this.leaves2 = this.stem.getChild("leaves2");
		this.leaf4 = this.leaves2.getChild("leaf4");
		this.leaf3 = this.leaves2.getChild("leaf3");
		this.leaves3 = this.stem.getChild("leaves3");
		this.leaf5 = this.leaves3.getChild("leaf5");
		this.leaf6 = this.leaves3.getChild("leaf6");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition body = partdefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-10.0F, -6.0F, -10.0F, 20.0F, 6.0F, 20.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 24.0F, 0.0F));

		PartDefinition head = body.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.offset(0.0F, -4.0F, 0.0F));

		PartDefinition petal1 = head.addOrReplaceChild("petal1", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition cube_r1 = petal1.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(48, 132).addBox(-15.0F, -16.0F, 3.0F, 12.0F, 16.0F, 12.0F, new CubeDeformation(0.0F))
				.texOffs(64, 100).addBox(-15.0F, 0.0F, -1.0F, 16.0F, 16.0F, 16.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-15.0F, -16.0F, -15.0F, 0.0F, 1.5708F, 0.0F));

		PartDefinition petal2 = head.addOrReplaceChild("petal2", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition cube_r2 = petal2.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(0, 130).addBox(-15.0F, -16.0F, 3.0F, 12.0F, 16.0F, 12.0F, new CubeDeformation(0.0F))
				.texOffs(0, 98).addBox(-15.0F, 0.0F, -1.0F, 16.0F, 16.0F, 16.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-15.0F, -16.0F, 15.0F, 0.0F, 3.1416F, 0.0F));

		PartDefinition petal3 = head.addOrReplaceChild("petal3", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition cube_r3 = petal3.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(128, 128).addBox(-15.0F, -16.0F, 3.0F, 12.0F, 16.0F, 12.0F, new CubeDeformation(0.0F))
				.texOffs(88, 68).addBox(-15.0F, 0.0F, -1.0F, 16.0F, 16.0F, 16.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(15.0F, -16.0F, 15.0F, 0.0F, -1.5708F, 0.0F));

		PartDefinition petal4 = head.addOrReplaceChild("petal4", CubeListBuilder.create().texOffs(88, 36).addBox(0.0F, -16.0F, -16.0F, 16.0F, 16.0F, 16.0F, new CubeDeformation(0.0F))
				.texOffs(128, 100).addBox(0.0F, -32.0F, -12.0F, 12.0F, 16.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition tongue = head.addOrReplaceChild("tongue", CubeListBuilder.create().texOffs(96, 132).addBox(-2.0F, -38.0F, 0.0F, 4.0F, 40.0F, 0.0F, new CubeDeformation(0.0F))
				.texOffs(0, 158).addBox(-3.0F, -19.0F, -3.0F, 6.0F, 6.0F, 6.0F, new CubeDeformation(0.0F))
				.texOffs(0, 170).addBox(-4.0F, -20.0F, 0.0F, 8.0F, 8.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition cube_r4 = tongue.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(16, 170).addBox(-4.0F, -4.0F, 0.0F, 8.0F, 8.0F, 0.0F, new CubeDeformation(0.0F))
				.texOffs(104, 132).addBox(-2.0F, -22.0F, 0.0F, 4.0F, 40.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -16.0F, 0.0F, 0.0F, 1.5708F, 0.0F));

		PartDefinition stem = body.addOrReplaceChild("stem", CubeListBuilder.create(), PartPose.offset(0.0F, -3.0F, 0.0F));

		PartDefinition leaves1 = stem.addOrReplaceChild("leaves1", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition leaf1 = leaves1.addOrReplaceChild("leaf1", CubeListBuilder.create().texOffs(0, 26).addBox(-24.0F, 0.0F, -9.0F, 26.0F, 0.0F, 18.0F, new CubeDeformation(0.0F)), PartPose.offset(-10.0F, 0.0F, 0.0F));

		PartDefinition leaf2 = leaves1.addOrReplaceChild("leaf2", CubeListBuilder.create(), PartPose.offset(10.0F, 0.0F, 0.0F));

		PartDefinition leaf2_r1 = leaf2.addOrReplaceChild("leaf2_r1", CubeListBuilder.create().texOffs(0, 44).addBox(-24.0F, 0.0F, -9.0F, 26.0F, 0.0F, 18.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 3.1416F, 0.0F, 3.1416F));

		PartDefinition leaves2 = stem.addOrReplaceChild("leaves2", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -1.0472F, 0.0F));

		PartDefinition leaf4 = leaves2.addOrReplaceChild("leaf4", CubeListBuilder.create(), PartPose.offset(10.0F, 0.0F, 0.0F));

		PartDefinition leaf2_r2 = leaf4.addOrReplaceChild("leaf2_r2", CubeListBuilder.create().texOffs(0, 80).addBox(-24.0F, 0.0F, -9.0F, 26.0F, 0.0F, 18.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 3.1416F, 0.0F, -3.098F));

		PartDefinition leaf3 = leaves2.addOrReplaceChild("leaf3", CubeListBuilder.create().texOffs(0, 62).addBox(-24.0F, 0.0F, -9.0F, 26.0F, 0.0F, 18.0F, new CubeDeformation(0.0F)), PartPose.offset(-10.0F, 0.0F, 0.0F));

		PartDefinition leaves3 = stem.addOrReplaceChild("leaves3", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -2.0944F, 0.0F));

		PartDefinition leaf5 = leaves3.addOrReplaceChild("leaf5", CubeListBuilder.create().texOffs(80, 0).addBox(-24.0F, 0.0F, -9.0F, 26.0F, 0.0F, 18.0F, new CubeDeformation(0.0F)), PartPose.offset(-10.0F, 0.0F, 0.0F));

		PartDefinition leaf6 = leaves3.addOrReplaceChild("leaf6", CubeListBuilder.create(), PartPose.offset(10.0F, 0.0F, 0.0F));

		PartDefinition leaf2_r3 = leaf6.addOrReplaceChild("leaf2_r3", CubeListBuilder.create().texOffs(88, 18).addBox(-24.0F, 0.0F, -9.0F, 26.0F, 0.0F, 18.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 3.1416F, 0.0F));

		return LayerDefinition.create(meshdefinition, 256, 256);
	}

	// Вспомогательный метод для сглаживания значений
	private float smooth(float current, float target) {
		return current + (target - current) * ANIMATION_SMOOTHING;
	}

	// Вспомогательный метод для ограничения значений
	private float clamp(float value, float min, float max) {
		return Math.max(min, Math.min(max, value));
	}

	// Метод для применения всех сглаженных значений
	private void applySmoothedValues(Razorleaf entity) {
		// Чтение текущих значений из сущности
		float currentPetal1XRot = entity.getCurrentPetal1XRot();
		float currentPetal1ZRot = entity.getCurrentPetal1ZRot();
		float currentPetal2XRot = entity.getCurrentPetal2XRot();
		float currentPetal2ZRot = entity.getCurrentPetal2ZRot();
		float currentPetal3XRot = entity.getCurrentPetal3XRot();
		float currentPetal3ZRot = entity.getCurrentPetal3ZRot();
		float currentPetal4XRot = entity.getCurrentPetal4XRot();
		float currentPetal4ZRot = entity.getCurrentPetal4ZRot();
		float currentStemYRot = entity.getCurrentStemYRot();
		float currentLeaves1XRot = entity.getCurrentLeaves1XRot();
		float currentLeaves2XRot = entity.getCurrentLeaves2XRot();
		float currentLeaves3XRot = entity.getCurrentLeaves3XRot();
		float currentLeaf1ZRot = entity.getCurrentLeaf1ZRot();
		float currentLeaf2ZRot = entity.getCurrentLeaf2ZRot();
		float currentLeaf3ZRot = entity.getCurrentLeaf3ZRot();
		float currentLeaf4ZRot = entity.getCurrentLeaf4ZRot();
		float currentLeaf5ZRot = entity.getCurrentLeaf5ZRot();
		float currentLeaf6ZRot = entity.getCurrentLeaf6ZRot();
		float currentTongueYRot = entity.getCurrentTongueYRot();
		float currentTongueZRot = entity.getCurrentTongueZRot();
		float currentTongueXRot = entity.getCurrentTongueXRot();
		float currentHeadXRot = entity.getCurrentHeadXRot();

		// Чтение целевых значений из сущности
		float targetPetal1XRot = entity.getTargetPetal1XRot();
		float targetPetal1ZRot = entity.getTargetPetal1ZRot();
		float targetPetal2XRot = entity.getTargetPetal2XRot();
		float targetPetal2ZRot = entity.getTargetPetal2ZRot();
		float targetPetal3XRot = entity.getTargetPetal3XRot();
		float targetPetal3ZRot = entity.getTargetPetal3ZRot();
		float targetPetal4XRot = entity.getTargetPetal4XRot();
		float targetPetal4ZRot = entity.getTargetPetal4ZRot();
		float targetStemYRot = entity.getTargetStemYRot();
		float targetLeaves1XRot = entity.getTargetLeaves1XRot();
		float targetLeaves2XRot = entity.getTargetLeaves2XRot();
		float targetLeaves3XRot = entity.getTargetLeaves3XRot();
		float targetLeaf1ZRot = entity.getTargetLeaf1ZRot();
		float targetLeaf2ZRot = entity.getTargetLeaf2ZRot();
		float targetLeaf3ZRot = entity.getTargetLeaf3ZRot();
		float targetLeaf4ZRot = entity.getTargetLeaf4ZRot();
		float targetLeaf5ZRot = entity.getTargetLeaf5ZRot();
		float targetLeaf6ZRot = entity.getTargetLeaf6ZRot();
		float targetTongueYRot = entity.getTargetTongueYRot();
		float targetTongueZRot = entity.getTargetTongueZRot();
		float targetTongueXRot = entity.getTargetTongueXRot();
		float targetHeadXRot = entity.getTargetHeadXRot();

		// Сглаживаем и применяем значения для лепестков
		currentPetal1XRot = smooth(currentPetal1XRot, targetPetal1XRot);
		currentPetal1ZRot = smooth(currentPetal1ZRot, targetPetal1ZRot);
		currentPetal2XRot = smooth(currentPetal2XRot, targetPetal2XRot);
		currentPetal2ZRot = smooth(currentPetal2ZRot, targetPetal2ZRot);
		currentPetal3XRot = smooth(currentPetal3XRot, targetPetal3XRot);
		currentPetal3ZRot = smooth(currentPetal3ZRot, targetPetal3ZRot);
		currentPetal4XRot = smooth(currentPetal4XRot, targetPetal4XRot);
		currentPetal4ZRot = smooth(currentPetal4ZRot, targetPetal4ZRot);

		petal1.setRotation(currentPetal1XRot, petal1.yRot, currentPetal1ZRot);
		petal2.setRotation(currentPetal2XRot, petal2.yRot, currentPetal2ZRot);
		petal3.setRotation(currentPetal3XRot, petal3.yRot, currentPetal3ZRot);
		petal4.setRotation(currentPetal4XRot, petal4.yRot, currentPetal4ZRot);

		// Стебель
		currentStemYRot = smooth(currentStemYRot, targetStemYRot);
		stem.yRot = currentStemYRot;

		// Группы листьев
		currentLeaves1XRot = smooth(currentLeaves1XRot, targetLeaves1XRot);
		currentLeaves2XRot = smooth(currentLeaves2XRot, targetLeaves2XRot);
		currentLeaves3XRot = smooth(currentLeaves3XRot, targetLeaves3XRot);

		leaves1.xRot = currentLeaves1XRot;
		leaves2.xRot = currentLeaves2XRot;
		leaves3.xRot = currentLeaves3XRot;

		// Отдельные листья
		currentLeaf1ZRot = smooth(currentLeaf1ZRot, targetLeaf1ZRot);
		currentLeaf2ZRot = smooth(currentLeaf2ZRot, targetLeaf2ZRot);
		currentLeaf3ZRot = smooth(currentLeaf3ZRot, targetLeaf3ZRot);
		currentLeaf4ZRot = smooth(currentLeaf4ZRot, targetLeaf4ZRot);
		currentLeaf5ZRot = smooth(currentLeaf5ZRot, targetLeaf5ZRot);
		currentLeaf6ZRot = smooth(currentLeaf6ZRot, targetLeaf6ZRot);

		leaf1.zRot = currentLeaf1ZRot;
		leaf2.zRot = currentLeaf2ZRot;
		leaf3.zRot = currentLeaf3ZRot;
		leaf4.zRot = currentLeaf4ZRot;
		leaf5.zRot = currentLeaf5ZRot;
		leaf6.zRot = currentLeaf6ZRot;

		// Язык
		currentTongueXRot = smooth(currentTongueXRot, targetTongueXRot);
		currentTongueYRot = smooth(currentTongueYRot, targetTongueYRot);
		currentTongueZRot = smooth(currentTongueZRot, targetTongueZRot);

		tongue.xRot = currentTongueXRot;
		tongue.yRot = currentTongueYRot;
		tongue.zRot = currentTongueZRot;

		currentHeadXRot = smooth(currentHeadXRot, targetHeadXRot);
		head.xRot = currentHeadXRot;

		// Запись обновлённых текущих значений обратно в сущность
		entity.setCurrentPetal1XRot(currentPetal1XRot);
		entity.setCurrentPetal1ZRot(currentPetal1ZRot);
		entity.setCurrentPetal2XRot(currentPetal2XRot);
		entity.setCurrentPetal2ZRot(currentPetal2ZRot);
		entity.setCurrentPetal3XRot(currentPetal3XRot);
		entity.setCurrentPetal3ZRot(currentPetal3ZRot);
		entity.setCurrentPetal4XRot(currentPetal4XRot);
		entity.setCurrentPetal4ZRot(currentPetal4ZRot);
		entity.setCurrentStemYRot(currentStemYRot);
		entity.setCurrentLeaves1XRot(currentLeaves1XRot);
		entity.setCurrentLeaves2XRot(currentLeaves2XRot);
		entity.setCurrentLeaves3XRot(currentLeaves3XRot);
		entity.setCurrentLeaf1ZRot(currentLeaf1ZRot);
		entity.setCurrentLeaf2ZRot(currentLeaf2ZRot);
		entity.setCurrentLeaf3ZRot(currentLeaf3ZRot);
		entity.setCurrentLeaf4ZRot(currentLeaf4ZRot);
		entity.setCurrentLeaf5ZRot(currentLeaf5ZRot);
		entity.setCurrentLeaf6ZRot(currentLeaf6ZRot);
		entity.setCurrentTongueYRot(currentTongueYRot);
		entity.setCurrentTongueZRot(currentTongueZRot);
		entity.setCurrentTongueXRot(currentTongueXRot);
		entity.setCurrentHeadXRot(currentHeadXRot);
	}

	@Override
	public void setupAnim(Razorleaf entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		this.head.yRot = netHeadYaw * Mth.DEG_TO_RAD;

		Razorleaf.State currentState = entity.getState();
		int currentAttackType = entity.getAttackType();
		Razorleaf.State prevState = entity.getPrevState();
		int prevAttackType = entity.getPrevAttackType();

		// Сбрасываем сглаживание при смене состояния или типа атаки
		if (currentState != prevState || (currentState == Razorleaf.State.ATTACKING && currentAttackType != prevAttackType)) {
			// Можно сбросить сглаживание, установив текущие значения равными целевым
			resetSmoothing(entity);
		}

		entity.setPrevState(currentState);
		entity.setPrevAttackType(currentAttackType);

		// В зависимости от состояния
		switch (currentState) {
			case IDLE:
				setTargetIdle(entity, ageInTicks);
				break;

			case ATTACKING:
				switch (currentAttackType) {
					case 1: // Атака языком
						setTargetTongueAttack(entity, ageInTicks, netHeadYaw, headPitch);
						break;

					case 2: // Огненное дыхание
						setTargetFireBreath(entity, ageInTicks, netHeadYaw, headPitch);
						break;

					case 3: // Вращение лепестками
						setTargetPetalSpin(entity, ageInTicks);
						break;
				}
				break;

			case EXTINGUISHED:
				setTargetExtinguished(entity);
				break;
		}

		// Обновляем выдвижение языка
		float tongueExtension = entity.getCurrentTongueExtension();
		float targetTongueExtension = tongueExtension;
		float currentTongueExtension = entity.getCurrentTongueExtension();
		currentTongueExtension = smooth(currentTongueExtension, targetTongueExtension);
		entity.setCurrentTongueExtension(currentTongueExtension);
		updateTonguePosition(entity, currentTongueExtension);

		// Применяем сглаженные значения
		applySmoothedValues(entity);
	}

	private void resetSmoothing(Razorleaf entity) {
		// Устанавливаем текущие значения равными их текущему положению
		entity.setCurrentPetal1XRot(petal1.xRot);
		entity.setCurrentPetal1ZRot(petal1.zRot);
		entity.setCurrentPetal2XRot(petal2.xRot);
		entity.setCurrentPetal2ZRot(petal2.zRot);
		entity.setCurrentPetal3XRot(petal3.xRot);
		entity.setCurrentPetal3ZRot(petal3.zRot);
		entity.setCurrentPetal4XRot(petal4.xRot);
		entity.setCurrentPetal4ZRot(petal4.zRot);
		entity.setCurrentStemYRot(stem.yRot);
		entity.setCurrentLeaves1XRot(leaves1.xRot);
		entity.setCurrentLeaves2XRot(leaves2.xRot);
		entity.setCurrentLeaves3XRot(leaves3.xRot);
		entity.setCurrentLeaf1ZRot(leaf1.zRot);
		entity.setCurrentLeaf2ZRot(leaf2.zRot);
		entity.setCurrentLeaf3ZRot(leaf3.zRot);
		entity.setCurrentLeaf4ZRot(leaf4.zRot);
		entity.setCurrentLeaf5ZRot(leaf5.zRot);
		entity.setCurrentLeaf6ZRot(leaf6.zRot);
		entity.setCurrentTongueXRot(tongue.xRot);
		entity.setCurrentTongueYRot(tongue.yRot);
		entity.setCurrentTongueZRot(tongue.zRot);
		entity.setCurrentTongueExtension(entity.getCurrentTongueExtension());
	}

	private void setTargetIdle(Razorleaf entity, float ageInTicks) {
		// Медленное движение лепестков
		float time = ageInTicks * 0.05f;
		float petalOpen = 0.05f + Mth.sin(time) * 0.05f;
		if(!entity.isDay(entity.level())) petalOpen *= 0.05f;

		entity.setTargetPetal1XRot(petalOpen);
		entity.setTargetPetal1ZRot(-petalOpen);
		entity.setTargetPetal2XRot(-petalOpen);
		entity.setTargetPetal2ZRot(-petalOpen);
		entity.setTargetPetal3XRot(-petalOpen);
		entity.setTargetPetal3ZRot(petalOpen);
		entity.setTargetPetal4XRot(petalOpen);
		entity.setTargetPetal4ZRot(petalOpen);

		float stemRotate = Mth.sin(time)/3f;
		if(!entity.isDay(entity.level())) stemRotate *= 0.05f;
		entity.setTargetStemYRot(stemRotate); // Уменьшаем вращение стебля

		entity.setTargetLeaf1ZRot(-petalOpen*0.25f);
		entity.setTargetLeaf2ZRot(petalOpen*0.25f);
		entity.setTargetLeaf3ZRot(-petalOpen*0.25f);
		entity.setTargetLeaf4ZRot(-petalOpen*0.25f);
		entity.setTargetLeaf5ZRot(-petalOpen*0.25f);
		entity.setTargetLeaf6ZRot(petalOpen*0.25f);
		float yOffset = 1f + Mth.sin(time);
		if(!entity.isDay(entity.level())) yOffset *= 0.05f;
		leaf1.y = yOffset;
		leaf2.y = yOffset;
		leaf3.y = yOffset;
		leaf4.y = yOffset;
		leaf5.y = yOffset;
		leaf6.y = yOffset;

		// Слегка приоткрытый рот
		entity.setTargetTongueYRot(0f);
		entity.setTargetTongueZRot(0f);
		entity.setTargetTongueXRot(0f);
		entity.setTargetHeadXRot(0.0F);
	}

	private void setTargetTongueAttack(Razorleaf entity, float ageInTicks, float netHeadYaw, float headPitch) {
		float tongueTime = entity.getTongueAnimationTime();

		// Вращение головы - плавно
		if (tongueTime < 31) {
			float progress = tongueTime / 22f;
			entity.setTargetHeadXRot(0.9F * progress);
		}
		else {
			entity.setTargetHeadXRot(0F);
		}

		float petalOpen;

		// Лепестки открываются шире - плавно
		if (tongueTime < 22) {
			float progress = tongueTime / 22f;
			petalOpen = 0.5f * progress;
		}
		else if (tongueTime >= 22 && tongueTime <= 30) {
			petalOpen = 0.5f;
		} else if (tongueTime > 30 && tongueTime < 40) {
			float progress = (tongueTime - 30) / 9f;
			petalOpen = 0.5f * (1f - progress);
		} else {
			petalOpen = 0f;
		}

		entity.setTargetPetal1XRot(petalOpen);
		entity.setTargetPetal1ZRot(-petalOpen);
		entity.setTargetPetal2XRot(-petalOpen);
		entity.setTargetPetal2ZRot(-petalOpen);
		entity.setTargetPetal3XRot(-petalOpen);
		entity.setTargetPetal3ZRot(petalOpen);
		entity.setTargetPetal4XRot(petalOpen);
		entity.setTargetPetal4ZRot(petalOpen);

		// Ограничиваем вращение стебля во время атаки
		entity.setTargetStemYRot(0f);

		// Ограничиваем движение листьев
		entity.setTargetLeaves1XRot(0f);
		entity.setTargetLeaves2XRot(0f);
		entity.setTargetLeaves3XRot(0f);

		entity.setTargetLeaf1ZRot(0f);
		entity.setTargetLeaf2ZRot(0f);
		entity.setTargetLeaf3ZRot(0f);
		entity.setTargetLeaf4ZRot(0f);
		entity.setTargetLeaf5ZRot(0f);
		entity.setTargetLeaf6ZRot(0f);

		// Позиция языка будет обработана в updateTonguePosition
		entity.setTargetTongueYRot(0f);
		entity.setTargetTongueZRot(0f);
		entity.setTargetTongueXRot(0f);
	}

	private void setTargetFireBreath(Razorleaf entity, float ageInTicks, float netHeadYaw, float headPitch) {
		// Лепестки широко открыты для дыхания огнем - плавно
		float petalOpen = 0.15f;

		entity.setTargetPetal1XRot(petalOpen);
		entity.setTargetPetal1ZRot(-petalOpen);
		entity.setTargetPetal2XRot(-petalOpen);
		entity.setTargetPetal2ZRot(-petalOpen);
		entity.setTargetPetal3XRot(-petalOpen);
		entity.setTargetPetal3ZRot(petalOpen);
		entity.setTargetPetal4XRot(petalOpen);
		entity.setTargetPetal4ZRot(petalOpen);

		entity.setTargetHeadXRot(0.9F);

		// Рот открыт
		entity.setTargetTongueYRot(0f);

		// Пульсация при дыхании огнем
		float pulse = Mth.sin(ageInTicks * 0.3f) * 0.1f;
		entity.setTargetTongueZRot(pulse);
		entity.setTargetTongueXRot(0f);

		// Минимальное движение стебля и листьев
		entity.setTargetStemYRot(0f);
		entity.setTargetLeaves1XRot(0f);
		entity.setTargetLeaves2XRot(0f);
		entity.setTargetLeaves3XRot(0f);
		entity.setTargetLeaf1ZRot(0f);
		entity.setTargetLeaf2ZRot(0f);
		entity.setTargetLeaf3ZRot(0f);
		entity.setTargetLeaf4ZRot(0f);
		entity.setTargetLeaf5ZRot(0f);
		entity.setTargetLeaf6ZRot(0f);
	}

	private void setTargetPetalSpin(Razorleaf entity, float ageInTicks) {
		float spinSpeed = 25.0f;

		// Лепестки открываются плавно
		float petalOpen = 0.15f;

		entity.setTargetPetal1XRot(petalOpen);
		entity.setTargetPetal1ZRot(-petalOpen);
		entity.setTargetPetal2XRot(-petalOpen);
		entity.setTargetPetal2ZRot(-petalOpen);
		entity.setTargetPetal3XRot(-petalOpen);
		entity.setTargetPetal3ZRot(petalOpen);
		entity.setTargetPetal4XRot(petalOpen);
		entity.setTargetPetal4ZRot(petalOpen);

		// Вращение стебля (листьев) - плавно
		entity.setTargetStemYRot((float) Math.toRadians(ageInTicks * spinSpeed * 0.5f)); // Уменьшаем скорость

		// Листья следуют за вращением стебля
		entity.setTargetLeaves1XRot(0f);
		entity.setTargetLeaves2XRot(0f);
		entity.setTargetLeaves3XRot(0f);
		entity.setTargetLeaf1ZRot(0f);
		entity.setTargetLeaf2ZRot(0f);
		entity.setTargetLeaf3ZRot(0f);
		entity.setTargetLeaf4ZRot(0f);
		entity.setTargetLeaf5ZRot(0f);
		entity.setTargetLeaf6ZRot(0f);

		// Язык убран
		entity.setTargetTongueYRot(0f);
		entity.setTargetTongueZRot(0f);
		entity.setTargetTongueXRot(0f);
		entity.setTargetHeadXRot(0.0F);
	}

	private void setTargetExtinguished(Razorleaf entity) {
		// Все лепестки и листья опущены плавно
		float petalOpen = 0.5f;

		entity.setTargetPetal1XRot(petalOpen);
		entity.setTargetPetal1ZRot(-petalOpen);
		entity.setTargetPetal2XRot(-petalOpen);
		entity.setTargetPetal2ZRot(-petalOpen);
		entity.setTargetPetal3XRot(-petalOpen);
		entity.setTargetPetal3ZRot(petalOpen);
		entity.setTargetPetal4XRot(petalOpen);
		entity.setTargetPetal4ZRot(petalOpen);

		entity.setTargetLeaf1ZRot(-petalOpen*0.25f);
		entity.setTargetLeaf2ZRot(petalOpen*0.25f);
		entity.setTargetLeaf3ZRot(-petalOpen*0.25f);
		entity.setTargetLeaf4ZRot(-petalOpen*0.25f);
		entity.setTargetLeaf5ZRot(-petalOpen*0.25f);
		entity.setTargetLeaf6ZRot(petalOpen*0.25f);
		float yOffset = 2f;
		leaf1.y = yOffset;
		leaf2.y = yOffset;
		leaf3.y = yOffset;
		leaf4.y = yOffset;
		leaf5.y = yOffset;
		leaf6.y = yOffset;

		entity.setTargetTongueYRot(0f);
		entity.setTargetTongueZRot(0f);
		entity.setTargetTongueXRot(0f);
		entity.setTargetHeadXRot(0.0F);
	}

	private void updateTonguePosition(Razorleaf entity, float extension) {
		// Позиция языка зависит от выдвижения
		this.tongue.x = 0f;
		this.tongue.y = 0f;
		this.tongue.z = 0f;
		this.tongue.yRot = 0f;
		this.tongue.xRot = 0.6f * extension; // Двигаем язык вперед
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		body.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
	}
}