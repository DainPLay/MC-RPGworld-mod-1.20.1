package net.dainplay.rpgworldmod.item.custom;

import com.mojang.serialization.Dynamic;
import net.dainplay.rpgworldmod.RPGworldMod;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Fox;
import net.minecraft.world.entity.monster.Endermite;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.portal.PortalInfo;
import net.minecraft.world.phys.Vec3;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.util.ITeleporter;

import java.util.Objects;
import java.util.function.Function;

public class GasbassItem extends Item {
   public GasbassItem(Item.Properties pProperties) {
      super(pProperties);
   }

   /**
    * Called when the player finishes using this Item (E.g. finishes eating.). Not called when the player stops using
    * the Item before the action is complete.
    */
   public ItemStack finishUsingItem(ItemStack pStack, Level pLevel, LivingEntity entity) {
       if (!pLevel.isClientSide()) {
           double teleportPosX;
           double teleportPosY;
           double teleportPosZ;
           ResourceKey<Level> teleportDimension;
           if (pStack.hasTag()) {
               teleportPosX = pStack.getTag().getDouble("rpgworldmod.return_pos_x");
               teleportPosY = pStack.getTag().getDouble("rpgworldmod.return_pos_y");
               teleportPosZ = pStack.getTag().getDouble("rpgworldmod.return_pos_z");
               teleportDimension = pStack.getTag() != null ? DimensionType.parseLegacy(new Dynamic<>(NbtOps.INSTANCE, pStack.getTag().get("rpgworldmod.return_pos_dimension"))).resultOrPartial(RPGworldMod.LOGGER::error).orElse(Level.OVERWORLD) : Level.OVERWORLD;
           } else {
                   teleportPosX = entity.getX();
                   teleportPosY = entity.getY();
                   teleportPosZ = entity.getZ();
                   teleportDimension = entity.getCommandSenderWorld().dimension();
           }

           ITeleporter teleporter = new ITeleporter() {
               @Override
               public PortalInfo getPortalInfo(Entity entity, ServerLevel destWorld, Function<ServerLevel, PortalInfo> defaultPortalInfo) {
                   return new PortalInfo(new Vec3(teleportPosX, teleportPosY, teleportPosZ), Vec3.ZERO, entity.getYRot(), entity.getXRot());
               }
           };

           if (entity instanceof ServerPlayer serverplayer) {
               if (serverplayer.connection.isAcceptingMessages() && serverplayer.level() == pLevel && !serverplayer.isSleeping()) {

                   if (serverplayer.isPassenger()) {
                       serverplayer.dismountTo(serverplayer.getX(), serverplayer.getY(), serverplayer.getZ());
                   }
                   if(entity.getCommandSenderWorld().dimension() != teleportDimension) serverplayer.changeDimension(pLevel.getServer().getLevel(teleportDimension), teleporter);
                   serverplayer.teleportTo(teleportPosX, teleportPosY, teleportPosZ);
                   serverplayer.resetFallDistance();
               }
           } else if (entity != null) {
               if (entity.canChangeDimensions()) {
                   if(entity.getCommandSenderWorld().dimension() != teleportDimension) entity.changeDimension(pLevel.getServer().getLevel(teleportDimension), teleporter);
                   entity.teleportTo(teleportPosX, teleportPosY, teleportPosZ);
                   entity.resetFallDistance();
               }
           }
       }

       ItemStack itemstack = super.finishUsingItem(pStack, pLevel, entity);
       emitSmokeParticles(entity);
           return itemstack;
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
