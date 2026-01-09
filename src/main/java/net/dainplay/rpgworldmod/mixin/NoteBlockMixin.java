package net.dainplay.rpgworldmod.mixin;

import net.dainplay.rpgworldmod.block.ModBlocks;
import net.dainplay.rpgworldmod.damage.ModDamageTypes;
import net.dainplay.rpgworldmod.data.tags.ModAdvancements;
import net.dainplay.rpgworldmod.entity.custom.Mintobat;
import net.dainplay.rpgworldmod.sounds.RPGSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ambient.Bat;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.NoteBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.vibrations.VibrationSystem;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NoteBlock.class)
public class NoteBlockMixin {
    public double getDistance(Entity target, BlockPos pos2) {
        double x1 = target.getX();
        double y1 = target.getY();
        double z1 = target.getZ();

        double x2 = pos2.getX();
        double y2 = pos2.getY();
        double z2 = pos2.getZ();

        double distance = Math.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1) + (z2 - z1) * (z2 - z1));

        return distance;
    }

    @Inject(method = "playNote", at = @At(value = "HEAD"), cancellable = true)
    private void playNoteMintal(Entity pEntity, BlockState pState, Level pLevel, BlockPos pPos, CallbackInfo ci) {
        if (pLevel.getBlockState(pPos.above()).isAir() && pLevel.getBlockState(pPos.below()).getBlock() == ModBlocks.MINTAL_BLOCK.get()) {
            if (pEntity instanceof ServerPlayer serverPlayer) ModAdvancements.MINTAL_BLOCK_NOTE_TRIGGER.trigger(serverPlayer);
            pLevel.playSound(null, pPos, RPGSounds.TRIANGLE_DING.get(), SoundSource.BLOCKS, 1F, (pLevel.random.nextFloat() - pLevel.random.nextFloat()) * 0.2F + 1.0F);
            ((ServerLevel)pLevel).sendParticles(ParticleTypes.SONIC_BOOM, pPos.getX()+0.5D, pPos.getY()+1D, pPos.getZ()+0.5D, 1, 0.0D, 0.0D, 0.0D, 0.0D);
            AABB damageArea = new AABB(pPos).inflate(16D);

            pLevel.getEntities((Entity) null, damageArea, entity -> entity instanceof LivingEntity && !(entity instanceof Mintobat) && !(entity instanceof Bat))
                    .forEach(target -> {
                        Vec3 targetPos = new Vec3(target.getX(), target.getEyeY(), target.getZ());
                        if (!VibrationSystem.Listener.isOccluded(pLevel, pPos.getCenter(), targetPos)) {
                            double scaledDamage = 2D * (1 - Math.min(1, getDistance(target, pPos) / 16D));
                            if (Mintobat.isEyeInAnyFluid(target)) scaledDamage /= 4;
                            (target).hurt(ModDamageTypes.getDamageSource(pLevel, ModDamageTypes.DING), (float) scaledDamage);
                        }
                    });


            ci.cancel();
        }
    }

}