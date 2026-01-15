package net.dainplay.rpgworldmod.damage;

import net.dainplay.rpgworldmod.RPGworldMod;
import net.dainplay.rpgworldmod.util.EntityExcludedDamageSource;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class ModDamageTypes {
    public static final ResourceKey<DamageType> WIDOWEED = create("widoweed");
    public static final ResourceKey<DamageType> FAIRAPIER_SEED = create("fairapier_seed");
    public static final ResourceKey<DamageType> BURR_SPIKE = create("burr_spike");
    public static final ResourceKey<DamageType> SPIKY_IVY = create("spiky_ivy");
    public static final ResourceKey<DamageType> MOSSIOSIS = create("mossiosis");
    public static final ResourceKey<DamageType> MOSQUITOS = create("mosquitos");
    public static final ResourceKey<DamageType> SCREAM = create("scream");
    public static final ResourceKey<DamageType> DING = create("ding");
    public static final ResourceKey<DamageType> SWALLOW = create("swallow");
    public static final ResourceKey<DamageType> ENT_RIE_FRUIT = create("ent_rie_fruit");
    public static final ResourceKey<DamageType> ENT_SNAP = create("ent_snap");
    public static final ResourceKey<DamageType> ENT_ROOTS = create("ent_roots");

    public static ResourceKey<DamageType> create(String name) {
        return ResourceKey.create(Registries.DAMAGE_TYPE, RPGworldMod.prefix(name));
    }

    public static DamageSource getDamageSource(Level level, ResourceKey<DamageType> type, EntityType<?>... toIgnore) {
        return getEntityDamageSource(level, type, null, toIgnore);
    }

    public static DamageSource getEntityDamageSource(Level level, ResourceKey<DamageType> type, @Nullable Entity attacker, EntityType<?>... toIgnore) {
        return getIndirectEntityDamageSource(level, type, attacker, attacker, toIgnore);
    }

    public static DamageSource getIndirectEntityDamageSource(Level level, ResourceKey<DamageType> type, @Nullable Entity attacker, @Nullable Entity indirectAttacker, EntityType<?>... toIgnore) {
        return toIgnore.length > 0 ? new EntityExcludedDamageSource(level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(type), toIgnore) : new DamageSource(level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(type), attacker, indirectAttacker);
    }
            
    public static void bootstrap(BootstapContext<DamageType> context) {
        context.register(WIDOWEED, new DamageType("widoweed", 0.0F));
        context.register(FAIRAPIER_SEED, new DamageType("fairapier_seed", 0.0F));
        context.register(BURR_SPIKE, new DamageType("burr_spike", 0.0F));
        context.register(SPIKY_IVY, new DamageType("spiky_ivy", 0.0F));
        context.register(MOSSIOSIS, new DamageType("mossiosis", 0.0F));
        context.register(MOSQUITOS, new DamageType("mosquitos", 0.0F));
        context.register(SCREAM, new DamageType("scream", 0.0F));
        context.register(DING, new DamageType("ding", 0.0F));
        context.register(SWALLOW, new DamageType("swallow", 0.0F));
        context.register(ENT_RIE_FRUIT, new DamageType("ent_rie_fruit", 0.0F));
        context.register(ENT_SNAP, new DamageType("ent_snap", 0.0F));
        context.register(ENT_ROOTS, new DamageType("ent_roots", 0.0F));
    }
}
