package net.dainplay.rpgworldmod;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.datafixers.util.Pair;
import net.dainplay.rpgworldmod.block.ModBlocks;
import net.dainplay.rpgworldmod.block.entity.ModBlockEntities;
import net.dainplay.rpgworldmod.block.entity.ModWoodTypes;
import net.dainplay.rpgworldmod.data.tags.ModAdvancements;
import net.dainplay.rpgworldmod.effect.ModEffects;
import net.dainplay.rpgworldmod.entity.client.render.CurioLayers;
import net.dainplay.rpgworldmod.entity.client.render.CurioRenderers;
import net.dainplay.rpgworldmod.fluid.ModFluids;
import net.dainplay.rpgworldmod.gui.OverlayEventHandler;
import net.dainplay.rpgworldmod.item.ModItems;
import net.dainplay.rpgworldmod.item.custom.MintalTriangleItem;
import net.dainplay.rpgworldmod.particle.ModParticles;
import net.dainplay.rpgworldmod.util.EnchantedBlockRenderer;
import net.minecraft.client.model.BoatModel;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.blockentity.HangingSignRenderer;
import net.minecraft.client.renderer.blockentity.SignRenderer;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.client.renderer.item.ItemPropertyFunction;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod.EventBusSubscriber(modid = RPGworldMod.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class RPGworldClient {


    public RPGworldClient() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        modBus.addListener(this::onRegisterLayerDefinitions);
        modBus.addListener(this::guiSetup);
    }

    public void onRegisterLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        CurioLayers.register(event);
    }
    public void guiSetup(final RegisterGuiOverlaysEvent event) {
        //Register Armor Renderer for events
        event.registerAbove(VanillaGuiOverlay.PLAYER_HEALTH.id(),RPGworldMod.MOD_ID, new OverlayEventHandler());
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
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.TYPHON.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.POTTED_MIMOSSA.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.POTTED_TYPHON.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.POTTED_SILICINA.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.POTTED_GLOSSOM.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.POTTED_HOLTS_REFLECTION.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.PARALILY.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.PROJECTRUFFLE.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.SILICINA.get(), RenderType.cutout());
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
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.MOSQUITOS.get(), RenderType.cutout());

        WoodType.register(ModWoodTypes.RIE_WOOD_TYPE);
        Sheets.addWoodType(ModWoodTypes.RIE_WOOD_TYPE);
        BlockEntityRenderers.register(ModBlockEntities.SIGN_BLOCK_ENTITIES.get(), SignRenderer::new);
        BlockEntityRenderers.register(ModBlockEntities.HANGING_SIGN_BLOCK_ENTITIES.get(), HangingSignRenderer::new);
        BlockEntityRenderers.register(ModBlockEntities.FAIRAPIER_WILTED_PLANT_BLOCK_ENTITY.get(), EnchantedBlockRenderer::new);
        CurioRenderers.register();


        event.enqueueWork(() -> {

            ItemProperties.register(ModItems.RPGIROLLE_ITEM.get().asItem(), new ResourceLocation( "token"), (stack, world, entity, seed) -> {
                if (stack.isEmpty()) {
                    return 0.0F;
                }
                CompoundTag tag = stack.getTag();
                if (tag != null && tag.contains("Token", Tag.TAG_INT)) {
                    int tokenValue = tag.getInt("Token");
                    if (tokenValue == 1) {
                        return 1.0F;
                    } else if (tokenValue == 2) {
                        return 2.0F;
                    } else if (tokenValue == 3) {
                        return 3.0F;
                    }
                }
                return 0.0F;
            });
            ItemProperties.register(ModItems.WEALD_BLADE.get().asItem(), new ResourceLocation( "blocking"), (stack, world, entity, seed) -> entity != null && entity.isUsingItem() && entity.getUseItem() == stack ? 1.0F : 0.0F);
            ItemProperties.register(ModItems.DRILL_SPEAR.get().asItem(), new ResourceLocation( "throwing"), (stack, world, entity, seed) -> entity != null && entity.isUsingItem() && entity.getUseItem() == stack ? 1.0F : 0.0F);
            ItemProperties.register(ModItems.MINTAL_TRIANGLE.get(), new ResourceLocation("vibration"), (itemstack, level, livingEntity, p_174608_) -> livingEntity != null && MintalTriangleItem.getVibes(itemstack)>0 ? (23F-(float)MintalTriangleItem.getVibes(itemstack))/100 : 1.0F);
            ItemProperties.register(Items.CROSSBOW, new ResourceLocation("projectruffle"), (p_174605_, p_174606_, p_174607_, p_174608_) -> p_174607_ != null && CrossbowItem.isCharged(p_174605_) && CrossbowItem.containsChargedProjectile(p_174605_, ModItems.PROJECTRUFFLE_ITEM.get()) ? 1.0F : 0.0F);
            ItemProperties.register(Items.BOW, new ResourceLocation("projectruffle"), (stack, level, living, id) -> {
                if (stack.hasTag() && stack.getTag().contains("UsingProjectruffle")) {
                    return stack.getTag().getBoolean("UsingProjectruffle") ? 1.0F : 0.0F;
                }
                return 0.0F;
            });
        });
    }
}