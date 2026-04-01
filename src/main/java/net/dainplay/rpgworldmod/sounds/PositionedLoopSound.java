package net.dainplay.rpgworldmod.sounds;

import net.dainplay.rpgworldmod.enchantment.ModEnchantments;
import net.dainplay.rpgworldmod.item.custom.NetherStarScrollItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class PositionedLoopSound extends AbstractTickableSoundInstance {
    private final int ownerId;
    private Vec3 position;
    private int checkCooldown = 0;
    private boolean isActive = true;          // флаг активности (управляет громкостью)
    private boolean shouldStop = false;       // флаг, что звук нужно остановить

    public PositionedLoopSound(SoundEvent sound, int ownerId, Vec3 position) {
        super(sound, SoundSource.PLAYERS, SoundInstance.createUnseededRandom());
        this.ownerId = ownerId;
        this.position = position;
        this.looping = true;
        this.delay = 0;
        this.volume = 0.1F;
        this.pitch = 1.0F;
        this.x = position.x;
        this.y = position.y;
        this.z = position.z;
        this.attenuation = Attenuation.LINEAR;
        this.relative = false;
    }

    public void setPosition(Vec3 newPos) {
        this.x = newPos.x;
        this.y = newPos.y;
        this.z = newPos.z;
        this.position = newPos;
    }

    /**
     * Устанавливает активность звука. Если false – громкость 0, иначе 0.1.
     */
    public void setActive(boolean active) {
        this.isActive = active;
        this.volume = active ? 0.1F : 0.0F;
    }

    /**
     * Вызывается извне, если нужно остановить звук (например, при удалении из менеджера).
     */
    public void markForStop() {
        this.shouldStop = true;
    }

    public void setVolume(float volume) {
        this.volume = volume;
    }

    public void setSound(Sound sound) {
        this.sound = sound;
    }

    @Override
    public void tick() {
        // Если уже помечен на остановку – завершаем
        if (shouldStop) {
            this.stop();
            return;
        }

        // Проверка каждые 5 тиков
        if (checkCooldown-- <= 0) {
            checkCooldown = 5;

            Minecraft mc = Minecraft.getInstance();
            if (mc.level == null) {
                this.stop();
                return;
            }

            Player owner = (Player) mc.level.getEntity(ownerId);
            if (owner == null || !owner.isAlive()) {
                this.stop();
                return;
            }

            ItemStack usingItem = owner.getUseItem();
            if (usingItem.getItem() instanceof NetherStarScrollItem) {
                int useTicks = owner.getTicksUsingItem();
                if (useTicks > 40 && usingItem.getEnchantmentLevel(ModEnchantments.DESTRUCTION.get()) > 0) {
                    // Всё хорошо – продолжаем
                    return;
                }
            }
            // Условие не выполнено – останавливаем
            this.stop();
        }
    }
}