package net.dainplay.rpgworldmod.entity.client.render;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.dainplay.rpgworldmod.entity.client.model.BibbitArmorModel;
import net.dainplay.rpgworldmod.entity.client.model.BibbitModel;
import net.dainplay.rpgworldmod.entity.custom.Bibbit;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.DyeableLeatherItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.armortrim.ArmorTrim;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

import javax.annotation.Nullable;
import java.util.*;
import net.minecraftforge.registries.ForgeRegistries;

public class BibbitArmorLayer extends RenderLayer<Bibbit, BibbitModel<Bibbit>> {
   private static final Map<String, ResourceLocation> ARMOR_LOCATION_CACHE = Maps.newHashMap();
   private static final Map<String, BibbitArmorModel> ARMOR_MODEL_CACHE = Maps.newHashMap();

   private final BibbitArmorModel defaultArmorModel;
   private final TextureAtlas armorTrimAtlas;

   public BibbitArmorLayer(RenderLayerParent<Bibbit, BibbitModel<Bibbit>> parent,
                           BibbitArmorModel armorModel, ModelManager modelManager) {
      super(parent);
      this.defaultArmorModel = armorModel;
      this.armorTrimAtlas = modelManager.getAtlas(Sheets.ARMOR_TRIMS_SHEET);
   }

   @Override
   public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight,
                      Bibbit livingEntity, float limbSwing, float limbSwingAmount,
                      float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
      this.renderArmorPiece(poseStack, buffer, livingEntity, EquipmentSlot.LEGS, packedLight);
      this.renderArmorPiece(poseStack, buffer, livingEntity, EquipmentSlot.FEET, packedLight);
   }

   private void renderArmorPiece(PoseStack poseStack, MultiBufferSource buffer,
                                 Bibbit entity, EquipmentSlot slot, int packedLight) {
      ItemStack itemstack = entity.getItemBySlot(slot);

      if (itemstack.getItem() instanceof ArmorItem armorItem) {
         if (armorItem.getEquipmentSlot() == slot) {
            // Получаем кастомную модель брони
            BibbitArmorModel armorModel = getCustomArmorModel(entity, itemstack, slot);

            // Синхронизируем анимацию с основной моделью
            syncAnimationWithParentModel(entity, armorModel);

            // Устанавливаем видимость
            armorModel.setAllVisible(true);

            // Получаем текстуру брони
            ResourceLocation armorResource = getArmorResource(entity, itemstack, slot, null);

            // Рендерим основную текстуру брони
            if (armorItem instanceof DyeableLeatherItem dyeableItem) {
               // Для красимой кожи
               int color = dyeableItem.getColor(itemstack);
               float r = (float)(color >> 16 & 255) / 255.0F;
               float g = (float)(color >> 8 & 255) / 255.0F;
               float b = (float)(color & 255) / 255.0F;

               renderModel(poseStack, buffer, packedLight, armorModel, r, g, b, armorResource);

               // Рендерим накладку
               ResourceLocation overlayResource = getArmorResource(entity, itemstack, slot, "overlay");
               renderModel(poseStack, buffer, packedLight, armorModel, 1.0F, 1.0F, 1.0F, overlayResource);
            } else {
               // Стандартная броня
               renderModel(poseStack, buffer, packedLight, armorModel, 1.0F, 1.0F, 1.0F, armorResource);
            }

            // Рендерим тримы если есть
            renderArmorTrim(entity, poseStack, buffer, packedLight, armorItem, itemstack, armorModel);

            // Рендерим блеск если есть
            if (itemstack.hasFoil()) {
               renderGlint(poseStack, buffer, packedLight, armorModel);
            }
         }
      }
   }

   private BibbitArmorModel getCustomArmorModel(Bibbit entity, ItemStack stack, EquipmentSlot slot) {
      // Пытаемся получить кастомную модель через IClientItemExtensions
      HumanoidModel<?> humanoidModel = IClientItemExtensions.of(stack).getHumanoidArmorModel(
              entity, stack, slot,
              new HumanoidModel<>(Minecraft.getInstance().getEntityModels().bakeLayer(net.minecraft.client.model.geom.ModelLayers.PLAYER_INNER_ARMOR))
      );

      // Если модель не ванильная, преобразуем её
      if (humanoidModel != null) {
         ResourceLocation itemId = ForgeRegistries.ITEMS.getKey(stack.getItem());
         String cacheKey = itemId.toString() + "_" + slot.getName();

         return ARMOR_MODEL_CACHE.computeIfAbsent(cacheKey, key ->
                 convertHumanoidToBibbitModel(humanoidModel, slot)
         );
      }

      // Используем дефолтную модель для ванильной брони
      return defaultArmorModel;
   }

   @SuppressWarnings("unchecked")
   private BibbitArmorModel convertHumanoidToBibbitModel(HumanoidModel<?> humanoidModel, EquipmentSlot slot) {
      // Создаём новую модель BibbitArmorModel на основе HumanoidModel
      ModelPart leftLegPart = null;
      ModelPart rightLegPart = null;

      // Определяем, какие части брать в зависимости от слота
      if (slot == EquipmentSlot.LEGS) {
         // Для поножей используем стандартные ноги
         leftLegPart = humanoidModel.leftLeg;
         rightLegPart = humanoidModel.rightLeg;
      } else if (slot == EquipmentSlot.FEET) {
         // Для обуви пытаемся найти специальные части, или используем стандартные
         leftLegPart = humanoidModel.leftLeg;
         rightLegPart = humanoidModel.rightLeg;

         // Проверяем наличие специальных частей для обуви (shoe) через reflection
         try {
            // Используем java.lang.reflect для проверки полей
            java.lang.reflect.Field leftShoeField = humanoidModel.getClass().getDeclaredField("left_shoe");
            java.lang.reflect.Field rightShoeField = humanoidModel.getClass().getDeclaredField("right_shoe");

            leftShoeField.setAccessible(true);
            rightShoeField.setAccessible(true);

            Object leftShoe = leftShoeField.get(humanoidModel);
            Object rightShoe = rightShoeField.get(humanoidModel);

            if (leftShoe instanceof ModelPart) {
               leftLegPart = (ModelPart) leftShoe;
            }
            if (rightShoe instanceof ModelPart) {
               rightLegPart = (ModelPart) rightShoe;
            }
         } catch (NoSuchFieldException | IllegalAccessException e) {
            // Попробуем другие варианты имен
            try {
               java.lang.reflect.Field leftShoeField = humanoidModel.getClass().getDeclaredField("leftShoe");
               java.lang.reflect.Field rightShoeField = humanoidModel.getClass().getDeclaredField("rightShoe");

               leftShoeField.setAccessible(true);
               rightShoeField.setAccessible(true);

               Object leftShoe = leftShoeField.get(humanoidModel);
               Object rightShoe = rightShoeField.get(humanoidModel);

               if (leftShoe instanceof ModelPart) {
                  leftLegPart = (ModelPart) leftShoe;
               }
               if (rightShoe instanceof ModelPart) {
                  rightLegPart = (ModelPart) rightShoe;
               }
            } catch (NoSuchFieldException | IllegalAccessException e2) {
               // Используем стандартные части, если специальных нет
            }
         }
      }

      // Создаём модель BibbitArmorModel с нужными частями
      return createBibbitArmorModelFromParts(leftLegPart, rightLegPart);
   }

   private BibbitArmorModel createBibbitArmorModelFromParts(ModelPart leftLeg, ModelPart rightLeg) {
      // Клонируем и масштабируем части для Bibbit
      ModelPart leftLegCopy = cloneAndScaleModelPart(leftLeg, 0.75f, 1.9F, 15.0F, 0.5F); // Уменьшаем размер
      ModelPart rightLegCopy = cloneAndScaleModelPart(rightLeg, 0.75f, -1.9F, 15.0F, 0.5F);

      // Сброс поворота к начальному состоянию
      leftLegCopy.setRotation(0, 0, 0);
      rightLegCopy.setRotation(0, 0, 0);

      // Сохраняем начальную позу
      leftLegCopy.setInitialPose(leftLegCopy.storePose());
      rightLegCopy.setInitialPose(rightLegCopy.storePose());

      // Создаём корневой ModelPart с клонированными ногами
      Map<String, ModelPart> children = new HashMap<>();
      children.put("left_leg", leftLegCopy);
      children.put("right_leg", rightLegCopy);

      ModelPart root = new ModelPart(Collections.emptyList(), children);

      return new BibbitArmorModel(root);
   }

   private ModelPart cloneAndScaleModelPart(ModelPart original, float scale, float xOffset, float yOffset, float zOffset) {
      // Создаем глубокую копию ModelPart
      List<ModelPart.Cube> cubesCopy = new ArrayList<>(original.cubes);
      Map<String, ModelPart> childrenCopy = new HashMap<>();

      // Рекурсивно клонируем дочерние части с масштабированием
      for (Map.Entry<String, ModelPart> entry : original.children.entrySet()) {
         childrenCopy.put(entry.getKey(), cloneAndScaleModelPart(entry.getValue(), 1F, 0F, 0F, 0F));
      }

      ModelPart copy = new ModelPart(cubesCopy, childrenCopy);

      // Копируем основные свойства
      copy.copyFrom(original);
      copy.visible = original.visible;
      copy.skipDraw = original.skipDraw;

      // Применяем масштабирование
      copy.xScale *= scale;
      copy.yScale *= scale;
      copy.zScale *= scale;

      copy.setInitialPose(original.getInitialPose());

      //copy.setPos(xOffset, yOffset, zOffset);


      return copy;
   }

   private void syncAnimationWithParentModel(Bibbit entity, BibbitArmorModel armorModel) {
      BibbitModel<Bibbit> parentModel = getParentModel();

      // Копируем вращение ног из родительской модели
      // Бибит имеет особую анимацию ног, которую нужно сохранить
      armorModel.left_leg.xRot = parentModel.left_leg.xRot;
      armorModel.right_leg.xRot = parentModel.right_leg.xRot;
      armorModel.left_leg.zRot = parentModel.left_leg.zRot;
      armorModel.right_leg.zRot = parentModel.right_leg.zRot;

      // Копируем позицию ног
      armorModel.left_leg.x = parentModel.left_leg.x;
      armorModel.left_leg.y = parentModel.left_leg.y;
      armorModel.left_leg.z = parentModel.left_leg.z;

      armorModel.right_leg.x = parentModel.right_leg.x;
      armorModel.right_leg.y = parentModel.right_leg.y;
      armorModel.right_leg.z = parentModel.right_leg.z;

      // Не копируем позицию, так как она уже установлена правильно для Bibbit
      // armorModel уже имеет правильные позиции (1.9F, 18.0F, 0.5F) и (-1.9F, 18.0F, 0.5F)
   }

   private void renderModel(PoseStack poseStack, MultiBufferSource buffer, int packedLight,
                            BibbitArmorModel model, float red, float green, float blue,
                            ResourceLocation texture) {
      VertexConsumer vertexconsumer = buffer.getBuffer(RenderType.entityCutoutNoCull(texture));
      model.renderToBuffer(poseStack, vertexconsumer, packedLight,
              OverlayTexture.NO_OVERLAY, red, green, blue, 1.0F);
   }

   private void renderArmorTrim(Bibbit entity, PoseStack poseStack, MultiBufferSource buffer,
                                int packedLight, ArmorItem armorItem, ItemStack stack,
                                BibbitArmorModel armorModel) {
      Optional<ArmorTrim> trimOptional = ArmorTrim.getTrim(entity.level().registryAccess(), stack);

      if (trimOptional.isPresent()) {
         ArmorTrim trim = trimOptional.get();
         ArmorMaterial material = armorItem.getMaterial();

         // Для ног используем inner texture (layer 2)
         TextureAtlasSprite sprite = armorTrimAtlas.getSprite(trim.outerTexture(material));

         VertexConsumer vertexconsumer = sprite.wrap(buffer.getBuffer(Sheets.armorTrimsSheet()));
         armorModel.renderToBuffer(poseStack, vertexconsumer, packedLight,
                 OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
      }
   }

   private void renderGlint(PoseStack poseStack, MultiBufferSource buffer, int packedLight,
                            BibbitArmorModel model) {
      VertexConsumer vertexconsumer = buffer.getBuffer(RenderType.entityGlint());
      model.renderToBuffer(poseStack, vertexconsumer, packedLight,
              OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
   }

   public ResourceLocation getArmorResource(Bibbit entity, ItemStack stack,
                                            EquipmentSlot slot, @Nullable String type) {
      ArmorItem item = (ArmorItem) stack.getItem();
      String texture = item.getMaterial().getName();
      String domain = "minecraft";
      int idx = texture.indexOf(':');

      if (idx != -1) {
         domain = texture.substring(0, idx);
         texture = texture.substring(idx + 1);
      }

      // Для ног всегда используем layer 2 (inner layer)
      int layer = 1;

      String path = String.format("%s:textures/models/armor/%s_layer_%d%s.png",
              domain, texture, layer, type == null ? "" : "_" + type);

      // Используем Forge hooks для кастомных текстур
      path = net.minecraftforge.client.ForgeHooksClient.getArmorTexture(entity, stack, path, slot, type);

      return ARMOR_LOCATION_CACHE.computeIfAbsent(path, ResourceLocation::new);
   }
}