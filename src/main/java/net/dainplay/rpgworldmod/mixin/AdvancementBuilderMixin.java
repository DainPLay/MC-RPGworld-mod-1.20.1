package net.dainplay.rpgworldmod.mixin;

import net.dainplay.rpgworldmod.util.MultiParentHelper;
import net.minecraft.advancements.*;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Function;

@Mixin(Advancement.Builder.class)
public abstract class AdvancementBuilderMixin {
    @Shadow @Nullable private ResourceLocation parentId;
    @Shadow @Nullable private Advancement parent;
    @Shadow private DisplayInfo display;
    @Shadow private Map<String, Criterion> criteria;
    @Shadow @Nullable private String[][] requirements;
    @Shadow private RequirementsStrategy requirementsStrategy;
    @Shadow @Final private boolean sendsTelemetryEvent;

    // ... (canBuild остаётся без изменений) ...

    @Inject(method = "serializeToNetwork", at = @At("HEAD"), cancellable = true)
    private void multiparents$serializeToNetwork(FriendlyByteBuf buf, CallbackInfo ci) {
        Advancement.Builder self = (Advancement.Builder)(Object)this;
        List<ResourceLocation> extras = MultiParentHelper.getExtraParents(self);
        if (this.requirements == null) {
            this.requirements = this.requirementsStrategy.createRequirements(this.criteria.keySet());
        }
        buf.writeNullable(this.parentId, FriendlyByteBuf::writeResourceLocation);
        buf.writeVarInt(extras.size());
        for (ResourceLocation id : extras) buf.writeResourceLocation(id);
        buf.writeVarInt(MultiParentHelper.getShowDepth(self));

        Integer displayOrder = MultiParentHelper.getExplicitDisplayOrder(self);
        buf.writeBoolean(displayOrder != null);
        if (displayOrder != null) buf.writeVarInt(displayOrder);

        buf.writeBoolean(MultiParentHelper.isGreyscale(self));
        buf.writeNullable(this.display, (b, d) -> d.serializeToNetwork(b));
        Criterion.serializeToNetwork(this.criteria, buf);
        buf.writeVarInt(this.requirements.length);
        for (String[] arr : this.requirements) {
            buf.writeVarInt(arr.length);
            for (String s : arr) buf.writeUtf(s);
        }
        buf.writeBoolean(this.sendsTelemetryEvent);
        ci.cancel();
    }

    @Inject(method = "fromNetwork", at = @At("HEAD"), cancellable = true)
    private static void multiparents$fromNetwork(FriendlyByteBuf buf,
                                                 CallbackInfoReturnable<Advancement.Builder> cir) {
        ResourceLocation parentId = buf.readNullable(FriendlyByteBuf::readResourceLocation);
        int extraCount = buf.readVarInt();
        List<ResourceLocation> extras = new ArrayList<>(extraCount);
        for (int i = 0; i < extraCount; i++) extras.add(buf.readResourceLocation());
        int depth = buf.readVarInt();

        Integer displayOrder = buf.readBoolean() ? buf.readVarInt() : null;
        boolean greyscale = buf.readBoolean();

        DisplayInfo display = buf.readNullable(DisplayInfo::fromNetwork);
        Map<String, Criterion> criteria = Criterion.criteriaFromNetwork(buf);
        String[][] reqs = new String[buf.readVarInt()][];
        for (int i = 0; i < reqs.length; i++) {
            reqs[i] = new String[buf.readVarInt()];
            for (int j = 0; j < reqs[i].length; j++) reqs[i][j] = buf.readUtf();
        }
        boolean telemetry = buf.readBoolean();

        Advancement.Builder builder = new Advancement.Builder(parentId, display, AdvancementRewards.EMPTY,
                criteria, reqs, telemetry);
        for (ResourceLocation id : extras) MultiParentHelper.addExtraParent(builder, id);
        MultiParentHelper.SHOW_DEPTH_BUILDERS.put(builder, depth);
        if (displayOrder != null) MultiParentHelper.DISPLAY_ORDER_BUILDERS.put(builder, displayOrder);
        if (greyscale) MultiParentHelper.GREYSCALE_BUILDERS.put(builder, true);
        cir.setReturnValue(builder);
    }
}