package net.dainplay.rpgworldmod.block.entity.custom;

import net.dainplay.rpgworldmod.block.custom.BlowerBlock;
import net.dainplay.rpgworldmod.block.entity.ModBlockEntities;
import net.dainplay.rpgworldmod.data.tags.ModAdvancements;
import net.dainplay.rpgworldmod.entity.custom.Fireflantern;
import net.dainplay.rpgworldmod.particle.ModParticles;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.List;
import java.util.stream.Stream;

import static net.dainplay.rpgworldmod.block.custom.BlowerBlock.RANGE;

public class BlowerBlockEntity extends BlockEntity {

    public BlowerBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(ModBlockEntities.BLOWER_BLOCK_ENTITY.get(), blockPos, blockState);
    }
    protected double getBlowerSpeed() {
        return 0.1;
    }


    protected AABB getScan(int boxLength) {
        return switch (getBlockState().getValue(BlockStateProperties.FACING)) {
            case DOWN, NORTH, WEST -> new AABB(worldPosition, worldPosition.relative(getBlockState().getValue(BlockStateProperties.FACING), boxLength + 1).offset(1, 1, 1));
            default -> new AABB(worldPosition, worldPosition.relative(getBlockState().getValue(BlockStateProperties.FACING), boxLength).offset(1, 1, 1));
        };
    }

    public static void tick(Level pLevel, BlockPos pPos, BlockState pState, BlowerBlockEntity blowerBlockEntity) {

            for (int i = 1; i <= pState.getValue(BlockStateProperties.POWER); ++i) {
                BlockPos scanPos = pPos.relative(pState.getValue(BlockStateProperties.FACING), i);
                BlockState blockState =  pLevel.getBlockState(scanPos);
                if (blockState.isFaceSturdy(pLevel, scanPos, pState.getValue(BlockStateProperties.FACING))) {
                    pLevel.setBlock(pPos, pState.setValue(RANGE, i - 1), 2);
                    break;
                }
                pLevel.setBlock(pPos, pState.setValue(RANGE, pState.getValue(BlockStateProperties.POWER)), 2);
            }
        if (!pLevel.isClientSide) {
            if (pState.getValue(BlockStateProperties.POWERED) && pLevel.hasNeighborSignal(pPos) && pLevel.getBestNeighborSignal(pPos) != pState.getValue(BlockStateProperties.POWER)) {
                pLevel.setBlock(pPos, pState.setValue(BlockStateProperties.POWER, pLevel.getBestNeighborSignal(pPos)), 2);
            }
            if (pState.getValue(BlockStateProperties.POWERED) && !pLevel.hasNeighborSignal(pPos)) {
                pLevel.setBlock(pPos, pState.setValue(BlockStateProperties.POWERED, false), 2);
            }
            if (!pState.getValue(BlockStateProperties.POWERED))
                pLevel.setBlock(pPos, pState.setValue(BlockStateProperties.POWER, 0).setValue(RANGE, 0), 2);
        }
        if (pState.getValue(RANGE) > 0) {
            moveEntities(pLevel, pPos, pState, blowerBlockEntity, blowerBlockEntity.getScan(pState.getValue(RANGE)));
            spawnParticles(pLevel, pPos, pState.getValue(BlockStateProperties.FACING), pState.getValue(RANGE), pState.getValue(BlockStateProperties.POWER));
        }
    }

    private static void spawnParticles(Level pLevel, BlockPos pPos, Direction facing, int speed, int power) {
        SimpleParticleType particleType = ModParticles.LEAVES.get();

        double xOffset = 0.5;
        double yOffset = 0.5;
        double zOffset = 0.5;
        double newspeed = (double) speed / 8.5 * 15;

        double xSpeed = facing.getStepX() * newspeed * 0.03;
        double ySpeed = facing.getStepY() * newspeed * 0.03;
        double zSpeed = facing.getStepZ() * newspeed * 0.03;

        pLevel.addParticle(particleType,
                pPos.getX() + xOffset + (pLevel.random.nextDouble() - 0.5) * 0.8,
                pPos.getY() + yOffset + (pLevel.random.nextDouble() - 0.5) * 0.4,
                pPos.getZ() + zOffset + (pLevel.random.nextDouble() - 0.5) * 0.8,
                xSpeed, ySpeed, zSpeed
        );
    }
    protected static void moveEntities(Level pLevel, BlockPos pPos, BlockState pState, BlowerBlockEntity blowerBlockEntity, AABB scan) {
        List<Entity> newList = Stream.concat(pLevel.getEntitiesOfClass(Entity.class, scan).stream(), pLevel.getEntitiesOfClass(Entity.class, ((BlowerBlock)pState.getBlock()).getAlignedBlockAABB(pLevel, pPos, pState)).stream()).toList();
        for (Entity entity : newList) {
//            if (!(entity instanceof Player && ((Player) entity).abilities.flying)) {
                addMotion(entity, blowerBlockEntity, pState);
//            }
            if (pState.getValue(BlockStateProperties.FACING) == Direction.UP) {
                entity.fallDistance = 0;
            }
        }
    }

    protected static void addMotion(Entity entity, BlowerBlockEntity blowerBlockEntity, BlockState blockState) {
        if(entity instanceof Fireflantern) {
            for (ServerPlayer serverplayer : entity.level().getEntitiesOfClass(ServerPlayer.class, new AABB(blowerBlockEntity.worldPosition).inflate(10.0D, 15.0D, 10.0D))) {
                ModAdvancements.BLOW_AWAY_A_FIREFLANTERN.trigger(serverplayer);
            }
        }
        Vec3 motion = entity.getDeltaMovement();
        double speed = blowerBlockEntity.getBlowerSpeed() * ((double) blockState.getValue(BlockStateProperties.POWER) / 15D);
        if (entity.onGround()) {
            speed /= 4D;
        }
        switch (blockState.getValue(BlockStateProperties.FACING)) {
            case DOWN -> entity.setDeltaMovement(motion.x, motion.y - speed, motion.z);
            case UP -> entity.setDeltaMovement(motion.x, motion.y + speed, motion.z);
            case NORTH -> entity.setDeltaMovement(motion.x, motion.y, motion.z - speed);
            case SOUTH -> entity.setDeltaMovement(motion.x, motion.y, motion.z + speed);
            case WEST -> entity.setDeltaMovement(motion.x - speed, motion.y, motion.z);
            case EAST -> entity.setDeltaMovement(motion.x + speed, motion.y, motion.z);
        }
    }
}