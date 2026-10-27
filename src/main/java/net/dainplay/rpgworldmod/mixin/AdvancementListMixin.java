package net.dainplay.rpgworldmod.mixin;

import com.mojang.logging.LogUtils;
import net.dainplay.rpgworldmod.util.MultiParentHelper;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementList;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;
import java.util.*;

@Mixin(AdvancementList.class)
public abstract class AdvancementListMixin {
    @Shadow @Final private static Logger LOGGER;
    @Shadow @Final private Map<ResourceLocation, Advancement> advancements;
    @Shadow @Final private Set<Advancement> roots;
    @Shadow @Final private Set<Advancement> tasks;
    @Shadow @Nullable private AdvancementList.Listener listener;

    @Inject(method = "add", at = @At("HEAD"), cancellable = true)
    private void multiparents$add(Map<ResourceLocation, Advancement.Builder> pAdvancements, CallbackInfo ci) {
        // ==== 1. Перенос кастомных метаданных в Builder ====
        for (Map.Entry<ResourceLocation, Advancement.Builder> entry : pAdvancements.entrySet()) {
            ResourceLocation id = entry.getKey();
            Advancement.Builder builder = entry.getValue();

            // Экстра-родители: на одиночке SERVER_PARENTS авторитетен, мержим
            List<ResourceLocation> fromServer = MultiParentHelper.SERVER_PARENTS.get(id);
            if (fromServer != null && !fromServer.isEmpty()) {
                if (builder.parentId == null) builder.parentId = fromServer.get(0);
                List<ResourceLocation> current = MultiParentHelper.getExtraParents(builder);
                for (int i = 1; i < fromServer.size(); i++) {
                    ResourceLocation extraId = fromServer.get(i);
                    if (!current.contains(extraId)) {
                        MultiParentHelper.addExtraParent(builder, extraId);
                    }
                }
            }

            Integer depth = MultiParentHelper.SHOW_DEPTH_IDS.get(id);
            if (depth != null && !MultiParentHelper.SHOW_DEPTH_BUILDERS.containsKey(builder)) {
                MultiParentHelper.SHOW_DEPTH_BUILDERS.put(builder, depth);
            }

            Integer order = MultiParentHelper.DISPLAY_ORDER_IDS.get(id);
            if (order != null && !MultiParentHelper.DISPLAY_ORDER_BUILDERS.containsKey(builder)) {
                MultiParentHelper.DISPLAY_ORDER_BUILDERS.put(builder, order);
            }

            // ← ключевой фикс для greyscale: без этого сервер всегда писал false
            Boolean greyscale = MultiParentHelper.GREYSCALE_IDS.get(id);
            if (greyscale != null && !MultiParentHelper.GREYSCALE_BUILDERS.containsKey(builder)) {
                MultiParentHelper.GREYSCALE_BUILDERS.put(builder, greyscale);
            }
        }

        // ==== 2. Основной цикл сборки ====
        Map<ResourceLocation, Advancement.Builder> map = new HashMap<>(pAdvancements);
        while (!map.isEmpty()) {
            boolean flag = false;
            Iterator<Map.Entry<ResourceLocation, Advancement.Builder>> iterator = map.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<ResourceLocation, Advancement.Builder> entry = iterator.next();
                ResourceLocation id = entry.getKey();
                Advancement.Builder builder = entry.getValue();

                if (!builder.canBuild(this.advancements::get)) continue;

                Advancement adv = builder.build(id);
                this.advancements.put(id, adv);
                flag = true;
                iterator.remove();

                List<Advancement> parents = new ArrayList<>();
                Advancement primaryParent = adv.getParent();
                if (primaryParent != null) parents.add(primaryParent);
                for (ResourceLocation extraId : MultiParentHelper.getExtraParents(builder)) {
                    Advancement extra = this.advancements.get(extraId);
                    if (extra != null && !parents.contains(extra)) parents.add(extra);
                }
                MultiParentHelper.PARENTS.put(adv, parents);

                List<ResourceLocation> extraIds = new ArrayList<>();
                for (Advancement p : parents) {
                    if (p != primaryParent) extraIds.add(p.getId());
                }
                // ВАЖНО: кладём ВСЕ extra-ids из билдера, а не только те, что уже нашли
                // Advancement по id — иначе потеряем late-arrivals
                List<ResourceLocation> builderExtras = MultiParentHelper.getExtraParents(builder);
                if (!builderExtras.isEmpty()) {
                    MultiParentHelper.EXTRA_PARENT_IDS_BY_ID.put(id, new ArrayList<>(builderExtras));
                } else if (!extraIds.isEmpty()) {
                    MultiParentHelper.EXTRA_PARENT_IDS_BY_ID.put(id, extraIds);
                }

                Integer builderDepth = MultiParentHelper.SHOW_DEPTH_BUILDERS.get(builder);
                if (builderDepth != null) MultiParentHelper.SHOW_DEPTH_BY_ID.put(id, builderDepth);

                Integer builderOrder = MultiParentHelper.DISPLAY_ORDER_BUILDERS.get(builder);
                if (builderOrder != null) MultiParentHelper.DISPLAY_ORDER_BY_ID.put(id, builderOrder);

                Boolean builderGreyscale = MultiParentHelper.GREYSCALE_BUILDERS.get(builder);
                if (builderGreyscale != null) MultiParentHelper.GREYSCALE_BY_ID.put(id, builderGreyscale);

                if (parents.isEmpty()) {
                    this.roots.add(adv);
                    if (this.listener != null) this.listener.onAddAdvancementRoot(adv);
                } else {
                    this.tasks.add(adv);
                    if (this.listener != null) this.listener.onAddAdvancementTask(adv);
                }
            }

            if (!flag) {
                for (Map.Entry<ResourceLocation, Advancement.Builder> entry : map.entrySet()) {
                    LOGGER.error("Couldn't load advancement {}: {}", entry.getKey(), entry.getValue());
                }
                break;
            }
        }

        // ==== 3. Пост-проход: долинковываем экстра-родителей, пришедших позже ====
        // Если ребёнок был собран раньше своего extra-родителя, на момент сборки
        // Advancement для extra ещё не было в this.advancements. Здесь чиним это.
        for (Advancement adv : this.advancements.values()) {
            List<ResourceLocation> extraIds = MultiParentHelper.EXTRA_PARENT_IDS_BY_ID.get(adv.getId());
            if (extraIds == null || extraIds.isEmpty()) continue;

            List<Advancement> current = MultiParentHelper.PARENTS.get(adv);
            if (current == null) {
                current = new ArrayList<>();
                if (adv.getParent() != null) current.add(adv.getParent());
                MultiParentHelper.PARENTS.put(adv, current);
            }

            for (ResourceLocation extraId : extraIds) {
                Advancement extra = this.advancements.get(extraId);
                if (extra != null && !current.contains(extra)) {
                    current.add(extra);
                }
            }
        }

        LOGGER.info("Loaded {} advancements", this.advancements.size());
        ci.cancel();
    }

    @Inject(method = "remove(Lnet/minecraft/advancements/Advancement;)V", at = @At("HEAD"))
    private void multiparents$removeOne(Advancement adv, CallbackInfo ci) {
        MultiParentHelper.PARENTS.remove(adv);
        MultiParentHelper.SHOW_DEPTH_BY_ID.remove(adv.getId());
        MultiParentHelper.EXTRA_PARENT_IDS_BY_ID.remove(adv.getId());
        MultiParentHelper.DISPLAY_ORDER_BY_ID.remove(adv.getId());
        MultiParentHelper.GREYSCALE_BY_ID.remove(adv.getId());
    }

    @Inject(method = "clear", at = @At("HEAD"))
    private void multiparents$clear(CallbackInfo ci) {
        MultiParentHelper.clearRuntime();
    }
}