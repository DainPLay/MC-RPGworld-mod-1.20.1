package net.dainplay.rpgworldmod.data.tags;

import com.google.gson.JsonObject;
import net.dainplay.rpgworldmod.RPGworldMod;
import net.dainplay.rpgworldmod.data.AdvancementData;
import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public class FindRieWealdFirstTrigger extends SimpleCriterionTrigger<FindRieWealdFirstTrigger.Instance> {
	public static final ResourceLocation ID = RPGworldMod.prefix("find_rie_weald_first");

	@Override
	public ResourceLocation getId() {
		return ID;
	}

	@Override
	public Instance createInstance(JsonObject json, ContextAwarePredicate player, DeserializationContext condition) {
		return new FindRieWealdFirstTrigger.Instance(player);
	}

	public void trigger(ServerPlayer player) {
		AdvancementData data = AdvancementData.get(player.getServer());


		if (!data.isRieWealdFound()) {
			this.trigger(player, (instance) -> true);


			data.setRieWealdFound(true);
		}
	}

	public static class Instance extends AbstractCriterionTriggerInstance {
		public Instance(ContextAwarePredicate player) {
			super(FindRieWealdFirstTrigger.ID, player);
		}
	}
}