package net.dainplay.rpgworldmod.data.tags;

import com.google.gson.JsonObject;
import net.dainplay.rpgworldmod.RPGworldMod;
import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public class SwitchFireCatherToHungryModeTrigger extends SimpleCriterionTrigger<SwitchFireCatherToHungryModeTrigger.Instance> {

    public static final ResourceLocation ID = RPGworldMod.prefix("switch_fire_cather_to_hungry_mode");

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    public Instance createInstance(JsonObject json, ContextAwarePredicate player, DeserializationContext condition) {
		return new SwitchFireCatherToHungryModeTrigger.Instance(player);
    }

    public void trigger(ServerPlayer player) {
       this.trigger(player, (instance) -> true);
    }

    public static class Instance extends AbstractCriterionTriggerInstance {

        public Instance(ContextAwarePredicate player) {
            super(SwitchFireCatherToHungryModeTrigger.ID, player);
        }

    }
}
