package net.dainplay.rpgworldmod.block.entity.custom;

import net.dainplay.rpgworldmod.block.custom.StareblossomPotBlock;
import net.dainplay.rpgworldmod.block.entity.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.Comparator;
import java.util.List;

public class PottedStareblossomBlockEntity extends BlockEntity {
    private int tickCounter = 0;
    private int lastSignal = 0;

    public PottedStareblossomBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.POTTED_STAREBLOSSOM_BLOCK_ENTITY.get(), pos, state);
    }

    public void serverTick() {
        if (this.level == null || this.level.isClientSide) return;

        tickCounter++;

        // Проверяем игроков каждые 10 тиков для оптимизации
        if (tickCounter % 10 == 0) {
            updateSignalAndCheckPlayers();
        }
    }

    private void updateSignalAndCheckPlayers() {
        if (this.level == null || this.level.isClientSide) return;

        List<Player> players = this.level.getEntitiesOfClass(
                Player.class,
                new AABB(
                        worldPosition.getX() - 50, worldPosition.getY() - 50, worldPosition.getZ() - 50,
                        worldPosition.getX() + 50, worldPosition.getY() + 50, worldPosition.getZ() + 50
                )
        );

        if (players.isEmpty()) {
            updateSignal(0);
            return;
        }

        // Находим ближайшего видимого игрока
        Player closestVisiblePlayer = players.stream()
                .filter(player -> canSeePlayer(player))
                .min(Comparator.comparingDouble(player ->
                        player.distanceToSqr(Vec3.atCenterOf(worldPosition))))
                .orElse(null);

        if (closestVisiblePlayer != null) {
            double distance = Math.sqrt(closestVisiblePlayer.distanceToSqr(Vec3.atCenterOf(worldPosition)));
            updateSignal(calculateSignalStrength(distance));
        } else {
            updateSignal(0);
        }
    }

    private boolean canSeePlayer(Player player) {
        if (this.level == null) return false;

        Vec3 blockCenter = Vec3.atCenterOf(this.worldPosition);
        Vec3 playerEyes = player.getEyePosition();

        blockCenter.add(0d,0.5d,0d);

        // Проверяем, нет ли блоков между цветком и игроком
        HitResult hitResult = this.level.clip(new ClipContext(
                blockCenter,
                playerEyes,
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE,
                player
        ));

        // Если луч попал в блок до достижения игрока, значит есть препятствие
        if (hitResult.getType() != HitResult.Type.MISS) {
            double distanceToHit = hitResult.getLocation().distanceTo(blockCenter);
            double distanceToPlayer = playerEyes.distanceTo(blockCenter);

            // Если попадание произошло ближе чем игрок, значит луч уперся в блок
            return distanceToHit >= distanceToPlayer - 0.5;
        }

        return true;
    }

    private int calculateSignalStrength(double distance) {
        if (distance > 50) return 0;

        // 50 блоков = сигнал 1
        // Каждые 3 блока ближе увеличивают сигнал на 1
        int signal = 1 + (int)((50 - distance) / 3.0);

        // Ограничиваем максимальным значением редстоуна (15)
        return Math.min(15, Math.max(0, signal));
    }

    private void updateSignal(int newSignal) {
        if (this.level == null || this.level.isClientSide || newSignal == lastSignal) return;

        lastSignal = newSignal;

        BlockState currentState = this.getBlockState();
        if (currentState.getBlock() instanceof StareblossomPotBlock) {
            BlockState newState = currentState.setValue(StareblossomPotBlock.SIGNAL, newSignal);
            this.level.setBlock(this.worldPosition, newState, 3);
        }
    }
}