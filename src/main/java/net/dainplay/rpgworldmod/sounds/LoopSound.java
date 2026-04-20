package net.dainplay.rpgworldmod.sounds;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public class LoopSound extends AbstractTickableSoundInstance {
	private final LivingEntity living;
	private final ItemStack stack;

	public LoopSound(LivingEntity living, ItemStack stack, SoundEvent sound) {
		super(sound, SoundSource.PLAYERS, living.getRandom());
		this.living = living;
		this.stack = stack;
		this.looping = true;
		this.delay = 0;
		this.volume = 1.0F;
		this.pitch = 1.0F;
		this.x = living.getX();
		this.y = living.getY();
		this.z = living.getZ();
		this.attenuation = Attenuation.LINEAR;
		this.relative = false;
	}

	@Override
	public void tick() {
		if (this.living == null || !this.living.isAlive() || !this.living.isUsingItem() ||
				(this.living == Minecraft.getInstance().player &&
						(!this.living.isUsingItem() || this.living.getUseItem().getItem() != this.stack.getItem()))) {
			this.stop();
			return;
		}


		this.x = this.living.getX();
		this.y = this.living.getY();
		this.z = this.living.getZ();


		if (Minecraft.getInstance().player != null) {
			double distance = Minecraft.getInstance().player.distanceToSqr(this.living);
			if (distance > 64 * 64) {
				this.volume = 0.0F;
			} else {
				this.volume = (float) Math.max(0.0F, 1.0F - (float) Math.sqrt(distance) / 64.0F);
			}
		}
	}
}