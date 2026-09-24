package com.createnucleararmaments.compat;

import dev.ryanhcode.sable.companion.SableCompanion;
import dev.ryanhcode.sable.companion.SubLevelAccess;
import dev.ryanhcode.sable.companion.math.BoundingBox3d;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * Soft bridge to {@link SableCompanion} for Sable sub-level (physical structure) coordinates.
 * Companion is JiJ'd and no-ops when Sable is absent.
 */
public final class SableSpace {
    private static final double CENTER_DEDUP_SQ = 1.0E-4D;

    private SableSpace() {
    }

    public static Vec3 projectOut(Level level, Vec3 position) {
        return SableCompanion.INSTANCE.projectOutOfSubLevel(level, position);
    }

    public static boolean isInPlotGrid(Level level, Vec3 position) {
        return SableCompanion.INSTANCE.isInPlotGrid(level, position);
    }

    /**
     * True when {@code local} sits in a sub-level plot and differs from its global projection.
     */
    public static boolean hasDistinctGlobal(Level level, Vec3 local) {
        Vec3 global = projectOut(level, local);
        return global.distanceToSqr(local) > CENTER_DEDUP_SQ;
    }

    public static double distanceSquared(Level level, Vec3 a, Vec3 b) {
        return SableCompanion.INSTANCE.distanceSquaredWithSubLevels(level, a, b);
    }

    public static double distance(Level level, Vec3 a, Vec3 b) {
        return Math.sqrt(distanceSquared(level, a, b));
    }

    /**
     * Global-space direction from {@code from} toward {@code to} (either may be in a plot).
     */
    public static Vec3 directionGlobal(Level level, Vec3 from, Vec3 to) {
        return projectOut(level, to).subtract(projectOut(level, from));
    }

    /**
     * Collects every space the blast volume must run in for consistent world/structure damage:
     * <ul>
     *   <li>global (world) center</li>
     *   <li>plot-local center for every intersecting sub-level (including the origin structure)</li>
     *   <li>fallback: raw {@code origin} when it is already in a plot (covers Companion no-op / miss)</li>
     * </ul>
     * Plot centers are the global blast center inverse-transformed into each structure's pose,
     * so a block at world point W takes the same spherical falloff as a world block at W.
     */
    public static List<Vec3> blastVolumeCenters(Level level, Vec3 origin, double radius) {
        Vec3 global = projectOut(level, origin);
        List<Vec3> centers = new ArrayList<>(4);
        addUnique(centers, global);

        if (radius > 0.0D) {
            AABB worldAabb = new AABB(global, global).inflate(radius);
            BoundingBox3d query = new BoundingBox3d(worldAabb);
            for (SubLevelAccess access : SableCompanion.INSTANCE.getAllIntersecting(level, query)) {
                addUnique(centers, access.logicalPose().transformPositionInverse(global));
            }
        }

        if (hasDistinctGlobal(level, origin)) {
            addUnique(centers, origin);
        }
        return centers;
    }

    /**
     * Collects living entities within {@code radius} of {@code center} in global space,
     * querying both plot and projected AABBs so riders on structures are included.
     */
    public static void forLivingInRadius(
            ServerLevel level,
            Vec3 center,
            double radius,
            BiConsumer<LivingEntity, Double> consumer
    ) {
        if (radius <= 0.0D) {
            return;
        }
        double radiusSq = radius * radius;
        Vec3 global = projectOut(level, center);
        Map<LivingEntity, Boolean> seen = new IdentityHashMap<>();

        collect(level, new AABB(global, global).inflate(radius), seen);
        if (global.distanceToSqr(center) > CENTER_DEDUP_SQ) {
            collect(level, new AABB(center, center).inflate(radius), seen);
        }

        for (LivingEntity entity : seen.keySet()) {
            double distSq = distanceSquared(level, entity.position(), global);
            if (distSq <= radiusSq) {
                consumer.accept(entity, Math.sqrt(distSq));
            }
        }
    }

    private static void addUnique(List<Vec3> centers, Vec3 candidate) {
        for (Vec3 existing : centers) {
            if (existing.distanceToSqr(candidate) <= CENTER_DEDUP_SQ) {
                return;
            }
        }
        centers.add(candidate);
    }

    private static void collect(ServerLevel level, AABB area, Map<LivingEntity, Boolean> seen) {
        for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, area)) {
            seen.put(entity, Boolean.TRUE);
        }
    }
}
