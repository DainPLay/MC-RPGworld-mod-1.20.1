// LivingWoodStaffItem.java
package net.dainplay.rpgworldmod.item.custom;

import com.mojang.blaze3d.vertex.PoseStack;
import net.dainplay.rpgworldmod.enchantment.ModEnchantments;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Vanishable;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

public class StaffItem extends Item implements RPGtooltip, Vanishable {

	public enum GemType {
		EMBER_GEM("ember_gem"),
		HEART_OF_THE_SEA("heart_of_the_sea"),
		ENDER_EYE("ender_eye"),
		NETHER_STAR("nether_star");

		private final String name;

		GemType(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}

		public static GemType fromName(String name) {
			for (GemType type : values()) {
				if (type.name.equals(name)) {
					return type;
				}
			}
			return EMBER_GEM;
		}
	}

	public StaffItem(Properties properties) {
		super(properties);
	}

	public static GemType getGemType(ItemStack stack) {
		if (stack.hasTag()) {
			CompoundTag tag = stack.getTag();
			if (tag.contains("Gem")) {
				return GemType.fromName(tag.getString("Gem"));
			}
		}
		return GemType.EMBER_GEM;
	}

	public static void setGemType(ItemStack stack, GemType gemType) {
		CompoundTag tag = stack.getOrCreateTag();
		tag.putString("Gem", gemType.getName());
	}

	@Override
	public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltip, TooltipFlag pFlag) {
		DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
				ClientRPGtooltipHandler.appendHoverText(pStack, pLevel, pTooltip, pFlag, this)
		);
	}

	public static ItemStack createForGemType(ItemStack itemStack, GemType gemType) {
		setGemType(itemStack, gemType);
		return itemStack;
	}

	@Override
	public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
		if (ItemStack.isSameItem(oldStack, newStack)) {
			return getGemType(oldStack) != getGemType(newStack);
		} else return true;
	}

	@Override
	public @NotNull String getDescriptionId(ItemStack pStack) {
		return this.getDescriptionId() + "." + getGemType(pStack).getName();
	}

	public boolean isEnchantable(ItemStack pStack) {
		return pStack.getCount() == 1;
	}

	public int getEnchantmentValue() {
		return 1;
	}

	public MutableComponent getDisplayCooldown(ItemStack item) {
		return Component.translatable(((Item) this).getDescriptionId(item) + ".cooldown");
	}

	public boolean hasCooldown(ItemStack item) {
		return true;
	}

	public int getMaxCooldown(ItemStack item) {
		return 15;
	}

	public int getUseCooldown(ItemStack item) {
		return getMaxCooldown(item);
	}

	public PoseStack getUsingPose(ItemStack stack, Player player, PoseStack poseStack, float flip) {
		return poseStack;
	}

	public PoseStack getEffectUsingPose(ItemStack stack, Player player, PoseStack poseStack, float flip) {
		return poseStack;
	}

	public int getColor(ItemStack stack, Entity entity) {
		return -65536;
	}

	public String getTexture(ItemStack stack, Entity entity) {
		if (getGemType(stack) == GemType.EMBER_GEM)
			return "textures/entity/spells/fire";
		else if (getGemType(stack) == GemType.HEART_OF_THE_SEA)
			return "textures/entity/spells/sea";
		else if (getGemType(stack) == GemType.NETHER_STAR)
			return "textures/entity/spells/nether";
		else if (getGemType(stack) == GemType.ENDER_EYE)
			return "textures/entity/spells/ender";
		else
			return null;
	}

	public int getAnimationSpeed(ItemStack stack, Entity entity) {
		if (getGemType(stack) == GemType.EMBER_GEM)
			return 1;
		else if (getGemType(stack) == GemType.HEART_OF_THE_SEA)
			return 2;
		else if (getGemType(stack) == GemType.NETHER_STAR)
			return 2;
		else if (getGemType(stack) == GemType.ENDER_EYE)
			return 2;
		else
			return 1;
	}

	public int getAnimationLength(ItemStack stack, Entity entity) {
		if (getGemType(stack) == GemType.EMBER_GEM)
			return 32;
		else if (getGemType(stack) == GemType.HEART_OF_THE_SEA)
			return 24;
		else if (getGemType(stack) == GemType.NETHER_STAR)
			return 10;
		else if (getGemType(stack) == GemType.ENDER_EYE)
			return 8;
		else
			return 1;
	}

	public float get1XOffset(ItemStack stack, Entity entity) {
		return 0.2F;
	}

	public float get1YOffset(ItemStack stack, Entity entity) {
		if (getGemType(stack) == GemType.EMBER_GEM)
			return 0.6F;
		else if (getGemType(stack) == GemType.HEART_OF_THE_SEA)
			return 0.5F;
		else if (getGemType(stack) == GemType.NETHER_STAR)
			return 0.5F;
		else if (getGemType(stack) == GemType.ENDER_EYE)
			return 0.5F;
		else
			return 0.5F;
	}

	public float get1ZOffset(ItemStack stack, Entity entity) {
		return -0.6F;
	}

	public float getX(ItemStack stack, Entity entity) {
		return -0.035F;
	}

	public float getY(ItemStack stack, Entity entity) {
		return 0.6F;
	}

	public float getZ(ItemStack stack, Entity entity) {
		if (getGemType(stack) == GemType.EMBER_GEM)
			return -0.95F;
		else if (getGemType(stack) == GemType.HEART_OF_THE_SEA)
			return -0.85F;
		else if (getGemType(stack) == GemType.NETHER_STAR)
			return -0.85F;
		else if (getGemType(stack) == GemType.ENDER_EYE)
			return -0.85F;
		else
			return -0.95F;
	}

	public float getZOffset(ItemStack stack, Entity entity) {
		return -0.05F;
	}

	public boolean isOffCooldown(ItemStack item, Player player) {
		if (item.getEnchantmentLevel(ModEnchantments.DOUBLE_EXPOSURE.get()) > 0 && player.getCooldowns().getCooldownPercent(item.getItem(), 0.0F) > 0.0F) {
			Map<Item, ItemCooldowns.CooldownInstance> cooldownsMap = player.getCooldowns().cooldowns;
			ItemCooldowns.CooldownInstance instance = cooldownsMap.get(item.getItem());
			if (instance == null) return true;
			int endTick = instance.endTime;
			int currentTick = player.getCooldowns().tickCount;
			return endTick - currentTick <= getMaxCooldown(item);
		} else return !(player.getCooldowns().getCooldownPercent(item.getItem(), 0.0F) > 0.0F);
	}


	@Override
	public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
		super.inventoryTick(stack, level, entity, slotId, isSelected);

		if (!level.isClientSide && entity instanceof Player player) {
			if (isSelected || player.getOffhandItem() == stack) {
				CompoundTag tag = stack.getOrCreateTag();

				if (!isOffCooldown(stack, player)) {
					tag.putBoolean("onCooldown", true);
				} else {
					tag.remove("onCooldown");
					if (tag.isEmpty()) {
						stack.setTag(null);
					}
				}
			}
		}
	}

}