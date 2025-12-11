package net.dainplay.rpgworldmod.item.custom;

import net.dainplay.rpgworldmod.data.tags.ModAdvancements;
import net.dainplay.rpgworldmod.entity.ModEntities;
import net.dainplay.rpgworldmod.entity.custom.MosquitoSwarm;
import net.dainplay.rpgworldmod.item.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;

import java.util.Objects;
import java.util.UUID;

public class MosquitoBottleItem extends Item {
    public MosquitoBottleItem(Properties p_41580_) {
        super(p_41580_);
    }

    @Override
    public InteractionResult useOn(UseOnContext pContext) {
        Level level = pContext.getLevel();
        if (!(level instanceof ServerLevel)) {
            return InteractionResult.SUCCESS;
        } else {
            ItemStack itemstack = pContext.getItemInHand();
            BlockPos blockpos = pContext.getClickedPos();
            Direction direction = pContext.getClickedFace();
            BlockState blockstate = level.getBlockState(blockpos);

            BlockPos blockpos1;
            if (blockstate.getCollisionShape(level, blockpos).isEmpty()) {
                blockpos1 = blockpos;
            } else {
                blockpos1 = blockpos.relative(direction);
            }

            EntityType<?> entitytype = ModEntities.MOSQUITO_SWARM.get();
            Entity entity = entitytype.spawn((ServerLevel)level, itemstack, pContext.getPlayer(), blockpos1, MobSpawnType.SPAWN_EGG, true, !Objects.equals(blockpos, blockpos1) && direction == Direction.UP);
            if (entity != null) {
                ((MosquitoSwarm)entity).setSize(1);
                if(itemstack.hasTag()){
                    UUID ownerid = itemstack.getTag().getUUID("rpgworldmod.mosquitos_owner");
                    Entity owner = (((ServerLevel) level).getEntity(ownerid));
                    if(owner instanceof Player playerOwner) ((MosquitoSwarm)entity).tame(playerOwner);
                }
                Player player = pContext.getPlayer();
                if (!player.isCreative() && !level.isClientSide) {
                    itemstack.shrink(1);
                    if (itemstack.isEmpty())
                        player.setItemInHand(pContext.getHand(), new ItemStack(Items.GLASS_BOTTLE));
                    else if (player.getInventory().getFreeSlot() == -1)
                        player.drop(new ItemStack(Items.GLASS_BOTTLE), false);
                    else player.getInventory().add(new ItemStack(Items.GLASS_BOTTLE));
                }
                player.playSound(SoundEvents.BOTTLE_EMPTY, 1.0F, 1.0F);
                level.gameEvent(pContext.getPlayer(), GameEvent.ENTITY_PLACE, blockpos);
                if (pContext.getPlayer() instanceof ServerPlayer serverPlayer && level.dimension() == Level.NETHER) {
                    ModAdvancements.RELEASE_MOSQUITOS_IN_NETHER.trigger(serverPlayer);
                }
            }

            return InteractionResult.CONSUME;
        }
    }
}