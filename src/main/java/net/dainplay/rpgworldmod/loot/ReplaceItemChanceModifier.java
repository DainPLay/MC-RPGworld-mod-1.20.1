package net.dainplay.rpgworldmod.loot;

import com.google.common.base.Suppliers;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public class ReplaceItemChanceModifier extends LootModifier {
    public static final Supplier<Codec<ReplaceItemChanceModifier>> CODEC = Suppliers.memoize(() ->
            RecordCodecBuilder.create(inst -> codecStart(inst)
                    .and(ForgeRegistries.ITEMS.getCodec().fieldOf("target").forGetter(m -> m.targetItem))
                    .and(ForgeRegistries.ITEMS.getCodec().fieldOf("replacement").forGetter(m -> m.replacementItem))
                    .and(Codec.FLOAT.fieldOf("chance").forGetter(m -> m.chance))
                    .and(Codec.FLOAT.optionalFieldOf("both_chance", 0.0F).forGetter(m -> m.bothChance))
                    .apply(inst, ReplaceItemChanceModifier::new))
    );

    private final Item targetItem;
    private final Item replacementItem;
    private final float chance;
    private final float bothChance;

    public ReplaceItemChanceModifier(LootItemCondition[] conditionsIn, Item targetItem, Item replacementItem, float chance, float bothChance) {
        super(conditionsIn);
        this.targetItem = targetItem;
        this.replacementItem = replacementItem;
        this.chance = chance;
        this.bothChance = bothChance;
    }

    @Override
    protected @NotNull ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        for (LootItemCondition condition : this.conditions) {
            if (!condition.test(context)) {
                return generatedLoot;
            }
        }

        RandomSource random = context.getRandom();
        for (int i = 0; i < generatedLoot.size(); i++) {
            ItemStack stack = generatedLoot.get(i);
            if (stack.is(this.targetItem)) {
                float r = random.nextFloat();
                if (r < this.chance) {
                    generatedLoot.set(i, new ItemStack(this.replacementItem, stack.getCount()));
                } else if (r < this.chance + this.bothChance && this.bothChance > 0) {
                    generatedLoot.add(new ItemStack(this.replacementItem, stack.getCount()));
                }
            }
        }
        return generatedLoot;
    }

    @Override
    public Codec<? extends IGlobalLootModifier> codec() {
        return CODEC.get();
    }
}