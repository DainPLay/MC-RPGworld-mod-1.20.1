package net.dainplay.rpgworldmod;

import net.dainplay.rpgworldmod.block.ModBlocks;
import net.dainplay.rpgworldmod.block.entity.ModBlockEntities;
import net.dainplay.rpgworldmod.block.entity.ModWoodTypes;
import net.dainplay.rpgworldmod.enchantment.ModEnchantments;
import net.dainplay.rpgworldmod.entity.client.model.SkirtModel;
import net.dainplay.rpgworldmod.entity.client.render.CurioLayers;
import net.dainplay.rpgworldmod.entity.client.render.CurioRenderers;
import net.dainplay.rpgworldmod.entity.client.render.SkirtArmorLayer;
import net.dainplay.rpgworldmod.event.ClientModEvents;
import net.dainplay.rpgworldmod.fluid.ModFluids;
import net.dainplay.rpgworldmod.gui.ChargesOverlayEventHandler;
import net.dainplay.rpgworldmod.gui.HealthOverlayEventHandler;
import net.dainplay.rpgworldmod.gui.ManaOverlayEventHandler;
import net.dainplay.rpgworldmod.item.ModItems;
import net.dainplay.rpgworldmod.item.custom.BlazeStaffItem;
import net.dainplay.rpgworldmod.item.custom.DoubleSidedRecordItem;
import net.dainplay.rpgworldmod.item.custom.FireproofSkirtItem;
import net.dainplay.rpgworldmod.item.custom.MintalTriangleItem;
import net.dainplay.rpgworldmod.item.custom.SculkStaffItem;
import net.dainplay.rpgworldmod.item.custom.StaffItem;
import net.dainplay.rpgworldmod.render.BakedModelShadeLayerFullbright;
import net.dainplay.rpgworldmod.render.BoundCampfireBlockRenderer;
import net.dainplay.rpgworldmod.render.BreakingEntFaceRenderer;
import net.dainplay.rpgworldmod.render.EnchantedBlockRenderer;
import net.dainplay.rpgworldmod.render.PottedStareblossomBlockEntityRenderer;
import net.dainplay.rpgworldmod.render.ScrollGlintItemModel;
import net.dainplay.rpgworldmod.render.ScrollGlintItemModelSupport;
import net.dainplay.rpgworldmod.render.StareblossomBlockEntityRenderer;
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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

import java.util.Map;

@Mod.EventBusSubscriber(modid = RPGworldMod.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class RPGworldClient {
	@SubscribeEvent
	public static void onRegisterLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
		CurioLayers.register(event);
	}

	@SubscribeEvent
	public static void guiSetup(final RegisterGuiOverlaysEvent event) {
		event.registerAbove(VanillaGuiOverlay.PLAYER_HEALTH.id(), RPGworldMod.MOD_ID + "_hearts_overlay", new HealthOverlayEventHandler());
		event.registerAbove(VanillaGuiOverlay.FOOD_LEVEL.id(), RPGworldMod.MOD_ID + "_mana_overlay", new ManaOverlayEventHandler());
		event.registerAbove(VanillaGuiOverlay.CROSSHAIR.id(), RPGworldMod.MOD_ID + "_crosshair_overlay", new ChargesOverlayEventHandler());
	}

	@SubscribeEvent
	public static void bakeModels(final ModelEvent.ModifyBakingResult e) {
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

		BakedModel alterationHeartOfTheSeaScrollModel = null;
		BakedModel destructionHeartOfTheSeaScrollModel = null;
		BakedModel restorationHeartOfTheSeaScrollModel = null;
		BakedModel illusionHeartOfTheSeaScrollModel = null;
		BakedModel conjurationHeartOfTheSeaScrollModel = null;
		BakedModel necromancyHeartOfTheSeaScrollModel = null;

		BakedModel alterationEnderEyeScrollModel = null;
		BakedModel destructionEnderEyeScrollModel = null;
		BakedModel restorationEnderEyeScrollModel = null;
		BakedModel illusionEnderEyeScrollModel = null;
		BakedModel conjurationEnderEyeScrollModel = null;
		BakedModel necromancyEnderEyeScrollModel = null;

		BakedModel alterationNetherStarScrollModel = null;
		BakedModel destructionNetherStarScrollModel = null;
		BakedModel restorationNetherStarScrollModel = null;
		BakedModel illusionNetherStarScrollModel = null;
		BakedModel conjurationNetherStarScrollModel = null;
		BakedModel necromancyNetherStarScrollModel = null;

		BakedModel alterationPillagerScrollModel = null;
		BakedModel destructionPillagerScrollModel = null;
		BakedModel restorationPillagerScrollModel = null;
		BakedModel illusionPillagerScrollModel = null;
		BakedModel conjurationPillagerScrollModel = null;
		BakedModel necromancyPillagerScrollModel = null;

		BakedModel boundCampfireModel = null;
		BakedModel conjuredPickaxeModel = null;

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

			if (idString.contains("rpgworldmod:item/heart_of_the_sea_scroll_alteration")) {
				alterationHeartOfTheSeaScrollModel = new ScrollGlintItemModelSupport(originalModel);
			}

			if (idString.contains("rpgworldmod:item/heart_of_the_sea_scroll_destruction")) {
				destructionHeartOfTheSeaScrollModel = new ScrollGlintItemModelSupport(originalModel);
			}

			if (idString.contains("rpgworldmod:item/heart_of_the_sea_scroll_restoration")) {
				restorationHeartOfTheSeaScrollModel = new ScrollGlintItemModelSupport(originalModel);
			}

			if (idString.contains("rpgworldmod:item/heart_of_the_sea_scroll_illusion")) {
				illusionHeartOfTheSeaScrollModel = new ScrollGlintItemModelSupport(originalModel);
			}

			if (idString.contains("rpgworldmod:item/heart_of_the_sea_scroll_conjuration")) {
				conjurationHeartOfTheSeaScrollModel = new ScrollGlintItemModelSupport(originalModel);
			}

			if (idString.contains("rpgworldmod:item/heart_of_the_sea_scroll_necromancy")) {
				necromancyHeartOfTheSeaScrollModel = new ScrollGlintItemModelSupport(originalModel);
			}

			if (idString.contains("rpgworldmod:item/ender_eye_scroll_alteration")) {
				alterationEnderEyeScrollModel = new ScrollGlintItemModelSupport(originalModel);
			}

			if (idString.contains("rpgworldmod:item/ender_eye_scroll_destruction")) {
				destructionEnderEyeScrollModel = new ScrollGlintItemModelSupport(originalModel);
			}

			if (idString.contains("rpgworldmod:item/ender_eye_scroll_restoration")) {
				restorationEnderEyeScrollModel = new ScrollGlintItemModelSupport(originalModel);
			}

			if (idString.contains("rpgworldmod:item/ender_eye_scroll_illusion")) {
				illusionEnderEyeScrollModel = new ScrollGlintItemModelSupport(originalModel);
			}

			if (idString.contains("rpgworldmod:item/ender_eye_scroll_conjuration")) {
				conjurationEnderEyeScrollModel = new ScrollGlintItemModelSupport(originalModel);
			}

			if (idString.contains("rpgworldmod:item/ender_eye_scroll_necromancy")) {
				necromancyEnderEyeScrollModel = new ScrollGlintItemModelSupport(originalModel);
			}

			if (idString.contains("rpgworldmod:item/nether_star_scroll_alteration")) {
				alterationNetherStarScrollModel = new ScrollGlintItemModelSupport(originalModel);
			}

			if (idString.contains("rpgworldmod:item/nether_star_scroll_destruction")) {
				destructionNetherStarScrollModel = new ScrollGlintItemModelSupport(originalModel);
			}

			if (idString.contains("rpgworldmod:item/nether_star_scroll_restoration")) {
				restorationNetherStarScrollModel = new ScrollGlintItemModelSupport(originalModel);
			}

			if (idString.contains("rpgworldmod:item/nether_star_scroll_illusion")) {
				illusionNetherStarScrollModel = new ScrollGlintItemModelSupport(originalModel);
			}

			if (idString.contains("rpgworldmod:item/nether_star_scroll_conjuration")) {
				conjurationNetherStarScrollModel = new ScrollGlintItemModelSupport(originalModel);
			}

			if (idString.contains("rpgworldmod:item/nether_star_scroll_necromancy")) {
				necromancyNetherStarScrollModel = new ScrollGlintItemModelSupport(originalModel);
			}

			if (idString.contains("rpgworldmod:item/pillager_scroll_alteration")) {
				alterationPillagerScrollModel = new ScrollGlintItemModelSupport(originalModel);
			}

			if (idString.contains("rpgworldmod:item/pillager_scroll_destruction")) {
				destructionPillagerScrollModel = new ScrollGlintItemModelSupport(originalModel);
			}

			if (idString.contains("rpgworldmod:item/pillager_scroll_restoration")) {
				restorationPillagerScrollModel = new ScrollGlintItemModelSupport(originalModel);
			}

			if (idString.contains("rpgworldmod:item/pillager_scroll_illusion")) {
				illusionPillagerScrollModel = new ScrollGlintItemModelSupport(originalModel);
			}

			if (idString.contains("rpgworldmod:item/pillager_scroll_conjuration")) {
				conjurationPillagerScrollModel = new ScrollGlintItemModelSupport(originalModel);
			}

			if (idString.contains("rpgworldmod:item/pillager_scroll_necromancy")) {
				necromancyPillagerScrollModel = new ScrollGlintItemModelSupport(originalModel);
			}

			if (idString.contains("rpgworldmod:item/bound_campfire")) {
				boundCampfireModel = new ScrollGlintItemModelSupport(originalModel);
			}

			if (idString.contains("rpgworldmod:item/conjured_pickaxe")) {
				conjuredPickaxeModel = new ScrollGlintItemModelSupport(originalModel);
			}
		}

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
			if (idString.contains("rpgworldmod:heart_of_the_sea_scroll")) {
				map.put(id, new ScrollGlintItemModel(
						originalModel,
						alterationHeartOfTheSeaScrollModel == null ? originalModel : alterationHeartOfTheSeaScrollModel,
						restorationHeartOfTheSeaScrollModel == null ? originalModel : restorationHeartOfTheSeaScrollModel,
						destructionHeartOfTheSeaScrollModel == null ? originalModel : destructionHeartOfTheSeaScrollModel,
						illusionHeartOfTheSeaScrollModel == null ? originalModel : illusionHeartOfTheSeaScrollModel,
						conjurationHeartOfTheSeaScrollModel == null ? originalModel : conjurationHeartOfTheSeaScrollModel,
						necromancyHeartOfTheSeaScrollModel == null ? originalModel : necromancyHeartOfTheSeaScrollModel,
						originalModel
				));
			}
			if (idString.contains("rpgworldmod:ender_eye_scroll")) {
				map.put(id, new ScrollGlintItemModel(
						originalModel,
						alterationEnderEyeScrollModel == null ? originalModel : alterationEnderEyeScrollModel,
						restorationEnderEyeScrollModel == null ? originalModel : restorationEnderEyeScrollModel,
						destructionEnderEyeScrollModel == null ? originalModel : destructionEnderEyeScrollModel,
						illusionEnderEyeScrollModel == null ? originalModel : illusionEnderEyeScrollModel,
						conjurationEnderEyeScrollModel == null ? originalModel : conjurationEnderEyeScrollModel,
						necromancyEnderEyeScrollModel == null ? originalModel : necromancyEnderEyeScrollModel,
						originalModel
				));
			}
			if (idString.contains("rpgworldmod:nether_star_scroll")) {
				map.put(id, new ScrollGlintItemModel(
						originalModel,
						alterationNetherStarScrollModel == null ? originalModel : alterationNetherStarScrollModel,
						restorationNetherStarScrollModel == null ? originalModel : restorationNetherStarScrollModel,
						destructionNetherStarScrollModel == null ? originalModel : destructionNetherStarScrollModel,
						illusionNetherStarScrollModel == null ? originalModel : illusionNetherStarScrollModel,
						conjurationNetherStarScrollModel == null ? originalModel : conjurationNetherStarScrollModel,
						necromancyNetherStarScrollModel == null ? originalModel : necromancyNetherStarScrollModel,
						conjuredPickaxeModel == null ? originalModel : conjuredPickaxeModel
				));
			}
			if (idString.contains("rpgworldmod:pillager_scroll")) {
				map.put(id, new ScrollGlintItemModel(
						originalModel,
						alterationPillagerScrollModel == null ? originalModel : alterationPillagerScrollModel,
						restorationPillagerScrollModel == null ? originalModel : restorationPillagerScrollModel,
						destructionPillagerScrollModel == null ? originalModel : destructionPillagerScrollModel,
						illusionPillagerScrollModel == null ? originalModel : illusionPillagerScrollModel,
						conjurationPillagerScrollModel == null ? originalModel : conjurationPillagerScrollModel,
						necromancyPillagerScrollModel == null ? originalModel : necromancyPillagerScrollModel,
						originalModel
				));
			}
		}
	}

	@SubscribeEvent
	public static void registerAdditionalModels(ModelEvent.RegisterAdditional event) {
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
		event.register(new ResourceLocation("rpgworldmod:item/heart_of_the_sea_scroll_restoration"));
		event.register(new ResourceLocation("rpgworldmod:item/heart_of_the_sea_scroll_destruction"));
		event.register(new ResourceLocation("rpgworldmod:item/heart_of_the_sea_scroll_illusion"));
		event.register(new ResourceLocation("rpgworldmod:item/heart_of_the_sea_scroll_alteration"));
		event.register(new ResourceLocation("rpgworldmod:item/heart_of_the_sea_scroll_conjuration"));
		event.register(new ResourceLocation("rpgworldmod:item/heart_of_the_sea_scroll_necromancy"));
		event.register(new ResourceLocation("rpgworldmod:item/ender_eye_scroll_restoration"));
		event.register(new ResourceLocation("rpgworldmod:item/ender_eye_scroll_destruction"));
		event.register(new ResourceLocation("rpgworldmod:item/ender_eye_scroll_illusion"));
		event.register(new ResourceLocation("rpgworldmod:item/ender_eye_scroll_alteration"));
		event.register(new ResourceLocation("rpgworldmod:item/ender_eye_scroll_conjuration"));
		event.register(new ResourceLocation("rpgworldmod:item/ender_eye_scroll_necromancy"));
		event.register(new ResourceLocation("rpgworldmod:item/nether_star_scroll_restoration"));
		event.register(new ResourceLocation("rpgworldmod:item/nether_star_scroll_destruction"));
		event.register(new ResourceLocation("rpgworldmod:item/nether_star_scroll_illusion"));
		event.register(new ResourceLocation("rpgworldmod:item/nether_star_scroll_alteration"));
		event.register(new ResourceLocation("rpgworldmod:item/nether_star_scroll_conjuration"));
		event.register(new ResourceLocation("rpgworldmod:item/nether_star_scroll_necromancy"));
		event.register(new ResourceLocation("rpgworldmod:item/pillager_scroll_restoration"));
		event.register(new ResourceLocation("rpgworldmod:item/pillager_scroll_destruction"));
		event.register(new ResourceLocation("rpgworldmod:item/pillager_scroll_illusion"));
		event.register(new ResourceLocation("rpgworldmod:item/pillager_scroll_alteration"));
		event.register(new ResourceLocation("rpgworldmod:item/pillager_scroll_conjuration"));
		event.register(new ResourceLocation("rpgworldmod:item/pillager_scroll_necromancy"));
		event.register(new ResourceLocation("rpgworldmod:item/bound_campfire"));
		event.register(new ResourceLocation("rpgworldmod:item/conjured_pickaxe"));
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
			ItemProperties.register(ModItems.EMBER_SCROLL.get().asItem(), new ResourceLocation("summoned_object"), (stack, world, entity, seed) -> {
				if (stack.isEmpty()) {
					return 0.0F;
				}
				CompoundTag tag = stack.getTag();
				if (tag != null && tag.contains("SummonedObject", Tag.TAG_INT)) {
					return 1.0F;
				}
				return 0.0F;
			});

			ItemProperties.register(ModItems.NETHER_STAR_SCROLL.get().asItem(), new ResourceLocation("summoned_object"), (stack, world, entity, seed) -> {
				if (stack.isEmpty()) {
					return 0.0F;
				}
				CompoundTag tag = stack.getTag();
				if (tag != null && tag.contains("SummonedObject", Tag.TAG_INT)) {
					return 1.0F;
				}
				return 0.0F;
			});

			ItemProperties.register(ModItems.FAIRAPIER_SWORD.get().asItem(), new ResourceLocation("growing"), (stack, world, entity, seed) -> {
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

			ItemProperties.register(ModItems.RPGIROLLE_ITEM.get().asItem(), new ResourceLocation("token"), (stack, world, entity, seed) -> {
				if (stack.isEmpty()) {
					return 0.0F;
				}
				CompoundTag tag = stack.getTag();
				if (tag != null && tag.contains("Token", Tag.TAG_INT)) {
					return (float) tag.getInt("Token");
				}
				return 0.0F;
			});
			ItemProperties.register(ModItems.GUITAR_AX.get().asItem(), new ResourceLocation("playing"), (stack, world, entity, seed) -> entity != null && entity.getOffhandItem() == stack ? 1.0F : 0.0F);
			ItemProperties.register(ModItems.WEALD_BLADE.get().asItem(), new ResourceLocation("blocking"), (stack, world, entity, seed) -> entity != null && entity.isUsingItem() && entity.getUseItem() == stack ? 1.0F : 0.0F);
			ItemProperties.register(ModItems.DRILL_SPEAR.get().asItem(), new ResourceLocation("throwing"), (stack, world, entity, seed) -> entity != null && entity.isUsingItem() && entity.getUseItem() == stack ? 1.0F : 0.0F);
			ItemProperties.register(ModItems.WOODEN_DAGGER.get().asItem(), new ResourceLocation("stabbing"), (stack, world, entity, seed) -> {
				if (entity != null && entity.isUsingItem() && entity.getUseItem() == stack)
					return stack.getEnchantmentLevel(ModEnchantments.IMMOLATION.get()) > 0 ? 2.0F : 1.0F;
				if (entity instanceof Player player && player.getCooldowns().isOnCooldown(stack.getItem()) && player.swinging && player.getItemInHand(player.swingingArm) == stack)
					return stack.getEnchantmentLevel(ModEnchantments.IMMOLATION.get()) > 0 ? 2.0F : 1.0F;
				;
				return 0.0F;
			});
			ItemProperties.register(ModItems.STONE_DAGGER.get().asItem(), new ResourceLocation("stabbing"), (stack, world, entity, seed) -> {
				if (entity != null && entity.isUsingItem() && entity.getUseItem() == stack)
					return stack.getEnchantmentLevel(ModEnchantments.IMMOLATION.get()) > 0 ? 2.0F : 1.0F;
				if (entity instanceof Player player && player.getCooldowns().isOnCooldown(stack.getItem()) && player.swinging && player.getItemInHand(player.swingingArm) == stack)
					return stack.getEnchantmentLevel(ModEnchantments.IMMOLATION.get()) > 0 ? 2.0F : 1.0F;
				;
				return 0.0F;
			});
			ItemProperties.register(ModItems.IRON_DAGGER.get().asItem(), new ResourceLocation("stabbing"), (stack, world, entity, seed) -> {
				if (entity != null && entity.isUsingItem() && entity.getUseItem() == stack)
					return stack.getEnchantmentLevel(ModEnchantments.IMMOLATION.get()) > 0 ? 2.0F : 1.0F;
				if (entity instanceof Player player && player.getCooldowns().isOnCooldown(stack.getItem()) && player.swinging && player.getItemInHand(player.swingingArm) == stack)
					return stack.getEnchantmentLevel(ModEnchantments.IMMOLATION.get()) > 0 ? 2.0F : 1.0F;
				;
				return 0.0F;
			});
			ItemProperties.register(ModItems.GOLDEN_DAGGER.get().asItem(), new ResourceLocation("stabbing"), (stack, world, entity, seed) -> {
				if (entity != null && entity.isUsingItem() && entity.getUseItem() == stack)
					return stack.getEnchantmentLevel(ModEnchantments.IMMOLATION.get()) > 0 ? 2.0F : 1.0F;
				if (entity instanceof Player player && player.getCooldowns().isOnCooldown(stack.getItem()) && player.swinging && player.getItemInHand(player.swingingArm) == stack)
					return stack.getEnchantmentLevel(ModEnchantments.IMMOLATION.get()) > 0 ? 2.0F : 1.0F;
				;
				return 0.0F;
			});
			ItemProperties.register(ModItems.DIAMOND_DAGGER.get().asItem(), new ResourceLocation("stabbing"), (stack, world, entity, seed) -> {
				if (entity != null && entity.isUsingItem() && entity.getUseItem() == stack)
					return stack.getEnchantmentLevel(ModEnchantments.IMMOLATION.get()) > 0 ? 2.0F : 1.0F;
				if (entity instanceof Player player && player.getCooldowns().isOnCooldown(stack.getItem()) && player.swinging && player.getItemInHand(player.swingingArm) == stack)
					return stack.getEnchantmentLevel(ModEnchantments.IMMOLATION.get()) > 0 ? 2.0F : 1.0F;
				;
				return 0.0F;
			});
			ItemProperties.register(ModItems.NETHERITE_DAGGER.get().asItem(), new ResourceLocation("stabbing"), (stack, world, entity, seed) -> {
				if (entity != null && entity.isUsingItem() && entity.getUseItem() == stack)
					return stack.getEnchantmentLevel(ModEnchantments.IMMOLATION.get()) > 0 ? 2.0F : 1.0F;
				if (entity instanceof Player player && player.getCooldowns().isOnCooldown(stack.getItem()) && player.swinging && player.getItemInHand(player.swingingArm) == stack)
					return stack.getEnchantmentLevel(ModEnchantments.IMMOLATION.get()) > 0 ? 2.0F : 1.0F;
				;
				return 0.0F;
			});
			ItemProperties.register(ModItems.MASKONITE_DAGGER.get().asItem(), new ResourceLocation("stabbing"), (stack, world, entity, seed) -> {
				if (entity != null && entity.isUsingItem() && entity.getUseItem() == stack)
					return stack.getEnchantmentLevel(ModEnchantments.IMMOLATION.get()) > 0 ? 2.0F : 1.0F;
				if (entity instanceof Player player && player.getCooldowns().isOnCooldown(stack.getItem()) && player.swinging && player.getItemInHand(player.swingingArm) == stack)
					return stack.getEnchantmentLevel(ModEnchantments.IMMOLATION.get()) > 0 ? 2.0F : 1.0F;
				;
				return 0.0F;
			});
			ItemProperties.register(ModItems.FLINT_DAGGER.get().asItem(), new ResourceLocation("stabbing"), (stack, world, entity, seed) -> {
				if (entity != null && entity.isUsingItem() && entity.getUseItem() == stack)
					return stack.getEnchantmentLevel(ModEnchantments.IMMOLATION.get()) > 0 ? 2.0F : 1.0F;
				if (entity instanceof Player player && player.getCooldowns().isOnCooldown(stack.getItem()) && player.swinging && player.getItemInHand(player.swingingArm) == stack)
					return stack.getEnchantmentLevel(ModEnchantments.IMMOLATION.get()) > 0 ? 2.0F : 1.0F;
				;
				return 0.0F;
			});
			ItemProperties.register(ModItems.WOODEN_DAGGER.get().asItem(), new ResourceLocation("effect"), (stack, world, entity, seed) -> {
				Potion potionType = PotionUtils.getPotion(stack);
				if (potionType != Potions.EMPTY)
					return 1.0F;
				return 0.0F;
			});
			ItemProperties.register(ModItems.STONE_DAGGER.get().asItem(), new ResourceLocation("effect"), (stack, world, entity, seed) -> {
				Potion potionType = PotionUtils.getPotion(stack);
				if (potionType != Potions.EMPTY)
					return 1.0F;
				return 0.0F;
			});
			ItemProperties.register(ModItems.IRON_DAGGER.get().asItem(), new ResourceLocation("effect"), (stack, world, entity, seed) -> {
				Potion potionType = PotionUtils.getPotion(stack);
				if (potionType != Potions.EMPTY)
					return 1.0F;
				return 0.0F;
			});
			ItemProperties.register(ModItems.GOLDEN_DAGGER.get().asItem(), new ResourceLocation("effect"), (stack, world, entity, seed) -> {
				Potion potionType = PotionUtils.getPotion(stack);
				if (potionType != Potions.EMPTY)
					return 1.0F;
				return 0.0F;
			});
			ItemProperties.register(ModItems.DIAMOND_DAGGER.get().asItem(), new ResourceLocation("effect"), (stack, world, entity, seed) -> {
				Potion potionType = PotionUtils.getPotion(stack);
				if (potionType != Potions.EMPTY)
					return 1.0F;
				return 0.0F;
			});
			ItemProperties.register(ModItems.NETHERITE_DAGGER.get().asItem(), new ResourceLocation("effect"), (stack, world, entity, seed) -> {
				Potion potionType = PotionUtils.getPotion(stack);
				if (potionType != Potions.EMPTY)
					return 1.0F;
				return 0.0F;
			});
			ItemProperties.register(ModItems.MASKONITE_DAGGER.get().asItem(), new ResourceLocation("effect"), (stack, world, entity, seed) -> {
				Potion potionType = PotionUtils.getPotion(stack);
				if (potionType != Potions.EMPTY)
					return 1.0F;
				return 0.0F;
			});
			ItemProperties.register(ModItems.FLINT_DAGGER.get().asItem(), new ResourceLocation("effect"), (stack, world, entity, seed) -> {
				Potion potionType = PotionUtils.getPotion(stack);
				if (potionType != Potions.EMPTY)
					return 1.0F;
				return 0.0F;
			});
			ItemProperties.register(ModItems.FIREPROOF_SKIRT.get().asItem(), new ResourceLocation("broken"), (stack, world, entity, seed) -> FireproofSkirtItem.isFireproof(stack) ? 0.0F : 1.0F);
			ItemProperties.register(ModItems.MINTAL_TRIANGLE.get(), new ResourceLocation("vibration"), (itemstack, level, livingEntity, p_174608_) -> livingEntity != null && MintalTriangleItem.getVibes(itemstack) > 0 ? (23F - (float) MintalTriangleItem.getVibes(itemstack)) / 100 : 1.0F);
			ItemProperties.register(ModItems.MUSIC_DISC_RAIN_A_SIDE.get(), new ResourceLocation("flip"), (itemstack, level, livingEntity, p_174608_) -> DoubleSidedRecordItem.getFlipStage(itemstack));
			ItemProperties.register(ModItems.MUSIC_DISC_RAIN_B_SIDE.get(), new ResourceLocation("flip"), (itemstack, level, livingEntity, p_174608_) -> DoubleSidedRecordItem.getFlipStage(itemstack));
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
					return p_174637_.getUseItem() != p_174635_ ? 0.0F : (float) (p_174635_.getUseDuration() - p_174637_.getUseItemRemainingTicks()) / 20.0F;
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
			ItemProperties.register(ModItems.LIVING_WOOD_STAFF.get(), new ResourceLocation("gem_type"),
					(stack, level, entity, seed) -> {
						StaffItem.GemType gemType = StaffItem.getGemType(stack);
						return switch (gemType) {
							case EMBER_GEM -> 1.0F;
							case HEART_OF_THE_SEA -> 2.0F;
							case ENDER_EYE -> 3.0F;
							case NETHER_STAR -> 4.0F;
							default -> 0.0F;
						};
					});
			ItemProperties.register(ModItems.BLAZE_STAFF.get(), new ResourceLocation("gem_type"),
					(stack, level, entity, seed) -> {
						StaffItem.GemType gemType = StaffItem.getGemType(stack);
						return switch (gemType) {
							case EMBER_GEM -> 1.0F;
							case HEART_OF_THE_SEA -> 2.0F;
							case ENDER_EYE -> 3.0F;
							case NETHER_STAR -> 4.0F;
							default -> 0.0F;
						};
					});
			ItemProperties.register(ModItems.SCULK_STAFF.get(), new ResourceLocation("gem_type"),
					(stack, level, entity, seed) -> {
						StaffItem.GemType gemType = StaffItem.getGemType(stack);
						return switch (gemType) {
							case EMBER_GEM -> 1.0F;
							case HEART_OF_THE_SEA -> 2.0F;
							case ENDER_EYE -> 3.0F;
							case NETHER_STAR -> 4.0F;
							default -> 0.0F;
						};
					});
			ItemProperties.register(ModItems.FIRE_CORAL_STAFF.get(), new ResourceLocation("gem_type"),
					(stack, level, entity, seed) -> {
						StaffItem.GemType gemType = StaffItem.getGemType(stack);
						return switch (gemType) {
							case EMBER_GEM -> 1.0F;
							case HEART_OF_THE_SEA -> 2.0F;
							case ENDER_EYE -> 3.0F;
							case NETHER_STAR -> 4.0F;
							default -> 0.0F;
						};
					});
			ItemProperties.register(ModItems.BRAIN_CORAL_STAFF.get(), new ResourceLocation("gem_type"),
					(stack, level, entity, seed) -> {
						StaffItem.GemType gemType = StaffItem.getGemType(stack);
						return switch (gemType) {
							case EMBER_GEM -> 1.0F;
							case HEART_OF_THE_SEA -> 2.0F;
							case ENDER_EYE -> 3.0F;
							case NETHER_STAR -> 4.0F;
							default -> 0.0F;
						};
					});
			ItemProperties.register(ModItems.HORN_CORAL_STAFF.get(), new ResourceLocation("gem_type"),
					(stack, level, entity, seed) -> {
						StaffItem.GemType gemType = StaffItem.getGemType(stack);
						return switch (gemType) {
							case EMBER_GEM -> 1.0F;
							case HEART_OF_THE_SEA -> 2.0F;
							case ENDER_EYE -> 3.0F;
							case NETHER_STAR -> 4.0F;
							default -> 0.0F;
						};
					});
			ItemProperties.register(ModItems.TUBE_CORAL_STAFF.get(), new ResourceLocation("gem_type"),
					(stack, level, entity, seed) -> {
						StaffItem.GemType gemType = StaffItem.getGemType(stack);
						return switch (gemType) {
							case EMBER_GEM -> 1.0F;
							case HEART_OF_THE_SEA -> 2.0F;
							case ENDER_EYE -> 3.0F;
							case NETHER_STAR -> 4.0F;
							default -> 0.0F;
						};
					});
			ItemProperties.register(ModItems.BUBBLE_CORAL_STAFF.get(), new ResourceLocation("gem_type"),
					(stack, level, entity, seed) -> {
						StaffItem.GemType gemType = StaffItem.getGemType(stack);
						return switch (gemType) {
							case EMBER_GEM -> 1.0F;
							case HEART_OF_THE_SEA -> 2.0F;
							case ENDER_EYE -> 3.0F;
							case NETHER_STAR -> 4.0F;
							default -> 0.0F;
						};
					});
			ItemProperties.register(ModItems.BLAZE_STAFF.get().asItem(), new ResourceLocation("using"), (stack, world, entity, seed) -> entity != null && entity.isUsingItem() && entity.getUseItem() == stack && stack.getItem() instanceof BlazeStaffItem staff && entity instanceof Player player && staff.isOffCooldown(stack, player) ? 1.0F : 0.0F);
			ItemProperties.register(ModItems.SCULK_STAFF.get().asItem(), new ResourceLocation("using"),
					(stack, world, entity, seed) -> {
						if (entity instanceof Player player && entity.isUsingItem() && stack.getItem() instanceof SculkStaffItem staff && staff.isOffCooldown(stack, player)) {
							boolean hasVibration = false;
							ItemStack temp1 = player.getUseItem().copy();
							ItemStack temp2 = stack.copy();
							if (temp1.hasTag() && temp1.getTag().contains("caughtVibration") && temp1.getTag().getInt("caughtVibration") > 0) {
								hasVibration = true;
								temp1.getTag().remove("caughtVibration");
							}
							if (temp2.hasTag() && temp2.getTag().contains("caughtVibration") && temp2.getTag().getInt("caughtVibration") > 0) {
								hasVibration = true;
								temp2.getTag().remove("caughtVibration");
							}
							if (ItemStack.isSameItemSameTags(temp1, temp2)) {
								if (hasVibration) {
									return 2.0F;
								}
								return 1.0F;
							}
						}
						return 0.0F;
					});

		});
		ClientModEvents.initSelector();
	}
}