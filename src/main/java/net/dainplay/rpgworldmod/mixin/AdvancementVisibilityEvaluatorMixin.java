package net.dainplay.rpgworldmod.mixin;

import net.dainplay.rpgworldmod.util.MultiParentHelper;
import net.minecraft.advancements.Advancement;
import net.minecraft.server.advancements.AdvancementVisibilityEvaluator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

@Mixin(AdvancementVisibilityEvaluator.class)
public abstract class AdvancementVisibilityEvaluatorMixin {

    /**
     * Помечаем ВСЕ достижения видимыми на сервере. Это заставляет
     * PlayerAdvancements.flushDirty отправить их клиенту.
     * Реальная фильтрация по глубине/скрытости делается на клиенте
     * в AdvancementWidgetMixin.multiparents$isVisible().
     */
    @Inject(
            method = "evaluateVisibility(" +
                    "Lnet/minecraft/advancements/Advancement;" +
                    "Ljava/util/function/Predicate;" +
                    "Lnet/minecraft/server/advancements/AdvancementVisibilityEvaluator$Output;" +
                    ")V",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void multiparents$evaluateVisibility(
            Advancement advancement,
            Predicate<Advancement> isDone,
            AdvancementVisibilityEvaluator.Output output,
            CallbackInfo ci) {

        Advancement root = advancement.getRoot();
        if (root == null) {
            ci.cancel();
            return;
        }

        Set<Advancement> visited = new HashSet<>();
        Deque<Advancement> queue = new ArrayDeque<>();
        queue.add(root);

        while (!queue.isEmpty()) {
            Advancement a = queue.poll();
            if (!visited.add(a)) continue;
            output.accept(a, true);
            for (Advancement child : a.getChildren()) {
                queue.add(child);
            }
        }

        ci.cancel();
    }

    @Inject(
            method = "isVisible(Lnet/minecraft/advancements/Advancement;Ljava/util/function/Predicate;)Z",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private static void multiparents$isVisible(
            Advancement advancement,
            Predicate<Advancement> isDone,
            CallbackInfoReturnable<Boolean> cir) {

        if (advancement.getParent() == null) {
            cir.setReturnValue(true);
            return;
        }

        if (isDone.test(advancement)) {
            cir.setReturnValue(true);
            return;
        }

        if (advancement.getDisplay() != null && advancement.getDisplay().isHidden()) {
            cir.setReturnValue(false);
            return;
        }

        int depth = MultiParentHelper.getEffectiveShowDepth(advancement);
        boolean visible = multiparents$anyAncestorDoneWithin(advancement, isDone, depth);
        cir.setReturnValue(visible);
    }

    @Unique
    private static boolean multiparents$anyAncestorDoneWithin(
            Advancement a, Predicate<Advancement> isDone, int maxLevels) {
        if (maxLevels <= 0) return false;

        Set<Advancement> visited = new HashSet<>();
        visited.add(a);

        Set<Advancement> currentLevel = new HashSet<>();
        currentLevel.add(a);

        for (int level = 0; level < maxLevels; level++) {
            Set<Advancement> nextLevel = new HashSet<>();
            for (Advancement node : currentLevel) {
                for (Advancement parent : MultiParentHelper.getAllParents(node)) {
                    if (!visited.add(parent)) continue;
                    if (isDone.test(parent)) return true;
                    nextLevel.add(parent);
                }
            }
            if (nextLevel.isEmpty()) return false;
            currentLevel = nextLevel;
        }
        return false;
    }
}