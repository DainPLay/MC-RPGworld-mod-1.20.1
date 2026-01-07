/*******************************************************************************
 * Copyright 2022, the Glitchfiend Team.
 * All rights reserved.
 ******************************************************************************/
package net.dainplay.rpgworldmod.sounds;

import net.dainplay.rpgworldmod.RPGworldMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.RegistryObject;

import static net.dainplay.rpgworldmod.sounds.RPGSounds.*;

public class ModSounds
{
    public static void setup()
    {
        registerSounds();
    }

    public static void registerSounds()
    {
        MUSIC_BIOME_RIE_WEALD = registerSound("music.overworld.rie_weald");
        MUSIC_BIOME_RIE_WEALD_FOG = registerSound("music.overworld.rie_weald_fog");
        ZAP = registerSound("rpgworldmod.zap");
        PARALISED = registerSound("rpgworldmod.paralised");

        DRILL = registerSound("rpgworldmod.drill");

        DRILLHOG_ATTACK = registerSound("rpgworldmod.drillhog.attack");
        DRILLHOG_DRILL = registerSound("rpgworldmod.drillhog.drill");
        DRILLHOG_HURT = registerSound("rpgworldmod.drillhog.hit");
        DRILLHOG_DEATH = registerSound("rpgworldmod.drillhog.death");
        DRILLHOG_AMBIENT = registerSound("rpgworldmod.drillhog.ambient");

        BURR_PURR_SPIN = registerSound("rpgworldmod.burr_purr.spin");
        BURR_PURR_DASH = registerSound("rpgworldmod.burr_purr.dash");
        BURR_PURR_HURT = registerSound("rpgworldmod.burr_purr.hit");
        BURR_PURR_DEATH = registerSound("rpgworldmod.burr_purr.death");
        BURR_PURR_AMBIENT = registerSound("rpgworldmod.burr_purr.ambient");

        MINTOBAT_ATTACK = registerSound("rpgworldmod.mintobat.attack");
        MINTOBAT_HURT = registerSound("rpgworldmod.mintobat.hit");
        MINTOBAT_DEATH = registerSound("rpgworldmod.mintobat.death");
        MINTOBAT_AMBIENT = registerSound("rpgworldmod.mintobat.ambient");

        RAZORLEAF_HURT = registerSound("rpgworldmod.razorleaf.hurt");
        RAZORLEAF_DEATH = registerSound("rpgworldmod.razorleaf.death");
        RAZORLEAF_ATTACK = registerSound("rpgworldmod.razorleaf.attack");
        RAZORLEAF_FIRE = registerSound("rpgworldmod.razorleaf.fire");
        RAZORLEAF_EXTINGUISH = registerSound("rpgworldmod.razorleaf.extinguish");
        RAZORLEAF_SPIN = registerSound("rpgworldmod.razorleaf.spin");
        RAZORLEAF_BITE = registerSound("rpgworldmod.razorleaf.bite");
        RAZORLEAF_EAT = registerSound("rpgworldmod.razorleaf.eat");
        RAZORLEAF_SPIT = registerSound("rpgworldmod.razorleaf.spit");

        MOSQUITO_SWARM_ATTACK = registerSound("rpgworldmod.mosquito_swarm.attack");
        MOSQUITO_SWARM_DEATH = registerSound("rpgworldmod.mosquito_swarm.death");
        MOSQUITO_SWARM_AMBIENT = registerSound("rpgworldmod.mosquito_swarm.ambient");

        FIREFLANTERN_FALL = registerSound("rpgworldmod.fireflantern.fall");
        FIREFLANTERN_DEATH = registerSound("rpgworldmod.fireflantern.death");
        FIREFLANTERN_IMPACT = registerSound("rpgworldmod.fireflantern.impact");
        FIREFLANTERN_AMBIENT = registerSound("rpgworldmod.fireflantern.ambient");
        FIREFLANTERN_HURT = registerSound("rpgworldmod.fireflantern.hit");

        TRIANGLE_DING = registerSound("rpgworldmod.triangle_ding");
        GUITAR_AX_PLAY = registerSound("rpgworldmod.guitar_ax_play");

        BIBBIT_HURT = registerSound("rpgworldmod.bibbit.hit");
        SITTING_BIBBIT_HURT = registerSound("rpgworldmod.sitting_bibbit.hit");
        BIBBIT_DEATH = registerSound("rpgworldmod.bibbit.death");

        BRAMBLEFOX_HURT = registerSound("rpgworldmod.bramblefox.hit");
        BRAMBLEFOX_DEATH = registerSound("rpgworldmod.bramblefox.death");
        BRAMBLEFOX_AMBIENT = registerSound("rpgworldmod.bramblefox.ambient");
        BRAMBLEFOX_EAT = registerSound("rpgworldmod.bramblefox.eat");
        BRAMBLEFOX_SPIT = registerSound("rpgworldmod.bramblefox.spit");
        BRAMBLEFOX_SCREECH = registerSound("rpgworldmod.bramblefox.screech");
        BRAMBLEFOX_SLEEP = registerSound("rpgworldmod.bramblefox.sleep");
        BRAMBLEFOX_SNIFF = registerSound("rpgworldmod.bramblefox.sniff");
        BRAMBLEFOX_AGGRO = registerSound("rpgworldmod.bramblefox.aggro");

        TREE_HOLLOW_PUT = registerSound("rpgworldmod.hollow_put");
        TREE_HOLLOW_GET = registerSound("rpgworldmod.hollow_get");

        BURR_SPIKE_HIT = registerSound("rpgworldmod.burr_spike_hit");
        BURR_SPIKE_MISS = registerSound("rpgworldmod.burr_spike_miss");
        FAIRAPIER_SEED_THROW = registerSound("rpgworldmod.fairapier_seed_throw");
        FAIRAPIER_SEED_HIT = registerSound("rpgworldmod.fairapier_seed_hit");
        FAIRAPIER_SEED_MISS = registerSound("rpgworldmod.fairapier_seed_miss");
        FAIRAPIER_WILTS = registerSound("rpgworldmod.fairapier_wilts");

        MOSSIOSIS_CONVERTED = registerSound("rpgworldmod.mossiosis_converted");

        MUSIC_DISC_HOWLING = registerSound("rpgworldmod.music_disc_howling");

        PLATINUMFISH_AMBIENT = registerSound("rpgworldmod.platinumfish.ambient");
        PLATINUMFISH_DEATH = registerSound("rpgworldmod.platinumfish.death");
        PLATINUMFISH_HURT = registerSound("rpgworldmod.platinumfish.hurt");
        PLATINUMFISH_FLOP = registerSound("rpgworldmod.platinumfish.flop");
        MOSSFRONT_AMBIENT = registerSound("rpgworldmod.mossfront.ambient");
        MOSSFRONT_DEATH = registerSound("rpgworldmod.mossfront.death");
        MOSSFRONT_HURT = registerSound("rpgworldmod.mossfront.hurt");
        MOSSFRONT_FLOP = registerSound("rpgworldmod.mossfront.flop");
        BHLEE_AMBIENT = registerSound("rpgworldmod.bhlee.ambient");
        BHLEE_DEATH = registerSound("rpgworldmod.bhlee.death");
        BHLEE_HURT = registerSound("rpgworldmod.bhlee.hurt");
        BHLEE_FLOP = registerSound("rpgworldmod.bhlee.flop");
        SHEENTROUT_AMBIENT = registerSound("rpgworldmod.sheentrout.ambient");
        SHEENTROUT_DEATH = registerSound("rpgworldmod.sheentrout.death");
        SHEENTROUT_HURT = registerSound("rpgworldmod.sheentrout.hurt");
        SHEENTROUT_FLOP = registerSound("rpgworldmod.sheentrout.flop");
        GASBASS_AMBIENT = registerSound("rpgworldmod.gasbass.ambient");
        GASBASS_DEATH = registerSound("rpgworldmod.gasbass.death");
        GASBASS_HURT = registerSound("rpgworldmod.gasbass.hurt");
        GASBASS_FLOP = registerSound("rpgworldmod.gasbass.flop");

        ARBOR_FUEL_EMULSION = registerSound("rpgworldmod.arbor_fuel_emulsion");

        DRILL_SPEAR_HIT = registerSound("rpgworldmod.drill_spear_hit");
        DRILL_SPEAR_HIT_GROUND = registerSound("rpgworldmod.drill_spear_hit_ground");
        DRILL_SPEAR_THROW = registerSound("rpgworldmod.drill_spear_throw");
        DRILL_SPEAR_RETURN = registerSound("rpgworldmod.drill_spear_return");

        GOLDEN_TOKEN_FAIL = registerSound("rpgworldmod.golden_token_fail");

        ARMOR_EQUIP_LIVING_WOOD = registerSound("rpgworldmod.armor_equip_living_wood");
        ARMOR_EQUIP_FIREPROOF_SKIRT = registerSound("rpgworldmod.armor_equip_fireproof_skirt");

        EMBER_GEM_SNAP = registerSound("rpgworldmod.ember_gem_snap");
        EMBER_GEM_IGNITE_BLOCK = registerSound("rpgworldmod.ember_gem_ignite_block");
        EMBER_GEM_IGNITE_ENTITY = registerSound("rpgworldmod.ember_gem_ignite_entity");
        EMBER_GEM_EXTINGUISH = registerSound("rpgworldmod.ember_gem_extinguish");

        INHALE = registerSound("rpgworldmod.inhale");
    }

    private static RegistryObject<SoundEvent> registerSound(String name)
    {
        ResourceLocation location = new ResourceLocation(RPGworldMod.MOD_ID, name);
        SoundEvent event = SoundEvent.createVariableRangeEvent(location);
        return RPGworldMod.SOUND_EVENT_REGISTER.register(name, () -> event);
    }
}