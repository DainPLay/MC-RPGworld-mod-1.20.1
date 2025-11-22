package net.dainplay.rpgworldmod.util;

import com.google.common.collect.Lists;
import net.dainplay.rpgworldmod.block.custom.DrillTuskBlock;
import net.dainplay.rpgworldmod.block.custom.QuartziteBlock;
import net.dainplay.rpgworldmod.block.custom.QuartziteDrillTuskBlock;
import net.dainplay.rpgworldmod.data.tags.ModAdvancements;
import net.dainplay.rpgworldmod.effect.ModEffects;
import net.dainplay.rpgworldmod.sounds.RPGSounds;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.piston.PistonBaseBlock;
import net.minecraft.world.level.block.piston.PistonStructureResolver;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.Tags;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.level.PistonEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.List;
import java.util.Objects;

public class DrillTuskHandler {
    static final RandomSource random = RandomSource.create();



    public static List<BlockPos> resolve(PistonEvent.Pre event) {
        List<BlockPos> toPush = Lists.newArrayList();
        Direction pistonDirection = event.getDirection();
        BlockPos pistonPos = event.getPos();
        Direction pushDirection;
        BlockPos startPos;
        boolean extending = event.getPistonMoveType().isExtend;
        if (extending) {
            pushDirection = pistonDirection;
            startPos = pistonPos.relative(pistonDirection);
        } else {
            pushDirection = pistonDirection.getOpposite();
            startPos = pistonPos.relative(pistonDirection, 2);
        }
        LevelAccessor level = event.getLevel();
        BlockState blockstate = level.getBlockState(startPos);
        if (!PistonBaseBlock.isPushable(blockstate, (Level) level, startPos, pushDirection, false, pistonDirection)) {
            return toPush;
        } else if (!addBlockLine(startPos, pushDirection, level, pushDirection, pistonPos, toPush)) {
            return toPush;
        } else {
            for(int i = 0; i < toPush.size(); ++i) {
                BlockPos blockpos = toPush.get(i);
                if (level.getBlockState(blockpos).isStickyBlock() && !addBranchingBlocks(blockpos, level, pushDirection, pistonPos, toPush)) {
                    return toPush;
                }
            }

            return toPush;
        }
    }
    public static boolean addBlockLine(BlockPos pOriginPos, Direction pDirection, LevelAccessor level, Direction pushDirection,
                                       BlockPos pistonPos, List<BlockPos> toPush) {
        BlockState blockstate = level.getBlockState(pOriginPos);
        if (level.isEmptyBlock(pOriginPos)) {
            return true;
        } else if (!PistonBaseBlock.isPushable(blockstate, (Level) level, pOriginPos, pushDirection, false, pDirection)) {
            return true;
        } else if (pOriginPos.equals(pistonPos)) {
            return true;
        } else if (toPush.contains(pOriginPos)) {
            return true;
        } else {
            int i = 1;
            if (i + toPush.size() > 12) {
                return false;
            } else {
                BlockState oldState;
                while(blockstate.isStickyBlock()) {
                    BlockPos blockpos = pOriginPos.relative(pushDirection.getOpposite(), i);
                    oldState = blockstate;
                    blockstate = level.getBlockState(blockpos);
                    if (blockstate.isAir() || !(oldState.canStickTo(blockstate) && blockstate.canStickTo(oldState)) || !PistonBaseBlock.isPushable(blockstate, (Level) level, blockpos, pushDirection, false, pushDirection.getOpposite()) || blockpos.equals(pistonPos)) {
                        break;
                    }

                    ++i;
                    if (i + toPush.size() > 12) {
                        return false;
                    }
                }

                int l = 0;

                for(int i1 = i - 1; i1 >= 0; --i1) {
                    toPush.add(pOriginPos.relative(pushDirection.getOpposite(), i1));
                    ++l;
                }

                int j1 = 1;

                while(true) {
                    BlockPos blockpos1 = pOriginPos.relative(pushDirection, j1);
                    int j = toPush.indexOf(blockpos1);
                    if (j > -1) {
                        toPush = reorderListAtCollision(l, j, toPush);

                        for(int k = 0; k <= j + l; ++k) {
                            BlockPos blockpos2 = toPush.get(k);
                            if (level.getBlockState(blockpos2).isStickyBlock() && !addBranchingBlocks(blockpos2, level, pushDirection, pistonPos, toPush)) {
                                return false;
                            }
                        }

                        return true;
                    }

                    blockstate = level.getBlockState(blockpos1);
                    if (blockstate.isAir()) {
                        return true;
                    }

                    if (!PistonBaseBlock.isPushable(blockstate, (Level) level, blockpos1, pushDirection, true, pushDirection) || blockpos1.equals(pistonPos)) {
                        return false;
                    }

                    if (blockstate.getPistonPushReaction() == PushReaction.DESTROY) {
                        return true;
                    }

                    if (toPush.size() >= 12) {
                        return false;
                    }

                    toPush.add(blockpos1);
                    ++l;
                    ++j1;
                }
            }
        }
    }


    public static List<BlockPos> reorderListAtCollision(int pOffsets, int pIndex, List<BlockPos> toPush) {
        List<BlockPos> list = Lists.newArrayList();
        List<BlockPos> list1 = Lists.newArrayList();
        List<BlockPos> list2 = Lists.newArrayList();
        list.addAll(toPush.subList(0, pIndex));
        list1.addAll(toPush.subList(toPush.size() - pOffsets, toPush.size()));
        list2.addAll(toPush.subList(pIndex, toPush.size() - pOffsets));
        toPush.clear();
        toPush.addAll(list);
        toPush.addAll(list1);
        toPush.addAll(list2);
        return toPush;
    }

    public static boolean addBranchingBlocks(BlockPos pFromPos, LevelAccessor level, Direction pushDirection, BlockPos pistonPos, List<BlockPos> toPush) {
        BlockState blockstate = level.getBlockState(pFromPos);

        for(Direction direction : Direction.values()) {
            if (direction.getAxis() != pushDirection.getAxis()) {
                BlockPos blockpos = pFromPos.relative(direction);
                BlockState blockstate1 = level.getBlockState(blockpos);
                if (blockstate1.canStickTo(blockstate) && blockstate.canStickTo(blockstate1) && !addBlockLine(blockpos, direction, level, pushDirection, pistonPos, toPush)) {
                    return false;
                }
            }
        }

        return true;
    }


    @SubscribeEvent
    public static void prePistonMove(PistonEvent.Pre event){
        LevelAccessor level = event.getLevel();
        List<BlockPos> toPush = resolve(event);
        Direction movingDirection;
        if(event.getPistonMoveType() == PistonEvent.PistonMoveType.EXTEND) movingDirection = event.getDirection();
        else movingDirection = event.getDirection().getOpposite();

        for(int i = 0; i <= toPush.size()-1; i++) {
            BlockPos pos = toPush.get(i);
            BlockState blockstate = level.getBlockState(pos);

            if(blockstate.getBlock() instanceof DrillTuskBlock && DrillTuskBlock.getConnectedDirection(blockstate) != event.getDirection())
            {
                Block.dropResources(blockstate, level, pos, null);
                level.setBlock(pos, Blocks.AIR.defaultBlockState(), 18);
                level.gameEvent(GameEvent.BLOCK_DESTROY, pos, GameEvent.Context.of(blockstate));
            }
            else {
                PistonStructureResolver sResolver = event.getStructureHelper();
                assert sResolver != null;
                sResolver.resolve();
                if(sResolver.getToPush().contains(pos))
                    if (blockstate.getBlock() instanceof DrillTuskBlock && DrillTuskBlock.getConnectedDirection(blockstate) == movingDirection) {
                        BlockPos pos1 = pos.relative(movingDirection);
                        BlockState blockstate1 = level.getBlockState(pos1);
                        if (blockstate1.getBlock().getExplosionResistance() < 20F) {
                            if(blockstate1.is(Tags.Blocks.ORES))
                                for(ServerPlayer serverplayer : level.getEntitiesOfClass(ServerPlayer.class, new AABB(pos1).inflate(10.0D, 5.0D, 10.0D))) {
                                    ModAdvancements.DRILL_TUSK_ORE.trigger(serverplayer);
                                }
                            if (level.isClientSide())
                                ((ClientLevel)level).addDestroyBlockEffect(pos1, blockstate1);
                            if (blockstate.getBlock() instanceof QuartziteDrillTuskBlock) {
                                ItemStack silkTouchTool = new ItemStack(Items.DIAMOND_PICKAXE);
                                silkTouchTool.enchant(Enchantments.SILK_TOUCH, 1);

                                LootParams.Builder lootparams$builder = (new LootParams.Builder((ServerLevel)level)).withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(pos1)).withParameter(LootContextParams.TOOL, silkTouchTool);
                                List<ItemStack> drops = blockstate1.getDrops(lootparams$builder);

                                for(ItemStack itemStack: drops) {
                                    Block.popResource((Level) level, pos1, itemStack);
                                }
                            }
                                else Block.dropResources(blockstate1, level, pos1, null);
                            level.playSound(null, pos1, RPGSounds.DRILL.get(), SoundSource.BLOCKS, 0.5F, (random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F);
                            level.setBlock(pos1, Blocks.AIR.defaultBlockState(), 18);
                            level.gameEvent(GameEvent.BLOCK_DESTROY, pos1, GameEvent.Context.of(blockstate1));
                        }
                    }
            }

        }
    }
}
