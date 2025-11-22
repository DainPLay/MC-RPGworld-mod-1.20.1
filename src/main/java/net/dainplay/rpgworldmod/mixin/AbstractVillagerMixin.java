package net.dainplay.rpgworldmod.mixin;

import net.dainplay.rpgworldmod.effect.ParalysisEffect;
import net.dainplay.rpgworldmod.sounds.ModSounds;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.npc.WanderingTrader;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.trading.MerchantOffers;
import org.apache.http.util.Args;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;

@Mixin(AbstractVillager.class)
public abstract class AbstractVillagerMixin {

    AbstractVillager entity = (AbstractVillager) (Object) this;
    @Inject(method = "addAdditionalSaveData", at = @At(value = "TAIL"))
    private void addAdditionalSaveDataParalysisCheck(CompoundTag pCompound, CallbackInfo ci) {

        if (entity.getAttribute(Attributes.MOVEMENT_SPEED).getModifier(ParalysisEffect.MODIFIER_UUID) != null
        && entity.offers != null && !entity.offers.isEmpty()) {
                pCompound.put("Offers", entity.offers.createTag());
        }
    }
    @Inject(method = "getOffers", at = @At(value = "HEAD"), cancellable = true)
    private void getOffersParalysisCheck(CallbackInfoReturnable<MerchantOffers> cir) {

        if (entity.getAttribute(Attributes.MOVEMENT_SPEED).getModifier(ParalysisEffect.MODIFIER_UUID) != null) {

            cir.setReturnValue(new MerchantOffers());
        }
    }
}
