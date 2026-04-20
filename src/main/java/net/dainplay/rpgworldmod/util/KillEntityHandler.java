package net.dainplay.rpgworldmod.util;

import net.dainplay.rpgworldmod.entity.custom.Burr_purr;
import net.dainplay.rpgworldmod.entity.custom.Drillhog;
import net.dainplay.rpgworldmod.entity.custom.Fireflantern;
import net.dainplay.rpgworldmod.entity.custom.Mintobat;
import net.dainplay.rpgworldmod.entity.custom.Razorleaf;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class KillEntityHandler {
	@SubscribeEvent
	public static void onLivingDeath(LivingDeathEvent event) {
		LivingEntity entity = event.getEntity();

		if (!(entity instanceof Mintobat)
				&& !(entity instanceof Burr_purr)
				&& !(entity instanceof Razorleaf)
				&& !(entity instanceof Fireflantern)
				&& !(entity instanceof Drillhog)) {
			return;
		}

		Entity killer = event.getSource().getEntity();


		if (!(killer instanceof ServerPlayer serverPlayer)) {
			return;
		}

		ResourceLocation vanillaAdvancementId = new ResourceLocation("adventure/kill_a_mob");

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
