package net.dainplay.rpgworldmod.effect;

import net.dainplay.rpgworldmod.RPGworldMod;
import net.dainplay.rpgworldmod.enchantment.ModEnchantments;
import net.dainplay.rpgworldmod.item.custom.BlazeStaffItem;
import net.dainplay.rpgworldmod.item.custom.BrainCoralStaffItem;
import net.dainplay.rpgworldmod.item.custom.BubbleCoralStaffItem;
import net.dainplay.rpgworldmod.item.custom.EmberScrollItem;
import net.dainplay.rpgworldmod.item.custom.FireCoralStaffItem;
import net.dainplay.rpgworldmod.item.custom.HeartOfTheSeaScrollItem;
import net.dainplay.rpgworldmod.item.custom.HornCoralStaffItem;
import net.dainplay.rpgworldmod.item.custom.LivingWoodStaffItem;
import net.dainplay.rpgworldmod.item.custom.TubeCoralStaffItem;
import net.dainplay.rpgworldmod.network.ClientAdditionalHealthCostData;
import net.dainplay.rpgworldmod.network.ClientItemTargetData;
import net.dainplay.rpgworldmod.network.ClientStorageTargetData;
import net.dainplay.rpgworldmod.network.IgniteSelfPacket;
import net.dainplay.rpgworldmod.network.LeftClickWhileRightClickUsePacket;
import net.dainplay.rpgworldmod.network.UseOnAnimateTargetPacket;
import net.dainplay.rpgworldmod.network.ClientAnimateTargetData;
import net.dainplay.rpgworldmod.network.ModMessages;
import net.dainplay.rpgworldmod.network.UseOnItemStorageBlockTargetPacket;
import net.dainplay.rpgworldmod.network.UseOnItemStorageEntityTargetPacket;
import net.dainplay.rpgworldmod.network.UseOnItemTargetPacket;
import net.dainplay.rpgworldmod.sounds.RPGSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Mod.EventBusSubscriber(modid = RPGworldMod.MOD_ID, value = Dist.CLIENT)
public class ParalysisHandler {

	private static boolean wasAttackKeyPressed = false;

	@SubscribeEvent
	public static void onScroll(InputEvent.MouseScrollingEvent event) {
		Player player = Minecraft.getInstance().player;
		if (player != null) {
			if (hasParalysisEffect(player)) {

				Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(RPGSounds.PARALISED.get(), 1.0F, 0.1F));
				event.setCanceled(true); // Cancel the mouse scroll event
			}
		}
	}

	@SubscribeEvent
	public static void onMouseInput(InputEvent.MouseButton.Post event) {
		handleAttackKey();
	}

	@SubscribeEvent
	public static void onClickInput(InputEvent.InteractionKeyMappingTriggered event) {
		Player player = Minecraft.getInstance().player;
		if (player != null && hasParalysisEffect(player) && event.isPickBlock()) {
			Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(RPGSounds.PARALISED.get(), 1.0F, 0.1F));
			event.setCanceled(true);
		}
	}

	@SubscribeEvent
	public static void onKeyInput(InputEvent.Key event) {

		handleAttackKey();
		Player player = Minecraft.getInstance().player;
		if (player != null && hasParalysisEffect(player)) {
			if (Minecraft.getInstance().options.keyHotbarSlots[0].consumeClick()) {

				Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(RPGSounds.PARALISED.get(), 1.0F, 0.1F));
				Minecraft.getInstance().options.keyHotbarSlots[0].setDown(false);
				Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(RPGSounds.PARALISED.get(), 1.0F, 0.1F));
			}
			if (Minecraft.getInstance().options.keyHotbarSlots[1].consumeClick()) {

				Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(RPGSounds.PARALISED.get(), 1.0F, 0.1F));
				Minecraft.getInstance().options.keyHotbarSlots[1].setDown(false);
				Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(RPGSounds.PARALISED.get(), 1.0F, 0.1F));
			}
			if (Minecraft.getInstance().options.keyHotbarSlots[2].consumeClick()) {

				Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(RPGSounds.PARALISED.get(), 1.0F, 0.1F));
				Minecraft.getInstance().options.keyHotbarSlots[2].setDown(false);
			}
			if (Minecraft.getInstance().options.keyHotbarSlots[3].consumeClick()) {

				Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(RPGSounds.PARALISED.get(), 1.0F, 0.1F));
				Minecraft.getInstance().options.keyHotbarSlots[3].setDown(false);
			}
			if (Minecraft.getInstance().options.keyHotbarSlots[4].consumeClick()) {

				Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(RPGSounds.PARALISED.get(), 1.0F, 0.1F));
				Minecraft.getInstance().options.keyHotbarSlots[4].setDown(false);
			}
			if (Minecraft.getInstance().options.keyHotbarSlots[5].consumeClick()) {

				Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(RPGSounds.PARALISED.get(), 1.0F, 0.1F));
				Minecraft.getInstance().options.keyHotbarSlots[5].setDown(false);
			}
			if (Minecraft.getInstance().options.keyHotbarSlots[6].consumeClick()) {

				Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(RPGSounds.PARALISED.get(), 1.0F, 0.1F));
				Minecraft.getInstance().options.keyHotbarSlots[6].setDown(false);
			}
			if (Minecraft.getInstance().options.keyHotbarSlots[7].consumeClick()) {

				Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(RPGSounds.PARALISED.get(), 1.0F, 0.1F));
				Minecraft.getInstance().options.keyHotbarSlots[7].setDown(false);
			}
			if (Minecraft.getInstance().options.keyHotbarSlots[8].consumeClick()) {

				Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(RPGSounds.PARALISED.get(), 1.0F, 0.1F));
				Minecraft.getInstance().options.keyHotbarSlots[8].setDown(false);
			}
			if (Minecraft.getInstance().options.keySwapOffhand.consumeClick()) {

				Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(RPGSounds.PARALISED.get(), 1.0F, 0.1F));
				Minecraft.getInstance().options.keySwapOffhand.setDown(false);
			}
			if (Minecraft.getInstance().options.keyDrop.consumeClick()) {

				Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(RPGSounds.PARALISED.get(), 1.0F, 0.1F));
				Minecraft.getInstance().options.keyDrop.setDown(false);
			}
		}
	}

	@SubscribeEvent
	public static void onMouseClickInInventory(InputEvent.MouseButton event) {
		Player player = Minecraft.getInstance().player;
		if (player != null) {
			if (hasParalysisEffect(player) && Minecraft.getInstance().screen instanceof AbstractContainerScreen) {
				if (event.getAction() == GLFW.GLFW_PRESS)
					Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(RPGSounds.PARALISED.get(), 1.0F, 0.1F));
				event.setCanceled(true); // Cancel the mouse scroll event
			}
		}
	}

	@SubscribeEvent
	public static void onInputInInventory(InputEvent.Key event) {
		Player player = Minecraft.getInstance().player;
		if (player != null) {
			if (hasParalysisEffect(player) && (Minecraft.getInstance().screen instanceof ContainerScreen || Minecraft.getInstance().screen instanceof InventoryScreen)
					&& (Minecraft.getInstance().options.keyDrop.getKey().getValue() == event.getKey()
					|| Minecraft.getInstance().options.keyPickItem.getKey().getValue() == event.getKey()
					|| Minecraft.getInstance().options.keySwapOffhand.getKey().getValue() == event.getKey()
					|| Minecraft.getInstance().options.keyHotbarSlots[0].getKey().getValue() == event.getKey()
					|| Minecraft.getInstance().options.keyHotbarSlots[1].getKey().getValue() == event.getKey()
					|| Minecraft.getInstance().options.keyHotbarSlots[2].getKey().getValue() == event.getKey()
					|| Minecraft.getInstance().options.keyHotbarSlots[3].getKey().getValue() == event.getKey()
					|| Minecraft.getInstance().options.keyHotbarSlots[4].getKey().getValue() == event.getKey()
					|| Minecraft.getInstance().options.keyHotbarSlots[5].getKey().getValue() == event.getKey()
					|| Minecraft.getInstance().options.keyHotbarSlots[6].getKey().getValue() == event.getKey()
					|| Minecraft.getInstance().options.keyHotbarSlots[7].getKey().getValue() == event.getKey()
					|| Minecraft.getInstance().options.keyHotbarSlots[8].getKey().getValue() == event.getKey())) {

				Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(RPGSounds.PARALISED.get(), 1.0F, 0.1F));
				//event.isCanceled();
			}
		}
	}

	private static boolean hasParalysisEffect(Player player) {
		if (!EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(player)) return false;
		for (MobEffectInstance effect : player.getActiveEffects()) {
			MobEffect mobEffect = effect.getEffect();
			if (mobEffect == ModEffects.PARALYSIS.get()) {
				return true;
			}
		}
		return false;
	}


	private static void handleAttackKey() {
		Minecraft mc = Minecraft.getInstance();
		Player player = mc.player;

		if (player == null) return;

		boolean isAttackKeyPressed = mc.options.keyAttack.isDown();

		// Проверяем, была ли нажата кнопка атаки
		if (isAttackKeyPressed && !wasAttackKeyPressed) {
			if (player.isUsingItem()) {
				ItemStack useItem = player.getUseItem();

				if (useItem.getItem() instanceof EmberScrollItem scroll) {
					if (EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.ILLUSION.get(), useItem) > 0 && ClientAnimateTargetData.get() != null) {
						ModMessages.sendToServer(new UseOnAnimateTargetPacket(player.getId(), ClientAnimateTargetData.get().getId()));
						player.swing(player.getUsedItemHand());
					}
					if (EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.NECROMANCY.get(), useItem) > 0 && !player.isShiftKeyDown() && Mth.ceil(player.getHealth()) >= (ClientAdditionalHealthCostData.get() + scroll.getManaCost(useItem, player))) {
						ModMessages.sendToServer(new IgniteSelfPacket(player.getId(), ClientAdditionalHealthCostData.get() + scroll.getManaCost(useItem, player)));
						player.swing(player.getUsedItemHand());
					}
				}

				if (useItem.getItem() instanceof HeartOfTheSeaScrollItem scroll) {
					if (EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.ILLUSION.get(), useItem) > 0 && ClientAnimateTargetData.get() != null) {
						ModMessages.sendToServer(new UseOnAnimateTargetPacket(player.getId(), ClientAnimateTargetData.get().getId()));
						player.swing(player.getUsedItemHand());
					}
					if (EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.DESTRUCTION.get(), useItem) > 0
							&& ClientAnimateTargetData.get() != null) {
						ModMessages.sendToServer(new UseOnAnimateTargetPacket(player.getId(), ClientAnimateTargetData.get().getId()));
					}
				}

				if (useItem.getItem() instanceof LivingWoodStaffItem staff && staff.isOffCooldown(useItem, player)) {
					if (ClientAnimateTargetData.get() != null) {
						ModMessages.sendToServer(new UseOnAnimateTargetPacket(player.getId(), ClientAnimateTargetData.get().getId()));
						player.swing(player.getUsedItemHand());
					}
				}

				if (useItem.getItem() instanceof BlazeStaffItem staff && staff.isOffCooldown(useItem, player)) {
					ModMessages.sendToServer(new LeftClickWhileRightClickUsePacket(player.getId()));
					player.swing(player.getUsedItemHand());
				}

				if ((useItem.getItem() instanceof BrainCoralStaffItem brainCoralStaff && brainCoralStaff.isOffCooldown(useItem, player))
						|| (useItem.getItem() instanceof TubeCoralStaffItem tubeCoralStaff && tubeCoralStaff.isOffCooldown(useItem, player))
						|| (useItem.getItem() instanceof FireCoralStaffItem fireCoralStaff && fireCoralStaff.isOffCooldown(useItem, player))
						|| (useItem.getItem() instanceof BubbleCoralStaffItem bubbleCoralStaff && bubbleCoralStaff.isOffCooldown(useItem, player))) {
					List<ItemEntity> targets = ClientItemTargetData.getTargets();
					if (targets != null && !targets.isEmpty()) {
						List<Integer> targetIds = targets.stream()
								.filter(Objects::nonNull)
								.map(Entity::getId)
								.collect(Collectors.toList());
						ModMessages.sendToServer(new UseOnItemTargetPacket(player.getId(), targetIds));
						ClientItemTargetData.clear();
						player.swing(player.getUsedItemHand());
					}
				}

				if (useItem.getItem() instanceof HornCoralStaffItem staff && staff.isOffCooldown(useItem, player)) {
					if (ClientStorageTargetData.getEntityTarget() != null) {
						ModMessages.sendToServer(new UseOnItemStorageEntityTargetPacket(player.getId(), ClientStorageTargetData.getEntityTarget().getId()));
						player.swing(player.getUsedItemHand());
					}
					else if (ClientStorageTargetData.getBlockTarget() != null) {
						ModMessages.sendToServer(new UseOnItemStorageBlockTargetPacket(player.getId(), ClientStorageTargetData.getBlockTarget()));
						player.swing(player.getUsedItemHand());
					}
				}
			}
		}

		wasAttackKeyPressed = isAttackKeyPressed;
	}

}