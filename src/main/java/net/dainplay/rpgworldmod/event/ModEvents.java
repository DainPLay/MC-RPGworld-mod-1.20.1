package net.dainplay.rpgworldmod.event;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.dainplay.rpgworldmod.RPGworldMod;
import net.dainplay.rpgworldmod.item.ModItems;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraftforge.event.village.VillagerTradesEvent;
import net.minecraftforge.event.village.WandererTradesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

@Mod.EventBusSubscriber(modid= RPGworldMod.MOD_ID)
public class ModEvents {

	@SubscribeEvent
	public static void addCustomTrades(VillagerTradesEvent event) {
		if(event.getType() == VillagerProfession.FISHERMAN)
		{
			Int2ObjectMap<List<VillagerTrades.ItemListing>> trades = event.getTrades();

			trades.get(5).add((pTrader, pRandom) -> new MerchantOffer(
					new ItemStack(Items.EMERALD, 21),
					new ItemStack(ModItems.GASBASS.get(), 1),
					1, 50, 0.5f
			));
		}
	}

	@SubscribeEvent
	public static void addCustomWanderingTrades(WandererTradesEvent event) {

		List<VillagerTrades.ItemListing> genericTrades = event.getGenericTrades();
		List<VillagerTrades.ItemListing> rareTrades = event.getRareTrades();

		rareTrades.add((pTrader, pRandom) -> new MerchantOffer(
				new ItemStack(Items.EMERALD, 21),
				new ItemStack(ModItems.GASBASS.get(), 1),
				1, 50, 0.5f
		));
	}

}
