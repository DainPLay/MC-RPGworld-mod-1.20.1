package net.dainplay.rpgworldmod.entity.custom;

import net.dainplay.rpgworldmod.item.ModItems;
import net.dainplay.rpgworldmod.sounds.RPGSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.animal.AbstractSchoolingFish;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;

public class Gasbass extends AbstractSchoolingFish {
   public Gasbass(EntityType<? extends Gasbass> pEntityType, Level pLevel) {
      super(pEntityType, pLevel);
   }

   public ItemStack getBucketItemStack() {
      return new ItemStack(ModItems.GASBASS_BUCKET.get());
   }

   protected SoundEvent getAmbientSound() {
      return RPGSounds.GASBASS_AMBIENT.get();
   }

   protected SoundEvent getDeathSound() {
      return RPGSounds.GASBASS_DEATH.get();
   }

   protected SoundEvent getHurtSound(DamageSource pDamageSource) {
      return RPGSounds.GASBASS_HURT.get();
   }

   protected SoundEvent getFlopSound() {
      return RPGSounds.GASBASS_FLOP.get();
   }

   @Override
   protected void dropCustomDeathLoot(DamageSource pSource, int pLooting, boolean pRecentlyHit) {
      super.dropCustomDeathLoot(pSource, pLooting, pRecentlyHit);

      ItemStack itemstack = ModItems.GASBASS.get().getDefaultInstance();
      CompoundTag nbtData = new CompoundTag();
      nbtData.putDouble("rpgworldmod.return_pos_x", this.getX());
      nbtData.putDouble("rpgworldmod.return_pos_y", this.getY());
      nbtData.putDouble("rpgworldmod.return_pos_z", this.getZ());
      nbtData.putString("rpgworldmod.return_pos_dimension", this.getCommandSenderWorld().dimension().location().toString());
      itemstack.setTag(nbtData);
      this.spawnAtLocation(itemstack);

   }
   private static void emitSmokeParticles(LivingEntity entity) {
      double x = entity.getX();
      double y = entity.getY() + entity.getEyeHeight() / 2.0;
      double z = entity.getZ();

      // Adjust these values for particle spread and density.
      for (int i = 0; i < 10; i++) { // Increased particle count
         double offsetX = (entity.getRandom().nextDouble() - 0.5) * 0.8; // Particles spread
         double offsetY = (entity.getRandom().nextDouble() - 0.5) * 0.4;
         double offsetZ = (entity.getRandom().nextDouble() - 0.5) * 0.8;

         entity.level().addParticle(
                 ParticleTypes.SMOKE,
                 x + offsetX,
                 y + offsetY,
                 z + offsetZ,
                 0.0, 0.02, 0.0 // Slight upward movement
         );
      }
   }
   @Override
   public void tick() {
      super.tick();
      if (this.tickCount % 5 == 0) {
         emitSmokeParticles(this);
      }

   }

   public static boolean checkGasbassSpawnRules(EntityType<Gasbass> gasbass, LevelAccessor level, MobSpawnType type, BlockPos pPos, RandomSource random) {
      int i = level.getSeaLevel();
      int j = i - 13;
      return pPos.getY() >= j && pPos.getY() <= i && level.getFluidState(pPos.below()).is(FluidTags.WATER) && level.getBlockState(pPos.above()).is(Blocks.WATER) && (level.getLevelData().isRaining() || level.getLevelData().isThundering());
   }

}