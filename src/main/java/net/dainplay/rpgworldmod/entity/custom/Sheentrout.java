package net.dainplay.rpgworldmod.entity.custom;

import net.dainplay.rpgworldmod.item.ModItems;
import net.dainplay.rpgworldmod.sounds.RPGSounds;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.AbstractSchoolingFish;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class Sheentrout extends AbstractSchoolingFish {
   public Sheentrout(EntityType<? extends Sheentrout> pEntityType, Level pLevel) {
      super(pEntityType, pLevel);
   }

   public ItemStack getBucketItemStack() {
      return new ItemStack(ModItems.SHEENTROUT_BUCKET.get());
   }

   protected SoundEvent getAmbientSound() {
      return RPGSounds.SHEENTROUT_AMBIENT.get();
   }

   protected SoundEvent getDeathSound() {
      return RPGSounds.SHEENTROUT_DEATH.get();
   }

   protected SoundEvent getHurtSound(DamageSource pDamageSource) {
      return RPGSounds.SHEENTROUT_HURT.get();
   }

   protected SoundEvent getFlopSound() {
      return RPGSounds.SHEENTROUT_FLOP.get();
   }
}