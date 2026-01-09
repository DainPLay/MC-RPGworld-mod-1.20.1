package net.dainplay.rpgworldmod.block.entity;

import net.dainplay.rpgworldmod.RPGworldMod;
import net.dainplay.rpgworldmod.block.ModBlocks;
import net.dainplay.rpgworldmod.block.entity.custom.*;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, RPGworldMod.MOD_ID);

    public static final RegistryObject<BlockEntityType<HoltsReflectionBlockEntity>> HOLTS_REFLECTION_BLOCK_ENTITY = BLOCK_ENTITY_TYPES.register("holts_reflection_block_entity", () ->
            BlockEntityType.Builder.of(HoltsReflectionBlockEntity::new, ModBlocks.HOLTS_REFLECTION.get()).build(null));
    public static final RegistryObject<BlockEntityType<GlossomBlockEntity>> GLOSSOM_BLOCK_ENTITY = BLOCK_ENTITY_TYPES.register("glossom_block_entity", () ->
            BlockEntityType.Builder.of(GlossomBlockEntity::new, ModBlocks.GLOSSOM.get()).build(null));
    public static final RegistryObject<BlockEntityType<TyphonBlockEntity>> TYPHON_BLOCK_ENTITY = BLOCK_ENTITY_TYPES.register("typhon_block_entity", () ->
            BlockEntityType.Builder.of(TyphonBlockEntity::new, ModBlocks.TYPHON.get()).build(null));
    public static final RegistryObject<BlockEntityType<StareblossomBlockEntity>> STAREBLOSSOM_BLOCK_ENTITY = BLOCK_ENTITY_TYPES.register("stareblossom_block_entity", () ->
            BlockEntityType.Builder.of(StareblossomBlockEntity::new, ModBlocks.STAREBLOSSOM.get()).build(null));
    public static final RegistryObject<BlockEntityType<PottedStareblossomBlockEntity>> POTTED_STAREBLOSSOM_BLOCK_ENTITY = BLOCK_ENTITY_TYPES.register("potted_stareblossom_block_entity", () ->
            BlockEntityType.Builder.of(PottedStareblossomBlockEntity::new, ModBlocks.POTTED_STAREBLOSSOM.get()).build(null));
    public static final RegistryObject<BlockEntityType<DirectionalFlowerBlockEntity>> DIRECTIONAL_FLOWER_BLOCK_ENTITY = BLOCK_ENTITY_TYPES.register("directional_flower_block_entity", () ->
            BlockEntityType.Builder.of(DirectionalFlowerBlockEntity::new).build(null));

    public static final RegistryObject<BlockEntityType<BlowerBlockEntity>> BLOWER_BLOCK_ENTITY = BLOCK_ENTITY_TYPES.register("blower",
            () -> BlockEntityType.Builder.of(BlowerBlockEntity::new, ModBlocks.BLOWER.get()).build(null));
    public static final RegistryObject<BlockEntityType<FireCatcherBlockEntity>> FIRE_CATCHER =
            BLOCK_ENTITY_TYPES.register("fire_catcher",
                    () -> BlockEntityType.Builder.of(
                            FireCatcherBlockEntity::new,
                            ModBlocks.FIRE_CATCHER.get()
                    ).build(null));

    public static final RegistryObject<BlockEntityType<TreeHollowBlockEntity>> TREE_HOLLOW_BLOCK_ENTITY = BLOCK_ENTITY_TYPES.register("tree_hollow_block_entity", () ->
            BlockEntityType.Builder.of(TreeHollowBlockEntity::new, ModBlocks.RIE_HOLLOW.get()).build(null));
    public static final RegistryObject<BlockEntityType<FairapierWiltedPlantBlockEntity>> FAIRAPIER_WILTED_PLANT_BLOCK_ENTITY = BLOCK_ENTITY_TYPES.register("fairapier_wilted_plant_block_entity", () ->
            BlockEntityType.Builder.of(FairapierWiltedPlantBlockEntity::new, ModBlocks.FAIRAPIER_WILTED_PLANT.get()).build(null));
    public static final RegistryObject<BlockEntityType<ModSignBlockEntity>> SIGN_BLOCK_ENTITIES = BLOCK_ENTITY_TYPES.register("sign_block_entity", () ->
            BlockEntityType.Builder.of(ModSignBlockEntity::new, ModBlocks.RIE_WALL_SIGN.get(),ModBlocks.RIE_SIGN.get()).build(null));

    public static final RegistryObject<BlockEntityType<ModHangingSignBlockEntity>> HANGING_SIGN_BLOCK_ENTITIES = BLOCK_ENTITY_TYPES.register("hanging_sign_block_entity", () ->
            BlockEntityType.Builder.of(ModHangingSignBlockEntity::new,
                    ModBlocks.RIE_HANGING_SIGN.get(), ModBlocks.RIE_WALL_HANGING_SIGN.get()).build(null));

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITY_TYPES.register(eventBus);
    }

}