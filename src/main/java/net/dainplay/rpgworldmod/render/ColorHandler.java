package net.dainplay.rpgworldmod.render;

import net.dainplay.rpgworldmod.RPGworldMod;
import net.dainplay.rpgworldmod.block.ModBlocks;
import net.dainplay.rpgworldmod.item.ModItems;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.level.FoliageColor;
import net.minecraft.world.level.GrassColor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = RPGworldMod.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ColorHandler {
	@SubscribeEvent
	public static void registerItemColors(RegisterColorHandlersEvent.Item event) {
		event.getItemColors().register((stack, tintIndex) -> {
					BlockState state = ((BlockItem) stack.getItem()).getBlock().defaultBlockState();
					return event.getBlockColors().getColor(state, null, null, tintIndex);
				},
				ModBlocks.WIDOWEED.get(), ModBlocks.TRIPLOVER.get(), ModBlocks.RIE_LEAVES.get(), ModBlocks.SPIKY_IVY.get());
		event.getItemColors().register((stack, tintIndex) ->
						tintIndex == 1 ? PotionUtils.getColor(stack) : -1,
				ModItems.WOODEN_DAGGER.get(),
				ModItems.STONE_DAGGER.get(),
				ModItems.IRON_DAGGER.get(),
				ModItems.GOLDEN_DAGGER.get(),
				ModItems.DIAMOND_DAGGER.get(),
				ModItems.NETHERITE_DAGGER.get(),
				ModItems.MASKONITE_DAGGER.get(),
				ModItems.FLINT_DAGGER.get());
	}

	@SubscribeEvent
	public static void registerBlockColors(RegisterColorHandlersEvent.Block event) {
		event.getBlockColors().register((state, world, pos, tintIndex) ->
						world != null && pos != null ? BiomeColors.getAverageGrassColor(world, pos) : GrassColor.get(0.5D, 1.0D),
				ModBlocks.WIDOWEED.get(), ModBlocks.TRIPLOVER.get(), ModBlocks.HOLTS_REFLECTION.get(), ModBlocks.SPIKY_IVY.get(), ModBlocks.POTTED_SPIKY_IVY.get(), ModBlocks.POTTED_HOLTS_REFLECTION.get(), ModBlocks.PARALILY.get());


		event.getBlockColors().register((state, world, pos, tintIndex) ->
						world != null && pos != null ? BiomeColors.getAverageFoliageColor(world, pos) : FoliageColor.getDefaultColor(),
				ModBlocks.RIE_LEAVES.get());

		event.getBlockColors().register((state, world, pos, tintIndex) ->
						11633736,
				ModBlocks.ARBOR_FUEL_CAULDRON.get());


		event.getBlockColors().register((p_92596_, p_92597_, p_92598_, p_92599_) -> p_92597_ != null && p_92598_ != null ? 2129968 : 7455580, ModBlocks.PARALILY.get());
	}
}