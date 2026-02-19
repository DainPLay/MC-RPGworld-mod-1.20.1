package net.dainplay.rpgworldmod.sounds;

import net.dainplay.rpgworldmod.network.ClientGuardianAttackData;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;

public class GuardianAttackSoundInstance extends AbstractTickableSoundInstance {
    private final Player player;
    private final int playerId;

    public GuardianAttackSoundInstance(Player player, int playerId) {
        super(SoundEvents.GUARDIAN_ATTACK, SoundSource.PLAYERS, SoundInstance.createUnseededRandom());
        this.player = player;
        this.playerId = playerId;
        this.looping = true;
        this.delay = 0;
        this.attenuation = SoundInstance.Attenuation.NONE;
        this.relative = false;
    }

    @Override
    public void tick() {
        if (player.isRemoved() || !player.isUsingItem()) {
            stop();
            return;
        }
        ClientGuardianAttackData.AttackData data = ClientGuardianAttackData.getForPlayer(playerId);
        if (data == null || data.target == null || !data.target.isAlive()) {
            stop();
            return;
        }

        // Обновляем позицию звука (центр между игроком и целью? или у игрока)
        this.x = (float) player.getX();
        this.y = (float) player.getY();
        this.z = (float) player.getZ();

        // Громкость и высота зависят от прогресса атаки
        float progress = Math.min(1.0F, data.attackTime / 80.0F);
        this.volume = 0.0F + 1.0F * progress * progress;
        this.pitch = 0.7F + 0.5F * progress;
    }

    @Override
    public boolean canPlaySound() {
        return !player.isSilent();
    }
}