package net.dainplay.rpgworldmod.item.custom;

import net.dainplay.rpgworldmod.biome.BiomeRegistry;
import net.dainplay.rpgworldmod.data.tags.ModAdvancements;
import net.dainplay.rpgworldmod.entity.ModEntities;
import net.dainplay.rpgworldmod.entity.custom.MosquitoSwarm;
import net.dainplay.rpgworldmod.item.ModItems;
import net.dainplay.rpgworldmod.util.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import top.theillusivec4.curios.api.SlotContext;

public class ChitinThimbleItem extends CurioItem {
	public ChitinThimbleItem(Properties properties) {
		super(properties);
	}

	@SuppressWarnings("removal")
	@Override
	public void curioTick(SlotContext slotContext, ItemStack stack) {
		Level level = slotContext.getWearer().level();
		Boolean swarmIsNear = false;
		ItemStack powder = ItemStack.EMPTY;
		if (!(slotContext.getWearer() instanceof Player player)) return;
		if (!player.getAbilities().instabuild)
			for (int i = 0; i < player.getInventory().getContainerSize(); ++i) {
				if (ModItems.CHITIN_POWDER.get() == (player.getInventory().getItem(i)).getItem()) {
					powder = player.getInventory().getItem(i);
					break;
				}
			}

		for (MosquitoSwarm swarm : player.level().getEntitiesOfClass(MosquitoSwarm.class, new AABB(player.blockPosition()).inflate(25.0F))) {
			if(swarm.getOwner() != null && swarm.getOwner() == player && swarm.getSize() >= 3) swarmIsNear = true;
		}
		BlockState playerState = level.getBlockState(player.blockPosition());

		if (!player.getCooldowns().isOnCooldown(stack.getItem())
				&& !((level.isRaining() && level.getBiome(new BlockPos(player.blockPosition().getX(), (int) player.getEyeY(), player.blockPosition().getZ())).value().getPrecipitationAt(new BlockPos(player.blockPosition().getX(), (int) player.getEyeY(), player.blockPosition().getZ())) == Biome.Precipitation.RAIN && level.canSeeSky(new BlockPos(player.blockPosition().getX(), (int) player.getEyeY(), player.blockPosition().getZ()))) || (level.getBiome(player.getOnPos()).is(BiomeRegistry.RIE_WEALD) && level.isRaining()))
				&& (!powder.isEmpty() || player.getAbilities().instabuild)
				&& !swarmIsNear
				&& playerState.getFluidState().isEmpty()
				&& !playerState.is(BlockTags.FIRE)
				&& !playerState.is(Blocks.NETHER_PORTAL)
				&& !playerState.is(ModTags.Blocks.STICKY_FOR_MOSQUITOS)
				&& !hasStickyBlockNearby(player)
				&& level.dimension() != Level.NETHER
		) {
			if (!level.isClientSide) {
				player.getCooldowns().addCooldown(stack.getItem(), 200);
				ServerLevel serverLevel = (ServerLevel) level;

				MosquitoSwarm swarm = new MosquitoSwarm(ModEntities.MOSQUITO_SWARM.get(), level);
				swarm.dealtDamage();
				swarm.setSize(1);
				swarm.setPos(player.getX(), player.getY(), player.getZ());

				serverLevel.addFreshEntityWithPassengers(swarm);
				swarm.tame(player);
				if (!player.getAbilities().instabuild) {
					powder.shrink(1);
					if (powder.isEmpty()) {
						player.getInventory().removeItem(powder);
					}
				}
			}
		}
		super.curioTick(slotContext.identifier(), slotContext.index(), slotContext.entity(), stack);
	}



	public boolean hasStickyBlockNearby(LivingEntity entity) {
		Level level = entity.level();
		BlockPos centerPos = entity.blockPosition();

		BlockPos[] directions = {
				centerPos.above(),    // +Y
				centerPos.below(),    // -Y
				centerPos.north(),    // -Z
				centerPos.south(),    // +Z
				centerPos.west(),     // -X
				centerPos.east()      // +X
		};

		for (BlockPos checkPos : directions) {
			BlockState state = level.getBlockState(checkPos);
			if (state.is(ModTags.Blocks.STICKY_FOR_MOSQUITOS)) {
				return true;
			}
		}

		return false;
	}
}
