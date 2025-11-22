package net.dainplay.rpgworldmod.mixin;

import net.dainplay.rpgworldmod.effect.ModEffects;
import net.dainplay.rpgworldmod.item.ModCreativeModeTab;
import net.dainplay.rpgworldmod.potion.ModPotions;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

import static net.minecraft.world.item.alchemy.PotionUtils.getMobEffects;

@Mixin(PotionUtils.class)
public abstract class PotionUtilsMixin {
    @Inject(at = @At("TAIL"), method = "addPotionTooltip(Ljava/util/List;Ljava/util/List;F)V")
    private static void addPotionTooltipRPGCheck(List<MobEffectInstance> pEffects, List<Component> pTooltips, float pDurationFactor, CallbackInfo ci) {
        // Check if the item corresponds to the one you want to modify the tooltip for
        for(MobEffectInstance mobeffectinstance : pEffects) {
            MobEffect mobeffect = mobeffectinstance.getEffect();
            if(mobeffect == ModEffects.MOSSIOSIS.get() || mobeffect == ModEffects.PARALYSIS.get() || mobeffect == ModEffects.FUELING.get()) {
                pTooltips.remove(pTooltips.size() - 1);
                pTooltips.remove(pTooltips.size() - 1);
            }
        }
    }
}
