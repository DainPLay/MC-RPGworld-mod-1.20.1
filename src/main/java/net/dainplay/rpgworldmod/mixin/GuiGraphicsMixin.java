package net.dainplay.rpgworldmod.mixin;

import net.dainplay.rpgworldmod.enchantment.ModEnchantments;
import net.dainplay.rpgworldmod.item.custom.BlazeStaffItem;
import net.dainplay.rpgworldmod.item.custom.SculkStaffItem;
import net.dainplay.rpgworldmod.item.custom.StaffItem;
import net.dainplay.rpgworldmod.network.ClientSculkStaffCDData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.Map;

@Mixin(GuiGraphics.class)
public abstract class GuiGraphicsMixin {

	@ModifyVariable(
			method = "renderItemDecorations(Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;IILjava/lang/String;)V",
			at = @At(value = "LOAD"),
			name = "f"
	)
	private float modifyCooldownForStaves(
			float current,
			Font pFont,
			ItemStack pStack,
			int pX,
			int pY,
			@Nullable String pText
	) {

		if(pStack.getItem() instanceof BlazeStaffItem staff
				&& Minecraft.getInstance().player != null) {
			Map<Item, ItemCooldowns.CooldownInstance> cooldownsMap = Minecraft.getInstance().player.getCooldowns().cooldowns;
			int currentTick = Minecraft.getInstance().player.getCooldowns().tickCount;
			ItemCooldowns.CooldownInstance instance = cooldownsMap.get(pStack.getItem());
			if (instance == null) return current;
			int endTick = instance.endTime;
			int cooldown = staff.getMaxCooldown(pStack);
			if (pStack.getEnchantmentLevel(ModEnchantments.DOUBLE_EXPOSURE.get()) > 0) cooldown *= 3;
			return Math.min(1F,(float) (endTick - currentTick) / cooldown);
		} else if(pStack.getItem() instanceof SculkStaffItem staff
				&& Minecraft.getInstance().player != null) {
			Map<Item, ItemCooldowns.CooldownInstance> cooldownsMap = Minecraft.getInstance().player.getCooldowns().cooldowns;
			int currentTick = Minecraft.getInstance().player.getCooldowns().tickCount;
			ItemCooldowns.CooldownInstance instance = cooldownsMap.get(pStack.getItem());
			int cooldown = staff.getMaxCooldown(pStack);
			if (pStack.getEnchantmentLevel(ModEnchantments.DOUBLE_EXPOSURE.get()) > 0) cooldown *= 3;
			if (instance == null)
				return Math.min(1F,(float) ClientSculkStaffCDData.get() / cooldown);
			int endTick = instance.endTime;
			return Math.min(1F,Math.max((float) (endTick - currentTick) / cooldown, (float) ClientSculkStaffCDData.get() / cooldown));
		}
		else if (pStack.getItem() instanceof StaffItem staff
				&& pStack.getEnchantmentLevel(ModEnchantments.DOUBLE_EXPOSURE.get()) > 0
				&& Minecraft.getInstance().player != null) {
			Map<Item, ItemCooldowns.CooldownInstance> cooldownsMap = Minecraft.getInstance().player.getCooldowns().cooldowns;
			int currentTick = Minecraft.getInstance().player.getCooldowns().tickCount;
			ItemCooldowns.CooldownInstance instance = cooldownsMap.get(pStack.getItem());
			if (instance == null) return current;
			int endTick = instance.endTime;
			return Math.min(1F,(float) (endTick - currentTick) / (staff.getMaxCooldown(pStack)*3));
		}
		return current;
	}
}