package net.dainplay.rpgworldmod.features;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.dainplay.rpgworldmod.RPGworldMod;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecorator;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecoratorType;

public class TrunkDecorator extends TreeDecorator {
    public static final Codec<TrunkDecorator> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    Codec.intRange(0, 64).fieldOf("placement_count").forGetter(o -> o.count),
                    Codec.floatRange(0f, 1f).fieldOf("probability_of_placement").forGetter(o -> o.probability),
                    BlockStateProvider.CODEC.fieldOf("deco_provider").forGetter(o -> o.decoration)
            ).apply(instance, TrunkDecorator::new)
    );

    private final int count;
    private final float probability;
    private final BlockStateProvider decoration;

    public TrunkDecorator(int count, float probability, BlockStateProvider decorator) {
        this.count = count;
        this.probability = probability;
        this.decoration = decorator;
    }

    @Override
    protected TreeDecoratorType<TrunkDecorator> type() {
        return RPGFeatureModifiers.TRUNK_DECORATOR.get();
    }

    @Override
    public void place(Context context) {
        int blockCount = context.logs().size();

        if (blockCount == 0) {
            RPGworldMod.LOGGER.error("[TrunkDecorator] Trunk Blocks were empty! Why?");
            return;
        }
            if (context.random().nextFloat() >= this.probability) return;

            //initial position on the tree trunk
            BlockPos logPos = context.logs().get(2);
            //commit and set our new placement position there
            context.setBlock(logPos, this.decoration.getState(context.random(), logPos));
}
}
