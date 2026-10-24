package net.dainplay.rpgworldmod.sounds;

import net.dainplay.rpgworldmod.enchantment.ModEnchantments;
import net.dainplay.rpgworldmod.item.custom.EmberScrollItem;
import net.dainplay.rpgworldmod.item.custom.EnderEyeScrollItem;
import net.dainplay.rpgworldmod.item.custom.HeartOfTheSeaScrollItem;
import net.dainplay.rpgworldmod.item.custom.NetherStarScrollItem;
import net.dainplay.rpgworldmod.item.custom.PillagerScrollItem;
import net.dainplay.rpgworldmod.item.custom.StaffItem;
import net.dainplay.rpgworldmod.network.ClientPacketHandlers.BeamSoundManager;
import net.dainplay.rpgworldmod.network.ClientPacketHandlers.SpecialBeamSoundManager;
import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Mod.EventBusSubscriber(modid = "rpgworldmod", value = Dist.CLIENT)
public class ClientLoopSoundHandler {

    private static final int DESTRUCTION_CHARGE_TICKS = 40;
    private static final double DESTRUCTION_BEAM_RANGE = 128.0;

    private static class Active {
        LoopSound sound;         // null после перехода charge → beam
        int useTicks;
        boolean chargeOnly;      // ещё до 40 тика: играет charge-loop
        boolean beamActive;      // с 40 тика: играют beam-звуки через менеджеры
    }

    private static final Map<Integer, Active> ACTIVE = new HashMap<>();

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent e) {
        if (e.phase != TickEvent.Phase.END) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            ACTIVE.values().forEach(a -> { if (a.sound != null) a.sound.stop(); });
            ACTIVE.clear();
            return;
        }

        Set<Integer> stillUsing = new HashSet<>();

        // ---- 1. Создание/обновление charge-loop, переход на beam ----
        for (Player player : mc.level.players()) {
            if (!player.isUsingItem()) continue;
            ItemStack stack = player.getUseItem();
            if (stack.isEmpty()) continue;

            Active active = ACTIVE.get(player.getId());

            if (active == null) {
                SoundEvent sound = resolveSound(stack);
                if (sound == null) continue;

                active = new Active();
                active.sound = new LoopSound(player, stack, sound);
                active.chargeOnly = isDestructionNetherStar(stack);
                mc.getSoundManager().play(active.sound);
                ACTIVE.put(player.getId(), active);
            }

            active.useTicks++;
            stillUsing.add(player.getId());

            if (active.chargeOnly && active.useTicks >= DESTRUCTION_CHARGE_TICKS) {
                if (active.sound != null) {
                    active.sound.stop();
                    active.sound = null;
                }
                active.chargeOnly = false;
                active.beamActive = true;
            }
        }

        // ---- 2. Остановка ----
        ACTIVE.entrySet().removeIf(entry -> {
            if (stillUsing.contains(entry.getKey())) return false;
            Active a = entry.getValue();
            if (a.sound != null) a.sound.stop();
            if (a.beamActive) {
                BeamSoundManager.updateBeamSounds(entry.getKey(),
                        Vec3.ZERO, Vec3.ZERO, 0, 0, false);
                SpecialBeamSoundManager.updateSpecialSounds(entry.getKey(),
                        Collections.emptyList(), Vec3.ZERO, 0);
            }
            return true;
        });

        // ---- 3. Beam-update ----
        for (Player player : mc.level.players()) {
            Active active = ACTIVE.get(player.getId());
            if (active == null || !active.beamActive) continue;

            Vec3 eyePos = player.getEyePosition(1.0F);
            Vec3 dir = player.getLookAngle().normalize();
            Vec3 endPos = clientRaycastBeam(mc, eyePos, dir, DESTRUCTION_BEAM_RANGE);
            double length = eyePos.distanceTo(endPos);

            BeamSoundManager.updateBeamSounds(
                    player.getId(), eyePos, dir, length,
                    active.useTicks, true);

            List<Vec3> hits = collectBeamHitPositions(mc, player, eyePos, dir, length);
            SpecialBeamSoundManager.updateSpecialSounds(
                    player.getId(), hits, endPos,
                    active.useTicks);
        }
    }

    private static boolean isDestructionNetherStar(ItemStack stack) {
        return stack.getItem() instanceof NetherStarScrollItem
                && stack.getEnchantmentLevel(ModEnchantments.DESTRUCTION.get()) > 0;
    }

    private static Vec3 clientRaycastBeam(Minecraft mc, Vec3 start, Vec3 direction, double maxDist) {
        double remaining = maxDist;
        Vec3 current = start;
        while (remaining > 0.01) {
            Vec3 end = current.add(direction.scale(remaining));
            BlockHitResult hit = mc.level.clip(new ClipContext(
                    current, end,
                    ClipContext.Block.COLLIDER,
                    ClipContext.Fluid.NONE,
                    null));
            if (hit.getType() == HitResult.Type.MISS) return end;
            if (mc.level.getBlockState(hit.getBlockPos()).canOcclude()) return hit.getLocation();
            double d = current.distanceTo(hit.getLocation());
            current = hit.getLocation().add(direction.scale(0.001));
            remaining -= d;
        }
        return start;
    }

    private static List<Vec3> collectBeamHitPositions(Minecraft mc, Player owner,
                                                      Vec3 eyePos, Vec3 dir, double length) {
        List<Vec3> out = new ArrayList<>();
        Vec3 end = eyePos.add(dir.scale(length));
        AABB search = new AABB(eyePos, end).inflate(1.0);
        for (LivingEntity target : mc.level.getEntitiesOfClass(LivingEntity.class, search,
                ent -> ent != owner && ent.isAlive())) {
            Optional<Vec3> hit = target.getBoundingBox().clip(eyePos, end);
            if (hit.isPresent()) out.add(hit.get());
        }
        return out;
    }

    private static SoundEvent resolveSound(ItemStack stack) {
        SoundEvent sound = RPGSounds.STAFF_LOOP.get();

        if (stack.getItem() instanceof EmberScrollItem) {
            if (stack.getEnchantmentLevel(ModEnchantments.DESTRUCTION.get()) > 0)
                sound = RPGSounds.SPELL_DESTRUCTION_EMBER_LOOP.get();
            if (stack.getEnchantmentLevel(ModEnchantments.RESTORATION.get()) > 0)
                sound = RPGSounds.SPELL_RESTORATION_LOOP.get();
            if (stack.getEnchantmentLevel(ModEnchantments.ALTERATION.get()) > 0)
                sound = RPGSounds.SPELL_ALTERATION_LOOP.get();
            if (stack.getEnchantmentLevel(ModEnchantments.ILLUSION.get()) > 0)
                sound = RPGSounds.SPELL_ILLUSION_LOOP.get();
            if (stack.getEnchantmentLevel(ModEnchantments.NECROMANCY.get()) > 0)
                sound = RPGSounds.SPELL_NECROMANCY_LOOP.get();
        } else if (stack.getItem() instanceof HeartOfTheSeaScrollItem) {
            if (stack.getEnchantmentLevel(ModEnchantments.DESTRUCTION.get()) > 0)
                sound = RPGSounds.SPELL_DESTRUCTION_HEART_OF_THE_SEA_LOOP.get();
            if (stack.getEnchantmentLevel(ModEnchantments.RESTORATION.get()) > 0)
                sound = RPGSounds.SPELL_RESTORATION_LOOP.get();
            if (stack.getEnchantmentLevel(ModEnchantments.ALTERATION.get()) > 0)
                sound = RPGSounds.SPELL_ALTERATION_LOOP.get();
            if (stack.getEnchantmentLevel(ModEnchantments.ILLUSION.get()) > 0)
                sound = RPGSounds.SPELL_ILLUSION_LOOP.get();
            if (stack.getEnchantmentLevel(ModEnchantments.NECROMANCY.get()) > 0)
                sound = RPGSounds.SPELL_NECROMANCY_LOOP.get();
        } else if (stack.getItem() instanceof EnderEyeScrollItem) {
            if (stack.getEnchantmentLevel(ModEnchantments.DESTRUCTION.get()) > 0)
                sound = RPGSounds.SPELL_DESTRUCTION_ENDER_EYE_LOOP.get();
            if (stack.getEnchantmentLevel(ModEnchantments.RESTORATION.get()) > 0)
                sound = RPGSounds.SPELL_RESTORATION_LOOP.get();
            if (stack.getEnchantmentLevel(ModEnchantments.ALTERATION.get()) > 0)
                sound = RPGSounds.SPELL_ALTERATION_LOOP.get();
            if (stack.getEnchantmentLevel(ModEnchantments.ILLUSION.get()) > 0)
                sound = RPGSounds.SPELL_ILLUSION_LOOP.get();
            if (stack.getEnchantmentLevel(ModEnchantments.NECROMANCY.get()) > 0)
                sound = RPGSounds.SPELL_NECROMANCY_LOOP.get();
        } else if (stack.getItem() instanceof NetherStarScrollItem) {
            if (stack.getEnchantmentLevel(ModEnchantments.DESTRUCTION.get()) > 0)
                sound = RPGSounds.SPELL_DESTRUCTION_NETHER_STAR_CHARGE.get();
            if (stack.getEnchantmentLevel(ModEnchantments.RESTORATION.get()) > 0)
                sound = RPGSounds.SPELL_RESTORATION_LOOP.get();
            if (stack.getEnchantmentLevel(ModEnchantments.ALTERATION.get()) > 0)
                sound = RPGSounds.SPELL_ALTERATION_LOOP.get();
            if (stack.getEnchantmentLevel(ModEnchantments.ILLUSION.get()) > 0)
                sound = RPGSounds.SPELL_ILLUSION_LOOP.get();
            if (stack.getEnchantmentLevel(ModEnchantments.CONJURATION.get()) > 0)
                sound = RPGSounds.SPELL_CONJURATION_LOOP.get();
            if (stack.getEnchantmentLevel(ModEnchantments.NECROMANCY.get()) > 0)
                sound = RPGSounds.SPELL_NECROMANCY_LOOP.get();
        } else if (stack.getItem() instanceof PillagerScrollItem) {
            if (stack.getEnchantmentLevel(ModEnchantments.RESTORATION.get()) > 0)
                sound = RPGSounds.SPELL_RESTORATION_LOOP.get();
            if (stack.getEnchantmentLevel(ModEnchantments.ALTERATION.get()) > 0)
                sound = RPGSounds.SPELL_ALTERATION_LOOP.get();
            if (stack.getEnchantmentLevel(ModEnchantments.CONJURATION.get()) > 0)
                sound = RPGSounds.SPELL_CONJURATION_PILLAGER_PREPARE.get();
            if (stack.getEnchantmentLevel(ModEnchantments.ILLUSION.get()) > 0)
                sound = RPGSounds.SPELL_ILLUSION_PILLAGER_PREPARE.get();
        } else if (stack.getItem() instanceof StaffItem) {
            sound = RPGSounds.STAFF_LOOP.get();
        } else {
            return null;
        }

        return sound;
    }
}