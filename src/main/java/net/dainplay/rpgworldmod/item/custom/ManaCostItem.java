package net.dainplay.rpgworldmod.item.custom;

import net.dainplay.rpgworldmod.network.PlayerManaProvider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.concurrent.atomic.AtomicInteger;

public interface ManaCostItem {

    default int getManaCost() {
        return 0;
    }

    default void updateManaTag(ItemStack stack, Player player) {
        CompoundTag tag = stack.getOrCreateTag();
        boolean hasEnough = hasEnoughMana(player);

        if (!hasEnough) {
            tag.putBoolean("notEnoughMana", true);
        } else {
            tag.remove("notEnoughMana");
            if (tag.isEmpty()) {
                stack.setTag(null);
            }
        }
    }

    default boolean hasEnoughMana(Player player) {
        AtomicInteger playerMana = new AtomicInteger();
        if(player instanceof ServerPlayer) {
            player.getCapability(PlayerManaProvider.PLAYER_MANA).ifPresent(mana -> {
                playerMana.set(mana.getMana());
            });
        }
        return playerMana.get() >= getManaCost();
    }
}