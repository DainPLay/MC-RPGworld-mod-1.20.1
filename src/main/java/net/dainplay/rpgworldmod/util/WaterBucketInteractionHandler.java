package net.dainplay.rpgworldmod.util;

import net.dainplay.rpgworldmod.entity.custom.Bhlee;
import net.dainplay.rpgworldmod.entity.custom.Gasbass;
import net.dainplay.rpgworldmod.entity.custom.Mossfront;
import net.dainplay.rpgworldmod.entity.custom.Platinumfish;
import net.dainplay.rpgworldmod.entity.custom.Sheentrout;
import net.dainplay.rpgworldmod.item.ModItems;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class WaterBucketInteractionHandler {
	@SubscribeEvent
	public static void onEntityInteractSpecific(PlayerInteractEvent.EntityInteractSpecific event) {
		if (!(event.getTarget() instanceof Mossfront)
				&& !(event.getTarget() instanceof Platinumfish)
				&& !(event.getTarget() instanceof Gasbass)
				&& !(event.getTarget() instanceof Sheentrout)
				&& !(event.getTarget() instanceof Bhlee)) {
			return;
		}

		Player player = event.getEntity();

		if (!(player instanceof ServerPlayer serverPlayer)) {
			return;
		}


		ItemStack itemInHand = player.getItemInHand(event.getHand());
		if (itemInHand.getItem() == ModItems.BHLEE_BUCKET.get()
				|| itemInHand.getItem() == ModItems.MOSSFRONT_BUCKET.get()
				|| itemInHand.getItem() == ModItems.GASBASS_BUCKET.get()
				|| itemInHand.getItem() == ModItems.PLATINUMFISH_BUCKET.get()
				|| itemInHand.getItem() == ModItems.SHEENTROUT_BUCKET.get()) {
			return;
		}

		ResourceLocation vanillaAdvancementId = new ResourceLocation("husbandry/tactical_fishing");

		Advancement vanillaAdvancement = serverPlayer.server.getAdvancements().getAdvancement(vanillaAdvancementId);

		if (vanillaAdvancement == null) {
			return;
		}
		AdvancementProgress advancementprogress = serverPlayer.getAdvancements().getOrStartProgress(vanillaAdvancement);
		if (!advancementprogress.isDone()) {
			for (String s : advancementprogress.getRemainingCriteria()) {
				serverPlayer.getAdvancements().award(vanillaAdvancement, s);
			}

		}

	}
}
