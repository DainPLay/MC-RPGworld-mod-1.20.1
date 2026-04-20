package net.dainplay.rpgworldmod.util;

import net.dainplay.rpgworldmod.RPGworldMod;
import net.dainplay.rpgworldmod.entity.ModEntities;
import net.dainplay.rpgworldmod.entity.custom.Bhlee;
import net.dainplay.rpgworldmod.entity.custom.Bibbit;
import net.dainplay.rpgworldmod.entity.custom.Bramblefox;
import net.dainplay.rpgworldmod.entity.custom.Burr_purr;
import net.dainplay.rpgworldmod.entity.custom.ConjuredDolphin;
import net.dainplay.rpgworldmod.entity.custom.Drillhog;
import net.dainplay.rpgworldmod.entity.custom.Fireflantern;
import net.dainplay.rpgworldmod.entity.custom.FriendlyRavager;
import net.dainplay.rpgworldmod.entity.custom.FriendlyVex;
import net.dainplay.rpgworldmod.entity.custom.Gasbass;
import net.dainplay.rpgworldmod.entity.custom.Mintobat;
import net.dainplay.rpgworldmod.entity.custom.MosquitoSwarm;
import net.dainplay.rpgworldmod.entity.custom.Mossfront;
import net.dainplay.rpgworldmod.entity.custom.Platinumfish;
import net.dainplay.rpgworldmod.entity.custom.Razorleaf;
import net.dainplay.rpgworldmod.entity.custom.Sheentrout;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = RPGworldMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class EntityHandler {
	@SubscribeEvent
	public static void registerAttributes(EntityAttributeCreationEvent event) {
		event.put(ModEntities.BRAMBLEFOX.get(), Bramblefox.createAttributes().build());
		event.put(ModEntities.MINTOBAT.get(), Mintobat.createAttributes().build());
		event.put(ModEntities.FIREFLANTERN.get(), Fireflantern.createAttributes().build());
		event.put(ModEntities.BURR_PURR.get(), Burr_purr.createAttributes().build());
		event.put(ModEntities.DRILLHOG.get(), Drillhog.createAttributes().build());
		event.put(ModEntities.CONJURED_DOLPHIN.get(), ConjuredDolphin.createAttributes().build());
		event.put(ModEntities.FRIENDLY_VEX.get(), FriendlyVex.createAttributes().build());
		event.put(ModEntities.FRIENDLY_RAVAGER.get(), FriendlyRavager.createAttributes().build());
		event.put(ModEntities.MOSQUITO_SWARM.get(), MosquitoSwarm.createAttributes().build());
		event.put(ModEntities.RAZORLEAF.get(), Razorleaf.createAttributes().build());
		event.put(ModEntities.BIBBIT.get(), Bibbit.createAttributes().build());
		event.put(ModEntities.PLATINUMFISH.get(), Platinumfish.createAttributes().build());
		event.put(ModEntities.MOSSFRONT.get(), Mossfront.createAttributes().build());
		event.put(ModEntities.BHLEE.get(), Bhlee.createAttributes().build());
		event.put(ModEntities.SHEENTROUT.get(), Sheentrout.createAttributes().build());
		event.put(ModEntities.GASBASS.get(), Gasbass.createAttributes().build());
	}
}
