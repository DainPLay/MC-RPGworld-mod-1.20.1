package net.dainplay.rpgworldmod.item.custom;

import net.dainplay.rpgworldmod.RPGworldMod;
import net.dainplay.rpgworldmod.item.ModItems;
import net.dainplay.rpgworldmod.sounds.RPGSounds;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Equipable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;

import javax.annotation.Nullable;
import java.util.List;

public class FireproofSkirtItem extends Item implements Equipable, RPGtooltip {

	public FireproofSkirtItem(Properties pProperties) {
		super(pProperties);
		DispenserBlock.registerBehavior(this, ArmorItem.DISPENSE_ITEM_BEHAVIOR);
	}

	public static boolean isFireproof(ItemStack skirt) {
		return skirt.getDamageValue() < skirt.getMaxDamage() - 1;
	}

	@Override
	public boolean isValidRepairItem(ItemStack pToRepair, ItemStack pRepair) {
		return pRepair.is(ModItems.FIREPROOF_PETALS.get());
	}

	public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pHand) {
		return this.swapWithEquipmentSlot(this, pLevel, pPlayer, pHand);
	}

	@Override
	public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltip, TooltipFlag pFlag) {
		RPGappendHoverText(pStack,pLevel,pTooltip,pFlag);
	}

	@Override
	public SoundEvent getEquipSound() {
		return RPGSounds.ARMOR_EQUIP_FIREPROOF_SKIRT.get();
	}

	@Override
	public EquipmentSlot getEquipmentSlot() {
		return EquipmentSlot.LEGS;
	}

	public static boolean isWornBy(Entity entity) {
		if (!(entity instanceof LivingEntity livingEntity)) {
			return false;
		}
		if (!(livingEntity.getItemBySlot(EquipmentSlot.LEGS).getItem() instanceof FireproofSkirtItem item)) {
			return false;
		}
		return true;
	}

	public static ResourceLocation getTextureLocation() {
		// Верните ResourceLocation текстуры для юбки
		return new ResourceLocation(RPGworldMod.MOD_ID, "textures/models/armor/fireproof_skirt.png");
	}

}
