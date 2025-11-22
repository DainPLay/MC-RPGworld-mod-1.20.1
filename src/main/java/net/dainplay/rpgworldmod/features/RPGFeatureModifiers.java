package net.dainplay.rpgworldmod.features;

import net.dainplay.rpgworldmod.RPGworldMod;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecoratorType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class RPGFeatureModifiers {
    public static final DeferredRegister<TreeDecoratorType<?>> TREE_DECORATORS = DeferredRegister.create(ForgeRegistries.TREE_DECORATOR_TYPES, RPGworldMod.MOD_ID);
    public static final RegistryObject<TreeDecoratorType<TrunkDecorator>> TRUNK_DECORATOR = TREE_DECORATORS.register("trunkside_decorator", () -> new TreeDecoratorType<>(TrunkDecorator.CODEC));
}
