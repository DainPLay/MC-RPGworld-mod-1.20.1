package net.dainplay.rpgworldmod.mixin.compat.betteradvancements;

import betteradvancements.common.gui.BetterAdvancementTab;
import betteradvancements.common.gui.BetterAdvancementWidget;
import net.dainplay.rpgworldmod.util.IBetterAdvancementWidgetAccess;
import net.dainplay.rpgworldmod.util.IAdvancementTabAccess;
import net.dainplay.rpgworldmod.util.MultiParentHelper;
import net.minecraft.advancements.Advancement;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.*;

@Pseudo
@Mixin(value = BetterAdvancementTab.class, remap = false)
public abstract class BetterAdvancementTabMixin implements IAdvancementTabAccess {
    @Shadow @Final protected Map<Advancement, BetterAdvancementWidget> widgets;
    @Shadow private int minY;
    @Shadow private int maxY;
    @Shadow private int minX;
    @Shadow private int maxX;
    @Shadow private boolean centered;

    @Unique private final Map<BetterAdvancementWidget, Integer> mp$origY = new IdentityHashMap<>();
    @Unique private final Map<BetterAdvancementWidget, Integer> mp$origX = new IdentityHashMap<>();
    @Unique private boolean mp$dirty = true;

    @Override
    public void mp_markDirty() {
        this.mp$dirty = true;
        this.mp$origX.clear();   // ← добавить
        this.mp$origY.clear();   // ← добавить
    }

    @Unique
    private boolean mp_isVisible(BetterAdvancementWidget w) {
        return ((IBetterAdvancementWidgetAccess)(Object)w).mp_isVisible();
    }

    @Inject(method = "addAdvancement", at = @At("TAIL"))
    private void mp$markDirty(Advancement adv, CallbackInfo ci) {
        mp$dirty = true;
    }

    @Inject(method = "drawContents", at = @At("HEAD"))
    private void mp$ensureAttachedAndRelayout(GuiGraphics g, int pX, int pY, int pWidth, int pHeight, CallbackInfo ci) {
        for (BetterAdvancementWidget w : this.widgets.values()) w.attachToParent();
        if (mp$dirty) {
            mp$dirty = false;
            mp$relayout();
        }
    }

    @Redirect(
            method = "drawContents",
            at = @At(value = "INVOKE",
                    target = "Lbetteradvancements/common/gui/BetterAdvancementWidget;drawConnectivity(Lnet/minecraft/client/gui/GuiGraphics;IIZ)V")
    )
    private void mp$drawConnectivityAll(BetterAdvancementWidget rootWidget, GuiGraphics g, int x, int y, boolean shadow) {
        MultiParentHelper.CONN.get().clear();
        for (BetterAdvancementWidget w : this.widgets.values()) {
            w.drawConnectivity(g, x, y, shadow);
        }
    }

    @Redirect(
            method = "drawContents",
            at = @At(value = "INVOKE",
                    target = "Lbetteradvancements/common/gui/BetterAdvancementWidget;draw(Lnet/minecraft/client/gui/GuiGraphics;II)V")
    )
    private void mp$drawAll(BetterAdvancementWidget rootWidget, GuiGraphics g, int x, int y) {
        MultiParentHelper.DRAWN.get().clear();
        for (BetterAdvancementWidget w : this.widgets.values()) w.draw(g, x, y);
    }

    @Unique
    private void mp$relayout() {
        if (this.widgets.isEmpty()) return;

        for (BetterAdvancementWidget w : this.widgets.values()) {
            mp$origY.computeIfAbsent(w, BetterAdvancementWidget::getY);
        }

        Map<BetterAdvancementWidget, List<BetterAdvancementWidget>> childrenByParent = new HashMap<>();
        List<BetterAdvancementWidget> roots = new ArrayList<>();
        for (BetterAdvancementWidget w : this.widgets.values()) {
            if (!mp_isVisible(w)) continue;
            BetterAdvancementWidget p = ((IBetterAdvancementWidgetAccess)(Object)w).mp_getParent();
            if (p == null || !mp_isVisible(p)) roots.add(w);
            else childrenByParent.computeIfAbsent(p, k -> new ArrayList<>()).add(w);
        }

        Map<BetterAdvancementWidget, Integer> subMinY = new HashMap<>();
        Map<BetterAdvancementWidget, Integer> subMaxY = new HashMap<>();
        for (BetterAdvancementWidget r : roots) {
            mp$computeBounds(r, childrenByParent, subMinY, subMaxY);
        }

        // --- Y-раскладка ---
        Map<BetterAdvancementWidget, Integer> deltaMap = new HashMap<>();
        Set<BetterAdvancementWidget> visited = new HashSet<>();
        Deque<BetterAdvancementWidget> queue = new ArrayDeque<>();
        for (BetterAdvancementWidget r : roots) { deltaMap.put(r, 0); queue.add(r); }

        while (!queue.isEmpty()) {
            BetterAdvancementWidget parent = queue.poll();
            if (!visited.add(parent)) continue;
            int parentDelta = deltaMap.getOrDefault(parent, 0);

            List<BetterAdvancementWidget> children = childrenByParent.getOrDefault(parent, Collections.emptyList());
            if (children.isEmpty()) continue;

            List<BetterAdvancementWidget> sortedByY = new ArrayList<>(children);
            sortedByY.sort(Comparator.comparingInt(mp$origY::get));

            Map<BetterAdvancementWidget, Integer> orderMap = new HashMap<>();
            boolean anyExplicit = false;
            for (int i = 0; i < sortedByY.size(); i++) {
                BetterAdvancementWidget c = sortedByY.get(i);
                Integer explicit = MultiParentHelper.getExplicitDisplayOrder(
                        ((IBetterAdvancementWidgetAccess)(Object)c).mp_getAdvancement());
                if (explicit != null) { orderMap.put(c, explicit); anyExplicit = true; }
                else orderMap.put(c, i);
            }

            if (!anyExplicit) {
                for (BetterAdvancementWidget c : children) {
                    deltaMap.put(c, parentDelta);
                    queue.add(c);
                }
                continue;
            }

            List<BetterAdvancementWidget> sortedByOrder = new ArrayList<>(children);
            sortedByOrder.sort(Comparator.comparingInt(orderMap::get));

            int groupTop = Integer.MAX_VALUE;
            for (BetterAdvancementWidget c : children) {
                groupTop = Math.min(groupTop, subMinY.getOrDefault(c, mp$origY.get(c)));
            }

            int cursor = groupTop;
            for (BetterAdvancementWidget c : sortedByOrder) {
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

        for (BetterAdvancementWidget w : this.widgets.values()) {
            int d = deltaMap.getOrDefault(w, 0);
            ((IBetterAdvancementWidgetAccess)(Object)w).mp_setY(mp$origY.get(w) + d);
        }

        int newMinY = Integer.MAX_VALUE, newMaxY = Integer.MIN_VALUE;
        for (BetterAdvancementWidget w : this.widgets.values()) {
            if (!mp_isVisible(w)) continue;
            int y = w.getY();
            newMinY = Math.min(newMinY, y);
            newMaxY = Math.max(newMaxY, y + 27);
        }
        this.minY = newMinY;
        this.maxY = newMaxY;


        // --- X-раскладка ---
        for (BetterAdvancementWidget w : this.widgets.values()) {
            mp$origX.computeIfAbsent(w, BetterAdvancementWidget::getX);
        }

        Map<BetterAdvancementWidget, Integer> newX = new IdentityHashMap<>();
        for (BetterAdvancementWidget w : this.widgets.values()) {
            newX.put(w, mp$origX.get(w));
        }

        Map<BetterAdvancementWidget, List<BetterAdvancementWidget>> parentsOf = new IdentityHashMap<>();
        for (BetterAdvancementWidget w : this.widgets.values()) {
            if (!mp_isVisible(w)) continue;
            Advancement adv = ((IBetterAdvancementWidgetAccess)(Object)w).mp_getAdvancement();
            List<BetterAdvancementWidget> ps = new ArrayList<>();
            for (Advancement pa : MultiParentHelper.getAllParents(adv)) {
                BetterAdvancementWidget pw = this.widgets.get(pa);
                if (pw != null && mp_isVisible(pw) && !ps.contains(pw)) ps.add(pw);
            }
            parentsOf.put(w, ps);
        }

        boolean xChanged = true;
        int iter = 0;
        while (xChanged && iter++ < 64) {
            xChanged = false;
            for (BetterAdvancementWidget w : this.widgets.values()) {
                if (!mp_isVisible(w)) continue;
                List<BetterAdvancementWidget> ps = parentsOf.get(w);
                if (ps.isEmpty()) continue;

                int maxPR = 0;
                for (BetterAdvancementWidget p : ps) {
                    maxPR = Math.max(maxPR, newX.get(p) + 32);   // BA step
                }

                int req = Math.max(mp$origX.get(w), maxPR);

                if (ps.size() > 1) {
                    boolean hit = true;
                    int safety = 0;
                    while (hit && safety++ < 64) {
                        hit = false;

                        int trunkX = req - 4;
                        for (BetterAdvancementWidget p : ps) {
                            int minTrunk = newX.get(p) + 32;   // BA step + 2
                            if (trunkX < minTrunk) trunkX = minTrunk;
                        }
                        int childCx = req + 13;
                        int childCy = w.getY() + 13;

                        int vMinY = childCy, vMaxY = childCy;
                        for (BetterAdvancementWidget p : ps) {
                            int pCy = p.getY() + 13;
                            if (pCy < vMinY) vMinY = pCy;
                            if (pCy > vMaxY) vMaxY = pCy;
                        }

                        collision_check:
                        for (BetterAdvancementWidget o : this.widgets.values()) {
                            if (o == w || ps.contains(o) || !mp_isVisible(o)) continue;
                            int ox = newX.getOrDefault(o, o.getX());
                            int oy = o.getY();
                            int oright = ox + 32;
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
                            for (BetterAdvancementWidget p : ps) {
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

        for (BetterAdvancementWidget w : this.widgets.values()) {
            int targetX = newX.get(w);
            if (w.getX() != targetX) {
                ((IBetterAdvancementWidgetAccess)(Object)w).mp_setX(targetX);
            }
        }

        int newMinX = Integer.MAX_VALUE, newMaxX = Integer.MIN_VALUE;
        for (BetterAdvancementWidget w : this.widgets.values()) {
            if (!mp_isVisible(w)) continue;
            newMinX = Math.min(newMinX, w.getX());
            newMaxX = Math.max(newMaxX, w.getX() + 28);
        }
        if (newMinX != this.minX || newMaxX != this.maxX) this.centered = false;
        this.minX = newMinX;
        this.maxX = newMaxX;
    }

    @Unique
    private void mp$computeBounds(BetterAdvancementWidget w,
                                  Map<BetterAdvancementWidget, List<BetterAdvancementWidget>> childrenByParent,
                                  Map<BetterAdvancementWidget, Integer> subMinY,
                                  Map<BetterAdvancementWidget, Integer> subMaxY) {
        if (subMinY.containsKey(w)) return;
        int origY = mp$origY.getOrDefault(w, w.getY());
        int min = origY;
        int max = origY + 27;
        subMinY.put(w, min);
        subMaxY.put(w, max);
        for (BetterAdvancementWidget c : childrenByParent.getOrDefault(w, Collections.emptyList())) {
            mp$computeBounds(c, childrenByParent, subMinY, subMaxY);
            min = Math.min(min, subMinY.get(c));
            max = Math.max(max, subMaxY.get(c));
        }
        subMinY.put(w, min);
        subMaxY.put(w, max);
    }
}