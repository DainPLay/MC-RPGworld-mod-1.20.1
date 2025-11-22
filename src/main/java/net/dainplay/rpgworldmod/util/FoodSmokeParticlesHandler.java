package net.dainplay.rpgworldmod.util;

import net.dainplay.rpgworldmod.RPGworldMod;
import net.dainplay.rpgworldmod.item.ModItems;
import net.minecraft.core.particles.ParticleTypes;
   import net.minecraft.world.entity.LivingEntity;
   import net.minecraft.world.item.Item;
   import net.minecraft.world.item.ItemStack;
   import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
   import net.minecraftforge.eventbus.api.SubscribeEvent;
   import net.minecraftforge.fml.common.Mod;

   public class FoodSmokeParticlesHandler {

       @SubscribeEvent
       public static void onLivingEntityUseItem(LivingEntityUseItemEvent event) {
           ItemStack itemStack = event.getItem();
           LivingEntity entity = event.getEntity();

           // Check if the item being eaten is our specific food item.
           if (itemStack.getItem() == ModItems.GASBASS.get()) { //Replace YOUR_FOOD_ITEM with your item name

               // Create and emit smoke particles.
               emitSmokeParticles(entity);
           }
       }

       @SubscribeEvent
       public static void onLivingEntityUseItem(LivingEntityUseItemEvent.Finish event) {
           ItemStack itemStack = event.getItem();
           LivingEntity entity = event.getEntity();
            
           // Check if the item being eaten is our specific food item.
            if (itemStack.getItem() == ModItems.GASBASS.get()) { //Replace YOUR_FOOD_ITEM with your item name
            
              // Create and emit smoke particles.
             emitSmokeParticles(entity);
           }
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
                      ParticleTypes.LARGE_SMOKE,
                      x + offsetX,
                      y + offsetY,
                      z + offsetZ,
                      0.0, 0.02, 0.0 // Slight upward movement
                  );
                }
         }
    }
