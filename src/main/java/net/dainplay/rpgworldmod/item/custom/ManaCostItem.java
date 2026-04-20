package net.dainplay.rpgworldmod.item.custom;

import net.dainplay.rpgworldmod.enchantment.ModEnchantments;
import net.dainplay.rpgworldmod.network.PlayerManaProvider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.concurrent.atomic.AtomicInteger;

public interface ManaCostItem {
	default int getManaCost(ItemStack item, Player player) {
		return 0;
	}

	default String getDisplayManaCost(ItemStack item, Player player) {
		return "" + getManaCost(item, player);
	}

	default Component getManaCostAdditionalLine(ItemStack item) {
		return Component.literal("");
	}

	default Boolean usesHealthInsteadOfMana(ItemStack item) {
		return item.getEnchantmentLevel(ModEnchantments.NECROMANCY.get()) > 0;
	}

	default void updateManaTag(ItemStack stack, Player player) {
		CompoundTag tag = stack.getOrCreateTag();
		boolean hasEnough = hasEnoughMana(player, stack) || player.getAbilities().instabuild;
		if (usesHealthInsteadOfMana(stack))
			hasEnough = hasEnoughHealth(player, stack) || player.getAbilities().instabuild;

		if (!hasEnough) {
			tag.putBoolean("notEnoughMana", true);
		} else {
			tag.remove("notEnoughMana");
			if (tag.isEmpty()) {
				stack.setTag(null);
			}
		}
	}

	default boolean hasEnoughMana(Player player, ItemStack item) {
		AtomicInteger playerMana = new AtomicInteger();
		if (player instanceof ServerPlayer) {
			player.getCapability(PlayerManaProvider.PLAYER_MANA).ifPresent(mana -> {
				playerMana.set(mana.getMana());
			});
		}
		return playerMana.get() >= getManaCost(item, player);
	}

	default boolean hasEnoughHealth(Player player, ItemStack item) {
		return (int) Math.ceil(player.getHealth()) >= getManaCost(item, player);
	}
}