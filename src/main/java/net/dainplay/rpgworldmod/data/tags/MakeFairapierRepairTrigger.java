package net.dainplay.rpgworldmod.data.tags;

import com.google.gson.JsonObject;
import net.dainplay.rpgworldmod.RPGworldMod;
import net.minecraft.advancements.critereon.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public class MakeFairapierRepairTrigger extends SimpleCriterionTrigger<MakeFairapierRepairTrigger.Instance> {

    public static final ResourceLocation ID = RPGworldMod.prefix("fairapier_repaired");

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    public Instance createInstance(JsonObject json, ContextAwarePredicate player, DeserializationContext condition) {
		return new MakeFairapierRepairTrigger.Instance(player);
    }

    public void trigger(ServerPlayer player) {
       this.trigger(player, (instance) -> true);
    }

    public static class Instance extends AbstractCriterionTriggerInstance {

        public Instance(ContextAwarePredicate player) {
            super(MakeFairapierRepairTrigger.ID, player);
        }

    }
}
