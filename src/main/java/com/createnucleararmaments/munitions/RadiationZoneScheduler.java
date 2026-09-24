package com.createnucleararmaments.munitions;

import com.createnucleararmaments.compat.CreateNuclearBridge;
import com.createnucleararmaments.compat.SableSpace;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Server-side radiation fields without spawning visible {@link net.minecraft.world.entity.AreaEffectCloud} entities.
 */
public final class RadiationZoneScheduler {
    private static final float COVERAGE_MARGIN = 1.12F;
    private static final int REAPPLY_INTERVAL_TICKS = 20;

    private static final List<Zone> ZONES = new ArrayList<>();

    private RadiationZoneScheduler() {
    }

    public static void clearAll() {
        ZONES.clear();
    }

    public static void clearLevel(ServerLevel level) {
        ZONES.removeIf(zone -> zone.level == level);
    }

    public static void schedule(ServerLevel level, Vec3 center, NuclearTier tier) {
        float blastRadius = tier.blastRadius();
        float falloutRadius = tier.radiationRadius();
        int baseDuration = tier.radiationDurationTicks();
        long endTick = level.getGameTime() + baseDuration;
        int effectDuration = 100 + tier.radiationAmplifier() * 45;

        ZONES.add(new Zone(
                level,
                center,
                blastRadius * COVERAGE_MARGIN,
                endTick,
                tier.radiationAmplifier(),
                effectDuration
        ));

        if (falloutRadius > blastRadius + 2.0F) {
            long outerEndTick = level.getGameTime() + (long) (baseDuration * 0.8F);
            ZONES.add(new Zone(
                    level,
                    center,
                    falloutRadius,
                    outerEndTick,
                    Math.max(0, tier.radiationAmplifier() - 1),
                    effectDuration
            ));
        }
    }

    public static void tick(ServerLevel level) {
        long gameTime = level.getGameTime();
        Iterator<Zone> iterator = ZONES.iterator();
        while (iterator.hasNext()) {
            Zone zone = iterator.next();
            if (zone.level != level) {
                continue;
            }
            if (gameTime >= zone.endTick) {
                iterator.remove();
                continue;
            }
            if (gameTime % REAPPLY_INTERVAL_TICKS != 0) {
                continue;
            }
            zone.apply(level);
        }
    }

    private static final class Zone {
        private final ServerLevel level;
        private final Vec3 center;
        private final double radius;
        private final long endTick;
        private final int amplifier;
        private final int effectDuration;

        private Zone(
                ServerLevel level,
                Vec3 center,
                float radius,
                long endTick,
                int amplifier,
                int effectDuration
        ) {
            this.level = level;
            this.center = center;
            this.radius = radius;
            this.endTick = endTick;
            this.amplifier = amplifier;
            this.effectDuration = effectDuration;
        }

        private void apply(ServerLevel currentLevel) {
            SableSpace.forLivingInRadius(currentLevel, center, radius, (entity, distance) ->
                    CreateNuclearBridge.applyFalloutEffects(entity, effectDuration, amplifier, false, true, true)
            );
        }
    }
}
