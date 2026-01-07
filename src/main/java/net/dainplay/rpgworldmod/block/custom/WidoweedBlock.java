package net.dainplay.rpgworldmod.block.custom;

import net.dainplay.rpgworldmod.block.ModBlocks;
import net.dainplay.rpgworldmod.damage.ModDamageTypes;
import net.dainplay.rpgworldmod.util.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.Tags;
import net.minecraft.util.RandomSource;

import javax.annotation.Nullable;

public class WidoweedBlock extends BushBlock {
    protected static final float AABB_OFFSET = 6.0F;
    protected RandomSource random = RandomSource.create();
    protected static final VoxelShape SHAPE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 16.0D, 16.0D);

    public WidoweedBlock(Properties properties) {
        super(properties);
    }
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        Vec3 vec3 = pState.getOffset(pLevel, pPos);
        return SHAPE.move(vec3.x, vec3.y, vec3.z);
    }

    @Override
    public PushReaction getPistonPushReaction(BlockState pState) {
        return PushReaction.BLOCK;
    }

    @Override
    public float getDestroyProgress(BlockState state, Player player, BlockGetter getter, BlockPos pos) {
        // ItemShears#getDestroySpeed is really dumb and doesn't check IShearable so we have to do it this way to try to match the wool break speed with shears
        return (player.getMainHandItem().getItem() instanceof ShearsItem || player.getMainHandItem().getItem() instanceof SwordItem) ? 1.0F : super.getDestroyProgress(state, player, getter, pos);
    }
    @Override
    public void entityInside(BlockState pState, Level pLevel, BlockPos pPos, Entity pEntity) {
        if ((pEntity instanceof LivingEntity && ((LivingEntity) pEntity).getMobType() == MobType.UNDEAD)) {
            pEntity.hurt(ModDamageTypes.getDamageSource(pLevel, ModDamageTypes.WIDOWEED), 2.5F);
            pEntity.makeStuckInBlock(pState, new Vec3(0.25D, 1.0F, 0.25D));
            pLevel.addParticle(ParticleTypes.HAPPY_VILLAGER, (double)pPos.getX() + 0.5D, (double)pPos.getY() + 0.5D, (double)pPos.getZ() + 0.5D, 0.0D, 0.0D, 0.0D);
        }
        else if (pEntity instanceof ItemEntity item && item.getItem().is(ModTags.Items.WIDOWEED_CONSUMABLE)) {
                item.hurt(ModDamageTypes.getDamageSource(pLevel, ModDamageTypes.WIDOWEED), 2.5F);
                if(pLevel instanceof ServerLevel serverLevel) serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER, pPos.getX()+0.5f, pPos.getY()+0.5f, pPos.getZ()+0.5f, 8, serverLevel.getRandom().nextFloat()/5, serverLevel.getRandom().nextFloat()/5, serverLevel.getRandom().nextFloat()/5, 0.0f);
                if((pLevel.getBlockState(pPos.south()).getBlock() == Blocks.AIR || pLevel.getBlockState(pPos.south()).is(BlockTags.REPLACEABLE)) && random.nextInt(2) > 0 && this.canSurvive(pLevel.getBlockState(pPos.south()), pLevel, pPos.south())) pLevel.setBlockAndUpdate(pPos.south(), ModBlocks.WIDOWEED.get().defaultBlockState());
                if((pLevel.getBlockState(pPos.north()).getBlock() == Blocks.AIR || pLevel.getBlockState(pPos.north()).is(BlockTags.REPLACEABLE)) && random.nextInt(2) > 0 && this.canSurvive(pLevel.getBlockState(pPos.north()), pLevel, pPos.north())) pLevel.setBlockAndUpdate(pPos.north(), ModBlocks.WIDOWEED.get().defaultBlockState());
                if((pLevel.getBlockState(pPos.west()).getBlock() == Blocks.AIR || pLevel.getBlockState(pPos.west()).is(BlockTags.REPLACEABLE)) && random.nextInt(2) > 0 && this.canSurvive(pLevel.getBlockState(pPos.west()), pLevel, pPos.west())) pLevel.setBlockAndUpdate(pPos.west(), ModBlocks.WIDOWEED.get().defaultBlockState());
                if((pLevel.getBlockState(pPos.east()).getBlock() == Blocks.AIR || pLevel.getBlockState(pPos.east()).is(BlockTags.REPLACEABLE)) && random.nextInt(2) > 0 && this.canSurvive(pLevel.getBlockState(pPos.east()), pLevel, pPos.east())) pLevel.setBlockAndUpdate(pPos.east(), ModBlocks.WIDOWEED.get().defaultBlockState());

                if((pLevel.getBlockState(pPos.south().below()).getBlock() == Blocks.AIR || pLevel.getBlockState(pPos.south().below()).is(BlockTags.REPLACEABLE)) && random.nextInt(2) > 0 && this.canSurvive(pLevel.getBlockState(pPos.south().below()), pLevel, pPos.south().below())) pLevel.setBlockAndUpdate(pPos.south().below(), ModBlocks.WIDOWEED.get().defaultBlockState());
                if((pLevel.getBlockState(pPos.north().below()).getBlock() == Blocks.AIR || pLevel.getBlockState(pPos.north().below()).is(BlockTags.REPLACEABLE)) && random.nextInt(2) > 0 && this.canSurvive(pLevel.getBlockState(pPos.north().below()), pLevel, pPos.north().below())) pLevel.setBlockAndUpdate(pPos.north().below(), ModBlocks.WIDOWEED.get().defaultBlockState());
                if((pLevel.getBlockState(pPos.west().below()).getBlock() == Blocks.AIR || pLevel.getBlockState(pPos.west().below()).is(BlockTags.REPLACEABLE)) && random.nextInt(2) > 0 && this.canSurvive(pLevel.getBlockState(pPos.west().below()), pLevel, pPos.west().below())) pLevel.setBlockAndUpdate(pPos.west().below(), ModBlocks.WIDOWEED.get().defaultBlockState());
                if((pLevel.getBlockState(pPos.east().below()).getBlock() == Blocks.AIR || pLevel.getBlockState(pPos.east().below()).is(BlockTags.REPLACEABLE)) && random.nextInt(2) > 0 && this.canSurvive(pLevel.getBlockState(pPos.east().below()), pLevel, pPos.east().below())) pLevel.setBlockAndUpdate(pPos.east().below(), ModBlocks.WIDOWEED.get().defaultBlockState());

                if((pLevel.getBlockState(pPos.south().above()).getBlock() == Blocks.AIR || pLevel.getBlockState(pPos.south().above()).is(BlockTags.REPLACEABLE)) && random.nextInt(2) > 0 && this.canSurvive(pLevel.getBlockState(pPos.south().above()), pLevel, pPos.south().above())) pLevel.setBlockAndUpdate(pPos.south().above(), ModBlocks.WIDOWEED.get().defaultBlockState());
                if((pLevel.getBlockState(pPos.north().above()).getBlock() == Blocks.AIR || pLevel.getBlockState(pPos.north().above()).is(BlockTags.REPLACEABLE)) && random.nextInt(2) > 0 && this.canSurvive(pLevel.getBlockState(pPos.north().above()), pLevel, pPos.north().above())) pLevel.setBlockAndUpdate(pPos.north().above(), ModBlocks.WIDOWEED.get().defaultBlockState());
                if((pLevel.getBlockState(pPos.west().above()).getBlock() == Blocks.AIR || pLevel.getBlockState(pPos.west().above()).is(BlockTags.REPLACEABLE)) && random.nextInt(2) > 0 && this.canSurvive(pLevel.getBlockState(pPos.west().above()), pLevel, pPos.west().above())) pLevel.setBlockAndUpdate(pPos.west().above(), ModBlocks.WIDOWEED.get().defaultBlockState());
                if((pLevel.getBlockState(pPos.east().above()).getBlock() == Blocks.AIR || pLevel.getBlockState(pPos.east().above()).is(BlockTags.REPLACEABLE)) && random.nextInt(2) > 0 && this.canSurvive(pLevel.getBlockState(pPos.east().above()), pLevel, pPos.east().above())) pLevel.setBlockAndUpdate(pPos.east().above(), ModBlocks.WIDOWEED.get().defaultBlockState());
        }
    }

    @Override
    public void playerDestroy(Level world, Player player, BlockPos pos, BlockState state, @Nullable BlockEntity te, ItemStack stack) {
        super.playerDestroy(world, player, pos, state, te, stack);
        if (!(player.getMainHandItem().getItem() instanceof ShearsItem || player.getMainHandItem().getItem() instanceof SwordItem)) player.hurt(ModDamageTypes.getDamageSource(world, ModDamageTypes.WIDOWEED), 2.5F);
    }

    private boolean shouldDamage(Entity entity) {
        return (((LivingEntity) entity).getMobType() == MobType.UNDEAD);
    }


    @Nullable
    @Override
    public BlockPathTypes getBlockPathType(BlockState state, BlockGetter getter, BlockPos pos, @Nullable Mob entity) {
        return entity != null && shouldDamage(entity) ? BlockPathTypes.DANGER_OTHER : null;
    }
    public BlockBehaviour.OffsetType getOffsetType() {
        return BlockBehaviour.OffsetType.XYZ;
    }
}
