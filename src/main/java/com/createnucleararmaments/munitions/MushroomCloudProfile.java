package com.createnucleararmaments.munitions;

/**
 * Yield-scaled mushroom cloud geometry and timing for client-side procedural rendering.
 */
public record MushroomCloudProfile(
        NuclearTier tier,
        double stemHeight,
        double stemBaseRadius,
        double stemTopRadius,
        double capRadius,
        double rollDrop,
        double shockRingMaxRadius,
        int shockRingEndTicks,
        int fireballTicks,
        int stemRiseTicks,
        int capStartTick,
        int capExpandTicks,
        int rollStartTick,
        int rollExpandTicks,
        int dissipateStartTick,
        int endTicks
) {
    /** Client effect begins this many ticks before detonation so the shock ring meets the blast. */
    public static final int SYNC_LEAD_TICKS = 6;
    private static final float TIMING_SCALE = 0.86F;

    private static int faster(int ticks) {
        return Math.max(6, Math.round(ticks * TIMING_SCALE));
    }

    public static MushroomCloudProfile forTier(NuclearTier tier) {
        int level = tier.tier();
        double yieldScale = Math.sqrt(tier.yieldKilotons());

        double stemHeight = 12.0D + yieldScale * 3.0D;
        double capRadius = 5.0D + yieldScale * 2.5D;
        int stemRiseTicks = faster(80 + level * 20);
        int capStartTick = (int) (stemRiseTicks * 0.22F);
        int capExpandTicks = faster(100 + level * 28);
        int rollStartTick = capStartTick + capExpandTicks / 4;
        int rollExpandTicks = faster(110 + level * 32);
        int dissipateStartTick = rollStartTick + rollExpandTicks;
        int endTicks = dissipateStartTick + faster(100 + level * 30);
        double shockRingMaxRadius = tier.radiationRadius() * 1.22D;

        return new MushroomCloudProfile(
                tier,
                stemHeight,
                2.0D + level * 0.35D,
                1.1D + level * 0.2D,
                capRadius,
                2.5D + level * 0.7D,
                shockRingMaxRadius,
                endTicks,
                faster(32),
                stemRiseTicks,
                capStartTick,
                capExpandTicks,
                rollStartTick,
                rollExpandTicks,
                dissipateStartTick,
                endTicks
        );
    }
}
