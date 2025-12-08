package net.dainplay.rpgworldmod.item.custom;

import net.dainplay.rpgworldmod.damage.ModDamageTypes;
import net.dainplay.rpgworldmod.data.tags.ModAdvancements;
import net.dainplay.rpgworldmod.enchantment.ModEnchantments;
import net.dainplay.rpgworldmod.entity.custom.Mintobat;
import net.dainplay.rpgworldmod.item.ModItems;
import net.dainplay.rpgworldmod.sounds.RPGSounds;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.CombatTracker;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.vibrations.VibrationSystem;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class GuitarAxItem extends AxeItem implements RPGtooltip {
    protected final RandomSource random = RandomSource.create();
    public GuitarAxItem(Tier pTier, int pAttackDamageModifier, float pAttackSpeedModifier, Properties pProperties) {
        super(pTier, pAttackDamageModifier, pAttackSpeedModifier, pProperties);
    }
    public boolean isValidRepairItem(ItemStack pToRepair, ItemStack pRepair) {
        return pRepair.is(ModItems.MINTAL_INGOT.get());
    }
    public double range(ItemStack itemstack) {
        return 16D+getEnchantmentLevel(itemstack, ModEnchantments.CRESCENDO.get())*8D;
    }
    public float volume(ItemStack itemstack) {
        return 1F+getEnchantmentLevel(itemstack, ModEnchantments.CRESCENDO.get())*0.5F;
    }
    @Override
    public boolean hurtEnemy(ItemStack pStack, LivingEntity pTarget, LivingEntity pAttacker) {
        pStack.hurtAndBreak(2, pAttacker, (p_41007_) -> {
            p_41007_.broadcastBreakEvent(EquipmentSlot.MAINHAND);
        });
        pTarget.level().playSound(pTarget, pTarget.blockPosition(), RPGSounds.GUITAR_AX_PLAY.get(), SoundSource.PLAYERS, volume(pStack), (random.nextFloat() - random.nextFloat()) * 0.05F + 1.0F);
        Level pLevel = pAttacker.level();
        LivingEntity pPlayer = pAttacker;
        AtomicBoolean skip = new AtomicBoolean(false);
        if(!pLevel.isClientSide) ((ServerLevel) pLevel).sendParticles(ParticleTypes.SONIC_BOOM, pTarget.getX(), pTarget.getY(0.5D), pTarget.getZ(), 1, 0.0D, 0.0D, 0.0D, 0.0D);
        pLevel.getEntities(pPlayer, pPlayer.getBoundingBox().inflate(range(pStack)),
                        target ->
                                target instanceof LivingEntity && !(target instanceof Mintobat) && !(target instanceof TamableAnimal && ((TamableAnimal)target).getOwner()==pPlayer))
                .forEach(target -> {
                    if(getEnchantmentLevel(pStack, ModEnchantments.STEREO.get()) > 0) {
                        skip.set(true);
                    if (target instanceof Player && pTarget instanceof Player) skip.set(false);
                    if (target instanceof Enemy) skip.set(false);
                    if (target instanceof NeutralMob neutral && neutral.getTarget() != pAttacker) skip.set(true);
                    }
                    if(!skip.get()) {
                        skip.set(false);
                        BlockPos entityPos = new BlockPos(pPlayer.getBlockX(), pPlayer.getBlockY(), pPlayer.getBlockZ());
                        Vec3 targetPos = new Vec3(target.getX(), target.getEyeY(), target.getZ());
                        if (!VibrationSystem.Listener.isOccluded(pLevel, entityPos.getCenter(), targetPos)) {
                            double scaledDamage = 4D * (1 - Math.min(1, pPlayer.distanceTo(target) / range(pStack)));
                            if (Mintobat.isEyeInAnyFluid(pPlayer) && !Mintobat.isEyeInAnyFluid(target))
                                scaledDamage /= 4;
                            if (Mintobat.isEyeInAnyFluid(target) && !Mintobat.isEyeInAnyFluid(pPlayer))
                                scaledDamage /= 4;
                            (target).hurt(ModDamageTypes.getEntityDamageSource(pLevel, ModDamageTypes.SCREAM, pPlayer), (float) scaledDamage);
                        }
                    }
                });
            return true;
    }
    @Override
    public boolean mineBlock(ItemStack pStack, Level pLevel, BlockState pState, BlockPos pPos, LivingEntity pEntityLiving) {
        if (!pLevel.isClientSide && pState.getDestroySpeed(pLevel, pPos) != 0.0F) {
            pStack.hurtAndBreak(1, pEntityLiving, (p_40992_) -> {
                p_40992_.broadcastBreakEvent(EquipmentSlot.MAINHAND);
            });
        }
        return true;
    }
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pUsedHand) {
        ItemStack itemstack = pPlayer.getItemInHand(pUsedHand);
        if(pUsedHand.equals(InteractionHand.OFF_HAND)) {
            pLevel.playSound(pPlayer, pPlayer.blockPosition(), RPGSounds.GUITAR_AX_PLAY.get(), SoundSource.PLAYERS, volume(itemstack), (random.nextFloat() - random.nextFloat()) * 0.05F + 1.0F);
            if(!pLevel.isClientSide) ((ServerLevel) pLevel).sendParticles(ParticleTypes.SONIC_BOOM, pPlayer.getX(), pPlayer.getY(0.5D), pPlayer.getZ(), 1, 0.0D, 0.0D, 0.0D, 0.0D);
            pPlayer.getCooldowns().addCooldown(this, 50);
            pPlayer.swing(InteractionHand.MAIN_HAND);
        }
        return InteractionResultHolder.fail(itemstack);
    }
    @Override
    public void inventoryTick(ItemStack pStack, Level pLevel, Entity pEntity, int pSlotId, boolean pIsSelected) {
        if(pEntity.getName().getString().equals("Lynxscorner")) {
            if (!pLevel.isClientSide) {
                for (Player server_player : pLevel.players()) {
                    ModAdvancements.CLYDE_GOT_GUITAR_AX_TRIGGER.trigger((ServerPlayer) server_player);
                }
            }
        }
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltip, TooltipFlag pFlag) {
        RPGappendHoverText(pStack,pLevel,pTooltip,pFlag);
    }

    @Override
    public MutableComponent getDisplayFeatures(ItemStack item) {
        if(getEnchantmentLevel(item, ModEnchantments.STEREO.get()) > 0) {
            return Component.translatable(this.getDescriptionId() + ".features.stereo");
        }
        else {
            return Component.translatable(this.getDescriptionId() + ".features");
        }
    }
}
