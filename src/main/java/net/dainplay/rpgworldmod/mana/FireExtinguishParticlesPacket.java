package net.dainplay.rpgworldmod.mana;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class FireExtinguishParticlesPacket {
    private final BlockPos fireCatcherPos;
    private final BlockPos firePos;
    
    public FireExtinguishParticlesPacket(BlockPos fireCatcherPos, BlockPos firePos) {
        this.fireCatcherPos = fireCatcherPos;
        this.firePos = firePos;
    }
    
    public FireExtinguishParticlesPacket(FriendlyByteBuf buf) {
        this.fireCatcherPos = buf.readBlockPos();
        this.firePos = buf.readBlockPos();
    }
    
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeBlockPos(fireCatcherPos);
        buf.writeBlockPos(firePos);
    }
    
    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            // Клиентский код
            Level level = Minecraft.getInstance().level;
            if (level != null) {
                spawnFireParticles(level, fireCatcherPos, firePos);
            }
        });
        context.setPacketHandled(true);
        return true;
    }
    
    private void spawnFireParticles(Level level, BlockPos fireCatcherPos, BlockPos firePos) {
        // Рассчитываем вектор направления от огня к Fire Catcher
        double catcherX = fireCatcherPos.getX() + 0.5;
        double catcherY = fireCatcherPos.getY() + 0.5;
        double catcherZ = fireCatcherPos.getZ() + 0.5;
        
        double fireX = firePos.getX() + 0.5;
        double fireY = firePos.getY() + 0.5;
        double fireZ = firePos.getZ() + 0.5;
        
        double dx = catcherX - fireX;
        double dy = catcherY - fireY;
        double dz = catcherZ - fireZ;
        
        double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
        
        if (distance < 0.1) return; // Если слишком близко, не создаём частицы

        // Нормализуем вектор и рассчитываем скорость
        dx /= distance;
        dy /= distance;
        dz /= distance;

        double speed = distance / 8.5 * 15 * 0.03;
        
        // Спавним несколько частиц
        for (int i = 0; i < 3; i++) {
            double offsetX = (level.random.nextDouble() - 0.5) * 0.3;
            double offsetY = (level.random.nextDouble() - 0.5) * 0.3;
            double offsetZ = (level.random.nextDouble() - 0.5) * 0.3;
            
            double particleX = fireX + offsetX;
            double particleY = fireY + offsetY;
            double particleZ = fireZ + offsetZ;
            
            level.addParticle(ParticleTypes.FLAME,
                    particleX, particleY, particleZ,
                    dx * speed, dy * speed, dz * speed);
        }
    }
}