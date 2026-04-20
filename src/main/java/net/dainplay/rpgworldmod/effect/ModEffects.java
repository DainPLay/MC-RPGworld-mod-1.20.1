package net.dainplay.rpgworldmod.effect;

import net.dainplay.rpgworldmod.RPGworldMod;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModEffects {
	public static final DeferredRegister<MobEffect> MOB_EFFECTS
			= DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, RPGworldMod.MOD_ID);

	public static final RegistryObject<MobEffect> PARALYSIS = MOB_EFFECTS.register("paralysis",
			() -> new ParalysisEffect(MobEffectCategory.HARMFUL, 16666551));

	public static final RegistryObject<MobEffect> MOB_BECKON = MOB_EFFECTS.register("mob_beckon",
			() -> new MobBeckonEffect(MobEffectCategory.HARMFUL, 0x00000000));

	public static final RegistryObject<MobEffect> MOSSIOSIS = MOB_EFFECTS.register("mossiosis",
			() -> new MossiosisEffect(MobEffectCategory.HARMFUL, 7377453));

	public static final RegistryObject<MobEffect> FUELING = MOB_EFFECTS.register("fueling",
			() -> new FuelingEffect(MobEffectCategory.HARMFUL, 11633736));

	public static final RegistryObject<MobEffect> MOSQUITOING = MOB_EFFECTS.register("mosquitoing",
			() -> new MosquitoingEffect(MobEffectCategory.HARMFUL, 0x00000000));

	public static final RegistryObject<MobEffect> PARANOIA = MOB_EFFECTS.register("paranoia",
			() -> new ParanoiaEffect(MobEffectCategory.HARMFUL, 11699890));

	public static final RegistryObject<MobEffect> HAPPINESS = MOB_EFFECTS.register("happiness",
			() -> new HappinessEffect(MobEffectCategory.BENEFICIAL, 0x00000000));

	public static final RegistryObject<MobEffect> BURN_ILLUSION = MOB_EFFECTS.register("burn_illusion",
			() -> new BurnIllusionEffect(MobEffectCategory.HARMFUL, 0x00000000));

	public static final RegistryObject<MobEffect> NETHER_PORTAL_ILLUSION = MOB_EFFECTS.register("nether_portal_illusion",
			() -> new NetherPortalIllusionEffect(MobEffectCategory.HARMFUL, 0x00000000));

	public static final RegistryObject<MobEffect> NECROSIS = MOB_EFFECTS.register("necrosis",
			() -> new NecrosisEffect(MobEffectCategory.HARMFUL, 0x00000000));

	public static final RegistryObject<MobEffect> BURNOUT = MOB_EFFECTS.register("burnout",
			() -> new BurnoutEffect(MobEffectCategory.NEUTRAL, 0x00000000));

	public static final RegistryObject<MobEffect> AMPHIBIOSIS = MOB_EFFECTS.register("amphibiosis",
			() -> new AmphibiosisEffect(MobEffectCategory.NEUTRAL, 0x1F96B1));

	public static void register(IEventBus eventBus) {
		MOB_EFFECTS.register(eventBus);
	}
}