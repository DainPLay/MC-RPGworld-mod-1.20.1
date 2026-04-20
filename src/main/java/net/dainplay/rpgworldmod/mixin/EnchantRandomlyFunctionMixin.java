package net.dainplay.rpgworldmod.mixin;

import com.mojang.logging.LogUtils;
import net.dainplay.rpgworldmod.enchantment.ModEnchantments;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.EnchantRandomlyFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;
import java.util.stream.Collectors;

@Mixin(EnchantRandomlyFunction.class)
public abstract class EnchantRandomlyFunctionMixin extends LootItemConditionalFunction {
	@Shadow
	@Final
	List<Enchantment> enchantments;

	private static final Logger LOGGER = LogUtils.getLogger();

	protected EnchantRandomlyFunctionMixin(LootItemCondition[] conditions) {
		super(conditions);
	}


	@Overwrite
	public ItemStack run(ItemStack pStack, LootContext pContext) {
		RandomSource randomsource = pContext.getRandom();
		Enchantment enchantment;

		if (this.enchantments.isEmpty()) {
			boolean flag = pStack.is(Items.BOOK);
			List<Enchantment> list = BuiltInRegistries.ENCHANTMENT.stream()
					.filter(Enchantment::isDiscoverable)
					.filter(e -> flag || e.canEnchant(pStack))

					.filter(e -> !isForbiddenEnchantment(e))
					.collect(Collectors.toList());

			if (list.isEmpty()) {
				LOGGER.warn("Couldn't find a compatible enchantment for {} (after filtering forbidden mod enchants)", pStack);
				return pStack;
			}

			enchantment = list.get(randomsource.nextInt(list.size()));
		} else {
			List<Enchantment> filtered = this.enchantments.stream()
					.filter(e -> !isForbiddenEnchantment(e))
					.collect(Collectors.toList());

			if (filtered.isEmpty()) {
				LOGGER.warn("All specified enchantments are forbidden for {} (mod enchants filtered)", pStack);
				return pStack;
			}

			enchantment = filtered.get(randomsource.nextInt(filtered.size()));
		}

		return enchantItem(pStack, enchantment, randomsource);
	}


	private static boolean isForbiddenEnchantment(Enchantment e) {
		return e == ModEnchantments.DESTRUCTION.get() ||
				e == ModEnchantments.RESTORATION.get() ||
				e == ModEnchantments.ALTERATION.get() ||
				e == ModEnchantments.ILLUSION.get() ||
				e == ModEnchantments.CONJURATION.get() ||
				e == ModEnchantments.NECROMANCY.get();
	}

	@Shadow
	private static ItemStack enchantItem(ItemStack pStack, Enchantment pEnchantment, RandomSource pRandom) {
		throw new AssertionError();
	}
}