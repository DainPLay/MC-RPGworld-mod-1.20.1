package net.dainplay.rpgworldmod.item.custom;

import net.dainplay.rpgworldmod.entity.client.model.LivingWoodArmorModel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class LivingWoodArmorItem extends ArmorItem implements RPGtooltip {
	public LivingWoodArmorItem(ArmorMaterial pMaterial, Type pType, Properties pProperties) {
		super(pMaterial, pType, pProperties);
	}

	@Override
	public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltip, TooltipFlag pFlag) {
		RPGappendHoverText(pStack, pLevel, pTooltip, pFlag);
	}

	@Override
	public void initializeClient(Consumer<IClientItemExtensions> consumer) {
		consumer.accept(new IClientItemExtensions() {
			@Override
			public HumanoidModel<LivingEntity> getHumanoidArmorModel(LivingEntity living, ItemStack stack, EquipmentSlot slot, HumanoidModel defaultModel) {
				HumanoidModel layer1Model = new HumanoidModel(new ModelPart(Collections.emptyList(),
						Map.of("head", new LivingWoodArmorModel(Minecraft.getInstance().getEntityModels().bakeLayer(LivingWoodArmorModel.LAYER_LOCATION)).head,
								"hat", new ModelPart(Collections.emptyList(), Collections.emptyMap()),
								"body", new LivingWoodArmorModel(Minecraft.getInstance().getEntityModels().bakeLayer(LivingWoodArmorModel.LAYER_LOCATION)).body,
								"right_arm", new LivingWoodArmorModel(Minecraft.getInstance().getEntityModels().bakeLayer(LivingWoodArmorModel.LAYER_LOCATION)).right_arm,
								"left_arm", new LivingWoodArmorModel(Minecraft.getInstance().getEntityModels().bakeLayer(LivingWoodArmorModel.LAYER_LOCATION)).left_arm,
								"right_leg", new LivingWoodArmorModel(Minecraft.getInstance().getEntityModels().bakeLayer(LivingWoodArmorModel.LAYER_LOCATION)).right_shoe,
								"left_leg", new LivingWoodArmorModel(Minecraft.getInstance().getEntityModels().bakeLayer(LivingWoodArmorModel.LAYER_LOCATION)).left_shoe)));
				HumanoidModel layer2Model = new HumanoidModel(new ModelPart(Collections.emptyList(),
						Map.of("head", new ModelPart(Collections.emptyList(), Collections.emptyMap()),
								"hat", new ModelPart(Collections.emptyList(), Collections.emptyMap()),
								"body", new LivingWoodArmorModel(Minecraft.getInstance().getEntityModels().bakeLayer(LivingWoodArmorModel.LAYER_LOCATION)).waist,
								"right_arm", new ModelPart(Collections.emptyList(), Collections.emptyMap()),
								"left_arm", new ModelPart(Collections.emptyList(), Collections.emptyMap()),
								"right_leg", new LivingWoodArmorModel(Minecraft.getInstance().getEntityModels().bakeLayer(LivingWoodArmorModel.LAYER_LOCATION)).right_leg,
								"left_leg", new LivingWoodArmorModel(Minecraft.getInstance().getEntityModels().bakeLayer(LivingWoodArmorModel.LAYER_LOCATION)).left_leg)));
				return (slot == EquipmentSlot.LEGS ? layer2Model : layer1Model);
			}
		});
	}

	@Override
	public String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, String type) {
		return "rpgworldmod:textures/models/armor/living_wood_layer_" + (this.getType() == Type.LEGGINGS ? 2 : 1) + ".png";
	}

}
