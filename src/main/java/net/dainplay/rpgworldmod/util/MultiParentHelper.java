package net.dainplay.rpgworldmod.util;

import net.minecraft.advancements.Advancement;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;

public final class MultiParentHelper {
    private MultiParentHelper() {}

    public static final int DEFAULT_MULTI_PARENT_DEPTH = 2;

    /** Заполняется в ServerAdvancementManager.apply (сервер) и живёт до следующего apply. */
    public static final Map<ResourceLocation, List<ResourceLocation>> SERVER_PARENTS = new ConcurrentHashMap<>();
    public static final Map<ResourceLocation, Integer> SHOW_DEPTH_IDS = new ConcurrentHashMap<>();
    public static final Map<ResourceLocation, Integer> DISPLAY_ORDER_IDS = new ConcurrentHashMap<>();
    public static final Map<ResourceLocation, Boolean> GREYSCALE_IDS = new ConcurrentHashMap<>();

    /** Строители (weak, чтобы не течь). */
    public static final Map<Advancement.Builder, List<ResourceLocation>> EXTRA_PARENT_IDS =
            Collections.synchronizedMap(new WeakHashMap<>());
    public static final Map<Advancement.Builder, Integer> SHOW_DEPTH_BUILDERS =
            Collections.synchronizedMap(new WeakHashMap<>());
    public static final Map<Advancement.Builder, Integer> DISPLAY_ORDER_BUILDERS =
            Collections.synchronizedMap(new WeakHashMap<>());
    public static final Map<Advancement.Builder, Boolean> GREYSCALE_BUILDERS =
            Collections.synchronizedMap(new WeakHashMap<>());
    public static final Map<Advancement.Builder, Boolean> RESOLVED =
            Collections.synchronizedMap(new WeakHashMap<>());

    /** По ID — переживает любые пересоздания объектов. */
    public static final Map<ResourceLocation, List<ResourceLocation>> EXTRA_PARENT_IDS_BY_ID = new ConcurrentHashMap<>();
    public static final Map<ResourceLocation, Integer> SHOW_DEPTH_BY_ID = new ConcurrentHashMap<>();
    public static final Map<ResourceLocation, Integer> DISPLAY_ORDER_BY_ID = new ConcurrentHashMap<>();
    public static final Map<ResourceLocation, Boolean> GREYSCALE_BY_ID = new ConcurrentHashMap<>();

    /** По объекту — быстрый кеш для рендера. */
    public static final Map<Advancement, List<Advancement>> PARENTS =
            Collections.synchronizedMap(new IdentityHashMap<>());

    public static List<Advancement> getParents(Advancement adv) {
        List<Advancement> list = PARENTS.get(adv);
        return list != null ? list : Collections.emptyList();
    }

    public static List<ResourceLocation> getExtraParents(Advancement.Builder builder) {
        List<ResourceLocation> list = EXTRA_PARENT_IDS.get(builder);
        return list != null ? list : Collections.emptyList();
    }

    public static void addExtraParent(Advancement.Builder builder, ResourceLocation id) {
        EXTRA_PARENT_IDS.computeIfAbsent(builder, b -> new ArrayList<>()).add(id);
    }

    /** Приоритет — по ID (переживает любые смены объектов). */
    public static int getShowDepth(Advancement adv) {
        Integer d = SHOW_DEPTH_BY_ID.get(adv.getId());
        return d != null ? d : 0;
    }

    public static int getShowDepth(Advancement.Builder builder) {
        Integer d = SHOW_DEPTH_BUILDERS.get(builder);
        return d != null ? d : 0;
    }

    /** null/0 → ванильная VISIBILITY_DEPTH = 2. */
    public static int getEffectiveShowDepth(Advancement adv) {
        Integer d = SHOW_DEPTH_BY_ID.get(adv.getId());
        return d != null && d > 0 ? d : 2;
    }

    /** null → display_order не задан. */
    public static Integer getExplicitDisplayOrder(Advancement adv) {
        return DISPLAY_ORDER_BY_ID.get(adv.getId());
    }

    public static Integer getExplicitDisplayOrder(Advancement.Builder builder) {
        return DISPLAY_ORDER_BUILDERS.get(builder);
    }

    /** true → рисуем серые линии для multi-parent достижения. */
    public static boolean isGreyscale(Advancement adv) {
        return Boolean.TRUE.equals(GREYSCALE_BY_ID.get(adv.getId()));
    }

    public static boolean isGreyscale(Advancement.Builder builder) {
        return Boolean.TRUE.equals(GREYSCALE_BUILDERS.get(builder));
    }

    public static Boolean getGreyscale(Advancement.Builder builder) {
        return GREYSCALE_BUILDERS.get(builder);
    }

    public static void clearRuntime() {
        RESOLVED.clear();
        PARENTS.clear();
        CHANNEL_XS.clear();
        DETOURS.clear();
    }

    /** Полная очистка (при выходе из мира, если нужно). */
    public static void clearAll() {
        SERVER_PARENTS.clear();
        SHOW_DEPTH_IDS.clear();
        DISPLAY_ORDER_IDS.clear();
        GREYSCALE_IDS.clear();
        EXTRA_PARENT_IDS_BY_ID.clear();
        SHOW_DEPTH_BY_ID.clear();
        DISPLAY_ORDER_BY_ID.clear();
        GREYSCALE_BY_ID.clear();
        CHANNEL_XS.clear();
        clearRuntime();
    }

    public static List<Advancement> getAllParents(Advancement adv) {
        List<Advancement> result = new ArrayList<>();
        if (adv.getParent() != null) result.add(adv.getParent());
        for (Advancement p : getParents(adv)) {
            if (!result.contains(p)) result.add(p);
        }
        return result;
    }

    // Клиентские миксины используют для дедупа при итерации всех виджетов.
    public static final ThreadLocal<java.util.Set<Object>> DRAWN =
            ThreadLocal.withInitial(java.util.HashSet::new);
    public static final ThreadLocal<java.util.Set<Object>> CONN =
            ThreadLocal.withInitial(java.util.HashSet::new);

    public static final Map<Advancement, int[]> CHANNEL_XS = new ConcurrentHashMap<>();
    public static final Map<Advancement, Map<Advancement, Integer>> DETOURS = new ConcurrentHashMap<>();
}