package net.dainplay.rpgworldmod.util;

import net.dainplay.rpgworldmod.RPGworldMod;
import net.dainplay.rpgworldmod.block.custom.EntFaceBlock;
import net.dainplay.rpgworldmod.network.EntFaceDestroyProgressPacket;
import net.dainplay.rpgworldmod.network.ModMessages;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = RPGworldMod.MOD_ID, value = Dist.CLIENT)
public class EntFaceBlockClientHandler {
    private static BlockPos lastAttackedBlock = null;
    private static int attackTicks = 0;
    private static final int PACKET_INTERVAL = 2;

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.player == null || minecraft.level == null) return;

            if (minecraft.options.keyAttack.isDown()) {
                HitResult hitResult = minecraft.hitResult;
                if (hitResult != null && hitResult.getType() == HitResult.Type.BLOCK) {
                    BlockHitResult blockHitResult = (BlockHitResult) hitResult;
                    BlockPos pos = blockHitResult.getBlockPos();
                    BlockState state = minecraft.level.getBlockState(pos);

                    if (state.getBlock() instanceof EntFaceBlock) {
                        if (pos.equals(lastAttackedBlock)) {
                            attackTicks++;
                            if (attackTicks >= PACKET_INTERVAL) {
                                attackTicks = 0;
                                // Отправляем пакет с предметом в руке игрока
                                ModMessages.sendToServer(new EntFaceDestroyProgressPacket(
                                        pos,
                                        true,
                                        minecraft.player.getMainHandItem() // Добавляем предмет
                                ));
                            }
                        } else {
                            lastAttackedBlock = pos;
                            attackTicks = 0;
                            // Отправляем сразу первый пакет с предметом
                            ModMessages.sendToServer(new EntFaceDestroyProgressPacket(
                                    pos,
                                    true,
                                    minecraft.player.getMainHandItem()
                            ));
                        }
                        return;
                    }
                }
            }

            lastAttackedBlock = null;
            attackTicks = 0;
        }
    }
}