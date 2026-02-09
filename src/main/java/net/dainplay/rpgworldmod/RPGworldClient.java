package net.dainplay.rpgworldmod;

import net.dainplay.rpgworldmod.block.ModBlocks;
import net.dainplay.rpgworldmod.block.entity.ModBlockEntities;
import net.dainplay.rpgworldmod.block.entity.ModWoodTypes;
import net.dainplay.rpgworldmod.enchantment.ModEnchantments;
import net.dainplay.rpgworldmod.entity.client.model.SkirtModel;
import net.dainplay.rpgworldmod.entity.client.render.CurioLayers;
import net.dainplay.rpgworldmod.entity.client.render.CurioRenderers;
import net.dainplay.rpgworldmod.entity.client.render.SkirtArmorLayer;
import net.dainplay.rpgworldmod.fluid.ModFluids;
import net.dainplay.rpgworldmod.gui.ManaOverlayEventHandler;
import net.dainplay.rpgworldmod.gui.HealthOverlayEventHandler;
import net.dainplay.rpgworldmod.item.ModItems;
import net.dainplay.rpgworldmod.item.custom.FireproofSkirtItem;
import net.dainplay.rpgworldmod.item.custom.MintalTriangleItem;
import net.dainplay.rpgworldmod.render.BakedModelShadeLayerFullbright;
import net.dainplay.rpgworldmod.render.BreakingEntFaceRenderer;
import net.dainplay.rpgworldmod.render.EnchantedBlockRenderer;
import net.dainplay.rpgworldmod.render.PottedStareblossomBlockEntityRenderer;
import net.dainplay.rpgworldmod.render.ScrollGlintItemModel;
import net.dainplay.rpgworldmod.render.ScrollGlintItemModelSupport;
import net.dainplay.rpgworldmod.render.StareblossomBlockEntityRenderer;
import net.dainplay.rpgworldmod.render.BoundCampfireBlockRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.blockentity.HangingSignRenderer;
import net.minecraft.client.renderer.blockentity.SignRenderer;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import java.util.Map;

@Mod.EventBusSubscriber(modid = RPGworldMod.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class RPGworldClient {

    public RPGworldClient() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        modBus.addListener(this::onRegisterLayerDefinitions);
        modBus.addListener(this::guiSetup);
        modBus.addListener(this::bakeModels);
    }

    public void onRegisterLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        CurioLayers.register(event);
    }
    public void guiSetup(final RegisterGuiOverlaysEvent event) {
        //Register Armor Renderer for events
        event.registerAbove(VanillaGuiOverlay.PLAYER_HEALTH.id(),RPGworldMod.MOD_ID+"_hearts_overlay", new HealthOverlayEventHandler());
        event.registerAbove(VanillaGuiOverlay.FOOD_LEVEL.id(),RPGworldMod.MOD_ID+"_mana_overlay", new ManaOverlayEventHandler());
    }

    private void bakeModels(final ModelEvent.ModifyBakingResult e) {
        Map<ResourceLocation, BakedModel> map = e.getModels();
        BakedModel alterationScrollModel = null;
        BakedModel destructionScrollModel = null;
        BakedModel restorationScrollModel = null;
        BakedModel illusionScrollModel = null;
        BakedModel conjurationScrollModel = null;
        BakedModel necromancyScrollModel = null;
        BakedModel alterationEmberScrollModel = null;
        BakedModel destructionEmberScrollModel = null;
        BakedModel restorationEmberScrollModel = null;
        BakedModel illusionEmberScrollModel = null;
        BakedModel conjurationEmberScrollModel = null;
        BakedModel necromancyEmberScrollModel = null;

        BakedModel boundCampfireModel = null;

        for (ResourceLocation id : map.keySet()) {
            String idString = id.toString();
            BakedModel originalModel = map.get(id);

            if (originalModel == null) continue;

            if (idString.startsWith("rpgworldmod:ent_face#")) {
                map.put(id, new BakedModelShadeLayerFullbright(originalModel));
            }

            if (idString.contains("rpgworldmod:item/scroll_alteration")) {
                alterationScrollModel = new ScrollGlintItemModelSupport(originalModel);
            }

            if (idString.contains("rpgworldmod:item/scroll_destruction")) {
                destructionScrollModel = new ScrollGlintItemModelSupport(originalModel);
            }

            if (idString.contains("rpgworldmod:item/scroll_restoration")) {
                restorationScrollModel = new ScrollGlintItemModelSupport(originalModel);
            }

            if (idString.contains("rpgworldmod:item/scroll_illusion")) {
                illusionScrollModel = new ScrollGlintItemModelSupport(originalModel);
            }

            if (idString.contains("rpgworldmod:item/scroll_conjuration")) {
                conjurationScrollModel = new ScrollGlintItemModelSupport(originalModel);
            }

            if (idString.contains("rpgworldmod:item/scroll_necromancy")) {
                necromancyScrollModel = new ScrollGlintItemModelSupport(originalModel);
            }
            if (idString.contains("rpgworldmod:item/ember_scroll_alteration")) {
                alterationEmberScrollModel = new ScrollGlintItemModelSupport(originalModel);
            }

            if (idString.contains("rpgworldmod:item/ember_scroll_destruction")) {
                destructionEmberScrollModel = new ScrollGlintItemModelSupport(originalModel);
            }

            if (idString.contains("rpgworldmod:item/ember_scroll_restoration")) {
                restorationEmberScrollModel = new ScrollGlintItemModelSupport(originalModel);
            }

            if (idString.contains("rpgworldmod:item/ember_scroll_illusion")) {
                illusionEmberScrollModel = new ScrollGlintItemModelSupport(originalModel);
            }

            if (idString.contains("rpgworldmod:item/ember_scroll_conjuration")) {
                conjurationEmberScrollModel = new ScrollGlintItemModelSupport(originalModel);
            }

            if (idString.contains("rpgworldmod:item/ember_scroll_necromancy")) {
                necromancyEmberScrollModel = new ScrollGlintItemModelSupport(originalModel);
            }

            if (idString.contains("rpgworldmod:item/bound_campfire")) {
                boundCampfireModel = new ScrollGlintItemModelSupport(originalModel);
            }
        }

        // Отдельный цикл для свитков, если нужно явное разделение логики
        for (ResourceLocation id : map.keySet()) {
            String idString = id.toString();
            BakedModel originalModel = map.get(id);
            if (idString.contains("rpgworldmod:empty_scroll")) {
                map.put(id, new ScrollGlintItemModel(
                        originalModel,
                        alterationScrollModel == null ? originalModel : alterationScrollModel,
                        restorationScrollModel == null ? originalModel : restorationScrollModel,
                        destructionScrollModel == null ? originalModel : destructionScrollModel,
                        illusionScrollModel == null ? originalModel : illusionScrollModel,
                        conjurationScrollModel == null ? originalModel : conjurationScrollModel,
                        necromancyScrollModel == null ? originalModel : necromancyScrollModel,
                        originalModel
                ));
            }
            if (idString.contains("rpgworldmod:ember_scroll")) {
                map.put(id, new ScrollGlintItemModel(
                        originalModel,
                        alterationEmberScrollModel == null ? originalModel : alterationEmberScrollModel,
                        restorationEmberScrollModel == null ? originalModel : restorationEmberScrollModel,
                        destructionEmberScrollModel == null ? originalModel : destructionEmberScrollModel,
                        illusionEmberScrollModel == null ? originalModel : illusionEmberScrollModel,
                        conjurationEmberScrollModel == null ? originalModel : conjurationEmberScrollModel,
                        necromancyEmberScrollModel == null ? originalModel : necromancyEmberScrollModel,
                        boundCampfireModel == null ? originalModel : boundCampfireModel
                ));
            }
        }
    }

    @SubscribeEvent
    public static void registerAdditionalModels(ModelEvent.RegisterAdditional event) {
        // Регистрируем модели свитков, которые используются в override
        event.register(new ResourceLocation("rpgworldmod:item/scroll_restoration"));
        event.register(new ResourceLocation("rpgworldmod:item/scroll_destruction"));
        event.register(new ResourceLocation("rpgworldmod:item/scroll_illusion"));
        event.register(new ResourceLocation("rpgworldmod:item/scroll_alteration"));
        event.register(new ResourceLocation("rpgworldmod:item/scroll_conjuration"));
        event.register(new ResourceLocation("rpgworldmod:item/scroll_necromancy"));
        event.register(new ResourceLocation("rpgworldmod:item/ember_scroll_restoration"));
        event.register(new ResourceLocation("rpgworldmod:item/ember_scroll_destruction"));
        event.register(new ResourceLocation("rpgworldmod:item/ember_scroll_illusion"));
        event.register(new ResourceLocation("rpgworldmod:item/ember_scroll_alteration"));
        event.register(new ResourceLocation("rpgworldmod:item/ember_scroll_conjuration"));
        event.register(new ResourceLocation("rpgworldmod:item/ember_scroll_necromancy"));
        event.register(new ResourceLocation("rpgworldmod:item/bound_campfire"));
    }

    @SubscribeEvent
    public static void addEntityRendererLayers(EntityRenderersEvent.AddLayers event) {
        EntityRenderDispatcher dispatcher = Minecraft.getInstance()
                .getEntityRenderDispatcher();
        SkirtArmorLayer.registerOnAll(dispatcher, new SkirtModel(event.getEntityModels().bakeLayer(SkirtModel.LAYER_LOCATION)));

    }

    @SuppressWarnings("removal")
    @SubscribeEvent
    public static void clientSetup(final FMLClientSetupEvent event) {
        ItemBlockRenderTypes.setRenderLayer(ModFluids.FLOWING_ARBOR_FUEL.get(), RenderType.translucent());
        ItemBlockRenderTypes.setRenderLayer(ModFluids.SOURCE_ARBOR_FUEL.get(), RenderType.translucent());
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.ARBOR_FUEL_BLOCK.get(), RenderType.translucent());
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.EMULSION_BLOCK.get(), RenderType.translucent());
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.MASKONITE_GLASS.get(), RenderType.translucent());
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.MASKONITE_GLASS_PANE.get(), RenderType.translucent());
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.BOUND_CAMPFIRE.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.ENT_FACE.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.TIRE.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.SHIVERALIS.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.POTTED_SHIVERALIS.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.POTTED_RIE_SAPLING.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.RPGIROLLE.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.FAIRAPIER_PLANT.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.FAIRAPIER_WILTED_PLANT.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.POTTED_RPGIROLLE.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.WILD_FAIRAPIER.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.POTTED_WILD_FAIRAPIER.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.MIMOSSA.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.STAREBLOSSOM.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.TYPHON.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.POTTED_MIMOSSA.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.POTTED_STAREBLOSSOM.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.POTTED_TYPHON.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.POTTED_SILICINA.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.POTTED_GLOSSOM.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.POTTED_HOLTS_REFLECTION.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.PARALILY.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.PROJECTRUFFLE.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.SILICINA.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.RAZORLEAF_BUD.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.YOUNG_RAZORLEAF.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.POTTED_PROJECTRUFFLE.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.HOLTS_REFLECTION.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.GLOSSOM.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.SPIKY_IVY.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.POTTED_SPIKY_IVY.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.WIDOWEED.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.TRIPLOVER.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.RIE_LEAVES.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.RIE_SAPLING.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.RIE_DOOR.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.RIE_TRAPDOOR.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.MOSSHROOM.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.POTTED_MOSSHROOM.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.CHEESE_CAP.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.POTTED_CHEESE_CAP.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.BLOWER.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.WINGOLD_BLOCK.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.LIVING_WOOD_LOG.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.LIVING_WOOD_WOOD.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.STRIPPED_LIVING_WOOD_LOG.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.STRIPPED_LIVING_WOOD_WOOD.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.MOSQUITOS.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.FIRE_CATCHER.get(), RenderType.cutout());


        WoodType.register(ModWoodTypes.RIE_WOOD_TYPE);
        Sheets.addWoodType(ModWoodTypes.RIE_WOOD_TYPE);
        BlockEntityRenderers.register(ModBlockEntities.SIGN_BLOCK_ENTITIES.get(), SignRenderer::new);
        BlockEntityRenderers.register(ModBlockEntities.HANGING_SIGN_BLOCK_ENTITIES.get(), HangingSignRenderer::new);
        BlockEntityRenderers.register(ModBlockEntities.FAIRAPIER_WILTED_PLANT_BLOCK_ENTITY.get(), EnchantedBlockRenderer::new);
        BlockEntityRenderers.register(ModBlockEntities.STAREBLOSSOM_BLOCK_ENTITY.get(), StareblossomBlockEntityRenderer::new);
        BlockEntityRenderers.register(ModBlockEntities.POTTED_STAREBLOSSOM_BLOCK_ENTITY.get(), PottedStareblossomBlockEntityRenderer::new);
        BlockEntityRenderers.register(ModBlockEntities.ENT_FACE_BLOCK_ENTITY.get(), BreakingEntFaceRenderer::new);
        BlockEntityRenderers.register(ModBlockEntities.BOUND_CAMPFIRE_BLOCK_ENTITY.get(), BoundCampfireBlockRenderer::new);
        CurioRenderers.register();


        event.enqueueWork(() -> {

            ItemProperties.register(ModItems.EMBER_SCROLL.get().asItem(), new ResourceLocation( "summoned_object"), (stack, world, entity, seed) -> {
                if (stack.isEmpty()) {
                    return 0.0F;
                }
                CompoundTag tag = stack.getTag();
                if (tag != null && tag.contains("SummonedObject", Tag.TAG_INT)) {
                    return 1.0F;
                }
                return 0.0F;
            });

            ItemProperties.register(ModItems.FAIRAPIER_SWORD.get().asItem(), new ResourceLocation( "growing"), (stack, world, entity, seed) -> {
                if (stack.isEmpty()) {
                    return 0.0F;
                }
                CompoundTag tag = stack.getTag();
                if (tag != null && tag.contains("Growing", Tag.TAG_INT)) {
                    int growingValue = tag.getInt("Growing");
                    if (growingValue == 1) {
                        return 1.0F;
                    } else if (growingValue == 2) {
                        return 2.0F;
                    }
                }
                return 0.0F;
            });

            ItemProperties.register(ModItems.RPGIROLLE_ITEM.get().asItem(), new ResourceLocation( "token"), (stack, world, entity, seed) -> {
                if (stack.isEmpty()) {
                    return 0.0F;
                }
                CompoundTag tag = stack.getTag();
                if (tag != null && tag.contains("Token", Tag.TAG_INT)) {
					return (float) tag.getInt("Token");
                }
                return 0.0F;
            });
            ItemProperties.register(ModItems.WEALD_BLADE.get().asItem(), new ResourceLocation( "blocking"), (stack, world, entity, seed) -> entity != null && entity.isUsingItem() && entity.getUseItem() == stack ? 1.0F : 0.0F);
            ItemProperties.register(ModItems.DRILL_SPEAR.get().asItem(), new ResourceLocation( "throwing"), (stack, world, entity, seed) -> entity != null && entity.isUsingItem() && entity.getUseItem() == stack ? 1.0F : 0.0F);
            ItemProperties.register(ModItems.FIREPROOF_SKIRT.get().asItem(), new ResourceLocation( "broken"), (stack, world, entity, seed) -> FireproofSkirtItem.isFireproof(stack) ? 0.0F : 1.0F);
            ItemProperties.register(ModItems.MINTAL_TRIANGLE.get(), new ResourceLocation("vibration"), (itemstack, level, livingEntity, p_174608_) -> livingEntity != null && MintalTriangleItem.getVibes(itemstack)>0 ? (23F-(float)MintalTriangleItem.getVibes(itemstack))/100 : 1.0F);
            ItemProperties.register(Items.CROSSBOW, new ResourceLocation("projectruffle"), (p_174605_, p_174606_, p_174607_, p_174608_) -> p_174607_ != null && CrossbowItem.isCharged(p_174605_) && CrossbowItem.containsChargedProjectile(p_174605_, ModItems.PROJECTRUFFLE_ITEM.get()) ? 1.0F : 0.0F);
            ItemProperties.register(Items.BOW, new ResourceLocation("projectruffle"), (stack, level, living, id) -> {
                if (stack.hasTag() && stack.getTag().contains("UsingProjectruffle")) {
                    return stack.getTag().getBoolean("UsingProjectruffle") ? 1.0F : 0.0F;
                }
                return 0.0F;
            });

            ItemProperties.register(ModItems.LIVING_WOOD_BOW.get(), new ResourceLocation("pull"), (p_174635_, p_174636_, p_174637_, p_174638_) -> {
                if (p_174637_ == null) {
                    return 0.0F;
                } else {
                    return p_174637_.getUseItem() != p_174635_ ? 0.0F : (float)(p_174635_.getUseDuration() - p_174637_.getUseItemRemainingTicks()) / 20.0F;
                }
            });
            ItemProperties.register(ModItems.LIVING_WOOD_BOW.get(), new ResourceLocation("pulling"), (p_174630_, p_174631_, p_174632_, p_174633_) -> {
                return p_174632_ != null && p_174632_.isUsingItem() && p_174632_.getUseItem() == p_174630_ ? 1.0F : 0.0F;
            });
            ItemProperties.register(ModItems.LIVING_WOOD_BOW.get(), new ResourceLocation("projectruffle"), (stack, level, living, id) -> {
                if (stack.hasTag() && stack.getTag().contains("UsingProjectruffle")) {
                    return stack.getTag().getBoolean("UsingProjectruffle") ? 1.0F : 0.0F;
                }
                return 0.0F;
            });
            ItemProperties.register(ModItems.DRIED_WIDOWEED.get().asItem(), new ResourceLocation("smoking"), (p_234978_, p_234979_, p_234980_, p_234981_) -> {
                return p_234980_ != null && p_234980_.isUsingItem() && p_234980_.getUseItem() == p_234978_ ? 1.0F : 0.0F;
            });
        });
    }
}