package net.dainplay.rpgworldmod.potion;

import net.dainplay.rpgworldmod.RPGworldMod;
import net.dainplay.rpgworldmod.effect.ModEffects;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModPotions {
    public static final DeferredRegister<Potion> POTIONS
            = DeferredRegister.create(ForgeRegistries.POTIONS, RPGworldMod.MOD_ID);

    public static final RegistryObject<Potion> PARALYSIS_POTION = POTIONS.register("paralysis_potion",
            () -> new Potion(new MobEffectInstance(ModEffects.PARALYSIS.get(), 450, 0)));
    public static final RegistryObject<Potion> LONG_PARALYSIS_POTION = POTIONS.register("long_paralysis_potion",
            () -> new Potion(new MobEffectInstance(ModEffects.PARALYSIS.get(), 900, 0)));
    public static final RegistryObject<Potion> STRONG_PARALYSIS_POTION = POTIONS.register("strong_paralysis_potion",
            () -> new Potion(new MobEffectInstance(ModEffects.PARALYSIS.get(), 216, 1)));

    public static final RegistryObject<Potion> MOSSIOSIS_POTION = POTIONS.register("mossiosis_potion",
            () -> new Potion(new MobEffectInstance(ModEffects.MOSSIOSIS.get(), 900, 0)));
    public static final RegistryObject<Potion> LONG_MOSSIOSIS_POTION = POTIONS.register("long_mossiosis_potion",
            () -> new Potion(new MobEffectInstance(ModEffects.MOSSIOSIS.get(), 1800, 0)));
    public static final RegistryObject<Potion> STRONG_MOSSIOSIS_POTION = POTIONS.register("strong_mossiosis_potion",
            () -> new Potion(new MobEffectInstance(ModEffects.MOSSIOSIS.get(), 432, 1)));

    public static final RegistryObject<Potion> ARBOR_FUEL_BOTTLE = POTIONS.register("fueling_potion",
            () -> new Potion(new MobEffectInstance(ModEffects.FUELING.get(), 900, 0)));
    public static final RegistryObject<Potion> LONG_ARBOR_FUEL_BOTTLE = POTIONS.register("long_fueling_potion",
            () -> new Potion(new MobEffectInstance(ModEffects.FUELING.get(), 1800, 0)));


    public static void register(IEventBus eventBus) {
        POTIONS.register(eventBus);
    }
}
