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
    private static final float TIMING_SCALE = 0.95F;

    private static int scaled(int ticks) {
        return Math.max(12, Math.round(ticks * TIMING_SCALE));
    }

    public static MushroomCloudProfile forTier(NuclearTier tier) {
        int level = tier.tier();
        double yieldScale = Math.sqrt(tier.yieldKilotons());

        double stemHeight = 32.0D + yieldScale * 9.0D + tier.blastRadius() * 0.4D;
        double capRadius = 14.0D + yieldScale * 4.8D + tier.blastRadius() * 0.2D;
        int stemRiseTicks = scaled(90 + level * 22);
        int capStartTick = (int) (stemRiseTicks * 0.35F);
        int capExpandTicks = scaled(100 + level * 28);
        int rollStartTick = capStartTick + capExpandTicks / 4;
        int rollExpandTicks = scaled(110 + level * 30);
        int dissipateStartTick = rollStartTick + rollExpandTicks;
        int endTicks = dissipateStartTick + scaled(120 + level * 35);
        double shockRingMaxRadius = Math.max(tier.blastRadius() * 2.15D, tier.radiationRadius() * 1.05D);
        int shockRingEndTicks = scaled(140 + level * 24);

        return new MushroomCloudProfile(
                tier,
                stemHeight,
                4.5D + level * 1.1D,
                2.4D + level * 0.55D,
                capRadius,
                5.0D + level * 1.4D,
                shockRingMaxRadius,
                shockRingEndTicks,
                scaled(42),
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
