package net.dainplay.rpgworldmod.mixin;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.dainplay.rpgworldmod.util.MultiParentHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.ServerAdvancementManager;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Mixin(ServerAdvancementManager.class)
public abstract class ServerAdvancementManagerMixin {

    @Inject(
            method = "apply(Ljava/util/Map;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)V",
            at = @At("HEAD")
    )
    private void multiparents$captureParents(Map<ResourceLocation, JsonElement> pObject,
                                             ResourceManager pResourceManager,
                                             ProfilerFiller pProfiler,
                                             CallbackInfo ci) {
        MultiParentHelper.SERVER_PARENTS.clear();
        MultiParentHelper.SHOW_DEPTH_IDS.clear();
        MultiParentHelper.DISPLAY_ORDER_IDS.clear();
        MultiParentHelper.GREYSCALE_IDS.clear();

        for (Map.Entry<ResourceLocation, JsonElement> entry : pObject.entrySet()) {
            JsonElement el = entry.getValue();
            if (el == null || !el.isJsonObject()) continue;
            JsonObject json = el.getAsJsonObject();
            ResourceLocation id = entry.getKey();

            if (json.has("parents")) {
                JsonArray arr = json.getAsJsonArray("parents");
                List<ResourceLocation> list = new ArrayList<>(arr.size());
                for (JsonElement e : arr) {
                    list.add(new ResourceLocation(e.getAsString()));
                }
                if (!list.isEmpty()) {
                    MultiParentHelper.SERVER_PARENTS.put(id, list);
                }
            }

            if (json.has("parents") || json.has("show_depth")) {
                int depth = json.has("show_depth")
                        ? json.get("show_depth").getAsInt()
                        : MultiParentHelper.DEFAULT_MULTI_PARENT_DEPTH;
                MultiParentHelper.SHOW_DEPTH_IDS.put(id, depth);
            }

            if (json.has("display_order")) {
                MultiParentHelper.DISPLAY_ORDER_IDS.put(id, json.get("display_order").getAsInt());
            }

            if (json.has("greyscale") && json.get("greyscale").getAsBoolean()) {
                MultiParentHelper.GREYSCALE_IDS.put(id, true);
            }
        }
    }
}