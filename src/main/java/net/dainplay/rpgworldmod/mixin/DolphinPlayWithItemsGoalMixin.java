package net.dainplay.rpgworldmod.mixin;

import net.dainplay.rpgworldmod.effect.ParalysisEffect;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Dolphin;
import net.minecraft.world.entity.item.ItemEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Dolphin.PlayWithItemsGoal.class)
public abstract class DolphinPlayWithItemsGoalMixin {
	@Shadow
	@Final
	private Dolphin this$0;
	Dolphin dolphin = this.this$0;

	@Inject(method = "tick", at = @At(value = "HEAD"), cancellable = true)
	private void dropParalysisCheck(CallbackInfo ci) {
		if (dolphin.getAttribute(Attributes.MOVEMENT_SPEED).getModifier(ParalysisEffect.MODIFIER_UUID) != null) {
			if (!dolphin.level().getEntitiesOfClass(ItemEntity.class, dolphin.getBoundingBox().inflate(8.0D, 8.0D, 8.0D), Dolphin.ALLOWED_ITEMS).isEmpty()) {
				dolphin.getNavigation().moveTo(dolphin.level().getEntitiesOfClass(ItemEntity.class, dolphin.getBoundingBox().inflate(8.0D, 8.0D, 8.0D), Dolphin.ALLOWED_ITEMS).get(0), (double) 1.2F);
			}
			ci.cancel();
		}
	}


}
