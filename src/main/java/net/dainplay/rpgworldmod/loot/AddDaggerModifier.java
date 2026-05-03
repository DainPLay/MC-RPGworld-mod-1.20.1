package net.dainplay.rpgworldmod.loot;

import com.google.common.base.Suppliers;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class AddDaggerModifier extends LootModifier {
	private final Item item;
	private final float chance;
	private final float potionEffectChance;
	private final int enchantLevel;
	private final float enchantChance;
	private final boolean damaged;

	public static final Supplier<Codec<AddDaggerModifier>> CODEC = Suppliers.memoize(() ->
			RecordCodecBuilder.create(inst -> codecStart(inst)
					.and(ForgeRegistries.ITEMS.getCodec().fieldOf("item").forGetter(m -> m.item))
					.and(Codec.FLOAT.fieldOf("chance").forGetter(m -> m.chance))
					.and(Codec.FLOAT.fieldOf("potionEffectChance").forGetter(m -> m.potionEffectChance))
					.and(Codec.INT.fieldOf("enchantLevel").forGetter(m -> m.enchantLevel))
					.and(Codec.FLOAT.fieldOf("enchantChance").forGetter(m -> m.enchantChance))
					.and(Codec.BOOL.optionalFieldOf("damaged", false).forGetter(m -> m.damaged))
					.apply(inst, AddDaggerModifier::new)
			));

	public AddDaggerModifier(LootItemCondition[] conditions,
							 Item item,
							 float chance,
							 float potionEffectChance,
							 int enchantLevel,
							 float enchantChance,
							 boolean damaged) {
		super(conditions);
		this.item = item;
		this.chance = chance;
		this.potionEffectChance = potionEffectChance;
		this.enchantLevel = enchantLevel;
		this.enchantChance = enchantChance;
		this.damaged = damaged;
	}

	@Override
	protected @NotNull ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
		for (LootItemCondition condition : this.conditions) {
			if (!condition.test(context)) {
				return generatedLoot;
			}
		}

		if (context.getRandom().nextFloat() < this.chance) {
			ItemStack daggerStack = new ItemStack(this.item);


			if (this.damaged && daggerStack.getMaxDamage() > 0) {
				int maxDamage = daggerStack.getMaxDamage();
				int maxDamageToApply = (int) (maxDamage * 0.5f);
				int damage = context.getRandom().nextInt(maxDamageToApply + 1);
				daggerStack.setDamageValue(damage);
			}


			if (context.getRandom().nextFloat() < this.potionEffectChance) {
				List<Potion> potions = ForgeRegistries.POTIONS.getValues()
						.stream()
						.filter(p -> p != Potions.EMPTY)
						.collect(Collectors.toList());

				if (!potions.isEmpty()) {
					Potion randomPotion = potions.get(context.getRandom().nextInt(potions.size()));
					PotionUtils.setPotion(daggerStack, randomPotion);
				}
			}


			if (context.getRandom().nextFloat() < this.enchantChance) {
				daggerStack = EnchantmentHelper.enchantItem(
						context.getRandom(),
						daggerStack,
						this.enchantLevel,
						false
				);
			}

			generatedLoot.add(daggerStack);
		}

		return generatedLoot;
	}

	@Override
	public Codec<? extends IGlobalLootModifier> codec() {
		return CODEC.get();
	}
}