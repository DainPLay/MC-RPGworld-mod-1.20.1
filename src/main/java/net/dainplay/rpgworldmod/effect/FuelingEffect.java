package net.dainplay.rpgworldmod.effect;


import net.dainplay.rpgworldmod.data.tags.ModAdvancements;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;

public class FuelingEffect extends MobEffect {
	public FuelingEffect(MobEffectCategory mobEffectCategory, int color) {
		super(mobEffectCategory, color);
	}

	@Override
	public void applyEffectTick(LivingEntity entity, int amplifier) {
		Level level = entity.level();
		BlockPos pos = entity.blockPosition();
		BlockPos newpos = new BlockPos(pos.getX(), (int) entity.getEyeY(), pos.getZ());

		if ((level.isRainingAt(newpos)) || level.getFluidState(pos).getType() == Fluids.WATER || level.getFluidState(newpos).getType() == Fluids.WATER) {
			if (!EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(entity) && !entity.isPushedByFluid()) return;

			float liftAmount;
			if (entity.onGround()) liftAmount = 0.16f;
			else liftAmount = 0.08f;
			Vec3 motion = entity.getDeltaMovement();
			entity.setDeltaMovement(motion.x, motion.y + liftAmount, motion.z);
			if ((level.isRainingAt(newpos)) && !level.isClientSide && entity instanceof ServerPlayer serverPlayer)
				ModAdvancements.FLY_IN_OIL.trigger(serverPlayer);
			entity.hasImpulse = true;
			entity.resetFallDistance();
		}
	}


	public boolean isDurationEffectTick(int i, int j) {
		return true;
	}
}