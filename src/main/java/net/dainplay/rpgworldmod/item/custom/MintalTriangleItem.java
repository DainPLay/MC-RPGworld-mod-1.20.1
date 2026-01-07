package net.dainplay.rpgworldmod.item.custom;

import net.dainplay.rpgworldmod.damage.ModDamageTypes;
import net.dainplay.rpgworldmod.data.tags.ModAdvancements;
import net.dainplay.rpgworldmod.enchantment.ModEnchantments;
import net.dainplay.rpgworldmod.entity.custom.Mintobat;
import net.dainplay.rpgworldmod.item.ModItems;
import net.dainplay.rpgworldmod.sounds.RPGSounds;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ambient.Bat;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.vibrations.VibrationSystem;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.data.ForgeBlockTagsProvider;
import net.minecraftforge.common.extensions.IForgeBlockState;
import net.minecraftforge.common.extensions.IForgeTagAppender;

import javax.annotation.Nullable;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static net.minecraft.world.level.block.Block.dropResources;

public class MintalTriangleItem extends Item implements Vanishable, RPGtooltip {
    protected final RandomSource random = RandomSource.create();
    public MintalTriangleItem(Properties pProperties) {
        super(pProperties);
    }
   /**
     * Return the enchantability factor of the item, most of the time is based on material.
     */
    public int getEnchantmentValue() {
        return 14;
    }

    public boolean isValidRepairItem(ItemStack pToRepair, ItemStack pRepair) {
        return pRepair.is(ModItems.MINTAL_INGOT.get());
    }
    public int cooldown(ItemStack itemstack) {
        int tempo = getEnchantmentLevel(itemstack, ModEnchantments.TEMPO.get());
        return 100 - tempo*20 - (tempo != 0 ? (tempo-1)*10 : 0);
    }
    public double damage(ItemStack itemstack) {
        return 4D + getEnchantmentLevel(itemstack, ModEnchantments.PITCH.get())*1D;
    }
    public double range(ItemStack itemstack) {
        return 16D+getEnchantmentLevel(itemstack, ModEnchantments.CRESCENDO.get())*8D;
    }
    public float volume(ItemStack itemstack) {
        return 1F+getEnchantmentLevel(itemstack, ModEnchantments.CRESCENDO.get())*0.5F;
    }
    public float pitch(ItemStack itemstack) {
        return 0F+getEnchantmentLevel(itemstack, ModEnchantments.PITCH.get())*0.4F;
    }

    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pUsedHand) {
        ItemStack itemstack = pPlayer.getItemInHand(pUsedHand);
        AtomicInteger entities_hit = new AtomicInteger();
        if(!pLevel.isClientSide) {
            entities_hit.set(0);
            pPlayer.getCooldowns().addCooldown(this, cooldown(itemstack));
            pPlayer.awardStat(Stats.ITEM_USED.get(this));
            itemstack.hurtAndBreak(1, pPlayer, (p_43296_) -> {
                p_43296_.broadcastBreakEvent(pUsedHand);
            });
            setVibes(itemstack, 23);
            ModAdvancements.MINTAL_TRIANGLE_USED.trigger((ServerPlayer) pPlayer);
        }
        pLevel.playSound(pPlayer, pPlayer.blockPosition(), RPGSounds.TRIANGLE_DING.get(), SoundSource.PLAYERS, volume(itemstack), (random.nextFloat() - random.nextFloat()) * 0.05F + 1.0F + pitch(itemstack));

        AtomicBoolean skip = new AtomicBoolean(false);

        BlockPos entityPos = new BlockPos(pPlayer.getBlockX(), pPlayer.getBlockY(), pPlayer.getBlockZ());

        if(!pLevel.isClientSide) ((ServerLevel) pLevel).sendParticles(ParticleTypes.SONIC_BOOM, pPlayer.getX(), pPlayer.getY(0.5D), pPlayer.getZ(), 1, 0.0D, 0.0D, 0.0D, 0.0D);
        pLevel.getEntities(pPlayer, pPlayer.getBoundingBox().inflate(range(itemstack)),
                            target ->
                                    target instanceof LivingEntity && !(target instanceof Mintobat) && !(target instanceof TamableAnimal && ((TamableAnimal)target).getOwner()==pPlayer))
                    .forEach(target -> {
                        if(getEnchantmentLevel(itemstack, ModEnchantments.STEREO.get()) > 0) {
                            skip.set(true);
                            if (target instanceof Player && pPlayer.isCrouching()) skip.set(false);
                            if (target instanceof Enemy) skip.set(false);
                            if (target instanceof NeutralMob neutral && neutral.getTarget() != pPlayer) skip.set(true);
                        }
                        if(!skip.get()) {
                            Vec3 targetPos = new Vec3(target.getX(), target.getEyeY(), target.getZ());
                            if (!VibrationSystem.Listener.isOccluded(pLevel, entityPos.getCenter(), targetPos)) {
                                double scaledDamage = damage(itemstack) * (1 - Math.min(1, pPlayer.distanceTo(target) / range(itemstack)));
                                if (Mintobat.isEyeInAnyFluid(pPlayer) && !Mintobat.isEyeInAnyFluid(target))
                                    scaledDamage /= 4;
                                if (Mintobat.isEyeInAnyFluid(target) && !Mintobat.isEyeInAnyFluid(pPlayer))
                                    scaledDamage /= 4;
                                (target).hurt(ModDamageTypes.getEntityDamageSource(pLevel, ModDamageTypes.SCREAM, pPlayer), (float) scaledDamage);
                                entities_hit.getAndIncrement();
                                if (entities_hit.get() >= 16 && !pLevel.isClientSide)
                                    ModAdvancements.MINTAL_TRIANGLE_HIT_16_TARGETS.trigger((ServerPlayer) pPlayer);
                            }
                        }
                    });

        if (getEnchantmentLevel(itemstack, ModEnchantments.PITCH.get()) == 3) {
            double radius = range(itemstack)/2;
            double radiusSquared = radius * radius;
            BlockPos playerPos = pPlayer.blockPosition();

            for (int x = (int)-radius; x <= radius; ++x) {
                for (int y = (int)-radius; y <= radius; ++y) {
                    for (int z = (int)-radius; z <= radius; ++z) {
                        BlockPos pos = playerPos.offset(x, y, z);
                        if (playerPos.distSqr(pos) <= radiusSquared) {
                            BlockState state = pLevel.getBlockState(pos);
                            if (state.is(Tags.Blocks.GLASS) || state.is(Tags.Blocks.GLASS_PANES) && !VibrationSystem.Listener.isOccluded(pLevel, entityPos.getCenter(), pos.getCenter())) {
                                pLevel.destroyBlock(pos, true);
                            }
                        }
                    }
                }
            }
        }

        return InteractionResultHolder.fail(itemstack);
    }



    public static int getVibes(ItemStack triangle) {
        CompoundTag compoundtag = triangle.getTag();
        return compoundtag != null ? compoundtag.getInt("vibration") : 0;
    }

    public static void setVibes(ItemStack triangle, int vibes) {
        CompoundTag compoundtag = triangle.getOrCreateTag();
        compoundtag.putInt("vibration", vibes);
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltip, TooltipFlag pFlag) {
        RPGappendHoverText(pStack,pLevel,pTooltip,pFlag);
    }

    @Override
    public MutableComponent getDisplayFeatures(ItemStack item) {
        if(getEnchantmentLevel(item, ModEnchantments.STEREO.get()) > 0) {
            return Component.translatable(this.getDescriptionId() + ".features.stereo",
                    Minecraft.getInstance().options.keyShift.getKey().getDisplayName());
        }
        else {
            return Component.translatable(this.getDescriptionId() + ".features");
        }
    }
}
