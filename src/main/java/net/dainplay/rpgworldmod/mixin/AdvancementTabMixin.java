package net.dainplay.rpgworldmod.mixin;

import net.dainplay.rpgworldmod.util.IAdvancementTabAccess;
import net.dainplay.rpgworldmod.util.IAdvancementWidgetAccess;
import net.dainplay.rpgworldmod.util.MultiParentHelper;
import net.minecraft.advancements.Advancement;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.advancements.AdvancementTab;
import net.minecraft.client.gui.screens.advancements.AdvancementWidget;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Mixin(AdvancementTab.class)
public abstract class AdvancementTabMixin implements IAdvancementTabAccess {
    @Shadow @Final private Map<Advancement, AdvancementWidget> widgets;
    @Shadow private int minY;
    @Shadow private int maxY;
    @Shadow private int minX;
    @Shadow private int maxX;
    @Shadow private boolean centered;

    @Unique private final Map<AdvancementWidget, Integer> mp$origY = new IdentityHashMap<>();
    @Unique private final Map<AdvancementWidget, Integer> mp$origX = new IdentityHashMap<>();
    @Unique private boolean mp$dirty = true;

    @Override
    public void mp_markDirty() {
        this.mp$dirty = true;
    }

    @Unique
    private boolean mp_isVisible(AdvancementWidget w) {
        return ((IAdvancementWidgetAccess)(Object)w).mp_isVisible();
    }

    @Inject(method = "drawContents", at = @At("HEAD"))
    private void mp$ensureAttachedAndRelayout(GuiGraphics g, int pX, int pY, CallbackInfo ci) {
        for (AdvancementWidget w : this.widgets.values()) w.attachToParent();
        if (mp$dirty) {
            mp$dirty = false;
            mp$relayout();
        }
    }

    @Inject(method = "addAdvancement", at = @At("TAIL"))
    private void mp$markDirty(Advancement adv, CallbackInfo ci) {
        mp$dirty = true;
    }

    @Redirect(
            method = "drawContents",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/screens/advancements/AdvancementWidget;drawConnectivity(Lnet/minecraft/client/gui/GuiGraphics;IIZ)V")
    )
    private void mp$drawConnectivityAll(AdvancementWidget rootWidget, GuiGraphics g, int x, int y, boolean shadow) {
        MultiParentHelper.CONN.get().clear();
        for (AdvancementWidget w : this.widgets.values()) {
            w.drawConnectivity(g, x, y, shadow);
        }
    }

    @Redirect(
            method = "drawContents",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/screens/advancements/AdvancementWidget;draw(Lnet/minecraft/client/gui/GuiGraphics;II)V")
    )
    private void mp$drawAll(AdvancementWidget rootWidget, GuiGraphics g, int x, int y) {
        MultiParentHelper.DRAWN.get().clear();
        for (AdvancementWidget w : this.widgets.values()) w.draw(g, x, y);
    }

    @Unique
    private void mp$relayout() {
        if (this.widgets.isEmpty()) return;

        for (AdvancementWidget w : this.widgets.values()) {
            mp$origY.computeIfAbsent(w, AdvancementWidget::getY);
        }

        Map<AdvancementWidget, List<AdvancementWidget>> childrenByParent = new HashMap<>();
        List<AdvancementWidget> roots = new ArrayList<>();
        for (AdvancementWidget w : this.widgets.values()) {
            if (!mp_isVisible(w)) continue; // Игнорируем невидимые
            AdvancementWidget p = ((IAdvancementWidgetAccess)(Object)w).mp_getParent();
            if (p == null || !mp_isVisible(p)) roots.add(w);
            else childrenByParent.computeIfAbsent(p, k -> new ArrayList<>()).add(w);
        }

        Map<AdvancementWidget, Integer> subMinY = new HashMap<>();
        Map<AdvancementWidget, Integer> subMaxY = new HashMap<>();
        for (AdvancementWidget r : roots) {
            mp$computeBounds(r, childrenByParent, subMinY, subMaxY);
        }

        // --- Y-раскладка ---
        Map<AdvancementWidget, Integer> deltaMap = new HashMap<>();
        Set<AdvancementWidget> visited = new HashSet<>();
        Deque<AdvancementWidget> queue = new ArrayDeque<>();
        for (AdvancementWidget r : roots) { deltaMap.put(r, 0); queue.add(r); }

        while (!queue.isEmpty()) {
            AdvancementWidget parent = queue.poll();
            if (!visited.add(parent)) continue;
            int parentDelta = deltaMap.getOrDefault(parent, 0);

            List<AdvancementWidget> children = childrenByParent.getOrDefault(parent, Collections.emptyList());
            if (children.isEmpty()) continue;

            List<AdvancementWidget> sortedByY = new ArrayList<>(children);
            sortedByY.sort(Comparator.comparingInt(mp$origY::get));

            Map<AdvancementWidget, Integer> orderMap = new HashMap<>();
            boolean anyExplicit = false;
            for (int i = 0; i < sortedByY.size(); i++) {
                AdvancementWidget c = sortedByY.get(i);
                Integer explicit = MultiParentHelper.getExplicitDisplayOrder(
                        ((IAdvancementWidgetAccess)(Object)c).mp_getAdvancement());
                if (explicit != null) { orderMap.put(c, explicit); anyExplicit = true; }
                else orderMap.put(c, i);
            }

            if (!anyExplicit) {
                for (AdvancementWidget c : children) {
                    deltaMap.put(c, parentDelta);
                    queue.add(c);
                }
                continue;
            }

            List<AdvancementWidget> sortedByOrder = new ArrayList<>(children);
            sortedByOrder.sort(Comparator.comparingInt(orderMap::get));

            int groupTop = Integer.MAX_VALUE;
            for (AdvancementWidget c : children) {
                groupTop = Math.min(groupTop, subMinY.getOrDefault(c, mp$origY.get(c)));
            }

            int cursor = groupTop;
            for (AdvancementWidget c : sortedByOrder) {
                int origY = mp$origY.get(c);
                int cMin = subMinY.getOrDefault(c, origY);
                int cMax = subMaxY.getOrDefault(c, origY + 27);
                int subTop = cMin - origY;
                int subHeight = cMax - cMin;

                int targetY = cursor - subTop;
                deltaMap.put(c, parentDelta + (targetY - origY));
                queue.add(c);

                cursor += subHeight;
            }
        }

        for (AdvancementWidget w : this.widgets.values()) {
            int d = deltaMap.getOrDefault(w, 0);
            ((IAdvancementWidgetAccess)(Object)w).mp_setY(mp$origY.get(w) + d);
        }

        int newMinY = Integer.MAX_VALUE, newMaxY = Integer.MIN_VALUE;
        for (AdvancementWidget w : this.widgets.values()) {
            if (!mp_isVisible(w)) continue;
            int y = w.getY();
            newMinY = Math.min(newMinY, y);
            newMaxY = Math.max(newMaxY, y + 27);
        }
        this.minY = newMinY;
        this.maxY = newMaxY;

        // --- X-раскладка ---
        for (AdvancementWidget w : this.widgets.values()) {
            mp$origX.computeIfAbsent(w, AdvancementWidget::getX);
        }

        Map<AdvancementWidget, Integer> newX = new IdentityHashMap<>();
        for (AdvancementWidget w : this.widgets.values()) {
            newX.put(w, mp$origX.get(w));
        }

        Map<AdvancementWidget, List<AdvancementWidget>> parentsOf = new IdentityHashMap<>();
        for (AdvancementWidget w : this.widgets.values()) {
            if (!mp_isVisible(w)) continue;
            Advancement adv = ((IAdvancementWidgetAccess)(Object)w).mp_getAdvancement();
            List<AdvancementWidget> ps = new ArrayList<>();
            for (Advancement pa : MultiParentHelper.getAllParents(adv)) {
                AdvancementWidget pw = this.widgets.get(pa);
                if (pw != null && mp_isVisible(pw) && !ps.contains(pw)) ps.add(pw);
            }
            parentsOf.put(w, ps);
        }

        boolean xChanged = true;
        int iter = 0;
        while (xChanged && iter++ < 64) {
            xChanged = false;
            for (AdvancementWidget w : this.widgets.values()) {
                if (!mp_isVisible(w)) continue;
                List<AdvancementWidget> ps = parentsOf.get(w);
                if (ps.isEmpty()) continue;

                int maxPR = 0;
                for (AdvancementWidget p : ps) {
                    maxPR = Math.max(maxPR, newX.get(p) + 28);
                }

                int req = Math.max(mp$origX.get(w), maxPR);

                if (ps.size() > 1) {
                    boolean hit = true;
                    int safety = 0;
                    while (hit && safety++ < 64) {
                        hit = false;

                        int trunkX = req - 4;
                        for (AdvancementWidget p : ps) {
                            int minTrunk = newX.get(p) + 30;
                            if (trunkX < minTrunk) trunkX = minTrunk;
                        }
                        int childCx = req + 13;
                        int childCy = w.getY() + 13;

                        int vMinY = childCy, vMaxY = childCy;
                        for (AdvancementWidget p : ps) {
                            int pCy = p.getY() + 13;
                            if (pCy < vMinY) vMinY = pCy;
                            if (pCy > vMaxY) vMaxY = pCy;
                        }

                        collision_check:
                        for (AdvancementWidget o : this.widgets.values()) {
                            if (o == w || ps.contains(o) || !mp_isVisible(o)) continue;
                            int ox = newX.getOrDefault(o, o.getX());
                            int oy = o.getY();
                            int oright = ox + 28;
                            int obottom = oy + 27;

                            if (childCy >= oy && childCy <= obottom
                                    && trunkX <= oright && childCx >= ox) {
                                req = Math.max(req, oright + 8);
                                hit = true;
                                break collision_check;
                            }
                            if (trunkX >= ox && trunkX <= oright
                                    && vMinY <= obottom && vMaxY >= oy) {
                                req = Math.max(req, oright + 8);
                                hit = true;
                                break collision_check;
                            }
                            for (AdvancementWidget p : ps) {
                                int pCx = newX.get(p) + 13;
                                int pCy = p.getY() + 13;
                                int hMinX = Math.min(pCx, trunkX);
                                int hMaxX = Math.max(pCx, trunkX);
                                if (pCy >= oy && pCy <= obottom
                                        && hMinX <= oright && hMaxX >= ox) {
                                    req = Math.max(req, oright + 8);
                                    hit = true;
                                    break collision_check;
                                }
                            }
                        }
                    }
                }

                if (newX.get(w) < req) {
                    newX.put(w, req);
                    xChanged = true;
                }
            }
        }

        for (AdvancementWidget w : this.widgets.values()) {
            if (!newX.get(w).equals(mp$origX.get(w))) {
                ((IAdvancementWidgetAccess)(Object)w).mp_setX(newX.get(w));
            }
        }

        int newMinX = Integer.MAX_VALUE, newMaxX = Integer.MIN_VALUE;
        for (AdvancementWidget w : this.widgets.values()) {
            if (!mp_isVisible(w)) continue;
            newMinX = Math.min(newMinX, w.getX());
            newMaxX = Math.max(newMaxX, w.getX() + 28);
        }
        if (newMinX != this.minX || newMaxX != this.maxX) this.centered = false;
        this.minX = newMinX;
        this.maxX = newMaxX;
    }

    @Unique
    private void mp$computeBounds(AdvancementWidget w,
                                  Map<AdvancementWidget, List<AdvancementWidget>> childrenByParent,
                                  Map<AdvancementWidget, Integer> subMinY,
                                  Map<AdvancementWidget, Integer> subMaxY) {
        if (subMinY.containsKey(w)) return;
        int origY = mp$origY.getOrDefault(w, w.getY());
        int min = origY;
        int max = origY + 27;
        subMinY.put(w, min);
        subMaxY.put(w, max);
        for (AdvancementWidget c : childrenByParent.getOrDefault(w, Collections.emptyList())) {
            mp$computeBounds(c, childrenByParent, subMinY, subMaxY);
            min = Math.min(min, subMinY.get(c));
            max = Math.max(max, subMaxY.get(c));
        }
        subMinY.put(w, min);
        subMaxY.put(w, max);
    }
}