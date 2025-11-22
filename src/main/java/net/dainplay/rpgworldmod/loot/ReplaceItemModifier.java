package net.dainplay.rpgworldmod.loot;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.dainplay.rpgworldmod.entity.custom.Gasbass;
import net.dainplay.rpgworldmod.item.custom.GasbassItem;
import net.dainplay.rpgworldmod.world.RPGLootTables;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Supplier;
import com.google.common.base.Suppliers;

public class ReplaceItemModifier extends LootModifier {
    public static final Supplier<Codec<ReplaceItemModifier>> CODEC = Suppliers.memoize(() -> RecordCodecBuilder.create(inst ->
            codecStart(inst)
                    .and(ResourceLocation.CODEC.fieldOf("lootTable").forGetter(m -> m.lootTable))
                    .apply(inst, ReplaceItemModifier::new)));

    private final ResourceLocation lootTable;

    public ReplaceItemModifier(LootItemCondition[] conditionsIn, ResourceLocation lootTable) {
        super(conditionsIn);
        this.lootTable = lootTable;
    }

    @Override
    protected @NotNull ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        for(LootItemCondition condition : this.conditions) {
            if(!condition.test(context)) {
                return generatedLoot;
            }
        }
        ItemStack newItem = ItemStack.EMPTY;

        if(this.lootTable != null) {
            LootParams lootparams = (new LootParams.Builder(context.getLevel())).withParameter(LootContextParams.ORIGIN, context.getParam(LootContextParams.ORIGIN)).withParameter(LootContextParams.TOOL, context.getParam(LootContextParams.TOOL)).withParameter(LootContextParams.THIS_ENTITY, context.getParam(LootContextParams.THIS_ENTITY)).withParameter(LootContextParams.KILLER_ENTITY, context.getParam(LootContextParams.KILLER_ENTITY)).withParameter(LootContextParams.THIS_ENTITY, context.getParam(LootContextParams.THIS_ENTITY)).withLuck(context.getLuck()).create(LootContextParamSets.FISHING);
            List<ItemStack> loot =  context.getLevel().getServer().getLootData().getLootTable(this.lootTable).getRandomItems(lootparams);
            if (!loot.isEmpty()) {
                newItem = loot.get(0);
            }
        }
        if (newItem.getItem() instanceof GasbassItem)
        {
            CompoundTag nbtData = new CompoundTag();
            nbtData.putDouble("rpgworldmod.return_pos_x", context.getParam(LootContextParams.ORIGIN).x);
            nbtData.putDouble("rpgworldmod.return_pos_y", context.getParam(LootContextParams.ORIGIN).y);
            nbtData.putDouble("rpgworldmod.return_pos_z", context.getParam(LootContextParams.ORIGIN).z);
            nbtData.putString("rpgworldmod.return_pos_dimension", context.getLevel().dimension().location().toString());
            newItem.setTag(nbtData);
        }
        // Create a new list to avoid modifying the original list during iteration
        ObjectArrayList<ItemStack> newLoot = new ObjectArrayList<>();

        for (ItemStack stack : generatedLoot) {
                // Replace the item if it matches
                newLoot.add(newItem);
        }
        return newLoot;
    }


    @Override
    public Codec<? extends IGlobalLootModifier> codec() {
        return CODEC.get();
    }
}