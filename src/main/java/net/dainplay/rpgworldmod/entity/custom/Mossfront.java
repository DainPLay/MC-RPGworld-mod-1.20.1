package net.dainplay.rpgworldmod.entity.custom;

import net.dainplay.rpgworldmod.effect.ModEffects;
import net.dainplay.rpgworldmod.item.ModItems;
import net.dainplay.rpgworldmod.sounds.RPGSounds;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.AbstractSchoolingFish;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class Mossfront extends AbstractSchoolingFish {
   public Mossfront(EntityType<? extends Mossfront> pEntityType, Level pLevel) {
      super(pEntityType, pLevel);
   }

   public ItemStack getBucketItemStack() {
      return new ItemStack(ModItems.MOSSFRONT_BUCKET.get());
   }

   protected SoundEvent getAmbientSound() {
      return RPGSounds.MOSSFRONT_AMBIENT.get();
   }

   protected SoundEvent getDeathSound() {
      return RPGSounds.MOSSFRONT_DEATH.get();
   }

   protected SoundEvent getHurtSound(DamageSource pDamageSource) {
      return RPGSounds.MOSSFRONT_HURT.get();
   }

   protected SoundEvent getFlopSound() {
      return RPGSounds.MOSSFRONT_FLOP.get();
   }

   public boolean canBeAffected(MobEffectInstance pPotioneffect) {
      return pPotioneffect.getEffect() == ModEffects.MOSSIOSIS.get() ? false : super.canBeAffected(pPotioneffect);
   }
}