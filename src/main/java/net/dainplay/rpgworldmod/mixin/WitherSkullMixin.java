package net.dainplay.rpgworldmod.mixin;

import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.WitherSkull;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(WitherSkull.class)
public abstract class WitherSkullMixin {
	@Redirect(
			method = "onHitEntity",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;getDifficulty()Lnet/minecraft/world/Difficulty;")
	)
	private Difficulty redirectGetDifficulty(Level level) {
		WitherSkull skull = (WitherSkull) (Object) this;
		Entity owner = skull.getOwner();


		if (owner instanceof Player) {
			return Difficulty.NORMAL;
		}


		return level.getDifficulty();
	}
}