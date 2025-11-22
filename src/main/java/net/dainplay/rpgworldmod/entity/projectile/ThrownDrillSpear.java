package net.dainplay.rpgworldmod.entity.projectile;

import javax.annotation.Nullable;

import net.dainplay.rpgworldmod.enchantment.ModEnchantments;
import net.dainplay.rpgworldmod.entity.ModEntities;
import net.dainplay.rpgworldmod.item.ModItems;
import net.dainplay.rpgworldmod.sounds.RPGSounds;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.chat.report.ReportEnvironment;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.*;

import java.util.List;

public class ThrownDrillSpear extends AbstractArrow {
   private static final EntityDataAccessor<Byte> ID_LOYALTY = SynchedEntityData.defineId(ThrownDrillSpear.class, EntityDataSerializers.BYTE);
   private static final EntityDataAccessor<Boolean> ID_FOIL = SynchedEntityData.defineId(ThrownDrillSpear.class, EntityDataSerializers.BOOLEAN);
   private static final EntityDataAccessor<Float> ID_POWER = SynchedEntityData.defineId(ThrownDrillSpear.class, EntityDataSerializers.FLOAT);
   private ItemStack drillSpearItem = new ItemStack(ModItems.DRILL_SPEAR.get());
   private boolean dealtDamage;
   public int clientSideReturnTridentTickCount;

   public ThrownDrillSpear(EntityType<? extends ThrownDrillSpear> pEntityType, Level pLevel) {
      super(pEntityType, pLevel);
   }

   public ThrownDrillSpear(Level pLevel, LivingEntity pShooter, ItemStack pStack) {
      super(ModEntities.DRILL_SPEAR_PROJECTILE.get(), pShooter, pLevel);
      this.drillSpearItem = pStack.copy();
      this.entityData.set(ID_LOYALTY, (byte)EnchantmentHelper.getLoyalty(pStack));
      this.entityData.set(ID_FOIL, pStack.hasFoil());
      this.entityData.set(ID_POWER, 1F);
      if (EnchantmentHelper.getEnchantments(pStack).containsKey(Enchantments.BLOCK_EFFICIENCY))
         this.entityData.set(ID_POWER, 1F + 1.5F * (EnchantmentHelper.getEnchantments(pStack).get(Enchantments.BLOCK_EFFICIENCY)));
   }

   protected void defineSynchedData() {
      super.defineSynchedData();
      this.entityData.define(ID_LOYALTY, (byte)0);
      this.entityData.define(ID_FOIL, false);
      this.entityData.define(ID_POWER, (float)0F);
   }

   /**
    * Called to update the entity's position/logic.
    */
   private void checkCollision() {
      HitResult hitresult = ProjectileUtil.getHitResultOnMoveVector(this, this::canHitEntity);
      if (hitresult.getType() == HitResult.Type.MISS || !net.minecraftforge.event.ForgeEventFactory.onProjectileImpact(this, hitresult)) this.onHit(hitresult);
   }

   public void tick() {

      AABB aabb = this.getBoundingBox().expandTowards(this.getDeltaMovement());
      if (this.entityData.get(ID_POWER) > 0F) {
         for(BlockPos pos : BlockPos.betweenClosed(Mth.floor(aabb.minX), Mth.floor(aabb.minY), Mth.floor(aabb.minZ), Mth.floor(aabb.maxX), Mth.floor(aabb.maxY), Mth.floor(aabb.maxZ))) {
            BlockState blockstate = this.level().getBlockState(pos);
            Level level = this.level();
            if (blockstate.getBlock().getExplosionResistance() < 20F && !blockstate.isAir()) {
               if (level.isClientSide())
                  level.addDestroyBlockEffect(pos, blockstate);
               if (level instanceof ServerLevel) {
                  ItemStack silkTouchTool = new ItemStack(Items.DIAMOND_PICKAXE);
                  if (EnchantmentHelper.getEnchantments(drillSpearItem).containsKey(Enchantments.SILK_TOUCH))
                     silkTouchTool.enchant(Enchantments.SILK_TOUCH, 1);
                  if (EnchantmentHelper.getEnchantments(drillSpearItem).containsKey(Enchantments.BLOCK_FORTUNE))
                     silkTouchTool.enchant(Enchantments.BLOCK_FORTUNE, EnchantmentHelper.getEnchantments(drillSpearItem).get(Enchantments.BLOCK_FORTUNE));

                  LootParams.Builder lootparams$builder = (new LootParams.Builder((ServerLevel) level)).withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(pos)).withParameter(LootContextParams.TOOL, silkTouchTool);
                  List<ItemStack> drops = blockstate.getDrops(lootparams$builder);
                  int fortuneLevel = 0;
                  if (EnchantmentHelper.getEnchantments(drillSpearItem).containsKey(Enchantments.BLOCK_FORTUNE))
                     fortuneLevel = EnchantmentHelper.getEnchantments(drillSpearItem).get(Enchantments.BLOCK_FORTUNE);
                  int exp = blockstate.getExpDrop(level, level.random, pos, fortuneLevel, 0);
                  for (ItemStack itemStack : drops) {
                     if(EnchantmentHelper.getEnchantments(drillSpearItem).containsKey(ModEnchantments.COLLECTION.get())
                             && this.getOwner() instanceof Player player) {
                        if (!(player.getInventory().add(itemStack))) {
                              Block.popResource(level, pos, itemStack);
                        }
                        if (player instanceof ServerPlayer && !EnchantmentHelper.getEnchantments(drillSpearItem).containsKey(Enchantments.SILK_TOUCH)) player.giveExperiencePoints(exp);
                     }
                     else {
                           Block.popResource(level, pos, itemStack);
                        if (!EnchantmentHelper.getEnchantments(drillSpearItem).containsKey(Enchantments.SILK_TOUCH)) blockstate.getBlock().popExperience((ServerLevel) level, pos, exp);
                     }
                  }
                  level.destroyBlock(pos, false);
               }
               this.entityData.set(ID_POWER, this.entityData.get(ID_POWER) - blockstate.getBlock().defaultDestroyTime());
               this.playSound(RPGSounds.DRILL.get(), 0.5F, 1.2F / (this.random.nextFloat() * 0.2F + 0.9F));
            }
         }
      }

      if (this.inGroundTime > 4) {
         this.dealtDamage = true;
      }

      Entity entity = this.getOwner();
      int i = this.entityData.get(ID_LOYALTY);
      if (i > 0 && (this.dealtDamage || this.isNoPhysics()) && entity != null) {
         if (!this.isAcceptibleReturnOwner()) {
            if (!this.level().isClientSide && this.pickup == AbstractArrow.Pickup.ALLOWED) {
               this.spawnAtLocation(this.getPickupItem(), 0.1F);
            }

            this.discard();
         } else {
            this.setNoPhysics(true);
            Vec3 vec3 = entity.getEyePosition().subtract(this.position());
            this.setPosRaw(this.getX(), this.getY() + vec3.y * 0.015D * (double)i, this.getZ());
            if (this.level().isClientSide) {
               this.yOld = this.getY();
            }

            double d0 = 0.05D * (double)i;
            this.setDeltaMovement(this.getDeltaMovement().scale(0.95D).add(vec3.normalize().scale(d0)));
            if (this.clientSideReturnTridentTickCount == 0) {
               this.playSound(RPGSounds.DRILL_SPEAR_RETURN.get(), 10.0F, 1.0F);
            }

            ++this.clientSideReturnTridentTickCount;
         }
      }

      super.tick();
   }

   private boolean isAcceptibleReturnOwner() {
      Entity entity = this.getOwner();
      if (entity != null && entity.isAlive()) {
         return !(entity instanceof ServerPlayer) || !entity.isSpectator();
      } else {
         return false;
      }
   }

   protected ItemStack getPickupItem() {
      return this.drillSpearItem.copy();
   }

   public boolean isFoil() {
      return this.entityData.get(ID_FOIL);
   }

   /**
    * Gets the EntityHitResult representing the entity hit
    */
   @Nullable
   protected EntityHitResult findHitEntity(Vec3 pStartVec, Vec3 pEndVec) {
      return this.dealtDamage ? null : super.findHitEntity(pStartVec, pEndVec);
   }

   /**
    * Called when the arrow hits an entity
    */
   protected void onHitEntity(EntityHitResult pResult) {
      Entity entity = pResult.getEntity();
      float f = 3.0F;

      Entity entity1 = this.getOwner();
      DamageSource damagesource = this.damageSources().trident(this, (Entity)(entity1 == null ? this : entity1));
      this.dealtDamage = true;
      SoundEvent soundevent = RPGSounds.DRILL_SPEAR_HIT.get();
      if (entity.hurt(damagesource, f)) {
         if (entity.getType() == EntityType.ENDERMAN) {
            return;
         }

         if (entity instanceof LivingEntity) {
            LivingEntity livingentity1 = (LivingEntity)entity;
            if (entity1 instanceof LivingEntity) {
               EnchantmentHelper.doPostHurtEffects(livingentity1, entity1);
               EnchantmentHelper.doPostDamageEffects((LivingEntity)entity1, livingentity1);
            }

            this.doPostHurtEffects(livingentity1);
         }
      }

      this.setDeltaMovement(this.getDeltaMovement().multiply(-0.01D, -0.1D, -0.01D));
      float f1 = 1.0F;

      this.playSound(soundevent, f1, 1.0F);
   }

   protected boolean tryPickup(Player pPlayer) {
      return super.tryPickup(pPlayer) || this.isNoPhysics() && this.ownedBy(pPlayer) && pPlayer.getInventory().add(this.getPickupItem());
   }

   /**
    * The sound made when an entity is hit by this projectile
    */
   protected SoundEvent getDefaultHitGroundSoundEvent() {
      return RPGSounds.DRILL_SPEAR_HIT_GROUND.get();
   }

   /**
    * Called by a player entity when they collide with an entity
    */
   public void playerTouch(Player pEntity) {
      if (this.ownedBy(pEntity) || this.getOwner() == null) {
         super.playerTouch(pEntity);
      }

   }

   /**
    * (abstract) Protected helper method to read subclass entity data from NBT.
    */
   public void readAdditionalSaveData(CompoundTag pCompound) {
      super.readAdditionalSaveData(pCompound);
      if (pCompound.contains("Drill Spear", 10)) {
         this.drillSpearItem = ItemStack.of(pCompound.getCompound("Drill Spear"));
      }

      this.dealtDamage = pCompound.getBoolean("DealtDamage");
      this.entityData.set(ID_LOYALTY, (byte)EnchantmentHelper.getLoyalty(this.drillSpearItem));
   }

   public void addAdditionalSaveData(CompoundTag pCompound) {
      super.addAdditionalSaveData(pCompound);
      pCompound.put("Drill Spear", this.drillSpearItem.save(new CompoundTag()));
      pCompound.putBoolean("DealtDamage", this.dealtDamage);
   }

   public void tickDespawn() {
      int i = this.entityData.get(ID_LOYALTY);
      if (this.pickup != AbstractArrow.Pickup.ALLOWED || i <= 0) {
         super.tickDespawn();
      }

   }

   protected float getWaterInertia() {
      return 0.99F;
   }

   public boolean shouldRender(double pX, double pY, double pZ) {
      return true;
   }
}