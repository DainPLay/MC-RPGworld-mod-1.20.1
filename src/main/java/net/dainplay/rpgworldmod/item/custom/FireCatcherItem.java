package net.dainplay.rpgworldmod.item.custom;

import net.dainplay.rpgworldmod.block.custom.FireCatcherBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.gameevent.GameEvent;

public class FireCatcherItem extends BlockItem {
    public FireCatcherItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public InteractionResult place(BlockPlaceContext context) {
        if (!context.canPlace()) {
            return InteractionResult.FAIL;
        }

        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();
        
        // Проверяем размещение под потолком
        BlockPos abovePos = pos.above();
        if (!level.getBlockState(abovePos).isSolidRender(level, abovePos)) {
            return InteractionResult.FAIL;
        }
        
        // Проверяем место для нижней части
        if (!level.getBlockState(pos.below()).canBeReplaced(context)) {
            return InteractionResult.FAIL;
        }

        BlockState bottomState = this.getBlock().defaultBlockState()
            .setValue(FireCatcherBlock.HALF, Half.BOTTOM)
            .setValue(FireCatcherBlock.HUNGRY, false);
            
        BlockState topState = this.getBlock().defaultBlockState()
            .setValue(FireCatcherBlock.HALF, Half.TOP)
            .setValue(FireCatcherBlock.HUNGRY, false);

        // Размещаем нижнюю часть
        level.setBlock(pos.below(), bottomState, 11);
        
        // Размещаем верхнюю часть
        level.setBlock(pos, topState, 11);

        // Звук размещения
        level.playSound(player, pos, SoundEvents.MOSS_PLACE, SoundSource.BLOCKS, 1.0F, 1.0F);
        
        // Game event для анимации
        level.gameEvent(GameEvent.BLOCK_PLACE, pos, GameEvent.Context.of(player, topState));

        // Тратим предмет (если не креатив)
        if (player != null && !player.isCreative()) {
            context.getItemInHand().shrink(1);
        }

        return InteractionResult.sidedSuccess(level.isClientSide());
    }
}