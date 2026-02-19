package net.dainplay.rpgworldmod.network;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public class TargetHelper {
    public static double getMinAngleToBoundingBox(Player player, Entity entity, double maxDistance) {
        Vec3 eyePos = player.getEyePosition(1.0F);
        Vec3 lookVec = player.getViewVector(1.0F).normalize();
        AABB box = entity.getBoundingBox();
        List<Vec3> testPoints = generateTestPoints(box);
        double minAngle = 360.0;
        for (Vec3 point : testPoints) {
            Vec3 toPoint = point.subtract(eyePos);
            double dist = toPoint.length();
            if (dist > maxDistance) continue;
            double dot = lookVec.dot(toPoint.normalize());
            double angle = Math.acos(Math.max(-1.0, Math.min(1.0, dot))) * (180.0 / Math.PI);
            minAngle = Math.min(minAngle, angle);
        }
        return minAngle;
    }

    public static boolean hasLineOfSightToBoundingBox(Player player, Entity entity, Vec3 startPos, double maxDistance) {
        AABB box = entity.getBoundingBox();
        List<Vec3> points = generateTestPoints(box);
        for (Vec3 point : points) {
            if (hasLineOfSightToPoint(player.level(), startPos, point, maxDistance, player)) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasLineOfSightToPoint(Level level, Vec3 start, Vec3 end, double maxDistance, Entity entity) {
        double dist = start.distanceTo(end);
        if (dist > maxDistance) return false;
        BlockHitResult result = level.clip(new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, entity));
        return result.getType() == HitResult.Type.MISS || result.getLocation().distanceTo(start) >= dist - 0.3;
    }

    private static List<Vec3> generateTestPoints(AABB box) {
        List<Vec3> points = new ArrayList<>();
        // 8 углов
        points.add(new Vec3(box.minX, box.minY, box.minZ));
        points.add(new Vec3(box.minX, box.minY, box.maxZ));
        points.add(new Vec3(box.minX, box.maxY, box.minZ));
        points.add(new Vec3(box.minX, box.maxY, box.maxZ));
        points.add(new Vec3(box.maxX, box.minY, box.minZ));
        points.add(new Vec3(box.maxX, box.minY, box.maxZ));
        points.add(new Vec3(box.maxX, box.maxY, box.minZ));
        points.add(new Vec3(box.maxX, box.maxY, box.maxZ));
        // центр
        points.add(box.getCenter());
        return points;
    }
}