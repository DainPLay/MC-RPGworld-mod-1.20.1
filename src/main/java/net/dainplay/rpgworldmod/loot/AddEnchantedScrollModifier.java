package net.dainplay.rpgworldmod.loot;

import com.google.common.base.Suppliers;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Supplier;

public class AddEnchantedScrollModifier extends LootModifier {
	private final Item item;
	private final int minCount;
	private final int maxCount;
	private final float enchantChance;
	private final List<ResourceLocation> enchantments;

	public static final Supplier<Codec<AddEnchantedScrollModifier>> CODEC = Suppliers.memoize(() ->
			RecordCodecBuilder.create(inst -> codecStart(inst)
					.and(ForgeRegistries.ITEMS.getCodec().fieldOf("item").forGetter(m -> m.item))
					.and(Codec.INT.fieldOf("minCount").forGetter(m -> m.minCount))
					.and(Codec.INT.fieldOf("maxCount").forGetter(m -> m.maxCount))
					.and(Codec.FLOAT.fieldOf("enchantChance").forGetter(m -> m.enchantChance))
					.and(ResourceLocation.CODEC.listOf().fieldOf("enchantments").forGetter(m -> m.enchantments))
					.apply(inst, AddEnchantedScrollModifier::new)
			));

	public AddEnchantedScrollModifier(LootItemCondition[] conditions,
									  Item item,
									  int minCount,
									  int maxCount,
									  float enchantChance,
									  List<ResourceLocation> enchantments) {
		super(conditions);
		this.item = item;
		this.minCount = minCount;
		this.maxCount = maxCount;
		this.enchantChance = enchantChance;
		this.enchantments = enchantments;
	}

	@Override
	protected @NotNull ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
		for (LootItemCondition condition : this.conditions) {
			if (!condition.test(context)) {
				return generatedLoot;
			}
		}


		int count = minCount + context.getRandom().nextInt(maxCount - minCount + 1);

		for (int i = 0; i < count; i++) {
			ItemStack stack = new ItemStack(item);


			if (!enchantments.isEmpty() && context.getRandom().nextFloat() < enchantChance) {
				ResourceLocation enchId = enchantments.get(context.getRandom().nextInt(enchantments.size()));
				Enchantment ench = ForgeRegistries.ENCHANTMENTS.getValue(enchId);
				if (ench != null) {
					stack.enchant(ench, 1);
				}
			}

			generatedLoot.add(stack);
		}

		return generatedLoot;
	}

	@Override
	public Codec<? extends IGlobalLootModifier> codec() {
		return CODEC.get();
	}
}