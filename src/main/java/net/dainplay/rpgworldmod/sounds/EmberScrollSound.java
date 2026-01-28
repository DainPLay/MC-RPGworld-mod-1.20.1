package net.dainplay.rpgworldmod.sounds;

import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public class EmberScrollSound extends AbstractTickableSoundInstance {
    private final LivingEntity living;
    private final ItemStack stack;

    public EmberScrollSound(LivingEntity living, ItemStack stack) {
        super(RPGSounds.SPELL_DESTRUCTION_EMBER_LOOP.get(), SoundSource.PLAYERS, living.getRandom());
        this.living = living;
        this.stack = stack;
        this.looping = true;
        this.delay = 0;
        this.volume = 1.0F; // Фиксированная громкость 1
        this.pitch = 1.0F;
        this.x = living.getX();
        this.y = living.getY();
        this.z = living.getZ();
    }

    @Override
    public void tick() {
        if (!living.isAlive() || !living.isUsingItem() || living.getUseItem() != stack) {
            this.stop();
            return;
        }
        this.x = living.getX();
        this.y = living.getY();
        this.z = living.getZ();

        // Громкость всегда 1, без динамических изменений
        this.volume = 1.0F;
    }
}