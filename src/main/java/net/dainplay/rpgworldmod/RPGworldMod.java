package net.dainplay.rpgworldmod;

import net.dainplay.rpgworldmod.biome.RPGworldRegionProvider;
import net.dainplay.rpgworldmod.block.ModBlocks;
import net.dainplay.rpgworldmod.block.entity.ModBlockEntities;
import net.dainplay.rpgworldmod.data.tags.ActionRewardHandler;
import net.dainplay.rpgworldmod.data.tags.ModAdvancements;
import net.dainplay.rpgworldmod.effect.FuelingEffect;
import net.dainplay.rpgworldmod.effect.FuelingHandler;
import net.dainplay.rpgworldmod.effect.ModEffects;
import net.dainplay.rpgworldmod.effect.MossiosisEffect;
import net.dainplay.rpgworldmod.enchantment.ModEnchantments;
import net.dainplay.rpgworldmod.entity.ModEntities;
import net.dainplay.rpgworldmod.entity.projectile.BurrSpikeEntity;
import net.dainplay.rpgworldmod.entity.projectile.FairapierSeedEntity;
import net.dainplay.rpgworldmod.entity.projectile.ProjectruffleArrowEntity;
import net.dainplay.rpgworldmod.features.RPGFeatureModifiers;
import net.dainplay.rpgworldmod.fluid.ModFluidTypes;
import net.dainplay.rpgworldmod.fluid.ModFluids;
import net.dainplay.rpgworldmod.fluid.RPGFluidRegistry;
import net.dainplay.rpgworldmod.item.ModCreativeModeTab;
import net.dainplay.rpgworldmod.item.ModItems;
import net.dainplay.rpgworldmod.item.custom.WealdBladeItem;
import net.dainplay.rpgworldmod.loot.ModLootModifiers;
import net.dainplay.rpgworldmod.particle.ModParticles;
import net.dainplay.rpgworldmod.potion.ModPotions;
import net.dainplay.rpgworldmod.sounds.ModSounds;
import net.dainplay.rpgworldmod.util.*;
import net.dainplay.rpgworldmod.world.feature.RPGFeatures;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Position;
import net.minecraft.core.dispenser.AbstractProjectileDispenseBehavior;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.*;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.brewing.BrewingRecipeRegistry;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.FillBucketEvent;
import net.minecraftforge.event.entity.player.ItemFishedEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import terrablender.api.Regions;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotTypeMessage;
import top.theillusivec4.curios.api.SlotTypePreset;

import java.util.Locale;
import java.util.stream.Collectors;


@Mod(RPGworldMod.MOD_ID)
public class RPGworldMod
{
    public static final String MOD_ID = "rpgworldmod";
    public static final DeferredRegister<SoundEvent> SOUND_EVENT_REGISTER = DeferredRegister.create(Registries.SOUND_EVENT, MOD_ID);
    public static final Logger LOGGER = LogManager.getLogger();
    public RPGworldMod() {
        DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> RPGworldClient::new);

        IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModEntities.ENTITY_TYPES.register(eventBus);


        ModCreativeModeTab.register(eventBus);
        ModEffects.register(eventBus);
        ModPotions.register(eventBus);
        ModItems.register(eventBus);
        ModEnchantments.register(eventBus);
        ModBlocks.register(eventBus);
        ModLootModifiers.register(eventBus);
        ModBlockEntities.register(eventBus);
        ModFluids.register(eventBus);
        ModFluidTypes.register(eventBus);
        ModParticles.PARTICLE_TYPES.register(eventBus);
        RPGFeatures.FEATURES.register(eventBus);
        RPGFeatureModifiers.TREE_DECORATORS.register(eventBus);
        ModSounds.setup();
        ModAdvancements.init();
        eventBus.addListener(this::setup);
        eventBus.addListener(this::processIMC);
        eventBus.addListener(this::enqueueIMC);
        eventBus.addListener(this::addCreative);
        eventBus.addListener(this::loadComplete);
        MinecraftForge.EVENT_BUS.addListener(this::onLivingHurt);
        MinecraftForge.EVENT_BUS.addListener(this::onItemFished);


        SOUND_EVENT_REGISTER.register(eventBus);


        MinecraftForge.EVENT_BUS.register(new TweakReloadManager());
        MinecraftForge.EVENT_BUS.register(new AIHandler());
        MinecraftForge.EVENT_BUS.register(ActionRewardHandler.class);
        MinecraftForge.EVENT_BUS.register(FuelingHandler.class);
        MinecraftForge.EVENT_BUS.register(TriangleAnimationHandler.class);
        MinecraftForge.EVENT_BUS.register(CauldronInteractionHandler.class);
        MinecraftForge.EVENT_BUS.register(WaterBucketInteractionHandler.class);
        MinecraftForge.EVENT_BUS.register(KillEntityHandler.class);
        MinecraftForge.EVENT_BUS.register(FoodSmokeParticlesHandler.class);
        MinecraftForge.EVENT_BUS.register(DrillTuskHandler.class);
        MinecraftForge.EVENT_BUS.register(this);



    }
    public void onItemFished(ItemFishedEvent event) {
        FishingHook hook = event.getHookEntity();
        if (!(hook.getPlayerOwner() instanceof ServerPlayer player)) {
            return;
        }

        if (event.getDrops().isEmpty()) {
            return; // No items were fished up, so we shouldn't trigger the advancement.
        }

        for(ItemStack itemStack : event.getDrops()){

            if (itemStack.getItem() == ModItems.MOSSFRONT.get()
            || itemStack.getItem() == ModItems.PLATINUMFISH.get()
            || itemStack.getItem() == ModItems.GASBASS.get()
            || itemStack.getItem() == ModItems.SHEENTROUT.get()
            || itemStack.getItem() == ModItems.BHLEE.get()) {

                ResourceLocation vanillaAdvancementId = new ResourceLocation("husbandry/fishy_business");

                Advancement vanillaAdvancement = player.server.getAdvancements().getAdvancement(vanillaAdvancementId);

                if (vanillaAdvancement == null) {
                    return;
                }
                AdvancementProgress advancementprogress = player.getAdvancements().getOrStartProgress(vanillaAdvancement);
                if (!advancementprogress.isDone()) {
                    for (String s : advancementprogress.getRemainingCriteria()) {
                        player.getAdvancements().award(vanillaAdvancement, s);
                    }

                }
            }
        }
    }
    private void onLivingHurt(LivingHurtEvent event) {
        if (event.getSource() == null || event.getSource().getEntity() == null || !(event.getSource().getEntity() instanceof LivingEntity)) return;
        LivingEntity entity = event.getEntity();
        Entity attacker = event.getSource().getEntity();
        if (entity == null) return;

        ItemStack itemStack = entity.getUseItem();
        if (itemStack.getItem() instanceof WealdBladeItem blade) {
            entity.playSound(SoundEvents.THORNS_HIT, 1.0F, (entity.getRandom().nextFloat() - entity.getRandom().nextFloat()) * 0.2F + 1.0F);
            attacker.hurt(entity.damageSources().thorns(entity), blade.getDamage()/3);
        }
    }
    public void loadComplete(FMLLoadCompleteEvent event) {
        event.enqueueWork(RPGFluidRegistry::postInit);
    }
    private void addCreative(BuildCreativeModeTabContentsEvent event)
    {
        if(event.getTab() == ModCreativeModeTab.RPGWORLD_BLOCKS_TAB.get()) {
            event.accept(ModBlocks.RIE_LEAVES);
            event.accept(ModBlocks.RIE_LOG);
            event.accept(ModBlocks.RIE_WOOD);
            event.accept(ModBlocks.STRIPPED_RIE_LOG);
            event.accept(ModBlocks.STRIPPED_RIE_WOOD);
            event.accept(ModBlocks.RIE_PLANKS);
            event.accept(ModBlocks.RIE_STAIRS);
            event.accept(ModBlocks.RIE_SLAB);
            event.accept(ModBlocks.RIE_FENCE);
            event.accept(ModBlocks.RIE_FENCE_GATE);
            event.accept(ModBlocks.RIE_DOOR);
            event.accept(ModBlocks.RIE_TRAPDOOR);
            event.accept(ModBlocks.RIE_PRESSURE_PLATE);
            event.accept(ModBlocks.RIE_BUTTON);
            event.accept(ModBlocks.CHISELED_MASKONITE_BLOCK);
            event.accept(ModBlocks.MASKONITE_BLOCK);
            event.accept(ModBlocks.MASKONITE_STAIRS);
            event.accept(ModBlocks.MASKONITE_SLAB);
            event.accept(ModBlocks.MASKONITE_WALL);
            event.accept(ModBlocks.MASKONITE_PRESSURE_PLATE);
            event.accept(ModBlocks.MASKONITE_BUTTON);
            event.accept(ModBlocks.MASKONITE_BRICKS);
            event.accept(ModBlocks.CRACKED_MASKONITE_BRICKS);
            event.accept(ModBlocks.MASKONITE_BRICK_STAIRS);
            event.accept(ModBlocks.MASKONITE_BRICK_SLAB);
            event.accept(ModBlocks.MASKONITE_BRICK_WALL);
            event.accept(ModBlocks.QUARTZITE);
            event.accept(ModBlocks.QUARTZITE_STAIRS);
            event.accept(ModBlocks.QUARTZITE_SLAB);
            event.accept(ModBlocks.QUARTZITE_WALL);
            event.accept(ModBlocks.POLISHED_QUARTZITE);
            event.accept(ModBlocks.POLISHED_QUARTZITE_STAIRS);
            event.accept(ModBlocks.POLISHED_QUARTZITE_SLAB);
            event.accept(ModBlocks.QUARTZITE_BRICKS);
            event.accept(ModBlocks.QUARTZITE_BRICK_STAIRS);
            event.accept(ModBlocks.QUARTZITE_BRICK_SLAB);
            event.accept(ModBlocks.JASPER);
            event.accept(ModBlocks.EMULSION_BLOCK);
            event.accept(ModBlocks.MINTAL_BLOCK);
            event.accept(ModBlocks.WINGOLD_BLOCK);
            event.accept(ModBlocks.MASKONITE_GLASS);
            event.accept(ModBlocks.MASKONITE_GLASS_PANE);
            event.accept(ModItems.RIE_SIGN);
            event.accept(ModItems.RIE_HANGING_SIGN);
            event.accept(ModBlocks.RIE_HOLLOW);
            event.accept(ModBlocks.BLOWER);
            event.accept(ModBlocks.DRILL_TUSK);
            event.accept(ModBlocks.QUARTZITE_DRILL_TUSK);
        }

        if(event.getTab() == ModCreativeModeTab.RPGWORLD_FLORA_TAB.get()) {
            event.accept(ModBlocks.RIE_SAPLING);
            event.accept(ModBlocks.PROJECTRUFFLE);
            event.accept(ModBlocks.SILICINA);
            event.accept(ModBlocks.MOSSHROOM);
            event.accept(ModBlocks.WILD_FAIRAPIER);
            event.accept(ModItems.RPGIROLLE_ITEM);
            event.accept(ModItems.CHEESE_CAP);
            event.accept(ModBlocks.WIDOWEED);
            event.accept(ModBlocks.TRIPLOVER);
            event.accept(ModBlocks.SPIKY_IVY);
            event.accept(ModItems.PARALILY);
            event.accept(ModBlocks.HOLTS_REFLECTION);
            event.accept(ModBlocks.GLOSSOM);
            event.accept(ModBlocks.MIMOSSA);
            event.accept(ModBlocks.TYPHON);
            event.accept(ModItems.FAIRAPIER_SEED);
            event.accept(ModItems.RIE_FRUIT);
            event.accept(ModItems.BRAMBLEFOX_BERRIES);
            event.accept(ModItems.SHIVERALIS_BERRIES);
            event.accept(ModItems.PARALILY_BERRY);
            event.accept(ModItems.PROJECTRUFFLE_ITEM);
        }

        if(event.getTab() == ModCreativeModeTab.RPGWORLD_FAUNA_TAB.get()) {
            event.accept(ModItems.PLATINUMFISH);
            event.accept(ModItems.BHLEE);
            event.accept(ModItems.MOSSFRONT);
            event.accept(ModItems.SHEENTROUT);
            event.accept(ModItems.GASBASS);
        }

        if(event.getTab() == ModCreativeModeTab.RPGWORLD_EQUIPMENT_TAB.get()) {
            event.accept(ModItems.RIE_BOAT);
            event.accept(ModItems.RIE_CHEST_BOAT);
            event.accept(ModItems.MASKONITE_SHOVEL);
            event.accept(ModItems.MASKONITE_PICKAXE);
            event.accept(ModItems.MASKONITE_AXE);
            event.accept(ModItems.MASKONITE_HOE);
            event.accept(ModItems.MASKONITE_SWORD);
            event.accept(ModItems.FLINT_SHOVEL);
            event.accept(ModItems.FLINT_PICKAXE);
            event.accept(ModItems.FLINT_AXE);
            event.accept(ModItems.FLINT_HOE);
            event.accept(ModItems.FLINT_SWORD);
            event.accept(ModItems.WEALD_BLADE);
            event.accept(ModItems.FAIRAPIER_SWORD);
            event.accept(ModItems.FAIRAPIER_SEED);
            event.accept(ModItems.PROJECTRUFFLE_ITEM);
            event.accept(ModItems.MINTAL_TRIANGLE);
            event.accept(ModItems.DRILL_SPEAR);
            event.accept(ModItems.PLATINUMFISH_BUCKET);
            event.accept(ModItems.BHLEE_BUCKET);
            event.accept(ModItems.MOSSFRONT_BUCKET);
            event.accept(ModItems.SHEENTROUT_BUCKET);
            event.accept(ModItems.GASBASS_BUCKET);
            event.accept(ModItems.ARBOR_FUEL_BUCKET);
            event.accept(ModItems.BRAMBLEFOX_SCARF);
            event.accept(ModItems.FIG_LEAF);
            event.accept(PotionUtils.setPotion(new ItemStack(Items.TIPPED_ARROW), ModPotions.PARALYSIS_POTION.get()), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            event.accept(PotionUtils.setPotion(new ItemStack(Items.TIPPED_ARROW), ModPotions.LONG_PARALYSIS_POTION.get()), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            event.accept(PotionUtils.setPotion(new ItemStack(Items.TIPPED_ARROW), ModPotions.STRONG_PARALYSIS_POTION.get()), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            event.accept(PotionUtils.setPotion(new ItemStack(Items.TIPPED_ARROW), ModPotions.MOSSIOSIS_POTION.get()), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            event.accept(PotionUtils.setPotion(new ItemStack(Items.TIPPED_ARROW), ModPotions.LONG_MOSSIOSIS_POTION.get()), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            event.accept(PotionUtils.setPotion(new ItemStack(Items.TIPPED_ARROW), ModPotions.STRONG_MOSSIOSIS_POTION.get()), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            event.accept(PotionUtils.setPotion(new ItemStack(Items.TIPPED_ARROW), ModPotions.ARBOR_FUEL_BOTTLE.get()), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            event.accept(PotionUtils.setPotion(new ItemStack(Items.TIPPED_ARROW), ModPotions.LONG_ARBOR_FUEL_BOTTLE.get()), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);

            event.accept(ModItems.MUSIC_DISC_HOWLING);
            event.accept(ModItems.PORTABLE_TURRET);
            event.accept(ModItems.GUITAR_AX);
        }

        if(event.getTab() == ModCreativeModeTab.RPGWORLD_FOODS_AND_DRINKS_TAB.get()) {
            event.accept(ModItems.RIE_FRUIT);
            event.accept(ModItems.MINT_RIE_FRUIT);
            event.accept(ModItems.BRAMBLEFOX_BERRIES);
            event.accept(ModItems.SHIVERALIS_BERRIES);
            event.accept(ModItems.PARALILY_BERRY);
            event.accept(ModItems.RPGIROLLE_ITEM);
            event.accept(ModItems.CHEESE_CAP);
            event.accept(ModItems.STEELPORK);
            event.accept(ModItems.COOKED_STEELPORK);
            event.accept(ModItems.BHLEE);
            event.accept(ModItems.COOKED_BHLEE);
            event.accept(ModItems.MOSSFRONT);
            event.accept(ModItems.GASBASS);
            ItemStack itemstack = new ItemStack(Items.SUSPICIOUS_STEW);
            SuspiciousStewItem.saveMobEffect(itemstack, ModEffects.MOSSIOSIS.get(), 320);
            event.accept(itemstack, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            event.accept(PotionUtils.setPotion(new ItemStack(Items.POTION), ModPotions.PARALYSIS_POTION.get()), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            event.accept(PotionUtils.setPotion(new ItemStack(Items.POTION), ModPotions.LONG_PARALYSIS_POTION.get()), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            event.accept(PotionUtils.setPotion(new ItemStack(Items.POTION), ModPotions.STRONG_PARALYSIS_POTION.get()), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            event.accept(PotionUtils.setPotion(new ItemStack(Items.POTION), ModPotions.MOSSIOSIS_POTION.get()), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            event.accept(PotionUtils.setPotion(new ItemStack(Items.POTION), ModPotions.LONG_MOSSIOSIS_POTION.get()), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            event.accept(PotionUtils.setPotion(new ItemStack(Items.POTION), ModPotions.STRONG_MOSSIOSIS_POTION.get()), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            event.accept(PotionUtils.setPotion(new ItemStack(Items.POTION), ModPotions.ARBOR_FUEL_BOTTLE.get()), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            event.accept(PotionUtils.setPotion(new ItemStack(Items.POTION), ModPotions.LONG_ARBOR_FUEL_BOTTLE.get()), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            event.accept(PotionUtils.setPotion(new ItemStack(Items.SPLASH_POTION), ModPotions.PARALYSIS_POTION.get()), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            event.accept(PotionUtils.setPotion(new ItemStack(Items.SPLASH_POTION), ModPotions.LONG_PARALYSIS_POTION.get()), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            event.accept(PotionUtils.setPotion(new ItemStack(Items.SPLASH_POTION), ModPotions.STRONG_PARALYSIS_POTION.get()), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            event.accept(PotionUtils.setPotion(new ItemStack(Items.SPLASH_POTION), ModPotions.MOSSIOSIS_POTION.get()), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            event.accept(PotionUtils.setPotion(new ItemStack(Items.SPLASH_POTION), ModPotions.LONG_MOSSIOSIS_POTION.get()), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            event.accept(PotionUtils.setPotion(new ItemStack(Items.SPLASH_POTION), ModPotions.STRONG_MOSSIOSIS_POTION.get()), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            event.accept(PotionUtils.setPotion(new ItemStack(Items.SPLASH_POTION), ModPotions.ARBOR_FUEL_BOTTLE.get()), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            event.accept(PotionUtils.setPotion(new ItemStack(Items.SPLASH_POTION), ModPotions.LONG_ARBOR_FUEL_BOTTLE.get()), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            event.accept(PotionUtils.setPotion(new ItemStack(Items.LINGERING_POTION), ModPotions.PARALYSIS_POTION.get()), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            event.accept(PotionUtils.setPotion(new ItemStack(Items.LINGERING_POTION), ModPotions.LONG_PARALYSIS_POTION.get()), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            event.accept(PotionUtils.setPotion(new ItemStack(Items.LINGERING_POTION), ModPotions.STRONG_PARALYSIS_POTION.get()), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            event.accept(PotionUtils.setPotion(new ItemStack(Items.LINGERING_POTION), ModPotions.MOSSIOSIS_POTION.get()), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            event.accept(PotionUtils.setPotion(new ItemStack(Items.LINGERING_POTION), ModPotions.LONG_MOSSIOSIS_POTION.get()), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            event.accept(PotionUtils.setPotion(new ItemStack(Items.LINGERING_POTION), ModPotions.STRONG_MOSSIOSIS_POTION.get()), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            event.accept(PotionUtils.setPotion(new ItemStack(Items.LINGERING_POTION), ModPotions.ARBOR_FUEL_BOTTLE.get()), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            event.accept(PotionUtils.setPotion(new ItemStack(Items.LINGERING_POTION), ModPotions.LONG_ARBOR_FUEL_BOTTLE.get()), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);

        }

        if(event.getTab() == ModCreativeModeTab.RPGWORLD_MATERIALS_TAB.get()) {
            event.accept(ModBlocks.MASKONITE_BLOCK);
            event.accept(ModItems.TYPHON_DYE);
            event.accept(ModItems.MINTAL_NUGGET);
            event.accept(ModItems.MINTAL_INGOT);
            event.accept(ModItems.SAMARAGUARD);
            event.accept(ModItems.BURR_SPIKE);
            event.accept(ModBlocks.MOSSHROOM);
            event.accept(ModItems.PARALILY_BERRY);
            event.accept(ModItems.MASKONITE_UPGRADE_SMITHING_TEMPLATE);
            event.accept(ModItems.LEAVES_ARMOR_TRIM_SMITHING_TEMPLATE);
            event.accept(EnchantedBookItem.createForEnchantment(new EnchantmentInstance(ModEnchantments.PITCH.get(), ModEnchantments.PITCH.get().getMaxLevel())), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            event.accept(EnchantedBookItem.createForEnchantment(new EnchantmentInstance(ModEnchantments.CRESCENDO.get(), ModEnchantments.CRESCENDO.get().getMaxLevel())), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            event.accept(EnchantedBookItem.createForEnchantment(new EnchantmentInstance(ModEnchantments.STEREO.get(), ModEnchantments.STEREO.get().getMaxLevel())), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            event.accept(EnchantedBookItem.createForEnchantment(new EnchantmentInstance(ModEnchantments.TEMPO.get(), ModEnchantments.TEMPO.get().getMaxLevel())), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            event.accept(EnchantedBookItem.createForEnchantment(new EnchantmentInstance(ModEnchantments.SOUNDPROOF.get(), ModEnchantments.SOUNDPROOF.get().getMaxLevel())), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            event.accept(EnchantedBookItem.createForEnchantment(new EnchantmentInstance(ModEnchantments.COLLECTION.get(), ModEnchantments.COLLECTION.get().getMaxLevel())), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            event.accept(EnchantedBookItem.createForEnchantment(new EnchantmentInstance(ModEnchantments.BLOWING.get(), ModEnchantments.BLOWING.get().getMaxLevel())), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
        }

        if(event.getTab() == ModCreativeModeTab.RPGWORLD_SPAWN_EGGS_TAB.get()) {
            event.accept(ModItems.BIBBIT_SPAWN_EGG);
            event.accept(ModItems.BRAMBLEFOX_SPAWN_EGG);
            event.accept(ModItems.MINTOBAT_SPAWN_EGG);
            event.accept(ModItems.FIREFLANTERN_SPAWN_EGG);
            event.accept(ModItems.BURR_PURR_SPAWN_EGG);
            event.accept(ModItems.DRILLHOG_SPAWN_EGG);
            event.accept(ModItems.MOSQUITO_SWARM_SPAWN_EGG);
            event.accept(ModItems.PLATINUMFISH_SPAWN_EGG);
            event.accept(ModItems.BHLEE_SPAWN_EGG);
            event.accept(ModItems.MOSSFRONT_SPAWN_EGG);
            event.accept(ModItems.SHEENTROUT_SPAWN_EGG);
            event.accept(ModItems.GASBASS_SPAWN_EGG);
        }
    }
    private void setup(final FMLCommonSetupEvent event)
    {
        event.enqueueWork(() -> {
            // Given we only add two biomes, we should keep our weight relatively low.
            Regions.register(new RPGworldRegionProvider(new ResourceLocation(MOD_ID, "overworld_1"),1));

            ComposterBlock.COMPOSTABLES.put(ModBlocks.RIE_LEAVES.get().asItem(), 0.3F);
            ComposterBlock.COMPOSTABLES.put(ModItems.FAIRAPIER_SEED.get().asItem(), 0.3F);
            ComposterBlock.COMPOSTABLES.put(ModItems.RIE_FRUIT.get().asItem(), 0.5F);
            ComposterBlock.COMPOSTABLES.put(ModItems.SHIVERALIS_BERRIES.get().asItem(), 0.3F);
            ComposterBlock.COMPOSTABLES.put(ModItems.PARALILY_BERRY.get().asItem(), 0.3F);
            ComposterBlock.COMPOSTABLES.put(ModItems.RPGIROLLE_ITEM.get().asItem(), 0.65F);
            ComposterBlock.COMPOSTABLES.put(ModBlocks.HOLTS_REFLECTION.get().asItem(), 0.65F);
            ComposterBlock.COMPOSTABLES.put(ModBlocks.GLOSSOM.get().asItem(), 0.65F);
            ComposterBlock.COMPOSTABLES.put(ModBlocks.SILICINA.get().asItem(), 0.65F);
            ComposterBlock.COMPOSTABLES.put(ModBlocks.TRIPLOVER.get().asItem(), 0.3F);
            ComposterBlock.COMPOSTABLES.put(ModBlocks.WILD_FAIRAPIER.get().asItem(), 0.65F);
            ComposterBlock.COMPOSTABLES.put(ModBlocks.MIMOSSA.get().asItem(), 0.65F);
            ComposterBlock.COMPOSTABLES.put(ModBlocks.TYPHON.get().asItem(), 0.65F);
            ComposterBlock.COMPOSTABLES.put(ModBlocks.PARALILY.get().asItem(), 0.65F);
            ComposterBlock.COMPOSTABLES.put(ModBlocks.SPIKY_IVY.get().asItem(), 0.5F);
            ComposterBlock.COMPOSTABLES.put(ModBlocks.RIE_SAPLING.get().asItem(), 0.3F);
            ComposterBlock.COMPOSTABLES.put(ModItems.BRAMBLEFOX_BERRIES.get().asItem(), 0.3F);
            ComposterBlock.COMPOSTABLES.put(ModBlocks.MOSSHROOM.get().asItem(), 0.65F);
            ComposterBlock.COMPOSTABLES.put(ModItems.CHEESE_CAP.get().asItem(), 0.65F);
            ((FlowerPotBlock) Blocks.FLOWER_POT).addPlant(ModBlocks.SHIVERALIS.getId(), ModBlocks.POTTED_SHIVERALIS);
            ((FlowerPotBlock) Blocks.FLOWER_POT).addPlant(ModBlocks.RPGIROLLE.getId(), ModBlocks.POTTED_RPGIROLLE);
            ((FlowerPotBlock) Blocks.FLOWER_POT).addPlant(ModBlocks.PROJECTRUFFLE.getId(), ModBlocks.POTTED_PROJECTRUFFLE);
            ((FlowerPotBlock) Blocks.FLOWER_POT).addPlant(ModBlocks.RIE_SAPLING.getId(), ModBlocks.POTTED_RIE_SAPLING);
            ((FlowerPotBlock) Blocks.FLOWER_POT).addPlant(ModBlocks.SPIKY_IVY.getId(), ModBlocks.POTTED_SPIKY_IVY);
            ((FlowerPotBlock) Blocks.FLOWER_POT).addPlant(ModBlocks.WILD_FAIRAPIER.getId(), ModBlocks.POTTED_WILD_FAIRAPIER);
            ((FlowerPotBlock) Blocks.FLOWER_POT).addPlant(ModBlocks.MIMOSSA.getId(), ModBlocks.POTTED_MIMOSSA);
            ((FlowerPotBlock) Blocks.FLOWER_POT).addPlant(ModBlocks.TYPHON.getId(), ModBlocks.POTTED_TYPHON);
            ((FlowerPotBlock) Blocks.FLOWER_POT).addPlant(ModBlocks.SILICINA.getId(), ModBlocks.POTTED_SILICINA);
            ((FlowerPotBlock) Blocks.FLOWER_POT).addPlant(ModBlocks.HOLTS_REFLECTION.getId(), ModBlocks.POTTED_HOLTS_REFLECTION);
            ((FlowerPotBlock) Blocks.FLOWER_POT).addPlant(ModBlocks.GLOSSOM.getId(), ModBlocks.POTTED_GLOSSOM);
            ((FlowerPotBlock) Blocks.FLOWER_POT).addPlant(ModBlocks.MOSSHROOM.getId(), ModBlocks.POTTED_MOSSHROOM);
            ((FlowerPotBlock) Blocks.FLOWER_POT).addPlant(ModBlocks.CHEESE_CAP.getId(), ModBlocks.POTTED_CHEESE_CAP);
            DispenserBlock.registerBehavior(ModItems.PROJECTRUFFLE_ITEM.get(), new AbstractProjectileDispenseBehavior() {
                protected Projectile getProjectile(Level p_123407_, Position p_123408_, ItemStack p_123409_) {
                    ProjectruffleArrowEntity arrow = new ProjectruffleArrowEntity(ModEntities.PROJECTRUFFLE_ARROW.get(), p_123408_.x(), p_123408_.y(), p_123408_.z(), p_123407_);
                    arrow.pickup = AbstractArrow.Pickup.ALLOWED;
                    return arrow;
                }
            });
            DispenserBlock.registerBehavior(ModItems.FAIRAPIER_SEED.get(), new AbstractProjectileDispenseBehavior() {
                protected Projectile getProjectile(Level p_123407_, Position p_123408_, ItemStack p_123409_) {
                    FairapierSeedEntity arrow = new FairapierSeedEntity(ModEntities.FAIRAPIER_SEED_PROJECTILE.get(), p_123408_.x(), p_123408_.y(), p_123408_.z(), p_123407_);
                    arrow.pickup = AbstractArrow.Pickup.DISALLOWED;
                    return arrow;
                }
            });
            DispenserBlock.registerBehavior(ModItems.BURR_SPIKE.get(), new AbstractProjectileDispenseBehavior() {
                protected Projectile getProjectile(Level p_123407_, Position p_123408_, ItemStack p_123409_) {
                    BurrSpikeEntity arrow = new BurrSpikeEntity(ModEntities.BURR_SPIKE_PROJECTILE.get(), p_123408_.x(), p_123408_.y(), p_123408_.z(), p_123407_);
                    arrow.pickup = AbstractArrow.Pickup.DISALLOWED;
                    return arrow;
                }
            });

            BrewingRecipeRegistry.addRecipe(Ingredient.of(PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.AWKWARD)), Ingredient.of(ModItems.PARALILY_BERRY.get()), PotionUtils.setPotion(new ItemStack(Items.POTION), ModPotions.PARALYSIS_POTION.get()));
            //BrewingRecipeRegistry.addRecipe(new ModBrewingRecipe(Potions.AWKWARD, ModItems.PARALILY_BERRY.get(), ModPotions.PARALYSIS_POTION.get()));
            BrewingRecipeRegistry.addRecipe(new ProperBrewingRecipe(Ingredient.of(createPotion(ModPotions.PARALYSIS_POTION.get())), Ingredient.of(Items.REDSTONE), createPotion(ModPotions.LONG_PARALYSIS_POTION.get())));
            BrewingRecipeRegistry.addRecipe(new ProperBrewingRecipe(Ingredient.of(createPotion(ModPotions.PARALYSIS_POTION.get())), Ingredient.of(Items.GLOWSTONE_DUST), createPotion(ModPotions.STRONG_PARALYSIS_POTION.get())));
            BrewingRecipeRegistry.addRecipe(Ingredient.of(PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.AWKWARD)), Ingredient.of(ModBlocks.MOSSHROOM.get()), PotionUtils.setPotion(new ItemStack(Items.POTION), ModPotions.MOSSIOSIS_POTION.get()));
            //BrewingRecipeRegistry.addRecipe(new ModBrewingRecipe(Potions.AWKWARD, ModBlocks.MOSSHROOM.get().asItem(), ModPotions.MOSSIOSIS_POTION.get()));
            BrewingRecipeRegistry.addRecipe(new ProperBrewingRecipe(Ingredient.of(createPotion(ModPotions.MOSSIOSIS_POTION.get())), Ingredient.of(Items.REDSTONE), createPotion(ModPotions.LONG_MOSSIOSIS_POTION.get())));
            BrewingRecipeRegistry.addRecipe(new ProperBrewingRecipe(Ingredient.of(createPotion(ModPotions.MOSSIOSIS_POTION.get())), Ingredient.of(Items.GLOWSTONE_DUST), createPotion(ModPotions.STRONG_MOSSIOSIS_POTION.get())));
            BrewingRecipeRegistry.addRecipe(new ProperBrewingRecipe(Ingredient.of(createPotion(ModPotions.ARBOR_FUEL_BOTTLE.get())), Ingredient.of(Items.REDSTONE), createPotion(ModPotions.LONG_ARBOR_FUEL_BOTTLE.get())));
        });


    }
    public static ItemStack createPotion(Potion potion) {
        return PotionUtils.setPotion(new ItemStack(Items.POTION), potion);
    }

    private void processIMC(final InterModProcessEvent event)
    {
        LOGGER.info("Got IMC {}", event.getIMCStream().
                map(m->m.getMessageSupplier().get()).
                collect(Collectors.toList()));
    }

    public static ResourceLocation prefix(String name) {
        return new ResourceLocation(MOD_ID, name.toLowerCase(Locale.ROOT));
    }

    public void enqueueIMC(final InterModEnqueueEvent event) {
        SlotTypePreset[] types = {SlotTypePreset.NECKLACE, SlotTypePreset.BELT, SlotTypePreset.BACK};
        for (SlotTypePreset type : types) {
                InterModComms.sendTo(CuriosApi.MODID, SlotTypeMessage.REGISTER_TYPE, () -> type.getMessageBuilder().build());
            }
    }

}
