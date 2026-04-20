package net.dainplay.rpgworldmod.data.tags;

import com.google.gson.JsonObject;
import net.dainplay.rpgworldmod.RPGworldMod;
import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public class SpellRestorationNetherStarTrigger extends SimpleCriterionTrigger<SpellRestorationNetherStarTrigger.Instance> {
	public static final ResourceLocation ID = RPGworldMod.prefix("spell_restoration_nether_star");

	@Override
	public ResourceLocation getId() {
		return ID;
	}

	@Override
	public Instance createInstance(JsonObject json, ContextAwarePredicate player, DeserializationContext condition) {
		return new SpellRestorationNetherStarTrigger.Instance(player);
	}

	public void trigger(ServerPlayer player) {
		this.trigger(player, (instance) -> true);
	}

	public static class Instance extends AbstractCriterionTriggerInstance {
		public Instance(ContextAwarePredicate player) {
			super(SpellRestorationNetherStarTrigger.ID, player);
		}

	}
}
