package net.dainplay.rpgworldmod.loot;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.dainplay.rpgworldmod.item.custom.NetherStarScrollItem;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class NetherStarScrollPickaxeModifier extends LootModifier {

    public static final Codec<NetherStarScrollPickaxeModifier> CODEC = RecordCodecBuilder.create(inst ->
            LootModifier.codecStart(inst).apply(inst, NetherStarScrollPickaxeModifier::new)
    );

    public NetherStarScrollPickaxeModifier(LootItemCondition[] conditions) {
        super(conditions);
    }

    @Override
    protected @NotNull ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        // Получаем инструмент, которым был сломан блок
        ItemStack tool = context.getParamOrNull(LootContextParams.TOOL);
        if (!(tool.getItem() instanceof NetherStarScrollItem scrollItem)) return generatedLoot;
        // Проверяем, что свиток находится в режиме кирки
        if (!scrollItem.isPickaxeMode(tool)) return generatedLoot;

        // Получаем игрока (добытчика)
        Entity entity = context.getParamOrNull(LootContextParams.THIS_ENTITY);
        if (!(entity instanceof Player player)) return generatedLoot;

        // Получаем информацию о блоке
        BlockState state = context.getParamOrNull(LootContextParams.BLOCK_STATE);
        if (state == null) return generatedLoot;

        Vec3 origin = context.getParamOrNull(LootContextParams.ORIGIN);
        if (origin == null) return generatedLoot;
        BlockPos pos = new BlockPos((int) origin.x, (int) origin.y, (int) origin.z);

        // Создаём копию инструмента и добавляем нужное зачарование
        ItemStack modifiedTool = Items.NETHERITE_PICKAXE.getDefaultInstance();
        if (player.isShiftKeyDown()) {
            // Зажат Shift → Удача III
            modifiedTool.enchant(Enchantments.BLOCK_FORTUNE, 3);
        } else {
            // Не зажат Shift → Шёлковое касание
            modifiedTool.enchant(Enchantments.SILK_TOUCH, 1);
        }

        // Генерируем дроп заново с модифицированным инструментом
        List<ItemStack> newDrops = Block.getDrops(
                state,
                context.getLevel(),
                pos,
                context.getParamOrNull(LootContextParams.BLOCK_ENTITY),
                entity,
                modifiedTool
        );

        ObjectArrayList<ItemStack> result = new ObjectArrayList<>();
        result.addAll(newDrops);
        return result;
    }

    @Override
    public Codec<? extends IGlobalLootModifier> codec() {
        return CODEC;
    }
}