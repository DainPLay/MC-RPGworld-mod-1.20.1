package net.dainplay.rpgworldmod.effect;


import net.dainplay.rpgworldmod.network.PlayerManaProvider;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

public class ParanoiaEffect extends MobEffect {
	public ParanoiaEffect(MobEffectCategory mobEffectCategory, int color) {
		super(mobEffectCategory, color);
	}

	@Override
	public void applyEffectTick(LivingEntity pLivingEntity, int pAmplifier) {
		if (pLivingEntity instanceof ServerPlayer serverPlayer) {
			if (!serverPlayer.getAbilities().instabuild) {
				serverPlayer.getCapability(PlayerManaProvider.PLAYER_MANA).ifPresent(mana -> {
					if (pLivingEntity.tickCount % Math.max(5, 20 - 10 * pAmplifier) == 0)
						mana.reduceMana(serverPlayer, 1);
					mana.setManaRegenBlocked(serverPlayer, 25);
				});
			}
		}
		super.applyEffectTick(pLivingEntity, pAmplifier);
	}

	public boolean isDurationEffectTick(int i, int j) {
		return true;
	}
}