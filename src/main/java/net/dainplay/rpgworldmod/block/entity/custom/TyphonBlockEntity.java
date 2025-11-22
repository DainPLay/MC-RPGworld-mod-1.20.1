package net.dainplay.rpgworldmod.block.entity.custom;

import net.dainplay.rpgworldmod.block.entity.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.behavior.SetWalkTargetAwayFrom;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.function.Predicate;

import java.util.List;

import static net.dainplay.rpgworldmod.block.custom.GlossomBlock.GLOWING;

public class TyphonBlockEntity extends BlockEntity {
    public TyphonBlockEntity(BlockPos pWorldPosition, BlockState pBlockState) {
        super(ModBlockEntities.TYPHON_BLOCK_ENTITY.get(), pWorldPosition, pBlockState);
    }

    public static void tick(Level level, BlockPos blockPos, BlockState blockState, TyphonBlockEntity typhonBlockEntity) {
        Predicate<Entity> mobPredicate = entity -> entity instanceof PathfinderMob;
        AABB aabb = new AABB(
                blockPos.getX() - 9,
                blockPos.getY() - 9,
                blockPos.getZ() - 9,
                blockPos.getX() + 9,
                blockPos.getY() + 9,
                blockPos.getZ() + 9
        );
        List<Entity> foundEntities =  level.getEntities((Entity) null, aabb, mobPredicate);
        for (Entity entity : foundEntities) {
            if (entity instanceof PathfinderMob mob){
                fleeFrom(mob, 1.2F, 10, blockPos);
            }
        }
    }



    public static boolean fleeFrom(PathfinderMob livingEntity, float speedModifier, int desiredDistance, BlockPos fleeFrom) {
        if (livingEntity == null || livingEntity.level() == null || fleeFrom == null) {
            return false; // Handle null cases
        }

        PathNavigation navigation = livingEntity.getNavigation();

        Vec3 entityPos = livingEntity.position();
        Vec3 fleePos = Vec3.atBottomCenterOf(fleeFrom);

        if (!entityPos.closerThan(fleePos, (double) desiredDistance)) {
            return false; // Already far enough
        }

        // Calculate flee position (similar to SetWalkTargetAwayFrom)
        Vec3 fleeTarget = null;
        for (int i = 0; i < 10; i++){
            Vec3 tempFleeTarget = LandRandomPos.getPosAway(livingEntity, 16, 7, fleePos);
            if(tempFleeTarget != null){
                fleeTarget = tempFleeTarget;
                break;
            }
        }

        if (fleeTarget != null) {
            return navigation.moveTo(fleeTarget.x(), fleeTarget.y(), fleeTarget.z(), speedModifier);
        }

        return false; // No valid flee position found
    }

}
