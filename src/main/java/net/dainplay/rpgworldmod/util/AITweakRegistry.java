package net.dainplay.rpgworldmod.util;

import net.dainplay.rpgworldmod.entity.ModEntities;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

public class AITweakRegistry {
	private final LinkedHashMap<ResourceLocation, List<ITweak>> tweakMap = new LinkedHashMap<>();

	private static AITweakRegistry INSTANCE;

	public static AITweakRegistry instance() {
		if (INSTANCE == null)
			INSTANCE = new AITweakRegistry();
		return INSTANCE;
	}

	public void initializeTweaks() {
		tweakMap.clear();
				registerTweak(new AttackNearestTweak(EntityType.CAT, ModEntities.MINTOBAT.get(), 2, true));
				registerTweak(new AttackNearestTweak(EntityType.OCELOT, ModEntities.MINTOBAT.get(), 2, true));
	}

	public void registerTweak(ITweak event) {
		ResourceLocation entityLocation = event.getEntityLocation();
		if (tweakMap.containsKey(entityLocation)) {
			List<ITweak> dataList = new ArrayList<>(tweakMap.get(entityLocation));
			dataList.add(event);
			tweakMap.put(entityLocation, dataList);
		} else {
			tweakMap.put(entityLocation, Collections.singletonList(event));
		}
	}

	public boolean containsEntity(ResourceLocation entityLocation) {
		return tweakMap.containsKey(entityLocation);
	}

	public List<ITweak> getTweaksFromType(ResourceLocation entityLocation) {
		return tweakMap.containsKey(entityLocation) ? tweakMap.get(entityLocation) : new ArrayList<>();
	}
}