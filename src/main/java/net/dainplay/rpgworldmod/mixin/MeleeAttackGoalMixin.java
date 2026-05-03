package net.dainplay.rpgworldmod.mixin;

import net.dainplay.rpgworldmod.item.custom.DaggerItem;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MeleeAttackGoal.class)
public abstract class MeleeAttackGoalMixin {
	MeleeAttackGoal attackGoal = (MeleeAttackGoal) (Object) this;

	@Inject(method = "checkAndPerformAttack(Lnet/minecraft/world/entity/LivingEntity;D)V",
			at = @At("HEAD"), cancellable = true)
	private void onCheckAndPerformAttack(LivingEntity target, double distSqr, CallbackInfo ci) {
		PathfinderMob mob = attackGoal.mob;
		if (mob.isUsingItem() && mob.getMainHandItem().getItem() instanceof DaggerItem) {
			attackGoal.resetAttackCooldown();
			ci.cancel();
			return;
		}
		if (mob.getMainHandItem().getItem() instanceof DaggerItem) {
			Vec3 vec32 = mob.position();
			Vec3 vec3 = target.getViewVector(1.0F);
			Vec3 vec31 = vec32.vectorTo(target.position()).normalize();
			vec31 = new Vec3(vec31.x, 0.0D, vec31.z);
			boolean backstab = vec31.dot(vec3) >= 0.0D;
			if (backstab) {
				if (!mob.isUsingItem()) {
					mob.startUsingItem(InteractionHand.MAIN_HAND);
				}
				ci.cancel();
				return;
			}
		}
	}
}