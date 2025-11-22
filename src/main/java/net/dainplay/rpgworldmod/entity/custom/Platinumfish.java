package net.dainplay.rpgworldmod.entity.custom;

import net.dainplay.rpgworldmod.item.ModItems;
import net.dainplay.rpgworldmod.sounds.RPGSounds;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.AbstractSchoolingFish;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

public class Platinumfish extends AbstractSchoolingFish {
   public Platinumfish(EntityType<? extends Platinumfish> pEntityType, Level pLevel) {
      super(pEntityType, pLevel);
   }

   public ItemStack getBucketItemStack() {
      return new ItemStack(ModItems.PLATINUMFISH_BUCKET.get());
   }

   protected SoundEvent getAmbientSound() {
      return RPGSounds.PLATINUMFISH_AMBIENT.get();
   }

   protected SoundEvent getDeathSound() {
      return RPGSounds.PLATINUMFISH_DEATH.get();
   }

   protected SoundEvent getHurtSound(DamageSource pDamageSource) {
      return RPGSounds.PLATINUMFISH_HURT.get();
   }

   protected SoundEvent getFlopSound() {
      return RPGSounds.PLATINUMFISH_FLOP.get();
   }
}