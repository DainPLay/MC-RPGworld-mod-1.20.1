package net.dainplay.rpgworldmod.mixin;

import net.dainplay.rpgworldmod.util.MultiParentHelper;
import net.minecraft.advancements.Advancement;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(Advancement.class)
public abstract class AdvancementMixin {

    @Inject(
            method = "getRoot(Lnet/minecraft/advancements/Advancement;)Lnet/minecraft/advancements/Advancement;",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void multiparents$getRoot(Advancement adv, CallbackInfoReturnable<Advancement> cir) {
        Advancement current = adv;
        int safety = 0;
        while (current != null) {
            if (++safety > 1024) break;
            Advancement parent = current.getParent();
            if (parent == null) break;
            current = parent;
        }
        cir.setReturnValue(current);
    }

    @Inject(method = "deconstruct", at = @At("RETURN"))
    private void multiparents$deconstruct(CallbackInfoReturnable<Advancement.Builder> cir) {
        Advancement self = (Advancement)(Object)this;
        Advancement.Builder newBuilder = cir.getReturnValue();
        if (newBuilder == null) return;

        List<ResourceLocation> extraIds = MultiParentHelper.EXTRA_PARENT_IDS_BY_ID.get(self.getId());
        if (extraIds != null) {
            for (ResourceLocation id : extraIds) {
                MultiParentHelper.addExtraParent(newBuilder, id);
            }
        }

        Integer depth = MultiParentHelper.SHOW_DEPTH_BY_ID.get(self.getId());
        if (depth != null) {
            MultiParentHelper.SHOW_DEPTH_BUILDERS.put(newBuilder, depth);
        }

        Integer order = MultiParentHelper.DISPLAY_ORDER_BY_ID.get(self.getId());
        if (order != null) {
            MultiParentHelper.DISPLAY_ORDER_BUILDERS.put(newBuilder, order);
        }

        Boolean greyscale = MultiParentHelper.GREYSCALE_BY_ID.get(self.getId());
        if (greyscale != null) {
            MultiParentHelper.GREYSCALE_BUILDERS.put(newBuilder, greyscale);
        }
    }
}