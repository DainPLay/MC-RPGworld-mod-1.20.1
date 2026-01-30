package net.dainplay.rpgworldmod.sounds;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public class EmberScrollSound extends AbstractTickableSoundInstance {
    private final LivingEntity living;
    private final ItemStack stack;

    public EmberScrollSound(LivingEntity living, ItemStack stack, SoundEvent sound) {
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
        this.attenuation = Attenuation.LINEAR; // Важно для 3D звука
        this.relative = false; // Звук позиционный, не относительный
    }

    @Override
    public void tick() {
        if (this.living == null || !this.living.isAlive() ||
                (this.living == Minecraft.getInstance().player &&
                        (!this.living.isUsingItem() || this.living.getUseItem() != this.stack))) {
            this.stop();
            return;
        }

        // Обновляем позицию звука
        this.x = this.living.getX();
        this.y = this.living.getY();
        this.z = this.living.getZ();

        // Проверяем, находится ли игрок на расстоянии слышимости
        if (Minecraft.getInstance().player != null) {
            double distance = Minecraft.getInstance().player.distanceToSqr(this.living);
            if (distance > 64 * 64) { // 64 блока - дальность слышимости
                this.volume = 0.0F;
            } else {
                // Плавное изменение громкости в зависимости от расстояния
                this.volume = (float) Math.max(0.0F, 1.0F - (float)Math.sqrt(distance) / 64.0F);
            }
        }
    }
}