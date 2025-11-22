package net.dainplay.rpgworldmod.effect;

import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class FuelingHandler {

    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent event){
        if (!event.getEntity().isSpectator() && (event.getEntity()).hasEffect(ModEffects.FUELING.get())){
            event.getEntity().setSecondsOnFire(5);
        }
    }
}
